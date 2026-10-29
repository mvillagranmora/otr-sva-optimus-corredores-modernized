# BEHAVIORAL EQUIVALENCE AUDIT
## otr-sva-optimus-corredores
**Fecha:** 2026-10-07  
**Auditor:** Claude Sonnet 4.6 (automatizado)  
**Scope:** BcsMesaServicios, BcsOrdenServicios, BcsNormativaServicios, BcsJobManager  
**Metodología:** Comparación directa de output contract, input contract, cantidad de SQLs y lógica de negocio entre código legado (`BSCL/`) y modernizado (`modernized/`)  

---

## Resumen Ejecutivo

| Microservicio | Total Servicios | CRITICAS | MEDIAS | MENORES | Stubs |
|---|---|---|---|---|---|
| BcsMesaServicios | 122 | 5 (patrón sistémico) | 3 | 0 | 0 |
| BcsOrdenServicios | 138 | 1 (sistémica: 87 svc) | 2 | 0 | 87 (63%) |
| BcsNormativaServicios | 164 | 0 | 1 | 0 | 0 |
| BcsJobManager | N/A (hexagonal) | 0 | 1 | 1 | 0 |
| **TOTAL** | **424** | **6** | **7** | **1** | **87** |

**Hallazgo sistémico principal:** BcsMesaServicios tiene un patrón sistémico crítico donde los servicios transaccionales retornan datos de consulta (pre-execution data) en lugar de resultados de ejecución. BcsOrdenServicios tiene 87 stubs (63%) que aceptan cualquier input y siempre retornan éxito sin ejecutar ninguna lógica.

---

## Hallazgos por Microservicio

---

### BcsMesaServicios

#### [CRITICA-MSA-01] Patrón sistémico: output contract de servicios transaccionales SPT completamente diferente al legado

**Servicios afectados (muestra confirmada):**
- `CrearOperacionSpotService` → FQN `cl.bcs.mesa.SPT.CrearOperacionSpot`
- `ModificarOperacionSpotService` → FQN `cl.bcs.mesa.SPT.ModificarOperacionSpot`
- `AnularOperacionSpotService` → FQN `cl.bcs.mesa.SPT.AnularOperacionSpot`
- `ConfirmarLiquidacionSpotService` → FQN `cl.bcs.mesa.SPT.ConfirmarLiquidacionSpot`
- `ReprocesoDatatecService` → FQN `cl.bcs.mesa.SPT.ReprocesoDatatec`

**Ruta modernizado (ejemplo):** `BcsMesaServicios/src/main/java/cl/bcs/mesa/javacall/spt/CrearOperacionSpotService.java`  
**Ruta legado (ejemplo):** `BSCL/Mesa/JAVA/BcsMesaServicios/src/cl/bcs/mesa/SPT/CrearOperacionSpot.java`

**Diferencia — output contract:**

| Campo | Legado | Modernizado |
|---|---|---|
| `P_OUT_MENSAJES` | `List<MensajeSalida>` — mensajes de resultado de negocio | **AUSENTE** |
| `P_OUT_ERRORESCONTADOR` | `Long` — contador de errores real | `Long` (siempre 0 si no hay excepción) |
| `P_OUT_ADVERTENCIASCONTADOR` | `Long` — contador de advertencias | **AUSENTE** |
| `P_OUT_IDOPERACIONSPOT` (CrearOperacionSpot) | `List<OperacionObjOut>` — IDs de operaciones creadas | **AUSENTE** |
| `P_OUT_LIMITES` | — | Presente solo en modernizado (pre-query) |
| `P_OUT_PUNTAS` | — | Presente solo en modernizado (pre-query) |
| `P_OUT_OPERACIONES / P_OUT_OP` | — | Presente solo en modernizado (pre-query) |

**Diferencia — lógica de negocio:**

Legado `CrearOperacionSpot`: 60 referencias SQL, llama a `ConfirmarLiquidacionSpot`, `CrearOperacionSpotFacturacion`, `NotificarEstadoSpot`, `CrearEventoAutorizacionMasivo`, `AgregarLog`, `AgregarMensajeWebsocket`, `ObtenerDiaSiguiente`, `ObtenerDiaAnterior`, `SaldoFlujoCaja`, `ConsultarConfiguracionMesa`, `ConsultarParametroMesa`, entre otros.

Modernizado `CrearOperacionSpotService`: 10 referencias SQL, llama solo a `plataforma.callRevisarRequisitosBasicos()`.

**Impacto:** Los clientes (frontend/otros servicios) que llaman a `cl.bcs.mesa.SPT.CrearOperacionSpot` esperan `P_OUT_MENSAJES` y `P_OUT_IDOPERACIONSPOT`. El modernizado retorna campos distintos. La validación de reglas de negocio, notificaciones, facturación, y el ID de la operación creada se pierden. **Esto aplica a todos los servicios transaccionales SPT y FWD que siguen este mismo patrón de transformación.**

---

#### [CRITICA-MSA-02] CrearOperacionForwardService — output contract diferente + lógica incompleta

**FQN:** `cl.bcs.mesa.FWD.CrearOperacionForward`  
**Modernizado:** `BcsMesaServicios/src/main/java/cl/bcs/mesa/javacall/fwd/CrearOperacionForwardService.java`  
**Legado:** `BSCL/Mesa/JAVA/BcsMesaServicios/src/cl/bcs/mesa/FWD/CrearOperacionForward.java`

**Output legado:**
```
P_OUT_MENSAJES           → List<MensajeSalida>
P_OUT_ERRORESCONTADOR    → Long
P_OUT_ADVERTENCIASCONTADOR → Long
P_OUT_TBLIDFORWARD       → List<OperacionForwardObjOut>  (IDs de forwards creados)
```

**Output modernizado:**
```
P_OUT_ERRORESCONTADOR    → Long
P_OUT_OP_CALCE           → List<OperacionCalce>  (datos de consulta pre-ejecución)
P_OUT_PARAM              → List<Parametro>
P_OUT_PARAM_FWD          → List<Parametro>
P_OUT_REQUISITOS         → List<Requisito>
```

**Impacto:** Mismo patrón que MSA-01. Los IDs de las operaciones forward creadas no se retornan. Los mensajes de validación de negocio no se propagan.

---

#### [CRITICA-MSA-03] ReprocesoDatatecService — output contract diferente + campos faltantes

**FQN:** `cl.bcs.mesa.SPT.ReprocesoDatatec`  
**Modernizado:** `BcsMesaServicios/src/main/java/cl/bcs/mesa/javacall/spt/ReprocesoDatatecService.java`  
**Legado:** `BSCL/Mesa/JAVA/BcsMesaServicios/src/cl/bcs/mesa/SPT/ReprocesoDatatec.java`

**Output legado:**
```
P_OUT_MENSAJES                    → List<MensajeSalida>
P_OUT_ERRORESCONTADOR             → Long
P_OUT_ADVERTENCIASCONTADOR        → Long
P_OUT_ERRORCONFIGURACIONCONTADOR  → Long (inicializa en 0)
P_OUT_ERRORCONFIGURACIONDETALLE   → List<MensajeSalida> (errores de config MESA/AGENTE/etc.)
P_OUT_DATOSDATATEC                → List<DatosDatatecObjOut>
```

**Output modernizado:**
```
P_OUT_ERRORESCONTADOR  → Long
P_OUT_OPERACIONES      → List<OperacionDatatec>
P_OUT_PENDIENTES       → List<OperacionDatatec>
P_OUT_INFO             → List<InfoReproceso>
```

**Impacto:** El campo `P_OUT_ERRORCONFIGURACIONDETALLE` (crítico para diagnóstico de configuraciones faltantes de MESA/AGENTE/TIPO_COMPROBANTE) desaparece. El campo `P_OUT_DATOSDATATEC` se reemplaza por tres listas de estructura diferente.

---

#### [CRITICA-MSA-04] ConfirmarLiquidacionSpotService — output contract diferente (confirmado)

**FQN:** `cl.bcs.mesa.SPT.ConfirmarLiquidacionSpot`  
**Modernizado:** `BcsMesaServicios/src/main/java/cl/bcs/mesa/javacall/spt/ConfirmarLiquidacionSpotService.java`  
**Legado:** `BSCL/Mesa/JAVA/BcsMesaServicios/src/cl/bcs/mesa/SPT/ConfirmarLiquidacionSpot.java`

**Output legado:**
```
P_OUT_MENSAJES             → List<MensajeSalida>
P_OUT_ERRORESCONTADOR      → Long
P_OUT_ADVERTENCIASCONTADOR → Long
```

**Output modernizado:**
```
P_OUT_ERRORESCONTADOR → Long
P_OUT_FP              → List<FormaPago>
P_OUT_OP              → List<OperacionSpot>
P_OUT_IVA             → List<CobroIva>
P_OUT_COND            → List<CondLiq>
```

**Impacto:** El servicio retorna datos de consulta en lugar de resultado de confirmación. Los mensajes de negocio (ej: "UI_TRANSACCION_BLOQUEADA_EN_PROCESO", "OPERACION_NO_TIENE_FORMA_PAGO", "MONTO_FORMAS_PAGO_NO_COINCIDEN_CON_OPERACION") se pierden.

---

#### [CRITICA-MSA-05] ModificarOperacionSpotService — output contract diferente + 11 SQLs faltantes

**FQN:** `cl.bcs.mesa.SPT.ModificarOperacionSpot`  
**Modernizado:** `BcsMesaServicios/src/main/java/cl/bcs/mesa/javacall/spt/ModificarOperacionSpotService.java`  
**Legado:** `BSCL/Mesa/JAVA/BcsMesaServicios/src/cl/bcs/mesa/SPT/ModificarOperacionSpot.java`

**SQL en legado:** 13 archivos. **SQL en modernizado:** 3 archivos.  
SQLs ausentes en modernizado (usados en legado): `CrearOperacionSpot_InsertOperacionesSpotWrapper.sql`, `CrearOperacionSpot_InsertOperacionesSpot.sql`, `CrearOperacionSpot_ConsultarRequisitosCrearOperacionSpot.sql`, `CrearOperacionSpot_ConsultarLimitesParidadYTipoCambio.sql`, `ConsultarFormasDePagoMesa.sql`, `MODALIDAD_DE_PAGO` (inline), `INTERB`, `ARBITRAJE_INTERB`, `DISTRIBUCION`, `ARBITRAJE_DISTRIBUCION`, `ConsultarPuntasCalcularMargen.sql`.

**Output legado:**
```
P_OUT_MENSAJES, P_OUT_ERRORESCONTADOR, P_OUT_ADVERTENCIASCONTADOR, P_OUT_IDOPERACIONSPOT
```
**Output modernizado:**
```
P_OUT_ERRORESCONTADOR, P_OUT_OPERACIONES, P_OUT_PORTAFOLIOS
```

---

#### [MEDIA-MSA-06] AnularOperacionSpotService — output diferente pero SQL count correcto (8 vs 8)

**FQN:** `cl.bcs.mesa.SPT.AnularOperacionSpot`  

Legado tiene 1 SQL adicional (`SQL_SELECT_ROLES → ConsultarRolesUsuario.sql`) que el modernizado no usa. El output legado es solo `P_OUT_MENSAJES + P_OUT_ERRORESCONTADOR + P_OUT_ADVERTENCIASCONTADOR`, mientras el modernizado retorna 5 listas de datos de consulta (`P_OUT_OP`, `P_OUT_REQUISITOS`, `P_OUT_MOV_CTA/FAC/TES`).

---

#### [MEDIA-MSA-07] IngresoDatatecService — cantidad de SQL similar pero lógica de validación ausente

**FQN:** derivable desde `IngresoDatatecService.java`  
**Modernizado:** 9 SQLs cargados.  
**Legado:** Verificar lógica completa de validación Datatec.

El servicio fue corregido en la sesión anterior (nombres de SQL). Sin embargo, el output contract sigue el mismo patrón de MSA-01 (retorna datos de consulta en lugar de resultado de ingreso).

---

#### [MEDIA-MSA-08] ProcesarCargaMasivaArchivoOperacionSpotService — reescrito en sesión anterior

**FQN:** `cl.bcs.mesa.SPT.ProcesarCargaMasivaArchivoOperacionSpot`  
**Estado:** Reescrito en la sesión anterior para restaurar equivalencia con legado. El output `P_OUT_ERRORESCONTADOR + P_OUT_MENSAJES` es correcto. **Sin diferencias en esta revisión.**

---

### BcsOrdenServicios

#### [CRITICA-ORD-01] 87 servicios son stubs que no ejecutan ninguna lógica de negocio

**Servicios afectados (87 de 138 = 63%):**

```
AceptaCancelacionInstruccionMasivoService, AgregarEventoBitacoraMasivoService,
AjustarTipoCambioAsignacionService, AnticiparOrdenService, AnularAsignacionesService,
AnularInstruccionMasivoService, AnularMovFacturarAsignacionService,
AnularTransaccionMasivaService, AprobarRechazarSolicitudAnulacionOrdenesService,
AsignacionMasivaPorArchivoService, AsignacionTransaccionLiquidacionFisicaOpMarketDataService,
BorrarDocumentoInstruccionService, CalcularExigibilidadOrdenesService,
CambiarEstadoAsignacionesService, CambiarEstadoLibroOrdenesService,
CargaArchivoAsignacionRFService, CargaArchivoAsignacionRVService,
CargaArchivoCuadraturaCavaliService, CargaArchivoIGKService, CargaArchivoTransaccionBVLService,
CargaArchivoTransaccionROIXService, CargaArchivoTransaccionTOMSService,
CargaAsignacionesPostrerasService, ConfirmarInstruccionMasivoService,
ConfirmarRechazarAutorizacionAsignacionService, ConsultarClientesLibroOrdenesService,
ConsultarCobrosOrdenesPeService, ConsultarCobrosOrdenesService,
ConsultarCustodiaGarantizableService, ConsultarCustodiaIngresoOrdenService,
ConsultarFormaDePagoUltimaOrdenService, ConsultarInformacionPortafolioOrdenesService,
ConsultarInstruccionMasivoService, ConsultarInstruccionMotivoService,
ConsultarOrdenesWSService, ConsultarProcesosCuadraturaDiariaOperacionesService,
ConsultarTransaccionesAsignacionPrecioPromedioService, CrearConfiguracionShortNameService,
CrearGarantiasOrdenesService, CrearInstruccionHistoricaService, CrearInstruccionMotivoService,
CrearLibroOrdenesService, CrearLibrosPorDefectoService, CrearOrdenesMasivaService,
CrearOrdenesPostrerasService, CrearOrdenesRapidasService,
CrearSimulacionAsignacionCronologicaService, CrearSimulacionAsignacionPostreraService,
CrearSimulacionAsignacionPropCronoService, CrearSimulacionAsignacionService,
CrearTransaccionesHistoricasService, CrearTransaccionesMarketDataService,
CuadraturaDiariaService, EditarConfiguracionShortNameService,
EjecutarAsignacionPropCronoService, EjecutarDesasignacionService,
EjecutarLiberacionMasivaService, EnvioFacturacionMasivoService,
GenerarInformeEstadisticaService, GuardarConfiguracionAsignacionDirectaService,
InformeOrdenesConfirmadasService, LiquidarCustodiaOrdenCompraService,
LiquidarCustodiaOrdenVentaService, LiquidarInstruccionService, ManejadorOrdenesService,
MarketData_CrearTransaccionesService, ModificarInstruccionMotivoService,
ModificarLibroOrdenesService, NotificacionOrdenAsignadaService,
NotificarAnticipoOrdenService, NotificarCambioTransaccionService,
NotificarConfirmacionOrdenService, NotificarEstadoOrdenService,
ObtenerDocumentoInstruccionService, ObtenerTipoOperacionInstrumentoService,
PrepararOrdenesPostrerasJobService, ProcesarMensajeFixMMEService,
RechazaCancelacionInstruccionMasivoService, RechazarInstruccionMasivoService,
ReprocesarTransaccionService, ReservarLiberarTransaccionesService,
ResumenOperacionesDiariasService, ReversarAnularMovFacturarAsignacionService,
RutearOrdenesService, ServicioCumpleRequisitosModificarTransaccionService,
ValidacionArchivoCalceService, VencimientoOrdenesService
```

**Comportamiento del stub:**
```java
JsonObject out = new JsonObject();
out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
out.addProperty("_migration_status", "stub — implement from legacy X.logica()");
return out;
```

**Impacto:** Cualquier llamada a estos 87 servicios retorna éxito silencioso sin ejecutar ninguna operación de base de datos ni validación de negocio. Servicios críticos como `CuadraturaDiariaService`, `EjecutarDesasignacionService`, `CrearGarantiasOrdenesService`, `LiquidarInstruccionService`, `RutearOrdenesService` son operacionalmente no funcionales.

---

#### [MEDIA-ORD-02] AprobarRechazarSolicitudAnulacionOrdenesService — stub con output contract diferente al legado

**FQN:** `cl.bcs.ordenes.AprobarRechazarSolicitudAnulacionOrdenes`  
**Modernizado:** `BcsOrdenServicios/src/main/java/cl/bcs/ordenes/service/AprobarRechazarSolicitudAnulacionOrdenesService.java`  
**Legado:** `BSCL/Orden/JAVA/BcsOrdenServicios/src/cl/bcs/ordenes/AprobarRechazarSolicitudAnulacionOrdenes.java`

**Output legado:**
```
P_OUT_ERRORESCONTADOR      → Long
P_OUT_INFORMACIONCONTADOR  → Long
P_OUT_ADVERTENCIASCONTADOR → Long
P_OUT_ERRORESDETALLE       → List<ErrorDetalle>
P_OUT_INFORMACIONDETALLE   → List<InformacionDetalle>
P_OUT_ADVERTENCIASDETALLE  → List<AdvertenciaDetalle>
```

**Output stub:**
```
P_OUT_ERRORESCONTADOR → Long (siempre 0)
P_OUT_MENSAJES        → [] (vacío siempre)
_migration_status     → "stub..."
```

**Campos faltantes:** `P_OUT_INFORMACIONCONTADOR`, `P_OUT_ADVERTENCIASCONTADOR`, `P_OUT_ERRORESDETALLE`, `P_OUT_INFORMACIONDETALLE`, `P_OUT_ADVERTENCIASDETALLE`. El campo `_migration_status` es visible en la respuesta HTTP (contaminación del contrato).

---

#### [MEDIA-ORD-03] CrearOrdenesService — migrado como helper de reglas de negocio, no como servicio transaccional completo

**FQN:** `cl.bcs.ordenes.CrearOrdenes`  
**Modernizado:** `BcsOrdenServicios/src/main/java/cl/bcs/ordenes/service/CrearOrdenesService.java`  
**Legado:** `BSCL/Orden/JAVA/BcsOrdenServicios/src/cl/bcs/ordenes/CrearOrdenes.java` (5779 LOC, CCN=813)

El modernizado fue transformado como clase de métodos estáticos (`public static String buildIdBloqueaOrden(...)`, `public static String resolveGeneraSpot(...)`, etc.) — extrae reglas de negocio puras para testing. **No tiene un método `execute(JsonObject)` que reproduzca el comportamiento del legado como endpoint HTTP.**

**Output legado:** `P_OUT_MENSAJES + P_OUT_ERRORESCONTADOR + P_OUT_COBROS + P_OUT_GARANTIAS + P_OUT_RESULTADO`

**Impacto:** Si el frontend o algún otro servicio llama a `cl.bcs.ordenes.CrearOrdenes` como endpoint REST a través del bridge, no encontrará un handler `execute()`. La clase existe pero como librería de utilidades, no como endpoint equivalente al legado.

---

### BcsNormativaServicios

#### [MEDIA-NRM-01] AgendaGenerarNCG365MasivoService — el Javadoc indica "stub" aunque tiene estructura implementada

**FQN:** derivado del nombre de clase `cl.bcs.normativa.AgendaGenerarNCG365Masivo`  
**Modernizado:** `BcsNormativaServicios/src/main/java/cl/bcs/normativa/svc/service/AgendaGenerarNCG365MasivoService.java`

El archivo no tiene `_migration_status` en el body del método (por eso no fue detectado por grep), pero el Javadoc dice `Status: stub — business logic pending HANA connection validation`. El método `execute()` tiene estructura parcial que devuelve `P_OUT_ERRORESCONTADOR=0, P_OUT_MENSAJE="OK"` sin ejecutar los 12 SQLs definidos como campos de la clase.

**Impacto:** MENOR si el job no está activo en producción, CRITICA si está programado en Agenda de jobs.

---

### BcsJobManager

#### [MEDIA-JBM-01] RULE-059: retryFailedPrograms() habilitado cuando en legado estaba comentado

**Archivo:** `BcsJobManager/src/main/java/cl/bcs/jobmanager/adapter/out/ProgramaJdbcAdapter.java:301`

El método `retryFailedPrograms()` ejecuta `SQL_RETRY_FAILED` que en el legado (`poolDiary()`) estaba comentado: `// this.updateTryProgramm(con, nodo)`. El modernizado lo activa.

**Cambio de comportamiento:** Programas en estado `ERR` con intentos restantes serán reestablecidos a `PEN` automáticamente en cada ciclo de polling. En el legado esta reposición no ocurría automáticamente.

**Clasificación:** MEDIA — es una mejora intencional (RULE-059), pero cambia el comportamiento de recovery de jobs.

---

#### [MENOR-JBM-02] SQL_READ_PENDING tiene FIFO ORDER BY que no existía en legado

**Archivo:** `BcsJobManager/src/main/java/cl/bcs/jobmanager/adapter/out/ProgramaJdbcAdapter.java:56`

El modernizado agrega `ORDER BY P.\"fechaCreacion\" ASC` (FIFO). El legado no tenía ORDER BY. El orden de despacho de jobs puede diferir en escenarios de múltiples jobs pendientes simultáneos.

**Clasificación:** MENOR — mejora determinista, sin impacto en corrección funcional.

---

## Servicios Sin Diferencias Significativas (muestra verificada)

Los siguientes servicios de BcsMesaServicios fueron verificados y presentan equivalencia estructural (FQN correcto, SQL correctamente referenciado):
- `ProcesarCargaMasivaArchivoOperacionSpotService` — reescrito en sesión anterior, equivalente
- `IngresoDatatecService` — SQL corregidos en sesión anterior, structure OK (output pattern pendiente)
- `AnularRegistroCargaMasivaOperacionSpotService` — patrón consistente con FWD

Los siguientes servicios de BcsOrdenServicios (51 implementados) presentan estructura válida:
`ConsultarOrdenesService`, `CrearAsignacionesService`, `ModificarOrdenesService`, `CrearTransaccionesService`, `CrearAsociacionesService`, `ModificarTransaccionService`, `ConsultarPosicionService`, `ConsultarAsignacionesService`, y otros 43.

---

## Tabla de Prioridad de Remediación

| Prioridad | Hallazgo | Servicios afectados | Riesgo en producción |
|---|---|---|---|
| P1 | CRITICA-MSA-01 a MSA-05 | ~80 servicios transaccionales SPT+FWD | Alto — respuestas incorrectas en producción |
| P1 | CRITICA-ORD-01 | 87 servicios BcsOrdenServicios | Crítico — operaciones no ejecutadas |
| P2 | MEDIA-ORD-02 | AprobarRechazarSolicitudAnulacionOrdenes | Medio — contrato roto |
| P2 | MEDIA-ORD-03 | CrearOrdenes | Alto — endpoint core no disponible |
| P3 | MEDIA-NRM-01 | AgendaGenerarNCG365Masivo | Depende de scheduling |
| P3 | MEDIA-JBM-01 | retryFailedPrograms | Bajo — mejora intencional |
| P4 | MENOR-JBM-02 | ORDER BY fechaCreacion | Nulo |

---

## Notas Metodológicas

1. **Falso positivo BcsOrdenServicios SQL (sesión anterior):** Los 22 SQL "faltantes" reportados previamente en BcsOrdenServicios son un falso positivo — el método `loadSql()` de ese microservicio prepende automáticamente el prefijo `cl/bcs/ordenes/sql/`, por lo que los archivos existen.

2. **BcsNormativaServicios:** No se detectaron stubs mediante el patrón `_migration_status`. Los 164 servicios tienen código ejecutable. La verificación de equivalencia profunda requiere comparación manual adicional con el legado (`BSCL/Normativa/`).

3. **Pattern de la transformación automática en BcsMesaServicios:** El agente de transformación interpretó los servicios transaccionales como servicios de "consulta con efecto secundario" — retornando los datos pre-ejecución en lugar del resultado post-ejecución. Este patrón es sistemático y afecta a todos los servicios transaccionales del módulo.
