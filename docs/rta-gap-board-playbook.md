# RTA gap-board playbook

Operator-facing cheat sheet for reading Reeforce `gap` output in under three minutes.

## 60-second scan

1. Read the **header** — `understaffed`, `peak_gap`, `roster`.
2. Find every **UNDER** row; sort mentally by `gap` descending.
3. Ask *why* available fell: stacked meals, shrinkage, or demand jump?
4. If a **MOVE_MEAL** is proposed, check the contractual window before acting.
5. Re-run after one move — success means `peak_gap` drops, not that the whole day is flat.

## Columns

| Column | Meaning |
|--------|---------|
| `interval_start` | Start of the staffing interval (UTC in demos) |
| `required` | Agents needed for the SL target (Erlang-C / Erlang-A) |
| `available` | Agents in AVAILABLE state (meals / shrinkage reduce this) |
| `gap` | `required − available` (positive = short) |
| `flag` | Quick status — see below |

Header line also reports `intervals`, `understaffed` count, `peak_gap`, and roster size.

## UNDER / OVER / ok / peak_gap

| Flag | Rule of thumb | What to do |
|------|---------------|------------|
| **UNDER** | `gap > 0` — short on staff this interval | Prioritize: sort by gap descending; check meals / shrinkage stacked on the cliff |
| **OVER** | `gap < −0.5` — spare capacity | Candidate *destination* for meal moves off a cliff; not a reason to cancel productive work wholesale |
| **ok** | Near-zero gap | Leave alone unless adjacent UNDER intervals need a move |

**`peak_gap`** — largest positive gap on the board. Use it as the severity signal (lunch-sl-cliff expects `peak_gap > 5`). Closing a cliff usually means shrinking `peak_gap`, not flattening the whole afternoon.

## When to trust a meal-move delta

Trust the proposal when **all** of these hold:

- The agent’s current meal overlaps an UNDER interval (they are part of the cliff).
- Proposed `to` start stays inside the contractual meal window.
- The move frees capacity on the peak without creating a worse cliff next door.
- You can name the root cause (stacked meals / shrinkage), not only “gap is red.”

Treat the delta as a **suggestion with a blast radius of one agent**, not an auto-apply.

## What not to do

- Do not cancel training all afternoon to “fix” a 30-minute lunch cliff.
- Do not move meals outside published windows.
- Do not treat forecast error and clustered shrinkage as the same root cause — check the roster.
- Do not chase every UNDER row; fix `peak_gap` first, then re-evaluate.
- Do not assume OVER means “send people home” — it often means “safe place to land a meal.”

---

## Walkthrough A — lunch SL cliff

Fixture: [datasets/lunch-sl-cliff/](../datasets/lunch-sl-cliff/). Six meals stacked on 12:00–12:30 while volume peaks.

```bash
mvn -q install -DskipTests
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/lunch-sl-cliff/demand.csv --roster datasets/lunch-sl-cliff/roster.csv --meal-windows datasets/lunch-sl-cliff/meal-windows.csv"
```

### What you should see

```text
Reeforce gap board — voice/inbound
intervals=12 understaffed=2 peak_gap=7.0 roster=30 agents
...
2026-07-22T12:00:00Z         29.0       24.0        5.0 UNDER
2026-07-22T12:15:00Z         31.0       24.0        7.0 UNDER
2026-07-22T12:30:00Z         28.0       30.0       -2.0 OVER
...
Proposed deltas: 1
 - MOVE_MEAL agent=A1 from=12:00→12:30 to=11:30→12:00 (...)
```

### How an RTA reads it

| Signal | Interpretation |
|--------|----------------|
| Pre-12:00 all **OVER** | Roster has spare; problem is not all-day understaffing |
| 12:00 / 12:15 **UNDER**, `peak_gap=7.0` | Cliff is sharp and short — classic lunch stack |
| 12:30 recovers to **OVER** | Capacity returns when meals end; don’t over-correct the afternoon |
| `MOVE_MEAL` A1 → 11:30 | Pull one meal off the cliff into an OVER interval inside the window |

**Action:** accept one in-window meal move, re-run, confirm `peak_gap` fell (~1 agent of relief per meal moved). Repeat only if still above policy.

---

## Walkthrough B — voice surge (demand-driven)

Fixture: [datasets/demo-voice-surge/](../datasets/demo-voice-surge/). Smaller roster; volume spikes mid-board. One agent is on meal during the peak.

```bash
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/demo-voice-surge/demand.csv"
```

### What you should see

```text
Reeforce gap board — voice/inbound
intervals=10 understaffed=10 peak_gap=30.0 roster=8 agents
...
2026-07-22T14:00:00Z         34.0        7.0       27.0 UNDER
2026-07-22T14:15:00Z         37.0        7.0       30.0 UNDER
...
Proposed deltas: 1
 - MOVE_MEAL agent=A3 from=14:00→14:30 to=13:30→14:00 (...)
```

### How an RTA reads it

| Signal | Interpretation |
|--------|----------------|
| **Every** row UNDER, `peak_gap=30` | This is mostly a **demand / headcount** problem, not only meal stacking |
| Available drops 8 → 7 at 14:00 | Meal on A3 worsens an already-short board |
| `MOVE_MEAL` still proposed | Correct local fix (free 1 on the peak) — but it will **not** close a 30-agent cliff |

**Action:** take the meal move if in-window (cheap win), then escalate for OT / skill borrow / traffic control. A meal delta is not a substitute for missing bodies.

### Contrast with lunch cliff

| | Lunch cliff | Voice surge |
|--|-------------|-------------|
| Root cause | Clustered meals on a peak | Offered volume ≫ roster |
| Meal move impact | Often enough to kill `peak_gap` | Marginal; buy minutes while you escalate |
| First question | “Who can slide lunch?” | “Do we need OT / divert?” |

---

## Related

- [glossary.md](glossary.md)
- [thesis.md](thesis.md)
- [datasets/lunch-sl-cliff/README.md](../datasets/lunch-sl-cliff/README.md)
- Sample output in the [README](../README.md#sample-gap-board-output-lunch-sl-cliff)
