# Demo: voice surge around lunch

Synthetic 15-minute inbound voice demand for a single skill.

- **Scenario:** steady mid-afternoon volume, then a sharp surge at 14:00–14:30 UTC while one agent is on meal.
- **Use:** `mvn -pl reeforce-cli -am package` then run the CLI `gap` command against `demand.csv`.
- **Columns:** `start_iso`, `end_iso`, `offered_volume`, `aht_seconds` (ISO-8601 instants, UTC).

This fixture is intentionally small so contributors can extend it into the lunch-break service-level cliff benchmark (see open issues).
