# Auditoría de Equivalencia — Jobs y Servicios Pequeños
**Fecha**: 2026-10-07
**Microservicios auditados**: 29
**Clases/servicios auditados**: ~320 (legacy) → ~540 archivos Java modernizados (main)

---

## Por Microservicio

---

### BcsAPVJobs
**Legado encontrado**: Sí — `BSCL/APV/JAVA/BcsAPVJobs/src/cl/bcs/APV/jobs/`
**Archivos legacy**: 4 jobs
**Archivos modernizados**: 30 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 4 |
| ⚠️ SCHEDULER PENDIENTE | 1 (trigger cron no implementado) |

#### Hallazgos significativos
##### Scheduler/Trigger sin configurar
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: MEDIO
**Divergencia**: La aplicación tiene `@EnableScheduling` en `ApvJobsApplication.java` pero ningún método `@Scheduled` está definido. El README indica explícitamente que "El mecanismo de disparo (cron, REST trigger o Quartz) queda diferido a Fase 3." En legacy, los jobs son invocados por `cl.bcs.scheduler.BcsJobManager` (TomEE scheduled). Los 4 jobs (`CargaMasivaPreviredJob`, `IngresarOrdenBonificacionFiscalJob`, `IngresarOrdenFMBonificacionFiscalJob`, `IngresarOrdenRVBonificacionFiscalJob`) existen como `@Service` pero sin disparador activo.

---

### BcsCuentaInversionJobs
**Legado encontrado**: Sí — `BSCL/CuentaInversion/JAVA/BcsCuentaInversionJobs/src/`
**Archivos legacy**: 23 clases
**Archivos modernizados**: 40 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 19 |
| ❓ SIN_LEGADO | 0 |

#### Hallazgos significativos
Sin divergencias críticas. Todos los jobs identificados en legacy tienen correspondencia en modernizado con cobertura estructural completa. El trigger es HTTP POST (cron externo → REST endpoint), coherente con la arquitectura modernizada.

---

### BcsCustodiaJobs
**Legado encontrado**: Sí — `BSCL/Custodia/JAVA/BcsCustodiaJobs/src/`
**Archivos legacy**: 37 clases de job
**Archivos modernizados**: 9 main Java (solo 6 endpoints HTTP)

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 3 |
| ❌ STUB / SIN CUBRIR | 27 |
| ⚠️ DIVERGENTE | 2 |

#### Hallazgos significativos

##### Jobs BlockChain — sin equivalente
**Estado**: ❌ STUB
**Riesgo**: ALTO
**Divergencia**: Los jobs `CrearPrestamoDesdeBlockChainJob`, `ProcesaLogBlockChainJob`, `ActualizaEstadosBlockChainJob`, `ReintentoPrestamosBlockChainJob` no tienen endpoints ni servicios correspondientes en el modernizado. Representan lógica crítica de integración blockchain.

##### Jobs de archivo — sin cubrir
**Estado**: ❌ STUB
**Riesgo**: ALTO
**Divergencia**: Los siguientes jobs legacy no tienen equivalente en modernizado: `GenerarArchivoInformeMayorMenorValor`, `GenerarArchivoInformeSimultaneas`, `GenerarArchivoGarantia`, `CargarArchivoRetenciones`, `CargarArchivoCustodia`, `GenerarResumenPosiciones`, `GenerarDetallePosiciones`, `DerivadosNeteo`, `NetearCarteraNegativaJob`, `GuardarPatrimonioCustodiaJob`, `GuardarPatrimonioConsolidadoJob`, `ValorizarCustodiaUIbatchJob`, `CargaVariacionCapitalLog`, `ReporteCuadratura`, `JobRetiraCustodiaDeMovimientoNominal`, `BcsJobProcesaCargaMasivaCustodia`, `ProcesarEventosCapitalesRetroactivos`.

##### Solo 6 endpoints implementados
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencia**: El modernizado expone únicamente: `GET /fecha-sistema`, `GET /precios`, `PUT /precios` (actualizar precio), `GET /posiciones`, `POST /vencimiento`, `POST /tipificar`, `POST /validar-horario`. Esto cubre solo `RegistrarPrecioInstrumentosJob`, `VencimientoProductos` (parcial), `ValidarHorarioEjercicioBloqueoOpciones` (lógica pura) del universo de 37 jobs legacy.

---

### BcsFacturacionJobs
**Legado encontrado**: Sí — `BSCL/Facturacion/JAVA/BcsFacturacionJobs/src/`
**Archivos legacy**: 14 clases
**Archivos modernizados**: 9 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 3 |
| ❌ STUB | 5 |
| ⚠️ DIVERGENTE | 1 |

#### Hallazgos significativos

##### FormatoArchivoService — equivalente exacto
**Estado**: ✅ EQUIVALENTE
**Nota**: La lógica de formateo CHAR/NUM/DEC/DATE está fielmente replicada incluyendo el separador decimal extra no contado en el largo, truncado de decimales y relleno con ceros/espacios.

##### Jobs sin equivalente
**Estado**: ❌ STUB
**Riesgo**: ALTO
**Divergencia**: No tienen endpoint ni servicio en modernizado: `BcsJobGenerarInformesAFP`, `BcsJobGenerarInformesComprobanteComisionesDiarias`, `BcsJobGenerarInformesComprobanteComisionesMensuales`, `BcsJobGenerarInformeMasivoIGK`, `BcsJobDte`/`Dte`, `BcsJobGenerarPrefacturasMasivas` (parcial), `EmisionMasivaComprobanteJob` (parcial). El endpoint `POST /emitir-masivo` cubre parcialmente `EmisionMasivaComprobanteJob` y `POST /prefacturas-masivas` cubre `BcsJobGenerarPrefacturasMasivas`, pero sin implementación completa de la lógica de negocio (delegan a dataPort).

---

### BcsContabilidadJobs
**Legado encontrado**: Sí — `BSCL/Contabilidad/JAVA/BcsContabilidadJobs/src/`
**Archivos legacy**: 8 clases
**Archivos modernizados**: 15 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 3 |
| ❌ STUB (sin trigger) | 1 |
| ⚠️ DIVERGENTE | 1 |

#### Hallazgos significativos

##### InterfazContableJobService — bien implementado
**Estado**: ✅ EQUIVALENTE
**Nota**: Decodificación base64 UTF-8 → secuencias separadas por ";", login SAP B1, verificación de BusinessPartner (404/200), envío asiento contable, marcado de errores. La lógica fluye idéntica al legacy `InterfazContable.logica()`.

##### CentralizaMultiSeleccionJob — no encontrado
**Estado**: ❌ STUB
**Riesgo**: MEDIO
**Divergencia**: `CentralizaMultiSeleccionJob.java` del legacy no tiene equivalente en el modernizado. Su función de centralización multi-selección no está cubierta.

##### Sin controlador REST ni trigger
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencia**: El modernizado no tiene `@RestController` ni `CommandLineRunner` ni `@Scheduled`. Los beans de servicio (`InterfazContableJobService`, `CreaInterfazDemandaJobService`, `GenerarInformeClienteRelacionadoBatchJobService`) existen pero no tienen mecanismo de invocación implementado. Los jobs no pueden ser disparados.

---

### BcsSocioNegocioJobs
**Legado encontrado**: Sí — `BSCL/SocioNegocio/JAVA/BcsSocioNegocioJobs/src/`
**Archivos legacy**: 13 clases
**Archivos modernizados**: 9 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 2 |
| ❌ STUB | 6 |

#### Hallazgos significativos

##### Jobs sin equivalente REST
**Estado**: ❌ STUB
**Riesgo**: MEDIO
**Divergencia**: El controller solo expone `POST /bloquear-por-vencimiento`, `POST /purga-prospectos`, `GET /fecha-sistema`. No tienen equivalente: `GenerarReporteSocioNegocioJob`, `GenerarDocumentosPeriodicoJob`, `CargaCelebracionNacimientosJob`, `GenerarReporteConsultaAvanzadaClienteJob`, `DatosProximosPorVencerJob`, `DocumentosClientePorVencerJob`.

---

### BcsMesaJobs
**Legado encontrado**: Sí — `BSCL/Mesa/JAVA/BcsMesaJobs/src/`
**Archivos legacy**: 17 clases
**Archivos modernizados**: 9 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 12 (file-generating jobs vía SP) |
| ⚠️ DIVERGENTE | 1 (LiberaBloqueoEdicionPactos) |

#### Hallazgos significativos

##### LiberaBloqueoEdicionPactos — filtrado de tiempo eliminado
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencia CRÍTICA**: En el legacy, antes de liberar una operación bloqueada, se verifica:
1. Se obtiene `TIEMPOMAXEDICIONPACTO` de la configuración (si no existe → no libera nada)
2. Se calcula `minutosTranscurridos = (fechaHoraActual - FechaHoraInicioEdicion) / 60000`
3. Solo libera si `minutosTranscurridos > tiempoMaximoMinutos`

En el modernizado (`MesaJobController.liberaBloqueo()`), el código llama directamente a `dataPort.buscarOperacionesBloqueadas()` y `dataPort.liberarOperaciones()` **sin ninguna verificación de tiempo transcurrido ni de configuración**. Esto liberaría TODAS las operaciones bloqueadas sin importar cuánto tiempo llevan bloqueadas, rompiendo la invariante de negocio.

Además, en el legacy se registra un `AgregarLog` por cada operación liberada; en el modernizado no hay log de auditoría.

##### Scheduler: jobs de archivo (13 endpoints)
**Estado**: ✅ EQUIVALENTE (contrato)
**Nota**: Los 13 jobs generadores de archivo (`GenerarAnexoIX`, `GenerarAnexoXIX`, `GenerarArchivoOMA`, etc.) están mapeados como endpoints POST que delegan a SPs HANA pinned. Los nombres de SP están correctamente definidos como constantes.

---

### BcsOrdenJobs
**Legado encontrado**: Sí — `BSCL/Orden/JAVA/BcsOrdenJobs/src/`
**Archivos legacy**: 29 clases
**Archivos modernizados**: 9 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 27 |
| ⚠️ DIVERGENTE | 1 |

#### Hallazgos significativos

##### SqlTableBuilderService — bug pinned correctamente
**Estado**: ✅ EQUIVALENTE
**Nota**: El método `insertarPorClase()` replica correctamente el bug de la coma final del legacy (`B-ORD-04`): cuando el último campo tiene prefijo "_" y se omite, la coma se añade igualmente basándose en el índice, no en si el campo fue escrito.

##### ControlMercadoService — constantes correctas
**Estado**: ✅ EQUIVALENTE
**Nota**: Constantes `COMPRA="COM"`, `VENTA="VEN"`, `MOTIVO_ANULACION="ANULACION POR SISTEMA BURSATIL"` (espacio, no guion bajo) verificadas. `sonTransaccionesIguales()` usa `BigDecimal.compareTo()` en lugar de `.equals()` tal como el legacy.

##### 27 endpoints HTTP cubriendo todos los jobs legacy
**Estado**: ✅ EQUIVALENTE
**Nota**: Todos los jobs del legacy (`AsignacionAutomaticaJob`, `CargarArchivoOrdenesTransacciones`, `ControlMercadoFix`, `ControlMercadoFixMME`, `MarketDataRvInstrumentos`, etc.) tienen endpoint POST correspondiente.

---

### BcsTributarioJobs
**Legado encontrado**: Sí — `BSCL/Normativa/Java/BcsTributarioJobs/src/`
**Archivos legacy**: 1 clase (`BcsJobCargaIPC`)
**Archivos modernizados**: 7 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 1 |

#### Hallazgos significativos
`CargaIpcJobService` cubre completamente `BcsJobCargaIPC`. Estructura hexagonal completa con adapter y port.

---

### BcsNormativaJobs
**Legado encontrado**: Sí — `BSCL/Normativa/Java/BcsNormativaJobs/src/`
**Archivos legacy**: 229 clases
**Archivos modernizados**: 21 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 14 (endpoints) |
| ❌ STUB / SIN CUBRIR | ~40 jobs específicos |

#### Hallazgos significativos

##### Cobertura masivamente insuficiente
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencia**: Solo 14 endpoints implementados cubren el universo de 229 archivos legacy (que incluyen ~50+ jobs específicos). Jobs legacy no cubiertos incluyen: `BcsJobGenerarInterfazDetalleOperacionesPolizas`, `GenerarReporteSaldosCuentasContables`, `BcsJobGenerarRepoSicam`, `BcsJobGenerarCartasCIR1985`, `BcsJobGenerarInterfazC08Todos`, `BcsJobGenerarInterfazC40CBI`, `BcsJobGenerarInterfazLavadoDinero`, `BcsJobGenCir1064`, `BcsJobGenerarInterfazRCP`, `BcsJobGenerarInterfazOrdenesEnLinea`, `BcsJobCertificadosClienteMasivo`, `BcsJobGenerarInformeMensualInversionistasRV13419`, `CargaCalificacionesTributariasBD`, `CargaTBPRAJHI`, `BcsJobGenerarAnexo1Circular20`, `BcsJobGenerarP40FilialUnitario`, `BcsJobCargaPagoOperacionesInterfaz`, `ProcesoConsolidaCli`, `BcsJobGenerarInterfazInstrumentosFinancieros`, entre otros.

Los 14 endpoints expuestos (`/generacion`, `/validacion`, `/carga`, `/cuadratura`, `/exportacion`, `/exportacion-ncg380`, `/procesar-serv`, `/valorizar-ncg499`, `/valorizar-declaracion`, `/certificados-tributarios`, `/reporte-apv`, `/cargar-datos-apv`, `/procesar-costos`, `/health`) cubren los workflows regulatorios principales delegando a SPs HANA.

---

### BcsJobsFondoMutuo
**Legado encontrado**: Sí — `BSCL/FondoMutuo/JAVA/BcsJobsFondoMutuo/src/`
**Archivos legacy**: 18 clases
**Archivos modernizados**: 33 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 18 |

#### Hallazgos significativos
Cobertura completa. Los jobs `IngresarOrdenBonificacionFiscalReinversion`, `CargaMasivaSolicitudes`, `CertificacionMasivaSolicitudes`, `GenerarArchivosFFMM`, `TrxCFileManager`, `BloqueoCierreSeries`, `ConfirmarTraspaso`, `GenerarCertificado31FFMM`, `ProcesarComisionesNN`, `GenerarArchivoSolicitudAporteRescate` tienen servicios modernizados con cobertura de tests unitarios. El job `BcsJobAdministracionLiquidez` no se encontró en los archivos de servicio pero sí existe referencia en los Pauta stubs de PlataformaJobs.

---

### BcsJobOrders
**Legado encontrado**: Sí — `BSCL/Orden/JAVA/BcsJobOrders/src/`
**Archivos legacy**: 7 clases
**Archivos modernizados**: 10 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 7 |

#### Hallazgos significativos
`AsignacionCronologicaService` implementa el algoritmo O(n²) de asignación cronológica. Los modelos `Asignacion`, `Orden`, `ParametrosDefault`, `Transaccion` replican los POJOs legacy. `ConnectorsOrders` legacy → `AsignacionDataPort` (port hexagonal).

---

### BcsCartolasJobs
**Legado encontrado**: Sí — `BSCL/Normativa/Java/BcsCartolasJobs/src/`
**Archivos legacy**: 15 clases
**Archivos modernizados**: 19 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 15 |

#### Hallazgos significativos
Cobertura completa. `BcsJobCartolas` → `CartolasJobService`. `BcsScheduledJobCartolas` → `ProgramacionCartolasService`. Las herramientas utilitarias (`EmisorUnitarioApp`, `EmisorCartolasApp`, etc.) → puertos de adaptador HANA (`HanaCargaDatosAdapter`, `HanaEmisionAdapter`, `HanaParametroAdapter`, etc.).

---

### BcsPlataformaJobs
**Legado encontrado**: Sí — `BSCL/Plataforma/JAVA/BcsPlataformaJobs/src/`
**Archivos legacy**: 153 clases (incluyendo ~60 Pauta*, LaPauta, y ~30 jobs standalone)
**Archivos modernizados**: 19 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 4 (lógica pura) |
| ❌ STUB | ~60 (todos los Pauta*) |
| ⚠️ DIVERGENTE | 1 (ejecutarPauta) |
| ❌ STUB (sin cubrir) | ~30 jobs standalone |

#### Hallazgos significativos

##### CRÍTICO: Todos los procesos Pauta* son stubs
**Estado**: ❌ STUB
**Riesgo**: CRÍTICO
**Divergencia**: `PautaProcessStubs` contiene ~60 procesos pauta, todos implementados con `throw new UnsupportedOperationException("... requires a live HANA connection")`. Esto incluye procesos críticos del día: `PautaAbrirSistema`, `PautaEntregarSistema`, `PautaTerminarPauta`, `PautaIniciarPauta`, `PautaCambioFechaSistema`, `PautaValorizarCarteraRF`, `PautaValorizarCarteraRFIF`, `PautaValorizarFFMM`, `PautaVencimientosPactos`, `PautaEjecutarReplicaBatch`, `PautaFacturacionMensual`, `PautaFatca`, etc.

##### CRÍTICO: LaPautaService.ejecutarPauta() es stub
**Estado**: ❌ STUB
**Riesgo**: CRÍTICO
**Divergencia**: El método principal de ejecución de la pauta diaria `LaPautaService.ejecutarPauta()` lanza `UnsupportedOperationException`. Solo las 4 funciones puras están implementadas (`obtenerFechaHora`, `hayModuloAbierto`, `obtenerProcesoDependencia`, `truncarMensaje`).

##### BcsPlataformaJobs sin endpoints de invocación
**Estado**: ❌ STUB
**Riesgo**: CRÍTICO
**Divergencia**: El único endpoint expuesto es `GET /health`. No existen endpoints para disparar la pauta ni ningún job standalone. Los jobs standalone legacy sin equivalente incluyen: `LimpiarDataHistoricoJob`, `ActualizaInstrumentosNegociadosJob`, `ActualizaContratosFirmadosJob`, `CargarArchivoTasasNemoJob`, `ExportarPerfilesJob`, `ExportarRolesJob`, `CargarDiasFeriados`, `NotificarOwpJob`, `EstandarizarCorredoresBolsaJob`, `GenerarArchivoTasas`, `ActualizarParametrosFinancieros`, `FileManagerJob`, `MigrarDataHistoricoJob`, y otros.

---

### BcsBECB
**Legado encontrado**: Sí — `BSCL/Corredor/BECB/JAVA/`
**Archivos legacy**: 4 clases (2 Jobs + 2 Servicios core + 4 clases de apoyo)
**Archivos modernizados**: 12 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 2 |

#### Hallazgos significativos
`RiesgoFinancieroService` cubre `GenerarInformesRiesgoFinanciero`. La consolidación legacy-Jobs (`BcsJobGenerarInformeVencimientosGenerales` + `BcsJobGenerarInformesRiesgoFinanciero`) → `BecbController` con dos endpoints. El modelo `DataRiesgoFinanciero` replica las clases `InformeRiesgoFinanciero`/`DetalleRiesgoFinanciero`/`RiesgoFinancieroCompleto` del legacy.

---

### BcsValo2
**Legado encontrado**: Sí — `BSCL/Plataforma/JAVA/BcsValo2/src/`
**Archivos legacy**: 10 clases
**Archivos modernizados**: 23 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 10 |

#### Hallazgos significativos
`BonosService` ← `ValorizadorBonos`. `PagaresService` ← `ValorizadorPagares`. `ContadorDiasService` ← `Cuponera`. `ValorizadorConfig` → `ValorizadorConfig` (Spring Config). Las fórmulas financieras de valorización de bonos y pagarés están correctamente trasladadas.

---

### BcsSURA
**Legado encontrado**: Sí — `BSCL/Corredor/SURA/JAVA/Servicios/src/`
**Archivos legacy**: 5 clases
**Archivos modernizados**: 13 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 5 |

#### Hallazgos significativos
Los 3 flujos de modificación de perfiles de portafolio están cubiertos: `CrearSocioNegocioModificarPerfilesPortafolioService`, `ModificarSocioNegocioModificarPerfilesPortafolioService`, `ModificarClienteModificarPerfilesPortafolioWSService`. El utility `PerfilesPortafolioUtils` → `PerfilesPortafolioUtilsService`. El POJO `IdsEntidadYPerfiles` → domain `IdsEntidadYPerfiles`.

---

### BcsReplica
**Legado encontrado**: Sí — `BSCL/Replica/JAVA/BcsReplica/src/`
**Archivos legacy**: 4 clases (`IniciarReplica`, `IniciarReplicaFull`, `IniciarReplicaDelta`, `EnviarTablaReplica`, `ReplicaUtils`)
**Archivos modernizados**: 9 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 3 |
| ❓ SIN_LEGADO | 0 |

#### Hallazgos significativos
`ReplicaService` cubre los 3 modos de réplica (Full, Delta, EnviarTabla). Los dominios `EntradaIniciarReplica`/`SalidaIniciarReplica` capturan el contrato de entrada/salida. `Workstack` → domain para rastreo de estado.

---

### BcsCartolas
**Legado encontrado**: En `BSCL/Normativa/Java/BcsCartolasJobs/` (servicios de cartolas)
**Archivos legacy**: ~20 clases (incluidas en `BcsCartolasJobs`)
**Archivos modernizados**: 37 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 15+ |

#### Hallazgos significativos
`CartolasService` y `NominaService` cubren la generación de cartolas. Los adaptadores HANA (5 adapters) reemplazan las queries directas del legacy. El modelo de dominio es más completo que el legacy con separación de responsabilidades.

---

### BcsCartolasMaster
**Legado encontrado**: No — componente derivado de BcsCartolas
**Archivos legacy**: N/A (sin counterpart directo separado)
**Archivos modernizados**: 16 main Java

#### Resumen
| Estado | N |
|--------|---|
| 🆕 NUEVO | 1 |

#### Hallazgos significativos
`CartolaMasterService` con `GraficadorSerieService` y `GraficadorPastelService` es un microservicio nuevo que genera la cartola consolidada/master con gráficos. No tiene correspondencia directa en el legacy como módulo separado.

---

### BcsNormativaCore
**Legado encontrado**: Sí — `BSCL/Normativa/Java/BcsNormativaCore/src/`
**Archivos legacy**: ~40 clases (DataParser, esquemas, etc.)
**Archivos modernizados**: 59 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 30+ |

#### Hallazgos significativos
`DataParser` → `DataParserService`. Modelos de dominio completos (`Esquema`, `Clasificador`, `Cuadratura`, `Validacion`, `Documento`, etc.). Cobertura estructural superior al legacy con 59 vs ~40 clases.

---

### BcsPlataformaWeb
**Legado encontrado**: Sí — `BSCL/Plataforma/JAVA/BcsPlataformaWeb/src/`
**Archivos legacy**: ~15 servlets/servicios
**Archivos modernizados**: 35 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 15 |

#### Hallazgos significativos
`JavaCallRouterService` reemplaza el patrón de routing TomEE JavaCall. `QueryDispatcherService` cubre las queries directas a HANA. `TransaccionWSService` cubre los servicios web de transacción. Modernización de SSO/session management completa.

---

### BcsPortalConfirmacionWeb
**Legado encontrado**: Sí — `BSCL/Plataforma/JAVA/BcsPortalConfirmacionWeb/src/`
**Archivos legacy**: 3 clases (`NotificationsValidate`, `KeyValue`, `EvalTemplate`)
**Archivos modernizados**: 9 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 3 |

#### Hallazgos significativos
`EvalTemplateService` cubre `EvalTemplate`. `NotificationService` cubre `NotificationsValidate`. `KeyValue` → domain `KeyValue`.

---

### BcsGateway
**Legado encontrado**: No — reemplaza el routing TomEE nativo
**Archivos modernizados**: 2 main Java (Application + FallbackController)

#### Resumen
| Estado | N |
|--------|---|
| 🆕 NUEVO | 1 |

#### Hallazgos significativos
Spring Cloud Gateway reemplaza el routing TomEE. `FallbackController` provee respuesta de fallback 503. La configuración de rutas está en `application.yml` (no en código Java). Microservicio nuevo sin contraparte en el legacy (era un concern transversal de TomEE).

---

### BcsFilters
**Legado encontrado**: Sí — `BSCL/Plataforma/JAVA/BcsFilters/src/`
**Archivos legacy**: 2 filtros (`SesionFilter`, `ServiceFilter`)
**Archivos modernizados**: 21 main Java (incluyendo SecurityConfig)

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 2 |
| 🆕 NUEVO | 1 (SecurityConfig con Spring Security) |

#### Hallazgos significativos
`SessionValidationFilter` ← `SesionFilter`. `ServiceAccessFilter` ← `ServiceFilter`. La integración de Spring Security (`SecurityConfig`, `PasswordEncoderConfig`) es nuevo y reemplaza la validación manual de sesión TomEE.

---

### BcsPlataformaReporteApi
**Legado encontrado**: No encontrado directo
**Archivos modernizados**: 3 main Java

#### Resumen
| Estado | N |
|--------|---|
| ❓ SIN_LEGADO | 1 |

#### Hallazgos significativos
`ReporteApiShimService` con `ReporteApiController` actúa como shim/adapter para el módulo de reportes. No se encontró un módulo legacy correspondiente separado (`BcsPlataformaReporteApi`). Posiblemente es un nuevo microservicio de integración para la API de reportes de la plataforma.

---

### BcsCrystalReports
**Legado encontrado**: Parcial — `DatosComprobanteCrystal.java` en `BcsFacturacionServicios`
**Archivos legacy**: ~2 referencias (no módulo separado)
**Archivos modernizados**: 10 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE (reemplazado) | 1 |
| 🆕 NUEVO | 1 |

#### Hallazgos significativos
Crystal Reports (SAP) reemplazado por motor propio: `ReportDocument` con `JsonResultSet`/`ListMapResultSet`/`SubreportDocument`. `ReportController` expone `POST /reports/generate`. La dependencia de Crystal Reports SDK (legacy) es eliminada. Cambio arquitectural válido pero se debe verificar que todos los reportes Crystal del legacy puedan ser generados por el nuevo motor.

---

### BcsJobManager
**Legado encontrado**: Sí — `BSCL/Plataforma/JAVA/BcsJobManager/src/`
**Archivos legacy**: 11 clases (BcsJobManager, BcsJobManagerException, BcsJob, etc.)
**Archivos modernizados**: 17 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 11 |

#### Hallazgos significativos
`JobManagerService` ← `BcsJobManager`. `JobDispatcherService` implementa `@Scheduled(fixedDelayString = "${jobmanager.poll-interval-ms:1000}")`, replicando el polling del scheduler legacy. `DiaryService` cubre la agenda de jobs. `BcsJobManagerException` → `BcsJobManagerException` (mismo nombre). La transición de JavaEE scheduler → Spring `@Scheduled` es equivalente en comportamiento.

---

### BcsTesoreriaJobs
**Legado encontrado**: Sí — `BSCL/Tesoreria/JAVA/BcsTesoreriaJobs/src/`
**Archivos legacy**: 12 clases
**Archivos modernizados**: 21 main Java

#### Resumen
| Estado | N |
|--------|---|
| ✅ EQUIVALENTE | 12 |

#### Hallazgos significativos
`ProcesarDistribucionJob` ← `BcsJobProcesarDistribucion`. `CargaMasivaJob` ← `BcsJobCargaMasivaIngresoEgresoDAPFFMM`. La lógica de generación de archivos BancoEstado (`GenerarArchivoPagoBancoEstado` legacy) está cubierta con tests específicos (`BancoEstadoVariantsTest`, `GenerarArchivoPagoTest`, `ProcesarDistribucionCsvTest`). `ArchivoFieldFormatterTest` cubre el formateo de campos.

---

## Resumen Global

| Microservicio | Clases Legacy | Main Java Modern | Equiv | Diverg | Stub | SinLeg | Nuevo |
|--------------|---------------|-----------------|-------|--------|------|--------|-------|
| BcsAPVJobs | 4 | 30 | 4 | 0 (sched pendiente) | 0 | 0 | 0 |
| BcsCuentaInversionJobs | 23 | 40 | 19 | 0 | 0 | 0 | 0 |
| BcsCustodiaJobs | 37 | 9 | 3 | 2 | 27 | 0 | 0 |
| BcsFacturacionJobs | 14 | 9 | 3 | 1 | 5 | 0 | 0 |
| BcsContabilidadJobs | 8 | 15 | 3 | 1 | 1 | 0 | 0 |
| BcsSocioNegocioJobs | 13 | 9 | 2 | 0 | 6 | 0 | 0 |
| BcsMesaJobs | 17 | 9 | 12 | 1 | 0 | 0 | 0 |
| BcsOrdenJobs | 29 | 9 | 27 | 1 | 0 | 0 | 0 |
| BcsTributarioJobs | 1 | 7 | 1 | 0 | 0 | 0 | 0 |
| BcsNormativaJobs | 229 | 21 | 14 (endpoints) | 1 | ~40 | 0 | 0 |
| BcsJobsFondoMutuo | 18 | 33 | 18 | 0 | 0 | 0 | 0 |
| BcsJobOrders | 7 | 10 | 7 | 0 | 0 | 0 | 0 |
| BcsCartolasJobs | 15 | 19 | 15 | 0 | 0 | 0 | 0 |
| BcsPlataformaJobs | 153 | 19 | 4 | 1 | ~90 | 0 | 0 |
| BcsBECB | 10 | 12 | 2 | 0 | 0 | 0 | 0 |
| BcsValo2 | 10 | 23 | 10 | 0 | 0 | 0 | 0 |
| BcsSURA | 5 | 13 | 5 | 0 | 0 | 0 | 0 |
| BcsReplica | 5 | 9 | 3 | 0 | 0 | 0 | 0 |
| BcsCartolas | ~20 | 37 | 15+ | 0 | 0 | 0 | 0 |
| BcsCartolasMaster | 0 | 16 | 0 | 0 | 0 | 0 | 1 |
| BcsNormativaCore | ~40 | 59 | 30+ | 0 | 0 | 0 | 0 |
| BcsPlataformaWeb | ~15 | 35 | 15 | 0 | 0 | 0 | 0 |
| BcsPortalConfirmacionWeb | 3 | 9 | 3 | 0 | 0 | 0 | 0 |
| BcsGateway | 0 | 2 | 0 | 0 | 0 | 0 | 1 |
| BcsFilters | 2 | 21 | 2 | 0 | 0 | 0 | 1 |
| BcsPlataformaReporteApi | 0 | 3 | 0 | 0 | 0 | 1 | 0 |
| BcsCrystalReports | ~2 | 10 | 1 | 0 | 0 | 0 | 1 |
| BcsJobManager | 11 | 17 | 11 | 0 | 0 | 0 | 0 |
| BcsTesoreriaJobs | 12 | 21 | 12 | 0 | 0 | 0 | 0 |
| **TOTALES** | **~713** | **~540** | **~242** | **7** | **~169** | **1** | **4** |

---

## Hallazgos Críticos Transversales

### 1. BcsPlataformaJobs — BLOQUEANTE para go-live
**Prioridad**: CRÍTICA
La pauta diaria (LaPauta) y sus ~60 procesos Pauta* son el corazón operacional del sistema. Todo está stubbed con `UnsupportedOperationException`. Sin esto, la operación diaria (valorización de carteras, vencimientos, réplicas, facturación mensual, FATCA, etc.) no puede ejecutarse. Este microservicio requiere implementación completa antes de go-live.

### 2. BcsMesaJobs — LiberaBloqueoEdicionPactos
**Prioridad**: ALTA
El filtro de tiempo (`minutosTranscurridos > tiempoMaximoMinutos`) fue eliminado del modernizado. Sin esto, la API liberaría operaciones bloqueadas instantáneamente sin respetar el tiempo máximo de edición configurado. Riesgo de datos incorrectos en producción.

### 3. BcsCustodiaJobs — Cobertura del 16%
**Prioridad**: ALTA
Solo 6 endpoints vs 37 jobs legacy. Los jobs BlockChain, generación de archivos de custodia, derivados y neteo de carteras negativas no están implementados.

### 4. BcsContabilidadJobs — Sin trigger de invocación
**Prioridad**: ALTA
Los servicios están implementados correctamente pero no hay mecanismo de invocación (ni REST endpoint, ni `@Scheduled`, ni `CommandLineRunner`). Los jobs no pueden ser ejecutados.

### 5. Scheduler/Trigger — APVJobs deferred
**Prioridad**: MEDIA
BcsAPVJobs tiene `@EnableScheduling` pero sin `@Scheduled`. Los jobs existen pero no se dispararán automáticamente. Requiere configuración de trigger en Fase 3.

### 6. BcsNormativaJobs — Cobertura parcial (~6%)
**Prioridad**: MEDIA-ALTA
229 clases legacy → 14 endpoints. Muchos reportes regulatorios (CMF, SBif, CIR1985, etc.) no tienen equivalente.

---

## Leyenda de Estados
- `✅ EQUIVALENTE` — comportamiento idéntico al legacy
- `⚠️ DIVERGENTE` — diferencia de comportamiento real
- `❌ STUB` — no implementado (UnsupportedOperationException, placeholder, o sin trigger)
- `⚠️ SQL_MISMATCH` — SQL difiere semánticamente
- `❓ SIN_LEGADO` — no hay clase legado correspondiente
- `🆕 NUEVO` — microservicio nuevo sin contraparte en legado
