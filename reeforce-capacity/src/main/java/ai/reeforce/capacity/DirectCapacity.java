package ai.reeforce.capacity;

/**
 * Domain-neutral required capacity: demand volume maps 1:1 to required units.
 *
 * <p>Used for school conference slots, class seats, and corridor vehicles where
 * Erlang queueing does not apply. {@code ahtSeconds} is accepted for CSV
 * compatibility but ignored.
 */
public final class DirectCapacity {

    private DirectCapacity() {
    }

    /**
     * @param offeredVolume appointments, seat requests, or vehicles in the interval
     * @return required capacity units (same numeric value; non-negative)
     */
    public static double requiredCapacity(double offeredVolume) {
        if (offeredVolume < 0) {
            throw new IllegalArgumentException("offeredVolume must be >= 0");
        }
        return offeredVolume;
    }
}
