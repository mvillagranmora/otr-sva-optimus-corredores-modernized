# BcsCanalAgenteServicios

Estado: **pasos 2-3 de modernize-transform hechos (implementacion verde)**. 136 pruebas de caracterizacion en verde (88 casos de equivalencia contra goldens del legacy ejecutado). Falta el paso de verificacion independiente y las decisiones del experto (Q-CAN-01..07 en `TRANSFORMATION_NOTES.md`). El puerto de la cartola nace CERRADO: el modulo no sirve trafico real hasta que se porte o se conecte `ConsultarCartolaDeMovimiento` y se abra la compuerta `canalagente.compuertas.cartola`.

## Que es el legacy
`legacy/otr-sva-optimus-corredores/BSCL/CanalAgente` (Java EE 8 / TomEE) tiene UNA clase de servicio, `cl.bcs.canalagente.ConsultarHistorialMovimientosCuentaInversion` (288 lineas, `JavaCall`), con 4 SQL de recursos, mas 3 procedimientos HANA en `BD/Servicios` y 3 scripts de prueba en `BD/Scripts`. No tiene endpoints REST propios: se invoca por el despacho `JavaCall` de la plataforma.

Flujo del servicio (`logica`):
1. `SELECT TOP 1 "IdCliente"` de `POR.Portafolio` por `p_IdPortafolio`. Si no hay fila el legacy **lanza `NullPointerException`** (el mensaje "Cliente no encontrado." se arma pero `P_OUT_ERRORESCONTADOR++` falla sobre un `Long` nulo: D-CAN-01). La modernizacion lo replica.
2. Llama a `ConsultarCartolaDeMovimiento` (otro modulo) con `p_UsaReglasNegocio = "S"`, `p_SaldoPorFechaEfectiva = "N"`, `p_IdCuentaInversion = null`, `p_IdEstado = null`, el cliente encontrado y el resto de la entrada, incluidos `p_IdMatrizAgrupador` y `p_EsADC` (opcionales).
3. Enriquece con folio, codigo de portafolio, DTE y documento de identidad: INSERT por lote en la tabla de trabajo `BSCL.CanalAgente.BD::ConsultarHistorialMovimientosCuentaInversion_Proceso` (en HANA GLOBAL TEMPORARY ON COMMIT DELETE ROWS; en H2 de pruebas tabla normal), SELECT con 5 LEFT JOIN y DELETE.
4. Devuelve 26 campos por movimiento. Saldos inicial y final y contador de advertencias salen nulos; los errores de la cartola se descartan.

## Que hay en este modulo
- `src/main/java/cl/bcs/canalagente/`:
  - `service/ConsultarHistorialMovimientosCuentaInversionServicio` (logica y `call`, con `Entrada`/`Salida`/`Movimiento`), `service/CartolaJson` (forma JSON del colaborador).
  - `kit/Compuertas` (G-CTA), `kit/ServicioNoServidoException`, `kit/GsonPlataforma`.
  - `port/out/ConsultarCartolaDeMovimientoPort` (JSON, el mismo contrato del legacy), `ColaboradoresNoPortados` (valor por defecto: lanza `ServicioNoServidoException`), `ProcedimientosBdPort`.
  - `adapter/out/jdbc/SqlNombrado` (semantica de `SQL`/`NamedParameterStatement` de BcsUtils), `SqlRecursos`; `adapter/out/bd/ProcedimientosBdRecursos`.
  - El modulo es un jar de libreria (sin `BcsCanalAgenteServiciosApplication`, `application.yml` ni `Dockerfile`): ver "Publicacion".
- `src/main/resources/cl/bcs/canalagente/{sql,bd/servicios}/`: los 7 SQL del legacy, byte a byte (pinados por SHA-256 en las pruebas).
- `src/test/java/.../caracterizacion/` (136 pruebas, `README.md`, `API_ASSUMED.md`) y `src/test/resources/canal-caracterizacion/` (88 casos con semilla H2 y golden grabado ejecutando el legacy).
- `analysis/otr-sva-optimus-corredores/equivalence/BcsCanalAgenteServicios/`: oraculo, generadores, catalogo HANA y `ORACLE_NOTES.md`.

## Que NO hay (y por que)
No hay controlador REST, ni handler `JavaCall`, ni `@Component` que abra la compuerta: el legacy no tiene REST propio y el despacho de `JavaCall` de la plataforma lo decide quien integre el modulo. Se retiraron los controladores, el `CanalAgenteService`, los DTO y el adaptador del esqueleto del 2026-10-05 porque describian un contrato que el legacy no tiene (respuesta de error en vez de excepcion, `idEmpresa` obligatorio, 5 tablas, etc.).

## Publicacion
Decision del usuario (2026-10-30): el modulo se publica como jar de libreria en proceso, no como servicio. El `pom.xml` ya no trae web, actuator, validacion ni el plugin de empaquetado de Spring Boot (solo JDBC, gson y el driver de HANA en runtime), y se retiraron la aplicacion, `application.yml` y el `Dockerfile`. El servicio `bcs-canal-agente-servicios` y su `depends_on` salieron de `docker-compose.yml`. La ruta `bcs-canal-agente-servicios` del `BcsGateway` sigue con `enabled: false` y no recibe trafico; queda a la vista para retirarla cuando se cierre el despacho de `cl.bcs.canalagente` en la Plataforma (hoy `BcsPlataformaWeb` apunta esa clase a `http://bcs-canal-agente-servicios:8102`, que ya no existe; con la lista de clases habilitadas vacia todo va a TomEE).

## Como correr
`mvn -o test` desde este directorio (Java 25, `JAVA_HOME` de Temurin 25). Las pruebas usan H2 en memoria; HANA nunca se toca.
