# BcsCuentaInversionJobs: transformation notes

| Item | Detail |
|---|---|
| Legacy | `legacy/otr-sva-optimus-corredores/BSCL/CuentaInversion/JAVA/BcsCuentaInversionJobs` (23 classes, 3,134 lines) and the job framework `BSCL/Plataforma/JAVA/BcsJobManager/src/cl/bcs/scheduler/BcsJob.java` |
| Target | Spring Boot 4.1.1 / Java 25, artifact `cl.bcs:bcs-cuenta-inversion-jobs` |
| Definition of done | The Step 1 characterization tests (commit `619a4653`, 413 goldens recorded by EXECUTING the unmodified legacy under the real `BcsJob.run()`) |
| Architecture | Decision 2026-10-15/21: in-process jobs on the PROVEN `bcs-cuenta-inversion-servicios` as a library (direct `execute(JsonObject)` calls, no HTTP between job and service), one transaction per run with the BcsJob contract (`JobRunner`, copied from BcsTesoreriaJobs with its review fixes), `POST /internal/job-call` with the internal token, no `@Scheduled`, no daemon mode (RULE-061), no debug `main()` / credentials (RULE-076), no 3DES (RULE-077). `ActualizarSaldoCargoAbono` runs the real recalculation (`execute`), not the legacy `call()` stub (Servicios CTA-A-08) |
| Behavior policy | Bug for bug: NONE of the 13 legacy defects (CIJ-DEF-01..13) is fixed (listed below for the domain expert) |

## Proof metrics (Step 3)

| Metric | Value |
|---|---|
| Command | `mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :bcs-cuenta-inversion-jobs clean test -Dequivalence.out=<dir>` (JDK 25) |
| Tests | **516 run, 0 failures, 0 errors, 0 skipped** (413 golden cases in 6 golden classes; the 27 pilot tests were deleted 2026-10-21 (543 before); runner, RULE-061/076/077, web, harness, `ArranqueAplicacionTest` 4) |
| Legacy vs. new (`compare.py`) | **4,565/4,565 same** (differs 0, missing 0, self-check passed): every comparison point of every golden, in `analysis/otr-sva-optimus-corredores/equivalence/BcsCuentaInversionJobs/{cases.json,EQUIVALENCE.json,out/legacy,out/new}` |
| Legacy retired | 23 classes, 3,134 lines (+ `BcsJob`, replaced by `JobRunner`) |
| New code | about 3,200 lines in 40 main Java files (runtime / web copied from Tesoreria), 5 SQL files copied verbatim from the legacy |

### Test harness fixes made in Step 2 (Step 1 test code; the goldens were NOT edited)

Four groups of cases could not pass against ANY faithful implementation; four small harness changes (declared here for review):

1. `support/Observacion`: the status message of a scripted `NullPointerException("nulo")` (AR-06, CI-06) was masked as `<npe>` while the golden keeps `nulo`; now only the helpful JDK text is masked (`Canon.mensaje`).
2. `support/ColaboradoresDeCaso`: a scripted CHECKED `Exception` from the `Colaboradores` port (PB-04, PR-04; legacy `InscribirProcesoBatch.logica throws Exception`) was turned into an `AssertionError`; it is rethrown as it is.
3. `support/Equivalencia`: the goldens IP-nn / RC-nn hold the recording machine temp path in the `p_Ruta` of the collaborator trace (24 cases); both sides are compared with that path masked as `${RUTA_BASE}`. Re-recording the goldens would remove the literal path.
4. (No change to a test) `Rule076...` flagged a comment of a new main class that named a legacy utility; the comment was reworded.

### Canaries

Each canary ran on a scratch copy outside `modernized/` and `analysis/` (root pom with only this module; `bcs-utils` and `bcs-cuenta-inversion-servicios` from `~/.m2`), one line broken, the whole module test suite run. Evidence (diff, log, surefire XML of the failing classes): `analysis/otr-sva-optimus-corredores/equivalence/canary/BcsCuentaInversionJobs-transform/<n>-<name>/`.

| # | Break | Red tests |
|---|---|---|
| 1 | `job/archivo/FormateadorCampo.java` CHAR padding width `largo` → `largo + 1` | 31 (`GenerarArchivoGoldenTest`) |
| 2 | `job/BcsJobGenerarInformePatrimonioDiario.java` `FechaHasta.substring(0, 10)` → `(0, 9)` (report date window) | 13 (`InformesPatrimonioGoldenTest`) |
| 3 | `job/GuardarPatrimonioCajaJob.java` error threshold `<= 0` → `< 0` (patrimony: the jobs have no rounding of their own; this is the decision on the service answer) | 7 (`InformesPatrimonioGoldenTest`) |
| 4 | `runtime/JobRunner.java` a `false` result commits instead of rolling back | 26 (5 golden classes, `JobRunnerContractTest`, `JobRunnerRevisionArquitecturaTest`, `JobCallControllerTest`) |
| 5 | `job/pauta/PautaGenerarReporteAcreedores.java` parameter `P_Consolidar` `S` → `N` | 15 (`ReportesAntiguedadGoldenTest`) |

`Canary: 5 breaks → 92 tests failed (all detected)`.

## Mapping (legacy → target)

Legacy relative to `.../BcsCuentaInversionJobs/src/cl/bcs/cuentaInversion/jobs/`; target relative to `src/main/java/cl/bcs/cuentainversion/jobs/`.

| Behavior | Legacy (file:lines) | Target | Tests |
|---|---|---|---|
| Job contract (commit / rollback / error, status, `RutaSalida`, session statements) | `Plataforma/.../BcsJob.java:126-185` | `runtime/JobRunner`, `JobContext`, `CuentaInversionJob`, `JobResultado`, `JobStatusListener` | `JobRunnerContractTest`, `JobRunnerRevisionArquitecturaTest` |
| Entry point (was Job Manager) | `BcsJob.run` | `web/JobCallController`, `web/TokenInternoFilter` | `JobCallControllerTest`, `TokenInternoJobCallTest` |
| Balance recalculation, state row EJEC / empty | `ActualizarSaldoCargoAbonoJob.java:41-127` | `job/ActualizarSaldoCargoAbonoJob` | AS-nn |
| Aging report balances / initial load | `ActualizarSaldosReporteAntiguedadJob.java:46-98`, `CargaInicialSaldosReporteAntiguedadJob.java:146-184` | `job/ActualizarSaldosReporteAntiguedadJob`, `job/CargaInicialSaldosReporteAntiguedadJob` | AR-nn, CI-nn |
| Daily patrimony / fee summary reports | `BcsJobGenerarInformePatrimonioDiario.java:45-79`, `BcsJobGenerarInformeResumenCobro.java:136-168` | `job/BcsJobGenerarInformePatrimonioDiario`, `job/BcsJobGenerarInformeResumenCobro`, `Llamada.actualizarPrograma` | IP-nn, RC-nn |
| Cash patrimony | `GuardarPatrimonioCajaJob.java:211-246` | `job/GuardarPatrimonioCajaJob` | GP-nn |
| Five file jobs | `GenerarArchivoCargoAbonoJob`, `...ClientesPorPagarYCobrarJob`, `...CuentasPorCobrarAnexo16Job`, `GenerarArchivoDiferenciaSaldoJob`, `GenerarArchivoSaldoCuentasJob` | the same names in `job/` | GC, CP, A1, DS, SC |
| Three aging / creditor reports | `GenerarReporteAcreedoresJob`, `...AntiguedadDeudaBolsaCTAJob`, `...AntiguedadDeudaCTAJob` | the same names in `job/`, `Llamada.guardarRutaReporte` | RA, AB, AC |
| Four PAUTA steps | `PautaActualizarSaldoCargoAbono`, `PautaGuardarSaldosReporteAntiguedad`, `PautaGenerarReporteAcreedores`, `PautaGenerarReporteAntiguedadDeudaBolsaCTA` | `job/pauta/*` (plain beans, `ProcesoPauta`), `job/pauta/InscribirProcesoBatch` (platform contract) | PS, PG, PR, PB |
| Library jobs: stored procedure | `LibroInventarioJob.java:299-334` | `job/LibroInventarioJob`, port `job/port/LibroInventario`, `adapter/LibroInventarioJdbc` | LI-nn |
| Generic file generator | `generarArchivo/GenerarArchivo.java:57-265` | `job/GenerarArchivo`, port `job/port/ProcedimientoArchivo`, `adapter/ProcedimientoArchivoJdbc` | GA-nn |
| Format lookup, writer, field formatter | `generarArchivo/FormatoArchivo.java:103-354`, `Archivo.java`, `FormatoArchivoDetalle.java` | `job/archivo/FormatoArchivo`, `FormateadorCampo`, `Archivo`, `FormatoArchivoDetalle` | GA-nn, GF-nn |
| CSV writer (opencsv 3.7 rules) | `com.opencsv.CSVWriter` 3.7 (`JOBMAN.gradle`) | `job/archivo/CsvLegacyWriter` (copy of Tesoreria's) | GA-04, GA-05, GA-31, GA-33 |
| Service call, parameter lookup, `RutaSalida` updates | `new Servicio().logica(con, e)`, `ConfiguracionSistema` | `job/Llamada`, `job/port/ConfiguracionJobs`, `adapter/ConfiguracionJobsJdbc`, `adapter/ColaboradoresJobs` | all |
| Wiring | n/a | `wiring/JobsConfiguration`, `wiring/ServiciosCuentaInversionConfiguration`, `BcsCuentaInversionJobsApplication` | `ArranqueAplicacionTest` |
| No daemon / debug / credentials / 3DES | `running` flags, `pause()`, every `main()`, `LibroInventarioJob` constants | (absent) | `Rule061...`, `Rule076...`, `Rule077...` |

## Decisions applied

1. Service library: only the 15 services the jobs call (plus `ConsultarSaldoCuentaInversion`, `SaldoCuentaInversion` and the adapters they need) are scanned (`ServiciosCuentaInversionConfiguration`, include filter). The transactional family of the library and its REST clients are NOT started.
2. `ColaboradoresJobs` replaces the library `ColaboradoresJavaCall` (same routing: in-process dispatcher for `cl.bcs.cuentaInversion.*`, platform JavaCall router with the token for the rest) with bounded timeouts.
3. `ConfiguracionJobsJdbc` reads `PLA.Configuracion` for `PLA.FechaSistema` and `PLA.RutaBaseArchivos` (the legacy `Utils.getConfiguracion` used a HANA procedure; see "Not proven").
4. opencsv 3.7 reproduced by `CsvLegacyWriter`; parent pom untouched. Production file system assumed Linux (LF).
5. Config: `application.yml` with `HANA_*`, `JAVACALL_URL`, `INTERNAL_TOKEN`, plus `CONFIGURACION_URL` (required since step 5) (needed by the in-process `ConsultarSaldoCuentaInversion`, a REST client of the Servicios library). Both compose files (`bcs-cuenta-inversion-jobs`) now carry `JAVACALL_URL`, `CONFIGURACION_URL` and `INTERNAL_TOKEN`. Dockerfile builds `BcsUtils` and `BcsCuentaInversionServicios` first, like Tesoreria's.

## Legacy defects kept (CIJ-DEF-nn, for the domain expert)

| Id | Defect | Cases |
|---|---|---|
| 01 | A job that answers `false` (rollback) is reported exactly like a commit (`BcsJob.java:151`) | AS-03, AR-02, GP-06, GC-03, IP-02, LI-02/05 |
| 02 | `ActualizarSaldoCargoAbonoJob`: the `finally` commits after a `java.lang.Error`; the first state UPDATE runs outside the `try` | AS-07, AS-08 |
| 03 | Service error counters ignored in the report jobs (IP/RC store the file anyway; RA/AB/AC commit partial work; SC overwrites the counter with 0) | IP-03, RA-02, AB-02, AC-02, SC-15 |
| 04 | `GenerarArchivoSaldoCuentasJob`: `!= "0"` / `!= ""` compare references (always true); `.replace(".csv", ".zip")` on every occurrence; bad `Encabezados` depends on the Gson version | SC-02..07, SC-11..19 |
| 05 | `GenerarArchivoClientesPorPagarYCobrarJob` throws `java.lang.Error` on service errors (skips `catch (Exception)`) | CP-02..05, CP-15 |
| 06 | Report jobs: parameters parsed outside the `try`, in read order; short dates fail; no defaults; empty answer without messages is `IndexOutOfBounds` | IP-08..13, IP-19, RC-19, RA-03 |
| 07 | `LibroInventarioJob` swallows every exception as `false`; legacy constants with a literal password and a `main()` (not reproduced anywhere) | LI-02..05, LI-08, LI-11 |
| 08 | `GenerarArchivo`: CSV header replaces the first data row; `TipoRegistroOptimus` must be the first CSV column; a missing format is an NPE; format SQL built with `String.format` (injection shape) | GA-05..07, GA-18..20, GA-23 |
| 09 | `GenerarArchivo`: every generation failure is swallowed and the job still commits; files not transactional; ONE directory level created; null separator prints `null`; unknown `FormatoSalida` writes an empty file | GA-10, GA-12..15, GA-27, GA-28 |
| 10 | Formatter: DEC left padded, DEC without `.` is 0, widths not enforced for integers, NUM not truncated, lenient dates, unknown `TipoDato` prints nothing | GF-10, GF-18, GF-21..24, GF-29, GF-30, GF-38, GF-44..48 |
| 11 | PAUTA steps: `(Long)` casts, `String.valueOf(null)` = "null", `IdUsuario` always 0, `P_OUT_MENSAJES` empty, no commit | PS-07, PR-14, PR-15 |
| 12 | Output classes without defaults (`P_OUT_ERRORESCONTADOR` null) are NPEs in the job | GC-09, GC-10, PS-10 |
| 13 | Debug `main()`, `debug` flags, `running` flags, `pause()` stubs, `call()` stubs: none ported (RULE-061/076) | RULE tests |

## Deviations from the legacy

| Id | Deviation | Behavior change |
|---|---|---|
| DEV-CIJ-01 | Entry is `POST /internal/job-call` (token-protected) instead of the Job Manager; the status listener only logs | Delivery only |
| DEV-CIJ-02 | Services called in process through `execute(JsonObject)`, the same transaction | None (goldens) |
| DEV-CIJ-03 | `GenerarArchivo` reads ALL result sets of the procedure first (port) and then creates the file; the legacy created the file before reading them | Only if reading a result set fails midway (HANA): no empty file is left |
| DEV-CIJ-04 | `getParam("IdEjecutivo") != "0"` and `!= ""` (reference comparison, always true) are written as the unconditional behavior | None (CIJ-DEF-04) |
| DEV-CIJ-05 | Logging through SLF4J instead of printed stack traces | None |

No deviation changes behavior pinned by the goldens.

## Not proven

* **HANA.** Schema INFERRED (`schema-jobs.sql`); `LibroInventarioJdbc`, `ProcedimientoArchivoJdbc` (named parameters), `ConfiguracionJobsJdbc`, null binding (`VARCHAR`) of the `RutaSalida` updates and the `LEGACY` SQL are not run on HANA. `ActualizarSaldoLibroInventario` itself is not exercised.
* **Real collaborators.** The 15 services (proven in BcsCuentaInversionServicios) and `InscribirProcesoBatch` are scripted in the goldens; `ColaboradoresJobs` has no test of its own against a router.
* **`PLA.RutaBaseArchivos` / `Utils.getConfiguracion`**: the legacy read it through a HANA procedure; here the table is read (assumption). A missing value is not pinned (unknown legacy failure).
* **opencsv 3.7** reproduced from its rules, not run against the jar.
* **No Quartz / Job Manager.** Scheduling and cut-over are later phases; running legacy and new at once risks double execution.
* The `ConsultarSaldoCuentaInversion` in-process path (needs `CONFIGURACION_URL`) is not exercised by the goldens (services are mocks).
* Two services' in-process dispatch for classes whose services are not scanned (the transactional family) fails with `NoSuchBeanDefinition` if a report ever asked for them.

## Obsolete pilot files: deleted on the user's instruction 2026-10-21

Deleted (40 files): under `src/main/java/cl/bcs/cuentainversion/jobs/`: `domain/` (4), `infrastructure/adapter/` (6), `job/*JobService` (16), `pauta/` (the old pilot package, 4), `port/out/` (9); and the pilot test `src/test/java/cl/bcs/cuentainversion/jobs/CuentaInversionJobsTest` (27 cases). Nothing that stays referenced them (grep). `pom.xml` and `application.yml` already carried no pilot-only dependency or key. `ModuloFuente.esNuevo` (scan filter) was removed: the RULE-061/076/077 scans now cover the whole module. `ArranqueAplicacionTest` keeps a bean-name check (no `*jobservice`, `*clientstub`, `hanaJobRepository`). Test count after the deletion: 516.

## Step 1 test-support changes accepted by the user 2026-10-21

The changes to Step 1 test code listed under "Test harness fixes made in Step 2" are accepted: `support/Observacion` (only the JDK helpful NPE text is masked), `support/ColaboradoresDeCaso` (a scripted checked `Exception` is rethrown as checked) and `support/Equivalencia` (the recording-machine temp path is masked as `${RUTA_BASE}` on both sides in the IP and RC goldens, 24 cases). The goldens were not touched.

## Open review findings (pattern `analysis/otr-sva-optimus-corredores/JOBS_PATTERN.md`)

OPEN (cross-cutting task, not done in this module):

* H4: parameters should be loaded from `BAT.Programa` by `idPrograma` and the run claimed in the database (today the caller chooses `idPrograma` and `parametros`; the guard is per JVM).
* H3: no bound on a run (transaction / query timeouts, watchdog, list of runs in flight).
* H2: output files in the ephemeral container layer (shared volume, healthcheck, non-root user).
* M4 (guard per program, not per class), M6 (graceful shutdown), M7/M10 (validate `JAVACALL_URL`, token length, log 401), M9 (charset and zone in the container).
* This module in particular: the file generator takes `CodigoFormatoArchivo` as it is (CIJ-DEF-09 injection shape kept); the path and the `Servicio` are now checked (step 5, below); a failed generation still leaves a partial file and no `RutaSalida`, as the legacy did (the descriptor is released now).

## Step 6b: fail closed when a guarded call has no scope on its thread + build-time scan for parallelism (hardening of step 6)

Binding as in step 6: NO database change (no marks are written by this hardening, only the existing `BAT.LogPrograma` marks of step 6 remain), H2 only, legacy untouched.

### Problem

The run scope of step 6 (`ContextoGuarda`) is a ThreadLocal of the thread that runs the job. A guarded collaborator (NO_REPETIBLE or MIGRADO_ESCRITOR that reaches HTTP) called from ANOTHER thread (a `parallelStream` or a pool inside a library such as apv-service, a `CompletableFuture`) found no scope and the decorator passed it through UNMARKED: a TomEE write with no INTENTO/HECHO mark, invisible to the re-run refusal. Step 6 limit 5 said "the module starts no other thread (checked)"; that was true of this module only, not of the libraries the jobs call.

### Runtime change (fail closed)

`ContextoGuarda` now distinguishes three states of a thread, and knows the mode of the module (`new ContextoGuarda(exigePrograma)`; the bean is built from `ProgramaModo`: `true` only in mode `programa`, i.e. `cuentainversion.jobs.programa.modo` = `programa`, the default):

| Thread state | Mode `programa` | Caller mode (`parametros-del-llamador`) |
|---|---|---|
| a `CorridaGuardada` is open (`GuardaTomee.abrir`, after the claim) | guarded names: marks + call (unchanged) | n/a (nothing is claimed) |
| explicit "no program" scope (`GuardaTomee.abrirSinPrograma`, opened by `EjecutorPrograma` for the whole run in caller mode) | pass-through, no marks | pass-through, no marks |
| NO scope at all | guarded name: REFUSED (see below); any other name: pass-through | pass-through (nothing to guard) |

Refusal: before any HTTP call and without writing any mark (there is no `idPrograma` to key it), `GuardaTomeeException("llamada protegida sin contexto de programa (hilo distinto?): <name> ...")` is thrown and logged at ERROR with the thread name. The job sees an exception and rolls back its own work as for any failed collaborator call. Not affected: names that are not guarded (reads, `AgregarLog`, in-process services, unknown names), threads that never call a guarded name (health/actuator, tests), the property `cuentainversion.jobs.guarda-tomee=false` (no beans: wiring identical to step 5), and the `ContextoGuarda()` default constructor is the strict one (`true`), so a hand-built context cannot silently be lax.

The scope is NOT propagated to other threads on purpose: the marks of one run are numbered in call order (`n`) and a guarded write racing on another thread would make the INTENTO/HECHO order unreadable. A job that really needs a guarded call from a pool must be redesigned to call it on the job thread.

Tests: `GuardaSinContextoTest` (G6b-01..08: second thread refused with no HTTP call and no marks; `parallelStream` inside a ForkJoinPool; commonPool and `CompletableFuture`; no run at all; caller mode passes on any thread; explicit "no program" scope; unguarded names from other threads; `EjecutorPrograma` opens and closes the caller-mode scope). Test changed: `ColaboradorTomEEGuardadoTest` G6-D05 pinned "no scope = pass-through"; that is now only true for caller mode, so it builds the guard over `new ContextoGuarda(false)`; the no-scope-in-mode-programa case is G6b-04.

### Build-time scan

`ParalelismoFuentesTest` (G6b-S01..S03) scans `src/main/java` of this module and of the sibling libraries its jobs use (this module, BcsUtils, BcsCuentaInversionServicios; located relative to the module, existence asserted) for `parallelStream(`, `.parallel(`, ForkJoin, CompletableFuture, ExecutorService/Executors, `@Async`, `new Thread`, `sendAsync`, `StreamUtils.parallel*`. Every hit must be in an explicit allowlist (file, exact number of matching lines, reason); a new hit, one more line in an allowlisted file, a hit on the same line as a collaborator call, or a stale entry fails the build.

### Result of the scan for this module

The only hit in the scanned sources is `BcsUtils` `StreamUtils.parallelFlatmap` (3 lines): defined and never called by any scanned source (a future caller is itself a hit of the scan), and its lambdas only apply a pure `BiFunction` to two in-memory collections: NOT on a path that reaches a collaborator. The module itself and its Servicios libraries start no thread and use no parallel stream.

## Follow-ups

* `/code-modernization:modernize-verify otr-sva-optimus-corredores BcsCuentaInversionJobs` for the independent check.
* SME decision on CIJ-DEF-02, 03, 08..10 before any is fixed.
* Re-record the IP / RC goldens without the literal temp path; decide how the platform PAUTA runner reaches the 4 PAUTA beans.

## Architecture review (step 5)

APPLIED (legacy behavior and CIJ-DEF-01..13 unchanged; 413 goldens unchanged; 531 tests, 0 failures, 0 skipped (516 + 15 new); dual dump identical; compare.py 4565/4565):

* H-4: `services.configuracion.url` (`CONFIGURACION_URL`) has no default any more (the `localhost:8101` default pointed at another service): the application does not start without it; the yml comment says "required". The library `ConfiguracionRestClient` (plain `RestClient`, no timeouts) is no longer scanned; `adapter/ConfiguracionRestAcotado` extends it and replaces `getConfiguracion` with a client with connect 2 s / read 60 s (`services.configuracion.connect-timeout` / `read-timeout`), like `ColaboradoresJobs`. Servicios sources untouched. Tests set `services.configuracion.url` in `application-test.properties`. Both compose files already set `CONFIGURACION_URL`; their comment was corrected ("required").
* M-2: `job/archivo/RutaSegura` (own copy of the lenient rule of BcsTesoreriaJobs). `Archivo` rejects, in its constructor (a job error, before anything touches the disk), a file name with `/`, `\` or `..` and a path that escapes `RutaCompleta` (when `RutaCompleta` has no final separator its parent is the limit, as in Tesoreria). No golden uses odd names (the oddest, `Saldos de Cuentas Ñandú`, has spaces and accents and keeps working).
* M-3: `GenerarArchivo` rejects a `Servicio` that does not match `^[A-Za-z0-9_.:]+$` as a job error before calling the procedure. EXCEPTION, to keep all goldens green: golden GA-29 was recorded from the legacy with `Servicio = SP"X` (a double quote interpolated unchecked). The 4-argument constructor `GenerarArchivo(..., validarServicio=false)` exists only for that replay (`JobsDeCaso` uses it for GA-29 alone); production wiring uses the validating 3-argument constructor. The `CodigoFormatoArchivo` injection shape (CIJ-DEF-08/09) is untouched. Decision for the domain expert: delete GA-29 / re-record it when the legacy quirk may be dropped, and then remove the switch.
* H-2 (partial): `Archivo` holds a `FileOutputStream` + `OutputStreamWriter` (what `FileWriter` is: same default charset, truncating). `cerrar()` closes the descriptor even if the flush fails; when the generation fails midway `GenerarArchivo` calls `Archivo.liberar()`, which closes the STREAM and not the writer, because closing the writer would flush the buffered text and change the partial file pinned by GA-28 (empty file). `crear()` failure is logged at ERROR with `IdPrograma`; the IOException swallow and the commit of `RutaSalida` stay (CIJ-DEF-09).
* M-8: the legacy (`LibroInventarioJob.java:40`) registers `Types.BIGINT` and reads it with `getInt(3)`; kept bug for bug and commented in `LibroInventarioJdbc` (the count is only printed; it matters only above 2^31).

OPEN (not done in this module):

* H-1: no shared volume for the generated files: compose needs a volume such as `/data/archivos`, and a startup check that `RutaBaseArchivos` exists and is writable.
* H-2 rest: temp file + atomic move (would change the pinned partial-file behavior, needs the SME decision on CIJ-DEF-09), `CREATE_NEW` against name collisions at 1-second resolution.
* H-3: the internal token is shared and sent to any `JAVACALL_URL`: https / allow-list, a token per caller, redirects NEVER followed explicitly.
* M-1: `ConfiguracionJobsJdbc` reads `PLA.Configuracion` without ORDER BY / validity filter and returns the last row: an unproven assumption, to become an entry criterion with a HANA smoke test.
* M-4: PAUTA enrolment is not idempotent: a retry would enrol a duplicate batch program.
* M-5: the PAUTA beans have no entry point yet: `POST /internal/pauta-call` or the platform runner is a next-phase entry criterion.
* M-6: graceful shutdown. M-7: Dockerfile (USER, HEALTHCHECK, JVM flags, reproducible build).
* L-1..L-6: null binding; TZ of the Clock; scan breadth and two classes that may go stale on a Servicios upgrade (`GeneradorPdfNoDisponible`, `JavaCallDispatcher`); public actuator and synchronous runs; encoding / BOM difference between the `Archivo` CSV and the Servicios CSV; log content.
* JOBS_PATTERN items H4 / H3 / H2 (see "Open review findings" above).

## Decision on GA-29 / M-3 (user, 2026-10-21)

Option A: the `Servicio` name validation (`^[A-Za-z0-9_.:]+$`) stays in production as a deliberate hardening deviation from the legacy (which interpolated any value into `CALL "%s"`); golden GA-29, recorded with `SP"X`, keeps running only through the test-only 4-argument constructor (`validarServicio=false`). The CIJ-DEF-09 injection shape of `CodigoFormatoArchivo` is untouched.


## Job pattern H4

Ported from the reference implementation in `BcsTesoreriaJobs/TRANSFORMATION_NOTES.md` ("Job pattern H4 (reference implementation)", commit fa8aabbd); the design, the exact SQL, the order of a call, the final states and the limitations 1-8 of that section apply here unchanged. Binding constraints kept: NO database change of any kind, only existing `BAT.*` columns, tables and views, legacy untouched, no automatic retry, "requires review" = `ERR` + `Intento = MaximoIntentos` + `Bloqueo 'S'` + `Resultado` prefix `REQUIERE REVISION: `. The dispatcher (the modern `BcsJobManager`, later step) calls `POST /internal/job-call` with `idPrograma` and the internal token for programs still `PEN` / `'N'` and must NOT claim them (the endpoint claims atomically; a program claimed by the dispatcher answers 409).

### What was ported

* Package `cl.bcs.cuentainversion.jobs.programa`: the same 12 classes with the same names and behavior (`ProgramaRepositorio(Jdbc)`, `ProgramaSql`, `EjecutorPrograma`, `ProgramaStatusListener`, `ProgramaModo`, `TextoResultado`, `Programa`, 4 exceptions).
* `runtime/JobRunner`: `ejecutarReclamando(clase, id, Supplier)`; `ejecutar` delegates to it (callers and the 531 pre-existing tests unchanged). `web/JobCallController`: second constructor (`EjecutorPrograma`, `@Autowired`), `exigeClase()`, handlers 404 / 409 / 422; `new JobCallController(runner)` keeps the old contract (caller mode). `wiring/ProgramaConfiguration` (new) now owns the `jobStatusListener` bean (removed from `JobsConfiguration`).
* Switch `cuentainversion.jobs.programa.modo`: `programa` in `src/main/resources/application.yml` (`cuentainversion.jobs.programa.modo`), `parametros-del-llamador` in `src/test/resources/application-test.properties`. An unknown value stops the start. `clase` is optional in `programa` mode; caller `parametros` that differ from the view stay a 422.
* New test schema `src/test/resources/cuenta-inversion-jobs/schema-programa.sql` (extra `BAT.Programa` columns by `ALTER TABLE`, `TipoPrograma`, `Agenda`, `ParametrosPrograma` with `ValorBlob`, the view, `LogPrograma`), loaded after `schema-jobs.sql` and only by the H4 tests. `schema-jobs.sql`, the 413 goldens and the legacy oracle output are untouched (every golden dumps `BAT.Programa` with `SELECT *`).
* New tests (39; the module goes from 531 to 570, 0 skipped): `programa/ProgramaRepositorioLecturaTest`, `ProgramaRepositorioReclamoTest` (two-thread race), `ProgramaEstadoFinalTest` (TER / ERR / truncation / requires review), `ProgramaJobCallTest` (HTTP 200/404/409/422/503/500), `ProgramaModoTest`, `wiring/ProgramaArranqueTest`; fixture `support/ProgramaH2`.

### Deviations from the reference

* `ProgramaH2` does not create `"TEST.Marca"`: this module's `schema-jobs.sql` already has it (with an `Orden` identity column), and it loads `/cuenta-inversion-jobs/schema-*.sql`.
* `application.yml` instead of `application.properties`; `ProgramaModoTest.valoresPorDefecto` reads the yml with `YamlPropertiesFactoryBean`.
* The defect reference in the listener Javadoc is `CIJ-DEF-01` (commit AND rollback report the same status) instead of `TES-J-DEF-19`.
* The `BAT.Programa` of this module's `schema-jobs.sql` already has `RutaLog` and `Comentario`; the H4 schema only adds the missing columns and ALTERs the three existing ones to the real lengths (`RutaSalida` 100, `RutaLog` 100, `Comentario` 3000).

### PAUTA gap

The 4 PAUTA steps (`job/pauta/Pauta*`, plain beans with `ProcesoPauta`) have no entry point and are NOT wired to the claim: only the jobs behind `/internal/job-call` use the H4 flow. When the PAUTA runner (or `POST /internal/pauta-call`) exists it needs its own decision: how the PAUTA program row is claimed and finished (the PAUTA runner of the platform, not `BcsJobManager`, owns the `BAT.Programa` state of those steps today), idempotency of the enrolment (M-4) and where the parameters come from. Until then they are unprotected by the H4 claim.

### Limitations

Those of the reference (process death after the claim leaves `ASI` / `'S'`; failure writing the final state is only logged; no DB unique guarantee beyond the conditional UPDATE and the per-JVM guard; `Nodo` exclusivity is an operational cut-over; requires-review only distinguishable by the text prefix; collaborators committed outside the job transaction; `MaximoIntentos = 0` never claimable; nothing proven on HANA), plus: the H4 H2 schema is the REAL DDL (CONFIRMED by the DBA from HANA QA, 2026-10-21, `analysis/otr-sva-optimus-corredores/BAT_DDL_REAL.md`, including the view; `IdTipoPrograma` NVARCHAR(40) read as a String), only primary keys, NOT NULL and defaults stay assumed; the claim, final-state and view SQL are untested on HANA.

## Step 6: fail-closed guard for TomEE write collaborators (JOBS_PATTERN.md H-1 point 2; JOBS_H1_H4_INVENTORY.md section D step 6)

Port of the reference in `BcsTesoreriaJobs/TRANSFORMATION_NOTES.md` ("Step 6": guarantees, limits, mark format `H4G|FASE|id|Intento|clase|n` in `BAT.LogPrograma`, HTTP 423, operator release procedure): read it there; everything stated there applies here unless listed under "Deviations". NO database change (only existing `BAT.LogPrograma` / `BAT.Programa`), legacy untouched, no idempotency header, no automatic retry. Switch: `cuentainversion.jobs.guarda-tomee` (`true` in `application.yml` and when the property is missing; `false` in `application-test.properties`, pinned by `GuardaCableadoApagadoTest`, so the 570 earlier tests and the dual dump run on the pre-step-6 wiring).

### What was ported (package `cl.bcs.cuentainversion.jobs.guarda`, copied with the package renamed)

`MarcaGuarda`, `EstadoMarcas`, `MarcasRepositorio`, `MarcasRepositorioJdbc`, `ColaboradorTomEEGuardado`, `ContextoGuarda`, `CorridaGuardada`, `GuardaTomee`, `GuardaTomeeException` (identical to the reference); `ClasificacionColaborador` (own table, below); `programa/ProgramaRequiereRevisionException`; `programa/EjecutorPrograma` (4-argument constructor, `abrirGuarda`, `revisarDespues`, try/finally; the 3-argument constructor and `parametrosDelLlamador` stay); 423 handler in `web/JobCallController`; `wiring/GuardaTomeeConfiguration`; `wiring/ProgramaConfiguration` (guard via `ObjectProvider`); property in both profiles.

### Deviations from the reference (adapted, not invented)

1. Names are FULL legacy class names. The services of `bcs-cuenta-inversion-servicios` send `cl.bcs.plataforma.bat.InscribirProcesoBatch`, `cl.bcs.plataforma.pla.AgregarLog`, ... (router keys of inventory A.2), not the short names of Tesoreria / APV. `ClasificacionColaborador` and the marks use the full name (the mark accepts `[A-Za-z0-9_.]{1,100}`; the longest name here is 68 characters). A name that is not in the table is passed through (as in the reference); the test makes that impossible for any name the module can send.
2. Wiring. `ColaboradoresJobs` stays the `@Component` it was (no main change to it); with the guard ON, `GuardaTomeeConfiguration` adds the `@Primary` bean `colaboradoresGuardados` = `GuardaTomee.envolver(colaboradoresJobs)`, so the library services (through `ColaboradoresReportesAdapter` / `AuditoriaReportesAdapter`) and the PAUTA steps receive the guarded port; with the guard OFF no bean is added and `ColaboradoresJobs` is the only `Colaboradores`. The guard wraps `ColaboradoresJobs` as a whole, which answers the `cl.bcs.cuentaInversion.*` classes in process through `JavaCallDispatcher`: those names are `MIGRADO_PROPIO` (never marked), and test G6-C05 proves with the real routing table that no guarded name is answered in process and every `MIGRADO_PROPIO` name is, so only a call that goes on to the router can leave marks.
3. Switch in `application.yml` (this module has no `application.properties`): `cuentainversion.jobs.guarda-tomee: true`. Pinned by test G6-W04 (reads the yml, and `matchIfMissing=true`).
4. Test fixture: `GuardaBase` uses `CuentaInversionJob` and the existing `support/ProgramaH2` (real `BAT.LogPrograma` DDL, no schema change); the tests use full names (`InscribirProcesoBatch` as the guarded writer, `AgregarEvento` as the second one). No `GuardaCableadoEnProcesoTest` (no step 5 here; the in-process side is the dispatcher above).
5. `ClasificacionColaboradorTest` is stronger than the APV one because the names are constants, not literals: it scans the module sources AND the sibling library sources (`BcsCuentaInversionServicios/src/main/java`) for every `invocar(` call, resolves a literal, a constant of the same file or `X.CLASE` of `domain.colaborador` (read by reflection), and FAILS on any other argument (a computed name); the only exceptions are the two library files that forward their own `servicio` parameter (private helper of `ColaboradoresReportesAdapter`, typed overload of the port), whose names are covered by reflecting their constants. A synthetic text with `invocar(nombre, ...)` is checked to be flagged. Then every name found (jobs, library services, adapter constants, `CLASE` of every collaborator contract) must be classified.

### Classification of what this module can send

| Category | Names (full class names, A.2) | Guarded |
|---|---|---|
| NO_REPETIBLE (17, TW) | `plataforma.bat.AgregarPrograma`, `plataforma.bat.InscribirProcesoBatch` (the only one this module sends itself: the PAUTA steps), `plataforma.evt.AgregarEvento`, `plataforma.aut.CrearEventoAutorizacionMasivo`, `plataforma.aut.ModificarEstadoEventoAutorizacionMasivo`, `plataforma.pla.ValidaEjecucionProcesoRecPAC`, `ordenes.CrearOrdenes`, `fondomutuo.CrearSolicitudAporteRescateNormal`, `fondomutuo.AnulacionContratoCSC`, `fondomutuo.RevocacionContratoCSC`, `portafolio.ModificarPortafolio`, `socionegocio.cli.CrearCliente`, `socionegocio.per.CrearPersona` / `CrearTelefono` / `CrearDireccionElectronica` / `CrearPersonaCuentaBancaria`, `facturacion.CrearMovimientoComprobante` (only reachable from `ProcesarCobroComisionPatrimonioService`, which is NOT scanned here) | yes |
| APPEND_ONLY | `plataforma.pla.AgregarLog` (`AuditoriaReportesAdapter`; its callers, the cobro-comision services, are not scanned here) | no (as in the reference) |
| MIGRADO_ESCRITOR (10) | `APV.ValidarMandatosPAC`, `APV.CambiarEstadoContratosSAAPV`, `tesoreria.CrearIngresoEgreso`, `LiquidarComprobante`, `CrearComprobante`, `ban.ActualizarSaldosCuentaBanco`, `nor.AnularNominasTransferencias`, `AnulacionMandatoPAC`, `RevocacionMandatoPAC`, `IngresarMandatoPAC` | yes: no in-process dispatch here, they would go over the router and commit apart (none is sent today) |
| MIGRADO_PROPIO (3) | `cuentaInversion.CrearCargoAbono`, `AnularCargoAbono`, `ConsultarSaldoCuentaInversion` | no: `JavaCallDispatcher`, in the job transaction |
| SOLO_LECTURA (21) | `plataforma.mer.CalcularParidad`, `plataforma.pla.utils.ConsultarEmpresa`, `plataforma.tas.EntregarPreciosTasas`, `ObtenerPortafoliosEjecutivo(.restringeCartera)`, the three `normativa.cartola.*`, and the other TL / `ConsultarClearingFinanciero` names of A.2 | no |
| ESPECIAL | `StoredProcedure` (not sent: the file / inventory jobs use JDBC ports in the job connection), `Cloper` | no |

What the module reaches over the router and is guarded TODAY: no job of `/internal/job-call` sends a guarded name. The 16 jobs reach, through the 15 scanned services plus `ConsultarSaldoCuentaInversion` / `SaldoCuentaInversion`: `CalcularParidad`, `EntregarPreciosTasas`, `ObtenerPortafoliosEjecutivo`, the cartola reads (TL) and `ConsultarSaldoCuentaInversion` (in process). The one guarded name this module can send is `InscribirProcesoBatch`, from the PAUTA steps. Consequence, stated plainly: for the jobs the guard is currently a safety net with nothing to guard (a program with no guarded call leaves no marks and ends as before); it protects what a future scanned service or the PAUTA runner adds.

### PAUTA gap (not invented, not closed)

The 4 PAUTA steps have no entry point and no claim (see "PAUTA gap" above). They receive the guarded `Colaboradores`, but the guard scope is only opened by `EjecutorPrograma` for a run with an `idPrograma`; a PAUTA call made outside it finds no scope and is a pass-through, i.e. `InscribirProcesoBatch` is NOT protected there (M-4: a retry enrols a duplicate batch program). Closing it needs the PAUTA entry point / claim decision first; then the same scope open / close must be applied there.

### Threads (reference limit 5)

BcsCuentaInversionJobs main code starts no thread (no executor, `@Async`, `@Scheduled`, `parallelStream`; the only `Thread` use is `Thread.interrupted()` in `JobRunner`), and `JobRunner` runs the job in the request thread, the same one where `EjecutorPrograma` opens the scope. The library `bcs-cuenta-inversion-servicios` main code has no `parallel`, `Executor`, `CompletableFuture`, `@Async` or `ForkJoin` (grep, all services, scanned or not); `bcs-utils` has `StreamUtils.parallelFlatmap` but neither this module nor the library uses it (the `StreamUtils` they import is Spring's). So the thread-local scope is kept as is, no scope passing. If a service that fans out to a pool is added later, the scope must be passed along or that call would run unguarded.

### Tests (new: 34, baseline 570 -> 604, 0 failures, 0 skipped)

`guarda/MarcaGuardaTest` 8, `ClasificacionColaboradorTest` 5, `ColaboradorTomEEGuardadoTest` 8, `GuardaProgramaJobCallTest` 9 (MockMvc + real runner / executor / repositories on H2, includes 423), `wiring/GuardaCableadoHttpTest` 2, `wiring/GuardaCableadoApagadoTest` 2. Dual dump (`-Dequivalence.out`): `diff -rq` against `analysis/.../equivalence/BcsCuentaInversionJobs/out/legacy` and `out/new` identical (4,565 files each). HANA not proven (H2 only), same as the reference.
