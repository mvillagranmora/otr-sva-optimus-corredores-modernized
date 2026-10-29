# BcsCartolasJobs — Transformation Notes

## Summary

Transforms `BcsCartolasJobs` from Java EE 8 / TomEE to Spring Boot 4.1.1 / Java 25.

| Item | Legacy | Modernized |
|------|--------|------------|
| Stack | Java EE 8 / TomEE | Spring Boot 4.1.1 / Java 25 |
| Job framework | `BcsJob` (custom TomEE scheduler) | `@Service` + REST endpoints |
| HTTP port | N/A (EJB) | 8104 |
| DB access | `javax.sql.DataSource` / `Connection` | `NamedParameterJdbcTemplate` (stubbed) |
| Architecture | Monolithic EAR | Hexagonal (domain / service / port / adapter) |

---

## Legacy Source Files Covered

| Legacy file | Role |
|-------------|------|
| `BcsJobCartolas.java` | Main cartolas job (BcsJob subclass) |
| `BcsScheduledJobCartolas.java` | Scheduled cartolas job (Thread.sleep 30s) |
| `ProcesoCartolasApp.java` | Process orchestrator — TipoProceso routing |
| `ProcesoProgramadoCartolasApp.java` | Programmed process CLI |
| `CargadorDatosCartolasApp.java` | Data loader CLI |
| `EmisorCartolasApp.java` | Emission CLI |
| `EmisorOnlineApp.java` | Online emission CLI |
| `EmisorParcialCartolasApp.java` | Partial emission CLI |
| `EmisorUnitarioApp.java` | Unit emission CLI |
| `ConfiguradorCartolasApp.java` | Configurator CLI |
| `SimuladorCartolasApp.java` | Simulator CLI |
| `generator/DataGenerator.java` | Test data generator |
| `generator/Field.java` | Field descriptor |
| `generator/GeneratorApp.java` | SP generator app |
| `generator/NameValue.java` | Name-value pair |

---

## Architecture Decisions

### Hexagonal Architecture
```
domain/
  TipoProceso.java         — constants (PROGRAMADO, NORMAL, RECURRENTE, REPROCESO, REEMPAQUETA)
  PasosEjecucion.java      — pure record: which steps to execute per TipoProceso
  CartolasJobRequest.java  — input value object with compact constructor validation

service/
  CartolasJobService.java        — implements CartolasJobUseCase; static pure methods for tests
  ProgramacionCartolasService.java — delegates to ProgramacionPort

port/in/
  CartolasJobUseCase.java  — inbound port

port/out/
  NominaPort.java          — nómina operations (abrir/cerrar/poblar)
  ParametroPort.java       — save parameters
  CargaDatosPort.java      — data loading process
  EmisionPort.java         — emission process
  ProgramacionPort.java    — scheduled process

adapter/in/
  CartolasJobController.java   — REST controller (POST /api/jobs/cartolas/ejecutar)
  GlobalExceptionHandler.java  — NPE/IAE/NFE → 400; UOE → 503

adapter/out/
  HanaNominaAdapter.java        — stub (UnsupportedOperationException)
  HanaParametroAdapter.java     — stub
  HanaCargaDatosAdapter.java    — stub
  HanaEmisionAdapter.java       — stub
  HanaProgramacionAdapter.java  — stub
```

### Business Rules Extracted

**B-CAR-01 — TipoProceso Routing** (Oracle: `ProcesoCartolasApp.procesar()` lines 64-78)
- `PROGRAMADO`  → guardar parámetros only (1 step)
- `NORMAL`      → guardar parámetros + poblar nómina + cargar datos + emitir (4 steps)
- `RECURRENTE`  → same as NORMAL (4 steps)
- `REPROCESO`   → cargar datos + emitir (2 steps, without repopulating nómina)
- `REEMPAQUETA` → emitir only (1 step, without reloading data)

**B-CAR-02 — Nómina Certification Guard** (Oracle: `ProcesoCartolasApp.procesar()` lines 57-58)
- If nómina is already certified → `IllegalStateException` (was `NominaException` in legacy)

**B-CAR-03 — Nómina lifecycle** (Oracle: `Nomina.abrir()` / `Nomina.cerrar()`)
- nomina.abrir() before processing
- nomina.cerrar() in success path
- nomina.cerrarConError() in catch block

**B-CAR-07 — Scheduled Process** (Oracle: `BcsScheduledJobCartolas.transaction()`)
- Legacy used `Thread.sleep(30000)` to avoid double execution in same 1-minute cycle
- Spring Boot equivalent: `@Scheduled(fixedDelay=30000)` on the caller side

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/jobs/cartolas/ejecutar` | Execute cartolas process (B-CAR-01) |
| `POST` | `/api/jobs/cartolas/programado/ejecutar` | Execute scheduled process (B-CAR-07) |
| `GET`  | `/api/jobs/cartolas/flujo/{tipoProceso}` | Describe steps for a TipoProceso (pure logic) |

---

## Test Results

```
Tests run: 35, Failures: 0, Errors: 0, Skipped: 3
BUILD SUCCESS
```

Skipped: 3 integration tests annotated `@Disabled("requires HANA")`.

### Canary Tests
- `CAR-01-02`: NORMAL has 4 steps — change to 3 in `PasosEjecucion` → test fails
- `CAR-01-09`: `contarPasosActivos(NORMAL) == 4` — CANARY

---

## What Requires HANA to Wire Up (Phase 3+)

All five `Hana*Adapter` classes currently throw `UnsupportedOperationException`.
To wire them up in Phase 3:

1. **HanaNominaAdapter** — call SPs: `CAR.NominaSelectCertificada`, `CAR.NominaAbrir`, `CAR.NominaCerrar`, `CAR.NominaPoblar`, `CAR.NominaSelectActiva`
2. **HanaParametroAdapter** — call SP: `CAR.ParametroSave`
3. **HanaCargaDatosAdapter** — invoke `ProcesoCargaDatos` logic via HANA SPs
4. **HanaEmisionAdapter** — invoke `ProcesoEmisionMaster` + Crystal Reports via `BcsCrystalReports` service
5. **HanaProgramacionAdapter** — invoke `ProcesoProgramacionCartolas` SPs

SP prefix from legacy: `BSCL.Normativa.BD.Servicios::CAR.*`

---

## Dropped in Modernization

| Legacy component | Reason dropped |
|------------------|----------------|
| `EmisorOnlineApp.java` | Developer-only diagnostic tool (hardcoded user/path); no production use |
| `ConfiguradorCartolasApp.java` | One-time DB setup tool; to be replaced by a migration script |
| `SimuladorCartolasApp.java` | Test-data generation; not needed in production service |
| `generator/` package (DataGenerator, Field, GeneratorApp, NameValue) | SP scaffold generator for development only |
| `CargadorDatosCartolasApp.java`, `EmisorParcialCartolasApp.java`, `EmisorUnitarioApp.java` | CLI wrappers; functionality exposed through REST endpoints |
| JasperReports dependency | Not applicable to Spring Boot service |
| `Thread.sleep(30000)` | Replaced by `@Scheduled(fixedDelay=30000)` pattern |

---

## Credentials

No credentials are hardcoded. All HANA connection parameters use environment variables:
- `HANA_URL`
- `HANA_USER`
- `HANA_PASSWORD`
