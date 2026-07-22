# Datasets

## Demand CSV

```text
start_iso,end_iso,offered_volume,aht_seconds
```

ISO-8601 instants (UTC recommended). Used by `DemandCsvLoader`.

## Roster CSV

```text
agent_id,start_iso,end_iso,state
```

`state` ∈ `AVAILABLE`, `MEAL`, `BREAK`, `TRAINING`, `OFFLINE`.

Optional meal windows:

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

| Path | Purpose |
|------|---------|
| `demo-voice-surge/` | Small surge + single meal for CLI smoke |
| `lunch-sl-cliff/` | Clustered lunches → SL cliff benchmark |
| `examples/shrinkage-calendar.csv` | Shrinkage schema example |
