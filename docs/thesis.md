# Thesis: intraday WFM as a control plane

## Claim

Contact-center service level fails mid-shift less often from bad day-ahead rosters than from **unclosed gaps** between planned capacity and reality: forecast error, adherence by time-of-day, clustered shrinkage, and lunch cliffs.

Reeforce treats the published schedule as the plant model and mid-shift edits as **actuators**. The objective is not a perfect replan of the day; it is a sequence of **feasible, low-disruption deltas** that keep the gap board inside policy bounds for the next N intervals.

## Loop

1. Ingest demand (forecast + actuals) and roster state (planned + adherence).
2. Convert demand to required staff (Erlang-A / channel models).
3. Convert roster + adherence to available staff.
4. Emit a gap board RTAs can read in seconds.
5. Search a small action space (move meal in window, cancel non-phone, OT volunteer) under hard labor rules.
6. Score actions by expected SL recovery vs disruption; apply; measure.

## Why not “just re-optimize”?

Full re-solves are valuable overnight and for major events. During the hour, operators need **explainable micro-moves** with predictable blast radius. Reeforce is that control surface; solvers and forecast libraries remain upstream/downstream tools.
