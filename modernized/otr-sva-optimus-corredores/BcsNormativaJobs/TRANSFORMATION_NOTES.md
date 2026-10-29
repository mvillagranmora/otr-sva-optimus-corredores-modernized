# BcsNormativaJobs — Transformation Notes

## Summary

- **Legacy**: Java EE 8 / TomEE, `BcsJob` base class, direct JDBC (`Connection`), `cl.bcs.scheduler.BcsJob`
- **Modern**: Spring Boot 4.1.1 / Java 25, hexagonal architecture, `NamedParameterJdbcTemplate`
- **Port**: 8112
- **Legacy file count**: 229 Java files (~58K LOC)
- **Package groups**: jobs(76), generacion(39), validacion(36), registros(28), complementos(12), carga(8), cuadratura(7), lectores(5), procesos(3), ils(3), exportacion(3), generacircular1064(2), exportacionserv(2), consolidacli(2), cartola(2), cargaipc(1)

---

## Architecture

```
cl.bcs.normativa.jobs/
├── domain/
│   ├── ParametrosJob.java          — generic parameter container (replaces BcsJob.parameters Map)
│   ├── PeriodoComercial.java       — DJ1 file naming logic (pure logic, fully tested)
│   └── ResultadoJob.java           — job execution result
├── port/
│   ├── out/
│   │   ├── ConfiguracionPort.java  — system configuration (PLA.Configuracion)
│   │   ├── NormativaHanaPort.java  — HANA SP delegates (all @Disabled)
│   │   └── ProgramaLogPort.java    — BAT.Programa updates
├── service/
│   ├── generacion/GeneracionService.java       — BcsJobGeneracion
│   ├── validacion/ValidacionService.java       — BcsJobValidacion
│   ├── carga/CargaService.java                 — BcsJobCarga
│   ├── cuadratura/CuadraturaService.java       — BcsJobCuadratura
│   ├── exportacion/ExportacionService.java     — BcsJobExportacion + BcsJobExportacionNCG380
│   ├── interfaz/InterfazService.java           — ALL BcsJobGenerarInterfaz* jobs (25 jobs)
│   ├── declaraciones/CertificadosTributariosService.java — BcsJobGenerarCertificadosTributarios
│   ├── valorizar/ValorizarService.java         — BcsJobValorizarNCG499 + BcsJobValorizarDeclaracion
│   ├── serv/ProcesarServService.java           — BcsJobProcesarServ + BcsJobProcesarPautaServ
│   ├── costos/ProcesarCostosService.java       — BcsJobProcesarCostos + BcsJobProcesarCostosComplemento
│   └── apv/ReporteApvService.java              — BcsJobGenerarReporteAPV + BcsJobCargarDatosReporteAPV
├── adapter/
│   ├── in/NormativaJobsController.java         — REST endpoints for job dispatch
│   └── out/NormativaRepository.java            — HANA + ConfiguracionPort + ProgramaLogPort
└── infrastructure/GlobalExceptionHandler.java  — NPE→400, IAE→400, NFE→400, UOE→503
```

---

## Jobs Inventory: Implemented vs @Disabled

### Fully implemented (pure logic tested)

| Job | Service | Tests |
|-----|---------|-------|
| BcsJobGeneracion | GeneracionService | NJB-GEN-PAR-01/02 |
| BcsJobValidacion | ValidacionService | NJB-VAL-PAR-01/02 |
| BcsJobCarga | CargaService | NJB-CAR-PAR-01 |
| BcsJobCuadratura | CuadraturaService | NJB-CUA-PAR-01 |
| BcsJobExportacion | ExportacionService | NJB-EXP-PAR-01 |
| BcsJobExportacionNCG380 | ExportacionService.ejecutarServ() | NJB-EXP-PAR-01 |
| BcsJobProcesarServ | ProcesarServService | NJB-SRV-PAR-01/02/03 |
| BcsJobProcesarPautaServ | ProcesarServService | NJB-SRV-PAR-01 |
| BcsJobValorizarNCG499 | ValorizarService | NJB-VAL-NCG-PAR-01/02 |
| BcsJobValorizarDeclaracion | ValorizarService | NJB-VAL-DEC-PAR-01/02 |
| BcsJobGenerarCertificadosTributarios | CertificadosTributariosService | NJB-CERT-PAR-01/02/03/04 |
| BcsJobGenerarReporteAPV | ReporteApvService | NJB-APV-PAR-01/02 |
| BcsJobCargarDatosReporteAPV | ReporteApvService.cargarDatos() | NJB-APV-PAR-01/02 |
| BcsJobProcesarCostos | ProcesarCostosService | NJB-CST |
| BcsJobProcesarCostosComplemento | ProcesarCostosService | NJB-CST |
| DJ1 file naming logic (PeriodoComercial) | domain/PeriodoComercial | NJB-10/11/12 (17 tests) |

### Jobs with @Disabled("requires HANA") integration tests

These jobs are wired to REST endpoints but their SP execution requires HANA:

**Interface generation jobs** (all use GenerarInterfazXxx.logica() which calls HANA SPs):
- BcsJobGenerarInterfazC08CBC/CBI/CBN/Todos
- BcsJobGenerarInterfazC40CBC/CBI/CBN/INT/Todos
- BcsJobGenerarInterfazP40Aseguradora/Filial/SBIF
- BcsJobGenerarInterfazCliente/ClientesEnLinea/ClientesFinales
- BcsJobGenerarInterfazDCI/DPD/RCP/SIG
- BcsJobGenerarInterfazOrdenes/OrdenesEnLinea
- BcsJobGenerarInterfazPolizas/DetalleOperacionesPolizas
- BcsJobGenerarInterfazInstrumentosFinancieros
- BcsJobGenerarInterfazLavadoDinero
- BcsJobGenerarInterfazMontosIntermediados
- BcsJobGenerarInterfazAsignaciones
- BcsJobGenerarInterfazArchivoCM
- BcsJobGenerarC08CBP/BcsJobGenerarC40CBP
- BcsJobGenerarP40FilialUnitario
- BcsJobGenerarInformeForwardsVigentesEBox
- BcsJobGenerarInformeGarantiasForwardEBox
- BcsJobGenerarInformeMensualInversionistasRV13419
- BcsJobGenerarInformeSaldoCuentasCambio

**Other HANA-specific jobs**:
- BcsJobConsolidaCli
- BcsJobGenCir1064
- BcsJobncg365FFMMMasivo/MasivoAgenda/Notificacion
- BcsJobCartaCircularizacionMasivo
- BcsJobCertificadosClienteMasivo
- BcsJobCircularizacionGrabarCertificado
- BcsJobGrabarCertificadoCliente
- BcsJobInformeEjecucionOrdenesMasivo
- BcsJobReporteParticipesGravamenes
- BcsJobGenerarCartasCIR1985/MasivasCIR1985
- BcsJobGenerarCertificadosMasivo
- BcsJobGenerarCircularUAF57
- BcsJobGenerarAnexo1Circular20/Detalle
- BcsJobGenerarRepoSicam
- BcsJobCargaDJ1Pagos/BcsJobCargaExtornosInterfaz/BcsJobCargaPagoOperacionesInterfaz
- CargaCalificacionesTributariasBD/CargaTBPRAJHI/CargaTBRELMTK
- GenerarReporteSaldosCuentasContables
- GenerarCartolaProgramadaJob/GenerarCartolasJob (in cartola subpackage)
- BcsJobncg365FFMMNotificacion

**Job groups delegated entirely to HANA SPs** (domain packages not migrated individually):
- generacion/* — 39 files — complex file generation engine (HANA table scan + file I/O)
- validacion/* — 36 files — DJ validation rules engine (HANA-backed)
- registros/* — 28 files — DJ record types (HANA schema-specific)
- complementos/* — 12 files — complementary processing (HANA SP)
- carga/* — 8 files — DJ carga process (HANA)
- cuadratura/* — 7 files — cuadratura process (HANA)
- lectores/* — 5 files — HANA table readers
- procesos/* — 3 files — process controllers
- ils/* — 3 files — ILS processing (HANA)
- exportacion/* — 3 files — exportacion process (HANA)
- generacircular1064/* — 2 files — circular 1064 generation (HANA)
- exportacionserv/* — 2 files — exportacion serv (HANA)
- consolidacli/* — 2 files — client consolidation (HANA)

---

## Domain Logic Preserved (with tests)

### PeriodoComercial (B-NJB-10/11/12)
- `periodicidadEfectiva(periodicidad, rectifica)`: when `rectifica=S` and `periodicidad=M`, returns `"C"` (rectificación mensual → periodicidad comercial)
- `construirNombreArchivoDJ1(plantilla, rut, periodicidad, rectifica, fechaPeriodo, tipoArchivo)`: builds DJ1 filename by replacing template tokens, stripping dashes from RUT and periodo

### ParametrosJob (B-NJB-01/02/03/04/05)
- `requerido(nombre)`: throws IAE if absent/blank
- `requeridoLong(nombre)`: throws NFE if non-numeric
- `opcional(nombre, default)`: returns default if absent/blank

### CertificadosTributariosService (B-NJB-CERT-03)
- `parsearAnoTributario(ano)`: validates 4-digit year in [1900, 2100]

### ProcesarServService (B-NJB-SRV-01/02/03)
- Parameter extraction: IdEmpresa (long), IdTipoTarea (String), IdSolicitudCircular (long)
- SP call: `NOR.ProcesarTarea(idEmpresa, idTipoTarea, idSolicitudCircular, jobId)`

---

## REST Endpoints (port 8112)

| Method | Path | Legacy job |
|--------|------|-----------|
| GET  | /api/normativa/jobs/health | — |
| POST | /api/normativa/jobs/generacion | BcsJobGeneracion |
| POST | /api/normativa/jobs/validacion | BcsJobValidacion |
| POST | /api/normativa/jobs/carga | BcsJobCarga |
| POST | /api/normativa/jobs/cuadratura | BcsJobCuadratura |
| POST | /api/normativa/jobs/exportacion | BcsJobExportacion |
| POST | /api/normativa/jobs/exportacion-ncg380 | BcsJobExportacionNCG380 |
| POST | /api/normativa/jobs/procesar-serv | BcsJobProcesarServ |
| POST | /api/normativa/jobs/valorizar-ncg499 | BcsJobValorizarNCG499 |
| POST | /api/normativa/jobs/valorizar-declaracion | BcsJobValorizarDeclaracion |
| POST | /api/normativa/jobs/certificados-tributarios | BcsJobGenerarCertificadosTributarios |
| POST | /api/normativa/jobs/reporte-apv | BcsJobGenerarReporteAPV |
| POST | /api/normativa/jobs/cargar-datos-apv | BcsJobCargarDatosReporteAPV |
| POST | /api/normativa/jobs/procesar-costos | BcsJobProcesarCostos |

---

## Credentials

No credentials are hardcoded. All connection properties use env vars:
- `HANA_URL`
- `HANA_USER`
- `HANA_PASSWORD`

---

## Test Results (BUILD SUCCESS)

```
Tests run: 64, Failures: 0, Errors: 0, Skipped: 10
```

- **10 @Disabled tests**: require HANA (HANA SP calls)
- **54 enabled tests**: pure logic (ParametrosJob, PeriodoComercial, validators, GlobalExceptionHandler)
- **2 integration tests** (H2): context load + health endpoint
