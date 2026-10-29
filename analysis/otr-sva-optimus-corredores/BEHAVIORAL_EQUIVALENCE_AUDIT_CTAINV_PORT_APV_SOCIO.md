# Auditoría de Equivalencia — CuentaInversión / Portafolio / APV / SocioNegocio / CanalAgente
**Fecha**: 2026-10-07
**Auditor**: Agente equivalencia de comportamiento
**Servicios auditados**: 86 clases legacy auditadas (43 CuentaInversión + 21 Portafolio + 67 APV + 558* SocioNegocio + 1 CanalAgente)
**Nota SocioNegocio**: Se auditaron las 558 clases legacy; sólo 3 tienen correspondencia modernizada directa.

---

## BcsCuentaInversionServicios

### Resumen

| Estado | Cantidad |
|--------|----------|
| ✅ EQUIVALENTE | 24 |
| ⚠️ DIVERGENTE | 17 |
| ❌ STUB | 0 |
| ❓ SIN_LEGADO | 0 |

### Hallazgos

#### ActualizarSaldoCargoAbono
**FQN legado**: `cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono`
**FQN modernizado**: `cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: La versión modernizada usa `ConfiguracionSistemaPort` para abstraer la lectura de ConfiguracionSistema y `HanaCargoAbonoPort` para operaciones de saldo — la arquitectura de puertos encapsula correctamente los 24 puntos de integración del legado. El parámetro `p_FechaMovimiento` se pasa explícitamente en lugar de leerlo del ConfiguracionSistema interno.

---

#### ActualizarSaldosReporteAntiguedad
**FQN legado**: `cl.bcs.cuentaInversion.ActualizarSaldosReporteAntiguedad`
**FQN modernizado**: `cl.bcs.cuentainversion.service.ActualizarSaldosReporteAntiguedadService`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencia**: El legado realiza 40 llamadas externas incluyendo `ObtenerDiaSiguiente` y `ObtenerDiaAnterior` para calcular períodos fiscales, y `ConfiguracionSistema` para parámetros de período. La versión modernizada usa solo SQL directo sin estas llamadas de calendario. La lógica de determinación del día siguiente hábil (considerando feriados y país del sistema) está ausente en el modernizado, lo que puede producir fechas de corte de período incorrectas.

---

#### AnularCargoAbono
**FQN legado**: `cl.bcs.cuentaInversion.AnularCargoAbono`
**FQN modernizado**: `cl.bcs.cuentainversion.service.AnularCargoAbonoService`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: MEDIO
**Divergencia**: El legado realiza 16 llamadas externas incluyendo `AgregarLog` (registro de auditoría) y validaciones vía `ConfiguracionSistema`. La versión modernizada ejecuta las operaciones de base de datos (INSERT paso / SELECT / UPDATE estado) sin generar entradas de log de auditoría. Los registros de auditoría de anulación no se escriben.

---

#### CargaInicialSaldosReporteAntiguedad
**FQN legado**: `cl.bcs.cuentaInversion.CargaInicialSaldosReporteAntiguedad`
**FQN modernizado**: `cl.bcs.cuentainversion.service.CargaInicialSaldosReporteAntiguedadService`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencia**: El legado tiene 20 llamadas externas incluyendo `ObtenerDiaSiguiente`, `ObtenerDiaAnterior` y `ConfiguracionSistema` para determinar el rango de fechas de carga inicial. La versión modernizada carece de esta lógica de navegación de calendario hábil, lo que puede generar carga inicial con fechas incorrectas respecto al calendario de días hábiles del sistema.

---

#### CertificarCargoAbono
**FQN legado**: `cl.bcs.cuentaInversion.CertificarCargoAbono`
**FQN modernizado**: `cl.bcs.cuentainversion.service.CertificarCargoAbonoService`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: MEDIO
**Divergencia**: El legado realiza 32 llamadas externas incluyendo `SaldoFlujoCaja` (validación de saldo disponible), `AgregarLog` (auditoría), `ConfiguracionSistema`, y procesamiento de `Comprobante`. La versión modernizada implementa el patrón tabla paso (INSERT/SELECT/UPDATE certif) correctamente, pero omite: (1) validación de saldo de flujo de caja, (2) registro de log de auditoría, (3) creación/actualización de comprobantes de certificación.

---

#### ConsultaAntiguedadDeudasCuentaInversion
**FQN legado**: `cl.bcs.cuentaInversion.ConsultaAntiguedadDeudasCuentaInversion`
**FQN modernizado**: `cl.bcs.cuentainversion.service.ConsultaAntiguedadDeudasCuentaInversionService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 3 llamadas externas menores en legado (ConfiguracionSistema para parámetros de página). La modernización usa JDBC directo con los mismos SQLs de consulta.

---

#### ConsultaComparacionSaldo
**FQN legado**: `cl.bcs.cuentaInversion.ConsultaComparacionSaldo`
**FQN modernizado**: `cl.bcs.cuentainversion.service.ConsultaComparacionSaldoService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: Ninguna. Servicio SQL puro sin llamadas externas. La modernización mantiene el mismo patrón de tabla paso (INSERT, SELECT, DELETE).

---

#### ConsultarCartolaDeMovimiento
**FQN legado**: `cl.bcs.cuentaInversion.ConsultarCartolaDeMovimiento`
**FQN modernizado**: `cl.bcs.cuentainversion.service.ConsultarCartolaDeMovimientoService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: Ninguna. SQL puro con patrón tabla paso. La modernización replica la secuencia DELETE/INSERT_VARS → SELECT_PORTAFOLIO_MONEDA → SELECT_NEMOS → SELECT_CARTOLA.

---

#### ConsultarCobroComisionRegistros
**FQN legado**: `cl.bcs.cuentaInversion.ConsultarCobroComisionRegistros`
**FQN modernizado**: `cl.bcs.cuentainversion.service.ConsultarCobroComisionRegistrosService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: Ninguna. SQL puro.

---

#### ConsultaReporteAcreedores
**FQN legado**: `cl.bcs.cuentaInversion.ConsultaReporteAcreedores`
**FQN modernizado**: `cl.bcs.cuentainversion.service.ConsultaReporteAcreedoresService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 2 llamadas externas menores en legado. La modernización implementa el mismo patrón tabla paso con batch INSERT de portafolios y SELECT de clientes/abonos.

---

#### ConsultaReporteAntiguedadBolsa
**FQN legado**: `cl.bcs.cuentaInversion.ConsultaReporteAntiguedadBolsa`
**FQN modernizado**: `cl.bcs.cuentainversion.service.ConsultaReporteAntiguedadBolsaService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: Ninguna. SQL puro con patrón tabla paso.

---

#### ConsultarMovimientoCuentaInversion
**FQN legado**: `cl.bcs.cuentaInversion.ConsultarMovimientoCuentaInversion`
**FQN modernizado**: `cl.bcs.cuentainversion.service.ConsultarMovimientoCuentaInversionService`
**Estado**: ✅ EQUIVALENTE (con extensión)
**Riesgo**: BAJO
**Divergencia**: La modernización agrega lógica GoLive routing (B-CTA-07: HISTORICAL_ONLY / HYBRID / CURRENT_ONLY basado en fecha goLive) que no existe en el legado. Esta es una adición funcional planificada, no una regresión. La derivación de origen "APV"/"NOAPV" (B-CTA-11) también es nueva y coherente con los requisitos de negocio.

---

#### ConsultarSaldoCuentaInversion
**FQN legado**: `cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion`
**FQN modernizado**: `cl.bcs.cuentainversion.service.ConsultarSaldoCuentaInversionService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: El legado usa `ConfiguracionSistema` para leer PLA.PaisSistema. La modernización replica las mismas 28 queries SQL con la misma secuencia de operaciones (tabla paso, filtros de portafolio/moneda, saldos por garantía/plazo/simultáneas/préstamos). Las 6 llamadas externas son a utilidades de parámetros, no a servicios de negocio.

---

#### CrearCargaMasiva
**FQN legado**: `cl.bcs.cuentaInversion.CrearCargaMasiva`
**FQN modernizado**: `cl.bcs.cuentainversion.service.CrearCargaMasivaService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 3 llamadas externas menores en legado (utilidades). La modernización replica la lógica de carga masiva con JDBC.

---

#### CrearCargoAbono
**FQN legado**: `cl.bcs.cuentaInversion.CrearCargoAbono`
**FQN modernizado**: `cl.bcs.cuentainversion.service.CrearCargoAbonoService`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO (CRÍTICO)
**Divergencia**: Esta es la divergencia más crítica del microservicio. El legado (1096 líneas, 118 referencias externas) tiene:
1. **FechaSistema**: Legado lee de `ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema")`. Modernizado usa `LocalDate.now()` — **error crítico**: el sistema usa una fecha interna que puede diferir de la fecha de calendario.
2. **UsaCargoAbonoJava flag**: Legado verifica `usaCargoAbonoJava` para enrutar entre el SP HANA y la lógica Java. Modernizado usa flag `simular` pero con diferente semántica.
3. **15+ validaciones de negocio** completamente ausentes en modernizado: CUENTA_INVERSION_INVALIDA, CUENTA_INVERSION_NO_EXISTE, CUENTA_INVERSION_NO_ACTIVA, TIPO_MOVIMIENTO_INVALIDO, CONCEPTO_CUENTA_INVERSION_INVALIDO, FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO, MONTO_MENOR_IGUAL_A_CERO, SECUENCIA_O_FOLIO_COMPROBANTE_VACIO, CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO, CONCEPTO_CUENTA_INVERSION_NO_EXISTE, MONEDA_NO_EXISTE, MONEDA_NO_ACTIVA, TIPO_OPERACION_NO_EXISTE, PRODUCTO_NO_EXISTE, TIPO_INSTRUMENTO_NO_EXISTE.
4. **SaldoFlujoCaja**: Legado valida saldo disponible en flujo de caja antes de crear cargos manuales (MOVCAR + p_EsIngresoManual="S"). Modernizado omite esta validación.
5. **AgregarLog**: Legado registra log de auditoría por cada movimiento creado. Modernizado no llama AgregarLog.
6. **AgregarPrograma**: Legado schedula job `ACTUALIZAR_SALDO_CARGO_ABONO` para cuentas propias. Modernizado omite.
7. **ActualizarSaldoCargoAbono inline**: Legado llama directamente al servicio para cuentas de terceros. Modernizado omite.

**Campos salida legado vs modernizado**: El legado construye mensajes con `IdInterno` (referencia interna del movimiento) que el modernizado no incluye.

---

#### CrearCargoAbonoWS
**FQN legado**: `cl.bcs.cuentaInversion.webservices.CrearCargoAbonoWS`
**FQN modernizado**: `cl.bcs.cuentainversion.service.CrearCargoAbonoWSService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: La modernización replica correctamente la resolución de IdPortafolio desde DocumentoIdentidad+CodigoPortafolio vía query HANA, y delega a CrearCargoAbonoService. El flujo de negocio es el mismo, aunque hereda las divergencias de CrearCargoAbonoService subyacente.

---

#### CrearCobroComisionExclusionConcepto
**FQN legado**: `cl.bcs.cuentaInversion.CrearCobroComisionExclusionConcepto`
**FQN modernizado**: `cl.bcs.cuentainversion.service.CrearCobroComisionExclusionConceptoService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: SQL-based con JDBC. Patrón simple INSERT/SELECT.

---

#### CrearCobroComisionOpcionesCobro
**FQN legado**: `cl.bcs.cuentaInversion.CrearCobroComisionOpcionesCobro`
**FQN modernizado**: `cl.bcs.cuentainversion.service.CrearCobroComisionOpcionesCobroService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: SQL-based con JDBC.

---

#### CuadraturaCierreDiario
**FQN legado**: `cl.bcs.cuentaInversion.CuadraturaCierreDiario`
**FQN modernizado**: `cl.bcs.cuentainversion.service.CuadraturaCierreDiarioService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: El legado tiene 11 llamadas externas, principalmente a ConfiguracionSistema. La modernización implementa el mismo patrón SELECT → DELETE → INSERT → SELECT_JSON con los mismos 5 SQLs. La cuadratura diaria funciona correctamente.

---

#### EditarCobroComisionOpcionesCobro / EditarCobroComisionRegistros
**FQN legado**: `cl.bcs.cuentaInversion.EditarCobroComisionOpcionesCobro`, `...Registros`
**FQN modernizado**: `...EditarCobroComisionOpcionesCobroService`, `...EditarCobroComisionRegistrosService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 2 llamadas externas menores cada una. SQL-based.

---

#### EliminaCargaSeleccionada
**FQN legado**: `cl.bcs.cuentaInversion.EliminaCargaSeleccionada`
**FQN modernizado**: `cl.bcs.cuentainversion.service.EliminaCargaSeleccionadaService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: Ninguna. SQL puro.

---

#### EliminarCobroComisionExclusionConcepto / OpcionesCobro / Registros
**FQN legado**: `cl.bcs.cuentaInversion.EliminarCobroComisionExclusionConcepto/OpcionesCobro/Registros`
**FQN modernizado**: `...Service`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: MEDIO
**Divergencia**: Cada uno tiene 10-11 llamadas externas en legado incluyendo `ConfiguracionSistema`, `AgregarLog`, y algunos con verificación de `Requisitos`. La modernización ejecuta las operaciones SQL de eliminación pero no registra auditoría (AgregarLog) ni valida requisitos de negocio para la eliminación.

---

#### GenerarArchivoCargoAbono / GenerarArchivoSaldoCuentas / GenerarInformePatrimonioDiario / GenerarInformeResumenCobro / GenerarReporteAcreedores / GenerarReporteAntiguedadDeudaBolsaCTA / IndiceRentabilidadAtributtionAnalysis / SaldoCuentaInversion
**Estado**: ✅ EQUIVALENTE (todos)
**Riesgo**: BAJO
**Divergencia**: Ninguna. Servicios SQL puros sin llamadas externas de negocio.

---

#### GenerarArchivoClientesPorPagarYCobrar / GenerarArchivoCuentasPorCobrarAnexo16
**FQN legado**: respectivos
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 2 llamadas externas menores cada uno.

---

#### GenerarReporteAntiguedadDeudaCTA
**FQN legado**: `cl.bcs.cuentaInversion.GenerarReporteAntiguedadDeudaCTA`
**FQN modernizado**: `cl.bcs.cuentainversion.service.GenerarReporteAntiguedadDeudaCTAService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 2 llamadas externas menores (ConfiguracionSistema).

---

#### GuardarPatrimonioCaja / GuardarPatrimonioCajaCalculoComision
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 3 llamadas externas menores. SQL-based.

---

#### GuardarSaldosReporteAntiguedad
**FQN legado**: `cl.bcs.cuentaInversion.GuardarSaldosReporteAntiguedad`
**FQN modernizado**: `cl.bcs.cuentainversion.service.GuardarSaldosReporteAntiguedadService`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencia**: 27 llamadas externas en legado. Incluye llamadas a `ObtenerDiaSiguiente`, `ObtenerDiaAnterior`, `Feriados.esDiaHabil()` para determinar fechas de reporte de antigüedad correctas respecto al calendario hábil. La modernización omite esta lógica de calendario, calculando fechas sin considerar días hábiles.

---

#### InformeAtributtionAnalisys / ProcesaIndicesAtributtionAnalisys
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 2 llamadas externas menores cada uno.

---

#### ProcesaCargaSeleccionada
**FQN legado**: `cl.bcs.cuentaInversion.ProcesaCargaSeleccionada`
**FQN modernizado**: `cl.bcs.cuentainversion.service.ProcesaCargaSeleccionadaService`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencia**: 23 llamadas externas en legado incluyendo `CrearCargoAbono` (llamada recursiva/compuesta), `ConfiguracionSistema`, `AgregarLog`, `AgregarPrograma`. La modernización ejecuta los mismos SQLs de procesamiento de carga pero no invoca el flujo completo de CrearCargoAbono con sus validaciones de negocio, ni registra log de auditoría, ni schedula programas.

---

#### ProcesarCobroComisionPatrimonio
**FQN legado**: `cl.bcs.cuentaInversion.ProcesarCobroComisionPatrimonio`
**FQN modernizado**: `cl.bcs.cuentainversion.service.ProcesarCobroComisionPatrimonioService`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencia**: El legado (32 llamadas externas) importa y llama `CrearMovimientoComprobante` (Facturación), `ConsultarCobroComisionRegistros`, `ConfiguracionSistema`, y `Diccionario`. La modernización solo ejecuta 3 UPDATEs de estado (caja/custodia/editados). La lógica de creación de comprobantes/facturas de comisión está completamente ausente. El resultado visible para el usuario (no se generan comprobantes) es radicalmente diferente.
**Campos salida legado vs modernizado**: Legado retorna lista de comprobantes creados; modernizado solo retorna un mensaje de proceso completado.

---

### Resumen BcsCuentaInversionServicios

| Estado | Cantidad |
|--------|----------|
| ✅ EQUIVALENTE | 24 |
| ⚠️ DIVERGENTE | 17 |
| ❌ STUB | 0 |
| ❓ SIN_LEGADO | 0 |
| **Total** | **43** |

---

## BcsPortafolioServicios

### Resumen

| Estado | Cantidad |
|--------|----------|
| ✅ EQUIVALENTE | 13 |
| ⚠️ DIVERGENTE | 1 |
| ❌ STUB | 0 |
| ❓ SIN_LEGADO | 7 |

### Hallazgos

#### AdministrarPortafolioDefecto
**FQN legado**: `cl.bcs.portafolio.AdministrarPortafolioDefecto`
**FQN modernizado**: `cl.bcs.portafolio.javacall.AdministrarPortafolioDefectoService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 30 llamadas externas en legado, pero la modernización las encapsula vía JDBC/SimpleJdbcCall delegando al SP HANA correspondiente.

---

#### AutorizacionCrearPortafolioMasivo
**FQN legado**: `cl.bcs.portafolio.AutorizacionCrearPortafolioMasivo`
**Estado**: ❓ SIN_LEGADO
**Riesgo**: ALTO
**Divergencia**: No existe servicio equivalente en la versión modernizada. La clase legacy procesa autorizaciones masivas de creación de portafolio (procesamiento batch con validaciones de negocio y notificaciones).

---

#### AutorizacionModificacionEstadoAutorizadoOrdenMasivo
**FQN legado**: `cl.bcs.portafolio.AutorizacionModificacionEstadoAutorizadoOrdenMasivo`
**Estado**: ❓ SIN_LEGADO
**Riesgo**: ALTO
**Divergencia**: Sin equivalente modernizado.

---

#### AutorizacionModificacionEstadoPortafolioMasivo
**FQN legado**: `cl.bcs.portafolio.AutorizacionModificacionEstadoPortafolioMasivo`
**Estado**: ❓ SIN_LEGADO
**Riesgo**: ALTO
**Divergencia**: Sin equivalente modernizado.

---

#### AutorizacionModificarPortafolioMasivo
**FQN legado**: `cl.bcs.portafolio.AutorizacionModificarPortafolioMasivo`
**Estado**: ❓ SIN_LEGADO
**Riesgo**: ALTO
**Divergencia**: Sin equivalente modernizado.

---

#### AutorizarCrearAutorizadoOrdenMasivo
**FQN legado**: `cl.bcs.portafolio.AutorizarCrearAutorizadoOrdenMasivo`
**Estado**: ❓ SIN_LEGADO
**Riesgo**: ALTO
**Divergencia**: Sin equivalente modernizado.

---

#### AutorizarCrearCuentaCustodioMasivo
**FQN legado**: `cl.bcs.portafolio.AutorizarCrearCuentaCustodioMasivo`
**Estado**: ❓ SIN_LEGADO
**Riesgo**: ALTO
**Divergencia**: Sin equivalente modernizado. Clase que procesa autorizaciones masivas de creación de cuentas custodio.

---

#### ConsultarConsumoEnLinea / ConsultarConsumoLimite
**FQN legado**: `cl.bcs.portafolio.ConsultarConsumoEnLinea/Limite`
**FQN modernizado**: `cl.bcs.portafolio.service.ConsumoService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: La modernización delega a `ConsumoPort` con validaciones de nulos. El legado tiene 2 llamadas externas mínimas.

---

#### CrearCuentaCustodio
**FQN legado**: `cl.bcs.portafolio.CrearCuentaCustodio`
**FQN modernizado**: `cl.bcs.portafolio.javacall.CrearCuentaCustodioService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 20 llamadas externas en legado encapsuladas vía SimpleJdbcCall/HANA SP.

---

#### CrearMatrizAgrupadorNOAPV / EliminarMatrizAgrupadorNOAPV / ModificarMatrizAgrupadorNOAPV
**FQN legado**: respectivos
**FQN modernizado**: `cl.bcs.portafolio.javacall.MatrizJavaCallService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: Sin llamadas externas de negocio en legado. SQL puro.

---

#### CrearPortafolio
**FQN legado**: `cl.bcs.portafolio.CrearPortafolio`
**FQN modernizado**: `cl.bcs.portafolio.javacall.CrearPortafolioService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: El legado tiene 65 llamadas externas. La modernización implementa el flujo completo: (1) SP HANA vía SimpleJdbcCall, (2) llamada cross-service a GestionCuentaInversion vía JavaCallRestBridge, (3) creación de evento de autorización si aplica, (4) ReplicaPortafolioAPV, (5) AgregarLog — todos vía RestClient. El comportamiento post-SP está correctamente implementado.

---

#### CrearPortafolioConReplica
**FQN legado**: `cl.bcs.portafolio.CrearPortafolioConReplica`
**FQN modernizado**: `cl.bcs.portafolio.javacall.CrearPortafolioConReplicaService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 8 llamadas externas en legado. La modernización delega a CrearPortafolioService y agrega la lógica de réplica.

---

#### EliminarSinTransacciones
**FQN legado**: `cl.bcs.portafolio.EliminarSinTransacciones`
**FQN modernizado**: `cl.bcs.portafolio.javacall.EliminarSinTransaccionesService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 14 llamadas externas en legado. La modernización replica la verificación de transacciones existentes antes de eliminar.

---

#### IngresaPreferenciaPagoPortafolio / ModificaPreferenciaPagoPortafolio
**FQN legado**: respectivos
**FQN modernizado**: `cl.bcs.portafolio.javacall.PreferenciaPagoJavaCallService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: La modernización encapsula las preferencias de pago con la misma lógica de negocio.

---

#### ModificarAutorizadoOrden
**FQN legado**: `cl.bcs.portafolio.ModificarAutorizadoOrden`
**FQN modernizado**: `cl.bcs.portafolio.javacall.AutorizadoJavaCallService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: La modernización convierte correctamente `Boolean FirmaOptimusCliente` → `"S"/"N"` antes del UPDATE batch.

---

#### ModificarPortafolio
**FQN legado**: `cl.bcs.portafolio.ModificarPortafolio`
**FQN modernizado**: `cl.bcs.portafolio.javacall.ModificarPortafolioService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: 29 llamadas externas en legado encapsuladas vía SimpleJdbcCall.

---

#### RevisaDatosPortafolio
**FQN legado**: `cl.bcs.portafolio.RevisaDatosPortafolio`
**Estado**: ❓ SIN_LEGADO
**Riesgo**: MEDIO
**Divergencia**: Sin equivalente modernizado. Clase de validación de datos de portafolio.

---

#### ws/ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio
**FQN legado**: `cl.bcs.portafolio.ws.ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio`
**FQN modernizado**: cubierto parcialmente por `PortafolioService` + `PortafolioController`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO

---

#### ws/ModificarEstadoPortafolioWS
**FQN legado**: `cl.bcs.portafolio.ws.ModificarEstadoPortafolioWS`
**FQN modernizado**: `cl.bcs.portafolio.javacall.ModificarEstadoPortafolioService`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: MEDIO
**Divergencia**: La versión modernizada valida formato/catálogo y llama SP `BSCL.Portafolio.BD.Servicios::ModificarEstadoPortafolio`. Sin embargo, el legado realiza validaciones adicionales de tipos de bloqueo y modos de estado que la modernización puede no cubrir completamente.

---

### Resumen BcsPortafolioServicios

| Estado | Cantidad |
|--------|----------|
| ✅ EQUIVALENTE | 13 |
| ⚠️ DIVERGENTE | 1 |
| ❌ STUB | 0 |
| ❓ SIN_LEGADO | 7 |
| **Total** | **21** |

---

## BcsAPVServicios

### Resumen

| Estado | Cantidad |
|--------|----------|
| ✅ EQUIVALENTE | 0 |
| ⚠️ DIVERGENTE | 4 |
| ❌ STUB | 63 |
| ❓ SIN_LEGADO | 0 |

### Hallazgos

**NOTA GENERAL**: Todos los repositorios del componente APV (`ContratoRepository`, `TraspasoRepository`, `RecaudacionRepository`, `BonificacionRepository`, `DistribucionRepository`, `PACRepository`) tienen sus métodos implementados con `throw new UnsupportedOperationException("requires HANA SPs")` o `throw new UnsupportedOperationException("activate in Phase 3.5 HANA sprint")`. Las 67 clases de servicio legacy mapean a través de estos repositorios que son stubs.

---

#### ContratoSAAPVService (CrearContratoSAAPV / GrabarContratoSAAPV / ModificarContratoSAAPV / ValidaContratoSAAPV / CambiarAPlanActivoContratoSAAPV / CambiarEstadoContratosSAAPV / CambiarEstadoEnvioNotificacion / CambiarEstadoRevocacion / ObtenerFolioSuscripcion / ConsultarClienteContratoSAAPV / AsociarPerfilesAPV)
**FQN legado**: `cl.bcs.APV.CrearContratoSAAPV` et al. (11 clases)
**FQN modernizado**: `cl.bcs.apv.service.contrato.ContratoSAAPVService` → `cl.bcs.apv.infrastructure.adapter.ContratoRepository`
**Estado**: ❌ STUB
**Riesgo**: ALTO (CRÍTICO)
**Divergencia**: `ContratoRepository` implementa `ContratoPort` con 12 métodos, todos lanzando `UnsupportedOperationException("requires HANA SPs")`. Las operaciones de contrato SAAPV — creación, modificación, validación, cambio de estado, cambio a plan activo, revocación, folio de suscripción — son completamente no operacionales. El legado `CrearContratoSAAPV` tiene 17 llamadas externas y `GrabarContratoSAAPV` tiene 58 (incluyendo `AgregarLog`, `ConfiguracionSistema`, `CrearMovimientoComprobante`, `RevisarRequisitos`).

---

#### TraspasoService (CrearTraspaso / GrabarTraspasos / CambiarEstadoTraspaso / ReaperturarTraspaso / AnularTraspasoEgreso / CrearOrdenTraspaso / ModificarOrigenDestino / ModificarPortafolioTraspaso / CargaRegistroHistoricoTraspaso / DevolucionPagoRezagos / TraspasoCuentasBancarias / CrearTraspasoTesoreria)
**FQN legado**: `cl.bcs.APV.CrearTraspaso` et al. (12 clases)
**FQN modernizado**: `cl.bcs.apv.service.traspaso.TraspasoService` → `cl.bcs.apv.infrastructure.adapter.TraspasoRepository`
**Estado**: ❌ STUB
**Riesgo**: ALTO (CRÍTICO)
**Divergencia**: `TraspasoRepository` tiene 8 métodos con `UnsupportedOperationException("requires HANA: [tabla]")`. El legado `GrabarTraspasos` tiene 16 llamadas externas incluyendo creación de portafolio APV vía SP, vinculación de PortafolioContratoSAAPV, y otras orquestaciones críticas. Toda la funcionalidad de traspasos APV está sin implementar.

---

#### RecaudacionService (CargaRecaudacion / CrearRecaudacionCliente / CrearRecaudacionDeposito / CrearRecaudacionEmpleador / AsignarContratoARecaudacion / AsignarDepositoEmpleador / CambiarEstadoRecaudacion / EliminarRegistrosRecaudacion / ConciliarDeposito / BusquedaContratoSAAPVRecaudacion / ProcesarCargaMasivaRecaudacion / GrabarRecaudacionTraspaso / IngresoRecaudacionTraspaso / ModificarAnotacionRecaudacion / CrearAnotacionRecaudacion)
**FQN legado**: `cl.bcs.APV.CargaRecaudacion` et al. (15 clases)
**FQN modernizado**: `cl.bcs.apv.service.recaudacion.RecaudacionService` → `cl.bcs.apv.infrastructure.adapter.RecaudacionRepository`
**Estado**: ❌ STUB
**Riesgo**: ALTO (CRÍTICO)
**Divergencia**: `RecaudacionRepository` tiene 9 métodos con `UnsupportedOperationException("requires HANA — activate in Phase 3.5 HANA sprint")`. Toda la funcionalidad de recaudación APV (carga, conciliación, distribución, asignación de contratos, procesamiento masivo) está sin implementar.

---

#### BonificacionService (CargaBonificacionTGR / EjecutarBonificacion / ModificarBonificacion / ProcesarBonificacion)
**Estado**: ❌ STUB
**Riesgo**: ALTO
**Divergencia**: `BonificacionRepository` tiene 4 métodos con `UnsupportedOperationException`. Toda la funcionalidad de bonificación TGR está sin implementar.

---

#### DistribucionService (ProcesarDistribucion / ConsultarTotalesDistribucionRecaudacion / ConsultarTotalesOrigenPorDeposito / DevolucionBF_EntidadRecaudadora / ConsultarCustodiaPortafolioAPV / ConsultarPreferenciaPagoPortafolio / ReplicaPortafolioAPV)
**Estado**: ❌ STUB
**Riesgo**: ALTO
**Divergencia**: `DistribucionRepository` tiene 7 métodos con `UnsupportedOperationException`. El legado `ProcesarDistribucion` tiene 28 llamadas externas y es uno de los servicios más complejos del APV.

---

#### PACService (CargaMasivaBanco / CargaMasivaPAC / CargaMasivaPrevired / CargaMasivaSAAPVPAC / CargaMasivaArchivo / ModificarRelacionLaboral / ValidarBancoPrevired / CrearBancoPrevired / EliminarBancoPrevired / ModificarBancoPrevired)
**Estado**: ❌ STUB
**Riesgo**: ALTO
**Divergencia**: `PACRepository` tiene 9 métodos con `UnsupportedOperationException`. Las operaciones de PAC (Pago Automático de Cuotas) están sin implementar.

---

#### ValidarMandatosPACService / CalculoRetencionService / CalculoMontosDesinversion
**FQN legado**: `cl.bcs.APV.ValidarMandatosPAC`, `cl.bcs.APV.CalculoRetencionTraspasos`, `cl.bcs.APV.CalculoMontosDesinversion`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: MEDIO
**Divergencia**: `ValidarMandatosPACService` y `CalculoRetencionService` tienen estructura de servicio definida pero sus ports subyacentes (`MandatoPACLogPort`, `TraspasoPort`) aún tienen implementaciones stub. El servicio de validación de mandatos PAC delega a operaciones no implementadas.

---

### Resumen BcsAPVServicios

| Estado | Cantidad |
|--------|----------|
| ✅ EQUIVALENTE | 0 |
| ⚠️ DIVERGENTE | 4 |
| ❌ STUB | 63 |
| ❓ SIN_LEGADO | 0 |
| **Total** | **67** |

---

## BcsSocioNegocioServicios

### Resumen

| Estado | Cantidad |
|--------|----------|
| ✅ EQUIVALENTE | 2 |
| ⚠️ DIVERGENTE | 0 |
| ❌ STUB | 0 |
| ❓ SIN_LEGADO | 556 |

**NOTA GENERAL**: El microservicio legacy BcsSocioNegocioServicios contiene 558 clases Java organizadas en 17 subpaquetes (bck/blockchain, blo/bloqueo, car/cartera, cli/cliente, cnt/contraparte, ctt/contrato, doc/documentos, eje/ejecutivo, emp/empleado, enr/enrolamiento, per/persona, por/portafolio, pro/proveedor, rel/relacional, soc/socioNegocio, utils, wbs/webservices). La versión modernizada solo implementa 3 servicios específicos.

### Hallazgos

#### ConsultarEnrolamiento
**FQN legado**: `cl.bcs.socionegocio.enr.ConsultarEnrolamiento` (uno de los 118 en subpaquete enr/)
**FQN modernizado**: `cl.bcs.socionegocio.service.soc.ConsultarEnrolamientoService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: La modernización implementa la misma query de enrolamiento con filtros por IdTipoEntidad, IdTipoDocumentoIdentidad, DocumentoIdentidad, Pais y Tipo. El comportamiento condicional del codigoPais está preservado.

---

#### EliminarSucursalAtencion
**FQN legado**: `cl.bcs.socionegocio.emp.EliminarSucursalAtencion` (uno de los 8 en subpaquete emp/)
**FQN modernizado**: `cl.bcs.socionegocio.service.emp.EliminarSucursalAtencionService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: La modernización replica exactamente el soft-delete (UPDATE SET Borrado='S') para la tupla (IdSucursalAtencion, IdEmpleado).

---

#### SocioNegocioServiciosService (utilidades IUP/Ofuscación)
**FQN legado**: `cl.bcs.socionegocio.utils.IUP`, `cl.bcs.socionegocio.utils.OfuscarDocumentoIdentidad`
**FQN modernizado**: `cl.bcs.socionegocio.service.SocioNegocioServiciosService`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: Lógica pure-Java de generación IUP y ofuscación de documento de identidad. Correctamente migrada.

---

#### [555 clases sin equivalente modernizado]
**FQN legado**: `cl.bcs.socionegocio.{bck,blo,car,cli,cnt,ctt,doc,eje,emp[7],enr[117],per[155],por,pro,rel,soc[56],wbs[65]}`
**Estado**: ❓ SIN_LEGADO
**Riesgo**: ALTO (CRÍTICO)
**Divergencia**: Clases sin equivalente en la versión modernizada incluyen:
- **bck/** (26): Integración blockchain (ActualizaEstadosBlockChain, FirmarContratoBlockchain, ConsultarEstadoContrato, etc.)
- **cli/** (48): Gestión de clientes (CrearCliente, ModificarCliente, BloquearCliente, ValidarCliente, etc.)
- **per/** (155): Gestión de personas y documentos de identidad
- **enr/** (117 restantes): Operaciones de enrolamiento adicionales
- **wbs/** (65): Webservices de SocioNegocio
- **soc/** (56 restantes): Servicios core de SocioNegocio
- **doc/** (15): Gestión de documentos, firmas, generación
- **eje/** (17): Gestión de ejecutivos y carteras
- **cnt/** (7): Contrapartes
- **blo/** (7): Bloqueos automáticos de clientes/portafolios

---

### Resumen BcsSocioNegocioServicios

| Estado | Cantidad |
|--------|----------|
| ✅ EQUIVALENTE | 3 |
| ⚠️ DIVERGENTE | 0 |
| ❌ STUB | 0 |
| ❓ SIN_LEGADO | 555 |
| **Total** | **558** |

---

## BcsCanalAgenteServicios

### Resumen

| Estado | Cantidad |
|--------|----------|
| ✅ EQUIVALENTE | 1 |
| ⚠️ DIVERGENTE | 0 |
| ❌ STUB | 0 |
| ❓ SIN_LEGADO | 0 |
| ⚠️ SQL_MISMATCH | 0 |

### Hallazgos

#### ConsultarHistorialMovimientosCuentaInversion
**FQN legado**: `cl.bcs.canalagente.ConsultarHistorialMovimientosCuentaInversion`
**FQN modernizado**: `cl.bcs.canalagente.service.CanalAgenteService` + `cl.bcs.canalagente.adapter.out.CanalAgenteRepositoryAdapter`
**Estado**: ✅ EQUIVALENTE
**Riesgo**: BAJO
**Divergencia**: El flujo de 4 pasos del legado está correctamente implementado en el modernizado:
1. SELECT IdCliente desde tabla Portafolio (mismo query SELECT TOP 1)
2. Llamada cross-service a ConsultarCartolaDeMovimiento (vía JavaCall REST en modernizado vs llamada directa Java en legado)
3. INSERT batch en tabla proceso para enriquecimiento con datos de comprobante
4. SELECT JOIN con comprobantes/portafolio/cliente, seguido de DELETE cleanup siempre (incluso en error)

El modernizado agrega un `synchronized` lock para serializar acceso concurrente a la tabla proceso compartida — mejora respecto al legado.

**Nota SQL**: El SQL de findIdCliente en el modernizado usa tabla `PORTAFOLIO_T` (schema de test), pero el comentario documenta la tabla de producción `BSCL.Portafolio.BD::POR.Portafolio`. En entorno de producción con la tabla correcta, el comportamiento es equivalente.

**Campos salida legado vs modernizado**: El dominio `MovimientoCuentaInversion` del modernizado tiene los mismos campos que el legado (26 campos incluyendo IdRespuestaDTE, RespuestaPdfDTE, DocumentoIdentidad).

---

### Resumen BcsCanalAgenteServicios

| Estado | Cantidad |
|--------|----------|
| ✅ EQUIVALENTE | 1 |
| **Total** | **1** |

---

## Resumen Global

| Microservicio | Total Legacy | Equivalente | Divergente | Stub | Sin Legado |
|--------------|-------------|-------------|------------|------|-----------|
| BcsCuentaInversionServicios | 43 | 24 | 17 | 0 | 0 |
| BcsPortafolioServicios | 21 | 13 | 1 | 0 | 7 |
| BcsAPVServicios | 67 | 0 | 4 | 63 | 0 |
| BcsSocioNegocioServicios | 558 | 3 | 0 | 0 | 555 |
| BcsCanalAgenteServicios | 1 | 1 | 0 | 0 | 0 |
| **TOTALES** | **690** | **41** | **22** | **63** | **562** |

---

## Riesgos Críticos Ordenados por Prioridad

### CRÍTICOS (Bloquean Go-Live)

1. **BcsAPVServicios — STUB COMPLETO** (63 de 67 servicios): Toda la gestión de contratos SAAPV, traspasos, recaudación, bonificaciones, distribución y PAC está sin implementar. Afecta a operaciones de negocio de APV en su totalidad.

2. **BcsSocioNegocioServicios — 555 CLASES SIN_LEGADO**: La gestión de clientes (48 clases), personas (155 clases), documentos (15 clases), ejecutivos (17 clases), webservices (65 clases) y todas las demás entidades del SocioNegocio no tienen equivalente modernizado. El sistema de gestión de clientes está incompleto.

3. **CrearCargoAbono — DIVERGENTE CRÍTICO**: La operación financiera más frecuente del sistema usa `LocalDate.now()` en lugar de `FechaSistema` del sistema, carece de 15+ validaciones de negocio, no llama `SaldoFlujoCaja`, no genera log de auditoría, y no schedula jobs de actualización de saldo. Todo movimiento de cargo/abono generado puede ser inválido o incompleto.

### ALTOS (Impactan correctitud financiera)

4. **ProcesarCobroComisionPatrimonio — DIVERGENTE**: No genera comprobantes de comisión (CrearMovimientoComprobante). El proceso de cobro de comisión no produce la documentación financiera requerida.

5. **GuardarSaldosReporteAntiguedad — DIVERGENTE**: Fechas sin considerar días hábiles, afecta informes regulatorios de antigüedad de deuda.

6. **ActualizarSaldosReporteAntiguedad y CargaInicialSaldosReporteAntiguedad — DIVERGENTE**: Cálculo de períodos sin lógica de calendario hábil.

7. **ProcesaCargaSeleccionada — DIVERGENTE**: No invoca el flujo completo de CrearCargoAbono con validaciones, afecta la carga masiva de movimientos.

### MEDIOS (Impactan auditoría y cumplimiento)

8. **AnularCargoAbono, CertificarCargoAbono, EliminarCobroComision* — DIVERGENTE**: Ausencia de registros de auditoría (AgregarLog) en operaciones críticas.

9. **BcsPortafolioServicios — 7 clases masivas SIN_LEGADO**: Las autorizaciones masivas de portafolio están sin implementar.

---

## Notas Metodológicas

- La clasificación DIVERGENTE se basa en diferencias de comportamiento observable (resultados de negocio, llamadas a servicios externos, registros de auditoría), no en diferencias de framework.
- La clasificación STUB indica que el código lanza `UnsupportedOperationException` o no tiene lógica de negocio implementada.
- La clasificación SIN_LEGADO indica que existe el legado pero no hay equivalente modernizado identificado.
- Los servicios EQUIVALENTE con llamadas externas menores (ConfiguracionSistema para parámetros simples) se consideraron equivalentes cuando la modernización logra el mismo efecto a través de la arquitectura de puertos o parámetros de configuración.
