# BcsTesoreriaJobs: characterization tests (Step 1)

These tests define "done" for the rewrite of the legacy module
`legacy/otr-sva-optimus-corredores/BSCL/Tesoreria/JAVA/BcsTesoreriaJobs` (12 classes, 2897 lines) and of the job framework
`BSCL/Plataforma/JAVA/BcsJobManager/src/cl/bcs/scheduler/BcsJob.java`. The oracle is the legacy code itself: the goldens were
recorded by EXECUTING the unmodified legacy job classes under the real legacy `BcsJob.run()` (see "How the oracle runs").

Binding decisions (user, 2026-10-15/16): the jobs are IN-PROCESS (a library on top of the proven `tesoreria-service`, typed calls, no HTTP
between job and service), run by a `JobRunner` copied into this module with the BcsJob contract (one transaction per run: commit / rollback /
error, status, `RutaSalida`), entry `POST /internal/job-call` protected by the internal token; NO `@Scheduled`, NO daemon mode (RULE-061), no
debug `main()` and no credentials (RULE-076), no 3DES (RULE-077). Legacy behavior is reproduced bug for bug; the defects are listed below for the
domain expert (SME). Legacy exception class names are kept.

## State

Step 2 is done: every skeleton is implemented and all the new tests pass (see `TRANSFORMATION_NOTES.md` for the counts, the equivalence
comparison and the canaries). The old pilot tests (6 classes, 54 cases, 10 skipped) still pass untouched. Step 2 also added tests that are
not goldens: `adapter/ColaboradoresJobsTest`, `adapter/CabecerasCargaJdbcTest`, `adapter/ProcedimientoArchivoJdbcTest`,
`job/archivo/CsvLegacyWriterTest` and `wiring/ArranqueAplicacionTest` (the application context starts on H2 with the real wiring and
without the pilot classes).

## Test classes

| Class | What it pins | Cases |
|---|---|---|
| `job/CargaMasivaGoldenTest` | `BcsJobCargaMasivaIngresoEgresoDAPFFMM`: 6 guards, time window, outcome of the load, parameters, configuration, failures | CM-01..CM-68 |
| `job/GenerarArchivoCargosGoldenTest` | `GenerarArchivoCargos` (+ `Archivo`, `FormatoArchivo`): structure, FTP storage, failures (CG-nn); the field formatter (CG-Fnn) | CG-01..CG-45, CG-F01..CG-F42 |
| `job/GenerarArchivoPagoGoldenTest` | `GenerarArchivoPago` (+ `Archivo`, `FormatoArchivo`): format lookup, TXT / CSV / DAT, directories, nomina update (PG-nn); the formatter (PG-Fnn); BANCO_ESTADO through `GenerarArchivoPagos` (PG-Bnn); BCP telecredito (PG-Cnn) | PG-01..PG-45, PG-F01..PG-F46, PG-B01..PG-B24, PG-C01..PG-C08 |
| `job/ArchivoPagoDirectoGoldenTest` | `GenerarArchivoPagos` (PG-Enn) and `GenerarArchivoPagoBancoEstado` (PG-Dnn) called directly | PG-E01..10, PG-D01..10 |
| `job/ProcesarDistribucionGoldenTest` | `BcsJobProcesarDistribucion`: CSV with BOM, RutaSalida, two commits, failures | PD-01..PD-36 |
| `runtime/JobRunnerContractTest`, `JobRunnerRevisionArquitecturaTest` | the BcsJob contract (commit / rollback / error / status / params / RutaSalida / session statements / one run per program) | 17 |
| `runtime/Rule061SinModoDemonioTest`, `security/Rule061SinSchedulerTest` | RULE-061: one `transaction()` per call, no daemon flag, no scheduler anywhere | 6 |
| `security/Rule076SinMainDebugNiCredencialesTest` | RULE-076: only main() is the `@SpringBootApplication`; no debug connection, no credential literal | 5 |
| `security/Rule077Sin3DesTest` | RULE-077: no 3DES / OpenEJB cipher in bytecode, classpath or configuration | 10 |
| `web/JobCallControllerTest`, `web/TokenInternoJobCallTest` | `POST /internal/job-call` (200 / 500 / 503 / 400 / 409) and the `X-Internal-Token` filter | 26 |
| `support/HarnessSelfCheckTest`, `support/ColaboradoresSelfCheckTest` | the harness itself: goldens complete, no secret, canonical comparison can fail, byte exact file capture, the scripted collaborators replay the recorded traces | 7 |

Every golden case compares these facets, each as a separate assertion: outcome (COMMIT / ROLLBACK / ERROR), exception type and message, status
listener calls `(error, message)`, the order of physical commits and rollbacks, the dumped tables (committed state), the files written (length,
SHA-256 and escaped text, so BYTES) and the directories created, the ordered trace of what the job handed to each collaborator (service name and
JSON input), and the sequence values consumed.

Counts per job are printed by each class: `equivalence cases executed (<job>): N/M`.

## Target API defined by these tests

`cl.bcs.tesoreria.jobs.runtime`: `TesoreriaJob` (`clase()`, `transaction(JobContext)`, `onError`), `JobContext`, `JobRunner`,
`JobResultado`, `JobStatusListener`, `JobNoMigradoException`, `JobEnCursoException`: the same contract as BcsContabilidadJobs; Step 2 COPIES its
`JobRunner` (decision 2). `cl.bcs.tesoreria.jobs.web`: `JobCallController`, `TokenInternoFilter(String token)` (copy of the Servicios filter).

`cl.bcs.tesoreria.jobs.job` (constructors are the contract; collaborators are the proven `tesoreria-service` classes and ports):

| Class (`clase()` = legacy FQN) | Constructor |
|---|---|
| `BcsJobProcesarDistribucion` (`cl.bcs.tesoreria.BcsJobProcesarDistribucion`) | `(ProcesarDistribucionService, Colaboradores, ConfiguracionSistema, Reloj)` |
| `BcsJobCargaMasivaIngresoEgresoDAPFFMM` (`cl.bcs.tesoreria.cargaMasivaIngresoEgresoDAPFFMM.BcsJobCargaMasivaIngresoEgresoDAPFFMM`) | `(CargaMasivaIngresoEgresoDAPFFMMService, CabecerasCarga, Colaboradores, ConfiguracionSistema, DiccionarioMensajes, Reloj, JdbcTemplate)` |
| `GenerarArchivoCargos` (`cl.bcs.tesoreria.generarArchivoCargos.GenerarArchivoCargos`) | `(ConsultarFormatoArchivoService, ActivarMandatosArchivoCargaService, ProcedimientoArchivo, Secuencias, JdbcTemplate, Clock)` |
| `GenerarArchivoPago` (`cl.bcs.tesoreria.generarArchivoPago.GenerarArchivoPago`) | `(ProcedimientoArchivo, ConfiguracionSistema, JdbcTemplate, Clock)` |
| `GenerarArchivoPagos`, `GenerarArchivoPagoBancoEstado` (JavaCall helpers, not BcsJobs) | `(JdbcTemplate)`; `JsonObject call(JsonObject)` |

New ports (package `job.port`): `ProcedimientoArchivo` (one call = one `CallableStatement.execute()`; result sets as text), `CabecerasCarga`
(`getCabeceras` count), `ServiciosExternos` (the `Colaboradores` service names and their JSON). The service names the jobs pass to
`Colaboradores.invocar` are: `ValidarMandatosPAC` (input `{salida, idEmpresa, idUsuario}`), `ValidaEjecucionProcesoRecPAC`,
`ConsultarEstadoModulo`, `ObtenerDiaAnterior`, `AgregarLog`; the production `ColaboradoresJavaCall` does not route them yet (Step 2).

How the contract is used by the tests: the case database is H2 `MODE=LEGACY` with the legacy SQL unmodified (translated by `support/H2Hana`); the
services are Mockito mocks answering from the case (`support/ServiciosDeCaso`); `Colaboradores`, `ConfiguracionSistema`, `DiccionarioMensajes`,
`Reloj`, `Secuencias`, `CabecerasCarga`, `ProcedimientoArchivo` are scripted by `support/Puertos` and `ColaboradoresDeCaso`; the jobs write REAL
files below a temporary directory (`${RUTA_BASE}`), with `java.nio` (no file port); `Reloj` is the database UTC clock of the case; `Clock` is only
used for wall-clock file names (masked). The JVM runs with zone UTC, locale en-US and UTF-8 (surefire `argLine`).

## How the oracle runs, and how to re-record

`src/test/resources/tesoreria-jobs/legacy-oracle/record.sh [casePrefix]` (JDK 25, jars h2 / gson / commons-lang3 from `~/.m2`, no network):

1. compiles into a temporary directory (`$ORACLE_WORK`, give a SHORT path on Windows): the WHOLE legacy jobs tree (12 classes), the real legacy
   `BcsJob.java` and `BcsJobListener.java` (the framework class, not a copy), the pure utilities `Fecha`, `StringUtils`, `BcsPojo`, the Gson date
   adapters, and the oracle shims under `legacy-oracle/shims` (nothing under `legacy/` is ever written or edited);
2. runs `OracleRunner`: per case it builds H2 (`schema-jobs.sql`, `seed/base.sql`, the case `seed.sql`), a temporary directory, the configuration
   (`seed/config.json` plus the case `config`), and runs the REAL `BcsJob.run()` (non-daemon branch) of a thin subclass of the legacy job whose
   only change is that `onError(t)` keeps the exception. Autocommit is off; every commit and rollback is recorded; session statements become no-ops;
3. writes `cases/<id>/expected.json` (never edit it by hand; re-record).

Shims (all `legacy-oracle/shims`): `BcsJob` is real; `Utils`, `SQL` (named parameters, list expansion, BigDecimal scale 16 in batches, bean mapping
by public field), `Log`, `JavaCall`, `ConfiguracionSistema` (the legacy missing-parameter message), `Diccionario` (`"[CODE]{k=v}"`, keys sorted),
`FechaDB` (the legacy composition over a fixed database clock), `PlataformaUtils.MensajeSalida`, log4j, `Sql2o` are minimal and cite the legacy
lines. The services the jobs call are FAKES that record `{servicio, entrada}` (the input serialised with the legacy Gson) and answer what the case
scripts: `ProcesarDistribucion`, `ValidarMandatosPAC`, `ValidaEjecucionProcesoRecPAC`, `CargaMasivaIngresoEgresoDAPFFMM` (+ `getCabeceras`),
`ConsultarEstadoModulo`, `ObtenerDiaAnterior`, `ConsultarFormatoArchivo`, `ActivarMandatosArchivoCargo`, `AgregarLog`. The HANA stored procedures
(`CALL "<servicio>"(...)`) are a scripted `CallableStatement`: result sets of text, one trace entry per `execute()`. `GenerarArchivoPagos` /
`GenerarArchivoPagoBancoEstado` run through the typed `logica` with the `Archivo` allocated by reflection (see defect 18).

`tools/gen_all.py` regenerates every `case.json` / `seed.sql` (`py -3 gen_all.py [CM|CG|PG|PD]`); the cases are the input, the goldens the
recording. To add a case: add it to a generator, run it, run `record.sh <id>`, read the golden, and the test class picks it up by prefix.

The dump hook: `-Dequivalence.out=<dir>` writes `<dir>/legacy/tesoreria-jobs/<case>__<facet>.txt` and `<dir>/new/...` with the canonical text of every
comparison point (`support/VolcadoEquivalencia`, same contract as BcsTesoreriaServicios), for `scripts/compare.py`. The self-check tests never write.

## Masks (the only normalisations; both sides apply the same function)

* `yyyyMMdd_HHmmss` in file names, `RutaSalida` and file text -> `<TS>` (the legacy Cargos / Pago names use `new Date()`); the SHA-256 is taken
  after the mask. `RendicionPAC<yyyyMMddHHmmss>.csv` is NOT masked: it comes from the database clock, pinned by the case.
* the temporary base path -> `${RUTA_BASE}`; the OS text of an `IOException` -> `(<os-error>)`; backslashes in messages -> `/`.
* a `NullPointerException` message (it names local variables) -> `<npe>`, including its nested form inside a wrapping `Exception`.
* identity hashes -> `@<hash>`; null JSON keys are dropped; numbers compare by value; line ends and BOM are compared exactly (bytes).

## Covered / not covered

Covered: the five legacy classes that run (4 jobs + the 2 JavaCall helpers), every guard and branch of the mass load, every format type and boundary
of both formatters (padding, truncation, decimals, dates, null, defaults), empty and large lists (up to 300 rows), amounts with decimals and
different scales, TXT / CSV / DAT / unknown formats, CRLF / LF, BOM, accents, quotes and separators in CSV, commit / rollback / error paths, rollback
of the FTP and nomina writes, files left behind by failures, `RutaSalida`, collaborator traces and payloads, sequences, parameters and defaults.

Not covered (and why): the debug `main()` and `logica(con, debug = true)` of every legacy class (RULE-076: not ported); daemon mode (RULE-061);
the internals of the collaborator services (proven in BcsTesoreriaServicios; here they are scripted); a missing `PLA.RutaBaseArchivos` for the
distribution job (`Utils.getConfiguracion` is a HANA procedure: its failure is unknown); real HANA behavior (types, `TO_NVARCHAR`, sequences);
the Job Manager scheduling; the `FileManager` / FTP transfer of the generated files (the jobs only insert the FTP rows).

## Legacy defects found (pinned as they are; list for the SME, "TES-J-DEF-nn")

| Id | Defect | Cases |
|---|---|---|
| 01 | Mass load: the time window excludes both ends (`isAfter` / `isBefore`): at exactly the start or end time the job answers "fuera de horario". | CM-20, CM-23 |
| 02 | `GenerarArchivoCargos` executes its stored procedure TWICE (`getService` and again in `logica`): any side effect of the procedure happens twice. | every CG |
| 03 | `GenerarArchivoCargos`: a procedure without result sets makes `getService` return null and the next line throws NullPointerException; the "NO EXISTE DATA PARA GENERAR EL ARCHIVO" branch is unreachable. | CG-13 |
| 04 | Cargos formatter: a DEC value without a '.' is read as 0 (`1500` -> `0`); `ValorDefecto` is ignored for CHAR / NUM / DEC / DATE (it only applies to an unknown TipoDato); DEC integer digits above the width LOSE their leading digits; a null value is a NullPointerException; `RellenarCampo` must be exactly `S`. | CG-F05, CG-F13, CG-F19, CG-F21, CG-F25 |
| 05 | Pago formatter: the decimal part is LEFT padded with zeros (`1234.5` -> `1234.05`); a DEC without '.' becomes an empty field; every formatting error is swallowed and the field is written as `''` (the line keeps its separator); a field with no definition prints `''`. | PG-F21, PG-F24, PG-F30, PG-F44, PG-F46 |
| 06 | `GenerarArchivoPago`: the format constructor swallows every error (no row, SQL error): the failure surfaces later as a NullPointerException on `CantidadDeTiposDeRegistro` instead of "NO EXISTE FORMATO", wrapped twice in `Exception`. | PG-09, PG-24 |
| 07 | `GenerarArchivoPago` builds its format SQL with `String.format` and the request parameters (SQL injection shape): an `IdMoneda` of `X' OR '1'='1` selects a format. | PG-24, PG-25 |
| 08 | Files are not transactional: a failed run leaves a partial or empty file behind (`rollback` does not remove it). | CG-23, CG-37, PG-41, PG-B12 |
| 09 | `Archivo.crear()` creates only ONE directory level and swallows the failure; the job then updates `TES.NominaTransferencia.NombreArchivo` and `BAT.Programa.RutaSalida` with the name of a file that does not exist. | PG-27 |
| 10 | `TipoSaltoLinea` null makes `Archivo.escribir` swallow a NullPointerException: no line is written. An unknown `FormatoSalida` (XLS) writes an empty file and still reports success. | PG-03, PG-08 |
| 11 | CSV generic output: `TipoRegistroOptimus` must be the first column, otherwise `ArrayIndexOutOfBoundsException` (index -1). | PG-41 |
| 12 | BancoEstado: `MontoRemesa` is cut with `substring(0, length - 9)`, which assumes a DECIMAL scale of 8: other scales fail (`StringIndexOutOfBounds`), scale 9 leaves a trailing '.', a zero amount prints as `0E-8`; RUTs with dots fail in the RUT sum (`NumberFormatException`); the `CSBC` and `AVAP` payment types are unreachable (the `ITF` branch wins); a missing CONTRATO row prints `null` as ordenante. | PG-B12..PG-B17 |
| 13 | `GenerarArchivoPagoBancoEstado` (no job calls it): reads the column `BancoDelBeneficiario`, which the SQL does not return (bank column always empty), does not strip accents, prints the full amount, has the typo "Nuamero de Nomina". | PG-D01..PG-D10 |
| 14 | `BcsJobProcesarDistribucion`: the flag reset `ValidaEjecucionProcesoRecPAC` always uses `p_IdEmpresa = 1`; it runs AFTER the first commit, so a failure there leaves the CSV and `RutaSalida` committed but reports an error; the error counter of the message loop is computed and never used. | PD-12, PD-26, PD-29 |
| 15 | `BcsJobProcesarDistribucion`: a message with null `tipo` or `id` aborts the CSV half written (header only) after the mandates were already validated. | PD-14, PD-15 |
| 16 | Mass load: when the output folder does not exist (or the base path has no trailing slash, `base + relative` is concatenated without separator) the error file is NOT written and `RutaSalida` is set to the empty string; a null path from the service is stored as NULL. | CM-39, CM-61, CM-62 |
| 17 | Mass load: `CURRENT_DATE` of the database (UTC date) is the load date while the time window uses the LOCAL time: around midnight they are different days. Some guards (module closed, already loaded, no file) write no AgregarLog. A null / empty `TipoArchivoCarga` returns false (ROLLBACK) and the framework reports it as success (BcsJob defect, `status(id, false, "")`). | CM-27, CM-28, CM-29 |
| 18 | `GenerarArchivoPagos.call` / `GenerarArchivoPagoBancoEstado.call` deserialize `p_Archivo` (a class with `FileWriter` fields) with Gson: it fails on any JDK after 8. The oracle (JDK 25) therefore calls the typed `logica`; the production JavaCall of these two classes only works on the legacy Java 8. | PG-E*, PG-D* |
| 19 | A `false` return is reported to the Job Manager exactly like a commit (BcsJob.java:151); `main()` blocks open desa / UAT connections with a hard-coded debug path (RULE-076). | CM-28, CM-29 |

## Assumptions (all documented, none verified against a real HANA)

1. HANA schema INFERRED from the legacy SQL: `schema-jobs.sql` (column types of the views and tables; `MontoRemesa` is `NUMERIC(28,8)`, a case may
   `ALTER` the scale). The base seed gives the program row 900, the time window 08:00-20:00 and a processed previous-day PAUTA.
2. The legacy runtime shipped opencsv 3.x / 4.x (the legacy calls `new CSVWriter(w, ';')`, which opencsv 5.9 removed): a field is quoted only when it
   contains the quote / escape character or a line break; a separator alone is NOT quoted; `close()` on a closed `FileWriter` does not throw. With
   opencsv 5.9 (the target) `CSVWriter` with `NO_QUOTE_CHARACTER` ESCAPES `;` and line breaks (proven: a scratch reference implementation fails
   CM-41 exactly there) and `close()` throws. Cases that depend on it: CM-41, CG-02, CG-35, PG-05, PG-40. Step 2 needs a small writer with the 3.x rules,
   unless the owner confirms the legacy version.
3. `CURRENT_DATE` = the UTC date of the database clock; `FechaDB.ObtenerHoraSistemaLocal` = UTC time + `PLA.SegundosZonaHoraria`;
   `FechaDB.ObtenerFechaHoraTs` also reads `PLA.FechaSistema` (a missing one fails there); `Utils.getFechaSistema` = `PLA.FechaSistema`;
   `Utils.getConfiguracion("PLA.RutaBaseArchivos")` = `PLATAFORMA/PLA.RutaBaseArchivos` (the legacy goes through a HANA procedure).
4. Locale en-US and zone UTC for `DecimalFormat` / `SimpleDateFormat` (the legacy depends on the JVM defaults).
5. The services answer the complete legacy output object (absent fields take the legacy constructor defaults); `ConsultarFormatoArchivo` copies the
   request `IdEmpresa` / `IdUsuario` into the first format; `Diccionario` is the deterministic text `"[CODE]{k=v}"`.

## Open questions for the user (Step 1) and how Step 2 resolved them (user decisions, 2026-10-16)

1. Library jar: the Servicios pom now builds a plain jar plus `*-exec.jar`; the module uses a normal `cl.bcs:tesoreria-service` dependency.
2. opencsv: the legacy shipped 3.7 (`JOBMAN.gradle`); `job/archivo/CsvLegacyWriter` reproduces its behavior (the 3.7 jar is not in `~/.m2`); the
   parent pom is untouched.
3. The four collaborator names are routed by `adapter/ColaboradoresJobs` (platform JavaCall router), without editing the Servicios sources.
4. `CabecerasCarga` is implemented by `adapter/CabecerasCargaJdbc` with the same SQL file as the service's private `getCabeceras`.
5. None of the 19 defects is fixed (bug for bug); the SME still has to say which ones must be fixed.

## Obsolete pilot files (do NOT delete here; list for the cleanup script)

Main (20): `config/TesoreriaJobsConfiguration`, `formatter/ArchivoFieldFormatter`, `infrastructure/adapter/ModuloRestClient`,
`infrastructure/adapter/TesoreriaJobsRepository`, `job/CargaMasivaJob`, `job/GenerarArchivoCargosJob`, `job/GenerarArchivoPagoJob`,
`job/ProcesarDistribucionJob`, `model/CabeceraCargaRow`, `model/CargaMasivaJobInput`, `model/ContenidoArchivo`, `model/ControlRowTotals`,
`model/RangoHorarioRow`, `port/out/CabeceraCargaPort`, `port/out/ModuloStatePort`, `port/out/PautaEstadoPort`, `port/out/RangoHorarioPort`,
`service/AuditLogService`, `service/BancoEstadoPagoWriter`, `service/BancoEstadoPaymentWriter` (all under `src/main/java/cl/bcs/tesoreria/jobs/`);
`TesoreriaJobsApplication` is KEPT (the `@SpringBootApplication`, the only `main()`). Tests (6 + support): `ArchivoFieldFormatterTest`,
`AuditLogStateTest`, `BancoEstadoVariantsTest`, `CargaMasivaGuardTest`, `GenerarArchivoPagoTest`, `ProcesarDistribucionCsvTest`, the old
`src/test/java/cl/bcs/tesoreria/jobs/README.md` content (replaced by this file) and `src/test/resources/application-test.properties`. Module files to
rewrite in Step 2: `src/main/resources/application.properties` (Spring Batch / TomEE proxy settings), `README.md`, `TRANSFORMATION_NOTES.md`,
`Dockerfile`; the pom can drop `spring-boot-starter-batch` and `spring-batch-test` once the pilot is gone. While the pilot exists, the RULE-076 / 061 /
077 scans cover only the new packages (`security/ModuloFuente.esNuevo`); after the cleanup the filter changes nothing.
