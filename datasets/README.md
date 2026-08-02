# Datasets

## Demand CSV

```text
start_iso,end_iso,offered_volume,aht_seconds
```

ISO-8601 instants (UTC recommended). Used by `DemandCsvLoader`.

For non-Erlang domains (`school-conference`, `class-section`, `traffic-corridor`),
`offered_volume` is required capacity directly; `aht_seconds` is kept for schema
compatibility (use interval length, e.g. `900`).

## Roster CSV

```text
agent_id,start_iso,end_iso,state[,capacity]
```

`state` ∈ `AVAILABLE`, `MEAL`, `BREAK`, `TRAINING`, `OFFLINE`.

Optional `capacity` (default `1.0`) scales AVAILABLE coverage — seats in a
section or vehicles/interval of corridor throughput.

Optional meal/slot windows:

```text
agent_id,meal_earliest_start,meal_latest_start
```

## Shrinkage calendar CSV

Example: [`examples/shrinkage-calendar.csv`](examples/shrinkage-calendar.csv)

| Column | Required | Meaning |
|--------|----------|---------|
| `start_iso` | yes | Interval start |
| `end_iso` | yes | Interval end |
| `shrinkage_fraction` | one of | Fraction of available headcount offline in [0,1] |
| `headcount_offline` | one of | Absolute heads offline (converted using scheduled available) |
| `reason` | no | Free-text reason code |
| `skill` | no | Optional skill filter (empty = all) |
| `site` | no | Optional site filter |
| `org_unit` | no | Optional org unit |

Intervals should align with demand CSV boundaries when possible. Overlapping rows
combine as independent fractions: `1 − Π(1 − f_i)`.

Loaded by `ShrinkageCsvLoader` and applied in `GapBoardBuilder` as
`available *= (1 − shrinkage)`.

## Fixtures

| Path | Domain | Purpose |
|------|--------|---------|
| `demo-voice-surge/` | contact-center | Small surge + single meal for CLI smoke |
| `lunch-sl-cliff/` | contact-center | Clustered lunches → SL cliff benchmark |
| `school-conference-cliff/` | school-conference | Parent-teacher slot cliff + `MOVE_SLOT` |
| `class-section-overfill/` | class-section | Registration overfill + `OPEN_SECTION` |
| `traffic-corridor-peak/` | traffic-corridor | Morning peak + open spare lane |
| `examples/shrinkage-calendar.csv` | — | Shrinkage schema example |
