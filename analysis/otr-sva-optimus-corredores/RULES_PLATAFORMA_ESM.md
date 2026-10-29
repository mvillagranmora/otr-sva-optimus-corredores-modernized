# Reglas de `cl.bcs.plataforma.cre.ConsultarEstadoModulo` (JavaCall 600017) - pasos 1A y 1B del bloque 1

Fecha: 2026-10-22. Alcance acordado para esta ejecucion: SOLO esta clase (las otras tres de la ola A, `CalcularParidad`, `ConsultarFlujoCaja` y `ConsultarMatrizOperacional`, quedan aplazadas). Decisiones D-1..D-7 del plan aprobadas por el usuario. No se implemento el servicio (paso 3) y no se tocaron `legacy/`, HANA, el esquema ni `modernized/`. Ids `PLA-ESM-nn` y `D-ESM-n` **provisionales** (Q-07: `RULE-nnn` los asigna la persona duena de `BUSINESS_RULES.md`).

Rutas legacy relativas a `legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA/`. Abreviaturas: **ESM** = `BcsPlataformaServicios/src/cl/bcs/plataforma/cre/ConsultarEstadoModulo.java`; **SQL** = `BcsUtils/src/cl/bcs/utils/SQL.java`; **NPS** = `.../NamedParameterStatement.java`; **JC** = `.../JavaCall.java`; **PLS** = `.../ProcesaLlamadaServletCall.java`; **CS** = `BcsPlataformaWeb/src/cl/bcs/plataforma/services/CallServlet.java`; **UTL** = `.../Utils.java`; **PL** = `.../PluginLoader.java`.

## 1. Resumen ejecutivo

- La clase es una **lectura pura de una tabla** (`PAU.Modulo`), sin colaboradores de plataforma: NO usa `Diccionario`, `ConfiguracionSistema`, `MensajeSalida` ni `RevisarRequisitosBasicos` (el plan los listaba para el kit; para esta clase no aplican). Cierre de dependencias: `SQL` + `NamedParameterStatement` + `JavaCall` (+ `GsonTimestamp/DateDeserializer` y `NamedCallableStatement` solo por compilacion) + Gson. 124 LOC brutas.
- Complejidad (lizard 1.24.2 **instalado con `pip` y ejecutado**; antes el plan solo traia un conteo manual): `call` CCN 1, `logica` CCN 2, `main` CCN 2; NLOC 95 en el archivo. Coincide con la decision unica (`isEmpty`, `ESM:117`) del plan.
- **Hallazgo que cierra el riesgo R-05 / Q-05 para el legacy:** el legacy SI exige sesion para el 600017. `RegistraUso` NO se lee en ningun punto del camino (solo se selecciona, `PLS:41,77`); el servlet valida siempre el token (ver 5). `RegistraUso='N'` solo coincide con que `Utils.registrarAuditoriaServicios` no audita los ids 600017, 1381 y 14 (`UTL:1477,1481`). El despachador moderno que salta la validacion de sesion con `RegistraUso='N'` (`SessionValidatorService.java:82`) es por tanto una **desviacion de seguridad respecto del legacy**, no una equivalencia.
- **Hallazgo de datos reales:** en el catalogo HANA (`hana_qa_results/cat_columnas_tablas.csv`) `PAU.Modulo."IdEstado"` es `NVARCHAR(3) NOT NULL` y `"CodigoModulo"` `NVARCHAR(40) NOT NULL`: los dos `IFNULL` del SELECT no pueden activarse con el esquema real (D-ESM-4). Se fijan igual con casos marcados "hipoteticos".
- El oraculo compila y ejecuta las clases legacy REALES (sin copias ni "oraculos falsos", incluido el `SQL.java` real) sobre H2 con el esquema derivado del catalogo: 29 casos grabados, `HARNESS_SELFCHECK` con 18 controles en verde (ver 8).

## 2. Entrada y salida (contrato JavaCall)

Ruta HTTP: `POST /BSCL/Plataforma/GWJAVA/PLA/call?t=600017` (`UI/ModuloPlataforma.js:788-795`), sin `r`, sin `ch` (canal `null` -> `"COR"`, `PLS:93-95`). El cliente la dispara con `$interval(estadoModulo, 15000)` por sesion de navegador (`UI/baseapp.js:225-245`; payload `p_IdEmpresa=SesionIdEmpresa, p_IdUsuario=SesionIdUsuario, p_IdModulo=''`, `:226-229`) y usa `respuesta.p_out_ListaModulos` como mapa `NombreModulo -> IdEstadoModulo` (`baseapp.js:235-240`). Registro: `PLA.Servicio` id 600017, `PLUGIN_PLA.jar@cl.bcs.plataforma.cre.ConsultarEstadoModulo`, tipo `J`, `RegistraUso=N`, `ValidaAccesoServicio=N`, 1.777.141 usos (`hana_qa_results/pla_servicio_filas.csv`, linea 2277).

| Direccion | Campo | Tipo Java (`ESM:46-57`) | Notas |
|---|---|---|---|
| in | `p_IdEmpresa` | `Long` | unico campo que se usa; el servlet lo **sobrescribe con la empresa de la sesion solo si la clave viene en el JSON** (`CS:347-348,282-283,680-683`) |
| in | `p_IdModulo` | `String` | **ignorado** (D-ESM-1) |
| in | `p_IdUsuario` | `Long` | **ignorado**; el servlet lo sobrescribe con el de sesion si viene (`CS:349-350`) |
| out | `P_OUT_ADVERTENCIASCONTADOR` | `Long` | siempre `0` (`ESM:112`) |
| out | `P_OUT_ERRORESCONTADOR` | `Long` | siempre `0` (`ESM:111`); el servlet lo busca como `P_OUT_ERRORESCONTADOR` (`CS:441-442`) |
| out | `p_out_ListaModulos` | `List<{IdEstadoModulo, NombreModulo}>` | nunca `null` (`ESM:117-121`) |
| out (no se emite) | `p_out_AdvertenciasDetalle`, `p_out_ErroresDetalle` | listas | quedan `null`; `new Gson()` (`ESM:63`) NO usa `serializeNulls`, asi que las claves no aparecen. El `getGsonBuilder()` de `JavaCall` (con `serializeNulls`, `JC:44-51`) NO se usa aqui |

Texto exacto de salida (golden `PLA-ESM-01`, `salidaCruda`, orden de claves = orden de campos de la clase de salida): `{"P_OUT_ADVERTENCIASCONTADOR":0,"P_OUT_ERRORESCONTADOR":0,"p_out_ListaModulos":[{"IdEstadoModulo":"ABT","NombreModulo":"SIS"},...]}`. Sobre esa salida el servlet agrega `TotalExecutionTime`, `SessionExecutionTime`, `CallExecutionTime` (`CS:489-491`, no deterministas: la comparacion del oraculo se hace antes de ese paso).

SELECT exacto (`ESM:22-23`, parametro nombrado `:IdEmpresa`; unica consulta de la clase, una por llamada, sin `ORDER BY`):

```
SELECT IFNULL("IdEstado",'ABT') AS "IdEstadoModulo", IFNULL("CodigoModulo",'') as "NombreModulo" FROM "BSCL.Plataforma.BD::PAU.Modulo" where "IdEmpresa" = :IdEmpresa
```

Esquema real de `PAU.Modulo` (catalogo, 20 columnas): `IdEmpresa BIGINT(19,0) NOT NULL`, `CodigoModulo NVARCHAR(40) NOT NULL`, `IdEstado NVARCHAR(3) NOT NULL`, `CodigoModuloDependenciaCierre1..5 NVARCHAR(40) NOT NULL`, `CodigoUsuario NVARCHAR(40) NOT NULL`, `FechaUltimaActualizacion TIMESTAMP(27,7) NOT NULL`, `CodigoModuloDependenciaCierre6..15 NVARCHAR(40)` nulos. El catalogo no trae restricciones: la PK es desconocida (probable `(IdEmpresa, CodigoModulo)`; Q-ESM-1). Valores de `IdEstado` que aparecen en las rutinas que escriben la tabla (`BSCL/Plataforma/BD/Servicios/PAU/AbrirModulo.sql:138,262`, `CerrarModulo.sql:136,265,284`, `EntregarSistema.sql:105`): `ENT` (sistema entregado), `SIS`, `CER`, y `ABT` (valor por defecto de la clase). Es una **inferencia de lectura**: no hay dominio declarado en el codigo Java ni se leyeron datos reales (Q-ESM-2).

## 3. Cartas de reglas (Given / When / Then)

Casos = carpetas `equivalence/BcsPlataformaServicios/cases/PLA-ESM-nn_*` (golden grabado ejecutando el legacy). "H" = caso hipotetico (no puede ocurrir con el esquema real).

**PLA-ESM-01 - estado por defecto `ABT`** (`ESM:22`). Given una fila de `PAU.Modulo` de la empresa con `IdEstado` NULL. When se consulta. Then `IdEstadoModulo = 'ABT'`. Con el esquema real `IdEstado` es NOT NULL, por lo que esta rama es codigo muerto en HANA real (D-ESM-4). Casos: 26 (H).

**PLA-ESM-02 - `NombreModulo` es `CodigoModulo` sin transformar** (`ESM:22`). Given filas con codigos como `Contabilidad`, `CONTABILIDAD`, `contabilidad`, acentos, `Ñandú`, `''`, `' '`, `' X '`, 40 caracteres. When se consulta. Then cada fila devuelve el valor tal cual (sin `TRIM`, `UPPER` ni normalizacion Unicode); `CodigoModulo` NULL -> `''` (`IFNULL`, rama muerta en HANA real). Casos: 01, 22, 24, 26 (H), 28.

**PLA-ESM-03 - se filtra solo por empresa** (`ESM:23,115`). Given modulos de varias empresas. When `p_IdEmpresa = N`. Then solo salen las filas de la empresa N, nunca de otra. Casos: 01, 02, 03, 20, 21 (cero, negativo y `Long.MAX_VALUE`).

**PLA-ESM-04 - empresa sin modulos, inexistente o nula -> lista vacia** (`ESM:117-118`). Given empresa sin filas, tabla vacia, `p_IdEmpresa` ausente o `null` (`SQL:193-195`: `setNull`, `"IdEmpresa" = NULL` nunca es verdadero). When se consulta. Then `p_out_ListaModulos = []` (no `null`), contadores 0, sin error. Casos: 04, 05, 06, 07, 12.

**PLA-ESM-05 - solo lectura** (`ESM:22-23`, sin DML en la clase). Given cualquier entrada valida. When se ejecuta. Then `PAU.Modulo` queda sin cambios y el servlet hace `commit` (`CS:495`; aqui nada que confirmar). El volcado `tablas` de cada golden lo verifica (comparado en la tabla completa, incluida la fecha de actualizacion). Casos: todos salvo 29.

**PLA-ESM-06 - forma fija de la salida** (`ESM:51-57,63-68,110-112`). Given cualquier entrada con exito. Then la salida contiene exactamente `P_OUT_ADVERTENCIASCONTADOR:0`, `P_OUT_ERRORESCONTADOR:0`, `p_out_ListaModulos` en ese orden; `p_out_AdvertenciasDetalle`/`p_out_ErroresDetalle` no aparecen; cada elemento tiene `IdEstadoModulo` y luego `NombreModulo`. Caso: 01 (`salidaCruda`); los mutantes `emit-nulls`, `swap-fields`, `counter-1` lo rompen y el self check los detecta.

**PLA-ESM-07 - lectura de la entrada con Gson plano** (`ESM:64`: `new Gson().fromJson(jin.toString(), InConsultarEstadoModulo.class)`). Given el JSON del servlet. Then: nombres de campo **sensibles a mayusculas** (`P_IDEMPRESA`, `IdEmpresa` no cuentan: queda empresa null -> lista vacia); claves desconocidas se ignoran; `"1"` y `1.0` se aceptan como `Long` 1; `1.5`, `"abc"` lanzan `JsonSyntaxException` (-> HTTP 500); `p_IdUsuario` no numerico o `p_IdModulo` objeto lanzan tambien aunque el campo se ignore (D-ESM-6, D-ESM-7); `p_IdModulo` numerico se convierte a texto. Casos: 12, 13, 14, 15, 16, 17, 18, 19.

**PLA-ESM-08 - orden = el que decida la BD** (`ESM:22-23`, sin `ORDER BY`). H2 devuelve orden de insercion; HANA no lo garantiza. Los goldens fijan el orden de insercion (limite declarado, D-ESM-3). Casos: 01, 27 (25 modulos sembrados desordenados). El cliente solo arma un mapa, asi que el orden no es visible para la UI.

**PLA-ESM-09 - sin deduplicacion** (`ESM:116-120`, sin `DISTINCT`). Given dos filas con el mismo `(IdEmpresa, CodigoModulo)`. Then ambas salen. Caso 25 (H: con PK real no ocurre; la UI se queda con la ultima por ser un mapa, `baseapp.js:238`).

**PLA-ESM-10 - un error SQL se propaga como excepcion, no como contador** (`SQL:253-254,794-814`, `ESM:109`). Given que la consulta falla (tabla ausente, conexion cerrada). Then `SQLException` con el texto `<mensaje del driver> Query: <SQL con :IdEmpresa> Parameters: [{IdEmpresa=1}]` (el parametro se imprime con la tabla `HashMap`); el servlet hace rollback y responde HTTP 500 con `{CODIGO, PILA, SP}`; `PILA` incluye la traza completa y el SQL salvo que `PLA.MuestraLog <> 'S'` (`CS:609-650`). El mensaje del driver depende de H2/HANA y se enmascara en el golden (queda desde ` Query: `). Caso: 29.

**PLA-ESM-11 - sin colaboradores, sin estado, sin cache** (`ESM:19-23,109-122`; `PL:79-80`). Una instancia nueva por llamada (`PluginLoader` hace `newInstance` cada vez), unica `final String` de instancia; sin cache, sin `ConfiguracionSistema`, sin `Diccionario`, sin otras tablas. Cada llamada = 1 SELECT de la clase mas ~5 del envoltorio (ver 5).

## 4. Defectos del legacy que se mantienen bug a bug (para el experto de dominio, Q-01)

| Id | Defecto | Evidencia | Caso que lo fija |
|---|---|---|---|
| D-ESM-1 | `p_IdModulo` y `p_IdUsuario` se ignoran: se devuelven TODOS los modulos de la empresa (el cliente manda `p_IdModulo=''` y depende de eso) | `ESM:22-23,46-50`; `baseapp.js:229` | 08, 09, 10, 11, 19 |
| D-ESM-2 | alias `NombreModulo` = columna `CodigoModulo` (nombre engañoso; el "nombre" real no existe) | `ESM:22` | 01, 22 |
| D-ESM-3 | sin `ORDER BY` | `ESM:22-23` | 27 (limite H2) |
| D-ESM-4 | `IdEstado` sin validar dominio (`abt`, `XYZ`, `'   '` pasan tal cual); ademas los `IFNULL` son codigo muerto con las columnas NOT NULL reales | `ESM:22`; catalogo | 23, 26 (H) |
| D-ESM-5 | error de SQL o de lectura de JSON -> excepcion (HTTP 500 con traza y SQL en `PILA` si `MuestraLog='S'`), nunca contador > 0 | `SQL:254,794-814`; `CS:609-650` | 15, 16, 29 |
| D-ESM-6 | empresa omitida o con otro nombre de clave = lista vacia en silencio; y como el servlet solo sobrescribe `p_IdEmpresa` **si la clave existe**, una peticion sin la clave no queda acotada a la sesion (devuelve vacio, no datos ajenos) | `CS:282-283,347-348,680-681`; `ESM:64,115` | 06, 07, 12 |
| D-ESM-7 | los campos ignorados igual se validan por tipo (`p_IdUsuario:"abc"` o `p_IdModulo:{}` rompen la llamada) | `ESM:46-50,64` | 17, 18 |
| D-ESM-8 | el servlet lee `p_IdModulo` cuando existe `IdModulo` (posible NPE si falta `p_IdModulo`); no ocurre con el payload de la UI | `CS:199-200` | no cubierto (fuera de la clase; bloque `BcsPlataformaWeb`) |
| D-ESM-9 | `ProcesaLlamadaServletCall` re-comprueba `ses.size()==0` en vez de `usuario.isEmpty()` (usuario inexistente -> `IndexOutOfBounds` -> 500) y `FechaVigencia` nula -> NPE | `PLS:140-145,156` | no cubierto (fuera de la clase) |
| D-ESM-10 | `main()` con host, usuario y clave HANA en claro (RULE-076): no se copia ni se ejecuta; en el oraculo `BasicDataSource` es un shim que siempre falla | `ESM:71-104` | n/a |
| D-ESM-11 | `SQL.java` actual tiene una rama PostgreSQL (`isPostgresConnection`, `IFNULL`->`COALESCE`, mapeo estricto): la clase se comporta distinto segun el dialecto de la conexion; el oraculo (H2) ejerce la rama no-Postgres | `SQL:86-124,179-188,239-242` | n/a (ver Q-ESM-3) |

## 5. Lo que aportan `JavaCall`, `SQL` y `CallServlet` (camino real de una llamada, NO ejercitado por el oraculo salvo donde se indica)

1. `CS:162-173`: falta `t` -> excepcion `bad request` (500); falta la cabecera `X-SESSION-TOKEN` -> **401** `401 - NOT AUTHORIZED`.
2. `CS:194-196`: cuerpo vacio -> `{ }`; el cuerpo debe ser un objeto JSON (si no, 500). `CS:208-217`: conexion `jdbc/hanaDB`, `SET TRANSACTION AUTOCOMMIT DDL OFF` y `LOCK WAIT TIMEOUT 60000` (conexion sin autocommit).
3. `PLS:90-196` (llamado en `CS:226`): servicio inexistente -> **404** (`SERVICIO INVALIDO`); token invalido (`IdTokenSesion` + `TokenValido='S'` + canal en (`COR`, `SSO`)) -> **401** `TOKEN INVALIDO`; falta `PLA.FechaSistema` -> 401; usuario con `FechaVigencia` anterior a la fecha de sistema (si no es `0001-01-01`) o `IdEstado='BLO'` -> 401 `CUENTA BLOQUEADA O INACTIVA`. Con canal `COR` (el de la UI) no hay expiracion por inactividad ni se actualiza la sesion (`PLS:123,170`).
4. `CS:248-249,347-350,680-683`: la empresa y el usuario de la sesion **reemplazan** `p_IdEmpresa`/`p_IdUsuario` del cuerpo cuando la clave esta presente. Esto es lo que impide que un cliente consulte otra empresa; el servicio nuevo debe preservarlo (R-05).
5. Por llamada, el envoltorio hace 5 SELECT mas aparte del de la clase: `PLA.Servicio`(+`ServicioExtra`) por id/canal, `ACC.Sesion`, `ACC.Usuario`, `PLA.Configuracion` (`PLA.FechaSistema`) y `PLA.Configuracion` por empresa (`PLA.MuestraLog`, `CS:120,259-265`); la hora UTC se cachea 1 hora (`SesionUtils.java:92-101`).
6. `CS:693,699-701`: la clase se carga de `PLUGIN_OPTIMUS.jar` (el usuario confirmo que sale de este arbol, Q-02); si el jar no existe la llamada devuelve `null` y falla con NPE (`CS:441`). Se instancia y se llama `call(conn, json)` (`JC:14-18`).
7. `CS:441-456`: si `P_OUT_ERRORESCONTADOR` falta o es > 0 -> rollback, HTTP 200 con la salida y los tiempos. Si es 0 -> `CS:458-484` ejecuta `Extras` del servicio (`PLA.ServicioExtra` por canal; no hay evidencia de extras para el 600017, Q-ESM-4), agrega los tres tiempos y hace **commit** (`CS:495`). Como la clase siempre devuelve contador 0, nunca hay rollback por contador; las excepciones -> 500 (`CS:609-650`).
8. Id 600017 es especial en tres sitios: sin log de peticion (`CS:186-191`, `496`), sin traza Datadog (`CS:72,667-670`) y **sin auditoria** (`UTL:1477,1481`: ni `INICIO` ni `FIN`). No hay contador de uso por `RegistraUso`.
9. Serializacion: `SQL:213-246` mapea cada fila al POJO por **campo publico = alias de columna** (distingue mayusculas, `CASE_SENSITIVE=true`), `SQL:164-170` valida conexion/sql/params/tipo (`Null connection`...). `SQL:502-528` limpia comentarios (`--`, `//`, `/* */`) del SQL y expande listas (no aplica). `NPS:71-131` ignora `:` dentro de comillas (por eso `"BSCL.Plataforma.BD::PAU.Modulo"` no se interpreta como parametro).

## 6. Trazabilidad regla -> casos -> sondas de mutacion

29 casos (`PLA-ESM-01..29`, ids de caso = ids de carta donde coinciden numero y tema; el inventario completo con entrada y resultado esta en `equivalence/BcsPlataformaServicios/cases.json`). Mutaciones del `HARNESS_SELFCHECK.json` y los casos que las detectan: orden invertido (16 casos lo detectan), "arreglar" D-ESM-1 con filtro por modulo (3: 08, 09, 19), perder el ultimo modulo (19), contador 1 (24), emitir nulos (24), minusculas en estado (19), campos cruzados (19), `trim` del nombre (1: el 24, unico con espacios), y 8 mutaciones del fuente legacy (default `ABT`, default `''`, quitar el filtro de empresa, contador 1, `ORDER BY`, renombrar alias, lista nula, `serializeNulls`).

## 7. Preguntas abiertas

| Id | Pregunta |
|---|---|
| Q-ESM-1 | PK/UNIQUE de `PAU.Modulo` (el catalogo extraido no trae restricciones): decide si el caso de duplicados (25) es posible. Se resuelve con `SYS.CONSTRAINTS`/`INDEXES` (solo lectura, como Q-03; no se ejecuto) |
| Q-ESM-2 | Dominio real de `PAU.Modulo.IdEstado` y los codigos de modulo en QA/PROD (requiere leer datos; no se leyo ninguno). Los goldens usan `ABT/CER/ENT/SIS` inferidos de los SQL de `PAU` y codigos inventados |
| Q-ESM-3 | Dialecto de la BD en produccion: `SQL.java` ya trae una rama PostgreSQL (`isPostgresConnection`); si hay migracion en curso el mapeo estricto cambia el comportamiento de las columnas NULL y los `Long`. El oraculo solo cubre HANA/no-Postgres |
| Q-ESM-4 | `PLA.ServicioExtra` para el 600017 y canales `COR`/`SSO` (datos de HANA): confirmar que no hay extras (el plan asume ninguno) |
| Q-ESM-5 | Quien consume `p_out_ListaModulos` fuera de `baseapp.js` (la busqueda cubrio `BSCL/Plataforma`; otros modulos de UI no se revisaron): relevante para decidir si D-ESM-1 puede corregirse algun dia |
| Q-ESM-6 | El texto del mensaje de las excepciones (`PILA` con traza y SQL, D-ESM-5) lo mantiene o no el servicio nuevo (DEV-A3 del plan propone 500 generico sin `e.getMessage()`): decision de seguridad, el golden 29 fija el texto del legacy |

## 8. Infraestructura del oraculo (paso 1B) - donde esta y como se usa

Todo bajo `analysis/otr-sva-optimus-corredores/equivalence/BcsPlataformaServicios/` (no se modifico `modernized/`):

- `legacy-oracle/record.sh` (compila a un directorio temporal con `--release 11` y JDK 25, usa jars de `~/.m2`, graba `cases/*/expected.json`; `ORACLE_SUBJECT`/`ORACLE_OUT`/`ORACLE_EXTRA_SOURCES` apuntan el mismo conjunto de casos a otra implementacion que implemente `cl.bcs.oracle.Subject`; `LEGACY_OVERLAY` para mutar copias del legacy), `runner/OracleRunner.java`, `runner/cl/bcs/oracle/{Subject,LegacySubject,MutantSubject}.java`, shims `datadog.trace.api.Trace` y `org.apache.commons.dbcp2.BasicDataSource` (solo compilacion).
- `schema-plataforma-esm.sql` (tipos reales del catalogo), `seed/base-esm.sql`, `make_cases.py` (genera `case.json`/`seed.sql`; los `expected.json` solo los escribe el oraculo).
- `compare.py` (autoprueba del comparador, veredicto `EQUIVALENCE.json`), `self_check.py` -> `HARNESS_SELFCHECK.json`, `cases.json`.
- Limites: HANA nunca se ejercito (dialecto, orden de filas, mensajes de excepcion del driver, tipos); el servlet y `PluginLoader` no corren (la decision `COMMIT/ROLLBACK` del volcado replica `CS:441-456` pero no es el servlet); entradas frescas, mutacion de goldens del codigo nuevo y canario quedan para el paso 7.

