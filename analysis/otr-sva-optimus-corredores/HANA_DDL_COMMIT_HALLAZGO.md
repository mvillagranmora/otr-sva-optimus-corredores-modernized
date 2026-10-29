# Hallazgo: en HANA un DDL sobre tabla temporal local confirma (commit) implicitamente la transaccion

Fecha: 2026-10-22. Prueba autorizada por el usuario para QA (solo tablas temporales locales de sesion `#fmprobe_*`, sin objetos persistentes ni datos de negocio; conexion cerrada al final). Programa: `tools/HanaProbarDdlCommit.java`.

## Resultado medido
1. `CREATE LOCAL TEMPORARY TABLE #fmprobe_a`, commit; `INSERT` en `#fmprobe_a` (sin commit); `CREATE LOCAL TEMPORARY TABLE #fmprobe_b`; `ROLLBACK` -> **la fila del INSERT sobrevive** (el CREATE confirmo implicitamente el INSERT).
2. `INSERT` (sin commit); `DROP TABLE #fmprobe_b`; `ROLLBACK` -> **la fila sobrevive** (el DROP tambien confirma).

H2 NO se comporta asi (no confirma con `CREATE LOCAL TEMPORARY`), por eso los tests H2 no lo ven.

## Alcance
- El legacy ejecuta el mismo SQL (`CREATE LOCAL TEMPORARY TABLE #...`/`DROP TABLE #...`) sobre la misma conexion: su semantica real en HANA incluye ese commit implicito. Replicar el SQL en la misma conexion replica ese comportamiento (equivalencia) pero **rompe la promesa de diseno nueva** "una respuesta no-2xx (503 / contador>0) nunca deja datos confirmados" cuando hay escrituras antes del DDL.
- Modulos con SQL de este tipo en `src/main/resources` (conteo de archivos .sql/.java que lo contienen): APV, APVJobs, Contabilidad, CuentaInversion, Tesoreria, FondoMutuo, y los esqueletos de Mesa, Normativa, Orden, Plataforma, Custodia (por revisar). Las tablas temporales GLOBALES (p. ej. `ValidaPatrimonioEmisor`, tablas de paso de Flujo de Caja) usan solo DML (DELETE/INSERT/SELECT) y no confirman.
- Riesgo para FondoMutuo F1 (revision de arquitectura H2): la pasada de simulacion del Manejador escribe la comision NN (D-NOR-6), luego la pasada real hace `CREATE LOCAL TEMPORARY TABLE #p_Bloqueo...` (commit implicito) y despues las compuertas G1..G4 o un fallo responden 503/contador>0: las escrituras previas ya estan confirmadas y el router repetiria en TomEE (duplicado).

## Decision del usuario (2026-10-22)
**Opcion A: replicar el legacy** (el bloqueo `#p_Bloqueo...` y sus procedimientos autonomos siguen en la misma conexion de la unidad de trabajo; el commit implicito del DDL se acepta como comportamiento legacy). Consecuencias para la implementacion:
- Se conserva la semantica legacy; el DDL confirma lo escrito antes en la solicitud.
- Salvaguardas que NO cambian el comportamiento legacy de las solicitudes servidas: (1) evaluar las compuertas G1..G4 lo mas temprano posible, antes de la pasada de simulacion y del primer DDL, si se pueden decidir solo con la entrada y la configuracion; si no se pueden decidir antes, documentar el riesgo residual (503 tras una escritura ya confirmada => duplicado al reintentar en TomEE) y mantener el modulo sin trafico; (2) liberar la tabla temporal local si `bloquear` falla (H1, fuga en la conexion del pool); (3) una bandera por solicitud "ya hubo escritura HTTP/DDL confirmada" para que un 503 posterior se convierta en 500 y el router NO repita en TomEE (M3).
- La promesa "no-2xx nunca deja datos confirmados" se retira de los javadoc de `JavaCallController` y `JavaCallUnitOfWorkFilter`; se documenta la excepcion por DDL.
