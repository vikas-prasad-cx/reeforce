# Use case: class section overfill

At registration open, seat demand can spike above published section capacity while
an overflow section sits unused (offline). Planners need a clear gap signal and a
low-disruption action: open overflow or move demand — not a full re-timetable.

## Control loop

1. **Sense** — seat requests by registration window (SIS export or forecast)
2. **Compare** — required seats vs open section capacity (`capacity` column)
3. **Act** — `OPEN_SECTION` brings offline overflow capacity onto the peak
4. **Learn** — whether opening overflow closed the waitlist cliff

## Demo

[`datasets/class-section-overfill/`](../../datasets/class-section-overfill/)

```bash
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/class-section-overfill/demand.csv --domain class-section --roster datasets/class-section-overfill/roster.csv"
```

## Complements

- Full student sectioning / course timetabling → UniTime / FET
- Forecast of enrollment pressure → skforecast / NeuralForecast
- Reeforce → live seat gap + open-overflow delta during registration
