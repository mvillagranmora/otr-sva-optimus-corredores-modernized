# Prioridad de construccion segun uso real (PLA.Servicio, HANA QA)

Fuente: `hana_qa_results/pla_servicio_filas.csv` (export de solo lectura de `"BSCL"."BSCL.Plataforma.BD::PLA.Servicio"`, 3.163 filas, 2026-10-21). Columna `UsoServicio` = contador acumulado de llamadas por servicio.

## Limites de la medida

- Es un contador **acumulado de QA**: mide frecuencia de llamada, no criticidad de negocio; QA es una copia de produccion exportada a QA1 (confirmado por el usuario), por lo que el contador es representativo.
- Solo los servicios de **tipo J** (clase Java, 981) corresponden a modulos que migramos. Los tipo Q (1.347, 12,57 M de usos) y P (826, 1,82 M) son vistas y procedimientos HANA: viven en la BD y no se tocan (regla: sin cambios de BD); solo cambia quien los despacha (`BcsPlataformaWeb` `/call`).

## Uso por tipo de servicio

| Tipo | Servicios | Uso acumulado |
|---|---:|---:|
| Q | 1347 | 12.565.591 |
| J | 981 | 2.410.604 |
| P | 826 | 1.819.030 |
| X | 6 | 4.074 |
| O | 2 | 76 |
| A | 1 | 18 |

## Uso Java (tipo J) por dominio

| Dominio | Clases (con uso) | Uso | % Java | Modulo de servicios | Fase brief | Estado |
|---|---:|---:|---:|---|---|---|
| plataforma | 180 (70) | 1.927.385 | 80.0 % | BcsPlataformaServicios | 5 | sin migrar |
| fondomutuo | 67 (41) | 249.966 | 10.4 % | BcsFondoMutuoServicios | 5 | sin migrar |
| custodia | 129 (48) | 108.706 | 4.5 % | BcsCustodiaServicios | 4 | sin migrar |
| ordenes | 90 (39) | 42.501 | 1.8 % | BcsOrdenServicios | 3 | sin migrar |
| cuentaInversion | 27 (5) | 35.239 | 1.5 % | BcsCuentaInversionServicios | - | migrado |
| socionegocio | 163 (47) | 15.809 | 0.7 % | BcsSocioNegocioServicios | 4 | sin migrar |
| APV | 56 (40) | 9.076 | 0.4 % | BcsAPVServicios | - | migrado |
| tesoreria | 35 (21) | 8.257 | 0.3 % | BcsTesoreriaServicios | - | migrado |
| mesa | 84 (19) | 5.611 | 0.2 % | BcsMesaServicios | 3 | sin migrar |
| contabilidad | 21 (14) | 3.821 | 0.2 % | BcsContabilidadServicios | - | migrado |
| portafolio | 13 (5) | 2.440 | 0.1 % | BcsPortafolioServicios | 3 | sin migrar |
| facturacion | 30 (9) | 974 | 0.0 % | BcsFacturacionServicios | 4 | sin migrar |
| normativa | 81 (15) | 796 | 0.0 % | BcsNormativaServicios | 5 | sin migrar |
| Mesa | 1 (1) | 23 | 0.0 % | - | - | sin migrar |
| canalagente | 1 (0) | 0 | 0.0 % | BcsCanalAgenteServicios | 5 | sin migrar |
| sura | 3 (0) | 0 | 0.0 % | BcsSURA | 3 | sin migrar |

## Clases mas usadas por dominio sin migrar (top 10)

### plataforma (BcsPlataformaServicios)

| Uso | Clase |
|---:|---|
| 1.777.141 | `cl.bcs.plataforma.cre.ConsultarEstadoModulo` |
| 58.968 | `cl.bcs.plataforma.mer.CalcularParidad` |
| 52.679 | `cl.bcs.plataforma.pla.ConsultarFlujoCaja` |
| 15.489 | `cl.bcs.plataforma.mer.CalcularParidad` |
| 4.298 | `cl.bcs.plataforma.com.ConsultarMatrizOperacional` |
| 2.775 | `cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo` |
| 2.312 | `cl.bcs.plataforma.ins.ConsultarDatosInstrumentos` |
| 1.891 | `cl.bcs.plataforma.pla.GuardarConfiguracionGrillaUsuario` |
| 1.796 | `cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa` |
| 1.630 | `cl.bcs.plataforma.ind.ConsultarRequisito` |

### fondomutuo (BcsFondoMutuoServicios)

| Uso | Clase |
|---:|---|
| 167.843 | `cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate` |
| 35.047 | `cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal` |
| 16.217 | `cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate` |
| 8.886 | `cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo` |
| 4.422 | `cl.bcs.fondomutuo.CalcularMontoNumeroCuota` |
| 4.261 | `cl.bcs.fondomutuo.ConsultarSolicitudesFFMM` |
| 4.261 | `cl.bcs.fondomutuo.ConsultarSolicitudesFFMMWS` |
| 1.773 | `cl.bcs.fondomutuo.ConsultarFondoMutuoReinversion` |
| 1.183 | `cl.bcs.fondomutuo.ConsultarInstrumentosFondosMutuos` |
| 923 | `cl.bcs.fondomutuo.CargarValorCuota` |

### custodia (BcsCustodiaServicios)

| Uso | Clase |
|---:|---|
| 86.632 | `cl.bcs.custodia.ConsultarCustodiaPortafolio` |
| 11.962 | `cl.bcs.custodia.ConsultarCustodiaPortafolioAgente` |
| 2.731 | `cl.bcs.custodia.ConsultarFlujoCustodia` |
| 1.707 | `cl.bcs.custodia.IngresoTitulos` |
| 1.352 | `cl.bcs.custodia.ConsultarCustodiaMovimientosAgente` |
| 562 | `cl.bcs.custodia.evc.ProcesarEventoCapital` |
| 559 | `cl.bcs.custodia.evc.CrearEventoCapital` |
| 416 | `cl.bcs.custodia.IngresoRetiroTitulosJava` |
| 384 | `cl.bcs.custodia.utils.ValorizarInstrumento` |
| 237 | `cl.bcs.custodia.prt.CrearEmprestito` |

### ordenes (BcsOrdenServicios)

| Uso | Clase |
|---:|---|
| 16.733 | `cl.bcs.ordenes.CrearOrdenes` |
| 6.395 | `cl.bcs.ordenes.CalcularMontoYCobrosOrden` |
| 5.469 | `cl.bcs.ordenes.CalcularCoberturaOrdenes` |
| 4.660 | `cl.bcs.ordenes.CalcularExigibilidadOrdenes` |
| 2.036 | `cl.bcs.ordenes.CrearOrdenesPostreras` |
| 1.036 | `cl.bcs.ordenes.EnviarAsignacionesFacturar` |
| 992 | `cl.bcs.ordenes.CrearSimulacionAsignacion` |
| 958 | `cl.bcs.ordenes.CrearTransacciones` |
| 722 | `cl.bcs.ordenes.ConsultarFormaDePagoUltimaOrden` |
| 469 | `cl.bcs.ordenes.ConsultarAsignaciones` |

### socionegocio (BcsSocioNegocioServicios)

| Uso | Clase |
|---:|---|
| 3.331 | `cl.bcs.socionegocio.enr.ConsultarEntidadEnrolamiento` |
| 2.755 | `cl.bcs.socionegocio.doc.GenerarDocumentos` |
| 2.755 | `cl.bcs.socionegocio.utils.RegenerarDocumentosCliente` |
| 1.384 | `cl.bcs.socionegocio.doc.ConsultarDocumentos` |
| 1.323 | `cl.bcs.socionegocio.soc.ConsultarSocioNegocio` |
| 732 | `cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales` |
| 477 | `cl.bcs.socionegocio.eje.ObtenerResumenEjecutivo` |
| 330 | `cl.bcs.socionegocio.soc.CrearSocioNegocio` |
| 322 | `cl.bcs.socionegocio.emp.CrearEmpleado` |
| 317 | `cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria` |

### mesa (BcsMesaServicios)

| Uso | Clase |
|---:|---|
| 3.408 | `cl.bcs.mesa.MES.MarcarOperacion` |
| 852 | `cl.bcs.mesa.SPT.ConsultarPosicion` |
| 267 | `cl.bcs.mesa.FWD.ValorizacionForward` |
| 263 | `cl.bcs.mesa.FWD.AnticiparOperacionForward` |
| 241 | `cl.bcs.mesa.SPT.CrearPuntasSpot` |
| 182 | `cl.bcs.mesa.SPT.ConsultarPuntasMantenedor` |
| 124 | `cl.bcs.mesa.FWD.CrearMovimientoForwardTesoreria` |
| 105 | `cl.bcs.mesa.FWD.ReversaAnticipoForward` |
| 43 | `cl.bcs.mesa.SPT.CuadraturaCierreDiario` |
| 28 | `cl.bcs.mesa.MRF.CuadraturaCierreDiario` |

### portafolio (BcsPortafolioServicios)

| Uso | Clase |
|---:|---|
| 1.676 | `cl.bcs.portafolio.ModificarPortafolio` |
| 658 | `cl.bcs.portafolio.CrearPortafolio` |
| 76 | `cl.bcs.portafolio.CrearCuentaCustodio` |
| 23 | `cl.bcs.portafolio.CrearMatrizAgrupadorNOAPV` |
| 7 | `cl.bcs.portafolio.EliminarMatrizAgrupadorNOAPV` |
| 0 | `cl.bcs.portafolio.ws.ModificarEstadoPortafolioWS` |
| 0 | `cl.bcs.portafolio.EliminarSinTransacciones` |
| 0 | `cl.bcs.portafolio.ModificaPreferenciaPagoPortafolio` |
| 0 | `cl.bcs.portafolio.IngresaPreferenciaPagoPortafolio` |
| 0 | `cl.bcs.portafolio.ModificarMatrizAgrupadorNOAPV` |

### facturacion (BcsFacturacionServicios)

| Uso | Clase |
|---:|---|
| 362 | `cl.bcs.facturacion.CuadraturaCierreDiario` |
| 219 | `cl.bcs.facturacion.AnularMovimientoComprobante` |
| 147 | `cl.bcs.facturacion.CrearComprobante` |
| 137 | `cl.bcs.facturacion.ConsultaDocumentoDTE` |
| 51 | `cl.bcs.facturacion.CrearMovimientoComprobante` |
| 34 | `cl.bcs.facturacion.CrearComprobanteNotaDeCredito` |
| 9 | `cl.bcs.facturacion.ProcesarCargaMasiva` |
| 8 | `cl.bcs.facturacion.CrearMovimientoDesdeOtroComprobante` |
| 7 | `cl.bcs.facturacion.AnularComprobanteFinal` |
| 0 | `cl.bcs.facturacion.CrearComprobanteCobroMensual` |

### normativa (BcsNormativaServicios)

| Uso | Clase |
|---:|---|
| 375 | `cl.bcs.normativa.cartola.ResumenInversionWrapper` |
| 264 | `cl.bcs.normativa.circulares.NotificarCircularizacion` |
| 36 | `cl.bcs.normativa.cartola.GeneradorCartola` |
| 24 | `cl.bcs.normativa.IngresarMLT` |
| 23 | `cl.bcs.normativa.declaraciones.DJ1899.ActualizarDetalles` |
| 19 | `cl.bcs.normativa.cartola.ActualizarReproceso` |
| 14 | `cl.bcs.normativa.declaraciones.DJ1871.ReajustarValorIPC` |
| 13 | `cl.bcs.normativa.declaraciones.DJ1899.ReajustarValorIPC` |
| 12 | `cl.bcs.normativa.declaraciones.DJ1944.ReporteCertificado17` |
| 7 | `cl.bcs.normativa.declaraciones.DJ1871.AjustarRescatesDelPeriodo` |

## Servicios Q y P por dominio HANA (no se migran: se despachan igual)

| Dominio HANA | Servicios | Uso |
|---|---:|---:|
| Plataforma | 548 | 11.442.334 |
| Portafolio | 48 | 765.144 |
| SocioNegocio | 216 | 581.755 |
| FondoMutuo | 109 | 425.264 |
| Mesa | 209 | 275.415 |
| Tesoreria | 88 | 183.240 |
| Contabilidad | 91 | 175.283 |
| Orden | 117 | 148.893 |
| APV | 62 | 87.753 |
| Custodia | 144 | 84.016 |
| Facturacion | 73 | 66.076 |
| CuentaInversion | 41 | 66.008 |
| Normativa | 407 | 62.035 |
| Mercado | 5 | 20.204 |
| CanalAgente | 4 | 1.192 |
| Homologacion | 2 | 9 |
| (otro) | 3 | 0 |
| CanalCliente | 5 | 0 |
| BSCL | 1 | 0 |

## Dependencias entre modulos (topology.json)

Los modulos pendientes forman **ciclos**: Orden llama a Custodia, Mesa, Facturacion y SocioNegocio; Custodia llama a Orden y a Mesa; Plataforma llama a casi todos. No existe un orden topologico limpio. El patron ya usado (colaboradores aun no migrados se llaman por HTTP/TomEE detras de un puerto, y pasan a proceso cuando el dueno se migra) sigue siendo valido: el orden se decide por uso y riesgo, no por dependencias.

## Orden final propuesto (para aprobacion)

| # | Bloque | Modulos | Por que en este lugar | Riesgo |
|---|---|---|---|---|
| 1 | **Plataforma, ola A** | `BcsPlataformaServicios`: solo las clases de mayor uso (`ConsultarEstadoModulo` 1.777.141, `CalcularParidad` 74.457, `ConsultarFlujoCaja` 52.679, `ConsultarMatrizOperacional`) | Concentran ~80 % del uso Java; son lecturas; poco codigo. Mayor efecto con menor riesgo. Alterna: ola B (resto de las 180 clases) en el bloque 9. | Bajo |
| 2 | **Ordenes** | `BcsOrdenServicios`, `BcsOrdenJobs`, `BcsJobOrders` | Fase 3 del brief. `CrearOrdenes` (16.733 usos) hoy es escritura protegida TomEE en 3 modulos Jobs: al migrarlo pasa a proceso. RULE-068 (inyeccion SQL) se corrige aqui. | Alto |
| 3 | **Portafolio** | `BcsPortafolioServicios` | 13 clases, pequeno, lo usan Orden, Mesa y SocioNegocio. | Bajo |
| 4 | **FondoMutuo** | `BcsFondoMutuoServicios`, `BcsJobsFondoMutuo` | 10,4 % del uso Java; `ManejadorSolicitudAporteRescate` 167.843 y `CrearSolicitudAporteRescateNormal` 35.047 (escritura protegida en jobs). Depende de Custodia y Plataforma por HTTP. Fase 5 del brief: pasa antes por uso. | Medio |
| 5 | **Custodia** | `BcsCustodiaServicios`, `BcsCustodiaJobs` | 4,5 %; `ConsultarCustodiaPortafolio` 86.632 (APV la llama por HTTP). Fase 4. Hoy 97/99 stubs, NOT PROVEN. | Alto |
| 6 | **Mesa + externos** | `BcsMesaServicios`, `BcsMesaJobs`, `BcsValo2`, `BECB`, `SURA` | Poco uso (0,2 %) pero 65.153 lineas; BECB/SURA exigen credenciales sandbox (pendiente del usuario). | Alto |
| 7 | **SocioNegocio y Facturacion** | `BcsSocioNegocioServicios/Jobs`, `BcsFacturacionServicios/Jobs`, `BcsReplica` | 0,7 % y 0,0 %; muchos modulos de los que dependen otros. | Medio |
| 8 | **Normativa y reportes** | `BcsNormativa*`, `BcsCartolas*`, `BcsTributarioJobs`, `BcsPlataformaReporteApi` | Uso casi nulo; carga regulatoria (revision del experto de dominio). | Medio |
| 9 | **Plataforma, ola B y retiro** | resto de `BcsPlataformaServicios`, `BcsPlataformaWeb` (despacho Q/P), `BcsFilters`, `BcsJobManager`, `BcsPlataformaJobs`, `BcsCanalAgenteServicios`, `BcsPortalConfirmacionWeb` | Cierre; el despacho de los 12,57 M de usos Q y 1,82 M P pasa de TomEE al nuevo core en el corte global. | Medio |

Cambios respecto al brief: la ola A de Plataforma (fase 5) se adelanta al inicio y FondoMutuo (fase 5) pasa antes que Custodia y Mesa. Requieren aceptacion explicita porque el brief esta firmado con otro orden.

Estado de decisiones (2026-10-21): el usuario **aprobo este orden** (incluye adelantar la ola A de Plataforma y FondoMutuo respecto del brief); **no se requiere** export de produccion de `PLA.Servicio`; las **credenciales sandbox de BECB/SURA se omiten por ahora** (esas integraciones se construyen sin validacion contra sandbox y quedan sin probar). Pendiente: cobertura de ramas >= 80 % de `CrearOrdenes` y `BcsMesaServicios` (`lizard`) antes del corte.
