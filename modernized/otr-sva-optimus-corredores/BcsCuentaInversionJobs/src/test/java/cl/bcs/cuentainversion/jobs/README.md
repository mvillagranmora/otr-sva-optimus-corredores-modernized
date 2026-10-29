# BcsCuentaInversionJobs: characterization tests (Step 1)

These tests define "done" for the rewrite of the legacy module
`legacy/otr-sva-optimus-corredores/BSCL/CuentaInversion/JAVA/BcsCuentaInversionJobs` (23 classes, 3134 lines) and of the job framework
`BSCL/Plataforma/JAVA/BcsJobManager/src/cl/bcs/scheduler/BcsJob.java`. The oracle is the legacy code itself: the goldens were recorded by EXECUTING the
unmodified legacy classes under the real legacy `BcsJob.run()` (see "How the oracle runs"). Same process and conventions as BcsTesoreriaJobs and BcsAPVJobs.

Binding decisions (user): the jobs are IN-PROCESS (a library on top of the proven `bcs-cuenta-inversion-servicios`, direct calls, no HTTP between job and
service), run by a `JobRunner` copied from BcsTesoreriaJobs with the BcsJob contract (one transaction per run: commit / rollback / error, status,
`RutaSalida`), entry `POST /internal/job-call` with the internal token; NO scheduler annotation, NO daemon mode (RULE-061), no debug `main()` / credentials
(RULE-076), no 3DES (RULE-077). Legacy behavior is reproduced bug for bug; defects are listed for the domain expert (SME) as CIJ-DEF-nn.

## State

Step 1 only. Skeletons exist (`runtime`, `job`, `job.pauta`, `job.port`, `web`, `wiring`): every job throws `UnsupportedOperationException("TODO Step 2:
<legacy class>")`. `mvn -o -f ../pom.xml -pl :bcs-cuenta-inversion-jobs test`: 543 tests at Step 1 (516 after the pilot test was deleted 2026-10-21), 126 pass (the copied runner / web / security tests, the harness
self-checks and the 27 pilot tests, untouched at that time), 413 golden cases FAIL on the TODO and `ArranqueAplicacionTest` (4 tests, one cause) fails on the TODO of
the wiring. Canary (done and reverted): a throwaway implementation of `GuardarPatrimonioCajaJob` passed its 16 cases, so the goldens are satisfiable.

## Test classes and cases (413 cases; the case id prefix selects the job)

| Class | Legacy class (prefix) | Cases |
|---|---|---|
| `job/SaldosGoldenTest` | ActualizarSaldoCargoAbonoJob (AS) 22, ActualizarSaldosReporteAntiguedadJob (AR) 14, CargaInicialSaldosReporteAntiguedadJob (CI) 12, PautaActualizarSaldoCargoAbono (PS) 13, PautaGuardarSaldosReporteAntiguedad (PG) 13 | 74 |
| `job/InformesPatrimonioGoldenTest` | BcsJobGenerarInformePatrimonioDiario (IP) 20, BcsJobGenerarInformeResumenCobro (RC) 20, GuardarPatrimonioCajaJob (GP) 16 | 56 |
| `job/ArchivosServiciosGoldenTest` | GenerarArchivoCargoAbonoJob (GC) 17, GenerarArchivoClientesPorPagarYCobrarJob (CP) 17, GenerarArchivoCuentasPorCobrarAnexo16Job (A1) 14, GenerarArchivoDiferenciaSaldoJob (DS) 14, GenerarArchivoSaldoCuentasJob (SC) 25 | 87 |
| `job/ReportesAntiguedadGoldenTest` | GenerarReporteAcreedoresJob (RA) 17, ...AntiguedadDeudaBolsaCTAJob (AB) 19, ...AntiguedadDeudaCTAJob (AC) 19, PautaGenerarReporteAcreedores (PR) 17, PautaGenerarReporteAntiguedadDeudaBolsaCTA (PB) 17 | 89 |
| `job/GenerarArchivoGoldenTest` | generarArchivo.GenerarArchivo (+ Archivo, FormatoArchivo, FormatoArchivoDetalle): structure (GA) 40, field formatter (GF) 51 | 91 |
| `job/LibroInventarioGoldenTest` | LibroInventarioJob (LI) | 16 |
| `runtime/JobRunnerContractTest`, `JobRunnerRevisionArquitecturaTest`, `Rule061SinModoDemonioTest` | the BcsJob contract, one run per program / class, RULE-061 | 21 |
| `security/Rule061SinSchedulerTest`, `Rule076SinMainDebugNiCredencialesTest`, `Rule077Sin3DesTest` | RULE-061 / 076 / 077 on the new packages | 17 |
| `web/JobCallControllerTest`, `TokenInternoJobCallTest`, `JobCallRevisionPaso5Test` | `POST /internal/job-call` and the `X-Internal-Token` filter (copies of the Tesoreria tests, same review fixes) | 32 |
| `wiring/ArranqueAplicacionTest` | the application starts with the real wiring, 16 jobs, 4 PAUTA beans, no pilot beans | 4 |
| `support/HarnessSelfCheckTest` | goldens complete, no secret / connection string, canonical comparison can fail, byte exact capture, JVM env (UTC, en-US, UTF-8, LF), random-name mask | 8 |

Every golden case compares, each as a separate assertion: outcome (COMMIT / ROLLBACK / ERROR), exception type and message, status listener calls, order of
physical commits and rollbacks, the dumped tables (committed state only), the files written (length, SHA-256, escaped text) and directories, the ordered trace
of what the job handed to each collaborator (service name and JSON input), and `RutaSalida`/`BAT.Programa` columns. PAUTA steps are not BcsJobs: they have the
facets `salida` (P_OUT_MENSAJE, P_OUT_ERRORESCONTADOR, P_OUT_MENSAJES) or `excepcion`, `colaboradores`, `tablas`.

## Target API defined by these tests

`cl.bcs.cuentainversion.jobs.runtime`: `CuentaInversionJob` (`clase()`, `transaction(JobContext)`, `onError`), `JobContext`, `JobRunner`, `JobResultado`,
`JobStatusListener`, `JobNoMigradoException`, `JobEnCursoException` (copied from BcsTesoreriaJobs) and `ProcesoPauta` (`clase()`,
`SalidaPauta transaction(Map<String,Object>)`) / `SalidaPauta` (public fields `P_OUT_MENSAJES`, `P_OUT_MENSAJE`, `P_OUT_ERRORESCONTADOR`). `web`:
`JobCallController`, `TokenInternoFilter(String)`. `job.port`: `ConfiguracionJobs.parametro(idEmpresa, modulo, nombre)` (null = not configured: the job throws
the legacy message `Parametro <n> en modulo <m> no encontrado`), `ProcedimientoArchivo`, `LibroInventario`.

Jobs (constructors are the contract; `clase()` = legacy FQN `cl.bcs.cuentaInversion.jobs.<Name>`, GenerarArchivo under `.generarArchivo`). Every service is called
through its JSON entry `execute(JsonObject)` with the typed `Entrada` of the Servicios class serialized by `Utils.getGsonBuilder()`:

| Class (package `job`) | Constructor |
|---|---|
| `ActualizarSaldoCargoAbonoJob` | `(ActualizarSaldoCargoAbonoService, JdbcTemplate)` |
| `ActualizarSaldosReporteAntiguedadJob`, `CargaInicialSaldosReporteAntiguedadJob` | `(<Service>)` |
| `BcsJobGenerarInformePatrimonioDiario`, `BcsJobGenerarInformeResumenCobro` | `(<Service>, ConfiguracionJobs, JdbcTemplate)` |
| `GenerarArchivoCargoAbonoJob`, `...ClientesPorPagarYCobrarJob`, `...CuentasPorCobrarAnexo16Job`, `GenerarArchivoDiferenciaSaldoJob` (ConsultaComparacionSaldoService), `GenerarArchivoSaldoCuentasJob`, `GenerarReporteAcreedoresJob`, `GenerarReporteAntiguedadDeudaBolsaCTAJob`, `GenerarReporteAntiguedadDeudaCTAJob` | `(<Service>, JdbcTemplate)` |
| `GuardarPatrimonioCajaJob` | `(GuardarPatrimonioCajaService, ConfiguracionJobs)` |
| `LibroInventarioJob` | `(LibroInventario)` |
| `GenerarArchivo` | `(ProcedimientoArchivo, JdbcTemplate, Clock)` |
| `job.pauta.PautaActualizarSaldoCargoAbono`, `PautaGuardarSaldosReporteAntiguedad` | `(ConfiguracionJobs, <Service>)` |
| `job.pauta.PautaGenerarReporteAcreedores`, `PautaGenerarReporteAntiguedadDeudaBolsaCTA` | `(ConfiguracionJobs, Colaboradores)`: `InscribirProcesoBatch` through `cl.bcs.plataforma.bat.InscribirProcesoBatch` |

How the contract is used: the case database is H2 `MODE=LEGACY` with the legacy SQL unmodified (translated by `support/H2Hana`); the services are Mockito mocks
(`support/ServiciosDeCaso`) that record the input under the LEGACY service name, run the scripted SQL on the transaction of the run
(`colaboradores.<Servicio>.sql`, so commit / rollback are visible in `TEST.Marca`), throw the scripted failure (`"<Servicio>.lanza"`) and answer
`colaboradores.<Servicio>.salida` (fields the legacy output class defaults take their default). Files are REAL, below a temp directory (`${RUTA_BASE}`).

## How the oracle runs, and how to re-record

`src/test/resources/cuenta-inversion-jobs/legacy-oracle/record.sh [casePrefix]` (JDK 25, h2 / gson / commons-lang3 from `~/.m2`, no network; give a short
`ORACLE_WORK` on Windows): compiles the WHOLE legacy jobs tree (23 classes), the real `BcsJob`, `BcsJobListener`, `BcsProcesoPauta`, `BcsPojo`, `Fecha`,
`StringUtils`, the Gson date adapters and the shims under `legacy-oracle/shims`; runs `OracleRunner` per case (H2 + `schema-jobs.sql` + `seed/base.sql` + the case
`seed.sql`, config `seed/config.json` + the case `config`), real `BcsJob.run()` (non-daemon branch) on a thin subclass whose only change is that `onError` keeps
the exception; PAUTA steps are called directly. The JVM runs with `-Duser.timezone=UTC -Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US` and
`-Dline.separator=LF` (the legacy ran on Linux; surefire gets the same through `src/test/surefire-jvm.args`, an argument file, the only way to pass a LF).
Writes `cases/<id>/expected.json`: never edit by hand. Legacy stack traces go to `$ORACLE_WORK/run/legacy-stderr.log`.

Shims (nothing under `legacy/` is edited): `Utils`, `SQL` (named parameters, list expansion, BigDecimal scale 16 in batches), `Log`, `JavaCall`,
`ConfiguracionSistema` (legacy missing-parameter message), `Diccionario`, `PlataformaUtils` (MensajeSalida, `concatenarMensajesSalida`), log4j, `Sql2o`,
`com.opencsv.CSVWriter` and one FAKE per called service (`cl/bcs/cuentaInversion/<Svc>.java`, 15, + `plataforma/bat/InscribirProcesoBatch`), GENERATED by
`tools/gen_shims.py` with the Entrada / Salida classes copied VERBATIM from the legacy services (so trace payloads and output defaults are the legacy ones).
The HANA stored procedures are a scripted `CallableStatement` (`runner/ScriptedProc`).

`tools/gen_all.py [AS|AR|...|GA|GF|LI]` regenerates every `case.json` / `seed.sql` (the input); to add a case: add it to a generator, run it, run
`record.sh <id>`, read the golden. `tools/gen_golden_tests.py`, `gen_skeleton_step1.py`, `gen_shims.py`, `gen_oraclejobs.py` are one-off generators of Step 1.
Dump hook: `-Dequivalence.out=<dir>` writes `<dir>/legacy/cuenta-inversion-jobs/<case>__<facet>.txt` and `<dir>/new/...` (`support/VolcadoEquivalencia`).

## Masks (the only normalisations; both sides apply the same function)

`yyyyMMdd_HHmmss` in file names, `RutaSalida` and file text -> `<TS>` (the generic generator names files with `new Date()`; the SHA-256 is taken after the mask);
the temp base path -> `${RUTA_BASE}`; OS text of an `IOException` -> `(<os-error>)`; a `NullPointerException` message -> `<npe>` (scripted NPEs with a message
are NOT used: the status message is compared as `<npe>`); identity hashes -> `@<hash>`; the PAUTA `NombreInternoArchivo` value (30 DISTINCT letters / digits,
legacy `generarNombreRandom`) -> `<RANDOM30>` only when it has exactly that shape; null JSON keys dropped; numbers by value; line ends and BOM exact.

## Covered / not covered

Covered: every parameter, default and branch of the 16 BcsJobs and 4 PAUTA steps, commit / rollback / error / java.lang.Error paths, `BAT.Programa` and
`BAT.TipoProgramaEjecucion` updates, partial work of a failing service, config fallbacks, parse errors in read order, boundaries (empty, null, 300 and 500 items),
the whole formatter (CHAR / NUM / DEC / DATE, padding, truncation, defaults, nulls), TXT / CSV / unknown formats, missing directories, collaborator traces.
Not covered: the debug `main()` and `debug` flags of every class (RULE-076); daemon mode (RULE-061); the internals of the 15 services (proven in
BcsCuentaInversionServicios, scripted here); real HANA (types, stored procedures, `ActualizarSaldoLibroInventario` itself); the Job Manager scheduling; the
ZIP / PDF / XLSX content (made by the services); a missing `PLA.RutaBaseArchivos` in the two report jobs (a HANA procedure: unknown failure).

## Legacy defects found (pinned as they are; list for the SME, "CIJ-DEF-nn")

| Id | Defect | Cases |
|---|---|---|
| 01 | A job that answers `false` (rollback) is reported to the Job Manager exactly like a commit (BcsJob.java:151: `status(id, false, "")`); the job and the framework both roll back, so a failed run cannot be told from a good one. Several jobs answer `false` for business errors (service error counter, no file name, any LibroInventario failure). | AS-03, AR-02, GP-06, GC-03, IP-02, LI-02/05 |
| 02 | `ActualizarSaldoCargoAbonoJob`: the `finally` commits after a `java.lang.Error` (the service work done before it is COMMITTED) and always frees the state row; the first UPDATE of the state row runs outside the `try`. | AS-07, AS-08 |
| 03 | Service error counters are ignored in the report jobs: IP/RC store the file name whatever `p_out_ErroresContador` says; RA/AB/AC store the first message and still end as a success, COMMITTING the partial work of the service; `GenerarArchivoSaldoCuentasJob` overwrites the counter with 0 (the error branch is unreachable). | IP-03, RA-02, AB-02, AC-02, SC-15 |
| 04 | `GenerarArchivoSaldoCuentasJob`: `getParam("IdEjecutivo") != "0"` compares references (the executive 0 is handed as 0, a missing one is a NumberFormatException); an empty `IdMoneda` is never turned into null; `.replace(".csv", ".zip")` changes every occurrence, case-sensitive, a null path is a NullPointerException; the Gson text of a bad `Encabezados` depends on the Gson version. | SC-02..07, SC-11..19 |
| 05 | `GenerarArchivoClientesPorPagarYCobrarJob` turns service errors into `java.lang.Error`: it skips `catch (Exception)`, BcsJob does not roll back (the driver does on close, assumption 6); the message is `tipo: descripcion` + two spaces per message ("null:" when there is no type). | CP-02..05, CP-15 |
| 06 | Report jobs: `Long.parseLong` of each parameter outside the `try`, in read order (the first bad one wins; IP and RC read in a different order); a date shorter than 10 characters is a StringIndexOutOfBoundsException; no default for missing optional values (nulls go to the service); RA/AB/AC with an empty answer and no messages fail with IndexOutOfBoundsException. | IP-08..13, IP-19, RC-19, RA-03 |
| 07 | `LibroInventarioJob`: every exception (also a bad `IdEjecucion`) is swallowed and answered as `false`; the class declares connection constants with a literal password and a `main()` that opens a HANA connection (RULE-076; the values are deliberately NOT reproduced anywhere). | LI-02..05, LI-08, LI-11 |
| 08 | `GenerarArchivo`: with `AgregoNombreColumna = S` the header REPLACES the first data row of every result set (data lost); in CSV a `TipoRegistroOptimus` that is not the first column gives ArrayIndexOutOfBounds (swallowed, empty file); a format that is not found is a NullPointerException (null Integer unboxed), not "NO EXISTE FORMATO"; the format SQL is built with `String.format` and the code (SQL injection shape: two formats match, the last wins). | GA-05, GA-06, GA-07, GA-18..20, GA-23 |
| 09 | `GenerarArchivo`: every failure inside the generation is swallowed and the job still returns true (COMMIT); files are not transactional (a failure leaves an empty or partial file); `Archivo.crear` creates ONE directory level and swallows the failure, then `BAT.Programa.RutaSalida` names a file that does not exist; a null `SeparadorCampos` prints the text `null`; an unknown or lower-case `FormatoSalida` writes an empty file with that extension; the TXT line end is `System.lineSeparator()` (OS dependent). | GA-10, GA-12..15, GA-27, GA-28 |
| 10 | Formatter: DEC decimals are LEFT padded (`1234.5` -> `1234.05`), a DEC without `.` becomes 0, integer digits above the width are not cut, a width / `Decimales` of 0 makes the field empty (format exception swallowed), NUM is not truncated, an inner space of a NUM becomes a zero, a null `RellenarCampo` or `ValorDefecto` (empty value) is swallowed as an empty field, dates use the lenient `SimpleDateFormat` (2025-13-45 -> 14/02/2026), an unknown or lower-case `TipoDato` prints nothing. | GF-10, GF-18, GF-21..24, GF-29, GF-30, GF-38, GF-44..48 |
| 11 | PAUTA steps: `(Long) parameters.get("IdEmpresa")` (ClassCastException for a text), `String.valueOf(null)` sends the text "null" as the `IdEmpresa` parameter of the enrolled program, `IdUsuario` is always 0, only the summary text (descriptions after a line break) is returned, `P_OUT_MENSAJES` stays empty; no step commits. | PS-07, PR-14, PR-15 |
| 12 | Output classes without defaults: `GenerarArchivoCargoAbono_Salida.P_OUT_ERRORESCONTADOR` and the platform `InscribirProcesoBatchSalida` fields are null unless the service sets them (NullPointerException in the job). | GC-09, GC-10, PS-10 |
| 13 | RULE-076 / 061: most classes have a debug `main()` and a `debug` flag with literal ids / dates, `running` flags (AtomicBoolean) and `pause()` stubs, and every `call()` of the jobs is a `return null` JavaCall stub. None is ported. | (RULE tests) |

## Assumptions (documented, none verified against a real HANA)

1. HANA schema INFERRED from the legacy SQL: `schema-jobs.sql` (`BAT.Programa` with `RutaSalida`, `RutaLog`, `Comentario`; `BAT.TipoProgramaEjecucion`; the two
   `ARC.ConsultarFormatoArchivo*` views as plain tables). The base seed gives program 900 and the type `ACTUALIZAR_SALDO_CARGO_ABONO` in state `LIBRE`.
2. The 15 services and `InscribirProcesoBatch` are scripted collaborators (JSON `execute(JsonObject)`); their Entrada / Salida shapes are the legacy ones; the
   proven modern services answer the same fields. A service's SQL effects are scripted (`TEST.Marca`).
3. opencsv: the legacy shipped 3.7 (`JOBMAN.gradle`, jar not in `~/.m2`); the shim reproduces the 3.x / 4.x rules (a field is quoted only when it contains the
   quote / escape char or a line break, a lone `;` is not, `close()` twice does not throw, lines end with LF). Step 2 needs a small writer with these rules
   (pin: GA-04, GA-05, GA-31, GA-33); the parent pom (5.9) is untouched.
4. JVM zone UTC, locale en-US, UTF-8 and LF line separator (the legacy ran on Linux).
5. `Utils.getConfiguracion(.., "PLA.RutaBaseArchivos")` = `PLATAFORMA/PLA.RutaBaseArchivos`; `ConfiguracionSistema` parameters come from the case config.
6. A `java.lang.Error` leaves BcsJob without a rollback call; the oracle records the rollback of the driver on close as one `rollback` event (the modern runner
   rolls back explicitly).
7. The PAUTA contract (`ProcesoPauta` / `SalidaPauta`) is new: the legacy `BcsProcesoPauta` receives `(Connection, Map)` from the BcsPlataformaJobs runner.

## Open questions for the user

1. How must the platform PAUTA runner reach the four PAUTA steps (the modern BcsPlataformaJobs has its own `ProcesoPauta`)? Here they are plain beans.
2. Which of CIJ-DEF-01..13 must be FIXED (all are pinned today)? Notably 02 (partial commit on Error), 03, 08..10.
3. Confirm opencsv 3.7 (assumption 3) and that the production file system is Linux (LF).
4. `ActualizarSaldoCargoAbono` is called through `execute(JsonObject)` (the real recalculation); its `call()` is the legacy stub that does nothing (Servicios README CTA-A-08).
5. Dockerfile / config of this module (Dockerfile, `application.yml` of the pilot, port, `HANA_*`, `INTERNAL_TOKEN`) are rewritten in Step 2.

## Obsolete pilot files: deleted

The 39 main files of the obsolete Step-0 pilot (`domain/`, `infrastructure/adapter/`, `job/*JobService`, the old `pauta/`, `port/out/`) and the pilot test
`CuentaInversionJobsTest` (27 cases) were deleted on the user's instruction 2026-10-21 (list in `TRANSFORMATION_NOTES.md`). The RULE-076 / 061 / 077 scans now
cover the whole module (`ModuloFuente.esNuevo` was removed).
