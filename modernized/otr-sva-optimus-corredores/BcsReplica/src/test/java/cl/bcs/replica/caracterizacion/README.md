# Pruebas de caracterizacion de BcsReplica (modernize-transform: pasos 1 a 3, hoy en verde)

Legacy: `legacy/otr-sva-optimus-corredores/BSCL/Replica/JAVA/BcsReplica` (`EnviarTablaReplica` 546 lineas, `ReplicaUtils` 369, `IniciarReplica`/`IniciarReplicaFull`/`IniciarReplicaDelta`, 58 SQL). Oraculo, generadores y notas: `analysis/otr-sva-optimus-corredores/equivalence/BcsReplica/` (`ORACLE_NOTES.md`).
Escritas primero en rojo (commit 8ad227ce, fallaban con `not implemented: the production class ... does not exist yet`); con la implementacion del paso 3 pasan las 469 (ver `TRANSFORMATION_NOTES.md`, "Replica: implementacion"). Si una clase de produccion cambia de nombre o forma, solo se toca `ApiReplica.java`.

## Que hay
| Archivo | Que prueba |
|---|---|
| `ApiReplica.java` | UNICO archivo que alcanza las clases de produccion (por reflexion) y que crea los puertos de colaborador a partir del guion del caso. Si produccion cambia de forma, solo se toca este archivo (`API_ASSUMED.md`). |
| `CasoReplica.java`, `VolcadoAyuda.java` | Carga de un caso (`src/test/resources/replica-caracterizacion/cases/<id>/`), ejecucion sobre H2 detras de la conexion registradora, comparacion con el golden y lectores del volcado. |
| `ReplicaEquivalenciaTest` | 264 casos + 2: salida o excepcion, SQL enviado (texto y filas de parametros, en orden), llamadas a colaboradores y tablas, contra el golden grabado del legacy. El nombre de cada caso cita las reglas que fija. |
| `ReplicaDeterminismoTest` | Cada caso dos veces mas: mismo volcado (reloj inyectado, sin estado entre peticiones); el informe del oraculo dice 20 corridas por caso y 0 inestables. |
| `ReplicaReglasTest` | 47 pruebas con el valor esperado escrito a mano: reglas `B-REP-nn` y defectos `D-REP-nn` (catalogo en `reglas.json`). |
| `ReplicaCompuertaTest` | Compuertas G-TRG / G-SEB / G-OPT cerradas por defecto, colaboradores no portados cerrados, propiedades `replica.compuertas.*`; y que con las tres cerradas todo lo que no pasa por ellas sigue equivaliendo al legacy. |
| `ReplicaSqlRecursosTest` | Los 58 SQL del legacy en `cl/bcs/replica/sql/` con el mismo SHA-256 (`sql-manifest.txt`). |
| `ReplicaCatalogoCasosTest` | La matriz: familias, ids citados cerrados contra `reglas.json`, rutas de error. |
| `../oraculo/*.java` | Arnes COMPARTIDO con el oraculo (la misma fuente, copiada byte a byte por `sync_replica.py`): conexion registradora, guion de colaboradores, volcado de tablas. No se edita aqui. |

## La matriz (264 casos, 107 con excepcion del legacy)
| Familia | Casos | Entry point legacy |
|---|---|---|
| `ENV` | 106 | `EnviarTablaReplica.logica`: INICIO_PAUTA, VALORIZAR_FFMM, PAUTA_PATRIMONIO, COPIA_PATRIMONIO_PARA_INHABIL, PAUTA_RESPALDAR_CUSTODIA, CAMBIO_FECHA_SISTEMA, VALORIZAR_CARTERA_RV_PRECIO_MERCADO, FIN_PAUTA y procesos desconocidos |
| `INH` | 15 | `EnviarTablaReplica.respaldarDataDiaInhabil` (publico) |
| `CAL` | 3 | `EnviarTablaReplica.call` y `ReplicaUtils.call` (devuelven null) |
| `SEB` | 31 | `ReplicaUtils.procesarRespaldoSebra` |
| `DIA` | 24 | `ReplicaUtils.procesarRespaldoDiarioCarterasSebra` |
| `OPT` | 21 | `ReplicaUtils.procesarRespaldoOptimus` |
| `POS` | 14 | `ReplicaUtils.guardarEnvioPostPauta` |
| `INI` | 50 | `IniciarReplicaFull` / `IniciarReplicaDelta`, `logica` y `call` |

Un caso (`case.json`): `servicio`, `ref` (reglas), `entrada`, `colaboradores` (guion de ObtenerDiaSiguiente, ConsultarEmpresa, Feriados, ConfiguracionSistema, Utils.getFechaSistema/getSecuencia), `fallos` (SQL que debe fallar: texto contenido, ocurrencia, mensaje), `tablas` a volcar, `reloj`; `seed.sql`; `expected.json` (golden). Las compuertas no estan en el caso: el legacy no las tiene; las pruebas de equivalencia las abren todas.

## Reglas y defectos
El catalogo (24 reglas `B-REP-nn`, 22 defectos `D-REP-nn`, con la referencia a las lineas del legacy congelado) esta en `reglas.json` y en `TRANSFORMATION_NOTES.md`. Cada defecto replicado es un caso con id propio (`D-REP-22`, el ciclo sin tope, solo se documenta: el legacy no puede ejecutarse hasta terminar).

## Limites H2 vs HANA
Ver `analysis/.../equivalence/BcsReplica/ORACLE_NOTES.md`: triggers (`NO_EJECUTABLE_H2`, se pina texto y orden), vistas analiticas y Sebra modeladas como tablas sembradas, `UPSERT`/`ADD_DAYS`/`CURRENT_TIMESTAMP` traducidos, `CHAR` como `VARCHAR`, sin commit implicito del DDL, sin SEBRA externa.

## Regrabar
`py -3 -I make_cases.py` -> `legacy-oracle/record.sh REP` (20 corridas) -> `py -3 -I check_replica.py` -> `py -3 -I sync_replica.py`, todo en `analysis/otr-sva-optimus-corredores/equivalence/BcsReplica/`. Nunca se edita un golden a mano.
