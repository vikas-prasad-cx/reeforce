# Use case: traffic corridor peak

Ops planners watch corridor demand vs throughput. When the morning peak exceeds
mainline capacity while a spare lane is held offline, the gap board makes the
overload obvious and proposes opening spare capacity (or retiming a pulse).

## Control loop

1. **Sense** — vehicles per interval (detector / forecast)
2. **Compare** — required vs corridor throughput (`capacity` = vehicles/interval)
3. **Act** — `OPEN_SECTION` opens a spare lane; fallback `RETIME_DEMAND` slides a pulse
4. **Learn** — which actions recovered level-of-service on the peak

## Demo

[`datasets/traffic-corridor-peak/`](../../datasets/traffic-corridor-peak/)

```bash
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/traffic-corridor-peak/demand.csv --domain traffic-corridor --roster datasets/traffic-corridor-peak/roster.csv --meal-windows datasets/traffic-corridor-peak/slot-windows.csv"
```

## Complements

- Full traffic assignment / four-step models → AequilibraE / dyntapy
- Demand forecasting → skforecast / NeuralForecast
- Reeforce → interval gap board + explainable capacity/retime deltas
