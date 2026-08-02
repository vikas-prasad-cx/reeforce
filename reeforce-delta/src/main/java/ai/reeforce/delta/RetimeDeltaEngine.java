package ai.reeforce.delta;

import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.ScheduleDelta;
import ai.reeforce.model.TimeInterval;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Traffic retime: slide a scheduled pulse (TRAINING/BREAK block representing a
 * platoon or timed release) off the peak understaffed corridor interval.
 */
public final class RetimeDeltaEngine {

    public ScheduleDelta propose(GapBoard board, List<AgentSchedule> schedules) {
        List<GapBoard.GapRow> understaffed = board.rows().stream()
                .filter(GapBoard.GapRow::understaffed)
                .sorted(Comparator.comparingDouble(GapBoard.GapRow::gap).reversed())
                .toList();
        if (understaffed.isEmpty()) {
            return new ScheduleDelta(List.of());
        }

        GapBoard.GapRow peak = understaffed.getFirst();
        // Prefer landing on the strongest OVER interval after the peak.
        Optional<GapBoard.GapRow> landing = board.rows().stream()
                .filter(r -> r.gap() < -0.5)
                .filter(r -> !r.interval().start().isBefore(peak.interval().end()))
                .min(Comparator.comparingDouble(GapBoard.GapRow::gap));

        Optional<ScheduleDelta.DeltaAction> best = Optional.empty();
        double bestImprovement = 0.0;

        for (AgentSchedule schedule : schedules) {
            for (AgentSchedule.ScheduleBlock block : schedule.blocks()) {
                if (block.state() != AgentSchedule.State.TRAINING
                        && block.state() != AgentSchedule.State.BREAK) {
                    continue;
                }
                if (!overlaps(block.interval(), peak.interval())) {
                    continue;
                }
                TimeInterval candidate = landing
                        .map(row -> shiftTo(block.interval(), row.interval().start()))
                        .orElse(shiftBy(block.interval(), peak.interval().durationSeconds()));
                if (candidate.start().equals(block.interval().start())) {
                    continue;
                }
                if (!withinWindow(schedule, candidate.start())) {
                    continue;
                }
                double improvement = overlapFraction(block.interval(), peak.interval())
                        * schedule.capacityUnits();
                if (improvement > bestImprovement + 1e-9) {
                    bestImprovement = improvement;
                    best = Optional.of(new ScheduleDelta.DeltaAction(
                            schedule.agentId(),
                            ScheduleDelta.ActionType.RETIME_DEMAND,
                            block.interval(),
                            candidate,
                            "Peak corridor gap at " + peak.interval().start()
                                    + " (gap=" + format(peak.gap())
                                    + "); retime pulse " + schedule.agentId()
                                    + " off the cliff"
                    ));
                }
            }
        }

        List<ScheduleDelta.DeltaAction> actions = new ArrayList<>();
        best.ifPresent(actions::add);
        return new ScheduleDelta(actions);
    }

    static TimeInterval shiftTo(TimeInterval original, Instant newStart) {
        long len = original.durationSeconds();
        return new TimeInterval(newStart, newStart.plusSeconds(len));
    }

    static TimeInterval shiftBy(TimeInterval original, long seconds) {
        return new TimeInterval(
                original.start().plusSeconds(seconds),
                original.end().plusSeconds(seconds)
        );
    }

    static boolean withinWindow(AgentSchedule schedule, Instant start) {
        return schedule.mealWindowOptional()
                .map(w -> w.containsStart(start))
                .orElse(true);
    }

    static boolean overlaps(TimeInterval a, TimeInterval b) {
        return a.start().isBefore(b.end()) && b.start().isBefore(a.end());
    }

    static double overlapFraction(TimeInterval block, TimeInterval interval) {
        long start = Math.max(block.start().getEpochSecond(), interval.start().getEpochSecond());
        long end = Math.min(block.end().getEpochSecond(), interval.end().getEpochSecond());
        long overlap = Math.max(0, end - start);
        return overlap / (double) interval.durationSeconds();
    }

    private static String format(double v) {
        return String.format("%.1f", v);
    }
}
