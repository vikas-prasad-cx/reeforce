package ai.reeforce.capacity;

import ai.reeforce.model.DomainProfile;

/**
 * Selects the required-capacity formula for a {@link DomainProfile}.
 */
public final class DomainCapacity {

    private DomainCapacity() {
    }

    public static double required(
            DomainProfile domain,
            double offeredVolume,
            double ahtSeconds,
            long intervalSeconds,
            double serviceLevel,
            double targetAnswerSeconds
    ) {
        if (domain.usesErlangStaffing()) {
            return ErlangStaffing.requiredStaff(
                    offeredVolume, ahtSeconds, intervalSeconds, serviceLevel, targetAnswerSeconds);
        }
        return DirectCapacity.requiredCapacity(offeredVolume);
    }
}
