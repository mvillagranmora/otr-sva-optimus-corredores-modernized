# Data Objects — otr-sva-optimus-corredores / Plataforma Core

**Scope:** Core records touched by the business rules in `BUSINESS_RULES.md`.  
All HANA table names use the namespace prefix `BSCL.Plataforma.BD::` unless noted.

---

## DO-001: ACC.Usuario (User Account)

**Schema location:** `BSCL.Plataforma.BD::ACC.Usuario`
**Kind:** Master record (one row per platform user)

| Field | Type | Description |
|-------|------|-------------|
| IdUsuario | Long | Primary key |
| IdEmpresa | Long | Company ID (FK to PLA.Empresa) |
| CodigoUsuario | String | Username (stored and matched UPPER-CASE) |
| Clave | String | SHA-256 uppercase hex password hash (no salt) |
| ClaveProvisoria | String | SHA-256 hex of provisional password (nullable) |
| FechaClaveProvisoria | Timestamp | Date provisional password was issued |
| IntentosConexionClaveProvisoria | Integer | Failed provisional-password attempts |
| IdEstado | String | Account state: `'ACT'` active, `'BLO'` blocked |
| EsAutorizado | String | Authorization status: `'S'` authorized, `'P'` pending |
| FechaVigencia | String | Account expiry date `'YYYY-MM-DD'`; sentinel `'0001-01-01'` = never expires |
| FechaBloqueoTemporal | Timestamp | UTC expiry of temporary lockout (null = not locked) |
| IntentosConexion | Integer | Rolling count of consecutive failed login attempts |
| DebeCambiarClave | String | `'S'` = user must change password at next login |
| TipoEntidad | String | `'EMP'` employee, `'PER'` person, `'CLI'` client |
| IdEntidad | Long | FK to the Empleado / Persona / Cliente record |
| UsuarioCorredores | String | `'S'` = permitted on COR channel |
| UsuarioNegocios | String | `'S'` = permitted on AGE channel |
| UsuarioClientes | String | `'S'` = permitted on CLI channel |
| UsuarioServicios | String | `'S'` = permitted on WS channel |
| IdTipoBloqueo | String | Block-type code when blocked (e.g., `'BVIG'`) |
| ComentarioBloqueo | String | Human-readable block reason |

**Rules that use it:** RULE-008 through RULE-020, RULE-026, RULE-035 through RULE-037, RULE-069, RULE-070

---

## DO-002: ACC.Sesion (Active Session)

**Schema location:** `BSCL.Plataforma.BD::ACC.Sesion`
**Kind:** Operational record (one row per session token)

| Field | Type | Description |
|-------|------|-------------|
| IdTokenSesion | String | Base64-encoded token (company:user:timestamp+UUID) |
| IdUsuario | Long | FK to ACC.Usuario |
| IdCanal | String | `'COR'`, `'AGE'`, `'CLI'`, `'WS'`, `'SSO'` |
| TokenValido | String | `'S'` valid, `'N'` invalidated |
| FechaIngreso | Timestamp | UTC creation time |
| FechaUltimoUso | Timestamp | UTC of last service call (not updated for COR channel) |
| UsoToken | Integer | Usage counter |

**Rules that use it:** RULE-001, RULE-002, RULE-020 through RULE-025

---

## DO-003: PLA.Configuracion (Configuration Parameters)

**Schema location:** `BSCL.Plataforma.BD::PLA.Configuracion`
**Kind:** Reference (key-value config per company)

| Field | Type | Description |
|-------|------|-------------|
| IdEmpresa | Long | Company ID |
| CodigoParametro | String | Parameter key (e.g., `PLA.FechaSistema`) |
| Valor | String | Parameter value (always string; callers parse as needed) |

**Key parameters:** See Configuration Parameter Registry in `BUSINESS_RULES.md`.

**Rules that use it:** RULE-007, RULE-012, RULE-015, RULE-022, RULE-024, RULE-031, RULE-033, RULE-050, RULE-051, RULE-073

---

## DO-004: PLA.Servicio (Service Catalog)

**Schema location:** `BSCL.Plataforma.BD::PLA.Servicio`
**Kind:** Reference (one row per callable service)

| Field | Type | Description |
|-------|------|-------------|
| IdServicio | Long | Service ID (the `t` request parameter) |
| NombreClase | String | Fully-qualified Java class name to instantiate |
| TipoServicio | String | `'S'` standard, `'X'` JavaView, `'B'` batch, etc. |
| ValidaAccesoServicio | String | `'S'` = enforce role-based ACL check (RULE-028) |

**Rules that use it:** RULE-027, RULE-028

---

## DO-005: ACC.PerfilAcceso / ACC.Acceso / ACC.AccesoServicio (RBAC Hierarchy)

**Schema location:** `BSCL.Plataforma.BD::ACC.*`
**Kind:** Reference (role–profile–service access grants)

| Table | Purpose |
|-------|---------|
| `ACC.Rol` | Role definitions |
| `ACC.RolUsuario` | User-to-role assignments |
| `ACC.Perfil` | Access profile definitions |
| `ACC.RolPerfil` | Role-to-profile assignments |
| `ACC.Acceso` | Access grants (profile → service group) |
| `ACC.PerfilAcceso` | Profile-to-access assignments |
| `ACC.AccesoServicio` | Access-to-service mappings |

**Rules that use it:** RULE-006, RULE-028

---

## DO-006: BAT.Programa (Job Program Instance)

**Schema location:** `BSCL.Plataforma.BD::BAT.Programa`
**Kind:** Operational record (one row per job execution instance)

| Field | Type | Description |
|-------|------|-------------|
| IdPrograma | Long | Primary key |
| IdAgenda | Long | FK to BAT.Agenda |
| IdTipoPrograma | String | FK to BAT.TipoPrograma |
| idEstado | String | `'PEN'`, `'ASI'`, `'TER'`, `'ERR'` |
| Bloqueo | String | `'S'` locked (in-flight or completed), `'N'` available |
| Intento | Integer | Number of attempts so far (incremented before execution) |
| Nodo | String | Target node ID (dashes stripped before comparison) |
| fechaEjecucion | Timestamp | Scheduled or retried execution datetime |
| fechaFin | Timestamp | Actual completion datetime (`'0001-01-01'` = not finished) |
| Resultado | String | Result description (truncated to 1999 chars) |

**States:** `PEN` → pending; `ASI` → assigned/running; `TER` → terminated (success); `ERR` → error.

**Rules that use it:** RULE-055 through RULE-067

---

## DO-007: BAT.Agenda (Job Schedule)

**Schema location:** `BSCL.Plataforma.BD::BAT.Agenda`
**Kind:** Reference (defines recurring job schedule)

| Field | Type | Description |
|-------|------|-------------|
| IdAgenda | Long | Primary key |
| NombreClase | String | Java class to execute |
| EsDemonio | String | `'S'` = daemon (runs continuously) |
| habilitado | String | `'S'` = enabled for scheduling |
| Minuto | String | Cycle interval in minutes; `'**'` = use 5-second default |
| SiguienteFechaDeEjecucion | Timestamp | Next scheduled run |
| UltimaFechaDeEjecucion | Timestamp | Last actual run (used for restart detection) |
| Predeterminado | String | `'S'` = default entry (SSO entity config lookup) |

**Rules that use it:** RULE-061, RULE-062, RULE-063

---

## DO-008: BAT.TipoPrograma (Job Type Definition)

**Schema location:** `BSCL.Plataforma.BD::BAT.TipoPrograma`
**Kind:** Reference (defines job type constraints)

| Field | Type | Description |
|-------|------|-------------|
| IdTipoPrograma | String | Primary key |
| MaximoIntentos | Integer | Maximum retry attempts before ERR becomes permanent |

**Rules that use it:** RULE-055, RULE-058, RULE-060

---

## DO-009: BAT.Nodos (Job Manager Node Registry)

**Schema location:** `BSCL.Plataforma.BD::BAT.Nodos`
**Kind:** Operational record (one row per running job-manager JVM)

| Field | Type | Description |
|-------|------|-------------|
| Nodo | String | Node identifier |
| Job | String | Task type this node handles |
| Pid | Long | OS process ID |
| FechaUltimoCiclo | Timestamp | Last heartbeat timestamp |
| Reiniciar | String | `'S'` = node should call System.exit(0) |

**Rules that use it:** RULE-064, RULE-065, RULE-066

---

## DO-010: ACC.PasswordProhibidas (Forbidden Password List)

**Schema location:** `BSCL.Plataforma.BD::ACC.PasswordProhibidas`
**Kind:** Reference (exact-match blocklist)

| Field | Type | Description |
|-------|------|-------------|
| Palabra | String | Forbidden password word (exact match, case per DB collation) |

**Rules that use it:** RULE-034

---

## DO-011: PLA.MensajeWebsocket (WebSocket Message Queue)

**Schema location:** `BSCL.Plataforma.BD::PLA.MensajeWebsocket`
**Kind:** Operational record (server-push event queue)

| Field | Type | Description |
|-------|------|-------------|
| IdEmpresa | Long | Company ID (used to route to correct WebSocket sessions) |
| Evento | String | Event type identifier |
| DATA | String | Event payload |
| EsProcesado | String | `'S'` delivered, `'N'` pending |
| FechaRegistro | Timestamp | Creation timestamp (used for 1-hour lookback window) |

**Rules that use it:** RULE-054

---

## DO-012: ConfiguracionSSO (SSO Provider Configuration)

**Schema location:** `BSCL.Plataforma.BD::PLA.ConfiguracionSSO` (exact name inferred from context)
**Kind:** Reference (one row per SSO identity provider or entity)

| Field | Type | Description |
|-------|------|-------------|
| URLAuth | String | OAuth2 authorization endpoint URL |
| ClientId | String | OAuth2 client ID |
| RedirectURI | String | OAuth2 callback URL |
| Predeterminado | String | `'S'` = default provider |

**Rules that use it:** RULE-049

---

## DO-013: Empleado / Persona / Cliente (Entity Records)

**Schema locations:**
- `BSCL.SocioNegocio.BD::EMP.Empleado`
- `BSCL.SocioNegocio.BD::PER.Persona`
- `BSCL.SocioNegocio.BD::CLI.Cliente`

**Kind:** Master record (entity linked to ACC.Usuario)

| Field | Type | Description |
|-------|------|-------------|
| IdEstado | String | Raw entity status (e.g., `'ACT'`) |
| EsEnrolado | String | `'S'` = enrolled; `'N'` = pending enrollment |
| IdCliente / IdEmpleado / IdPersona | Long | Primary key |

**Derived computed state (from SQL CASE):**
- `IdEstado='ACT' AND EsEnrolado='S'` → `'ACT'` (login allowed)
- `IdEstado='ACT' AND EsEnrolado='N'` → `'PEN'` (login denied)
- `IdEstado != 'ACT'` → `'BLO'` (login denied)

**Rules that use it:** RULE-019, RULE-043, RULE-044

---

## DO-014: DireccionElectronica (Electronic Address)

**Schema location:** `BSCL.SocioNegocio.BD::PER.DireccionElectronica`
**Kind:** Detail record (one per address per entity)

| Field | Type | Description |
|-------|------|-------------|
| Predeterminado | String | `'S'` = primary email address |
| IdEstado | String | `'ACT'` = active |
| DireccionElectronica | String | Email address (matched UPPER for SSO lookup) |

**Rules that use it:** RULE-043

---

## DO-015: PLA.Heartbeat (JVM Heartbeat)

**Schema location:** `BSCL.Plataforma.BD::PLA.Heartbeat`
**Kind:** Operational record (secondary heartbeat for external monitors)

| Field | Type | Description |
|-------|------|-------------|
| Id | String/Long | Process identifier |
| LastTimestamp | Timestamp | Last heartbeat from the JVM |

**Rules that use it:** RULE-064
