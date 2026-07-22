# Benchmark: lunch-break service-level cliff

Classic failure mode: **six agents** take meal in the same 12:00–12:30 UTC window
while inbound volume peaks. Available staff cliffs; required staff (Erlang-C 80/20)
stays high → gap board shows a sharp understaffed spike.

## Files

| File | Role |
|------|------|
| `demand.csv` | 15-minute voice demand; lunch surge at 12:00–12:45 |
| `roster.csv` | 30 agents; A1–A6 clustered on meal at 12:00–12:30 |
| `meal-windows.csv` | Contractual meal start windows for A1–A6 (11:30–13:30) |

## Expected gap shape

- Pre-lunch (11:00–12:00): mild/near-zero gap — ~24–30 available vs mid-teens required.
- Cliff (12:00–12:30): available drops by ~6; required jumps with volume → **peak gap ≫ 5**.
- Post-lunch (12:30+): available recovers; gap shrinks even if volume stays elevated briefly.

## RTA narrative (manual)

1. Open the gap board; sort by gap descending — 12:00 and 12:15 should top the list.
2. Confirm six meals stacked on the cliff (roster), not a forecast miss alone.
3. Ask for meal-move deltas inside windows — prefer sliding 1–2 agents to 12:30+.
4. Re-check: peak gap should fall by roughly the number of meals moved off the cliff.
5. Do **not** cancel training across the whole afternoon to "fix" a 30-minute lunch cliff.

## Run

```bash
mvn -q -pl reeforce-cli -am package
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/lunch-sl-cliff/demand.csv --roster datasets/lunch-sl-cliff/roster.csv --meal-windows datasets/lunch-sl-cliff/meal-windows.csv"
```

Automated assertion: `LunchSlCliffBenchmarkTest` checks peak gap &gt; 5 and that a
windowed meal-move proposal exists for an agent currently on the cliff.
