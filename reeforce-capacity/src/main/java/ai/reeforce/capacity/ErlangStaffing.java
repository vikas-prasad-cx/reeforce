package ai.reeforce.capacity;

/**
 * Erlang-C / Erlang-A staffing estimator for voice intervals.
 *
 * <p>Primary path is classic Erlang-C (M/M/n, infinite patience): find the minimum
 * agent count {@code n > a} such that service level {@code P(W ≤ T) ≥ target}.
 * Optional average patience enables an Erlang-A (M/M/n+M) approximation that
 * accounts for abandonments reducing queue pressure.
 *
 * <p>Sources and golden-vector notes: {@code docs/erlang-a.md}.
 */
public final class ErlangStaffing {

    /** Default mean patience when using the Erlang-A overload without an explicit value. */
    public static final double DEFAULT_PATIENCE_SECONDS = 180.0;

    private static final int MAX_AGENTS = 10_000;

    private ErlangStaffing() {
    }

    /**
     * Required staff under Erlang-C (infinite patience).
     *
     * @param offeredVolume contacts offered in the interval
     * @param ahtSeconds average handle time in seconds
     * @param intervalSeconds length of the planning interval in seconds
     * @param serviceLevel target answer fraction in [0,1]
     * @param targetAnswerSeconds ASA / service-time target T
     * @return minimum agents (integer-valued double) meeting the SL target
     */
    public static double requiredStaff(
            double offeredVolume,
            double ahtSeconds,
            long intervalSeconds,
            double serviceLevel,
            double targetAnswerSeconds
    ) {
        return requiredStaff(offeredVolume, ahtSeconds, intervalSeconds, serviceLevel, targetAnswerSeconds,
                Double.POSITIVE_INFINITY);
    }

    /**
     * Required staff under Erlang-A when {@code averagePatienceSeconds} is finite,
     * otherwise Erlang-C.
     *
     * @param averagePatienceSeconds mean time-to-abandon (seconds); use
     *                               {@link Double#POSITIVE_INFINITY} for Erlang-C
     */
    public static double requiredStaff(
            double offeredVolume,
            double ahtSeconds,
            long intervalSeconds,
            double serviceLevel,
            double targetAnswerSeconds,
            double averagePatienceSeconds
    ) {
        validate(offeredVolume, ahtSeconds, intervalSeconds, serviceLevel, targetAnswerSeconds, averagePatienceSeconds);
        if (offeredVolume == 0) {
            return 0.0;
        }
        double trafficIntensity = trafficIntensity(offeredVolume, ahtSeconds, intervalSeconds);
        int minAgents = Math.max(1, (int) Math.floor(trafficIntensity) + 1);
        for (int n = minAgents; n <= MAX_AGENTS; n++) {
            double sl = serviceLevel(n, trafficIntensity, ahtSeconds, targetAnswerSeconds, averagePatienceSeconds);
            if (sl + 1e-12 >= serviceLevel) {
                return n;
            }
        }
        throw new IllegalStateException("Unable to meet service level within " + MAX_AGENTS + " agents");
    }

    /** Offered load in Erlangs: {@code a = volume * AHT / interval}. */
    public static double trafficIntensity(double offeredVolume, double ahtSeconds, long intervalSeconds) {
        return (offeredVolume * ahtSeconds) / intervalSeconds;
    }

    /**
     * Service level P(answered within T) for {@code n} agents.
     * Infinite patience → Erlang-C; finite patience → Erlang-A approximation.
     */
    public static double serviceLevel(
            int agents,
            double trafficIntensity,
            double ahtSeconds,
            double targetAnswerSeconds,
            double averagePatienceSeconds
    ) {
        if (agents <= 0) {
            return 0.0;
        }
        if (trafficIntensity <= 0) {
            return 1.0;
        }
        if (agents <= trafficIntensity) {
            return 0.0;
        }
        double mu = 1.0 / ahtSeconds;
        if (Double.isInfinite(averagePatienceSeconds) || averagePatienceSeconds <= 0) {
            double pWait = erlangC(agents, trafficIntensity);
            return 1.0 - pWait * Math.exp(-(agents - trafficIntensity) * mu * targetAnswerSeconds);
        }
        // Erlang-A (M/M/n+M): exponential patience rate θ = 1/patience.
        double theta = 1.0 / averagePatienceSeconds;
        double pWait = erlangADelayProbability(agents, trafficIntensity, mu, theta);
        // Answered within T among those who do not abandon before T (industry approx).
        double survival = Math.exp(-theta * targetAnswerSeconds);
        double serviceRateExcess = agents * mu + theta - trafficIntensity * mu;
        // Protect numerical edge cases near stability boundary.
        if (serviceRateExcess <= 1e-12) {
            return Math.max(0.0, 1.0 - pWait);
        }
        double delayBeyondT = pWait * Math.exp(-serviceRateExcess * targetAnswerSeconds) * survival;
        return Math.clamp(1.0 - delayBeyondT, 0.0, 1.0);
    }

    /**
     * Numerically stable Erlang-C probability of delay C(n, a).
     * Uses the recursive form recommended for WFM calculators.
     */
    public static double erlangC(int agents, double trafficIntensity) {
        if (agents <= 0) {
            return 1.0;
        }
        if (trafficIntensity <= 0) {
            return 0.0;
        }
        if (agents <= trafficIntensity) {
            return 1.0;
        }
        // Recursive computation of the normalizing constant (Deslauriers / Koole style).
        double b = 1.0;
        for (int k = 1; k <= agents; k++) {
            b = 1.0 + (k / trafficIntensity) * b;
        }
        return 1.0 / (1.0 + (agents - trafficIntensity) / trafficIntensity * b);
    }

    /**
     * Probability an arrival must wait in M/M/n+M (Erlang-A), exponential patience.
     * Recursive algorithm adapted from Palm / Garnett–Mandelbaum–Reiman style ratios.
     */
    static double erlangADelayProbability(int agents, double trafficIntensity, double mu, double theta) {
        double lambda = trafficIntensity * mu;
        // Build successive state ratios; start from empty system probability mass.
        double p0Inverse = 1.0;
        double term = 1.0;
        for (int j = 1; j <= agents; j++) {
            term *= lambda / (j * mu);
            p0Inverse += term;
        }
        // Queued states j = n+1, n+2, ... truncated when mass becomes negligible.
        double queueMass = 0.0;
        double last = term; // P(n) / P(0) contribution before normalizing
        for (int k = 1; k <= 10_000; k++) {
            double abandonPlusService = agents * mu + k * theta;
            last *= lambda / abandonPlusService;
            p0Inverse += last;
            queueMass += last;
            if (last < 1e-15 * p0Inverse) {
                break;
            }
        }
        double p0 = 1.0 / p0Inverse;
        double pWait = (term + queueMass) * p0; // wait when all n busy (state >= n)
        // More precisely: delay iff state >= n at arrival → mass of states n, n+1, ...
        // term is P(n)/P(0); queueMass is sum_{k>=1} P(n+k)/P(0)
        return Math.clamp(pWait, 0.0, 1.0);
    }

    private static void validate(
            double offeredVolume,
            double ahtSeconds,
            long intervalSeconds,
            double serviceLevel,
            double targetAnswerSeconds,
            double averagePatienceSeconds
    ) {
        if (offeredVolume < 0 || ahtSeconds <= 0 || intervalSeconds <= 0) {
            throw new IllegalArgumentException("invalid traffic inputs");
        }
        if (serviceLevel < 0 || serviceLevel > 1 || targetAnswerSeconds < 0) {
            throw new IllegalArgumentException("invalid service targets");
        }
        if (averagePatienceSeconds <= 0 && !Double.isInfinite(averagePatienceSeconds)) {
            throw new IllegalArgumentException("averagePatienceSeconds must be > 0 or +∞");
        }
    }
}
