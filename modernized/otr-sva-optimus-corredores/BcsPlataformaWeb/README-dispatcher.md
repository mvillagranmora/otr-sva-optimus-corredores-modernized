# Dispatcher — BcsPlataformaWeb

Este documento describe la arquitectura del dispatcher central del sistema, alojado en `BcsPlataformaWeb`. Es el punto de entrada único para todas las llamadas del frontend y los clientes externos al ecosistema de microservicios modernizados.

---

## Visión general

```
Frontend / Clientes externos
          │
          ▼
   POST/GET /call?t={IdServicio}
          │
   ┌──────▼──────────────┐
   │   CallController    │  BcsPlataformaWeb
   │  (adapter/in/rest)  │
   └──────┬──────────────┘
          │ valida sesión + resuelve PLA.Servicio
          │
          ├─ TipoServicio = P ──► ProcedureDispatcherService  ──► HANA Stored Procedure
          │
          ├─ TipoServicio = Q ──► QueryDispatcherService      ──► HANA View (OData)
          │
          └─ TipoServicio = J ──► JavaCallRouterService ──► POST /internal/java-call
                                        │                         │
                                        │   paquete mapeado       ▼
                                        │              Microservicio Spring Boot
                                        │
                                        │   paquete NO mapeado / error
                                        └──────────────────────► TomEE legacy (fallback)
```

El registro central de servicios es la tabla HANA `BSCL."PLA.Servicio"`. Cada fila define un servicio por `IdServicio` e incluye los campos:

| Campo | Descripción |
|---|---|
| `IdServicio` | Identificador numérico pasado como `?t=` en la URL |
| `TipoServicio` | Código del tipo de despacho: **P**, **Q**, **J** (ver tabla abajo) |
| `RutaServicio` | Nombre del SP, nombre de la vista, o FQN de la clase Java |
| `TablasTempIN` / `TablasTempOUT` | Parámetros TABLE-type para tipo P |
| `Extras` | FQNs adicionales (servicios extendidos) |

---

## Tipos de servicio (`TipoServicio`)

### Tipo P — Stored Procedure

**Clase:** `ProcedureDispatcherService`
**Fuente legacy:** `CallServlet` + `StoredProcedure.call()`

Invoca un stored procedure HANA del schema `BSCL` por su nombre (`RutaServicio`).

- La firma del SP se introspecciona una vez por nombre desde `SYS.PROCEDURE_PARAMETERS` y se cachea en memoria.
- Parámetros `IN` se obtienen del JSON body de la request; parámetros `OUT` se devuelven como JSON.
- Soporta parámetros `TABLE-TYPE`: el body debe incluir el parámetro como array JSON; el driver HANA ngdbc recibe un `ResultSet` vía `setObject()`.
- Si `p_out_ErroresContador > 0`, el controller devuelve el envelope de error sin commit.
- Si se pasa `?r={key}` en la URL, solo se devuelve el sub-objeto indicado del resultado.

**Protocolo:**
```
POST /call?t={IdServicio}&r={resultKey}
Header: X-SESSION-TOKEN: {token}
Body:   {"p_IdEmpresa": 1, "p_IdUsuario": 42, ...}
```

**Servicios que consumen tipo P:** cualquier microservicio o frontend que llame a `CallController` con un `IdServicio` de tipo P. El propio `CallController` en `BcsPlataformaWeb` es el único punto de entrada; los microservicios internos **no** usan este tipo directamente.

---

### Tipo Q — OData View Query

**Clase:** `QueryDispatcherService`
**Fuente legacy:** `CallServlet` + `OdataImpSql.QueryJsonObject()`

Ejecuta un `SELECT` sobre una vista HANA del schema `BSCL` usando parámetros OData estándar.

**Parámetros OData soportados:**

| Parámetro | Descripción |
|---|---|
| `$select` | Columnas a retornar (separadas por coma); si se omite, retorna `*` |
| `$filter` | Expresión de filtro OData (ver operadores abajo) |
| `$orderby` | Orden: `campo asc`, `campo2 desc` |
| `$top` | Máximo de filas a retornar |
| `$skip` | Offset de paginación |
| `$inlinecount=allpages` | Incluye `odata.count` con el total sin paginar |

**Operadores `$filter` soportados:**

| Operador | SQL equivalente |
|---|---|
| `eq`, `ne`, `lt`, `le`, `gt`, `ge` | `=`, `<>`, `<`, `<=`, `>`, `>=` |
| `and`, `or`, `not` | `AND`, `OR`, `NOT` |
| `startswith(campo,'val')` | `"campo" LIKE 'val%'` |
| `endswith(campo,'val')` | `"campo" LIKE '%val'` |
| `substringof('val',campo)` | `"campo" LIKE '%val%'` |
| `null` | `IS NULL` |

**Respuesta con `$inlinecount`:**
```json
{ "odata.count": 1500, "value": [...] }
```

**Respuesta sin `$inlinecount`:**
```json
[{ "col1": "val1", ... }, ...]
```

**Servicios que consumen tipo Q:** frontend SAPUI5 para listados, filtros y búsquedas en tiempo real. Ningún microservicio interno usa este tipo directamente.

---

### Tipo J — JavaCall Plugin (proxy a microservicio)

**Clase:** `JavaCallRouterService`
**Fuente legacy:** `CallServlet` + `PluginLoader.getObject("PLUGIN_OPTIMUS.jar", FQN)`

Despacha llamadas al microservicio Spring Boot correspondiente según el prefijo de paquete del FQN (`RutaServicio`).

El FQN puede venir en tres formatos:
```
PLUGIN_XXX.jar@cl.bcs.pkg.ClassName   ← formato legacy con plugin JAR
PLUGIN_XXX@cl.bcs.pkg.ClassName       ← formato sin extensión
cl.bcs.pkg.ClassName                   ← FQN directo
```

**Tabla de routing de paquetes → microservicio:**

| Prefijo de paquete | Microservicio | Puerto |
|---|---|---|
| `cl.bcs.plataforma` | `bcs-plataforma-servicios` | 8114 |
| `cl.bcs.socionegocio`, `cl.bcs.SocioNegocio` | `bcs-socio-negocio-servicios` | 8098 |
| `cl.bcs.ordenes` | `bcs-orden-servicios` | 8086 |
| `cl.bcs.normativa` | `bcs-normativa-servicios` | 8113 |
| `cl.bcs.mesa`, `cl.bcs.Mesa` | `bcs-mesa-servicios` | 8085 |
| `cl.bcs.custodia`, `cl.bcs.Custodia` | `bcs-custodia-servicios` | 8097 |
| `cl.bcs.fondomutuo`, `cl.bcs.fondoMutuo`, `cl.bcs.FondoMutuo` | `bcs-fondo-mutuo-servicios` | 8111 |
| `cl.bcs.APV`, `cl.bcs.apv` | `bcs-apv-servicios` | 8083 |
| `cl.bcs.tesoreria`, `cl.bcs.Tesoreria` | `bcs-tesoreria-servicios` | 8073 |
| `cl.bcs.facturacion`, `cl.bcs.Facturacion` | `bcs-facturacion-servicios` | 8099 |
| `cl.bcs.cuentaInversion` | `bcs-cuenta-inversion-servicios` | 8080 |
| `cl.bcs.contabilidad` | `bcs-contabilidad-servicios` | 8081 |
| `cl.bcs.portafolio`, `cl.bcs.Portafolio` | `bcs-portafolio-servicios` | 8076 |
| `cl.bcs.canalagente`, `cl.bcs.CanalAgente` | `bcs-canal-agente-servicios` | 8102 |

**Fallbacks (dos niveles):**

1. **Paquete no mapeado** → proxy directo a TomEE legacy (`TOMEE_URL/call?t={idServicio}`)
2. **Microservicio responde 4xx/5xx** → proxy a TomEE legacy (canary degradation)

**Protocolo interno (`POST /internal/java-call`):**
```json
{
  "class": "cl.bcs.ordenes.AnularAsignaciones",
  "params": { "p_IdEmpresa": 1, ... },
  "skip": 0,
  "take": 0
}
```
Header: `X-SESSION-TOKEN: {token}`

**Servicios que exponen `/internal/java-call`:**

| Microservicio | Clase dispatcher |
|---|---|
| `BcsOrdenServicios` | `cl.bcs.ordenes.controller.InternalJavaCallController` |
| `BcsCuentaInversionServicios` | `cl.bcs.cuentainversion.controller.InternalJavaCallController` |
| `BcsPortafolioServicios` | `cl.bcs.portafolio.controller.InternalJavaCallController` |
| `BcsCustodiaServicios` | `cl.bcs.custodia.controller.InternalJavaCallController` |
| `BcsMesaServicios` | `cl.bcs.mesa.controller.InternalJavaCallController` |
| `BcsNormativaServicios` | `cl.bcs.normativa.svc.adapter.in.InternalJavaCallController` |
| `BcsContabilidadServicios` | `cl.bcs.contabilidad.adapter.in.InternalJavaCallController` |
| `BcsAPVServicios` | `cl.bcs.apv.adapter.in.InternalJavaCallController` |
| `BcsTesoreriaServicios` | `cl.bcs.tesoreria.adapter.in.InternalJavaCallController` |
| `BcsFondoMutuoServicios` | `cl.bcs.fondomutuo.adapter.in.web.InternalJavaCallController` |
| `BcsFacturacionServicios` | `cl.bcs.facturacion.adapter.in.InternalJavaCallController` |
| `BcsSocioNegocioServicios` | `cl.bcs.socionegocio.controller.InternalJavaCallController` (por mapear) |
| `BcsCanalAgenteServicios` | `cl.bcs.canalagente.adapter.in.InternalJavaCallController` |
| `BcsPlataformaServicios` | `cl.bcs.plataforma.adapter.in.rest.InternalJavaCallController` |
| `BcsSURA` | `cl.bcs.sura.controller.InternalJavaCallController` |

---

### Tipo O — (Legacy, sin implementar)

Registrado en `SessionContext.tipoServicio` como valor posible según el modelo de dominio legacy. El `CallController` modernizado no lo implementa: cualquier servicio con `TipoServicio = O` cae en el `default` del switch y devuelve **HTTP 503**.

> **Pendiente:** Investigar si este tipo existe en `PLA.Servicio` con registros activos antes de descomisionar TomEE.

---

## Servicios pendientes de migrar desde TomEE

Los siguientes FQNs devuelven **HTTP 503** en sus respectivos `InternalJavaCallController`, lo que activa el fallback a TomEE en `JavaCallRouterService`. Cada uno está bloqueado por una dependencia no migrada indicada en el comentario inline del switch.

### BcsOrdenServicios — 45 FQNs diferidos

| FQN (`cl.bcs.ordenes.*`) | Bloqueado por |
|---|---|
| `ActualizarOrderIdHT` | `cl.bcs.plataforma.pla.ObtenerDiaSiguiente`, `utils.ConsultarEmpresa` |
| `AdjuntarDocumentoInstruccion` | `cl.bcs.socionegocio.per.pojo.Entidad` |
| `AnularAsociaciones` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `AnularOrdenMasiva` | `cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP` |
| `CalcularCantidadVisible` | `cl.bcs.plataforma.mer.CalcularParidad` |
| `CalcularCoberturaOrdenes` | `cl.bcs.plataforma.mer.CalcularParidad` |
| `CalcularMontoYCobrosOrden` | `cl.bcs.plataforma.mer.CalcularParidad` |
| `CambiarEstadoOrdenes` | `cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP` |
| `CantarOrdenes` | `cl.bcs.plataforma.pla.AgregarMensajeWebsocket` |
| `CargaArchivoOrdenesRF` | `cl.bcs.plataforma.pla.ObtenerDiaSiguiente` |
| `CargaArchivoOrdenesRV` | `cl.bcs.plataforma.pla.ObtenerDiaSiguiente` |
| `ConfirmarAutorizacionOrdenes` | `cl.bcs.plataforma.pla.AgregarMensajeWebsocket` |
| `ConfirmarOrdenes` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ConfirmarOrdenesRE` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ConsultarAsignaciones` | `cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo` |
| `ConsultarCtaCustodioDefectoPortafolio` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ConsultarCustodiaSubyacenteSimultanea` | `cl.bcs.plataforma.pla.ObtenerDiaSiguiente` |
| `ConsultarDemanda` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ConsultarDemandaAsignacion` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ConsultarInformacionAsignacionPropCrono` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ConsultarInformacionEnvioFacturacionMasivo` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ConsultarOrdenes` | `cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo` |
| `ConsultarOrdenesConSobregiroCustodia` | `cl.bcs.plataforma.pla.ObtenerDiaSiguiente` |
| `ConsultarPosicion` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ConsultarPosicionAsignacion` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ConsultarSolInstruccionRechazada` | `cl.bcs.plataforma.pla.ObtenerDiaAnterior` |
| `CrearAsignaciones` | `cl.bcs.plataforma.mer.CalcularParidad` |
| `CrearAsociaciones` | `cl.bcs.plataforma.mer.CalcularParidad` |
| `CrearInstrucciones` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `CrearOrdenes` | `cl.bcs.plataforma.pla.AgregarMensajeWebsocket` |
| `CrearTransaccionInstruccionMasivo` | `cl.bcs.socionegocio.soc.pojo.ResumenUsuario` |
| `CrearTransacciones` | `cl.bcs.plataforma.mer.CalcularParidad` |
| `CrearTransaccionesBolsaElectronica` | `cl.bcs.plataforma.pla.ObtenerDiaSiguiente` |
| `CrearTransaccionesMarketDataBEC` | `cl.bcs.plataforma.pla.ObtenerPlazo` |
| `EnviarAsignacionesFacturar` | `cl.bcs.plataforma.pla.ObtenerPlazo` |
| `ModificarInstrucciones` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ModificarOrdenes` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ModificarTransaccion` | `cl.bcs.plataforma.pla.ObtenerPlazo` |
| `ModificarTransaccionMarketData` | `cl.bcs.plataforma.pla.ObtenerDiaSiguiente` |
| `ObtenerDetalleRefacturacion` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `PrevisualizarDesasignacion` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ProcesarCargaMasiva` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |
| `ProcesarCargaMasivaInstruccion` | `cl.bcs.plataforma.pla.ObtenerDiaSiguiente` |
| `ProcesarMensajeFix` | `cl.bcs.plataforma.pla.ObtenerPlazo` |
| `RefacturarMovimiento` | `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` |

### Dependencias compartidas que desbloquean múltiples FQNs

Migrar estas clases en BcsPlataformaServicios desbloquea el mayor número de FQNs diferidos:

| Clase bloqueante (legacy FQN) | FQNs que desbloquea |
|---|---|
| `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` | 18 FQNs en BcsOrdenServicios |
| `cl.bcs.plataforma.mer.CalcularParidad` | 6 FQNs en BcsOrdenServicios |
| `cl.bcs.plataforma.pla.ObtenerDiaSiguiente` | 7 FQNs en BcsOrdenServicios |
| `cl.bcs.plataforma.pla.AgregarMensajeWebsocket` | 3 FQNs en BcsOrdenServicios |
| `cl.bcs.plataforma.pla.ObtenerPlazo` | 4 FQNs en BcsOrdenServicios |
| `cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo` | 2 FQNs en BcsOrdenServicios |

### CallBatch — pendiente de sustitución

`POST /CallBatch` (stub en `CallBatchController`) devuelve siempre **HTTP 503**. El plugin `PLUGIN_OPTIMUS.jar` que cargaba trabajos en background no existe en el entorno modernizado. Requiere reemplazarse con una cola de tareas (ej. Spring Batch, AWS SQS, o similar).

---

## Flujo de autenticación

Antes de cualquier despacho, `CallController` llama a `SessionValidatorService.validate()`, que:

1. Verifica el token en `ACC.Sesion` (tabla HANA).
2. Resuelve los metadatos del servicio desde `PLA.Servicio`.
3. Verifica que el usuario tenga acceso al servicio en `ACC.PerfilServicio`.
4. Retorna un `SessionContext` inmutable con `idEmpresa`, `idUsuario`, `tipoServicio`, `rutaServicio`.

Si la sesión es inválida → **HTTP 401**.
Si el servicio no existe → **HTTP 404**.
Si el usuario no tiene acceso → **HTTP 401**.

---

## Archivos clave

| Archivo | Rol |
|---|---|
| `adapter/in/CallController.java` | Entrada principal `/call`; switch por `TipoServicio` |
| `service/ProcedureDispatcherService.java` | Dispatcher tipo P (stored procedures) |
| `service/QueryDispatcherService.java` | Dispatcher tipo Q (OData views) |
| `service/JavaCallRouterService.java` | Dispatcher tipo J (routing a microservicios) |
| `service/SessionValidatorService.java` | Validación de sesión y resolución de metadatos |
| `adapter/in/CallBatchController.java` | Stub `/CallBatch` (devuelve 503; pendiente) |
| `adapter/in/TransaccionWSController.java` | Idempotencia WS (`/ProcesarIdTransaccion`) |
| `domain/SessionContext.java` | Registro inmutable del contexto autenticado |
