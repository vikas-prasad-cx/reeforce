# Launch announcements (ready to paste)

Do **not** auto-post. Copy when you are ready.

Repo: https://github.com/vikas-prasad-cx/reeforce

---

## LinkedIn (short)

**Plan the day. Replan the hour.**

Shipping **Reeforce** — open-source intraday WFM for contact centers.

Day-ahead tools (Timefold, OR-Tools) and forecast stacks (Nixtla) are strong. The missing piece is the **hour-of** control loop: sense the staffing gap, propose small contractual deltas (e.g. move a meal inside its window), close the lunch SL cliff.

Java 21 · Apache-2.0 · `mvn test` green

Try the lunch cliff demo and open good-first issues if you want to help (chat concurrency, adherence-by-TOD, statsforecast adapter).

https://github.com/vikas-prasad-cx/reeforce

---

## Discord / Slack (short)

**Reeforce is public:** open-source intraday WFM control plane for contact centers — gap board + mid-shift meal-move deltas.

Complements Timefold / Nixtla / pyworkforce (not a replacement).

```bash
git clone https://github.com/vikas-prasad-cx/reeforce.git && cd reeforce
mvn -q install -DskipTests
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/lunch-sl-cliff/demand.csv --roster datasets/lunch-sl-cliff/roster.csv --meal-windows datasets/lunch-sl-cliff/meal-windows.csv"
```

Good first issues: #2 #4 #7 → https://github.com/vikas-prasad-cx/reeforce
