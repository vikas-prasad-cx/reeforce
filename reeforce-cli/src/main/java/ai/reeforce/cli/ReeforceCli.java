package ai.reeforce.cli;

import ai.reeforce.coverage.GapBoardBuilder;
import ai.reeforce.delta.SimpleDeltaEngine;
import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.DemandCsvLoader;
import ai.reeforce.model.DemandSeries;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.ScheduleDelta;
import ai.reeforce.model.TimeInterval;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal CLI: reeforce gap &lt;demand.csv&gt;
 */
public final class ReeforceCli {

    public static void main(String[] args) throws IOException {
        if (args.length == 0 || "help".equals(args[0]) || "--help".equals(args[0])) {
            printHelp();
            return;
        }
        if ("gap".equals(args[0])) {
            if (args.length < 2) {
                System.err.println("Usage: reeforce gap <demand.csv>");
                System.exit(2);
            }
            runGap(Path.of(args[1]));
            return;
        }
        System.err.println("Unknown command: " + args[0]);
        printHelp();
        System.exit(2);
    }

    static void runGap(Path demandCsv) throws IOException {
        String csv = Files.readString(demandCsv);
        DemandSeries demand = DemandCsvLoader.load("voice", "inbound", csv);
        List<AgentSchedule> schedules = demoSchedules(demand);
        GapBoard board = new GapBoardBuilder(0.80, 20).build(demand, schedules);
        ScheduleDelta delta = new SimpleDeltaEngine().propose(board, schedules);

        System.out.println("Reeforce gap board — " + demand.skill() + "/" + demand.channel());
        System.out.printf("%-22s %10s %10s %10s%n", "interval_start", "required", "available", "gap");
        for (GapBoard.GapRow row : board.rows()) {
            System.out.printf(
                    "%-22s %10.1f %10.1f %10.1f%n",
                    row.interval().start(),
                    row.requiredStaff(),
                    row.availableStaff(),
                    row.gap()
            );
        }
        System.out.println();
        System.out.println("Proposed deltas: " + delta.actions().size());
        for (ScheduleDelta.DeltaAction action : delta.actions()) {
            System.out.println(" - " + action.type() + " agent=" + action.agentId()
                    + " from=" + action.from().start() + " to=" + action.to().start()
                    + " (" + action.rationale() + ")");
        }
    }

    /**
     * Synthetic roster for the demo: 8 agents available, except A3 on meal during the surge hour.
     */
    static List<AgentSchedule> demoSchedules(DemandSeries demand) {
        if (demand.points().isEmpty()) {
            return List.of();
        }
        Instant dayStart = demand.points().getFirst().interval().start();
        Instant dayEnd = demand.points().getLast().interval().end();
        TimeInterval fullDay = new TimeInterval(dayStart, dayEnd);

        List<AgentSchedule> schedules = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            String id = "A" + i;
            if (i == 3) {
                Instant mealStart = Instant.parse("2026-07-22T14:00:00Z");
                Instant mealEnd = Instant.parse("2026-07-22T14:30:00Z");
                schedules.add(new AgentSchedule(id, List.of(
                        new AgentSchedule.ScheduleBlock(
                                new TimeInterval(dayStart, mealStart), AgentSchedule.State.AVAILABLE),
                        new AgentSchedule.ScheduleBlock(
                                new TimeInterval(mealStart, mealEnd), AgentSchedule.State.MEAL),
                        new AgentSchedule.ScheduleBlock(
                                new TimeInterval(mealEnd, dayEnd), AgentSchedule.State.AVAILABLE)
                )));
            } else {
                schedules.add(new AgentSchedule(id, List.of(
                        new AgentSchedule.ScheduleBlock(fullDay, AgentSchedule.State.AVAILABLE)
                )));
            }
        }
        return schedules;
    }

    private static void printHelp() {
        System.out.println("""
                Reeforce — Plan the day. Replan the hour.
                
                Usage:
                  reeforce gap <demand.csv>   Compute gap board + meal-move deltas
                  reeforce help               Show this help
                """);
    }
}
