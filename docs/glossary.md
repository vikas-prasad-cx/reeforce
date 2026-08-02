# Glossary

| Term | Meaning |
|------|---------|
| **AHT** | Average Handle Time — talk + hold + wrap for a contact |
| **Adherence** | How closely agents follow the published schedule / state |
| **Adherence-by-TOD** | Adherence estimated as a function of time-of-day (and optionally day-of-week) |
| **ASA** | Average Speed of Answer |
| **Delta** | A proposed change to the published schedule (e.g., move meal) |
| **Erlang-A / Erlang-C** | Queueing models relating load, agents, and service level / abandonment (see [erlang-a.md](erlang-a.md)) |
| **Gap board** | Per-interval view of required vs available capacity and the signed gap (how to read: [rta-gap-board-playbook.md](rta-gap-board-playbook.md)) |
| **Domain profile** | Capacity + delta ruleset: `contact-center`, `school-conference`, `class-section`, `traffic-corridor` |
| **peak_gap** | Largest positive gap on the board; severity of the worst understaffed interval |
| **UNDER / OVER** | CLI flags: UNDER when `gap > 0`; OVER when spare capacity (`gap < −0.5`) |
| **Meal / slot window** | Contractual earliest/latest meal or prep-slot *start*; deltas must keep moves inside it |
| **OPEN_SECTION** | Delta that brings offline overflow capacity (section seats / spare lane) online on the peak |
| **MOVE_SLOT** | Delta that slides a conference prep/unavailable block inside a teacher window |
| **RETIME_DEMAND** | Delta that shifts a timed pulse off a corridor peak |
| **Shrinkage calendar** | Planned offline capacity by interval (meetings, training, etc.) |
| **Intraday** | Within the operating day; typically next 15–120 minutes |
| **RTA** | Real-Time Analyst / Real-Time Adherence specialist |
| **Shrinkage** | Non-productive time (breaks, meetings, absenteeism, etc.) reducing available capacity |
| **SL / Service level** | Fraction of contacts answered within a target time |
| **TOD** | Time of day |
| **WFM** | Workforce Management |
