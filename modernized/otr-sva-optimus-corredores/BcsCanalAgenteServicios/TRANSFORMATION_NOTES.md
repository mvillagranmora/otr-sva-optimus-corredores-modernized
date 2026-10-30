# BcsCanalAgenteServicios — Transformation Notes

Pattern: strangler-fig, hexagonal. Source: Java EE 8 / TomEE (`legacy/otr-sva-optimus-corredores/BSCL/CanalAgente`). Target: Spring Boot 4.1.1 / Java 25.

## Correccion de la nota anterior (2026-10-05)
La nota previa daba por hecho cosas que el legacy no hace. Comparada con `ConsultarHistorialMovimientosCuentaInversion.java`:
| La nota previa decia | El legacy hace |
|---|---|
| Sin cliente "retorna el error Cliente no encontrado." (`erroresContador` 1) | **Lanza `NullPointerException`**: arma el error pero `salida.P_OUT_ERRORESCONTADOR++` opera sobre un `Long` nulo (D-CAN-01). El mensaje no sale nunca. |
| `advertenciasContador` 0 en la respuesta de error | `P_OUT_ADVERTENCIASCONTADOR` no se asigna jamas: sale null (D-CAN-02) |
| Respuesta con saldos / errores y advertencias de la cartola "propagados" | Saldos inicial y final nulos (D-CAN-03); errores, mensajes y contador de la cartola se descartan (D-CAN-04) |
| B-CA-04: "todos los campos de comprobante son null" sin comprobante | Solo folio, DTE y pdf; `CodigoPortafolio` y `DocumentoIdentidad` vienen del portafolio de la fila y salen igual (B-CA-04 corregida; el reemplazo vacio es codigo muerto, D-CAN-07) |
| Enriquecimiento "sobre 5 tablas HANA" con tabla temporal; `synchronized` para serializar la tabla compartida | 6 tablas (la de trabajo mas 5 en los JOIN). La tabla de trabajo es **GLOBAL TEMPORARY ON COMMIT DELETE ROWS** (catalogo HANA QA): privada de la sesion, no hay concurrencia que serializar |
| Endpoint REST `/canal-agente/historial-movimientos` con `idPortafolio`, `idEmpresa`, `idMoneda` obligatorios | El legacy no tiene REST propio: es un `JavaCall` con 9 campos `p_*` (sin ninguna validacion, D-CAN-09), JSON leido con Gson indulgente (D-CAN-10) |
| Solo el SELECT del cliente era testeable en H2 (tabla inventada `PORTAFOLIO_T`, `LIMIT 1`) | Los 4 SQL corren en H2 sobre el esquema REAL del catalogo (`POR.Portafolio`, `CLI.Cliente`, `PER.Persona`, `PER.DocumentoIdentidad`, `FAC.Comprobante`, tabla de trabajo) |
| Faltaba `p_IdModulo`, fechas, moneda, usuario | Van a la cartola sin cambios; `p_IdEstado` y `p_IdCuentaInversion` fijos en null (D-CAN-11) |
| No mencionaba los 3 procedimientos de `BD/Servicios` ni los 3 scripts | Se portan como recursos SQL (7 textos pinados); los scripts de prueba llaman cosas que no existen y no se portan (D-CAN-16) |
Se conservan los ids B-CA-01..05 del esqueleto con el contenido corregido; B-CA-06..13 y D-CAN-01..17 son nuevos.
Estado actual: **pasos 2-3 de modernize-transform hechos: implementacion en verde (136 de 136)**; el paso 1 (pruebas y oraculo, en rojo) quedo registrado en la seccion "CanalAgente: pruebas" y la implementacion en "CanalAgente: implementacion" (al final). El `src/main` del esqueleto de 2026-10-05 fue reemplazado.

## CanalAgente: pruebas

### Que se entrega
| Pieza | Donde | Cantidad |
|---|---|---|
| Casos (entrada, semilla H2, golden grabado del legacy) | `analysis/otr-sva-optimus-corredores/equivalence/BcsCanalAgenteServicios/cases/` y `src/test/resources/canal-caracterizacion/cases/` | **88** (LOG 56, CAL 19, PRC 13) |
| Goldens (`expected.json`, legacy ejecutado sin modificar sobre H2) | idem | 88; 22 con excepcion del legacy; 242 sentencias SQL registradas |
| Estabilidad | `determinism.txt` | 20 corridas por caso = 1.760 corridas, **0 inestables** |
| Pruebas JUnit | `src/test/java/cl/bcs/canalagente/caracterizacion/` | 136 (equivalencia 90, determinismo 2, reglas 29, compuerta 8, SQL 3, catalogo 4) |
| Oraculo y generadores | `analysis/.../equivalence/BcsCanalAgenteServicios/` (`legacy-oracle/record.sh`, `make_cases.py`, `make_schema.py`, `check_canal.py`, `sync_canal.py`, `make_sql_manifest.py`, `ORACLE_NOTES.md`) | — |
| Catalogo REAL de HANA QA (solo SELECT sobre SYS) | `analysis/.../equivalence/BcsCanalAgenteServicios/catalog/` | 270 columnas de 6 tablas, 5 claves, 61 columnas de la vista de portafolio, 4 procedimientos |

(Estado del paso 1, antes de implementar.) Todas las pruebas **compilan** y fallan solo con `not implemented: the production class ... does not exist yet` (`ApiCanalAgente.java` es el unico archivo que alcanza produccion, por reflexion). Estado medido (`mvn -o test`): 136 pruebas, **128 en rojo** (126 con `not implemented: the production class ...`, 1 con `SQL resources missing in src/main/resources/cl/bcs/canalagente/` y la de "equivalence cases executed: 0 => not proven", por diseno) y 8 en verde, que no necesitan produccion (4 del catalogo de la matriz, informe de determinismo, manifiesto de 7 SQL, "todos tienen golden" y la de documentacion D-CAN-16/17).

### Prueba de que el arnes es satisfacible (no se entrega)
Implementacion de referencia escrita FUERA del repo (`$CLAUDE_JOB_DIR/tmp/canalref`, ~330 lineas: compuerta, puertos, servicio con JDBC plano, adaptador de recursos y los 7 SQL copiados): con los mismos goldens pasan las **136 pruebas**.

### Mutacion
46 mutantes de una linea sobre la implementacion de referencia (compuerta invertida o quitada o abierta por defecto, puerto cerrado que devuelve null, cada argumento fijo hacia la cartola -S/N/S, reglas, saldo, cliente, moneda, fechas, matriz-, cliente no encontrado que responde en vez de lanzar, lista nula o elemento nulo tolerados, fechas cruzadas, folio, codigo de portafolio, documento, DTE, pdf, el primero gana en vez del ultimo, contadores y saldos asignados, DELETE omitido o tragado, commit agregado, lote sin ejecutar, Cargo/Abono/Saldo/EsADC/matriz/nombre cruzados, salida invertida, union por posicion, salida de `call` sin nulos, `TOP 2`, JOIN interno en el SELECT de comprobantes, INNER -> LEFT y saldo en 1 dentro de los textos de los procedimientos, etc.): **46 muertos, 0 sobrevivientes, 0 equivalentes**. Los de compuerta (M01, M02, M33, M34) los matan las pruebas de compuerta (sin golden: el legacy no tiene compuertas); los de texto SQL, el manifiesto SHA-256 y los casos que ejecutan el SQL. El mutante M35 (union por posicion) solo lo matan 6 pruebas porque en H2 el orden del SELECT coincide con el de insercion: sin el caso de fila ajena con el mismo id (D-CAN-12) sobreviviria (limite H2 documentado). Detalle por mutante: `equivalence/BcsCanalAgenteServicios-mutantes.txt`.

### Retiro de las pruebas del esqueleto
Se retiraron `CanalAgenteServiceTest` (8 pruebas que afirmaban `erroresContador` 1, `advertenciasContador` 0 y el mensaje "Cliente no encontrado." en una respuesta: el legacy lanza NPE) y `CanalAgenteIntegrationTest` (8: `@SpringBootTest` sobre un endpoint REST `/canal-agente/*` y un `/health` que el legacy no tiene, tabla inventada `PORTAFOLIO_T`, validaciones 400 que el legacy no hace, y dos casos `@Disabled`) con sus recursos (`application-test.yml`, `schema-canal-agente.sql`, `data-canal-agente.sql`). Ninguna corresponde al legacy; lo unico que coincidia (busqueda del cliente por portafolio, delegacion con "S"/"N", campos del movimiento) esta ahora en los goldens del legacy ejecutado. Su `src/main` no se toco. En el `pom.xml` solo se agrego `maven-surefire-plugin` con `-Duser.timezone=UTC -Duser.language=en` (los goldens comparan fechas como texto).

### Que hace el oraculo (resumen; detalle en `ORACLE_NOTES.md`)
- Ejecuta la clase legacy REAL (sin copia: no lee el reloj para nada observable) con BcsUtils real (`SQL`, `NamedParameterStatement`, `JavaCall`, ...) y `PlataformaUtils.MensajeSalida` real sobre H2, detras de una conexion registradora que anota cada SQL (consulta, lote, ejecucion; texto, filas de parametros, orden) y cualquier operacion de transaccion.
- Colaborador fuera del modulo: `ConsultarCartolaDeMovimiento`, doble registrado generado del fuente real (respuestas por caso). En el codigo nuevo sera el puerto `ConsultarCartolaDeMovimientoPort` con valor por defecto cerrado (`ColaboradoresNoPortados` lanza `ServicioNoServidoException`) y compuerta `canalagente.compuertas.cartola=false`.
- **Puerto de la cartola: por que no se reutiliza el servicio ya portado.** `BcsCuentaInversionServicios` tiene `ConsultarCartolaDeMovimientoService` con el mismo contrato JSON (`Entrada`/`Salida` con los nombres del legacy; `Colaboradores.invocar("cl.bcs.cuentaInversion.ConsultarCartolaDeMovimiento", ...)`). Pero es un `@Service` con su propio `NamedParameterJdbcTemplate` y `@Transactional` (no usa la `Connection` que recibe CanalAgente), su `Salida` y su `CartolaDeMovimiento` viven en otro modulo y arrastra su grafo de colaboradores. Por eso el puerto habla JSON (la forma de `Colaboradores`), el adaptador real se decide en el paso 3 (jar plano en proceso, como `BcsCuentaInversionJobs`, o JavaCall) y las pruebas usan un doble. Se revisaron BcsUtils y Plataforma modernizados: `BcsUtils` no tiene `SQL`/`NamedParameterStatement`; `MensajeSalida` existe en `BcsPlataformaServicios` (clase con `idMensaje, tipoMensaje, codigo, descripcion`) y en `BcsCuentaInversionServicios` (record `tipo, codigo, descripcion`), con formas distintas entre si y del legacy; este modulo no lo usa en ninguna salida (el unico uso, `MENSAJE_ERROR`, esta en el camino que lanza NPE) y los mensajes de la cartola viajan como JSON sin tipar: no hay nada que reutilizar.

### Reglas (B-CA) y defectos replicados (D-CAN)
| Id | Regla | Legacy |
|---|---|---|
| B-CA-01 | Busca el IdCliente con SELECT TOP 1 sobre POR.Portafolio y usa el primero | `:157-162` |
| B-CA-02 | Delega a la cartola con "S", saldo por fecha efectiva "N", cuenta y estado nulos | `:172-191` |
| B-CA-03 | Enriquece con INSERT por lote, SELECT y DELETE sobre la tabla de trabajo, en ese orden | `:195-242` |
| B-CA-04 | Sin comprobante: folio, DTE y pdf nulos; codigo de portafolio y documento vienen del portafolio de la fila | `:205-237` |
| B-CA-05 | p_IdMatrizAgrupador / p_EsADC opcionales; viajan a la cartola y vuelven por fila | `:44-49,185-186,238-239` |
| B-CA-06 | FechaMovimiento = FechaSistema, FechaLiquidacion = FechaLiquidacionOperacion, resto 1:1 | `:214-239` |
| B-CA-07 | El orden es el de la cartola | `:210-240` |
| B-CA-08 | Al terminar bien contador de errores 0 y listas vacias | `:243` |
| B-CA-09 | La tabla de trabajo queda vacia (DELETE sin WHERE) | `:242` |
| B-CA-10 | El folio sale como texto (TO_VARCHAR) y el de la cartola se ignora | `:225` |
| B-CA-11 | `call`: Gson de la plataforma, nulos explicitos, fechas `yyyy-MM-dd HH:mm:ss.SSS` | `:255-262` |
| B-CA-12 | El comprobante se une al movimiento por IdCargoAbonoMovimiento | `:205-211` |
| B-CA-13 | El modulo no hace commit, rollback ni cierra la conexion | `:150-252` |
| D-CAN-01 | Cliente no encontrado lanza `NullPointerException`; el mensaje nunca sale | `:163-170` |
| D-CAN-02 | `P_OUT_ADVERTENCIASCONTADOR` siempre null | `:61-70` |
| D-CAN-03 | Saldos inicial y final de efectivo siempre null (la BD da 0) | `:58-59` |
| D-CAN-04 | Errores, mensajes y contador de la cartola descartados | `:191-243` |
| D-CAN-05 | Un fallo a mitad no limpia la tabla de trabajo | `:244-247` |
| D-CAN-06 | Id repetido: el hash se queda con la ultima fila del SELECT sin ORDER BY | `:205-208` |
| D-CAN-07 | El reemplazo por un comprobante vacio es codigo muerto | `:211-212` |
| D-CAN-08 | Salida defectuosa de la cartola (lista nula, elemento nulo, salida nula) | `:191-196` |
| D-CAN-09 | Sin validacion de entrada | `:150-186` |
| D-CAN-10 | Gson de entrada: numeros como texto y `.0` pasan, no numerico falla, mayusculas distinguen | `:255-258` |
| D-CAN-11 | p_IdEstado / p_IdCuentaInversion nulos; valores por omision S/N/S de la cartola | `:177,181` |
| D-CAN-12 | La tabla de trabajo no se limpia antes; filas ajenas se mezclan y el DELETE las borra | `:196-242` |
| D-CAN-13 | Procedimiento de BD de cuenta inversion difiere del Java (RETURN sin error, saldos 0, una fila por fila de cartola, folio numerico) | `BD/Servicios/...CuentaInversion.sql` |
| D-CAN-14 | Procedimiento de BD de custodia: cabecera copiada, `p_IdCliente` comentado con `:p_IdEmpresa`, sin contadores (solo texto, sin caso) | `BD/Servicios/...Custodia.sql` |
| D-CAN-15 | ConsultaAvanzadaClientes: INNER JOIN descarta clientes sin fila; variable de tabla no declarada | `BD/Servicios/ConsultaAvanzadaClientes.sql` |
| D-CAN-16 | Scripts `BD/Scripts/Prueba*.sql` apuntan a procedimientos o parametros inexistentes (sin caso) | `BD/Scripts/` |
| D-CAN-17 | HANA QA tiene `ConsultarFlujoInstrumento` sin fuente en el repositorio (sin caso) | `catalog/canal_procedimientos.csv` |
Ningun `RULE-nnn` de `BUSINESS_RULES.md` describe este modulo (se busco `ConsultarHistorial`, `Cliente no encontrado`, tabla de trabajo y canal); `RULE-076` (credenciales en `main()`) no aplica: el `main()` de la clase usa `connections.properties` y no se ejecuta.

### Limites H2 vs HANA
Resumen (detalle en `ORACLE_NOTES.md`): (1) la tabla de trabajo es una tabla normal en H2 (en HANA GLOBAL TEMPORARY ON COMMIT DELETE ROWS: sin aislamiento por sesion ni borrado al commit); (2) el SELECT de comprobantes no tiene ORDER BY, H2 devuelve en orden de insercion: los casos de id repetido fijan ese orden; (3) `TO_VARCHAR` se traduce a `CAST(... AS VARCHAR)`; (4) los procedimientos SQLScript solo corren en HANA: se pinan por SHA-256 y se ejecuta en H2 solo el SELECT final de dos de ellos con tablas de apoyo y la vista de portafolio como tabla; (5) los SQL no usan COALESCE ni CASE con decimales y el centinela `0001-01-01` solo es valor por omision de columnas de fecha que ningun caso lee; (6) `FolioComprobante` es NOT NULL en HANA, asi que no se prueba `TO_VARCHAR(NULL)`.

### Que no se porta y por que
- `main()` de la clase (arnes manual con conexion real): no es servicio.
- `BD/Scripts/Prueba*.sql`: material de prueba manual con llamadas rotas (D-CAN-16); se uso para armar casos, no se porta como codigo.
- Ejecucion de los procedimientos HANA (`CALL`): el legacy Java nunca los llama; solo se porta su texto como recurso SQL detras de `ProcedimientosBdPort`.
- `ConsultarCartolaDeMovimiento`: colaborador de otro modulo, detras de puerto cerrado (ver arriba).

### Preguntas abiertas (requieren decision humana)
- **Q-CAN-01**: el cliente no encontrado hoy lanza NPE (D-CAN-01). Replicar bug-for-bug (lo que fijan las pruebas) o corregir para devolver `P_OUT_ERRORESCONTADOR` 1 con "Cliente no encontrado." como pretendia el codigo. Cambiarlo es un cambio de contrato visible al llamador (hoy ve un error de servidor).
- **Q-CAN-02**: adaptador real de la cartola: jar plano de `bcs-cuenta-inversion-servicios` en proceso (su servicio usa su propia conexion/transaccion) o llamada JavaCall. Condiciona abrir la compuerta `canalagente.compuertas.cartola`.
- **Q-CAN-03**: saldos inicial/final (D-CAN-03) y advertencias (D-CAN-02): el procedimiento de BD devuelve 0/0; el Java, null. ¿Cual consume el canal?
- **Q-CAN-04**: errores de la cartola descartados (D-CAN-04): hoy un fallo de requisitos basicos se ve como una lista vacia exitosa. ¿Se propaga en la version nueva?
- **Q-CAN-05**: ¿quien invoca hoy los 3 procedimientos de `BD/Servicios` y `ConsultarFlujoInstrumento` (existe en QA sin fuente)? Si nadie, bastaria el texto pinado.
- **Q-CAN-06**: tipos de fecha del movimiento: el legacy usa `java.sql.Date` con la hora que traiga (los casos CAL de fechas con hora lo fijan). Si la cartola nueva entrega `LocalDate`, hay que decidir si se acepta esa diferencia.
- **Q-CAN-07**: el orden del SELECT de comprobantes y los ids repetidos (D-CAN-06) dependen del motor; confirmar con HANA real (solo lectura) si en la practica puede haber ids repetidos.

## CanalAgente: implementacion (pasos 2-3)

### Resultado
| Medida | Valor |
|---|---|
| `mvn -o clean test` | **136 pruebas, 0 fallos, 0 errores, 0 omitidas** (equivalencia 90, determinismo 2, reglas 29, compuerta 8, SQL 3, catalogo 4); 6 XML = 6 clases; sin OutOfMemoryError |
| "equivalence cases executed" | **88 de 88** (el legacy lanza en 22) |
| Log | `analysis/otr-sva-optimus-corredores/equivalence/BcsCanalAgenteServicios-implementacion-verde.test-output.txt` |
| Canarios | 53 mutantes de una linea sobre el codigo real (copia fuera del repo): **50 muertos, 3 sobreviven (los 3 equivalentes), 0 invalidos**; log `...-implementacion-canario.test-output.txt` |
| `legacy/` | `legacy_manifest.py verificar`: different 0, missing 0, new 0 |
| Pruebas, goldens, casos, `ApiCanalAgente.java` | sin cambios (ni una linea) |

### Tabla legacy a codigo nuevo
Todo en `src/main/java/cl/bcs/canalagente/` salvo los recursos.
| Legacy (`ConsultarHistorialMovimientosCuentaInversion.java`, salvo indicacion) | Nuevo |
|---|---|
| `:31-34` carga de los 4 SQL | `service/ConsultarHistorialMovimientosCuentaInversionServicio` (constantes estaticas) con `adapter/out/jdbc/SqlRecursos`; los 4 SQL en `src/main/resources/cl/bcs/canalagente/sql/` |
| `BD/Servicios/*.sql` (3) | `src/main/resources/cl/bcs/canalagente/bd/servicios/`; `port/out/ProcedimientosBdPort` + `adapter/out/bd/ProcedimientosBdRecursos` |
| `:36-75,123-140` `Entrada`, `Salida`, `AdvertenciasDetalle`, `ErroresDetalle` | clases anidadas publicas del servicio (mismos nombres y orden de campos; sin `toString`/`Utils.printJson`) |
| `:77-108` `MovimientoCuentaInversion` | `Movimiento` anidado (26 campos, fechas `java.sql.Date`) |
| `:110-121,142-148` `MovimientoCuentaInversionComprobante`, `IdClientePortafolio` | `MovimientoComprobante`, `IdClientePortafolio` anidados |
| `:150-153` inicio | `Compuertas.gCartola()` (NUEVO, unico agregado), traza INFO |
| `:157-162` busqueda del cliente | `SqlNombrado.consultar` (SQL.query + NamedParameterStatement de BcsUtils) |
| `:163-170` cliente no encontrado | igual, incluido el `NullPointerException` (D-CAN-01) |
| `:172-186` armado de la entrada de la cartola | `service/CartolaJson.Entrada` (POJO JSON con los mismos nombres y los valores por omision del constructor del legacy: `p_Validaciones` "S", `p_IncluyeSaldo` "N", `p_IncluyeAnulados` "S") |
| `:188-193` llamada a la cartola | `port/out/ConsultarCartolaDeMovimientoPort.consultar(Connection, JsonObject)`; por defecto `ColaboradoresNoPortados` |
| `:196` INSERT por lote | `SqlNombrado.lote` (SQL.batch) |
| `:201-208` SELECT y hash por id | `SqlNombrado.consultar` y `HashMap.put` (el ultimo gana: D-CAN-06) |
| `:210-241` armado de los movimientos | igual, campo a campo (B-CA-06/B-CA-10/B-CA-12) |
| `:242-243` DELETE y contador 0 | `SqlNombrado.ejecutar` (SQL.exec), `P_OUT_ERRORESCONTADOR = 0L` |
| `:244-247` catch con log y relanzar | igual (log ERROR y `throw e`; sin limpiar la tabla: D-CAN-05) |
| `:254-263` `call` | `call(Connection, JsonObject)` con `kit/GsonPlataforma` (Gson de la plataforma: nulos explicitos, fechas `yyyy-MM-dd HH:mm:ss.SSS`, lectores indulgentes de fecha de `GsonDateDeserializer` / `GsonTimestampDeserializer`) |
| `BcsUtils` `SQL.query/batch/exec`, `NamedParameterStatement` | `adapter/out/jdbc/SqlNombrado` (misma semantica: quita comentarios con la misma expresion, `:nombre` fuera de comillas, `setNull(VARCHAR)`, mapeo por campo publico sin conversion, `rethrow` con ` Query: ... Parameters: [...]`, `Map Object failed`, BigDecimal a 16 decimales HALF_DOWN en el lote) |
| `PlataformaUtils.MensajeSalida.MENSAJE_ERROR` (`:165`) | constante local `"ERROR"` (valor del legacy; nunca sale: el NPE va antes) |

### Desvios respecto del legacy (todos deliberados)
1. **Compuerta G-CTA** (`canalagente.compuertas.cartola`, por defecto `false`): `logica` lanza `ServicioNoServidoException` antes del primer SQL. El legacy no tiene compuertas. `Compuertas` no lleva `@Component` ni nada que la abra; con la compuerta abierta y el puerto por defecto, `ColaboradoresNoPortados` lanza la misma excepcion tras buscar el cliente y antes de escribir.
2. **Puerto JSON de la cartola** en lugar de la llamada directa a la clase de otro modulo (razones en la nota del paso 1: no se reutiliza `ConsultarCartolaDeMovimientoService`). El POJO de salida `CartolaJson.Movimiento` declara solo los 21 campos que CanalAgente lee; los demas se ignoran como Gson hace con campos desconocidos. Consecuencia: un campo NO usado con tipo invalido en la respuesta de la cartola (p. ej. `Monto` no numerico) fallaria en el legacy y aqui pasa. Los casos fijados (listas nulas, elementos nulos, salida nula) se comportan igual.
3. **Sin traza de tiempos** (`Tiempo SQL_...`), sin `@Trace` de Datadog y sin `toString` con `Utils.printJson`: observabilidad, no comportamiento. Log con SLF4J.
4. **SQL**: sin lista como parametro ni traduccion a PostgreSQL (`isPostgresConnection`): ningun SQL del modulo la usa y HANA es el unico destino.
5. **Sin web**: se borraron `CanalAgenteController`, `GlobalExceptionHandler`, `InternalJavaCallController`, `CanalAgenteService`, `CanalAgenteDataPort`, `CanalAgenteRepositoryAdapter`, `ConsultarHistorialMovimientosUseCase` y los DTO de `domain/`: todo inventado (el legacy no tiene REST propio, devolvia error donde el legacy lanza, etc.). No hay handler `JavaCall` nuevo. Se conservan `BcsCanalAgenteServiciosApplication`, `application.yml`, `Dockerfile` y `pom.xml` sin cambios (ver "Abiertos de plataforma").

### Defectos del legacy replicados (bug-for-bug)
Los D-CAN-01 a D-CAN-12 estan replicados en codigo y fijados por goldens; el codigo marca con un comentario los D-CAN-01, 02-04 (en conjunto), 05, 06 y 07:
D-CAN-01 (NPE de "Cliente no encontrado."; **no se corrige**, decision aprobada), D-CAN-02 (advertencias nulas), D-CAN-03 (saldos nulos), D-CAN-04 (errores de la cartola descartados), D-CAN-05 (fallo a mitad sin limpiar), D-CAN-06 (el ultimo gana), D-CAN-07 (reemplazo vacio muerto), D-CAN-08 (salida defectuosa de la cartola: lista nula = `SQLException` "Null parameters", elemento nulo = `SQLException` "Map Object failed", salida nula = NPE), D-CAN-09 (sin validacion), D-CAN-10 (Gson indulgente), D-CAN-11 (`p_IdEstado`/`p_IdCuentaInversion` nulos), D-CAN-12 (tabla de trabajo sin limpiar antes). D-CAN-13 a D-CAN-17 son de los procedimientos de BD y scripts: se portan solo como texto pinado (sin caso ni codigo).

### Que NO se porta y por que
- `main()` (`:265-286`): arnes manual con conexion real y credenciales de `connections.properties`; no es servicio.
- `BD/Scripts/Prueba*.sql`: material manual con llamadas rotas (D-CAN-16).
- Ejecucion de los 3 procedimientos HANA: el legacy Java no los llama; solo se porta su texto.
- `ConsultarCartolaDeMovimiento`: de otro modulo, detras del puerto cerrado.
- Endpoints REST, handler JavaCall y beans que abran la compuerta (ver desvio 5).
- HANA: nunca se toco. La tabla de trabajo es GLOBAL TEMPORARY ON COMMIT DELETE ROWS en HANA y tabla normal en H2 (limite documentado arriba): el aislamiento por sesion y el borrado al commit NO estan probados.

### Canarios (mutacion sobre el codigo real)
Copia del modulo fuera del repo, una mutacion de una linea por corrida de `mvn -o test`, 53 mutantes: compuerta quitada, invertida y abierta por defecto; puerto no portado que devuelve null; cada argumento hacia la cartola (reglas S/N, saldo por fecha, cuenta, estado, cliente, portafolio, fechas cruzadas, moneda, matriz, EsADC, defaults de Validaciones/IncluyeSaldo/IncluyeAnulados); D-CAN-01 corregido; el primero gana; DELETE omitido, con WHERE, con commit y tras fallo; contadores y saldos asignados; campos del movimiento cruzados (fecha, folio, codigo, DTE, Cargo); comprobante vacio siempre; excepcion tragada; lista y elemento nulos tolerados; lote sin `addBatch` y sin ejecutar; `exec` sin ejecutar; comillas dobles sin proteger; Gson sin nulos, con otra fecha y con otro largo; 4 SQL y 1 procedimiento alterados; adaptador de procedimientos con nombre fijo. **50 muertos**. Sobreviven 3, equivalentes:
- M51: el codigo del error ("ERR" por "ERROR") es inobservable, el NPE sale antes.
- M52: la conexion nula hacia el puerto de la cartola; el doble de prueba la ignora. Equivalente solo para el arnes: cuando exista el adaptador real (Q-CAN-02) hace falta una prueba que verifique que recibe la conexion del llamador.
- M53: reescala del `BigDecimal` del lote; el INSERT solo lleva `Long`, la rama no se ejerce aqui.
Detalle por mutante: `equivalence/BcsCanalAgenteServicios-implementacion-canario.test-output.txt`.

### Pruebas imposibles
Ninguna. Las 136 pasaron sin cambiar pruebas, goldens ni `ApiCanalAgente.java`.

### Abiertos de plataforma (nuevos, ademas de Q-CAN)
- **P-CAN-01 (resuelto 2026-10-30, decision del usuario)**: el modulo es un jar de libreria en proceso. Se retiraron `BcsCanalAgenteServiciosApplication`, `application.yml` y el `Dockerfile`; el `pom.xml` perdio web, actuator, validacion y el plugin de Spring Boot; el servicio `bcs-canal-agente-servicios` y su `depends_on` salieron de `docker-compose.yml`. Quedan, sin cambios: la ruta del `BcsGateway` (con `enabled: false`) y el mapeo de `cl.bcs.canalagente` a `:8102` en `BcsPlataformaWeb` (README y variable `BCS_CANAL_AGENTE_SERVICIOS_URL`), que habra que retirar o reapuntar cuando se cablee el despacho; con la lista de clases habilitadas vacia todo va a TomEE.
- **P-CAN-02 (resuelto)**: se corrigieron el `README.md` y `API_ASSUMED.md` de `src/test/java/.../caracterizacion/` (solo texto; ninguna prueba, golden ni aserciones).

### Preguntas abiertas
Q-CAN-01 a Q-CAN-07 siguen abiertas, para el experto (ver la lista de la seccion de pruebas): no se corrigio ningun defecto. Q-CAN-01 se resolvio como replicar (decision aprobada), las demas no cambian el codigo hoy.
