# BcsTesoreriaJobs: transformation notes

| Item | Detail |
|---|---|
| Legacy | `legacy/otr-sva-optimus-corredores/BSCL/Tesoreria/JAVA/BcsTesoreriaJobs` (12 classes, 2,897 lines) and the job framework `BSCL/Plataforma/JAVA/BcsJobManager/src/cl/bcs/scheduler/BcsJob.java` |
| Target | Spring Boot 4.1.1 / Java 25, artifact `cl.bcs:tesoreria-jobs` |
| Definition of done | The Step 1 characterization tests (commit `fcfb08d`, 334 goldens recorded by EXECUTING the unmodified legacy under the real `BcsJob.run()`) |
| Architecture | Decision 2026-10-15/16: in-process jobs on the PROVEN `tesoreria-service` as a library (direct calls, no HTTP between job and service), one transaction per run with the BcsJob contract (`JobRunner`), `POST /internal/job-call` with the internal token, no `@Scheduled`, no daemon mode (RULE-061), no debug `main()` / credentials (RULE-076), no 3DES (RULE-077) |
| Behavior policy | Bug for bug: NONE of the 19 legacy defects is fixed (listed below for the domain expert) |

> **Equivalence is oracle-based and real.** Unlike most job modules, the legacy classes WERE executed: the goldens come from the
> unmodified legacy jobs run under the real `BcsJob.run()` on H2 with minimal shims (`src/test/resources/tesoreria-jobs/legacy-oracle`).
> What is NOT real is HANA (the schema is inferred) and the collaborator services (scripted); see "Not proven".

## Proof metrics (Step 3)

| Metric | Value |
|---|---|
| Command | `mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :tesoreria-jobs clean test -Dequivalence.out=<dir>` (JDK 25) |
| Tests | **454 run, 0 failures, 0 errors, 0 skipped** (after the pilot cleanup; 484 with 10 skipped before it). The 10 skipped are the obsolete pilot's own `@Disabled` tests (the pilot's 54 tests are unchanged). The other 430 are new: the Step 1 tests (5 golden classes with 334 golden cases, runner, RULE-061/076/077, web, harness) and 20 added in Step 2 (`ColaboradoresJobsTest` 7, `CabecerasCargaJdbcTest` 2, `ProcedimientoArchivoJdbcTest` 3, `CsvLegacyWriterTest` 4, `ArranqueAplicacionTest` 4) |
| Golden cases executed | **334/334**: CM 68/68, CG 87/87, PG 123/123, PG-E/D 20/20, PD 36/36 |
| Legacy vs. new (`compare.py`) | **3,637/3,637 same** (differs 0, missing 0, self-check passed): every comparison point of every golden (outcome, exception, status calls, commit/rollback order, dumped tables, files as bytes + SHA-256, directories, collaborator trace, sequences), in `analysis/otr-sva-optimus-corredores/equivalence/BcsTesoreriaJobs/{cases.json,EQUIVALENCE.json,out/legacy,out/new}` |
| Legacy retired | 12 classes, **2,897 lines** (+ `BcsJob`, replaced by `JobRunner`) |
| New code | 35 main Java files (about 3,360 lines) + 7 SQL files copied verbatim from the legacy |

The Step 2 run found one real mismatch first (PD-25: the output handed to `ValidarMandatosPAC` was re-typed and gained default fields);
fixed by passing the service answer on as it is.

### Canaries

Each canary ran on a scratch copy outside `modernized/` and `analysis/` (root pom with only this module; `bcs-utils` and `tesoreria-service`
from `~/.m2`), one line broken, the covering test classes run. Evidence (diff, log, surefire XML) is in
`analysis/otr-sva-optimus-corredores/equivalence/canary/BcsTesoreriaJobs-transform/<n>-<name>/`.

| # | Break | Red tests |
|---|---|---|
| 1 | `job/archivo/FormatoCargos.java` CHAR padding width `length` → `length + 1` | 28 / 88 (`GenerarArchivoCargosGoldenTest`) |
| 2 | `job/BcsJobCargaMasivaIngresoEgresoDAPFFMM.java` window start `isAfter` → `!isBefore` (the start bound becomes inclusive) | 1 / 69 (`CargaMasivaGoldenTest`) |
| 3 | `runtime/JobRunner.java` a `false` result commits instead of rolling back | 6 / 86 (`JobRunnerContractTest`, `JobRunnerRevisionArquitecturaTest`, `CargaMasivaGoldenTest`) |
| 4 | `job/GenerarArchivoPagos.java` `substring(0, length - 9)` → `- 8` | 21 / 145 (`GenerarArchivoPagoGoldenTest`, `ArchivoPagoDirectoGoldenTest`) |
| 5 | `job/BcsJobProcesarDistribucion.java` BOM byte `0xef` → `0xee` | 29 / 37 (`ProcesarDistribucionGoldenTest`) |

`Canary: 5 breaks → 85 tests failed (all detected)`. The weakest pin is the exact start bound of the time window (1 test, CM-20); the end
bound is pinned by CM-23.

## Mapping (legacy → target)

Legacy paths relative to `legacy/otr-sva-optimus-corredores/BSCL/Tesoreria/JAVA/BcsTesoreriaJobs/src/cl/bcs/tesoreria/`; target relative to
`src/main/java/cl/bcs/tesoreria/jobs/`.

| Behavior | Legacy (file:lines) | Target (file:lines) | Tests |
|---|---|---|---|
| Job contract: one connection, commit / rollback / error, status, `RutaSalida`, session statements | `Plataforma/.../BcsJobManager/.../BcsJob.java:126-185` | `runtime/JobRunner.java:1-261`, `JobContext`, `TesoreriaJob`, `JobResultado`, `JobStatusListener` | `JobRunnerContractTest`, `JobRunnerRevisionArquitecturaTest` |
| Entry point (was Quartz / Job Manager) | `BcsJob.setParameters` / `run` | `web/JobCallController.java:1-116`, `web/TokenInternoFilter.java:1-60` | `JobCallControllerTest`, `TokenInternoJobCallTest` |
| PAC rendition job | `BcsJobProcesarDistribucion.java:1-169` | `job/BcsJobProcesarDistribucion.java:1-129` | `ProcesarDistribucionGoldenTest` (PD-nn) |
| Mass load: 6 guards, window, service call, AgregarLog | `cargaMasivaIngresoEgresoDAPFFMM/BcsJobCargaMasivaIngresoEgresoDAPFFMM.java:1-447` | `job/BcsJobCargaMasivaIngresoEgresoDAPFFMM.java:1-241`, `job/CargaMasivaSalida.java:1-110`, `job/ContratosPlataforma.java:1-77` | `CargaMasivaGoldenTest` (CM-nn) |
| PAC charge file job | `generarArchivoCargos/GenerarArchivoCargos.java:1-242` | `job/GenerarArchivoCargos.java:1-212` | `GenerarArchivoCargosGoldenTest` (CG-nn) |
| Charge file writer and field formatter | `generarArchivoCargos/Archivo.java:1-109`, `FormatoArchivo.java:1-106`, `FormatoArchivoDetalle.java` | `job/archivo/ArchivoCargos.java:1-125`, `job/archivo/FormatoCargos.java:1-122` (detail type: the service's) | CG-nn, CG-Fnn |
| Payment file job | `generarArchivoPago/GenerarArchivoPago.java:1-418` | `job/GenerarArchivoPago.java:1-139`, `job/archivo/GeneradorPago.java:1-101` (generic + BCP telecredito) | `GenerarArchivoPagoGoldenTest` (PG-nn, PG-Cnn) |
| Payment file writer, format lookup, formatter | `generarArchivoPago/Archivo.java:1-171`, `FormatoArchivo.java:1-450`, `FormatoArchivoDetalle.java` | `job/archivo/ArchivoPago.java:1-125`, `FormatoPago.java:1-238`, `FormateadorPago.java:1-109` | PG-nn, PG-Fnn |
| BancoEstado instruction file | `generarArchivoPago/GenerarArchivoPagos.java:1-313`, `GenerarArchivoPagoBancoEstado.java:1-296` | `job/GenerarArchivoPagos.java:1-180`, `job/GenerarArchivoPagoBancoEstado.java:1-161`, `job/archivo/BloqueBancoEstado.java:1-147` (the shared rows) | PG-Bnn, `ArchivoPagoDirectoGoldenTest` (PG-E/D) |
| CSV writer (opencsv 3.7 rules) | `com.opencsv.CSVWriter` 3.7 (`JOBMAN.gradle`) | `job/archivo/CsvLegacyWriter.java:1-103` | CM-41, CG-02, CG-35, PG-05, PG-40, `CsvLegacyWriterTest` |
| Ports and production adapters | `getCabeceras` (`CargaMasivaIngresoEgresoDAPFFMM.java:814`), `CallableStatement` of `getService` / `logica` | `job/port/*`, `adapter/CabecerasCargaJdbc.java`, `adapter/ProcedimientoArchivoJdbc.java`, `adapter/ColaboradoresJobs.java` | `CabecerasCargaJdbcTest`, `ProcedimientoArchivoJdbcTest`, `ColaboradoresJobsTest` |
| Wiring | n/a | `wiring/JobsConfiguration.java`, `wiring/ServiciosTesoreriaConfiguration.java`, `TesoreriaJobsApplication.java` | `ArranqueAplicacionTest` |
| One `transaction()` per call, no daemon (RULE-061) | `BcsJob.java:90-125` | `runtime/JobRunner.java` | `Rule061SinModoDemonioTest`, `Rule061SinSchedulerTest` |
| No debug `main()`, no credentials, no 3DES (RULE-076/077) | every `main()`, `logica(con, true)` | (absent) | `Rule076...`, `Rule077Sin3DesTest` |

## Decisions applied (user, 2026-10-16)

1. `tesoreria-service` is a normal dependency (Servicios builds a plain jar plus `*-exec.jar`). The pom pins `jakarta.xml.ws-api` 4.0.2, as the service
   does, because the Spring Boot BOM would raise the transitive one to 4.0.3 (not available offline).
2. opencsv: the legacy shipped 3.7; the 3.7 jar is not in `~/.m2`, so `CsvLegacyWriter` reproduces its behavior (quote-all or quote-special, separator
   alone is not special, null field writes nothing, no exception on a second `close()`). Parent pom untouched.
3. `ValidarMandatosPAC`, `ValidaEjecucionProcesoRecPAC`, `ConsultarEstadoModulo`, `ObtenerDiaAnterior`: `adapter/ColaboradoresJobs` (`@Primary`
   `Colaboradores`) sends them to the platform JavaCall router with their legacy class and the internal token; any other name goes to the service's
   `ColaboradoresJavaCall`. No Servicios source was edited.
4. `CabecerasCarga` = `adapter/CabecerasCargaJdbc`, the same SQL file as the service's private `getCabeceras`.
5. No defect fixed.

## Legacy defects kept (TES-J-DEF-nn, for the domain expert)

| Id | Defect | Cases |
|---|---|---|
| 01 | Mass load: the time window excludes both ends: at exactly the start or end time it answers "fuera de horario" | CM-20, CM-23 |
| 02 | `GenerarArchivoCargos` executes its stored procedure TWICE: a side effect of the procedure happens twice | every CG |
| 03 | `GenerarArchivoCargos`: a procedure without result sets makes the next line a NullPointerException; "NO EXISTE DATA" is unreachable | CG-13 |
| 04 | Cargos formatter: DEC without '.' is read as 0; `ValorDefecto` ignored for CHAR / NUM / DEC / DATE; DEC integer digits above the width lose their leading digits; null value is an NPE; `RellenarCampo` must be exactly `S` | CG-F05, F13, F19, F21, F25 |
| 05 | Pago formatter: decimal part LEFT padded (`1234.5` → `1234.05`); DEC without '.' becomes empty; every formatting error is swallowed and prints `''`; a field without definition prints `''` | PG-F21, F24, F30, F44, F46 |
| 06 | `GenerarArchivoPago`: the format constructor swallows every error; the failure surfaces later as an NPE, wrapped twice in `Exception` | PG-09, PG-24 |
| 07 | `GenerarArchivoPago` builds its format SQL with `String.format` and request parameters (SQL injection shape) | PG-24, PG-25 |
| 08 | Files are not transactional: a failed run leaves a partial or empty file | CG-23, CG-37, PG-41, PG-B12 |
| 09 | `Archivo.crear()` creates ONE directory level and swallows the failure; the job then records a file that does not exist | PG-27 |
| 10 | `TipoSaltoLinea` null makes `escribir` swallow an NPE (no line); an unknown `FormatoSalida` writes an empty file and reports success | PG-03, PG-08 |
| 11 | CSV generic output: `TipoRegistroOptimus` must be the first column (`ArrayIndexOutOfBounds` -1) | PG-41 |
| 12 | BancoEstado: `MontoRemesa` cut with `substring(0, length - 9)` (assumes scale 8); dotted RUTs fail; `CSBC` / `AVAP` unreachable; missing CONTRATO prints `null` | PG-B12..B17 |
| 13 | `GenerarArchivoPagoBancoEstado` (no job calls it): reads a column the SQL does not return, no accent stripping, full amount, typo "Nuamero de Nomina" | PG-D01..D10 |
| 14 | Distribution job: flag reset always uses `p_IdEmpresa = 1`, runs AFTER the first commit; the error counter is never used | PD-12, PD-26, PD-29 |
| 15 | Distribution job: a message with null `tipo` or `id` leaves the CSV half written | PD-14, PD-15 |
| 16 | Mass load: a missing output folder gives an empty `RutaSalida` and no error file; a null path is stored as NULL | CM-39, CM-61, CM-62 |
| 17 | Mass load: database `CURRENT_DATE` (UTC) against local time; some guards write no AgregarLog; a null `TipoArchivoCarga` returns false (ROLLBACK) reported as success | CM-27, CM-28, CM-29 |
| 18 | `GenerarArchivoPagos.call` / `GenerarArchivoPagoBancoEstado.call` deserialized `p_Archivo` (FileWriter fields) with Gson: fails on JDK > 8 | PG-E*, PG-D* |
| 19 | A `false` return is reported exactly like a commit (`BcsJob.java:151`) | CM-28, CM-29 |

Additional quirk kept (pinned by PG-B09): with more than one header row, `GenerarArchivoPagos` fails with `ArrayIndexOutOfBounds` ("Index 4
out of bounds for length 2"), because the legacy reuses one array variable between rows; `BloqueBancoEstado` keeps that variable on purpose.

## Deviations from the legacy

| Id | Deviation | Behavior change |
|---|---|---|
| DEV-TJ-01 | Entry is `POST /internal/job-call` (token-protected) instead of the Quartz Job Manager; the status listener writes the final state to BAT.Programa in mode `programa` (see "Job pattern H4") | Delivery only |
| DEV-TJ-02 | The services are called in-process (typed, same transaction) instead of `new Service().logica(con, ...)` | None (goldens) |
| DEV-TJ-03 | `GenerarArchivoPagos.call` / `...BancoEstado.call` read the three `p_Archivo` fields directly (the legacy Gson path fails on JDK > 8, TES-J-DEF-18) | They work as on Java 8; no JDK 25 legacy behavior to preserve |
| DEV-TJ-04 | A failed run releases the file descriptor WITHOUT flushing (`abandonar()`); the legacy leaked it | None on disk (same partial or empty file) |
| DEV-TJ-05 | Logging through SLF4J; the stack traces the legacy printed for swallowed errors are logged as one line | None |
| DEV-TJ-06 | The database clock is the service's `Reloj` (UTC JVM clock); the local time is that UTC time plus `PLA.SegundosZonaHoraria`; `CURRENT_DATE` stays in SQL | None in the goldens; see "Not proven" |

No deviation changes behavior.

## Structure

| Package | Content |
|---|---|
| `runtime` | `TesoreriaJob`, `JobContext`, `JobRunner` (copy of the Contabilidad runner), `JobResultado`, `JobStatusListener`, `JobNoMigradoException`, `JobEnCursoException` |
| `job` | The four jobs, `GenerarArchivoPagos`, `GenerarArchivoPagoBancoEstado`, `CargaMasivaSalida`, `ContratosPlataforma`, `ConfiguracionLegacy` |
| `job/archivo` | File writers, formatters, `BloqueBancoEstado`, `CsvLegacyWriter` |
| `job/port` | `CabecerasCarga`, `ProcedimientoArchivo`, `ServiciosExternos` |
| `adapter` | `CabecerasCargaJdbc`, `ProcedimientoArchivoJdbc`, `ColaboradoresJobs` |
| `web` | `JobCallController`, `TokenInternoFilter` |
| `wiring` | `ServiciosTesoreriaConfiguration` (scan of the service without its web layer), `JobsConfiguration` |
| `programa` | Job pattern H4: `ProgramaRepositorio(Jdbc)`, `EjecutorPrograma`, `ProgramaStatusListener`, `ProgramaModo`, exceptions; `wiring/ProgramaConfiguration` |

`TesoreriaJobsApplication` scans only `wiring`, `adapter` and `web` (`scanBasePackageClasses`), so the pilot classes are not beans.

## Not proven

* **HANA.** The schema is INFERRED (`schema-jobs.sql`); types, `TO_NVARCHAR`, sequences and the real stored procedures are not exercised.
  `ProcedimientoArchivoJdbc` is tested with mocks only; `CabecerasCargaJdbc` and the job SQL only on H2.
* **Real collaborators.** `ValidarMandatosPAC`, `ValidaEjecucionProcesoRecPAC`, `ConsultarEstadoModulo`, `ObtenerDiaAnterior`, `AgregarLog` and the
  four Servicios services are scripted in the goldens; `ColaboradoresJobs` is tested against a mock router only.
* **opencsv 3.7** is reproduced from its documented behavior, not run against the jar.
* **No Quartz / Job Manager.** Scheduling and cut-over are Phase 5; running both the legacy and this module risks double execution.
* The distribution job with a missing `PLA.RutaBaseArchivos` (the legacy failure is a HANA procedure, unknown).
* Time zones: file names use the JVM default zone (`Clock.systemDefaultZone()`), the local time uses UTC plus `PLA.SegundosZonaHoraria`; the
  goldens run in UTC. FTP transfer of the generated files is not part of the jobs.

## Obsolete pilot files (DELETED 2026-10-21 on the user's instruction)

The old pilot was removed from this module (26 files under `modernized/`; `legacy/` was not touched): main `config`, `formatter`, `infrastructure`, `model`, `port/out`, `service` and the four pilot `job/*Job` classes, and its six test classes. The pom no longer has `spring-boot-starter-batch`, `spring-batch-test`, `opencsv` nor the explicit `spring-web`; `application.properties` lost the batch auto-configuration exclude and the dead `plataforma.base-url`. `src/test/resources/application-test.properties` stays (used by `ArranqueAplicacionTest`). After the cleanup: **454 tests, 0 failures, 0 errors, 0 skipped**; dual dump identical (3637 files).

## Architecture review (step 5)

**APPLIED** (legacy behavior and TES-J-DEF-01..19 unchanged; goldens and dual dump identical):

* H1: the top-level `modernized/docker-compose.yml` block `bcs-tesoreria-jobs` now has the same environment as the code repo's compose
  (`HANA_*`, `JAVACALL_URL` default `http://bcs-plataforma-web:8117`, `INTERNAL_TOKEN` with the `:?` guard). That file is outside the code repo.
* M1: `RutaSegura`: `TipoArchivoCarga` in the error-CSV name must match `^[A-Za-z0-9_.-]{1,60}$` without `..` (null/empty stay accepted: legacy
  builds the name with them); target paths of `CargaMasivaSalida`, `ArchivoPago`, `ArchivoCargos` and `BcsJobProcesarDistribucion` are normalized
  and must stay under their base. A violation is a job error (`IllegalArgumentException`, 500 ERROR).
* M3: `/internal/job-call` binds parameter values as their literal JSON text (1.50 stays "1.50", 100000000.0 stays "100000000.0").
* M8: `JobRunner` wraps `listener.status` and `job.onError` (logged at ERROR, the `JobResultado` is kept); catch-all `@ExceptionHandler` in
  `JobCallController` returns `{"error":true,"mensaje":"INTERNAL_ERROR"}` (Spring's own 4xx keep their status).
* M2 (partial): the HTTP body of an ERROR run whose cause chain holds a `SQLException`, `DataAccessException`, `IOException` or
  `RestClientResponseException` is `INFRASTRUCTURE_ERROR: <ClassName>`. Job-level texts stay as pinned, and the listener text
  (`status(id, true, e.getMessage())`, legacy contract) is unchanged.

**OPEN HIGH (decision or deployment work needed)**

* H2: output files land in the ephemeral container layer: needs a shared volume, a healthcheck and a non-root `USER`.
* H3: no bound on a run: a hung HANA call blocks its job class with 409 until restart: needs transaction/query timeouts, a watchdog and an
  in-flight listing.
* H4: `idPrograma` and `parametros` are caller-controlled and the in-flight guard is per JVM; the legacy Job Manager read the parameters from
  `BAT.Programa`. Needs a DB-level claim and loading the parameters from `BAT.Programa`; the same design applies to BcsContabilidadJobs.

**MEDIUM / LOW not applied**

* M4: the per-class lock rejects a second `GenerarArchivoPago` with 409 (legacy claimed per program).
* M5: file-name collision at 1-second resolution overwrites the file (propose TES-J-DEF-20).
* M6: no graceful shutdown / `stop_grace_period`.
* M7: `JAVACALL_URL` validation, https, distinct outbound credential.
* M9: charset and timezone not pinned in the container.
* M10: token filter (minimum length, 401 logging, health groups).
* L1-L9: body limits; dead `plataforma.base-url`; `jakarta.xml.ws-api` pinned twice; unused batch/opencsv/spring-web deps after the pilot
  cleanup; HANA session settings on a pooled connection; wrapped interrupts; log policy; Dockerfile hygiene; no alert on `Error`.

## Job pattern H4 (reference implementation)

Steps 1-4 of the plan in `analysis/otr-sva-optimus-corredores/JOBS_H1_H4_INVENTORY.md` (section D), implemented here FIRST so that BcsAPVJobs, BcsCuentaInversionJobs and BcsContabilidadJobs copy it. Binding constraints: NO database change of any kind (only existing columns of `BAT.Programa` and existing `BAT.TipoPrograma`, `BAT.Agenda`, `BAT.LogPrograma` and the view `BAT.ConsultarParametrosProgramas`), the legacy code untouched, no automatic retry (legacy parity: `updateTryProgramm` is commented out, BcsJobManager.java:104-105). The dispatcher (the modern `BcsJobManager`) is a later step: it will call `POST /internal/job-call` with `idPrograma` and the internal token, and MUST NOT claim the program itself (the claim is made here; a program already claimed by the dispatcher would answer 409). REAL DDL CONFIRMED (HANA QA, read-only query, 2026-10-21; `analysis/otr-sva-optimus-corredores/BAT_DDL_REAL.md`): the test schema `schema-programa.sql` now matches the real column names, types and lengths of `BAT.Programa`, `TipoPrograma`, `Agenda`, `ParametrosPrograma`, `LogPrograma` (no `IdLog`) and the 10 columns of the view `ConsultarParametrosProgramas` (all CONFIRMED); `IdTipoPrograma` is NVARCHAR(40) and is a String in the code and tests. Still assumed: primary keys, NOT NULL and the column DEFAULTs (`Bloqueo 'N'`, `Intento 0`, `IdAgenda 0`), not part of the DBA result.

### Design

Package `cl.bcs.tesoreria.jobs.programa` (all of it is copied as is):

| Class | Role |
|---|---|
| `ProgramaRepositorio` (port) / `ProgramaRepositorioJdbc` | `cargarPrograma`, `parametros`, `reclamar`, `terminar`, `error`, `requiereRevision`. Each method opens ITS OWN connection (auto-commit off, explicit commit), so it is independent of the job transaction, like the legacy `updateProgramm` (TransactionManager.java:212-257). At most one extra pooled connection at a time (claim before the job transaction, final state after it). |
| `ProgramaSql` | All SQL, qualified `BSCL."BSCL.Plataforma.BD::BAT.Programa"` etc. (this connection does not get the job's `SET SCHEMA BSCL`). |
| `EjecutorPrograma` | What `/internal/job-call` runs: loads the row, checks class and parameters, takes the per-JVM guard, claims, runs. |
| `ProgramaStatusListener` | The real `JobStatusListener`: writes TER / ERR after the run; `marcarRequiereRevision(id, motivo)`. |
| `ProgramaModo`, `TextoResultado`, 4 exceptions | The switch; the 2000/1999 cut and the legacy texts; `ProgramaNoEncontradoException` (404), `ProgramaNoReclamableException` (409), `ProgramaIncoherenteException` (422), `ProgramaJdbcException` (500, never swallowed). |

Also changed: `JobRunner` (new `ejecutarReclamando(clase, id, Supplier<parametros>)`; `ejecutar` delegates to it and is unchanged for callers), `JobCallController` (second constructor taking `EjecutorPrograma`, three new handlers), `wiring/ProgramaConfiguration` (new; the `jobStatusListener` bean moved here from `JobsConfiguration`).

Order of a call in mode `programa` (each failure starts nothing): row exists (404) and its type names a class (422); `clase` sent, if any, equals `TipoPrograma.NombreClasePrograma` (422); the class is one this module runs (503); parameters read from the view (a non-empty `parametros` sent must equal them, 422; values are never echoed); per-JVM guard (409); atomic claim (409); the job transaction; listener writes the final state. `clase` is optional and `parametros` are only compared, never used.

### Switch

`tesoreria.jobs.programa.modo`: `programa` (default in `application.properties`) or `parametros-del-llamador` (set in `src/test/resources/application-test.properties`; the golden tests build `JobRunner` directly and `new JobCallController(runner)` keeps the old contract: `clase` required, caller parameters used, BAT never read or written, listener only logs). An unknown value stops the start.

### SQL (exact; HANA-only constructs of the legacy are written in an equivalent portable form)

* Row: `SELECT P.IdPrograma, idEstado, IdTipoPrograma, Intento, Bloqueo, Nodo, IdAgenda, TP.NombreClasePrograma, TP.MaximoIntentos FROM BAT.Programa P LEFT JOIN BAT.TipoPrograma TP ON ... WHERE P.IdPrograma = ?`.
* Parameters: `SELECT NombreParametro, Valor, ValorBlob FROM <view> WHERE IdPrograma = ?` (no ORDER BY; `ValorBlob` UTF-8 replaces `Valor`; last duplicate wins; null values dropped, the legacy `getParam` gave null for them too).
* Claim (one statement, after reading the row and checking `Bloqueo='N'`, PEN, `Intento < MaximoIntentos`): `UPDATE BAT.Programa SET Bloqueo='S', idEstado='ASI', Intento=<read+1>, Resultado='Programa Encolado', fechaFin=TIMESTAMP '0001-01-01 00:00:00' WHERE IdPrograma=? AND Bloqueo='N' AND idEstado='PEN' AND Intento=<read>`; 1 row = claimed, 0 rows = 409. IMPROVEMENT over the legacy (TM:68-73: no condition, no row check): two instances cannot both run the program. `Intento` is incremented at claim, as the legacy did at dequeue (TM:281-300), so `MaximoIntentos=1` is one run.
* Final state (TM:226-250): `UPDATE BAT.Programa SET idEstado=<TER|ERR>, fechaFin=CURRENT_UTCTIMESTAMP, Resultado=? WHERE IdPrograma=?` then `UPDATE BAT.Agenda SET UltimaFechaDeEjecucion=CURRENT_UTCTIMESTAMP WHERE IdAgenda IN (SELECT IdAgenda FROM BAT.Programa WHERE IdPrograma=?)` (the legacy `update ag ... from ... INNER JOIN` is HANA-only), one commit. Listener: commit AND rollback of the job (legacy defect TES-J-DEF-19, kept) -> TER, `Resultado='Finalizado correctamente.'`; exception -> ERR, `Resultado='Terminado con errores:'+message`. `Resultado` is cut to its first 1999 characters when longer than 2000 (TM:216-218; we also cut a 2000+ text that only fits after trimming, which would overflow the column).
* Requires review: `UPDATE BAT.Programa SET idEstado='ERR', Intento=max(Intento, MaximoIntentos), Bloqueo='S', fechaFin=CURRENT_UTCTIMESTAMP, Resultado='REQUIERE REVISION: '+motivo WHERE IdPrograma=?` (+ the Agenda update). The text prefix `REQUIERE REVISION: ` (`ProgramaRepositorio.PREFIJO_REVISION`) is the only marker that tells it from an ordinary exhausted failure. The legacy reader (PEN, Bloqueo 'N') and the modern retry (`Intento < MaximoIntentos`) do not take it (tested).

### Tests and schema

New tests (39): `programa/ProgramaRepositorioLecturaTest`, `ProgramaRepositorioReclamoTest` (includes a two-thread race, 25 programs), `ProgramaEstadoFinalTest`, `ProgramaJobCallTest` (HTTP: parameters from the row, 422 class and parameters, 404, 409, 503, TER/ERR, two simultaneous calls), `ProgramaModoTest`, `wiring/ProgramaArranqueTest`; fixture `support/ProgramaH2`. The H2 schema for them is the NEW file `src/test/resources/tesoreria-jobs/schema-programa.sql` (extra `BAT.Programa` columns by `ALTER TABLE`, `TipoPrograma`, `Agenda`, `ParametrosPrograma` with `ValorBlob`, the view, `LogPrograma`), loaded after `schema-jobs.sql` and only by these tests. DEVIATION from "extend schema-jobs.sql": every golden case dumps `BAT.Programa` with `SELECT *`, so new columns there would change all 334 dumps and the legacy oracle output; `schema-jobs.sql` and the goldens are untouched. The schema is an assumption (columns from the legacy Java and `AgregarPrograma`; `Bloqueo DEFAULT 'N'` assumed) until seen in HANA.

Scheduler P0 rules (2026-10-23, added after the verify pack tied this module to RULE-055..068 through the legacy files the H4 notes cite): RULE-055 (PEN → ASI → TER/ERR) is pinned by H4-R01, H4-F01, H4-F02; RULE-056 (Bloqueo S committed before any job logic) by H4-R01, H4-R04; RULE-057 (lock race) by H4-R05, H4-J10, H4-J11 (fixed: conditional claim instead of the removed FOR UPDATE). New `programa/ProgramaReglasP0Test` (5 tests): RULE-057/062 the database claim refuses a row changed after the read with the same Intento (a canary removing the `Bloqueo`/`idEstado` conditions of `RECLAMAR` went unnoticed before this test, because `reclamable()` covers the case outside that window), RULE-058/059 no automatic retry (legacy parity; a failed program stays ERR and no statement puts a program back to PEN), RULE-062 a program terminated by a daemon restart is never run (409), RULE-066 no node reboot in this module (dispatcher's concern: no `System.exit`, `halt` or `BAT.Nodos`), RULE-068 no SQL built from input (fixed texts with binds; an injection-shaped parameter value is plain data).

### Limitations (cannot be guaranteed without database changes; inventory C.2 and C.4)

1. A process that dies after the claim leaves the program `ASI` / `Bloqueo 'S'`: no heartbeat column per program, no automatic recovery (a policy on `fechaEjecucion` would be needed). An operator reprocesses it by hand (`ReprocesarPrograma`).
2. A failure while writing the final state (database down) is logged and the run result kept (review M8); the program stays `ASI` / 'S'.
3. No unique constraint in the database over (program, attempt): the guarantee is the conditional UPDATE plus the per-JVM guard; the claim stores no owner (no column; encoding it in `Resultado` was not done).
4. `Nodo` of the modern engine cannot be made exclusive without changing `TipoPrograma.Nodo` data (C.2): the cut-over per `IdTipoPrograma` stays an operational step (the legacy Job Manager must not process the migrated types).
5. 'Requires review' is indistinguishable from an exhausted failure except by the text prefix; `ReprocesarPrograma` and `ActualizarEstadoPrograma(p_Result=2)` can relaunch it outside any review (operational control).
6. A collaborator already committed outside the job transaction cannot be undone and the router sends no idempotency header (steps 5 and 6 of the plan, not done here).
7. `CURRENT_UTCTIMESTAMP` is the database clock; the H2 tests fix it. The claim condition `Intento < MaximoIntentos` follows the legacy reader: a type with `MaximoIntentos=0` can never be claimed (409).
8. Not proven on HANA: types, the portable `UPDATE ... IN (SELECT ...)` and the `TIMESTAMP '0001-01-01 00:00:00'` literal, privileges of the job user on `BAT.*` and the view (DBA to confirm).

### Files to copy to BcsAPVJobs, BcsCuentaInversionJobs and BcsContabilidadJobs

Rename the package (`cl.bcs.<modulo>.jobs.programa`) and copy: the 12 classes of `programa/`; in `runtime/JobRunner.java` the `ejecutarReclamando` method and `ejecutar` delegating to it; in `web/JobCallController.java` the second constructor, `exigeClase()` check, the three handlers and the imports; `wiring/ProgramaConfiguration.java` (and remove the logging `jobStatusListener` from `JobsConfiguration`); the two property lines (`application.properties` = `programa`, test profile = `parametros-del-llamador`); `src/test/resources/<modulo>/schema-programa.sql` and the tests with `support/ProgramaH2` adapted to that module's H2 setup (APV and CuentaInversion already have a partial `BAT.Programa`; Contabilidad builds it in `support/H2Jobs.java:40`; each golden harness must keep its own dumps unchanged). Contabilidad also needs `TokenInternoFilter` and `ParametrosTexto` first (inventory B.5). Schema names (`BSCL."BSCL.Plataforma.BD::BAT.Programa"`) and `SELECT *` dumps must be checked per module.

## Step 5: collaborators in process (JOBS_PATTERN.md H-1; JOBS_H1_H4_INVENTORY.md section D step 5)

Reference implementation for the other Jobs modules. Switch: `tesoreria.jobs.colaboradores-en-proceso` (`application.properties` = `true`; `false` = exactly the pre-step-5 wiring, pinned by `ArranqueAplicacionTest`).

### What runs in process (group M, verified in the code) and what still goes over HTTP

Group M collaborators this module reached over HTTP, and their in-process entry point (all inside the transaction of the job):

| Name | Caller | Owner entry point |
|---|---|---|
| `ValidarMandatosPAC` | `BcsJobProcesarDistribucion` | APV: `ApvJavaCallPipelineEnProceso`, typed operation `cl.bcs.APV.ValidarMandatosPAC.logica` (updates `LogMandatoPAC` in the job transaction like the legacy `logica(con, ...)`; limitation 3) |
| `CambiarEstadoContratosSAAPV` | Tesoreria service `AnulacionMandatoPAC` (shared `Colaboradores` port) | APV: the same pipeline, real use case `CambiarEstadoContratos` |
| `CrearCargoAbono` | `MovimientosDistribucion` (job ProcesarDistribucion) | CuentaInversion: `service.JavaCallDispatcher` -> `CrearCargoAbonoService` |
| `AnularCargoAbono` | Tesoreria service `AnularNominasTransferencias` | CuentaInversion: the dispatcher -> `AnularCargoAbonoService` |

The Tesoreria services (`CrearIngresoEgreso`, `LiquidarComprobante`, ...) were already in process (`ColaboradoresJavaCall` + `JavaCallDispatcher`) and are untouched. Everything else keeps going through `ColaboradoresJobs` -> `{JAVACALL_URL}/internal/java-call` (that endpoint is step 5b): ValidaEjecucionProcesoRecPAC, ConsultarEstadoModulo, ObtenerDiaAnterior, AgregarLog, CrearOrdenes, CrearSolicitudAporteRescateNormal, the TL calculators, `ConsultarSaldoCuentaInversion` (its bean is deliberately not scanned here) and `Cloper`.

### Design

* `adapter/ColaboradoresEnProceso` (`@Primary`, built in `wiring/ColaboradoresJobsConfiguration`): answers the four names through `DespachadorEnProceso`, delegates everything else to `ColaboradoresJobs` (now created by that configuration, no longer a `@Component`: it is `@Primary` when the property is false and the plain delegate when it is true).
* `adapter/EntradaEnProceso` = the in-process entry point of an owner library (`propietario`, `maneja`, `despachar`); `DespachadorEnProceso` finds the owner of a legacy class. The three owners are wired in `wiring/EnProcesoConfiguration`: Tesoreria's and CuentaInversion's `JavaCallDispatcher`, and the APV pipeline.
* Pom: `bcs-apv-api`, `bcs-apv-javacall-adapter`, `apv-service`, `bcs-cuenta-inversion-servicios` (plain jars, already in `~/.m2`; no Maven cycle, none of the three libraries depends on another: checked on the class files by `ningunaLibreriaDependeDeOtraEnTiempoDeCompilacion`). Their `application.yml` would otherwise leak into this application (Spring takes the first `classpath:/application.yml`: port, HANA driver class): `src/main/resources/application.yml` is an intentionally empty file that shadows them. Do not delete it.
* CuentaInversion: a limited scan (`ServiciosCuentaInversionEnProcesoConfiguration`, 7 classes, beans named with the fully qualified class name because both libraries have a `JavaCallDispatcher` and a `DiccionarioJdbc`). Its two REST ports are replaced: `ConfiguracionCuentaInversionJdbc` reads the parameters with this module's JDBC `ConfiguracionSistema` (inside the job transaction, as the legacy did with `con`; module from the key prefix `PLA.` / `CUENTAINVERSION.`; no new required URL) and `ColaboradoresCuentaInversionEnProceso` (owners in process, anything else to the router with bounded waits).
* APV: `ApvCasosDeUso.crear` (the composition root apv-service documents for in-process callers), built on first use, plain instances without transaction proxy on this application's `NamedParameterJdbcTemplate`, so they use the connection the job transaction bound. Its `Colaboradores` is `ColaboradoresApvEnProceso`; the APV library adapter (`ColaboradoresJavaCall`, router with bounded waits, `StoredProcedure`) serves what no owner serves.
* Cycle Tesoreria <-> APV (inventory C.6.5) resolved in this application, never in the libraries: every library keeps its own `Colaboradores` port and all of them get the same `DespachadorEnProceso`, so APV -> Tesoreria (CrearIngresoEgreso, the three `*MandatoPAC`, ...) and Tesoreria -> APV are two entry points of one JVM and one transaction. The entry points are used lazily by name (dispatchers look services up at call time, the APV graph is built on first use): no Spring cycle (Boot forbids circular references; the context starts).

### Tests (new: 22; baseline 493 -> 515, 0 skipped)

* `ColaboradoresEnProcesoTest` (10, no Spring): each group M name resolves in process with no HTTP; the four names are exactly the ones that were HTTP and their classes equal the ones the HTTP adapters sent; non-M names (and Tesoreria's own) still go to the HTTP port; owner not serving -> HTTP; input passed unaltered; runtime exception propagates unchanged, checked one wrapped with cause; APV and CuentaInversion ports route to the owner or fail closed.
* `ColaboradoresEnProcesoWiringTest` (8, real context on H2): primary and delegate, only the 7 CuentaInversion beans scanned, owners per class, `ValidarMandatosPAC` null in process vs the request the HTTP adapter sends, `CambiarEstadoContratosSAAPV` in process equals the recorded legacy JSON and tables of golden APV-A-311 (copied byte for byte under `src/test/resources/tesoreria-jobs/enproceso/`, DDL verbatim from the APV H2 schema) and its `AgregarLog` goes out as the only HTTP request with the golden's input, a non-M class still goes over HTTP, `CrearCargoAbono` reaches the real `CrearCargoAbonoService` without HTTP, no library depends on another at class-file level.
* `ColaboradoresEnProcesoTransaccionTest` (4, real `JobRunner` REQUIRES_NEW + real writing collaborator): job commits -> the collaborator's write is committed; job returns false -> undone; job throws after the call -> undone; intermediate `ctx.commit()` keeps the first write and a later failure undoes only the second.
* `ArranqueAplicacionTest` now sets the property to `false` (its assertions are unchanged); `ProgramaArranqueTest` runs the default (true) context. Dual dump of the goldens identical (`diff -rq` legacy and new against `equivalence/BcsTesoreriaJobs/out`).

### Limitations (read before real traffic)

1. HANA is not exercised: all proofs are on H2. The `BAT`/APV/CuentaInversion SQL runs on H2 here; the same-transaction behaviour on HANA (session statements `SET TRANSACTION AUTOCOMMIT DDL OFF`, procedures called by `CrearCargoAbono` when `PLA.UsaCargoAbonoJava = N`) is not proven.
2. What the transaction test proves: a collaborator write made through the in-process path shares the connection of the job (rolled back with it, committed with it, split by an intermediate commit). What it does not prove: CuentaInversion's `CrearCargoAbono` writing (its golden harness needs ~50 HANA tables and was not copied: only that the real service is reached and fails in the expected way) and Tesoreria's services under `JobRunner` (same Spring mechanism, `@Transactional` REQUIRED, not re-tested here).
3. `ValidarMandatosPAC` (legacy-fidelity gap, CLOSED in process; still open over HTTP). The legacy job called the typed `ValidarMandatosPAC.logica(conn, salida, idEmpresa, idUsuario)` (BcsJobProcesarDistribucion.java:81-82), which UPDATEs the existing `LogMandatoPAC` rows (WHERE `IdMandatoPAC` AND `FechaSistema` = `PLA.FechaSistema`; PRO / 'Procesado.' for the requests to process that were not rejected, PEN / `EstadoRendicion` for the rejected ones; no row, no write).
   * With `tesoreria.jobs.colaboradores-en-proceso=true` the job's call (name `ValidarMandatosPAC`, `ColaboradoresEnProceso.GRUPO_M`) is now the typed in-process operation `cl.bcs.APV.ValidarMandatosPAC.logica` (`ValidarMandatosPACLogicaMapeo.OPERACION`, APV-C-480..509 of BcsAPVServicios): `EnProcesoConfiguration.entradaApv` claims it with `ApvJavaCallPipelineEnProceso.esOperacionTipada` (the registry does not know it) and the pipeline runs the real use case on this application's `NamedParameterJdbcTemplate`, i.e. on the connection of the job transaction. The JavaCall class `cl.bcs.APV.ValidarMandatosPAC` (call(), null) is still served in process by the registry for the other callers (`ColaboradoresApvEnProceso`).
   * Argument mapping (what the legacy passes -> what the job sends, unchanged and identical to what the 334 goldens record): `salida` (typed `SalidaRecaudacionTraspaso` returned by `ProcesarDistribucion.logica`) -> JSON `salida` (the complete output of `ProcesarDistribucionService`, the object the job also deserialises for the CSV); `idEmpresa` / `idUsuario` (the job parameters) -> `idEmpresa` / `idUsuario`. Only `P_OUT_SOLICITUDES_A_PROCESAR` and `P_OUT_SOLICITUDES_NO_PROCESADAS` (`IdMandatoPAC`, `IdentServicio`, `EstadoRendicion`) are read.
   * When and with what result/exceptions (as the legacy): it is called after the distribution and before the CSV, once, inside the job transaction; its result (void: `null`) is ignored; an exception is NOT swallowed: it reaches the `catch` of the job (`ctx.rollback()`, rethrow), so the run ends ERROR with the original exception and the write is undone. If a future change swallowed it and committed, the shared transaction (marked rollback-only by the `@Transactional` proxy case) would end in `UnexpectedRollbackException`.
   * Proven (`ValidarMandatosPacEnProcesoTest`, H2, tables of the goldens APV-C-480/481/484/487/488/494 copied byte for byte under `src/test/resources/tesoreria-jobs/enproceso/apv-C-nnn`, DDL verbatim): rows equal the golden dump in both JSON shapes (golden `entrada` and job `salida`); legacy NPE (APV-C-494); with the real job + real `JobRunner`: committed with the job, undone when the job fails afterwards (CSV abort, TES-J-DEF-15) and when the collaborator itself throws (APV-C-500 style, original `NullPointerException` reaches the job, flag reset not sent); dispatcher accepts `cl.bcs.APV.ValidarMandatosPAC.logica` and rejects other names. The golden harness (`ColaboradoresDeCaso`) still scripts this collaborator: goldens and dual dump unchanged.
   * Limitations: (a) with `colaboradores-en-proceso=false` the call still goes over HTTP to the router, whose `ValidarMandatosPAC` answers the JavaCall `call()` = `null`: NO `LogMandatoPAC` row is written on that path (it cannot reproduce the legacy write; keep the property `true` in production). (b) The job sends the JSON the service answered; if the service omitted a list that the legacy typed object would hold as `null`, the in-process operation reads it as empty (legacy NPE not reproduced for that omission; an explicit JSON null does reproduce it). (c) Not proven on HANA (UPDATE/batch statement and `PLA.FechaSistema` read are H2 here).
4. The CuentaInversion JSON equivalence (in-process answer == HTTP answer) relies on the same `execute(JsonObject)` entry point that CuentaInversion Servicios' own dispatcher serves behind its `/internal/java-call` and on that module's goldens; it is not re-run here.
5. `ConfiguracionCuentaInversionJdbc` replaces the platform configuration REST service by direct reads of `PLA.Configuracion` / `CTA.ConfiguracionCuentaInversion` (assumption: the service reads the same tables; not proven against HANA).
6. APV collaborators of non-M owners (Plataforma, Orden, ...) inside the APV use cases still go over HTTP, in the job transaction's thread (bounded waits); they commit on their own (steps 6/5b). `ConsultarSaldoCuentaInversion` called from APV also goes over HTTP.
7. Limitation 6 of the H4 list (a collaborator already committed outside the job transaction) remains true for the TW/TL collaborators; it is closed only for the four group M names above.

## Step 6: fail-closed guard for TomEE write collaborators (JOBS_PATTERN.md H-1 point 2; JOBS_H1_H4_INVENTORY.md section D step 6)

Reference implementation for the other Jobs modules. Binding: NO database change (only the existing `BAT.LogPrograma` and `BAT.Programa`), legacy untouched, the router carries NO idempotency header, NO automatic retry. Switch: `tesoreria.jobs.guarda-tomee` (`application.properties` = `true`, also when the property is missing; `false` in `application-test.properties`, so every golden, the 525 earlier tests and the dual dump run on exactly the pre-step-6 wiring, pinned by `GuardaCableadoApagadoTest`).

### What it guarantees and what it does NOT

Guarantees (for a run that has an `idPrograma`, mode `programa`):

1. A guarded TomEE write is announced BEFORE it is made, in an independent connection committed at once; if the announcement cannot be written the call is not made.
2. After a crash, a lost connection or a restart, a program whose earlier attempt left a call without outcome (INTENTO without HECHO/FALLO) or a committed one (HECHO) is NOT run again by this engine: it goes to "requires review". No silent duplicate order / request / event after a crash or a rerun.
3. A run that ends in rollback or error after TomEE calls were answered or failed is left in "requires review" (not as a plain TER/ERR), so TomEE and the job output are compared by a person.
4. Marks survive the rollback of the job (independent connection); the job's own writes roll back as before.

Limits (document, do not hide):

1. The guard DETECTS and STOPS; it never undoes a TomEE write that already committed (no transaction is shared with TomEE). Its value is that nothing is repeated silently.
2. The INTENTO mark and the HTTP call are not atomic: a crash between them looks like an unknown outcome and goes to review although TomEE never saw the call (false positive on purpose: fail closed). The reverse (call made, no mark) cannot happen: no mark, no call.
3. A call that answered 2xx whose HECHO could not be written leaves the INTENTO open: the job is stopped (exception) and the next attempt is refused.
4. FALLO is NOT proof that nothing was written (a read timeout or a 5xx can follow a commit). A FALLO alone does not refuse a later attempt (only HECHO and an open INTENTO do), but a run that does not commit after a FALLO is left in review (point 3 above). With `MaximoIntentos` exhausted (review sets Intento = Max) the engine never claims it again anyway.
5. Only the guarded names (table below) and only inside a run with an `idPrograma`: caller mode (`parametros-del-llamador`) and `guarda-tomee=false` are pass-through. The run scope is a thread-local of the thread that runs the job: the module starts no other thread (checked); a port to a job that fans out to a pool must pass the scope along or it would silently be unguarded.
6. No unique constraint exists (no DDL): the marks are plain rows of `LogPrograma`; the claim of H4 (conditional UPDATE) is what keeps two instances out of one program. `LogPrograma` grows by 2 rows per guarded call (a marked run of ProcesarDistribucion: one pair per `CrearOrdenes` / `CrearSolicitudAporteRescateNormal` and one for `ValidaEjecucionProcesoRecPAC`); the read is `WHERE IdPrograma = ? AND Mensaje LIKE 'H4G|%'` (unindexed here; DBA to confirm cost on HANA, there is no purge: nothing is deleted).
7. HANA is not proven (H2 only): `CURRENT_UTCTIMESTAMP` into `FechaLog`, the LIKE, the independent connection under the HANA pool.
8. The final "review" over TER/ERR is a second write after the listener's: a crash between the two leaves TER/ERR; the marks still refuse a rerun.

### Design (package `cl.bcs.tesoreria.jobs.guarda`, all of it is copied as is)

| Class | Role |
|---|---|
| `MarcaGuarda` | The mark text `H4G|<INTENTO\|HECHO\|FALLO>|<idPrograma>|<Intento>|<clase>|<n>`: format, parse (a text that does not parse is "unreadable"), validation (name `[A-Za-z0-9_.]{1,100}`, no separator; far below the 5000 characters of `Mensaje`). `n` = 1-based ordinal of the guarded call in the run. |
| `EstadoMarcas` | Pairs the marks of one program over all its attempts: `sinCierre` (INTENTO without close = unknown outcome), `hechas` (committed), `ilegibles`; `limpio()`. |
| `MarcasRepositorio` / `MarcasRepositorioJdbc` | `registrar` (INSERT into `BAT.LogPrograma`: FechaLog = `CURRENT_UTCTIMESTAMP`, IdTipoPrograma of the program, IdPrograma, Mensaje; own connection, commit) and `estado`. Failures are `GuardaTomeeException` (fail closed). |
| `ClasificacionColaborador` | Name -> category; `guardada()` is true for `NO_REPETIBLE` and `MIGRADO_ESCRITOR`. |
| `ColaboradorTomEEGuardado` | The decorator of the `Colaboradores` port (see order below). |
| `ContextoGuarda`, `CorridaGuardada` | Thread-local scope of the run: id, Intento (of the claim), type, call ordinal, answered and failed calls. |
| `GuardaTomee` | Run level: `motivoPrevio` (check before the run), `abrir` / `cerrar` (scope), `motivoPosterior` (verdict after the run), `envolver(http)`. |
| `programa/ProgramaRequiereRevisionException` + handler in `JobCallController` | The refusal: HTTP 423. |
| `wiring/GuardaTomeeConfiguration` | The three beans, only when the property is not `false`. |

Decorator order per guarded call: (a) INTENTO mark committed, (b) the HTTP call once (no retry), (c) HECHO on success; on ANY exception or error (non-2xx included: `RestClient` throws) FALLO and the SAME exception is rethrown (the job rolls back its own work). `EjecutorPrograma` (mode `programa` with a guard): after the claim and before the job transaction it reads the marks of the same `idPrograma`; if there is a HECHO, an INTENTO without close or an unreadable mark (or they cannot be read) it calls `requiereRevision` with a precise `Resultado`, logs ERROR with the ids and throws; otherwise it opens the scope with the Intento of the claim and closes it in a `finally`. After the run, a ROLLBACK or ERROR with answered or failed guarded calls is set to review (over the listener's TER/ERR).

Response of the refusal: HTTP 423 (Locked), body `{"error":true,"clase":null,"mensaje":"Program #n requires review and was not started: ..."}` (ids and collaborator names, never parameters). DECISION: not the generic 409, which means "somebody else has it, ask later" (and the future dispatcher maps it to "do not mark ERR"); a 423 tells it the state was already written, there is nothing to retry and a person decides. The dispatcher must map 423 like 409 (no ERR, no retry) and alert.

State written: `idEstado='ERR'`, `Intento = MaximoIntentos`, `Bloqueo='S'`, `Resultado = 'REQUIERE REVISION: ' + reason` (same method as the H4 listener; neither the legacy reader nor `SQL_RETRY_FAILED` takes it).

### Classification of the collaborators this module reaches over the collaborator port

Inventory A.2 (group TW = 18 classes) verified against what the code sends (`ServiciosExternos`, `ColaboradoresJavaCall.nombresConocidos()`, `ColaboradoresEnProceso.GRUPO_M`; test `ClasificacionColaboradorTest` fails if a name is unclassified).

| Category | Names | Guarded | Why |
|---|---|---|---|
| NO_REPETIBLE (17, TW) | `CrearOrdenes`, `CrearSolicitudAporteRescateNormal` (both reached by ProcesarDistribucion through `MovimientosDistribucion`), `ValidaEjecucionProcesoRecPAC` (called by ProcesarDistribucion; it only sets a flag, repeatable by value, but it is a TomEE write whose state this module cannot read back), `AgregarPrograma`, `InscribirProcesoBatch`, `AgregarEvento`, `CrearEventoAutorizacionMasivo`, `ModificarEstadoEventoAutorizacionMasivo`, `AnulacionContratoCSC`, `RevocacionContratoCSC`, `ModificarPortafolio`, `CrearCliente`, `CrearPersona`, `CrearTelefono`, `CrearDireccionElectronica`, `CrearPersonaCuentaBancaria`, `CrearMovimientoComprobante` | yes (mandatory) | a repeat duplicates or re-applies a business write |
| APPEND_ONLY (1, TW) | `AgregarLog` (called by the DAPFFMM job in `CargaMasivaSalida` and, inside the in-process Tesoreria services, per movement) | NO | DECISION: it appends audit lines and changes no business state, a repeat is cosmetic; a mark pair per log line would flood `LogPrograma` (the services call it per movement) and turn a harmless duplicate into a review. Its failure still fails the job as before. The audit lines it committed stay after a job rollback (legacy difference already known). |
| MIGRADO_ESCRITOR (4, M) | `ValidarMandatosPAC`, `CambiarEstadoContratosSAAPV`, `CrearCargoAbono`, `AnularCargoAbono` | yes, only if a call reaches the HTTP port | with `colaboradores-en-proceso=true` they run in the job transaction and never reach the guard; with `false`, or when the owner does not serve them, they commit apart. (Small extension of the plan, which listed TW only.) |
| MIGRADO_PROPIO (5) | `CrearIngresoEgreso`, `LiquidarComprobante`, `CrearComprobante`, `ActualizarSaldosCuentaBanco`, `AnularNominasTransferencias` | no | Tesoreria's own services, always in process in the job transaction |
| SOLO_LECTURA (TL) | `ConsultarEstadoModulo`, `ObtenerDiaAnterior`, `RevisarRequisitosBasicos`, `ConsultarFlujoCaja`, `SaldoFlujoCaja`, `CalcularParidad`, `ConsultarInstrumentoPrecioBolsa`, `CalcularMontoNumeroCuota`, `CalcularMontoYCobrosOrden`, `ObtenerPortafoliosEjecutivo(.restringeCartera)`, others for the ports | no | a repeat is harmless (the tables of paso they write are scratch) |
| ESPECIAL | `StoredProcedure` (JDBC in the job connection), `Cloper` (not migrated, 503) | no | joins the job transaction / never writes |

What each job of the module can reach over HTTP and is guarded: ProcesarDistribucion: `ValidaEjecucionProcesoRecPAC`, `CrearOrdenes`, `CrearSolicitudAporteRescateNormal`; DAPFFMM: none (only `AgregarLog`, append-only, and reads); GenerarArchivoCargos / GenerarArchivoPago / PagoBancoEstado: none.

### Wiring (the guard wraps the HTTP side only)

* step 5 off, guard on: `ColaboradorTomEEGuardado` is the `@Primary` port around the non-primary `ColaboradoresJobs` (`colaboradoresJobsHttp`).
* step 5 on, guard on: `ColaboradoresEnProceso` stays primary; its HTTP delegate is the guard around `ColaboradoresJobs`. In-process collaborators never touch the guard.
* guard off (or property `false`): none of the beans exists, `ColaboradoresJobs` is exactly as before (`@Primary` when step 5 is off). `EjecutorPrograma` gets `null`.

### How an operator releases a program in review

The engine never re-runs it and nobody deletes anything (no marks, no rows). A DBA or operator: (1) reads the program row (`idEstado='ERR'`, `Bloqueo='S'`, `Resultado` starting with `REQUIERE REVISION:` and listing `clase#n(attempt i)`) and its marks `SELECT "FechaLog","Mensaje" FROM BAT.LogPrograma WHERE "IdPrograma" = ? AND "Mensaje" LIKE 'H4G|%' ORDER BY "FechaLog"`; (2) verifies in TomEE (orders / requests / events of that run) what was really written; (3) decides: if the business effect is complete, leave the row as is; if something is missing, complete it by hand or use `ReprocesarPrograma`, which creates a NEW program id (new `IdPrograma`, so its marks are separate and it runs normally). Resetting the same row (`ActualizarEstadoPrograma` p_Result=2 to PEN, or any UPDATE) does NOT bypass the guard: the next claim finds the marks and returns it to review.

### Tests (new: 33, baseline 525 -> 558, 0 skipped)

`guarda/MarcaGuardaTest` (8: format, parse, pairing, limits, longest mark), `ClasificacionColaboradorTest` (5: every name classified, the 17 + AgregarLog = 18 TW names, categories), `ColaboradorTomEEGuardadoTest` (8: INTENTO committed and visible from another connection when the call goes out, ordinal, FALLO + same exception, an `Error`, non-guarded names leave no marks, no scope = pass-through, no mark = no call, no HECHO = job stopped, two programs on two threads), `GuardaProgramaJobCallTest` (9, MockMvc + real runner / executor / repositories on H2: success, HTTP failure with job rollback and surviving marks, rollback after HECHO, rollback with no guarded call stays TER, crash simulation refused 423 with review state, HECHO refuses the rerun, other programs' marks ignored, Intento of the marks = Intento of the claim, caller mode pass-through), `wiring/GuardaCableado{Http,EnProceso,Apagado}Test` (3). Schema: the existing `schema-programa.sql` already holds `LogPrograma` with the real DDL (no change; the oracle schemas are untouched). Dual dump of the goldens identical.

### Files to copy to BcsAPVJobs, BcsCuentaInversionJobs and BcsContabilidadJobs

Rename the package: the 10 classes of `guarda/` (`ClasificacionColaborador` adjusted to the names that module's port sends; the TW table above is shared), `programa/ProgramaRequiereRevisionException`; in `EjecutorPrograma` the 4-argument constructor, `abrirGuarda`, `revisarDespues` and the try/finally; the 423 handler in `JobCallController`; `wiring/GuardaTomeeConfiguration` and the three `ColaboradoresJobsConfiguration` conditions (adapt to that module's port wiring: the guard must wrap the HTTP side only); the property (main `true`, test profile `false`); the tests with their `GuardaBase`. The Contabilidad jobs have no TW collaborator over HTTP (SAP B1 and SFTP are not guardable this way): the guard is not needed there.

## Step 6b: fail closed when a guarded call has no scope on its thread + build-time scan for parallelism (hardening of step 6)

Binding as in step 6: NO database change (no marks are written by this hardening, only the existing `BAT.LogPrograma` marks of step 6 remain), H2 only, legacy untouched.

### Problem

The run scope of step 6 (`ContextoGuarda`) is a ThreadLocal of the thread that runs the job. A guarded collaborator (NO_REPETIBLE or MIGRADO_ESCRITOR that reaches HTTP) called from ANOTHER thread (a `parallelStream` or a pool inside a library such as apv-service, a `CompletableFuture`) found no scope and the decorator passed it through UNMARKED: a TomEE write with no INTENTO/HECHO mark, invisible to the re-run refusal. Step 6 limit 5 said "the module starts no other thread (checked)"; that was true of this module only, not of the libraries the jobs call.

### Runtime change (fail closed)

`ContextoGuarda` now distinguishes three states of a thread, and knows the mode of the module (`new ContextoGuarda(exigePrograma)`; the bean is built from `ProgramaModo`: `true` only in mode `programa`, i.e. `tesoreria.jobs.programa.modo` = `programa`, the default):

| Thread state | Mode `programa` | Caller mode (`parametros-del-llamador`) |
|---|---|---|
| a `CorridaGuardada` is open (`GuardaTomee.abrir`, after the claim) | guarded names: marks + call (unchanged) | n/a (nothing is claimed) |
| explicit "no program" scope (`GuardaTomee.abrirSinPrograma`, opened by `EjecutorPrograma` for the whole run in caller mode) | pass-through, no marks | pass-through, no marks |
| NO scope at all | guarded name: REFUSED (see below); any other name: pass-through | pass-through (nothing to guard) |

Refusal: before any HTTP call and without writing any mark (there is no `idPrograma` to key it), `GuardaTomeeException("llamada protegida sin contexto de programa (hilo distinto?): <name> ...")` is thrown and logged at ERROR with the thread name. The job sees an exception and rolls back its own work as for any failed collaborator call. Not affected: names that are not guarded (reads, `AgregarLog`, in-process services, unknown names), threads that never call a guarded name (health/actuator, tests), the property `tesoreria.jobs.guarda-tomee=false` (no beans: wiring identical to step 5), and the `ContextoGuarda()` default constructor is the strict one (`true`), so a hand-built context cannot silently be lax.

The scope is NOT propagated to other threads on purpose: the marks of one run are numbered in call order (`n`) and a guarded write racing on another thread would make the INTENTO/HECHO order unreadable. A job that really needs a guarded call from a pool must be redesigned to call it on the job thread.

Tests: `GuardaSinContextoTest` (G6b-01..08: second thread refused with no HTTP call and no marks; `parallelStream` inside a ForkJoinPool; commonPool and `CompletableFuture`; no run at all; caller mode passes on any thread; explicit "no program" scope; unguarded names from other threads; `EjecutorPrograma` opens and closes the caller-mode scope). Test changed: `ColaboradorTomEEGuardadoTest` G6-D05 pinned "no scope = pass-through"; that is now only true for caller mode, so it builds the guard over `new ContextoGuarda(false)`; the no-scope-in-mode-programa case is G6b-04.

### Build-time scan

`ParalelismoFuentesTest` (G6b-S01..S03) scans `src/main/java` of this module and of the sibling libraries its jobs use (this module, BcsUtils, BcsTesoreriaServicios, BcsAPVApi, BcsApvJavaCallAdapter, BcsAPVServicios, BcsCuentaInversionServicios; located relative to the module, existence asserted) for `parallelStream(`, `.parallel(`, ForkJoin, CompletableFuture, ExecutorService/Executors, `@Async`, `new Thread`, `sendAsync`, `StreamUtils.parallel*`. Every hit must be in an explicit allowlist (file, exact number of matching lines, reason); a new hit, one more line in an allowlisted file, a hit on the same line as a collaborator call, or a stale entry fails the build.

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

## Follow-ups

* `/code-modernization:modernize-verify otr-sva-optimus-corredores BcsTesoreriaJobs` for the independent check.
* SME decision on defects 02, 03, 05, 07, 08 (and the rest) before any is fixed.
* `BcsTesoreriaServicios/Dockerfile` copies `target/*.jar`; with the plain jar next to `*-exec.jar` it must copy `*-exec.jar` (as Contabilidad does).
* `docker-compose.yml` (`bcs-tesoreria-jobs`): fixed in step 5 (H1).
* `/internal/job-call` is token-protected, but one instance per job class (the in-flight guard is per JVM); callers must not retry on timeout.
