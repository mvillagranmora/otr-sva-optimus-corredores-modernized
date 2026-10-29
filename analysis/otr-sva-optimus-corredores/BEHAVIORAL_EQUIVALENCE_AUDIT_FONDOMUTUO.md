# Auditoría de Equivalencia — BcsFondoMutuoServicios
**Fecha**: 2026-10-07  
**Servicios auditados**: 37 de 132 en javacall/ (+ 4 ausentes en modernizado)  
**Metodología**: Comparación manual legado vs modernizado — lectura directa de código fuente

---

## Resumen Ejecutivo

El patrón dominante encontrado es que **prácticamente todos los servicios modernizados son stubs**: cargan los mismos archivos SQL del legado pero omiten la totalidad de la lógica de negocio (validaciones, cálculos, llamadas cross-service, notificaciones). Solo 1 servicio auditado es funcionalmente equivalente, y 5 presentan divergencias parciales de comportamiento con lógica real.

| Estado | Cantidad | % del total auditado |
|--------|----------|----------------------|
| ✅ EQUIVALENTE | 1 | 3% |
| ⚠️ DIVERGENTE | 5 | 14% |
| ❌ STUB | 27 | 73% |
| ❓ SIN_LEGADO | 1 | 3% |
| 🚫 AUSENTE_EN_MODERNIZADO | 4 | 11% |

**Riesgo general: CRÍTICO.** Los servicios de mayor impacto operacional (Crear, Certificar, Anular, Liquidar solicitudes) son todos stubs. El sistema modernizado NO puede procesar solicitudes de fondo mutuo de forma autónoma.

---

## Patrón de STUB identificado

Todos los servicios marcados ❌ STUB siguen el mismo patrón:

```java
// 1. Cargan los mismos archivos SQL que el legado
this.sqlX = StreamUtils.copyToString(new ClassPathResource("sql/X.sql")…

// 2. Ejecutan las queries con parámetros simplificados
jdbc.queryForList(sqlX, ps);

// 3. Retornan los resultados crudos sin procesamiento
out.add("P_OUT_X", rawRows);
```

Lo que **falta** en todos los stubs:
- `RevisarRequisitosBasicos` (validación de empresa/usuario/módulo)
- Cálculos financieros internos (`CalcularMontoNumeroCuota`, `CalcularComisiones`, `CalcularParidad`)
- Llamadas cross-service al legado de Plataforma (`EntregarPreciosTasas`, `CalcularParidad`, `CrearCargoAbono`)
- Llamadas a Custodia (`IngresoCustodia`, `RetiroCustodia`, `ProcesarCustodiaFondoMutuo`)
- Llamadas a Tesorería (`ProcesarIngresoEgresoTesoreria`, `ProcesarIngresoOperacionSpot`)
- Creación de secuencias DB para IDs de solicitud
- Notificaciones OWP (`NotificarEstadoOrdenesOWP`)
- Integración AGF (`CrearSolicitudAGF`)
- Logging de auditoría (`AgregarLog`)
- Mecanismos de bloqueo anti-duplicados

---

## Hallazgos Detallados

### CrearSolicitudAporteRescateNormal
**FQN legado**: `cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal`  
**Archivo modernizado**: `service/javacall/CrearSolicitudAporteRescateNormalService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado implementa 1.790 líneas de lógica: mecanismo anti-duplicados con tabla temporal de bloqueo, validaciones de requisitos básicos, ObtenerPerfilFondoMutuo, CalcularFechaSolicitudAporteRescate, CalcularMontoNumeroCuota, CalcularParidad, CalcularComisiones, ServicioCumpleRequisitos, ValidaMontoInversion, ValidaPatrimonioEmisor, inserción de solicitud con secuencia DB, ProcesarIngresoEgresoTesoreria, CrearSolicitudAGF, NotificarEstadoOrdenesOWP. El modernizado ejecuta ~8 queries SQL y retorna filas crudas sin insertar solicitud procesada.

---

### CalcularMontoNumeroCuota
**FQN legado**: `cl.bcs.fondomutuo.CalcularMontoNumeroCuota`  
**Archivo modernizado**: `service/javacall/CalcularMontoNumeroCuotaService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado: (a) llama a `EntregarPreciosTasas` para obtener el valor cuota actual, (b) maneja conversión de monedas con `CalcularParidad`, (c) calcula NumeroCuotas = Monto / ValorCuota con redondeo configurable, (d) calcula MontoDestino, ValorParidad, CostoEnFactorAjuste (en UF), MontoMonedaSistema. El modernizado: ejecuta 2 queries SQL y retorna las filas crudas de valor cuota sin ningún cálculo. Output `P_OUT_RESULTADO` contiene filas DB en vez de resultados calculados.

---

### CalcularSpreadRescate
**FQN legado**: `cl.bcs.fondomutuo.CalcularSpreadRescate`  
**Archivo modernizado**: `service/javacall/CalcularSpreadRescateService.java`  
**Estado**: ⚠️ DIVERGENTE  
**Riesgo**: MEDIO  
**Divergencia**:
1. **Cálculo cross-currency ausente**: El legado llama a `CalcularParidad` cuando `monedaSistema != IdMonedaFondo` para convertir el MontoReajuste antes de calcular el spread. El modernizado siempre calcula `Spread = (CantidadRescate × ValorCuota) - MontoReajuste` sin conversión, produciendo un spread incorrecto en fondos en moneda extranjera.
2. **Manejo de errores**: El legado devuelve `P_OUT_MENSAJES` con mensajes localizados de BD; el modernizado solo incrementa `P_OUT_ERRORESCONTADOR` sin lista de mensajes.
3. **NumeroCalculoSpreadRescate**: El legado lo incluye en output; el modernizado sí lo incluye (correcto).

---

### CalcularCostoFactorAjuste
**FQN legado**: `cl.bcs.fondomutuo.CalcularCostoFactorAjuste`  
**Archivo modernizado**: `service/javacall/CalcularCostoFactorAjusteService.java`  
**Estado**: ⚠️ DIVERGENTE  
**Riesgo**: MEDIO  
**Divergencia**:
1. **Contrato de entrada**: El legado recibe `NumeroCalculoCostoFactorAjuste` por ítem; el modernizado recibe `IdInstrumento` e `IdSerieFondo`. Los campos de correlación son distintos.
2. **Contrato de salida**: El legado devuelve `NumeroCalculoCostoFactorAjuste`; el modernizado devuelve `IdInstrumento` e `IdSerieFondo`. Los callers del legado no podrán correlacionar resultados.
3. La fórmula matemática es equivalente: `costoRescate = cantidadRescate × costoTotal / cantidadTotal`.
4. **P_OUT_MENSAJES ausente** en modernizado.

---

### ValidaMontoInversion
**FQN legado**: `cl.bcs.fondomutuo.ValidaMontoInversion`  
**Archivo modernizado**: `service/javacall/ValidaMontoInversionService.java`  
**Estado**: ⚠️ DIVERGENTE  
**Riesgo**: ALTO  
**Divergencia**:
1. **Fuente de límites**: El legado obtiene `MontoMinimoTotalInversion`, `MontoMaximoTotalInversion`, `MontoMinimoAportesAltos`, `MontoMaximoAportesAltos` desde `ConfiguracionSistema` (BD). El modernizado los recibe como parámetros de entrada. Si el caller no los proporciona correctamente (ambos son 0 por defecto), NO se valida nada.
2. **Lógica de AportesAltos ausente**: El legado valida separadamente solicitudes con flag `AportesAltos=S` contra límites específicos. El modernizado ignora este flag.
3. **Agrupación por FolioContrato ausente**: El legado agrupa solicitudes por `FolioContrato` para validar montos acumulados. El modernizado suma todos los montos sin agrupación.
4. **Flag ValidaMontoMinimo ausente**: El legado respeta el flag `ValidaMontoMinimo` por solicitud. El modernizado siempre valida el mínimo.

---

### ConsultarSolicitudAporteRescateUI
**FQN legado**: `cl.bcs.fondomutuo.ConsultarSolicitudAporteRescateUI`  
**Archivo modernizado**: `service/javacall/ConsultarSolicitudAporteRescateUIService.java`  
**Estado**: ⚠️ DIVERGENTE  
**Riesgo**: MEDIO  
**Divergencia**:
1. **Parámetro p_Estado ausente**: El legado recibe y usa `p_Estado` como filtro; el modernizado no lo incluye. Todas las solicitudes en cualquier estado serán retornadas.
2. **Enriquecimiento ausente**: El legado llama a `CalcularMontoNumeroCuota` para calcular el valor cuota actual de cada solicitud en la UI. El modernizado retorna solo datos de BD sin enriquecimiento.
3. **Nombre de campo de salida**: El legado usa `P_OUT_RESULTADO`; el modernizado usa `P_OUT_SOLICITUDES`.

---

### ConsultarCustodiaPortafolioFFMM
**FQN legado**: `cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFFMM`  
**Archivo modernizado**: `service/javacall/ConsultarCustodiaPortafolioFFMMService.java`  
**Estado**: ⚠️ DIVERGENTE  
**Riesgo**: MEDIO  
**Divergencia**:
1. **Contrato de salida**: El legado retorna `p_out_tbl` como lista tipada `SalidaCustodia`. El modernizado retorna `P_OUT_CUSTODIAS`, `P_OUT_GARANTIAS`, `P_OUT_PERSONAS_DESTINO` como arrays separados.
2. **Parámetros ignorados**: `p_AgrupaPorBoveda`, `p_AgrupaPorRelacion` existen en el legado pero no en el modernizado.
3. **Campos calculados ausentes**: `TotalPosicion`, `PPPCustodia`, `CantidadPrecioPromedio` pueden requerir enriquecimiento que el SQL no provee directamente.

---

### ObtenerPerfilFondoMutuo
**FQN legado**: `cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo`  
**Archivo modernizado**: `service/javacall/ObtenerPerfilFondoMutuoService.java`  
**Estado**: ✅ EQUIVALENTE  
**Riesgo**: NINGUNO  
**Nota**: Mismo SQL, misma lógica (tomar primer resultado), mismo campo de salida `PerfilFondoMutuo`. La única diferencia es que el legado incluye `P_OUT_MENSAJES` (siempre lista vacía) que el modernizado omite — comportamiento funcionalmente idéntico.

---

### CertificarSolicitudAporteRescate
**FQN legado**: `cl.bcs.fondomutuo.CertificarSolicitudAporteRescate`  
**Archivo modernizado**: `service/javacall/CertificarSolicitudAporteRescateService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado orquesta: RevisarRequisitosBasicos, CalcularMontoNumeroCuota, CalcularComisiones, CalcularParidad, ProcesarCustodiaFondoMutuo, ProcesarIngresoEgresoTesoreria, ProcesarIngresoOperacionSpot, CrearSolicitudAporteReinversion (para reinversiones), CrearComprobante/CrearMovimientoComprobante (facturación), NotificarEstadoOrdenFondoMutuo, AgregarLogCertificacionAporteRescate, SaldoFlujoCaja. El modernizado ejecuta 4 queries y un UPDATE sin lógica de negocio.

---

### AnulacionSolicitudAporteRescate
**FQN legado**: `cl.bcs.fondomutuo.AnulacionSolicitudAporteRescate`  
**Archivo modernizado**: `service/javacall/AnulacionSolicitudAporteRescateService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado: valida estados de solicitud, llama `AnularOperacionSpot`, llama `CrearCargoAbono`, llama `IngresoCustodia`/`RetiroCustodia`, llama `CrearSolicitudAGF`, llama `NotificarEstadoOrdenesOWP`, llama `AgregarLog`. El modernizado: ejecuta queries y un batch UPDATE sin validación de estado ni reversiones de custodia/tesorería.

---

### LiquidacionSolicitudAporteRescate
**FQN legado**: `cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate`  
**Archivo modernizado**: `service/javacall/LiquidacionSolicitudAporteRescateService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado procesa cada solicitud liquidada aplicando movimientos de custodia, generando comprobantes de facturación, coordinando con tesorería. El modernizado hace un `UPDATE` masivo y retorna filas sin esta orquestación.

---

### CalcularFechaSolicitudAporteRescate
**FQN legado**: `cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate`  
**Archivo modernizado**: `service/javacall/CalcularFechaSolicitudAporteRescateService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado calcula las fechas de aceptación, valorCuota, certificación y liquidación avanzando días hábiles según calendarios de feriados, estados de fondo y valuta configurada por serie. El modernizado retorna la configuración de fechas cruda sin realizar el cálculo de avance de días hábiles.

---

### CalcularComisiones
**FQN legado**: `cl.bcs.fondomutuo.CalcularComisiones`  
**Archivo modernizado**: `service/javacall/CalcularComisionesService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado calcula el monto de comisión aplicando tramos, tipos de comisión (trimestral/porcentual), conversiones de moneda. El modernizado solo consulta los tramos de comisión desde BD sin aplicar ningún cálculo.

---

### CalcularValores
**FQN legado**: `cl.bcs.fondomutuo.CalcularValores`  
**Archivo modernizado**: `service/javacall/CalcularValoresService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El modernizado pasa los datos de entrada sin cambio. El comentario en el código indica que la integración con `CalcularParidad` (ajuste UF/UTM) está pendiente.

---

### CalcularCostoUFAportes
**FQN legado**: `cl.bcs.fondomutuo.CalcularCostoUFAportes`  
**Archivo modernizado**: `service/javacall/CalcularCostoUFAportesService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El modernizado retorna `CostoUF=0` y `ValorUF=0` para todos los ítems. El comentario dice explícitamente: *"Currency conversion (Moneda→UF) requires cross-service call to BcsPlataformaServicios:CalcularParidad. Returns zero as placeholder until integration is wired."*

---

### CalculaCustodiaRescate
**FQN legado**: `cl.bcs.fondomutuo.CalculaCustodiaRescate`  
**Archivo modernizado**: `service/javacall/CalculaCustodiaRescateService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado calcula la custodia disponible para rescate considerando solicitudes en tránsito y movimientos pendientes. El modernizado solo ejecuta el SQL de carteras y retorna filas crudas sin el cálculo de disponibilidad.

---

### ProcesarCustodiaFondoMutuo
**FQN legado**: `cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo`  
**Archivo modernizado**: `service/javacall/ProcesarCustodiaFondoMutuoService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado aplica movimientos de custodia (ingresos/retiros de cuotas) post-certificación. El modernizado solo consulta las carteras y las retiradas sin aplicar movimientos.

---

### ProcesarReinversionRescate
**FQN legado**: `cl.bcs.fondomutuo.ProcesarReinversionRescate`  
**Archivo modernizado**: `service/javacall/ProcesarReinversionRescateService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El modernizado solo ejecuta 6 queries de consulta y retorna datos crudos. El legado procesa reinversiones generando nuevas solicitudes de aporte en el fondo destino.

---

### ActualizarCarterasFondosMutuos
**FQN legado**: `cl.bcs.fondomutuo.ActualizarCarterasFondosMutuos`  
**Archivo modernizado**: `service/javacall/ActualizarCarterasFondosMutuosService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado actualiza las carteras valorizadas de fondos mutuos llamando a `EntregarPreciosTasas`. El modernizado solo consulta las carteras sin actualizarlas.

---

### CrearTraspaso
**FQN legado**: `cl.bcs.fondomutuo.CrearTraspaso`  
**Archivo modernizado**: `service/javacall/CrearTraspasoService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado: llama a `RevisarRequisitosBasicos`, valida el estado del portafolio, verifica flujo de custodia, inserta el traspaso con sus detalles origen/destino correctamente populados. El modernizado: inserta directamente con parámetros simplificados (no incluye IdCliente, IdPortafolio, IdMotivoTraspaso, etc.) desde los datos de entrada.

---

### ConsultarFondo
**FQN legado**: `cl.bcs.fondomutuo.ConsultarFondo`  
**Archivo modernizado**: `service/javacall/ConsultarFondoService.java`  
**Estado**: ❌ STUB  
**Riesgo**: MEDIO  
**Divergencia**: El legado enriquece los resultados con `ValorCuota`, `ValorDolarAporte`, `ValorDolarRescate` obtenidos de `EntregarPreciosTasas`. El modernizado retorna solo los datos de BD sin valor cuota actual.

---

### ManejadorSolicitudAporteRescate
**FQN legado**: `cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate`  
**Archivo modernizado**: `service/javacall/ManejadorSolicitudAporteRescateService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El orquestador moderno delega correctamente a los sub-servicios (por tipo de solicitud), pero todos esos sub-servicios son también stubs, por lo que el comportamiento final es incompleto.

---

### ServicioCumpleRequisitosCrearSolicitudAporteRescate
**FQN legado**: `cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate`  
**Archivo modernizado**: `service/javacall/ServicioCumpleRequisitosCrearSolicitudAporteRescateService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado valida ~15 condiciones de negocio (estado de fondo, montos mínimo/máximo, régimen, cuenta inversión, mandato, prospecto, etc.) y devuelve errores específicos. El modernizado ejecuta 2 queries y retorna filas sin validar ninguna condición.

---

### GenerarArchivoSolicitudAporteRescate
**FQN legado**: `cl.bcs.fondomutuo.GenerarArchivoSolicitudAporteRescate`  
**Archivo modernizado**: `service/javacall/GenerarArchivoSolicitudAporteRescateService.java`  
**Estado**: ❌ STUB  
**Riesgo**: MEDIO  
**Divergencia**: El legado genera un archivo físico con solicitudes para envío a AGF. El modernizado retorna la configuración cruda desde BD sin generar ningún archivo.

---

### CalcularFechaSolicitudAporteRescateJava
**FQN legado**: `cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescateJava`  
**Archivo modernizado**: `service/javacall/CalcularFechaSolicitudAporteRescateJavaService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Divergencia**: El legado calcula fechas de solicitud en Java puro (sin BD). El modernizado es un stub que solo retorna datos de series y feriados sin el cálculo de avance de días.

---

### ConsultarFechaSolicitudAporteRescate
**FQN legado**: `cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate`  
**Archivo modernizado**: `service/javacall/ConsultarFechaSolicitudAporteRescateService.java`  
**Estado**: ❌ STUB  
**Riesgo**: BAJO  
**Divergencia**: El modernizado ejecuta el SQL correcto pero no aplica lógica de cálculo. Para consulta pura de configuración es parcialmente aceptable.

---

### ConsultarCustodiaPortafolioFFMMWS / ConsultarCustodiaPortafolioFFMMWSS / ConsultarCustodiaPortafolioFondoMutuo / ConsultarCustodiaPortafolioFondoMutuoWS
**Estado**: ❌ STUB (todos)  
**Riesgo**: MEDIO  
**Nota**: Variantes del servicio de consulta de custodia para distintos canales. Todos siguen el mismo patrón stub.

---

### BuscarCantidadesPorRegimen
**FQN legado**: `cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen` (en utils/)  
**Archivo modernizado**: `service/javacall/BuscarCantidadesPorRegimenService.java`  
**Estado**: ❓ SIN_LEGADO (como JavaCall independiente)  
**Riesgo**: MEDIO  
**Nota**: En el legado existe como clase utilitaria que usa `ConsultarFlujoCustodia` (llamada cross-service a custodia). El modernizado lo implementa con 2 queries SQL directas. La funcionalidad es similar pero la fuente de datos difiere.

---

## Servicios Ausentes en el Modernizado

Los siguientes servicios legados **no tienen equivalente** en el modernizado:

| Servicio Legado | Funcionalidad | Riesgo |
|----------------|---------------|--------|
| `AutorizacionModificacionFechaGestionOperaciones` | Autoriza modificación de fecha de gestión de operaciones | ALTO |
| `ConfirmarEjecutarFechaGestionOperaciones` | Confirma y ejecuta el cambio de fecha de gestión | ALTO |
| `ObtenerSolicitudesDeCambiosFechaGestionOperacionesMasivo` | Obtiene solicitudes de cambio de fecha pendientes | MEDIO |
| `TrxFileManager` | Gestión de archivos de transacciones | MEDIO |

---

## Servicios No Auditados (95 de 132)

Los siguientes servicios del modernizado **no fueron auditados** individualmente, pero por el patrón universal observado (todos tienen <70 líneas de código, ninguna lógica de negocio, solo queries JDBC) se clasifican provisionalmente como ❌ STUB:

**Crear/Modificar** (alto riesgo estimado):
- CrearAporteCSCService, CrearAporteRescateFusionService, CrearAporteSAAPVService
- CrearComisionService, CrearIngresoCSCService, CrearIngresoCSCManejadorService
- CrearIngresoRetiroGarantiasFondoMutuoService, CrearIngresoRetiroTituloFondoMutuoService
- CrearSolicitudAGFService, CrearSolicitudAporteOvernightService
- CrearSolicitudAportePorDividendosService, CrearSolicitudAportePorDividendosCFIService
- CrearSolicitudAporteReinversionService, CrearSolicitudAporteRescateProgramadoService
- CrearSolicitudCambioSerieService, CrearSolicitudReinversionPorClienteService
- CrearSolicitudRescateComisionNNService, CrearSolicitudRescateNormalPorClienteService
- CrearSolicitudRescatePorComisionService, CrearSolicitudRescateReinversion1NService
- CrearSolicitudRescateReinversionService, CrearSolicitudRescateTransferenciaService
- CrearSolicitudRescateTraspasoService, CrearSolicitudRescateTraspasoPortafolioService
- CrearTramoComisionService, ModificarComisionService
- ModificarComisionSolicitudPorPerfeccionamientoService, ModificarCSCService, ModificarCSCManejadorService
- ModificarMontoNumeroCuotasSolicitudService, ModificarTramoComisionService, ModificarTraspasoService

**Procesar/Ejecutar** (alto riesgo estimado):
- ProcesarArchivoCuadraturaInternacionalService, ProcesarArchivoCuadraturaNacionalService
- ProcesarCargaSolicitudesAporteRescateService, ProcesarComisionesNNService
- ProcesarIngresoEgresoTesoreriaService, ProcesarIngresoOperacionSpotService

**Anular/Revocar/Confirmar** (alto riesgo estimado):
- AnulacionContratoCSCService, AnulacionSolicitudAporteRescateFolioContratoService
- AnulacionTraspasoService, RevocacionContratoCSCService, RevocacionContratoCSCManejadorService
- ConfirmarTraspasoService, ConfirmarModificarFechaGestionOperacionesService

**Calcular adicionales** (alto riesgo estimado):
- ActualizarCostoUFService, ActualizarCostoUFCargaService, ActualizarCostoUF_MLTService
- ActualizaCarteraValorizadaFFMMService
- CuadrarOmnibusDetalleService, CuadraturaCierreDiarioService

**Consultar** (riesgo bajo-medio estimado):
- ConsultarComisionesNNFondoMutuoService, ConsultarCustodiaDisponibleReinversion1NService
- ConsultarDetalleSolicitudService, ConsultarFlujoCustodiaFondoMutuoService
- ConsultarFondoMutuoReinversionService, ConsultarInstrumentosFondosMutuosService
- ConsultarSolicitudesCertificacionMasivaService, ConsultarValorCuotaHistoricoService
- ConsultarValorDolarFondoMutuoService, ObtenerPerfilFondoMutuoService (auditado = EQUIVALENTE)

---

## Conclusiones y Recomendaciones

### Estado actual
El modernizado de `BcsFondoMutuoServicios` **no puede operar en producción** en su estado actual. Los 132 servicios javacall son mayoritariamente scaffolding con SQL correcto pero sin implementación.

### Prioridades de implementación (por criticidad de negocio)

**PRIORIDAD 1 — Bloquea operaciones de solicitud (implementar antes de ir a producción)**:
1. `CalcularFechaSolicitudAporteRescate` — sin fechas correctas ninguna solicitud puede crearse
2. `CalcularMontoNumeroCuota` — sin cálculo de cuotas/montos no hay solicitudes válidas
3. `CalcularComisiones` — comisiones incorrectas afectan todos los aportes
4. `CrearSolicitudAporteRescateNormal` — flujo principal de creación
5. `CertificarSolicitudAporteRescate` — sin certificación las solicitudes quedan en limbo
6. `AnulacionSolicitudAporteRescate` — sin reversión correcta se pierden movimientos de custodia

**PRIORIDAD 2 — Bloquea integridad financiera**:
7. `LiquidacionSolicitudAporteRescate` — sin liquidación real no se aplican movimientos
8. `ProcesarCustodiaFondoMutuo` — sin custodia real los saldos serán incorrectos
9. `CalcularCostoUFAportes` — reportes de costos en UF serán cero (en producción: incorrecto)
10. `ValidaMontoInversion` — con lógica incorrecta se pueden aceptar operaciones fuera de límite

**PRIORIDAD 3 — Integración cross-service (CalcularParidad)**:
- Todos los servicios que requieren conversión de moneda dependen de que `BcsPlataformaServicios:CalcularParidad` esté integrado. Sin esto, fondos en moneda extranjera tendrán cálculos incorrectos.
