package ai.reeforce.coverage;

import ai.reeforce.capacity.ErlangStaffing;
import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.DemandSeries;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.TimeInterval;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a gap board by comparing required staff (from demand) to available agents.
 */
public final class GapBoardBuilder {

    private final double serviceLevel;
    private final double targetAnswerSeconds;

    public GapBoardBuilder(double serviceLevel, double targetAnswerSeconds) {
        this.serviceLevel = serviceLevel;
        this.targetAnswerSeconds = targetAnswerSeconds;
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
            double available = countAvailable(schedules, interval);
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
