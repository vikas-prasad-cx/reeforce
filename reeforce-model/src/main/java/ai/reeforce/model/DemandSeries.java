package ai.reeforce.model;

import java.util.List;
import java.util.Objects;

/**
 * Forecast or observed demand indexed by planning intervals.
 */
public record DemandSeries(String skill, String channel, List<DemandPoint> points) {

    public DemandSeries {
        Objects.requireNonNull(skill, "skill");
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(points, "points");
        points = List.copyOf(points);
    }

    public record DemandPoint(TimeInterval interval, double offeredVolume, double ahtSeconds) {
        public DemandPoint {
            Objects.requireNonNull(interval, "interval");
            if (offeredVolume < 0) {
                throw new IllegalArgumentException("offeredVolume must be >= 0");
            }
            if (ahtSeconds <= 0) {
                throw new IllegalArgumentException("ahtSeconds must be > 0");
            }
        }
    }
}
