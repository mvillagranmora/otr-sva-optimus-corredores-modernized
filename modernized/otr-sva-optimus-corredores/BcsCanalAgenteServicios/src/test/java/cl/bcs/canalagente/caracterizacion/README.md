# Pruebas de caracterizacion de BcsCanalAgenteServicios (modernize-transform: escritas en rojo en el paso 1; en verde tras la implementacion)

Legacy: `legacy/otr-sva-optimus-corredores/BSCL/CanalAgente` (`ConsultarHistorialMovimientosCuentaInversion` 288 lineas, 4 SQL de recursos, 3 procedimientos de `BD/Servicios`). Oraculo, generadores y notas: `analysis/otr-sva-optimus-corredores/equivalence/BcsCanalAgenteServicios/` (`ORACLE_NOTES.md`).
Estado actual: las 136 pruebas pasan contra la implementacion de `src/main` (ver `TRANSFORMATION_NOTES.md`, seccion "CanalAgente: implementacion"). En el paso 1 se escribieron en rojo y fallaban solo con `not implemented: the production class ... does not exist yet` (o `SQL resources missing`); ese es el estado del commit 31d7cb2b.

## Que hay
| Archivo | Que prueba |
|---|---|
| `ApiCanalAgente.java` | UNICO archivo que alcanza las clases de produccion (por reflexion) y que crea el puerto de la cartola a partir del guion del caso. Si produccion cambia de forma, solo se toca este archivo (`API_ASSUMED.md`). |
| `CasoCanal.java`, `VolcadoAyuda.java`, `BdEfimeraH2.java` | Carga de un caso (`src/test/resources/canal-caracterizacion/cases/<id>/`), ejecucion sobre H2 detras de la conexion registradora, comparacion con el golden y lectores del volcado. `BdEfimeraH2` (copia del de FondoMutuo) hace `SHUTDOWN` de cada base al cerrarla: sin eso cientos de bases H2 agotan el heap. |
| `CanalEquivalenciaTest` | 88 casos + 2: salida o excepcion, SQL enviado (texto y filas de parametros, en orden), llamadas al colaborador y tablas, contra el golden grabado del legacy. El nombre de cada caso cita las reglas que fija. |
| `CanalDeterminismoTest` | Cada caso dos veces mas: mismo volcado; el informe del oraculo dice 20 corridas por caso y 0 inestables. |
| `CanalReglasTest` | 29 pruebas con el valor esperado escrito a mano: reglas `B-CA-nn` y defectos `D-CAN-nn` (catalogo en `reglas.json`), incluidos los textos de los procedimientos de BD. |
| `CanalCompuertaTest` | Compuerta G-CTA y puerto de la cartola cerrados por defecto, propiedad `canalagente.compuertas.cartola`, y que con la compuerta abierta todo equivale al legacy. |
| `CanalSqlRecursosTest` | Los 7 SQL del legacy en `cl/bcs/canalagente/{sql,bd/servicios}/` con el mismo SHA-256 (`sql-manifest.txt`) y el puerto de procedimientos. |
| `CanalCatalogoCasosTest` | La matriz: familias, ids citados cerrados contra `reglas.json`, rutas de error. |
| `../oraculo/*.java` | Arnes COMPARTIDO con el oraculo (la misma fuente, copiada byte a byte por `sync_canal.py`): conexion registradora, guion de colaboradores, volcado de tablas, ejecucion del SELECT de un procedimiento. No se edita aqui. |

## La matriz (88 casos, 22 con excepcion del legacy)
| Familia | Casos | Entry point legacy |
|---|---|---|
| `LOG` | 56 | `ConsultarHistorialMovimientosCuentaInversion.logica` (entrada tipada): enriquecimiento, delegacion, cliente no encontrado, errores de la cartola, respuestas defectuosas, ids repetidos y filas ajenas, fallos de la base |
| `CAL` | 19 | `ConsultarHistorialMovimientosCuentaInversion.call` (JSON de entrada y de salida) |
| `PRC` | 13 | el `SELECT` final de `BD/Servicios/ConsultarHistorialMovimientosCuentaInversion.sql` (8) y de `ConsultaAvanzadaClientes.sql` (5), ejecutado en H2 |

Un caso (`case.json`): `servicio`, `ref` (reglas), `entrada`, `colaboradores` (guion de `ConsultarCartolaDeMovimiento.logica`), `fallos` (SQL que debe fallar: texto contenido, ocurrencia, mensaje), `tablas` a volcar, `esquemasExtra`; `seed.sql`; `expected.json` (golden). La compuerta no esta en el caso: el legacy no la tiene; las pruebas de equivalencia la abren.

## Reglas y defectos
El catalogo (13 reglas `B-CA-nn`, 17 defectos `D-CAN-nn`, con la referencia a las lineas del legacy congelado) esta en `reglas.json` y en `TRANSFORMATION_NOTES.md`. Cada defecto replicado es un caso con id propio; `D-CAN-14` (cabecera y parametro comentado del procedimiento de custodia), `D-CAN-16` y `D-CAN-17` no tienen caso que se ejecute (texto fijado por SHA-256 y prueba de texto, o solo documentacion). Ningun `RULE-nnn` de `BUSINESS_RULES.md` describe este modulo.

## Limites H2 vs HANA
Ver `analysis/.../equivalence/BcsCanalAgenteServicios/ORACLE_NOTES.md`: tabla de trabajo (GLOBAL TEMPORARY ON COMMIT DELETE ROWS en HANA, tabla normal en H2), orden del SELECT sin ORDER BY, `TO_VARCHAR`, procedimientos SQLScript solo en HANA (se ejecuta el SELECT final con tablas de apoyo), centinela `0001-01-01`.

## Regrabar
`py -3 -I make_schema.py` -> `py -3 -I make_cases.py` -> `legacy-oracle/record.sh CAN` (20 corridas) -> `py -3 -I check_canal.py` -> `py -3 -I sync_canal.py`, todo en `analysis/otr-sva-optimus-corredores/equivalence/BcsCanalAgenteServicios/`. Nunca se edita un golden a mano.
