# Business Rules — otr-sva-optimus-corredores / Plataforma Core

**Scope:** BcsPlataformaWeb · BcsPlataformaServicios · BcsFilters · BcsJobManager  
**Method:** Three parallel lens-extractors (Calculations, Validations/Eligibility, State/Lifecycle); citations spot-verified.  
**Legend:** ⚠️ = suspected defect confirmed in source; 🔴 = security risk; SME = question for a subject-matter expert.

---

## Summary Table

| Rule | Name | Category | Priority | Source | Confidence |
|------|------|----------|----------|--------|------------|
| RULE-001 | Session token required for all non-exempt requests | Authentication | P0 | BcsFilters/SesionFilter.java:69-118 | High |
| RULE-002 | Session token must reference an active DB session | Authentication | P0 | BcsFilters/SesionFilter.java:76-99 | High |
| RULE-003 | ServiceFilter URL / service / channel bypass whitelist | Authorization | P0 | BcsFilters/ServiceFilter.java:93-140 | High |
| RULE-004 | ⚠️🔴 ServiceFilter `ch != null` bypasses all role auth | Authorization | P0 | BcsFilters/ServiceFilter.java:140 | High |
| RULE-005 | ⚠️🔴 ServiceFilter fail-open: exceptions grant access | Authorization | P0 | BcsFilters/ServiceFilter.java:214-217 | High |
| RULE-006 | ServiceFilter RBAC: role–profile–service access check | Authorization | P0 | BcsFilters/ServiceFilter.java:190-212 | High |
| RULE-007 | ServiceFilter validation mode (NADIE / LISTA / TODOS) | Authorization | P0 | BcsFilters/ServiceFilter.java:161-185 | High |
| RULE-008 | Login: user must exist for the given company | Eligibility | P0 | BcsPlataformaWeb/CrearSesion.java:241-255 | High |
| RULE-009 | Login: temporary lockout blocks login | Eligibility | P0 | BcsPlataformaWeb/CrearSesion.java:258-259 | High |
| RULE-010 | Login: permanent block (BLO) blocks login | Eligibility | P0 | BcsPlataformaWeb/CrearSesion.java:262-264 | High |
| RULE-011 | Login: pending authorization (EsAutorizado=P) blocks login | Eligibility | P0 | BcsPlataformaWeb/CrearSesion.java:267-269 | High |
| RULE-012 | Login: expired account (FechaVigencia) blocks login | Eligibility | P0 | BcsPlataformaWeb/CrearSesion.java:272-274 | High |
| RULE-013 | Login: password verified by SHA-256 (platform-charset bytes) | Calculation | P0 | BcsPlataformaWeb/CrearSesion.java:277 | High |
| RULE-014 | Login: failed-attempt counter increments on wrong password | Calculation | P0 | BcsPlataformaWeb/CrearSesion.java:279-286 | High |
| RULE-015 | Login: temporary lockout after N failed attempts | Calculation | P0 | BcsPlataformaWeb/CrearSesion.java:280-295 | High |
| RULE-016 | Login: failed-attempt counter resets on successful login | Lifecycle | P0 | BcsPlataformaWeb/CrearSesion.java:383-390 | High |
| RULE-017 | Login: channel / entity-type authorization matrix | Eligibility | P0 | BcsPlataformaWeb/CrearSesion.java:301-340 | High |
| RULE-018 | ⚠️🔴 Login: CLI entity with BLO status can log in | Eligibility | P0 | BcsPlataformaWeb/CrearSesion.java:331 | High |
| RULE-019 | Login: entity enrollment must be ACT | Eligibility | P0 | BcsPlataformaWeb/CrearSesion.java:199-211 | High |
| RULE-020 | Login: WS channel reuses existing valid token | Lifecycle | P0 | BcsPlataformaWeb/CrearSesion.java:346-381 | High |
| RULE-021 | Login: session token format (company:user:timestamp+UUID, Base64) | Calculation | P1 | BcsUtils/Sesion.java:184-185 | High |
| RULE-022 | Login: unique-session policy per channel | Lifecycle | P1 | BcsUtils/Sesion.java:156-170 | High |
| RULE-023 | Service call: token validity + channel match | Authentication | P0 | BcsUtils/ProcesaLlamadaServletCall.java:113-135 | High |
| RULE-024 | Service call: idle session timeout by channel | Threshold | P0 | BcsUtils/SesionUtils.java:42-81 | High |
| RULE-025 | Service call: COR channel is exempt from idle timeout | Policy | P0 | BcsUtils/ProcesaLlamadaServletCall.java:123 | High |
| RULE-026 | Service call: user must be active and unexpired | Eligibility | P0 | BcsUtils/ProcesaLlamadaServletCall.java:156-168 | High |
| RULE-027 | Service call: service ID must exist in PLA.Servicio catalog | Validation | P0 | BcsUtils/ProcesaLlamadaServletCall.java:104-111 | High |
| RULE-028 | Service call: per-service ACL enforcement flag | Authorization | P0 | BcsFilters/ServiceFilter.java:190-212 | High |
| RULE-029 | Password hash: change-password path uses double SHA-256 (UTF-8) | Calculation | P0 | BcsPlataformaServicios/CambiarClaveUsuario.java:130-133 | High |
| RULE-030 | ⚠️🔴 Password hash: login and change-password hashing differs | Calculation | P0 | CrearSesion.java:277 vs CambiarClaveUsuario.java:130 | Medium |
| RULE-031 | Password complexity: length, uppercase, lowercase, specials, no spaces | Validation | P0 | BcsUtils/ValidarNuevaClave.java:147-222 | High |
| RULE-032 | ⚠️ Password complexity: digits ignored by scanner | Validation | P0 | BcsUtils/ValidarNuevaClave.java:164-182 | High |
| RULE-033 | ⚠️ Password validation hardcoded to company IdEmpresa=1 | Validation | P1 | BcsUtils/ValidarNuevaClave.java:107 | High |
| RULE-034 | Password: prohibited-word check (exact match) | Validation | P1 | BcsUtils/ValidarNuevaClave.java:127-143 | High |
| RULE-035 | Password: history — cannot reuse any prior password | Policy | P0 | BcsPlataformaServicios/ActualizarUsuario.java:384-396 | High |
| RULE-036 | Password: must-change flag set on admin reset | Lifecycle | P1 | BcsPlataformaServicios/ActualizarUsuario.java:399-415 | High |
| RULE-037 | Password stored as SHA-256 uppercase hex (no salt) | Calculation | P0 | BcsPlataformaServicios/ActualizarUsuario.java:382 | High |
| RULE-038 | Provisional password: valid only on day of issue | Policy | P0 | BcsPlataformaWeb/ValidaClaveProvisoria.java:121 | High |
| RULE-039 | Provisional password: max-attempts lockout | Policy | P0 | BcsPlataformaWeb/ValidaClaveProvisoria.java:122 | High |
| RULE-040 | ⚠️ Provisional password: off-by-one in attempts check | Policy | P0 | BcsPlataformaWeb/ValidaClaveProvisoria.java:122 | High |
| RULE-041 | Password recovery: channel / entity-type authorization | Eligibility | P0 | BcsPlataformaWeb/RecuperacionClave.java:115 | High |
| RULE-042 | Change-password: new and confirm fields must match | Validation | P1 | BcsPlataformaWeb/ActualizarClave.java:89-90 | High |
| RULE-043 | SSO: email must resolve to exactly one UsuarioCorredores user | Eligibility | P0 | BcsPlataformaWeb/SSOUtils.java:91-98 | High |
| RULE-044 | SSO: user eligibility (BLO, EsAutorizado, temp-lock, employee ACT) | Eligibility | P0 | BcsPlataformaWeb/SSOUtils.java:101-124 | High |
| RULE-045 | ⚠️ SSO: FechaVigencia expiry check skipped (comment: --todo) | Eligibility | P0 | BcsPlataformaWeb/SSOUtils.java:112 | High |
| RULE-046 | ⚠️🔴 SSO: OAuth2 CSRF state parameter is hardcoded | Security | P0 | BcsPlataformaWeb/SSOInit.java:101 | High |
| RULE-047 | ⚠️ SSO: company ID hardcoded to 1 in session creation | Policy | P0 | BcsPlataformaWeb/SSOUtils.java:269 | High |
| RULE-048 | SSO: JWT id_token claims provide identity (signature not verified) | Authentication | P0 | BcsPlataformaWeb/SSOUtils.java:259-267 | High |
| RULE-049 | SSO: entity configuration must exist in DB | Validation | P0 | BcsPlataformaWeb/SSOInit.java:79-82 | High |
| RULE-050 | CallBatch: shared-secret token authentication | Authentication | P0 | BcsPlataformaWeb/CallBatch.java:72-101 | High |
| RULE-051 | ⚠️🔴 CallBatch: fallback to hardcoded token "CALLBATCH" | Security | P0 | BcsPlataformaWeb/CallBatch.java:91 | High |
| RULE-052 | CallBatch: LIFO job queue; jobs discarded on server shutdown | Lifecycle | P0 | BcsPlataformaWeb/CallBatch.java:125-198 | Medium |
| RULE-053 | WebSocket: auth required in first message; unauthenticated until then | Lifecycle | P0 | BcsPlataformaWeb/CallWebsocket.java:73-141 | High |
| RULE-054 | WebSocket: poll every 10s, deliver messages with 1-hour lookback | Lifecycle | P1 | BcsPlataformaWeb/CallWebsocket.java:166-256 | High |
| RULE-055 | Job state machine: PEN → ASI → TER / ERR | Lifecycle | P0 | BcsJobManager/TransactionManager.java:41-93 | High |
| RULE-056 | Job lock (Bloqueo=S) set before execution to prevent double-pickup | Lifecycle | P0 | BcsJobManager/TransactionManager.java:68-73 | High |
| RULE-057 | ⚠️ Job lock: FOR UPDATE removed; race condition window exists | Lifecycle | P0 | BcsJobManager/TransactionManager.java:48 | High |
| RULE-058 | Job retry: ERR → PEN after 60-second delay, within 1-hour window | Lifecycle | P0 | BcsJobManager/TransactionManager.java:259-278 | High |
| RULE-059 | ⚠️ Job retry: updateTryProgramm call is commented out in poolDiary | Lifecycle | P0 | BcsJobManager/BcsJobManager.java:105 | High |
| RULE-060 | Job eligibility: unlocked, PEN, below max retries, correct node, date window | Eligibility | P1 | BcsJobManager/TransactionManager.java:41-62 | High |
| RULE-061 | Daemon job: infinite loop with configurable cycle interval | Lifecycle | P0 | BcsJobManager/BcsJob.java:86-160 | High |
| RULE-062 | Daemon restart: terminates all PEN/ASI programs for that agenda | Lifecycle | P0 | BcsJobManager/TransactionManager.java:174-192 | High |
| RULE-063 | Agenda: schedules new programs when next date is due | Lifecycle | P1 | BcsJobManager/BcsJobManager.java:96-158 | High |
| RULE-064 | Node heartbeat written every 10 seconds | Lifecycle | P1 | BcsJobManager/BcsJobManager.java:31,178-181 | High |
| RULE-065 | Zombie node detection after 30 seconds, OS-level kill | Lifecycle | P1 | BcsJobManager/BcsJobManager.java:234-248 | High |
| RULE-066 | Node reboot-on-demand via DB flag (System.exit(0)) | Lifecycle | P0 | BcsJobManager/BcsJobManager.java:183-190 | High |
| RULE-067 | Job result description truncated at 1999 chars | Validation | P2 | BcsJobManager/TransactionManager.java:216-219 | High |
| RULE-068 | ⚠️🔴 SQL injection in TransactionManager.executionCount | Security | P0 | BcsJobManager/TransactionManager.java:526-535 | High |
| RULE-069 | User block/unblock triggers email to security team | Lifecycle | P1 | BcsPlataformaServicios/ActualizarUsuario.java:509-622 | High |
| RULE-070 | Expiry batch: blocks all users with FechaVigencia < FechaSistema | Lifecycle | P0 | BcsPlataformaServicios/BloqueoUsuariosPorVigencia.java:93-149 | High |
| RULE-071 | UTC timestamp cached 1 hour across JVM (all threads) | Policy | P2 | BcsUtils/SesionUtils.java:93-100 | High |
| RULE-072 | HANA connection lock-wait timeout: 60 seconds | Policy | P1 | BcsPlataformaWeb/CallServlet.java:213-215 | High |
| RULE-073 | Error stack trace controlled by PLA.MuestraLog | Policy | P2 | BcsPlataformaWeb/CallServlet.java:265,634-643 | High |
| RULE-074 | ⚠️ PLA.MuestraLog is a static field — thread-safety risk | Policy | P1 | BcsPlataformaWeb/CallServlet.java:74 | High |
| RULE-075 | JavaView default pagination: skip=0, take=1,000,000 | Policy | P2 | BcsPlataformaWeb/CallServlet.java:548-549 | High |
| RULE-076 | ⚠️🔴 Hardcoded HANA credentials in Sesion.java main() (dead code) | Security | P0 | BcsUtils/Sesion.java:304-319 | High |
| RULE-077 | ⚠️🔴 Static3DES with publicly known key used for credential encryption | Security | P0 | BcsJobManager/CypherEncryption.java:11 | High |
| RULE-078 | ⚠️ SSO JWT signature not verified (trust from TLS only) | Security | P0 | BcsPlataformaWeb/SSOUtils.java:255-267 | High |

---

## Rule Cards

All file paths are relative to `legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA/`.

---

### RULE-001: Session Token Required for All Non-Exempt Requests
**Category:** Authentication
**Priority:** P0
**Source:** `BcsFilters/src/cl/bcs/filters/SesionFilter.java:69-118`
**Plain English:** Every HTTP request to the platform must carry a valid session token in the `X-SESSION-TOKEN` header, unless the URL is on a public-access whitelist.

**Specification:**
```
Given an inbound HTTP request
When the request URI is NOT in the noAuthURLs whitelist AND does not start with "/PLA/WebStatic"
Then:
  - X-SESSION-TOKEN header absent → HTTP 400 Bad Request
  - Header present but session invalid → HTTP 401 Unauthorized
  - Session valid → request forwarded
```

**Parameters — Public-Access URL Whitelist (noAuthURLs):**
`/PLA/ValidaClaveProvisoria`, `/PLA/ActualizarClave`, `/PLA/RecuperacionClave`, `/PLA/CrearSesion`,
`/PLA/bootstrap`, `/PLA/LoginCliente`, `/PLA/SolicitudRecuperacionClave`, `/PLA/ConfirmarCambioClave`,
`/PLA/CallBatch`, `/PLA/WebStatic` (also prefix-matched), `/PLA/RepoStatic`, `/PORTAL/validate`,
`/PLA/CallWebsocket`, `/PLA/AuditoriaWS`, `/PLA/Configuraciones`, `/PLA/Ping`,
`/PLA/SSOInit`, `/PLA/SSOLogon`, `/PLA/SSOLogonBofa`, `/PLA/SSOLogonGoogle`, `/PLA/SSOLogonLocalGoogle`, `/PLA/SSOLogout`

**Edge cases:** DB exception during token lookup → HTTP 401 (fail-closed).

**Confidence:** High

---

### RULE-002: Session Token Must Reference an Active Session
**Category:** Authentication
**Priority:** P0
**Source:** `BcsFilters/src/cl/bcs/filters/SesionFilter.java:76-99`
**Plain English:** The token in `X-SESSION-TOKEN` is validated against `ACC.Sesion` with `TokenValido='S'`; expired or revoked tokens are rejected.

**Specification:**
```
Given protected request with X-SESSION-TOKEN header
When SELECT COUNT(1) FROM ACC.Sesion WHERE IdTokenSesion=? AND TokenValido='S'
Then COUNT = 0 → HTTP 401; COUNT > 0 → proceed
```

**Edge cases:** Exception during query → HTTP 401 (fail-closed).

**Confidence:** High

---

### RULE-003: ServiceFilter — URL / Service / Channel Bypass Whitelist
**Category:** Authorization
**Priority:** P0
**Source:** `BcsFilters/src/cl/bcs/filters/ServiceFilter.java:93-140`
**Plain English:** The service-level authorization filter maintains three independent bypass lists: a URL whitelist (same as SesionFilter), a service-ID whitelist, and a channel whitelist. Any match skips the role-based access check.

**Specification:**
```
Given request at ServiceFilter
When ANY of:
  a) URI in noAuthURLs OR starts with "/PLA/WebStatic"
  b) service param t in noAuthServices
  c) t is absent (t == null)
  d) ch != null  ← see RULE-004 (defect)
  e) ch in noAuthChanels
Then forward without role check
```

**Parameters:**
- `noAuthServices` (service IDs exempt from role check): `700729, 600017, 171721, 1381, 14, 1556, 384`
- `noAuthChanels`: `WS`, `CLI`, `AGE`

**Confidence:** High

---

### RULE-004: ⚠️🔴 DEFECT — ServiceFilter `ch != null` Bypasses All Role Authorization
**Category:** Authorization
**Priority:** P0
**Source:** `BcsFilters/src/cl/bcs/filters/ServiceFilter.java:140`
**Plain English:** A logic error means that any request carrying any `ch` query parameter (with any value, not just the three allowed channel codes) bypasses the service-level authorization filter entirely.

**Specification:**
```
Code (verified):
  if (noAuthURLs.contains(URI) || ... || t == null || ch != null || noAuthChanels.contains(ch))

Because "ch != null" short-circuits to true for ANY ch value:
  ?ch=HACKER bypasses auth just as ?ch=WS would

Expected: only ch in {WS, CLI, AGE} should bypass
Actual:   any ch value bypasses
```

**Suspected defect:** Critical authorization bypass. `noAuthChanels.contains(ch)` after `ch != null` is dead code.

**Confidence:** High — Java short-circuit `||` is deterministic. Verified at line 140.

**SME question:** Is allowing all ch-bearing requests through intentional?

---

### RULE-005: ⚠️🔴 DEFECT — ServiceFilter Fail-Open on Exception
**Category:** Authorization
**Priority:** P0
**Source:** `BcsFilters/src/cl/bcs/filters/ServiceFilter.java:214-217`
**Plain English:** If any exception occurs during role-based access checking (DB timeout, null pointer, etc.), the filter catches it, prints a stack trace, and forwards the request as if authorization passed.

**Specification:**
```
catch(Exception e) {
  e.printStackTrace();
  chain.doFilter(request, response);  // ← grants access on error
}

Expected: HTTP 500 or HTTP 401
Actual:   request proceeds as authorized
```

**Confidence:** High

---

### RULE-006: ServiceFilter — Role-Based Service Access Control
**Category:** Authorization
**Priority:** P0
**Source:** `BcsFilters/src/cl/bcs/filters/ServiceFilter.java:190-212`
**Plain English:** For services flagged `ValidaAccesoServicio='S'`, the system queries the role hierarchy to confirm the user has explicit access; access is denied with HTTP 403 if not.

**Specification:**
```
Given service with ValidaAccesoServicio='S'
When role check runs via join:
  ACC.PerfilAcceso → ACC.Acceso → ACC.AccesoServicio
  linked to ACC.Usuario → ACC.RolUsuario → ACC.Rol → ACC.RolPerfil → ACC.Perfil
Then COUNT > 0 → allow; COUNT = 0 → HTTP 403 "Unauthorized Session"
When ValidaAccesoServicio != 'S' → allow unconditionally
```

**Confidence:** High

---

### RULE-007: ServiceFilter — Validation Mode (NADIE / LISTA / TODOS)
**Category:** Authorization
**Priority:** P0
**Source:** `BcsFilters/src/cl/bcs/filters/ServiceFilter.java:161-185`
**Plain English:** `PLA.TipoValidaServicio` controls whether role checks apply to no one, a list, or everyone.

**Specification:**
```
When PLA.TipoValidaServicio = 'NADIE' → all requests proceed (global bypass)
When PLA.TipoValidaServicio = 'LISTA' →
  user NOT in PLA.ListaValidaServicio → bypass
  user IN list → role check applies (RULE-006)
When PLA.TipoValidaServicio = other (incl. 'TODOS') → role check applies to all
```

**Parameters:** `PLA.ListaValidaServicio` — comma-separated user codes (when mode=LISTA).

**Confidence:** High

---

### RULE-008: Login — User Must Exist for Given Company
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:241-255`
**Plain English:** A login attempt is rejected if the company ID is missing or no user with the given username exists in that company.

**Specification:**
```
Given POST /PLA/CrearSesion {p_IdEmpresa, p_NombreUsuario, p_ClaveUsuario, p_IdCanal}
When p_IdEmpresa is null OR no row in ACC.Usuario WHERE IdEmpresa=? AND CodigoUsuario=UPPER(?)
Then HTTP 401 {CODIGO: "USUARIO_CLAVE_INCORRECTA"}
```

**Parameters:** Username is uppercased before lookup.

**Confidence:** High

---

### RULE-009: Login — Temporary Lockout Blocks Login
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:258-259`
**Plain English:** If the user's temporary-lockout timestamp has not yet passed (UTC), login is denied.

**Specification:**
```
Given user with FechaBloqueoTemporal and CurrentTimestamp (CURRENT_UTCTIMESTAMP from DB)
When FechaBloqueoTemporal.after(CurrentTimestamp)
Then HTTP 401 {CODIGO: "USUARIO_CLAVE_INCORRECTA"}
```

**Edge cases:** Self-healing — lock expires automatically without admin intervention.

**Confidence:** High

---

### RULE-010: Login — Permanent Block (BLO) Blocks Login
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:262-264`
**Plain English:** Users with `IdEstado='BLO'` are unconditionally denied login.

**Specification:**
```
Given user with IdEstado='BLO'
When /CrearSesion is called
Then HTTP 401 {CODIGO: "USUARIO_CLAVE_INCORRECTA"}
```

**Confidence:** High

---

### RULE-011: Login — Pending Authorization (EsAutorizado=P) Blocks Login
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:267-269`
**Plain English:** Users with `EsAutorizado='P'` (pending) cannot log in even if their password is correct.

**Specification:**
```
Given user with EsAutorizado='P'
When /CrearSesion is called
Then HTTP 401 before password is checked
```

**SME question:** What are the full enumerated values for `EsAutorizado`? Is 'N' treated as not authorized? The code only blocks 'P'.

**Confidence:** High

---

### RULE-012: Login — Expired Account (FechaVigencia) Blocks Login
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:272-274`
**Plain English:** If a user's expiry date is before the business system date (and is not the sentinel "no expiry" value), login is denied.

**Specification:**
```
Given user with FechaVigencia=V and PLA.FechaSistema=S
When V != "0001-01-01" AND V.before(S)
Then HTTP 401 {CODIGO: "USUARIO_CLAVE_INCORRECTA"}
When V == "0001-01-01" → expiry check skipped (never expires)
```

**Parameters:**
- Sentinel "no expiry": `"0001-01-01"` (string comparison)
- System date: `PLA.FechaSistema` (business date config, NOT wall-clock date)

**Confidence:** High

---

### RULE-013: Login — Password Verified by SHA-256
**Category:** Calculation
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:277`
**Plain English:** The submitted password is hashed with SHA-256 and compared against the stored hash.

**Specification:**
```
Given submitted claveUsuario
When DigestUtils.sha256Hex(claveUsuario.getBytes()).toUpperCase() != ACC.Usuario.Clave
Then increment failed attempt counter (RULE-014) → HTTP 401
```

**Parameters:**
- Algorithm: SHA-256; encoding: platform-default charset (not explicitly UTF-8)
- Result: uppercase hex

**Suspected defect:** See RULE-030 — login and change-password paths use different hash schemes.

**Confidence:** High

---

### RULE-014: Login — Failed-Attempt Counter Increments on Wrong Password
**Category:** Calculation
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:279-286`
**Plain English:** Every wrong-password attempt increments `ACC.Usuario.IntentosConexion` by 1.

**Specification:**
```
Given password mismatch for user U with IntentosConexion=N
When UPDATE ACC.Usuario SET IntentosConexion=N+1 WHERE IdUsuario=?
Then if N+1 >= PLA.ClaveUsuarioIntentos → apply temporary lock (RULE-015)
```

**Edge cases:** If `PLA.ClaveUsuarioIntentos` is null/unconfigured, lockout never fires; counter still increments.

**Confidence:** High

---

### RULE-015: Login — Temporary Lockout After N Failed Attempts
**Category:** Calculation
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:280-295`
**Plain English:** When the failed-attempt count reaches the configured maximum, the account is temporarily locked for a configurable number of minutes and the counter is reset to zero.

**Specification:**
```
Given IntentosConexion+1 >= PLA.ClaveUsuarioIntentos
When lockout applies
Then FechaBloqueoTemporal = CURRENT_UTCTIMESTAMP + (PLA.ClaveUsuarioTiempoBloqueo * 60 seconds)
     IntentosConexion = 0
```

**Parameters:**
- `PLA.ClaveUsuarioIntentos` — max failed attempts (count; no hardcoded default)
- `PLA.ClaveUsuarioTiempoBloqueo` — lockout duration in **minutes** (multiplied by 60 in SQL to get seconds)

**Confidence:** High

---

### RULE-016: Login — Failed-Attempt Counter Resets on Successful Login
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:383-390`
**Plain English:** A user who logs in successfully with a non-zero failed-attempt count has their counter reset to zero.

**Specification:**
```
Given user with IntentosConexion > 0 who successfully authenticates
When session token is about to be issued
Then UPDATE ACC.Usuario SET IntentosConexion=0 WHERE IdUsuario=?
```

**Confidence:** High

---

### RULE-017: Login — Channel / Entity-Type Authorization Matrix
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:301-340`
**Plain English:** A user can only log into a channel if their entity type and channel flag permit it.

**Specification:**
```
| TipoEntidad | Channel | Required Flag      |
|-------------|---------|-------------------|
| EMP         | COR     | UsuarioCorredores='S' |
| EMP         | AGE     | UsuarioNegocios='S'   |
| EMP         | WS      | UsuarioServicios='S'  |
| PER         | CLI     | UsuarioClientes='S'   |
| CLI         | WS      | UsuarioServicios='S'  |
| CLI         | CLI     | UsuarioClientes='S'   |
Any other combination → deny
```

**Parameters:** Channel codes: `COR` (broker), `AGE` (business), `CLI` (client), `WS` (web services).

**Confidence:** High

---

### RULE-018: ⚠️🔴 DEFECT — CLI Entity with BLO Status Can Log In
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:331`
**Plain English:** The entity status check for CLI-type entities uses an incorrect conjunction that allows accounts with `IdEstado='BLO'` to successfully complete login.

**Specification:**
```
Code (verified):
  if (lst.isEmpty() || lst.get(0).IdCliente==null
      || !lst.get(0).IdEstado.equals("ACT") && !lst.get(0).IdEstado.equals("BLO"))

Operator precedence: && binds tighter than ||:
  (!ACT && !BLO) for IdEstado='BLO' → (false && false) = false → login SUCCEEDS

Expected behavior: a blocked CLI cannot log in
Correct condition: !lst.get(0).IdEstado.equals("ACT")
```

Compare with EMP path (line 308) and PER path (line 318): both correctly check `!IdEstado.equals("ACT")`.

**Confidence:** High — verified at line 331.

**SME question:** Is a Cliente entity in BLO state permitted to log in to the CLI/WS channels?

---

### RULE-019: Login — Entity Enrollment Must Be ACT
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:199-211`
**Plain English:** The business entity linked to the user account must have computed status 'ACT'. An active-but-unenrolled entity is mapped to 'PEN' and denied.

**Specification:**
```
Given Empleado/Persona/Cliente with IdEstado=ES and EsEnrolado=EN
When entity state is computed via SQL CASE:
  ES='ACT' AND EN='S' → 'ACT' (allow)
  ES='ACT' AND EN='N' → 'PEN' (deny)
  ES != 'ACT'         → 'BLO' (deny)
Then login allowed iff effective state = 'ACT'
```

**Note:** CASE expression contains duplicate `WHEN 'N' THEN 'PEN'` — second branch is dead code, likely a copy-paste error.

**Confidence:** High

---

### RULE-020: Login — WS Channel Reuses Existing Valid Token
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:346-381`
**Plain English:** For the WS channel, if a valid token exists within the idle timeout window, it is returned instead of creating a new one.

**Specification:**
```
Given WS channel user with existing Sesion (TokenValido='S')
When (currentTime - FechaUltimoUso) <= PLA.IdleMaxTimeWS (ms)
Then return existing token unchanged (no new audit log)
When idle-expired → invalidate old token, create new one
When no existing token → create new one
```

**Parameters:** `PLA.IdleMaxTimeWS` — idle timeout in milliseconds.

**Confidence:** High

---

### RULE-021: Session Token Format
**Category:** Calculation
**Priority:** P1
**Source:** `BcsUtils/src/cl/bcs/utils/Sesion.java:184-185`
**Plain English:** Session tokens are Base64-encoded strings of `company:username:timestamp+UUID`.

**Specification:**
```
rawToken = IdEmpresa + ":" + CodigoUsuario + ":" + CURRENT_UTCTIMESTAMP + UUID4
token = Base64.encodeBase64(rawToken.getBytes())
```

**Parameters:** Token has no embedded expiry; validity checked via DB lookup only.

**Confidence:** High

---

### RULE-022: Unique-Session Policy per Channel
**Category:** Lifecycle
**Priority:** P1
**Source:** `BcsUtils/src/cl/bcs/utils/Sesion.java:156-170`
**Plain English:** When configured, creating a new session invalidates all prior active sessions for the same user and channel.

**Specification:**
```
Given new login for user U on channel C
When PLA.SesionUnicaUsuario='S' AND C in PLA.CanalesSesionUnica
Then UPDATE ACC.Sesion SET TokenValido='N' WHERE IdUsuario=U AND IdCanal=C AND TokenValido='S'
Then INSERT new session
```

**Parameters:**
- `PLA.SesionUnicaUsuario`: 'S' enables enforcement
- `PLA.CanalesSesionUnica`: semicolon-delimited channel list (e.g., `"COR;CLI"`)

**Confidence:** High

---

### RULE-023: Service Call — Token Validity + Channel Match
**Category:** Authentication
**Priority:** P0
**Source:** `BcsUtils/src/cl/bcs/utils/ProcesaLlamadaServletCall.java:113-135`
**Plain English:** Each service call validates the session token is active and that the channel matches, with SSO tokens accepted on any channel.

**Specification:**
```
Given CallServlet request with X-SESSION-TOKEN and ?t=<serviceId>&ch=<channel>
When SELECT from ACC.Sesion WHERE IdTokenSesion=? AND TokenValido='S' AND IdCanal IN (?, 'SSO')
  0 rows → HTTP 401 "TOKEN INVALIDO"
  1 row AND channel != 'COR':
    if (currentUTC - FechaUltimoUso) > idleMaxTime → invalidate, HTTP 401
    else → update FechaUltimoUso, proceed
  1 row AND channel = 'COR' → proceed (no idle check; last-use not updated)
```

**Confidence:** High

---

### RULE-024: Service Call — Idle Session Timeout by Channel
**Category:** Threshold
**Priority:** P0
**Source:** `BcsUtils/src/cl/bcs/utils/SesionUtils.java:42-81`
**Plain English:** Sessions on non-COR channels expire after a configurable idle period.

**Specification:**
```
| Channel | Config Key          | Default   |
|---------|--------------------  |-----------|
| WS      | PLA.IdleMaxTimeWS   | (none)    |
| CLI     | PLA.IdleMaxTimeCLI  | (none)    |
| AGE     | PLA.IdleMaxTimeAGE  | (none)    |
| COR     | exempt (RULE-025)   | N/A       |
| other   | hardcoded fallback  | 1,800,000 ms |
```

**Edge cases:** Parameters cached in static JVM fields; runtime config changes require JVM restart.

**Confidence:** High

---

### RULE-025: COR Channel Sessions Never Expire
**Category:** Policy
**Priority:** P0
**Source:** `BcsUtils/src/cl/bcs/utils/ProcesaLlamadaServletCall.java:123`
**Plain English:** Sessions on the COR (broker) channel are not subject to idle-timeout invalidation; they remain valid indefinitely.

**Specification:**
```
Given service call on channel COR with valid token
When ProcesaLlamadaServletCall validates session
Then idle timeout comparison skipped; FechaUltimoUso NOT updated
  (condition: if (!entrada.p_IdCanal.equalsIgnoreCase("COR")))
```

**SME question:** Is indefinite session validity on the COR channel intentional policy?

**Confidence:** High

---

### RULE-026: Service Call — User Must Be Active and Unexpired
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsUtils/src/cl/bcs/utils/ProcesaLlamadaServletCall.java:156-168`
**Plain English:** Even with a valid session token, the user's account is re-validated on each service call.

**Specification:**
```
Given valid token resolving to IdUsuario
When FechaVigencia != '0001-01-01' AND FechaVigencia < PLA.FechaSistema
  OR IdEstado = 'BLO'
Then HTTP 401 "CUENTA BLOQUEADA O INACTIVA"
```

**Confidence:** High

---

### RULE-027: Service Call — Service ID Must Exist in Catalog
**Category:** Validation
**Priority:** P0
**Source:** `BcsUtils/src/cl/bcs/utils/ProcesaLlamadaServletCall.java:104-111`
**Plain English:** A service call is rejected with HTTP 404 if the service ID does not exist in `PLA.Servicio`.

**Specification:**
```
When PLA.Servicio has no row with IdServicio=t → HTTP 404 "SERVICIO INVALIDO"
```

**Confidence:** High

---

### RULE-028: Service Call — Per-Service ACL Enforcement Flag
**Category:** Authorization
**Priority:** P0
**Source:** `BcsFilters/src/cl/bcs/filters/ServiceFilter.java:190-212`
**Plain English:** Each service record carries `ValidaAccesoServicio`; only services flagged 'S' undergo role/profile ACL checking.

**Specification:**
```
When ValidaAccesoServicio='S' → role check (RULE-006) applies
When ValidaAccesoServicio != 'S' → unconditional access
When service not found → null → treated as not 'S' → unconditional access
```

**Confidence:** High

---

### RULE-029: Password Hash — Change-Password Path (Double SHA-256, UTF-8)
**Category:** Calculation
**Priority:** P0
**Source:** `BcsPlataformaServicios/src/cl/bcs/plataforma/acc/CambiarClaveUsuario.java:130-133`
**Plain English:** When a user changes their password, the new password is hashed twice with SHA-256 using explicit UTF-8 encoding.

**Specification:**
```
Given new password P
When CambiarClaveUsuario stores:
  step1 = SHA-256(P, UTF-8) via Guava Hashing.sha256()
  step2 = SHA-256(hex(step1), UTF-8)
  stored = UPPER(hex(step2))
```

**Suspected defect:** See RULE-030.

**Confidence:** High

---

### RULE-030: ⚠️🔴 DEFECT — Password Hash Inconsistency (Login vs Change-Password)
**Category:** Calculation
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:277` vs `BcsPlataformaServicios/src/cl/bcs/plataforma/acc/CambiarClaveUsuario.java:130`
**Plain English:** The login path uses single SHA-256 with platform-default charset; the change-password path uses double SHA-256 with UTF-8. If the server receives raw passwords, these produce different hashes.

**Specification:**
```
Login:         stored = SHA-256(P, platform-charset)  — single round
ChangePassword: stored = SHA-256(SHA-256(P, UTF-8))   — double round

If the server receives raw passwords: SHA-256(P) ≠ SHA-256(SHA-256(P))
→ a changed password cannot be verified at login
```

**Suspected defect:** If the browser pre-hashes the password (SHA-256 on client side), both paths receive SHA-256(P) and apply one more round, producing consistent results. This must be confirmed.

**Confidence:** Medium — defect is real if server receives raw passwords; may be by design if client pre-hashes.

**SME question:** Does the browser/mobile client hash the password with SHA-256 before sending it? If not, users who change their password cannot log in afterward.

---

### RULE-031: Password Complexity Rules
**Category:** Validation
**Priority:** P0
**Source:** `BcsUtils/src/cl/bcs/utils/ValidarNuevaClave.java:147-222`
**Plain English:** New passwords must meet minimum length, uppercase, lowercase, and special-character requirements, and must not contain spaces. All thresholds are configurable per company (but see RULE-033 defect).

**Specification:**
```
Given new password P
When:
  length(P) < ACC.numLongitud                    → reject "Mínimo N caracteres"
  count([A-Z] in P) < ACC.numMayusculas          → reject "Al menos N mayúscula"
  count([a-z] in P) < ACC.numMinuscula           → reject "Al menos N minúscula"
  count(\W in P) < ACC.numCaracteres             → reject "Al menos N carácter especial"
  any char == ' '                                → reject "Caracteres inválidos"
```

**Parameters (loaded from `PLA.Configuracion`):**
- `ACC.numLongitud` — minimum total length
- `ACC.numMayusculas` — minimum uppercase count
- `ACC.numMinuscula` — minimum lowercase count
- `ACC.numCaracteres` — minimum special-char count (regex `\W` — see RULE-032)

**Confidence:** High

---

### RULE-032: ⚠️ DEFECT — Digits Not Counted in Any Complexity Category
**Category:** Validation
**Priority:** P0
**Source:** `BcsUtils/src/cl/bcs/utils/ValidarNuevaClave.java:164-182`
**Plain English:** Digit characters [0-9] match none of the three counters (uppercase `[A-Z]`, lowercase `[a-z]`, or special `\W`), so they are silently ignored.

**Specification:**
```
'5'.matches("[A-Z]") → false
'5'.matches("[a-z]") → false
'5'.matches("\\W")   → false  (digits are \w, not \W)
→ digit contributes to length only, never to a category counter
```

**SME question:** Should digits count as "special characters"? Should there be a separate minimum-digit requirement?

**Confidence:** High

---

### RULE-033: ⚠️ DEFECT — Password Validation Hardcoded to Company ID=1
**Category:** Validation
**Priority:** P1
**Source:** `BcsUtils/src/cl/bcs/utils/ValidarNuevaClave.java:107`
**Plain English:** Despite receiving the actual company ID, the password complexity query always uses `p_IdEmpresa=1L`.

**Specification:**
```
Code (verified): params.put("p_IdEmpresa", 1L);  // ignores entrada.p_IdEmpresa
Effect: all companies use company-1's password policy
```

**SME question:** Is this currently a single-company system? If multi-company is planned, this must use `entrada.p_IdEmpresa`.

**Confidence:** High

---

### RULE-034: Password — Prohibited-Word Check (Exact Match)
**Category:** Validation
**Priority:** P1
**Source:** `BcsUtils/src/cl/bcs/utils/ValidarNuevaClave.java:127-143`
**Plain English:** A new password is rejected if it exactly matches any entry in `ACC.PasswordProhibidas`.

**Specification:**
```
When SELECT COUNT(*) FROM ACC.PasswordProhibidas WHERE Palabra=P > 0 → reject
Match type: exact equality (case-sensitivity per DB collation)
```

**Edge cases:** Substring matches are not checked (e.g., `password1` is not caught by rule for `password`).

**Confidence:** High

---

### RULE-035: Password History — Cannot Reuse Any Prior Password
**Category:** Policy
**Priority:** P0
**Source:** `BcsPlataformaServicios/src/cl/bcs/plataforma/acc/ActualizarUsuario.java:384-396`
**Plain English:** When an administrator sets a new password, the system checks both the current hash and the full password history; a match rejects the change.

**Specification:**
```
Given new hash P_NEW
When P_NEW == ACC.Usuario.Clave → error "UI_CLAVES_IGUALES"
When P_NEW in password history log → error "UI_CLAVES_IGUALES"
Otherwise → update proceeds
```

**Parameters:** History depth: bounded by `ConsultaLogClaveUsuario.sql` (comment says "last 10" — SME should confirm).

**Confidence:** High

---

### RULE-036: Password Must-Change Flag Set on Admin Reset
**Category:** Lifecycle
**Priority:** P1
**Source:** `BcsPlataformaServicios/src/cl/bcs/plataforma/acc/ActualizarUsuario.java:399-415`
**Plain English:** When an admin sets a new password, the user is forced to change it at next login.

**Specification:**
```
When admin sets p_Clave (non-empty, passes history checks)
Then DebeCambiarClave = 'S'

When admin saves user WITHOUT new password AND EsAutorizado='S'
Then DebeCambiarClave = 'N'
```

**Confidence:** High

---

### RULE-037: Password Stored as SHA-256 Uppercase Hex (No Salt)
**Category:** Calculation
**Priority:** P0
**Source:** `BcsPlataformaServicios/src/cl/bcs/plataforma/acc/ActualizarUsuario.java:382`
**Plain English:** Passwords are stored as the uppercase hex SHA-256 hash of the plaintext, with no per-user salt.

**Specification:**
```
stored = Hashing.sha256().newHasher().putString(p_Clave, Charsets.UTF_8).hash().toString().toUpperCase()
```

**Suspected defect:** No salt — all users with the same password have the same hash; rainbow-table attacks succeed on a DB compromise.

**Confidence:** High

---

### RULE-038: Provisional Password — Valid Only on Day of Issue
**Category:** Policy
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/ValidaClaveProvisoria.java:121`
**Plain English:** A provisional (one-time) password is only accepted on the same calendar day it was issued.

**Specification:**
```
Given user with FechaClaveProvisoria stored
When format(FechaClaveProvisoria, 'yyyy-MM-dd') != PLA.FechaSistema (date only)
Then clear ClaveProvisoria, FechaClaveProvisoria; reset IntentosConexionClaveProvisoria=0
     Return {Estado:"NOK", CLAVE_PROVISORIA_VENCIDA}
```

**Parameters:** Comparison is date-only (time truncated).

**Confidence:** High

---

### RULE-039: Provisional Password — Maximum Attempts Lockout
**Category:** Policy
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/ValidaClaveProvisoria.java:122`
**Plain English:** Exceeding the maximum provisional-password attempts clears the provisional password entirely.

**Specification:**
```
When intentosConexionClaveProvisoria > PLA.ClaveUsuarioIntentos
Then clear ClaveProvisoria, FechaClaveProvisoria; reset counter=0
     Return {SUPERA_INTENTOS_MAXIMOS}
```

**Parameters:** `PLA.ClaveUsuarioIntentos` (shared with regular login lockout, RULE-015).

**Confidence:** High

---

### RULE-040: ⚠️ DEFECT — Provisional Password Off-By-One in Attempts Check
**Category:** Policy
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/ValidaClaveProvisoria.java:122`
**Plain English:** The attempts check uses `<=` (passes at max), so the lockout fires at max+1 instead of max.

**Specification:**
```
Code: if (intentosConexionClaveProvisoria <= cantidadIntentosMaximo)  → success path
Else (intentosConexionClaveProvisoria > cantidadIntentosMaximo)       → lockout path

At intentos == max: <=max is TRUE → user succeeds (not locked out)
Effective lockout fires at max+1 attempts instead of max
```

**Confidence:** High

---

### RULE-041: Password Recovery — Channel / Entity-Type Authorization
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/RecuperacionClave.java:115`
**Plain English:** A password recovery request is only processed when the requesting channel matches the user's entity type.

**Specification:**
```
| p_CanalSistema | tipoEntidad allowed |
|----------------|---------------------|
| 'CLIENTE'      | CLI, PER            |
| 'OPTIMUS'      | EMP                 |
| 'AGENTE'       | EMP                 |
Any other combination → HTTP 401 ERROR_RECUPERAR_CLAVE
```

**Confidence:** High

---

### RULE-042: Change-Password — Confirmation Fields Must Match
**Category:** Validation
**Priority:** P1
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/ActualizarClave.java:89-90`
**Plain English:** The new password and its confirmation must be non-empty and identical.

**Specification:**
```
When body empty OR Clave1 empty OR Clave2 empty → {Estado:"NOK", CONTRASENA_INVALIDA}
When Clave1 != Clave2 → {Estado:"NOK", CONTRASENA_Y_CONFIRMACION_NO_SON_IGUALES}
```

**Confidence:** High

---

### RULE-043: SSO — Email Must Resolve to Exactly One UsuarioCorredores User
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/SSOUtils.java:91-98`
**Plain English:** SSO login requires the authenticated email to match exactly one platform user with `UsuarioCorredores='S'`.

**Specification:**
```
Given email from SSO JWT
When 0 users found → "USUARIO_NO_ENCONTRADO" → SSO error page
When >1 users found → "USUARIO_MAL_CONFIGURADO" → SSO error page
When exactly 1 → proceed to status checks
```

**Parameters:** Lookup joins DireccionElectronica (email, UPPER) → Empleado → ACC.Usuario where TipoEntidad='EMP' AND UsuarioCorredores='S'.

**Confidence:** High

---

### RULE-044: SSO — User Eligibility Checks
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/SSOUtils.java:101-124`
**Plain English:** After finding a unique user, SSO checks: not BLO, EsAutorizado='S', not temp-locked, linked employee is ACT.

**Specification:**
```
When IdEstado='BLO' → "USUARIO_BLOQUEADO"
When EsAutorizado != 'S' → "USUARIO_NO_AUTORIZADO"
When FechaBloqueoTemporal > CurrentUtcTimestamp → "USUARIO_BLOQUEO_TEMPORAL"
When IdEstadoEmpleado != 'ACT' → "EMPLEADO_BLOQUEADO"
All pass → create session with IdCanal='SSO'
```

**Confidence:** High

---

### RULE-045: ⚠️ DEFECT — SSO Does Not Check Account Expiry (FechaVigencia)
**Category:** Eligibility
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/SSOUtils.java:112`
**Plain English:** The SSO login path explicitly skips the `FechaVigencia` expiry check (marked `//UsuarioVigente --todo`), allowing expired accounts to authenticate via SSO.

**Specification:**
```
Comment at line 112: //UsuarioVigente --todo
Effect: users with FechaVigencia < PLA.FechaSistema can still log in via SSO,
        even though the same check blocks them on direct login (RULE-012)
```

**Confidence:** High

**SME question:** Is SSO expiry checking planned or intentionally omitted?

---

### RULE-046: ⚠️🔴 DEFECT — SSO OAuth2 CSRF State Parameter Hardcoded
**Category:** Security
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/SSOInit.java:101`
**Plain English:** The OAuth2 `state` parameter, which should be a random per-request value to prevent CSRF, is hardcoded to a fixed 40-character string.

**Specification:**
```
Code (verified): state=fee7e77508933e8ba8c6008dc587b4c6934a94e9
This value is the same for every user, every request, every time.
A fixed state provides no CSRF protection — an attacker can craft a valid SSO redirect.
```

**Confidence:** High

**SME question:** Does the SSOLogon callback validate the returned `state` against a stored expected value? (If yes, any fixed expected value still provides no CSRF protection.)

---

### RULE-047: ⚠️ DEFECT — SSO Company ID Hardcoded to 1
**Category:** Policy
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/SSOUtils.java:269`
**Plain English:** The SSO session creation always uses company ID 1, regardless of which company the employee belongs to.

**Specification:**
```
Code: idEmpresa = 1L  (hardcoded)
In a multi-company deployment, all SSO users are assigned to company 1.
```

**Confidence:** High

**SME question:** Does the system serve only one company in production?

---

### RULE-048: SSO — JWT id_token Provides Identity (Signature Not Verified)
**Category:** Authentication
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/SSOUtils.java:259-267`
**Plain English:** After OAuth2 code exchange, the user's identity is read from the JWT `id_token` payload; the JWT signature is not verified.

**Specification:**
```
Given id_token JWT from OAuth2 token endpoint
When Base64-decode part[1] and parse JSON:
  'email' field → use as identity
  'mail' field → use as identity
  neither → ServletException "SSO payload sin direccion electronica"
Signature (part[2]): never checked
```

**Parameters:** Trust relies on TLS transport security.

**Confidence:** High

---

### RULE-049: SSO — Entity Configuration Must Exist
**Category:** Validation
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/SSOInit.java:79-82`
**Plain English:** A SSO login can only be initiated if a `ConfiguracionSSO` record exists for the requested entity.

**Specification:**
```
Given GET /PLA/SSOInit?a=<entity>
When 0 ConfiguracionSSO rows → ServletException "SSO no configurado" (HTTP 500)
If ?a absent → query for Predeterminado='S' record; same failure if not found
```

**Confidence:** High

---

### RULE-050: CallBatch — Shared-Secret Token Authentication
**Category:** Authentication
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CallBatch.java:72-101`
**Plain English:** The CallBatch endpoint (outside the standard session filter) is protected by a shared secret stored in `PLA.CallBatchToken`.

**Specification:**
```
Given POST /PLA/CallBatch with X-SESSION-TOKEN header
When header absent → HTTP 401
When header != PLA.CallBatchToken → HTTP 401
When match → enqueue job
```

**Parameters:**
- `PLA.CallBatchToken` — loaded once from DB at company ID 1; cached for JVM lifetime

**Confidence:** High

---

### RULE-051: ⚠️🔴 DEFECT — CallBatch Fallback to Hardcoded Token "CALLBATCH"
**Category:** Security
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CallBatch.java:91`
**Plain English:** If `PLA.CallBatchToken` cannot be read from the DB, the fallback shared secret is the publicly known string `"CALLBATCH"`.

**Specification:**
```
Code (verified): if (tokenSeguridad == null) tokenSeguridad = "CALLBATCH";
Any caller who knows this default can authenticate to /PLA/CallBatch if DB is unreachable.
```

**Confidence:** High

---

### RULE-052: CallBatch — LIFO Job Queue; Jobs Discarded on Shutdown
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CallBatch.java:125-198`
**Plain English:** Submitted batch jobs are held in a `java.util.Stack` (LIFO) and processed by a background thread; any jobs remaining at server shutdown are permanently lost.

**Specification:**
```
Given jobs submitted while server is running
When background thread runs (outer period: 10s; inner: 1s sleep per empty check)
Then jobs pop in LIFO order (last submitted is executed first)

Given jobs in stack when contextDestroyed()
When stop=true is set
Then remaining jobs logged as warnings and dropped: "WARNING: No se va ejecutar"
```

**Suspected defect:** LIFO ordering and lack of persistence may cause financial sequencing issues.

**Confidence:** High for code behavior; Medium for whether LIFO is intentional.

**SME question:** Is last-in-first-out processing order of CallBatch jobs intentional? Are there financial operations relying on submission order?

---

### RULE-053: WebSocket — Auth Required in First Message
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CallWebsocket.java:73-141`
**Plain English:** Every new WebSocket connection starts unauthenticated. The client must send `X-SESSION-TOKEN:<token>` as its first message; until then no messages are delivered.

**Specification:**
```
Given new WebSocket connection
When onOpen fires → WSession created with Autentificado=false

Given message starting with "X-SESSION-TOKEN:"
When token validated via ACC.ProcesaLlamadaServletCall(serviceId=14)
  PermiteUso='S' → Autentificado=true; IdEmpresa, Usuario, IdUsuario stored; send "OK+"
  PermiteUso != 'S' → Autentificado remains false; no response

Given WSession with Autentificado=false
When server pushes a message → session skipped
```

**Parameters:** Service ID 14 used for token validation (hardcoded).

**Confidence:** High

---

### RULE-054: WebSocket — Poll Every 10 Seconds, 1-Hour Lookback
**Category:** Lifecycle
**Priority:** P1
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CallWebsocket.java:166-256`
**Plain English:** A background thread polls for unprocessed WebSocket messages every 10 seconds and delivers them to authenticated connections for the matching company.

**Specification:**
```
Given PLA.MensajeWebsocket rows with EsProcesado='N' AND FechaRegistro > CURRENT_UTCTIMESTAMP-3600s
When scheduler fires (period: 10s)
Then deliver to all WSession where Autentificado=true AND IdEmpresa matches
After delivery: mark all as EsProcesado='S' up to max FechaRegistro

Delivery format: {"Evento":"...", "DATA":"..."}
```

**Parameters:**
- Poll period: 10 seconds (hardcoded)
- Lookback: 3600 seconds (hardcoded)

**Edge cases:** Messages are batch-marked as processed; failed deliveries are not retried.

**Confidence:** High

---

### RULE-055: Job State Machine — PEN → ASI → TER / ERR
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsJobManager/src/cl/bcs/scheduler/sql/TransactionManager.java:41-93, 212-258`
**Plain English:** Each batch job passes through a defined state sequence recorded in `BAT.Programa.idEstado`.

**Specification:**
```
States:
  PEN — pending (scheduled, not yet picked up)
  ASI — assigned/running
  TER — terminated (success)
  ERR — error (failure)

Transitions:
  PEN → Bloqueo='S' (lock, same state) → ASI (picked up)
  ASI → TER (updateProgramm result=1, fechaFin=CURRENT_UTCTIMESTAMP)
  ASI → ERR (updateProgramm result=-1, fechaFin=CURRENT_UTCTIMESTAMP)
  ERR → PEN (retry, RULE-058; if Intento < MaximoIntentos)
  PEN/ASI → TER (daemon restart, RULE-062)

Selection criteria (PEN → ASI pickup):
  Bloqueo='N' AND idEstado='PEN' AND Intento < MaximoIntentos
  AND Nodo=<node> AND fechaEjecucion BETWEEN today 00:00:00 AND CURRENT_UTCTIMESTAMP
```

**Parameters:** `Intento` incremented before execution; `MaximoIntentos` per `BAT.TipoPrograma`.

**Confidence:** High

---

### RULE-056: Job Lock (Bloqueo=S) Set Before Execution
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsJobManager/src/cl/bcs/scheduler/sql/TransactionManager.java:68-73`
**Plain English:** When a job is selected for execution, `Bloqueo='S'` is immediately committed to the DB before any job logic runs, preventing another node from picking up the same job.

**Specification:**
```
Given Programa with Bloqueo='N' AND idEstado='PEN'
When scheduler selects it
Then UPDATE Programa SET Bloqueo='S' WHERE IdPrograma=?
     conn.commit()   ← immediate commit before any further processing
```

**Confidence:** High

---

### RULE-057: ⚠️ DEFECT — Job Lock Race Condition (FOR UPDATE Removed)
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsJobManager/src/cl/bcs/scheduler/sql/TransactionManager.java:48`
**Plain English:** A commented-out `FOR UPDATE` clause means two nodes could read the same PEN job row before either sets the lock, creating a double-execution race window.

**Specification:**
```
Comment at line 48: //ESTA GENERANDO LOG TRAN  (next to a commented-out FOR UPDATE)
Without row-level locking, two nodes can SELECT the same job concurrently
before either executes the Bloqueo='S' UPDATE
```

**Confidence:** High

---

### RULE-058: Job Retry — ERR → PEN After 60-Second Delay
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsJobManager/src/cl/bcs/scheduler/sql/TransactionManager.java:259-278`
**Plain English:** Failed jobs below their retry limit are automatically re-queued 60 seconds later, but only if they failed within the last hour.

**Specification:**
```
Given Programa with idEstado='ERR', Bloqueo='S', Intento < MaximoIntentos
     AND fechaEjecucion BETWEEN ADD_SECONDS(CURRENT_UTCTIMESTAMP,-3600) AND CURRENT_UTCTIMESTAMP
When retry runs
Then idEstado='PEN', Bloqueo='N'
     fechaEjecucion = ADD_SECONDS(CURRENT_UTCTIMESTAMP, 60)
     Resultado = '(Reintento N+1)' + previous result
```

**Parameters:**
- Retry delay: **60 seconds** (hardcoded)
- Eligibility window: **3600 seconds** (1 hour, hardcoded)

**Confidence:** High

---

### RULE-059: ⚠️ DEFECT — Job Retry Call Commented Out in poolDiary
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsJobManager/src/cl/bcs/scheduler/BcsJobManager.java:105`
**Plain English:** The `updateTryProgramm` call that triggers automatic job retry is commented out, meaning the retry mechanism (RULE-058) may not be active in production.

**Specification:**
```
Code at line 105: // transactionManager.updateTryProgramm(conn);
Effect: ERR jobs stay in ERR state permanently; no automatic retry occurs
```

**Confidence:** High for the code; Medium for production behavior.

**SME question:** Is `updateTryProgramm` called anywhere in the deployed configuration?

---

### RULE-060: Job Eligibility — Full Selection Criteria
**Category:** Eligibility
**Priority:** P1
**Source:** `BcsJobManager/src/cl/bcs/scheduler/sql/TransactionManager.java:41-62`
**Plain English:** A job is eligible for pickup when it is unlocked, pending, below max retries, assigned to the correct node, and within today's execution window.

**Specification:**
```
SELECT ... FROM BAT.Programa WHERE
  Bloqueo='N' AND idEstado='PEN' AND Intento < MaximoIntentos
  AND Nodo=<requesting node ID (dashes stripped)>
  AND fechaEjecucion BETWEEN (CURRENT_DATE 00:00:00) AND CURRENT_UTCTIMESTAMP
```

**Confidence:** High

---

### RULE-061: Daemon Job — Continuous Loop with Configurable Cycle Interval
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsJobManager/src/cl/bcs/scheduler/BcsJob.java:86-160`
**Plain English:** Jobs flagged as daemons run in an infinite loop, committing or rolling back after each iteration, with a per-cycle sleep interval.

**Specification:**
```
Given BcsJob where isDaemon=true (EsDemonio='S' in Agenda)
When job starts
Then infinite while(true):
  call transaction()
    true → commit → sleep <delay>ms
    false or exception → rollback → sleep <delay>ms
  on exception: sleep 60,000ms (1-minute cooldown, hardcoded)
```

**Parameters:**
- Cycle delay: `Minuto='**'` → 5,000 ms (5s); else `Minuto * 60 * 1,000` ms
- Exception cooldown: **60 seconds** (hardcoded in BcsJob.java:119)

**Confidence:** High

---

### RULE-062: Daemon Restart — Terminates All PEN/ASI Programs
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsJobManager/src/cl/bcs/scheduler/sql/TransactionManager.java:174-192`
**Plain English:** When the scheduler detects a daemon needs to restart, all PEN and ASI programs for that agenda are forcibly terminated.

**Specification:**
```
Given daemon Agenda where SiguienteFechaDeEjecucion < UltimaFechaDeEjecucion
When diary poll runs
Then:
  1. Agenda.SiguienteFechaDeEjecucion = CURRENT_UTCTIMESTAMP + 1 second
  2. All BAT.Programa with idEstado IN ('PEN','ASI') for this agenda →
       idEstado='TER', Bloqueo='S', Resultado='Proceso abortado/reiniciado'
       fechaFin=CURRENT_UTCTIMESTAMP
  3. New Programa created for the daemon
```

**Confidence:** High

---

### RULE-063: Agenda — Schedules New Programs When Due
**Category:** Lifecycle
**Priority:** P1
**Source:** `BcsJobManager/src/cl/bcs/scheduler/BcsJobManager.java:96-158`
**Plain English:** The diary scheduler creates a new program when the next scheduled date has been passed.

**Specification:**
```
Given Agenda with habilitado='S' AND nextExecution < lastExecution
When diary poll runs
Then:
  compute next execution via BAT.ObtenerSiguienteFechaAgendada
  update SiguienteFechaDeEjecucion via BAT.ActualizarSiguienteFechaEjecucionAgenda
  create new Programa via BAT.AgregarPrograma (type 'ORD', hardcoded)
Given habilitado='N' → agenda ignored
```

**Confidence:** High

---

### RULE-064: Node Heartbeat — Written Every 10 Seconds
**Category:** Lifecycle
**Priority:** P1
**Source:** `BcsJobManager/src/cl/bcs/scheduler/BcsJobManager.java:31, 178-181`
**Plain English:** Each running job node updates a heartbeat timestamp every 10 seconds.

**Specification:**
```
Every HEARTBIT_CONTROL ms (10,000ms):
  UPDATE BAT.Nodos SET FechaUltimoCiclo=CURRENT_UTCTIMESTAMP WHERE Pid=?
  UPDATE PLA.Heartbeat SET LastTimestamp=CURRENT_TIMESTAMP WHERE Id=? (if processId set)
```

**Parameters:** `HEARTBIT_CONTROL = 10,000 ms` (hardcoded).

**Confidence:** High

---

### RULE-065: Zombie Node Detection — 30-Second Threshold, OS Kill
**Category:** Lifecycle
**Priority:** P1
**Source:** `BcsJobManager/src/cl/bcs/scheduler/BcsJobManager.java:234-248`
**Plain English:** Nodes that haven't updated their heartbeat within 30 seconds are killed at the OS level and removed from the registry.

**Specification:**
```
Given BAT.Nodos row where FechaUltimoCiclo < CURRENT_UTCTIMESTAMP - 30s
When heartbeat check runs (every 10s)
Then for each zombie PID:
  Linux: kill -9 <pid>
  Windows: taskkill /pid <pid>
  DELETE from BAT.Nodos WHERE Pid=?
```

**Parameters:** `ZOMBIE_CONTROL = 30 seconds` (hardcoded).

**Confidence:** High

---

### RULE-066: Node Reboot-on-Demand via DB Flag
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsJobManager/src/cl/bcs/scheduler/BcsJobManager.java:183-190`
**Plain English:** Every 5 seconds a node checks whether its `Reiniciar` flag is set; if so, it calls `System.exit(0)`.

**Specification:**
```
Every REBOOT_CONTROL ms (5,000ms):
  if BAT.Nodos.Reiniciar='S' for this Pid
  Then System.exit(0)  ← hard JVM termination
```

**Parameters:** `REBOOT_CONTROL = 5,000 ms` (hardcoded).

**Suspected defect:** `System.exit(0)` gives in-flight jobs no chance to commit or roll back.

**Confidence:** High

---

### RULE-067: Job Result Description Truncated at 1999 Characters
**Category:** Validation
**Priority:** P2
**Source:** `BcsJobManager/src/cl/bcs/scheduler/sql/TransactionManager.java:216-219`
**Plain English:** Job result descriptions longer than 2000 characters are silently truncated to 1999.

**Specification:**
```
When length(Resultado) > 2000 → stored = substring(0, 1999)
When length(Resultado) <= 2000 → stored as-is
```

**Suspected defect:** Off-by-one — threshold 2000 but truncation to 1999. If column is VARCHAR(2000), this is harmless but wasteful.

**Confidence:** High

---

### RULE-068: ⚠️🔴 DEFECT — SQL Injection in TransactionManager.executionCount
**Category:** Security
**Priority:** P0
**Source:** `BcsJobManager/src/cl/bcs/scheduler/sql/TransactionManager.java:526-535`
**Plain English:** The `executionCount` method builds a SQL query by concatenating caller-supplied `parameterName` and `parameterValue` strings without sanitization.

**Specification:**
```
Code (verified):
  String query = "... WHERE ... \"NombreParametro\"='"
    + parameterName + "' and \"Valor\"='" + parameterValue + "' ...";
```

**Confidence:** High

**SME question:** What callers supply `parameterName` and `parameterValue` to `executionCount`? If always from internal config, practical risk is lower — but must be parameterized regardless.

---

### RULE-069: User Block/Unblock Triggers Email to Security Team
**Category:** Lifecycle
**Priority:** P1
**Source:** `BcsPlataformaServicios/src/cl/bcs/plataforma/acc/ActualizarUsuario.java:509-622`
**Plain English:** When a user transitions between active and blocked states, an email notification is sent — but only for users with COR or AGE channel access.

**Specification:**
```
Given state transition (in.p_IdEstado != usuario.get(0).IdEstado)
When PLA.EnvioNotificacion='S'
  AND (UsuarioCorredores='S' OR UsuarioNegocios='S')
Then send "NOTIFICAR_CREACION_USUARIO" event to security recipients
  Message: "con fecha YYYY-MM-DD se ha [bloqueado|desbloqueado] un usuario de codigo: X"
```

**Parameters:** Event code `"NOTIFICAR_CREACION_USUARIO"` is reused for both creation and block/unblock — misleading name.

**Confidence:** High

---

### RULE-070: Expiry Batch — Blocks All Users Past FechaVigencia
**Category:** Lifecycle
**Priority:** P0
**Source:** `BcsPlataformaServicios/src/cl/bcs/plataforma/acc/BloqueoUsuariosPorVigencia.java:93-149`
**Plain English:** A batch process selects all users whose `FechaVigencia` is before the system date and transitions them to blocked status using the 'BVIG' block type.

**Specification:**
```
Given batch run for company E on PLA.FechaSistema=S
When BloqueoUsuariosPorVigencia.logica executes
Then all users WHERE FechaVigencia < S →
  IdEstado = TipoBloqueo['BVIG'].IdAccion
  IdTipoBloqueo = 'BVIG'
  ComentarioBloqueo = TipoBloqueo['BVIG'].ComentarioPorDefecto
```

**Confidence:** High

---

### RULE-071: UTC Timestamp Cached 1 Hour Across JVM
**Category:** Policy
**Priority:** P2
**Source:** `BcsUtils/src/cl/bcs/utils/SesionUtils.java:93-100`
**Plain English:** The DB-fetched UTC timestamp is cached in a static JVM field for 1 hour; between refreshes, the returned value is the cached value plus elapsed wall-clock time.

**Specification:**
```
When (System.currentTimeMillis() - lastFetch) > 3,600,000ms
  re-fetch UTC from DB; update cache
Returned = cachedUTC + (now - lastFetch)ms
```

**Edge cases:** All threads share one cache; JVM clock drift accumulates up to 1 hour.

**Confidence:** High

---

### RULE-072: HANA Connection Lock-Wait Timeout — 60 Seconds
**Category:** Policy
**Priority:** P1
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CallServlet.java:213-215`
**Plain English:** Every HANA connection opened by the servlet is configured with a 60-second lock-wait timeout.

**Specification:**
```
On new HANA connection:
  SET TRANSACTION AUTOCOMMIT DDL OFF
  SET TRANSACTION LOCK WAIT TIMEOUT 60000   (ms)
PostgreSQL connections skip this setting.
```

**Confidence:** High

---

### RULE-073: Error Stack Trace Controlled by PLA.MuestraLog
**Category:** Policy
**Priority:** P2
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CallServlet.java:265, 634-643`
**Plain English:** On unhandled exceptions, `PLA.MuestraLog='S'` returns the full stack trace to the caller; any other value returns only an internal error code.

**Specification:**
```
When PLA.MuestraLog='S' → {CODIGO:<id>, PILA:<full stack>, SP:<serviceId>}
When PLA.MuestraLog!='S' → {CODIGO:<id>, PILA:"INTERNAL ERROR #<id>", SP:<serviceId>}
Default (initial JVM value): 'S' (stack trace shown until first service call reads config)
```

**Confidence:** High

---

### RULE-074: ⚠️ DEFECT — PLA.MuestraLog Is a Static Shared Field (Thread-Safety Risk)
**Category:** Policy
**Priority:** P1
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CallServlet.java:74`
**Plain English:** `muestraLog` is declared as a static field updated during request processing without synchronization; concurrent requests can see each other's company configuration.

**Specification:**
```
Code: private static String muestraLog = "S";
On each request: muestraLog = getParametro(conn, "PLA.MuestraLog", idEmpresa)
Two concurrent threads for different companies can interleave reads/writes of this field.
```

**Confidence:** High

---

### RULE-075: JavaView Default Pagination — Skip=0, Take=1,000,000
**Category:** Policy
**Priority:** P2
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CallServlet.java:548-549`
**Plain English:** Type-"X" (JavaView) service calls without explicit pagination parameters default to returning up to 1,000,000 rows.

**Specification:**
```
Given service call type='X' without ?skip= or ?take= parameters
Then skip=0, take=1,000,000 (hardcoded defaults via Optional.orElse)
```

**Confidence:** High

---

### RULE-076: ⚠️🔴 SECURITY — Hardcoded HANA Credentials in Dead Code
**Category:** Security
**Priority:** P0
**Source:** `BcsUtils/src/cl/bcs/utils/Sesion.java:304-319`
**Plain English:** A `main()` method used for manual testing contains a hardcoded SAP HANA connection string with credentials; these credentials must be rotated immediately even though the code is never called at runtime.

**Specification:**
```
main() at Sesion.java:304-319 contains:
  HANA connection URL: jdbc:sap://161.238.0.14:30215/
  Username: <masked — see Sesion.java:308>
  Password: <masked, 4-char preview: 'Bols' — see Sesion.java:309>
  currentschema=BSCL
```

**Action required:** Rotate database credentials immediately; remove or sanitize the `main()` method.

**Confidence:** High

---

### RULE-077: ⚠️🔴 SECURITY — Static3DES with Publicly Known Key
**Category:** Security
**Priority:** P0
**Source:** `BcsJobManager/src/cl/bcs/scheduler/tools/CypherEncryption.java:11`
**Plain English:** Job-manager credentials are encrypted with OpenEJB's `Static3DES`, which uses a fixed publicly documented key (`"OpenEJB5PasswordCipherKey"`), providing obfuscation only.

**Specification:**
```
CypherEncryption.encrypt(P) = PasswordCipherFactory("Static3DES").encrypt(P)
The OpenEJB library key is public; any third party with the library can decrypt values.
```

**Confidence:** High

**SME question:** Are there credentials currently stored in Static3DES-encrypted form in the DB or config files?

---

### RULE-078: ⚠️ SSO — JWT Signature Not Verified
**Category:** Security
**Priority:** P0
**Source:** `BcsPlataformaWeb/src/cl/bcs/plataforma/services/SSOUtils.java:255-267`
**Plain English:** After OAuth2 code exchange, the server decodes the JWT `id_token` payload without verifying the cryptographic signature; identity is trusted purely from TLS transport security.

**Specification:**
```
parts = id_token.split("\\.")
payload = Base64.decode(parts[1])
email = JSON.parse(payload)["email"]  ← parts[2] (signature) never checked
```

**Confidence:** High

---

## Configuration Parameter Registry

All values stored in `BSCL.Plataforma.BD::PLA.Configuracion` per company unless noted.

| Parameter Key | Meaning | Unit | Used In |
|---|---|---|---|
| `PLA.FechaSistema` | Business system date (not wall-clock) | YYYY-MM-DD string | RULE-012, RULE-026, RULE-070 |
| `PLA.ClaveUsuarioIntentos` | Max failed attempts before temp lock | Count | RULE-015, RULE-039 |
| `PLA.ClaveUsuarioTiempoBloqueo` | Temporary lockout duration | **Minutes** | RULE-015 |
| `PLA.IdleMaxTimeWS` | WS channel idle timeout | Milliseconds | RULE-020, RULE-024 |
| `PLA.IdleMaxTimeCLI` | CLI channel idle timeout | Milliseconds | RULE-024 |
| `PLA.IdleMaxTimeAGE` | AGE channel idle timeout | Milliseconds | RULE-024 |
| `PLA.SesionUnicaUsuario` | Enable unique-session enforcement | 'S'/'N' | RULE-022 |
| `PLA.CanalesSesionUnica` | Channels subject to uniqueness | Semicolon-delimited | RULE-022 |
| `PLA.TipoValidaServicio` | Service authorization scope | 'NADIE'/'LISTA'/other | RULE-007 |
| `PLA.ListaValidaServicio` | Users subject to role check (LISTA mode) | Comma-delimited | RULE-007 |
| `PLA.CallBatchToken` | Shared secret for /CallBatch endpoint | String (credential) | RULE-050, RULE-051 |
| `PLA.MuestraLog` | Error stack trace disclosure | 'S'/other | RULE-073, RULE-074 |
| `PLA.EnvioNotificacion` | Enable block/unblock email | 'S'/'N' | RULE-069 |
| `ACC.numLongitud` | Min password length | Integer | RULE-031 |
| `ACC.numMayusculas` | Min uppercase letter count | Integer | RULE-031 |
| `ACC.numMinuscula` | Min lowercase letter count | Integer | RULE-031 |
| `ACC.numCaracteres` | Min special-character count | Integer | RULE-031 |

**Hardcoded constants that should become configurable parameters in the target system:**

| Constant | Value | Location |
|---|---|---|
| Retry delay | 60 s | TransactionManager.java:261 |
| Retry eligibility window | 3,600 s | TransactionManager.java:263 |
| Daemon exception cooldown | 60 s | BcsJob.java:119 |
| Zombie threshold | 30 s | BcsJobManager.java:33 |
| Heartbeat interval | 10,000 ms | BcsJobManager.java:31 |
| Reboot-flag poll | 5,000 ms | BcsJobManager.java:32 |
| DB lock-wait timeout | 60,000 ms | CallServlet.java:215 |
| UTC timestamp cache TTL | 3,600,000 ms | SesionUtils.java:93 |
| WebSocket poll period | 10 s | CallWebsocket.java:255 |
| WebSocket lookback | 3,600 s | CallWebsocket.java:198 |
| CallBatch poll (inner) | 1 s | CallBatch.java:192 |
| CallBatch poll (outer) | 10 s | CallBatch.java:189 |
| Daemon next-cycle offset | 1 s | TransactionManager.java:156 |
| JavaView default take | 1,000,000 rows | CallServlet.java:549 |
| SSO OAuth2 scope | "openid profile email phone address" | SSOInit.java:99 |
| SSO company ID | 1L | SSOUtils.java:269 |

---

## Rules Requiring SME Confirmation

| Rule | Question |
|------|----------|
| RULE-011 | What are the full enumerated values for `EsAutorizado`? Is 'N' unauthorized? |
| RULE-018 | Is a CLI entity in BLO state intentionally permitted to log in via CLI/WS? |
| RULE-025 | Is indefinite session validity on the COR channel intentional policy? |
| RULE-030 | Does the browser pre-hash the password before sending it to the server? **(Critical)** |
| RULE-033 | Is the system currently single-company in production? |
| RULE-035 | Confirm exact history depth in `ConsultaLogClaveUsuario.sql`. |
| RULE-045 | Is SSO expiry checking (FechaVigencia) planned or intentionally omitted? |
| RULE-046 | Does the SSOLogon callback validate the returned `state` value? |
| RULE-047 | Is the system currently single-company (SSO company ID = 1)? |
| RULE-052 | Is LIFO processing order of CallBatch jobs intentional? Any ordering dependencies? |
| RULE-059 | Is `updateTryProgramm` called anywhere in the deployed configuration? |
| RULE-068 | What callers supply `parameterName`/`parameterValue` to `executionCount`? |
| RULE-077 | Are credentials stored in Static3DES-encrypted form in any config or DB? |
