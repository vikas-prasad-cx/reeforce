# Reeforce

**Plan the day. Replan the hour.**

Open-source **intraday WFM control plane** for contact centers: turn live demand and real adherence into feasible mid-shift schedule deltas.

[![CI](https://github.com/vikas-prasad-cx/reeforce/actions/workflows/ci.yml/badge.svg)](https://github.com/vikas-prasad-cx/reeforce/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

## Why Reeforce

Day-ahead optimizers and forecast labs solve yesterday’s plan and tomorrow’s volume. RTAs still replan the **next hour** by instinct when lunches stack, adherence slips, or shrinkage lands unevenly.

Reeforce is the missing control loop: sense the gap → propose small, contractual schedule deltas → learn what closed the cliff.

It **complements** (does not replace):

| Project | Role vs Reeforce |
|---------|------------------|
| [Timefold](https://github.com/TimefoldAI/timefold-solver) | Day-ahead / constraint solve; Reeforce consumes published schedules and proposes intraday deltas |
| [Nixtla / statsforecast](https://github.com/Nixtla/statsforecast) | Demand forecasting; Reeforce consumes forecast series |
| [pyworkforce](https://github.com/CarlosHerreraC/pyworkforce) | Erlang / staffing utilities; Reeforce owns the live control loop |

## Quick start

Requires **Java 21+** and **Maven 3.9+**.

```bash
git clone https://github.com/vikas-prasad-cx/reeforce.git
cd reeforce
mvn test
```

Run the lunch SL-cliff demo (copy-paste):

```bash
mvn -q install -DskipTests
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/lunch-sl-cliff/demand.csv --roster datasets/lunch-sl-cliff/roster.csv --meal-windows datasets/lunch-sl-cliff/meal-windows.csv"
```

Smoke with the smaller surge fixture:

```bash
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/demo-voice-surge/demand.csv"
```

### Sample gap-board output (lunch-sl-cliff)

Six meals stacked on the 12:00–12:30 window while volume peaks — the board flags the cliff and proposes a meal move inside the contractual window:

```text
Reeforce gap board — voice/inbound
intervals=12 understaffed=2 peak_gap=7.0 roster=30 agents
interval_start           required  available        gap flag
2026-07-22T11:00:00Z         13.0       30.0      -17.0 OVER
2026-07-22T11:15:00Z         13.0       30.0      -17.0 OVER
2026-07-22T11:30:00Z         14.0       30.0      -16.0 OVER
2026-07-22T11:45:00Z         15.0       30.0      -15.0 OVER
2026-07-22T12:00:00Z         29.0       24.0        5.0 UNDER
2026-07-22T12:15:00Z         31.0       24.0        7.0 UNDER
2026-07-22T12:30:00Z         28.0       30.0       -2.0 OVER
2026-07-22T12:45:00Z         23.0       30.0       -7.0 OVER
2026-07-22T13:00:00Z         17.0       30.0      -13.0 OVER
2026-07-22T13:15:00Z         16.0       30.0      -14.0 OVER
2026-07-22T13:30:00Z         15.0       30.0      -15.0 OVER
2026-07-22T13:45:00Z         14.0       30.0      -16.0 OVER

Proposed deltas: 1
 - MOVE_MEAL agent=A1 from=2026-07-22T12:00:00Z→2026-07-22T12:30:00Z to=2026-07-22T11:30:00Z→2026-07-22T12:00:00Z (Peak understaffed gap at 2026-07-22T12:15:00Z (gap=7.0); move meal within window to free capacity)
```

How to read `UNDER` / `OVER` / `peak_gap`: [docs/rta-gap-board-playbook.md](docs/rta-gap-board-playbook.md).

Dataset schemas: [datasets/README.md](datasets/README.md). Staffing notes: [docs/erlang-a.md](docs/erlang-a.md).

## Thesis (short)

Intraday WFM is a closed-loop control problem, not a one-shot roster solve.

1. **Sense** — demand now / next hour, adherence-by-TOD, shrinkage calendar.
2. **Compare** — required vs available capacity → gap board.
3. **Act** — constrained deltas (move meal within window, pull OT, cancel activity).
4. **Learn** — which moves actually closed service-level cliffs.

See [docs/thesis.md](docs/thesis.md) and [docs/glossary.md](docs/glossary.md).

## Architecture

```text
DemandSeries ──► Capacity (required N) ──┐
                                         ├──► GapBoard ──► DeltaEngine ──► ScheduleDelta
AgentSchedule ─► Coverage (available N) ─┘
```

| Module | Role |
|--------|------|
| `reeforce-model` | Demand series, schedules, gap board, delta types, CSV loaders |
| `reeforce-capacity` | Erlang-C / Erlang-A required staffing |
| `reeforce-coverage` | Gap board from demand + schedules (+ optional shrinkage) |
| `reeforce-delta` | Mid-shift deltas (meal move within contractual window) |
| `reeforce-cli` | `gap` command for demo datasets |

## Non-goals (MVP)

- Not a full enterprise WFM suite (timekeeping, payroll, HRIS).
- Not a general MIP/CP day-ahead roster optimizer (use Timefold / OR-Tools / etc.).
- Not a forecasting research lab (use Nixtla, skforecast, statsforecast, NeuralForecast).
- Not a multi-tenant SaaS product in this repository.

## How to contribute

Read [CONTRIBUTING.md](CONTRIBUTING.md). Good first issues (open for contributors):

- [#2 Model chat concurrency for required staff](https://github.com/vikas-prasad-cx/reeforce/issues/2)
- [#4 Adherence-by-TOD estimator stub](https://github.com/vikas-prasad-cx/reeforce/issues/4)
- [#7 Adapter stub: statsforecast → DemandSeries](https://github.com/vikas-prasad-cx/reeforce/issues/7)
- [#8 Docs: RTA playbook for reading the gap board](https://github.com/vikas-prasad-cx/reeforce/issues/8) — stub landed; expand welcome

Also browse labels [`good first issue`](https://github.com/vikas-prasad-cx/reeforce/labels/good%20first%20issue) and [`help wanted`](https://github.com/vikas-prasad-cx/reeforce/labels/help%20wanted).

## License

Apache License 2.0 — see [LICENSE](LICENSE).

## Security

See [SECURITY.md](SECURITY.md).
