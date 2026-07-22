package ai.reeforce.coverage;

import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.DemandSeries;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.TimeInterval;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GapBoardBuilderTest {

    @Test
    void understaffedWhenNobodyIsAvailable() {
        Instant start = Instant.parse("2026-07-22T14:00:00Z");
        Instant end = Instant.parse("2026-07-22T14:15:00Z");
        TimeInterval interval = new TimeInterval(start, end);

        DemandSeries demand = new DemandSeries(
                "voice",
                "inbound",
                List.of(new DemandSeries.DemandPoint(interval, 40, 300))
        );

        AgentSchedule mealOnly = new AgentSchedule(
                "A1",
                List.of(new AgentSchedule.ScheduleBlock(interval, AgentSchedule.State.MEAL))
        );

        GapBoard board = new GapBoardBuilder(0.80, 20).build(demand, List.of(mealOnly));
        assertEquals(1, board.rows().size());
        assertTrue(board.rows().getFirst().understaffed());
        assertEquals(0.0, board.rows().getFirst().availableStaff());
    }
}
