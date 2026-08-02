# Contributing to Reeforce

Thanks for helping build an open intraday capacity control plane.

## Ground rules

- Be kind; follow the [Code of Conduct](CODE_OF_CONDUCT.md).
- Prefer small, reviewable PRs with tests.
- Discuss larger design changes in an issue first.

## Development setup

1. Install **JDK 21** and **Maven 3.9+**.
2. Clone the repo and run tests:

```bash
mvn test
```

3. Run the demo gap path (from a local IDE main, or via module classpath after `mvn -pl reeforce-cli -am package`).

## Project layout

- `reeforce-model` — domain types (`DomainProfile`, schedules, gap board)
- `reeforce-capacity` — Erlang + direct capacity models
- `reeforce-coverage` — gap board
- `reeforce-delta` — domain-aware schedule deltas
- `reeforce-cli` — demo CLI (`--domain`)
- `datasets/` — fixtures (contact center, school, class, traffic)
- `docs/` — thesis, glossary, use cases

## Good first contributions

Look for issues labeled `good first issue` or `help wanted`: Erlang test vectors, CSV schemas, forecast adapters, domain fixtures, and docs are intentionally scoped for newcomers.

## Pull requests

- Keep the Apache-2.0 license headers / license intact.
- Add or update unit tests for behavior changes.
- Update docs when you change public concepts (gap board columns, CSV schemas).

## Security

Do not open public issues for vulnerabilities — see [SECURITY.md](SECURITY.md).
