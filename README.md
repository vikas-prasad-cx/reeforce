# Reeforce

**Plan the day. Replan the hour.**

Open-source **intraday WFM control plane** for contact centers: turn live demand and real adherence into feasible mid-shift schedule deltas.

[![CI](https://github.com/vikas-prasad-cx/reeforce/actions/workflows/ci.yml/badge.svg)](https://github.com/vikas-prasad-cx/reeforce/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

## The problem

Most WFM stacks are strong at **day-ahead planning** and weak at **hour-of operations**:

- Forecasts drift; adherence is not 100%; shrinkage lands unevenly.
- RTAs stare at wallboards and move breaks by instinct.
- Optimizers that replan the whole day are too slow, too disruptive, or both.

Reeforce focuses on the missing layer: a **control plane** that reads the gap and proposes **small, feasible schedule deltas** for the next few intervals.

## Thesis

Intraday WFM is a closed-loop control problem, not a one-shot roster solve.

1. **Sense** — demand now / next hour, adherence-by-TOD, shrinkage calendar.
2. **Compare** — required vs available capacity → gap board.
3. **Act** — constrained deltas (move meal within window, pull OT, cancel activity).
4. **Learn** — which moves actually closed service-level cliffs.

See [docs/thesis.md](docs/thesis.md) and [docs/glossary.md](docs/glossary.md).

## Non-goals (MVP)

- Not a full enterprise WFM suite (timekeeping, payroll, HRIS).
- Not a general MIP/CP day-ahead roster optimizer (use Timefold / OR-Tools / etc.).
- Not a forecasting research lab (use Nixtla, skforecast, statsforecast, NeuralForecast).
- Not a multi-tenant SaaS product in this repository.

## MVP scope

| Module | Role |
|--------|------|
| `reeforce-model` | Demand series, schedules, gap board, delta types, CSV loaders |
| `reeforce-capacity` | Staffing requirement stubs (Erlang-A target) |
| `reeforce-coverage` | Gap board from demand + schedules |
| `reeforce-delta` | Mid-shift delta proposals (meal move stub) |
| `reeforce-cli` | `gap` command for demo datasets |

## Architecture

```text
DemandSeries ──► Capacity (required N) ──┐
                                         ├──► GapBoard ──► DeltaEngine ──► ScheduleDelta
AgentSchedule ─► Coverage (available N) ─┘
```

Adapters (planned): forecast toolkits → `DemandSeries`; ACD/RTA feeds → adherence & shrinkage; WFM systems → publish deltas.

## Complements (not competitors)

| Project | Role vs Reeforce |
|---------|------------------|
| [Timefold](https://github.com/TimefoldAI/timefold-solver) | Day-ahead / constraint solve; Reeforce consumes published schedules and proposes intraday deltas |
| [Nixtla / statsforecast](https://github.com/Nixtla/statsforecast) | Demand forecasting; Reeforce consumes forecast series |
| [pyworkforce](https://github.com/CarlosHerreraC/pyworkforce) | Erlang / staffing utilities; Reeforce owns the live control loop |

## Requirements

- Java 21+
- Maven 3.9+

## Quick start

```bash
git clone https://github.com/vikas-prasad-cx/reeforce.git
cd reeforce
mvn test
mvn -q install -DskipTests
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/demo-voice-surge/demand.csv"
```
## How to contribute

Read [CONTRIBUTING.md](CONTRIBUTING.md). Good first issues are labeled `good first issue` and `help wanted`.

## License

Apache License 2.0 — see [LICENSE](LICENSE).

## Security

See [SECURITY.md](SECURITY.md).
