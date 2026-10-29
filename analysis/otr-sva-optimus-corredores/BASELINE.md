# BASELINE — BcsContabilidadServicios (Phase 1 Pilot)

**Project:** otr-sva-optimus-corredores  
**Phase:** 1 (Pilot — BcsContabilidadServicios + BcsUtils)  
**Captured:** 2026-10-02  
**Captured by:** m.villagran.mora@accenture.com

---

## Equivalence strategy

**TRACE-BASED** — legacy cannot run standalone. It requires live HANA (`jdbc:sap://161.238.*.*:30215`) and a running BcsPlataformaServicios TomEE instance; neither is available in CI. All port calls (PlataformaPort, ContabilidadRepository) are mocked to match production traces extracted from legacy source analysis and rule extraction (78 rules, 20 P0 reviewed).

Dual-execution diff (≥ 1,000 production calls, legacy side-by-side with modernized) is **deferred to staging deployment gate** — a TomEE staging environment must be provisioned first.

---

## Test results — 2026-10-02

**Run command:**
```bash
mvn install -pl BcsUtils -am -DskipTests
mvn test -pl BcsContabilidadServicios
```

**Result: 34 PASS / 0 FAIL / 0 SKIP / 0 ERROR**

### CentralizaAnticipoPactosServiceTest (17 tests — B-02, B-04)

| Test | Status | Behavior |
|------|--------|----------|
| rule_b04_mapDatosMovimientoProduces26Entries | PASS | B-04 |
| rule_b04_nullStringFieldsBecomeEmptyString | PASS | B-04 |
| rule_b04_toResultadoCopiesAllFieldsFromTabMovimiento | PASS | B-04 |
| rule_b04_nullLongFolioOperacionBecomesStringNull | PASS | B-04 |
| rule_b02_realRunCallsPersistir | PASS | B-02 |
| rule_b04_firstEntryIsIdMovimiento | PASS | B-04 |
| rule_b04_movimientosQueriedWithCorrectParams | PASS | B-04 |
| rule_b02_simulationModeSkipsRepositoryWrites | PASS | B-02 |
| rule_b04_folioOperacionValueIsStringOfLong | PASS | B-04 |
| rule_b04_singleMovimientoProducesOneResultadoEntry | PASS | B-04 |
| rule_b04_toResultadoConvertsEjercicioLongToString | PASS | B-04 |
| rule_b04_portafolioFieldNameIsPortafolioCliente | PASS | B-04 |
| rule_b04_allDatoEntriesHaveValorDecimalZero | PASS | B-04 |
| rule_b04_eventosSubyacenteUseFechaAnteriorFromPlatforma | PASS | B-04 |
| rule_b04_tipoReajusteFieldNameIsIdTipoReajuste | PASS | B-04 |
| rule_b04_obtenerDiaAnteriorUsesIdPaisFromEmpresa | PASS | B-04 |
| rule_b04_callOrderConsultarEmpresaThenObtenerDiaAnterior | PASS | B-04 |

### CentralizacionOrchestratorTest (8 tests — B-01, B-03, B-05, B-06)

| Test | Status | Behavior |
|------|--------|----------|
| rule_b01_iteratesTwoJavaTypesAndAccumulatesResultados | PASS | B-01 |
| rule_b01_erroresContadorStartsAtZeroForEmptyInput | PASS | B-01 |
| rule_b03_singleExceptionIncrementsCounterAndBatchContinues | PASS | B-03 |
| rule_b05_typeWithNullTipoIsFilteredWithWarning | PASS | B-05 |
| rule_b05_duplicateIdsAreDeduplicatedBeforeDispatch | PASS | B-05 |
| rule_b05_unknownIdProducesNoExisteWarning | PASS | B-05 |
| rule_b06_simulaOperacionPropagatedToEachDelegate | PASS | B-06 |
| rule_b06_simulationStillComputesResultados | PASS | B-06 |

### CreaInterfazDiariaJobTest (9 tests — B-07)

| Test | Status | Behavior |
|------|--------|----------|
| rule_b07_fechaEjecucionIsYesterday | PASS | B-07 |
| rule_b07_jobWritesAllThirteenExpectedFiles | PASS | B-07 |
| rule_b07_fiFileNamesMatchLegacyOracle | PASS | B-07 |
| rule_b07_bpcFileNamesMatchLegacyOracle | PASS | B-07 |
| rule_b07_fiOutputPathStructure | PASS | B-07 |
| rule_b07_bpcOutputPathStructure | PASS | B-07 |
| rule_b07_jobWritesBroadCuentasFile | PASS | B-07 |
| rule_b07_cuentasFailureDoesNotAbortMovimiento | PASS | B-07 |
| rule_b07_sftpCredentialsAreNotHardcoded | PASS | B-07 |

---

## Behaviors pinned

| Code | Description | Legacy oracle (file:line) | Deviation |
|------|-------------|---------------------------|-----------|
| B-01 | Dispatch loop accumulates resultados across all TipoCentralizacion | `CentralizaMultiSeleccion.logica()` L301–366 | DEV-01: counter is additive not overwriting |
| B-02 | simulaOperacion="S" → persistirCentralizacion NOT called | `CentralizaMultiSeleccion.logica()` L411 | None |
| B-03 | Exception per type → erroresContador +1; batch continues | `CentralizaMultiSeleccion.logica()` catch L361–366 | DEV-01: additive (intentional fix) |
| B-04 | consultarEmpresa → obtenerDiaAnterior → query → map 26 dato-fields | `CentralizaAnticipoPactos.centralizar()` L64–143 | DEV-04: null Long → "null" string preserved as oracle |
| B-05 | Unknown idTipo → warning; null Tipo → warning; dedup on input ids | `CentralizaMultiSeleccion.logica()` L211–237 | DEV-03: sequential not parallel stream |
| B-06 | simulaOperacion propagated verbatim to each delegate | `CentralizaMultiSeleccion.logica()` L343 | None |
| B-07 | Date -1, 13 files generated, FI + BPC path structure | `CreaInterfazDiaria.logica()` L78–227 | SFTP credentials from env vars only (no hardcoded) |

### Key field-name oracles (B-04)

- `"PortafolioCliente"` — NOT `"Portafolio"` (legacy `CentralizaAnticipoPactos` L113)
- `"IdTipoReajuste"` — NOT `"TipoReajuste"` (legacy `CentralizaAnticipoPactos` L117)
- `ejercicio` → `String.valueOf(Long)` — NOT `Long.toString()` (produces `"null"` for null)

---

## Dual-execution baseline

**Not available — deferred to staging deployment gate.**

A TomEE staging environment must be provisioned with access to the HANA QA instance before a production-trace comparison (≥ 1,000 calls) can be executed. The strangler-fig gateway (`contabilidad-routes.yml`) will route a shadow percentage of traffic to the modernized service; diverging responses will be captured and reviewed before cutover.

---

## Canary

Changed `"PortafolioCliente"` → `"Portafolio"` in `CentralizaAnticipoPactos.mapDatosMovimiento()` → **1 test failed** (`rule_b04_portafolioFieldNameIsPortafolioCliente`). Restored → 0 failures. The test suite is sensitive to field-name changes.
