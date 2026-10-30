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
El estado real es: **paso 1 de modernize-transform (pruebas de caracterizacion y oraculo, en rojo)**. No hay codigo de produccion de la logica; el `src/main` previo se reemplaza en el paso 3.

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

Todas las pruebas **compilan** y fallan solo con `not implemented: the production class ... does not exist yet` (`ApiCanalAgente.java` es el unico archivo que alcanza produccion, por reflexion). Estado medido (`mvn -o test`): 136 pruebas, **128 en rojo** (126 con `not implemented: the production class ...`, 1 con `SQL resources missing in src/main/resources/cl/bcs/canalagente/` y la de "equivalence cases executed: 0 => not proven", por diseno) y 8 en verde, que no necesitan produccion (4 del catalogo de la matriz, informe de determinismo, manifiesto de 7 SQL, "todos tienen golden" y la de documentacion D-CAN-16/17).

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

### Que falta para implementar (paso 3)
`kit` (`Compuertas`, `ServicioNoServidoException`), puertos (`ConsultarCartolaDeMovimientoPort`, `ColaboradoresNoPortados`, `ProcedimientosBdPort`) y `ProcedimientosBdRecursos`, el servicio con sus clases `Entrada`/`Salida`/`Movimiento`, y los 7 SQL copiados a `src/main/resources/cl/bcs/canalagente/{sql,bd/servicios}/`; retirar el `src/main` del esqueleto. Las firmas estan en `src/test/java/cl/bcs/canalagente/caracterizacion/API_ASSUMED.md`.
