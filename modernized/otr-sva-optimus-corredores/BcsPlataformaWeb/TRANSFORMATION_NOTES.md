# BcsPlataformaWeb — Transformation Notes

**Legacy**: Java EE 8 / TomEE — `BSCL/Plataforma/JAVA/BcsPlataformaWeb/`  
**Target**: Spring Boot 4.1.1 / Java 25 — hexagonal architecture  
**Port**: 8117  
**Build**: `mvn test -pl BcsUtils,BcsPlataformaWeb` → BUILD SUCCESS, 42 passed, 3 skipped

---

## CallServlet dispatcher — stub rationale

`CallServlet` (718 LOC) is the central API dispatcher of the legacy system. It:

1. Receives every API call via `POST /call?t=<serviceId>`
2. Looks up the service definition in the `PLA.Servicio` table (1 817+ rows) to determine:
   - type `"p"` → StoredProcedure call
   - type `"q"` → OData query
   - type `"j"` / `"j"` + resultset → Java plugin call via `PluginLoader`
   - type `"o"` → OData proxy
   - type `"X"` → JavaView (also via PluginLoader)
3. Calls `ProcesaLlamadaServletCall.logica()` for session/service validation
4. Dispatches to the appropriate backend

### Why it is a stub

The `PLA.Servicio` registry has not been exported. Without it:
- We cannot reconstruct the service-type routing table statically.
- We cannot determine which parameters each service expects.
- The `PluginLoader` JAR path (`PLUGIN_OPTIMUS.jar`) is unavailable.

**Implemented as**: `@RestController POST /call` + `GET /call` that throws
`UnsupportedOperationException("requires PLA.Servicio registry export")`,
handled by `GlobalExceptionHandler` → **503 Service Unavailable**.

### What is needed to complete this

1. Export `PLA.Servicio` from the HANA system (SELECT * FROM "BSCL.Plataforma.BD::PLA.Servicio")
2. Replace PluginLoader Java calls with equivalent Spring `@Service` beans
3. Replace StoredProcedure calls with `NamedParameterJdbcTemplate` + `SimpleJdbcCall`
4. Implement an `ODataProxyService` for type "o" and "q" calls

---

## CallBatch — stub rationale

`CallBatch` was a `@WebListener` that ran a single-threaded scheduled executor,
loading plugin JARs from the filesystem via `PluginLoader` to execute background jobs.

**Implemented as**: `@RestController POST /CallBatch` that throws
`UnsupportedOperationException("requires PLUGIN_OPTIMUS.jar or replacement task queue")`.

**To complete**: replace with Spring `@Scheduled` tasks or a proper job queue (e.g. Spring Batch, Quartz).

---

## Endpoints transformed

| Legacy Servlet              | Spring endpoint              | Status         |
|-----------------------------|------------------------------|----------------|
| `CallServlet`               | `POST /call`, `GET /call`    | Stub (503)     |
| `CallBatch`                 | `POST /CallBatch`            | Stub (503)     |
| `BootstrapServlet`          | `GET/POST /bootstrap`        | Partial (HANA) |
| `CrearSesion`               | `GET/POST /CrearSesion`      | Complete       |
| `PingServlet`               | `GET/POST /Ping`             | Complete       |
| `ConfiguracionServlet`      | `GET/POST /Configuraciones`  | Complete       |
| `AuditoriaWS`               | `POST /AuditoriaWS`          | Complete       |
| `ProcesarIdTransaccion`     | `POST /ProcesarIdTransaccion`| Complete       |
| `FileManagerServlet`        | `POST/GET /FileBlobManager`  | Complete       |
| `ActualizarClave`           | `POST /ActualizarClave`      | Complete       |
| `ValidaClaveProvisoria`     | `POST /ValidaClaveProvisoria`| Complete       |
| `SSOInit`                   | `GET/POST /SSOInit`          | Complete       |
| `SSOLogonGoogle`            | `GET/POST /SSOLogonGoogle`   | Complete       |
| `SSOLogonBofa`              | `GET/POST /SSOLogonBofa`     | Complete       |
| `SSOLogonLocalGoogle`       | `GET/POST /SSOLogonLocalGoogle` | Complete    |
| `SSOLogout`                 | `GET /SSOLogout`             | Complete       |

### Servlets NOT in scope (no legacy source read)
- `BoletinesServlet`, `ContratosServlet`, `DocumentosServlet`
- `CallReportServlet`, `CallWebsocket`, `FileDownload`
- `ImageManagerServlet`, `RepoStaticServlet`, `WebStaticServlet`, `TemplateServlet`

These document/report/static-serving servlets were not analyzed in Phase 1.
They should be added as individual stub controllers in a follow-up phase.

---

## Architecture decisions

### Hexagonal architecture
```
domain/          — Configuracion, RegistroConfiguracion, SesionCreada,
                   UsuarioProceso, ConfiguracionSSOData, TransaccionWSData
port/in/         — CrearSesionPort, PasswordPort
port/out/        — UsuarioRepositoryPort, SesionRepositoryPort
service/         — CrearSesionService, TransaccionWSService,
                   ConfiguracionCacheService
adapter/in/      — *Controller (one per legacy servlet)
adapter/out/     — UsuarioJdbcAdapter, SesionJdbcAdapter
config/          — GlobalExceptionHandler, PasswordEncoderConfig
```

### Password encoding
`HybridPasswordEncoder` (from BcsUtils) accepts both BCrypt (`$2a$`, `$2b$`, `$2y$` prefix)
and legacy unsalted SHA-256 (64-char hex). Canary mode (`security.password.rehash-enabled=false`)
accepts both without re-hashing — TomEE and Spring Boot can share the same ACC.Usuario table.

### HANA-specific SQL
All SQL queries that use HANA functions (`CURRENT_UTCTIMESTAMP`, `ADD_SECONDS`, `TOP 1`,
double-quoted schema names) are correctly written for HANA. Tests for these paths are
`@Disabled("requires HANA")`. H2 is used for the test-profile Spring context load only.

### Configuration cache
`ConfiguracionCacheService` uses Guava `CacheBuilder` (1-minute TTL, concurrencyLevel=4)
with a lazy-load strategy — the 11-schema UNION ALL is only executed on first request,
not at application startup.

### GlobalExceptionHandler mapping
| Exception                  | HTTP Status |
|----------------------------|-------------|
| NullPointerException       | 400         |
| IllegalArgumentException   | 400         |
| NumberFormatException      | 400         |
| UnsupportedOperationException | 503      |
| Throwable (fallback)       | 500         |

---

## Test results

```
Tests run: 60, Failures: 0, Errors: 0, Skipped: 3
  Skipped: intTest_crearSesion_withHana         @Disabled("requires HANA")
           intTest_callDispatcher_withHana       @Disabled("requires HANA")
           intTest_configuracion_withHana        @Disabled("requires HANA")
```

Build rules covered: B-PLA-01 through B-PLA-12 (60 assertions across 3 test classes).

## Fix applied — 2026-10-08

**Root cause of 3 verify failures:** `CallController.dispatch()` validated the `t` query parameter before reaching the UOE stub, so POST calls with `t` in the JSON body (not as a query param) received 400 instead of 503.

**Fix:** `callPost()` and `callGet()` now throw `UnsupportedOperationException` immediately (stub first, before any parameter validation). The full `dispatch()` implementation is preserved in the class and can be re-enabled in Phase 5 once PLA.Servicio registry is exported.

**Canary:** Changed `GlobalExceptionHandler.handleUoe()` to return 400 instead of 503 → 3 tests failed (rule01, rule01b, rule03). Restored. Equivalence cases executed: 60.

## Step 5b — internal JavaCall router `POST /internal/java-call` (2026-10-21)

**Problem.** Every collaborator adapter of the jobs and services sends `POST {JAVACALL_URL}/internal/java-call` (`{"class","params"}` + `X-Internal-Token`) but this module only exposed `/call` (user session). New code (existing `/call` and its tests untouched; baseline 70 tests, 3 skipped, unchanged): `adapter/in/InternalJavaCallController`, `internal/InternalTokenFilter`, `service/InternalJavaCallRouter`, `internal/*` (owner strategies, destinations, registry export reader). Usage, config and the routable table: README, last section.

**Decisions.**
- Own token filter, scoped to normalized `/internal/**` only (percent-decoding, `;`, `//`, dot segments, case); fails closed on an empty secret; constant-time compare. It does not apply to `/call` or any existing route. No session, no identity override (the caller is a trusted job; ARQ-H5 stays in `/call`).
- Routing reuses `JavaCallRouterService` (`claseHabilitada`, `permiteFallbackATomee`, `extractFqn`): enabled classes + a destination in `plataforma.javacall.destinos` go to the owner, the rest to TomEE; only 503 / connection refused fall back (ARQ-H6); everything else is returned unchanged. No idempotency header toward TomEE or owners.
- Owners: Contabilidad, Tesoreria and CuentaInversion already expose `POST /internal/java-call` (strategy `JAVACALL`, pass-through, no new behavior there). APV has no JavaCall entry: strategy `APV_REST` converts with the registry/mappers of `bcs-apv-javacall-adapter` and calls the typed REST route (the adapter library and `bcs-apv-api` are new dependencies of this module; Jackson 3 for the typed JSON). `cl.bcs.APV.ValidarMandatosPAC.logica` is in-process only: 422. `ValidarMandatosPAC.call` and `ValidarBancoPrevired` have no typed route (legacy `call()` returns null): "not routable yet", fall to TomEE.
- Type scope: only JavaCall type J. Registry export (HANA QA, 3,163 rows): Q 1,347, J 981, P 826, X 6, O 2, A 1 (J by package: plataforma 180, socionegocio 163, custodia 129, ordenes 90, mesa 84, normativa 81, fondomutuo 67, APV 56, tesoreria 35, facturacion 30, cuentaInversion 27, contabilidad 21, portafolio 13, sura 3, canalagente 1). Q and P are executed by the platform itself against HANA and are out of scope of the owner routing; this endpoint answers 422 `type not routable by this endpoint` for them and for X/O/A (the `/call` code in this module does not execute them today: it is a 503 stub, so there was nothing to reuse).
- The 1,817-class map assumption is superseded by the export: the 139 J classes of the migrated owners (APV 56, tesoreria 35, cuentaInversion 27, contabilidad 21) all resolve (`ContratoDestinosTest`): 56 APV typed routes, 80 owner routes, 3 owner-503 (to TomEE). Classes of the other packages still need explicit `destinos` entries + `clases-habilitadas`.

**Open questions / limits.**
1. The TomEE leg is `/call?t={IdServicio}` and needs a user session (`X-SESSION-TOKEN`) that jobs do not have; it forwards the header only if the caller sends one. Until the owners of TomEE confirm how a job reaches a legacy class by name (or a session-less legacy entry exists), calls that fall to TomEE can fail there. The class -> IdServicio id comes from `idServicio` in the body or from `plataforma.javacall.registro-servicios`.
2. The APV service answers 503 for `RestClientException` of a collaborator, which can happen after its own commit; under the shared 503 rule that falls back to TomEE and could repeat a write (same exposure as `/call`; decide whether APV should answer another status).
3. `services.internal-token` must be the same secret the jobs and owners use; the owners get it as `X-Internal-Token` (override with `plataforma.javacall.internal-token`).
4. Path-trick tests run on MockMvc/MockHttpServletRequest, not on a real Tomcat.

## Step 6 — Login MVP (option B: the UI is outside the core) (2026-10-26)

Plan: `analysis/otr-sva-optimus-corredores/TODO_MVP.md` (T-03..T-13, T-20..T-27). New package `cl.bcs.plataforma.login`; tests on H2 (as instructed), contract of the bootstrap derived from the `BCS_*` constants of the legacy page.

| Behavior | Legacy | Target |
|---|---|---|
| Prefix `/PLA` (T-10) | gateway servlet mapping | `PrefijoPlaFilter` (`/internal`, actuator and root routes do not move) |
| One transaction per request (T-13) | connection closed without commit on error | `UnidadDeTrabajo` (commit on normal return, rollback on exception) |
| Session filter (T-11) | `X-SESSION-TOKEN` filter | `FiltroSesion` (400 no header, 401 unknown token, 500 no connection; legacy exempt list + `/PLA/bootstrap/login`) |
| CrearSesion (T-03..09) | `CrearSesion.crearSesionUsuario` | `CrearSesionServicio`, `AuditoriaSesion` (PLA.Log + 4 DetalleLog, same hash), `SesionDatos` |
| Password management (T-12, T-24..27) | ActualizarClave, ValidaClaveProvisoria, recovery | `GestionClaveServicio`, `PasswordController` |
| Bootstrap (T-20/21/21b) | `BootstrapServlet` (HTML) | `BootstrapServicio`, `BootstrapController`: `GET /PLA/bootstrap/login`, `POST /PLA/bootstrap/home`, JSON keyed by the `BCS_*` names, `_contrato {optimus-bootstrap, 1}` |

**Deliberate deviations** (RULE_REVIEWS): RULE-018 (a BLO client does not enter), RULE-040 (`intentos < max`), RULE-032 (digit rule, already in BcsUtils `ValidadorClave`). Invalid JSON in CrearSesion is 401 (uniform refusal). A success answer keeps the full legacy shape.

**Not migrated:** menus, windows, RUM and HTML of the page (UI); per-service control (`ServiceFilter`, `/call`); RULE-025 (idle COR), RULE-004/005. `domain/UsuarioProceso` is unused.

**Legacy bugs kept:** `obj.add(null Fecha)` for a NULL holiday date throws NPE; a user with neither employee nor client row gives NPE/500 at home.

**Tests (H2):** CrearSesionServicioTest 20, LoginHttpTest 9, GestionClaveServicioTest 20, BootstrapTest 9, LoginPuntaAPuntaTest 1 (G-03: open login, wrong passwords, temporary block, block holds for the right password, expiry, session, home, bad token 401, no token 400). Module suite: 156, 0 failures. Equivalence is test-based on H2 (no legacy run for the login).
**Canary:** `IntentosConexion + 1 >=` → `>` in `CrearSesionServicio:220` → 2 tests failed (`bloqueo_temporal_al_llegar_al_maximo` and the end-to-end). Restored.

**Limits to hand over.** Writes (session, log, passwords, recovery SPs) are proven only on H2 and doubles; the SPs (`ACC.RecuperarClaveUsuario`, `ConfirmarCambioClaveUsuario`, `EVT.AgregarEvento`, `ObtenerFechaHoraSistema`, `ObtenerFechas`, `ObtenerConfiguracion`, `ConsultarFormatos`, `ACC.ConsultarUsuarioAdmin`) were not run against HANA (they write; HANA is read-only for us). Column types of HANA and a test base with users are pending. The JSON contract is a draft for the UI team to agree. ActualizarClave, ValidaClaveProvisoria and the recovery do not require a session (as the legacy): review in `harden`.

### Architecture review of the login MVP (architecture-critic, 2026-10-26) — open items, none applied

HIGH (need a person's decision; both are the legacy behavior, not new):
1. `FiltroSesion` only checks paths that came with `/PLA/` (as the legacy filter). Unprefixed controllers (`/call`, `/FileBlobManager`, `/ProcesarIdTransaccion`) are not covered, and the gateway route `bcs-plataforma-web` (`/call`, `/CallBatch`, `/bootstrap/**`) would expose them if enabled. Keep that route off; before G-01 decide: fail-closed default with an exempt list, and drop the unprefixed gateway paths.
2. `ActualizarClave` sets any user's password and reactivates the user with no session and no provisional password (SQL without `IdEmpresa`). Legacy behavior replicated; accept it explicitly in `RULE_REVIEWS.json` or require `ClaveProvisoria`/token.
MEDIUM: recovery token has ~29 bits (`generateToken(5)`) and `ConfirmarCambioClave` is anonymous (throttling, if any, is in the SP, not reviewed); user enumeration through `ValidaClaveProvisoria` / `SolicitudRecuperacionClave`; the attempt counter is read-then-written (`IntentosConexion + 1`), so parallel wrong passwords can skip the lockout (an atomic `SET x = x + 1` gives the same sequential results); the gateway `..` check is on the raw path (`%2e%2e`, `;` variants pass; the session filter still demands a token, but `/PLA/WebStatic` is exempt); the gateway copies a client `X-Forwarded-For` (pre-existing).
LOW: malformed JSON in CrearSesion is 401 (legacy 500); `/PORTAL/validate` exemption dropped; password hashed as UTF-8 (legacy: platform charset); unsanitized user name in a log line; a missing token-length row gives an empty provisional password; `SqlNombrado` list replacement is a substring replace and its comment regex would touch string literals (SQL is static today).

### Follow-up of the review (2026-10-26)
- Signatures of the 8 stored procedures of the login read from HANA QA (`SYS.PROCEDURES` / `SYS.PROCEDURE_PARAMETERS`, SELECT only, nothing executed): names, order and types of the parameters match what the code sends and reads (`ObtenerFechas`, `ObtenerFechaHoraSistema`, `ObtenerConfiguracion`, `ConsultarUsuarioAdmin`, `ConsultarFormatos`, `ACC.RecuperarClaveUsuario`, `ACC.ConfirmarCambioClaveUsuario`, `EVT.AgregarEvento`). Two facts to keep: `EVT.AgregarEvento` also takes `P_APLICAORIGENEVENTO` (NVARCHAR(1)), which neither the legacy nor this code sends (the dispatcher binds NULL), and `ConfirmarCambioClaveUsuario` has an extra OUT `P_STATUSCODE` that the code does not read. Their behavior was NOT run.
- Deliberate difference in the session filter, to be confirmed: the legacy exempted `/PLA/bootstrap` exactly and `/PORTAL/validate`; here only `/PLA/bootstrap/login` is exempt (the home needs the token) and `/PORTAL/validate` is not exempt.
- Attempt counter: kept read-then-write as the legacy; the atomic increment is left for `harden`.

### Home bootstrap against the real legacy and HANA QA (2026-10-26)
Legacy side: `LoginOraculoTest` (TestsOptimusSelenium) logged in as the QA test user and saved the page served by `POST /BSCL/Plataforma/UI/optimus` (`analysis/otr-sva-optimus-corredores/equivalence/login-legacy-qa-20261026/`). New side: `HomeQaComparacionTest` ran `BootstrapServicio.home` for that same user on HANA QA, read-only (read-only JDBC connection, every non-SELECT statement swallowed and listed, only checked read-only procedures called; the two writes skipped were the session-use and user-connected UPDATEs; `ACC.ConsultarUsuarioAdmin` is declared READ_ONLY=FALSE so HANA refuses it in a read-only transaction: its SELECT-only body was run as SELECTs).
**Result:** every constant the modernized home returns equals the legacy one (20 JSON constants byte-equal after parsing, 5 text constants equal; `BCS_TIMESTAMP_LOCAL` differs by the clock; the legacy prints the text `null` for `BCS_MUESTRA_SSO_GOOGLE`, the JSON has `null`). Not covered: `BCS_FULLMENU*`, `BCS_MENU_AGENTE`, `BCS_VENTANA` (UI), the body of the `CrearSesion` answer (not recoverable from the browser), and the write side.
**Three defects found only by running on real HANA (H2 hid them), fixed:** (1) `SqlNombrado` read DATE as `java.time.LocalDate`, which the HANA driver cannot convert: falls back to `getDate/getTimestamp`; (2) a BLOB column came as a `Blob` object: read with `getBytes`; (3) `ProcedureDispatcherService` did not recognize table parameters (HANA reports `TABLE_TYPE`, not `TABLE`) and read table OUT values with `getObject`: now `REF_CURSOR` + `getResultSet/getMoreResults`, as the legacy. (3) also concerned `EVT.AgregarEvento` (table IN) and every other table-parameter procedure called through the dispatcher; the TABLE-IN path is still untested on HANA.

### Login bootstrap, password recovery and session filter (2026-10-26, evening)
- **Session filter closed by default** (decision of the owner, not in the legacy, which only covered `/PLA/`): every path needs `X-SESSION-TOKEN` except the legacy exempt list (judged with the `/PLA` prefix added when the request came without it, so `/CrearSesion` and `/Ping` stay open) and `/internal/**` (own token), `/actuator/**`, `/error`. Tests: `LoginHttpTest.cerrado_por_defecto`, `exencion_exacta`. The gateway route `bcs-plataforma-web` already exposes only `/plataforma/**`, `/PLA/call`, `/PLA/CallBatch`.
- **Bootstrap login vs the public login page of the legacy** (HANA QA, read only; `equivalence/login-legacy-qa-20261026/legacy-login-page.html` vs `nuevo-login.json`): the 11 JSON constants are equal, the text constants are equal, and the one constant the legacy page has and this lacked (`CONF_IMAGEN_FONDO`, a fixed text in `BootstrapServlet`) was added to both bootstraps. Left to the UI: `BCS_FULLMENU*`, `BCS_MENU_AGENTE`, `BCS_VENTANA`. `empresas` and `logo` are extra keys (the legacy page fetches them elsewhere).
- **Recovery (`RecuperacionClave`)**: its three steps write (provisional password, e-mail through `EVT.AgregarEvento`), so it was NOT executed. Dry run on HANA QA for the test user (`RecuperacionQaTest`, read-only connection, UPDATE swallowed, events procedure not called): the reading part runs on the real tables and answers 200 `CLAVE ENVIADA`; the input it would send to `EVT.AgregarEvento` has the column names of the real table types (checked in `SYS.PROCEDURE_PARAMETER_COLUMNS`). Two differences from the legacy found by reading `RecuperacionClave.java`, `AgregarNotificacion.java` and the `cl.bcs.nuam.StoredProcedure` jar, and fixed: the legacy passes `''` (not NULL) as `P_APLICAORIGENEVENTO`, and a NULL person, e-mail address, company or user id made `Long.parseLong` fail, i.e. the error answer.
- **Known difference, not fixed**: the legacy `StoredProcedure` (`HProcedure.addProperty`) returns integer OUT values as JSON numbers; `ProcedureDispatcherService` returns every scalar OUT as a JSON string. The values the login reads (error counters, e-mail, dates) are read as text, so nothing observable changes in the login answers, but a caller that expects numbers from the dispatcher would see strings.
- **Still not proven on HANA**: the TABLE-IN binding of the dispatcher (`EVT.AgregarEvento` is the only login user of it; its column types are BIGINT / SECONDDATE and the dispatcher binds text), and every write of recovery and password change.

### Writes against the legacy and HANA QA with a dedicated test user (2026-10-26, 14:20-14:55 UTC)
User `MAURICIO.VILLAGRAN` (owner's own test account; password unchanged at the end, verified in the database). The legacy was called over HTTP (`qa.optimuscb.cl:9045`) and the modernized service ran locally against the same HANA QA; the same steps on both, state read after each step (results in `analysis/.../login-legacy-qa-20261026/mauricio/escrituras/`).
- **CrearSesion:** 5 wrong passwords → 401 with the same body, counter 1..4, then the 5th blocks 15 minutes and resets the counter to 0, on both. The right password during the block → 401, same body, on both (each side honoured the block the other had made). After the block, the right password → 200 with the same keys and order on both.
- **Recovery by token:** `SolicitudRecuperacionClave` (known user: `statusCode 0` + masked address `S*******@B**************.C**`; unknown user: `statusCode 1`), `ConfirmarCambioClave` with a wrong token (`statusCode 3`) and with the right one (`0`), same on both. The new password sent was the SHA-256 hex of the SAME password, and the stored value kept matching the double SHA-256 of it: this confirms the form the UI must send.
- **Provisional password:** `RecuperacionClave` (200 CLAVE ENVIADA; wrong channel 401), `ValidaClaveProvisoria` wrong (`Código Verificador Inválido.`, counter +1) and unknown user (200 empty), same on both.
- **ActualizarClave refusals** (different, empty, policy): same texts; mine lists one more requirement ("Al menos 1 dígito"), the approved RULE-032. A successful `ActualizarClave` was NOT run.
- **Three defects of the modernized service found only by these writes, fixed:** (1) the dispatcher bound TABLE IN parameters with a result set, which HANA refuses (`input parameter must be a table`): it now creates a local temporary table with the columns of the parameter type (from `SYS.PROCEDURE_PARAMETER_COLUMNS`), fills it, names it in the call and drops it after, as the legacy helper did; (2) with tables named inline the JDBC index of each `?` is no longer the parameter position: indices are now computed; (3) the failures of the recovery services logged only the exception class: they now include the database message. These were the cause of the `RecuperacionClave` 401 in the first run.
- **What stays in QA from these tests:** sessions and log rows, a recovery token consumed per flow, two events queued by `EVT.AgregarEvento` (recovery e-mails to a system address), a provisional password stored for the user (value unknown, useless to log in), `UltimoCambioClave` untouched by value. Local temporary tables only (session scoped, dropped).

### Differences corrected after the verify (2026-10-26, night)
- **`BCS_MUESTRA_SSO_GOOGLE`**: the legacy page wrote `'" + value + "'`, so with no configured value the constant is the TEXT `null`; the bootstrap now returns the text `"null"` (login and home) instead of a JSON null. All 44 development cases against the legacy pages are now identical.
- **`ActualizarClave` needs a session** (owner decision, deviation from the legacy, which asked for none): the session filter no longer exempts it, and the service refuses (403) a session that is not the one of the user whose password is changed. Tests: `actualizar_clave_exige_sesion`, `LoginHttpTest.rutas_exentas`. Consequence: the provisional-password path (`RecuperacionClave` → `ValidaClaveProvisoria` → `ActualizarClave`) cannot finish without a session; the token path (`SolicitudRecuperacionClave` → `ConfirmarCambioClave`) still can. The comparison cases FW16-FW18 (refusals of `ActualizarClave`) were captured before this change, anonymously; with a session the texts are the same.
- **Still different, not corrected**: `ActualizarClave` with a weak password lists one more requirement, "Al menos 1 dígito" (RULE-032, approved as a separate digit rule): case FW18. Needs a person's decision: approve it, or hide that line.

### Internal router body limit raised to 16 MiB (2026-10-28, F3 W2 CVC architecture review)
- `plataforma.javacall.internal.max-body-bytes` goes from 1048576 (1 MiB) to **16777216 (16 MiB)**, in `application.properties` and in the default of `InternalJavaCallController` (decision of the user). Reason: a `CargarValorCuota` load of thousands of series (a body of several MiB) was a 400 `Request body too large` at the router, before reaching the module. The reading is still bounded (`readNBytes(max + 1)`), so a body over the limit stays a 400; the memory held per request grows to at most 16 MiB (plus the copy of the string and the one forwarded).
- No test pinned 1048576 or the message (`InternalJavaCallControllerTest` builds the controller with its own limit, 200), so no test changed. `README.md` updated (the limit and the property table).
