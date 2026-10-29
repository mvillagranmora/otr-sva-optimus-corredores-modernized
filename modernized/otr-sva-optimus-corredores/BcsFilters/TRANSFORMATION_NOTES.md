# BcsFilters — Transformation Notes

## Overview

Legacy: `BcsFilters` — two Java EE 8 / TomEE servlet filter classes applied to the monolithic
web application.

Modernized: `bcs-filters` — a standalone Spring Boot 4.1.1 / Java 25 authentication gateway
running on port **8116**.

Architecture pattern: **Hexagonal** (domain ↔ port ↔ adapter).

---

## What changed

### 1. Servlet Filter → `OncePerRequestFilter`

| Legacy | Modernized |
|---|---|
| `javax.servlet.Filter` (EE 8) | `jakarta.servlet.http.HttpServletRequest` + `OncePerRequestFilter` (Spring 7 / Jakarta EE 11) |
| `@Resource(name="jdbc/hanaDB")` | Spring `JdbcTemplate` injected via constructor |
| `DataSource` + raw `PreparedStatement` | `JdbcTemplate` parameterised queries |

### 2. HANA catalog table names → portable SQL

Legacy SQL used HANA-specific catalog notation (e.g.
`"BSCL.Plataforma.BD::ACC.Sesion"`).
Modernized SQL uses portable table names (`ACC_SESION`, `PLA_CONFIGURACION`, …) compatible
with both SAP HANA (production) and H2 (tests).

### 3. Security layer expanded

In addition to the two filters, the modernised module exposes the authentication endpoints that
were listed in the legacy filter whitelists but implemented elsewhere in the monolith:
`/PLA/CrearSesion` (local login), `/PLA/SSOInit`, `/PLA/SSOLogon`.

---

## Intentional security fixes (deviations from legacy behaviour)

### RULE-004 — Authorization bypass via arbitrary channel value

**File:** `ServiceAccessFilter.java`

**Legacy bug (line 140 of `ServiceFilter.java`):**
```java
if (... || ch != null || noAuthChanels.contains(ch))
```
The condition `ch != null` was evaluated *before* the list check.  Any request carrying
*any* `ch` query parameter (e.g. `ch=HACKER`) bypassed the entire service-access check.

**Fix applied:**
```java
// ch != null alone does NOT bypass — only known trusted channels bypass
if (... || (ch != null && NO_AUTH_CHANNELS.contains(ch)))
```
Only `WS`, `CLI`, and `AGE` are whitelisted.

**Test:** `SecurityFixesTest.Rule004.unknown_channel_does_not_bypass_service_access`

---

### RULE-005 — Fail-open on exceptions (both filters)

**Files:** `SessionValidationFilter.java`, `ServiceAccessFilter.java`

**Legacy bug:**
```java
catch (Exception e) {
    e.printStackTrace();
    chain.doFilter(request, response); // ← access GRANTED on any exception
}
```
Any database error, timeout, or NPE silently granted access.

**Fix applied (fail-closed):**
```java
catch (Exception e) {
    log.error("...", e);
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "...");
    // chain.doFilter is NOT called
}
```

**Tests:** `SecurityFixesTest.Rule005.invalid_token_denied`,
`ServiceAccessFilterUnitTest.rule005_exception_in_access_check_returns_401`,
`ServiceAccessFilterUnitTest.rule005_exception_in_getCodigoUsuario_returns_401`

---

### RULE-018 — Entities with estado BLO can log in

**File:** `LoginService.java`

**Legacy bug:** The login path only checked whether a user existed; it did not validate the
`estado` field.  Blocked (`BLO`) users could authenticate successfully.

**Fix applied:**
```java
// Only ACT entities may log in
if (!"ACT".equals(usuario.estado())) {
    throw new DisabledException("Account is not active");
}
```

**Test:** `SecurityFixesTest.Rule018.estado_BLO_login_denied`

---

### RULE-025 — COR-channel sessions have no expiry

**File:** `SessionValidationFilter.java`

**Legacy bug:** Sessions for the `COR` (Corredor) channel were created with no timeout.
An attacker who obtained a valid COR token could use it indefinitely.

**Fix applied:**
```java
if ("COR".equalsIgnoreCase(sesion.canal())) {
    LocalDateTime expiry = sesion.fechaCreacion().plusMinutes(corTimeoutMinutes);
    if (LocalDateTime.now().isAfter(expiry)) {
        response.sendError(401, "Session Expired");
        return;
    }
}
```
Timeout is configurable via `COR_SESSION_TIMEOUT_MINUTES` env var (default: 30 minutes).

**Tests:** `SecurityFixesTest.Rule025.expired_cor_session_rejected`,
`SecurityFixesTest.Rule025.recent_cor_session_accepted`

---

### RULE-040 — Provisional password lock off-by-one

**File:** `LoginService.java`

**Legacy bug:**
```java
if (intentos > maxIntentos)  // locks at attempt max+1
```
A user with `maxIntentos=3` could make **4** failed attempts before being locked.

**Fix applied:**
```java
if (usuario.intentos() >= maxIntentos)  // locks at attempt max
```

**Test:** `SecurityFixesTest.Rule040.intentos_equal_max_is_locked`
(test user `usuario_max` has `intentos=3`, `maxIntentos=3` from config → locked immediately)

---

### RULE-045 — SSO login omits FechaVigencia check

**File:** `SsoService.java`

**Legacy bug:** The SSO (`SSOLogon`) code path did not validate the user's account-expiry date
(`FechaVigencia`), even though the local-password login path did.

**Fix applied:**
```java
if (usuario.fechaVigencia() != null && usuario.fechaVigencia().isBefore(LocalDate.now())) {
    throw new AccountExpiredException("Account has expired");
}
```
Added to `SsoService.handleCallback()` — identical expiry check as in `LoginService.login()`.

**Test:** `SecurityFixesTest.Rule045.sso_expired_account_rejected`

---

### RULE-046 — SSO CSRF state hardcoded

**File:** `SsoService.java`, `SsoStateJdbcAdapter.java`

**Legacy bug:**
```java
String state = "fee7e77..."; // hardcoded constant
```
Any attacker who knew the constant could forge an SSO callback and hijack the flow.

**Fix applied (two parts):**
1. **Generate:** `String state = UUID.randomUUID().toString();` — unique per flow.
2. **Validate:** state is stored in `SSO_STATE` table on `SSOInit` and consumed (deleted)
   atomically on `SSOLogon` callback.  Mismatches and replays return HTTP 401.

**Tests:** `SecurityFixesTest.Rule046.ssoinit_returns_uuid_state`,
`SecurityFixesTest.Rule046.ssoinit_produces_unique_states`,
`SecurityFixesTest.Rule046.sso_callback_wrong_state_rejected`,
`SecurityFixesTest.Rule046.sso_state_is_single_use`

---

## Other fixes (not in the original 6 rules)

* `NO_AUTH_SERVICES.contains(t)` evaluated before `t == null` in legacy would throw NPE on
  `ImmutableList.contains(null)` → reordered conditions so `t == null` is checked first
  (harmless logic change; behaviour is identical when t is non-null).

---

## Test strategy

| Test class | Purpose |
|---|---|
| `SecurityFixesTest` | Integration tests for all 6 security rules; H2 in-memory DB |
| `ServiceAccessFilterUnitTest` | Unit tests for RULE-004 and RULE-005 using Mockito |
| `BcsFiltersIntegrationTest` | General wiring, whitelist, happy-path integration tests |

H2 schema: `src/test/resources/schema-filters.sql`
H2 seed data: `src/test/resources/data-filters.sql`

HANA-specific full role/profile access tree query is replaced by a simplified
direct-grant table (`ACC_ACCESO_SERVICIO`) compatible with both H2 and HANA.
The original complex HANA query is **not** annotated `@Disabled("requires HANA")` because
the access-control logic itself is portable — only the legacy catalog naming was HANA-specific.

---

## Port

`8116` (configured in `application.yml`; overridable via standard Spring `server.port`).
