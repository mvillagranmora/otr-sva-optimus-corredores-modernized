# BcsAPVJobs: transformation notes

| Item | Detail |
|---|---|
| Legacy | `legacy/otr-sva-optimus-corredores/BSCL/APV/JAVA/BcsAPVJobs` (4 classes, 1,368 lines) and the job framework `BSCL/Plataforma/JAVA/BcsJobManager/src/cl/bcs/scheduler/BcsJob.java` |
| Target | Spring Boot 4.1.1 / Java 25, artifact `cl.bcs:apv-jobs` |
| Definition of done | The Step 1 characterization tests (commit `8c1a491c`, 140 goldens recorded by EXECUTING the unmodified legacy under the real `BcsJob.run()`) |
| Architecture | In-process jobs on the PROVEN `apv-service` as a library (its use case `CargarArchivoPrevired` and the ports `Colaboradores`, `ConfiguracionSistema`, `Reloj`; no HTTP between job and service), one transaction per run with the BcsJob contract (`JobRunner`, copy of the Tesoreria one with its architecture-review fixes), `POST /internal/job-call` with the internal token, no `@Scheduled`, no daemon mode (RULE-061), no debug `main()` / credentials (RULE-076), no 3DES (RULE-077) |
| Behavior policy | Bug for bug: NONE of the 17 legacy defects of `README_CARACTERIZACION.md` is fixed |

## Proof metrics (Step 3)

| Metric | Value |
|---|---|
| Command | `mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :apv-jobs clean test -Dequivalence.out=<dir>` (JDK 25) |
| Tests | **207 run, 0 failures, 0 errors, 0 skipped** (after the pilot was deleted): 140 golden cases (CMP 30, OBF 10, FMB 50, RVB 50), `JobRunnerContractTest` 11, `JobRunnerRevisionArquitecturaTest` 6, RULE-061 4, RULE-076 5, RULE-077 10, `JobCallControllerTest` 20, harness 5 + inventory 2, `ArranqueAplicacionTest` 4. (Before: 231 run with the pilot's 24 tests, 1 `@Disabled`.) |
| Golden cases executed | **140/140 pass** (OBF-006 resolved by option (a), below) |
| Legacy vs. new (`compare.py`) | **560/560 same**, differs 0, missing 0, self-check passed (dump regenerated 2026-10-21). Files: `analysis/otr-sva-optimus-corredores/equivalence/BcsAPVJobs/{cases.json,EQUIVALENCE.json,out/legacy,out/new}` |
| Legacy retired | 4 classes, **1,368 lines** (+ `BcsJob`, replaced by `JobRunner`) |
| New code | 1,340 hand-written lines in `job` (+ 417 `runtime`, 227 `web`, 100 `wiring`, 84 `adapter`) and 13 generated contract classes (1,888 lines, verbatim copies of the legacy JSON POJOs); 9 SQL files copied from the legacy (LF) |

### Resolved decision: APVJ-OBF-006 (option (a), user, 2026-10-21)

`IngresarOrdenBonificacionFiscal` inserts its log row, then `CrearOrdenes` throws an `Error`. The legacy `BcsJob.run()` catches only `Exception` for the
rollback (BcsJob.java:140-144) and, for an `Error`, only closes the connection (:146-148); the oracle dumped the tables on that same, still open
connection, so the golden shows the uncommitted `BAT.LogPrograma` row. `JobRunner` rolls back on every `Throwable`. In production both end the same (HANA
rolls back an open transaction when the connection is closed); only the in-process dump differs. Resolution: `VerificaJob.comparar` MASKS the `tablas`
comparison point, on both sides (legacy golden and new run, so both dump files carry the same masked text), when the run ends with a `java.lang.Error`
(`resultado.esException == false`: CMP-025, OBF-006, RVB-039); the trailing `rollback` event was already masked. No `expected.json` was changed.

## Structure

| Package | Content |
|---|---|
| `runtime` | `ApvJob`, `JobContext`, `JobRunner`, `JobResultado`, `JobStatusListener`, `JobNoMigradoException`, `JobEnCursoException` |
| `job` | The four jobs, `FilasBonificacion`, `FilasRV`, `ConstructorOrdenesRV`, `FormatoCantidad`, `LogPrograma`, `ConfiguracionLegacy`, `JsonLegacy`, `StoredProcedureException` |
| `job/contrato` | Verbatim JSON contracts of the collaborators (13 classes) |
| `adapter` / `web` / `wiring` | `ColaboradoresJobs` / `JobCallController`, `TokenInternoFilter` / `ServiciosApvConfiguration`, `JobsConfiguration` |

`ApvJobsApplication` scans only `wiring`, `adapter` and `web` (`scanBasePackageClasses`), so the pilot classes are not beans (`ArranqueAplicacionTest`);
`@EnableScheduling` and the datasource auto-configuration excludes of the pilot were removed. Configuration is `application.properties`
(`HANA_*`, `JAVACALL_URL`, `INTERNAL_TOKEN`, `APV_ARCHIVOS_DIR`); `Dockerfile` builds Utils, the API, the JavaCall adapter and Servicios first.

## Not proven

* **HANA.** The schema is INFERRED (`schema-apv-jobs.sql`); types, `TO_NVARCHAR`, `CURRENT_TIMESTAMP`, the temporary table `#p_Instrumentos` and the real
  procedures are not exercised. **H2 commits on DDL**: the `DROP TABLE #p_Instrumentos` of the RV job commits earlier writes, so the RV failure goldens show
  a `LogPrograma` row that HANA (`AUTOCOMMIT DDL OFF`) would roll back; the proof cannot show the HANA rollback.
* **Real collaborators.** `CrearOrdenes`, `CalcularMontoYCobrosOrden`, `ConsultarCustodiaPortafolio`, `ConsultarFlujoCaja`, `ConsultarEmpresa`, the fund
  services and the price procedure are scripted; `ColaboradoresJobs` is exercised through the Spring context only (no test against a router); the
  price-procedure call text is the legacy one and has not run on HANA.
* **No Quartz / Job Manager.** Scheduling and cut-over are Phase 5; running legacy and this module together risks double execution.
* The installed `apv-service` pom in `~/.m2` is reported as invalid when read as a dependency (its managed versions); this module declares the API and adapter
  jars itself, so it resolves, but a clean `mvn install` of Servicios should be repeated by the owner.

## Obsolete pilot: DELETED on the user's instruction 2026-10-21

Deleted (35 files): main `domain/*` (8), `infrastructure/adapter/ApvJobsRepository`,
`infrastructure/client/*` (5), `job/CargaMasivaPreviredJob`, `job/IngresarOrdenBonificacionFiscalJob`, `job/IngresarOrdenFMBonificacionFiscalJob`,
`job/IngresarOrdenRVBonificacionFiscalJob`, `port/out/*` (11); `src/main/resources/application.yml` (only duplicated `application.properties` plus the pilot
`services:` keys); tests `CargaMasivaPreviredJobTest`, `FMBonificacionFiscalTest`, `RVBonificacionFiscalTest` (24 tests, 1 `@Disabled`); the pilot
`README.md` of the module and of `src/test/java/cl/bcs/apv/jobs`. Verified by grep that no remaining class referenced them; `pom.xml` had no
spring-batch / opencsv dependency to remove. `ModuloFuente.PILOTO_OBSOLETO` (the skip list of the security scans) was removed, so those scans now cover all of
`src/main`. `ApvJobsApplication` and `application-test.properties` stay.

## Open review findings (pattern `analysis/.../JOBS_PATTERN.md`, all OPEN, planned as one cross-cutting task)

* **H4** parameters from `BAT.Programa` and a database claim of the run (today the caller of `/internal/job-call` chooses the parameters; the guard is per JVM
  and per class, not per program).
* **H3** bound every run (transaction and query timeouts, watchdog) and list the runs in progress.
* **H2** output files on a shared volume, healthcheck and non-root user in the `Dockerfile`.
* Also M4 (guard key per program), M6 (graceful shutdown), M7/M10 (`JAVACALL_URL` validation, minimum token length, log the 401s).
* The `RutaSegura` path check of Tesoreria is not needed here: the only request-controlled part of a file name is `p_IdUsuario`, a number; the base folder is
  configuration.

## Architecture review (step 5)

Legacy behavior and the 17 defects stay bug for bug; the 140 goldens and the dual dump are unchanged. (The last bullet above about `RutaSegura` is superseded by H-3.)

### APPLIED
* **H-2** `bcs-apv-jobs` in both `docker-compose.yml` files aligned with `bcs-tesoreria-jobs`: `HANA_URL/USER/PASSWORD`, `JAVACALL_URL` (default `http://bcs-plataforma-web:8117`),
  `INTERNAL_TOKEN` (`:?` guard), new `APV_ARCHIVOS_DIR` (default `/data/archivos`, property `apv.archivos.base-dir`) with a bind mount `${APV_ARCHIVOS_HOST_DIR:-./data/archivos}:/data/archivos`;
  the stale `APV_SERVICIOS_URL` (read nowhere) is gone. Env vars the module really reads: `SERVER_PORT`, `HANA_*`, `JAVACALL_URL`, `INTERNAL_TOKEN`, `APV_ARCHIVOS_DIR`.
  The `bcs-apv-servicios` entry (not touched) must mount the same host folder for the two to share files.
* **H-3 (partial)** `job/archivo/RutaSegura` (own copy of the lenient Tesoreria rule): `idUsuario` is checked as a name part and the log path must stay under `PLA.RutaBaseArchivos`.
  OPEN: with a blank base the legacy path is `"" + "/" + "APV/LOG..."` = `/APV/...` (container root), pinned by the goldens, so it is NOT checked and not changed; in a container this writes at `/`.
* **M-5** `ColaboradoresJobsEnrutamientoTest`: scans every `invocar` literal of the module (no non-literal call allowed), each has a route in `ColaboradoresJobs.CLASES` or is known by the service
  adapter; ConsultarFlujoCaja and ConsultarFlujoCustodiaFondoMutuo are sent to the router with their legacy FQN and the token.
* **M-9** `ContextoDeJobsTest`: expected beans by type, no `@Scheduled` method, no controller outside `cl.bcs.apv.jobs.web`. `TransaccionesLegacyConfig` (service, pulled by the scan) is intended:
  `globalRollbackOnParticipationFailure=false` also applies to the `JobRunner` transactions (a participating collaborator that throws does not mark the job transaction rollback-only).
* L-3 (missing `p_parametros`) deliberately NOT applied (a golden may pin the NPE). JVM flags/timezone not changed (see M-2).

### OPEN
* **H-1 (IMPORTANT, new, affects ALL jobs and the Servicios)**: in the legacy the jobs ran their collaborators in-process on the job's own connection and transaction
  (e.g. `procesoOrdenes.logica(con,...)` IngresarOrdenRVBonificacionFiscal.java:598, `crearSolicitudAporteRescateNormal.logica(con,...)` IngresarOrdenFMBonificacionFiscal.java:291,
  `llamadoFlujoCaja.logica(con,...)` :371). Now CrearOrdenes, CrearSolicitudAporteRescateNormal, AgregarLog etc. go over HTTP to the platform router and commit in their own transaction:
  a job rollback no longer undoes the orders already created, and a rerun creates them again. The 140 goldens cannot show it because the collaborators are scripted.
  Decision needed (idempotent claim per program, or collaborators that join the job's transaction) before any cutover.
* **H-4** no bound per run; router calls inside the open transaction; ConsultarFlujoCaja once per BUY order (N x 60s); Hikari pool size and connection timeout explicit; run deadline.
* **M-1** graceful shutdown. **M-2** JVM flags/timezone not pinned in the Dockerfile (goldens recorded under UTC/en-US; legacy ran in the TomEE host zone: decide).
* **M-3** router errors masked as INFRASTRUCTURE_ERROR and non-object/empty answers. **M-4** same token inbound/outbound and no https/allow-list on `JAVACALL_URL`.
* **M-6** temp table `#p_Instrumentos` lifecycle unproven on HANA (pooled connection, session-scoped). **M-7** guard per JVM only; `idPrograma` not verified against `BAT.Programa`.
* **M-8** Previred file name has second resolution (collision) and the file is orphaned after a rollback. **M-10** resolved (stale installed parent pom reinstalled). **L-1..L-7** minor, see the review.

## Job pattern H4

Ported from the reference `BcsTesoreriaJobs/TRANSFORMATION_NOTES.md` section "Job pattern H4 (reference implementation)" (commit fa8aabbd): the design, SQL, order of a call, final states and limitations are those, read there. Here only what is APV-specific. Binding constraints unchanged: no database change, legacy untouched, no automatic retry, the dispatcher (later, the modern `BcsJobManager`) calls `POST /internal/job-call` with `idPrograma` and the internal token and must NOT claim the program itself (the endpoint claims atomically; an already claimed program answers 409). Caller `parametros` that differ from the view stay a 422; `clase` is optional in mode `programa`.

### What was ported

* `cl.bcs.apv.jobs.programa` (12 classes, same names and behavior as the reference; `TesoreriaJob` -> `ApvJob`).
* `runtime/JobRunner`: `ejecutarReclamando(clase, id, Supplier<parametros>)`; `ejecutar` delegates to it (APV's JobRunner was identical to Tesoreria's apart from names, so the same edit applied).
* `web/JobCallController`: second constructor taking `EjecutorPrograma`, `exigeClase()` check, handlers 404 / 409 / 422. `TokenInternoFilter` untouched.
* `wiring/ProgramaConfiguration` (new); the logging `jobStatusListener` bean moved there from `JobsConfiguration`.
* Property `apv.jobs.programa.modo`: `programa` in `application.properties`, `parametros-del-llamador` in `application-test.properties` (the goldens and runner tests build `JobRunner` / `new JobCallController(runner)` directly and keep the caller contract).
* New tests (39, same set as the reference): `programa/ProgramaRepositorioLecturaTest` (8), `ProgramaRepositorioReclamoTest` (5, includes the two-thread race), `ProgramaEstadoFinalTest` (9), `ProgramaJobCallTest` (12, HTTP incl. 404/409/422/503), `ProgramaModoTest` (4), `wiring/ProgramaArranqueTest` (1, Spring wiring in mode programa); fixture `support/ProgramaH2`. Suite: 214 -> 253 tests, 0 failures, 0 skipped. Dual dump (`-Dequivalence.out`) identical to `analysis/.../equivalence/BcsAPVJobs/out/{legacy,new}` (560 files each).

### Deviations from the reference

1. Schema file `src/test/resources/apv-jobs/schema-programa.sql` is loaded after `schema-apv-jobs.sql` (APV's name; Tesoreria's is `schema-jobs.sql`) and ONLY by the H4 tests. APV's schema already creates `BAT.Programa` (13 columns), `ParametrosPrograma` (no `ValorBlob`) and `LogPrograma`, so the H4 file ALTERs them to the REAL DDL confirmed by the DBA (adds `Nodo`, `Bloqueo DEFAULT 'N'`, `ZonaHoraria`, `IdProgramaReprocesado`, `RutaLog`, `Comentario`, `ValorBlob`; defaults for `IdAgenda`, `Intento`; fechas TIMESTAMP(0), `RutaSalida` 100, `NombreParametro` 500, `Valor` 5000; `IdTipoPrograma` stays NVARCHAR(40) and is a String in the code) and creates only `TipoPrograma`, `Agenda` and the view. The 140 goldens dump `BAT.Programa` and keep the 13-column table (verified: dumps identical).
2. APV's `H2Hana` has a FIXED clock (`UTC_NOW` = 2025-04-01 10:15:30, no settable `reloj`, no translation of `SET SCHEMA`), unlike Tesoreria's. `ProgramaH2.RELOJ` is `H2Hana.UTC_NOW`; `H2Hana` was not modified. The H4 tests run with `apv.jobs.sesion-hana=false` / a runner without session statements.
3. Test class names use `cl.bcs.apv.Prueba` and the property prefix is `apv.jobs.` (the existing APV prefix, e.g. `apv.jobs.sesion-hana`).

### Limitations

Those of the reference (1-8: process dying after the claim, final-state write failure, no database uniqueness, `Nodo` exclusivity, review marker only by text prefix, committed collaborators, database clock and `MaximoIntentos=0`, not proven on HANA) apply unchanged. APV-specific: the column set and types of the BAT tables and of the view are now the real HANA DDL (CONFIRMED by the DBA, 2026-10-21, `BAT_DDL_REAL.md`); the repository reads `IdTipoPrograma` as a String; primary keys, NOT NULL and defaults stay assumed. The four APV jobs are still `NOT PROVEN` on HANA (see "Not proven").

## Step 5: collaborators in process (JOBS_PATTERN.md H-1; JOBS_H1_H4_INVENTORY.md section D step 5)

Result: NOTHING TO MOVE. No collaborator of group M (already migrated as a library) is reached by the four jobs of this module, directly or through the apv-service use cases they run. No code, wiring, pom, resource or test was changed; the reference mechanism of BcsTesoreriaJobs (`ColaboradoresEnProceso`, `DespachadorEnProceso`, shadowing `application.yml`) is not ported because it would have nothing to route.

Verified in the code (not only in the inventory A.3):

* Direct calls of the jobs (`IngresarOrden*BonificacionFiscal`): CrearOrdenes (TW), CrearSolicitudAporteRescateNormal (TW), AgregarLog (TW), ConsultarEmpresa (TL), ConsultarFlujoCustodiaFondoMutuo (TL), ConsultarCustodiaPortafolio (TL), ConsultarFlujoCaja (TL), CalcularMontoYCobrosOrden (TL) and `StoredProcedure` (already in process, JDBC). None is group M. `ColaboradoresJobs` only adds two router routes (ConsultarFlujoCaja, ConsultarFlujoCustodiaFondoMutuo), both TL.
* `BcsJobCargaMasivaPrevired` has no collaborator of its own; its use case `CargarArchivoPrevired` (`CargarArchivoPreviredServicio`) only invokes `AgregarLog` (TW) and runs `CrearRecaudacionCliente` / `CrearRecaudacionEmpleador` (-> `CambiarEstadoRecaudacion`), none of which invokes any collaborator.
* The group M names that apv-service can call (CrearIngresoEgreso and LiquidarComprobante from `CrearRecaudacionDeposito`, IngresarMandatoPAC from `CargarContratosPAC`, Anulacion/RevocacionMandatoPAC from `CambiarEstadoContratos`, CrearCargoAbono, ConsultarSaldoCuentaInversion from `ConsultarCustodiaPortafolio`) are reachable only from use cases that none of the four jobs runs.

Consequence: all collaborator traffic of this module that leaves the JVM remains HTTP to the platform router, unchanged; nothing is claimed about transactions. If a future job of this module runs one of those use cases, the reference pattern of BcsTesoreriaJobs (section 'Step 5') must be ported then, for the names that job reaches.

## Step 6: fail-closed guard for TomEE write collaborators (JOBS_PATTERN.md H-1 point 2; JOBS_H1_H4_INVENTORY.md section D step 6)

Port of the reference in `BcsTesoreriaJobs/TRANSFORMATION_NOTES.md` ("Step 6": guarantees, limits, mark format `H4G|FASE|id|Intento|clase|n` in `BAT.LogPrograma`, HTTP 423, operator release procedure): read it there; everything stated there applies here unless listed under "Deviations". NO database change (only existing `BAT.LogPrograma` / `BAT.Programa`), legacy untouched, no idempotency header, no automatic retry. Switch: `apv.jobs.guarda-tomee` (`application.properties` = `true`, also when missing; `false` in `application-test.properties`, pinned by `GuardaCableadoApagadoTest`, so the 253 earlier tests and the dual dump run on the pre-step-6 wiring).

### What was ported (package `cl.bcs.apv.jobs.guarda`, copied with the package renamed)

`MarcaGuarda`, `EstadoMarcas`, `MarcasRepositorio`, `MarcasRepositorioJdbc`, `ClasificacionColaborador`, `ColaboradorTomEEGuardado`, `ContextoGuarda`, `CorridaGuardada`, `GuardaTomee`, `GuardaTomeeException`; `programa/ProgramaRequiereRevisionException`; `EjecutorPrograma` (4-argument constructor, `abrirGuarda`, `revisarDespues`, try/finally); 423 handler in `web/JobCallController`; `wiring/GuardaTomeeConfiguration`; `wiring/ProgramaConfiguration` (guard via `ObjectProvider`); property in both profiles. The `programa` package (repository, `requiereRevision`) was already identical to the reference.

### Deviations from the reference (adapted, not invented)

1. Collaborator port wiring. In APV `ColaboradoresJobs` was a scanned `@Component @Primary` (no step 5, no `ColaboradoresEnProceso`). It is no longer a component: new `wiring/ColaboradoresJobsConfiguration` creates it. Guard off: `@Primary` bean `colaboradoresJobs` (same behaviour as before). Guard on: non-primary `colaboradoresJobsHttp` plus `@Primary` `colaboradoresGuardados` (= `GuardaTomee.envolver(http)`). Main change: `ColaboradoresJobs` lost `@Component`/`@Primary`/`@Autowired` and gained `public static nombresPropios()` (names it routes to the platform router, used by the classification test). The component scan (`ApvJobsApplication`) is unchanged: `wiring` is scanned.
2. `ClasificacionColaborador`: APV is the OWNER of `ValidarMandatosPAC` and `CambiarEstadoContratosSAAPV` (MIGRADO_PROPIO, not sent over the port); the group M writers of Tesoreria / CuentaInversion (`CrearIngresoEgreso`, `LiquidarComprobante`, `CrearComprobante`, `AnulacionMandatoPAC`, `RevocacionMandatoPAC`, `IngresarMandatoPAC`, `ActualizarSaldosCuentaBanco`, `AnularNominasTransferencias`, `CrearCargoAbono`, `AnularCargoAbono`) are MIGRADO_ESCRITOR (guarded): for APV they always travel over the router and commit apart (no in-process dispatch here). Added reads: `EntregarPreciosTasas`, `ConsultarImagen`, `ConsultarCustodiaPortafolioFondoMutuo`, `ConsultarClearingFinanciero`.
3. Thread check (reference limit 5): BcsAPVJobs main code starts no thread (no executor, `@Async`, `@Scheduled`; the only `Thread` use is `Thread.interrupted()` in `JobRunner`), so the thread-local scope is kept as is, no scope passing. The library `apv-service` uses `parallelStream()` in `ConsultarCustodiaPortafolioServicio` (over already-fetched lists, around the TL collaborator calls), `ValidarYAsignarContratosARecaudacionesServicio`, `BancoPreviredValidador` and `ProcesarDistribucionServicio`; none of those use cases is run by the four jobs (see Step 5); the lambdas were not audited one by one for collaborator calls. If a future job runs a use case that calls a guarded collaborator from inside a parallel stream, the scope must be passed along or that call would run unguarded.
4. Test fixture: `GuardaBase` uses `ApvJob` (instead of `TesoreriaJob`) and the existing APV `support/ProgramaH2`; no `GuardaCableadoEnProcesoTest` (no step 5). Test `ClasificacionColaboradorTest` scans the module sources for `colaboradores.invocar(` calls and fails if any is not a literal or any name (service adapter names, `ColaboradoresJobs.nombresPropios()`, source literals) is unclassified.

### Classification of what this module can send

| Category | Names | Guarded |
|---|---|---|
| NO_REPETIBLE (17, TW) | `CrearOrdenes`, `CrearSolicitudAporteRescateNormal` (the two the jobs send), `ValidaEjecucionProcesoRecPAC`, `AgregarPrograma`, `InscribirProcesoBatch`, `AgregarEvento`, `CrearEventoAutorizacionMasivo`, `ModificarEstadoEventoAutorizacionMasivo`, `AnulacionContratoCSC`, `RevocacionContratoCSC`, `ModificarPortafolio`, `CrearCliente`, `CrearPersona`, `CrearTelefono`, `CrearDireccionElectronica`, `CrearPersonaCuentaBancaria`, `CrearMovimientoComprobante` | yes |
| APPEND_ONLY | `AgregarLog` (sent by `IngresarOrdenRV...` and by `CargarArchivoPrevired`) | no (as in the reference) |
| MIGRADO_ESCRITOR (10) | the Tesoreria / CuentaInversion writers listed above | yes (none is reached by the four jobs today, see Step 5) |
| MIGRADO_PROPIO (2) | `ValidarMandatosPAC`, `CambiarEstadoContratosSAAPV` | no |
| SOLO_LECTURA | `ConsultarEmpresa`, `ConsultarFlujoCaja`, `ConsultarFlujoCustodiaFondoMutuo`, `ConsultarCustodiaPortafolio`, `CalcularMontoYCobrosOrden`, and the other TL names of A.2 | no |
| ESPECIAL | `StoredProcedure` (JDBC, job connection), `Cloper` | no |

What the jobs reach and is guarded: `IngresarOrdenBonificacionFiscal` and `IngresarOrdenRVBonificacionFiscal`: `CrearOrdenes`; `IngresarOrdenFMBonificacionFiscal`: `CrearSolicitudAporteRescateNormal`; `BcsJobCargaMasivaPrevired`: none (only `AgregarLog`).

### Tests (new: 33, baseline 253 -> 286, 0 failures, 0 skipped)

`guarda/MarcaGuardaTest` 8, `ClasificacionColaboradorTest` 5, `ColaboradorTomEEGuardadoTest` 8, `GuardaProgramaJobCallTest` 9 (MockMvc + real runner / executor / repositories on H2, includes 423), `wiring/GuardaCableadoHttpTest` 2, `wiring/GuardaCableadoApagadoTest` 1. Dual dump (`-Dequivalence.out`): `diff -rq` against `analysis/.../equivalence/BcsAPVJobs/out/legacy` and `out/new` identical (560 files each). HANA not proven (H2 only), same as the reference.

## Step 6b: fail closed when a guarded call has no scope on its thread + build-time scan for parallelism (hardening of step 6)

Binding as in step 6: NO database change (no marks are written by this hardening, only the existing `BAT.LogPrograma` marks of step 6 remain), H2 only, legacy untouched.

### Problem

The run scope of step 6 (`ContextoGuarda`) is a ThreadLocal of the thread that runs the job. A guarded collaborator (NO_REPETIBLE or MIGRADO_ESCRITOR that reaches HTTP) called from ANOTHER thread (a `parallelStream` or a pool inside a library such as apv-service, a `CompletableFuture`) found no scope and the decorator passed it through UNMARKED: a TomEE write with no INTENTO/HECHO mark, invisible to the re-run refusal. Step 6 limit 5 said "the module starts no other thread (checked)"; that was true of this module only, not of the libraries the jobs call.

### Runtime change (fail closed)

`ContextoGuarda` now distinguishes three states of a thread, and knows the mode of the module (`new ContextoGuarda(exigePrograma)`; the bean is built from `ProgramaModo`: `true` only in mode `programa`, i.e. `apv.jobs.programa.modo` = `programa`, the default):

| Thread state | Mode `programa` | Caller mode (`parametros-del-llamador`) |
|---|---|---|
| a `CorridaGuardada` is open (`GuardaTomee.abrir`, after the claim) | guarded names: marks + call (unchanged) | n/a (nothing is claimed) |
| explicit "no program" scope (`GuardaTomee.abrirSinPrograma`, opened by `EjecutorPrograma` for the whole run in caller mode) | pass-through, no marks | pass-through, no marks |
| NO scope at all | guarded name: REFUSED (see below); any other name: pass-through | pass-through (nothing to guard) |

Refusal: before any HTTP call and without writing any mark (there is no `idPrograma` to key it), `GuardaTomeeException("llamada protegida sin contexto de programa (hilo distinto?): <name> ...")` is thrown and logged at ERROR with the thread name. The job sees an exception and rolls back its own work as for any failed collaborator call. Not affected: names that are not guarded (reads, `AgregarLog`, in-process services, unknown names), threads that never call a guarded name (health/actuator, tests), the property `apv.jobs.guarda-tomee=false` (no beans: wiring identical to step 5), and the `ContextoGuarda()` default constructor is the strict one (`true`), so a hand-built context cannot silently be lax.

The scope is NOT propagated to other threads on purpose: the marks of one run are numbered in call order (`n`) and a guarded write racing on another thread would make the INTENTO/HECHO order unreadable. A job that really needs a guarded call from a pool must be redesigned to call it on the job thread.

Tests: `GuardaSinContextoTest` (G6b-01..08: second thread refused with no HTTP call and no marks; `parallelStream` inside a ForkJoinPool; commonPool and `CompletableFuture`; no run at all; caller mode passes on any thread; explicit "no program" scope; unguarded names from other threads; `EjecutorPrograma` opens and closes the caller-mode scope). Test changed: `ColaboradorTomEEGuardadoTest` G6-D05 pinned "no scope = pass-through"; that is now only true for caller mode, so it builds the guard over `new ContextoGuarda(false)`; the no-scope-in-mode-programa case is G6b-04.

### Build-time scan

`ParalelismoFuentesTest` (G6b-S01..S03) scans `src/main/java` of this module and of the sibling libraries its jobs use (this module, BcsUtils, BcsAPVApi, BcsApvJavaCallAdapter, BcsAPVServicios; located relative to the module, existence asserted) for `parallelStream(`, `.parallel(`, ForkJoin, CompletableFuture, ExecutorService/Executors, `@Async`, `new Thread`, `sendAsync`, `StreamUtils.parallel*`. Every hit must be in an explicit allowlist (file, exact number of matching lines, reason); a new hit, one more line in an allowlisted file, a hit on the same line as a collaborator call, or a stale entry fails the build.

### Audit of the `parallelStream` hits in apv-service (verdict per hit)

Question asked of each lambda: can its body, transitively, call a collaborator guarded in this module (`Colaboradores.invocar` of a NO_REPETIBLE / MIGRADO_ESCRITOR name)? Method: read every lambda and every `Colaboradores` reference of each class. Result: NONE can; no hit is on a job path that reaches a guarded collaborator, so nothing is reported as a defect.

| File (BcsAPVServicios, `application/...`) | Lines with a parallel construct | Verdict |
|---|---|---|
| `contratos/ConsultarCustodiaPortafolioServicio` | 11 (custody filters/maps, the `forEach` that builds zero-custody messages, totals, the traspaso filter) | NOT reaching a collaborator. Lambdas only read fields / do `BigDecimal` and collection work; the `forEach` calls `Mensajes.conDiccionario` (read port `DiccionarioMensajes`) and adds to a list (legacy data race kept, README APV-A). The only `Colaboradores.invocar` calls (`ConsultarCustodiaPortafolio`, `ConsultarCustodiaPortafolioFondoMutuo`, `ConsultarSaldoCuentaInversion`) are on the calling thread, outside every lambda, and are SOLO_LECTURA (not guarded in any case). |
| `contratos/ValidarYAsignarContratosARecaudacionesServicio` | 5 (field-equality filters over rows) | NOT reaching a collaborator. The class has no `Colaboradores` dependency (repositories, `BuscarContratosParaRecaudacion` called outside the lambdas, `DiccionarioMensajes`, `Reloj`). |
| `recaudacion/BancoPreviredValidador` | 6 (`distinct`, `toMap`, `groupingBy` over rows) | NOT reaching a collaborator. The class has no `Colaboradores` dependency (only the SQL read port). |
| `recaudacion/ProcesarDistribucionServicio` | 1 (`P_OUT_MENSAJES.parallelStream().filter(tipo == ERROR)`) | NOT reaching a collaborator. It filters the answer of `CrearCargoAbono` (guarded) AFTER that call returned on the job thread, where the guard scope and marks were applied; the lambda is a `tipo` equality. Every `Colaboradores.invocar` of the class (`CrearCargoAbono`, `CrearOrdenes`, `CrearSolicitudAporteRescateNormal`, `AgregarLog`, ...) is on the job thread outside any lambda. |
| `BcsUtils` `StreamUtils.parallelFlatmap` (3 lines) | defined, never called by any scanned source | NOT reaching a collaborator (no caller; a future caller is itself a hit of the scan). |

These verdicts hold for any module that scans these files (the lambdas call no collaborator at all); the allowlist reasons in `ParalelismoFuentesTest` repeat them. If any of these lambdas ever gets a collaborator call, the same-line check and the audit must be redone and the runtime refusal above is the safety net.
