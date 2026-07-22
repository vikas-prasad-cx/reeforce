package ai.reeforce.coverage;

import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.DemandSeries;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.ShrinkageCalendar;
import ai.reeforce.model.TimeInterval;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

    @Test
    void shrinkageReducesAvailableCoverage() {
        Instant start = Instant.parse("2026-07-22T14:00:00Z");
        Instant end = Instant.parse("2026-07-22T14:15:00Z");
        TimeInterval interval = new TimeInterval(start, end);

        DemandSeries demand = new DemandSeries(
                "voice",
                "inbound",
                List.of(new DemandSeries.DemandPoint(interval, 5, 300))
        );

        List<AgentSchedule> schedules = List.of(
                new AgentSchedule("A1", List.of(new AgentSchedule.ScheduleBlock(interval, AgentSchedule.State.AVAILABLE))),
                new AgentSchedule("A2", List.of(new AgentSchedule.ScheduleBlock(interval, AgentSchedule.State.AVAILABLE))),
                new AgentSchedule("A3", List.of(new AgentSchedule.ScheduleBlock(interval, AgentSchedule.State.AVAILABLE))),
                new AgentSchedule("A4", List.of(new AgentSchedule.ScheduleBlock(interval, AgentSchedule.State.AVAILABLE))),
                new AgentSchedule("A5", List.of(new AgentSchedule.ScheduleBlock(interval, AgentSchedule.State.AVAILABLE))),
                new AgentSchedule("A6", List.of(new AgentSchedule.ScheduleBlock(interval, AgentSchedule.State.AVAILABLE))),
                new AgentSchedule("A7", List.of(new AgentSchedule.ScheduleBlock(interval, AgentSchedule.State.AVAILABLE))),
                new AgentSchedule("A8", List.of(new AgentSchedule.ScheduleBlock(interval, AgentSchedule.State.AVAILABLE))),
                new AgentSchedule("A9", List.of(new AgentSchedule.ScheduleBlock(interval, AgentSchedule.State.AVAILABLE))),
                new AgentSchedule("A10", List.of(new AgentSchedule.ScheduleBlock(interval, AgentSchedule.State.AVAILABLE)))
        );

        ShrinkageCalendar shrinkage = new ShrinkageCalendar(List.of(
                new ShrinkageCalendar.ShrinkageEntry(
                        interval,
                        Optional.of(0.20),
                        Optional.empty(),
                        "meeting",
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()
                )
        ));

        GapBoard without = new GapBoardBuilder(0.80, 20).build(demand, schedules);
        GapBoard with = new GapBoardBuilder(0.80, 20, shrinkage).build(demand, schedules);
        assertEquals(10.0, without.rows().getFirst().availableStaff(), 1e-9);
        assertEquals(8.0, with.rows().getFirst().availableStaff(), 1e-9);
        assertTrue(with.rows().getFirst().gap() > without.rows().getFirst().gap());
    }
}
