# BcsJobManager — Transformation Notes

**Source:** `legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA/BcsJobManager/`  
**Target:** Spring Boot 4.1.1 / Java 25, port 8106  
**Pattern:** Hexagonal architecture  
**Build status:** `mvn -o -pl :bcs-job-manager clean test` → 89 tests, 0 failures, 0 errors, 0 skipped (2026-10-21)

---

## Role (step 7 of the Jobs H1/H4 plan, 2026-10-21)

Dispatcher of the migrated job modules. It selects eligible programs (read-only) and calls
`POST /internal/job-call` of the module that owns the class. The module claims the program atomically,
loads its parameters from the BAT view and writes TER/ERR (`EjecutorPrograma`, `ProgramaStatusListener`).
Therefore the dispatcher does not claim, does not update `BAT.Programa` and does not retry.
See README (contract, response mapping, activation checklist).

```
domain/        Programa (+demonio), Agenda, NodeInfo
port/out/      ProgramaPort (read only), AgendaPort, NodoPort
adapter/out/   ProgramaJdbcAdapter (read-only SELECT), AgendaJdbcAdapter, NodoJdbcAdapter
adapter/in/    AdminController, GlobalExceptionHandler
service/       JobDispatcherService (poll loop, inert gate), ProgramaDespachador (in-flight guard, bounded executor),
               JobCallClient (HTTP + response mapping), DestinoResolver, DiaryPollingService (off), JobManagerService, DiaryService
config/        DataSourceConfig (@Profile("!test")), JobManagerProperties
```

## Binding decisions (user, 2026-10-21)

1. **No database change of any kind** and legacy untouched; the legacy manager never calls modernized code.
2. **No automatic retry** (RULE-059 disabled). The legacy never retried: `updateTryProgramm` is commented out
   (`BcsJobManager.java:104-105`); a failure stays ERR and is relaunched by hand (`ReprocesarPrograma`).
   Removed: `retryFailedPrograms`/`SQL_RETRY_FAILED`, `POST /api/admin/retry`, and every write method of `ProgramaPort`
   (`lockPrograma`, `updateIntent`, `updatePrograma`, `updateCycleDaemon`, `updateStartDaemon`, `readParams`, `isDaemon`).
3. **Inert by default**: only `IdTipoPrograma` listed in `jobmanager.tipos-habilitados` are selected (empty = no query,
   no node registration, no HTTP). There is no safe per-type cutover while the legacy manager runs
   (`JOBS_CUTOVER_ANALYSIS.md`: the legacy filters by Nodo and at most ONE type, cannot exclude types, and its claim is not
   atomic), so production activation is one global decision; the code makes it explicit (WARN at startup with the types).
4. **Diary** (creating programs from `BAT.Agenda`): kept but OFF (`mode=AGENDA` AND `agenda.habilitada=true`), legacy-owned
   until the global cutover. Daemon agendas are skipped (RULE-061). The old zombie "kill" of OS processes now only logs.

## Corrected earlier statements

### RULE-057 (FIFO + FOR UPDATE SKIP LOCKED) — the SKIP LOCKED part was never implemented

The earlier notes and README said the queue used `FOR UPDATE SKIP LOCKED`. The code never did (HANA does not support it,
and the clause had been removed from the SQL). Nothing is locked in the dispatcher: the selection is a plain `SELECT`
(legacy `readProgramm` predicate; legacy line 48 had `FOR UPDATE` commented out) and double execution is prevented by the
atomic claim of the job module (`UPDATE ... SET Bloqueo='S', idEstado='ASI' WHERE Bloqueo='N' AND idEstado='PEN' AND Intento<Max`)
plus the per-program in-flight guard of the dispatcher. What stays: `ORDER BY fechaCreacion ASC, IdPrograma ASC`
(harmless FIFO). The `@Disabled` test `FifoOrderTest.concurrentDequeueSkipLocked_requiresHana` was removed (0 skipped).

### RULE-059 (retry restored) — reverted

See decision 2. `RetryTest` now pins that the port is read-only and that ERR programs with attempts left are neither selected
nor touched.

## Intentional deviations from the legacy selection

- The date window is bound from Java in UTC (`Timestamp` parameters, injectable clock); the legacy used HANA `CURRENT_DATE`
  (server date) for 00:00 and `CURRENT_UTCTIMESTAMP` for now. Same result when the HANA server runs in UTC.
- `jobmanager.task-type` is no longer a selection filter (replaced by the list `tipos-habilitados`, several types allowed).
- `Nodo` is `NVARCHAR(1)` in HANA (BAT_DDL_REAL.md); `jobmanager.node` is matched exactly after the legacy normalization.

## Not covered / open

- Real HANA is not exercised: tests run the production SQL on H2 with the real quoted table names
  (`schema-bat-real.sql`, types from `BAT_DDL_REAL.md`); the date binding and `IN (...)` expansion are portable but unverified on HANA.
- Delivery failures leave the program `PEN`. Module down (connection refused / connect timeout) is re-selected at every
  poll. 404, 422 and 503 (configuration/data inconsistency) start an in-memory cooldown per IdPrograma
  (`jobmanager.cooldown-no-entregable`, default PT60S, `PT0S` disables; user decision): `EnfriamientoNoEntregable`, no DB
  write, no retry, no claim; cleaned on expiry or when the program leaves the eligible set, bounded to 1000 entries
  (oldest evicted). Skipped at DEBUG; the first skip per cooldown logs once at INFO. 409, 200, 500, read timeout and other
  statuses never start a cooldown. A fixed configuration is picked up after at most the cooldown.
- `jobmanager.node` must be exactly ONE character (`BAT.Programa.Nodo` is NVARCHAR(1)); `''`/`*` = default node.
- `BAT.Nodos` registration/heartbeat/reboot-by-pid (`NodoPort`) is unchanged and only runs when the dispatcher is active.
- Stored-procedure calls of the diary (`AgendaPort`: `ActualizarSiguienteFechaEjecucionAgenda`, `AgregarPrograma`,
  `CorregirPrimeraEjecucion`, `ObtenerSiguienteFechaAgendada`) are not tested with H2.

## Other migration notes

| Legacy | Modernized |
|--------|-----------|
| `javax.sql.DataSource` (TomEE) | Spring `@ConfigurationProperties` HikariCP |
| `PreparedStatement` raw JDBC | `NamedParameterJdbcTemplate` + `MapSqlParameterSource` |
| `Class.forName()` job dispatch | `POST /internal/job-call` to the owning job module (`JobCallClient`) |
| `BcsJobManagerSingleBean.main()` | `BcsJobManagerApplication.main()` |
| `@WebServlet("/admin")` | `@RestController("/api/admin")` |
| JUL `Logger` | SLF4J |
| Hardcoded DB credentials in `.properties` file | `${HANA_URL}`, `${HANA_USER}`, `${HANA_PASSWORD}` env vars |
| `Static3DES` password cipher (TomEE-specific) | Removed — use secrets manager or env vars |
| System-command restart in `WebAdmin` | Removed — use Kubernetes rolling restart instead |
| `CypherEncryption` (OpenEJB API) | Removed — OpenEJB API not available in Spring Boot |
