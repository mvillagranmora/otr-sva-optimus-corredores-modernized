# Jobs H-1 / H4: inventario de colaboradores y contrato del Job Manager

Fecha: 2026-10-21. Solo lectura sobre `legacy/` y `modernized/`. Alcance: BcsTesoreriaJobs, BcsAPVJobs, BcsCuentaInversionJobs, BcsContabilidadJobs y los mapas `ColaboradoresJavaCall` de sus Servicios.
Restricciones del usuario (2026-10-21) aplicadas en todo el documento: (1) NINGUN cambio de base de datos (ni tablas, columnas, vistas, procedimientos ni datos de configuracion) y el legacy no se modifica; (2) el motor moderno despacha los programas nuevos y el Job Manager legacy nunca toca codigo modernizado; (3) "requiere revision" = `idEstado='ERR'`, `Intento=MaximoIntentos`, `Bloqueo='S'`; (4) el router no lleva cabecera de idempotencia, la idempotencia es solo del lado del job.

## A. Inventario de colaboradores

### A.0 Hallazgo que reencuadra H-1 (verificado)

- Ningun colaborador confirma por si mismo en el legacy. Se escaneo el codigo de 46 clases colaboradoras: los unicos `con.commit()` estan en los `main()` de prueba (p. ej. `AgregarLog.java:519`, `CrearCargoAbono.java:1082`, `CrearEventoAutorizacionMasivo.java:324`, `CrearComprobante.java:550` de Tesoreria, `ValidaEjecucionProcesoRecPAC.java:137`). `logica(con, ...)` y `call(Connection con, JsonObject)` reciben la conexion del llamador y no la cierran ni la confirman (p. ej. `InscribirProcesoBatch.java:206-221`).
- Por tanto el riesgo de H-1 no es "el colaborador confirma", sino el salto HTTP: el router (`JavaCallRouterService`, BcsPlataformaWeb) llama al servicio destino o a TomEE, y el anfitrion del JavaCall abre su PROPIA conexion y confirma al terminar cada llamada. Un rollback del job ya no deshace lo que hizo el colaborador.
- Todas las llamadas de los jobs legacy pasaban la conexion del job: `procesoOrdenes.logica(con,` (`IngresarOrdenRVBonificacionFiscal.java:598`), `llamadoFlujoCaja.logica(con,` (:371), `ConsultaCustodia.logica(con,` (:331), `procesoCalcularMonto.logica(con,` (:577), `agregarLogService.logica(con,` (:478), `AgregarLogPrograma.logica(con,` (:259/:432/:601), `crearSolicitudAporteRescateNormal.logica(con,` (`IngresarOrdenFMBonificacionFiscal.java:291`), `consultarEmpresaService.logica(con,` (:155), `flujo.logica(con,` (:208), `procesarDistribucion.logica(conn,` / `noProcesados.logica(conn,` (`BcsJobProcesarDistribucion.java:75,82`), `obtenerDiaAnterior.logica(con` (`BcsJobCargaMasivaIngresoEgresoDAPFFMM.java:153`), `objConsulta.logica(con,` (:277), `agregarLogService.logica(con,` (:329), `AgregarLogPrograma` -> `AgregarLogPrograma.java:15-26` (INSERT en BAT.LogPrograma con la conexion recibida), `PautaGenerarReporteAcreedores.java:47` (`agregarProgramaReporte.logica(con,` = InscribirProcesoBatch).
- Dos bordes que NO son colaboradores Java pero tienen efecto fuera de la transaccion: SAP B1 Service Layer (`SapB1Gateway`) y SFTP (`JschInterfazFileTransfer`) en Contabilidad; el `Cloper` (servicio web externo) de Tesoreria, sin cliente migrado (503).
- "Join en proceso" ya es posible para los modulos con Servicio PROVEN porque los servicios usan `@Transactional(rollbackFor=Exception.class)` con propagacion por defecto (REQUIRED) y el `JobRunner` abre su transaccion con `REQUIRES_NEW` sobre el mismo `PlatformTransactionManager`: un servicio invocado en proceso se une a la transaccion del job (`TransaccionesLegacyConfig.java` de Tesoreria; no hay un test que lo pruebe de punta a punta con rollback del job, ver D).

### A.1 Leyenda

- Propietario migrado: la clase legacy vive en un modulo con Servicio PROVEN (BcsUtils, BcsContabilidadServicios, BcsTesoreriaServicios, BcsAPVServicios, BcsCuentaInversionServicios). Los demas (Plataforma, Orden, FondoMutuo, Custodia, SocioNegocio, Portafolio, Facturacion, Normativa) siguen en TomEE aunque exista carpeta en `modernized/`.
- Con compartida (legacy): "si" = verificado en un job legacy con `.logica(con,` ; "si (patron)" = mismo patron `call(con, json)` en Servicios, no verificado llamada por llamada.
- Confirma solo (legacy): siempre "no" (ver A.0). Se omite como columna.
- E/L: L = solo lee; E = escribe (INSERT/UPDATE/DELETE/CALL de negocio); L* = lee pero usa tablas de paso/GBL (`GBL.*` son tablas temporales globales, `PLA_Create_Global_Tables.sql`) sin escritura de negocio.
- Grupo: M = ya migrado como libreria (puede unirse a la transaccion en proceso); TW = TomEE con escritura (reclamo + idempotencia + fallo cerrado); TL = TomEE solo lectura.
- Evidencia de E/L: SQL escaneado en la clase y en sus `.sql` de recursos (`legacy/.../JAVA/<Modulo>Servicios/src/...`).

### A.2 Colaboradores unicos (46 clases + 2 especiales), con su grupo

| # | Clase legacy (clave del router) | Modulo propietario (archivo legacy) | Propietario migrado | E/L | Grupo |
|---|---|---|---|---|---|
| 1 | cl.bcs.APV.ValidarMandatosPAC | APV (BcsAPVServicios/.../APV/ValidarMandatosPAC.java; INSERT LogMandatoPAC) | si | E | M |
| 2 | cl.bcs.APV.CambiarEstadoContratosSAAPV | APV (.../APV/CambiarEstadoContratosSAAPV.java; 4 UPDATE) | si | E | M |
| 3 | cl.bcs.tesoreria.CrearIngresoEgreso | Tesoreria | si | E | M |
| 4 | cl.bcs.tesoreria.LiquidarComprobante | Tesoreria | si | E | M |
| 5 | cl.bcs.tesoreria.CrearComprobante | Tesoreria (no confundir con cl.bcs.facturacion.CrearComprobante) | si | E | M |
| 6 | cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco | Tesoreria | si | E | M |
| 7 | cl.bcs.tesoreria.nor.AnularNominasTransferencias | Tesoreria | si | E | M |
| 8 | cl.bcs.tesoreria.AnulacionMandatoPAC | Tesoreria | si | E | M |
| 9 | cl.bcs.tesoreria.RevocacionMandatoPAC | Tesoreria | si | E | M |
| 10 | cl.bcs.tesoreria.IngresarMandatoPAC | Tesoreria | si | E | M |
| 11 | cl.bcs.tesoreria.liq.ConsultarClearingFinanciero | Tesoreria | si | L | M |
| 12 | cl.bcs.cuentaInversion.CrearCargoAbono | CuentaInversion | si | E | M |
| 13 | cl.bcs.cuentaInversion.AnularCargoAbono | CuentaInversion | si | E | M |
| 14 | cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion | CuentaInversion | si | L* | M |
| 15 | cl.bcs.plataforma.pla.AgregarLog | Plataforma (INSERT log y detalle) | no | E | TW |
| 16 | cl.bcs.plataforma.bat.AgregarPrograma | Plataforma (INSERT BAT.Programa) | no | E | TW |
| 17 | cl.bcs.plataforma.bat.InscribirProcesoBatch | Plataforma (INSERT Programa y ParametrosPrograma) | no | E | TW |
| 18 | cl.bcs.plataforma.evt.AgregarEvento | Plataforma (14 SQL, INSERT eventos) | no | E | TW |
| 19 | cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo | Plataforma | no | E | TW |
| 20 | cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo | Plataforma (+ notificaciones) | no | E | TW |
| 21 | cl.bcs.plataforma.pla.ValidaEjecucionProcesoRecPAC | Plataforma (`..._UpdateFlag.sql`: UPDATE) | no | E | TW |
| 22 | cl.bcs.ordenes.CrearOrdenes | Orden (30 SQL, 5780 lineas; INSERT/DELETE y CALL RegistrarIngresoOrden) | no | E | TW |
| 23 | cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal | FondoMutuo (INSERT solicitud, CALL CrearBloqueoSolicitudAporteRescate) | no | E | TW |
| 24 | cl.bcs.fondomutuo.AnulacionContratoCSC | FondoMutuo (UPDATE ContratoCSC) | no | E | TW |
| 25 | cl.bcs.fondomutuo.RevocacionContratoCSC | FondoMutuo (UPDATE) | no | E | TW |
| 26 | cl.bcs.portafolio.ModificarPortafolio | Portafolio (INSERT LogGenerarContratos) | no | E | TW |
| 27 | cl.bcs.socionegocio.cli.CrearCliente | SocioNegocio (INSERT/UPDATE, CALL AgregarPrograma) | no | E | TW |
| 28 | cl.bcs.socionegocio.per.CrearPersona | SocioNegocio | no | E | TW |
| 29 | cl.bcs.socionegocio.per.CrearTelefono | SocioNegocio | no | E | TW |
| 30 | cl.bcs.socionegocio.per.CrearDireccionElectronica | SocioNegocio | no | E | TW |
| 31 | cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria | SocioNegocio | no | E | TW |
| 32 | cl.bcs.facturacion.CrearMovimientoComprobante | Facturacion (18 SQL, INSERT movimientos) | no | E | TW |
| 33 | cl.bcs.plataforma.pla.ConsultarEstadoModulo | Plataforma | no | L | TL |
| 34 | cl.bcs.plataforma.pla.ObtenerDiaAnterior | Plataforma | no | L | TL |
| 35 | cl.bcs.plataforma.pla.utils.ConsultarEmpresa | Plataforma | no | L | TL |
| 36 | cl.bcs.plataforma.pla.ConsultarFlujoCaja | Plataforma (INSERT/DELETE solo en GBL) | no | L* | TL |
| 37 | cl.bcs.plataforma.pla.SaldoFlujoCaja | Plataforma (GBL.SaldoFlujoCaja_Proceso_Paso) | no | L* | TL |
| 38 | cl.bcs.plataforma.mer.CalcularParidad | Plataforma | no | L | TL |
| 39 | cl.bcs.plataforma.pla.utils.ConsultarImagen | Plataforma | no | L | TL |
| 40 | cl.bcs.plataforma.tas.EntregarPreciosTasas | Plataforma (tablas TAS.*_IN_* de paso; delete+insert por llamada) | no | L* | TL |
| 41 | cl.bcs.plataforma.pla.RevisarRequisitosBasicos | Plataforma | no | L | TL |
| 42 | cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa | Plataforma (tablas de paso) | no | L* | TL |
| 43 | cl.bcs.ordenes.CalcularMontoYCobrosOrden | Orden | no | L | TL |
| 44 | cl.bcs.custodia.ConsultarCustodiaPortafolio | Custodia (74 SQL; delete+insert en tablas de consulta) | no | L* | TL |
| 45 | cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo / CalcularMontoNumeroCuota / ConsultarCustodiaPortafolioFondoMutuo (3 clases) | FondoMutuo | no | L | TL |
| 46 | cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo (+ `.restringeCartera`) | SocioNegocio (escribe `EJE.ObtenerPortafolioEjecutivo_Proceso` por IdTrabajo y lo borra: tabla de paso, no verificado si es temporal) | no | L* | TL |
| 47 | cl.bcs.normativa.cartola.MovimientosTitulos / MovimientosCaja / DatosCartolaSocioNegocio (3) | Normativa (no son JavaCall; el router los responde "no migrado") | no | L | TL |
| E1 | StoredProcedure | procedimiento HANA ejecutado en proceso por `ProcedimientoAlmacenado` (JDBC, misma conexion/transaccion) | n/a | segun SP (APV: `INS_ConsultarInstrumentoPrecioBolsa`, solo staging) | fuera de grupos |
| E2 | Cloper | servicio web externo, sin cliente migrado (`UnsupportedOperationException` -> 503) | no | desconocido | fuera de grupos |

Conteo por grupo (clases unicas): M = 14 (11 E, 3 L), TW = 18, TL = 19 clases en 14 filas (incluye 3 de FondoMutuo y 3 cartolas; 5 son L* por tablas de paso), mas E1/E2.

### A.3 Llamadas por modulo llamador (clase llamadora -> colaborador)

Columnas: `Con compartida (legacy)` y `Grupo` segun A.1/A.2; "HTTP hoy" = como viaja ahora en el modernizado.

**BcsTesoreriaJobs** (`adapter/ColaboradoresJobs.java:38-41`, `job/port/ServiciosExternos.java`; el resto de nombres delega en `ColaboradoresJavaCall` de BcsTesoreriaServicios)

| Llamador | Colaborador | Con compartida (legacy) | HTTP hoy | Grupo |
|---|---|---|---|---|
| BcsJobProcesarDistribucion.java:82 | ValidarMandatosPAC (#1) | si (`BcsJobProcesarDistribucion.java:82`) | router, `ColaboradoresJobs` | M |
| BcsJobProcesarDistribucion.java:102 | ValidaEjecucionProcesoRecPAC (#21) | si (legacy :135) | router | TW |
| ContratosPlataforma.java:39 (job DAPFFMM) | ConsultarEstadoModulo (#33) | si (legacy :277) | router | TL |
| ContratosPlataforma.java:73 (job DAPFFMM) | ObtenerDiaAnterior (#34) | si (legacy :153) | router | TL |
| CargaMasivaSalida.java:106 (job DAPFFMM) | AgregarLog (#15) | si (legacy :329) | router via `ColaboradoresJavaCall` | TW |
| GenerarArchivoCargos / GenerarArchivoPago / GenerarArchivoPagoBancoEstado | ninguno por router (ConsultarFormatoArchivo, ActivarMandatosArchivoCarga y ProcedimientoArchivo van en proceso o por JDBC) | si | n/a | n/a |

Servicios de Tesoreria que los jobs ejecutan en proceso y cuyas llamadas salen por `ColaboradoresJavaCall` de BcsTesoreriaServicios (mapa `CLASES`, 24 nombres + StoredProcedure + Cloper): en proceso hoy (`JavaCallDispatcher.maneja`): CrearIngresoEgreso, LiquidarComprobante, CrearComprobante, ActualizarSaldosCuentaBanco, AnularNominasTransferencias (5). Por router: RevisarRequisitosBasicos (TL), AgregarLog (TW), ConsultarFlujoCaja (TL), SaldoFlujoCaja (TL), AgregarPrograma (TW), CalcularParidad (TL), ConsultarInstrumentoPrecioBolsa (TL), CrearEventoAutorizacionMasivo (TW), ModificarEstadoEventoAutorizacionMasivo (TW), CrearCargoAbono (M), AnularCargoAbono (M, invocado en `AnularNominasTransferenciasService.java:147`), AnulacionContratoCSC (TW), RevocacionContratoCSC (TW), CalcularMontoNumeroCuota (TL), CrearSolicitudAporteRescateNormal (TW), CambiarEstadoContratosSAAPV (M), CalcularMontoYCobrosOrden (TL), CrearOrdenes (TW), ObtenerPortafoliosEjecutivo y `.restringeCartera` (TL). Alcanzables desde los jobs: `MovimientosDistribucion.java:237,285,416,519,574,703,722,773` (CrearIngresoEgreso, LiquidarComprobante, CrearCargoAbono, CalcularMontoNumeroCuota, CrearSolicitudAporteRescateNormal, CalcularMontoYCobrosOrden, CrearOrdenes, ConsultarInstrumentoPrecioBolsa) a traves del job ProcesarDistribucion; `ProcesarCargaMasivaService.java:261,335` y `CargaMasivaIngresoEgresoDAPFFMMService.java:402` a traves del job DAPFFMM; los demas son de endpoints de Servicios, no de jobs (profundidad de cadena no verificada).

**BcsAPVJobs** (`adapter/ColaboradoresJobs.java:38-39` rutea por router ConsultarFlujoCaja y ConsultarFlujoCustodiaFondoMutuo; el resto delega en `ColaboradoresJavaCall` de BcsAPVServicios)

| Llamador | Colaborador | Con compartida (legacy) | HTTP hoy | Grupo |
|---|---|---|---|---|
| IngresarOrdenBonificacionFiscal.java:60 | CrearOrdenes (#22) | si (legacy :129) | router | TW |
| IngresarOrdenFMBonificacionFiscal.java:114 | CrearSolicitudAporteRescateNormal (#23) | si (legacy :291) | router | TW |
| IngresarOrdenFMBonificacionFiscal.java:139 | ConsultarEmpresa (#35) | si (legacy :155) | router | TL |
| IngresarOrdenFMBonificacionFiscal.java:171 | ConsultarFlujoCustodiaFondoMutuo (#45) | si (legacy :208) | router | TL |
| IngresarOrdenRVBonificacionFiscal.java:176 | AgregarLog (#15) | si (legacy :478) | router | TW |
| IngresarOrdenRVBonificacionFiscal.java:210 | StoredProcedure (E1) | si (legacy :294 `sql.call(con,`) | en proceso, JDBC | n/a |
| IngresarOrdenRVBonificacionFiscal.java:238 | ConsultarCustodiaPortafolio (#44) | si (legacy :331) | router | TL |
| IngresarOrdenRVBonificacionFiscal.java:260 | ConsultarFlujoCaja (#36) | si (legacy :371) | router | TL |
| IngresarOrdenRVBonificacionFiscal.java:274 | CalcularMontoYCobrosOrden (#43) | si (legacy :577) | router | TL |
| IngresarOrdenRVBonificacionFiscal.java:290 | CrearOrdenes (#22) | si (legacy :598) | router | TW |
| BcsJobCargaMasivaPrevired | ninguno por router (el servicio de carga masiva va en proceso) | si | n/a | n/a |

El log del programa (`AgregarLogPrograma`, legacy `AgregarLogPrograma.java:15-26`) ya se escribe en proceso por JDBC en la transaccion del job (`LogPrograma.java`), no por HTTP.
Servicios de APV (`ColaboradoresJavaCall` de BcsAPVServicios, 26 nombres + StoredProcedure, TODOS por router, `CLASES` en `.../adapter/ColaboradoresJavaCall.java`): Plataforma AgregarLog (TW), RevisarRequisitosBasicos (TL), ConsultarEmpresa (TL), CrearEventoAutorizacionMasivo (TW), ConsultarInstrumentoPrecioBolsa (TL), EntregarPreciosTasas (TL), CalcularParidad (TL); Tesoreria CrearIngresoEgreso, LiquidarComprobante, CrearComprobante, AnulacionMandatoPAC, RevocacionMandatoPAC, IngresarMandatoPAC (todos M, E, hoy por HTTP); CuentaInversion CrearCargoAbono (M, E), ConsultarSaldoCuentaInversion (M, L*); FondoMutuo CrearSolicitudAporteRescateNormal (TW), CalcularMontoNumeroCuota (TL), ConsultarCustodiaPortafolioFondoMutuo (TL); Custodia ConsultarCustodiaPortafolio (TL); Orden CalcularMontoYCobrosOrden (TL), CrearOrdenes (TW); Portafolio ModificarPortafolio (TW); SocioNegocio CrearCliente, CrearPersona, CrearTelefono, CrearDireccionElectronica, CrearPersonaCuentaBancaria (TW).

**BcsCuentaInversionJobs** (`adapter/ColaboradoresJobs.java`: clase migrada en proceso por `JavaCallDispatcher`, cualquier otra por router)

| Llamador | Colaborador | Con compartida (legacy) | HTTP hoy | Grupo |
|---|---|---|---|---|
| `job/pauta/InscribirProcesoBatch.java:109` (las 5 Pauta*: PautaActualizarSaldoCargoAbono, PautaGenerarReporteAcreedores, PautaGenerarReporteAntiguedadDeudaBolsaCTA, PautaGuardarSaldosReporteAntiguedad, y su inscripcion) | InscribirProcesoBatch (#17) | si (legacy `PautaGenerarReporteAcreedores.java:47`) | router (no en proceso, comentario de la clase) | TW |
| Servicios en proceso (`ConsultarSaldoCuentaInversion`, `CrearCargoAbono`, etc.) | por `CLASE`: AgregarEvento, AgregarLog, AgregarPrograma, CalcularParidad, ConsultarEmpresa, ConsultarImagen, CrearEventoAutorizacionMasivo, EntregarPreciosTasas, ObtenerPortafoliosEjecutivo, RevisarRequisitosBasicos, SaldoFlujoCaja (todos de Plataforma/SocioNegocio, TomEE) | si (patron) | router | TW: AgregarEvento, AgregarLog, AgregarPrograma, CrearEventoAutorizacionMasivo; TL: el resto |
| `ColaboradoresReportesAdapter.java:30-37` (reportes de jobs GenerarReporte*/Informe*) | ConsultarEmpresa, CalcularParidad, EntregarPreciosTasas (TL); ConsultarSaldoCuentaInversion (M, en proceso); CrearMovimientoComprobante (Facturacion, TW); MovimientosTitulos / MovimientosCaja / DatosCartolaSocioNegocio (Normativa, TL, "no migrado" hoy) | si (patron) | router | segun fila |

**BcsContabilidadJobs / BcsContabilidadServicios** (no usan router JavaCall; tres puertos con `PlataformaHttp`, `infrastructure/plataforma/*`)

| Llamador | Colaborador | Con compartida (legacy) | HTTP hoy | Grupo |
|---|---|---|---|---|
| `TomEePlataformaClient.java:59` (usado por CentralizaAnticipoPactos, `soporte/Contexto.java:34`, jobs de centralizacion) | ObtenerDiaAnterior (#34) | si (patron) | POST `/api/plataforma/ObtenerDiaAnterior` a TomEE | TL |
| `TomEePlataformaClient.java:96` (`CentralizaClearingFinanciero.java:40`) | ConsultarClearingFinanciero (#11, Tesoreria) | si (patron) | POST `/api/tesoreria/ConsultarClearingFinanciero` a TomEE (aunque Tesoreria ya esta migrada) | M |
| `TomEeProcesoBatchClient.java:59` (`EnviarPreComprobanteService`, es un Servicio, no un job) | InscribirProcesoBatch (#17) | si (patron) | POST `/api/plataforma/InscribirProcesoBatch` | TW |
| ConsultarEmpresa, parametros de configuracion (`PlataformaConfiguracionJdbc`) | JDBC en la conexion y transaccion del llamador, sin HTTP | si | en proceso | n/a |

Los jobs de Contabilidad (CentralizaMultiSeleccionJob, CreaInterfazDiaria, CreaInterfazDemanda, InterfazContable, GenerarInformeClienteRelacionadoBatch) no invocan colaboradores TW. Sus efectos fuera de la transaccion son SAP B1 y SFTP.

### A.4 Escritores TomEE o ya migrados que cada job alcanza (lista de TW y M-E por job)

| Job | Colaboradores que escriben (HTTP hoy) |
|---|---|
| Tesoreria BcsJobProcesarDistribucion | ValidarMandatosPAC (M), ValidaEjecucionProcesoRecPAC (TW); indirectos por MovimientosDistribucion: CrearCargoAbono (M, router), CrearSolicitudAporteRescateNormal (TW), CrearOrdenes (TW), AgregarLog (TW); CrearIngresoEgreso y LiquidarComprobante ya en proceso |
| Tesoreria BcsJobCargaMasivaIngresoEgresoDAPFFMM | AgregarLog (TW); CrearIngresoEgreso en proceso (indirecto) |
| Tesoreria GenerarArchivoCargos / GenerarArchivoPago / PagoBancoEstado | ninguno |
| APV IngresarOrdenBonificacionFiscal | CrearOrdenes (TW) |
| APV IngresarOrdenFMBonificacionFiscal | CrearSolicitudAporteRescateNormal (TW) |
| APV IngresarOrdenRVBonificacionFiscal | CrearOrdenes (TW), AgregarLog (TW) |
| APV BcsJobCargaMasivaPrevired | ninguno |
| CuentaInversion Pauta* (4 jobs que inscriben programas) | InscribirProcesoBatch (TW); cada Pauta confirma "inscribir" por HTTP antes de que el job termine |
| CuentaInversion jobs de reportes/archivos | AgregarLog (auditoria, TW) y CrearMovimientoComprobante (TW, via ColaboradoresReportesAdapter; que reporte lo invoca no se verifico); resto lectura |
| Contabilidad (5 jobs) | ninguno por Java; SAP B1 y SFTP son efectos externos |

Conteos de aristas "llamador -> colaborador" distintas (sin repetir por job): Tesoreria Jobs 5 directas + 8 alcanzables por Servicios; APV Jobs 9 directas (+1 StoredProcedure en proceso); CuentaInversion Jobs 1 directa (InscribirProcesoBatch) + 11 `CLASE` + 8 del adaptador de reportes; Contabilidad 3; Servicios: Tesoreria 24, APV 26, CuentaInversion 11 + 8.

## B. Contrato exacto del Job Manager legacy

Archivos: `legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA/BcsJobManager/src/cl/bcs/scheduler/` (`BcsJobManager.java` = JM, `BcsJob.java`, `BcsJobListener.java`, `sql/TransactionManager.java` = TM, `tools/BcsJobManagerSingleBean.java` = SB). El DDL de `BAT.Programa` del repo (`Plataforma/BD/Scripts/BAT_Create_Tables.sql:88-103`) esta desactualizado: no trae `Bloqueo`, `Nodo` (se agrego en `Scripts/Historia/LIBERACION_20160331.SQL:43-46`, `NVARCHAR(1) NOT NULL DEFAULT ''`) ni `RutaSalida`; las columnas reales se infieren del Java y del procedimiento `BAT.AgregarPrograma`.

### B.1 Estados y columnas

- `BAT.Programa`: IdPrograma, IdGrupoParametro, IdAgenda (0 si no hay agenda), IdTipoPrograma, Intento (DEFAULT 0), IdUsuario, idEstado (PEN / ASI / TER / ERR), Resultado (NVARCHAR 2000), fechaCreacion, fechaEjecucion, fechaFin, fechaSistema, IdProgramaReprocesado, Nodo, Bloqueo (S/N), RutaSalida. `BAT.TipoPrograma`: IdTipoPrograma, MaximoIntentos (DEFAULT 0), Modulo, NombreClasePrograma, TipoClasePrograma, EsPauta, Nodo. `BAT.Agenda`: EsDemonio, Minuto, habilitado, UltimaFechaDeEjecucion, SiguienteFechaDeEjecucion.
- Creacion de un programa (`Servicios/BAT/AgregarPrograma.sql:72,87-88,104-105,120-121`): `INSERT ... idEstado='PEN', Intento=0, Resultado='Esperando ejecucion', fechaEjecucion=ahora+segundos, Nodo=TipoPrograma.Nodo`; `Bloqueo` NO se escribe (toma el DEFAULT de la columna, asumido 'N'). Los parametros van a `BAT.ParametrosPrograma` (IdGrupoParametro) en la misma llamada (:92-98).
- `BAT.Programa.Nodo` lo escribe SOLO `AgregarPrograma` (copiado de `TipoPrograma.Nodo`) y `ReprocesarPrograma.sql` (copia el del programa original, lineas ~95-110, y deja `IdProgramaReprocesado`). No hay otro escritor en el repo. Valores vistos en datos: `''` (por defecto y NOTIFY, DTE*), `'A'` (VALORIZADOR, `LIBERACION_20160331.SQL:50`), `'D'` (EjecucionContinuaSQL, `InstalacionEjecucionContinua.sql:5`).

### B.2 Consultas y actualizaciones por estado (con linea)

| Paso | Archivo:linea | SQL / efecto |
|---|---|---|
| Lectura de pendientes | TM:33-93 (consulta :41-47) | `SELECT ... FROM BAT.Programa P JOIN BAT.TipoPrograma TP LEFT JOIN BAT.Agenda WHERE P.Bloqueo='N' [AND P.IdTipoPrograma=?] AND P.Nodo=? AND P.idEstado='PEN' AND P.Intento < TP.MaximoIntentos AND P.fechaEjecucion BETWEEN hoy 00:00:00 AND CURRENT_UTCTIMESTAMP`. Sin ORDER BY y sin FOR UPDATE (comentado :48). Nodo = argumento con `-` quitado (:55,59; SB:46-48 convierte `*` o `-` en `''`). |
| Reclamo | TM:68-73 | por cada fila: `update p set p."Bloqueo"='S' from BAT.Programa p where "IdPrograma"=?` + `con.commit()` (:73). SIN `AND Bloqueo='N'`, SIN revisar filas afectadas: dos nodos pueden leer la misma fila antes de que cualquiera la reclame. |
| Intento | JM:65-66, TM:281-300 | `Intento = Intento+1` (calculado en Java: `programm.getIntento()+1`), commit, ANTES de cargar clase y parametros. Primera ejecucion deja Intento=1. |
| Parametros | JM:68, TM:474-504 | `select NombreParametro, Valor, ValorBlob from "BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas" where IdPrograma=?` (la vista une ParametrosPrograma con Programa, `Vistas/BAT/ConsultarParametrosProgramas.sql`). Si `ValorBlob` no es NULL el valor es `new String(bytes, UTF_8)` y reemplaza a `Valor` (:490-495). Mapa nombre->valor, el ultimo duplicado gana, sin ORDER BY. El `catch (SQLException)` esta vacio (:499-501): un error devuelve mapa vacio sin avisar. `fetchSize 1000`. |
| Clase | JM:64,69-70 | `TP.NombreClasePrograma` -> `Class.forName(...).newInstance()` y cast a `BcsJob`. |
| Demonio | JM:71, TM:136-150 | `EsDemonio` de `BAT.Agenda` por IdAgenda del programa. |
| Parametros al job | JM:72, `BcsJob.java:35-52,71-77` | `job.setParameters(idPrograma, mapa, ahora, esDemonio, dsJobs, Minuto)`; `getParam(k)` = `parameters.get(k)` (nulo si falta), `getParamInteger` loguea y devuelve null, `getOptionalParam` = `Optional.ofNullable`. `Minuto` solo define el retardo del demonio (:40-49). |
| Encolado | JM:74, TM:221-224 | `updateProgramm(id, 0, "Programa Encolado")`: `idEstado='ASI', fechaFin=0001-01-01 00:00:00, Resultado='Programa Encolado'`, commit (:253). |
| Fallo antes de correr | JM:76,79,82,86, TM:227-229 | `updateProgramm(id, -1, msg)`: `idEstado='ERR', fechaFin=ahora, Resultado=msg` (msg: "No encuentro la clase : ...", "No puedo castear job ...", mensaje de excepcion). |
| Ejecucion | JM:193-211, `BcsJob.java:126-160` | pool de `concurrentJobs` hilos (JM:37); cada job abre UNA conexion de `dsJob` (usuario distinto del del Job Manager), `SET SCHEMA BSCL`, `SET TRANSACTION AUTOCOMMIT DDL OFF`, `transaction(con)` true -> commit, false -> rollback, excepcion -> rollback; luego `status(...)`. |
| Estado final | `BcsJob.java:151,158`, SB:150-173, TM:224-229 | `status(id,false,"")` -> `updateProgramm(id,1,"Finalizado correctamente.")`: `idEstado='TER', fechaFin=ahora, Resultado=...`. `status(id,true,msg)` -> `updateProgramm(id,-1,"Terminado con errores:"+msg)`: `idEstado='ERR', fechaFin=ahora`. Commit Y rollback devuelven `status(false)`: ambos quedan TER (defecto TES-J-DEF-19, ya pinado). `Resultado` se corta a 1999 caracteres (TM:216-218). |
| Agenda tras fin | TM:240-250 | si resultado 1 o -1: `update ag set UltimaFechaDeEjecucion=ahora from Programa pg JOIN Agenda ag ON pg.IdAgenda=ag.IdAgenda and pg.IdPrograma=?`. |
| RutaSalida | `BcsJob.java:171-185` | `UPDATE BAT.Programa SET RutaSalida=:RutaSalida WHERE IdPrograma=:IdPrograma` con la conexion del job (misma transaccion). |
| Demonio (ciclo) | `BcsJob.java:94`, TM:154-172 | por vuelta `updateCycleDaemon`: Agenda Siguiente=ahora+1s, Ultima=ahora; Programa `idEstado='ASI', Bloqueo='S', fechaEjecucion=ahora, fechaSistema=ahora`. Reinicio (`updateStartDaemon`, TM:174-192) marca los PEN/ASI de esa agenda como `TER, Bloqueo='S', Resultado='Proceso abortado/reiniciado'`. Fuera de alcance moderno (RULE-061). |
| Generacion desde agenda | JM:96-158, TM:302-371 | `AgregarPrograma` por agenda vencida (no se reclama nada). |

### B.3 Reintentos, Bloqueo, Intento, MaximoIntentos

- La fila solo se lee con `Bloqueo='N'`, `idEstado='PEN'` y `Intento < MaximoIntentos`; el reclamo la deja `Bloqueo='S'` para siempre (el legacy no vuelve a ponerla en 'N').
- `Intento` se incrementa al desencolar, no al fallar: `MaximoIntentos=1` = una sola ejecucion; `MaximoIntentos=0` (default del DDL) = nunca se ejecuta.
- El reintento automatico NO esta activo en el legacy: `updateTryProgramm` (TM:259-279: ERR + Bloqueo='S' + Intento<Max + `fechaEjecucion` en la ultima hora -> `Bloqueo='N', idEstado='PEN', Resultado='(Reintento n)'+anterior, fechaEjecucion=ahora+60s`) tiene su unica llamada comentada (`BcsJobManager.java:104-105`). Los reintentos reales son manuales: `ReprocesarPrograma.sql` crea un programa NUEVO (copia IdTipoPrograma/IdAgenda/Nodo, `IdProgramaReprocesado` = original, Intento=0, PEN) y `ActualizarEstadoPrograma.sql:84` con `p_Result=2` pone PEN/Intento=0 pero no toca `Bloqueo` (queda 'S' y no se desencola).
- El Job Manager moderno (`BcsJobManager`, ver C) SI reactiva el reintento (RULE-059, `ProgramaJdbcAdapter.SQL_RETRY_FAILED`): ERR + Bloqueo='S' + `Intento < MaximoIntentos` + ultima hora -> PEN, Bloqueo='N'. Con "requiere revision" = ERR, `Intento=MaximoIntentos`, Bloqueo='S' esa fila queda fuera del reintento automatico (condicion `Intento < Max`), y fuera de la lectura de pendientes (no es PEN).

### B.4 Controles de concurrencia

- Reclamo por fila no condicional (TM:68-73), sin FOR UPDATE (TM:48 comentado).
- `TransactionManager.executionCount` (TM:522-546; la consulta en :525-526 cuenta programas ASI del mismo parametro/valor/tipo del dia) no tiene NINGUN llamador en el repo legacy (busqueda en todo `legacy/`): es codigo muerto, no un control vigente.
- `firstExecutionDiary` estatico por JVM (JM:30,99-102) y `CorregirPrimeraEjecucion` solo se ejecuta una vez por proceso.
- Zombies/nodos (`BAT.Nodos`, `PLA.Heartbeat`): `addNodo` TM:605-648, `updateCycleNodo` :699-724, `getZombieNodo` :651-678, `isRebootNodo` :681-697. Estos NO reclaman programas.

### B.5 Que reproducen hoy los JobRunner modernos

Los cuatro `JobRunner.java` (`runtime/`) son casi identicos (diff verificado): Tesoreria, APV y CuentaInversion con `informar()` (M8); Contabilidad sin esa proteccion (`JobRunner.java:143-149`: un fallo del listener o de `onError` escapa) y su `JobCallController` recibe `parametros` como `Map<String,Object>` y no tiene filtro de token interno (los otros tres tienen `TokenInternoFilter` y `ParametrosTexto`).

| Elemento del contrato | Reproducido | Evidencia |
|---|---|---|
| Una transaccion por corrida, true -> commit, false -> rollback, excepcion -> rollback + status(error) + onError | si | `JobRunner.correr`, tests `JobRunnerContractTest` (11 casos por modulo, p. ej. `apvjRun01..09c`, `CONT-J-01..09`) |
| Commit y rollback ambos reportan status(false,"") | si (defecto conservado) | `JobRunner.java:152-154` |
| `SET SCHEMA BSCL` + `SET TRANSACTION AUTOCOMMIT DDL OFF` en cada transaccion | si | `SESION_HANA`, `JobRunnerRevisionArquitecturaTest` (apvjRun100..105) |
| commit/rollback intermedios del job | si | `Corrida.commit/rollback` |
| `updateRutaSalida` en la transaccion del job | si | `SQL_UPDATE_RUTA_SALIDA`, test CONT-J-07 / apvjRun07 |
| `getParam/getOptionalParam/getParamInteger` | si, sobre el mapa que envia el llamador | `Corrida`; NO se cargan de BD |
| Una corrida por IdPrograma y por clase | si, solo por JVM | `programasEnCurso` / `clasesEnCurso`, 409 |
| Clase desconocida | 503 | `JobNoMigradoException` |
| Carga de parametros desde la vista y ValorBlob | NO | ningun JobRunner toca la vista |
| Resolucion de clase desde `TipoPrograma` | NO (la clase la manda el llamador) | `JobCall(clase,...)` |
| Reclamo Bloqueo='S', Intento+1, ASI "Programa Encolado" | NO | no hay SQL |
| Estado final TER / ERR, `Resultado`, `fechaFin`, `Agenda.UltimaFechaDeEjecucion` | NO: el `JobStatusListener` de los cuatro `JobsConfiguration` solo escribe un log (`JobsConfiguration.java:82-91` Tesoreria, `:63-72` APV, `:171-180` CuentaInversion, `:122-131` Contabilidad) | |
| Reintentos, demonio, heartbeat de nodo | NO (demonio excluido por decision; el resto lo tendria el motor) | |

Tablas BAT en los esquemas H2 de pruebas: Tesoreria `schema-jobs.sql` = `BAT.Programa(IdPrograma, RutaSalida)` y `BAT.RangoHorario`; CuentaInversion `schema-jobs.sql` = `BAT.Programa(IdPrograma, RutaSalida, RutaLog, Comentario)` y `BAT.TipoProgramaEjecucion(IdTipoPrograma, IdEstado)` (bandera de ejecucion por TIPO, ya existente en la BD); APV `schema-apv-jobs.sql` = `BAT.Programa` con 13 columnas (IdPrograma, IdGrupoParametro, IdAgenda, IdTipoPrograma, Intento, IdUsuario, idEstado, Resultado, fechaCreacion, fechaEjecucion, fechaFin, fechaSistema, RutaSalida), `BAT.ParametrosPrograma` (sin ValorBlob) y `BAT.LogPrograma`; Contabilidad `support/H2Jobs.java:40` = `BAT.Programa(IdPrograma, RutaSalida)`. (Nota 2026-10-21: los esquemas del patron H4 `schema-programa.sql` ya agregan lo que sigue con el DDL real.) Ninguno tiene `Bloqueo`, `Nodo`, `BAT.TipoPrograma`, `BAT.Agenda`, `BAT.Nodos` ni la vista `ConsultarParametrosProgramas`; hay que ampliarlos para probar el reclamo.

## C. Preguntas abiertas y lo que NO es posible sin cambios en la BD

### C.1 Quien desencola y llama a `/internal/job-call` (hallazgo que contradice el supuesto)

- `BcsPlataformaJobs` (modernizado) NO es un despachador: es el modulo de jobs de plataforma (pauta diaria, parametros financieros; `README.md`: "los jobs no exponen endpoints de activacion... `@Scheduled`"; ningun SQL sobre `BAT.*` en su `src/main`; gran parte en stubs). No lee `BAT.Programa`, no llama a `/internal/job-call`.
- El motor de cola moderno es `BcsJobManager` (puerto 8106, `modernized/.../BcsJobManager`, `JobDispatcherService`, `ProgramaJdbcAdapter`). Hoy tiene cuatro brechas para ser el despachador de los jobs nuevos:
  1. `executeJob` es un marcador de posicion (`JobDispatcherService.executeJob`): marca TER "Finalizado correctamente." sin ejecutar nada ni llamar a `/internal/job-call`.
  2. Nunca llama a `lockPrograma` (existe en `ProgramaJdbcAdapter.java:228`, sin llamadores): no pone `Bloqueo='S'`; solo la lectura y luego `idEstado='ASI'` (dos replicas pueden despachar el mismo programa). La nota RULE-057 dice "FOR UPDATE SKIP LOCKED", pero el codigo lo tiene removido ("not supported by SAP HANA", `ProgramaJdbcAdapter.java:57,79`): README y notas estan desactualizados.
  3. Reactiva el reintento automatico (RULE-059), que el legacy no tenia activo (B.3).
  4. Ya carga parametros con `ValorBlob` (`readParams`, `ProgramaJdbcAdapter.java:270-285`) y filtra por `P.Nodo = :nodo` como el legacy.
- DECISION: (a) usar `BcsJobManager` como despachador (hay que completar 1-3), o (b) confirmar que se refiere a `BcsPlataformaJobs`, que hoy no tiene ese codigo. No se asume ninguno.

### C.2 Nodo del motor moderno con datos existentes (lo pedido por el usuario)

- Quien escribe `Programa.Nodo`: solo `BAT.AgregarPrograma` (copia `TipoPrograma.Nodo`, `AgregarPrograma.sql:72,88,105,121`) y `BAT.ReprocesarPrograma` (copia el del programa original). Llaman a `AgregarPrograma` el Job Manager legacy en modo AGENDA (`TM:302-371`), `AgregarPrograma`/`InscribirProcesoBatch` (Java) y `CrearCliente_CallAgregaPrograma.sql`. En todos los casos el valor sale del TIPO, no de la agenda ni del llamador.
- Legacy: `readProgramm` solo recoge `P.Nodo = <argumento del arranque>` (TM:47,55-59; SB:46-48) y opcionalmente `P.IdTipoPrograma = <argumento>` (TM:39,53-54). Los argumentos de arranque del Job Manager legacy (`PROGRAMA|AGENDA`, nodo, tipo, concurrencia) son configuracion de despliegue que NO esta en el repo (no hay scripts de arranque).
- Con datos existentes SOLO es posible si la BD ya contiene un `TipoPrograma.Nodo` para las clases migradas que ningun Job Manager legacy en produccion procesa. Eso no es verificable desde el repo y requiere una consulta de solo lectura del DBA: `SELECT DISTINCT "Nodo" FROM BAT.TipoPrograma` y `SELECT DISTINCT "Nodo","IdTipoPrograma" FROM BAT.Programa`, mas los argumentos reales de los procesos `BcsJobManagerSingleBean`.
- NO es posible sin cambio de datos/BD: (i) asignar un Nodo propio a los tipos migrados (seria `UPDATE BAT.TipoPrograma SET Nodo=...`, un cambio de datos de configuracion, excluido); (ii) un `UPDATE Programa.Nodo` posterior al alta por parte del motor moderno no es atomico con `AgregarPrograma` (que confirma en su llamada) y el poll del legacy lo puede alcanzar en medio: queda descartado.
- Alternativa operativa sin tocar BD ni codigo legacy (decision de despliegue, no de codigo): arrancar los procesos legacy con el filtro de tipo (`args[3]`) o retirar el Job Manager legacy de esos tipos al hacer el corte por tipo. Si ninguna de las dos es aceptable, "el legacy nunca toca codigo modernizado" no se puede garantizar solo con Nodo y debe quedar como limitacion documentada.

### C.3 Valor de estado de "requiere revision" (ERR, Intento = MaximoIntentos, Bloqueo = 'S')

- Es seguro con el legacy: el Job Manager legacy solo lee `PEN` y `Bloqueo='N'` (TM:45,47) y su reintento (`updateTryProgramm`, que exigiria `Intento < Max`) esta comentado (JM:104-105). El motor moderno tampoco lo reintenta (`SQL_RETRY_FAILED` exige `Intento < MaximoIntentos`).
- `MaximoIntentos` se lee de `BAT.TipoPrograma` por `IdTipoPrograma` (existente).
- Limitaciones sin cambios de BD: (a) es indistinguible de un fallo comun agotado (ERR con Intento=Max es el estado normal tras el ultimo intento); la unica diferencia es una convencion de texto en `Resultado` (p. ej. prefijo fijo "REQUIERE REVISION:"), NVARCHAR(2000); (b) las acciones manuales `ReprocesarPrograma` (crea un programa nuevo) y `ActualizarEstadoPrograma` con `p_Result=2` pueden relanzar el trabajo sin pasar por la revision: es un control operativo, no tecnico; (c) un proceso caido deja el programa en ASI/'S' (reclamado) sin senal propia.

### C.4 Idempotencia y reclamo con las columnas existentes

Posible (sin DDL): (1) reclamo atomico por programa con `UPDATE BAT.Programa SET Bloqueo='S', idEstado='ASI' ... WHERE IdPrograma=? AND idEstado='PEN' AND Bloqueo='N' AND Intento<MaximoIntentos` y verificar `filas==1` (el legacy no condiciona, TM:68-73); (2) segundo reclamo en el job por comparar-y-cambiar sobre `Resultado` (`'Programa Encolado'` -> `'En Ejecución'`, literal que ya usa `ActualizarEstadoPrograma.sql:72`) condicionado a `idEstado='ASI' AND Bloqueo='S'`: dos JVM no pueden correr el mismo programa; (3) clave = `IdPrograma` + `Intento` (el numero de corrida es la columna `Intento`); (4) diario previo a cada llamada a un colaborador TW en `BAT.LogPrograma` (columnas FechaLog, IdTipoPrograma, IdPrograma, Mensaje; tabla existente y su insert `AgregarLogPrograma_Insert.sql`), confirmado en una conexion APARTE antes y despues de la llamada; (5) parametros de la vista por `IdPrograma` y verificacion de la clase contra `TipoPrograma.NombreClasePrograma`.
NO posible sin cambios de BD (documentar como limitacion): (a) restriccion unica en BD sobre (programa, intento, colaborador): el diario de `LogPrograma` no tiene clave, la verificacion "ya existe la marca" es comprobar-y-luego-insertar y solo la cierra el reclamo por programa; (b) hacer idempotente al colaborador TomEE: sin cabecera en el router y sin tocar el legacy, una llamada repetida se ejecuta dos veces; solo se evita NO repitiendola (fallo cerrado); (c) deshacer lo que un colaborador TomEE ya confirmo (no hay transaccion compartida entre procesos); (d) un dueno/instancia del reclamo (no hay columna: se podria codificar en `Resultado`, texto libre); (e) recuperacion automatica de un programa que quedo en `En Ejecución` por caida de proceso: requiere una politica de antiguedad sobre `fechaEjecucion`, sin columna de latido por programa (`BAT.Nodos` es por nodo, no por programa).

### C.5 Router y cabecera de idempotencia

- El router no lleva cabecera de idempotencia (decision: legacy intacto). El contrato es `POST {JAVACALL_URL}/internal/java-call` con `{"class","params"}` y `X-Internal-Token` (los cuatro `ColaboradoresJobs`/`ColaboradoresJavaCall`). Hasta donde se leyo, `BcsPlataformaWeb` (`JavaCallRouterService`, `CallController` en `/call`) NO expone `/internal/java-call`: llama a ese endpoint de los servicios destino. No se encontro en `modernized/` que servicio atiende `JAVACALL_URL`. PREGUNTA: que servicio responde ese endpoint por paquete y quien es su propietario.
- `JavaCallRouterService`: lista de clases habilitadas vacia por defecto (`plataforma.javacall.clases-habilitadas`, "default EMPTY = todo a TomEE"); 503 o conexion rechazada reintenta en TomEE; cualquier otro error (500, 4xx, timeout de lectura) se propaga porque pudo ocurrir tras un commit (ARQ-H6). Consecuencia: hoy incluso los colaboradores del grupo M (ya migrados) se ejecutan en TomEE salvo que se habiliten. Impone `IdEmpresa/IdUsuario` de sesion sobre los params (ARQ-H5) y espera `X-SESSION-TOKEN`; los jobs envian solo `X-Internal-Token`: no verificado que el router acepte esa llamada sin sesion.

### C.6 Otras decisiones

1. Mapa de respuestas de `/internal/job-call` a estados: 200 COMMIT o ROLLBACK -> TER (paridad con el legacy, ambos TER); 500 -> ERR "Terminado con errores:"+mensaje; 503 (clase no migrada) y 409 (en curso) no deben marcar ERR. A confirmar por el dueno del motor.
2. El legacy usaba dos usuarios de BD: uno del Job Manager y otro de los jobs (SB:79-96). Los jobs modernos usan un solo `DataSource`; privilegios de `BAT.Programa`, `BAT.LogPrograma`, `BAT.TipoPrograma` y la vista para el usuario del job a confirmar por el DBA.
3. `readProgramm` solo toma `fechaEjecucion` entre hoy 00:00 y ahora (TM:47): un programa pendiente de dias anteriores nunca se ejecuta. Mantener o no.
4. BcsContabilidadJobs no tiene `TokenInternoFilter` y su `JobCallController` acepta `Map<String,Object>`; igualar a los otros tres antes de recibir trafico real.
5. Dependencia ciclica si Tesoreria y APV se llaman en proceso: Tesoreria Jobs llama `ValidarMandatosPAC` y `CambiarEstadoContratosSAAPV` (APV), y APV Servicios llama `CrearIngresoEgreso`, `LiquidarComprobante`, `CrearComprobante` y los tres `*MandatoPAC` (Tesoreria). Resolver en el ensamblado de la aplicacion de jobs (ambas librerias en el mismo contexto), no como dependencia Maven mutua. Decision de arquitectura.
6. SAP B1 y SFTP de Contabilidad no son transaccionales: un rollback no los deshace. Fuera del alcance de H-1, documentar.

## D. Plan de implementacion propuesto (sin DDL, sin tocar el legacy)

Todos los pasos son codigo Java y SQL DML sobre columnas y tablas existentes. Cada paso lleva tests nuevos; no se tocan los goldens del legacy. Las columnas de `BAT.*` y de la vista ya NO son suposicion: el DBA entrego el DDL real (HANA QA, 2026-10-21; `BAT_DDL_REAL.md`) y los esquemas H2 del patron H4 (`schema-programa.sql`) lo reproducen; siguen asumidos claves primarias, NOT NULL y DEFAULTs. `IdTipoPrograma` es NVARCHAR(40) (texto, no numerico). Se mantiene el patron de copiar `runtime/` por modulo (como hoy) salvo que se decida un modulo comun.

| Paso | Que hace | Archivos que toca | Tests nuevos |
|---|---|---|---|
| 0 | Decisiones C.1 (despachador), C.2 (Nodo/corte por tipo), C.5 (quien atiende `JAVACALL_URL`) y consulta de solo lectura del DBA (Nodo, `MaximoIntentos`, argumentos de arranque legacy). Sin esto no se cierran 3, 7 ni 8. | `TRANSFORMATION_NOTES.md` de cada Jobs y este inventario | n/a |
| 1 | Ampliar los esquemas H2 de prueba con las columnas reales de `BAT.Programa` (`Bloqueo`, `Nodo`, `Intento`, `idEstado`, `Resultado`, `fechaFin`, `IdAgenda`: CONFIRMADAS por el DBA el 2026-10-21), `BAT.TipoPrograma` (MaximoIntentos, NombreClasePrograma, Nodo), `BAT.ParametrosPrograma` (+ `ValorBlob`), la vista `ConsultarParametrosProgramas` como tabla, `BAT.Agenda`, `BAT.LogPrograma` (columnas reales confirmadas: `BAT_DDL_REAL.md`) | Tesoreria `src/test/resources/tesoreria-jobs/schema-jobs.sql`; CuentaInversion `.../cuenta-inversion-jobs/schema-jobs.sql`; APV `.../apv-jobs/schema-apv-jobs.sql`; Contabilidad `src/test/.../support/H2Jobs.java:40` | auto-verificacion del esquema (columna por columna contra A/B de este documento) |
| 2 | `ProgramaRepositorio` (JDBC, por modulo): `cargarPrograma(idPrograma)` con clase de `TipoPrograma`, `parametros(idPrograma)` desde la vista con `ValorBlob` UTF-8 (B.2, sin tragar el error), `reclamar(idPrograma)` = CAS descrito en C.4 (1)(2), `liberar/terminar`. Usa conexion APARTE (confirma por si mismo, como `updateProgramm`, TM:212-257), nunca la del job | nuevo `runtime/ProgramaRepositorio.java` y `runtime/ProgramaNoReclamableException` en los 4 Jobs | reclamo concurrente (dos hilos, 1 gana), reclamo ya hecho -> 409, fila inexistente -> 404, clase distinta de `TipoPrograma` -> 422, `ValorBlob` reemplaza a `Valor`, duplicados de parametro (ultimo gana como el legacy) |
| 3 | `JobCallController` y `JobRunner.ejecutar(idPrograma)`: el cuerpo exige `idPrograma`; `clase` y `parametros` se ignoran o deben coincidir con la fila (si no, 422). El `ParametrosTexto` y el token interno se igualan en Contabilidad. Se conserva el guard por JVM como segunda red | `web/JobCallController.java`, `runtime/JobRunner.java` (4 modulos); `BcsContabilidadJobs/.../web` (+ filtro de token) | `JobCall*Test` nuevos: parametros del llamador no se usan; Contabilidad 401 sin token; los tests `JobRunnerContractTest` existentes siguen verdes |
| 4 | `JobStatusListener` real: por salida del job aplica el estado final del legacy (TER/ERR, `Resultado` truncado a 1999, `fechaFin`, `Agenda.UltimaFechaDeEjecucion`, TM:212-257) en transaccion aparte; commit y rollback siguen reportando TER. Caso especial "requiere revision" (paso 6): `ERR`, `Intento=MaximoIntentos`, `Bloqueo='S'`, `Resultado` con prefijo fijo | `wiring/JobsConfiguration.java` (el bean hoy solo hace log) + `runtime/JobStatusListener` impl nueva | estados finales contra H2, truncado, agenda, fallo del listener no pierde el `JobResultado` (ya cubierto por M8 en 3 modulos; agregarlo a Contabilidad) |
| 5 | Grupo M en proceso: `ColaboradoresJobs` enruta primero a un `JavaCallDispatcher` local para las clases de A.2 grupo M que el modulo empaquete (Tesoreria Jobs: `ValidarMandatosPAC` de APV; los servicios de Tesoreria que APV y CuentaInversion usan; Contabilidad: `ConsultarClearingFinanciero` en vez de `/api/tesoreria/...`). Ensamblado de la app de jobs con ambas librerias (C.6.5) | los `adapter/ColaboradoresJobs.java` de Tesoreria y APV Jobs (hoy solo CuentaInversion Jobs tiene el despacho local), `wiring/Servicios*Configuration.java`, `pom.xml` de los jobs; Contabilidad `infrastructure/plataforma/TomEePlataformaClient.java` (es Servicios) | job escribe via colaborador M y devuelve `false` -> sin filas (rollback conjunto); job lanza excepcion tras la llamada -> sin filas; mismo `DataSource` y una sola transaccion (conteo de conexiones); demuestra la propagacion REQUIRED hoy asumida |
| 6 | Grupo TW con fallo cerrado: envoltorio `ColaboradorTomEEGuardado` que, por llamada a un colaborador TW escritor (A.2 lista TW), (a) escribe en `BAT.LogPrograma` la marca `INTENTO` con clave `idPrograma|Intento|clase|n` en conexion aparte, confirmada; (b) llama por HTTP; (c) marca `HECHO`. Antes de iniciar, si una corrida anterior del mismo programa deja `INTENTO` sin `HECHO` o un `HECHO` de un TW escritor, el programa pasa a "requiere revision" y NO se ejecuta (nada de reintento automatico). Si el job falla tras un `HECHO`, el estado final es "requiere revision". Lecturas (TL) no se guardan | nuevo `adapter/ColaboradorGuardado.java` por modulo, `ColaboradoresJobs`, `ProgramaRepositorio` | router simulado: llamada unica; caida entre INTENTO y HECHO -> segunda corrida rechazada; fallo del job tras HECHO -> estado de revision; lecturas no dejan marcas; el estado de revision no es tomado por la lectura del legacy ni por `SQL_RETRY_FAILED` (test contra el esquema del paso 1) |
| 7 | Despachador (segun C.1): si es `BcsJobManager`: `lockPrograma` condicional (`AND Bloqueo='N' AND idEstado='PEN'`, verificar filas) antes de `updateIntent`; `executeJob` real = `POST` a `/internal/job-call` del Jobs correspondiente con `idPrograma` y token, mapeo de respuestas de C.6.1; el `retryFailedPrograms` se deja o se apaga segun decision (legacy no reintenta) | `BcsJobManager/.../JobDispatcherService.java`, `ProgramaJdbcAdapter.java`, `TRANSFORMATION_NOTES.md`/README (corregir "SKIP LOCKED") | doble despacho con dos instancias -> una sola llamada; 503/409 no marcan ERR; 500 -> ERR; reclamo y estado final en H2; `FifoOrderTest` actualizado |
| 8 | Corte por tipo: documentar para cada `IdTipoPrograma` migrado que Job Manager legacy ya no lo procesa (C.2, alternativa operativa) y el valor de `Nodo` que usa el motor moderno; revisar que `AgregarPrograma`/agenda sigan creando los programas con ese Nodo | `TRANSFORMATION_NOTES.md`, runbook de despliegue (fuera de este repo si aplica) | consulta de verificacion de solo lectura que el DBA ejecuta antes del corte |
| 9 | Verificacion: ejecutar los tests de los 4 Jobs y de `BcsJobManager`; registrar H-1 y H4 como "cerrados con limitaciones" (C.4 (a)-(e), C.2, C.3 (a)(b)(c), C.6.6) en el informe de verificacion | `analysis/.../VERIFICATION.md` y `TRANSFORMATION_NOTES.md` de cada modulo | los de los pasos 1-8 |

Orden y dependencias: 0 -> 1 -> 2 -> 3 -> 4; 5 y 6 dependen de 2 y 4; 7 depende de 0 y 3; 8 depende de 0; 9 al final. Criterio de entrada para trafico real: cerrar 2, 3, 4, 6 y 7 (o aceptar por escrito las limitaciones de C).

