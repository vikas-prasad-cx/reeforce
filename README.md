# Reeforce

**Plan the day. Replan the hour.**

Open-source **intraday capacity control plane**: turn live demand and published
resources into a gap board and feasible mid-horizon schedule deltas — for
contact centers, school conferences, class sections, and corridor ops.

[![CI](https://github.com/vikas-prasad-cx/reeforce/actions/workflows/ci.yml/badge.svg)](https://github.com/vikas-prasad-cx/reeforce/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

## Why Reeforce

Day-ahead solvers and forecast labs solve yesterday’s plan and tomorrow’s volume.
Operators still replan the **next hour** by instinct when lunches stack, conference
slots cliff, sections overfill, or corridors overload.

Reeforce is the missing control loop: sense the gap → propose small, contractual
deltas → learn what closed the cliff.

It **complements** (does not replace):

| Project | Role vs Reeforce |
|---------|------------------|
| [Timefold](https://github.com/TimefoldAI/timefold-solver) / [UniTime](https://github.com/UniTime/unitime) / FET | Day-ahead / constraint solve; Reeforce consumes published schedules |
| [Nixtla / statsforecast](https://github.com/Nixtla/statsforecast) / [skforecast](https://github.com/skforecast/skforecast) | Demand forecasting; Reeforce consumes forecast series |
| [pyworkforce](https://github.com/CarlosHerreraC/pyworkforce) | Erlang utilities; Reeforce owns the live control loop |
| AequilibraE / dyntapy | Full traffic assignment; Reeforce owns interval capacity gaps |

## Quick start

Requires **Java 21+** and **Maven 3.9+**.

```bash
git clone https://github.com/vikas-prasad-cx/reeforce.git
cd reeforce
mvn test
```

### Contact center — lunch SL cliff

```bash
mvn -q install -DskipTests
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/lunch-sl-cliff/demand.csv --roster datasets/lunch-sl-cliff/roster.csv --meal-windows datasets/lunch-sl-cliff/meal-windows.csv"
```

### School conference — slot cliff

```bash
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/school-conference-cliff/demand.csv --domain school-conference --roster datasets/school-conference-cliff/roster.csv --meal-windows datasets/school-conference-cliff/slot-windows.csv"
```

### Class section — registration overfill

```bash
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/class-section-overfill/demand.csv --domain class-section --roster datasets/class-section-overfill/roster.csv"
```

### Traffic corridor — morning peak

```bash
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/traffic-corridor-peak/demand.csv --domain traffic-corridor --roster datasets/traffic-corridor-peak/roster.csv --meal-windows datasets/traffic-corridor-peak/slot-windows.csv"
```

How to read `UNDER` / `OVER` / `peak_gap`: [docs/rta-gap-board-playbook.md](docs/rta-gap-board-playbook.md).

Use cases: [school conference](docs/use-cases/school-conference.md) · [class section](docs/use-cases/class-section.md) · [traffic corridor](docs/use-cases/traffic-corridor.md).

Dataset schemas: [datasets/README.md](datasets/README.md). Staffing notes: [docs/erlang-a.md](docs/erlang-a.md).

## Thesis (short)

Intraday capacity management is a closed-loop control problem, not a one-shot roster solve.

1. **Sense** — demand now / next hour, adherence, shrinkage, open capacity.
2. **Compare** — required vs available → gap board.
3. **Act** — constrained deltas (move meal/slot, open overflow, retime pulse).
4. **Learn** — which moves actually closed the cliff.

See [docs/thesis.md](docs/thesis.md) and [docs/glossary.md](docs/glossary.md).

## Architecture

```text
DemandSeries ──► Capacity (required N) ──┐
                                         ├──► GapBoard ──► DeltaEngine ──► ScheduleDelta
ResourceSchedule ► Coverage (available) ─┘
```

Domains via `--domain`: `contact-center` (Erlang) · `school-conference` · `class-section` · `traffic-corridor`.

| Module | Role |
|--------|------|
| `reeforce-model` | Demand, schedules, gap board, domain profile, CSV loaders |
| `reeforce-capacity` | Erlang + direct capacity models |
| `reeforce-coverage` | Gap board from demand + resources |
| `reeforce-delta` | Domain-aware mid-horizon deltas |
| `reeforce-cli` | `gap` command for demo datasets |

## Non-goals (MVP)

- Not a full enterprise WFM / SIS / PTC booking suite.
- Not a general MIP/CP day-ahead timetable optimizer (use Timefold / UniTime / FET).
- Not a forecasting research lab (use Nixtla, skforecast, NeuralForecast).
- Not macroscopic traffic assignment (use AequilibraE / dyntapy).
- Not a multi-tenant SaaS product in this repository.

## How to contribute

Read [CONTRIBUTING.md](CONTRIBUTING.md). Good first issues (open for contributors):

- [#2 Model chat concurrency for required staff](https://github.com/vikas-prasad-cx/reeforce/issues/2)
- [#4 Adherence-by-TOD estimator stub](https://github.com/vikas-prasad-cx/reeforce/issues/4)
- [#7 Adapter stub: statsforecast → DemandSeries](https://github.com/vikas-prasad-cx/reeforce/issues/7)

Operator playbook: [docs/rta-gap-board-playbook.md](docs/rta-gap-board-playbook.md). Changelog: [CHANGELOG.md](CHANGELOG.md).

Also browse labels [`good first issue`](https://github.com/vikas-prasad-cx/reeforce/labels/good%20first%20issue) and [`help wanted`](https://github.com/vikas-prasad-cx/reeforce/labels/help%20wanted).

## License

Apache License 2.0 — see [LICENSE](LICENSE).

## Security

See [SECURITY.md](SECURITY.md).
