package ai.reeforce.cli;

import ai.reeforce.coverage.GapBoardBuilder;
import ai.reeforce.delta.SimpleDeltaEngine;
import ai.reeforce.model.AgentSchedule;
import ai.reeforce.model.DemandCsvLoader;
import ai.reeforce.model.DemandSeries;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.RosterCsvLoader;
import ai.reeforce.model.ScheduleDelta;
import ai.reeforce.model.ShrinkageCalendar;
import ai.reeforce.model.ShrinkageCsvLoader;
import ai.reeforce.model.TimeInterval;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Minimal CLI: reeforce gap &lt;demand.csv&gt; [--roster ...] [--meal-windows ...] [--shrinkage ...]
 */
public final class ReeforceCli {

    public static void main(String[] args) throws IOException {
        if (args.length == 0 || "help".equals(args[0]) || "--help".equals(args[0])) {
            printHelp();
            return;
        }
        if ("gap".equals(args[0])) {
            GapArgs gapArgs = GapArgs.parse(args);
            runGap(gapArgs);
            return;
        }
        System.err.println("Unknown command: " + args[0]);
        printHelp();
        System.exit(2);
    }

    static void runGap(GapArgs gapArgs) throws IOException {
        String csv = Files.readString(gapArgs.demandCsv);
        DemandSeries demand = DemandCsvLoader.load("voice", "inbound", csv);

        List<AgentSchedule> schedules;
        if (gapArgs.rosterCsv != null) {
            String roster = Files.readString(gapArgs.rosterCsv);
            String windows = gapArgs.mealWindowsCsv == null ? null : Files.readString(gapArgs.mealWindowsCsv);
            schedules = RosterCsvLoader.load(roster, windows);
        } else {
            schedules = demoSchedules(demand);
        }

        ShrinkageCalendar shrinkage = gapArgs.shrinkageCsv == null
                ? new ShrinkageCalendar(List.of())
                : ShrinkageCsvLoader.load(Files.readString(gapArgs.shrinkageCsv));

        GapBoard board = new GapBoardBuilder(0.80, 20, shrinkage).build(demand, schedules);
        ScheduleDelta delta = new SimpleDeltaEngine().propose(board, schedules);

        double peakGap = board.rows().stream().mapToDouble(GapBoard.GapRow::gap).max().orElse(0);
        long under = board.rows().stream().filter(GapBoard.GapRow::understaffed).count();

        System.out.println("Reeforce gap board — " + demand.skill() + "/" + demand.channel());
        System.out.println("intervals=" + board.rows().size()
                + " understaffed=" + under
                + " peak_gap=" + String.format(Locale.ROOT, "%.1f", peakGap)
                + " roster=" + schedules.size() + " agents"
                + (gapArgs.shrinkageCsv != null ? " shrinkage=on" : ""));
        System.out.printf(Locale.ROOT, "%-22s %10s %10s %10s %s%n",
                "interval_start", "required", "available", "gap", "flag");
        for (GapBoard.GapRow row : board.rows()) {
            String flag = row.understaffed() ? "UNDER" : (row.gap() < -0.5 ? "OVER" : "ok");
            System.out.printf(
                    Locale.ROOT,
                    "%-22s %10.1f %10.1f %10.1f %s%n",
                    row.interval().start(),
                    row.requiredStaff(),
                    row.availableStaff(),
                    row.gap(),
                    flag
            );
        }
        System.out.println();
        System.out.println("Proposed deltas: " + delta.actions().size());
        for (ScheduleDelta.DeltaAction action : delta.actions()) {
            System.out.println(" - " + action.type() + " agent=" + action.agentId()
                    + " from=" + action.from().start() + "→" + action.from().end()
                    + " to=" + action.to().start() + "→" + action.to().end()
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

        Instant mealStart = Instant.parse("2026-07-22T14:00:00Z");
        Instant mealEnd = Instant.parse("2026-07-22T14:30:00Z");
        AgentSchedule.MealWindow window = new AgentSchedule.MealWindow(
                Instant.parse("2026-07-22T13:30:00Z"),
                Instant.parse("2026-07-22T15:00:00Z")
        );

        List<AgentSchedule> schedules = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            String id = "A" + i;
            if (i == 3) {
                schedules.add(new AgentSchedule(id, List.of(
                        new AgentSchedule.ScheduleBlock(
                                new TimeInterval(dayStart, mealStart), AgentSchedule.State.AVAILABLE),
                        new AgentSchedule.ScheduleBlock(
                                new TimeInterval(mealStart, mealEnd), AgentSchedule.State.MEAL),
                        new AgentSchedule.ScheduleBlock(
                                new TimeInterval(mealEnd, dayEnd), AgentSchedule.State.AVAILABLE)
                ), window));
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
                  reeforce gap <demand.csv> [options]
                
                Options:
                  --roster <roster.csv>              Published agent blocks
                  --meal-windows <windows.csv>       Contractual meal start windows
                  --shrinkage <shrinkage.csv>        Shrinkage calendar
                
                Commands:
                  reeforce help                      Show this help
                
                Examples:
                  reeforce gap datasets/demo-voice-surge/demand.csv
                  reeforce gap datasets/lunch-sl-cliff/demand.csv \\
                    --roster datasets/lunch-sl-cliff/roster.csv \\
                    --meal-windows datasets/lunch-sl-cliff/meal-windows.csv
                """);
    }

    record GapArgs(Path demandCsv, Path rosterCsv, Path mealWindowsCsv, Path shrinkageCsv) {
        static GapArgs parse(String[] args) {
            if (args.length < 2) {
                System.err.println("Usage: reeforce gap <demand.csv> [--roster ...] [--meal-windows ...] [--shrinkage ...]");
                System.exit(2);
            }
            Path demand = Path.of(args[1]);
            Path roster = null;
            Path meals = null;
            Path shrinkage = null;
            for (int i = 2; i < args.length; i++) {
                switch (args[i]) {
                    case "--roster" -> roster = Path.of(requireValue(args, ++i, "--roster"));
                    case "--meal-windows" -> meals = Path.of(requireValue(args, ++i, "--meal-windows"));
                    case "--shrinkage" -> shrinkage = Path.of(requireValue(args, ++i, "--shrinkage"));
                    default -> {
                        System.err.println("Unknown option: " + args[i]);
                        System.exit(2);
                    }
                }
            }
            return new GapArgs(demand, roster, meals, shrinkage);
        }

        private static String requireValue(String[] args, int index, String flag) {
            if (index >= args.length) {
                System.err.println("Missing value for " + flag);
                System.exit(2);
            }
            return args[index];
        }
    }
}
