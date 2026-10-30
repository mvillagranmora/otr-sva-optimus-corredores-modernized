# Oraculo BcsReplica (modernize-transform, paso 1): notas

Modulo: `legacy/otr-sva-optimus-corredores/BSCL/Replica/JAVA/BcsReplica` (`EnviarTablaReplica` 546 lineas, `ReplicaUtils` 369, `IniciarReplica`, `IniciarReplicaFull`, `IniciarReplicaDelta`, 58 SQL).
Modelo: `../BcsFondoMutuoServicios/` (T1/T2 de certificacion). Sin HANA de escritura, sin red, sin credenciales en el oraculo: todo corre en H2 en memoria. `legacy/` solo se lee.

## Uso
- `py -3 -I make_schema.py` escribe `schema-replica.sql` desde `catalog/replica_*.csv` (catalogo REAL de HANA QA, ver mas abajo).
- `py -3 -I make_cases.py` escribe las ENTRADAS de los 264 casos en `cases/` (`case.json`, `seed.sql`); borra antes todo `cases/REP-*` (un caso renumerado nunca conserva un golden viejo).
- `legacy-oracle/record.sh REP` graba `cases/<caso>/expected.json` ejecutando las clases legacy, 20 corridas por caso en bases H2 nuevas (`ORACLE_REPEAT`); un caso inestable no recibe golden (exit 4) y queda en `determinism.txt`.
  Perillas: `ORACLE_WORK` (directorio temporal), `ORACLE_SKIP_COMPILE=1`, `ORACLE_CASES`, `ORACLE_REPORT`, `JAVA_HOME`, `M2_REPO`, `PYTHON_CMD`.
- `py -3 -I check_replica.py` (consistencia: golden con procedencia, ids de `reglas_rep.py` citados y cerrados, determinismo 0 inestables), `py -3 -I make_sql_manifest.py` (SHA-256 de los 58 SQL), `py -3 -I sync_replica.py [modulo]` (copia byte a byte a `modernized/.../BcsReplica/src/test/resources/replica-caracterizacion/` y las 3 fuentes del arnes compartido a `src/test/java/cl/bcs/replica/oraculo/`).
- `tools/HanaCatalogoReplica.java`: SELECT de solo lectura sobre vistas SYS de HANA QA (columnas, claves primarias, tipos de objeto, secuencias; credenciales de `.env` leidas en memoria, nunca impresas); su salida esta en `catalog/`.

## Que es real y que es doble
- REAL (fuente legacy tal cual): `ReplicaUtils`, `IniciarReplica`; BcsUtils `SQL`, `NamedParameterStatement`, `NamedCallableStatement`, `JavaCall`, `JavaCallMetadata`, `Gson*Deserializer`, `Fecha`, `BcsPojo`; Plataforma `PlataformaUtils` (`MensajeSalida`), `Diccionario`, `FileLoader`, `BarraProgreso`; los 58 SQL (en el classpath).
- COPIAS con UNA edicion, el reloj: `EnviarTablaReplica` (`LocalDateTime.now()` -> `cl.bcs.oracle.Clock.now()`: el original estampa `ResumenReplica.UltimaActualizacion` con la hora real) e `IniciarReplicaFull/Delta` (`System.currentTimeMillis()` -> `Clock.millis()`).
  `record.sh` ABORTA si una copia difiere del legacy en algo mas que esas lineas (mismo precedente que la copia de `FechaDB` del oraculo de FondoMutuo).
- DOBLES REGISTRADOS (colaboradores fuera del modulo): `ObtenerDiaSiguiente` y `ConsultarEmpresa` generados desde el fuente real por `tools/gen_doubles.py` (todas las declaraciones del original), `ConfiguracionSistema` y `Feriados` escritos a mano con la firma estatica real,
  y el shim `Utils` generado por `tools/gen_utils.py` (metodos copiados textuales; `getSecuencia` y `getFechaSistema` responden por guion). Cada llamada se registra (colaborador, metodo, argumentos sin la conexion y sin nulos, respuesta) y se responde desde `colaboradores` del caso
  (`cuando` por ruta, `defecto`, `error`, `una_vez`); sin respuesta configurada el doble FALLA (el caso debe declarar lo que alcanza). Claves: `Colaborador.metodo`.
- Conexion JDBC: `legacy-oracle/harness/cl/bcs/replica/oraculo/ConexionRegistradora.java` (la MISMA fuente que usan las pruebas JUnit). Registra cada SQL que llega a la base (sin comentarios, espacios colapsados; los textos de mas de 300 caracteres se guardan como 120 primeros + 200 ultimos + largo + SHA-256) y, para un lote, las filas de parametros.
  `GuionColaboradores.java` y `VolcadoTablas.java` tambien son compartidos.

## Limites H2 vs HANA (documentados, no ocultos)
1. **Triggers**: `CREATE TRIGGER` / `DROP TRIGGER` no existen en H2 como en HANA: se registran como `NO_EJECUTABLE_H2` y no tienen efecto. Se pina el TEXTO y el ORDEN (golden de las sentencias); no su efecto en HANA. Vale para INICIO_PAUTA y FIN_PAUTA.
2. **Vistas analiticas con PLACEHOLDER** (`ConsultarSaldoCuentaInversionFecha{Efectiva,Movimiento}Analitica`, `CUS.ConsultarCustodiaFecha{Operacion,Liquidacion}Analitica`) y vistas Sebra: se modelan como TABLAS H2 con las columnas de la tabla destino (SUPUESTO; el catalogo confirma que las 3 vistas Sebra consultadas tienen todas las columnas del destino mas `IdPortafolio` y `Tipo`) que el caso siembra; se descartan los argumentos `PLACEHOLDER`.
   Por eso el historico "se inserta" de verdad y la copia de dias inhabiles lo ve. Lo que entregan en HANA (consultas reales sobre cuenta inversion y custodia) NO se ejecuta aqui.
3. `UPSERT ... WITH PRIMARY KEY` -> `MERGE ... KEY(<clave real del catalogo>)`; `ADD_DAYS('x', n)` -> `DATEADD('DAY', n, CAST('x' AS TIMESTAMP))` (HANA acepta el texto con hora o sin ella; H2 necesita el cast); `CURRENT_TIMESTAMP` -> el reloj fijo del caso; una sentencia con varios `;` se parte (el legacy no la usa: cada SQL tiene una sola).
4. Tipos: `CHAR(n)`/`NVARCHAR(n)` -> `VARCHAR(n)` (sin relleno de CHAR); `SECONDDATE` -> `TIMESTAMP(0)`. Las claves primarias, NOT NULL, defaults, longitudes (`WorkStack.Parameters` VARCHAR(500), `EnvioPostPauta.*` NVARCHAR(10/100/10)) y la identidad de `WorkStack.IdStack` son las REALES.
5. Sin concurrencia real, sin plan de ejecucion, sin orden de filas sin ORDER BY (los volcados ordenan por clave), sin las vistas/tablas externas reales de SEBRA (base externa), sin la semantica de transaccion de HANA: DDL de trigger confirma implicitamente en HANA (`HANA_DDL_COMMIT_HALLAZGO.md`) y H2 no lo hace; las pruebas NO deciden commit/rollback (el llamador decide), solo el estado de la sesion al terminar.
6. El conteo de filas afectadas (`filasAfectadas`) es el de H2; HANA devuelve -2 en lotes.

## Supuestos del esquema (SUPUESTO = no verificado en HANA)
- REAL (catalogo QA, 752 columnas, 63 claves): `PLA.Replica.WorkStack` (IdStack identidad BY DEFAULT, Job NOT NULL default 0, Parameters VARCHAR(500)), `PLA.ResumenReplica` (clave `UltimaActualizacion, Nombre, FechaSistema`; `FechaReal` default CURRENT_DATE), `EnvioPostPauta` (clave de las 3 columnas),
  `CTA.SaldoFechaEfectiva/Movimiento` (`FechaSaldo` NVARCHAR(10), sin clave), `HIS.CustodiaFechaLiquidacion/Operacion` (`FechaCustodia` CHAR(10), sin clave), las 13 `TBC*`.
- SUPUESTO: las 4 vistas analiticas y las 13 vistas `Sebra.TBC*` como tablas; el contenido de todas las semillas (ficticio); los calendarios y los parametros (guiados por el caso, ficticios).
- No se pudo consultar `SYS.TRIGGERS` (columna distinta en esta version de HANA): el cuerpo de los triggers se toma de los SQL del legacy, no del catalogo.

## Hallazgos de la construccion (para el experto / pasos siguientes)
- Los 58 SQL son una sola sentencia cada uno (no hay SQL multi-sentencia; solo los `CREATE TRIGGER` llevan `;` dentro de su `BEGIN ... END`; el `bscl.` en minuscula de `DeleteCuentaInversionFechaEfectivaHistorica` y los `;` finales son cosmeticos).
- `EnviarTablaReplica_Insert*Historica` llaman a las vistas con `p_idempresa => 1` y `p_fechaconsulta => '0001-01-01'` fijos (D-REP-08).
- La clave primaria de `ResumenReplica` incluye `UltimaActualizacion`: el UPSERT nunca actualiza (salvo dos corridas en el mismo segundo): agrega una fila por FIN_PAUTA (D-REP-09).
- El camino `procesarRespaldoDiarioCarterasSebra` traga el error de un parametro ausente, `procesarRespaldoSebra` lo propaga (D-REP-12).
- El oraculo no puede ejecutar el ciclo infinito posible de los dias inhabiles (D-REP-22).
