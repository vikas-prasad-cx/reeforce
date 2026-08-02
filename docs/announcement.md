# Launch announcements (ready to paste)

Do **not** auto-post. Copy when you are ready.

Repo: https://github.com/vikas-prasad-cx/reeforce

---

## LinkedIn (short) — planners / ops

**Plan the day. Replan the hour.**

Shipping **Reeforce** — open-source intraday capacity control plane.

Day-ahead tools (Timefold, UniTime, FET) and forecast stacks (Nixtla, skforecast) are strong. The missing piece is the **hour-of** loop: sense the gap, propose small contractual deltas, close the cliff.

Same control surface across:

- Contact centers — lunch SL cliffs / meal moves
- Schools — parent-teacher slot cliffs / section overfills
- Corridor ops — peak overload / open spare capacity

Java 21 · Apache-2.0 · `mvn test` green

Try the demos and open good-first issues if you want to help (chat concurrency, adherence-by-TOD, statsforecast + per-domain forecast adapters #9–#11).

https://github.com/vikas-prasad-cx/reeforce

---

## LinkedIn (short) — contact-center focused

**Plan the day. Replan the hour.**

Shipping **Reeforce** — open-source intraday WFM for contact centers.

Day-ahead tools (Timefold, OR-Tools) and forecast stacks (Nixtla) are strong. The missing piece is the **hour-of** control loop: sense the staffing gap, propose small contractual deltas (e.g. move a meal inside its window), close the lunch SL cliff.

Also ships school-conference and corridor capacity demos for the same gap → delta pattern.

Java 21 · Apache-2.0 · `mvn test` green

https://github.com/vikas-prasad-cx/reeforce

---

## Discord / Slack (short)

**Reeforce is public:** open-source intraday capacity control plane — gap board + mid-horizon deltas for contact centers, school conferences, class sections, and corridors.

Complements Timefold / UniTime / Nixtla / skforecast (not a replacement).

```bash
git clone https://github.com/vikas-prasad-cx/reeforce.git && cd reeforce
mvn -q install -DskipTests
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/school-conference-cliff/demand.csv --domain school-conference --roster datasets/school-conference-cliff/roster.csv --meal-windows datasets/school-conference-cliff/slot-windows.csv"
```

Good first issues: #2 #4 #7 #9 #10 #11 → https://github.com/vikas-prasad-cx/reeforce
