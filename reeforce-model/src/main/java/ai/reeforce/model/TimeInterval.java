package ai.reeforce.model;

import java.time.Instant;
import java.util.Objects;

/**
 * A fixed-length planning interval (typically 15 or 30 minutes).
 */
public record TimeInterval(Instant start, Instant end) {

    public TimeInterval {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end must be after start");
        }
    }

    public long durationSeconds() {
        return end.getEpochSecond() - start.getEpochSecond();
    }
}
