package ai.reeforce.model;

import java.util.Locale;

/**
 * Planning domain for the gap → delta control loop.
 *
 * <p>Capacity units differ by domain (agents, conference slots, seats, vehicles)
 * but the control-plane shape stays the same.
 */
public enum DomainProfile {
    /** Contact-center voice/chat; Erlang required staffing. */
    CONTACT_CENTER("contact-center", "voice", "inbound", true),
    /** Parent-teacher / school conference slot capacity. */
    SCHOOL_CONFERENCE("school-conference", "conference", "slots", false),
    /** Class section enrollment vs seat capacity. */
    CLASS_SECTION("class-section", "course", "seats", false),
    /** Corridor / link demand vs throughput. */
    TRAFFIC_CORRIDOR("traffic-corridor", "corridor", "vehicles", false);

    private final String cliName;
    private final String defaultSkill;
    private final String defaultChannel;
    private final boolean erlangStaffing;

    DomainProfile(String cliName, String defaultSkill, String defaultChannel, boolean erlangStaffing) {
        this.cliName = cliName;
        this.defaultSkill = defaultSkill;
        this.defaultChannel = defaultChannel;
        this.erlangStaffing = erlangStaffing;
    }

    public String cliName() {
        return cliName;
    }

    public String defaultSkill() {
        return defaultSkill;
    }

    public String defaultChannel() {
        return defaultChannel;
    }

    /** When true, required capacity uses Erlang-C/A; otherwise demand volume is used directly. */
    public boolean usesErlangStaffing() {
        return erlangStaffing;
    }

    public static DomainProfile fromCli(String raw) {
        if (raw == null || raw.isBlank()) {
            return CONTACT_CENTER;
        }
        String key = raw.trim().toLowerCase(Locale.ROOT);
        for (DomainProfile profile : values()) {
            if (profile.cliName.equals(key) || profile.name().equalsIgnoreCase(key)) {
                return profile;
            }
        }
        throw new IllegalArgumentException(
                "Unknown domain '" + raw + "'. Expected: contact-center, school-conference, "
                        + "class-section, traffic-corridor");
    }
}
