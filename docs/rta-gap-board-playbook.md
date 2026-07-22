# RTA gap-board playbook (stub)

Operator-facing cheat sheet for reading Reeforce `gap` output in under three minutes.

> **Status:** stub for launch. Expand via [#8](https://github.com/vikas-prasad-cx/reeforce/issues/8) — walkthroughs, screenshots, and “what not to do” welcome.

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
| **OVER** | `gap < −0.5` — spare capacity | Candidate source for meal moves *onto* this interval, not a reason to cancel productive work wholesale |
| **ok** | Near-zero gap | Leave alone unless adjacent UNDER intervals need a move |

**`peak_gap`** — largest positive gap in the board. Use it as the severity signal for the cliff (lunch-sl-cliff demo expects `peak_gap > 5`). Closing the cliff usually means shrinking `peak_gap`, not flattening the whole afternoon.

## When to trust a meal-move delta

- Agent’s current meal overlaps an UNDER interval.
- Proposed `to` start stays inside the contractual meal window.
- Move frees capacity on the peak without creating a worse cliff next door.

See the lunch cliff fixture: [datasets/lunch-sl-cliff/](../datasets/lunch-sl-cliff/).

## What not to do

- Do not cancel training all afternoon to “fix” a 30-minute lunch cliff.
- Do not move meals outside published windows.
- Do not treat forecast error and clustered shrinkage as the same root cause — check the roster.

## Related

- [glossary.md](glossary.md)
- [thesis.md](thesis.md)
- Demo output embedded in the [README](../README.md#sample-gap-board-output-lunch-sl-cliff)
