# BcsAPVJobs: characterization tests (Step 1)

(The first-pass pilot and its `README.md` were deleted on the user's instruction, 2026-10-21.)

These tests define "done" for the in-process rewrite of the legacy module
`legacy/otr-sva-optimus-corredores/BSCL/APV/JAVA/BcsAPVJobs` (4 classes, 1,368 lines) and of the job framework
`BSCL/Plataforma/JAVA/BcsJobManager/src/cl/bcs/scheduler/BcsJob.java`. Nothing is implemented: `src/main` holds a TODO skeleton, so
every test that needs behavior FAILS with `UnsupportedOperationException("TODO Step 2: ...")` and nothing else.

## Binding decisions (user)

1. Jobs run IN-PROCESS: a library module on top of the proven `apv-service`, calling its typed use cases directly (no HTTP between job and
   service), executed with the BcsJob contract (one connection/transaction per run: commit / rollback / error, status, `RutaSalida`) by a
   `JobRunner`; entry `POST /internal/job-call`, protected by the internal token (`X-Internal-Token`, fails closed); no `@Scheduled`; no
   daemon mode (RULE-061); no debug `main()`, no credentials, no 3DES (RULE-076, RULE-077).
2. `JobRunner` is COPIED into this module (BcsUtils is signed): Step 2 copies `BcsContabilidadJobs .../runtime/JobRunner`.
3. Legacy behavior is reproduced bug-for-bug; defects are listed below for the domain expert (SME); the HANA schema is an inferred,
   documented assumption; legacy exception class names are kept (`StoredProcedureException` is a type of the skeleton).

## Target API (defined by these tests)

Package `cl.bcs.apv.jobs.runtime`: `ApvJob` (`clase()`, `transaction(JobContext)`, `onError`), `JobContext`, `JobStatusListener`, `JobResultado`,
`JobNoMigradoException`, `JobEnCursoException` (types, complete) and `JobRunner` (skeleton: `JobRunner(PlatformTransactionManager, JdbcTemplate,
List<ApvJob>, JobStatusListener[, List<String> sesion])`, `ejecutar(clase, idPrograma, parametros)`, `soporta`, `clases`).

Package `cl.bcs.apv.jobs.job` (`clase()` is the legacy FQN `cl.bcs.APV.jobs.<Name>`, the routing key):

| Job | Constructor | What it reaches |
|---|---|---|
| `BcsJobCargaMasivaPrevired` | `(CargarArchivoPrevired, ConfiguracionSistema, Reloj)` | typed use case of apv-service; `PLATAFORMA/PLA.RutaBaseArchivos`; the clock for the file stamp |
| `IngresarOrdenBonificacionFiscal` | `(NamedParameterJdbcTemplate, Colaboradores)` | `BAT.LogPrograma` insert on the job transaction; `CrearOrdenes` |
| `IngresarOrdenFMBonificacionFiscal` | `(NamedParameterJdbcTemplate, ConfiguracionSistema, Colaboradores)` | SQL of the legacy (copy it verbatim), config `APV/APV.PorcentajeBonificacionFiscal`, `PLATAFORMA/PLA.FechaSistema`; collaborators `ConsultarEmpresa`, `ConsultarFlujoCustodiaFondoMutuo`, `CrearSolicitudAporteRescateNormal` |
| `IngresarOrdenRVBonificacionFiscal` | same | SQL + the local temporary table `#p_Instrumentos`; `PLA.MonedaFormato` (CANTIDAD format: HALF_UP / FLOOR); collaborators `StoredProcedure` (`BSCL.Plataforma.BD.Servicios::INS.ConsultarInstrumentoPrecioBolsa`), `ConsultarFlujoCaja`, `ConsultarCustodiaPortafolio`, `CalcularMontoYCobrosOrden`, `CrearOrdenes`, `AgregarLog` |

`Colaboradores`, `ConfiguracionSistema` (null when not configured: the job raises the legacy exception), `Reloj` are the ports of `apv-service`.
Collaborator JSON uses the LEGACY field names of the legacy POJOs (same as the oracle trace). A scripted output is the legacy output JSON: an absent
list is the legacy empty list, an explicit `null` is the legacy null (NullPointerException when iterated).

Package `cl.bcs.apv.jobs.web`: `JobCallController(JobRunner)` handles `POST /internal/job-call` with `{"clase","idPrograma","parametros"}`:
200 COMMIT or ROLLBACK, 500 ERROR (`mensaje` = exception message), 503 class not run here, 409 already running, 400 malformed request, 401 no or
wrong token. `TokenInternoFilter(String token)` (same contract as BcsAPVServicios).

`BAT.LogPrograma` rows (legacy `AgregarLogPrograma`, a HANA procedure that INSERTs on the job connection) are pinned as TABLE STATE: the job may
insert directly with its `NamedParameterJdbcTemplate` (the log is rolled back with the job). `BAT.Programa.RutaSalida` likewise (`JobContext.updateRutaSalida`).

## What is compared (per case, `src/test/resources/apv-jobs/cases/<id>/`)

`case.json` (input: job, IdPrograma, job parameters, configuration, collaborator script, tables to dump), `seed.sql`, `expected.json`
(RECORDED by executing the unmodified legacy job through the unmodified legacy `BcsJob.run()`; never typed by hand):

| Point | Content |
|---|---|
| `resultado` | outcome COMMIT / ROLLBACK / ERROR, the ordered commit / rollback events of the connection (the job's own `con.commit()` included), the `status(id, error[, message])` calls, the return value, the exception class (message only when `pinMensaje`) |
| `tablas` | `BAT.Programa` (RutaSalida, stamp masked), `BAT.LogPrograma` |
| `colaboradores` | every call to a collaborator and its input payload, in order |
| `archivos` | the job files under the base folder: name (stamp masked) and exact content |

Equivalence is value based (`Canon`): object keys unordered, null-valued keys dropped, numbers by value (`1.50 == 1.5`), arrays ordered.
`-Dequivalence.out=<dir>` writes `<dir>/legacy|new/apv-jobs/<case>__<point>.txt` (`VolcadoEquivalencia`, the BcsAPVServicios design) for the
later dual-execution comparison.

## How the legacy was executed (the oracle)

`src/test/resources/apv-jobs/legacy-oracle/` (same design as BcsAPVServicios): `record.sh` compiles into a scratch folder the four UNMODIFIED legacy job
classes, the UNMODIFIED `BcsJob` and `BcsJobListener`, and the real `MonedaFormato`, `AgregarLogPrograma`, `BcsPojo` and the FondoMutuo POJOs; shims
replace what the legacy tree cannot provide (`Utils`, `SQL`, `Log`, `ConfiguracionSistema`, `PlataformaUtils`, log4j, Sql2o) and FAKE the collaborators of other
modules. The fakes are GENERATED by `tools/mkshims.py`, which copies the nested data classes VERBATIM from the legacy sources (nothing is retyped) and
records each call in `Hooks` while answering the scripted output of the case. `OracleRunner` runs each case: fresh H2 (MODE=LEGACY, `H2Hana` bridge:
`#temp` tables, `TO_NVARCHAR`, `TO_BIGINT`, `CURRENT_UTCTIMESTAMP`), a recording connection (commit / rollback / close), a real temporary base
folder, `job.setParameters(...)` + `job.run()`, JVM zone UTC, UTF-8.

## Re-record / add a case

```
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
cd modernized/otr-sva-optimus-corredores/BcsAPVJobs/src/test/resources/apv-jobs
python legacy-oracle/tools/mkcases.py .                  # writes case.json + seed.sql of every case from tools/casegen/c_*.py
ORACLE_WORK=<short scratch dir> bash legacy-oracle/record.sh [APVJ-RVB]   # records expected.json (needs the jars h2 2.4.240 and gson in ~/.m2)
python legacy-oracle/tools/mktests.py . ../../java/cl/bcs/apv/jobs/job     # regenerates the four *CasosTest classes
```
Knobs: `LEGACY_ROOT`, `ORACLE_SKIP_COMPILE=1`, `LEGACY_OVERLAY` + `ORACLE_OUT` (mutation check with a mutated copy of a legacy file),
`JAVA_OPTS=-Doracle.debug=true` (legacy stdout/stderr). `mkschema.py` regenerates `schema-apv-jobs.sql`.
Run: `mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :apv-jobs clean test` (surefire argLine: UTC, UTF-8, en_US).

## Masks (what is NOT compared, and why)

* The wall-clock stamp of the log file name (`yyyyMMddHHmmss`) and of `RutaSalida`: masked `<TS>` in the golden; the new code must stamp with the injected
  `Reloj` (fixed `2025-04-01T10:15:30Z`, formatted in the JVM zone = UTC), checked by `marcaDeTiempoEsLaHoraDeLaCorrida`.
* The JDK helpful NullPointerException text (`Cannot invoke ... is null`): `<NPE-MSG>`; absolute temp paths: `<BASE>`.
* Exception and status messages are compared only when the case sets `pinMensaje` (messages written by the legacy code or by a scripted collaborator);
  library messages (Gson, JDK, H2) are not a contract: the exception CLASS is.
* An `Error` (not an `Exception`) leaves the legacy connection un-rolled-back (closed only); `JobRunner` rolls back: a trailing `rollback` event is
  dropped on the new side for that case (RVB-039, CMP-025, OBF-006).
* `FechaLog` of `BAT.LogPrograma` (database clock) is not dumped.

## Legacy defects found (list for the SME; all REPRODUCED and pinned)

Framework (`BcsJob.java`): a `false` result rolls back but is reported as `status(id, false, "")` like a commit; an `Error` is not rolled back explicitly.
1. CMP-012: `new FileWriter(...)` runs BEFORE `mkdirs()`: with no `APV` folder under `PLA.RutaBaseArchivos` the job fails (the `mkdirs` is dead code).
2. CMP-023/024: any `Exception` of the Previred service is swallowed: rollback, a file with only the first line, but the job returns true (COMMIT, success status).
3. CMP-026: a null message list fails AFTER `RutaSalida` was updated; the rollback undoes it, the run still commits, no `FIN RESULTADO`.
4. CMP-007, CMP-005: `p_IdUsuario` null gives the name `..._null_...`; the file uses the JVM default charset (UTF-8 assumed here).
5. OBF-001: `IngresarOrdenBonificacionFiscal` sends a HARD-CODED test order ("PRUEBA APV V1": portfolio 20021, instrument 31, quantity 2, limit price
   406.26, user 5) every run; a failure is swallowed, the job commits three times (OBF-003..006); the error messages of `CrearOrdenes` are ignored (OBF-002).
6. FMB-002: `Monto*pct/100` is exact (no rounding): up to 10 decimals reach the fund request.
7. FMB-012/009: the custody cap compares only `NumeroCuotas`: a by-amount rescue is never capped; equal quantity is not capped; FMB-013 lines apply in sequence.
8. FMB-020: the same `InInstrumento` object is added for every series: all entries show the last `IdInstrumento`.
9. FMB-027: the custody flow is requested even with no request; FMB-017: `IdMonedaPago` is not set for non-RESNORM; `EsPorMontoLiquido` is always `N`.
10. FMB-044: collaborator errors are only logged ("ERRORES FFMM"); the job commits; FMB-034: a missing `IdEmpresa` becomes company 0.
11. RVB-003: the cash check compares the saldo with the PRICE OF ONE SHARE, not the amount: the whole saldo becomes the order amount (RVB-004).
12. RVB-010: the saldo variable is not reset per order: an order without cash flow reuses the previous order's saldo.
13. RVB-005/019/022: a `return true` inside the loop (insufficient saldo, VENTA without custody) discards ALL orders, including earlier ones, and commits.
14. RVB-020: only the first custody row is read; RVB-013: the last price row wins; RVB-011/014: no price gives a null price / NullPointerException.
15. RVB-023/024: `AccionOrden` is tested ignoring case first and case-sensitively later: "compra" gets no amount; an unknown action gets the defaults.
16. RVB-033: every `CrearOrdenes` message (ERROR, INFORMACION ...) is logged as "ADVERTENCIAS RV"; RVB-045: `PLA.MonedaSistema` is read but unused.
17. RVB-002: `RoundingMode.UP` at the scale of the amount; `MonedaFormato` caches the format table in a static for the life of the JVM (not testable here).

## Assumptions (to be reviewed by a person)

* The H2 schema (`schema-apv-jobs.sql`) takes column names and types from the legacy DDL scripts (`*_Create_Tables.sql`); NOT NULL, defaults and keys are dropped.
  Columns the legacy SQL reads that those scripts do not declare were ADDED (`RutaSalida`, `AccionOrden`, `EsPorMonto` ... see `mkschema.py` EXTRAS); the view
  `ConsultarPortafolio` and the table `POR.MatrizAgrupadorAPV` have no DDL and are modelled with the columns the SQL reads. Decimal scale (`DECIMAL(26,8)`) matters for `RoundingMode.UP`.
* H2 commits an open transaction on DDL: the `DROP TABLE #p_Instrumentos` of the RV job commits earlier writes (HANA runs with `AUTOCOMMIT DDL OFF`). In RVB failure cases a
  `BAT.LogPrograma` row written before the failure therefore SURVIVES in the golden; the new code on H2 behaves the same. The HANA rollback semantics are not shown.
* The Previred service, `CrearOrdenes`, `CalcularMontoYCobrosOrden`, `ConsultarCustodiaPortafolio`, `ConsultarFlujoCaja`, `ConsultarEmpresa`, the fund collaborators and the
  price procedure are scripted doubles; their own behavior is outside this proof. `PLA.Configuracion` is a map per case (the legacy cache lookups are not reproduced:
  a company without rows raises a NullPointerException in the legacy).
* No APV program type is declared a daemon in the repository (the agenda is outside it).

## Not covered

HANA procedure bodies; real file-system permissions and disk-full; concurrency (`JobEnCursoException` is pinned by the JobRunner tests only); the legacy `main()` / `debug`
paths (not ported on purpose, RULE-076); `AgregarLogPrograma` failing; `PLA.RutaBaseArchivos` empty (it would write at the drive root).

## Secrets

No credential of the legacy code is a fixture. The token of the web tests is the fake `fake-internal-token-0000`; `HarnessSelfCheckTest` scans every case and golden.

## Cases and classes

| Legacy job | Test class (generated) | Cases |
|---|---|---|
| `BcsJobCargaMasivaPrevired` | `job/BcsJobCargaMasivaPreviredCasosTest` | 30 (`APVJ-CMP-001..030`) |
| `IngresarOrdenBonificacionFiscal` | `job/IngresarOrdenBonificacionFiscalCasosTest` | 10 (`APVJ-OBF-001..010`) |
| `IngresarOrdenFMBonificacionFiscal` | `job/IngresarOrdenFMBonificacionFiscalCasosTest` | 50 (`APVJ-FMB-001..050`) |
| `IngresarOrdenRVBonificacionFiscal` | `job/IngresarOrdenRVBonificacionFiscalCasosTest` | 50 (`APVJ-RVB-001..050`) |

Hand-written: `runtime/JobRunnerContractTest` (APVJ-RUN-01..09c: commit, rollback, error, job-side commit/rollback, `updateRutaSalida`, parameters, unregistered class,
duplicate class), `runtime/JobRunnerRevisionArquitecturaTest` (session statements, intermediate commit failure, concurrent runs), `runtime/Rule061SinModoDemonioTest`
(RULE-061), `security/Rule076SinMainDebugNiCredencialesTest` (RULE-076), `security/Rule077Sin3DesTest` (RULE-077), `web/JobCallControllerTest` (APVJ-WEB-01..10, token included),
`job/HarnessSelfCheckTest` and `job/InventarioCasosTest` (the harness and the inventory). The security tests scan all of `src/main` (the pilot is gone, nothing is skipped).

## State against the Step 2 stubs

`JobRunner`, the four jobs, `JobCallController` and `TokenInternoFilter` throw `UnsupportedOperationException("TODO Step 2: ...")`, so every test that executes behavior is RED by design:
that red list is the Step 2 fix list. Nothing is skipped or disabled. Each case class prints `equivalence cases executed (<job>): N`.

HARNESS PROOF (scratch, not delivered): with a copy of `BcsContabilidadJobs` `JobRunner` and a 60-line implementation of the Previred job over the adapter mapper
`CargarArchivoPreviredMapeo`, all 30 CMP cases, `JobRunnerContractTest` (11) and `Rule061SinModoDemonioTest` (4) were GREEN, and the dump hook wrote identical legacy / new files.
The RV, FM and OBF cases have not been run against an implementation.

## Pilot files made obsolete by Step 2 (historical: ALL of them were DELETED on the user's instruction, 2026-10-21)

Main (`src/main/java/cl/bcs/apv/jobs/`): `domain/*` (8), `infrastructure/adapter/ApvJobsRepository`, `infrastructure/client/*` (5: the REST clients and `TomEeClientStubs`),
`job/CargaMasivaPreviredJob`, `job/IngresarOrdenBonificacionFiscalJob`, `job/IngresarOrdenFMBonificacionFiscalJob`, `job/IngresarOrdenRVBonificacionFiscalJob`, `port/out/*` (11);
`ApvJobsApplication` stays (single `main`, to be re-wired); `application.yml` loses the `services:` URLs. Tests: `cl/bcs/apv/jobs/CargaMasivaPreviredJobTest`,
`FMBonificacionFiscalTest`, `RVBonificacionFiscalTest` (24 tests, hand-inlined oracles) and this folder's old `README.md`; also the module `README.md` and `TRANSFORMATION_NOTES.md`.

Maven result of this state: Tests run: 227, Failures: 1, Errors: 178, Skipped: 1 (the skip is the pilot @Disabled); every failure or error is a TODO Step 2 skeleton (the one Failure expects IllegalStateException from the JobRunner constructor). 24 pilot tests still pass.
