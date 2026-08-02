## Summary

<!-- What changed and why (1–3 bullets). -->

## Test plan

- [ ] `mvn test` passes locally
- [ ] Updated or added unit tests for behavior changes
- [ ] Docs / glossary / playbook updated if concepts or CSV schemas changed
- [ ] Demo still runs (if CLI or datasets touched):

```bash
mvn -pl reeforce-cli exec:java \
  -Dexec.mainClass=ai.reeforce.cli.ReeforceCli \
  -Dexec.args="gap datasets/lunch-sl-cliff/demand.csv --roster datasets/lunch-sl-cliff/roster.csv --meal-windows datasets/lunch-sl-cliff/meal-windows.csv"
```

## Notes

<!-- Breaking changes, follow-ups, related issues: Fixes #N -->
