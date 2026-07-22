package ai.reeforce.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * CSV loader for published agent schedules.
 *
 * <p>Block file header:
 * {@code agent_id,start_iso,end_iso,state}
 *
 * <p>Optional meal-window file header:
 * {@code agent_id,meal_earliest_start,meal_latest_start}
 */
public final class RosterCsvLoader {

    private RosterCsvLoader() {
    }

    public static List<AgentSchedule> load(String rosterCsv) {
        return load(rosterCsv, null);
    }

    public static List<AgentSchedule> load(String rosterCsv, String mealWindowsCsv) {
        Map<String, List<AgentSchedule.ScheduleBlock>> blocksByAgent = new LinkedHashMap<>();
        String[] lines = rosterCsv.strip().split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")
                    || (i == 0 && line.toLowerCase(Locale.ROOT).startsWith("agent_id"))) {
                continue;
            }
            String[] parts = line.split(",", -1);
            if (parts.length < 4) {
                throw new IllegalArgumentException("Expected agent_id,start_iso,end_iso,state at line " + (i + 1));
            }
            String agentId = parts[0].trim();
            Instant start = Instant.parse(parts[1].trim());
            Instant end = Instant.parse(parts[2].trim());
            AgentSchedule.State state = AgentSchedule.State.valueOf(parts[3].trim().toUpperCase(Locale.ROOT));
            blocksByAgent
                    .computeIfAbsent(agentId, id -> new ArrayList<>())
                    .add(new AgentSchedule.ScheduleBlock(new TimeInterval(start, end), state));
        }

        Map<String, AgentSchedule.MealWindow> windows = new LinkedHashMap<>();
        if (mealWindowsCsv != null && !mealWindowsCsv.isBlank()) {
            String[] wlines = mealWindowsCsv.strip().split("\\R");
            for (int i = 0; i < wlines.length; i++) {
                String line = wlines[i].trim();
                if (line.isEmpty() || line.startsWith("#")
                        || (i == 0 && line.toLowerCase(Locale.ROOT).startsWith("agent_id"))) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 3) {
                    throw new IllegalArgumentException(
                            "Expected agent_id,meal_earliest_start,meal_latest_start at line " + (i + 1));
                }
                windows.put(
                        parts[0].trim(),
                        new AgentSchedule.MealWindow(
                                Instant.parse(parts[1].trim()),
                                Instant.parse(parts[2].trim())
                        )
                );
            }
        }

        List<AgentSchedule> schedules = new ArrayList<>();
        for (Map.Entry<String, List<AgentSchedule.ScheduleBlock>> e : blocksByAgent.entrySet()) {
            schedules.add(new AgentSchedule(
                    e.getKey(),
                    e.getValue(),
                    windows.get(e.getKey())
            ));
        }
        return schedules;
    }

    public static Optional<AgentSchedule.MealWindow> windowFor(List<AgentSchedule> schedules, String agentId) {
        return schedules.stream()
                .filter(s -> s.agentId().equals(agentId))
                .findFirst()
                .flatMap(AgentSchedule::mealWindowOptional);
    }
}
