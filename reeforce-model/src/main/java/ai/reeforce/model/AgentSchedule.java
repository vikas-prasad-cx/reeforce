package ai.reeforce.model;

import java.util.List;
import java.util.Objects;

/**
 * One agent's planned states across the day (work, meal, break, offline).
 */
public record AgentSchedule(String agentId, List<ScheduleBlock> blocks) {

    public AgentSchedule {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(blocks, "blocks");
        blocks = List.copyOf(blocks);
    }

    public enum State {
        AVAILABLE,
        MEAL,
        BREAK,
        TRAINING,
        OFFLINE
    }

    public record ScheduleBlock(TimeInterval interval, State state) {
        public ScheduleBlock {
            Objects.requireNonNull(interval, "interval");
            Objects.requireNonNull(state, "state");
        }
    }
}
