package ai.reeforce.capacity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DirectCapacityTest {

    @Test
    void mapsVolumeOneToOne() {
        assertEquals(42.0, DirectCapacity.requiredCapacity(42.0));
        assertEquals(0.0, DirectCapacity.requiredCapacity(0.0));
    }

    @Test
    void rejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> DirectCapacity.requiredCapacity(-1));
    }

    @Test
    void domainCapacityDispatches() {
        assertEquals(12.0, DomainCapacity.required(
                ai.reeforce.model.DomainProfile.SCHOOL_CONFERENCE, 12, 900, 900, 0.8, 20));
        assertEquals(100.0, DomainCapacity.required(
                ai.reeforce.model.DomainProfile.TRAFFIC_CORRIDOR, 100, 1, 900, 0.8, 20));
    }
}
