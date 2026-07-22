# Changelog

All notable changes to Reeforce are documented here.

## [0.1.0] — 2026-07-22

First public MVP of the intraday WFM control plane.

### Added

- Domain model: demand series, schedules, gap board, delta types, CSV loaders
- Erlang-C / Erlang-A staffing estimators with test vectors
- Gap board builder (required vs available, UNDER/OVER flags, `peak_gap`)
- Shrinkage calendar CSV schema
- Mid-shift `MOVE_MEAL` deltas constrained by contractual meal windows
- CLI `gap` command
- Demo fixtures: `datasets/lunch-sl-cliff/`, `datasets/demo-voice-surge/`
- Operator docs: thesis, glossary, Erlang-A notes, RTA gap-board playbook
- CI workflow, contributing guide, code of conduct, security policy

### Good first issues

- Chat concurrency model (#2)
- Adherence-by-TOD estimator (#4)
- statsforecast → DemandSeries adapter stub (#7)
