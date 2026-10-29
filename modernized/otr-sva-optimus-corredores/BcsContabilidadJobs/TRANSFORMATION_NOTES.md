# BcsContabilidadJobs: transformation notes

| Item | Detail |
|---|---|
| Legacy | `legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadJobs` (8 files) and the job framework `BSCL/Plataforma/JAVA/BcsJobManager/src/cl/bcs/scheduler/BcsJob.java` |
| Target | Spring Boot 4.1.1 / Java 25 |
| Definition of done | The Step 1 characterization tests (commit `b2b4294`) plus the decision-5 tests below |
| Architecture | Decision 2026-10-15: in-process jobs on BcsContabilidadServicios as a library, one transaction per run with the BcsJob contract (`JobRunner`), `POST /internal/job-call`, no `@Scheduled` |

> **Equivalence is mostly trace-based: the legacy jobs were not executable here.** They need the TomEE Job Manager
> (`BcsJob` + Quartz). The exception is the InterfazContable job shell, whose 12 golden cases (CONT-B-40..50) were
> recorded by running the unmodified legacy class through `BcsContabilidadServicios/src/test/resources/envio/legacy-oracle`
> (`OracleRunner` + `BcsJob` shim, H2 and a fake SAP Service Layer).

## Proof metrics (Step 3)

| Metric | Value |
|---|---|
| Command | `mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl BcsContabilidadJobs -am clean test` (JDK 25) |
| Tests | **Jobs 199/199** (189 characterization, 6 from the architecture review, 4 for RULE-061), 0 failures, 0 skipped; in the same reactor BcsUtils 61/61 and BcsContabilidadServicios 1315/1315 |
| Equivalence cases executed | **12/12** legacy goldens (InterfazContable job shell, CONT-J-77), plus 189 characterization cases |
| Legacy vs. new (`compare.py`) | **66/66 same**: the 11 goldens CONT-B-40..50 with 6 outputs each (outcome and commits, run-log bytes, PreComprobante Estado, SAP request sequence, every SAP request body canonicalised, state-procedure calls), in `analysis/otr-sva-optimus-corredores/equivalence/BcsContabilidadJobs/{cases.json,EQUIVALENCE.json,dev/}`. This is the CONT-J-77 comparison exported to files and extended with the request bodies and procedure calls (review 2026-10-16, H1/M1); the new side uses the production factory `InterfazContable.servicioConBitacora` (M3) |
| Legacy retired | 8 files, **1,518 lines** (`BSCL/Contabilidad/JAVA/BcsContabilidadJobs/src`) |
| New code | 17 main files (1,317 lines) |

### Canaries

Each canary ran on a scratch copy of the reactor (BcsUtils, Servicios, Jobs), with one line broken and the covering
test classes run. Evidence (diff, log, surefire XML) is in
`analysis/otr-sva-optimus-corredores/equivalence/canary/BcsContabilidadJobs-transform/<n>-<area>/`.

| # | Break | Red tests |
|---|---|---|
| 1 | `CentralizaMultiSeleccionJob.java:49` param `"fechaConsulta"` → `"FechaConsulta"` | 2 / 24 |
| 2 | `CreaInterfazDiaria.java:55` `minusDays(1)` → `minusDays(2)` | 6 / 30 |
| 3 | `CreaInterfazDemanda.java:54` `"APPS/GAB1/"` → `"APPS/GAB2/"` | 28 / 33 |
| 4 | `InterfazContable.java:99` log name `_InterfazContable.log` → `.txt` | 20 / 45 |
| 5 | `GenerarInformeClienteRelacionadoBatch.java:40` default IdUsuario `97L` → `1L` | 1 / 13 |
| 6 | `JobRunner.java:89` a `false` result commits instead of rolling back | 2 / 11 |
| 7 | `JobCallController.java:87` 503 → 404 for an unmigrated class | 5 / 19 |
| 8 | `JobRunner.java:129` runs `transaction()` twice per call (a daemon-like re-run) | 2 / 4 (`Rule061SinModoDemonioTest`) |

`Canary: 8 breaks → 66 tests failed (all detected)`. The weakest pin is GenerarInforme's default user (1 test).

## Mapping (legacy → target)

Legacy paths are relative to `legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadJobs/src/cl/bcs/contabilidad/`.
Target paths are relative to `src/main/java/cl/bcs/contabilidad/jobs/`.

| Behavior | Legacy (file:lines) | Target (file:lines) | Tests |
|---|---|---|---|
| Job contract: one connection, commit / rollback / error, status, `RutaSalida` | `Plataforma/.../BcsJobManager/src/cl/bcs/scheduler/BcsJob.java:86-185` | `runtime/JobRunner.java:1-184`, `runtime/JobContext.java:1-34`, `runtime/ContabilidadJob.java:1-24` | `JobRunnerContractTest` |
| Multi-selection centralization job | `jobs/CentralizaMultiSeleccionJob.java:1-141` (params :59-65, call and commit :89-92, rollback :96-98) | `job/CentralizaMultiSeleccionJob.java:1-62` | `CentralizaMultiSeleccionJobTest` |
| Daily interface (yesterday, 13 files, SFTP, log) | `jobs/CreaInterfazDiaria.java:1-290` | `job/CreaInterfazDiaria.java:1-73`, `job/InterfazSigaJob.java:1-112`, `job/LogServiceBroad.java:1-45`, `job/ConexionSap.java:1-18`, plus the Servicios generators | `CreaInterfazDiariaTest`, `CreaInterfazDiariaBitacoraTest` |
| On-demand interface (`FechaEjecucion`, `APPS/GAB1/`, 5 files) | `jobs/CreaInterfazDemanda.java:1-329` | `job/CreaInterfazDemanda.java:1-170`, `job/InterfazSigaJob.java` | `CreaInterfazDemandaTest`, `CreaInterfazDemandaBitacoraTest` |
| Accounting interface to SAP (job shell, log, `RutaSalida`, commit per voucher) | `jobs/InterfazContable.java:1-276`, `jobs/CallerWSJournalVoucher.java:1-327`, `jobs/Respuesta.java:1-20`, `pojo/LoginSAP.java:1-48` | `job/InterfazContable.java:1-182`, plus Servicios `InterfazContableService` and `SapB1Gateway` | `InterfazContableJobTest`, `InterfazContableJobGoldenTest`, `InterfazContableFallaParcialTest` |
| Related-client report batch | `jobs/GenerarInformeClienteRelacionadoBatch.java:1-87` | `job/GenerarInformeClienteRelacionadoBatch.java:1-64` | `GenerarInformeClienteRelacionadoBatchTest` |
| Job entry point (was Quartz/JobManager) | `BcsJob.setParameters` / `run` | `web/JobCallController.java:1-108` (`POST /internal/job-call`) | `JobCallControllerTest` |
| One `transaction()` per call, no daemon loop (RULE-061, decision 2026-10-16) | `BcsJob.java:90-125` (daemon branch) | `runtime/JobRunner.java` (non-daemon contract only), `web/JobCallController.java` (no daemon or delay field) | `Rule061SinModoDemonioTest` |
| No debug `main()`, no credentials, no 3DES (RULE-076/077) | every `main()` (for example `CentralizaMultiSeleccionJob.java:122-139`), Job Manager datasource cipher | (absent) | `Rule076SinMainDebugNiCredencialesTest`, `Rule077Sin3DesTest` |

## Side by side (CentralizaMultiSeleccionJob: parameters and outcome)

Legacy `jobs/CentralizaMultiSeleccionJob.java:59-65, 89-92` (left) vs. `job/CentralizaMultiSeleccionJob.java:43-53`
(right). Neither range holds credentials.

```text
idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));         |  long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
idUsuario = Long.parseLong(this.getParam("IdUsuario"));         |  long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
idModulo = this.getParam("IdModulo");                           |  String idModulo = ctx.getParam("IdModulo");
fechaContable = this.getParam("FechaContable");                 |  String fechaContable = ctx.getParam("FechaContable");
ejercicio = Long.parseLong(this.getParam("Ejercicio"));         |  long ejercicio = Long.parseLong(ctx.getParam("Ejercicio"));
idsTipoCentralizacion = this.getParam("IdsTipoCentralizacion"); |  List<TipoSeleccionado> tipos = tipos(ctx.getParam("IdsTipoCentralizacion"));
fechaConsulta = this.getParam("fechaConsulta");                 |  String fechaConsulta = ctx.getParam("fechaConsulta");
salida = centralizaMultiSeleccion.logica(con, entrada…);        |  servicio.execute(GSON.toJsonTree(entrada).getAsJsonObject());
con.commit();                                                   |  (JobRunner commits: transaction() returned true)
return true;                                                    |  return true;
```

What stays the same: the parameter names (including the lower-case `fechaConsulta`), the parsing, and the commit
regardless of the service's error counter (CONT-J-16, a legacy defect kept). What changes: the commit belongs to
`JobRunner` (the BcsJob contract) instead of the job, and the service is called in-process through its JavaCall
`execute`, so the Servicios rules (B-03, NESTED persistence) apply unchanged.

## Structure

| Package | Content |
|---|---|
| `runtime` | `ContabilidadJob`, `JobContext`, `JobRunner` (BcsJob.run, non-daemon branch), `JobResultado`, `JobStatusListener`, `JobNoMigradoException` |
| `job` | The five jobs; `InterfazSigaJob` (what Diaria and Demanda share); `LogServiceBroad` (legacy `cl.bcs.contabilidad.Log`); `ConexionSap` |
| `web` | `JobCallController` |
| `config` | `ServiciosContabilidadConfiguration` (scan of Servicios without its web layer and its daily job), `JobsConfiguration` (explicit wiring) |

The old first-pass code (REST clients to Servicios, a duplicate SAP B1 client and config, `*JobService`, ports and
domain copies) was deleted.

## BcsContabilidadServicios changes (no behavior change; Servicios suite 1315/1315)

1. `spring-boot-maven-plugin` `<classifier>exec</classifier>`: the main artifact is a plain jar usable as a library;
   the Dockerfile copies `*-exec.jar`.
2. Interface generators (`infrastructure/interfaz`): a file-name parameter (`ArchivosInterfaz`) on
   `BroadCuentasGenerator`, `BroadMovimientoGenerator`, `BroadSaldoGenerator`, `CuentasBpcGenerator` and
   `MovimientoBpcGenerator`; the control file names the given data file. The two-argument constructors keep the
   CreaInterfazDiaria names. Demanda passes its own names; nothing is staged or renamed.
3. Run-log hook for decision 5 (i): `InterfazBitacora` in `InterfazContext` (default `NINGUNA`). The generators
   report the legacy `Crea*` lines ("Creando Archivo: X", "Fin de creacion de Archivo: X", "Fin de creacion de
   ArchivoX" for four control files, "ERR - msg" for a swallowed write failure or a failed Descriptivas file).
4. Streaming hook for decision 5 (iii): `InterfazContableService` has an optional `Consumer<String>` that receives
   each run-log line when it is produced. The four-argument constructor (the bean's) passes a no-op.

Hooks 3 and 4 go beyond the two changes listed in decision 3. Points 5 (i) and 5 (iii) cannot be met without them:
the generator lines and the InterfazContable lines are produced inside Servicios. Both hooks are additive and their
default is a no-op.

## Decision 5: "match the legacy" (tests added first, `legacy file:line` in each)

| Test | Pins |
|---|---|
| CONT-J-38 `CreaInterfazDiariaBitacoraTest` | SAP connection fails: log `INF - Estableciendo conexion a SAP`, `ERR - <msg>`; no file, no SFTP; COMMIT (CreaInterfazDiaria.java:95, :109, :111-113, :229-232, :264-274) |
| CONT-J-39 | Full Diaria log: job lines plus every generator line, exact text and order (CreaBroadCuentas.java:83/130/153/201 ... CreaMovimientoBPC.java:247) |
| CONT-J-39b | Descriptivas query fails: `Creando Archivo` THEN `ERR - <msg>` from the generator's own catch (CreaDescriptivasBroad.java:137-174) |
| CONT-J-39c | Broad query fails before `Creando Archivo` (CreaBroadCuentas.java:76 vs :83) |
| CONT-J-56 `CreaInterfazDemandaBitacoraTest` | SAP connection fails: GAB1 emptied, `ERR - <first stack frame>`; no file, no SFTP; COMMIT (CreaInterfazDemanda.java:80-84, :110, :112-114, :253-257) |
| CONT-J-57 | Full Demanda log with the Demanda names, then "Fin de generacion" and "Subiendo" (:120-217) |
| CONT-J-57b | A step's `ERR - <label>: ...` between the neighbouring families (:146-150) |
| CONT-J-79 `InterfazContableFallaParcialTest` | 'E' batch fails: ERROR, the file holds every line, voucher 100 stays committed (InterfazContable.java:123, :160, :169, :175, :244) |
| CONT-J-79b | Login fails, then the homologation query fails: the file holds the connection-error line (:105-113, :116) |
| CONT-J-79c | Login ok, homologation query fails: no file (:116) |

Canary run: with the generator lines suppressed, CONT-J-39/39b/39c/57/57b fail; without the connection step,
CONT-J-38/56 fail; without streaming, CONT-J-79/79b fail.

## Open questions, decided from the legacy source

* **Q5.** InterfazContable reads `PLA.RutaBaseArchivos` with `Utils.getConfiguracion(conn, schema, idEmpresa, ...)`
  (InterfazContable.java:85). That calls `BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion` (BcsUtils
  Utils.java:131-138), which reads `"BSCL.Plataforma.BD::PLA.Configuracion"` (Plataforma/BD/Utilitarios/
  ObtenerConfiguracion.sql). That is the module "PLATAFORMA" table (`ConsultarConfiguracionPlataforma.sql` reads the
  same table), for the IdEmpresa parameter. A missing parameter signalled SQL error 10007 in the legacy; here the
  lookup returns null and the job fails (CONT-J-71).
* **Q7.** `/internal/job-call` answers 500 for an ERROR outcome (CONT-J-92 and CONT-J-94 require it).

## Legacy defects kept

* CONT-J-02: a `false` result rolls back but is reported as `status(id, false, "")`, like a commit; HTTP 200.
* CONT-J-16: CentralizaMultiSeleccionJob ignores the service output and commits even with `P_OUT_ERRORESCONTADOR > 0`.
* CONT-J-84: GenerarInformeClienteRelacionadoBatch reports errors with a null message (legacy `throw new Throwable()`).
* CONT-J-73/78: InterfazContable commits each accepted voucher at once, so a later failure leaves a partial batch;
  the 'E' marks are rolled back.
* CONT-J-75: InterfazContable always returns true, even when every voucher failed or the SAP login failed.
* CONT-J-21: CreaInterfazDiaria ignores the `FechaEjecucion` parameter.
* CONT-J-11: the consultation date is read from `fechaConsulta` (lower-case f).
* CONT-J-46: the label "Movimiento BCP" keeps its typo; four control files log "Fin de creacion de Archivo" without
  ": ".
* CONT-J-38/56: a SAP connection failure is logged and the run still commits.

## Deviations from the legacy

| Id | Deviation | Status |
|---|---|---|
| DEV-J-01 | The SFTP credentials file is replaced by `SFTP_*` environment settings; missing or invalid settings return false (ROLLBACK), as a missing file did | Approved |
| DEV-J-02 | An empty `PLA.RutaBaseArchivos` gives ROLLBACK in Diaria and Demanda (legacy wrote under a literal `null` prefix) | Approved |
| DEV-J-03 | Demanda: a missing or invalid `FechaEjecucion` gives one `ERR - ` line and no generation | Approved |
| CONT-J-35b | `SFTP_KNOWN_HOSTS` is required; the host key is verified | Approved |
| CONT-J-77 | B1SESSION cookie sent; the SAP password never logged | Approved |
| - | A controlled error instead of a crash (null dates or amounts in a SAP row name the field) | Approved (module) |
| - | The SAP connection step takes and releases one pooled connection instead of holding one for the run | Implementation detail |
| - | InterfazContable: a run-log write failure ends the run in ERROR after the service returns (legacy threw at the write) | Minor, documented |
| - | Demanda: an exception without a stack trace logs `toString()` where the legacy would have failed on `getStackTrace()[0]` | Minor, documented |

## Not migrated

* The `main()` debug entry points, the `debug` switches and their hard-coded inputs (RULE-076).
* `CallerWSJournalVoucher`, `Respuesta`, `LoginSAP`: replaced by Servicios' `SapB1Gateway`.
* `JavaCall.call()` of CreaInterfazDiaria/Demanda (returned null) and `pause()` (empty); the `running` flag of
  GenerarInformeClienteRelacionadoBatch (never read).
* The daemon branch of BcsJob (RULE-061). Decision (user, 2026-10-16): none of the five Contabilidad jobs is a
  daemon; the repository configures daemon agendas only for Orden jobs (`BSCL/Orden/BD/Scripts/ConfiguracionJobs/*`)
  and `InsertTipoProgramaSIGA.sql` registers Diaria/Demanda without one. Pinned by `Rule061SinModoDemonioTest`. If a
  program is ever scheduled as a daemon, the Job Manager (Phase 5) owns the cycle and calls `/internal/job-call` once
  per cycle. The daemon heartbeat `jobListener.updateDaemon(jobId)` (BcsJob.java:94) goes with it.
* The 3DES-encrypted Job Manager datasource (RULE-077).
* The status listener only logs; the caller gets the outcome in the HTTP response.

## Deployment prerequisites (open, owner action)

1. **Cut-over of the legacy Job Manager.** Until Phase 5 the TomEE Job Manager keeps scheduling these programs.
   Before any of them runs here, each program type must be switched, one at a time, so that the legacy class no longer
   runs and the Job Manager (or a thin legacy proxy) calls `POST /internal/job-call`. Running both risks double
   execution: SAP vouchers sent twice, and interface files uploaded twice.
2. **Network:** `/internal/job-call` has no authentication; keep it on the internal network and protect it with a
   service credential (same finding as ARQ-H5 in Servicios).
3. **Environment:** `HANA_*`, `SAP_B1_URL/USER/PASSWORD/COMPANY`, `SAP_B1_DB_*`, `SFTP_HOST/PORT/USER/PASSWORD/KNOWN_HOSTS`
   and `PLATAFORMA_BASE_URL`. `SAP_B1_PASS` and `SFTP_USERNAME` from the first pass are gone.
4. **Single active instance per job class.** The in-flight guard (ARQ-J-H3) is per JVM. Run one replica, or claim
   the `BAT.Programa` row in the database before scaling out. Callers must not retry `/internal/job-call` on a
   timeout; a 409 means a run is already in progress.
5. **Volumes:** mount `PLA.RutaBaseArchivos` (InterfazContable log, report files, the GAB1 tree, the Diaria files)
   on storage the platform UI and TomEE can read, and mount the file named by `SFTP_KNOWN_HOSTS`. Without the
   known_hosts file every SFTP upload fails host-key verification; the run still commits (legacy catch semantics),
   so the HTTP result is 200 COMMIT with nothing uploaded.
6. **Phase 1 entry criteria** still open from the brief: credential rotation (RULE-076/077), the API Gateway deployment
   and the API consumer strategy (user decision 2026-10-14: deployment prerequisites).

## Architecture review (Step 5, 2026-10-16)

Reviewer: `architecture-critic` subagent. It booted the production context by hand (one DataSource, one transaction
manager, only `/internal/job-call`, no Servicios web or batch beans) and confirmed the Servicios changes have no
behavior change. Findings: 3 HIGH, 6 MEDIUM, 10 LOW.

### HIGH (all fixed)

| Id | Finding | Fix |
|---|---|---|
| ARQ-J-H1 | `JobRunner` dropped the BcsJob session setup `SET SCHEMA BSCL` / `SET TRANSACTION AUTOCOMMIT DDL OFF` (BcsJob.java:130-132). HANA's default commits on DDL, and CentralizaSpotUtilidadPerdida creates and drops a temporary table, so a centralization run was not atomic. | Every transaction of a run executes the session statements (`JobRunner.SESION_HANA`, on by default; `contabilidad.jobs.sesion-hana=false` only for a non-HANA database). CONT-J-100/101. |
| ARQ-J-H2 | A failed intermediate `ctx.commit()` left the run with no transaction: later writes auto-committed, so SAP-accepted vouchers could end up marked `'E'`. | `commit()`/`rollback()` always reopen a transaction. If reopening is impossible, the run aborts with `JobRunner.CorridaInterrumpidaError`, an `Error` that per-voucher `catch (RuntimeException)` cannot swallow. CONT-J-102/103. |
| ARQ-J-H3 | `/internal/job-call` had no at-most-once guard: a retry or a duplicate call ran the same job twice (duplicate SAP vouchers, doubled or deleted interface files). | At most one run per `IdPrograma` and per job class in the instance; the duplicate gets `JobEnCursoException` → HTTP 409. CONT-J-104/105. Multi-replica: deployment prerequisite 4. |

The review also asked to put `idPrograma` in the MDC (legacy `ThreadContext.put("jobid")`, BcsJob.java:88) and not to
leave the worker thread interrupted (L2); both were done in `JobRunner` along with the HIGH fixes.

### MEDIUM and LOW (open)

| Id | Finding |
|---|---|
| ARQ-J-M1 | Servicios' `application.properties` on the classpath overrides Jobs' `application.yml` for the same keys. Rename Jobs' file to `application.properties`, which hides Servicios' copy, or set `spring.config.name`. |
| ARQ-J-M2 | No test loads the production wiring (`JobsConfiguration`, `ServiciosContabilidadConfiguration`). Add one `@SpringBootTest` on H2 with `contabilidad.jobs.sesion-hana=false`. |
| ARQ-J-M3 | The job tests mock the services. Add one H2 test with the real `CentralizaMultiSeleccionService` inside a `JobRunner` transaction (NESTED and savepoints). |
| ARQ-J-M4 | Container-local outputs and known_hosts. Moved to deployment prerequisite 5. |
| ARQ-J-M5 | Servicios still has a dead, diverging `CreaInterfazDiariaJob` bean (`InterfazDiariaConfiguration`). Delete it or deprecate it in favor of Jobs. |
| ARQ-J-M6 | Graceful shutdown (30 s default) can cut long InterfazContable or SFTP runs; set `spring.lifecycle.timeout-per-shutdown-phase`. There is no visibility of running jobs. |
| ARQ-J-L1 | `InterfazContable`'s `Function<Repository, Service>` factory and `ConexionSap.IMPLICITA` exist for the tests; they can be simplified. |
| ARQ-J-L3 | `REQUIRES_NEW` behaves like `REQUIRED` here; harmless. |
| ARQ-J-L4 | JSON numbers are passed as `String.valueOf` (`1.0` becomes `"1.0"`); send parameters as text. |
| ARQ-J-L5 | A 500 echoes the exception text; acceptable on the internal network only. |
| ARQ-J-L6 | The Dockerfile runs as root and has no `HEALTHCHECK`; compose has no healthcheck for jobs. |
| ARQ-J-L7 | `BcsGateway` has a stale `/contabilidad-jobs/**` route; remove it so the unauthenticated endpoint is never exposed. |
| ARQ-J-L8 | The local `.env` uses old variable names and there is no `.env.example`. |
| ARQ-J-L9 | `plataforma.base-url` defaults to localhost outside compose. |
| ARQ-J-L10 | The legacy `InterfazContable.java:97-98` has a commented-out SAP user and password; covered by the RULE-076 rotation. |

## Review of the RULE-061 / development-cases pass (2026-10-16)

`architecture-critic` on commit `28609d0` and the registered evidence. It reported 1 HIGH and 5 MEDIUM findings.

| Id | Finding | Status |
|---|---|---|
| H1 | The SAP request bodies (JournalVoucher, BusinessPartner) were not compared, only the routes | **Fixed:** `cuerpos.txt` per case, canonicalised (keys sorted, JournalEntryLines as a multiset, Login password masked, numbers literal); 11/11 bodies identical byte for byte |
| M1 | The Estado comparison is the seed on success, and the state-procedure parameters were not compared | **Fixed:** `procedimientos.txt` (IdEmpresa, IdUsuario, IdPreComprobante per call) |
| M2 | The dev evidence repeats CONT-J-77 | **Documented** in `cases.json` and above; H1 and M1 now extend it beyond CONT-J-77 |
| M3 | The new side did not use the production service factory | **Fixed** in the driver (`InterfazContable.servicioConBitacora`); CONT-J-77 itself still builds the service directly (open) |
| M4 | The RULE-061 exception test cannot see a sequential re-run | **Fixed** (renamed to "no retry after the exception"; canary 8 is caught by the commit and rollback cases) |
| M5 | The RULE-061 reflection check is close to tautological | **Open:** an exact public-signature allowlist, a `@Scheduled`/`Thread.sleep` source check and one MockMvc single-call test |
| L1-L5, N2-N3 | RutaSalida masked instead of dropped (record it in the oracle), driver reproducible from the repo, 10 s timeout, scope comment | **Open** (N1, the spliced bullet, fixed; N3, the daemon heartbeat, documented) |

## Job pattern H4 (ported from BcsTesoreriaJobs, 2026-10-21)

Port of the reference implementation (`BcsTesoreriaJobs/TRANSFORMATION_NOTES.md`, "Job pattern H4", commits fa8aabbd and 60773cf1). Same binding constraints: NO database change of any kind, the legacy untouched, only existing `BAT.*` columns, tables and views, NO automatic retry. The dispatcher (the modern `BcsJobManager`, inert by default) will call `POST /internal/job-call` with `idPrograma` and the internal token for programs still PEN / 'N' and MUST NOT claim them: the endpoint claims atomically (a program already claimed would answer 409).

### What was ported

* Package `cl.bcs.contabilidad.jobs.programa`: the 12 classes of the reference with the same names and behavior (`Programa`, `ProgramaRepositorio`/`ProgramaRepositorioJdbc`, `ProgramaSql`, `EjecutorPrograma`, `ProgramaStatusListener`, `ProgramaModo`, `TextoResultado`, 4 exceptions). `IdTipoPrograma` is a String (NVARCHAR(40)).
* `runtime/JobRunner`: `ejecutarReclamando(clase, id, Supplier<parametros>)` (`ejecutar` delegates to it, unchanged for callers). Also the reference's review M8: a failing listener or `onError` is logged and never loses the `JobResultado` (needed because the real listener now writes to the database).
* `web/JobCallController`: second constructor taking `EjecutorPrograma` (the old one, `new JobCallController(runner)`, keeps the caller-parameters contract), `clase` required only in caller mode, handlers 404 (program not found), 409 (claim lost; the existing 409 for the in-flight guard stays), 422 (class or parameters differ from the row), 503 (class not run here, as before) and 500 `INTERNAL_ERROR` for `ProgramaJdbcException` (BAT access failed; nothing ran).
* `config/ProgramaConfiguration` (the module's wiring package is `config`, not `wiring`): `ProgramaModo`, repository, `EjecutorPrograma` and the `jobStatusListener` bean, which moved here from `JobsConfiguration` (real listener in `programa`, logger in caller mode).
* Property `contabilidad.jobs.programa.modo`: `programa` in `application.yml`; `parametros-del-llamador` in the new `src/test/resources/application-test.properties`. An unknown value stops the start.
* Parameters: in mode `programa` they are read from the view `BAT.ConsultarParametrosProgramas` (this covers the role of the reference's `ParametrosTexto`, which this module does not have); a non-empty `parametros` sent by the caller must equal them (422). Caller mode is untouched, so the goldens and the controller test keep the module's own JSON-to-text conversion.

### Token filter added (the module had none)

`web/TokenInternoFilter`, a copy of the one in the other three Jobs modules: `X-Internal-Token` against `services.internal-token` (`${INTERNAL_TOKEN:}` in `application.yml`); empty token = every request rejected (401); constant-time compare; `;` path parameters and repeated `/` normalized before the allow-list; public paths: `/actuator/health` and `/actuator/info` only. It is a `@Component` of `cl.bcs.contabilidad.jobs.web`, so it covers `/internal/job-call` and the actuator. The test profile sets a fake token (`fake-internal-token-0000`); no existing test goes through the filter (they use standalone MockMvc), so none had to change. REVERSES the "Notes for operations" statement below that `/internal/job-call` has no authentication. OWNER ACTION: `docker-compose.yml` (`bcs-contabilidad-jobs`) does not pass `INTERNAL_TOKEN` yet (outside this module's scope); until it does the container rejects every call (fail closed), as the other three Jobs services already receive it.

### Tests

54 new tests (suite 199 -> 253, 0 skipped): `programa/ProgramaRepositorioLecturaTest` (8), `ProgramaRepositorioReclamoTest` (5, includes the two-thread race), `ProgramaEstadoFinalTest` (9: TER, ERR, truncation, requires review), `ProgramaJobCallTest` (12: HTTP end to end, 404/409/422/503, two simultaneous calls), `ProgramaModoTest` (4), `wiring/ProgramaArranqueTest` (2) and `wiring/ArranqueCallerModoTest` (2, real filter and controller beans), `web/TokenInternoJobCallTest` (12). Fixture `support/ProgramaH2` (own H2, MODE=LEGACY): it creates the same `BAT.Programa` table as `support/H2Jobs` (IdPrograma, RutaSalida) and layers the NEW `src/test/resources/contabilidad-jobs/schema-programa.sql` (real DDL, BAT_DDL_REAL.md) on top; `H2Jobs`, the goldens and the equivalence drivers are untouched. This module has no `H2Hana`: `ProgramaH2` wraps its connections to strip the `BSCL.` prefix and to replace `CURRENT_UTCTIMESTAMP` with the fixed clock 2026-03-15 08:30:00.

Equivalence: the module has no `-Dequivalence.out`; its dumps come from two scratch drivers kept under `analysis/.../equivalence/BcsContabilidadJobs/{dev,fresh}`. The dev driver (11 cases, B40-B50) was recompiled and run against the new classes: `legacy/` and `new/` identical (`diff -rq`). The dumps contain no `BAT.Programa` rows (only run results, logs, PreComprobante states, SAP requests and bodies). The fresh driver could not be rerun here (its cases need an `expected.json` recorded by the oracle that is not in the repository); no JobRunner behavior it exercises changed except the listener guard, which no case triggers.

### Deviations from the reference

1. Package `config` instead of `wiring`. 2. No `ParametrosTexto` and no generic `Exception` handler / message redaction of the reference (reviews M2, M3 not part of this module's scope): only the `ProgramaJdbcException` handler was added. Consequence: in mode `programa` a JSON number in a declared `parametros` is compared as `String.valueOf(Double)` (1.50 becomes "1.5"), so it can give 422 where the view holds "1.50"; the dispatcher should send text values or no `parametros`. 3. `ProgramaH2` has its own base table and clock instead of `schema-jobs.sql` and `H2Hana`.

### Limitations and unconfirmed DDL

The ones of the reference (process dying after the claim leaves ASI / 'S'; failed final write; no unique constraint over program and attempt; `Nodo` not exclusive; 'requires review' only by text prefix; collaborators committed outside the job transaction; database clock; not proven on HANA) apply here unchanged. Per `BAT_DDL_REAL.md` the column names, types and lengths of `BAT.Programa`, `TipoPrograma`, `Agenda`, `ParametrosPrograma`, `LogPrograma` and the 10 columns of the view are CONFIRMED; STILL UNCONFIRMED: primary keys, NOT NULL and the DEFAULTs (`Bloqueo 'N'`, `Intento 0`, `IdAgenda 0`, `Nodo`), the portable `UPDATE ... IN (SELECT ...)` and the `TIMESTAMP '0001-01-01 00:00:00'` literal on HANA, and the privileges of the job user on `BAT.*` and the view. A job of this module that writes `RutaSalida` still does it in the job transaction (`updateRutaSalida`); the final-state write is a separate commit afterwards.

## Step 5: collaborators in process (JOBS_PATTERN.md H-1; JOBS_H1_H4_INVENTORY.md section D step 5)

Port of the reference (`BcsTesoreriaJobs/TRANSFORMATION_NOTES.md`, "Step 5"). Switch: `contabilidad.jobs.colaboradores-en-proceso` (`application.yml` = `true`; `false` or absent = exactly the pre-step-5 wiring, pinned by `ColaboradoresHttpUnicamenteTest`). No database change, no legacy change, no edit of the BcsContabilidadServicios sources.

### What runs in process (group M, verified in the code) and what stays on HTTP

Inventory A.3 lists three platform ports for this module; the code (`TomEePlataformaClient`, `PlataformaHttp`, `TomEeProcesoBatchClient`, `SapB1Gateway`) confirms one group M collaborator:

| Name | Caller | Owner entry point |
|---|---|---|
| `ConsultarClearingFinanciero` | `CentralizaClearingFinanciero` (Servicios, reached by `CentralizaMultiSeleccionJob`), through `PlataformaPort.consultarClearingFinanciero`; was `POST /api/tesoreria/ConsultarClearingFinanciero` to TomEE | Tesoreria: `JavaCallDispatcher` (`cl.bcs.tesoreria.liq.ConsultarClearingFinanciero`) -> `ConsultarClearingFinancieroService` |

Still on the existing adapter (`TomEePlataformaClient`, untouched): `ObtenerDiaAnterior` (Plataforma, TL, HTTP to TomEE), `InscribirProcesoBatch` (`TomEeProcesoBatchClient`, TW, used by the Servicios `EnviarPreComprobanteService`, not by a job), company data and configuration parameters (already JDBC on the caller's connection, no HTTP), and the non-Java edges SAP B1 Service Layer / SAP B1 database / SFTP.

### Design

* The override is at the jobs-app level: `config/ColaboradoresEnProcesoConfiguration` defines `PlataformaEnProceso` as the `@Primary` `PlataformaPort`; the Servicios classes inject the port by type and get it (test S5-W02), and `TomEePlataformaClient` stays a plain bean, the delegate for every other method.
* `adapter/PlataformaEnProceso` builds the request of the legacy (`p_UsaReglasNegocio` = "S", chambers PM, RV, SM, PH, HS: `CentralizaClearingFinanciero.java:44-50`), calls Tesoreria's entry point through `adapter/DespachadorEnProceso` / `EntradaEnProceso` (copies of the reference) and maps `P_OUT_CLEARINGFINANCIERODETALLE` to `ClearingFinancieroDetalle` with the same conversions as the HTTP adapter (absent/null id -> 0, text -> "", amount -> 0). Without an owner for the name it falls back to the HTTP adapter.
* Tesoreria is a library with a LIMITED scan (`config/ServiciosTesoreriaEnProcesoConfiguration`, 3 beans named by fully qualified class name): `JavaCallDispatcher`, `ConsultarClearingFinancieroService`, JDBC `ConfiguracionSistemaJdbc`. No web layer, no router adapter (`ColaboradoresJavaCall`), no `infrastructure.config` (so Tesoreria's transaction-manager customizer is NOT applied: this application's transaction semantics do not change). Only the clearing is claimed from Tesoreria's dispatcher.
* Pom: `tesoreria-service` (plain jar) + `jakarta.xml.ws-api` 4.0.2 pin (as in BcsTesoreriaJobs). No Maven cycle and no compile-time dependency between the libraries (test S5-W08 checks the class files both ways). Consequence for the recorded command: `-am` now also builds BcsTesoreriaServicios, so its 955 tests run in the same build.
* `application.yml`: this module already has its own (with content), so the reference's empty shadow file is not needed. tesoreria-service ships `application.properties` only; the one of contabilidad-service is first on the classpath and wins, as before (S5-W07 pins `spring.application.name`, `server.port` and the absence of `services.javacall.url`). Keep `contabilidad-service` before `tesoreria-service` in the pom.

* The in-process call runs inside a NESTED unit (savepoint) of the run's transaction (`TransactionTemplate`, the same pattern as `ContabilidadJdbcRepository.persistirCentralizacion`; REQUIRED when there is no transaction). Reason: Tesoreria's service is `@Transactional` REQUIRED and, if it throws, its proxy marks the WHOLE job transaction rollback-only, while `CentralizaMultiSeleccionService` swallows the `RuntimeException` of a type and goes on (legacy: one connection, one failed type did not undo the others). With the savepoint only that call is undone and the flag is reset; the exception reaches the caller unchanged (S5-T06 vs S5-T07).

### Tests (new: 23; Jobs 253 -> 276, 0 skipped)

* `adapter/PlataformaEnProcesoTest` (7, no Spring): the clearing resolves in process and no HTTP method is touched, request as the legacy built it; the rows equal what `TomEePlataformaClient` builds from the same detail against a local HTTP server (rows of golden TES-B-23 of BcsTesoreriaServicios, copied byte for byte to `src/test/resources/contabilidad-jobs/enproceso/`); null/absent values; null module -> ""; every other port method goes to the existing port; no owner -> HTTP; runtime exception propagates unchanged, checked one wrapped with cause.
* `wiring/ColaboradoresEnProcesoWiringTest` (8, real context on H2): primary port and delegate, the Servicios service holds the primary, exactly 3 Tesoreria beans, only the clearing claimed, the REAL Tesoreria service reached with no request to the fake TomEE, `ObtenerDiaAnterior` still goes to the fake TomEE, no property leak, no library cycle (a context that starts is also the no-Spring-cycle proof).
* `wiring/ColaboradoresEnProcesoTransaccionTest` (7, real `JobRunner` REQUIRES_NEW + a REAL Tesoreria writing collaborator, `ActualizarTransaccionService`, an UPDATE of `ORD.Transaccion`, reached through the real `JavaCallDispatcher`): job commits -> committed; job returns false -> undone (and visible to the job before); job throws after the call -> undone; intermediate `ctx.commit()` keeps the first write, a later failure undoes only the second; a failing `@Transactional` Tesoreria stand-in swallowed by the caller -> the run still COMMITs with the earlier write (through the port), and the same failure through the bare dispatcher -> `UnexpectedRollbackException`, all undone (why the savepoint exists).
* `wiring/ColaboradoresHttpUnicamenteTest` (1): switch off -> the old wiring. Baselines unchanged: Servicios 1315, Utils 2199, Tesoreria Servicios 955 (new in the `-am` build), 0 skipped; the dev equivalence driver (B40-B50) `diff -rq` identical for `legacy/` and `new/`.

### Limitations (read before real traffic)

1. HANA is not exercised: all proofs are on H2. Same-transaction behaviour on HANA (session statements `SET TRANSACTION AUTOCOMMIT DDL OFF`) is not proven.
2. The transaction proof uses `ActualizarTransaccionService` because the clearing only reads: it proves the mechanism (Spring `@Transactional` REQUIRED bean fetched by the dispatcher, same `DataSource`), not a write by the clearing itself (it has none). The clearing path with data (chambers, dividends, simultaneous operations) is not re-run here: it rests on Tesoreria's own goldens TES-B-23..46; here the real service is reached with an empty company view (legacy TES-B-38 shape) and its output mapping is proven on the TES-B-23 rows.
3. Equivalence of the port: the HTTP endpoint `/api/tesoreria/ConsultarClearingFinanciero` was never verified against TomEE (it may not exist: the adapter turns any failure into 503). The in-process answer is the legacy `logica` output of the already migrated service. `FechaLiquidacion` of a row is "" in both paths (the service's `Detalle` has no such field). Amounts are exact JSON decimals in process (the HTTP adapter went through `Double.toString`): equal by value, scale can differ.
4. Legacy: the legacy ignores `P_OUT_MENSAJES`; so does this port (an error message with no rows yields an empty list). A runtime exception of the service (legacy NPE TES-B-45) reaches the job and rolls it back.
5. Tesoreria's `globalRollbackOnParticipationFailure=false` customizer is deliberately not scanned (it would change this application's transaction model); the failure isolation of the clearing is the savepoint of `PlataformaEnProceso` instead. Savepoints on HANA are not proven here (this module's `persistirCentralizacion` already depends on them). A failure that is NOT swallowed ends the run in ERROR with the original exception, as before.
6. The savepoint also undoes anything the clearing wrote before failing; the clearing only reads, so nothing is lost.
7. Collaborators not migrated (`ObtenerDiaAnterior`, `InscribirProcesoBatch`) still go over HTTP and commit on their own (step 5b/6).

## Step 6: fail-closed guard for TomEE write collaborators (JOBS_PATTERN.md H-1 point 2; JOBS_H1_H4_INVENTORY.md section D step 6)

Ported from BcsTesoreriaJobs (commit 2504267b; read its "Step 6" section for the design, the marks and the guarantees, which are the same here). Binding and kept: NO database change (only the existing `BAT.LogPrograma` / `BAT.Programa`), legacy untouched, no idempotency header, no automatic retry. Switch: `contabilidad.jobs.guarda-tomee` (`application.yml` = `true`, also when the property is missing; `false` in `application-test.properties`, so every golden, the 276 earlier tests and the equivalence dump run on the pre-step-6 wiring, pinned by `GuardaCableadoApagadoTest`).

### What it guarantees and what it does NOT

Same four guarantees and eight limits as the reference (announce before, never re-run after a HECHO or an unclosed INTENTO, review state after a non-commit with TomEE calls, marks survive the rollback; the guard detects and stops, it never undoes a committed TomEE write). Module specifics:

1. DECISION: in this module the guard is mostly dormant. The five jobs (CentralizaMultiSeleccion, CreaInterfazDiaria, CreaInterfazDemanda, InterfazContable, GenerarInformeClienteRelacionadoBatch) reach NO guarded name today (inventory A.3: no TW collaborator). The guard is wired and tested so that the day a job reaches `InscribirProcesoBatch` (or a new HTTP name is added) the call is announced, closed and never repeated silently; a new name that is not classified fails the build (below). It adds nothing at run time when no guarded call is made except one read of `LogPrograma` per claimed run (`H4G|%` marks of the program) and the thread-local scope.
2. Not covered, as the inventory already says: SAP B1 Service Layer (`SapB1Gateway`) and SFTP are external effects but not TomEE collaborators; a rollback does not undo them and this guard does not mark them (outside the scope of H-1 point 2).
3. Run scope: thread-local of the thread that runs the job. Checked (test `G6-C05`, scans `BcsContabilidadJobs/src/main` and `BcsContabilidadServicios/src/main` without comments): no `new Thread`, executor, `@Async`, `CompletableFuture`, parallel stream, `@Scheduled`, timer or `sendAsync`. `JobRunner` runs the job in the calling (request) thread; the HTTP calls of the Servicios adapters are synchronous in that thread. A job that fans out must pass the scope along or it would be silently unguarded.
4. The step 5 in-process call (`PlataformaEnProceso`, savepoint `PROPAGATION_NESTED`) is a read of Tesoreria and never touches the guard; the guard writes its marks in an independent connection, so it neither joins nor disturbs that savepoint or the job transaction.

### Differences from the reference (adapted to this module)

| Reference (Tesoreria) | Contabilidad | Why |
|---|---|---|
| One name-based port `Colaboradores.invocar(nombre, json)`; `ColaboradorTomEEGuardado implements Colaboradores` | No router. Two typed ports (`PlataformaPort`, `ProcesoBatchPort`), HTTP adapters in BcsContabilidadServicios (`PlataformaHttp`). `ColaboradorTomEEGuardado.invocar(String, Supplier<T>)` is the generic call guard; `ProcesoBatchGuardado implements ProcesoBatchPort` is the decorator of the only guarded port | there is no single name-based port to wrap |
| Names = router names | Names = the last segment of the `/api/<owner>/<Name>` path the adapter posts to | the legacy collaborator names |
| Wrapping depends on step 5 (three `ColaboradoresJobsConfiguration` conditions) | `config/GuardaTomeeConfiguration` adds one `@Primary ProcesoBatchPort` (the guard around `TomEeProcesoBatchClient`, which stays a plain bean); `PlataformaPort` is not wrapped; step 5 wiring (`ColaboradoresEnProcesoConfiguration`) is not edited | the in-process owner is Tesoreria's read; both Plataforma HTTP names are reads |
| `application.properties` | `application.yml` (this module has no .properties) | module structure |
| `programa/`, `web/`, `wiring/` | `programa/`, `web/`, `config/` (this module's package for wiring) | module structure |
| `JobCallController` already with token filter and typed parameters | the handler is added to the Contabilidad controller (`Map<String,Object>` parameters, `TokenInternoFilter` unchanged); 423 is behind the token filter like every other answer | H4 port of this module |
| Extra: `ClasificacionColaborador.puertos()` maps every port method to a name; tests read the Servicios sources | new | typed ports: a new method or a new `/api/...` literal must be classified |

### Classification (the names this module can send over HTTP)

| Port method (adapter) | HTTP name | Category | Guarded | Why |
|---|---|---|---|---|
| `ProcesoBatchPort.inscribir` (`TomEeProcesoBatchClient`) | `InscribirProcesoBatch` (TW #17) | NO_REPETIBLE | YES | registers a program in TomEE; a repeat registers a second one. Reached only by `EnviarPreComprobanteService` (a service no job calls) |
| `PlataformaPort.obtenerDiaAnterior` (`TomEePlataformaClient`) | `ObtenerDiaAnterior` (TL #34) | SOLO_LECTURA | no | pure read; repeatable |
| `PlataformaPort.consultarClearingFinanciero` | `ConsultarClearingFinanciero` (M #11, Tesoreria) | MIGRADO_LECTURA | no | read; with step 5 it is served IN PROCESS (never HTTP) unless the dispatcher does not serve it, and a repeat over HTTP is harmless |
| `consultarEmpresa`, `obtenerFechaSistema`, `obtenerLocalizacionSistema`, `obtenerMonedaSistema`, `obtenerParametroConfiguracion` | none (`JdbcEnConexion`) | ESPECIAL | no | JDBC on the job connection and transaction, no HTTP |
| (not sent by this module) | `AgregarLog` | APPEND_ONLY category exists, no name | no | append-only; kept as a category for parity |

Category MIGRADO_ESCRITOR exists (guarded when a call reaches HTTP) and has no member here. New category MIGRADO_LECTURA (not in the reference) for a migrated owner that only reads.

### Wiring

* guard on: `GuardaTomeeConfiguration` creates `ContextoGuarda`, `MarcasRepositorioJdbc` (own connections of the application `DataSource`), `GuardaTomee` and the `@Primary` `ProcesoBatchGuardado`; `ProgramaConfiguration` gives `GuardaTomee` to `EjecutorPrograma` through `ObjectProvider` (null when off).
* guard off (`false`): none of these beans exists and `EjecutorPrograma` gets `null`; the 4-argument constructor and the 3-argument one behave as before.
* `EjecutorPrograma`, `ProgramaRequiereRevisionException` and the 423 handler are identical to the reference except for the package; refusal state: `idEstado='ERR'`, `Intento = MaximoIntentos`, `Bloqueo='S'`, `Resultado = 'REQUIERE REVISION: ' + reason`; the dispatcher must map 423 like 409 (no ERR, no retry) and alert.

### How an operator releases a program in review

Same as the reference (read the row and the `H4G|%` marks of `BAT.LogPrograma`, verify in TomEE, decide; `ReprocesarPrograma` creates a new program id; resetting the same row does NOT bypass the guard).

### Tests (new: 36; Jobs 276 -> 312, 0 skipped)

`guarda/MarcaGuardaTest` (8, copied), `ClasificacionColaboradorTest` (6: every port method mapped and classified, every `/api/<owner>/<Name>` literal of the Servicios sources classified and exactly the three known, the scanner detects an unclassified name, only `PlataformaHttp` and `SapB1Gateway` are HTTP clients and only two adapters instantiate `PlataformaHttp`, no other thread, guarded = `InscribirProcesoBatch` only), `ColaboradorTomEEGuardadoTest` (9, incl. the real `ProcesoBatchGuardado`), `GuardaProgramaJobCallTest` (9, MockMvc + real runner / executor / repositories on H2, as the reference), `wiring/GuardaCableado{Apagado,Http}Test` (4: guard off = step 5 untouched and executor without guard; guard on = primary batch port is the guard around `TomEeProcesoBatchClient`, `PlataformaPort` still `PlataformaEnProceso`, no HTTP request leaves when the INTENTO cannot be written). `schema-programa.sql` already holds `BAT.LogPrograma` (no change). Equivalence dump of `dev/legacy` and `dev/new` (DevJobsDriverTest) re-run into a scratch directory: identical to the committed one (`diff -rq`).

### Limitations (read before real traffic)

1. The guard is not exercised by any job today (point 1 above); the production switch is `true`, so the only runtime effect is the per-run read of `LogPrograma`.
2. HANA is not proven (H2 only): `CURRENT_UTCTIMESTAMP` into `FechaLog`, the `LIKE`, the independent connection under the HANA pool (one more connection per run while the job holds its own).
3. `EnviarPreComprobanteService` is a bean of the jobs application (the Servicios library is scanned) but is not served by `/internal/job-call`; if it is ever reached from a job, the marks apply only inside a run with an `idPrograma`.

## Step 6b: fail closed when a guarded call has no scope on its thread + build-time scan for parallelism (hardening of step 6)

Binding as in step 6: NO database change (no marks are written by this hardening, only the existing `BAT.LogPrograma` marks of step 6 remain), H2 only, legacy untouched.

### Problem

The run scope of step 6 (`ContextoGuarda`) is a ThreadLocal of the thread that runs the job. A guarded collaborator (NO_REPETIBLE or MIGRADO_ESCRITOR that reaches HTTP) called from ANOTHER thread (a `parallelStream` or a pool inside a library such as apv-service, a `CompletableFuture`) found no scope and the decorator passed it through UNMARKED: a TomEE write with no INTENTO/HECHO mark, invisible to the re-run refusal. Step 6 limit 5 said "the module starts no other thread (checked)"; that was true of this module only, not of the libraries the jobs call.

### Runtime change (fail closed)

`ContextoGuarda` now distinguishes three states of a thread, and knows the mode of the module (`new ContextoGuarda(exigePrograma)`; the bean is built from `ProgramaModo`: `true` only in mode `programa`, i.e. `contabilidad.jobs.programa.modo` = `programa`, the default):

| Thread state | Mode `programa` | Caller mode (`parametros-del-llamador`) |
|---|---|---|
| a `CorridaGuardada` is open (`GuardaTomee.abrir`, after the claim) | guarded names: marks + call (unchanged) | n/a (nothing is claimed) |
| explicit "no program" scope (`GuardaTomee.abrirSinPrograma`, opened by `EjecutorPrograma` for the whole run in caller mode) | pass-through, no marks | pass-through, no marks |
| NO scope at all | guarded name: REFUSED (see below); any other name: pass-through | pass-through (nothing to guard) |

Refusal: before any HTTP call and without writing any mark (there is no `idPrograma` to key it), `GuardaTomeeException("llamada protegida sin contexto de programa (hilo distinto?): <name> ...")` is thrown and logged at ERROR with the thread name. The job sees an exception and rolls back its own work as for any failed collaborator call. Not affected: names that are not guarded (reads, `AgregarLog`, in-process services, unknown names), threads that never call a guarded name (health/actuator, tests), the property `contabilidad.jobs.guarda-tomee=false` (no beans: wiring identical to step 5), and the `ContextoGuarda()` default constructor is the strict one (`true`), so a hand-built context cannot silently be lax.

The scope is NOT propagated to other threads on purpose: the marks of one run are numbered in call order (`n`) and a guarded write racing on another thread would make the INTENTO/HECHO order unreadable. A job that really needs a guarded call from a pool must be redesigned to call it on the job thread.

Tests: `GuardaSinContextoTest` (G6b-01..08: second thread refused with no HTTP call and no marks; `parallelStream` inside a ForkJoinPool; commonPool and `CompletableFuture`; no run at all; caller mode passes on any thread; explicit "no program" scope; unguarded names from other threads; `EjecutorPrograma` opens and closes the caller-mode scope). Test changed: `ColaboradorTomEEGuardadoTest` G6-D05 pinned "no scope = pass-through"; that is now only true for caller mode, so it builds the guard over `new ContextoGuarda(false)`; the no-scope-in-mode-programa case is G6b-04.

### Build-time scan

`ParalelismoFuentesTest` (G6b-S01..S03) scans `src/main/java` of this module and of the sibling libraries its jobs use (this module, BcsUtils, BcsContabilidadServicios, BcsTesoreriaServicios; located relative to the module, existence asserted) for `parallelStream(`, `.parallel(`, ForkJoin, CompletableFuture, ExecutorService/Executors, `@Async`, `new Thread`, `sendAsync`, `StreamUtils.parallel*`. Every hit must be in an explicit allowlist (file, exact number of matching lines, reason); a new hit, one more line in an allowlisted file, a hit on the same line as a collaborator call, or a stale entry fails the build.

### Result of the scan for this module

The only hit in the scanned sources is `BcsUtils` `StreamUtils.parallelFlatmap` (3 lines): defined and never called by any scanned source (a future caller is itself a hit of the scan), and its lambdas only apply a pure `BiFunction` to two in-memory collections: NOT on a path that reaches a collaborator. The module itself and its Servicios libraries start no thread and use no parallel stream.

## Follow-ups

* `/code-modernization:modernize-verify otr-sva-optimus-corredores BcsContabilidadJobs` for the independent check.
* Strengthen the GenerarInforme default pins (canary 5 turned only 1 test red).
* `BcsUtils` is the last Phase 1 module.
* Phase 5 (`BcsJobManager`) replaces the cut-over proxy with the Job Manager service calling `/internal/job-call`.

## Notes for operations

* The Servicios jar carries its own `application.properties` (datasource placeholders, `plataforma.base-url`, a Spring
  Batch flag). On the Jobs classpath it is loaded too and, being `.properties`, wins over `application.yml` for the
  same keys; today the overlapping keys have identical values. Spring Batch is excluded from the dependency.
* `/internal/job-call` is protected by the internal token since the H4 port (see "Job pattern H4"); it must still stay on
  the internal network.
