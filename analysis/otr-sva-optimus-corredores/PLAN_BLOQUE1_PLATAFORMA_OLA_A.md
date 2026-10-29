# Plan del bloque 1: Plataforma, ola A (`BcsPlataformaServicios`)

Fecha: 2026-10-21. Estado: PLAN para aprobacion (no se escribio codigo ni se toco legacy, modernized ni HANA al producirlo).
Fuentes leidas: `PRIORIDAD_POR_USO.md`, `MODERNIZATION_BRIEF.md` (fase 5), `BUSINESS_RULES.md`, `JOBS_PATTERN.md`, `TRANSFORMATION_NOTES.md` de Contabilidad y Tesoreria, `hana_qa_results/pla_servicio_filas.csv`, README de `BcsPlataformaWeb` (router interno), `VERIFICATION.md` (linea BcsPlataformaServicios), `BEHAVIORAL_EQUIVALENCE_AUDIT_PLATAFORMA.md`, y el codigo legacy de las clases en alcance.

## 0. Resumen y corte recomendado

**Corte recomendado: 4 clases JavaCall (5 ids del registro) mas su cierre de dependencias.** Cubren 1.908.575 de los 1.927.385 usos de plataforma (99,0 %) y 79,2 % de todo el uso Java (tipo J, 2.410.604).

| # | Clase (FQN, `PLUGIN_PLA.jar@...`) | Ids registro | Uso | LOC legacy | Naturaleza |
|---|---|---|---:|---:|---|
| 1 | `cl.bcs.plataforma.cre.ConsultarEstadoModulo` | 600017 | 1.777.141 | 124 | lectura pura, 1 SELECT |
| 2 | `cl.bcs.plataforma.mer.CalcularParidad` | 3155 y 600056 | 74.457 (15.489 + 58.968) | 959 | lectura pura, calculo en memoria (6 SELECT) |
| 3 | `cl.bcs.plataforma.pla.ConsultarFlujoCaja` | 1673 | 52.679 | 338 (+891 de `SaldoFlujoCaja`) | lectura con 2 tablas de paso (INSERT/DELETE) y 2 colaboradores de otros modulos |
| 4 | `cl.bcs.plataforma.com.ConsultarMatrizOperacional` | 20052 | 4.298 | 518 (+ ~980 de cobros/productos) | lectura pura, 6 SELECT en 3 clases |

Cierre de dependencias que hay que portar (no tienen uso propio relevante): `RevisarRequisitosBasicos` (151), `SaldoFlujoCaja` (891), `ConsultarProductosPlantilla` (136), `ConsultarCobrosPlantilla` (414), `ConsultarCobrosPlantillaPeru` (429), `pla.utils.ConfiguracionSistema` (94), `pla.utils.Diccionario` (148) y `PlataformaUtils.MensajeSalida`. Total aproximado: 1.939 LOC de las 4 clases + 2.263 de cierre = ~4.200 LOC brutas de las 103.816 del modulo (4 %).

**Donde cortar (decision del usuario, ver seccion 10):**
- **Recomendado: cortar en la clase 4.** Las clases 5 a 15 aportan en total 0,78 % del uso de plataforma (15 clases = 99,80 %), tienen escrituras o mucha logica (ver seccion 3) y no cambian el efecto.
- Alternativa "ola A2" (opcional, despues de PROVEN la ola A): clases 6, 8 y 9 (`ConsultarDatosInstrumentos`, `ConsultarInstrumentoPrecioBolsa`, `ConsultarRequisito`), que parecen lecturas (no analizadas en profundidad).
- Fuera de la ola A: `ModificarEstadoEventoAutorizacionMasivo` (1.127 LOC, escritura), `GuardarConfiguracionGrillaUsuario` (escritura), `AgregarRolUsuario` y `CambiarClaveUsuario` (RULE-029/035/037, seguridad de claves), `Valorizar` (1.522 LOC).

**Hallazgo que condiciona todo el plan:** `modernized/.../BcsPlataformaServicios` YA existe (1.444 archivos, 350 en `src/main/java`, 1.090 SQL), pero es un esqueleto: solo `ConvertirMontoEscrito` esta implementado; las otras 326 clases son stubs que responden 200 sin hacer nada o con un algoritmo distinto del legacy. Tiene 46 tests (34 de `ConvertirMontoEscrito` y 12 de contexto) y `VERIFICATION.md` lo marca **PARTLY PROVEN (0 de 60 reglas)**. Los 4 servicios del alcance existen como stub y divergen del legacy (seccion 2). El plan los trata como NO existentes: se reescriben con el patron probado y se reaprovechan solo los 1.086 SQL (byte a byte iguales al legacy; 4 difieren y ninguno esta en el alcance).

## 1. Reglas vinculantes del proyecto (se repiten aqui; los agentes las heredan)

1. **Ningun cambio de base de datos, de ningun tipo:** ni tablas, columnas, vistas, procedimientos, indices, ni datos de configuracion. Solo se leen y escriben las tablas y vistas que el legacy ya usa, con los mismos SQL. Las dos tablas de paso de `ConsultarFlujoCaja` (INSERT/DELETE) las escribe el legacy hoy: se conserva exactamente ese comportamiento y no se crea ni se altera nada.
2. **HANA QA solo con SELECT de solo lectura**, solo si es estrictamente necesario, con las credenciales del `.env` local, sin imprimirlas ni guardarlas, y solo con los programas existentes de `analysis/otr-sva-optimus-corredores/tools` (`HanaReadOnlyQueries.java`, `ExportPlaServicio.java`, `ExportTemplates.java`). Nunca INSERT/UPDATE/DELETE/CALL/DDL. Este plan no ejecuto ninguna consulta HANA.
3. **El legacy nunca se modifica** (`legacy/` es de solo lectura; el oraculo compila copias en un directorio temporal, nunca dentro de `legacy/`).
4. **Defectos del legacy se mantienen bug a bug** y se listan para el experto de dominio (seccion 9), salvo donde el brief exija correccion (RULE-004/005/046 en BcsFilters/BcsPlataformaWeb; RULE-068 es del bloque 2). Cada defecto queda fijado por al menos un test.
5. **Sin secretos del legacy como fixtures:** los `main()` de las 4 clases traen credenciales y hosts HANA en claro (p. ej. `ConsultarEstadoModulo.main`); no se copian, y se cubre con el test RULE-076 (ningun `main()` ni literal de credencial/JDBC en `src/main`). Ademas `QA_ACCESS_PROCEDURE.md` contiene credenciales HANA en claro: no copiarlas a ningun archivo nuevo, y se recomienda sacarlas de ese archivo (hallazgo, seccion 10).
6. **Commits sin `Co-Authored-By`** (regla del proyecto; prevalece sobre la plantilla por defecto).
7. Produccion es Linux: LF, zona horaria, charset y locale fijados en la JVM (`-Dfile.encoding=UTF-8 -Duser.timezone=<zona> -Duser.language=en`, ver `JOBS_PATTERN.md`); los tests corren en una sola JVM UTC (H2 cachea la zona).
8. Ningun trafico real hasta que el modulo/clases esten PROVEN y firmados (`VERIFICATION.md`); el enrutamiento es opt-in por clase (`plataforma.javacall.clases-habilitadas`, vacio = todo a TomEE).

## 2. Estado actual de `modernized/.../BcsPlataformaServicios` (no se modifico)

Spring Boot 4.1.1 / Java 25, artefacto `cl.bcs:plataforma-service`, puerto 8114, hexagonal nominal (`domain`, `port`, `service`, `adapter/in/rest`, `adapter/out/jdbc`). Tiene `src/main/resources/application.yml`, Dockerfile y README.

| Hallazgo | Evidencia | Consecuencia para el plan |
|---|---|---|
| 326 clases `*Service implements JavaCallService` registradas por FQN en `InternalJavaCallController` | `service/**/*Service.java`; el controlador mapea `fqn()` -> bean y responde 200 con lo que devuelva el stub | Si el router enruta una de esas clases al modulo, responde 200 con datos falsos (el audit `BEHAVIORAL_EQUIVALENCE_AUDIT_PLATAFORMA.md` cuenta ~178 stubs que devuelven `P_OUT_ERRORESCONTADOR = 0` sin hacer nada y 37 clases sin contraparte). **Paso 0 obligatorio: dejar de servir todo lo que no este en una lista blanca.** |
| `InternalJavaCallController` sin token interno, sin filtro de ruta, sin unidad de trabajo por llamada, el 500 devuelve `e.getMessage()` | `adapter/in/rest/InternalJavaCallController.java` | Falta el patron de Contabilidad/Tesoreria: `TokenInternoFilter` (falla cerrado, ARQ-H3), `JavaCallUnitOfWorkFilter` (DEV-21), 500 generico con correlationId |
| `ConsultarEstadoModuloService` (stub) | omite `p_out_*` nulos como `[]` (el legacy no emite `p_out_ErroresDetalle` ni `p_out_AdvertenciasDetalle`: Gson sin `serializeNulls`) y convierte todos los valores a texto; ante error responde 200 con `P_OUT_ERRORESCONTADOR=1` (el legacy lanza la excepcion) | Contrato JSON distinto: reescribir y fijar con goldens |
| `CalcularParidadService` (stub, 96 lineas vs 959) | comentario "TODO: requires pla.* utility"; devuelve `FactorCambio` en vez de los 28 campos de `CalculoSalida`; ignora `p_IdEmpresa`, moneda local, pivote USD, uso, UF/UTM | Es otro algoritmo: descartar |
| `ConsultarFlujoCajaService` (stub) | no llama `RevisarRequisitosBasicos`, no resuelve mensajes, el DELETE inline lleva prefijo `"BSCL".` que el legacy no tiene (`SQL_DELETE...` difiere), `p_IdEmpresa` por defecto 1, `p_IdModulo` por defecto "PLA" | Descartar y reescribir |
| `ConsultarMatrizOperacionalService` (72 lineas vs 518+) | sin plantillas, sin cobros | Descartar |
| Utilidades `pla.*` (`ConfiguracionSistema`, `Diccionario`, `MensajeSalida`, `RevisarRequisitosBasicos`) no portadas | el audit las llama "bloquean los 176 servicios con TODO" | Son la primera pieza del bloque (kit de plataforma) |
| SQL | 1.090 SQL en `src/main/resources/cl/bcs/plataforma/**`; comparados por contenido con el legacy: 1.086 identicos, 4 distintos (`ActualizarUsuario_ClaveUsuarioReplica`, 2 de `NotificarEventoAutorizacion_*`, `InsertarLogVentana_InsertRegistroLogVentana`), 0 faltantes | Activo reutilizable; los 4 distintos estan fuera del alcance (anotarlos para la ola B) |
| Dependencias | `BcsUtils` moderno NO trae `SQL`, `ConfiguracionSistema` ni `Diccionario` (solo `Fecha`, `Utils`, `Timer`, `JavaCall`, `BcsPojo`...) | El kit propio debe traer un ayudante de SQL con parametros nombrados y listas `IN (:x)` |
| Contabilidad ya porto 15 SQL de configuracion y `PlataformaConfiguracionJdbc`/`CodigoDiccionarioAdapter` | `BcsContabilidadServicios/.../infrastructure/plataforma/` | Tomarlos como modelo; cuando exista el kit en Plataforma, Contabilidad puede dejar de duplicar (DEV-18 se cierra con el diccionario real) |

Estado de verificacion: `VERIFICATION.md` lo lista PARTLY PROVEN (0 de 60 reglas probadas; columnas de equivalencia y seguridad "not proven"); el audit de 2026-10-07 y las firmas previas corresponden a la version stub y no valen para este bloque.

## 3. Top 15 clases JavaCall de plataforma por uso (CSV `pla_servicio_filas.csv`, tipo J, 178 clases, 1.927.385 usos)

El CSV tiene 981 filas J; las clases con ruta `*@cl.bcs.plataforma.*` son 178 FQN distintos (180 filas: `CalcularParidad` figura dos veces y se suman). Porcentaje acumulado sobre el uso de plataforma. LOC = `wc -l` bruto del legacy. "Escritura" es una heuristica por busqueda de INSERT/UPDATE/DELETE/MERGE en Java y SQL (las clases 5 a 15 no se leyeron a fondo).

| # | FQN | Uso | Acum. | LOC | Lectura/escritura (heuristica) | Nota |
|---:|---|---:|---:|---:|---|---|
| 1 | `cre.ConsultarEstadoModulo` | 1.777.141 | 92,20 % | 124 | lectura | `RegistraUso=N`; la UI la consulta cada 15 s por sesion |
| 2 | `mer.CalcularParidad` | 74.457 | 96,07 % | 959 | lectura | usada en proceso por ~40 clases legacy de 6 modulos |
| 3 | `pla.ConsultarFlujoCaja` | 52.679 | 98,80 % | 338 | lectura + tablas de paso | llama `SaldoFlujoCaja` (891) |
| 4 | `com.ConsultarMatrizOperacional` | 4.298 | 99,02 % | 518 | lectura | |
| 5 | `aut.ModificarEstadoEventoAutorizacionMasivo` | 2.775 | 99,17 % | 1.127 | escritura | autorizaciones: ola B |
| 6 | `ins.ConsultarDatosInstrumentos` (`PLUGIN_OPTIMUS`) | 2.312 | 99,29 % | 507 | posible tabla de paso | candidata ola A2 |
| 7 | `pla.GuardarConfiguracionGrillaUsuario` | 1.891 | 99,39 % | 126 | escritura | ola B |
| 8 | `ins.ConsultarInstrumentoPrecioBolsa` | 1.796 | 99,48 % | 395 | posible tabla de paso | candidata ola A2 |
| 9 | `ind.ConsultarRequisito` (`PLUGIN_ORD.jar`) | 1.630 | 99,56 % | 380 | lectura (a verificar) | candidata ola A2 |
| 10 | `acc.AgregarRolUsuario` | 1.195 | 99,63 % | 317 | escritura | seguridad: ola B |
| 11 | `acc.CambiarClaveUsuario` | 932 | 99,67 % | 280 | escritura | RULE-029/035/037: ola B |
| 12 | `val.Valorizar` | 915 | 99,72 % | 1.522 | lectura (a verificar) | grande: ola B |
| 13 | `com.CrearPerfilCobrosDetalle` | 623 | 99,75 % | 323 | escritura | ola B |
| 14 | `com.CrearPerfilProductoDetalle` | 526 | 99,78 % | 281 | escritura | ola B |
| 15 | `com.CrearPerfilProducto` | 427 | 99,80 % | 301 | escritura | ola B |

Notas: (a) `ConsultarEstadoModulo` tiene `RegistraUso=N` y aun asi acumula el contador: lo cuenta el despachador, no el registro; (b) rutas con jar distinto de `PLUGIN_PLA.jar` (`PLUGIN_OPTIMUS`, `PLUGIN_ORD.jar`, `PLUGIN_PLATAFORMA.jar`) las resuelve igual `CallServlet` (prueba primero `PLUGIN_OPTIMUS.jar` para cualquier clase), el jar se compila de este mismo arbol con `build.gradle` (`:pluginJar`); (c) lo desplegado en produccion puede diferir del arbol (pregunta Q-02).

## 4. Fichas por clase

Rutas legacy relativas a `legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/`. Ninguna de las 4 clases tiene regla en `BUSINESS_RULES.md` (las reglas del archivo cubren BcsPlataformaWeb, BcsFilters, BcsJobManager y seguridad de claves en `acc.*`): hay que **extraer reglas nuevas** (paso 1) con ids propios del modulo `PLA-ESM-nn`, `PLA-PAR-nn`, `PLA-FLC-nn`, `PLA-MAT-nn` (convencion de `TES-A-nn`/`CONT-A-nn`) y pedir a la persona duena de `BUSINESS_RULES.md` que les asigne `RULE-nnn`. Reglas existentes que si aplican: RULE-076 (sin `main()` ni credenciales; las 4 clases tienen `main()` con conexion a HANA), RULE-073/074 (`PLA.MuestraLog`, lo hace `BcsPlataformaWeb`, no estas clases) y, por el camino de entrada, RULE-004/005 (BcsFilters) y la identidad de sesion ARQ-H5 (ya cerrada en el router).

Complejidad: `lizard` NO esta instalado (`which lizard` falla). Las cifras de decisiones son un **conteo manual aproximado** (script de busqueda de `if|for|while|case|catch`, `&&`, `||` y `?:` sobre el codigo sin comentarios ni `main()`), no la CCN de lizard; el brief pide `lizard` para la cobertura de ramas del bloque 2 y conviene instalarlo (`pip install lizard`) antes del paso 6.

### 4.1 `cre.ConsultarEstadoModulo` (id 600017, 1.777.141 usos)

| Aspecto | Detalle |
|---|---|
| Fuente | `cre/ConsultarEstadoModulo.java` 124 LOC brutas (109 no vacias); sin SQL en archivo aparte (SQL inline); decisiones: 1 (`isEmpty()`, ambas ramas equivalentes) |
| Entrada JavaCall | `RutaServicio = PLUGIN_PLA.jar@cl.bcs.plataforma.cre.ConsultarEstadoModulo`, `Descripcion "Consultar el Estado de un modulo del sistema"`, `RegistraUso=N`, `ValidaAccesoServicio=N`. UI: `ModuloPlataforma.js:790` `POST /BSCL/Plataforma/GWJAVA/PLA/call?t=600017`; `baseapp.js:228-243` la llama con `$interval(..., 15000)` por sesion de navegador: de ahi los 1,77 M |
| Entrada | `{p_IdEmpresa: Long, p_IdModulo: String, p_IdUsuario: Long}` (`Gson` plano; `p_IdModulo` y `p_IdUsuario` se IGNORAN) |
| Salida | `{P_OUT_ADVERTENCIASCONTADOR:0, P_OUT_ERRORESCONTADOR:0, p_out_ListaModulos:[{IdEstadoModulo, NombreModulo}]}`; `p_out_AdvertenciasDetalle` y `p_out_ErroresDetalle` quedan null y **no se emiten** (`new Gson()` sin `serializeNulls`); orden de claves = orden de campos de la clase de salida |
| HANA | solo lectura: `SELECT IFNULL("IdEstado",'ABT') AS "IdEstadoModulo", IFNULL("CodigoModulo",'') AS "NombreModulo" FROM "BSCL.Plataforma.BD::PAU.Modulo" WHERE "IdEmpresa" = :IdEmpresa` (sin procedimientos, sin tablas de paso). Una consulta por llamada, sin ORDER BY (el orden lo decide HANA) |
| Colaboradores | ninguno (no llama `RevisarRequisitosBasicos`) |
| Reglas a extraer | PLA-ESM-01 estado por defecto `ABT` si `IdEstado` es null; PLA-ESM-02 `NombreModulo` es `CodigoModulo` (o `''`); PLA-ESM-03 filtra solo por empresa (modulo y usuario ignorados); PLA-ESM-04 empresa sin modulos o null -> lista vacia; PLA-ESM-05 solo lectura |
| Defectos a mantener | D-ESM-1 `p_IdModulo` y `p_IdUsuario` ignorados (el cliente envia `p_IdModulo=''` y recibe todos); D-ESM-2 alias `NombreModulo` = columna `CodigoModulo`; D-ESM-3 sin ORDER BY; D-ESM-4 `Modulo.IdEstadoModulo` solo `ABT` por defecto, sin validar dominio; D-ESM-5 si el SQL falla la excepcion se propaga (HTTP de error, no contador) |
| Seguridad | `main()` con host y usuario HANA en claro (RULE-076, no se copia). SQL parametrizado (sin concatenacion). Riesgo de exposicion: `RegistraUso=N` y el despachador moderno omite la validacion de sesion en ese caso (`BcsPlataformaWeb/.../SessionValidatorService.java:82`) con identidad 0/0: **verificar** si el legacy tambien exige sesion (en `CallServlet` el 401 y la sobreescritura de `p_IdEmpresa` dependen de la sesion) antes de enrutar (R-05) |
| Nuevo core | clase `cre.ConsultarEstadoModuloService` (tipada) + puerto `ModuloRepository` (1 metodo) + JDBC con el SELECT verbatim; salida con `serializeNulls=false` como legacy; sin cache (el legacy no la tiene; con 1,77 M de llamadas, medir latencia en el canario, pero NO agregar cache sin decision) |

### 4.2 `mer.CalcularParidad` (ids 3155 y 600056, 74.457 usos)

| Aspecto | Detalle |
|---|---|
| Fuente | `mer/CalcularParidad.java` 959 LOC brutas (815 no vacias), 6 SQL en `mer/sql/CalcularParidad_*.sql` usados + 4 SQL muertos (`CallSp`, `InsertEntrada`, `SelectSalida`, `SelectParametroFinanciero`: restos del procedimiento antiguo `MER.CalcularParidadYTipoCambio`, no se invocan); decisiones ~136 (escalera de 15 casos de conversion en `:536-650`) |
| Entrada | `RutaServicio` x2 (ids 3155 y 600056, ambos `RegistraUso=S`, `ValidaAcceso=N`). La referencian 105 archivos del legacy en 12 modulos (`grep` de `new CalcularParidad(` o `mer.CalcularParidad`; incluye imports de tipos anidados): Custodia 25, FondoMutuo 23, Normativa 17, Plataforma 12, CuentaInversion 7, Orden 7, Facturacion 6, Mesa 3, Contabilidad 2, APV 1, Portafolio 1, Tesoreria 1; los de modulos sin migrar la llaman **en proceso** con `new CalcularParidad().logica(con, entrada)` (quedan en TomEE hasta su bloque). En los modulos ya migrados la llaman por `/internal/java-call` con el FQN `cl.bcs.plataforma.mer.CalcularParidad` (`ColaboradoresJavaCall` de APV, `PlataformaPort` de Contabilidad, `domain/colaborador/CalcularParidad.java` de CuentaInversion): hoy esas llamadas caen a TomEE; al habilitar la clase pasan al nuevo modulo sin tocar a los llamadores |
| Entrada JSON | `{p_IdEmpresa, p_IdTrabajo, p_CalculosEntrada:[{IdTrabajo, IdInterno, Fecha:"yyyy-MM-dd[...]", IdMonedaOrigen, IdMonedaDestino, ValorCambioLocalModificado (def. 0), MontoOrigen, IdUso (def. "")}]}` con `Utils.getGsonBuilder()` (formato de fecha `yyyy-MM-dd HH:mm:ss.SSS`, deserializadores de `java.sql.Date/Timestamp`) |
| Salida JSON | `{p_out_CalculosSalida:[28 campos: IdTrabajo, IdInterno, Fecha, IdMonedaOrigen/Destino, ValorCambioLocalModificado, MontoOrigen, IdUso, FactorOrigenDestino, MontoDestino, FactorOrigenLocal, MontoLocal, Factor/Fecha/IdTipoRegistro de OrigenCambio, CambioLocal, LocalCambio, CambioDestino, Resultado, CodigoMensaje, ValorParametroFinanciero, FechaParametroFinanciero], P_OUT_ERRORESCONTADOR:0}` con `serializeNulls`; los `BigDecimal` salen con la escala que resulte del calculo |
| HANA | solo lectura, 6 SELECT: `MER.DefinicionParametroFinanciero`, `MER.ParametroFinanciero`, `MER.UsoParidadParametroFinanciero`, `MER.UsoParidadYTipoCambio`; mas configuracion `PLATAFORMA/PLA.MonedaSistema` y `PLA.DiasFechaMinimaCalculoParidad` (esta con valor por defecto 1095 si falta) via `ConfiguracionSistema` (recarga TODA la configuracion del modulo en cada llamada: el cache esta apagado, `if (true)`). Sin procedimientos ni tablas de paso |
| Colaboradores | `ConfiguracionSistema` (lee `ConsultarConfiguracionPlataforma.sql`); ningun otro modulo |
| Reglas a extraer | PLA-PAR-01 moneda origen = destino = moneda sistema -> factores 1, OK; PLA-PAR-02 pivote USD ("DO" como parametro del dolar) ; PLA-PAR-03 ventana de fechas `[fechaMinima - diasDiferencia, fechaMaxima]`; PLA-PAR-04 factor = ultimo con `Fecha <= fecha` (lista ordenada por fecha descendente); PLA-PAR-05 inversos con `1/Valor` DECIMAL128; PLA-PAR-06 `ValorCambioLocalModificado <> 0` anula el pivote; PLA-PAR-07 pivote por uso vs pivote por defecto (`uso == null || Cantidad == 0`); PLA-PAR-08..PLA-PAR-21 las 14 ramas de conversion de `:558-650` (CLP-PEN, USD-PEN, COP-PEN, CLP-USD, USD-CLP, EUR-CLP, CLP-UF/UF-CLP, USD-UF, UTM-USD, UF-EUR, UF-UTM, PEN-UF, misma moneda); PLA-PAR-22 `RECHAZO` con `ERROR_CALCULAR_PARIDAD_MONTO_DESTINO` / `_MONTO_LOCAL` si el monto es null; PLA-PAR-23 parametro financiero informativo (UF, UTM) |
| Defectos a mantener | D-PAR-1 `FactoresEncontrados` es un `HashMap` de instancia mutado desde `parallelStream` (condicion de carrera; ademas se limpia con `.clear()` al final; en TomEE hay una instancia por llamada, en Spring un singleton compartiria estado: DEV obligatoria, estado local por corrida, como DEV-10 de Contabilidad); D-PAR-2 `calc.IdMonedaOrigen/Destino/MontoOrigen` se mutan en la entrada y se restauran al final (si hay excepcion quedan mutados); D-PAR-3 NPE en cadena si falta un factor (`MontoDestino = ...multiply(FactorOrigenDestino)` con factor null en UF/UTM, `:620,:649`) y aborta TODA la llamada en vez de marcar RECHAZO; D-PAR-4 `MontoLocal` de la rama UF/UTM sin `MathContext` (`:541`) mientras otras usan DECIMAL128 (las escalas de los goldens dependen de eso); D-PAR-5 (a confirmar por el experto) en `:452` la conversion previa de UF/UTM a su moneda de valor compara la moneda DESTINO con `IdMonedaValor`; si el destino ya es CLP no convierte el monto origen y la rama posterior usa `montoOrigenOriginal`; D-PAR-6 `SelectParametrosFinancieros` calcula `1/PAR."Valor"` en SQL y Java no lo usa; D-PAR-7 `IdUso` null se trata como `""`; D-PAR-8 listas vacias en `IN (:x)` (`monedas` vacia) dependen de `SQL.query` (a caracterizar); D-PAR-9 `Fecha.toDate(calc.Fecha)` antes de recortar la fecha a 10 caracteres; D-PAR-10 orden de salida = orden de entrada (la clase usa `parallelStream().map().collect()`, que conserva orden) |
| Seguridad | SQL con parametros nombrados (sin concatenacion); `main()` con conexion `qa02` por archivo de propiedades (RULE-076) |
| Nuevo core | `mer.CalcularParidadService` con `record`s tipados (BigDecimal, LocalDate) y adaptador JavaCall que traduce el JSON legacy; puerto `ParidadRepository` (6 SELECT verbatim), puerto `ConfiguracionSistemaPort`; calculo en una clase de dominio pura (`ConversionParidad`) sin I/O, probada con y sin BD; flujo secuencial (mismo resultado que `parallelStream` porque el legacy conserva el orden) |

### 4.3 `pla.ConsultarFlujoCaja` (id 1673, 52.679 usos) y su motor `pla.SaldoFlujoCaja`

| Aspecto | Detalle |
|---|---|
| Fuente | `pla/ConsultarFlujoCaja.java` 338 LOC brutas (decisiones ~25) + `pla/SaldoFlujoCaja.java` 891 (decisiones ~167, ~21 variantes de SQL elegidas por `ItemOrigen` en `:821-865`, `parallelStream`) + `pla/RevisarRequisitosBasicos.java` 151 (decisiones ~13) + 5 SQL propios `ConsultarFlujoCaja_*.sql` y 26 `SaldoFlujoCaja_*.sql`. `ConsultarFlujoCajaCliente` (id 101010131, 0 usos) queda fuera |
| Entrada JavaCall | `PLUGIN_PLA.jar@cl.bcs.plataforma.pla.ConsultarFlujoCaja`, `RegistraUso=S`. Tambien se llama en proceso desde APV Jobs, Custodia (`CerrarEventoCapital*`), Mesa (`ConsultarSaldosOperacion`), Normativa, SocioNegocio y Tesoreria (migrados: por `/internal/java-call`) |
| Entrada | `{p_IdEmpresa, p_IdUsuario, p_IdModulo, p_UsaReglasNegocio, p_FechaDesde, p_FechaHasta "yyyy-MM-dd", p_Uso, p_DesplegarEnUI, p_AgruparPorOrigen, p_PortafolioAConsultar:[{IdPortafolio, IdMoneda}], p_ClienteAConsultar:[{IdCliente, IdMoneda}]}` (`getGsonBuilder()` del `JavaCall`) |
| Salida | `{P_OUT_ERRORESCONTADOR, P_OUT_FLUJOCAJA:[{NombreUso, FechaSaldo, IdPortafolio, IdMoneda, Saldo}], P_OUT_FLUJOCAJADETALLADO:[{FechaSaldo, IdPortafolio, IdMoneda, IdUsoFlujoCaja, NombreUso, IdConfiguracionFlujoCaja, ItemCalculo, Monto}], P_OUT_MENSAJES:[{id, tipo, codigo, descripcion}]}` con `serializeNulls` |
| HANA (lectura) | Vistas y tablas de 8 dominios: `PLA.ConsultarConfiguracionFlujoCaja` (vista), `POR.Portafolio`, `CLI.Cliente`, `PER.Persona`, `PER.DocumentoIdentidad`, `COM.ConsultarCategoriaInversionista` (vista), `PLA.Moneda`, `CTA.CuentaInversion`, `CTA.CargoAbonoMovimiento`, `CUS.Cartera` y vistas `CUS.ConsultarCarteraFlujoCaja`/`ConsultarEventoCapitalClienteFlujoCaja`, `FAC.MovimientoComprobante` y su vista, `FMM.*` (4), `MES.*`/`SPT.OperacionSpot` y vistas de Mesa, 3 vistas `Flujos.*` de Orden, `Tesoreria...ConsultarPorComprobanteOrigenLiquidado`, `LIM.Limite`, `IND.ConsultarIndicadoresPortafolio` (lista completa en `grep` de `SaldoFlujoCaja_*.sql`) |
| HANA (escritura, existente) | **No es 100 % solo lectura:** `INSERT`/`DELETE` en dos tablas de paso, `"BSCL.Plataforma.BD::PLA.ConsultarFlujoCaja_Portafolio_Proceso"` (`ConsultarFlujoCaja.java:39,263,307`) y `"BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso"` (`SaldoFlujoCaja_Insert/DeletePortafoliosProcesoTablaPaso.sql`). Sus DDL no estan en el repo (no hay `hdbtable`); por el prefijo `GBL.`/`_Proceso`/`_Paso` y por ARQ-H2 de Contabilidad se asume que son tablas **temporales globales de HANA** (datos por sesion): hay que usar UNA conexion por llamada (misma sesion) y rollback al fallar. Es una suposicion a confirmar con un `SELECT` de catalogo (Q-04). No se crea ni altera ninguna tabla |
| Colaboradores | **En proceso, misma conexion:** `RevisarRequisitosBasicos` (plataforma), `SaldoFlujoCaja` (plataforma), `ConfiguracionSistema` (`PLA.TipoCalculoFlujoCaja`, `PLA.TipoCalculoFlujoCajaSpot`), `Diccionario`/`MensajeSalida`; y **dos clases de modulos YA migrados** (que se ejecutan en otros servicios): `cl.bcs.cuentaInversion.SaldoCuentaInversion` (registro id 4290647, 0 usos, existe en `BcsCuentaInversionServicios`) y `cl.bcs.tesoreria.SaldoPendienteDeLiquidar` (**no figura en `PLA.Servicio`**; existe `javacall/SaldoPendienteDeLiquidarService` en `BcsTesoreriaServicios`). Ciclo: `SaldoCuentaInversion` llama a `CalcularParidad` (plataforma) |
| Reglas a extraer | PLA-FLC-01 requisitos basicos (empresa/socio/cliente, usuario ACT, modulo existe) cortan con contador=1; PLA-FLC-02 `FECHA_NO_VALIDA` por `0001-01-01` en desde/hasta o desde>hasta (suma por error); PLA-FLC-03 `USO_FLUJO_CAJA_NO_EXISTE`; PLA-FLC-04 clientes se expanden a portafolios (moneda del cliente); PLA-FLC-05 deduplicacion portafolio/moneda; PLA-FLC-06 `CUENTA_INVERSION` usa la consulta por cuenta de inversion; PLA-FLC-07 filtro de moneda vacio (`FiltroMonedaValido=1`); PLA-FLC-08 sin moneda en la peticion = todos los portafolios; PLA-FLC-09.. saldo por item (~21 `ItemOrigen`), agrupar por origen, `P_DESPLEGARENUI`, tipo de calculo (configuracion) |
| Defectos a mantener | D-FLC-1 `P_OUT_ERRORESCONTADOR = 1` fijo si falla `RevisarRequisitosBasicos` (aunque haya varios errores) pero `+= 1` por cada error de fecha; D-FLC-2 `MensajeSalida.getNumeroErrores` compara `tipo == MENSAJE_ERROR` por referencia (`PlataformaUtils.java:636`): funciona solo con la constante interna, falla con mensajes deserializados (no portar con `equals` sin documentarlo); D-FLC-3 las salidas tempranas no ejecutan el DELETE de la tabla de paso (confia en el rollback de `CallServlet`; con un llamador en proceso sin rollback quedarian filas); D-FLC-4 `Fecha.toDate` se evalua antes de validar (nulo -> comportamiento a caracterizar); D-FLC-5 se insertan en la tabla de paso los portafolios SIN deduplicar; D-FLC-6 `p_UsaReglasNegocio` se recibe y no se usa; D-FLC-7 `clienteAConsultarHash.get(...)` de moneda: cliente repetido con monedas distintas toma la primera; D-FLC-8 los errores de `SaldoFlujoCaja` se convierten en mensajes con descripcion del diccionario sin valores; D-FLC-9 `SaldoFlujoCaja:347` filtra con `parallelStream().filter(por -> idPortafolios.add(...))` (efecto lateral sobre un conjunto en un flujo paralelo) y usa `HashMap` (el orden de salida puede depender del hash; se fija por golden y se reproduce; si el golden resulta inestable entre corridas del legacy se documenta como no determinista) |
| Seguridad | SQL con parametros nombrados; sin concatenacion en las 4 clases del alcance (verificado en estas clases; `SaldoFlujoCaja` y `Saldo*` por revisar en el paso 1). `main()` con `getDESAConnection()` (RULE-076) |
| Nuevo core | `pla.ConsultarFlujoCajaService` + `pla.SaldoFlujoCajaService` + `pla.RevisarRequisitosBasicosService`; puertos `ColaboradoresPort` (CuentaInversion y Tesoreria **por HTTP al router `/internal/java-call`**, JSON legacy; sin fallback a TomEE para esas dos clases porque ya no estan alla), `ConfiguracionSistemaPort`, `DiccionarioMensajesPort`; unidad de trabajo por llamada (una conexion, commit solo si contador=0, rollback en otro caso: DEV-21 de Contabilidad) para que las tablas de paso vivan en la sesion que las escribe. Decision D-3 de la seccion 10: este servicio es el mas riesgoso de la ola |

### 4.4 `com.ConsultarMatrizOperacional` (id 20052, 4.298 usos)

| Aspecto | Detalle |
|---|---|
| Fuente | `com/ConsultarMatrizOperacional.java` 518 LOC (decisiones ~39) + `ConsultarProductosPlantilla.java` 136 (~2) + `ConsultarCobrosPlantilla.java` 414 (~63) + `ConsultarCobrosPlantillaPeru.java` 429 (~58) + 6 SQL (`ConsultarMatrizOperacional_*` x4, `ConsultarProductosPlantilla_ConsultarDetallePerfilProducto`, `ConsultarCobrosPlantilla[Peru]_ConsultarCobros`). Variantes `...Excel` (16 usos), `...PortafolioExcel` (27) y `...Portafolios` quedan en la ola B |
| Entrada | `{p_IdEmpresa, p_IdUsuario, p_IdModulo, p_IdTipoEntidad (def. "PORTAFOLIO"; PORTAFOLIO/MODELO/PREVISUALIZAR), p_IdEntidad, p_IdPlantillaCobros, p_IdPlantillaProductos}` |
| Salida | `{P_OUT_ERRORESCONTADOR, P_OUT_MENSAJES:[], P_OUT_MATRIZOPERACIONAL:[~85 campos por linea (bolsa, producto, operacion, canal, cobro, montos fijos/porcentuales/rangos/tasa-plazo, banderas Otros_*, ValorCobroCombinado, IdMonedaCobroUI, UsoMonedaFormato, clasificacion de instrumento)]}` |
| HANA | solo lectura: `INS.Bolsa/TipoOperacion/TipoProducto/ClasificacionTipoInstrumento/TipoInstrumento/TipoOperacionTipoInstrumento`, `PLA.TipoCanalIngreso`, `COM.PerfilComercialBase`, `COM.PerfilProductos/DetallePerfilProductos`, `COM.PerfilCobros/DetallePerfilCobros/Cobro/CobroClasificacion/ValorCobro`, `COM_2.*` (Peru), `POR.Portafolio`; mas `PLA.LocalizacionSistema` y `PLA.MonedaSistema` de la configuracion |
| Colaboradores | en proceso: `ConsultarProductosPlantilla` (que llama `RevisarRequisitosBasicos`), `ConsultarCobrosPlantilla` (CL) o `ConsultarCobrosPlantillaPeru` (PE), `ConfiguracionSistema`, `Utils.copyPojo` (ya en `BcsUtils` moderno) |
| Reglas a extraer | PLA-MAT-01 localizacion CL/PE elige perfil de cobros (otra localizacion: matriz vacia); PLA-MAT-02 tipo de entidad PORTAFOLIO/PREVISUALIZAR/otro=MODELO; PLA-MAT-03 filtro por plantilla de productos con comodin `"0"` y estado `ACT`; PLA-MAT-04 lineas sin cobros salen como `SIN COBROS DEFINIDOS`; PLA-MAT-05 Peru: `ValorCobroCombinado`/`UsoMonedaFormato` por tipo de cobro; PLA-MAT-06 clasificacion de instrumento por tipo de operacion (gana la ultima coincidencia) |
| Defectos a mantener | D-MAT-1 `throw new Exception("NO SE ENCONTRO PORTAFOLIO")` para portafolio y modelo inexistentes (excepcion, no contador); D-MAT-2 `productos == null` (plantilla sin perfiles, p. ej. PREVISUALIZAR vacio) -> NPE en el `for` de filtrado; D-MAT-3 `p_IdTipoEntidad` null -> NPE; D-MAT-4 cualquier valor distinto de PORTAFOLIO/PREVISUALIZAR se trata como MODELO; D-MAT-5 `P_OUT_MENSAJES` nunca se llena (el contador siempre es 0); D-MAT-6 `ConfiguracionSistema` falla si falta `PLA.LocalizacionSistema`; D-MAT-7 `IdPerfilCobrosExtra`/`ProductosExtra` solo se usan en PORTAFOLIO/MODELO; D-MAT-8 cruce O(lineas x cobros) sin indice (rendimiento, no se cambia); D-MAT-9 `lx.IdTipoOperacion.equals(...)` con operacion null -> NPE |
| Seguridad | SQL con parametros nombrados; `main()` con `getDESAConnection()` (RULE-076) |
| Nuevo core | `com.ConsultarMatrizOperacionalService` + `ConsultarProductosPlantillaService` + `ConsultarCobrosPlantillaService`/`...PeruService`; puertos de lectura de COM/INS verbatim; las mismas piezas (`RevisarRequisitosBasicos`, `ConfiguracionSistema`) del kit. Es el servicio de menor riesgo despues de `ConsultarEstadoModulo` |

### 4.5 Kit de plataforma compartido (piezas del cierre, prerrequisito de 4.2 a 4.4)

`pla.utils.ConfiguracionSistema` (94 LOC; 14 SQL `ConsultarConfiguracion<Modulo>.sql`; recarga el modulo completo en cada llamada, sin cache, lanza `Exception` si falta el parametro o el modulo), `pla.utils.Diccionario` (148; `getMensaje(con, empresa, codigo[, valores])`, consulta el diccionario y sustituye valores: cierra DEV-18 de Contabilidad), `PlataformaUtils.MensajeSalida` (constantes `ERROR`, `ADVERTENCIA`, `AUTORIZACION`, `INFORMACION`, `getNumeroErrores`), `RevisarRequisitosBasicos` (3 SQL), ayudante de SQL con parametros nombrados y listas `IN (:x)` (en el legacy es `cl.bcs.utils.SQL`; el moderno no lo trae). Se construye una sola vez, con tests propios, y despues podran adoptarlo Contabilidad/Tesoreria/APV/CuentaInversion (tarea posterior, no de este bloque).

## 5. Como lo serviria el nuevo core

### 5.1 Estructura del modulo (mismo patron que Contabilidad/Tesoreria, sin tocar las otras 326 clases)

```
cl.bcs.plataforma
  adapter/in/javacall/   InternalJavaCallController (reescrito: 400 JSON malo, 503 clase no servida, 500 generico con correlationId),
                         JavaCallDispatcher (mapa FQN -> JavaCallHandler; SOLO las clases de la lista blanca), TokenInternoFilter, JavaCallUnitOfWorkFilter
  adapter/in/rest/       (opcional, decision D-2) controladores REST tipados
  adapter/out/jdbc/      SqlNombrado (parametros nombrados + IN (:x)), ConfiguracionSistemaJdbc, DiccionarioMensajesJdbc, <Servicio>Jdbc con SQL verbatim
  adapter/out/http/      ColaboradoresRouterClient (POST {JAVACALL_URL}/internal/java-call, X-Internal-Token, timeouts)
  port/out/              ConfiguracionSistemaPort, DiccionarioMensajesPort, ColaboradoresPort
  cre/ mer/ pla/ com/    un <Nombre>Service por clase legacy (mismo nombre, trazabilidad por LOC) con records tipados; dominio puro (ConversionParidad)
  kit/                   MensajeSalida (constantes y getNumeroErrores), JsonLegacy (Gson con y sin serializeNulls)
```
Puertos solo donde hay una frontera real (configuracion, diccionario, colaboradores, reloj si se usa); el acceso a SQL va por el ayudante con SQL verbatim: es la leccion ARQ-M6 de Contabilidad (los puertos de un solo implementador con nombres de SQL son ruido). Servicios sin estado compartido (todo el estado de una llamada es local; ver D-PAR-1). SQL en `src/main/resources/cl/bcs/plataforma/<pkg>/sql/` (ya existen, byte a byte iguales al legacy; la carga falla si falta un recurso, como `SqlResources` de Contabilidad). Charset de lectura explicito UTF-8.

**Paso 0 (higiene del modulo):** (1) sacar del contexto de Spring los 326 stubs (`@ConditionalOnProperty` por defecto apagado o mover a una carpeta no compilada; borrarlos queda para la ola B), (2) el despachador atiende solo la lista blanca (hoy 4 clases; cualquier otra: 503 y el router cae a TomEE), (3) `TokenInternoFilter` fail-closed con lista blanca de rutas publicas (`/actuator/health`), normalizando `//`, `;x=1`, `%xx`, `.`/`..` (H3 de Tesoreria; el mismo defecto existio en CuentaInversion), (4) `JavaCallUnitOfWorkFilter` (una conexion por llamada, commit solo con 2xx y contador de errores 0, rollback en otro caso, `NESTED` para savepoints), (5) `server.shutdown=graceful`, usuario no root, HEALTHCHECK, `-Dfile.encoding=UTF-8 -Duser.timezone -Duser.language=en`, sin `spring.batch.*`, (6) los 46 tests existentes se conservan si pasan (ConvertirMontoEscrito), sin contar como evidencia de las 4 clases.

### 5.2 Entrada JavaCall en proceso y typed REST

- **Entrada JavaCall:** `POST /internal/java-call` con `{"class":"cl.bcs.plataforma.cre.ConsultarEstadoModulo","params":{...}}` y respuesta con el JSON legacy sin alterar (mismos nombres `p_*`/`P_OUT_*`, mismas claves omitidas cuando son null, mismo `Content-Type`). Es lo que ya hablan `ColaboradoresJavaCall` (APV, Tesoreria, CuentaInversion) y el router.
- **Typed REST:** recomendado dejarlo para una segunda pasada, como Tesoreria ("primero probar equivalencia sobre el contrato JSON actual, despues refactor"): `POST /api/plataforma/paridad/calcular` (record `CalcularParidadEntrada`/`Salida`) seria el candidato natural porque lo usan 12 modulos y un cliente tipado evita el JSON; el resto no tiene llamadores fuera de la UI. Decision D-2.
- **En proceso para otros modulos:** los servicios se escriben sin dependencias de Spring en la logica (constructor con puertos) para poder empaquetar `CalcularParidad` como libreria cuando se migren Custodia/FondoMutuo/Orden (linea 1 de la decision H-1 de `JOBS_PATTERN.md`); no se crea la libreria en este bloque.

### 5.3 Cambios en el router (`BcsPlataformaWeb`, solo configuracion; el codigo del router no se toca en este bloque)

1. Agregar a `plataforma.javacall.destinos` (`PLATAFORMA_JAVACALL_DESTINOS`) la entrada `cl.bcs.plataforma.=JAVACALL@http://bcs-plataforma-servicios:8114` (estrategia `JAVACALL`: su `/internal/java-call` existente). Los destinos por defecto hoy son los de APV, Tesoreria, CuentaInversion y Contabilidad: agregar la fila al `ContratoDestinosTest` y al mapa del README.
2. `plataforma.javacall.clases-habilitadas`: habilitar de a una clase y solo cuando esa clase este PROVEN: primero `cl.bcs.plataforma.mer.CalcularParidad` (sin dependencias), luego `cre.ConsultarEstadoModulo`, `com.ConsultarMatrizOperacional` y por ultimo `pla.ConsultarFlujoCaja`. Vacio = todo a TomEE (estado inicial y de rollback inmediato).
3. 503 del modulo o conexion rechazada -> el router reintenta en TomEE (`{TOMEE_URL}/call?t={IdServicio}`); necesita el id: cargar el export del registro (`plataforma.javacall.registro-servicios`); `CalcularParidad` tiene 2 ids (ambiguo = sin ruta): pasar `idServicio` en el cuerpo o desambiguar por configuracion (hallazgo del router, Q-06).
4. Quien llama hoy: (a) servicios y jobs migrados (APV, Tesoreria, CuentaInversion, Contabilidad) -> `/internal/java-call` del router: cambian solo con la configuracion; (b) la UI (`/call?t=600017`) -> `BcsPlataformaWeb /call`, que hoy es un despachador cuyo cutover es el bloque 9 (R-01): `ConsultarEstadoModulo` seguira atendida por TomEE en produccion hasta entonces, aunque el modulo este PROVEN. (c) los modulos legacy sin migrar la llaman en proceso dentro de TomEE y no cambian.

### 5.4 Que debe seguir por HTTP o en TomEE

| Que | Como |
|---|---|
| Clases de plataforma fuera de la lista blanca | 503 -> fallback a TomEE (comportamiento actual) |
| `SaldoCuentaInversion` y `SaldoPendienteDeLiquidar` desde `ConsultarFlujoCaja` | **HTTP al router** (`/internal/java-call`, JSON legacy), destino CuentaInversion/Tesoreria (ya migrados, no TomEE). Perdida respecto del legacy: ya no comparten conexion ni transaccion con el llamador (son lecturas; sus tablas de paso viven en su propia sesion). Si el router responde 503/conexion rechazada, el servicio hace rollback y responde 503 (el router cae a TomEE, que ejecuta todo el flujo en proceso) |
| Consumidores en proceso (Custodia, FondoMutuo, Normativa, Orden, Mesa, Portafolio, Facturacion, SocioNegocio, Plataforma) | siguen con su copia legacy en TomEE hasta su bloque; sin cambios |
| Sesion de usuario e identidad (`p_IdEmpresa`, `p_IdUsuario`) | las impone el router (`JavaCallRouterService`, ARQ-H5 cerrado) en `/call`; en `/internal/java-call` no hay sesion: confiar en el llamador de confianza (token interno) |

## 6. Como se prueba la equivalencia sin el legacy en ejecucion

**El legacy no puede correr aqui en su entorno real:** no hay TomEE, `PLUGIN_OPTIMUS.jar` no esta desplegado y no se llama a HANA (regla 2). Compila: el smoke test del preflight construyo `:pluginJar` completo con JDK 11 y Gradle 6.3 (con red para Maven Central); el arbol actual de trabajo trae un parche de seguridad a medio aplicar (`BCrypt`), asi que se compila **solo lo necesario** en un directorio temporal, como Tesoreria y Contabilidad. Lo que se ejecuta son las **clases legacy sin modificar** sobre **H2 en memoria** con shims minimos.

| Elemento | Como (patron probado en Tesoreria/Contabilidad/APV/CuentaInversion) |
|---|---|
| Oraculo | `legacy-oracle/` por modulo: `record.sh` (compila a un `ORACLE_WORK` temporal con `javac`, JDK 25; jars de `~/.m2`: h2, gson, commons-collections), `runner/OracleRunner.java`, `shims/` (`datadog.trace.api.Trace`, log4j minimo, funciones HANA que falten en H2 como `CURRENT_UTCTIMESTAMP`, `FROM DUMMY`). Se usan **tal cual** (fuente real) `BcsUtils` legacy (`SQL`, `Utils`, `Fecha`, `JavaCall`, `Timer`), `ConfiguracionSistema`, `Diccionario`, `PlataformaUtils.MensajeSalida`, `RevisarRequisitosBasicos`, `SaldoFlujoCaja` y las clases de cobros/productos. NO usar los "oraculos falsos" de APV (`shims/cl/bcs/plataforma/mer/CalcularParidad.java` es una reproduccion para tests de APV, no el original) |
| Esquema H2 | derivado de los SQL legacy (`MODE=LEGACY`); las vistas HANA (`PLA.ConsultarConfiguracionFlujoCaja`, `COM.ConsultarCategoriaInversionista`, `Flujos.*`, etc.) se modelan como tablas sembradas; tablas de paso como tablas normales. Es un **supuesto documentado** (HANA nunca se ejercita) |
| Goldens | casos `PLA-ESM-nn`, `PLA-PAR-nn`, `PLA-FLC-nn`, `PLA-MAT-nn` en `src/test/resources/plataforma-a/cases/<id>_<slug>/{case.json,seed.sql,expected.json}`; `expected.json` se **graba ejecutando el legacy** (nunca a mano) y se revisa; reloj fijo, zona UTC |
| Volcado y comparacion | `-Dequivalence.out=<dir>` (clase `VolcadoEquivalencia` copiada por modulo) escribe `legacy/` y `new/` por caso; `analysis/.../equivalence/BcsPlataformaServicios/{cases.json,EQUIVALENCE.json,compare.py}`; autoprueba del comparador (cada salida legacy se compara con una copia cambiada en un byte y debe salir "distinta") |
| Entradas frescas | un generador independiente (`make_fresh.py`, semilla fija, no mira los casos de desarrollo) produce entradas nuevas que se ejecutan en ambos lados (`FRESH_EQUIVALENCE.json`, `fresh-legacy-oracle.output.txt`, `FreshVerifyDriverTest`), como Contabilidad/Tesoreria: evita que el nuevo codigo se ajuste a los casos que vio |
| Mutacion de goldens | `LEGACY_OVERLAY` + `ORACLE_OUT`: se muta una copia del legacy, se regraba y debe cambiar el golden (prueba que el caso fija ese comportamiento) |
| Canario | 8 mutaciones del codigo nuevo en una copia temporal, cada una con build limpio (evidencia en `equivalence/canary/BcsPlataformaServicios-transform/<n>-<area>/`); minimo: `CalcularParidad` (rama CLP-USD, `MathContext`, orden por fecha, `Fecha <= fecha`), `ConsultarFlujoCaja` (`FECHA_NO_VALIDA`, `desde>hasta`, agrupar por origen), `ConsultarEstadoModulo` (`IFNULL ... 'ABT'`), `ConsultarMatrizOperacional` (comodin `"0"`, `SIN COBROS DEFINIDOS`) y el filtro de token interno |

Particularidades a vigilar en la grabacion del oraculo:
- **`parallelStream` y `HashMap` compartido** en `CalcularParidad` y `SaldoFlujoCaja`: el oraculo se corre N veces (p. ej. 20) por caso con lotes grandes y se compara contra si mismo; si hay diferencias entre corridas el caso se marca no determinista y se excluye con motivo escrito (no se "arregla" el legacy).
- **Escala de `BigDecimal`:** la comparacion es por texto (la escala cuenta); el tipo de columna en H2 debe elegirse con la precision/escala que tenga HANA o los goldens fijaran una escala distinta (se documenta como supuesto; si se puede, confirmar con un SELECT de catalogo `TABLE_COLUMNS`, Q-03).
- **Fechas:** `java.sql.Date` y `Timestamp` en JSON con `yyyy-MM-dd HH:mm:ss.SSS` y zona fija; `Fecha.toDate`.
- **Consultas sin `ORDER BY`** (`ConsultarEstadoModulo`, listas de la matriz): el orden lo decide HANA; H2 devuelve orden de insercion. Se siembra un orden unico y se anota como limite.
- **Carga de configuracion** con el cache apagado (una lectura por parametro): reproducir el numero de lecturas no se prueba, solo el valor.

**Que NO prueba:** HANA (dialecto, tipos DECIMAL, vistas reales, tablas temporales globales, orden de filas), la carga real (1,77 M de llamadas), TomEE (`PluginLoader`, `CallServlet`), ni que el jar desplegado coincida con el arbol (Q-02). Todo es "equivalencia sobre H2 y trazas". `modernize-verify` hace despues la verificacion independiente.

## 7. Estrategia de pruebas

| Capa | Que se prueba | Nombre / evidencia |
|---|---|---|
| Caracterizacion contra el oraculo | cada regla `PLA-xxx-nn` con al menos un caso; cada defecto `D-xxx-n` con un caso que lo fija; ramas de la escalera de `CalcularParidad` (14), validaciones de `ConsultarFlujoCaja`, 3 tipos de entidad x CL/PE de la matriz | un `@Test` por caso con el id en el nombre (`plaPar07_pivotePorUsoSinParametros...`) y comentario con `archivo:linea` legacy; el id de regla se cita en el mismo nombre. Cada clase de test imprime `equivalence cases executed (Servicio): N` y exige N > 0; un test de inventario verifica que cada carpeta de caso la ejecuta exactamente un test (o esta en `pending-cases.txt` con motivo) |
| Reglas de proyecto | RULE-076 (ningun `main()` ni credencial/JDBC literal en `src/main`; escaneo de fuentes) y regla de ninguna operacion de esquema (un test que falla si algun SQL de `src/main/resources` contiene `CREATE`, `ALTER`, `DROP`, `TRUNCATE`, `GRANT`, `CALL`; los unicos `INSERT`/`DELETE` permitidos son los dos de las tablas de paso, en lista blanca) | `Rule076SinCredencialesNiMainTest`, `SinCambiosDeEsquemaTest` |
| Seguridad del punto de entrada | token interno: sin token, mal formado, `//internal/java-call`, `/internal;x=1/java-call`, `%2e`, mayusculas, secreto vacio = rechaza todo; 500 sin eco de la excepcion; clase fuera de la lista blanca = 503 (no 200 de stub) | `TokenInternoFilterTest`, `JavaCallDispatcherTest` |
| Transaccion | contador > 0 o 5xx -> rollback (tablas de paso vacias); 2xx con 0 -> commit; `NESTED`; una conexion por llamada (afirmar que la tabla de paso escrita se ve en la lectura siguiente de la misma llamada) | `JavaCallUnitOfWorkFilterTest` + caso `PLA-FLC` con tabla de paso |
| Cableado real | `@SpringBootTest` que resuelve las 4 rutas con el contexto real y contra H2, y verifica que otras clases devuelven 503 (ARQ-M5 de Contabilidad: ningun test levantaba el contexto) | `PlataformaCableadoRealTest` |
| Colaboradores | `ColaboradoresPort` con servidor simulado: 200, 503, conexion rechazada, 500, timeout; el JSON enviado a `SaldoCuentaInversion`/`SaldoPendienteDeLiquidar` coincide con el grabado del legacy (los POJO de entrada se copian verbatim); test que recorre todos los `invocar(...)` literales del codigo | `ColaboradoresEnrutamientoTest` |
| Router | fila nueva en `ContratoDestinosTest` (las 4 clases resuelven al destino de plataforma y las demas a TomEE); prueba de `clases-habilitadas` vacia | `BcsPlataformaWeb` (cambio de prueba + configuracion) |
| Equivalencia dual | volcado `-Dequivalence.out`, `compare.py` con autoprueba, entradas frescas | `equivalence/BcsPlataformaServicios/` |
| Canario | 8 mutaciones en copia temporal, cada una debe romper al menos 1 test; las que sobrevivan se registran como hallazgo, no como evidencia | `equivalence/canary/BcsPlataformaServicios-transform/` |
| Cobertura y complejidad | JaCoCo de ramas por clase en alcance (meta propuesta: >= 80 %, D-6) y `lizard` (hay que instalarlo) para la CCN real, que reemplaza el conteo manual de este plan | informe en `TRANSFORMATION_NOTES.md` |
| Rendimiento (canario de carga, sin HANA) | `ConsultarEstadoModulo` con 15 s por sesion x N sesiones simuladas contra H2: latencia y asignaciones por llamada (no hay cache); `CalcularParidad` con 1.000 calculos por llamada | opcional, informativo; la prueba real exige HANA |

Comando de referencia (patron actual): `mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :plataforma-service clean test` con JDK 25 y una sola JVM `-Duser.timezone=UTC`; volcado: `-Dequivalence.out=<dir>`; regrabacion: `legacy-oracle/record.sh [prefijo]` (solo si cambia una fuente legacy o un caso).

## 8. Pasos de trabajo (agentes; maximo 2 en paralelo)

Cada paso termina con tests en verde, notas en `TRANSFORMATION_NOTES.md` (mapa legacy -> nuevo con LOC, desviaciones DEV-nn, defectos SME-nn, preguntas Q-nn) y un commit pequeno **sin `Co-Authored-By`**. Ningun paso toca `legacy/`, HANA ni el esquema.

| Paso | Que | Agente | Depende de | Salida / criterio de cierre |
|---|---|---|---|---|
| 0 | Aprobacion del usuario: corte (D-1), politica de estado actual (D-4: dejar fuera de contexto los 326 stubs), D-2, D-3, D-5, D-6 y D-7, y las preguntas Q-03 y Q-09 | usuario | - | decisiones escritas en este archivo (seccion 10) |
| 1A | **Extraccion de reglas** de las 4 clases + cierre (`/code-modernization:modernize-extract-rules`, agente `business-rules-extractor`): cartas Given/When/Then con `archivo:linea` para `PLA-ESM/PAR/FLC/MAT`; lista de defectos D-xxx; revision de `SaldoFlujoCaja` completo (20 SQL, 168 decisiones, seguridad) y de `Diccionario`/`ConfiguracionSistema`; instalar `lizard` y medir CCN real | agente 1 | 0 | `analysis/.../RULES_PLATAFORMA_OLA_A.md` (o seccion nueva de `BUSINESS_RULES.md` con ids pendientes de asignar) |
| 1B | **Infraestructura del oraculo** (`test-engineer`): `legacy-oracle/` (`record.sh`, `OracleRunner`, shims), esquema H2 `schema-plataforma-a.sql` derivado de los SQL (vistas como tablas), semilla base, `VolcadoEquivalencia`, `compare.py`, `HarnessSelfCheckTest`, inventario de casos. Prueba con `ConsultarEstadoModulo` (1 caso) | agente 2 (en paralelo con 1A) | 0 | oraculo compila las clases legacy en un directorio temporal y graba un golden; sin credenciales ni HANA |
| 2 | **Paso 0 de higiene + kit:** lista blanca y dispatcher, `TokenInternoFilter`, `JavaCallUnitOfWorkFilter`, `SqlNombrado`, `ConfiguracionSistemaJdbc`, `DiccionarioMensajesJdbc`, `MensajeSalida`, `RevisarRequisitosBasicosService`; tests `PLA-KIT-nn` contra el oraculo del kit | agente 1 (`scaffolder` o `general-purpose`) | 1A, 1B | kit probado; los 326 stubs fuera del contexto; RULE-076 y `SinCambiosDeEsquemaTest` verdes |
| 3 | **`ConsultarEstadoModulo`** (la mas usada y la mas simple): servicio, repositorio, casos `PLA-ESM-*`, canario; primer corte por router con `clases-habilitadas` vacio | agente 1 | 2 | clase PROVEN en H2; medicion de latencia |
| 4 | **`CalcularParidad`**: dominio puro `ConversionParidad`, 6 SELECT, casos para las 14 ramas, determinismo (N corridas del oraculo), entradas frescas | agente 2 (paralelo con 3) | 2 | clase PROVEN; lista D-PAR para el experto |
| 5 | **`ConsultarMatrizOperacional`** + `ConsultarProductosPlantilla` + `ConsultarCobrosPlantilla` (+ Peru) | agente 1 | 3 | clase PROVEN |
| 6 | **`ConsultarFlujoCaja`** + `SaldoFlujoCaja` (26 SQL) + `ColaboradoresRouterClient` (SaldoCuentaInversion, SaldoPendienteDeLiquidar con servidores simulados); casos con tablas de paso y rollback | agente 2 (paralelo con 5) | 2 y decision D-3 | clase PROVEN con colaboradores simulados |
| 7 | **Equivalencia dual y entradas frescas** (`make_fresh.py`, `compare.py`, `FRESH_EQUIVALENCE.json`), mutacion de goldens, **canario de 8 mutaciones**, cobertura JaCoCo/lizard | agente 1 | 3-6 | `EQUIVALENCE.json`, `FRESH_EQUIVALENCE.json`, `equivalence/canary/BcsPlataformaServicios-transform/` |
| 8 | **Revision de arquitectura** (`architecture-critic`) y **revision de seguridad** (`security-auditor`), solo lectura sobre el codigo nuevo; los hallazgos HIGH se corrigen con tests | agente 2 (paralelo con 7) | 3-6 | seccion "Revision de arquitectura" en las notas, HIGH cerrados |
| 9 | **Cableado del router (solo configuracion + prueba) y runbook de corte**: fila de destinos, `clases-habilitadas` por clase, `registro-servicios`, token interno, puertos no publicados; ensayo con servidores simulados; plan de doble ejecucion en staging (>= 1.000 llamadas comparadas contra TomEE) | agente 1 | 7, 8 | runbook en `TRANSFORMATION_NOTES.md`; el trafico real sigue en 0 |
| 10 | **Verificacion independiente** (`/code-modernization:modernize-verify`) y actualizacion de `VERIFICATION.md`/`SIGNOFF.md` (firma de una persona) | persona + agente | 7-9 | veredicto por clase |

Estimacion de tamano (para planificar, no compromiso): pasos 1A/1B/3 chicos; 2, 4, 5 medianos; 6 el mas grande (`SaldoFlujoCaja` tiene 891 LOC y 26 SQL); 7-10 dependen de las decisiones.

## 9. Defectos del legacy que se mantienen (lista inicial para el experto de dominio) y desviaciones propuestas

Decision del usuario (vigente): los defectos se **mantienen bug a bug** y se listan. Las tablas de la seccion 4 traen el detalle; ids provisorios `D-ESM-1..5`, `D-PAR-1..10`, `D-FLC-1..9`, `D-MAT-1..9` (se renumeran como `SME-nn` en las notas del modulo cuando cada uno quede fijado por un test). Los mas relevantes para negocio:
- `ConsultarEstadoModulo` ignora `p_IdModulo` y devuelve todos los modulos de la empresa; el estado nulo es `ABT` (abierto) (D-ESM-1, D-ESM-4).
- `CalcularParidad`: un factor ausente en las ramas UF/UTM provoca NPE y aborta TODA la llamada en vez de marcar `RECHAZO` en esa fila (D-PAR-3); el redondeo mezcla `MathContext.DECIMAL128` y multiplicaciones sin contexto (D-PAR-4); estado compartido y carrera en la cache de factores (D-PAR-1).
- `ConsultarFlujoCaja`: contador de errores 1 fijo vs acumulativo, comparacion de `tipo` por referencia, tabla de paso que solo se vacia en la salida normal, portafolios duplicados en la tabla de paso (D-FLC-1..5).
- `ConsultarMatrizOperacional`: excepciones y NPE en lugar de mensajes (portafolio inexistente, plantilla vacia, entidad nula), `P_OUT_MENSAJES` siempre vacio (D-MAT-1..5).

Desviaciones que este plan propone y que **requieren aprobacion** (cada una con test nuevo, sin tocar los goldens del legacy):
- DEV-A1: los 326 stubs y su ruta de despacho dejan de servirse (infraestructura, no cambia comportamiento del legacy: hoy solo responden desde un modulo no desplegado).
- DEV-A2: el estado de `CalcularParidad` es local a cada llamada y la evaluacion es secuencial (mismo orden de salida que `parallelStream().map().collect()`); elimina la carrera del `HashMap`.
- DEV-A3: errores internos del servicio (excepciones) devuelven 500 generico con `correlationId` y sin `e.getMessage()` (el legacy dejaba el mensaje en la respuesta; se evalua con el equipo de UI si lee ese texto).
- DEV-A4: si un colaborador de otro modulo (`SaldoCuentaInversion`, `SaldoPendienteDeLiquidar`) no responde, se hace rollback y se contesta 503 para que el router caiga a TomEE (el legacy lo llamaba en proceso y no tenia ese modo de fallo).
- DEV-A5: el despachador exige token interno y lista blanca (el legacy no tiene `/internal/java-call`).

## 10. Riesgos, decisiones, preguntas y limites

### 10.1 Riesgos (R-01 a R-05 son los cinco mas importantes)

| Id | Riesgo | Mitigacion en el plan |
|---|---|---|
| R-01 | **El 92 % del uso es `ConsultarEstadoModulo` por sondeo de la UI cada 15 s por sesion**, y no puede recibir trafico real hasta que el despachador `/call` de `BcsPlataformaWeb` (bloque 9) o una ruta puntual del gateway la lleve al nuevo servicio. Probar el modulo no mueve el uso. Cualquier lentitud o 5xx afecta a todas las sesiones abiertas | decision D-7; se reporta como PROVEN-no-activo; medicion de latencia; fallback a TomEE por 503; sin cache nueva |
| R-02 | **El modulo existente responde 200 con datos falsos para 326 clases** (stubs, `P_OUT_ERRORESCONTADOR=0`) y para las 4 del alcance con un algoritmo distinto. Un prefijo `cl.bcs.plataforma.=JAVACALL@...` sin lista blanca los pondria en produccion | paso 2: lista blanca en el modulo y en el router (`clases-habilitadas`), test de cableado que exige 503 para todo lo demas |
| R-03 | **`ConsultarFlujoCaja` no es una lectura aislada:** escribe 2 tablas de paso (existentes), llama en proceso a `SaldoCuentaInversion` y `SaldoPendienteDeLiquidar` (modulos ya migrados, en otros servicios, con el ciclo CuentaInversion -> `CalcularParidad`), y su motor es el mas grande de la ola (891 LOC, 26 SQL, ~168 decisiones). Se pierde la transaccion compartida con esos dos colaboradores y la naturaleza temporal global de las tablas de paso no esta confirmada | D-3; una conexion por llamada; colaboradores por router con 503 -> TomEE; se habilita al final; Q-03/Q-04 |
| R-04 | **Equivalencia solo en H2**: dialecto, `DECIMAL` (escala), vistas HANA modeladas como tablas, orden sin `ORDER BY`, funciones y tablas temporales globales nunca se ejercitan; goldens de `BigDecimal` dependen de escalas supuestas | se declara en cada veredicto; Q-03 (un SELECT de catalogo de solo lectura si el usuario lo autoriza); canario y entradas frescas; doble ejecucion en staging antes de trafico real |
| R-05 | **`RegistraUso='N'` omite la sesion en el despachador moderno** (`SessionValidatorService.java:82`, identidad 0/0) y `ConsultarEstadoModulo` es de esas; en el legacy la sesion y la sobreescritura de `p_IdEmpresa` pertenecen a `CallServlet` (que no lee `RegistraUso`). Riesgo de exponer los estados de modulo sin autenticacion o con la empresa elegida por el cliente | Q-05; corregir en `BcsPlataformaWeb` (fuera de este bloque) antes de habilitar la clase por `/call`; en `/internal/java-call` rige el token interno |
| R-06 | No determinismo de `CalcularParidad`/`SaldoFlujoCaja` (`parallelStream`, `HashMap` compartido): goldens inestables o NPE intermitente en el propio legacy | N corridas del oraculo; casos no deterministas excluidos con motivo; DEV-A2 |
| R-07 | El jar desplegado (`PLUGIN_OPTIMUS.jar`, que `CallServlet` consulta primero para cualquier clase) puede no coincidir con el arbol; los parametros de `PLA.*` y el diccionario de mensajes son datos de HANA que se simulan | Q-02, Q-08; textos de mensajes fijados por golden con diccionario sembrado (no son los reales: DEV-18 sigue abierto hasta tener el diccionario real) |
| R-08 | Seguridad transversal pendiente fuera del modulo: `/internal/**` sin token en otros servicios, ARQ-H5/H6/H7, y credenciales HANA en claro en `QA_ACCESS_PROCEDURE.md` y en los `main()` legacy | regla 5; Q-09 |

### 10.2 Decisiones que necesitan al usuario

| Id | Decision | Recomendacion |
|---|---|---|
| D-1 | Corte de la ola A | 4 clases (99,0 % del uso de plataforma); ola A2 opcional con 3 lecturas del ranking (6, 8, 9) |
| D-2 | Typed REST ahora o despues | despues; solo `paridad/calcular` en una segunda pasada |
| D-3 | `ConsultarFlujoCaja` con colaboradores por HTTP (a), o aplazarla hasta empaquetar `SaldoCuentaInversion`/`SaldoPendienteDeLiquidar` como librerias en proceso (b) | (a) detras de bandera y al final; si la revision de `SaldoFlujoCaja` (paso 1A) muestra riesgo mayor, mover a ola B |
| D-4 | Estado de los 326 stubs actuales | sacarlos del contexto en el paso 2 y borrarlos en la ola B |
| D-5 | Aprobar DEV-A1..A5 | aprobar |
| D-6 | Meta de cobertura de ramas y numero de mutaciones del canario | >= 80 % de ramas por clase del alcance (como el criterio de `CrearOrdenes`) y 8 mutaciones |
| D-7 | Como mover el uso de `ConsultarEstadoModulo` | esperar el `/call` moderno (bloque 9); la alternativa (una regla del gateway solo para `t=600017`) requiere decidir quien valida la sesion |

### 10.3 Preguntas abiertas

| Id | Pregunta |
|---|---|
| Q-01 | Quien es el experto de dominio (paridad, flujo de caja, matriz operacional) que revisa la lista de defectos |
| Q-02 | El `PLUGIN_OPTIMUS.jar` de produccion/QA, se construyo de este mismo arbol (commit, fecha)? |
| Q-03 | Se autoriza un `SELECT` de catalogo de solo lectura en QA (columnas y escala de ~30 tablas/vistas del alcance, p. ej. `SYS.TABLE_COLUMNS`) con los programas existentes de `tools/`? Reduce R-04 sin tocar datos |
| Q-04 | Son `PLA.ConsultarFlujoCaja_Portafolio_Proceso` y `GBL.SaldoFlujoCaja_Proceso_Paso` tablas temporales globales? (mismo `SELECT` de catalogo, `TABLES`/`M_TEMPORARY_TABLES`) |
| Q-05 | En el legacy, `RegistraUso='N'` implica algo en la autenticacion? Quien cuenta `UsoServicio` (el contador sube aun con `N`) |
| Q-06 | `CalcularParidad` tiene dos ids (3155 y 600056): el router lo trata como ambiguo para el fallback a TomEE; se define uno canonico o se manda `idServicio`? `SaldoPendienteDeLiquidar` no esta en `PLA.Servicio`: se acepta que no tenga fallback a TomEE |
| Q-07 | Quien asigna los `RULE-nnn` a las reglas `PLA-*` nuevas en `BUSINESS_RULES.md` |
| Q-08 | Valores reales de `PLATAFORMA/PLA.MonedaSistema`, `PLA.LocalizacionSistema`, `PLA.DiasFechaMinimaCalculoParidad`, `PLA.TipoCalculoFlujoCaja` en QA (para sembrar H2 igual); se pueden leer del export existente o con `SELECT`? |
| Q-09 | Se saca `QA_ACCESS_PROCEDURE.md` de la carpeta compartida y se rotan esas credenciales? |

### 10.4 Lo que NO se puede probar (declarar en el veredicto)

HANA nunca se ejercita: todo es H2 con shims (dialecto, tipos, vistas reales, tablas temporales globales, plan de ejecucion, orden de filas, bloqueos). Tampoco TomEE (`PluginLoader`, `CallServlet`, transaccion del servlet), ni el comportamiento bajo carga real (1,77 M de llamadas, concurrencia en `ConfiguracionSistema` estatica), ni los textos reales del diccionario de mensajes, ni los datos reales de `PLA.*`/`MER.*`/`COM.*`, ni la integracion real con `BcsCuentaInversion/Tesoreria` (colaboradores simulados), ni la identidad de sesion de `/call`. La verdad del comportamiento es el codigo legacy ejecutado sobre H2; cualquier diferencia con HANA queda como riesgo hasta la doble ejecucion en staging.


## 11. Respuestas del usuario y resultados (2026-10-21)

| Pregunta | Respuesta / resultado |
|---|---|
| Q-02 | `PLUGIN_OPTIMUS.jar` desplegado sale de este mismo arbol (respuesta del usuario). Se deja de tratar la diferencia con produccion como riesgo R-07 por jar. |
| Q-03 | **Autorizado y ejecutado** (solo lectura, solo metadatos de `SYS.*`, sin datos de negocio): programa `tools/HanaCatalogoPlataformaOlaA.java`, resultados en `hana_qa_results/cat_*.csv`. Se encontraron las 37 tablas/vistas del alcance (32 tablas y 5 vistas): `cat_columnas_tablas.csv` 1065 columnas, `cat_columnas_vistas.csv` 216 columnas (tipo, longitud y escala reales: 293 columnas DECIMAL, 450 NVARCHAR, 189 BIGINT, 61 DATE, 39 SECONDDATE, 14 TIMESTAMP). Quedan fuera del catalogo las tablas `FMM.*`, `MES.*`, `SPT.*`, `Flujos.*` y la de Tesoreria usadas solo por `ConsultarFlujoCaja` (se pueden agregar al programa si se necesitan). |
| Q-04 | **Confirmado:** `PLA.ConsultarFlujoCaja_Portafolio_Proceso` y `GBL.SaldoFlujoCaja_Proceso_Paso` son tablas **temporales globales** de HANA (`IS_TEMPORARY=TRUE`, `TEMPORARY_TABLE_TYPE=GLOBAL`, tipo ROW): los datos viven por sesion. Se debe usar UNA conexion por llamada y rollback al fallar (como ya propone el plan, riesgo R-03). |
| Q-05 | El usuario indica que `RegistraUso='N'` significa **"No registra uso"** (no escribe el contador de uso). El riesgo R-05 (el despachador moderno salta la validacion de sesion con `RegistraUso='N'`) sigue vigente: debe documentarse y corregirse fuera de este bloque, antes de habilitar `/call` para `ConsultarEstadoModulo`. |
| Q-06 | Aceptado: `CalcularParidad` con dos ids se resuelve sin ambiguedad con `idServicio` o un id canonico, y `SaldoPendienteDeLiquidar` no tendra respaldo a TomEE. |
| Q-01, Q-07, Q-08, Q-09 | Sin respuesta por ahora (experto de dominio, asignacion de RULE-nnn, valores de `PLA.*`, rotacion de credenciales de `QA_ACCESS_PROCEDURE.md`). |
| D-1 a D-7 | **Pendientes de decision explicita del usuario** (no se lanza el paso 1 hasta que las confirme). |
