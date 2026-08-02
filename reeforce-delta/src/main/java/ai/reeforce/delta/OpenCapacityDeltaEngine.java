package ai.reeforce.delta;

import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.ScheduleDelta;
import ai.reeforce.model.TimeInterval;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Propose bringing offline overflow capacity online on the peak gap
 * ({@link ScheduleDelta.ActionType#OPEN_SECTION}).
 *
 * <p>Looks for resources whose block overlapping the peak is {@code OFFLINE}
 * and that have positive {@code capacityUnits} (section seats or spare lane).
 */
public final class OpenCapacityDeltaEngine {

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
        double bestCapacity = 0.0;

        for (AgentSchedule schedule : schedules) {
            for (AgentSchedule.ScheduleBlock block : schedule.blocks()) {
                if (block.state() != AgentSchedule.State.OFFLINE) {
                    continue;
                }
                if (!overlaps(block.interval(), peak.interval())) {
                    continue;
                }
                double units = schedule.capacityUnits() * overlapFraction(block.interval(), peak.interval());
                if (units > bestCapacity + 1e-9) {
                    bestCapacity = units;
                    // "to" is the same interval but conceptually flipped to AVAILABLE.
                    best = Optional.of(new ScheduleDelta.DeltaAction(
                            schedule.agentId(),
                            ScheduleDelta.ActionType.OPEN_SECTION,
                            block.interval(),
                            peak.interval(),
                            "Peak understaffed gap at " + peak.interval().start()
                                    + " (gap=" + format(peak.gap())
                                    + "); open offline capacity " + schedule.agentId()
                                    + " (+" + format(units) + " units) on the cliff"
                    ));
                }
            }
        }

        List<ScheduleDelta.DeltaAction> actions = new ArrayList<>();
        best.ifPresent(actions::add);
        return new ScheduleDelta(actions);
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
