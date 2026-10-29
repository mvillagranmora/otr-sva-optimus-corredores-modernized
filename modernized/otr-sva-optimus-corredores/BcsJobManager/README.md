# BcsJobManager

## Descripción

Despachador de la cola de programas batch (`BAT.Programa`) hacia los módulos de jobs modernizados. Cada ciclo selecciona, **solo lectura**, los programas elegibles de los tipos habilitados y hace `POST /internal/job-call` al módulo de jobs que corresponde. **No reclama, no cambia el estado de ningún programa y no reintenta**: el módulo de jobs reclama el programa de forma atómica (`Bloqueo='S'`, `ASI`), carga sus parámetros de la vista `BAT.ConsultarParametrosProgramas`, ejecuta el job y escribe el estado final (`TER`/`ERR`).

**INERTE POR DEFECTO.** Con `jobmanager.tipos-habilitados` vacío (valor por defecto) no hace nada: ni consulta la cola, ni registra el nodo, ni llama a ningún módulo. Es el mismo patrón de `plataforma.javacall.clases-habilitadas`.

## Tecnología

- **Framework:** Spring Boot 4.1.1 / Java 25
- **Puerto:** 8106
- **Artifact:** `cl.bcs:bcs-job-manager`
- **Patrón de arquitectura:** Hexagonal (domain / port / adapter)

## Contrato

**Selección** (`ProgramaJdbcAdapter`, mismo predicado que `TransactionManager.readProgramm` del legacy, sobre columnas existentes): `Bloqueo='N'`, `idEstado='PEN'`, `Nodo = jobmanager.node`, `Intento < MaximoIntentos`, `fechaEjecucion` entre hoy 00:00 UTC y ahora, `IdTipoPrograma IN (tipos-habilitados)`. Orden por `fechaCreacion`. Es un `SELECT` simple: **sin `FOR UPDATE` y sin `SKIP LOCKED`** (el reclamo atómico lo hace el módulo de jobs, no este proceso).

**Llamada:** `POST {destino}/internal/job-call`, cuerpo `{"idPrograma": N, "clase": "<TipoPrograma.NombreClasePrograma>"}`, cabecera `X-Internal-Token`. Destino por `jobmanager.destinos` (clave = `IdTipoPrograma` exacto, o prefijo de paquete de la clase, gana el más largo). Timeout de conexión 2 s; timeout de lectura generoso (`read-timeout-ms`, 30 min), porque el job corre síncrono dentro de la llamada.

| Respuesta | Acción del despachador (nunca toca `BAT.Programa`) |
|---|---|
| 200 | hecho |
| 409 | omitir (ya reclamado por el módulo) |
| 404 / 422 | `ERROR` en el log con el id del programa (inconsistencia de datos o configuración) |
| 503 | `WARN` (el módulo no ejecuta esa clase: revisar `destinos`) |
| 500 | nada (el módulo ya escribió `ERR`) |
| timeout de lectura | no es fallo del job; el módulo es dueño del estado |
| conexión rechazada / error antes de enviar | nada; el programa sigue `PEN` y se vuelve a seleccionar en el siguiente ciclo (seguro: nada fue reclamado) |
| otro estado (400, 401...) | `ERROR` en el log (token o URL mal configurados), sin cambios |

**Protecciones:** guarda en vuelo por programa (el mismo `IdPrograma` nunca se envía dos veces a la vez), ejecutor acotado (`concurrent-jobs` hilos + cola `queue-capacity`; lo que no cabe sigue `PEN`), apagado ordenado (`shutdown-wait-ms`). Los programas de agendas demonio (`BAT.Agenda.EsDemonio='S'`) no se despachan (RULE-061, sin modo demonio) y se registran en el log.

## Configuración

| Propiedad / variable | Descripción | Default |
|---|---|---|
| `jobmanager.tipos-habilitados` / `JM_TIPOS_HABILITADOS` | `IdTipoPrograma` que se despachan (lista separada por comas). Vacío = inerte | vacío |
| `jobmanager.destinos` | mapa tipo o prefijo de paquete -> URL base del módulo. Claves con punto entre corchetes: `"[cl.bcs.tesoreria]": http://host:puerto` | vacío |
| `jobmanager.internal-token` / `INTERNAL_TOKEN` | secreto enviado en `X-Internal-Token` | vacío |
| `jobmanager.node` / `JM_NODE` | valor de `BAT.Programa.Nodo` a seleccionar (`''` o `*` = nodo vacío; la columna es `NVARCHAR(1)`: **`jobmanager.node` debe ser UN solo carácter**) | `LOCAL` |
| `jobmanager.cooldown-no-entregable` | enfriamiento en memoria por programa tras 404, 422 o 503 (duración ISO-8601; `PT0S` lo desactiva). No aplica a conexión rechazada/timeout de conexión, 409, 200 ni 500 | `PT60S` |
| `jobmanager.connect-timeout-ms` / `read-timeout-ms` (`JM_READ_TIMEOUT_MS`) | timeouts de la llamada | 2000 / 1800000 |
| `jobmanager.concurrent-jobs` (`JM_CONCURRENT_JOBS`), `queue-capacity`, `shutdown-wait-ms` | hilos, cola y espera de apagado | 4, 100, 30000 |
| `jobmanager.poll-interval-ms` (`JM_POLL_INTERVAL_MS`) | intervalo de sondeo | 1000 |
| `jobmanager.mode` (`JM_MODE`) y `jobmanager.agenda.habilitada` | agenda/diario: ver más abajo | `PROGRAMA` / `false` |
| `jobmanager.task-type` (`JM_TASK_TYPE`), `process-id` | solo para registrar el nodo en `BAT.Nodos`; **ya no filtra la selección** | vacío |
| `HANA_URL`, `HANA_USER`, `HANA_PASSWORD` | conexión a SAP HANA (sin credenciales en el código) | requeridas |

## Agenda / diario (creación de programas desde `BAT.Agenda`)

**Apagado por defecto y a cargo del Job Manager legacy** hasta el corte global: el legacy crea los programas (`BAT.AgregarPrograma`) y dos creadores duplicarían los programas. Solo corre con `jobmanager.mode=AGENDA` **y** `jobmanager.agenda.habilitada=true`. Las agendas demonio se omiten (RULE-061). Sin reintento automático.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/admin/health` | Health check con verificación de conectividad a HANA |
| `GET` | `/api/admin/zombies?timeoutSeconds=30` | Lista nodos cuyo heartbeat ha expirado |

`POST /api/admin/retry` fue eliminado: el despachador no cambia el estado de los programas.

## Corte global: lista de activación (producción)

No existe un corte seguro por tipo mientras el Job Manager legacy corre: el legacy selecciona por `Nodo` (y a lo más UN tipo) y no puede excluir tipos; su reclamo no es atómico. Ver `analysis/otr-sva-optimus-corredores/JOBS_CUTOVER_ANALYSIS.md`. La activación es **una sola decisión global**:

1. Todos los tipos que procesa el manager legacy del nodo están migrados y probados.
2. **Detener el Job Manager legacy de ese nodo** (todos sus procesos).
3. Configurar `jobmanager.node` con el nodo real (`''` si los programas usan nodo vacío; confirmar con el DBA los argumentos de arranque reales del legacy).
4. Configurar `jobmanager.destinos` (un destino por módulo de jobs o por tipo) y `INTERNAL_TOKEN` igual al de los módulos de jobs.
5. Configurar `jobmanager.tipos-habilitados` con **todos** los tipos migrados.
6. Si además debe crear programas: `jobmanager.mode=AGENDA` y `jobmanager.agenda.habilitada=true` en UNA sola instancia (el legacy ya detenido).
7. Verificar en el log el aviso `ACTIVE for types ...` y que no haya `ERROR` de 404/422/503.

## Lo que NO hace, a propósito

- No reclama programas ni escribe `Bloqueo`, `Intento`, `idEstado`, `Resultado`, `fechaFin` (lo hace el módulo de jobs).
- No reintenta: el legacy no reintentaba (`updateTryProgramm` comentado, `BcsJobManager.java:104-105`); un fallo queda `ERR` y se relanza a mano (`ReprocesarPrograma`). RULE-059 queda deshabilitada.
- No carga parámetros (los lee el módulo de jobs desde la vista) ni ejecuta el job en proceso.
- No despacha demonios (RULE-061). No mata procesos de nodos zombie (solo los registra).
- No cambia nada en la base de datos (tablas, columnas, vistas, procedimientos, datos de configuración) ni toca el legacy; el legacy nunca llama a este código.
- Un programa que no se pudo entregar (módulo caído, 503, 404...) sigue `PEN`. Módulo caído (conexión rechazada/timeout de conexión) se vuelve a seleccionar en cada ciclo. Con 404, 422 o 503 (inconsistencia de configuración/datos) el programa entra en un enfriamiento en memoria (`jobmanager.cooldown-no-entregable`, `PT60S`): por programa, sin escribir en la base, se olvida al expirar o cuando deja de estar en el conjunto elegible, tamaño acotado (1000). Mientras dura se omite en `DEBUG`; la primera omisión de cada enfriamiento se registra una vez en `INFO`. Sin reintento ni reclamo.
- La ventana de fecha se calcula en UTC en Java (el legacy usaba `CURRENT_DATE` del servidor HANA para el límite inferior): igual si el servidor HANA está en UTC.
