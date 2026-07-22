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
 * Delta engine: when understaffed, propose moving a meal to a later (or earlier)
 * start that stays inside the agent's contractual meal window and best reduces
 * the largest understaffed gap.
 */
public final class SimpleDeltaEngine {

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
                if (block.state() != AgentSchedule.State.MEAL) {
                    continue;
                }
                if (!overlaps(block.interval(), peak.interval())) {
                    // Only move meals that currently sit on the peak gap.
                    continue;
                }
                for (TimeInterval candidate : candidateMealStarts(block.interval(), schedule, board)) {
                    if (!withinMealWindow(schedule, candidate.start())) {
                        continue;
                    }
                    if (candidate.start().equals(block.interval().start())) {
                        continue;
                    }
                    double improvement = estimateImprovement(peak, block.interval(), candidate);
                    if (improvement > bestImprovement + 1e-9
                            || (Math.abs(improvement - bestImprovement) <= 1e-9
                            && best.isPresent()
                            && candidate.start().isBefore(best.get().to().start()))) {
                        bestImprovement = improvement;
                        best = Optional.of(new ScheduleDelta.DeltaAction(
                                schedule.agentId(),
                                ScheduleDelta.ActionType.MOVE_MEAL,
                                block.interval(),
                                candidate,
                                "Peak understaffed gap at " + peak.interval().start()
                                        + " (gap=" + format(peak.gap())
                                        + "); move meal within window to free capacity"
                        ));
                    }
                }
            }
        }

        List<ScheduleDelta.DeltaAction> actions = new ArrayList<>();
        best.ifPresent(actions::add);
        return new ScheduleDelta(actions);
    }

    /**
     * Candidate meal intervals: shift by ± one planning step (duration of peak intervals)
     * and also try parking the meal just after the peak interval ends, if window allows.
     */
    static List<TimeInterval> candidateMealStarts(
            TimeInterval currentMeal,
            AgentSchedule schedule,
            GapBoard board
    ) {
        long mealLen = currentMeal.durationSeconds();
        long step = board.rows().isEmpty()
                ? mealLen
                : board.rows().getFirst().interval().durationSeconds();

        List<TimeInterval> out = new ArrayList<>();
        // Prefer moving off the cliff: after peak, then ± step grid inside window.
        Instant[] starts = new Instant[] {
                currentMeal.start().plusSeconds(step),
                currentMeal.start().plusSeconds(2 * step),
                currentMeal.start().minusSeconds(step),
                currentMeal.start().plusSeconds(3 * step)
        };
        for (Instant start : starts) {
            Instant end = start.plusSeconds(mealLen);
            if (end.isAfter(start)) {
                out.add(new TimeInterval(start, end));
            }
        }
        schedule.mealWindowOptional().ifPresent(window -> {
            // Also try earliest and latest legal starts.
            out.add(new TimeInterval(window.earliestStart(), window.earliestStart().plusSeconds(mealLen)));
            out.add(new TimeInterval(window.latestStart(), window.latestStart().plusSeconds(mealLen)));
        });
        return out;
    }

    static boolean withinMealWindow(AgentSchedule schedule, Instant mealStart) {
        return schedule.mealWindowOptional()
                .map(w -> w.containsStart(mealStart))
                .orElse(true); // no window modeled → allow (legacy / unconstrained)
    }

    /**
     * Heuristic: reclaiming a meal that fully covers the peak interval frees ~1.0 FTE
     * there; a candidate that still overlaps the peak frees less.
     */
    static double estimateImprovement(GapBoard.GapRow peak, TimeInterval fromMeal, TimeInterval toMeal) {
        double freed = overlapFraction(fromMeal, peak.interval());
        double stillBlocked = overlapFraction(toMeal, peak.interval());
        return freed - stillBlocked;
    }

    static boolean overlaps(TimeInterval a, TimeInterval b) {
        return a.start().isBefore(b.end()) && b.start().isBefore(a.end());
    }

    static double overlapFraction(TimeInterval agentBlock, TimeInterval interval) {
        long start = Math.max(agentBlock.start().getEpochSecond(), interval.start().getEpochSecond());
        long end = Math.min(agentBlock.end().getEpochSecond(), interval.end().getEpochSecond());
        long overlap = Math.max(0, end - start);
        return overlap / (double) interval.durationSeconds();
    }

    private static String format(double v) {
        return String.format("%.1f", v);
    }
}
