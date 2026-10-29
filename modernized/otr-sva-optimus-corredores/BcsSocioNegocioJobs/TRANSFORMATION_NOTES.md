# BcsSocioNegocioJobs — Transformation Notes

**Phase:** Phase 4  
**Stack:** Java EE 8 / TomEE → Spring Boot 4.1.1 / Java 25  
**Port:** 8094  
**Build status:** BUILD SUCCESS — 19 tests run, 0 failures, 1 skipped (@Disabled HANA SP)

---

## Canary

```
calcularFechaVencimiento(2024-01-01, 30) == 2024-01-31
```

---

## Behavior Mapping Table

| Rule ID   | Legacy location                                     | Behavior                                                                                          | Modern location                                              |
|-----------|-----------------------------------------------------|---------------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| B-BLQ-01  | `BloquearClientePorFechaVencimientoJob.calcularFechaVencimiento()` | `FechaVigencia = FechaModificacion + diasVencimiento days`                          | `ClienteBloqueoService.calcularFechaVencimiento()`          |
| B-BLQ-02  | `PautaBloqueoAutomaticoClientes.transaction()`      | Gate: if previous pauta `IdEstadoPrograma != "TER"` → don't execute, return false               | `ClienteBloqueoService.esPautaAnteriorTerminada()`          |
| B-BLQ-03  | `BloquearClientePorFechaVencimientoJob.logica()`    | Filter: `FechaVigencia.toString() == confFechaSistema` → collect annexes to mark VEN             | `ClienteBloqueoService.clienteVenceHoy()`                   |
| B-BLQ-04  | `BloquearClientePorFechaVencimientoJob.logica()`    | Filter: `BloquearAlVencer == "S"` → update client                                                | `ClienteBloqueoService.debeBloquearAlVencer()`              |
| B-BLQ-05  | `BloquearClientePorFechaVencimientoJob.logica()`    | Hard-coded: `IdTipoBloqueo="BAUTOM"`, `ComentarioBloqueo="Cliente bloqueado automaticamente por fecha de vencimiento"` | `ClienteBloqueoService.buildClienteABloquear()` |
| B-BLQ-06  | `BloquearClientePorFechaVencimientoJob.calcularFechaVencimiento()` | Skip doc if `FechaModificacion == null` OR `IdEstado == "PEN"`                     | `ClienteBloqueoService.calcularFechasVencimientoLote()`     |
| B-BLQ-07  | `BloquearClientePorFechaVencimientoJob.logica()`    | Block client only when `venceHoy AND BloquearAlVencer=="S"`                                       | `ClienteBloqueoService.filtrarClientesABloquear()`          |
| B-BLQ-08  | `BcsJobPurgaProspectoCliente.logica()`              | Calls HANA SP `SP_PURGA_PROSPECTO_CLIENTE` — delegated to service layer                          | `SocioNegocioJobsRepositoryAdapter.purgaProspectosClientes()` → throws UOE |
| B-BLQ-09  | `CargaCelebracionNacimientosJob.logica()`           | Calls HANA SPs `PND.CrearAsuntoPendiente` + `PND.VencerAsuntosPendientes` — not portable        | Documented in port; no H2-testable implementation           |

---

## Architecture Changes

| Concern              | Legacy                                          | Modern                                                     |
|----------------------|-------------------------------------------------|------------------------------------------------------------|
| Runtime              | TomEE (Java EE 8, EJB-style BcsJob)            | Spring Boot 4.1.1, embedded Tomcat                        |
| Entrypoint           | `BcsJob.transaction()` called by TomEE scheduler | REST `POST /api/socionegocio/jobs/bloquear-por-vencimiento` |
| DB access            | `SQL.query()` / `SQL.batch()` with raw JDBC     | `NamedParameterJdbcTemplate` via `SocioNegocioJobsRepositoryAdapter` |
| Config lookup        | `ConfiguracionSistema.getParametroConfiguracionString()` | `SocioNegocioJobsDataPort.obtenerFechaSistema()` / `obtenerDiasVencimiento()` |
| Business logic       | Interleaved with DB calls in `logica()`          | Extracted to `public static` methods in `ClienteBloqueoService` |
| HANA SPs             | Called directly via `CallableStatement`          | Throw `UnsupportedOperationException` (gated for real HANA) |
| Error handling       | Ad-hoc `P_OUT_ERRORESCONTADOR` pattern           | `GlobalExceptionHandler` → HTTP 400/503                   |

---

## Not Ported (HANA SP dependencies)

| Legacy class                        | Reason not ported                                              |
|-------------------------------------|----------------------------------------------------------------|
| `CargaCelebracionNacimientosJob`    | Relies on HANA temp tables + 2 SPs; no portable H2 equivalent |
| `BcsJobPurgaProspectoCliente`       | Full logic delegates to `PurgaProspectoCliente` HANA SP        |
| `PautaBloqueoAutomaticoClientes`    | Orchestrator; sub-processes remain HANA-specific               |
| `PautaBloqueoDesbloqueoAutomaticoPortafolio` | Calls portfolio blocking SPs                         |
| `PautaDesbloqueoAutomaticoClientes` | Mirror of blocking pattern — unblocking via HANA SP           |

---

## Executed-Case Count

| Test class                          | Active | Skipped | Total |
|-------------------------------------|--------|---------|-------|
| `ClienteBloqueoServiceTest`         | 13     | 0       | 13    |
| `SocioNegocioJobsIntegrationTest`   | 5      | 1       | 6     |
| **Total**                           | **18** | **1**   | **19**|
