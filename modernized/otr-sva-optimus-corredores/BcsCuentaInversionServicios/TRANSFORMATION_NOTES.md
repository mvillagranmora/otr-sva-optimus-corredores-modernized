# BcsCuentaInversionServicios: Transformation Notes

**Equivalence is dual-execution on the legacy classes run locally** (the legacy cannot build standalone; its 44 JavaCall classes were compiled from `legacy/` into a scratch folder and run on in-memory H2 with minimal runtime shims). No HANA, QA or third-party service was called.

| Item | Detail |
|---|---|
| Legacy | 44 JavaCall classes, 18,705 non-blank lines (`BSCL/CuentaInversion/JAVA/BcsCuentaInversionServicios`) |
| Target | Spring Boot 4.1.1 / Java 25, ports and adapters; one `*Service` per legacy class |
| Decisions (user, 2026-10-19) | Legacy defects are **kept** and listed for the domain expert (SME); no traffic goes to this service yet; HANA schema assumptions are documented assumptions; the transaction model (autocommit off, commit on return, rollback on throw) is confirmed |
| Tests | 588, 0 failures, 0 skipped (355 with JVM zone UTC + 233 `reportes` with JVM zone America/Santiago: two surefire executions, see Deviations) |
| Equivalence cases executed | **1296** in `analysis/otr-sva-optimus-corredores/equivalence/BcsCuentaInversionServicios/cases.json`: 1296 same, 0 differ, no masks (`compare.py`, self-check passed) |
| Goldens | 479 cases recorded by running the unmodified legacy: 254 transaccional (CTA-A-01..254), 225 reportes (CTA-B-01..225) |

## Mapping (class level; line-level traceability is by behavior id CTA-A-NN / CTA-B-NN in the READMEs and in a comment on every class)

| Legacy class | Legacy LOC | Modern class (`service/`) |
|---|---|---|
| `ActualizarSaldoCargoAbono` | 233 | `ActualizarSaldoCargoAbonoService` |
| `ActualizarSaldosReporteAntiguedad` | 311 | `ActualizarSaldosReporteAntiguedadService` |
| `AnularCargoAbono` | 475 | `AnularCargoAbonoService` |
| `CargaInicialSaldosReporteAntiguedad` | 316 | `CargaInicialSaldosReporteAntiguedadService` |
| `CertificarCargoAbono` | 559 | `CertificarCargoAbonoService` |
| `ConsultaAntiguedadDeudasCuentaInversion` | 1264 | `ConsultaAntiguedadDeudasCuentaInversionService` |
| `ConsultaComparacionSaldo` | 230 | `ConsultaComparacionSaldoService` |
| `ConsultaReporteAcreedores` | 750 | `ConsultaReporteAcreedoresService` |
| `ConsultaReporteAntiguedadBolsa` | 750 | `ConsultaReporteAntiguedadBolsaService` |
| `ConsultarCartolaDeMovimiento` | 554 | `ConsultarCartolaDeMovimientoService` |
| `ConsultarCobroComisionRegistros` | 186 | `ConsultarCobroComisionRegistrosService` |
| `ConsultarMovimientoCuentaInversion` | 293 | `ConsultarMovimientoCuentaInversionService` |
| `ConsultarMovimientos` | 341 | `ConsultarMovimientosWSService` |
| `ConsultarSaldoCuentaInversion` | 1027 | `ConsultarSaldoCuentaInversionService` |
| `CrearCargaMasiva` | 261 | `CrearCargaMasivaService` |
| `CrearCargoAbono` | 944 | `CrearCargoAbonoService` |
| `CrearCargoAbonoWS` | 284 | `CrearCargoAbonoWSService` |
| `CrearCobroComisionExclusionConcepto` | 267 | `CrearCobroComisionExclusionConceptoService` |
| `CrearCobroComisionOpcionesCobro` | 222 | `CrearCobroComisionOpcionesCobroService` |
| `CuadraturaCierreDiario` | 285 | `CuadraturaCierreDiarioService` |
| `EditarCobroComisionOpcionesCobro` | 176 | `EditarCobroComisionOpcionesCobroService` |
| `EditarCobroComisionRegistros` | 182 | `EditarCobroComisionRegistrosService` |
| `EliminaCargaSeleccionada` | 93 | `EliminaCargaSeleccionadaService` |
| `EliminarCobroComisionExclusionConcepto` | 180 | `EliminarCobroComisionExclusionConceptoService` |
| `EliminarCobroComisionOpcionesCobro` | 161 | `EliminarCobroComisionOpcionesCobroService` |
| `EliminarCobroComisionRegistros` | 184 | `EliminarCobroComisionRegistrosService` |
| `GenerarArchivoCargoAbono` | 252 | `GenerarArchivoCargoAbonoService` |
| `GenerarArchivoClientesPorPagarYCobrar` | 872 | `GenerarArchivoClientesPorPagarYCobrarService` |
| `GenerarArchivoCuentasPorCobrarAnexo16` | 1136 | `GenerarArchivoCuentasPorCobrarAnexo16Service` |
| `GenerarArchivoSaldoCuentas` | 672 | `GenerarArchivoSaldoCuentasService` |
| `GenerarInformePatrimonioDiario` | 319 | `GenerarInformePatrimonioDiarioService` |
| `GenerarInformeResumenCobro` | 230 | `GenerarInformeResumenCobroService` |
| `GenerarReporteAcreedores` | 342 | `GenerarReporteAcreedoresService` |
| `GenerarReporteAntiguedadDeudaBolsaCTA` | 412 | `GenerarReporteAntiguedadDeudaBolsaCTAService` |
| `GenerarReporteAntiguedadDeudaCTA` | 441 | `GenerarReporteAntiguedadDeudaCTAService` |
| `GuardarPatrimonioCaja` | 380 | `GuardarPatrimonioCajaService` |
| `GuardarPatrimonioCajaCalculoComision` | 249 | `GuardarPatrimonioCajaCalculoComisionService` |
| `GuardarSaldosReporteAntiguedad` | 335 | `GuardarSaldosReporteAntiguedadService` |
| `IndiceRentabilidadAtributtionAnalysis` | 159 | `IndiceRentabilidadAttributionAnalysisService` |
| `InformeAtributtionAnalisys` | 955 | `InformeAttributionAnalysisService` |
| `ProcesaCargaSeleccionada` | 289 | `ProcesaCargaSeleccionadaService` |
| `ProcesaIndicesAtributtionAnalisys` | 201 | `ProcesaIndicesAtributtionAnalisysService` |
| `ProcesarCobroComisionPatrimonio` | 329 | `ProcesarCobroComisionPatrimonioService` |
| `SaldoCuentaInversion` | 604 | `SaldoCuentaInversionService` |

Shared pieces (new): ports `Colaboradores`, `ColaboradoresReportes`, `DiccionarioMensajes`, `SecuenciasPort`, `AuditoriaReportes`, `GeneradorPdf` (`port/out`); adapters `ColaboradoresJavaCall`, `ColaboradoresReportesAdapter`, `DiccionarioJdbc`, `SecuenciasJdbc`, `AuditoriaReportesAdapter`, `GeneradorPdfNoDisponible` (`infrastructure/adapter`); `JavaCallDispatcher` (the class-name to service table the controller used to hold as a switch); the report kit in `cl.bcs.cuentainversion.reportes` (`SqlReportes`, xlsx writers, aging buckets, etc.). SQL lives in `src/main/resources/cl/bcs/cuentainversion/sql/`.

## Representative behavior, side by side

Legacy `CrearCargoAbono.java:566` checks `MONTO_MENOR_IGUAL_A_CERO` only for `< 0`, so a zero amount is accepted (CTA-A-58). The modern code reproduces it:

```
legacy:  ProcesaRequisito(con, in.p_IdEmpresa, (proc.Monto.compareTo(BigDecimal.ZERO) == -1), "MONTO_MENOR_IGUAL_A_CERO", ...)
modern:  boolean esMontoNegativo(BigDecimal monto) { return monto.compareTo(BigDecimal.ZERO) < 0; }   // CrearCargoAbonoService:656, LEGACY DEFECT kept (CTA-A-58)
```

## Deliberate deviations

- **None in behavior.** Every legacy defect found by the 479 recorded cases is reproduced and marked `LEGACY DEFECT kept (see README CTA-A-NN | CTA-B-NN)`. The full list (about 70 transaccional, about 45 reportes) is in `src/test/java/cl/bcs/cuentainversion/transaccional/README.md` and `.../reportes/README.md`, section "Legacy defects for SME review".
- **Structure:** the legacy called other JavaCall services in process; the modern code goes through the `Colaboradores` / `ColaboradoresReportes` ports (in process for this module's classes, HTTP to `{JAVACALL_URL}/internal/java-call` otherwise). Message dictionary, business calendar and currency format are injected ports.
- **POI:** xlsx are written with Apache POI 5.3.0 (the legacy used 3.17). The goldens were recorded with POI 3.17 and 4 styling constants rewritten (no cell value touched).
- **Test infrastructure:** surefire runs `transaccional/` and the pre-existing tests with `-Duser.timezone=UTC`, and `reportes/` with `America/Santiago`, in separate forks, because H2 caches the default zone and the goldens of the two families were recorded under different zones. In a single JVM that gives 96 red tests (dates off by one day).

## Not migrated / not proven

- **HANA stored procedure** `BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono` (the `PLA.UsaCargoAbonoJava=N` path): its SQLScript is not in the repo and cannot run on H2. Only the Java side is pinned (CTA-A-241..246); the case seed plays the procedure by pre-filling its output tables. The call syntax is unverified on HANA, and the test compares every scalar input except `p_UsaReglasNegocio` (the golden records `"N"`, the SQL fixes `'S'` as the legacy does).
- **PDF:** no PDF library is available offline. `GeneradorPdf` is a port with no real adapter (`GeneradorPdfNoDisponible` is a placeholder so the application starts). The tests use a recording double; the goldens pin the cell texts of `GenerarArchivoCuentasPorCobrarAnexo16` and `GenerarArchivoSaldoCuentas`, not layout, fonts or pagination. A production adapter and the choice of library are open.
- **Collaborator numerics** (`MovimientosTitulos`, `MovimientosCaja`, `DatosCartolaSocioNegocio`, `EntregarPreciosTasas`, `ConsultarSaldo`, `CalcularParidad`, `CrearMovimientoComprobante`): their answers are scripted; only the calls are pinned. `BcsNormativaServicios` does not yet expose the cartola classes under their legacy names, so those adapter calls only work once it does. `ConsultarEmpresa` has no JSON entry point in the legacy; the `{"valor": id}` convention is used.
- **Assumed on H2 and not verified on HANA:** `DECIMAL(28,16)` money columns (the CSV shows 16 decimals because of it; it is not a legacy rule), views modeled as tables, `PLA.Identidad` as a 6x6 identity, UNIQUE on `CobroComisionOpcionesCobro(OpcionCobro, PortafolioAPV, IdClasificacionTipoInstrumento)`, PK on `DefinicionIndiceFinanciero` and on `CuadraturaDiariaOperaciones`, `GBL.ConsultarSaldoCuentaInversion_Cuenta_Delete` as a separate table, the `CalcularParidad` shim with `MontoLocal = MontoDestino`, `TO_DECIMAL`/`MAP` rewritten to `CAST`/`CASE`, `HANA_UTCNOW()` as a fixed clock, the behavior of a failed batch (CTA-A-116), and the HANA syntax of `SecuenciasJdbc` (`SERIES_GENERATE_BIGINT`, `NEXTVAL`). A HANA-only construct that H2 cannot run would fail on both sides.
- **Case 222** (`InformeAtributtionAnalisys`, `subMercado == ""` is a reference comparison): under H2 the empty string is interned so no sub-market header is produced; HANA's driver may return a new String and the header would appear.
- **ICU4J:** the legacy builds against 71.1; 4.6 (the version in the local cache) was used for the oracle. The class only uses basic Gregorian `Calendar` calls.
- **JVM-dependent output:** currency formatting (JDK/CLDR) and the default Gson date format of `GenerarArchivoClientesPorPagarYCobrar`.
- **Router URL and internal token:** `JAVACALL_URL` defaults to the placeholder `http://localhost:8080`; the real router URL is a deployment decision. `X-Internal-Token` is sent by the router (`BcsPlataformaWeb`, ARQ-H5) and validated here by `TokenInternoFilter` (secret from `INTERNAL_TOKEN`; empty secret rejects every `/internal/**` call).

## Proof the tests can fail (Canary)

Three one-line breaks in a scratch copy outside `modernized/`; evidence in `analysis/otr-sva-optimus-corredores/equivalence/canary/BcsCuentaInversionServicios-transform/`:

- `ConsultarSaldoCuentaInversionService.java:736` CLP truncation `RoundingMode.DOWN` to `HALF_UP` -> **19 tests failed** (25 run).
- `CrearCargoAbonoService.java:656` `compareTo(ZERO) < 0` to `<= 0` -> **1 test failed** (CTA-A-58; 29 run).
- `IndiceRentabilidadAttributionAnalysisService.java:73` `RoundingMode.HALF_UP` to `DOWN` -> **6 tests failed** (28 run).

The Step 1 agents also killed mutants of their test-side reference ports (Actualizar 4, GenerarArchivoCargoAbono 4) and of copies of the legacy classes (family B: six mutations changed 32 of 181 goldens; four more changed the new goldens).

## How to run and re-record

- Run: `mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :bcs-cuenta-inversion-servicios clean test`
- Dual-execution dump: add `-Dequivalence.out=<dir>`; each comparison writes the canonical legacy and modern text to `<dir>/legacy|new/<familia>/`. The harness self-checks (deliberately altered outputs) are excluded from the dump.
- Re-record goldens: see the `README.md` of each test package (`legacy-oracle/record.sh` and the runner).

## Follow-ups

- Production PDF adapter (library decision).
- Verify on HANA (or a HANA-compatible environment): the SQL, the stored-procedure call syntax and the sequence syntax; confirm the schema assumptions against the real DDL.
- Decide with the SME which legacy defects stay (the modern code reproduces all of them).
- `BcsNormativaServicios` must expose the cartola services for `InformeAttributionAnalysisService`.
- `IndiceRentabilidadAttributionAnalysisService.calcularIndice` is not used by `procesar` and rounds to 4 decimals (legacy goldens: 2); it is kept only because the pre-existing `ServiceFormulasTest` pins it.
- Architecture review (Step 5): see below.

## Architecture review (Step 5, architecture-critic, static read: no build, tests or CVE scan; not reviewed: tests, READMEs, bug-for-bug fidelity, `HanaRepository` beyond grep, the 11 REST clients beyond one)

**HIGH (applied):**
- **H1, `/internal/java-call` had no authentication.** Anyone able to reach the port could run `CrearCargoAbono`, `AnularCargoAbono`, `CertificarCargoAbono` and the rest with a caller-chosen `p_IdUsuario` / `p_IdEmpresa`. Fixed: `infrastructure/web/TokenInternoFilter` requires `X-Internal-Token` (constant-time compare, fails closed when no secret is configured), `ColaboradoresJavaCall` sends the same token on its outbound calls, `services.internal-token: ${INTERNAL_TOKEN:}` in `application.yml`; 4 tests in `TokenInternoFilterTest`. Still to do on deployment: set `INTERNAL_TOKEN` in this service, in the router (`plataforma.javacall.internal-token`) and in the services that receive collaborator calls.

**MEDIUM (open, for a person to schedule):**
- M1 `@Transactional` without `rollbackFor = Exception.class` on the write services that declare `throws Exception` (`AnularCargoAbono:155`, `CertificarCargoAbono:168`, `CrearCargoAbono:353/360`, `CrearCargaMasiva:86`, `EliminaCargaSeleccionada:52`, `ProcesaCargaSeleccionada:75`, `CuadraturaCierreDiario:117`); a checked exception after the first write would commit a partial movement set. The reporting services already use `rollbackFor`.
- M2 `ActualizarSaldoCargoAbonoService` declares `Isolation.SERIALIZABLE`, but it is ignored when joining the caller's transaction (Anular, Certificar use the default). Concurrent calls on one account could overwrite balances. Put the isolation on the outer entry points or enable `validateExistingTransaction`, and add a concurrency test.
- M3 `p_Ruta` (and the DB-configured `RutaArchivo`) is used as an output directory without confinement in `GenerarInformePatrimonioDiarioService`, `GenerarInformeResumenCobroService` and the file generators; with H1 fixed the exposure drops to authenticated callers. Resolve against a configured base directory.
- M4 `JAVACALL_URL` defaults to `http://localhost:8080`, this same service; no connect/read timeouts on any `RestClient` (a hung call holds a thread inside an open DB transaction). Remove the default and add timeouts.
- M5 The 11 `*RestClient` classes send no token and have no timeout or error mapping; use a shared `RestClient.Builder` with timeouts and the token.
- M6 `InternalJavaCallController` returns `e.getMessage()` (HANA object names) to the caller; return a generic message plus a correlation id.
- M7 A non-object `params` or non-string `class` gives 500 instead of 400.
- M8 `ProcesarCobroComisionPatrimonioService:107` does `new ConsultarCobroComisionRegistrosService(dataSource)`, bypassing the Spring proxy (`readOnly` transaction not applied); inject it. Same for scattered `new SqlReportes(...)` / `new Mensajeria(...)`.
- M9 `poi-ooxml` pinned to 5.3.0 locally (large transitive tree: xmlbeans, commons-compress, log4j-api); only the xlsx writers need ooxml. Dockerfile runs as root; set `management.endpoints.web.exposure.include=health` explicitly.
- M10 `ThreadLocalRandom` job ids for staging rows and `:IdTrabajo` replaced as text (safe because it is a `long`): use a sequence and a bind parameter.

**LOW:** `ExcelInformes` uses `FileWriter` with the platform charset; `SimpleDateFormat` per call and zone-dependent dates (production has no `-Duser.timezone`: pin the zone in config); `SqlReportes` interpolates a sequence name into `NEXTVAL` (constant callers today; do not extend with input); the zips contain a placeholder `.pdf` until a real `GeneradorPdf` adapter exists; observability is only `INICIO::/FIN::` (no correlation id or metrics); `JavaCallDispatcher` uses a plain `HashMap` (populated once, use `Map.copyOf`); `CalendarioReportes.paisCargado` and `FormatoMoneda.reglas` are lazily cached instance fields; services of 700 to 900 lines with legacy field names (forced by the bug-for-bug JSON contract); `FormatoMoneda` and `MensajeSalida` are duplicated in `service/`, `reportes/` and `domain/`; the hexagonal structure is partial (only 2 `port/in` use cases, services use `NamedParameterJdbcTemplate` directly), so do not describe it as ports and adapters in documentation; `SqlReportes:143` sniffs `getDatabaseProductName()` to switch H2/HANA behavior (confirm it cannot be triggered in production by a misconfigured URL). No mocks, fakes or in-memory implementations found in `src/main`; no committed secrets.
