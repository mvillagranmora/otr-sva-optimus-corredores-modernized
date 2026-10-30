# API de produccion que asumen las pruebas de caracterizacion de BcsReplica

Las pruebas alcanzan las clases de produccion SOLO por reflexion desde `ApiReplica.java`. Mientras no existan, cada prueba falla con `AssertionError: not implemented: the production class ... does not exist yet`. Si produccion necesita otro nombre o forma, se cambia en `ApiReplica.java` (nunca en los goldens). El javadoc de `ApiReplica` tiene la misma lista con las firmas completas.

## Paquetes y clases (todas publicas; todo metodo declara `throws Exception`)
| Paquete | Clase | Contrato |
|---|---|---|
| `cl.bcs.replica.kit` | `Compuertas` | `Compuertas(boolean triggers, boolean respaldoSebra, boolean respaldoOptimus)`; bean Spring que lee `replica.compuertas.triggers`, `replica.compuertas.respaldo-sebra`, `replica.compuertas.respaldo-optimus` con valor por defecto `false` (cerradas) |
| `cl.bcs.replica.kit` | `ServicioNoServidoException` | `RuntimeException` que lanza una compuerta cerrada o un colaborador no portado (el router repite la peticion en TomEE, como en FondoMutuo) |
| `cl.bcs.replica.port.out` | `ConsultarEmpresaPort` | `Long idPais(Connection, Long idEmpresa)` (legacy `ConsultarEmpresa.logica(...).IdPais`) |
| | `ObtenerDiaSiguientePort` | `JsonObject logica(Connection, JsonObject entrada)` (JSON de `InObtenerDiaSiguiente` / `OutObtenerDiaSiguiente`: `P_OUT_ERRORESCONTADOR`, `p_out_fechas[].fechaCalculada`) |
| | `FeriadosPort` | `boolean esDiaHabil(Connection, String fecha, Long idPais)` |
| | `ConfiguracionSistemaPort` | `long parametroLong(Connection, Long idEmpresa, String modulo, String parametro)`, `String parametroString(...)` |
| | `FechaSistemaPort` | `String fechaSistema(Connection, String esquema, long idEmpresa)` (legacy `Utils.getFechaSistema(conn, "BSCL", idEmpresa)`) |
| | `SecuenciaPort` | `List<Long> siguientes(Connection, String secuencia, Long cantidad)` (legacy `Utils.getSecuencia`) |
| | `TriggersReplicaPort` | `void desactivar(Connection)`, `void reactivar(Connection)` (compuerta G-TRG) |
| | `RespaldoSebraPort` | `void respaldarPeriodo(Connection, String codPer)`, `void respaldarDiario(Connection, String codPer, String codDia)` (compuerta G-SEB) |
| | `RespaldoOptimusPort` | `void respaldarDiaActual(Connection, String fechaSistema)`, `void copiarDiaInhabil(Connection, String fechaOrigen, String fechaDestino)` (compuerta G-OPT) |
| | `ColaboradoresNoPortados` | implementa los 6 puertos de colaborador; cada metodo lanza `ServicioNoServidoException` (el valor por defecto en produccion) |
| `cl.bcs.replica.adapter.out.jdbc` | `TriggersReplicaJdbc`, `RespaldoSebraJdbc`, `RespaldoOptimusJdbc` | constructor publico sin argumentos; envian por la `Connection` recibida el SQL de `cl/bcs/replica/sql/` en el orden del legacy |
| `cl.bcs.replica.envio` | `EnviarTablaReplicaServicio` | `(Compuertas, ConsultarEmpresaPort, ObtenerDiaSiguientePort, FeriadosPort, ConfiguracionSistemaPort, TriggersReplicaPort, java.time.Clock)`; `void logica(Connection, Long idEmpresa, String proceso, Map<String,Object> parametros)`; `List<Workstack> respaldarDataDiaInhabil(Connection, Long, String fechaOrigen, String tabla, String parametros)`; `JsonObject call(Connection, JsonObject)` devuelve `null` |
| `cl.bcs.replica.respaldo` | `ReplicaUtilsServicio` | `(Compuertas, ConfiguracionSistemaPort, ConsultarEmpresaPort, ObtenerDiaSiguientePort, FeriadosPort, RespaldoSebraPort, RespaldoOptimusPort)`; `procesarRespaldoSebra`, `procesarRespaldoOptimus`, `procesarRespaldoDiarioCarterasSebra` `(Connection, String fechaSistemaEjecucion, Long idEmpresa)`; `guardarEnvioPostPauta(Connection, String tabla, String fechaSistema, String fechaEnvio)`; `JsonObject call(...)` devuelve `null` |
| `cl.bcs.replica.inicio` | `IniciarReplicaFull`, `IniciarReplicaDelta` | `(SecuenciaPort, FechaSistemaPort, java.time.Clock)`; `JsonObject logica(Connection, Long idEmpresa, Long idUsuario)` y `JsonObject call(Connection, JsonObject)` con `{"P_OUT_MENSAJES":[{id,tipo,codigo,descripcion}],"P_OUT_ERRORESCONTADOR":n}` |
| `cl.bcs.replica.domain` | `Workstack` | los campos `Key`, `Table`, `Action`, `Status`, `Stamp`, `Parameters` (campos publicos con esos nombres o los accesores `key()`, `table()`, ... de un record) |

## Decisiones de diseno que las pruebas fijan (a confirmar con el experto, ver `TRANSFORMATION_NOTES.md`)
1. Una compuerta cerrada lanza `ServicioNoServidoException` ANTES de la primera escritura y de cualquier llamada a un colaborador: G-TRG al inicio de `INICIO_PAUTA` y `FIN_PAUTA`; G-SEB solo cuando el parametro vale `S` (con `N` o ausente el comportamiento es el del legacy); G-OPT al inicio de `procesarRespaldoOptimus`. Los otros procesos y `guardarEnvioPostPauta`/`respaldarDataDiaInhabil` no pasan por compuerta.
2. `ServicioNoServidoException` NO se traga: el `catch` del legacy en `procesarRespaldoDiarioCarterasSebra` (parametro ausente = "N") la deja pasar (unica desviacion del bug-for-bug, porque un colaborador no portado nunca debe terminar en un respaldo "silencioso").
3. Los errores de colaborador (`Exception` simple del doble) y de SQL pasan sin envolver: el lote del legacy (`SQL.batch`) envuelve en `SQLException` con ` Query: ...`; `SQL.exec` deja pasar la excepcion de JDBC tal cual; las pruebas comparan la CLASE (y el mensaje solo para `java.lang.Exception`).
4. El reloj se inyecta (`java.time.Clock`): `UltimaActualizacion = LocalDateTime.now(clock) + SegundosZonaHoraria`, `Stamp` de IniciarReplica = `clock.millis()`; el `Stamp` de los Workstack de EnviarTablaReplica lo pone la base (`CURRENT_TIMESTAMP`).
5. Los 58 SQL se portan byte a byte a `src/main/resources/cl/bcs/replica/sql/` (`ReplicaSqlRecursosTest`).
