# BcsContabilidadJobs: characterization tests

These tests define "done" for the rewrite of the legacy jobs module
`legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadJobs`. The oracle is the legacy code: the
legacy job classes and the job framework `BSCL/Plataforma/JAVA/BcsJobManager/src/cl/bcs/scheduler/BcsJob.java`. Each
test names its rule: `RULE-076` / `RULE-077` from `analysis/otr-sva-optimus-corredores/BUSINESS_RULES.md` (both are
reviewer-verdict "wrong", so the tests follow the reviewer's notes), and `CONT-J-NN` for everything else. Each test cites
the legacy `file:line` it pins.

The tests are written against the target API described below. They do not compile against the first-pass `src/main`
code. That is expected until Step 2 replaces it.

## Target API (defined by these tests)

Package `cl.bcs.contabilidad.jobs.runtime`:

| Type | Shape |
|---|---|
| `ContabilidadJob` | `String clase()` (legacy FQN), `boolean transaction(JobContext) throws Exception`, `default void onError(Throwable)` |
| `JobContext` | `long idPrograma()`, `String getParam(String)`, `Optional<String> getOptionalParam(String)`, `Integer getParamInteger(String)` (null when missing or not numeric), `void commit()`, `void rollback()` (legacy `con.commit()` / `con.rollback()` inside a job; the job continues in a fresh transaction), `void updateRutaSalida(String rutaSalida, Long idPrograma)` |
| `JobStatusListener` | `void status(long idPrograma, boolean error, String mensaje)` |
| `JobResultado` | `record (String clase, long idPrograma, Desenlace desenlace, Throwable error)`, `enum Desenlace {COMMIT, ROLLBACK, ERROR}`, `String mensaje()` (`""` unless ERROR) |
| `JobNoMigradoException` | `RuntimeException(String clase)`, with the class name in the message |
| `JobRunner` | `JobRunner(PlatformTransactionManager, JdbcTemplate, List<? extends ContabilidadJob>, JobStatusListener)`; `boolean soporta(String)`, `Set<String> clases()`, `JobResultado ejecutar(String clase, long idPrograma, Map<String,String> parametros)`. Duplicate class names cause an `IllegalStateException` at construction. |

`updateRutaSalida` runs `UPDATE "BSCL.Plataforma.BD::BAT.Programa" SET "RutaSalida" = ? WHERE "IdPrograma" = ?` on the
job transaction.

Package `cl.bcs.contabilidad.jobs.job` (the collaborators come from BcsContabilidadServicios):

| Class (`clase()`) | Constructor |
|---|---|
| `CentralizaMultiSeleccionJob` (`cl.bcs.contabilidad.jobs.CentralizaMultiSeleccionJob`) | `(CentralizaMultiSeleccionService)`, which calls `execute(JsonObject)` |
| `CreaInterfazDiaria` (`cl.bcs.contabilidad.jobs.CreaInterfazDiaria`) | `(PlataformaPort, InterfazContadorSource, InterfazSapSource, InterfazFileTransfer, Supplier<SftpSettings>, Clock)` |
| `CreaInterfazDemanda` (`cl.bcs.contabilidad.jobs.CreaInterfazDemanda`) | same as Diaria |
| `InterfazContable` (`cl.bcs.contabilidad.jobs.InterfazContable`) | `(PlataformaPort, Function<InterfazContableRepository, InterfazContableService>, InterfazContableRepository, Clock)`. The factory receives the job's repository decorated with the per-voucher commit (InterfazContable.java:244). |
| `GenerarInformeClienteRelacionadoBatch` (`cl.bcs.contabilidad.jobs.GenerarInformeClienteRelacionadoBatch`) | `(GenerarInformeClienteRelacionadoService)`, which calls `execute(JsonObject)` |

Package `cl.bcs.contabilidad.jobs.web`: `JobCallController(JobRunner)` handles `POST /internal/job-call` with the body
`{"clase", "idPrograma", "parametros"}`. The parameter values are converted to text and JSON nulls are dropped.

| Case | Response |
|---|---|
| COMMIT or ROLLBACK | 200 `{clase, idPrograma, resultado, error:false, mensaje:""}` |
| ERROR | 500 with `error:true` and the exception message in `mensaje` |
| Class not run here | 503 `{error, clase}` |
| Bad JSON, no `clase`, no `idPrograma` or a non-numeric `idPrograma` | 400 |

The module has no `@Scheduled`.

Also required: exactly one `main()`, in the `@SpringBootApplication` class (RULE-076).

## Consuming BcsContabilidadServicios

BcsContabilidadServicios is a Spring Boot repackaged app: its installed jar has `BOOT-INF/` and cannot be used as a
library. Step 2 needs two changes:

1. In the Servicios pom, add `spring-boot-maven-plugin` `<configuration><classifier>exec</classifier></configuration>` so
   that the main artifact stays a plain jar. The Dockerfile then uses `*-exec.jar`.
2. In this pom, depend on `cl.bcs:contabilidad-service:${project.version}` and add `com.h2database:h2` (test).

## Running

From the workspace root, with JDK 25:

```
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl BcsContabilidadJobs -am test
```

`InterfazContableJobGoldenTest` reads the legacy-oracle recordings in place from
`../BcsContabilidadServicios/src/test/resources/envio`. Override the location with `-Dcontabilidad.servicios.envio=<dir>`.
A missing fixture is an `AssertionError`. The test prints `equivalence cases executed (InterfazContable job shell): N/12`,
and its `@AfterAll` fails when N is 0. Nothing is `@Disabled` or assumed away.

Everything runs locally: H2 in memory, mocks, and `support/FakeSapServiceLayer` (a copy of the Servicios test fake). No
test connects to SAP, HANA, SFTP, QA or desa02.

## Adding a case

* **Job-shell behavior:** add a method to the job's test class. Give it literal inputs and outputs, a `CONT-J-NN` id that
  is not used yet, and the legacy `file:line`. Run it through `H2Jobs.runner(job)`, so that commit or rollback shows in
  the data (`marcar`/`marcas`, `rutaSalida`).
* **InterfazContable golden:** first record the case with the BcsContabilidadServicios legacy oracle (see
  `BcsContabilidadServicios/src/test/java/cl/bcs/contabilidad/envio/README.md`, "Adding a case"). Then add its directory
  name to the `@ValueSource` of `InterfazContableJobGoldenTest.contJ77_golden`.
* **Secrets:** use only fake values of the legacy shape (`fake-b1-user`, `fake-b1-pass-0000`, `fake-sftp-pass-0000`,
  `sftp.invalid`). Never copy anything from the commented-out blocks at `CreaInterfazDiaria.java:42-47`,
  `CreaInterfazDemanda.java:40-45` or `InterfazContable.java:97-99`.

## Legacy defects kept (pinned as-is)

* CONT-J-02: a `false` return value rolls back, but it is reported as `status(id, false, "")`, the same as a success.
* CONT-J-16: `CentralizaMultiSeleccionJob` ignores the service output and commits even when `P_OUT_ERRORESCONTADOR > 0`.
* CONT-J-84: `GenerarInformeClienteRelacionadoBatch` reports errors with a null status message (legacy `throw new Throwable()`).
* CONT-J-73/78: InterfazContable commits each accepted voucher at once, so a later failure leaves a partial batch. The
  `'E'` marks are rolled back.
* CONT-J-75: InterfazContable always returns true, even when every voucher failed or the SAP login failed.
* CONT-J-21: CreaInterfazDiaria ignores the `FechaEjecucion` param.
* CONT-J-11: the consultation date is read from `fechaConsulta`, with a lower-case f.
* CONT-J-46: the error label "Movimiento BCP" contains a typo.

## Deviations (approved)

| Id | Deviation | Status |
|---|---|---|
| CONT-J-35b | `SFTP_KNOWN_HOSTS` is required. | Approved |
| CONT-J-77 | B1SESSION cookie; the SAP password never appears in logs or on stdout. | Approved |
| DEV-J-01 | The legacy `CreaInterfazSIGA.properties` is replaced by the SFTP settings from the environment. If the settings are missing or invalid, the job returns false, as it did when the legacy file was missing. A bad PORT used to fail later, inside the SFTP step. | Approved 2026-10-15 |
| DEV-J-02 | An empty `PLA.RutaBaseArchivos` gives ROLLBACK. Legacy wrote under a literal `null` prefix. | Approved 2026-10-15 |
| DEV-J-03 | In CreaInterfazDemanda, a missing or invalid `FechaEjecucion` gives one `ERR - ` line and no generation. Legacy passed the raw string on. | Approved 2026-10-15 |

## Added in Step 2 (decision 2026-10-15, point 5: "match the legacy")

Written before the implementation, each with its `legacy file:line`; no Step 1 test was edited.

| Class | Ids | Pins |
|---|---|---|
| `job/CreaInterfazDiariaBitacoraTest` | CONT-J-38, 39, 39b, 39c | (ii) a failed SAP connection aborts generation and SFTP, still COMMIT; (i) the generator lines in the run log, text and order |
| `job/CreaInterfazDemandaBitacoraTest` | CONT-J-56, 57, 57b | the same for Demanda (first stack frame; Demanda file names) |
| `job/InterfazContableFallaParcialTest` | CONT-J-79, 79b, 79c | (iii) a failure after some lines were written leaves those lines in the log file |

These read the whole run log; `Archivos.lineasJob` still hides the generator lines for the Step 1 tests.

