# Oraculo FondoMutuo (paso 1B): notas

Plan: `PLAN_FONDOMUTUO_SOLICITUD_APORTE_RESCATE.md` (7.1, 9 paso 1B, 12), reglas en `RULES_FONDOMUTUO_SOLICITUD.md` (5, 6, 6b). Modelo: `../BcsPlataformaServicios/`.
Sin HANA, sin red, sin credenciales: todo corre en H2 en memoria. El legacy no se modifica (se compila a un directorio temporal).

## Uso
- `legacy-oracle/record.sh [prefijo]` graba `cases/<caso>/expected.json` ejecutando las clases REALES. Perillas: `ORACLE_WORK`, `ORACLE_SKIP_COMPILE=1`, `LEGACY_OVERLAY`
  (copia mutada de un fuente legacy), `ORACLE_SUBJECT`+`ORACLE_OUT` (otro sujeto, p. ej. la implementacion nueva: `cl.bcs.oracle.Subject`, mismo volcado),
  `ORACLE_EXTRA_SOURCES`/`ORACLE_EXTRA_CP`.
- `make_schema.py` (esquema desde `schema_spec.py` + catalogos `hana_qa_results/{cat,fmm}_*.csv`), `make_base.py` (universo ficticio), `make_cases.py` (casos piloto + `cases_matrix.json`).
- `compare.py --new <dir> [--self-check]`, `self_check.py` (-> `HARNESS_SELFCHECK.json`).

## Que es real y que es doble
- Real (fuente legacy tal cual): Manejador, Normal y el cierre FondoMutuo (CalcularFecha, CalcularMontoNumeroCuota, CalcularComisiones, ServicioCumple, ValidaMontoInversion,
  ValidaPatrimonioEmisor, ConsultarFlujoCustodiaFondoMutuo, CrearSolicitudAGF, CrearSolicitudRescateComisionNN, ObtenerPerfil, ProcesarIngresoEgresoTesoreria/Spot, pojo/*, utils/*),
  BcsUtils `SQL`, `NamedParameterStatement`, `JavaCall`, `Fecha`, y el kit Plataforma ConfiguracionSistema, Diccionario, PlataformaUtils (MensajeSalida), MonedaFormato,
  RevisarRequisitosBasicos, ObtenerDia*.
- Generados desde el fuente real (`tools/gen_utils.py`, `tools/gen_doubles.py`): `Utils` reducido (metodos copiados textuales; `getSecuencia` sobre secuencias H2) y los DOBLES REGISTRADOS
  (declaraciones del legacy, cuerpo reemplazado por `cl.bcs.oracle.Doubles`): CrearIngresoEgreso, CrearOperacionSpot, ConsultarPuntasPorTramo, ConsultarFlujoCustodia, CalcularParidad,
  EntregarPreciosTasas, SaldoFlujoCaja, AgregarLog, InscribirProcesoBatch, ConsultarProductosPortafolio, NotificarEstadoOrdenesOWP, ConsultarDatosInstrumentos, ConsultarEmpresa y los 7
  destinos diferidos del Manejador. Cada llamada se registra (colaborador, argumentos, respuesta); las respuestas salen de `seed/doubles-base.json` y de `colaboradores` del caso
  (`cuando` por ruta, `defecto`, `error`). Sin respuesta configurada el doble FALLA (el caso debe declarar lo que alcanza).
- Copia con reloj fijo de `FechaDB` (solo `System.currentTimeMillis()` -> `cl.bcs.oracle.Clock`): el original suma milisegundos reales (no reproducible).
- Shims escritos a mano (`shims/`): `Trace`, `BasicDataSource` (solo compilacion), `Log` (el real escribe en el home).
- `SessionConnection` (borde JDBC, solo H2): procedimientos `Crear/EliminarBloqueoSolicitudAporteRescate` AUTONOMOUS TRANSACTION (confirmados por `fmm_procedimientos.csv`) = segunda
  conexion en autocommit que confirma al instante; `CREATE LOCAL TEMPORARY TABLE #p_Bloqueo...`/`DROP TABLE` = estado por sesion (`sesion.tablasLocalesSinDrop` en el volcado);
  `CURRENT_UTCTIMESTAMP` (reloj fijo `2026-03-02 14:30:00`), `schema.NEXTVAL`, `ADD_SECONDS`, `TO_BIGINT`, `TO_NVARCHAR`.
  `GBL.ValidaPatrimonioEmisor_Proceso_Paso` es tabla LOCAL TEMPORARY de la sesion H2 (en HANA es GLOBAL TEMPORARY: datos por sesion).
- Volcado: salida JSON (`salida`, `salidaCruda`), excepcion, `decisionServlet` (contador ausente o > 0 => ROLLBACK; excepcion => ROLLBACK_HTTP500), tablas (SolicitudAporteRescate, tabla de
  bloqueo, InterfazAgf), llamadas a colaboradores, secuencias pedidas, `sesion` (tabla local sin DROP, commits autonomos).

## Tipos
`schema-types.sql` (un DOMAIN por columna, tipo y NOT NULL) esta aislado de `schema-fmm.sql` (estructura) y `schema-keys.sql` (claves primarias de `fmm_claves.csv`). 476 columnas son REALES
(catalogos), 33 SUPUESTAS (etiqueta ASSUMED en cada linea): `FMM.Parametro` (modulo FONDOMUTUO de `ConfiguracionSistema`), `PLA.Diccionario`, `PLA.Empresa`, `PLA.Modulo`,
`PLA.MonedaFormato`, `SOC.ConsultarSocioDeNegocio` (los de Plataforma no estan en las 36 SQL del alcance ni en el export). De la vista `FMM.ConsultarSolicitudAporteRescate` se descartaron las
columnas que el catalogo real no tiene. Secuencias con inicio/ciclo reales (`fmm_secuencias.csv`; `SecuenciaMovimiento` es ciclica).
Para sustituir: re-ejecutar `py -3 make_schema.py` tras un nuevo `fmm_*.csv`.

## Hallazgos de la construccion (para el experto / pasos siguientes)
- La simulacion del Manejador consume numeros de secuencia (`SolicitudAporteRescate` y `FolioContrato`): el id de la solicitud real de la pasada "N" es el siguiente (piloto MAN: 11413789).
- `Diccionario`, `MonedaFormato`, `FechaDB` y `Fecha` tienen estado estatico: el runner lo reinicia por caso.
- `TO_NVARCHAR(CURRENT_UTCTIMESTAMP,...)` del INSERT fija `FechaIngresoRegistro` con la hora del reloj de BD (fijado en el oraculo).

## Supuestos / no probable aqui
- OPEN: si `CREATE LOCAL TEMPORARY TABLE`/`DROP TABLE` de HANA hacen COMMIT implicito de lo escrito antes en la misma transaccion. El oraculo NO emula ese commit (un DDL real de H2 si lo haria);
  si HANA lo hace, el `DROP` del bloqueo confirmaria el INSERT de la solicitud antes de la decision del servlet (afecta los casos de ROLLBACK posteriores al bloqueo).
- Concurrencia real del bloqueo, orden de filas sin ORDER BY, escalas HANA y claves/restricciones no exportadas (FKs): no se ejercitan en H2.
- Horas/valutas/feriados/valores de configuracion/textos del diccionario son ficticios; la fecha de aceptacion/curse sale del calendario sembrado (sin feriados).

## Paso 5 (verificacion independiente): perillas y semillas nuevas
- `OracleRunner` lee los casos de `-Doracle.cases=<dir>` (por defecto `cases/`): las entradas frescas viven en `fresh/cases/` y nunca se mezclan con los goldens de desarrollo (`JAVA_OPTS="-Doracle.cases=..."`).
- Semillas nuevas SOLO en los casos (el esquema y `seed/base-fmm.sql` no cambian; los 80 goldens previos siguen byte a byte): filas BATCH de `FMM.ConfiguracionFechasFondoMutuo` (copia de las UNITARIO; SUPUESTO: la configuracion real no esta en el export),
  `TipoMovimientoFondoMutuo` RESCNN (de los casos 3B), `ALTER TABLE ... ADD COLUMN "IdOmnibus" BIGINT` en la vista sembrada `FMM.ConsultarSolicitudAporteRescate` (REAL: `fmm_columnas_vistas.csv`, BIGINT NULL; no esta en `schema-fmm.sql` para no re-grabar el caso 17)
  y una fila de esa vista para el id 11413788 que entrega la secuencia en una llamada de una solicitud (`cases_nor_cov.py`).
- Doble de `CalcularParidad`: el de la base responde siempre `MontoDestino` 15001, de modo que el control de saldo de flujo de caja comparaba 15001 y no el monto del aporte; los casos de limite de saldo (`FRESH-NOR-11/12/41-43`) lo reemplazan por uno que responde el monto.
- D-NOR-14/15 de 4A (NPE del rescate NN y del lote BATCH): causa = faltaba la modalidad BATCH en la configuracion de fechas sembrada (D-FEC-2); con ella el rescate NN de Normal se inserta (caso 61: salida con RESNORM + RESCNN, 2 filas), el lote BATCH corre (62) y un NN rechazado en su propia validacion (Fondo.BloqueoAporte=S, caso 69) deja el resultado sin la fila NN.
