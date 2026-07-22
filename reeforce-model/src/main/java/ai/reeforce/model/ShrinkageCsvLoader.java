package ai.reeforce.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * CSV loader for shrinkage calendars.
 *
 * <p>Header (column order flexible by name):
 * {@code start_iso,end_iso,shrinkage_fraction,headcount_offline,reason,skill,site,org_unit}
 *
 * <p>Provide {@code shrinkage_fraction} and/or {@code headcount_offline}. Empty optional
 * filter columns mean "applies to all".
 */
public final class ShrinkageCsvLoader {

    private ShrinkageCsvLoader() {
    }

    public static ShrinkageCalendar load(String csv) {
        String[] lines = csv.strip().split("\\R");
        if (lines.length == 0) {
            return new ShrinkageCalendar(List.of());
        }
        String[] header = split(lines[0]);
        if (header.length == 0 || !header[0].toLowerCase(Locale.ROOT).startsWith("start")) {
            throw new IllegalArgumentException("Expected header starting with start_iso");
        }
        ColumnIndex idx = ColumnIndex.from(header);
        List<ShrinkageCalendar.ShrinkageEntry> entries = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = split(line);
            Instant start = Instant.parse(required(parts, idx.start, i));
            Instant end = Instant.parse(required(parts, idx.end, i));
            Optional<Double> fraction = optionalDouble(parts, idx.fraction);
            Optional<Double> headcount = optionalDouble(parts, idx.headcount);
            String reason = optionalString(parts, idx.reason).orElse("");
            entries.add(new ShrinkageCalendar.ShrinkageEntry(
                    new TimeInterval(start, end),
                    fraction,
                    headcount,
                    reason,
                    optionalString(parts, idx.skill),
                    optionalString(parts, idx.site),
                    optionalString(parts, idx.orgUnit)
            ));
        }
        return new ShrinkageCalendar(entries);
    }

    private static String[] split(String line) {
        return line.split(",", -1);
    }

    private static String required(String[] parts, int index, int lineNo) {
        if (index < 0 || index >= parts.length || parts[index].isBlank()) {
            throw new IllegalArgumentException("Missing required column at line " + (lineNo + 1));
        }
        return parts[index].trim();
    }

    private static Optional<Double> optionalDouble(String[] parts, int index) {
        return optionalString(parts, index).map(Double::parseDouble);
    }

    private static Optional<String> optionalString(String[] parts, int index) {
        if (index < 0 || index >= parts.length) {
            return Optional.empty();
        }
        String v = parts[index].trim();
        return v.isEmpty() ? Optional.empty() : Optional.of(v);
    }

    private record ColumnIndex(
            int start,
            int end,
            int fraction,
            int headcount,
            int reason,
            int skill,
            int site,
            int orgUnit
    ) {
        static ColumnIndex from(String[] header) {
            return new ColumnIndex(
                    find(header, "start_iso"),
                    find(header, "end_iso"),
                    find(header, "shrinkage_fraction"),
                    find(header, "headcount_offline"),
                    find(header, "reason"),
                    find(header, "skill"),
                    find(header, "site"),
                    find(header, "org_unit")
            );
        }

        private static int find(String[] header, String name) {
            for (int i = 0; i < header.length; i++) {
                if (header[i].trim().equalsIgnoreCase(name)) {
                    return i;
                }
            }
            return -1;
        }
    }
}
