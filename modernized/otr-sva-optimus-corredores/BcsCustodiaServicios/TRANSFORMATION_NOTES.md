# TRANSFORMATION_NOTES — BcsCustodiaServicios

**Date:** 2026-10-08  
**Author:** Mauricio Villagran Mora (Architect)  
**Strategy:** Full scope (expand-all / Option B) — Strangler Fig + hexagonal architecture  
**Target stack:** Spring Boot 4.1.1 / Java 25 / Jakarta EE namespace  
**Legacy source:** legacy/otr-sva-optimus-corredores/BcsCustodiaServicios/ (286 files, ~123,977 LOC)

---

## Behavior Mapping Table

| Legacy file (representative lines) | Behavior | Target file | Notes |
|---|---|---|---|
| `LiquidarCustodiaEmprestito.java` (cuentaDias) | Calendar/working-day count between two yyyyMMdd dates | `service/CustodiaServiciosService.java:123` | Legacy bug in working-days loop (`ld.plusDays(asd)`) preserved for behavioral equivalence — B-CUS-01, B-CUS-02 |
| `LiquidarCustodiaEmprestito.java` (calculoInteres) | Simple interest: monto × (tasa/100) × (plazo/base) | `service/CustodiaServiciosService.java:153` | BigDecimal.divide() without RoundingMode preserved — throws ArithmeticException on non-terminating decimals (legacy behavior). B-CUS-03, B-CUS-04 |
| `ControlRetiroCustodia.java` (getHashBusqueda) | Composite search key: "idInstrumento-idPortafolio-idCliente-nemosvs" | `service/CustodiaServiciosService.java:171` | String concatenation format identical to legacy. B-CUS-05, B-CUS-06 |
| `GuardarPatrimonioRetroactivo.java` (obtenerRangoDeFechas) | Inclusive list of yyyy-MM-dd dates in [start, end] | `service/CustodiaServiciosService.java:184` | B-CUS-07, B-CUS-08 |
| `AnticiparPrestamo.java` (TIPO_PRESTAMO_VENTA_CORTA) | Constant "PRTVTC" for short-sale loan type | `service/CustodiaServiciosService.java:38` | B-CUS-11 |
| `ConsultarCustodiaPortafolio.java` (TIPO_BUSQUEDAS) | 15-element search-type list | `service/CustodiaServiciosService.java:100` | B-CUS-09, B-CUS-10 |
| `DeterminaNuevoNemo.java` (NuevoNemo — migrated from COBOL LMRUCANE.SRC) | Nemotécnico transformation: `**`→` *`, ` *`→`01`, `&&`→` &`, ` &`→date-based substitution, identity | `service/CustodiaServiciosService.java:216` | COBOL-origin logic preserved verbatim. B-CUS-12..B-CUS-17. Known gap: ` &` branch 3rd sub-path (year-1 when mesNemo≥mesProceso and year≠00/01) not covered by tests — behaviorally identified but no golden-master fixture available. |
| `ConstantesCustodia.java` (getCodigoErroresBlockChain) | Blockchain error code map BC_501..BC_541 | `service/CustodiaServiciosService.java:49` | 41 put() calls; duplicate key "Missing field: Arbitrator1" (BC_518 overwritten by BC_529) → runtime size 40. Preserved as immutable map. B-CUS-18, B-CUS-19 |
| `ConsultarCustodiaPortafolio.java` (3,935 LOC, god-object) | All query/report operations on portfolio custody | `service/ConsultarCustodiaPortafolioService.java` (stub) | Stub-only: all 60+ methods throw `UnsupportedOperationException("requires HANA: ...")`. Intentional strangler fig — full implementation deferred pending HANA SP availability. |
| All other 280+ service classes | Domain operations (custody, settlements, guarantees, portfolios, etc.) | `service/*Service.java` (~185 stubs) | All throw `UnsupportedOperationException("requires HANA: ...")`. Strangler fig pattern — will be activated incrementally as HANA SPs are available in sandbox. |
| No legacy equivalent | Application bootstrap | `BcsCustodiaServiciosApplication.java` | Spring Boot 4.1.1 entry point. Only class permitted to have `main()` (RULE-076). |
| No legacy equivalent | Domain model | `domain/PosicionCustodia.java` | Extracted from scattered legacy DTOs |
| No legacy equivalent | HANA outbound adapter | `adapter/out/CustodiaServiciosRepositoryAdapter.java` | Hexagonal port stub — all HANA calls via `${HANA_URL}/${HANA_USER}/${HANA_PASSWORD}` env vars (RULE-076, RULE-077). No hardcoded credentials. |

---

## Deliberate Deviations

| # | Deviation | Rationale |
|---|---|---|
| D-01 | `cuentaDias` working-day loop preserves `ld = ld.plusDays(asd)` bug | Behavioral equivalence required. Tests bCus02 pin the exact buggy output (6L for a 7-calendar-day span). Fixing this would be a behavioral change requiring SME approval. |
| D-02 | `calculoInteres` uses `BigDecimal.divide()` without `RoundingMode` | Legacy behavior preserved. Clients that pass non-terminating decimals will still get `ArithmeticException`. Fixing this is a behavioral change. |
| D-03 | `BLOCKCHAIN_ERROR_CODES` size is 40, not 41 | Legacy `HashMap` silently overwrites BC_518 with BC_529 (same key). Preserved verbatim; both duplicate entries are documented in comments. |
| D-04 | `determinarNuevoNemo` throws checked `Exception` | Legacy signature preserved. No checked exception is actually thrown by this logic, but callers in legacy code declare `throws Exception`. |
| D-05 | All 280+ non-pure-logic service stubs throw `UnsupportedOperationException` | Strangler fig pattern. Stubs will be activated module by module as HANA sandboxes become available. |
| D-06 | `ConsultarCustodiaPortafolio` (3,935 LOC god-object) is stub-only | SME decision (Option A): complexity too high for single-session full expansion without HANA SP access. Full expansion is a follow-up task. |

---

## Not Migrated (Dead Code / Unreachable)

| Legacy artifact | Reason not migrated |
|---|---|
| `main()` in service classes (multiple) | RULE-076: hardcoded HANA credentials `JOBMAN`/`<masked>`, IP `161.238.34.39`. Removed. Credentials must be rotated before any deployment. |
| Static 3DES encryption key in `ConstantesCustodia` | RULE-077: publicly known key. Removed. Must be replaced with AES-256 or managed-key scheme before deployment. |
| `DeterminaNuevoNemo.java` as standalone class | Logic inlined into `CustodiaServiciosService.determinarNuevoNemo()`. Legacy standalone wrapper had no callers outside Custodia. |
| Legacy EJB/TomEE wiring (`@Stateless`, `@EJB` annotations) | Replaced with Spring `@Service` + constructor injection. |
| JNDI datasource lookups | Replaced with Spring Data / env-var DataSource (`${HANA_URL}/${HANA_USER}/${HANA_PASSWORD}`). |

---

## Canary Result

**Change applied:** `determinarNuevoNemo()` — replaced `nemoActual.replace("**", " *")` with `nemoActual.replace("**", "XX")`.

**Result:** 1 test failed — `bCus12_determinarNuevoNemo_doubleAsterisk_firstStage`.  
All other 31 tests passed (correct: the change only affects the `**` branch).

**Canary XML:** `analysis/otr-sva-optimus-corredores/equivalence/canary/BcsCustodiaServicios/TEST-cl.bcs.custodia.CustodiaServiciosServiceTest.xml`

**Conclusion:** Tests correctly pin the first-stage asterisk substitution. The canary confirms the suite can detect behavioral regressions.

---

## Equivalence Cases Executed

**32 total** (22 unit + 10 integration, 3 @Disabled requiring HANA SP):

| Category | Tests | IDs |
|---|---|---|
| cuentaDias (calendar/working) | 2 | B-CUS-01, B-CUS-02 |
| calculoInteres | 2 | B-CUS-03, B-CUS-04 |
| getHashBusqueda | 2 | B-CUS-05, B-CUS-06 |
| obtenerRangoDeFechas | 2 | B-CUS-07, B-CUS-08 |
| TIPO_BUSQUEDAS constant | 2 | B-CUS-09, B-CUS-10 |
| TIPO_PRESTAMO_VENTA_CORTA | 1 | B-CUS-11 |
| determinarNuevoNemo | 6 | B-CUS-12..B-CUS-17 |
| BLOCKCHAIN_ERROR_CODES | 2 | B-CUS-18, B-CUS-19 |
| RULE-076 (no main() in services) | 3 | RULE-076 ×3 |
| Integration (Spring context) | 7 | contextLoads + stub invocations |
| @Disabled (require HANA SP) | 3 | — |

**Known gap:** `determinarNuevoNemo` ` &` branch 3rd sub-path (year-1 when mesNemo≥mesProceso and year≠00/01) is not covered. No golden-master fixture available; legacy executable not available in this environment (TomEE 8 / Java 8 required). Documented as follow-up.

---

## Side-by-Side: determinarNuevoNemo (representative behavior)

**Legacy** (`DeterminaNuevoNemo.java`, NuevoNemo method — verbatim excerpt):
```java
// REEMPLAZA *
if (nemoActual.indexOf("**") > -1) {
    return (nemoActual.replace("**", " *"));   // Ej: ABNA12**02  >>  ABNA12 *02
} else if (nemoActual.indexOf(" *") > -1) {
    return (nemoActual.replace(" *", "01"));   // Ej: ABNA12 *02  >>  ABNA120102
}
```

**Modernized** (`CustodiaServiciosService.java:218`):
```java
if (nemoActual.indexOf("**") > -1) {
    return (nemoActual.replace("**", " *"));   // Ej: ABNA12**02  >>  ABNA12 *02
} else if (nemoActual.indexOf(" *") > -1) {
    return (nemoActual.replace(" *", "01"));   // Ej: ABNA12 *02  >>  ABNA120102
}
```

Identical. Legacy COBOL comment preserved. No structural change.

---

## Legacy Lines Retired (Pure-Logic Extraction)

| Class | Legacy LOC | Status |
|---|---|---|
| `DeterminaNuevoNemo.java` | ~120 | Logic inlined; class removed |
| `ConstantesCustodia.java` (error map method) | ~55 | Migrated to static initializer block |
| `LiquidarCustodiaEmprestito.java` (two methods) | ~40 | Migrated to static methods |
| `ControlRetiroCustodia.java` (one method) | ~10 | Migrated to static method |
| `GuardarPatrimonioRetroactivo.java` (one method) | ~20 | Migrated to static method |
| **Total** | **~245** | Directly tested; legacy removed from active path |

Remaining ~123,700 LOC: stub-replaced (strangler fig). Not retired yet — activated incrementally.

---

## HANA Schema Alignment Finding (verify 2026-10-08)

During independent verify against HANA dev/QA (`161.238.15.29:30415`, schema `BSCL_VIRTUAL`):

| Assumed (H2 placeholder) | Real HANA |
|---|---|
| `CONFIGURACION_T` | **Not found** in any accessible schema |
| `POSICION_CUSTODIA_T` with cols `ID_PORTAFOLIO, ID_INSTRUMENTO, CANTIDAD, PRECIO_PROMEDIO, FECHA_LIQUIDACION, ID_CLIENTE` | `BSCL_VIRTUAL.CUSTODIA_OPTIMUS` with cols `RUT_CLI, SEC_RUT_CLI, NEMO, CANTIDAD, PRECIO_CIERRE` (5,454 rows) |

**Impact:** The two plain-SQL methods in `CustodiaServiciosRepositoryAdapter` (`obtenerFechaSistema`, `consultarPosicion`) use invented table names designed for H2 test scaffolding. They will fail if connected to real HANA. Before any HANA deployment, these methods must be realigned to the real schema.

**determinarNuevoNemo HANA fresh-input:** 172 distinct real NEMOs from `BSCL_VIRTUAL.CUSTODIA_OPTIMUS` tested. None had transitional markers (`**`, ` *`, `&&`, ` &`) — all correctly returned via identity path (0 errors). Transitional codes only appear during active coupon events.

Evidence: `analysis/otr-sva-optimus-corredores/equivalence/fresh/BcsCustodiaServicios/nemo-fresh-check.txt`

---

## Follow-ups for Dependent Modules

1. **HANA credential rotation** (RULE-076/077 blocker): `JOBMAN`/`<masked>`, IP `161.238.34.39`, static 3DES key — must be rotated before any deployment. Owner: ops team.
2. **ConsultarCustodiaPortafolio full expansion**: 3,935 LOC god-object, currently stub-only. Requires HANA SP sandbox access. Estimated effort: 2–3 sessions.
3. **`determinarNuevoNemo` ` &` 3rd sub-path**: mesNemo≥mesProceso and year≠00/01 → year-1. Needs golden-master fixture from production data or SME confirmation.
4. **`calculoInteres` RoundingMode fix**: After SME approval, replace `BigDecimal.divide()` with `divide(divisor, scale, RoundingMode.HALF_UP)` to eliminate ArithmeticException risk on non-terminating decimals.
5. **Stub activation order**: Priority order per MODERNIZATION_BRIEF.md Phase 4 plan — activate stubs as HANA SP sandboxes become available, starting with highest-traffic operations.
6. **Architecture-critic findings**: HIGH findings H-1/H-2/H-3 applied. MEDIUM/LOW findings documented in Step 5 review.
7. **HANA schema alignment**: Before connecting to HANA, `CustodiaServiciosRepositoryAdapter` plain-SQL methods must be rewritten to use real table names (`BSCL_VIRTUAL.CUSTODIA_OPTIMUS` structure) instead of H2 placeholders. Column mapping differs completely. The H2 schema (`CONFIGURACION_T`, `POSICION_CUSTODIA_T`) exists solely for test scaffolding and is not deployable as-is.


---

## Slice ConsultarFlujoCustodia (transform 2026-10-26)

Package `cl.bcs.custodia.flujo` (about 2,900 lines of main code, with the Plataforma reads of the slice under `flujo/kit` and the SQL executor under `flujo/jdbc`). Legacy: `BSCL/Custodia/JAVA/BcsCustodiaServicios/src/cl/bcs/custodia/ConsultarFlujoCustodia.java` (2,612 lines, `logica` :937-2122) and its 29 `FlujoCustodia_*.sql` (verbatim, `src/main/resources/cl/bcs/custodia/sql/`). Rules: `analysis/otr-sva-optimus-corredores/RULES_CUSTODIA_FLUJO.md` (CUS-FLU-01..36, D-CUS-01..10).

**Equivalence is trace-based on the recorded legacy.** The goldens were recorded by `legacy-oracle/record.sh` from the UNMODIFIED legacy class on in-memory H2 (MODE=LEGACY, UTC, locale en); ValorizarInstrumento is a scripted double. The legacy could run here (H2); it was not run against HANA (QA unreachable on 2026-10-25), so column types and the HANA acceptance of `(1==1)` (CUS-FLU-15, no filter) are NOT proven.

### Mapping (legacy to target)

| Behavior | Legacy | Target |
|---|---|---|
| Entry, JSON in/out, error counter (CUS-FLU-01) | :916-924 | `ConsultarFlujoCustodiaFlujo.call` |
| Empty use, system date, company, key, dates (CUS-FLU-02..06) | :937-1011 | `ConsultarFlujoCustodiaFlujo.logica`, `kit/PlataformaLecturas`, `kit/DiaSiguiente` |
| Cross, similarity, winners, base table (CUS-FLU-08..13) | :1058-1195 | `MatrizDeUso.construir`, `FlujoCustodiaReglas.similitud` |
| Item to source map (CUS-FLU-13, D-CUS-07) | :2218-2278 | `FlujoCustodiaReglas.construirMapaItems` / `fuenteDeItem` |
| Work table insert / delete (CUS-FLU-14) | :1218, :2113 | `ConsultarFlujoCustodiaFlujo.logica` + `limpiarMatrizUso` |
| Sources and historic switch (CUS-FLU-07, 15..23) | :1233-1533, :2128-2215 | `FuentesFlujo` (`leer`, `carterasActuales`) |
| Process values, operator, discards (CUS-FLU-24..26) | :1543-1620 | `ProcesamientoFlujo.marcar` / `descartarNoSubyacentes`, `FlujoCustodiaReglas.motivoDescarte` / `aplicarOperador` |
| Detail and projection (CUS-FLU-27, 28) | :1631-1664 | `ProcesamientoFlujo.proyectar` |
| Grouping, counters, totals (CUS-FLU-29..32) | :1673-2018 | `AgrupacionFlujo.unir` / `totalizar`, `FlujoCustodiaReglas.considerarRegistro` |
| Valuation (CUS-FLU-33..35) | :2024-2104 | `ValorizacionFlujo.valorizar` (ValorizarInstrumento behind a `Function<JsonObject,JsonObject>`) |
| Sequences (CUS-FLU-36) | :980, :1624 | injected `BiFunction<String,Long,List<Long>>` (the legacy `Utils.getSecuencia`) |

### Deliberate deviations

- **DEV-FLU-1 Grouping is sequential** (D-CUS-06): the legacy used parallel streams with unsynchronised counters; the result is that of a run without the race.
- **DEV-FLU-2 No progress bar**: `p_BarraProgreso` is not carried (nothing in-process supplies one).
- **DEV-FLU-3 No state between calls**: the legacy kept static caches of configuration and dictionary; here every call reads again (as FondoMutuo, DEV-F2). The reads are the legacy SQL.
- **DEV-FLU-4 Error counter** compares the message type with `equals` (legacy `==` on interned constants): same result for the messages of this slice and for the ones ValorizarInstrumento builds from its constants.
- **DEV-FLU-5 Cleanup failure**: if the `DELETE` of the work table fails after another error, the original exception is thrown and the cleanup one is attached as suppressed (architecture review, HIGH).
- Kept bug for bug on purpose: D-CUS-01..10 (null use is NPE, similarity NPE, SUBYACENTE falls into the reserved portfolio, first-winner predicates, HashMap order of the output, random work id, valuation errors replace the messages). `HashMap.put`, not `computeIfAbsent`, builds the totalised groups: `computeIfAbsent` links a new node at the head of its bucket and changed the order of 3 of the 85 cases.

### Not migrated

`FlujoCustodia_SelectCarteras.sql` and `SelectCarterasVentaTransito.sql` (dead: the first is commented out in the legacy, the second is read by no `case`, D-CUS-07); the legacy `main()` (RULE-076, hardcoded environment); `DatosFlujoCustodia2`, `Cartera`, `FlujoCustodiaItems`, `toStringA` (no callers); `ValorizarInstrumento` (3,172 lines, a later slice); the HTTP registration in `InternalJavaCallController` (the slice ships as an in-process library, see the Custodia library decision; registering it before the global cutover would route gateway traffic to it).

### Evidence

- **Equivalence cases executed: 85 of 85** (`ConsultarFlujoCustodiaGoldenTest` prints `equivalence cases executed: 85 of 85`), plus 102 rule tests and 4 harness-integrity tests: 191 tests green. Full module: 228 tests, 0 failures, 6 skipped (3 HANA stored procedures, 3 live-QA tests that need `QA_*` variables).
- `compare.py` over the 85 pairs (`analysis/.../equivalence/BcsCustodiaServicios/cases.json`): **85 same, 0 differs, 0 missing**. Only the random work id is masked (by the harness, on both sides).
- **Canary:** in a scratch copy `FlujoCustodiaReglas.aplicarOperador` ignores the sign (factor always 1): **19 tests failed** (18 golden cases + 1 rule test) that pass clean. Evidence: `analysis/.../equivalence/canary/BcsCustodiaServicios-transform-flujo/`.
- Log of the full run: `analysis/.../equivalence/BcsCustodiaServicios-transform-flujo.test-output.txt`.

### Side by side (similarity, CUS-FLU-10; legacy :1107-1112 on the left, target `FlujoCustodiaReglas.similitud` on the right)

```
ufc.IdTipoInstrumento = ufc.IdTipoInstrumento == null ? "" : ...   |  String tipoConfigurado = ufc.IdTipoInstrumento == null ? "" : ...
if (ufc.IdTipoInstrumento.equals(u.IdTipoInstrumento+"")) {          |  if (tipoConfigurado.equals(u.IdTipoInstrumento + "")) {
    MayorSimilitud += 2;                                             |      puntaje += 2;
} else if ((u.IdTipoInstrumento+"").equals("")){                     |  } else if ((u.IdTipoInstrumento + "").equals("")) {
    MayorSimilitud += 1;                                             |      puntaje += 1;
}                                                                    |  }
```
(the legacy mutated `ufc`; the target keeps the value in a local, no other code read it.)

### Architecture review (architecture-critic, 2026-10-26)

Applied: HIGH-1 cleanup failure no longer hides the real cause (DEV-FLU-5); the comment regex is precompiled; `distinct()` runs on a sequential stream (same output, no common-pool use).
Listed, not applied (each would change a decision or needs the real database):
- HIGH-2 the paged portfolio read (LIMIT/OFFSET over 500 000 rows) has no ORDER BY in the legacy SQL: pages may overlap or skip under concurrent writes, and the pages are collected in memory anyway. Kept as the legacy; known risk next to D-CUS-04. Needs a decision (read once with a fetch size, or add an ORDER BY and accept a different row order).
- MEDIUM the work id is `Math.random()` (D-CUS-09, Q-CUS-03): offer to draw it from a sequence.
- MEDIUM list parameters expand to one bind per element (no cap) and the `:name` replacement is a prefix match as in the legacy (a key `Id` would corrupt `IdEmpresa`); latent today (`Instrumentos`, `idsTipoOperacion`).
- MEDIUM the comment-stripping regex also removes `//` and `--` inside string literals, as the legacy; no SQL of the slice has them.
- LOW `itemsPorUsoYCalculo` is filled inside the winners x items loop (redundant, same result); `LongStream` ids for the matrix rows; the `default` of `FuentesFlujo.leer` silently reads nothing for an unknown source (D-CUS-07 needs it); the three hand copies in `MatrizDeUso` are explicit on purpose.
- LOW `SqlNombrado`, `SqlRecursos`, `PlataformaLecturas` are public in the jar: consumers must not depend on `flujo.jdbc` / `flujo.kit`.
- The reviewer read only part of the files and its line numbers did not match the sources; each point above was checked against the code before it was recorded.

### Follow-ups for dependent modules

1. **In-process consumers (18 callers, FondoMutuo gate G-CUS):** supply the two collaborators (ValorizarInstrumento as JSON in/out, and the `Utils.getSecuencia` equivalent for `BSCL.Custodia.BD.Secuencias::FlujoCustodia` / `DatosFlujoCustodia`) and a connection whose transaction they own; the slice never commits and the work table rows are removed in `finally`.
2. **Package as the plain jar** decided on 2026-10-25 (the module is a Spring Boot application today: a separate jar artifact or sub-module is still to do).
3. **HANA**: types and scales of the 35 tables, and `(1==1)` in the no-filter portfolio query, are assumed; verify against QA read-only when reachable.
4. **verify**: `/code-modernization:modernize-verify otr-sva-optimus-corredores BcsCustodiaServicios` must still run fresh inputs and the independent re-check; the SME questions Q-CUS-01..04 stay open.

## Plain jar for in-process consumers (2026-10-28)

Follow-up 2 of "Follow-ups for dependent modules" is done for the pom: `spring-boot-maven-plugin` now has `<classifier>exec</classifier>` (as `BcsTesoreriaServicios`). The main artifact `bcs-custodia-servicios-1.0.0-SNAPSHOT.jar` is a plain jar (no `BOOT-INF`), the executable one is `...-exec.jar`; the `Dockerfile` copies `target/*-exec.jar` (a `*.jar` glob would now match both). No source, resource or behaviour changed.

- `mvn -o clean test`: **228 tests, 0 failures, 0 errors, 6 skipped** (the same count as `equivalence/BcsCustodiaServicios-verify5.test-output.txt`; `equivalence cases executed: 85 of 85`).
- `mvn -o install -DskipTests`: plain jar, `-exec` jar and pom installed in `~/.m2/repository/cl/bcs/custodia/bcs-custodia-servicios/1.0.0-SNAPSHOT/` (groupId `cl.bcs.custodia`, as this pom declares). FondoMutuo resolves it offline from there, like `tesoreria-service`.
- First consumer: BcsFondoMutuoServicios (`custodia.FlujoCustodiaEnProceso`, see its notes, "Custodia en proceso"). It uses only `cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo`, supplies `secuencias` with its `SecuenciaPort` and closes `valorizador` (ValorizarInstrumento is not available there; valued flows are refused with 503). Consumers must not depend on `flujo.jdbc` / `flujo.kit` (public only for the jar).
