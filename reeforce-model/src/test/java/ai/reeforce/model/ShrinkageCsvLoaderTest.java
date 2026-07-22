package ai.reeforce.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShrinkageCsvLoaderTest {

    @Test
    void parsesHappyPathExample() {
        String csv = """
                start_iso,end_iso,shrinkage_fraction,headcount_offline,reason,skill,site,org_unit
                2026-07-22T14:00:00Z,2026-07-22T15:00:00Z,0.10,,team_meeting,voice,site-a,ops
                2026-07-22T16:00:00Z,2026-07-22T16:30:00Z,,2,coaching,,,
                """;
        ShrinkageCalendar calendar = ShrinkageCsvLoader.load(csv);
        assertEquals(2, calendar.entries().size());
        ShrinkageCalendar.ShrinkageEntry first = calendar.entries().getFirst();
        assertEquals(Optional.of(0.10), first.shrinkageFraction());
        assertEquals("team_meeting", first.reason());
        assertEquals(Optional.of("voice"), first.skill());

        double fraction = calendar.shrinkageFraction(
                new TimeInterval(
                        java.time.Instant.parse("2026-07-22T14:00:00Z"),
                        java.time.Instant.parse("2026-07-22T14:15:00Z")
                ),
                Optional.of("voice"),
                Optional.of("site-a"),
                10
        );
        assertEquals(0.10, fraction, 1e-9);

        double coaching = calendar.shrinkageFraction(
                new TimeInterval(
                        java.time.Instant.parse("2026-07-22T16:00:00Z"),
                        java.time.Instant.parse("2026-07-22T16:15:00Z")
                ),
                Optional.empty(),
                Optional.empty(),
                10
        );
        assertEquals(0.20, coaching, 1e-9);
        assertTrue(calendar.entries().get(1).headcountOffline().isPresent());
    }
}
