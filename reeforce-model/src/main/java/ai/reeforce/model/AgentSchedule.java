package ai.reeforce.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One resource's planned states across the day (agent, teacher, section, corridor).
 *
 * <p>{@code capacityUnits} scales AVAILABLE coverage (default 1.0). Sections and
 * corridors use values &gt; 1 for seats / vehicles-per-interval of throughput.
 */
public record AgentSchedule(
        String agentId,
        List<ScheduleBlock> blocks,
        MealWindow mealWindow,
        double capacityUnits
) {

    public AgentSchedule {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(blocks, "blocks");
        blocks = List.copyOf(blocks);
        if (capacityUnits <= 0) {
            throw new IllegalArgumentException("capacityUnits must be > 0");
        }
    }

    /** Convenience when no contractual meal/slot window is modeled. */
    public AgentSchedule(String agentId, List<ScheduleBlock> blocks) {
        this(agentId, blocks, null, 1.0);
    }

    /** Convenience with meal/slot window and unit capacity. */
    public AgentSchedule(String agentId, List<ScheduleBlock> blocks, MealWindow mealWindow) {
        this(agentId, blocks, mealWindow, 1.0);
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
