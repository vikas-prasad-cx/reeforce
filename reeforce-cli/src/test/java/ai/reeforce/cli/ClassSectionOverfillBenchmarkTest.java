package ai.reeforce.cli;

import ai.reeforce.coverage.GapBoardBuilder;
import ai.reeforce.delta.DomainDeltaEngine;
import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.DemandCsvLoader;
import ai.reeforce.model.DemandSeries;
import ai.reeforce.model.DomainProfile;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.RosterCsvLoader;
import ai.reeforce.model.ScheduleDelta;
import ai.reeforce.model.TimeInterval;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ai.reeforce.cli.SchoolConferenceCliffBenchmarkTest.resolveFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassSectionOverfillBenchmarkTest {

    private static final Path FIXTURE = Path.of("datasets/class-section-overfill");

    @Test
    void detectsOverfillAndOpenSectionHelps() throws IOException {
        Path root = resolveFixture(FIXTURE);
        DomainProfile domain = DomainProfile.CLASS_SECTION;
        DemandSeries demand = DemandCsvLoader.load(
                domain.defaultSkill(), domain.defaultChannel(), Files.readString(root.resolve("demand.csv")));
        List<AgentSchedule> schedules = RosterCsvLoader.load(Files.readString(root.resolve("roster.csv")));

        GapBoard board = new GapBoardBuilder(domain, 0.80, 20).build(demand, schedules);
        double peakGap = board.rows().stream().mapToDouble(GapBoard.GapRow::gap).max().orElse(0);
        assertTrue(peakGap >= 15.0, "expected overfill peak gap >= 15, was " + peakGap);

        ScheduleDelta delta = new DomainDeltaEngine(domain).propose(board, schedules);
        assertFalse(delta.actions().isEmpty(), "should propose OPEN_SECTION");
        ScheduleDelta.DeltaAction action = delta.actions().getFirst();
        assertEquals(ScheduleDelta.ActionType.OPEN_SECTION, action.type());
        assertEquals("SEC-OVERFLOW", action.agentId());

        List<AgentSchedule> adjusted = applyOpenSection(schedules, action);
        GapBoard after = new GapBoardBuilder(domain, 0.80, 20).build(demand, adjusted);
        double peakAfter = after.rows().stream().mapToDouble(GapBoard.GapRow::gap).max().orElse(0);
        assertTrue(peakAfter < peakGap - 10.0,
                "opening overflow should cut peak gap: before=" + peakGap + " after=" + peakAfter);
    }

    private static List<AgentSchedule> applyOpenSection(
            List<AgentSchedule> schedules,
            ScheduleDelta.DeltaAction action
    ) {
        List<AgentSchedule> out = new ArrayList<>();
        for (AgentSchedule schedule : schedules) {
            if (!schedule.agentId().equals(action.agentId())) {
                out.add(schedule);
                continue;
            }
            List<AgentSchedule.ScheduleBlock> blocks = new ArrayList<>();
            for (AgentSchedule.ScheduleBlock block : schedule.blocks()) {
                if (block.state() == AgentSchedule.State.OFFLINE
                        && overlaps(block.interval(), action.from())) {
                    blocks.add(new AgentSchedule.ScheduleBlock(block.interval(), AgentSchedule.State.AVAILABLE));
                } else {
                    blocks.add(block);
                }
            }
            out.add(new AgentSchedule(
                    schedule.agentId(), blocks, schedule.mealWindow(), schedule.capacityUnits()));
        }
        return out;
    }

    private static boolean overlaps(TimeInterval a, TimeInterval b) {
        return a.start().isBefore(b.end()) && b.start().isBefore(a.end());
    }
}
