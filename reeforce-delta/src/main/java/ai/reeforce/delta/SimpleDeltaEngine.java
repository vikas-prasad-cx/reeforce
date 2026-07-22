package ai.reeforce.delta;

import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.ScheduleDelta;
import ai.reeforce.model.TimeInterval;

import java.util.ArrayList;
import java.util.List;

/**
 * MVP delta engine: when an interval is understaffed and an agent is on meal
 * overlapping that interval, propose moving the meal to the next free window.
 */
public final class SimpleDeltaEngine {

    public ScheduleDelta propose(GapBoard board, List<AgentSchedule> schedules) {
        List<ScheduleDelta.DeltaAction> actions = new ArrayList<>();
        for (GapBoard.GapRow row : board.rows()) {
            if (!row.understaffed()) {
                continue;
            }
            for (AgentSchedule schedule : schedules) {
                for (AgentSchedule.ScheduleBlock block : schedule.blocks()) {
                    if (block.state() != AgentSchedule.State.MEAL) {
                        continue;
                    }
                    if (!overlaps(block.interval(), row.interval())) {
                        continue;
                    }
                    TimeInterval proposed = shiftForward(block.interval(), row.interval().durationSeconds());
                    actions.add(new ScheduleDelta.DeltaAction(
                            schedule.agentId(),
                            ScheduleDelta.ActionType.MOVE_MEAL,
                            block.interval(),
                            proposed,
                            "Understaffed interval " + row.interval().start()
                                    + "; move meal to open capacity later"
                    ));
                }
            }
        }
        return new ScheduleDelta(actions);
    }

    static boolean overlaps(TimeInterval a, TimeInterval b) {
        return a.start().isBefore(b.end()) && b.start().isBefore(a.end());
    }

    static TimeInterval shiftForward(TimeInterval meal, long seconds) {
        return new TimeInterval(meal.start().plusSeconds(seconds), meal.end().plusSeconds(seconds));
    }
}
