# API de produccion que asumen las pruebas de caracterizacion de BcsCanalAgenteServicios

Las pruebas alcanzan las clases de produccion SOLO por reflexion desde `ApiCanalAgente.java`. Si no existieran (estado del paso 1, hoy ya implementadas), cada prueba fallaria con `AssertionError: not implemented: the production class ... does not exist yet`. Si produccion necesita otro nombre o forma, se cambia en `ApiCanalAgente.java` (nunca en los goldens). El javadoc de `ApiCanalAgente` tiene la misma lista con las firmas completas.

## Paquetes y clases (todas publicas; todo metodo declara `throws Exception`)
| Paquete | Clase | Contrato |
|---|---|---|
| `cl.bcs.canalagente.kit` | `Compuertas` | `Compuertas(boolean cartola)`; bean Spring (constructor unico con `@Value`) que lee `canalagente.compuertas.cartola` con valor por defecto `false` (cerrada) |
| `cl.bcs.canalagente.kit` | `ServicioNoServidoException` | `RuntimeException` que lanza una compuerta cerrada o un colaborador no portado (el router repite la peticion en TomEE, como en FondoMutuo) |
| `cl.bcs.canalagente.port.out` | `ConsultarCartolaDeMovimientoPort` | `JsonObject consultar(Connection, JsonObject entrada)`: la forma JSON de `EntradaConsultarCartolaDeMovimiento` / `SalidaConsultarCartolaDeMovimiento` del legacy (nombres de campo intactos, igual que el `Colaboradores` de BcsCuentaInversionServicios). La entrada lleva los valores por omision del legacy (`p_Validaciones` "S", `p_IncluyeSaldo` "N", `p_IncluyeAnulados` "S"). Una respuesta nula es `null` |
| `cl.bcs.canalagente.port.out` | `ColaboradoresNoPortados` | implementa el puerto; `consultar` lanza `ServicioNoServidoException` (el valor por defecto en produccion) |
| `cl.bcs.canalagente.port.out` | `ProcedimientosBdPort` | `String texto(String nombre)` (nombres: `ConsultaAvanzadaClientes`, `ConsultarHistorialMovimientosCuentaInversion`, `ConsultarHistorialMovimientosCustodia`) |
| `cl.bcs.canalagente.adapter.out.bd` | `ProcedimientosBdRecursos` | constructor publico sin argumentos; lee `cl/bcs/canalagente/bd/servicios/<nombre>.sql` |
| `cl.bcs.canalagente.service` | `ConsultarHistorialMovimientosCuentaInversionServicio` | `(Compuertas, ConsultarCartolaDeMovimientoPort)`; `Salida logica(Connection, Entrada)`; `JsonObject call(Connection, JsonObject)` |
| (anidadas) | `Entrada`, `Salida`, `Movimiento`, `AdvertenciasDetalle`, `ErroresDetalle` | clases publicas estaticas con campos publicos y los nombres del legacy; `Movimiento` con sus 26 campos y fechas `java.sql.Date` (conservan la hora y salen como `yyyy-MM-dd HH:mm:ss.SSS`) |

## Recursos de produccion que asumen las pruebas (`src/main/resources/cl/bcs/canalagente/`)
- `sql/ConsultarHistorialMovimientosCuentaInversion_{SelectIdClientePortafolio,InsertCartolaDeMovimientoComprobante,SelectCartolaDeMovimientoComprobante,DeleteCartolaDeMovimientoComprobante}.sql` y `bd/servicios/{ConsultaAvanzadaClientes,ConsultarHistorialMovimientosCuentaInversion,ConsultarHistorialMovimientosCustodia}.sql`: los 7 byte a byte (`CanalSqlRecursosTest`, `sql-manifest.txt`).

## Decisiones de diseno que las pruebas fijan (a confirmar con el experto, ver `TRANSFORMATION_NOTES.md`)
1. La cartola es un colaborador NO portado dentro de este modulo: queda detras de `ConsultarCartolaDeMovimientoPort` con compuerta G-CTA cerrada por defecto. Por que no se reutiliza `ConsultarCartolaDeMovimientoService` de BcsCuentaInversionServicios tal cual: es un `@Service` con su propio `NamedParameterJdbcTemplate` y `@Transactional` (no usa la `Connection` que recibe el modulo), su arbol de colaboradores (`Colaboradores`, saldo, movimientos) es de otro contexto Spring y su `Salida` es un tipo de otro modulo. Su contrato JSON si es el mismo del legacy, asi que el adaptador real (paso 3) se escribe contra el puerto (jar plano de cuenta inversion en proceso, o llamada JavaCall) y las pruebas usan un doble.
2. Compuerta cerrada = `ServicioNoServidoException` ANTES del primer SQL y de cualquier llamada al colaborador (tambien en los caminos que en el legacy fallan antes: cliente no encontrado, fallo del SELECT del cliente; la lectura del JSON de `call` ocurre antes de la compuerta y no esta fijada). Con la compuerta abierta y el puerto no portado, falla tras la busqueda del cliente y antes de la primera escritura.
3. Las excepciones se comparan por CLASE (y por mensaje solo para `java.lang.Exception` simple): el legacy envuelve las de SQL en `SQLException` con ` Query: ...`.
4. Se replican los defectos del legacy (D-CAN-01 a D-CAN-12): el cliente no encontrado lanza `NullPointerException`; saldos y contador de advertencias nulos; errores de la cartola descartados; fallo a mitad sin limpiar; hash por id con el ultimo gana. La unica desviacion deliberada es la compuerta (el legacy no la tiene).
5. El modulo no hace commit, rollback ni cierra la conexion (B-CA-13: la conexion registradora anota cualquier `transaccion`).
