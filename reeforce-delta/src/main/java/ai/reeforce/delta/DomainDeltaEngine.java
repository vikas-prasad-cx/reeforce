package ai.reeforce.delta;

import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.DomainProfile;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.ScheduleDelta;

import java.util.List;

/**
 * Dispatches delta search by {@link DomainProfile}.
 */
public final class DomainDeltaEngine {

    private final DomainProfile domain;
    private final SimpleDeltaEngine mealEngine = new SimpleDeltaEngine();
    private final SlotDeltaEngine slotEngine = new SlotDeltaEngine();
    private final OpenCapacityDeltaEngine openEngine = new OpenCapacityDeltaEngine();
    private final RetimeDeltaEngine retimeEngine = new RetimeDeltaEngine();

    public DomainDeltaEngine(DomainProfile domain) {
        this.domain = domain == null ? DomainProfile.CONTACT_CENTER : domain;
    }

    public ScheduleDelta propose(GapBoard board, List<AgentSchedule> schedules) {
        return switch (domain) {
            case CONTACT_CENTER -> mealEngine.propose(board, schedules);
            case SCHOOL_CONFERENCE -> slotEngine.propose(board, schedules);
            case CLASS_SECTION -> openEngine.propose(board, schedules);
            case TRAFFIC_CORRIDOR -> {
                ScheduleDelta open = openEngine.propose(board, schedules);
                if (!open.actions().isEmpty()) {
                    yield open;
                }
                yield retimeEngine.propose(board, schedules);
            }
        };
    }
}
