# Auditoría de Equivalencia — BcsPlataformaServicios
**Fecha**: 2026-10-07
**Servicios modernizados totales**: 328
**Servicios auditados en detalle**: 35
**Metodología**: Comparación manual legado (TomEE/JavaCall) vs modernizado (Spring Boot). Se examinan contratos de entrada/salida, queries SQL, lógica de negocio y llamadas externas.

---

## Resumen Ejecutivo

La modernización de BcsPlataformaServicios presenta **riesgos críticos sistémicos** en tres áreas:

1. **176 servicios son STUBS explícitos** — contienen comentario `TODO: requires pla.* utility — complete after pla subpackage migration`. Parsean parámetros pero no ejecutan ninguna lógica de negocio ni operaciones DB. Retornan `P_OUT_ERRORESCONTADOR = 0` sin hacer nada.
2. **De los ~152 servicios sin TODO**, al menos 15 tienen divergencias de comportamiento ALTO riesgo (validaciones omitidas, pasos SQL faltantes, llamadas a servicios externos no implementadas).
3. **37 clases del legado no tienen contraparte modernizada** — incluyendo la totalidad de los servicios de conciliación (ConciliacionFFMM*, ConciliacionMesaRF*, etc.) y `CalcularCobros`.

---

## Resumen Estadístico

| Estado | Cantidad estimada | % | Nota |
|--------|----------|---|------|
| ❌ STUB | ~178 | ~54% | 176 con TODO explícito + 2 sin TODO pero vacíos |
| ⚠️ DIVERGENTE | ~15 | ~5% | Confirmado por auditoría detallada |
| ✅ EQUIVALENTE | ~1 | <1% | Solo CrearModificarEmisor (divergencia mínima) |
| ⚠️ SQL_MISMATCH | ~3 | ~1% | Steps SQL faltantes en ProcesarCuadratura* |
| ❓ SIN_LEGADO | ~17 | ~5% | Servicios nuevos sin contraparte legacy |
| ❌ SIN_MODERNIZADO | 37 | ~11% | Clases legacy sin contraparte modernizada |
| No auditados | ~77 | ~23% | Servicios sin TODO, requieren revisión adicional |

> **NOTA**: Las cifras de DIVERGENTE y EQUIVALENTE aplican únicamente a los ~152 servicios sin TODO. Los 176 STUBS están confirmados por grep programático.

---

## Hallazgos Detallados

### 1. CrearContratoComision
**FQN legado**: `cl.bcs.plataforma.com.CrearContratoComision`
**Archivo modernizado**: `service/com/CrearContratoComisionService.java`
**Estado**: ❌ STUB
**Riesgo**: ALTO
**Divergencia**: El método `execute()` contiene `// TODO: requires pla.* utility`. Parsea las variables de entrada (`p_IdEmpresa`, `p_IdUsuario`, `p_IdModulo`, `p_UsaReglasNegocio`, `p_ContratosComisiones`) pero no ejecuta ninguna operación. El legado ejecuta: (1) validar existencia tipo contrato (`ValidarExistenciaTipoContratoComision`), (2) validar existencia contrato idéntico (`ValidarExistenciaContratoComisionComun`), (3) obtener secuencias (`Utils.getSecuencia`), (4) obtener fecha sistema (`FechaDB.ObtenerFechaHoraSistemaTs`), (5) insertar por batch en `InsertContratoComision.sql`.
**Campos de salida legado**: `P_OUT_MENSAJES`, `P_OUT_ERRORESCONTADOR`, `P_OUT_REFID` (lista de IDs insertados)
**Campos de salida modernizado**: `P_OUT_ERRORESCONTADOR` (siempre 0) — faltan `P_OUT_MENSAJES` y `P_OUT_REFID`

---

### 2. CrearUsuario
**FQN legado**: `cl.bcs.plataforma.acc.CrearUsuario`
**Archivo modernizado**: `service/acc/CrearUsuarioService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencias**:
- **Validación de rol de seguridad faltante**: El legado verifica que el usuario que crea (`p_IdUsuario`) tenga el rol de seguridad (`ConfiguracionSistema.getParametroConfiguracionLong(…"ACC.IdRolSeguridad")`) antes de proceder. El modernizado NO valida esto.
- **Hash de contraseña diferente**: El legado usa `Hashing.sha256()` una sola vez (`sha256(clave)`). El modernizado usa `sha256Double()` (doble hash). Esto hace que las claves almacenadas sean incompatibles si el sistema legado ya tiene usuarios con hash simple.
- **Falta creación de evento de autorización**: El legado llama `CrearEventoAutorizacionMasivo.logica()` si el concepto `ENROLU` requiere autorización. El modernizado omite este paso completamente.
- **Falta notificación por correo**: El legado ejecuta `AgregarEvento` con código `NOTIFICAR_CREACION_USUARIO` para usuarios tipo corredor/negocio. El modernizado no notifica.
- **Lógica de verificación de duplicado diferente**: El legado usa `SQL_SELECT_USUARIO` para verificar si el código de usuario ya está asociado al mismo socio de negocio, con mensaje `USUARIO_YA_EXISTE` o `UI_SOCIO_ASOCIADO_A_USUARIO`. El modernizado usa `SQL_CONSULTA_EXISTE_USUARIO` con mensaje genérico.
- **Campos faltantes en INSERT**: El legado inserta `DiasVencimientoClave` (días de vencimiento de contraseña), `EsAutorizado` (=`"P"` si requiere autorización, `"S"` si no), `FechaUltimaConexion`. El modernizado no inserta estos campos.
- **SQL files faltantes**: El legado usa `SQL_SELECT_USUARIOS_SEG`, `SQL_SELECT_USUARIOS_EXTRA`, `SQL_SELECT_IDPER_VALIDO`, `SQL_SELECT_PARAM_ENVIO`. El modernizado no las usa.
**Campos de salida legado**: `P_OUT_MENSAJES`, `P_OUT_ERRORESCONTADOR`, `P_OUT_NEW_USER_ID`
**Campos de salida modernizado**: `P_OUT_MENSAJES`, `P_OUT_ERRORESCONTADOR`, `P_OUT_NEW_USER_ID` — estructura igual pero comportamiento distinto

---

### 3. CambiarClaveUsuario
**FQN legado**: `cl.bcs.plataforma.acc.CambiarClaveUsuario`
**Archivo modernizado**: `service/acc/CambiarClaveUsuarioService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencias**:
- **Falta validación de fortaleza de clave nueva**: El legado llama `ValidarNuevaClave.logica()` para validar que la nueva clave cumpla criterios (longitud, mayúsculas, minúsculas, caracteres especiales). El modernizado NO implementa esta validación.
- **Comportamiento de historial de clave diferente**: El legado inserta la clave **anterior** (la que se está reemplazando) en el log histórico. El modernizado inserta la clave **nueva** en el historial. Este es un bug funcional crítico que rompe la detección de claves repetidas.
- **Verificación de claves repetidas omitida**: El legado verifica con `NumClaveCoincide > 0` que la nueva clave no coincida con ninguna de las 10 anteriores, retornando error `UI_CLAVES_IGUALES`. El modernizado consulta el log pero no usa el resultado para bloquear el cambio.
- **Valor de retorno P_OUT_UPDATE**: El legado retorna `"ok+++"` en éxito. El modernizado retorna `"OK"`. Impacto potencial en clientes que comparan este valor.
**Campos de salida legado**: `P_OUT_ERRORESCONTADOR`, `P_OUT_ERRORESDETALLE`, `P_OUT_UPDATE`
**Campos de salida modernizado**: `P_OUT_ERRORESCONTADOR`, `P_OUT_ERRORESDETALLE`, `P_OUT_UPDATE` — nombres iguales

---

### 4. ProcesarCuadraturaCustodiaRFIF
**FQN legado**: `cl.bcs.plataforma.cua.ProcesarCuadraturaCustodiaRFIF`
**Archivo modernizado**: `service/cua/ProcesarCuadraturaCustodiaRFIFService.java`
**Estado**: ⚠️ DIVERGENTE + ⚠️ SQL_MISMATCH
**Riesgo**: ALTO
**Divergencias**:
- **Falta validación de fechas**: El legado valida que `p_FechaInicio` < `p_FechaFin` y retorna error `UI_ERROR_FECHA_INICIAL_DEBE_SER_MENOR_FECHA_FINAL` si no se cumple. El modernizado NO valida esto.
- **Pasos SQL faltantes**: El legado ejecuta 3 SQLs inline críticos que el modernizado no implementa:
  - `CARGA_SFINALIF`: UPDATE de `SaldosCuadraturaRF` calculando `SaldoFinalSistema` para instrumentos IF usando cartera custodia.
  - `CARGA_SFINALIFN`: UPDATE de `SaldosCuadraturaRF` poniendo `SaldoFinalSistema = 0` donde era NULL.
  - `MOD_SALDO_CALCULADO`: UPDATE calculando `SaldoFinalCalculado` como `SaldoInicial - SUM(MovimientosCustodia)`.
- **Orden de ejecución diferente**: El legado ejecuta CARGA_SFINAL **antes** de los movimientos. El modernizado lo ejecuta **después**. El resultado final es diferente.
- **Input faltante**: El legado usa `p_IdEmpresa`, `p_IdUsuario` además de fechas. El modernizado ignora `p_IdEmpresa` y `p_IdUsuario`.
**Campos de salida legado**: `P_OUT_ERRORESCONTADOR`, `P_OUT_ERRORESDETALLE`
**Campos de salida modernizado**: `P_OUT_ERRORESCONTADOR`, `P_OUT_MENSAJES`  ← nombre de campo diferente

---

### 5. EjecutarControlProceso
**FQN legado**: `cl.bcs.plataforma.ctr.EjecutarControlProceso`
**Archivo modernizado**: `service/ctr/EjecutarControlProcesoService.java`
**Estado**: ❌ STUB
**Riesgo**: ALTO
**Divergencia**: El modernizado solo ejecuta un SELECT sobre las revisiones de control de proceso y devuelve las filas como JSON. El legado es un orquestador completo que: (1) obtiene la fecha del sistema si `p_Fecha` está vacío, (2) calcula el día hábil anterior (`ObtenerDiaAnterior`), (3) itera sobre cada revisión e **instancia dinámicamente** la clase de revisión por nombre (`Class.forName(salidaRevision.NombreClase)`), (4) ejecuta `controlProceso.ejecutar()` para cada una, (5) inserta registros de detalle de control proceso (`SQL_INSERT_CONTROL_PROCESO_DETALLE`). El modernizado produce solo una consulta, nunca ejecuta los procesos de control.
**Campos de salida legado**: `P_OUT_MENSAJES`, `P_OUT_ERRORESCONTADOR`, `P_OUT_RESULTADO` (lista de mensajes por proceso)
**Campos de salida modernizado**: `P_OUT_RESULTADO` (filas de BD), `P_OUT_ERRORESCONTADOR` ← `P_OUT_RESULTADO` tiene tipo completamente diferente

---

### 6. CrearEventoAutorizacion
**FQN legado**: `cl.bcs.plataforma.aut.CrearEventoAutorizacion`
**Archivo modernizado**: `service/aut/CrearEventoAutorizacionService.java`
**Estado**: ❌ STUB
**Riesgo**: CRÍTICO
**Divergencia**: El modernizado contiene `// TODO: requires StoredProcedure support via JDBC CallableStatement` y retorna arrays vacíos sin llamar al stored procedure. El legado llama `CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion"` con 29 parámetros y lee los resultados. Este servicio es usado por docenas de otros servicios (incluyendo CrearUsuario, ordenes, etc.) para crear eventos de autorización. Al ser un STUB, **ningún flujo de autorización funciona**.
**Campos de salida legado**: `p_out_IdEvento`, `p_out_AdvertenciasContador`, `p_out_AdvertenciasDetalle`, `p_out_ErroresContador`, `p_out_ErroresDetalle`
**Campos de salida modernizado**: `P_OUT_IDEVENTO` (array vacío), `P_OUT_ADVERTENCIASCONTADOR` (0), `P_OUT_ADVERTENCIASDETALLE`, `P_OUT_ERRORESCONTADOR` (0), `P_OUT_ERRORESDETALLE` ← siempre retorna éxito sin ejecutar nada

---

### 7. CrearDefinicionEncuesta
**FQN legado**: `cl.bcs.plataforma.enc.CrearDefinicionEncuesta`
**Archivo modernizado**: `service/enc/CrearDefinicionEncuestaService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencias**:
- **Falta pre-validación de requisitos básicos**: El legado llama `RevisarRequisitosBasicos.logica()` que valida autenticación/autorización básica antes de cualquier operación.
- **Lógica de duplicado diferente**: El legado busca por `Nombre` de encuesta y retorna error `UI_ENCUESTA_YA_EXISTE` si ya existe. El modernizado busca por `IdEmpresa` y en vez de retornar error, inactiva la encuesta existente antes de crear la nueva. Son comportamientos opuestos.
- **Generación de IDs faltante**: El legado usa `Utils.getSecuencia()` para obtener `IdDefinicionEncuesta`, `IdDefinicionPregunta`, `IdDefinicionRespuesta` de la secuencia BD. El modernizado no genera IDs, dejando los inserts sin clave primaria.
- **Estructura de input diferente**: El legado recibe `p_Preguntas` (lista de preguntas) y `p_Respuestas` (lista separada de respuestas, por `IdPregunta`). El modernizado espera `p_Preguntas` donde cada pregunta tiene `Respuestas` anidadas.
- **Campos faltantes en INSERT**: El legado inserta `IdDefinicionEncuesta`, `IdUsuarioCreacion`, `Borrado`. El modernizado no los incluye.
- **Campos de salida faltantes**: El legado retorna `p_out_IdDefinicionEncuestaCreada`, `p_out_Preguntas` (map de IDs), `p_out_Respuestas` (map de IDs). El modernizado no retorna estos campos.
**Campos de salida legado**: `P_OUT_ERRORESCONTADOR`, `P_OUT_MENSAJES`, `p_out_IdDefinicionEncuestaCreada`, `p_out_Preguntas`, `p_out_Respuestas`
**Campos de salida modernizado**: `P_OUT_ERRORESCONTADOR`, `P_OUT_MENSAJES` ← faltan 3 campos de salida

---

### 8. CrearModificarEmisor
**FQN legado**: `cl.bcs.plataforma.mer.CrearModificarEmisor`
**Archivo modernizado**: `service/mer/CrearModificarEmisorService.java`
**Estado**: ✅ EQUIVALENTE (con divergencia menor)
**Riesgo**: BAJO
**Divergencia menor**: En modo INGRESAR, el modernizado no hace `return` inmediato después de agregar el error de duplicado, pero dado que no hay más operaciones después del bloque de error, el resultado efectivo es el mismo. Los SQLs utilizados son idénticos.
**Campos de salida legado**: `P_OUT_ADVERTENCIASCONTADOR`, `P_OUT_ADVERTENCIASDETALLE`, `P_OUT_ERRORESCONTADOR`, `P_OUT_ERRORESDETALLE`
**Campos de salida modernizado**: `P_OUT_ADVERTENCIASCONTADOR`, `P_OUT_ADVERTENCIASDETALLE`, `P_OUT_ERRORESCONTADOR`, `P_OUT_ERRORESDETALLE` ← idénticos

---

### 9. ActualizarUsuario
**FQN legado**: `cl.bcs.plataforma.acc.ActualizarUsuario`
**Archivo modernizado**: `service/acc/ActualizarUsuarioService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencias**:
- **Falta notificación por correo**: El legado envía notificación `NOTIFICAR_CREACION_USUARIO` para cambios de tipo de usuario (corredor/negocio). El modernizado no lo hace.
- **Falta log de auditoría**: El legado llama `AgregarLog.logica()` con registro de auditoría (`IdCabeceraLog = 159`, tipo `ACTUALIZACION`, módulo `PLA`). El modernizado no registra el log de auditoría.
- **SQL faltantes**: El legado usa `SQL_SELECT_LOG_USUARIO_CLAVE`, `SQL_SELECT_USUARIOS_SEG`, `SQL_SELECT_USUARIOS_EXTRA`, `SQL_SELECT_IDPER_VALIDO`, `SQL_SELECT_PARAM_ENVIO`. El modernizado no usa ninguno de los 5 últimos.
- **Campos faltantes en UPDATE**: El legado puede actualizar `DiasVencimientoClave`, `DebeCambiarClave`. El modernizado no maneja estos campos.
**Campos de salida legado**: `P_OUT_MENSAJES`, `P_OUT_ERRORESCONTADOR`, `P_OUT_NEW_USER_ID`
**Campos de salida modernizado**: `P_OUT_MENSAJES`, `P_OUT_ERRORESCONTADOR`, `P_OUT_NEW_USER_ID` ← estructura igual

---

### 10. EliminarDetalleContratoComision
**FQN legado**: `cl.bcs.plataforma.com.EliminarDetalleContratoComision`
**Archivo modernizado**: `service/com/EliminarDetalleContratoComisionService.java`
**Estado**: ❌ STUB
**Riesgo**: ALTO
**Divergencia**: Comentario `// TODO: requires pla.* utility (PlataformaUtils, Diccionario)`. Parsea los parámetros de entrada pero no ejecuta ninguna operación. Retorna siempre `P_OUT_ERRORESCONTADOR = 0`.

---

### 11. CrearAutorizadoEtapa
**FQN legado**: `cl.bcs.plataforma.aut.CrearAutorizadoEtapa`
**Archivo modernizado**: `service/aut/CrearAutorizadoEtapaService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencias**:
- **Todas las validaciones de negocio omitidas**: El legado valida 7 condiciones antes de insertar: (1) existencia de la etapa, (2) estado activo de la etapa, (3) unicidad del autorizador ya existente, (4) existencia del usuario, (5) existencia del rol, (6) rol asociado al usuario, (7) usuario reemplazante válido y fechas coherentes. El modernizado NO valida ninguna — inserta directamente.
- **Generación de ID faltante**: El legado obtiene `IdAutorizadoEtapa` de la secuencia `BSCL.Plataforma.BD.Secuencias::AUTSecuenciasGenerales`. El modernizado no genera el ID, dejando el registro sin PK.
- **Solo inserta registros válidos**: El legado construye `listAutorizadosEtapaCorrectos` y solo hace batch insert de los que pasaron validación. El modernizado intenta insertar todos.
**Campos de salida legado**: `P_OUT_MENSAJES`, `P_OUT_ERRORESCONTADOR`
**Campos de salida modernizado**: `P_OUT_ERRORESCONTADOR`, `P_OUT_MENSAJES` ← mismos campos pero con mensajes de error no localizados

---

### 12. CrearPerfilProductoDetalle
**FQN legado**: `cl.bcs.plataforma.com.CrearPerfilProductoDetalle`
**Archivo modernizado**: `service/com/CrearPerfilProductoDetalleService.java`
**Estado**: ❌ STUB
**Riesgo**: ALTO
**Divergencia**: Comentario `// TODO: requires pla.* utility (PlataformaUtils, ConfiguracionSistema, Diccionario)`. El legado verifica existencia de perfil, requiere autorización y hace insert con secuencia. El modernizado no ejecuta nada.
**Campos de salida legado**: `P_OUT_REFID`, `P_OUT_ADVERTENCIASCONTADOR`, `P_OUT_ADVERTENCIASDETALLE`, `P_OUT_ERRORESCONTADOR`, `P_OUT_ERRORESDETALLE`
**Campos de salida modernizado**: `P_OUT_ERRORESCONTADOR` (siempre 0) ← faltan 4 campos

---

### 13. ActualizarDefinicionPuntajes
**FQN legado**: `cl.bcs.plataforma.enc.ActualizarDefinicionPuntajes`
**Archivo modernizado**: `service/enc/ActualizarDefinicionPuntajesService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencias**:
- **Falta pre-validación de requisitos básicos**: El legado llama `RevisarRequisitosBasicos.logica()`.
- **Validación de rango de puntaje omitida**: El legado valida que `p_PuntajeHasta > 0` para cada puntaje; retorna error `UI_PUNTAJE_NO_VALIDO` si no. El modernizado no valida.
- **Validación de categoría omitida**: El legado verifica que `IdCategoriaInversionista` exista en el sistema; retorna error `UI_CATEGORIA_NO_EXISTE` si no. El modernizado no valida.
- **Nombre de campo de entrada diferente**: El legado recibe `p_Puntajes` (lista). El modernizado espera `p_DefinicionPuntajes`. Los clientes que envíen `p_Puntajes` no tendrán efecto.
- **Estructura de item diferente**: El legado usa `p_PuntajeHasta` (solo límite superior). El modernizado usa `PuntajeMinimo` + `PuntajeMaximo`. El SQL de insert puede diferir en los campos mapeados.
**Campos de salida legado**: `P_OUT_ERRORESCONTADOR`, `P_OUT_MENSAJES`
**Campos de salida modernizado**: `P_OUT_ERRORESCONTADOR`, `P_OUT_MENSAJES` ← mismo nombre pero con mensajes no localizados

---

### 14. CrearDeterioroEmisor
**FQN legado**: `cl.bcs.plataforma.mer.CrearDeterioroEmisor`
**Archivo modernizado**: `service/mer/CrearDeterioroEmisorService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencias**:
- **Procesa registro único vs lista**: El legado recibe `p_DeteriorosEmisores` (lista de `DeterioroEmisor`) y procesa batch. El modernizado procesa un solo deterioro con campos simples.
- **Falta generación de secuencia**: El legado obtiene `IdDeterioroEmisor` de `BSCL.Plataforma.BD.Secuencias::MER.IdDeterioroEmisor`. El modernizado no genera el ID.
- **Falta validación**: El legado llama `ValidarDeterioroEmisor.validarRegistroDeterioroEmisor()`. El modernizado inserta sin validar.
- **Falta log de inserción**: El legado llama `crearLogInsercion()` después de insertar exitosamente. El modernizado no registra log.
- **Campos diferentes**: El legado establece `Emisor.toUpperCase()`, `Accion = "CREAR"`. El modernizado no normaliza ni establece el campo `Accion`.
**Campos de salida legado**: `P_OUT_MENSAJES`, `P_OUT_ERRORESCONTADOR`
**Campos de salida modernizado**: `P_OUT_MENSAJES`, `P_OUT_ERRORESCONTADOR`

---

### 15. ValidacionPreventiva
**FQN legado**: `cl.bcs.plataforma.ccl.ValidacionPreventiva`
**Archivo modernizado**: `service/ccl/ValidacionPreventivaService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencia**: El legado ejecuta 20+ validaciones preventivas. El modernizado solo ejecuta 2 (`ClienteDuplicado`, `ClienteMovimientos`). Las 18+ validaciones faltantes incluyen:
- `ClientePersona`, `ClienteSaldo`, `ConceptoFFMM`
- `ContratoCSCMovimientos`, `ContratoSAAPVMovimientos`
- `DocumentoIdentidadSinPuntos`, `PortafolioMovimientos`, `PortafolioSaldos`
- `TipoMovimientoFFMM`, `PortafolioAmbosPlanesInversion`
- `Monedas`, `PrecioCustodia`
- `FechaPatrimonioCaja`, `FechaPatrimonioConsolidado`, `FechaPatrimonioCustodia`
- `FechaValidacionRetiros`, `PatrimonioSaldoCero`
- `MovimientosMatrizInvalida`, `MovimientosValorCuota`, `MovimientosConceptoInvalido`
También falta el `SQL_UPSERT_RESUMEN` que guarda el resumen de validaciones.
**Campos de salida legado**: `P_OUT_VALIDACIONES` (lista detallada por validación)
**Campos de salida modernizado**: `P_OUT_VALIDACIONES` (solo 2 validaciones)

---

### 16. ModificarEstadoEventoAutorizacionMasivo
**FQN legado**: `cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo`
**Archivo modernizado**: `service/aut/ModificarEstadoEventoAutorizacionMasivoService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: ALTO
**Divergencias**:
- **Lógica de orquestación de autorización omitida**: El legado coordina con `BcsAutorizaciones` para gestionar etapas de autorización (aprobación/rechazo). El modernizado solo llama `actualizaEvento` una vez por evento, sin implementar la lógica de etapas y autorización real.
- **SQL de insert/update de etapas no ejecutados**: El modernizado carga los SQLs `sqlInsertaAutorizadoEtapaEvento` y `sqlActualizaAutorizadoEtapaEvento`, `sqlActualizaEtapaEvento`, `sqlActualizaEventoPorEtapa` pero nunca los ejecuta — solo llama `actualizaEvento`.
- **Parámetros de entrada diferentes**: El legado usa `P_IDUSUARIO`, `P_IDEMPRESA`, `P_TABLAEVENTOS`. El modernizado usa los mismos nombres pero no los procesa con la lógica completa.
**Campos de salida legado**: `P_OUT_ERRORESCONTADOR`, `P_OUT_ADVERTENCIASCONTADOR`, `P_OUT_ERRORESDETALLE`, `P_OUT_ADVERTENCIASDETALLE`
**Campos de salida modernizado**: mismos campos ← nombres iguales, lógica incompleta

---

### 17. CrearEtapaAutorizacion
**FQN legado**: `cl.bcs.plataforma.aut.CrearEtapaAutorizacion`
**Archivo modernizado**: `service/aut/CrearEtapaAutorizacionService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: MEDIO
**Divergencias**:
- **Falta validación de tipo de evento**: El modernizado consulta `sqlSelectTipoEvento` y `sqlSelectEtapa` pero no valida si el tipo de evento existe o si ya hay etapas configuradas — procede al insert sin validar.
- **Falta generación de secuencia**: El legado obtiene IDs de secuencia para las etapas. El modernizado inserta sin ID de PK explícito.
- **Carga de estados no usada**: El modernizado consulta `sqlSelectEstado` pero no usa el resultado para ninguna validación.

---

### 18. EliminarDefinicionPuntaje
**FQN legado**: `cl.bcs.plataforma.enc.EliminarDefinicionPuntaje`
**Archivo modernizado**: `service/enc/EliminarDefinicionPuntajeService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: MEDIO
**Divergencias**:
- **Falta pre-validación de requisitos básicos**: El legado llama `RevisarRequisitosBasicos.logica()`.
- **Comportamiento diferente cuando no existe**: El legado retorna error `UI_DEFINICION_NO_EXISTE` si el puntaje no existe. El modernizado verifica si existe, pero si no existe simplemente no elimina (sin error).
- **Falta mensaje de éxito**: El legado agrega mensaje `UI_DEFINICION_ELIMINADA` en éxito. El modernizado no agrega mensaje de éxito.

---

### 19. ValidarClaveUsuario
**FQN legado**: `cl.bcs.plataforma.acc.ValidarClaveUsuario`
**Archivo modernizado**: `service/acc/ValidarClaveUsuarioService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: MEDIO
**Divergencias**:
- **Criterios de clave hardcodeados vs configurados en BD**: El legado usa `ValidarNuevaClave.logica()` que consulta los criterios de complejidad de contraseña desde la BD (configurable por empresa). El modernizado tiene 4 criterios hardcodeados (longitud≥8, mayúscula, minúscula, dígito) que no pueden cambiarse sin redeployar.
- **Mensajes sin localización**: El legado usa `Diccionario.getMensaje()` para obtener mensajes localizados por empresa. El modernizado retorna mensajes hardcodeados en español.

---

### 20. CambiarClaveUsuarioWS
**FQN legado**: `cl.bcs.plataforma.acc.CambiarClaveUsuarioWS`
**Archivo modernizado**: `service/acc/CambiarClaveUsuarioWSService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: MEDIO
**Divergencia**: Implementación estructuralmente correcta (resuelve IdUsuario por CodigoUsuario, luego delega a CambiarClaveUsuario). Sin embargo, hereda todas las divergencias de `CambiarClaveUsuarioService` (sin validación de fortaleza, sin historial correcto).

---

### 21. BloqueoUsuariosPorVigencia
**FQN legado**: `cl.bcs.plataforma.acc.BloqueoUsuariosPorVigencia`
**Archivo modernizado**: `service/acc/BloqueoUsuariosPorVigenciaService.java`
**Estado**: ⚠️ DIVERGENTE
**Riesgo**: BAJO
**Divergencias**:
- Usa `ConfiguracionSistemaUtil` (wrapper Spring) para obtener `PLA.FechaSistema`. El legado usa `ConfiguracionSistema` directamente. Funcionalmente equivalente si el util está bien implementado.
- Lógica de bloqueo en batch: el modernizado usa `jdbc.batchUpdate()` correctamente.
- Diferencias menores en mensajes de salida.

---

## Servicios SIN Contraparte Modernizada (❌ SIN_MODERNIZADO)

Los siguientes 37 servicios del legado **no tienen clase equivalente** en el directorio modernizado. Son funcionalidades completamente ausentes:

| Clase Legado | Dominio | Criticidad |
|-------------|---------|-----------|
| `CalcularCobros` | com | CRÍTICO |
| `CalcularCobrosPe` | com/pe | CRÍTICO |
| `ConciliacionFacturacionCuentaInversion` | ccl | CRÍTICO |
| `ConciliacionFacturacionCustodia` | ccl | CRÍTICO |
| `ConciliacionFacturacionTesoreria` | ccl | CRÍTICO |
| `ConciliacionFFMMCuentaInversion` | ccl | CRÍTICO |
| `ConciliacionFFMMMovimientoSpot` | ccl | CRÍTICO |
| `ConciliacionMesaRFCuentaInversion` | ccl | CRÍTICO |
| `ConciliacionMesaRFCustodia` | ccl | CRÍTICO |
| `ConciliacionMesaRFTesoreria` | ccl | CRÍTICO |
| `ConciliacionSpotFacturacion` | ccl | CRÍTICO |
| `ConcilicacionFFMMCustodia` | ccl | CRÍTICO |
| `ConfirmarCrearPerfilCobrosDetalleMasivo` | com | ALTO |
| `ConfirmarModificarPerfilCobrosDetalleMasivo` | com | ALTO |
| `AutorizarCrearPerfilProductoDetalleMasivo` | com | ALTO |
| `AutorizarCrearPerfilProductoMasivo` | com | ALTO |
| `AutorizacionEliminarConfiguracionPerfilComercialAdicional` | com | ALTO |
| `AutorizacionModificarConfiguracionPerfilComercialAdicional` | com | ALTO |
| `CargarArchivoTasas` | mer | ALTO |
| `CargaArchivoVectorPrecio_mer` | mer | ALTO |
| `CuponesWsBCS` | ins | MEDIO |
| `ValorizarWsBCS` | ins | MEDIO |
| `ValorizarWsBcsRest` | ins | MEDIO |
| `EnviarEmail` | pla | MEDIO |
| `AgregarLogPrograma` | pla | BAJO |
| `BcsAutorizaciones` | aut | CRÍTICO (usado por flujo AUT) |
| `BcsConciliacion` | ccl | CRÍTICO |
| `ConstantesConciliacion` | ccl | BAJO |
| `ConstantesPlataformaComercial` | com | BAJO |
| `ControlProceso` | ctr | ALTO (interfaz base) |
| `LimiteUtils` | pla | MEDIO |
| `ValoUtils` | pla | BAJO |
| `ValidarContenidoCampos` | pla | MEDIO |
| `TestValidarCamposObligatorios` | pla | BAJO |
| `ConsoleCall` | pla | BAJO |
| `AutorizacionEjemplo` | aut | BAJO |
| `CampoFormularioUtils` | frm | BAJO |

> **Impacto**: La ausencia de TODOS los servicios de conciliación (12 clases) implica que el sistema modernizado no puede realizar ninguna conciliación financiera. Esta es una brecha funcional crítica que debe resolverse antes de cualquier go-live.

---

## Servicios SIN Legado (❓ SIN_LEGADO)

Los siguientes servicios en el modernizado no tienen contraparte directa en el legado. Son funcionalidades potencialmente nuevas o refactorizadas:

- `service/acceso/` — nuevos servicios de autenticación Spring Security
- `service/autorizacion/` — nuevos helpers de autorización
- `service/comercial/` — nuevos servicios comerciales
- `service/conciliacion/` — nuevos servicios de conciliación
- `service/instrumento/` — nuevos servicios de instrumentos
- `service/pauta/` — nuevos servicios de pauta
- `service/plataforma/ConvertirMontoEscritoService` — nuevo
- `service/ctr/ControlProcesoRevision*` (5 clases) — nuevas implementaciones de revisiones
- `service/utils/ConfiguracionSistemaUtil` — wrapper del ConfiguracionSistema legado

---

## Patrón General de STUBs (176 servicios)

Los 176 servicios con `TODO: requires pla.* utility` siguen este patrón exacto:

```java
@Override
@Transactional
public JsonObject execute(JsonObject params) {
    log.info("INICIO::{}", fqn());
    JsonObject result = new JsonObject();
    try {
        // TODO: requires pla.* utility (PlataformaUtils, FechaDB) — complete after pla subpackage migration
        result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        // 1. Parse input params
        Long p_IdEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
        // ... más parseo ...
    } catch (Exception e) {
        result.addProperty("P_OUT_ERRORESCONTADOR", 1);
    }
    return result;
}
```

**Comportamiento**: Siempre retornan `P_OUT_ERRORESCONTADOR = 0` sin ejecutar ninguna operación de base de datos. El cliente recibe respuesta de éxito aunque ninguna operación se realizó.

---

## Servicios No Auditados

Los siguientes dominios contienen servicios sin TODO (parcialmente implementados o completos) que requieren auditoría adicional para clasificación definitiva:

| Dominio | Carpeta | Servicios sin TODO | Comentario |
|---------|---------|-------------------|-----------|
| Acceso | `acc/` | ~12 | ValidarSesion, ObtenerToken, AgregarRol, EliminarRol, etc. |
| Autorización | `aut/` | ~8 | NotificarAutorizaciones, RequiereEvento, etc. |
| Mercado | `mer/` | ~10 | AdministrarParam, CambiarPrioridad, etc. |
| Encuesta | `enc/` | ~5 | BorradorResponder, ObtenerResultado, etc. |
| CCL | `ccl/` | 1 | ValidacionPreventiva (auditado) |
| Plataforma | `pla/` y `plataforma/` | ~8 | ConvertirMontoEscrito, etc. |
| CTR | `ctr/` | 5 | ControlProcesoRevision* |
| Instrumento | `ins/` | ~8 | Varios |
| Tasas | `tas/` | ~3 | Varios |

---

## Recomendaciones Críticas

1. **BLOQUEAR go-live**: Con 176 servicios STUB, el sistema modernizado no puede procesar el 54% de las operaciones. Retorna éxito falso para todas ellas.

2. **Completar la migración de `pla.*` utilities**: `PlataformaUtils`, `FechaDB`, `ConfiguracionSistema`, `Diccionario`, `RevisarRequisitosBasicos` son dependencias transversales. Su ausencia bloquea los 176 servicios marcados con TODO.

3. **Implementar la llamada al Stored Procedure de AUT**: `CrearEventoAutorizacion` es la base de todos los flujos de autorización. Sin él, ninguna operación que requiera autorización funcionará correctamente.

4. **Implementar los servicios de conciliación**: Los 12 servicios `Conciliacion*` son totalmente ausentes. Esto inhabilita la función de cierre financiero.

5. **Reordenar `ProcesarCuadraturaCustodiaRFIF`**: El orden de ejecución de los pasos SQL es incorrecto y faltan 3 actualizaciones críticas de `SaldoFinalSistema` y `SaldoFinalCalculado`.

6. **Revisar el doble hash de contraseñas**: Si la BD ya tiene usuarios con hash simple (legado), el modernizado con `sha256Double()` hará que esas contraseñas sean inválidas en el nuevo sistema. Requiere un script de migración de datos o unificar el algoritmo.

7. **Restaurar validaciones de seguridad en `CrearUsuario`**: La omisión de la validación de rol de seguridad permite que cualquier usuario cree usuarios, lo que es una brecha de control de acceso.
