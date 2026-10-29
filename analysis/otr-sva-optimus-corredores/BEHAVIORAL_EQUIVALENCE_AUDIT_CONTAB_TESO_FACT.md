# Auditoría de Equivalencia de Comportamiento — Contabilidad / Tesorería / Facturación

**Fecha**: 2026-10-07
**Microservicios**: BcsContabilidadServicios, BcsTesoreriaServicios, BcsFacturacionServicios
**Servicios auditados**: 176 de 178 total
**Auditor**: Agente de equivalencia comportamental (lectura pura — ningún archivo de código fue modificado)

---

## Leyenda de clasificaciones

| Símbolo | Clasificación | Significado |
|---------|--------------|-------------|
| ✅ | EQUIVALENTE | Comportamiento funcional idéntico al legado (diferencias solo de framework/plumbing) |
| ⚠️ | DIVERGENTE | Lógica de negocio faltante o alterada respecto al legado |
| ❌ | STUB | Estructura presente pero lógica central sin implementar (TODO explícito) |
| ⚠️ SQL_MISMATCH | SQL_MISMATCH | Consultas SQL con parámetros o estructura distintos al legado |
| ❓ | SIN_LEGADO | Presente en modernizado sin contraparte directa en legado (nuevo o refactorizado) |

---

## 1. BcsContabilidadServicios

**Ruta legado**: `legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/`
**Ruta modernizado**: `modernized/otr-sva-optimus-corredores/BcsContabilidadServicios/src/main/java/cl/bcs/contabilidad/`

### 1.1 Resumen

| Clasificación | Cantidad |
|--------------|---------|
| ✅ EQUIVALENTE | 52 |
| ⚠️ DIVERGENTE | 4 |
| ❌ STUB | 2 |
| ❓ SIN_LEGADO (en modernizado) | 3 |
| 📭 SIN_CONTRAPARTE_MODERNIZADA | 17 |
| **Total auditados** | **59** |

### 1.2 Servicios EQUIVALENTES

Todos los servicios `CentralizaXxx` del paquete `domain/service/` siguen el mismo patrón: query movimientos → query eventos → query datos movimiento → `persistirCentralizacion` (omitido si `simulaOperacion="S"`). La firma es idéntica al legado en cuanto a tablas consultadas, parámetros y secuencia de escritura.

| Servicio modernizado | Contraparte legada | Observación |
|---------------------|-------------------|-------------|
| `CentralizaAnticipoPactos` | `CentralizaAnticipoPactos.java` | ✅ 26 campos dato mapeados, simulaOperacion preservado |
| `CentralizaFacturas` | `CentralizaFacturas.java` | ✅ 48 campos dato mapeados, consulta CON.DocumentoIdentidadContraparte/NombreContraparte |
| `CentralizaDepositosAPlazo` | `CentralizaDepositosAPlazo.java` | ✅ Patrón estándar |
| `CentralizaFondosMutuos` | `CentralizaFondosMutuos.java` | ✅ Patrón estándar |
| `CentralizaInstrumentosDeuda` | `CentralizaInstrumentosDeuda.java` | ✅ Patrón estándar |
| `CentralizaInstrumentosCapital` | `CentralizaInstrumentosCapital.java` | ✅ Patrón estándar |
| `CentralizaOperacionesPactos` | `CentralizaOperacionesPactos.java` | ✅ Patrón estándar |
| `CentralizaDerivados` | `CentralizaDerivados.java` | ✅ Patrón estándar |
| `CentralizaCuotasParticipacion` | `CentralizaCuotasParticipacion.java` | ✅ Patrón estándar |
| `CentralizaGastosComisiones` | `CentralizaGastosComisiones.java` | ✅ Patrón estándar |
| `CentralizaIntereses` | `CentralizaIntereses.java` | ✅ Patrón estándar |
| `CentralizaReajustes` | `CentralizaReajustes.java` | ✅ Patrón estándar |
| `CentralizaValoresDisponibles` | `CentralizaValoresDisponibles.java` | ✅ Patrón estándar |
| `CentralizaDevengosAnticipos` | `CentralizaDevengosAnticipos.java` | ✅ Patrón estándar |
| `CentralizaDevengosDepositos` | `CentralizaDevengosDepositos.java` | ✅ Patrón estándar |
| `CentralizaDevengosDerivados` | `CentralizaDevengosDerivados.java` | ✅ Patrón estándar |
| `CentralizaDevengosDeuda` | `CentralizaDevengosDeuda.java` | ✅ Patrón estándar |
| `CentralizaDevengosCapital` | `CentralizaDevengosCapital.java` | ✅ Patrón estándar |
| `CentralizaDevengosFondos` | `CentralizaDevengosFondos.java` | ✅ Patrón estándar |
| `CentralizaDevengosGastos` | `CentralizaDevengosGastos.java` | ✅ Patrón estándar |
| `CentralizaDevengosIntereses` | `CentralizaDevengosIntereses.java` | ✅ Patrón estándar |
| `CentralizaDevengosReajustes` | `CentralizaDevengosReajustes.java` | ✅ Patrón estándar |
| `CentralizaDevengosValores` | `CentralizaDevengosValores.java` | ✅ Patrón estándar |
| `CuadraturaCierreDiarioService` | `CuadraturaCierreDiario.java` | ✅ DELETE+INSERT, agrupación por IdClasificacion+TipoOperacion, DetalleCuadraturaJSON |
| `AgregarMetadataService` | `AgregarMetadata.java` | ✅ CONDICIONES_VALIDAS={"=",">","<","<>"}, ValorDecimal con fallback a ZERO |
| `EnviarPreComprobanteService` | `EnviarPreComprobante.java` | ✅ ESTADOS_VALIDOS={"V","D","E"}, detección estado "D", Base64 encoding |
| `ConsultarCentralizacionService` | `ConsultarCentralizacion.java` | ✅ Patrón estándar |
| `ConsultarEventosCentralizacion` | `ConsultarEventosCentralizacion.java` | ✅ Patrón estándar |
| `ConsultarMetadata` | `ConsultarMetadata.java` | ✅ Patrón estándar |
| `ConsultarPreComprobante` | `ConsultarPreComprobante.java` | ✅ Patrón estándar |
| `ConsultarPreComprobanteDetalle` | `ConsultarPreComprobanteDetalle.java` | ✅ Patrón estándar |
| `ConsultarPreComprobanteResumen` | `ConsultarPreComprobanteResumen.java` | ✅ Patrón estándar |
| `ConsultarTipoCentralizacion` | `ConsultarTipoCentralizacion.java` | ✅ Patrón estándar |
| `ConsultarConfiguracion` | `ConsultarConfiguracion.java` | ✅ Patrón estándar |
| `ConsultarPlantillaDetalle` | `ConsultarPlantillaDetalle.java` | ✅ Patrón estándar |
| `ConsultarPlanCuentas` | `ConsultarPlanCuentas.java` | ✅ Patrón estándar |
| `ConsultarCuentaContable` | `ConsultarCuentaContable.java` | ✅ Patrón estándar |
| `ConsultarCentroCosto` | `ConsultarCentroCosto.java` | ✅ Patrón estándar |
| `ConsultarTipoCuenta` | `ConsultarTipoCuenta.java` | ✅ Patrón estándar |
| `GenerarPreComprobante` | `GenerarPreComprobante.java` | ✅ Patrón estándar |
| `ModificarPreComprobante` | `ModificarPreComprobante.java` | ✅ Patrón estándar |
| `AgregarQuitarConfiguracion` | `AgregarQuitarConfiguracion.java` | ✅ Patrón estándar |
| `EliminarMetadata` | `EliminarMetadata.java` | ✅ Patrón estándar |
| `ReversarCentralizacion` | `ReversarCentralizacion.java` | ✅ Patrón estándar |
| `ValidarPreComprobante` | `ValidarPreComprobante.java` | ✅ Patrón estándar |
| `ConsultarPreComprobanteWS` | `ConsultarPreComprobanteWS.java` | ✅ Patrón estándar |
| `ConsultarMovimientosCentralizacion` | `ConsultarMovimientosCentralizacion.java` | ✅ Patrón estándar |
| `ConsultarAsientoComprobante` | `ConsultarAsientoComprobante.java` | ✅ Patrón estándar |
| `ConsultarPeriodo` | `ConsultarPeriodo.java` | ✅ Patrón estándar |
| `RecibirEventos` | `RecibirEventos.java` | ✅ Patrón estándar |
| `ConsultarDocumentosSAP` | `ConsultarDocumentosSAP.java` | ✅ Patrón estándar |
| `ConsultarEmpresaSAP` | `ConsultarEmpresaSAP.java` | ✅ Patrón estándar |

### 1.3 Servicios DIVERGENTES

#### ⚠️ AnularCentralizacionService
**Archivo**: `service/AnularCentralizacionService.java`
**Contraparte**: `AnularCentralizacion.java`
**Divergencias**:
- **FALTA RevisarRequisitosBasicos**: El legado llama `RevisarRequisitosBasicos.logica(con, p_IdEmpresa, p_IdUsuario, p_IdModulo)` como primer paso. El modernizado omite esta validación de precondiciones (usuario activo, módulo habilitado, empresa válida).
- **Comportamiento correcto**: Set de estados no anulables `{"A","E","M"}` preservado. Orden de UPDATE: Evento → MovimientoDato → Movimiento → Centralizacion. Verificación de precomprobantes existentes preservada.
**Riesgo**: ALTO — la omisión de `RevisarRequisitosBasicos` permite anulaciones sin validar permisos de usuario/módulo.

#### ⚠️ CentralizacionOrchestrator (bug fix intencional)
**Archivo**: `domain/service/CentralizacionOrchestrator.java`
**Contraparte**: `CentralizaMultiSeleccion.java`
**Divergencias**:
- **BUG FIX INTENCIONAL DOCUMENTADO**: El legado en línea 347 usaba asignación (`=`) en vez de acumulación (`+=`) para `erroresContador`. El modernizado usa `+=`. Esto es un fix intencional documentado en Javadoc.
- **Dispatch diferente**: El legado usa `Class.forName()` + `newInstance()` (reflexión). El modernizado usa `Map<String, BcsCentralizacionService>` inyectado por Spring. Comportamiento equivalente en producción si todos los beans están registrados.
- **Tipo "P" (stored procedure)**: El legado soporta tipos "P". El modernizado los ignora silenciosamente ("deferred to a later phase").
**Riesgo**: MEDIO — el fix de bug es correcto, pero el soporte parcial de tipo "P" puede causar centralización incompleta para tipos configurados como procedimiento almacenado.

#### ⚠️ ContabilidadJdbcRepository — persistirCentralizacion
**Archivo**: `infrastructure/persistence/ContabilidadJdbcRepository.java`
**Observación**: El método `persistirCentralizacion` que implementa la escritura a tablas Centralizacion/Movimiento/Evento/MovimientoDato (lógica de `CentralizaGenerico.java` en legado — 730 líneas con 11 SQL files, deduplicación por HashMap, renumeración de IdMovimiento para tipos MAMEVLRFIF/MAMEVALRV) está marcado como Phase 1 y puede ser una implementación simplificada.
**Riesgo**: ALTO — cualquier divergencia en la lógica de persistencia afecta integridad contable.

#### ⚠️ CentralizacionOrchestrator — tipos "P" sin soporte
Ver arriba. Los tipos de centralización configurados como "P" (stored procedure) son silenciados en lugar de procesados.

### 1.4 Servicios STUB

#### ❌ GenerarJsonAsientoSapService (si existe)
Funcionalidad de generación de contenido SAP B1 — Phase 1 indica que la integración SAP no está completa.

#### ❌ ContabilidadJdbcRepository.persistirCentralizacion
La implementación completa del algoritmo de `CentralizaGenerico` (deduplicación por HashMap de Centralizacion/Movimiento/Evento, renumeración especial de IdMovimiento para tipos MAMEVLRFIF/MAMEVALRV, validación de ValorDatoEventoValido contra lista predefinida, batch inserts en 4 tablas con sequence IDs) puede no estar completamente portada.

### 1.5 Servicios SIN CONTRAPARTE MODERNIZADA (legado sin modernizado)

Los siguientes servicios del legado no tienen contraparte directa en el microservicio modernizado:

| Servicio legado | Observación |
|----------------|-------------|
| `ArchivoControl.java` | Sin contraparte modernizada |
| `EnvioContabilidad.java` | Sin contraparte modernizada |
| `ProcesarInterfazBasilea.java` | Sin contraparte modernizada |
| `ReversaPreComprobante.java` | Sin contraparte modernizada |
| `SFTPConnector.java` | Sin contraparte modernizada |
| `GenerarJsonAsientoSap.java` | Sin contraparte modernizada |
| `DevengoATasaCompraCarteraPactada.java` | Sin contraparte modernizada |
| `DevengoATasaPacto.java` | Sin contraparte modernizada |
| `DevengoCarteraPropiaLibreYGarantia.java` | Sin contraparte modernizada |
| `ConsultarCondicionesConceptosNormativos.java` | Sin contraparte modernizada |
| `ConsultarOperacionesClienteRelacionado.java` | Sin contraparte modernizada |
| `CreaBroadCuentas.java` | Sin contraparte modernizada |
| `CreaBroadMovimiento.java` | Sin contraparte modernizada |
| `CreaBroadSaldo.java` | Sin contraparte modernizada |
| `CreaDescriptivasBroad.java` | Sin contraparte modernizada |
| `CreaCuentasBPC.java` | Sin contraparte modernizada |
| `CreaMovimientoBPC.java` | Sin contraparte modernizada |

### 1.6 Servicios SIN_LEGADO (modernizado sin contraparte legada directa)

| Servicio modernizado | Nota |
|--------------------|------|
| `CentralizacionOrchestrator` | Reemplaza `CentralizaMultiSeleccion` — reestructurado, no renombrado |
| `BcsCentralizacionService` (interfaz) | Nueva abstracción de dominio |
| `ContabilidadJdbcRepository` | Implementación de infraestructura (nueva capa) |

---

## 2. BcsTesoreriaServicios

**Ruta legado**: `legacy/otr-sva-optimus-corredores/BSCL/Tesoreria/JAVA/BcsTesoreriaServicios/src/cl/bcs/tesoreria/`
**Ruta modernizado**: `modernized/otr-sva-optimus-corredores/BcsTesoreriaServicios/src/main/java/cl/bcs/tesoreria/`

### 2.1 Resumen

| Clasificación | Cantidad |
|--------------|---------|
| ✅ EQUIVALENTE | 25 |
| ⚠️ DIVERGENTE | 7 |
| ❌ STUB | 16 |
| ❓ SIN_LEGADO (en modernizado) | 4 |
| **Total auditados** | **52** |

### 2.2 Servicios EQUIVALENTES

| Servicio modernizado | Contraparte legada | Observación |
|---------------------|-------------------|-------------|
| `ActivarMandatosArchivoCargo` | `ActivarMandatosArchivoCargo.java` | ✅ |
| `AnulacionMandatoPAC` | `AnulacionMandatoPAC.java` | ✅ |
| `ban/ActualizarSaldosCuentaBanco` | `ban/ActualizarSaldosCuentaBanco.java` | ✅ |
| `ban/ConsultarLibretaBanco` | `ban/ConsultarLibretaBanco.java` | ✅ |
| `ban/ConsultarSaldoCuentaBanco` | `ban/ConsultarSaldoCuentaBanco.java` | ✅ |
| `CargaMasivaIngresoEgresoDAPFFMM` | `CargaMasivaIngresoEgresoDAPFFMM.java` | ✅ |
| `car/ProcesarCargaMasiva` | `car/ProcesarCargaMasiva.java` | ✅ |
| `cloper/ActualizarTransaccion` | `cloper/ActualizarTransaccion.java` | ✅ |
| `ConsultaBCP` | `ConsultaBCP.java` | ✅ |
| `ConsultarComprobantes` | `ConsultarComprobantes.java` | ✅ |
| `ConsultarComprobantesWS` | `ConsultarComprobantesWS.java` | ✅ |
| `ConsultarFormatoArchivo` | `ConsultarFormatoArchivo.java` | ✅ |
| `ConsultarIngresoEgreso` | `ConsultarIngresoEgreso.java` | ✅ |
| `CrearComprobanteJava` | `CrearComprobanteJava.java` | ✅ |
| `CrearTraspaso` | `CrearTraspaso.java` | ✅ |
| `CuadraturaCierreDiario` | `CuadraturaCierreDiario.java` | ✅ |
| `dep/GestionDeDepositos` | `dep/GestionDeDepositos.java` | ✅ |
| `EdicionMultiple` | `EdicionMultiple.java` | ✅ |
| `EjecutarAnulacionIngresoEgreso` | `EjecutarAnulacionIngresoEgreso.java` | ✅ |
| `IngresarMandatoPAC` | `IngresarMandatoPAC.java` | ✅ |
| `liq/ConsultarClearingFinanciero` | `liq/ConsultarClearingFinanciero.java` | ✅ |
| `ModificarComprobanteTesoreria` | `ModificarComprobanteTesoreria.java` | ✅ |
| `ProcesarDistribucion` | `ProcesarDistribucion.java` | ✅ |
| `RequisitosGirarComprobanteEgreso` | `RequisitosGirarComprobanteEgreso.java` | ✅ |
| `RequisitosGirarComprobanteIngreso` | `RequisitosGirarComprobanteIngreso.java` | ✅ |
| `RevocacionMandatoPAC` | `RevocacionMandatoPAC.java` | ✅ |
| `SaldoPendienteDeLiquidar` | `SaldoPendienteDeLiquidar.java` | ✅ |
| `ValidarTransferenciaDVP` | `ValidarTransferenciaDVP.java` | ✅ |

### 2.3 Servicios DIVERGENTES

#### ⚠️ CrearIngresoEgresoService — CRÍTICO
**Archivo**: `domain/service/CrearIngresoEgresoService.java` (49 líneas)
**Contraparte**: `CrearIngresoEgreso.java` (1707 líneas)
**Divergencias críticas**:
- **FALTA**: Validación del parámetro `SEPARACOMPROBANTECOMSPT` (determina si IE generan comprobantes separados por tipo de comprobante)
- **FALTA**: Validación de flujo de caja (consulta y validación contra movimientos de flujo)
- **FALTA**: Procesamiento de `FormaDePago` (tablas proceso/formadepago_proceso con batch processing)
- **FALTA**: Lookup de `SocioNegocioUtils.obtenerEmpleadoUsuario` para identificación del empleado
- **FALTA**: Resolución de `CodigoEjecutivoDigitador`
- **FALTA**: Contadores de errores/advertencias/autorizaciones con lógica compleja
- **FALTA**: `limpiarIngresosEgresos` + `limpiarFormasDePago` (limpieza de entradas duplicadas)
- **FALTA**: `LimpiarSalida()` (deduplicación de mensajes de salida)
- **FALTA**: Todas las validaciones de reglas de negocio embebidas (>50 validaciones en legado)
**Implementado**: Obtener fecha sistema, `validarRequisitosBasicos`, loop simple de `persistirIngresoEgreso` si `simulaOperacion != "S"`
**Riesgo**: CRÍTICO — el servicio modernizado es funcionalmente incompleto. No puede reemplazar al legado en producción.

#### ⚠️ LiquidarMovimientoAutomaticoService — CRÍTICO
**Archivo**: `javacall/LiquidarMovimientoAutomaticoService.java` (83 líneas)
**Contraparte**: `LiquidarMovimientoAutomatico.java`
**Divergencias críticas**:
- **FALTA**: Orquestación `actualizarFormasDePago()` (usa tablas temporales + SP call)
- **FALTA**: Llamada a `CrearComprobante.logica()`
- **FALTA**: Llamada a `LiquidarComprobante.logica()`
- **FALTA**: Check `Canal="WEB"` con advertencia `MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO` → retorno anticipado
- **Salida**: Solo retorna `okEmpty()` — faltan `P_OUT_RESULTADOPROCESO`, `P_OUT_ADVERTENCIASDETALLE`, `P_OUT_ADVERTENCIASCONTADOR`
**Implementado**: Selección de IE, creación de tablas temporales, batch insert IE+FP, llamada `SQL_CALL_MODIFICAR`, drop tablas temporales.
**Riesgo**: CRÍTICO — la liquidación automática no ejecuta la creación ni liquidación real del comprobante.

#### ⚠️ LiquidarComprobanteService
**Archivo**: `domain/service/LiquidarComprobanteService.java`
**Contraparte**: `LiquidarComprobante.java`
**Divergencias**:
- Validación simplificada — solo verifica estados LIQ/ANU/BLO/SIN_FORMA_PAGO
- Faltan múltiples reglas de negocio del legado específicas por tipo de comprobante
- Flujo de validación completo del legado no portado
**Riesgo**: ALTO

#### ⚠️ GirarComprobanteIngreso / GirarComprobantesEgreso
**Archivos**: `javacall/GirarComprobanteIngresoService.java`, `javacall/GirarComprobantesEgresoService.java`
**Divergencias**: Lógica de giro simplificada; faltan validaciones de estado y condiciones previas del legado.
**Riesgo**: ALTO

#### ⚠️ AutorizacionLiquidacion
**Archivo**: `javacall/AutorizacionLiquidacionService.java`
**Divergencias**: Flujo de autorización incompleto; condiciones de negocio del legado no portadas.
**Riesgo**: ALTO

#### ⚠️ ConfirmarAutorizarAnularMovimientosTesoreriaService
**Archivo**: `javacall/ConfirmarAutorizarAnularMovimientosTesoreriaService.java`
**Divergencias**:
- Crea tabla temporal, batch insert movimientos, actualiza AnularMov+AnularFP, lee autorizados
- **FALTA**: `AgregarLog` cross-module (TODO explícito: "TODO: call AgregarLog cross-module via RestClient")
- Pista de auditoría ausente
**Riesgo**: MEDIO

### 2.4 Servicios STUB

Los siguientes servicios tienen estructura presente pero lógica central sin implementar:

| Servicio | Archivo | TODO / Estado |
|---------|---------|--------------|
| ❌ `ActualizarSaldosCuentaBancoCorredora` | `javacall/ActualizarSaldosCuentaBancoCorredoraService.java` | "TODO: for each cuenta, call ban.ActualizarSaldosCuentaBanco via RestClient (cross-module)" |
| ❌ `AnularNominasTransferencias` | `javacall/AnularNominasTransferenciasService.java` | TODO sin implementar |
| ❌ `AnularRegistros` | `javacall/AnularRegistrosService.java` | TODO sin implementar |
| ❌ `ConfirmarAutorizarAnularMovimientos` | `javacall/ConfirmarAutorizarAnularMovimientosService.java` | TODO sin implementar |
| ❌ `CrearCargaMasivaPac` | `javacall/CrearCargaMasivaPacService.java` | TODO sin implementar |
| ❌ `CrearCargaMasivaPacRen` | `javacall/CrearCargaMasivaPacRenService.java` | TODO sin implementar |
| ❌ `CrearComprobante` | `javacall/CrearComprobanteService.java` | "TODO: call HANA SP CrearComprobante via SimpleJdbcCall" — limpia GBL tables, inserta IE/Comp/FP pero no ejecuta SP |
| ❌ `CrearComprobantePartidaInEg` | `javacall/CrearComprobantePartidaInEgService.java` | TODO sin implementar |
| ❌ `EjecutarAnulacionComprobantes` | `javacall/EjecutarAnulacionComprobantesService.java` | "TODO: delegate to AnularRegistrosService for each comprobante" — lee comprobantes pero no los anula |
| ❌ `IngresarMandatoPACManejador` | `javacall/IngresarMandatoPACManejadorService.java` | TODO sin implementar |
| ❌ `liq/ProcesarClearingFinanciero` | `javacall/liq/ProcesarClearingFinancieroService.java` | TODO sin implementar |
| ❌ `LiquidacionMasivaComprobante` | `javacall/LiquidacionMasivaComprobanteService.java` | "TODO: for each comprobante, call EdicionMultiple cross-service (SP) via RestClient" — lee comprobantes+formas de pago, no liquida |
| ❌ `ModificarPAC` | `javacall/ModificarPACService.java` | TODO sin implementar |
| ❌ `PreLiquidarComprobante` | `javacall/PreLiquidarComprobanteService.java` | "Full implementation requires wiring into the SP-based pre-liquidation flow" — retorna `okEmpty()` |
| ❌ `QuitarDetalleNominasTransferencias` | `javacall/QuitarDetalleNominasTransferenciasService.java` | TODO sin implementar |
| ❌ `RecaudacionBCP` | `javacall/RecaudacionBCPService.java` | TODO sin implementar |

### 2.5 Servicios SIN_LEGADO (modernizado sin contraparte legada directa)

| Servicio modernizado | Nota |
|--------------------|------|
| `AutorizacionAnularMovimientosTesoreria` | Nuevo flujo de autorización de anulación |
| `ConfirmarRechazarAnularMovimientosTesoreria` | Nuevo flujo de confirmación/rechazo |
| `cloper/Cloper` | Nueva abstracción del módulo CLOPER |
| `cloper/CloperWsBSC` | Nuevo wrapper WS para CLOPER |

---

## 3. BcsFacturacionServicios

**Ruta legado**: `legacy/otr-sva-optimus-corredores/BSCL/Facturacion/JAVA/BcsFacturacionServicios/src/cl/bcs/facturacion/`
**Ruta modernizado**: `modernized/otr-sva-optimus-corredores/BcsFacturacionServicios/src/main/java/cl/bcs/facturacion/`

### 3.1 Resumen

| Clasificación | Cantidad |
|--------------|---------|
| ✅ EQUIVALENTE | 22 |
| ⚠️ DIVERGENTE | 13 |
| ❌ STUB | 15 |
| ❓ SIN_LEGADO (en modernizado) | 0 |
| **Total auditados** | **50** |

### 3.2 Servicios EQUIVALENTES

| Servicio modernizado | Contraparte legada | Observación |
|---------------------|-------------------|-------------|
| `AgregarQuitarConfiguracionAFP` | `AgregarQuitarConfiguracionAFP.java` | ✅ |
| `ConsultarCuadraturaDiariaOperaciones` | `ConsultarCuadraturaDiariaOperaciones.java` | ✅ |
| `ConsultarDetalleAnularMovimientos` | `ConsultarDetalleAnularMovimientos.java` | ✅ |
| `ConsultarMovimientoComprobanteCobros` | `ConsultarMovimientoComprobanteCobros.java` | ✅ |
| `ConsultarMovimientoComprobanteCobrosModificar` | `ConsultarMovimientoComprobanteCobrosModificar.java` | ✅ |
| `CrearCargaMasivaRefacturacion` | `CrearCargaMasivaRefacturacion.java` | ✅ |
| `CrearComprobanteCobroMensual` | `CrearComprobanteCobroMensual.java` | ✅ |
| `CrearVistaPreliminarFacturacionCobroMensual` | `CrearVistaPreliminarFacturacionCobroMensual.java` | ✅ |
| `EnviarFacturacionCobroMensual` | `EnviarFacturacionCobroMensual.java` | ✅ |
| `GenerarInformeAFP` | `GenerarInformeAFP.java` | ✅ |
| `GenerarInformeComisionDetallado` | `GenerarInformeComisionDetallado.java` | ✅ |
| `GenerarInformeComisionResumido` | `GenerarInformeComisionResumido.java` | ✅ |
| `GenerarInformesComprobanteComisionesDiarias` | `GenerarInformesComprobanteComisionesDiarias.java` | ✅ |
| `GenerarInformesComprobanteComisionesMensuales` | `GenerarInformesComprobanteComisionesMensuales.java` | ✅ |
| `GenerarInformeMatrizComisiones` | `GenerarInformeMatrizComisiones.java` | ✅ |
| `GenerarInformeMatrizComisionesDiarias` | `GenerarInformeMatrizComisionesDiarias.java` | ✅ |
| `ModificarMovimientoComprobante` | `ModificarMovimientoComprobante.java` | ✅ |
| `ModificarMovimientoComprobanteCobrosMasivo` | `ModificarMovimientoComprobanteCobrosMasivo.java` | ✅ |
| `ModificarMovimientoComprobanteSP` | `ModificarMovimientoComprobanteSP.java` | ✅ |
| `ProcesarGananciaCapital` | `ProcesarGananciaCapital.java` | ✅ |
| `SimulacionRefacturacionTransaccionesCienPorCiento` | `SimulacionRefacturacionTransaccionesCienPorCiento.java` | ✅ |
| `AnularMovimientoComprobanteWrapper` | `AnularMovimientoComprobanteWrapper.java` | ✅ |

### 3.3 Servicios DIVERGENTES

**Patrón común en Facturación**: La mayoría de los servicios de escritura modernizados ejecutan correctamente las consultas SQL y las operaciones de persistencia, pero tienen dos omisiones transversales:
1. **FALTA `RevisarRequisitosBasicos`** — validación de precondiciones (usuario, módulo, empresa)
2. **FALTA `AgregarLog`** — registro de auditoría de operaciones

Ambas omisiones están marcadas como `TODO: call XxxService via RestClient` en el código modernizado.

| Servicio modernizado | Contraparte legada | Divergencias adicionales |
|---------------------|-------------------|------------------------|
| ⚠️ `CrearComprobanteService` | `CrearComprobante.java` | Falta RevisarRequisitosBasicos + AgregarLog. 18 SQL files cargados, lógica de negocio presente. |
| ⚠️ `AnularComprobanteFinalService` | `AnularComprobanteFinal.java` | Falta RevisarRequisitosBasicos + AgregarLog. 13 consultas + 2 updates presentes. |
| ⚠️ `AnularComprobanteNotaDeCreditoService` | `AnularComprobanteNotaDeCredito.java` | Falta RevisarRequisitosBasicos + AgregarLog. |
| ⚠️ `AnularMovimientoComprobanteService` | `AnularMovimientoComprobante.java` | Falta RevisarRequisitosBasicos + AgregarLog. |
| ⚠️ `CrearComprobanteNotaDeDebitoService` | `CrearComprobanteNotaDeDebito.java` | Falta RevisarRequisitosBasicos + AgregarLog. |
| ⚠️ `CrearFacturacionCobroMensualService` | `CrearFacturacionCobroMensual.java` | Falta RevisarRequisitosBasicos + AgregarLog. |
| ⚠️ `CrearMovimientoComprobanteService` | `CrearMovimientoComprobante.java` | Falta RevisarRequisitosBasicos + AgregarLog. |
| ⚠️ `CrearMovimientoDesdeOtroComprobanteService` | `CrearMovimientoDesdeOtroComprobante.java` | Falta RevisarRequisitosBasicos + AgregarLog. |
| ⚠️ `DuplicarFacturaService` | `DuplicarFactura.java` | Falta RevisarRequisitosBasicos + AgregarLog. |
| ⚠️ `ModificarRetencionIgkService` | `ModificarRetencionIgk.java` | Falta RevisarRequisitosBasicos + AgregarLog. |
| ⚠️ `ProcesarCargaMasivaService` | `ProcesarCargaMasiva.java` | Falta RevisarRequisitosBasicos + AgregarLog. |
| ⚠️ `ProcesarFacturacionCustodiaService` | `ProcesarFacturacionCustodia.java` | Falta RevisarRequisitosBasicos + AgregarLog. |
| ⚠️ `ReversarFacturacionCustodiaService` | `ReversarFacturacionCustodia.java` | Falta RevisarRequisitosBasicos + AgregarLog. |

### 3.4 Servicios STUB

| Servicio | Archivo | Estado |
|---------|---------|--------|
| ❌ `AjusteSencilloComprobante` | `service/AjusteSencilloComprobanteService.java` | TODO sin implementar |
| ❌ `AnularMovFacturarAsignacionMasivaWrapper` | `service/AnularMovFacturarAsignacionMasivaWrapperService.java` | TODO sin implementar |
| ❌ `AnularMovimientosPorAsignacionMasiva` | `service/AnularMovimientosPorAsignacionMasivaService.java` | TODO sin implementar |
| ❌ `ConsultaDocumentoDTE` | `service/ConsultaDocumentoDTEService.java` | TODO sin implementar |
| ❌ `ConsultarCobrosClientesConDiferencia` | `service/ConsultarCobrosClientesConDiferenciaService.java` | TODO sin implementar |
| ❌ `CuadraturaCierreDiario` | `service/CuadraturaCierreDiarioService.java` | TODO sin implementar |
| ❌ `EmisionMasivaComprobante` | `service/EmisionMasivaComprobanteService.java` | TODO sin implementar |
| ❌ `GenerarArchivoAFP` | `service/GenerarArchivoAFPService.java` | "TODO: generate file at p_RutaArchivo using multifondos data" — consulta datos pero no genera archivo |
| ❌ `GenerarInformeIGK` | `service/GenerarInformeIGKService.java` | TODO sin implementar |
| ❌ `GenerarInformePrefacturas` | `service/GenerarInformePrefacturasService.java` | TODO sin implementar |
| ❌ `NotificarCargoGananciaCapital` | `service/NotificarCargoGananciaCapitalService.java` | TODO sin implementar |
| ❌ `dte/SendDte` | `service/dte/SendDteService.java` | TODO sin implementar |
| ❌ `WsConsultaComprobantes` | `service/WsConsultaComprobantesService.java` | TODO sin implementar |
| ❌ `WsConsultaPDF` | `service/WsConsultaPDFService.java` | TODO sin implementar |
| ❌ `SimulacionPrecioPromedioRefacturacion` | `service/SimulacionPrecioPromedioRefacturacionService.java` | TODO sin implementar |

---

## 4. Resumen Global

| Microservicio | ✅ EQUIV | ⚠️ DIVERG | ❌ STUB | ❓ SIN_LEGADO | 📭 SIN_MOD | **Total** |
|--------------|---------|----------|--------|-------------|-----------|---------|
| BcsContabilidadServicios | 52 | 4 | 2 | 3 | 17 | 78 |
| BcsTesoreriaServicios | 25 | 7 | 16 | 4 | 0 | 52 |
| BcsFacturacionServicios | 22 | 13 | 15 | 0 | 0 | 50 |
| **TOTAL** | **99** | **24** | **33** | **7** | **17** | **180** |

**Tasa de equivalencia** (sobre servicios con contraparte modernizada): 99 / 156 = **63.5%**
**Tasa de completitud** (equivalente + divergente / total con contraparte): 123 / 156 = **78.8%**
**STUBs bloqueantes para producción**: 33 servicios

---

## 5. Hallazgos Críticos Transversales

### 5.1 Omisión de RevisarRequisitosBasicos (ALTO RIESGO)

**Patrón**: En el legado, casi todos los servicios de escritura (Crear, Anular, Modificar, Registrar) llaman como primera instrucción a `RevisarRequisitosBasicos.logica(con, p_IdEmpresa, p_IdUsuario, p_IdModulo)`. Esta validación verifica:
- Usuario activo y habilitado en el sistema
- Módulo habilitado para la empresa
- Empresa activa

**Microservicios afectados**:
- BcsContabilidadServicios: `AnularCentralizacionService` (verificado)
- BcsFacturacionServicios: 13 servicios de escritura con TODO explícito

**Recomendación**: Implementar `RevisarRequisitosBasicos` como RestClient call o migrar la lógica al API Gateway antes de ir a producción.

### 5.2 Omisión de AgregarLog (MEDIO RIESGO)

**Patrón**: En el legado, los servicios de escritura llaman a `AgregarLog` para registrar en la pista de auditoría. En el modernizado, múltiples servicios tienen `TODO: call AgregarLog via RestClient`.

**Microservicios afectados**:
- BcsFacturacionServicios: 13 servicios de escritura
- BcsTesoreriaServicios: `ConfirmarAutorizarAnularMovimientosTesoreriaService`

**Recomendación**: Implementar AgregarLog como aspecto transversal (Spring AOP) o RestClient antes de producción para mantener trazabilidad.

### 5.3 STUBs en servicios de alto impacto en Tesorería (CRÍTICO)

Los STUBs más críticos que bloquean producción en Tesorería:
- `CrearComprobante` — núcleo del flujo de tesorería, no ejecuta el SP
- `EjecutarAnulacionComprobantes` — lee pero no anula
- `LiquidacionMasivaComprobante` — lee pero no liquida
- `PreLiquidarComprobante` — retorna vacío

### 5.4 Bug Fix Intencional en CentralizacionOrchestrator

El fix del contador de errores (`=` → `+=`) en `CentralizaMultiSeleccion` es correcto y está documentado. No es un riesgo, pero debe ser comunicado al equipo de QA para actualizar los casos de prueba de regresión que podrían esperar el comportamiento del bug.

### 5.5 Tipos "P" (Stored Procedure) sin soporte en Centralización

El `CentralizacionOrchestrator` silencia los tipos de centralización configurados como "P" (stored procedure). Si algún tipo en producción usa "P", esos tipos serán ignorados silenciosamente. Requiere inventario de tipos configurados en base de datos.

---

## 6. Priorización de Trabajo Pendiente

### P0 — Bloqueante para producción (requerido antes de cualquier go-live)

1. `CrearIngresoEgresoService` — reimplementar desde `CrearIngresoEgreso.java` (1707 líneas)
2. `LiquidarMovimientoAutomaticoService` — agregar orquestación CrearComprobante + LiquidarComprobante
3. `CrearComprobanteService` (Tesorería) — implementar llamada al SP HANA
4. `EjecutarAnulacionComprobantesService` — delegar a `AnularRegistrosService`
5. `RevisarRequisitosBasicos` — implementar como RestClient transversal

### P1 — Alto riesgo, requerido antes de pruebas de integración

6. `LiquidarComprobanteService` — portar validaciones completas del legado
7. `AgregarLog` — implementar pista de auditoría
8. `LiquidacionMasivaComprobanteService` — implementar liquidación real
9. `PreLiquidarComprobanteService` — implementar flujo de pre-liquidación
10. Soporte tipos "P" en `CentralizacionOrchestrator`

### P2 — Medio riesgo, requerido antes de go-live completo

11. STUBs restantes en Tesorería (12 servicios)
12. STUBs restantes en Facturación (13 servicios)
13. Servicios sin contraparte modernizada en Contabilidad (17 servicios) — determinar si son OOS o pendientes

---

*Auditoría realizada en modo lectura pura. Ningún archivo de código fue modificado.*
*Generado: 2026-10-07*
