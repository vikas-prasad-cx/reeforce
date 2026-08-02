# Use case: school conference / parent-teacher slots

Planners need to keep appointment demand inside teacher×room capacity during
conference evening — especially when popular teachers stack prep on the same
window parents prefer.

## Control loop

1. **Sense** — appointment demand by 15-minute slot (from booking tool or forecast)
2. **Compare** — required slots vs teachers marked AVAILABLE → gap board
3. **Act** — `MOVE_SLOT` slides a prep/sync block inside the contractual window
4. **Learn** — which moves actually raised show-up capacity on the cliff

## Demo

[`datasets/school-conference-cliff/`](../../datasets/school-conference-cliff/)

```bash
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/school-conference-cliff/demand.csv --domain school-conference --roster datasets/school-conference-cliff/roster.csv --meal-windows datasets/school-conference-cliff/slot-windows.csv"
```

## Complements (not replacements)

- Day-ahead room/teacher assignment → UniTime / FET / Timefold
- Parent booking UX → commercial PTC tools
- Reeforce → hour-of gap board + feasible slot moves when the cliff appears
