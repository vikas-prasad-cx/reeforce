# Thesis: intraday capacity as a control plane

## Claim

Service and access failures mid-horizon less often come from bad day-ahead plans than from **unclosed gaps** between planned capacity and reality: forecast error, clustered unavailability, and demand spikes (lunch cliffs, conference slot piles, section overfills, corridor peaks).

Reeforce treats the published schedule as the plant model and mid-horizon edits as **actuators**. The objective is not a perfect replan of the day; it is a sequence of **feasible, low-disruption deltas** that keep the gap board inside policy bounds for the next N intervals.

The same loop applies across domains: contact-center agents, school conference teachers, class-section seats, and corridor throughput.

## Loop

1. Ingest demand (forecast + actuals) and resource state (planned + adherence / open capacity).
2. Convert demand to required capacity (Erlang-A / channel models, or direct volume).
3. Convert roster + adherence to available capacity.
4. Emit a gap board operators can read in seconds.
5. Search a small action space (move meal/slot, open overflow, retime pulse) under hard rules.
6. Score actions by expected recovery vs disruption; apply; measure.

## Why not “just re-optimize”?

Full re-solves are valuable overnight and for major events. During the hour, operators need **explainable micro-moves** with predictable blast radius. Reeforce is that control surface; solvers and forecast libraries remain upstream/downstream tools.
