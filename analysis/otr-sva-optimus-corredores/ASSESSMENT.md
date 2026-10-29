# Assessment — otr-sva-optimus-corredores

> **Secrets note:** Credential values discovered during this assessment are stored in
> `~/.modernize/otr-sva-optimus-corredores/SECRETS.local.md` (gitignored; not for sharing).
> All credential references in this document are masked.

---

## Executive Summary

`otr-sva-optimus-corredores` is a large Java EE monolith — 3,622 Java source files (791 KSLOC) plus 12,113 SQL files and 1,735 JavaScript files — running on Apache TomEE 8 and SAP HANA, implementing a Chilean broker-dealer platform covering orders, custody, settlement, mutual funds, regulatory reporting, and billing. The codebase is **actively evolving** (291 commits in the last month, 4,484 in 2026) with well-structured financial domain modules, but carries three compounding liabilities: a **critical unauthenticated remote-code-execution vulnerability** in the job-manager admin endpoint, **production SAP HANA credentials committed to git history**, and **zero automated test coverage** across the highest-risk modules. The recommended modernization pattern is **Rearchitect** → `reimagine`, decomposing the system into domain-aligned services on a modern stack — but two actions must happen first regardless of modernization path: patch the RCE and rotate the credentials.

---

## System Inventory

| Dimension | Value |
|---|---|
| **Artifacts** | `PLUGIN_OPTIMUS.jar` (TomEE shared lib), `PLA.war` (webapp) |
| **Runtime** | Apache TomEE 8.0.16, Java EE 8, JRE 8 |
| **Build** | Gradle 6.3, JDK 11 (builder Docker), bytecode Java 8 |
| **Total files** | 19,479 |
| **Total SLOC** | 3,546,752 |
| **Java files** | 3,622 |
| **Java SLOC** | 791,145 |
| **SQL files** | 12,113 (995,738 SLOC — DML/queries, no DDL found) |
| **JavaScript** | 1,735 files, 1,343,425 SLOC (frontend) |
| **HTML** | 1,568 files, 145,490 SLOC |
| **SAP HANA views** | 156 `.calculationview` + 10 `.attributeview` |
| **Report templates** | 182 Crystal Reports (`.rpt`, binary), 131 JasperReports (`.jrxml`) |
| **Test files** | 105 (~2.9% of Java files — see Technical Debt §2) |
| **Javadoc coverage** | 15.1% of Java files have an opening `/**` comment |
| **Git activity** | 4,484 commits in 2026; 291 in last month |
| **CI** | Jenkins (`Jenkinsfile`) → AWS EKS (`optimus` namespace, `us-east-1`) |
| **Monitoring** | Datadog APM (dd-java-agent 0.50.0) |

### Key frameworks

| Category | Library | Version | Status |
|---|---|---|---|
| DI/AOP | Spring Framework | 4.3.10.RELEASE | **EOL Dec 2020** |
| SOAP | Apache CXF | 3.2.0 | **EOL, CVEs** |
| Web/auth | Java Servlet API | 4.0 | Current for Java EE 8 |
| Data access | Apache DBCP2, sql2o, jtds, mssql-jdbc, ngdbc | Various | Multiple old |
| Reports | JasperReports | 6.3.0 | Old but functional |
| Reports | Crystal Reports | Proprietary JARs | No source, binary-only |
| PDF | iText | 5.5.13 | **CVEs, EOL** |
| Logging | Log4j 2 | 2.17.0 | ✅ Safe |
| **Logging** | **Log4j 1.x** | **1.2.6 (libs-private)** | **EOL, unpatched RCE CVEs** |
| Observability | Datadog Java agent | 0.50.0 | Old |

### Private JARs (no Maven coordinates, binary-only)

26 JARs in `libs-private/` committed to the repository. Notable entries:
- `CrystalReportsRuntime.jar`, `CrystalCommon2.jar`, `cvom.jar`, `pfjgraphics.jar`, `webreporting.jar` — SAP Crystal Reports (proprietary, no source)
- `ngdbc-2.8.12.jar` — SAP HANA JDBC driver
- `kaiser-odata-1.12.01.jar` — internal OData adapter (not on Maven Central)
- `storedprocedure-2.0-SNAPSHOT.jar` — internal SNAPSHOT
- `dd-java-agent.jar` — Datadog runtime agent
- `log4j.jar` — Log4j **1.2.6** (unversioned, EOL — see Security Findings)

---

## Architecture at a Glance

See `ARCHITECTURE.mmd` for the full domain dependency diagram.

> **Critical architectural finding:** `CallServlet` dispatches ALL business service calls via **runtime reflection** using class names stored in the SAP HANA table `BSCL.Plataforma.BD::PLA.Servicio`. The 1,817+ `JavaCall`-implementing classes are loaded by name from the DB at request time — **no static analysis tool can enumerate the full service catalogue without database access.** This has direct implications for modernization: the service registry itself must be migrated as a first-class artifact.

### Domain inventory

| Domain | Modules | Java files | Key classes | Role |
|---|---|---|---|---|
| **Plataforma** | BcsUtils, BcsFilters, BcsPlataformaWeb, BcsPlataformaServicios, BcsValo2, BcsPlataformaReporteApi | 995 | `CrearSesion`, `SSOUtils`, `BootstrapServlet`, `CallWebsocket`, `BcsJobManager` | Platform infrastructure: auth/SSO, session management, job scheduler, reporting API, valuation utilities, web framework |
| **Normativa** | BcsNormativaServicios, BcsNormativaJobs, BcsCartolas, BcsCartolasMaster, BcsCartolasJobs, BcsTributarioJobs | 603 | `GenerarCartola`, `ProcesarTributario`, `GenerarCartolasMaster` | Regulatory reporting, tax processing, client account statements |
| **SocioNegocio** | BcsSocioNegocioServicios | 571 | `CrearClienteFullWS`, `ModificarSocioNegocio`, `ConsultarSocioNegocio` | Counterparty/client lifecycle management |
| **Custodia** | BcsCustodiaServicios | 323 | `ConsultarCustodiaPortafolio`, `EjecutarVencimiento`, `ProcesarEventoCapital`, `RetiroCustodia` | Securities custody, corporate events, settlement |
| **Orden** | BcsOrdenServicios, BcsOrdenJobs | 224 | `CrearOrdenes`, `CrearAsignaciones`, `CrearTransacciones`, `ProcesamientoTransaccionesOperadores` | Order management, market data, trade processing |
| **FondoMutuo** | BcsFondoMutuoServicios | 193 | `CertificarSolicitudAporteRescate`, `ProcesarFondoMutuo` | Mutual fund subscriptions and redemptions |
| **Tesoreria** | BcsTesoreriaServicios, BcsTesoreriaWebServices | 146 | `SvcRecaudaInqV1`, `SvcRecaudaTrnV1` | Treasury operations, cash collection SOAP services |
| **Mesa** | BcsMesaServicios | 141 | `CrearOperacionSpot`, `ProcesarMesa` | Trading desk operations, spot transactions |
| **Facturacion** | BcsFacturacionServicios | 118 | `CrearComprobante`, `ProcesarFacturacionCustodia`, `WsConsultaPDF` | Billing, invoicing, DTE e-invoicing integration (Chilean SII) |
| **Contabilidad** | BcsContabilidadServicios | 115 | `CentralizaMultiSeleccion`, `CentralizaAnticipoPactos` | Accounting centralization, journal entries |
| **APV** | BcsAPVServicios | 77 | `GrabarContratoSAAPV`, `ProcesarAPV` | Voluntary pension savings (APV) — Chilean regulation |
| **CuentaInversion** | BcsCuentaInversionServicios | 68 | `AnularCargoAbono`, `ProcesaIndicesAtribution` | Investment accounts, attribution analysis |
| **Portafolio** | BcsPortafolioServicios | 25 | `ConsultarPortafolio` | Portfolio inquiry |
| **Corredor** | BcsCorredor (BECB + SURA) | 15 | `Servicios` | Broker integrations (BECB, SURA reinsurers) |

### Service dispatch architecture

`CallServlet` (`/call`) is the single HTTP entry point for all business operations. It routes on the `t` (service type) parameter:

| Type | Dispatch | Notes |
|---|---|---|
| `P` | SAP HANA stored procedure | JDBC call via `ngdbc` driver |
| `Q` | SAP HANA OData query | Via `kaiser-odata` internal library |
| `J` | JavaCall (reflection) | `Class.forName(name).newInstance()` — name from `PLA.Servicio` DB table |
| `X` | JavaView (paginated) | Same reflection, adds offset/limit |
| `O` | OData proxy | |

The `PLA.Servicio` HANA table contains the mapping from service ID to fully-qualified class name for the 1,817+ `JavaCall` implementations. **This catalogue is not visible from source code alone** and must be exported from the database before `reimagine` can produce a complete service inventory.

Session management: `SesionFilter` validates `X-SESSION-TOKEN` on `/*`. Session token is stored in HANA (`ACC.Sesion`). Login is multi-channel: `COR` (corredores/broker), `AGE` (agent/negocios), `CLI` (client), `WS` (web services).

### Build gap — orphaned modules (NOT compiled by Gradle)

The Gradle build (`build.gradle` sourceSets) is an **incomplete migration** from ANT. Several modules are present in the repo with source code but are excluded from the build:

| Module | Java files | Gap |
|---|---|---|
| `BcsJobManager` + domain `*Jobs/` dirs | 153+ | Quartz scheduler + all batch jobs — built by separate ANT scripts not migrated to Gradle. Jobs are a significant operational component. |
| `BcsPlataformaServlets` | ~20 | 7+ servlets with `@WebServlet` annotations (`/valo2/*`, `/cartolas/*`, `/files/*`, `/mail/*`) — **absent from PLA.war**, causing 404 at those paths in production. |
| `BcsPortalConfirmacionWeb` | 4 | Separate notification/e-mail confirmation portal WAR |
| `BcsCanalCliente` | 1 | Client-channel login servlet |
| `BcsCargasPeru` | 72 | Perú-market file-ingestion framework |
| `BcsTributarioJobs` | ~50 | Tax/IPC batch jobs |

### Integration points

| Type | Count | Details |
|---|---|---|
| HTTP Servlets | 57 files | Auth (SSO/Google/BOFA), session, file download, reporting, ADMIN (unauthenticated!) |
| CXF SOAP services (exposed) | 2 | Treasury: `SvcRecaudaInqV1`, `SvcRecaudaTrnV1` |
| CXF SOAP clients (consumed) | 6+ stubs | Custodia: `EjercicioBloqueo`, `sorteoContratos`; Orden: `AdditionalTradeInformation2` (BOVESPA) |
| HTTP API clients | 10+ files | DTE e-invoicing (SII), Datadog, valuation services, Jenkins trigger, blockchain login |
| Batch jobs | 153+ (ANT-only build) | Quartz-based (`BcsJobManager`), order processing, market data, tax jobs |
| SMTP/FTP/SFTP | 3 | Mail notifications, file extraction, archive transfer |

---

## Production Runtime Profile

No APM MCP server is connected. Datadog is configured at runtime (dd-java-agent 0.50.0) but no traces, dashboards, or logs are accessible in this environment. **No telemetry is available.**

Git activity is used as a proxy for operational risk:
- The **Orden** and **Custodia** domains show the highest recent commit frequency (order processing, settlement).
- **Normativa** (regulatory reporting) sees periodic bursts aligned with regulatory submission deadlines.
- **SocioNegocio** shows continuous change — client management is a high-churn area.

---

## Technical Debt

*Top 10 by remediation value. Credential values masked; full inventory in `~/.modernize/otr-sva-optimus-corredores/SECRETS.local.md`.*

| # | Item | Severity | File:line | Description |
|---|---|---|---|---|
| 1 | **Hardcoded credentials — 74+ occurrences** | High | `connections.properties:14,22,30,38,46,54`; `ExtractorArchivosJob.java:106–123`; `ConnectorsOrders.java:79`; `Tools.java:642`; `Connectors.java:78`; 25+ additional files | Production SAP HANA/SQL Server passwords stored as string literals across 30+ source files. The same shared password (`Bo****`) appears ~70 times. Externalize to AWS Secrets Manager or Vault. |
| 2 | **Zero automated test coverage** | High | `build.gradle:247` (JUnit declared but unused); 16 of 22 business modules have 0 test files | No safety net for refactoring or upgrades. Highest-risk modules (`CrearOrdenes`, `ProcesarFacturacionCustodia`, `ModificarSocioNegocio`) are completely untested. Write characterization tests before any modernization work. |
| 3 | **Vulnerable dependencies — Log4j 1.x, Groovy 2.4.7, Spring 4.3.10 EOL** | High | `libs-private/log4j.jar` (v1.2.6); `build.gradle:167` (Groovy 2.4.7 — CVE-2016-6814 CVSS 9.8 RCE); `build.gradle:231` (Spring 4.3.10 EOL) | Log4j 1.x has no patch available — must remove. Groovy 2.4.7 carries an unpatched RCE via deserialization. Spring 4.3.10 is end-of-life since December 2020. |
| 4 | **God objects — 6 classes > 3,000 lines** | High | `CrearOrdenes.java` (5,779L, 521 decisions); `ProcesarFacturacionCustodia.java` (5,384L, 406 decisions); `CrearComprobante.java` (5,174L, 479 decisions); `ConsultarCustodiaPortafolio.java` (4,001L, 251 decisions); `CrearClienteFullWS.java` (3,557L); `ModificarSocioNegocio.java` (3,122L, 264 decisions) | Full subsystem logic compressed into single classes. Merge conflicts frequent; testing impossible. Apply strangler-fig extraction per responsibility. |
| 5 | **Swallowed exceptions — 693 auto-generated catch stubs** | High | `ProcesamientoTransaccionesOperadores.java:243`; `GenerarArchivoSaldoCuentas.java:651`; `ModificarEstadoEventoAutorizacionMasivo.java:1039`; `TransactionManager.java:542`; 693+ `catch (Exception e) {}` across codebase | Financial transaction failures silently discarded. Operators cannot detect partial data corruption. Replace with `logger.error("…", e)` at minimum; propagate failures in financial paths. |
| 6 | **SQL injection via string concatenation** | High | `ConnectorsOrders.java:165,623`; `TransactionManager.java:525–535`; `Deploy.java:1296,1301,1663` | UPDATE/INSERT/SELECT statements built by concatenating runtime values. Some paths use DB-config values (second-order injection). Replace all with `PreparedStatement` bind parameters. |
| 7 | **Thread-unsafe static state in servlet / WebSocket** | High | `CallWebsocket.java:47` (`static HashMap<String,WSession>` — not thread-safe under concurrent connections); `CallServlet.java:74` (mutable `static String muestraLog` in `HttpServlet`) | Race conditions on session map could silently cross-associate trader sessions under load — data confidentiality breach. Replace `HashMap` with `ConcurrentHashMap`; move shared state to `@ApplicationScoped` CDI bean. |
| 8 | **Dead modules with credentials in git** | Medium | `BcsDeploy/Tools.java:642`; `BcsPortalConfirmacionWeb/.../NotificationsValidate.java:30`; `kaiser-odata/FilterMain.java:29` | 400+ Java files not compiled into the production build (`sourceSets.main`) but tracked in git, containing production credentials and hardcoded IP addresses. Archive separately or delete. |
| 9 | **Spring classpath collision — 2.5.6 (private JAR) + 4.3.10 (Maven)** | Medium | `build.gradle:111` (acknowledges v2.5.6 in `libs-private`); `build.gradle:231–236` (Spring 4.3.10) | Both Spring versions land on `implementation` classpath. TomEE classloading order determines which loads first — silent semantic regression possible. Remove `org.springframework.beans.jar` and `org.springframework.core.jar` from `libs-private/`. |
| 10 | **3DES encryption for financial data (NIST deprecated 2023)** | Medium | `OpenEJBEncryption.java:12`; `AnotacionPortafolioConcrete.java:32` | Portfolio annotations and EJB credentials encrypted with DESede (SWEET32 attack, CVE-2016-2183). Replace with AES/GCM/NoPadding (256-bit); Java 8 JCE includes it natively. |

---

## Security Findings

*Credential values in `~/.modernize/otr-sva-optimus-corredores/SECRETS.local.md`.*

| CWE | Title | Severity | File:line | Recommended Fix |
|---|---|---|---|---|
| CWE-78 | **Unauthenticated OS Command Injection (RCE)** | **Critical** | `BcsJobManager/…/WebAdmin.java:122–125` | The `/admin` servlet accepts `exe` and `path` parameters and passes them verbatim to `Runtime.getRuntime().exec(…)`. No session filter is applied to the JOBMANAGER WAR. Any unauthenticated HTTP client can execute arbitrary shell commands as the TomEE process user. **Take offline immediately.** Remove the `Action=command` handler; require mutual TLS or network ACL; add `SesionFilter`. |
| CWE-798 | **Hardcoded SAP HANA credentials in git (production-tier)** | **Critical** | `connections.properties:46,54` (MASTER01/MASTER02) | File is tracked in git despite `.gitignore` entry. Six environment credential sets exposed, including production. `git rm --cached connections.properties`, rotate all credentials, purge history with BFG. See credential inventory in SECRETS.local.md. |
| CWE-798 | **Hardcoded credentials — 74+ occurrences across 30+ Java files** | **Critical** | `ExtractorArchivosJob.java:106–123` (incl. SYSTEM account); `ConnectorsOrders.java:79`; `AsignacionAutomaticaJob.java:297–299`; 25+ more | Single shared password pattern (`Bo****`) used as application DB password throughout the codebase. SYSTEM superuser credentials at `ExtractorArchivosJob.java:123`. Move all to AWS Secrets Manager / environment variables. |
| CWE-532 | **FTP/SFTP password written to job log** | High | `ExtractorArchivosJob.java:62` | `ftpPassword` logged in plaintext. Remove from log statement; log only hostname and username. |
| CWE-89 | **SQL Injection — string concatenation** | High | `ConnectorsOrders.java:165,623,701`; `TransactionManager.java:525–535` | UPDATE/INSERT/SELECT built with runtime value concatenation. Use `PreparedStatement` everywhere. |
| CWE-916 | **Unsalted SHA-256 for password storage** | High | `CrearSesion.java:277`; `CrearUsuario.java:264`; `ActualizarUsuario.java:382`; `CambiarClaveUsuario.java:130–137` | Passwords stored as `DigestUtils.sha256Hex(…)` without per-user salt. Vulnerable to rainbow tables and GPU dictionary attacks. Migrate to BCrypt (cost ≥ 12) via `BCryptPasswordEncoder`. |
| CWE-276 | **Missing access control on JOBMANAGER admin endpoint** | High | `ANT/config/Jobmanager.xml` (web.xml); `WebAdmin.java:24` | JOBMANAGER WAR has no `<filter-mapping>`. All admin actions (`restart`, `tomcatlog`, `joblog`, `command`) accessible to unauthenticated callers. Apply `SesionFilter` or network-layer ACL. |
| CWE-209 | **Verbose error responses leaking paths and stack traces** | High | `FileDownload.java:149`; `CallReportServlet.java:526` | File paths and full stack traces returned to HTTP clients. Log server-side only; return fixed non-disclosing message. Ensure `debug=false` in production. |
| CVE-2023-24998 / CWE-400 | **commons-fileupload 1.3.3 — unbounded upload DoS** | Medium | `build.gradle:132` | Upgrade to 1.5+; add `@MultipartConfig(maxFileSize=…)`. |
| CVE-2018-1270 / CWE-1104 | **Spring 4.3.10.RELEASE — multiple CVEs, EOL** | Medium | `build.gradle:231–236` | Upgrade to Spring 5.3.x (LTS), then plan Jakarta namespace migration to 6.x. |
| CVE-2017-12624 / CWE-1104 | **Apache CXF 3.2.0 — MTOM DoS, TLS bypass, JWT validation** | Medium | `build.gradle:142–148` | Upgrade to CXF 3.5.x or 4.0.x. |
| CVE-2017-9096 / CWE-1104 | **iText 5.5.13 — XXE in PDF/XMP parsing, EOL** | Medium | `build.gradle:173` | Migrate to iText 7 or use PDFBox 3.0.2 (already declared). |
| CVE-2019-17571 / CWE-1104 | **Log4j 1.2.6 in libs-private — unpatched RCE (CVSS 9.8), EOL** | Medium | `libs-private/log4j.jar` | No patch exists for 1.x. Remove the JAR; use Log4j2 2.17.0 (already declared in `build.gradle`). |
| CVE-2022-25647 / CWE-1104 | **Gson 2.6.2 — stack overflow on deserialization** | Medium | `build.gradle:163` | Upgrade to 2.10.1 or later. |
| CVE-2016-6814 / CWE-1104 | **Groovy 2.4.7 — RCE via deserialization (CVSS 9.8)** | Medium | `build.gradle:167` | Upgrade to Groovy 3.x or 4.x. |
| CVE-2023-2976 / CWE-1104 | **Guava 18.0 — insecure temp file creation** | Medium | `build.gradle:164` | Upgrade to 32.1.3-jre or later. |
| CWE-312 / CWE-287 | **CallBatch security token logged, insecure default fallback** | Medium | `CallBatch.java:92–94` | On DB init failure falls back to literal `"CALLBATCH"` token and logs it. Fail securely; never log token values. |
| Multiple CVEs | **jackson-databind 2.1.0 (private JAR) — deserialization gadget chains** | Medium | `libs-private/com.fasterxml.jackson.databind.jar` | Pre-dates all polymorphic deserialization fixes. Replace with 2.15.x; disable default typing globally. |

---

## Documentation Gaps

Top 5 behaviors a new engineer would need explained:

1. **The PLUGIN_OPTIMUS.jar / PLA.war split** — There is no document explaining which classes go into which artifact, or why the split exists. The logic is embedded in Gradle task `from(sourceSets.main.output.classesDirs) { exclude 'cl/bcs/sql/**' … }` with no accompanying explanation of the runtime contract.

2. **SAP HANA calculation views as domain logic** — 156 HANA calculation views implement significant business logic (portfolio valuations, position aggregations) that is invisible to the Java code readers. There is no document mapping Java service calls to HANA view names, no schema overview, and no DDL for the relational tables those views query.

3. **The `connections.properties` environment model** — Six named environments (desa01, desa02, qa01, qa02, master01, master02) are used by the `PlataformaUtils.getPropertiesConnection("env", …)` pattern. No document explains which code paths use which environment, how the environment selector is passed at runtime, or what "master" vs "qa" means in production.

4. **The Quartz-based BcsJobManager scheduler** — The job scheduler (`BcsJobManager`) runs batch jobs across all business domains (normativa tax jobs, order processing, custody events). There is no documentation of the job catalog, its cron schedule, dependencies between jobs, or what happens on failure.

5. **Crystal Reports integration** — 182 `.rpt` binary files implement report templates. No document explains the report catalog, which servlet/service triggers which report, how the Crystal Reports runtime is configured, or how reports are parameterized. This is effectively a black box within the application.

---

## Relative Scale

| Metric | Value |
|---|---|
| Java KSLOC | 791 |
| Total KSLOC | 3,547 |
| COCOMO index (Java SLOC) | **4,533** |
| COCOMO index (total SLOC) | **23,614** |
| Tool used | `cloc` 2.04 |

**The COCOMO index is a relative size measure for ranking this system against others in a portfolio. It is not a timeline, not a cost, and not a person-month estimate.** Agentic modernization productivity is not modeled by COCOMO; the index is printed here solely to enable side-by-side comparison across systems.

---

## Recommended Modernization Pattern: Rearchitect → `reimagine`

**Pattern:** Rearchitect (domain-driven decomposition into independently deployable services on a modern Java stack)

**Rationale:**

The system's domain model is already well-articulated: 14 distinct business modules, each with a clear financial purpose (orders, custody, mutual funds, treasury, regulatory reporting, etc.). The Gradle build already treats them as logically separate compilation units. The blocker is not architectural imagination — it is the absence of a safety net. With **zero automated tests** across the highest-risk business modules and **critical security vulnerabilities that need immediate attention**, a direct reimagine is premature.

The recommended sequencing is:

1. **`/code-modernization:modernize-harden`** — Apply the security remediation patch immediately (rotate credentials, take `/admin` offline, remove Log4j 1.x JAR, fix unsalted SHA-256). This is independent of modernization path and cannot wait.

2. **`/code-modernization:modernize-extract-rules`** — Mine the business rules from the God-object classes (`CrearOrdenes`, `ProcesarFacturacionCustodia`, `ModificarSocioNegocio`, `ConsultarCustodiaPortafolio`) into Given/When/Then specifications. These become the acceptance criteria for the reimagined services.

3. **`/code-modernization:modernize-map`** — Produce the full dependency and data-flow map to verify domain boundaries before drawing service boundaries in the reimagine brief.

4. **`/code-modernization:modernize-reimagine`** — Rebuild domain by domain on Spring Boot 3 + Java 21 + Jakarta EE, with each domain as an independently deployable service. Priority order: Orden → Custodia → SocioNegocio → Normativa → Plataforma (platform last — it is the most cross-cutting).

**SAP HANA data layer:** 156 calculation views represent significant business logic that must be preserved or migrated. The `reimagine` brief must make an explicit decision per view: retain in HANA (JDBC call from new service), reimplement in service layer, or migrate to a different query engine.

**Service registry pre-condition:** The `PLA.Servicio` HANA table maps service IDs to the 1,817+ `JavaCall` class names. This registry must be exported and included in the modernization brief — without it, the service surface of the reimagined system is incomplete.

**Batch/scheduler gap:** The Gradle build does not compile the job scheduler or any domain batch jobs (BcsJobManager, 153+ `*Jobs` classes). These are a significant operational component. The `reimagine` scope must explicitly include the batch tier; currently it is invisible to Gradle-based analysis.

**Crystal Reports:** 182 binary `.rpt` files have no source. A parallel decision is required: migrate to JasperReports (source available), retain Crystal Reports runtime in containerized form, or replace with a reporting API. This affects roughly 30 servlet endpoints that render reports.

**BcsPlataformaServlets gap:** 7+ servlets exist in source but are not compiled into PLA.war — the paths `/valo2/*`, `/cartolas/*`, `/files/*`, `/mail/*` return 404 in the current build. Confirm whether this is intentional (features disabled) or an ANT→Gradle migration oversight before reimagining.

**Consumers:** The user confirmed this system has external runtime consumers that must not break. API versioning or a compatibility facade must be in the `brief` before any service boundary is cut.

**Build command:** `/code-modernization:modernize-reimagine otr-sva-optimus-corredores`
(After completing `harden`, `extract-rules`, and `map` first.)
