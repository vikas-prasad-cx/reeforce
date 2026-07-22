package ai.reeforce.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Planned non-productive capacity by interval (meetings, training, etc.).
 */
public record ShrinkageCalendar(List<ShrinkageEntry> entries) {

    public ShrinkageCalendar {
        Objects.requireNonNull(entries, "entries");
        entries = List.copyOf(entries);
    }

    /**
     * Effective shrinkage fraction in [0,1] for an interval after optional skill/site filters.
     * When multiple rows overlap, fractions are combined as {@code 1 - Π(1 - f_i)} (independent).
     * {@code headcountOffline} rows are converted using {@code scheduledHeadcount} when provided.
     */
    public double shrinkageFraction(
            TimeInterval interval,
            Optional<String> skill,
            Optional<String> site,
            double scheduledHeadcount
    ) {
        double remaining = 1.0;
        for (ShrinkageEntry entry : entries) {
            if (!overlaps(entry.interval(), interval)) {
                continue;
            }
            if (skill.isPresent() && entry.skill().isPresent() && !entry.skill().get().equals(skill.get())) {
                continue;
            }
            if (site.isPresent() && entry.site().isPresent() && !entry.site().get().equals(site.get())) {
                continue;
            }
            double fraction = entry.fraction(scheduledHeadcount);
            remaining *= (1.0 - Math.clamp(fraction, 0.0, 1.0));
        }
        return 1.0 - remaining;
    }

    private static boolean overlaps(TimeInterval a, TimeInterval b) {
        return a.start().isBefore(b.end()) && b.start().isBefore(a.end());
    }

    public record ShrinkageEntry(
            TimeInterval interval,
            Optional<Double> shrinkageFraction,
            Optional<Double> headcountOffline,
            String reason,
            Optional<String> skill,
            Optional<String> site,
            Optional<String> orgUnit
    ) {
        public ShrinkageEntry {
            Objects.requireNonNull(interval, "interval");
            Objects.requireNonNull(shrinkageFraction, "shrinkageFraction");
            Objects.requireNonNull(headcountOffline, "headcountOffline");
            Objects.requireNonNull(reason, "reason");
            Objects.requireNonNull(skill, "skill");
            Objects.requireNonNull(site, "site");
            Objects.requireNonNull(orgUnit, "orgUnit");
            if (shrinkageFraction.isEmpty() && headcountOffline.isEmpty()) {
                throw new IllegalArgumentException("need shrinkage_fraction or headcount_offline");
            }
            shrinkageFraction.ifPresent(f -> {
                if (f < 0 || f > 1) {
                    throw new IllegalArgumentException("shrinkage_fraction must be in [0,1]");
                }
            });
            headcountOffline.ifPresent(h -> {
                if (h < 0) {
                    throw new IllegalArgumentException("headcount_offline must be >= 0");
                }
            });
        }

        double fraction(double scheduledHeadcount) {
            if (shrinkageFraction.isPresent()) {
                return shrinkageFraction.get();
            }
            if (scheduledHeadcount <= 0) {
                return 0.0;
            }
            return Math.min(1.0, headcountOffline.orElse(0.0) / scheduledHeadcount);
        }
    }
}
