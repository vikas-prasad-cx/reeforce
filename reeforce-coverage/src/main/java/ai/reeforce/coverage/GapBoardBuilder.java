package ai.reeforce.coverage;

import ai.reeforce.capacity.ErlangStaffing;
import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.DemandSeries;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.ShrinkageCalendar;
import ai.reeforce.model.TimeInterval;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Builds a gap board by comparing required staff (from demand) to available agents,
 * optionally reduced by a shrinkage calendar.
 */
public final class GapBoardBuilder {

    private final double serviceLevel;
    private final double targetAnswerSeconds;
    private final ShrinkageCalendar shrinkage;
    private final Optional<String> skillFilter;
    private final Optional<String> siteFilter;

    public GapBoardBuilder(double serviceLevel, double targetAnswerSeconds) {
        this(serviceLevel, targetAnswerSeconds, new ShrinkageCalendar(List.of()), Optional.empty(), Optional.empty());
    }

    public GapBoardBuilder(
            double serviceLevel,
            double targetAnswerSeconds,
            ShrinkageCalendar shrinkage
    ) {
        this(serviceLevel, targetAnswerSeconds, shrinkage, Optional.empty(), Optional.empty());
    }

    public GapBoardBuilder(
            double serviceLevel,
            double targetAnswerSeconds,
            ShrinkageCalendar shrinkage,
            Optional<String> skillFilter,
            Optional<String> siteFilter
    ) {
        this.serviceLevel = serviceLevel;
        this.targetAnswerSeconds = targetAnswerSeconds;
        this.shrinkage = shrinkage == null ? new ShrinkageCalendar(List.of()) : shrinkage;
        this.skillFilter = skillFilter == null ? Optional.empty() : skillFilter;
        this.siteFilter = siteFilter == null ? Optional.empty() : siteFilter;
    }

    public GapBoard build(DemandSeries demand, List<AgentSchedule> schedules) {
        List<GapBoard.GapRow> rows = new ArrayList<>();
        for (DemandSeries.DemandPoint point : demand.points()) {
            TimeInterval interval = point.interval();
            double required = ErlangStaffing.requiredStaff(
                    point.offeredVolume(),
                    point.ahtSeconds(),
                    interval.durationSeconds(),
                    serviceLevel,
                    targetAnswerSeconds
            );
            double rawAvailable = countAvailable(schedules, interval);
            double shrink = shrinkage.shrinkageFraction(interval, skillFilter, siteFilter, rawAvailable);
            double available = rawAvailable * (1.0 - shrink);
            rows.add(new GapBoard.GapRow(interval, required, available, required - available));
        }
        return new GapBoard(rows);
    }

    static double countAvailable(List<AgentSchedule> schedules, TimeInterval interval) {
        double count = 0;
        for (AgentSchedule schedule : schedules) {
            for (AgentSchedule.ScheduleBlock block : schedule.blocks()) {
                if (block.state() == AgentSchedule.State.AVAILABLE && overlaps(block.interval(), interval)) {
                    count += overlapFraction(block.interval(), interval);
                }
            }
        }
        return count;
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
}
