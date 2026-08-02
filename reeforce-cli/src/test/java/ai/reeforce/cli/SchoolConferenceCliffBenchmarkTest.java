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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchoolConferenceCliffBenchmarkTest {

    private static final Path FIXTURE = Path.of("datasets/school-conference-cliff");

    @Test
    void detectsConferenceCliffAndMoveSlotHelps() throws IOException {
        Path root = resolveFixture(FIXTURE);
        DomainProfile domain = DomainProfile.SCHOOL_CONFERENCE;
        DemandSeries demand = DemandCsvLoader.load(
                domain.defaultSkill(), domain.defaultChannel(), Files.readString(root.resolve("demand.csv")));
        List<AgentSchedule> schedules = RosterCsvLoader.load(
                Files.readString(root.resolve("roster.csv")),
                Files.readString(root.resolve("slot-windows.csv"))
        );

        GapBoard board = new GapBoardBuilder(domain, 0.80, 20).build(demand, schedules);
        double peakGap = board.rows().stream().mapToDouble(GapBoard.GapRow::gap).max().orElse(0);
        assertTrue(peakGap > 5.0, "expected conference cliff peak gap > 5, was " + peakGap);

        Instant cliff = Instant.parse("2026-10-14T18:15:00Z");
        assertTrue(
                board.rows().stream().anyMatch(r ->
                        r.interval().start().equals(cliff) && r.understaffed()),
                "18:15 interval should be understaffed"
        );

        ScheduleDelta delta = new DomainDeltaEngine(domain).propose(board, schedules);
        assertFalse(delta.actions().isEmpty(), "should propose MOVE_SLOT");
        assertEquals(ScheduleDelta.ActionType.MOVE_SLOT, delta.actions().getFirst().type());

        List<AgentSchedule> adjusted = applySlotMove(schedules, delta.actions().getFirst());
        GapBoard after = new GapBoardBuilder(domain, 0.80, 20).build(demand, adjusted);
        double peakAfter = after.rows().stream().mapToDouble(GapBoard.GapRow::gap).max().orElse(0);
        assertTrue(peakAfter < peakGap - 0.5,
                "slot move should reduce peak gap: before=" + peakGap + " after=" + peakAfter);
    }

    static Path resolveFixture(Path fixture) {
        if (Files.isDirectory(fixture)) {
            return fixture;
        }
        Path fromModule = Path.of("..").resolve(fixture);
        if (Files.isDirectory(fromModule)) {
            return fromModule;
        }
        throw new IllegalStateException("Cannot find " + fixture + " from " + Path.of(".").toAbsolutePath());
    }

    private static List<AgentSchedule> applySlotMove(List<AgentSchedule> schedules, ScheduleDelta.DeltaAction move) {
        List<AgentSchedule> out = new ArrayList<>();
        for (AgentSchedule schedule : schedules) {
            if (!schedule.agentId().equals(move.agentId())) {
                out.add(schedule);
                continue;
            }
            Instant dayStart = schedule.blocks().getFirst().interval().start();
            Instant dayEnd = schedule.blocks().getLast().interval().end();
            AgentSchedule.State movedState = schedule.blocks().stream()
                    .filter(b -> b.interval().start().equals(move.from().start()))
                    .map(AgentSchedule.ScheduleBlock::state)
                    .findFirst()
                    .orElse(AgentSchedule.State.TRAINING);
            List<AgentSchedule.ScheduleBlock> blocks = List.of(
                    new AgentSchedule.ScheduleBlock(
                            new TimeInterval(dayStart, move.to().start()), AgentSchedule.State.AVAILABLE),
                    new AgentSchedule.ScheduleBlock(move.to(), movedState),
                    new AgentSchedule.ScheduleBlock(
                            new TimeInterval(move.to().end(), dayEnd), AgentSchedule.State.AVAILABLE)
            );
            out.add(new AgentSchedule(schedule.agentId(), blocks, schedule.mealWindow(), schedule.capacityUnits()));
        }
        return out;
    }
}
