package ai.reeforce.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RosterCsvLoaderTest {

    @Test
    void loadsBlocksAndMealWindows() {
        String roster = """
                agent_id,start_iso,end_iso,state
                A1,2026-07-22T13:00:00Z,2026-07-22T14:00:00Z,AVAILABLE
                A1,2026-07-22T14:00:00Z,2026-07-22T14:30:00Z,MEAL
                A1,2026-07-22T14:30:00Z,2026-07-22T16:00:00Z,AVAILABLE
                """;
        String windows = """
                agent_id,meal_earliest_start,meal_latest_start
                A1,2026-07-22T13:30:00Z,2026-07-22T15:00:00Z
                """;
        List<AgentSchedule> schedules = RosterCsvLoader.load(roster, windows);
        assertEquals(1, schedules.size());
        assertEquals(3, schedules.getFirst().blocks().size());
        assertEquals(AgentSchedule.State.MEAL, schedules.getFirst().blocks().get(1).state());
        assertTrue(schedules.getFirst().mealWindowOptional().isPresent());
    }
}
