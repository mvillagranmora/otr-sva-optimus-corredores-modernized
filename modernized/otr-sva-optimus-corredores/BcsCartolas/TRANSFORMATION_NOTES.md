# BcsCartolas — Transformation Notes

## Legacy → Modern mapping

| Legacy artifact | Role | Modern equivalent |
|----------------|------|-------------------|
| `cl.bcs.normativa.cartolas.*` | Shared library (no HTTP) | Spring Boot 4.1.1 microservice on port 8108 |
| `entidades/` | Domain POJOs + ResultSet mappers | `domain/` — clean POJOs, factory logic as `public static` |
| `dal/*Service` (static JDBC) | Raw JDBC + `CallableStatement` SP calls | `adapter/out/Hana*Adapter` with `NamedParameterJdbcTemplate` |
| `procesos/ContextoCartolas` | God-object holding connection + state | Eliminated — each adapter receives `NamedParameterJdbcTemplate` via DI |
| `procesos/ProcesoCargaDatos` | Bulk data-load orchestration | Endpoint `POST /cartolas/carga-datos` → `503 UOE` (HANA-only) |
| `procesos/ProcesoProgramacionCartolas` | Recurring nomination scheduler | `GET /cartolas/nominas/programadas` (queries) + `NominaService.condicionesEjecucionCumplidas` (pure-logic) |
| `nombres/Resolvedor*` | File-name pattern resolvers | `CartolasService.resolverNombre()` (pure-static) exposed via `GET /cartolas/nombre-archivo/*` |
| `caller/ProcedureCaller` | HANA SP orchestration | `@Disabled("requires HANA")` — UOE path tested via `POST /cartolas/carga-datos` → 503 |
| `caller/CallTable` | HANA temp table creation | HANA-only, no H2 equivalent; wrapped in `UnsupportedOperationException` path |
| `batch/Batch` | Batch scheduler SP | Not exposed as REST; scheduling is HANA-platform concern |

## Architecture decisions

### Hexagonal layers

```
adapter/in/     CartolasController, GlobalExceptionHandler
port/in/        CartolasUseCase, NominaUseCase
service/        CartolasService, NominaService
port/out/       CartolaPort, NominaPort, ConfigPort, ParametroPort,
                DatosPort, TrazaPort, PlantillaPort, ClientePort
adapter/out/    HanaCartolaAdapter, HanaNominaAdapter, HanaConfigAdapter,
                HanaParametroAdapter, HanaDatosAdapter, HanaTrazaAdapter,
                HanaPlantillaAdapter, HanaClienteAdapter
domain/         Cartola, Nomina, Config, Datos, Parametro, Plantilla,
                Recurso, Traza, Cliente, EstadoProceso, TipoProceso,
                TipoRecurso, FormatoDatos, Opcion
```

### HANA-specific operations → 503

All stored-procedure calls (`CAR.GrabarCartola`, `CAR.GrabarNomina`, `CAR.PoblarNomina`,
`CAR.ActualizarReproceso`, `CAR.GrabarParametro`, etc.) throw
`UnsupportedOperationException` in the adapter, which the `GlobalExceptionHandler`
maps to **503 Service Unavailable**. Tests covering these paths use
`@Disabled("requires HANA")`.

### Table naming convention

Legacy HANA used schema-qualified names like `"BSCL.Normativa.BD::CAR.Cartola"`.
The modernized adapters use lowercase `car_cartola`, `car_nomina`, etc.,
consistent with the H2 test schema. In production the HANA schema is configured
at the datasource URL level.

### Pure-logic promotion

The following logic was extracted as `public static` methods (testable without Spring context):
- `Cartola.buscarPorIdPortafolio(List<Cartola>, long)` — find by portfolio id
- `Nomina.startOfMonth(String)`, `Nomina.endOfMonth(String)` — date range from period
- `Cliente.buildNombre(String, String, String, String)` — full name composition
- `Recurso.toObject(String tipo, String valor)` — type-safe value conversion
- `CartolasService.extractKeys(String patron)` — file-name template key extraction
- `CartolasService.resolverNombre(String, Map, Date)` — template substitution
- `NominaService.getPeriodoCartolas(LocalDate)` — previous month period
- `NominaService.condicionesEjecucionCumplidas(Map, LocalDateTime)` — scheduling check

### Jasper / Crystal Reports

The legacy module used JasperReports (`.jasper` / `.jrxml` resources stored Base64 in
`CAR.Recurso`). The `Plantilla` and `Recurso` domain classes retain this metadata.
Actual report rendering is delegated to `BcsCartolasMaster` (port 8105), which manages
the Jasper lifecycle. `BcsCartolas` only persists the template metadata.

## Tests

| Test class | Coverage |
|-----------|---------|
| `CartolasIntegrationTest` | H2 integration tests for all REST endpoints — B-CAR-01 through B-CAR-07 (62 tests total) |
| `CartolasServiceTest` | Pure-logic unit tests — B-CAR-08 through B-CAR-16 |

Build result: **BUILD SUCCESS** — 62 tests run, 0 failures, 0 errors, 2 skipped (HANA).

## Port

**8108** — configured via `server.port` in `application.yml`.
