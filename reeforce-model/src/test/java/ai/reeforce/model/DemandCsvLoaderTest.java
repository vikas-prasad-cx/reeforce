package ai.reeforce.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DemandCsvLoaderTest {

    @Test
    void parsesDemoCsv() {
        String csv = """
                start_iso,end_iso,offered_volume,aht_seconds
                2026-07-22T13:00:00Z,2026-07-22T13:15:00Z,25,300
                2026-07-22T13:15:00Z,2026-07-22T13:30:00Z,28,300
                """;
        DemandSeries series = DemandCsvLoader.load("voice", "inbound", csv);
        assertEquals(2, series.points().size());
        assertEquals(25.0, series.points().getFirst().offeredVolume());
    }

    @Test
    void rejectsInvertedInterval() {
        assertThrows(IllegalArgumentException.class, () ->
                new TimeInterval(
                        java.time.Instant.parse("2026-07-22T14:15:00Z"),
                        java.time.Instant.parse("2026-07-22T14:00:00Z")
                ));
    }
}
