# BcsReplica — Transformation Notes

Pattern: strangler-fig, hexagonal. Source: Java EE 8 / TomEE (`legacy/otr-sva-optimus-corredores/BSCL/Replica`). Target: Spring Boot 4.1.1 / Java 25, port 8093.

## Correccion de la nota anterior
La nota previa de este modulo decia "completo" y que `EnviarTablaReplica.java` eran "~24000 lineas de SQL puro HANA, sin logica portable, @Disabled". Es falso:
- `EnviarTablaReplica.java` tiene **546 lineas de Java**: 8 procesos con logica de encolado, ciclo de dias inhabiles con colaboradores de Plataforma, triggers, resumen y limpieza. Sus SQL son **58 archivos** (44.615 bytes en total), una sentencia cada uno.
- Lo que sumaba ~24.000 lineas eran los scripts de instalacion de BD (`BD/Scripts/*`, 14 archivos, 27.844 lineas), que no son codigo del servicio.
- El esqueleto de `src/main` inventa tablas (`WORKSTACK_T`, `CONFIGURACION_T`), endpoints REST (`/api/replica/*`) y una clave `REPLICA_FULL` que el legacy no tiene; el legacy encola en `BSCL.Plataforma.BD::PLA.Replica.WorkStack` con `Key "-1"`. No se ejecuto contra el legacy ni se compara con el.
El estado real es: **paso 1 de modernize-transform (pruebas de caracterizacion y oraculo, en rojo)**. No hay codigo de produccion de la logica.

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

Todas las pruebas **compilan** y fallan solo con `not implemented: the production class ... does not exist yet` (`ApiReplica.java` es el unico archivo que alcanza produccion, por reflexion). Estado medido (`mvn -o test`): 469 pruebas, **461 en rojo** (460 con `not implemented: the production class ...` y la de "equivalence cases executed: 0 => not proven", por diseno) y 8 en verde, que no necesitan produccion (4 del catalogo de la matriz, informe de determinismo del oraculo, manifiesto de los 58 SQL, "todos tienen golden", recuento de casos sin compuerta).

### Prueba de que el arnes es satisfacible (no se entrega)
Implementacion de referencia escrita FUERA del repo (`%TEMP%/repref`, ~600 lineas: puertos, servicios, adaptadores JDBC con los 58 SQL): con los mismos goldens pasan las **469 pruebas**.

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
- `src/main` (controlador REST, servicio, puerto, adaptador y dominio del esqueleto) NO se toco: se reemplaza en el paso 3. Lo que coincide con el legacy y se conserva como intencion: puerto `Workstack` con Action "F" para Full/Delta, `QueryReplica` / `QueryReplicaDelta`, `substring(0,7)` y la regla "S".

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

### Que falta para pasar a implementar (paso 2 y 3)
1. Respuesta del usuario a Q-REP-1 y Q-REP-2 (alcance de `BD/*`) y decision sobre Q-REP-3..7 (replicar el defecto es lo vigente).
2. Decidir entornos y duenos de las compuertas (Q-REP-8) y la forma de los puertos de colaborador (si Plataforma portara `ObtenerDiaSiguiente`/`Feriados`/`ConsultarEmpresa` antes, se reemplaza `ColaboradoresNoPortados`).
3. Paso 3: escribir `cl.bcs.replica.{kit,port.out,adapter.out.jdbc,envio,respaldo,inicio,domain}` segun `API_ASSUMED.md`, copiar los 58 SQL a `src/main/resources/cl/bcs/replica/sql/`, reemplazar el esqueleto de `src/main` y dejar las 469 pruebas en verde con los goldens de siempre. La implementacion de referencia usada para validar el arnes no se entrega.
4. Paso 4 (verificacion independiente): casos frescos contra el oraculo y, con autorizacion, lecturas de solo lectura en HANA QA (los respaldos y triggers NO se ejecutan nunca contra HANA desde las pruebas).
