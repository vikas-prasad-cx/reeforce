package ai.reeforce.capacity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden vectors for Erlang-C / Erlang-A required staffing.
 * See {@code docs/erlang-a.md} for sources and input definitions.
 * Tolerance: ±1 agent vs published WFM calculator tables.
 */
class ErlangStaffingTest {

    @ParameterizedTest(name = "vol={0} aht={1} interval={2} sl={3}/{4}s → ~{5} agents")
    @CsvSource({
            // volume, aht_s, interval_s, SL, T_s, expected_agents
            // Classic: 100 calls / 30 min, AHT 180s → a=10; 80/20 ≈ 14 agents
            "100, 180, 1800, 0.80, 20, 14",
            // Half load: a=5; 80/20 ≈ 8 agents
            "50, 180, 1800, 0.80, 20, 8",
            // Heavier 15-min: 40 contacts, AHT 300s → a≈13.33; 80/20 ≈ 18 agents
            "40, 300, 900, 0.80, 20, 18",
            // Stricter SL: a=10; 90/15 needs more than 80/20
            "100, 180, 1800, 0.90, 15, 15",
            // Light voice interval: a≈6.67; 80/20 ≈ 10 agents
            "20, 300, 900, 0.80, 20, 10",
            // Surge-ish: a≈31.67; 80/20 ≈ 37 agents
            "95, 300, 900, 0.80, 20, 37"
    })
    void goldenVectorsWithinOneAgent(
            double volume,
            double aht,
            long interval,
            double sl,
            double targetAnswer,
            int expected
    ) {
        double n = ErlangStaffing.requiredStaff(volume, aht, interval, sl, targetAnswer);
        assertEquals(expected, n, 1.0, "staffing should match reference within ±1");
        // Returned value must actually meet SL under Erlang-C.
        double a = ErlangStaffing.trafficIntensity(volume, aht, interval);
        assertTrue(ErlangStaffing.serviceLevel((int) n, a, aht, targetAnswer, Double.POSITIVE_INFINITY) >= sl - 1e-9);
        if (n > a + 1) {
            assertTrue(ErlangStaffing.serviceLevel((int) n - 1, a, aht, targetAnswer, Double.POSITIVE_INFINITY) < sl);
        }
    }

    @Test
    void higherVolumeNeedsMoreStaff() {
        double low = ErlangStaffing.requiredStaff(20, 300, 900, 0.80, 20);
        double high = ErlangStaffing.requiredStaff(80, 300, 900, 0.80, 20);
        assertTrue(high > low);
    }

    @Test
    void zeroVolumeYieldsZeroStaff() {
        assertEquals(0.0, ErlangStaffing.requiredStaff(0, 300, 900, 0.80, 20));
    }

    @Test
    void erlangAWithPatienceDoesNotIncreaseStaffVsErlangC() {
        double erlangC = ErlangStaffing.requiredStaff(100, 180, 1800, 0.80, 20);
        double erlangA = ErlangStaffing.requiredStaff(100, 180, 1800, 0.80, 20, 60.0);
        assertTrue(erlangA <= erlangC, "abandonment should not raise required staff for same SL");
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> ErlangStaffing.requiredStaff(-1, 300, 900, 0.80, 20));
        assertThrows(IllegalArgumentException.class,
                () -> ErlangStaffing.requiredStaff(10, 300, 900, 1.5, 20));
    }
}
