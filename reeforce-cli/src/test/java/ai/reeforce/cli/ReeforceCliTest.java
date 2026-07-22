package ai.reeforce.cli;

import ai.reeforce.coverage.GapBoardBuilder;
import ai.reeforce.delta.SimpleDeltaEngine;
import ai.reeforce.model.DemandCsvLoader;
import ai.reeforce.model.DemandSeries;
import ai.reeforce.model.GapBoard;
import ai.reeforce.model.ScheduleDelta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReeforceCliTest {

    @Test
    void gapPathProducesBoardAndDeltas() {
        String csv = """
                start_iso,end_iso,offered_volume,aht_seconds
                2026-07-22T13:00:00Z,2026-07-22T13:15:00Z,22,300
                2026-07-22T14:00:00Z,2026-07-22T14:15:00Z,90,300
                2026-07-22T14:15:00Z,2026-07-22T14:30:00Z,95,300
                2026-07-22T15:00:00Z,2026-07-22T15:15:00Z,25,300
                """;
        DemandSeries demand = DemandCsvLoader.load("voice", "inbound", csv);
        var schedules = ReeforceCli.demoSchedules(demand);
        GapBoard board = new GapBoardBuilder(0.80, 20).build(demand, schedules);
        ScheduleDelta delta = new SimpleDeltaEngine().propose(board, schedules);

        assertFalse(board.rows().isEmpty());
        assertTrue(board.rows().stream().anyMatch(GapBoard.GapRow::understaffed));
        assertFalse(delta.actions().isEmpty());
    }
}
