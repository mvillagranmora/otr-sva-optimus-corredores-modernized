# BcsAPVServicios — Characterization Tests

## Purpose

These tests capture what the **legacy** `cl.bcs.APV` module *actually does* so that
a modernized rewrite can be proven equivalent. They follow the Phase 2 strangler-fig
strategy for `otr-sva-optimus-corredores`.

The legacy code cannot run standalone (it requires a live HANA database,
BcsPlataformaServicios, and BcsFondoMutuoServicios). All tests are therefore
**self-contained** — pure-Java oracle methods extract the exact arithmetic and
branching logic from the legacy source, and tests run against those oracles.
Tests that still require HANA or the modernized service are marked `@Disabled`
with an explanatory reason.

---

## Test files

| File | Legacy oracle | Rules pinned |
|---|---|---|
| `CalculoRetencionTest.java` | `CalculoRetencionTraspasos.java`, `CalculoMontosDesinversion.java` | APV-RET-01..07, APV-DES-01..10 |
| `ValidaContratoGuardTest.java` | `ValidaContratoSAAPV.java`, `GrabarTraspasos.java` | APV-VAL-01..16, APV-TRA-04..11 |
| `ValidarMandatosPACTest.java` | `ValidarMandatosPAC.java` | APV-PAC-01..07 |
| `EjecutarBonificacionTest.java` | `EjecutarBonificacion.java` | APV-BON-01..14 |
| `GrabarTraspasosMappingTest.java` | `GrabarTraspasos.java`, `CrearContratoSAAPV.java` | APV-TRA-04..18, APV-CTR-01..02 |

---

## How to run

```bash
# From the BcsAPVServicios module root
mvn test -pl modernized/otr-sva-optimus-corredores/BcsAPVServicios

# Run only enabled (non-@Disabled) tests
mvn test -pl modernized/otr-sva-optimus-corredores/BcsAPVServicios \
    -Dgroups='!requires-hana'

# Run a single test class
mvn test -pl modernized/otr-sva-optimus-corredores/BcsAPVServicios \
    -Dtest=CalculoRetencionTest

# Count equivalence cases executed (enabled tests only)
mvn test -pl modernized/otr-sva-optimus-corredores/BcsAPVServicios \
    | grep -E 'Tests run'
```

Expected output with all enabled tests passing:

```
Tests run: 54, Failures: 0, Errors: 0, Skipped: 14
equivalence cases executed: 54
```

> A zero-executed count is not green — it means the test harness is broken.
> The suite must fail if test infrastructure cannot load.

---

## Canary requirement

At least one test per calculation class is a **true oracle test** (inputs ->
expected output derived by tracing the legacy arithmetic). These are NOT
`@Disabled`. They will fail if a rewrite uses the wrong rounding mode, flipped
comparison, or incorrect formula. Verify the canary behavior:

```bash
# Break a single formula in the modernized service (e.g. change HALF_DOWN to HALF_UP)
# then run — at least one test must go RED.
# Restore the change and confirm the suite is GREEN again.
```

---

## Disabled tests

| Reason | When to enable |
|---|---|
| `@Disabled("pending Step 2 — service not yet wired")` | After Step 2 wires the modernized service ports |
| `@Disabled("requires HANA — enable in Phase 3")` | After Phase 3 HANA integration environment is available |

---

## Adding a new test case

1. Identify the legacy source file and line numbers for the behavior to pin.
2. Choose or create a rule ID (format: `APV-XXX-NNN`).
3. Add the rule to the summary table at the top of the relevant test file.
4. Write a `@Test` method whose name starts with `rule_APV_XXX_NNN_`.
5. Add a `@DisplayName` citing the rule ID and the source `file:line`.
6. If the test requires a DB, mark it `@Disabled("requires HANA — enable in Phase 3")`.
7. If the test requires the modernized service, mark it `@Disabled("pending Step 2 — service not yet wired")`.

### Credential discipline

Never copy passwords, API keys, connection strings, or session tokens from the
legacy source into test fixtures. Use clearly-fake values of the same shape and
length (e.g. `99999999L` for IDs, `"FAKE-RUT"` for RUT values) and annotate
with a comment citing the substitution.

---

## Architecture notes

- **D-A01**: `cl.bcs.fondomutuo.*` calls are proxied via `TomEePlataformaClient`.
  Do not import legacy fondomutuo classes in tests; use a mock port instead.
- **D-A02**: All HANA SQL stubs throw `UnsupportedOperationException("requires HANA")`.
- **D-A05**: `cl.bcs.tesoreria.*` types (`SalidaRecaudacionTraspaso`,
  `RegistroRecaudacion`) are re-imported from the modernized `BcsTesoreriaServicios`
  module.

---

## Relationship to BUSINESS_RULES.md

The project-level `analysis/otr-sva-optimus-corredores/BUSINESS_RULES.md` covers
`BcsPlataformaServicios` (RULE-001..RULE-068). The APV-specific rule IDs defined
here (`APV-RET-*`, `APV-DES-*`, `APV-VAL-*`, `APV-PAC-*`, `APV-BON-*`,
`APV-TRA-*`, `APV-CTR-*`) are local to this module.
