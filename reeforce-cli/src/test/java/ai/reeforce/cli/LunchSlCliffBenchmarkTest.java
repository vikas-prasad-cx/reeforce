package ai.reeforce.cli;

import ai.reeforce.coverage.GapBoardBuilder;
import ai.reeforce.delta.SimpleDeltaEngine;
import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.DemandCsvLoader;
import ai.reeforce.model.DemandSeries;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Benchmark: clustered lunches create an SL cliff that the gap board detects
 * and that a windowed meal-move can improve.
 */
class LunchSlCliffBenchmarkTest {

    private static final Path FIXTURE = Path.of("datasets/lunch-sl-cliff");

    @Test
    void detectsLunchCliffAndMealMoveReducesPeakGap() throws IOException {
        Path root = resolveFixture();
        DemandSeries demand = DemandCsvLoader.load("voice", "inbound", Files.readString(root.resolve("demand.csv")));
        List<AgentSchedule> schedules = RosterCsvLoader.load(
                Files.readString(root.resolve("roster.csv")),
                Files.readString(root.resolve("meal-windows.csv"))
        );

        GapBoard board = new GapBoardBuilder(0.80, 20).build(demand, schedules);
        double peakGap = board.rows().stream().mapToDouble(GapBoard.GapRow::gap).max().orElse(0);
        assertTrue(peakGap > 5.0, "expected lunch cliff peak gap > 5, was " + peakGap);

        Instant cliffStart = Instant.parse("2026-07-22T12:00:00Z");
        assertTrue(
                board.rows().stream().anyMatch(r ->
                        r.interval().start().equals(cliffStart) && r.understaffed()),
                "12:00 interval should be understaffed"
        );

        ScheduleDelta delta = new SimpleDeltaEngine().propose(board, schedules);
        assertFalse(delta.actions().isEmpty(), "should propose at least one in-window meal move");
        ScheduleDelta.DeltaAction move = delta.actions().getFirst();

        List<AgentSchedule> adjusted = applyMealMove(schedules, move);
        GapBoard after = new GapBoardBuilder(0.80, 20).build(demand, adjusted);
        double peakAfter = after.rows().stream().mapToDouble(GapBoard.GapRow::gap).max().orElse(0);
        assertTrue(peakAfter < peakGap - 0.5,
                "meal move should reduce peak gap: before=" + peakGap + " after=" + peakAfter);
    }

    private static Path resolveFixture() {
        Path direct = FIXTURE;
        if (Files.isDirectory(direct)) {
            return direct;
        }
        Path fromModule = Path.of("..").resolve(FIXTURE);
        if (Files.isDirectory(fromModule)) {
            return fromModule;
        }
        throw new IllegalStateException("Cannot find datasets/lunch-sl-cliff from " + Path.of(".").toAbsolutePath());
    }

    private static List<AgentSchedule> applyMealMove(List<AgentSchedule> schedules, ScheduleDelta.DeltaAction move) {
        List<AgentSchedule> out = new ArrayList<>();
        for (AgentSchedule schedule : schedules) {
            if (!schedule.agentId().equals(move.agentId())) {
                out.add(schedule);
                continue;
            }
            Instant dayStart = schedule.blocks().getFirst().interval().start();
            Instant dayEnd = schedule.blocks().getLast().interval().end();
            List<AgentSchedule.ScheduleBlock> blocks = List.of(
                    new AgentSchedule.ScheduleBlock(
                            new TimeInterval(dayStart, move.to().start()), AgentSchedule.State.AVAILABLE),
                    new AgentSchedule.ScheduleBlock(move.to(), AgentSchedule.State.MEAL),
                    new AgentSchedule.ScheduleBlock(
                            new TimeInterval(move.to().end(), dayEnd), AgentSchedule.State.AVAILABLE)
            );
            out.add(new AgentSchedule(schedule.agentId(), blocks, schedule.mealWindow()));
        }
        return out;
    }
}
