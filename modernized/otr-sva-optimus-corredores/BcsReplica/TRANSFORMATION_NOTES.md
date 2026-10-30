# BcsReplica — Transformation Notes

Pattern: strangler-fig, hexagonal. Source: Java EE 8 / TomEE (`legacy/otr-sva-optimus-corredores/BSCL/Replica`). Target: Spring Boot 4.1.1 / Java 25, port 8093.

## Correccion de la nota anterior
La nota previa de este modulo decia "completo" y que `EnviarTablaReplica.java` eran "~24000 lineas de SQL puro HANA, sin logica portable, @Disabled". Es falso:
- `EnviarTablaReplica.java` tiene **546 lineas de Java**: 8 procesos con logica de encolado, ciclo de dias inhabiles con colaboradores de Plataforma, triggers, resumen y limpieza. Sus SQL son **58 archivos** (44.615 bytes en total), una sentencia cada uno.
- Lo que sumaba ~24.000 lineas eran los scripts de instalacion de BD (`BD/Scripts/*`, 14 archivos, 27.844 lineas), que no son codigo del servicio.
- El esqueleto de `src/main` inventa tablas (`WORKSTACK_T`, `CONFIGURACION_T`), endpoints REST (`/api/replica/*`) y una clave `REPLICA_FULL` que el legacy no tiene; el legacy encola en `BSCL.Plataforma.BD::PLA.Replica.WorkStack` con `Key "-1"`. No se ejecuto contra el legacy ni se compara con el.
Estado al cerrar el paso 1: pruebas de caracterizacion y oraculo en rojo, sin codigo de produccion de la logica. **Hoy (pasos 2-3 hechos): implementacion completa y 469 pruebas en verde** (seccion "Replica: implementacion").

## Replica: pruebas

### Que se entrega
| Pieza | Donde | Cantidad |
|---|---|---|
| Casos (entrada, semilla H2, golden grabado del legacy) | `analysis/otr-sva-optimus-corredores/equivalence/BcsReplica/cases/` y `src/test/resources/replica-caracterizacion/cases/` | **264** (ENV 106, INI 50, SEB 31, DIA 24, OPT 21, INH 15, POS 14, CAL 3) |
| Goldens (`expected.json`, legacy ejecutado sin modificar sobre H2) | idem | 264; 107 con excepcion del legacy; 1.618 sentencias SQL registradas |
| Estabilidad | `determinism.txt` | 20 corridas por caso = 5.280 corridas, **0 inestables** |
| Pruebas JUnit | `src/test/java/cl/bcs/replica/caracterizacion/` | 469 (equivalencia 266, determinismo 2, reglas 47, compuertas 148, SQL 2, catalogo 4) |
| Oraculo y generadores | `analysis/.../equivalence/BcsReplica/` (`legacy-oracle/record.sh`, `make_cases.py`, `make_schema.py`, `check_replica.py`, `sync_replica.py`, `ORACLE_NOTES.md`) | — |
| Catalogo REAL de HANA QA (solo SELECT sobre SYS) | `analysis/.../equivalence/BcsReplica/catalog/` | 752 columnas, 63 claves |

Estado del paso 1 (historico, commit 8ad227ce): todas las pruebas **compilan** y fallan solo con `not implemented: the production class ... does not exist yet` (`ApiReplica.java` es el unico archivo que alcanza produccion, por reflexion). Estado medido (`mvn -o test`): 469 pruebas, **461 en rojo** (460 con `not implemented: the production class ...` y la de "equivalence cases executed: 0 => not proven", por diseno) y 8 en verde, que no necesitan produccion (4 del catalogo de la matriz, informe de determinismo del oraculo, manifiesto de los 58 SQL, "todos tienen golden", recuento de casos sin compuerta).

### Prueba de que el arnes es satisfacible
Implementacion de referencia escrita FUERA del repo (`%TEMP%/repref`, ~600 lineas: puertos, servicios, adaptadores JDBC con los 58 SQL): con los mismos goldens pasaron las **469 pruebas**. En el paso 3 esa referencia fue la base de la implementacion entregada (revisada contra el legacy, sin el helper de acceso que tenia, con `ReplicaConfig`).

### Mutacion
40 mutantes de una linea sobre la implementacion de referencia (proceso exacto en vez de sin mayusculas, compuertas quitadas o invertidas, errores que se propagan en vez de tragarse y al reves, consultas y tablas del Workstack, espacio sobrante de CARTERARV, desfase horario restado, `substring(0,6)`, sufijo del dia, parametro Sebra por defecto, orden de tablas Sebra, periodo del INSERT, fechas del envio cruzadas, Action/Key/Stamp/secuencia de IniciarReplica, contador recontado, pais igual a idEmpresa, la copia leyendo del destino, esquema de la fecha, etc.): **39 muertos, 1 equivalente**. Todos los de comportamiento caen en al menos dos capas (equivalencia con el golden y reglas con el valor escrito); los de compuerta (M02, M28, M29, M37) los matan las pruebas de compuerta, que no tienen golden porque el legacy no tiene compuertas.
- Equivalente: M40 (`Parameters.trim()` del lote de Workstack): ningun `Parameters` empieza ni termina en espacio (empiezan en `tables:` o `BCS_query:` y terminan en `serialLaunch:false`), asi que `trim()` no cambia nada observable.
- Un mutante sobrevivio en la primera pasada (M29: el puerto cerrado de `ConsultarEmpresa` devolvia null en vez de lanzar): ningun caso lo notaba porque el colaborador siguiente tambien lanza. Se agrego `cada_puerto_cerrado_lanza` (los 7 metodos de los 6 puertos del valor por defecto) y el mutante murio.

### Que hace el oraculo (resumen; detalle en `ORACLE_NOTES.md`)
- Ejecuta las clases legacy REALES (`ReplicaUtils`, `IniciarReplica`, mas `EnviarTablaReplica` e `IniciarReplicaFull/Delta` con UNA edicion de reloj, el script aborta si difieren en algo mas) con BcsUtils real (`SQL`, `NamedParameterStatement`, `Fecha`, ...) sobre H2, detras de una conexion registradora que anota cada SQL (texto, filas de parametros, orden) y lo ejecuta tras una traduccion mecanica HANA -> H2.
- Los colaboradores fuera del modulo (ObtenerDiaSiguiente, ConsultarEmpresa, Feriados, ConfiguracionSistema, `Utils.getFechaSistema/getSecuencia`) son dobles registrados con respuestas por caso (calendario por fecha); en el codigo nuevo seran **puertos** (`cl.bcs.replica.port.out`) cuyo valor por defecto es cerrado (`ColaboradoresNoPortados`, lanza `ServicioNoServidoException`). Se reviso BcsUtils / Plataforma modernizados: BcsUtils no tiene `SQL`/`NamedParameterStatement` (hay variantes `SqlNombrado` por modulo) ni esos colaboradores; `BcsCuentaInversionServicios` tiene `ObtenerDiaSiguientePort`/`ConfiguracionSistemaPort` pero con otra forma (devuelven `LocalDate`, sin calendario de pais ni feriados) y no son reutilizables como estan.
- Compuertas cerradas por defecto: G-TRG (CREATE/DROP TRIGGER), G-SEB (respaldos Sebra), G-OPT (respaldo Optimus); propiedades `replica.compuertas.triggers|respaldo-sebra|respaldo-optimus`.

### Reglas (B-REP) y defectos replicados (D-REP)
B-REP-01..04 son las del esqueleto anterior (formato de `Parameters`, Full/Delta, parametro "S", `substring(0,7)`), corregidas contra el legacy. El resto es nuevo. Cada defecto replicado es un caso con id propio; el nombre de cada caso cita sus ids.

| Id | Regla | Legacy |
|---|---|---|
| B-REP-01 | IniciarReplicaFull/Delta: la fila de WorkStack lleva Parameters = "BCS_query:<consulta>;Fecha:<fecha de sistema de la empresa>" (la fecha viene de Utils.getFechaSistema) | `IniciarReplicaFull.java:51` |
| B-REP-02 | Full usa QueryReplica y Delta QueryReplicaDelta; ambas encolan IdStack (secuencia WorkStack, primer valor), Key "-1", Table INICIAR_REPLICA_PAUTA, Action "F", Status 0, Stamp = ahora | `IniciarReplicaFull.java:45-50` |
| B-REP-03 | Respaldo Sebra: solo "S" (sin distinguir mayusculas) de PLA.RespaldoReplicaTablasSebra lo activa | `ReplicaUtils.java:70` |
| B-REP-04 | COD_PER = los 7 primeros caracteres de la fecha de sistema | `ReplicaUtils.java:76` |
| B-REP-05 | El proceso se compara sin distinguir mayusculas; uno desconocido no escribe nada | `EnviarTablaReplica.java:100-377` |
| B-REP-06 | INICIO_PAUTA: apaga triggers, encola "CUENTA INVERSION DIA ACTUAL", limpia (retencion 10 dias y fecha actual), inserta historico de cuenta inversion y custodia, respalda dias inhabiles de cuenta inversion | `:100-153` |
| B-REP-07 | VALORIZAR_FFMM: "CUSTODIA ACTUAL" y "CARTERAFM" (truncarDestino:true) + dias inhabiles de custodia | `:154-182` |
| B-REP-08 | PAUTA_PATRIMONIO: "PATRIMONIO DIA ACTUAL" con las 6 tablas de patrimonio | `:183-195` |
| B-REP-09 | COPIA_PATRIMONIO_PARA_INHABIL: un "PATRIMONIO DIA <fecha>" por dia inhabil siguiente | `:196-257` |
| B-REP-10 | PAUTA_RESPALDAR_CUSTODIA: CARTERAFMHIS, CARTERARVHIS, GARANTIAHIS | `:258-292` |
| B-REP-11 | CAMBIO_FECHA_SISTEMA: "CUENTA INVERSION DIA SIGUIENTE" | `:293-303` |
| B-REP-12 | VALORIZAR_CARTERA_RV_PRECIO_MERCADO: "CARTERARV" con truncarDestino:true | `:304-315` |
| B-REP-13 | FIN_PAUTA: encola "CUSTODIA DIA SIGUIENTE", lee PLA.SegundosZonaHoraria, reactiva triggers, registra resumen (PAUTA/FIN_PAUTA/OK), limpia e inserta historico | `:316-374` |
| B-REP-14 | respaldarDataDiaInhabil: por cada dia inhabil siguiente, Workstack + copia origen -> destino segun el tipo de tabla | `:447-530` |
| B-REP-15 | Los Workstack se insertan en un lote al final, solo si hay alguno: Key "", Action "M", Status 0, Stamp CURRENT_TIMESTAMP | `:379-384` |
| B-REP-16 | Parameters de un dia inhabil = prefijo + fecha destino + ";serialDelete:false;truncarDestino:false;serialLaunch:false" | `:498` |
| B-REP-17 | Respaldo Sebra: 13 tablas TBC* en orden fijo, DELETE del periodo y luego INSERT desde la vista Sebra | `ReplicaUtils.java:74-131` |
| B-REP-18 | Respaldo diario Sebra: 4 tablas, COD_DIA = "90"+dd+"-01" | `:299-349` |
| B-REP-19 | Respaldo Optimus: borra e inserta el historico, guarda 4 envios post pauta y repite por cada dia inhabil | `:176-287` |
| B-REP-20 | guardarEnvioPostPauta: UPSERT de (FechaSistema, Tabla, Fecha) | `:162-174` |
| B-REP-21 | Retencion: se borran las fechas <= fecha de sistema - 10 dias (inclusivo) y las de la fecha de sistema | `EnviarTablaReplica.java:126-140` |
| B-REP-22 | La copia de un dia inhabil recalcula Checksum (-IdCuentaInversion con saldo 0; suma ponderada; NULL si falta una columna) | `EnviarTablaReplica_InsertCuentaInversionFecha*DiaInhabil.sql` |
| B-REP-23 | Respaldo diario: parametro ausente = "N" | `ReplicaUtils.java:303-308` |
| B-REP-24 | IniciarReplica.call: JSON de la salida con P_OUT_ERRORESCONTADOR recontado | `IniciarReplicaFull.java:29-36` |

| Id | Defecto (replicado bug a bug) | Legacy |
|---|---|---|
| D-REP-01 | `EnviarTablaReplica.call` y `ReplicaUtils.call` son stubs que devuelven null | `EnviarTablaReplica.java:86-90`, `ReplicaUtils.java:351-355` |
| D-REP-02 | `procesarRespaldoOptimus` pasa el id de empresa como pais de cada fecha (FechasIn.pais = idEmpresa) | `ReplicaUtils.java:220` |
| D-REP-03 | COPIA_PATRIMONIO_PARA_INHABIL: ante error del dia siguiente retorna sin encolar lo acumulado | `EnviarTablaReplica.java:223-227` |
| D-REP-04 | respaldarDataDiaInhabil: ante error del dia siguiente corta sin excepcion y SI encola lo acumulado (incoherente con D-REP-03) | `:478-482` |
| D-REP-05 | VALORIZAR_CARTERA_RV: espacio sobrante antes de ";serialDelete" | `:310` |
| D-REP-06 | Los dos DROP TRIGGER en un try: el primero falla, se traga, no se intenta el segundo | `:104-111` |
| D-REP-07 | FIN_PAUTA: CREATE TRIGGER x2 y UPSERT del resumen en un try: un error se traga y omite el resto (incluido el resumen) | `:340-350` |
| D-REP-08 | Los INSERT historicos llaman a las vistas con p_idempresa => 1 y p_fechaconsulta => '0001-01-01' fijos | `EnviarTablaReplica_Insert*Historica.sql` |
| D-REP-09 | ResumenReplica: UltimaActualizacion = hora del servidor + desfase; la clave primaria la incluye: una fila por ejecucion | `:326-334,418-445` |
| D-REP-10 | EnvioPostPauta: clave de 3 columnas, el UPSERT agrega en vez de sobrescribir | `EnviarTablaReplica_InsertEnvioPostPauta.sql` |
| D-REP-11 | FechaSistemaEjecucion sin validar (nulo: NPE tras apagar triggers; no texto: ClassCastException; con hora o basura pasa al SQL) | `:95` |
| D-REP-12 | procesarRespaldoSebra propaga el parametro ausente; el diario lo traga y asume "N" | `ReplicaUtils.java:63-68,304-308` |
| D-REP-13 | Ningun proceso es atomico: ante un error quedan los borrados y copias ya hechos en la sesion | `ReplicaUtils.java:74-130` |
| D-REP-14 | COD_DIA "90dd-01" no incluye el mes: pisa el respaldo del mismo dia del mes anterior | `ReplicaUtils.java:315` |
| D-REP-15 | WorkStack.Parameters VARCHAR(500): un parametro largo falla al final, tras los borrados | `EnviarTablaReplica.java:383` |
| D-REP-16 | Proceso nulo: NPE; desconocido, con espacios o truncado: no hace nada (solo un log) | `:100,375-377` |
| D-REP-17 | IniciarReplica: Key "-1", Action "F", Stamp con la hora de la JVM, p_IdUsuario ignorado | `IniciarReplicaFull.java:45-50` |
| D-REP-18 | IniciarReplica sin validacion: empresa nula = NPE despues de pedir la secuencia; JSON con empresa no numerica falla en Gson | `:29-31,45,51` |
| D-REP-19 | FIN_PAUTA convierte la fecha con Fecha.toDate: otro formato o nulo falla antes de escribir y de encolar | `EnviarTablaReplica.java:330` |
| D-REP-20 | respaldarDataDiaInhabil con una tabla distinta de las dos conocidas (comparacion exacta) encola sin copiar datos | `:511-525` |
| D-REP-21 | Los Workstack se insertan despues de las copias: si falla la insercion final las copias quedan | `:379-384` |
| D-REP-22 | Los ciclos de dias inhabiles no tienen tope (documentado, sin caso: el legacy no puede terminar) | `:204,460` |

### Decisiones de diseno que fijan las pruebas (a confirmar, ver `API_ASSUMED.md`)
1. Una compuerta cerrada lanza `ServicioNoServidoException` antes de la primera escritura y de cualquier colaborador (G-TRG: inicio de INICIO_PAUTA y FIN_PAUTA; G-SEB: solo con el parametro en S; G-OPT: inicio del respaldo Optimus). Se evita que el DDL de triggers corra sin querer: en HANA el DDL confirma implicitamente la transaccion (`HANA_DDL_COMMIT_HALLAZGO.md`).
2. Unica desviacion del bug a bug: `procesarRespaldoDiarioCarterasSebra` traga todo error de lectura del parametro; el nuevo deja pasar `ServicioNoServidoException` para que un colaborador no portado no termine en un respaldo silenciosamente omitido (D-REP-12).
3. Reloj inyectado (`java.time.Clock`), puertos con `Connection` como primer argumento y `throws Exception`, JSON del contrato `JavaCall` para `IniciarReplica`.
4. Los 58 SQL se portan byte a byte (`ReplicaSqlRecursosTest`, manifiesto SHA-256).

### Limites H2 vs HANA (no se ocultan)
1. `CREATE/DROP TRIGGER`: no ejecutables en H2; se pina texto y orden (`NO_EJECUTABLE_H2`), no el efecto. Vale tambien para el commit implicito del DDL en HANA, que H2 no reproduce.
2. Las vistas analiticas con `PLACEHOLDER` (4) y las 13 vistas `Sebra.TBC*` se modelan como tablas H2 con las columnas de la tabla destino y filas sembradas por el caso (SUPUESTO; el catalogo confirma las columnas de 3 vistas Sebra). Lo que esas vistas calculan en HANA no se ejecuta.
3. `UPSERT ... WITH PRIMARY KEY`, `ADD_DAYS`, `CURRENT_TIMESTAMP` traducidos mecanicamente; `CHAR`/`NVARCHAR` como `VARCHAR`; conteos de filas de H2.
4. Sin SEBRA externa real, sin concurrencia, sin plan de ejecucion; los calendarios y parametros de los casos son ficticios (los colaboradores se guionan).
5. Esquema REAL (catalogo QA: columnas, tipos, NOT NULL, defaults, claves primarias, identidad); SUPUESTO solo lo dicho en 2.

### Que no se porta y por que
| Elemento | Motivo |
|---|---|
| `BD/Scripts/*` (14 scripts, 27.844 lineas: SQL Server, Oracle SURA, triggers por motor, tablas, configuracion) | Decision del usuario: instalacion de BD, no codigo del servicio. Siguen siendo la referencia de esquema para los DBA. |
| `BD/Vistas/*` (393 SQL: vistas `Replica*` de lectura que consume el job externo de replica) y `BD/Servicios/*` (8 SQL: `func_IsNumeric`, `MapeoCOD_MOVTO`, `MapeoTIPO_COMP_FAC`, `TO_LEGACY_RUT`, cada una HANA y PG) | Son objetos de base de datos que viven en HANA; **el codigo Java no usa ninguno de ellos** (ver Q-REP-1). No hay logica Java que caracterizar. |
| `UI/ACC/cambiarClaveUsuarioReplica.html/.js` | Pantalla de cambio de clave de un usuario tecnico, sin relacion con la logica. |
| `BcsReplicaTests/MapeoTIPO_COMP_FACTest` | Compara una funcion SQL en HANA contra PostgreSQL con conexiones reales (`connections.properties`), no es una prueba del modulo. |
| `main()` de `EnviarTablaReplica`, `IniciarReplicaFull/Delta` | Arneses de prueba manual con conexion real (RULE-076); se compilan en el oraculo y nunca se llaman. |
| Ejecutar en HANA el DDL de triggers y el respaldo Sebra/Optimus | Compuertas cerradas por defecto; el entorno que las abra es una decision humana (Q-REP-8). |

### Pruebas del esqueleto previo retiradas (y por que)
- `ReplicaServiceTest` (9 pruebas, REP-SVC-01..09): probaba `ReplicaService.buildParametersString`, `extractCodPer`, `esRespaldoActivo`, tres funciones estaticas inventadas que no existen en el legacy. Su contenido de negocio (formato de `Parameters`, Full vs Delta, "S" sin distinguir mayusculas, `substring(0,7)`) vive ahora en B-REP-01..04, contra goldens del legacy.
- `ReplicaIntegrationTest` (5 pruebas, REP-INT-01..05, una `@Disabled`): usaba las tablas inexistentes `WORKSTACK_T` y `CONFIGURACION_T` (el legacy escribe `PLA.Replica.WorkStack` con otra forma) y leia `PLA.FechaSistema` de una tabla que no es la que lee el legacy; ademas `Action "F"` y `PLA.RespaldoReplicaTablasSebra` coincidian pero `Key`, `Table` y `Stamp` no. Sus recursos (`application-test.yml`, `schema-replica.sql`, `data-replica.sql`) se retiraron con ellas.
- `src/main` (controlador REST, servicio, puerto, adaptador y dominio del esqueleto) se reemplazo en el paso 3 (ver "Replica: implementacion"). Lo que coincidia con el legacy se conserva como intencion: Action "F" para Full/Delta, `QueryReplica` / `QueryReplicaDelta`, `substring(0,7)` y la regla "S".

## Replica: implementacion

Pasos 2-3 de modernize-transform: el `src/main` del esqueleto previo se reemplazo por una implementacion hexagonal que replica el legacy bug a bug. Resultado: **469 pruebas, 0 fallos, 0 omitidas; "equivalence cases executed: 264 of 264"; 6 XML de surefire = 6 clases de prueba; sin `OutOfMemoryError`** (log: `analysis/otr-sva-optimus-corredores/equivalence/BcsReplica-implementacion-verde.test-output.txt`). No se toco ninguna prueba, golden, caso ni asercion, ni `ApiReplica.java` (la forma asumida bastó tal cual).

### Que se escribio (legacy file:lineas -> target)
| Legacy (solo lectura) | Target (`src/main/java/cl/bcs/replica/`) |
|---|---|
| `EnviarTablaReplica.java:100-153` INICIO_PAUTA | `envio/EnviarTablaReplicaServicio.java:62-71` (+ `limpiarEInsertarHistorico` `:124-137`, que INICIO_PAUTA y FIN_PAUTA comparten: legacy `:125-144` y `:353-372`) |
| `:154-182` VALORIZAR_FFMM, `:183-195` PAUTA_PATRIMONIO | `EnviarTablaReplicaServicio.java:72-77` |
| `:196-257` COPIA_PATRIMONIO_PARA_INHABIL | `EnviarTablaReplicaServicio.java:78-87` |
| `:258-315` PAUTA_RESPALDAR_CUSTODIA, CAMBIO_FECHA_SISTEMA, VALORIZAR_CARTERA_RV | `EnviarTablaReplicaServicio.java:88-95` |
| `:316-374` FIN_PAUTA, `:418-445` `obtenerFechaHora` | `EnviarTablaReplicaServicio.java:96-115` (reloj inyectado, `LocalDateTime.now(reloj).plusSeconds(desfase)`) |
| `:375-384` proceso desconocido (sin rama: solo log en el legacy) y lote de Workstack | `EnviarTablaReplicaServicio.java:117-122` |
| `:447-530` `respaldarDataDiaInhabil` | `EnviarTablaReplicaServicio.java:147-172` |
| `:86-90` `call` (stub, devuelve null) | `EnviarTablaReplicaServicio.java:53-55` |
| `:405-416` `Workstack` | `domain/Workstack.java` (campos publicos `Key`, `Table`, ...) |
| `ReplicaUtils.java:60-131` respaldo Sebra (13 tablas) | `respaldo/ReplicaUtilsServicio.java:45-51` + `adapter/out/jdbc/RespaldoSebraJdbc.java` (puerto `RespaldoSebraPort`, compuerta G-SEB) |
| `:162-174` `guardarEnvioPostPauta` | `ReplicaUtilsServicio.java:70-74` (+ `guardarCuatro` `:76-81`) |
| `:176-297` respaldo Optimus | `ReplicaUtilsServicio.java:83-98` + `adapter/out/jdbc/RespaldoOptimusJdbc.java` (puerto `RespaldoOptimusPort`, compuerta G-OPT) |
| `:299-349` respaldo diario Sebra (4 carteras) | `ReplicaUtilsServicio.java:53-68` + `RespaldoSebraJdbc.respaldarDiario` |
| `:351-355` `call` (stub) | `ReplicaUtilsServicio.java:41-43` |
| `EnviarTablaReplica.java:104-111, 340-350` DROP/CREATE TRIGGER | `adapter/out/jdbc/TriggersReplicaJdbc.java` (puerto `TriggersReplicaPort`, compuerta G-TRG) |
| `IniciarReplica.java`, `IniciarReplicaFull.java:29-60`, `IniciarReplicaDelta.java` | `inicio/IniciarReplicaBase.java` (Full y Delta solo difieren en la consulta: `inicio/IniciarReplicaFull.java`, `IniciarReplicaDelta.java`) |
| `cl.bcs.utils.SQL` (`exec`, `batch` con parametros nombrados) | `kit/Sql.java` (copia del comportamiento que usa Replica: sin comentarios, `:nombre` -> `?`, mensaje `Query: ... Parameters: ...`, `null` como `VARCHAR`). BcsUtils modernizado no tiene `SQL` |
| `InObtenerDiaSiguiente` armado en `:206-219`, `:461-474` | `kit/EntradaDiaSiguiente.java` |
| 58 SQL `cl/bcs/replica/sql/*.sql` | `src/main/resources/cl/bcs/replica/sql/` (byte a byte; `diff -rq` contra el legacy sin diferencias; SHA-256 pinados por `ReplicaSqlRecursosTest`) |
| (sin equivalente: el legacy no tiene compuertas) | `kit/Compuertas.java`, `kit/ServicioNoServidoException.java`, `port/out/*` (6 puertos de colaborador + 3 de escritura + `ColaboradoresNoPortados`), `config/ReplicaConfig.java` (cableado por defecto) |

Borrado del esqueleto previo: `controller/ReplicaController`, `controller/GlobalExceptionHandler`, `service/ReplicaService`, `adapter/out/ReplicaRepositoryAdapter`, `port/out/ReplicaDataPort`, `domain/EntradaIniciarReplica`, `domain/SalidaIniciarReplica` (endpoints REST `/api/replica/*`, tablas `WORKSTACK_T`/`CONFIGURACION_T` y clave `REPLICA_FULL` inventados). `domain/Workstack` se rehizo con la forma del legacy; `BcsReplicaApplication`, `application.yml` y `Dockerfile` se conservan (sin cambios; no hay handler JavaCall nuevo ni endpoint).

### Defectos replicados (bug a bug)
Los 22 D-REP, sin corregir ninguno: D-REP-01 (`call` devuelve null), 02 (`pais = idEmpresa` en el respaldo Optimus, comentado en el codigo), 03 y 04 (el error del dia siguiente corta con y sin encolar lo acumulado), 05 (espacio sobrante de CARTERARV), 06 y 07 (los `try` que se tragan el segundo DROP / el UPSERT del resumen), 08 (parametros fijos de las vistas: viven en los SQL byte a byte), 09 y 10 (claves primarias del resumen y del envio post pauta: viven en el esquema/SQL), 11 (fecha sin validar: NPE, `ClassCastException`), 12 (Sebra propaga el parametro ausente, el diario lo traga), 13 (nada es atomico: la implementacion no abre transaccion, usa la `Connection` recibida), 14 (`COD_DIA` sin mes), 15 (`Parameters` largo falla al final), 16 (proceso nulo/desconocido), 17 y 18 (IniciarReplica sin validacion), 19 (`Fecha.toDate` de FIN_PAUTA), 20 (tabla desconocida encola sin copiar), 21 (Workstack al final), 22 (`while (true)` sin tope, como el legacy; sin caso, no puede terminar).

### Desvios respecto del legacy (los unicos)
1. **Compuertas** G-TRG, G-SEB, G-OPT: cerradas por defecto (`replica.compuertas.triggers|respaldo-sebra|respaldo-optimus` = `false`; `Compuertas` es un `@Component` que solo lee las propiedades y por defecto no abre nada). Una compuerta cerrada lanza `ServicioNoServidoException` antes de la primera escritura y de cualquier colaborador.
2. **Colaboradores no portados** (ObtenerDiaSiguiente, ConsultarEmpresa, Feriados, ConfiguracionSistema, `Utils.getFechaSistema`, `Utils.getSecuencia`): puertos cuyo valor por defecto (`ColaboradoresNoPortados`) lanza `ServicioNoServidoException`.
3. `procesarRespaldoDiarioCarterasSebra` deja pasar `ServicioNoServidoException` en vez de tragarla como el resto de errores del parametro (D-REP-12): desvio fijado por las pruebas y aprobado.
4. Reloj inyectado (`Clock`), `Connection` recibida (sin pool ni transaccion propia), excepciones de JDBC sin envolver salvo las del lote (que replican el `Query: ...` del legacy).
5. HANA nunca se toca: ningun codigo se ejecuta contra HANA desde las pruebas (solo H2); con las compuertas cerradas el DDL de triggers y los respaldos ni siquiera se envian.

### Que NO se porta y por que
Ver la tabla "Que no se porta y por que" de la seccion de pruebas (sigue vigente): `BD/Scripts/*` (14 scripts de instalacion de BD, 27.844 lineas), `BD/Vistas/*` (393 vistas `Replica*` que consume el job externo) y `BD/Servicios/*` (8 SQL: 4 funciones HANA y PG que el Java de Replica no referencia), `UI/ACC/*`, `BcsReplicaTests/MapeoTIPO_COMP_FACTest` y los `main()` de prueba manual. Son objetos de base de datos o arneses, no logica del servicio; Q-REP-1 y Q-REP-2 quedan para el experto.

### Canarios (mutacion sobre el codigo real)
44 mutantes de una linea sobre el codigo real (`src/main`), aplicados a una copia FUERA del repo (carpeta temporal) con `analysis/otr-sva-optimus-corredores/equivalence/BcsReplica/canary_replica.py` (cada mutante: `mvn -o test`, se detiene en el primer fallo; el archivo se restaura despues). Log: `analysis/otr-sva-optimus-corredores/equivalence/BcsReplica-implementacion-canario.test-output.txt`. **43 muertos, 1 sobreviviente, 0 equivalentes, 0 que no compilan.**
- Cubren: proceso exacto en vez de sin mayusculas, compuertas G-TRG (inicio y fin de pauta) y G-OPT quitadas, compuertas cerradas por defecto abiertas, tablas y parametros del Workstack (`truncarDestino`, espacio de CARTERARV, Action, Key), desfase horario restado, lote vacio, dia habil invertido (tres ciclos), error del dia siguiente (`return` por `break`), `catch` de DROP y de CREATE/UPSERT que ya no se tragan, fecha de los borrados historicos, pais igual a idEmpresa, "S" exacto, `substring` del periodo y del dia, prefijo `90`, parametro diario por defecto, `ServicioNoServidoException` tragada, orden de fechas del respaldo Optimus y del envio post pauta, Key/Action/Table/Stamp/esquema/contador de IniciarReplica, consulta Full vs Delta, orden de tablas Sebra y del respaldo diario, `COD_PER` del respaldo, DELETE de custodia omitido, DROP TRIGGER omitido y puerto de colaborador abierto.
- **Sobreviviente M02** (`VALORIZAR_FFMM`: `equalsIgnoreCase` -> `equals`): NO es equivalente; es una laguna de las pruebas aprobadas. Solo `INICIO_PAUTA` tiene un caso en minusculas (REP-ENV-030); los otros 7 procesos no tienen caso con otra capitalizacion, asi que quitar el `IgnoreCase` en ellos no rompe nada. El codigo es correcto (usa `equalsIgnoreCase` en los 8, como el legacy). No se agrego prueba (las pruebas estan congeladas): se recomienda un caso por proceso en minusculas en el paso 4.
- No hay mutantes equivalentes detectados.

### Estado y abiertos
- Hecho: pasos 1 (pruebas), 2 (aprobacion) y 3 (implementacion). Falta el paso 4 (verificacion independiente con casos frescos contra el oraculo y, con autorizacion, lecturas de solo lectura en HANA QA).
- Abiertos para el experto: Q-REP-1..13 (abajo). Ninguno bloquea las pruebas; todos los defectos se replican hasta que el experto decida.
- No hay runtime acoplado: ningun otro modulo invoca estas clases todavia (el router sigue en TomEE; ver Q-REP-11).

### Preguntas abiertas para el experto de dominio
- **Q-REP-1**: la decision pide portar "los 11 de `BD/Servicios` que usa el codigo". `BD/Servicios` tiene 8 archivos (4 funciones, HANA y PG) y el Java de Replica no referencia ninguno (son llamadas desde las 393 vistas de `BD/Vistas`). Si se refieren a otras 11 funciones (p. ej. `InsertWorkStack`, `InsertWorkStackCartera`, con 933 y 3 usos en el repo), indicar cuales y de que modulo.
- **Q-REP-2**: `BD/Vistas/*` (393) y las tablas `Replica.*`: ¿se gestionan en HANA fuera del servicio (DBA) o hay que migrarlas? Hoy quedan fuera.
- **Q-REP-3** (D-REP-09): ¿la clave primaria de `ResumenReplica` con `UltimaActualizacion` es intencional (historial de cada fin de pauta) o un error? Con ella el `UPSERT` nunca actualiza.
- **Q-REP-4** (D-REP-10): ¿`EnvioPostPauta` debe acumular una fila por fecha de envio (lo que hace) o sobrescribir?
- **Q-REP-5** (D-REP-08): los INSERT historicos fijan `p_idempresa => 1` y `p_fechaconsulta => '0001-01-01'`: ¿vale para toda empresa, y la vista ignora la fecha?
- **Q-REP-6** (D-REP-03 vs D-REP-04): ante un error de `ObtenerDiaSiguiente`, COPIA_PATRIMONIO pierde lo acumulado y los otros procesos lo conservan. ¿Cual es el comportamiento correcto? Se replica el actual.
- **Q-REP-7** (D-REP-14): `COD_DIA "90dd-01"` sin mes: ¿se sobrescribe el respaldo diario del mismo dia de otro mes a proposito (ventana de un mes)?
- **Q-REP-8**: ¿en que entornos se abren las compuertas G-TRG, G-SEB y G-OPT, y quien las abre? El DDL de triggers confirma la transaccion en HANA.
- **Q-REP-9**: ¿`PLA.SegundosZonaHoraria` se suma a la hora local de la JVM (lo que hace el legacy) o debe sumarse a UTC? Con la JVM en una zona distinta de UTC el desfase se duplica.
- **Q-REP-10**: ciclos de dias inhabiles sin tope (D-REP-22): ¿se agrega un maximo (p. ej. 15 dias) en el codigo nuevo?
- **Q-REP-11**: `EnviarTablaReplica.call` / `ReplicaUtils.call` devuelven null y nadie los invoca por JavaCall (los llaman directamente `LaPauta`, `PautaRespaldoReplica`, `RespaldarCustodia`, ...): ¿se exponen como endpoint o se descartan?
- **Q-REP-12**: `INSERT ... select` de custodia (`Insert*Historica`) depende del orden de columnas de las tablas `HIS.Custodia*` (sin lista de columnas): ¿hay un contrato de esquema que la prueba deba verificar contra HANA (solo lectura)?
- **Q-REP-13**: ¿`SQL.exec` con un `;` final se acepta tal cual en HANA (el legacy lo hace en produccion) y se mantiene en el SQL portado?

### Que falta
1. Respuesta del experto a Q-REP-1..13 (alcance de `BD/*`, defectos a conservar o corregir, entornos de las compuertas).
2. Que Plataforma porte `ObtenerDiaSiguiente`, `Feriados`, `ConsultarEmpresa`, `ConfiguracionSistema`, `Utils.getFechaSistema/getSecuencia`: hasta entonces `ColaboradoresNoPortados` lanza `ServicioNoServidoException` y el modulo no corre extremo a extremo.
3. Paso 4 (verificacion independiente): casos frescos contra el oraculo (incluidos los 7 procesos en minusculas, ver M02) y, con autorizacion, lecturas de solo lectura en HANA QA (los respaldos y triggers NO se ejecutan nunca contra HANA desde las pruebas).

## Correccion del texto de Parameters (2026-10-30)

**Causa.** El verify independiente (2026-10-30) dio NOT PROVEN por 27 diferencias reales (25 de desarrollo y 2 frescas: REP-FRE-016 y REP-FRE-018, con Parameters de 501 caracteres) que eran una sola: el texto de la `SQLException` que arma la capa `kit/Sql.java` cuando falla un INSERT/UPSERT/lote. El legacy (`cl.bcs.utils.SQL.rethrow`, `SQL.java:794-814`, llamado desde `batch` en `SQL.java:605`) arma `<causa> Query: <sql> Parameters: ` + `Arrays.deepToString(params.toArray())`, y cada fila es un POJO (`Workstack`, `EnvioPostPauta`, `ResumenReplica`, `FechaRespaldo`) cuyo `toString()` es `Utils.printJson(this, false)`: Gson con `serializeSpecialFloatingPointValues`, `setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")` y `serializeNulls` (escape HTML de Gson activo), campos en el orden del POJO y `null` para los que nadie asigna (p. ej. `"Stamp":null` en el Workstack de `EnviarTablaReplica`). El modulo nuevo imprimia `Map.toString()` de un `HashMap` (`[{Status=0, Action=M, ...}]`).

**Cambio (codigo de produccion, 4 archivos).** `kit/Sql.java`: el mensaje usa el mismo Gson (`Sql.json`) por fila; `Sql.fila` pasa a `LinkedHashMap` (orden de insercion). Los llamadores arman la fila en el orden de los campos del POJO legacy: `EnviarTablaReplicaServicio` (Workstack: `Key, Table, Action, Status, Stamp(null), Parameters`; resumen: `Tipo, Nombre, Estado, Detalle, FechaSistema, UltimaActualizacion`) y `ReplicaUtilsServicio` (`FechaSistema, Tabla, Fecha`); `IniciarReplicaBase` y `RespaldoOptimusJdbc`/`FechaRespaldo` ya estaban en orden. Ninguna prueba aprobada, golden ni asercion se modifico (la comparacion aprobada solo fija la clase de la excepcion).

**Resultados.**
- `mvn -o clean test`: 477 pruebas, 0 fallos (469 + 8 nuevas), 7 XML = 7 clases, sin OutOfMemoryError (`equivalence/BcsReplica-correccion-parameters.test-output.txt`).
- Lado nuevo regenerado con `fresh/driver/FrescoNuevo` (solo `dev-new/` y `fresh/new/`, sin tocar goldens ni `legacy/`) y `compare.py`: casos de desarrollo 264 de 264 iguales; frescos 59 de 59 iguales (mismas mascaras: NPE del runtime y `_provenance`); `EQUIVALENCE.json` y `FRESH_EQUIVALENCE.json` reescritos.
- Prueba nueva `ReplicaMensajeSqlTest` (8): fija el texto literal de `Parameters` (7 casos: REP-POS-005 nulo, REP-OPT-020, REP-INI-013 y REP-INI-038 con Long y Timestamp, REP-ENV-061, REP-ENV-098, REP-ENV-021) y el mensaje completo de las 25 excepciones del golden que traen Parameters.
- Canarios (`canary_replica_correccion.py`, copia fuera del repo, log `equivalence/BcsReplica-correccion-canario.test-output.txt`, XML en `equivalence/canary/BcsReplica-20261030b/`): 10 mutantes de una linea, 9 muertos (todos por `ReplicaMensajeSqlTest`): N01 `Map.toString()` otra vez (8 rojas), N02 sin `serializeNulls`, N03 formato de fecha sin milisegundos, N04 campos en orden alfabetico, N05 sin `"Stamp":null`, N06 orden de `EnvioPostPauta`, N07 etiqueta ` Params: `, N08 ` Query:` sin espacio, N10 orden de `IniciarReplicaBase`. Sobreviviente N09 (doble espacio entre mensaje y causa): la rama `cause.getCause() != null` no se alcanza con H2 (sus excepciones no traen causa), laguna conocida; no es equivalente.

- RULE-076 (transversal): prueba `Rule076CredencialesHardcodeadasTest` (con `Rule076Escaner`); no cubre reglas de dominio (B-REP/D-REP siguen siendo ids propios sin RULE-nnn). Unico `main` admitido: el lanzador Spring Boot `BcsReplicaApplication` con cuerpo `SpringApplication.run(...)` sin literales. Canario en `equivalence/canary/BcsReplica-20261030c/`.
