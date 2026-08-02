# Benchmark: school conference slot cliff

Classic planner failure mode: **six popular teachers** stack prep / team sync
(`TRAINING`) on the same 18:00–18:30 UTC window while parent appointment demand
peaks. Available conference slots cliff; required slots stay high → gap board
shows a sharp understaffed spike.

## Files

| File | Role |
|------|------|
| `demand.csv` | 15-minute appointment demand; cliff at 18:00–18:30 |
| `roster.csv` | 14 teachers; T1–T6 clustered on TRAINING at 18:00–18:30 |
| `slot-windows.csv` | Contractual prep/slot start windows for T1–T6 |

## Expected gap shape

- Pre-cliff (17:00–18:00): OVER — ~14 available vs ~8–11 required
- Cliff (18:00–18:30): available drops by ~6; demand jumps → **peak gap ≫ 5**
- Post-cliff (18:30+): available recovers

## Run

```bash
mvn -q install -DskipTests
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/school-conference-cliff/demand.csv --domain school-conference --roster datasets/school-conference-cliff/roster.csv --meal-windows datasets/school-conference-cliff/slot-windows.csv"
```

Delta: `MOVE_SLOT` slides one teacher's prep block inside the window to free capacity on the cliff.

See [docs/use-cases/school-conference.md](../../docs/use-cases/school-conference.md).
