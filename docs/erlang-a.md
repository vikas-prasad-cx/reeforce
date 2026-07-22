# Erlang-C / Erlang-A staffing notes

Reeforce converts offered volume + AHT into **required staff** in `reeforce-capacity`.

## Inputs

| Input | Meaning |
|-------|---------|
| `offeredVolume` | Contacts offered in the planning interval |
| `ahtSeconds` | Average handle time (talk + hold + wrap) |
| `intervalSeconds` | Interval length (typically 900 or 1800) |
| `serviceLevel` | Target fraction answered within T (e.g. 0.80) |
| `targetAnswerSeconds` | T in the SL definition (e.g. 20) |
| `averagePatienceSeconds` | Mean time-to-abandon for Erlang-A; `+∞` → Erlang-C |

Traffic intensity (Erlangs):

```text
a = offeredVolume * ahtSeconds / intervalSeconds
```

## Models

- **Erlang-C (M/M/n):** infinite patience. Service level
  `1 − C(n,a) · exp(−(n−a)·μ·T)` with μ = 1/AHT.
- **Erlang-A (M/M/n+M):** exponential patience θ = 1/patience. Delay probability
  from the birth–death balance; SL approximated with abandonment survival to T.

`requiredStaff` returns the **minimum integer** `n > a` meeting the SL target.

## Sources

- A.K. Erlang (1917) — Erlang-C delay formula (telephone traffic).
- Conny Palm (1943/1946) — impatient customers (Erlang-A lineage).
- Gans, Koole, Mandelbaum — *Telephone Call Centers: Tutorial, Review, and Research Prospects*.
- Garnett, Mandelbaum, Reiman (2002) — many-server Erlang-A asymptotics.
- Industry 80/20 benchmark tables used widely in WFM calculators (Call Centre Helper, ICMI-style 80% in 20s).

## Golden vectors

Parameterized tests in `ErlangStaffingTest` use ±1 agent tolerance against reference
staffing levels for classic loads (e.g. a ≈ 10 Erlangs @ 80/20 → ~14 agents). Vectors
are Erlang-C unless patience is specified; Erlang-A cases assert that finite patience
does not *increase* required staff versus Erlang-C for the same SL.
