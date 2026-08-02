package ai.reeforce.model;

import java.util.List;
import java.util.Objects;

/**
 * Per-interval gap between required and available capacity.
 *
 * <p>Field names {@code requiredStaff}/{@code availableStaff} are historical
 * (contact-center origin). Prefer {@link GapRow#required()} / {@link GapRow#available()}
 * for domain-neutral code — units may be agents, seats, slots, or vehicles.
 */
public record GapBoard(List<GapRow> rows) {

    public GapBoard {
        Objects.requireNonNull(rows, "rows");
        rows = List.copyOf(rows);
    }

    public record GapRow(
            TimeInterval interval,
            double requiredStaff,
            double availableStaff,
            double gap
    ) {
        public GapRow {
            Objects.requireNonNull(interval, "interval");
        }

        /** Domain-neutral alias for {@link #requiredStaff}. */
        public double required() {
            return requiredStaff;
        }

        /** Domain-neutral alias for {@link #availableStaff}. */
        public double available() {
            return availableStaff;
        }

        public boolean understaffed() {
            return gap > 0;
        }
    }
}
