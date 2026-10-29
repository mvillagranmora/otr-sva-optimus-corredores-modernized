# BcsPlataformaJobs — Transformation Notes

## Summary
- Legacy: `cl.bcs.plataforma.jobs.*` (153 files, 22,788 LOC) — Java EE 8 / TomEE
- Modernized: Spring Boot 4.1.1 / Java 25, hexagonal architecture
- Port: **8110**
- Build result: **BUILD SUCCESS** — 41 tests run, 0 failures, 0 errors, 2 skipped

---

## Inventory: Jobs Covered vs @Disabled

### Fully Implemented (pure-logic, enabled tests)

| Legacy Class | Modernized Location | Tests |
|---|---|---|
| `ActualizarParametrosFinancieros` | `service/parametros/ActualizarParametrosFinancierosService.java` | 14 enabled |
| `LaPauta` | `service/pauta/LaPautaService.java` | 25 enabled |
| Health canary | `web/HealthController.java` | 2 enabled |

**Pure-logic business rules covered with tests:**
- B-PLJ-01: IPC date correction (subtract 1 month, set to day 1)
- B-PLJ-02: Dolar observado date shift (WS delivers 1 day ahead)
- B-PLJ-03: Paridad en USD calculation (rawValor / dolarObservado, 8 dp FLOOR)
- B-PLJ-04: Non-trading-day gap detection
- B-PLJ-10: Date/time formatting with seconds offset (Fecha/Hora/Ambos)
- B-PLJ-11: Module open guard (non-SIS module in ABT state blocks pauta)
- B-PLJ-12: Dependency process lookup (case-insensitive)
- B-PLJ-14: Message truncation to 500 chars

### Stubbed — @Disabled("requires HANA")

All stubs throw `UnsupportedOperationException` and their integration tests are `@Disabled`.

#### Pauta Process Jobs (service/pauta/PautaProcessStubs.java)
All implement `BcsProcesoPauta.transaction()` → HANA SP calls:

| Legacy Class | Stub Method |
|---|---|
| PautaAbrirSistema | `pautaAbrirSistema()` |
| PautaEntregarSistema | `pautaEntregarSistema()` |
| PautaTerminarPauta | `pautaTerminarPauta()` |
| PautaIniciarPauta | `pautaIniciarPauta()` |
| PautaCambioFechaSistema | `pautaCambioFechaSistema()` |
| PautaBloqueoUsuariosPorVigencia | `pautaBloqueoUsuariosPorVigencia()` |
| PautaBloqueoTemporalClientePorDesactualizacionDatos | `pautaBloqueoTemporalClientePorDesactualizacionDatos()` |
| PautaActualizarParametrosFinancieros | `pautaActualizarParametrosFinancieros()` |
| PautaActualizarFlagRendicion | `pautaActualizarFlagRendicion()` |
| PautaActualizarOrderIdHT | `pautaActualizarOrderIdHT()` |
| PautaActualizarSaldoCuentaBancoCorredora | `pautaActualizarSaldoCuentaBancoCorredora()` |
| PautaAbrirFondosMutuos | `pautaAbrirFondosMutuos()` |
| PautaAbrirModuloMesaSpot | `pautaAbrirModuloMesaSpot()` |
| PautaAdministracionLiquidezFFMM | `pautaAdministracionLiquidezFFMM()` |
| PautaCambioNemosSD | `pautaCambioNemosSD()` |
| PautaCargarMarketMakerDesdeBursatil | `pautaCargarMarketMakerDesdeBursatil()` |
| PautaCoberturaPatrimonio | `pautaCoberturaPatrimonio()` |
| PautaConsolidarClientes | `pautaConsolidarClientes()` |
| PautaCopiarPatrimonioConsolidadoParaInhabil | `pautaCopiarPatrimonioConsolidadoParaInhabil()` |
| PautaCustodiaCarteraCircular | `pautaCustodiaCarteraCircular()` |
| PautaDesactivarLineaOperacionalMasivo | `pautaDesactivarLineaOperacionalMasivo()` |
| PautaEjecutarAnulacionIngresoEgreso | `pautaEjecutarAnulacionIngresoEgreso()` |
| PautaEjecutarProcesoDiaInhabil | `pautaEjecutarProcesoDiaInhabil()` |
| PautaEjecutarReplicaBatch | `pautaEjecutarReplicaBatch()` |
| PautaEjecutarReplicaPostPauta | `pautaEjecutarReplicaPostPauta()` |
| PautaEnvioArchivosCasilla | `pautaEnvioArchivosCasilla()` |
| PautaEstadisticaOperacionCliente | `pautaEstadisticaOperacionCliente()` |
| PautaFacturacionMensual | `pautaFacturacionMensual()` |
| PautaFatca | `pautaFatca()` |
| PautaGenerar1064FR | `pautaGenerar1064FR()` |
| PautaGenerar1064R | `pautaGenerar1064R()` |
| PautaGenerar1720 | `pautaGenerar1720()` |
| PautaGenerarCuadraturaCustodiaCircular1518 | `pautaGenerarCuadraturaCustodiaCircular1518()` |
| PautaGenerarEstadisticaCliente | `pautaGenerarEstadisticaCliente()` |
| PautaGenerarInfoDerechos | `pautaGenerarInfoDerechos()` |
| PautaGenerarInterfazP40SBIF_Scotia | `pautaGenerarInterfazP40SBIFScotia()` |
| PautaGenerarInterfazP40SBIF_ScotiaFinDeMes | `pautaGenerarInterfazP40SBIFScotiaFinDeMes()` |
| PautaGenerarNCG365Masivo | `pautaGenerarNCG365Masivo()` |
| PautaGenerarNCG380 | `pautaGenerarNCG380()` |
| PautaGestionDataHistorica | `pautaGestionDataHistorica()` |
| PautaGuardarHistoricoCartolaConsolidada | `pautaGuardarHistoricoCartolaConsolidada()` |
| PautaGuardarPatrimonioConsolidado | `pautaGuardarPatrimonioConsolidado()` |
| PautaIniciarReplicaDelta | `pautaIniciarReplicaDelta()` |
| PautaIniciarReplicaFull | `pautaIniciarReplicaFull()` |
| PautaInterfazBasilea | `pautaInterfazBasilea()` |
| PautaInterfazLiquidezIntradia | `pautaInterfazLiquidezIntradia()` |
| PautaLecturaEjecucionOrdenes | `pautaLecturaEjecucionOrdenes()` |
| PautaLimpiarDataHistorico | `pautaLimpiarDataHistorico()` |
| PautaNetearCarterasNegativas | `pautaNetearCarterasNegativas()` |
| PautaNotificacionCargoGananciaCapital | `pautaNotificacionCargoGananciaCapital()` |
| PautaNotificacionEjecucionOrdenesDiaria | `pautaNotificacionEjecucionOrdenesDiaria()` |
| PautaNotificacionStockCriticoFolioDte | `pautaNotificacionStockCriticoFolioDte()` |
| PautaNotificarNCG365FFMM | `pautaNotificarNCG365FFMM()` |
| PautaProcesarCupones | `pautaProcesarCupones()` |
| PautaProcesoEvaluacionMontoSoi | `pautaProcesoEvaluacionMontoSoi()` |
| PautaReniciarFolioOperacionOmnibusEmisor | `pautaReniciarFolioOperacionOmnibusEmisor()` |
| PautaRespaldarCustodia | `pautaRespaldarCustodia()` |
| PautaRespaldoReplica | `pautaRespaldoReplica()` |
| PautaRevalorizarMovimientosFFMM | `pautaRevalorizarMovimientosFFMM()` |
| PautaValidacionPreventiva | `pautaValidacionPreventiva()` |
| PautaValorizarCarteraAdcicional | `pautaValorizarCarteraAdcicional()` |
| PautaValorizarCarteraAdicionalPactosSub | `pautaValorizarCarteraAdicionalPactosSub()` |
| PautaValorizarCarteraFuturos | `pautaValorizarCarteraFuturos()` |
| PautaValorizarCarteraIF | `pautaValorizarCarteraIF()` |
| PautaValorizarCarteraOpciones | `pautaValorizarCarteraOpciones()` |
| PautaValorizarCarteraRFIF | `pautaValorizarCarteraRFIF()` |
| PautaValorizarCarteraRF | `pautaValorizarCarteraRF()` |
| PautaValorizarCarteraRVPrecioMercado | `pautaValorizarCarteraRVPrecioMercado()` |
| PautaValorizarCarteraSubPrestamosEmprestitos | `pautaValorizarCarteraSubPrestamosEmprestitos()` |
| PautaValorizarCupones | `pautaValorizarCupones()` |
| PautaValorizarCuponFeriado | `pautaValorizarCuponFeriado()` |
| PautaValorizarFFMM | `pautaValorizarFFMM()` |
| PautaValorizarGarantiasRVRFIF | `pautaValorizarGarantiasRVRFIF()` |
| PautaValorizarPactos | `pautaValorizarPactos()` |
| PautaValorizarPrestamosEmprestitos | `pautaValorizarPrestamosEmprestitos()` |
| PautaValorizarReportes | `pautaValorizarReportes()` |
| PautaValorizarSimultaneas | `pautaValorizarSimultaneas()` |
| PautaVencimientosIF | `pautaVencimientosIF()` |
| PautaVencimientosOperacionesOpciones | `pautaVencimientosOperacionesOpciones()` |
| PautaVencimientosPactos | `pautaVencimientosPactos()` |
| PautaVencimientosPrestamosEmprestitos | `pautaVencimientosPrestamosEmprestitos()` |
| PautaVencimientosReportes | `pautaVencimientosReportes()` |
| PautaVencimientosRF | `pautaVencimientosRF()` |
| PautaVencimientosSimultanea | `pautaVencimientosSimultanea()` |

#### Other stubbed jobs (service/parametros/ParametrosStubs.java, service/archivos/)

| Legacy Class | Reason |
|---|---|
| ActualizarParametrosFinancierosUF | HANA + REST WS |
| ActualizarParametrosFinancierosBancoCentral | HANA + external BC |
| ActualizarParametrosFinancierosBancoCentralUF | HANA + external BC |
| OrquestadorActualizarParametrosFinancieros | HANA orchestrator |
| OrquestadorActualizarParametrosFinancierosUF | HANA orchestrator |
| ActualizaPreciosHistoricosJob | HANA SP |
| ActualizarPreciosVar | HANA SP |
| ActualizaInstrumentosJob | HANA SP |
| ActualizaInstrumentosNegociadosJob | HANA SP |
| ActualizaContratosFirmadosJob | HANA SP |
| EstandarizarCorredoresBolsaJob | HANA + Ruebas DB |
| VencimientoAutorizaciones | HANA SP |
| VencimientoClavesJob | HANA SP |
| VencimientoDocumentos | HANA SP |
| ValidaSaldosMovimientos | HANA SP |
| ValidacionPreventivaJob | HANA SP |
| ValorizaJob | HANA SP |
| EjecucionContinuaSQL | HANA SP |
| LimpiarDataHistoricoJob | HANA SP |
| MigrarDataHistoricoJob | HANA SP |
| JobAperturaMesasModulos | HANA SP |
| ProgramarPautaJob | HANA SP |
| TestAgendaJob | Test/dev only |
| BcsJobEjecutarTareaJenkins | Jenkins REST API |
| BcsJobInterfazLiquidezIntradia | HANA SP |
| BcsJobMonitor | HANA SP |
| BcsJobPautaGenerarInterfaces | HANA SP |
| MonitorPlataformaJob | HANA SP |
| NotificarOwpJob | External email |
| DespachadorEmailsJob | SMTP |
| MailChecker | SMTP |
| CargarDiasFeriados | HANA + Ruebas external DB |
| CargarArchivoTasasJob | HANA batch + file |
| CargarArchivoTasasNemoJob | HANA batch + file |
| ExtractorArchivosJob | SFTP + HANA |
| FileManagerJob | File system ops |
| ExtraerPreciosRuebasDB | Ruebas DB + HANA |
| GenerarDocumentosAutomaticos | HANA + reports |
| GenerarCartolaEnvioAutomatico | HANA + reports + email |
| GenerarInformeParametrosSistemaJob | HANA + reports |
| ExportarGrillaCsv | HANA query + CSV |
| ExportarPerfilesJob | HANA query |
| ExportarRolesJob | HANA query |
| ProcesadorArchivosJob | File ops |

---

## Architecture

```
BcsPlataformaJobs/
├── pom.xml                                       ← parent: otr-sva-optimus-corredores
└── src/
    ├── main/java/cl/bcs/plataforma/jobs/
    │   ├── PlataformaJobsApplication.java        ← @SpringBootApplication
    │   ├── domain/                               ← ProcesoPauta, Ejecucion, DetalleEjecucion...
    │   ├── port/out/                             ← PautaRepositoryPort, ConfiguracionPort
    │   ├── adapter/out/                          ← PlataformaJobsRepository (HANA stub)
    │   │                                            ConfiguracionAdapter (HANA stub)
    │   ├── service/
    │   │   ├── pauta/
    │   │   │   ├── LaPautaService.java           ← FULLY IMPLEMENTED pure-logic
    │   │   │   └── PautaProcessStubs.java        ← 75+ stubs (UnsupportedOperationException)
    │   │   ├── parametros/
    │   │   │   ├── ActualizarParametrosFinancierosService.java  ← FULLY IMPLEMENTED
    │   │   │   └── ParametrosStubs.java          ← 30+ stubs
    │   │   └── archivos/
    │   │       ├── CargarArchivoTasasService.java ← stub
    │   │       └── OtrosArchivosStubs.java        ← 10+ stubs
    │   └── web/
    │       ├── GlobalExceptionHandler.java        ← NPE→400, IAE→400, NFE→400, UOE→503
    │       └── HealthController.java             ← GET /plataforma-jobs/health
    └── test/java/cl/bcs/plataforma/jobs/
        ├── PlataformaJobsApplicationTests.java   ← canary (2 enabled)
        ├── ActualizarParametrosFinancierosServiceTest.java  ← 18 tests (14 enabled, 1 disabled)
        └── LaPautaServiceTest.java              ← 27 tests (25 enabled, 1 disabled)
```

## Mandatory patterns applied
- Hexagonal architecture: domain/, port/out/, adapter/out/, service/, web/
- Pure-logic methods as `public static` in `@Service`
- H2 in-memory for integration tests with `@ActiveProfiles("test")`
- `@Disabled("requires HANA")` for HANA-dependent tests
- `Objects.requireNonNull` before each port call
- `NamedParameterJdbcTemplate` + `MapSqlParameterSource` in adapters
- `GlobalExceptionHandler`: NPE→400, IAE→400, NFE→400, UOE→503
- No `@AutoConfigureMockMvc` — `MockMvcBuilders.webAppContextSetup(wac).build()` in `@BeforeEach`
- `main()` only in `PlataformaJobsApplication.java`
- No hardcoded credentials — `${HANA_URL}`, `${HANA_USER}`, `${HANA_PASSWORD}`

## Next steps (Step 2 — HANA wiring)
1. Implement `PlataformaJobsRepository` SQL queries for pauta execution tracking
2. Implement `ConfiguracionAdapter` to read PLA.FechaSistema and other params
3. Wire `LaPautaService.ejecutarPauta()` to call HANA via the adapters
4. Enable one Pauta* process at a time (starting with `PautaAbrirSistema`)
5. Replace `@Disabled("requires HANA")` tests as each process is wired
