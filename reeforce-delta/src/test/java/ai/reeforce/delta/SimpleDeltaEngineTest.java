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
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleDeltaEngineTest {

    private static final Instant T1400 = Instant.parse("2026-07-22T14:00:00Z");
    private static final Instant T1415 = Instant.parse("2026-07-22T14:15:00Z");
    private static final Instant T1430 = Instant.parse("2026-07-22T14:30:00Z");
    private static final Instant T1445 = Instant.parse("2026-07-22T14:45:00Z");
    private static final Instant T1500 = Instant.parse("2026-07-22T15:00:00Z");

    @Test
    void proposesInWindowMealMoveWhenUnderstaffed() {
        TimeInterval peak = new TimeInterval(T1400, T1415);
        TimeInterval meal = new TimeInterval(T1400, T1430);
        GapBoard board = new GapBoard(List.of(
                new GapBoard.GapRow(peak, 10, 2, 8),
                new GapBoard.GapRow(new TimeInterval(T1430, T1445), 5, 5, 0)
        ));

        AgentSchedule schedule = new AgentSchedule(
                "A1",
                List.of(new AgentSchedule.ScheduleBlock(meal, AgentSchedule.State.MEAL)),
                new AgentSchedule.MealWindow(T1400, T1445)
        );

        ScheduleDelta delta = new SimpleDeltaEngine().propose(board, List.of(schedule));
        assertFalse(delta.actions().isEmpty());
        ScheduleDelta.DeltaAction action = delta.actions().getFirst();
        assertEquals(ScheduleDelta.ActionType.MOVE_MEAL, action.type());
        assertEquals("A1", action.agentId());
        assertTrue(schedule.mealWindow().containsStart(action.to().start()));
        assertFalse(SimpleDeltaEngine.overlaps(action.to(), peak),
                "moved meal should leave the peak interval");
    }

    @Test
    void rejectsOutOfWindowMealMove() {
        TimeInterval peak = new TimeInterval(T1400, T1415);
        TimeInterval meal = new TimeInterval(T1400, T1430);
        // Window only allows the current start — any forward shift is illegal.
        GapBoard board = new GapBoard(List.of(new GapBoard.GapRow(peak, 10, 2, 8)));
        AgentSchedule schedule = new AgentSchedule(
                "A1",
                List.of(new AgentSchedule.ScheduleBlock(meal, AgentSchedule.State.MEAL)),
                new AgentSchedule.MealWindow(T1400, T1400)
        );

        ScheduleDelta delta = new SimpleDeltaEngine().propose(board, List.of(schedule));
        assertTrue(delta.actions().isEmpty(), "must not propose out-of-window moves");
    }

    @Test
    void noOpWhenAlreadyOptimal() {
        // Meal already after the understaffed peak — nothing to move off the cliff.
        TimeInterval peak = new TimeInterval(T1400, T1415);
        TimeInterval meal = new TimeInterval(T1430, T1500);
        GapBoard board = new GapBoard(List.of(new GapBoard.GapRow(peak, 10, 2, 8)));
        AgentSchedule schedule = new AgentSchedule(
                "A1",
                List.of(new AgentSchedule.ScheduleBlock(meal, AgentSchedule.State.MEAL)),
                new AgentSchedule.MealWindow(T1400, T1500)
        );

        ScheduleDelta delta = new SimpleDeltaEngine().propose(board, List.of(schedule));
        assertTrue(delta.actions().isEmpty());
    }
}
