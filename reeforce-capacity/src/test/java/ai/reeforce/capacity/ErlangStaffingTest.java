package ai.reeforce.capacity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ErlangStaffingTest {

    @Test
    void higherVolumeNeedsMoreStaff() {
        double low = ErlangStaffing.requiredStaff(20, 300, 900, 0.80, 20);
        double high = ErlangStaffing.requiredStaff(80, 300, 900, 0.80, 20);
        assertTrue(high > low);
    }

    @Test
    void zeroVolumeYieldsNearZeroStaff() {
        double n = ErlangStaffing.requiredStaff(0, 300, 900, 0.80, 20);
        assertTrue(n >= 0 && n < 1.0);
    }
}
