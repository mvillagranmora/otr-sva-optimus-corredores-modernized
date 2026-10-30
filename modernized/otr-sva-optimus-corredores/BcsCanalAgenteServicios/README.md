# BcsCanalAgenteServicios

Estado: **paso 1 de modernize-transform (pruebas de caracterizacion y oraculo, en rojo)**. No hay codigo de produccion de la logica: el `src/main` actual es el esqueleto del 2026-10-05 y se REEMPLAZA en el paso 3 (ver `TRANSFORMATION_NOTES.md`, seccion "CanalAgente: pruebas", para lo que el esqueleto daba por hecho y el legacy no hace).

## Que es el legacy
`legacy/otr-sva-optimus-corredores/BSCL/CanalAgente` (Java EE 8 / TomEE) tiene UNA clase de servicio, `cl.bcs.canalagente.ConsultarHistorialMovimientosCuentaInversion` (288 lineas, `JavaCall`), con 4 SQL de recursos, mas 3 procedimientos HANA en `BD/Servicios` y 3 scripts de prueba en `BD/Scripts`. No tiene endpoints REST propios: se invoca por el despacho `JavaCall` de la plataforma.

Flujo del servicio (`logica`):
1. `SELECT TOP 1 "IdCliente"` de `POR.Portafolio` por `p_IdPortafolio`. Si no hay fila el legacy **lanza `NullPointerException`** (el mensaje "Cliente no encontrado." se arma pero `P_OUT_ERRORESCONTADOR++` falla sobre un `Long` nulo: D-CAN-01).
2. Llama a `ConsultarCartolaDeMovimiento` (otro modulo) con `p_UsaReglasNegocio = "S"`, `p_SaldoPorFechaEfectiva = "N"`, `p_IdCuentaInversion = null`, `p_IdEstado = null`, el cliente encontrado y el resto de la entrada, incluidos `p_IdMatrizAgrupador` y `p_EsADC` (opcionales).
3. Enriquece con folio, codigo de portafolio, DTE y documento de identidad: INSERT por lote en la tabla de trabajo `BSCL.CanalAgente.BD::ConsultarHistorialMovimientosCuentaInversion_Proceso` (GLOBAL TEMPORARY, filas por sesion, borrado al commit), SELECT con 5 LEFT JOIN y DELETE.
4. Devuelve 26 campos por movimiento. Saldos inicial y final y contador de advertencias salen nulos; los errores de la cartola se descartan.

## Que hay en este modulo hoy
- `src/test/java/cl/bcs/canalagente/caracterizacion/`: 8 clases de prueba (136 pruebas), `README.md` y `API_ASSUMED.md` (la API de produccion que asumen).
- `src/test/resources/canal-caracterizacion/`: 88 casos con semilla H2 y golden grabado ejecutando el legacy, esquemas H2 generados del catalogo real de HANA QA, manifiesto SHA-256 de los 7 SQL y catalogo de reglas.
- `analysis/otr-sva-optimus-corredores/equivalence/BcsCanalAgenteServicios/`: oraculo (`legacy-oracle/record.sh`), generadores de casos, catalogo HANA y `ORACLE_NOTES.md`.
- `src/main` (esqueleto previo, sin tocar): controlador REST `/canal-agente/...`, `CanalAgenteService`, `CanalAgenteDataPort`... **no corresponden al legacy** (el legacy no tiene REST propio, devuelve un error donde el legacy lanza, exige `idEmpresa`/`idMoneda` no nulos mientras su README dice que solo `idPortafolio` es obligatorio y documenta `idMatrizAgrupador` como String, `5 tablas` donde el legacy usa 6...). No se usa como referencia.

## Diseno previsto (paso 3)
Hexagonal minimo: `service.ConsultarHistorialMovimientosCuentaInversionServicio` (misma logica y mismos SQL del legacy, con la `Connection` recibida), `port.out.ConsultarCartolaDeMovimientoPort` (colaborador no portado: por defecto `ColaboradoresNoPortados`, que lanza `ServicioNoServidoException`, tras la compuerta `canalagente.compuertas.cartola=false`), `port.out.ProcedimientosBdPort` con los 3 procedimientos como recursos SQL. Detalle y razones en `API_ASSUMED.md`.

## Como correr
`mvn -o test` desde este directorio (Java 25, `JAVA_HOME` de Temurin 25). Las pruebas fallan hasta el paso 3 con `not implemented`.
