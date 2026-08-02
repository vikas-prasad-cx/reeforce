# Benchmark: traffic corridor peak

Morning peak: corridor demand exceeds mainline throughput while a **spare lane
stays OFFLINE** on the cliff. Gap board flags UNDER; primary delta opens the
spare lane (`OPEN_SECTION`). If overflow capacity is unavailable, a timed
platoon pulse can be retimed (`RETIME_DEMAND`).

## Files

| File | Role |
|------|------|
| `demand.csv` | Vehicles per 15-minute interval; peak 11:30–12:00 |
| `roster.csv` | MAIN throughput 100; SPARE-LANE 50 offline on peak; PULSE-A timed release |
| `slot-windows.csv` | Optional retime window for PULSE-A |

## Expected gap shape

- Pre-peak: mild UNDER or near balance (100 available vs 80–90 demand)
- Peak (11:30–12:00): demand 140–150 vs 100 → **peak gap ≥ 40**
- Opening spare lane (+50) should cut peak gap roughly in half

## Run

```bash
mvn -q install -DskipTests
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/traffic-corridor-peak/demand.csv --domain traffic-corridor --roster datasets/traffic-corridor-peak/roster.csv --meal-windows datasets/traffic-corridor-peak/slot-windows.csv"
```

See [docs/use-cases/traffic-corridor.md](../../docs/use-cases/traffic-corridor.md).
