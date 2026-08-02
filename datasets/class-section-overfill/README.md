# Benchmark: class section overfill

Registration-open failure mode: enrollment demand for a popular course spikes
above open seat capacity while an **overflow section stays OFFLINE** on the peak
intervals. Gap board shows UNDER; delta proposes `OPEN_SECTION`.

## Files

| File | Role |
|------|------|
| `demand.csv` | Seat requests by 15-minute registration window |
| `roster.csv` | SEC-A (30) + SEC-B (25) open; SEC-OVERFLOW (25) offline on the cliff |

## Expected gap shape

- Early windows: mild UNDER or near balance (55 seats vs 40–45 demand)
- Cliff (14:30–15:00): demand 70–75 vs 55 open seats → **peak gap ≥ 15**
- After 15:00: overflow already AVAILABLE → gap shrinks

## Run

```bash
mvn -q install -DskipTests
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/class-section-overfill/demand.csv --domain class-section --roster datasets/class-section-overfill/roster.csv"
```

Delta: `OPEN_SECTION` brings `SEC-OVERFLOW` online on the peak (+25 seats).

See [docs/use-cases/class-section.md](../../docs/use-cases/class-section.md).
