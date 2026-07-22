package ai.reeforce.model;

import java.util.List;
import java.util.Objects;

/**
 * Per-interval gap between required and available capacity.
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

        public boolean understaffed() {
            return gap > 0;
        }
    }
}
