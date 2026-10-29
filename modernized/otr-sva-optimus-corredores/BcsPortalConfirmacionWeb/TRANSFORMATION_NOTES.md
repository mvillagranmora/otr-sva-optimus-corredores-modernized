# BcsPortalConfirmacionWeb — Transformation Notes

## Legacy → Modernized mapping

| Legacy | Modernized |
|--------|------------|
| `cl.bcs.notificacion.validate.NotificationsValidate` (HttpServlet, `@WebServlet("/validate")`) | `ValidateController` (`@RestController`, GET+POST `/validate`) |
| `cl.bcs.notificacion.validate.sql.ConnectionDB` | `NotificationJdbcAdapter` (`@Repository`, implements `NotificationPort`) |
| `cl.bcs.notificacion.template.EvalTemplate` | `EvalTemplateService` (`@Service`, all methods `public static`) |
| `cl.bcs.notificacion.template.KeyValue` (mutable POJO) | `domain/KeyValue` (Java 25 `record`) |
| JNDI `@Resource(name="jdbc/hanaDB") DataSource` | Spring Boot `NamedParameterJdbcTemplate` via auto-configured datasource |
| `javax.servlet.*` | Spring MVC / Spring Boot 4.1.1 |

## Architecture applied

Hexagonal (ports & adapters):
- `domain/` — `KeyValue` record
- `service/` — `EvalTemplateService`, `NotificationService`
- `port/in/` — `ValidateNotificationUseCase`
- `port/out/` — `NotificationPort`
- `adapter/in/` — `ValidateController`, `GlobalExceptionHandler`
- `adapter/out/` — `NotificationJdbcAdapter`

## Behaviour preserved (characterization)

| Rule | Legacy source | Notes |
|------|---------------|-------|
| B-CONF-01 | `validate()` line 62–63 | Null token or null URL → 400 |
| B-CONF-02 | `validate()` line 66–71 | No rut → fetch + inject token into HTML |
| B-CONF-03 | `validate()` line 72–86 | Rut, no accion → return template + `\|\|1\|\|0\|\|rut\|\|token` |
| B-CONF-04 | `validate()` line 88–100 | accion="1"→ACP accept; "2"→RZD reject |
| B-CONF-05 | `EvalTemplate.eval()` | FreeMarker template evaluation with JSON context |
| B-CONF-06 | `EvalTemplate.Agrupa()` | Group KeyValues by codigo into JSON array per code |
| B-CONF-07 | `EvalTemplate.NoAgrupa()` | Flat-group KeyValues by codigo into JSON object per code |

## Bug fixes

1. **Thread-safety** — Legacy `EvalTemplate` stored `reqObj` as an instance field, causing
   concurrent-request corruption. Modernized version uses local variables only.
2. **NPE in template check** — Legacy: `if (template != null || !template.equals("-1"))` is
   always true when `template != null`, and throws NPE when null. Fixed to
   `template != null && !"-1".equals(template)`.
3. **Deprecated `new URL(String)`** — Java 20+ deprecates `URL(String)`. Replaced with
   `URI.create().toURL()`.
4. **Hardcoded HANA credentials** — Legacy `sapHanaConnection()` had `"jdbc:sap://161.238.34.246:30215/BSCL"`,
   `"VARIACODE"`, `"Variacode123456"`. Removed entirely; all connectivity through
   `${HANA_URL}` / `${HANA_USER}` / `${HANA_PASSWORD}`.

## Security remediation

- Credentials removed (hardcoded IP + username + password in legacy servlet).
- All datasource configuration via environment variables (see `application.yml`).

## HANA dependencies (disabled in tests)

| Operation | HANA artifact | Test status |
|-----------|---------------|-------------|
| `validateToken()` | SP `BSCL.Plataforma.BD.Servicios::EVT.ValidarTokenNotificacion` | `@Disabled("requires HANA")` |
| `validateTokenAndId()` | SP `BSCL.Plataforma.BD.Servicios::EVT.ValidarConfirmarNotificacion` | `@Disabled("requires HANA")` |
| `getConfirmationMacro()` | table `BSCL.Plataforma.BD::EVT.TagGenerales` | H2-compatible ✓ |
| `getUrlHana()` | table `BSCL.Plataforma.BD::PLA.Configuracion` | H2-compatible ✓ |
| `acceptOrReject()` | table `BSCL.Plataforma.BD::EVT.Notificacion` (`CURRENT_UTCTIMESTAMP`) | H2-compatible ✓ (uses `CURRENT_TIMESTAMP`) |

## H2 table mapping

| HANA table | H2 test table |
|------------|---------------|
| `"BSCL.Plataforma.BD::EVT.TagGenerales"` | `EVT_TAG_GENERALES` |
| `"BSCL.Plataforma.BD::PLA.Configuracion"` | `PLA_CONFIGURACION` |
| `"BSCL.Plataforma.BD::EVT.Notificacion"` | `EVT_NOTIFICACION` |

## Dependencies added

| Artifact | Reason |
|----------|--------|
| `org.freemarker:freemarker` | FreeMarker template evaluation (managed by Spring Boot) |
| `com.google.code.gson:gson` | JSON context model + `JsonObject`/`JsonArray` replacing `org.json` |
| `org.apache.commons:commons-text:1.12.0` | `StringEscapeUtils` replacing deprecated `commons-lang:2.x` |

## Port (server)

Port **8103** configured in `src/main/resources/application.yml`.

## Test results

```
Tests run: 24, Failures: 0, Errors: 0, Skipped: 2
BUILD SUCCESS
```

- 15 unit tests (`EvalTemplateServiceTest`) — no Spring context, no HANA.
- 9 integration tests (`PortalConfirmacionIntegrationTest`) — Spring Boot + H2; 2 skipped (HANA SPs).
