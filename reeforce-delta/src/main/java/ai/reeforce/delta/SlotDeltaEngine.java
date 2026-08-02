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
 * School-conference deltas: move a prep/unavailable slot (MEAL/BREAK/TRAINING)
 * off the peak understaffed interval while staying inside the contractual window.
 */
public final class SlotDeltaEngine {

    public ScheduleDelta propose(GapBoard board, List<AgentSchedule> schedules) {
        List<GapBoard.GapRow> understaffed = board.rows().stream()
                .filter(GapBoard.GapRow::understaffed)
                .sorted(Comparator.comparingDouble(GapBoard.GapRow::gap).reversed())
                .toList();
        if (understaffed.isEmpty()) {
            return new ScheduleDelta(List.of());
        }

        GapBoard.GapRow peak = understaffed.getFirst();
        Optional<ScheduleDelta.DeltaAction> best = Optional.empty();
        double bestImprovement = 0.0;

        for (AgentSchedule schedule : schedules) {
            for (AgentSchedule.ScheduleBlock block : schedule.blocks()) {
                if (!isMovableSlot(block.state())) {
                    continue;
                }
                if (!overlaps(block.interval(), peak.interval())) {
                    continue;
                }
                for (TimeInterval candidate : candidateStarts(block.interval(), schedule, board)) {
                    if (!withinWindow(schedule, candidate.start())) {
                        continue;
                    }
                    if (candidate.start().equals(block.interval().start())) {
                        continue;
                    }
                    double improvement = estimateImprovement(peak, block.interval(), candidate)
                            * schedule.capacityUnits();
                    if (improvement > bestImprovement + 1e-9
                            || (Math.abs(improvement - bestImprovement) <= 1e-9
                            && best.isPresent()
                            && candidate.start().isBefore(best.get().to().start()))) {
                        bestImprovement = improvement;
                        best = Optional.of(new ScheduleDelta.DeltaAction(
                                schedule.agentId(),
                                ScheduleDelta.ActionType.MOVE_SLOT,
                                block.interval(),
                                candidate,
                                "Peak understaffed gap at " + peak.interval().start()
                                        + " (gap=" + format(peak.gap())
                                        + "); move slot within window to free conference capacity"
                        ));
                    }
                }
            }
        }

        List<ScheduleDelta.DeltaAction> actions = new ArrayList<>();
        best.ifPresent(actions::add);
        return new ScheduleDelta(actions);
    }

    static boolean isMovableSlot(AgentSchedule.State state) {
        return state == AgentSchedule.State.MEAL
                || state == AgentSchedule.State.BREAK
                || state == AgentSchedule.State.TRAINING;
    }

    static List<TimeInterval> candidateStarts(
            TimeInterval current,
            AgentSchedule schedule,
            GapBoard board
    ) {
        long len = current.durationSeconds();
        long step = board.rows().isEmpty()
                ? len
                : board.rows().getFirst().interval().durationSeconds();

        List<TimeInterval> out = new ArrayList<>();
        Instant[] starts = new Instant[] {
                current.start().plusSeconds(step),
                current.start().plusSeconds(2 * step),
                current.start().minusSeconds(step),
                current.start().plusSeconds(3 * step)
        };
        for (Instant start : starts) {
            Instant end = start.plusSeconds(len);
            if (end.isAfter(start)) {
                out.add(new TimeInterval(start, end));
            }
        }
        schedule.mealWindowOptional().ifPresent(window -> {
            out.add(new TimeInterval(window.earliestStart(), window.earliestStart().plusSeconds(len)));
            out.add(new TimeInterval(window.latestStart(), window.latestStart().plusSeconds(len)));
        });
        return out;
    }

    static boolean withinWindow(AgentSchedule schedule, Instant start) {
        return schedule.mealWindowOptional()
                .map(w -> w.containsStart(start))
                .orElse(true);
    }

    static double estimateImprovement(GapBoard.GapRow peak, TimeInterval from, TimeInterval to) {
        double freed = overlapFraction(from, peak.interval());
        double stillBlocked = overlapFraction(to, peak.interval());
        return freed - stillBlocked;
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
