package ai.reeforce.capacity;

/**
 * Minimal Erlang-C / Erlang-A style staffing estimator.
 * MVP stub: converts offered load (Erlangs) to required agents with a simple
 * square-root staffing rule as a placeholder until full Erlang-A lands.
 */
public final class ErlangStaffing {

    private ErlangStaffing() {
    }

    /**
     * @param offeredVolume contacts offered in the interval
     * @param ahtSeconds average handle time in seconds
     * @param intervalSeconds length of the planning interval in seconds
     * @param serviceLevel target answer fraction in [0,1] (reserved for full model)
     * @param targetAnswerSeconds ASA / service-time target (reserved for full model)
     * @return estimated required staff (may be fractional before rounding policy)
     */
    public static double requiredStaff(
            double offeredVolume,
            double ahtSeconds,
            long intervalSeconds,
            double serviceLevel,
            double targetAnswerSeconds
    ) {
        if (offeredVolume < 0 || ahtSeconds <= 0 || intervalSeconds <= 0) {
            throw new IllegalArgumentException("invalid traffic inputs");
        }
        if (serviceLevel < 0 || serviceLevel > 1 || targetAnswerSeconds < 0) {
            throw new IllegalArgumentException("invalid service targets");
        }
        double trafficIntensity = (offeredVolume * ahtSeconds) / intervalSeconds;
        // Square-root staffing placeholder: N ≈ a + z*sqrt(a)
        double z = serviceLevelToZ(serviceLevel);
        return trafficIntensity + z * Math.sqrt(Math.max(trafficIntensity, 0.0));
    }

    static double serviceLevelToZ(double serviceLevel) {
        if (serviceLevel >= 0.95) {
            return 1.65;
        }
        if (serviceLevel >= 0.90) {
            return 1.28;
        }
        if (serviceLevel >= 0.80) {
            return 0.84;
        }
        return 0.5;
    }
}
