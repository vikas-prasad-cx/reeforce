package ai.reeforce.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal CSV loader for demo demand files.
 * Expected header: start_iso,end_iso,offered_volume,aht_seconds
 */
public final class DemandCsvLoader {

    private DemandCsvLoader() {
    }

    public static DemandSeries load(String skill, String channel, String csv) {
        List<DemandSeries.DemandPoint> points = new ArrayList<>();
        String[] lines = csv.strip().split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#") || (i == 0 && line.toLowerCase().startsWith("start_"))) {
                continue;
            }
            String[] parts = line.split(",", -1);
            if (parts.length < 4) {
                throw new IllegalArgumentException("Expected 4 columns at line " + (i + 1));
            }
            Instant start = Instant.parse(parts[0].trim());
            Instant end = Instant.parse(parts[1].trim());
            double volume = Double.parseDouble(parts[2].trim());
            double aht = Double.parseDouble(parts[3].trim());
            points.add(new DemandSeries.DemandPoint(new TimeInterval(start, end), volume, aht));
        }
        return new DemandSeries(skill, channel, points);
    }

    /** Helper for tests: build N contiguous 15-minute intervals from a start instant. */
    public static List<TimeInterval> intervals(Instant start, int count) {
        List<TimeInterval> out = new ArrayList<>(count);
        Instant cursor = start;
        for (int i = 0; i < count; i++) {
            Instant next = cursor.plus(15, ChronoUnit.MINUTES);
            out.add(new TimeInterval(cursor, next));
            cursor = next;
        }
        return out;
    }
}
