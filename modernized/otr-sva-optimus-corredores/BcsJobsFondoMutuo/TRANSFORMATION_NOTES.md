# BcsJobsFondoMutuo — Transformation Notes

## Summary

| Item | Value |
|------|-------|
| Legacy location | `BSCL/FondoMutuo/JAVA/BcsJobsFondoMutuo/` |
| Legacy framework | Java EE 8 / TomEE (BcsJob scheduler) |
| Modern framework | Spring Boot 4.1.1 / Java 25 |
| Port | 8107 |
| Artifact ID | `fondo-mutuo-jobs` |
| Tests | 34 passing, 0 skipped |
| Build | `mvn test` → BUILD SUCCESS |

## Architecture

Hexagonal layout:

```
cl.bcs.fondomutuo.jobs
├── FondoMutuoJobsApplication.java     — @SpringBootApplication (DataSource excluded)
├── domain/                             — immutable records: MensajeSalida, ParametrosPrograma,
│                                         RegistroTraspaso, ApoResFFMM, ReinversionSerieFondoDestino,
│                                         SerieFondoSolicitudes, CustodiaPortafolio
├── port/
│   ├── in/                             (not used: adapter/in/JobController is the inbound adapter)
│   └── out/
│       ├── ParametrosProgramaPort      — reads BAT.ParametrosPrograma from HANA
│       ├── HanaFondoMutuoPort          — direct HANA queries (traspasos, BF solicitudes, config, log)
│       └── FondoMutuoServicioPort      — calls FFMM services via REST (Phase 3)
├── service/                            — 18 @Service classes (one per legacy BcsJob)
├── adapter/
│   ├── in/JobController                — POST /ffmm-jobs/* (18 endpoints)
│   └── out/
│       ├── FondoMutuoJobsRepository    — HANA implementation of ParametrosProgramaPort + HanaFondoMutuoPort
│       └── FondoMutuoServicioRestClient — Phase 2 stub (all methods throw UnsupportedOperationException)
└── exception/GlobalExceptionHandler    — NPE→400, IAE→400, NFE→400, UOE→503
```

## Legacy Jobs Mapped

| Legacy Class | Modern Service | Notes |
|---|---|---|
| BcsJobAdministracionLiquidez | AdministracionLiquidezJob | Delegates to FFMM service |
| BcsJobBloqueoCierreSeries | BloqueoCierreSeriesJob | HANA SP — Phase 3 required |
| BcsJobCargaMasivaSolicitudes | CargaMasivaSolicitudesJob | parseTransacciones (";") — pure logic tested |
| BcsJobCargaOnlineAGF | CargaOnlineAGFJob | Cross-DB (HANA→SQL Server) — Phase 3 |
| BcsJobCertificacionMasivaSolicitudes | CertificacionMasivaSolicitudesJob | parseFiltros + parseIdSolicitudes — tested |
| BcsJobGenerarCertificado31FFMM | GenerarCertificado31FFMMJob | Crystal Reports via FondoMutuoServicioPort |
| BcsJobGenerarInformeRentabilidadFFMMExtranjero | GenerarInformeRentabilidadFFMMExtranjeroJob | Crystal Reports |
| BcsJobTrxCFileManager | TrxCFileManagerJob | Delegates to FFMM service |
| BcsJobTrxPFileManager | TrxPFileManagerJob | Delegates to FFMM service |
| BcsJobValorCuotaFileManager | ValorCuotaFileManagerJob | HANA #temp_table SP — Phase 3 |
| BcsJobValorCuotaInternacionalFileManager | ValorCuotaInternacionalFileManagerJob | HANA #temp_table SP — Phase 3 |
| GenerarArchivo (generaarchivooperacionesagf) | GenerarArchivoAGFJob | AGF file generation |
| BcsJobConfirmarTraspaso | ConfirmarTraspasoJob | Reads HANA + calls FFMM service |
| BcsJobGenerarArchivosFFMM | GenerarArchivosFFMMJob | validarTipoArchivo() — pure logic tested |
| BcsJobNotificarTraspaso | NotificarTraspasoJob | Delegates to FFMM service |
| BcsJobProcesarComisionesNN | ProcesarComisionesNNJob | parseComisiones (",") — tested |
| GenerarArchivoSolicitudAporteRescateJob | GenerarArchivoSolicitudAporteRescateJob | Delegates to FFMM service |
| IngresarOrdenBonificacionFiscalReinversion | IngresarOrdenBonificacionFiscalReinversionJob | Complex BF calculation — all pure logic tested |

## Business Rules Pinned in Tests

| Rule ID | Description | Test |
|---|---|---|
| B-FMJ-01 | Transacciones split by ";" into IdCarga list | CargaMasivaSolicitudesJobTest |
| B-FMJ-02 | Empty/null Transacciones → empty list, no service call | CargaMasivaSolicitudesJobTest |
| B-FMJ-05 | esMasivo≠"S" → individual solicitud ID certification | CertificacionMasivaSolicitudesJobTest |
| B-FMJ-06 | Parallel semicolon arrays form filter records | CertificacionMasivaSolicitudesJobTest |
| B-FMJ-07 | Empty filter/solicitud list → no service call | CertificacionMasivaSolicitudesJobTest |
| B-FMJ-11 | "INFORME_MOV_FFMM" → GenerarArchivoMovimientos | GenerarArchivosFFMMJobTest |
| B-FMJ-12 | "INFORME_PARTICIPE_FFMM" → GenerarArchivoParticipes | GenerarArchivosFFMMJobTest |
| B-FMJ-13 | Unknown tipo archivo → IllegalArgumentException | GenerarArchivosFFMMJobTest |
| B-FMJ-15 | Comisiones split by "," (NOT ";") into IdCartera | IngresarOrdenBonificacionFiscalReinversionTest |
| B-FMJ-17 | porcentaje=0 → PorcentajeCalculado = NumeroCuotas*100/CantidadCustodia (HALF_UP, scale=2) | IngresarOrdenBonificacionFiscalReinversionTest |
| B-FMJ-19 | RESCATE: cuotas = CantidadCustodia * effectivePct / 100 (HALF_DOWN, scale=DecimalesCuota) | IngresarOrdenBonificacionFiscalReinversionTest |
| B-FMJ-20 | Custody overshoot cap: if diff ≤ 1 → adjust NumeroCuotas by negative delta | IngresarOrdenBonificacionFiscalReinversionTest |
| B-FMJ-21 | IdRegimenTributario hardcoded "BF" | IngresarOrdenBonificacionFiscalReinversionTest |

## Key Design Decisions

### D-FMJ-01: BcsJob → @Service
All `BcsJob` subclasses replaced with Spring `@Service` beans. Job parameters
previously injected via `this.getParam()` are now passed directly to the `ejecutar()` method
or read from HANA via `ParametrosProgramaPort`.

### D-FMJ-02: No main() methods
All `public static void main()` debug entrypoints removed per RULE-076.
The single entry point is `FondoMutuoJobsApplication.main()`.

### D-FMJ-03: HANA queries preserved via NamedParameterJdbcTemplate
Direct HANA queries (ConfirmarTraspaso_SelectTraspaso, IngresarOrdenFMBonificacionFiscal_Select*, etc.)
are implemented in `FondoMutuoJobsRepository` using `NamedParameterJdbcTemplate` + `MapSqlParameterSource`.
SQL text mirrors the legacy `.sql` resource files.

### D-FMJ-04: HANA stored procedures → Phase 3
HANA SPs with `#temp_table` input syntax:
- `CargarValorCuota` (ValorCuotaFileManager)
- `CargarValorCuotaInternacional` (ValorCuotaInternacionalFileManager)
- `CierreBloqueoAutomaticoFondos` (BloqueoCierreSeries)

These throw `UnsupportedOperationException` in Phase 2 with a descriptive message.

### D-FMJ-05: DataSource excluded
`FondoMutuoJobsApplication` excludes `DataSourceAutoConfiguration` following the
established pattern of other modernized Jobs modules (BcsAPVJobs, etc.).
At runtime, supply `HANA_URL`, `HANA_USER`, `HANA_PASSWORD` environment variables.

### D-FMJ-06: REST client stubs → Phase 3
`FondoMutuoServicioRestClient` implements `FondoMutuoServicioPort` with all methods
throwing `UnsupportedOperationException`. Phase 3 wires real REST calls to
`BcsFondoMutuoServicios` (configure via `services.fondo-mutuo.base-url`).

### D-FMJ-07: Cross-database CargaOnlineAGF → Phase 3
`BcsJobCargaOnlineAGF` read from HANA and wrote to a SQL Server target database
(configured at runtime via `FONDOMUTUO.TARGET_URL_CARGA_ONLINE` etc.).
This cross-database insert is deferred to Phase 3 as it requires both a HANA connection
and a SQL Server connection.

### D-FMJ-08: Pure logic as public static methods
Per mandatory patterns, pure logic in job classes is exposed as `public static` methods
on the `@Service` class, enabling direct testing without Spring context:
- `CargaMasivaSolicitudesJob.parseTransacciones()`
- `CertificacionMasivaSolicitudesJob.parseFiltros()` / `parseIdSolicitudes()`
- `GenerarArchivosFFMMJob.validarTipoArchivo()`
- `ProcesarComisionesNNJob.parseComisiones()`
- `GenerarCertificado31FFMMJob.parseListaTraspasos()`
- `IngresarOrdenBonificacionFiscalReinversionJob.calcularPorcentaje()`, `calcularNumeroCuotasRescate()`, `aplicarCapCustodia()`

## SQL Files Preserved
All legacy `.sql` resource files are inlined in `FondoMutuoJobsRepository` as text blocks,
maintaining the original HANA-specific SQL syntax:
- `ConfirmarTraspaso_SelectTraspaso.sql` → `consultarTraspasosPendientes()`
- `IngresarOrdenFMBonificacionFiscal_SelectFFMMBF.sql` → `consultarAportesRescateBF()`
- `IngresarOrdenFMBonificacionFiscal_SelectParametros.sql` → `consultarParametros()`
- `IngresarOrdenFMBonificacionFiscal_SelectSerieFondo.sql` → `consultarSeriesFondo()`
- `TrxFileManager_UpdateEstadoCabecera.sql` / `TrxFileManager_UpdateSolicitudAporteRescate.sql`
  → referenced by FFMM services (not directly in this module)
- `ValorCuotaFileManager_*` SQL files → HANA SP with #temp_table syntax (Phase 3)
- `GenerarArchivo_update.sql` → `actualizarRutaSalidaPrograma()`

## Environment Variables

| Variable | Description |
|---|---|
| `HANA_URL` | SAP HANA JDBC URL (e.g. `jdbc:sap://host:30015`) |
| `HANA_USER` | SAP HANA username |
| `HANA_PASSWORD` | SAP HANA password |
| `SERVER_PORT` | HTTP port (default: 8107) |
| `FONDO_MUTUO_URL` | BcsFondoMutuoServicios base URL (Phase 3) |
| `CUSTODIA_URL` | BcsCustodiaServicios base URL (Phase 3) |
| `PLATAFORMA_URL` | Plataforma services base URL (Phase 3) |

## Phase 3 Checklist

- [ ] Implement `FondoMutuoServicioRestClient` REST calls to `BcsFondoMutuoServicios`
- [ ] Implement HANA SP calls via `SimpleJdbcCall` for:
  - `CargarValorCuota` (temp table)
  - `CargarValorCuotaInternacional` (temp table)
  - `CierreBloqueoAutomaticoFondos`
- [ ] Implement cross-database CargaOnlineAGF (HANA → SQL Server)
- [ ] Wire Crystal Reports via `BcsCrystalReports` service for:
  - GenerarCertificado31FFMM
  - GenerarInformeRentabilidadFFMMExtranjero
- [ ] Add integration tests with `@SpringBootTest` + H2 + `@ActiveProfiles("test")`
  for HANA-compatible query paths
