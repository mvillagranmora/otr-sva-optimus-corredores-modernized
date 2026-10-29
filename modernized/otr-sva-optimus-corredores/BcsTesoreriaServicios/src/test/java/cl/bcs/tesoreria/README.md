# BcsTesoreriaServicios — Characterization Tests

These are **Phase 2 characterization tests** for the treasury domain.  They
define "done" for the implementation phase: once Step 2 creates the domain
model and service classes, these tests must all be green.

## Test files

| File | Behaviour group | Rules pinned |
|---|---|---|
| `CrearIngresoEgresoServiceTest.java` | T-01 CrearIngresoEgreso | T-01-A through T-01-F |
| `LiquidarComprobanteServiceTest.java` | T-02 LiquidarComprobante lifecycle | T-02-A through T-02-H |
| `GirarComprobanteServiceTest.java` | T-03 GirarComprobanteIngreso / GirarComprobantesEgreso | T-03-A through T-03-E |
| `ConfirmarRechazarAnularMovimientosServiceTest.java` | T-04 Multi-state workflow | T-04-A through T-04-F |
| `MandatoPACLifecycleTest.java` | T-05 MandatoPAC create/revoke/cancel | T-05-A through T-05-G |
| `SoapContractTest.java` | T-06 SOAP contract (BCP bank) | T-06-A through T-06-G |
| `TesoreriaOrchestratorTest.java` | T-07 BcsTesoreraOrchestrator dispatch | B-01, B-03, B-06 |

## Prerequisites

These tests compile and run **once Step 2 creates** the following packages:

```
cl.bcs.tesoreria.domain.model.*
cl.bcs.tesoreria.domain.port.in.*
cl.bcs.tesoreria.domain.port.out.*     (TesoreriaRepository, PlataformaPort)
cl.bcs.tesoreria.domain.service.*      (one @Service per legacy service)
cl.bcs.tesoreria.infrastructure.soap.* (SvcRecaudaInqV1Endpoint, SvcRecaudaTrnV1Endpoint)
```

Every import marked `// to be created in Step 2` is intentionally forward-referenced.

## Running the tests

```bash
# From the BcsTesoreriaServicios module root:
mvn test -pl modernized/otr-sva-optimus-corredores/BcsTesoreriaServicios

# Run only a single test group:
mvn test -Dtest=CrearIngresoEgresoServiceTest

# Run all tests matching T-01:
mvn test -Dtest="*T01*"
```

The test profile uses `application-test.properties` (already in
`src/test/resources/`).  All database interactions are mocked via Mockito —
no live database or H2 schema is required.

## Adding a new characterization case

1. Identify the legacy branch you want to pin (file + line reference).
2. Add a `@Test` method to the relevant test class.
3. Name the method `rule_<id>_<readable_description>` (hyphens become underscores).
4. Include the rule id (T-01, T-02, …) in the `@DisplayName`.
5. Use concrete input values and concrete expected outputs.
6. Never mock the system under test — only mock its ports (`TesoreriaRepository`,
   `PlataformaPort`).

## Equivalence counter

Run `mvn test` and look for the Surefire summary line:

```
Tests run: N, Failures: 0, Errors: 0, Skipped: 0
```

A fully green run with N > 0 proves equivalence for the pinned behaviours.
**A run where N = 0 proves nothing** — treat it as a build failure.

## Secret handling

No credential from the legacy codebase appears in any test file.  Any
connection string, password, or token in the legacy code has been replaced
with clearly fake same-shape values and noted in a comment.  Live database
connections are supplied via environment variables, never inlined.

## Platform rules are not this module's

The session and provisional-password rules of the platform (idle timeout per channel, provisional password validity and lockout) are implemented and tested in BcsUtils and BcsPlataformaWeb, not here: this module has no rule of its own in BUSINESS_RULES.md, so its oracle is the legacy code itself (reassigned on 2026-10-19 by decision of the user).
