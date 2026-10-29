# Phase 1 Pilot Notes — otr-sva-optimus-corredores

**Stack:** Java EE 8 / TomEE 8.0.16 → Spring Boot 4.1.1 / Java 25  
**Pattern:** Strangler-fig, hexagonal architecture (ports-and-adapters)  
**Date completed:** 2026-10-02

---

## What was ported

| Artifact | Status |
|---|---|
| `CentralizaAnticipoPactos` | Full port (canonical template) |
| 51 remaining `Centraliza*` services | Generated from canonical template |
| `CentralizacionOrchestrator` | Replaces `CentralizaMultiSeleccion` Class.forName dispatch |
| `CreaInterfazDiariaJob` | Spring Batch port |
| `ContabilidadRepository` port + JDBC impl | Fully wired |
| `PlataformaPort` + `TomEePlataformaClient` | HTTP proxy to legacy TomEE |

**Not ported (intentional):**
- `CentralizaGeneraCabecera` — utility absorbed into the repository layer; has no `BcsCentralizacion.centralizar()` signature.

---

## Design decisions

### Port extension: generic datos query

Rather than adding 52 class-specific typed methods to `ContabilidadRepository`, a single generic method was added:

```java
List<Map<String,Object>> queryDatosMovimientoRows(String sqlResourceName, MapSqlParameterSource params)
```

Each service loads its own SQL file (`sql/{ClassName}_SelectDatosMovimiento.sql`) and extracts fields with private static helpers (`s`, `d`, `t`, `bd`). This keeps the port stable and pushes SQL specificity to the filesystem.

### PlataformaPort extension

Two methods added beyond the original five:
- `obtenerParametroConfiguracion(idEmpresa, modulo, parametro, valorDefault)` — generic config lookup used by ~12 classes (Devengo*, Facturas*, Compra/VentaPactos, Emprestito/Prestamo inicio, Tesoreria*)
- `consultarClearingFinanciero(...)` — cross-module call to BcsTesoreriaServicios; used only by `CentralizaClearingFinanciero`

### ClearingFinanciero (cross-module)

`ClearingFinanciero` calls `BcsTesoreriaServicios` via `PlataformaPort.consultarClearingFinanciero`. The `ClearingFinancieroDetalle` record lives inside the port interface. The HTTP endpoint is `/api/tesoreria/ConsultarClearingFinanciero` on the same legacy TomEE. No database query is made by this service directly.

### CarterasForward computed events

`CentralizaCarterasForward`, `Valorizacion`, and `Vencimiento` compute `VALOR_RAZONABLE_*` events from an auxiliary `{Class}_SelectMontoEventos.sql` query (via `queryDatosMovimientoRows`) rather than the standard eventos queries. The sign of the net razonable value determines PERDIDA vs UTILIDAD.

### Pre-query pattern (clasificacion)

`CentralizaAnticipoVencimientoEmprestito`, `...Prestamo`, `CentralizaEmprestitoInicio`, and `CentralizaPrestamoInicio` call `queryClasificacionTipoInstrumento` before the datos query. The classification code is passed as a SQL parameter `:ClasificacionTipoInstrumento`.

### Simulation mode

All 52 services check `"N".equals(entrada.simulaOperacion())` before calling `persistirCentralizacion`. In simulation mode (`"S"`) the orchestrator returns the same payload but skips DB writes. This matches the legacy `simulaOperacion` contract exactly.

### Bean naming

Spring bean names follow the legacy `Class.forName` convention: first letter lowercased (e.g., `CentralizaAnticipoPactos` → `"centralizaAnticipoPactos"`). The `CentralizacionOrchestrator` injects `Map<String, BcsCentralizacionService>` and dispatches by this name.

---

## SQL files expected (Phase 2 deliverable)

Each `Centraliza*` service expects SQL files under `BcsContabilidadServicios/src/main/resources/sql/`:

- `{ClassName}_SelectDatosMovimiento.sql` — datos movimiento query
- `{ClassName}_SelectMontoEventos.sql` — only for CarterasForward*, ForwardValorizacion, ForwardVencimiento
- `{ClassName}_SelectEventosOperacion.sql` — only for Tesoreria*, TesoreriaFormasDePago, Facturas (LOC variant)

Standard movimiento/evento queries are shared across all classes (`queryMovimientos.sql`, `queryEventosOperacion.sql`, `queryEventosSubyacente.sql`).

The file `sql/queryClasificacionTipoInstrumento.sql` is needed for the four pre-query classes.

---

## Security constraints (verbatim, from modernization brief)

- **RULE-076 (Sesion.java):** Hardcoded credentials treated as compromised. Rotation required before any deployment. `main()` removed.
- **RULE-077 (Static3DES):** Publicly known key — rotate immediately; replace with AES-256 or a managed-key scheme.
- Patches in `~/.modernize/otr-sva-optimus-corredores/security_remediation.local.patch` — apply before first deployment.
- `SECRETS.local.md` at the same path — contains masked findings with `file:line` citations.

---

## Phase 1 exit criteria — status

| Criterion | Status |
|---|---|
| 52 `Centraliza*` services ported | ✅ Done |
| `CentralizacionOrchestrator` dispatching | ✅ Done |
| 34/34 characterization tests pass | ✅ Done |
| `PILOT_NOTES.md` written | ✅ This file |
| `BASELINE.md` | ⏳ Requires TomEE in staging |
| Dual-execution diff ≥ 1,000 calls | ⏳ Requires staging |
| Credential rotation RULE-076/077 | ⚠️ Day-zero prerequisite — must happen before any deployment |
| Apply `security_remediation.local.patch` | ⚠️ Pending rotation |
| API Gateway deployment to dev EKS | ⏳ After credential rotation |
