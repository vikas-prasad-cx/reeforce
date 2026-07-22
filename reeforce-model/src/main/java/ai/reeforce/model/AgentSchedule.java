package ai.reeforce.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One agent's planned states across the day (work, meal, break, offline).
 */
public record AgentSchedule(String agentId, List<ScheduleBlock> blocks, MealWindow mealWindow) {

    public AgentSchedule {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(blocks, "blocks");
        blocks = List.copyOf(blocks);
    }

    /** Convenience when no contractual meal window is modeled. */
    public AgentSchedule(String agentId, List<ScheduleBlock> blocks) {
        this(agentId, blocks, null);
    }

    public Optional<MealWindow> mealWindowOptional() {
        return Optional.ofNullable(mealWindow);
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

    /**
     * Contractual meal start window: meal may begin at any instant in
     * {@code [earliestStart, latestStart]} (inclusive bounds on start).
     */
    public record MealWindow(Instant earliestStart, Instant latestStart) {
        public MealWindow {
            Objects.requireNonNull(earliestStart, "earliestStart");
            Objects.requireNonNull(latestStart, "latestStart");
            if (latestStart.isBefore(earliestStart)) {
                throw new IllegalArgumentException("latestStart must be >= earliestStart");
            }
        }

        public boolean containsStart(Instant mealStart) {
            return !mealStart.isBefore(earliestStart) && !mealStart.isAfter(latestStart);
        }
    }
}
