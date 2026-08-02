package ai.reeforce.model;

import java.util.List;
import java.util.Objects;

/**
 * A proposed mid-shift change set relative to the published schedule.
 */
public record ScheduleDelta(List<DeltaAction> actions) {

    public ScheduleDelta {
        Objects.requireNonNull(actions, "actions");
        actions = List.copyOf(actions);
    }

    public enum ActionType {
        MOVE_MEAL,
        MOVE_BREAK,
        EXTEND_SHIFT,
        VOLUNTEER_OT,
        CANCEL_ACTIVITY,
        /** School conference: slide a booked/prep slot inside the teacher window. */
        MOVE_SLOT,
        /** School conference: suggest an alternate room for a stacked slot. */
        REASSIGN_ROOM,
        /** Class section / traffic: bring offline overflow capacity online on the peak. */
        OPEN_SECTION,
        /** Traffic: shift a demand pulse off the peak corridor interval. */
        RETIME_DEMAND
    }

    public record DeltaAction(
            String agentId,
            ActionType type,
            TimeInterval from,
            TimeInterval to,
            String rationale
    ) {
        public DeltaAction {
            Objects.requireNonNull(agentId, "agentId");
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
            Objects.requireNonNull(rationale, "rationale");
        }
    }
}
