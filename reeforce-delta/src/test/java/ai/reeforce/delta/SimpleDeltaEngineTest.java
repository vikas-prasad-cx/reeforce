package ai.reeforce.delta;

import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.ScheduleDelta;
import ai.reeforce.model.TimeInterval;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SimpleDeltaEngineTest {

    @Test
    void proposesMealMoveWhenUnderstaffed() {
        Instant start = Instant.parse("2026-07-22T14:00:00Z");
        Instant end = Instant.parse("2026-07-22T14:15:00Z");
        TimeInterval interval = new TimeInterval(start, end);

        GapBoard board = new GapBoard(List.of(
                new GapBoard.GapRow(interval, 10, 2, 8)
        ));

        AgentSchedule schedule = new AgentSchedule(
                "A1",
                List.of(new AgentSchedule.ScheduleBlock(interval, AgentSchedule.State.MEAL))
        );

        ScheduleDelta delta = new SimpleDeltaEngine().propose(board, List.of(schedule));
        assertFalse(delta.actions().isEmpty());
        assertEquals(ScheduleDelta.ActionType.MOVE_MEAL, delta.actions().getFirst().type());
        assertEquals("A1", delta.actions().getFirst().agentId());
    }
}
