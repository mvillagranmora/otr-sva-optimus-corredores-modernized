# Patron de jobs en proceso (aplica a TODOS los *Jobs)

Decisiones del usuario (2026-10-21): los jobs se modernizan **en proceso** (libreria sobre su Servicio, `JobRunner` copiado por modulo, `POST /internal/job-call`, sin `@Scheduled`, sin modo demonio RULE-061, sin `main()` ni credenciales RULE-076/077). Produccion es **Linux** (saltos de linea LF, zona y charset fijados en la JVM).

## Patron a seguir: H4 (parametros y reclamo de ejecucion), decidido 2026-10-21

Hallazgo de la revision de arquitectura de BcsTesoreriaJobs (H4), que se anota como patron para todos los jobs (Tesoreria, APV, CuentaInversion y, despues, BcsContabilidadJobs y los de fases siguientes):

1. **Parametros desde `BAT.Programa`.** El Job Manager del legacy leia los parametros del job (IdEmpresa, IdUsuario, ...) de la fila del programa, de modo que quien dispara la ejecucion no los elegia. El nuevo `POST /internal/job-call` debe recibir el `idPrograma` y cargar los parametros de esa fila (o, como minimo, verificar que la fila existe, que su clase coincide con la del job y que los parametros enviados coinciden). Hoy el llamador los elige libremente: es una diferencia con el legacy que este patron cierra.
2. **Reclamo en base de datos.** La guarda contra doble ejecucion no puede ser solo por JVM: reclamar la fila del programa en BD (`UPDATE ... WHERE estado <> 'RUNNING'` o `SELECT ... FOR UPDATE NOWAIT`) y liberarla al terminar. Con mas de una replica o en un despliegue progresivo, dos corridas del mismo programa generarian dos archivos y dos ejecuciones de procedimientos almacenados.
3. **Clave de la guarda por programa, no por clase** (M4): el legacy reclamaba por programa; la guarda por clase rechaza con 409 un segundo `GenerarArchivoPago` legitimo.
4. Las correcciones se hacen **sin cambiar el comportamiento pinado por los goldens**: hacen falta tests nuevos (no se tocan los casos grabados del legacy), y el esquema de `BAT.Programa` es una suposicion documentada hasta verla en HANA.

## Patrones de despliegue y robustez que acompanan (hallazgos de la misma revision)

- H3: acotar cada corrida (timeout de transaccion y de consulta, watchdog) y poder listar las corridas en curso, para que un HANA colgado no bloquee la clase hasta reiniciar.
- H2: archivos de salida en un volumen compartido (no en la capa efimera del contenedor), healthcheck y usuario no root.
- M6: apagado ordenado (`server.shutdown=graceful`, `stop_grace_period`, rechazar corridas nuevas al apagar).
- M9: fijar `-Dfile.encoding=UTF-8 -Duser.timezone=<zona> -Duser.language=en` y charset explicito por archivo.
- M7/M10: validar `JAVACALL_URL` al arrancar, token de longitud minima, registrar los 401.

Estado: PLANIFICADO como patron; se ejecuta como tarea transversal (una vez) sobre los jobs de la Fase 2 cuando estos esten PROVEN, con nueva verificacion y firma. Mientras tanto cada modulo lo lista como hallazgo abierto en su `TRANSFORMATION_NOTES.md`.

## Decision H-1 (aprobada por el usuario 2026-10-21): colaboradores y transaccion del job

**Legacy (verificado en el codigo):** `BcsJob.java:97-147` abre UNA conexion del datasource por corrida y hace commit/rollback/close. Los jobs llaman a los colaboradores en la misma JVM pasandoles esa misma conexion (`procesoOrdenes.logica(con, ...)` IngresarOrdenRVBonificacionFiscal.java:598, `llamadoFlujoCaja.logica(con, ...)` :371, `AgregarLogPrograma.logica(con, ...)` :259/:432/:601, `ConsultaCustodia.logica(con, ...)` :331; en FM `crearSolicitudAporteRescateNormal.logica(con, ...)` :291). Ordenes creadas, logs y cambios del job forman una sola transaccion.

**Problema del nuevo:** lo que vive fuera del modulo se llama por HTTP al router y cada llamada confirma en su propia transaccion: un rollback del job no deshace las ordenes ya creadas y un reintento las duplica. Los goldens no lo muestran (colaboradores simulados).

**Linea aprobada (se ejecuta junto con el patron H4, como una tarea transversal sobre los Jobs y los Servicios, cuando se decida iniciarla):**
1. Colaborador YA migrado y empaquetado como libreria (Tesoreria, CuentaInversion, APV...): llamarlo en proceso, con el mismo `DataSource` y la misma transaccion del job, sin HTTP (como ya hace CuentaInversion Jobs con los servicios de su modulo). Es lo mas fiel al legacy: una transaccion, rollback conjunto.
2. Colaborador que sigue en TomEE (Orden, FondoMutuo, Plataforma mientras no se migren): no se puede compartir la transaccion entre procesos. Para ese tramo: (a) reclamo por programa en `BAT.Programa` antes de llamar a un colaborador que confirma por su cuenta (el mismo reclamo en BD de H4); (b) llave de idempotencia `idPrograma` + numero de corrida enviada al router; (c) fallo cerrado: si el job falla despues de que un colaborador ya confirmo, el programa queda marcado "requiere revision" y no se reintenta solo.
3. A medida que se migren esos modulos, pasarlos al caso 1 y eliminar el HTTP (otro criterio para ordenar las fases por dependencias).
4. Los puntos 2(a)-(c) son un cambio de diseno respecto al legacy que busca el mismo resultado observable (sin duplicados ni rollbacks a medias); cada cambio lleva tests nuevos sin tocar los goldens del legacy, y el esquema de `BAT.Programa` es una suposicion documentada hasta verlo en HANA.
5. Criterio de entrada para cualquier trafico real: ningun job ni servicio con colaboradores que confirman por HTTP recibe trafico real hasta tener esto (o aceptar por escrito el riesgo). Aplica a: BcsAPVJobs (CrearOrdenes, CrearSolicitudAporteRescateNormal, AgregarLog, ConsultarFlujoCaja...), BcsTesoreriaJobs, BcsCuentaInversionJobs (PAUTA/InscribirProcesoBatch) y a los Servicios cuyos colaboradores van por HTTP (hallazgo H4 de APV Servicios, ColaboradoresJavaCall de Tesoreria/APV).

## Decisiones del usuario sobre H-1/H4 (2026-10-21)

1. **Quien dispara y administra los jobs del codigo nuevo es `BcsPlataformaJobs` (modernizado).** El codigo legacy no debe interactuar en ningun momento con el codigo modernizado: el Job Manager legacy (TomEE) NO llama a `/internal/job-call`. Consecuencia de diseno: los programas de los jobs modernizados no deben ser tomados por el Job Manager legacy. El legacy selecciona programas con `P.Nodo = ?` (TransactionManager.readProgramm, ~l.41-60: `Bloqueo='N'`, `idEstado='PEN'`, `Intento < MaximoIntentos`, ventana de `fechaEjecucion`): el motor nuevo puede usar su propio valor de `Nodo` en `BAT.Programa` para sus programas, sin tocar el codigo legacy.
2. **NO se hace ningun cambio en la base de datos (HANA) por esta modernizacion, bajo ningun motivo** (CORRECCION del usuario, 2026-10-21; reemplaza la version anterior que aceptaba cambios): ni tablas, ni columnas, ni vistas, ni procedimientos, ni datos de configuracion nuevos. Tampoco se modifica el codigo legacy. Si un dato no existe y la unica opcion es usar `BAT.Programa`, se usa `BAT.Programa` (columnas existentes: `idEstado`, `Bloqueo`, `Intento`, `Nodo`, `Resultado`, `fechaEjecucion`, `fechaFin`, `IdAgenda`...). Cualquier reclamo, marca de idempotencia o auditoria debe expresarse con las columnas y tablas que ya existen; lo que no se pueda expresar asi queda como limitacion documentada, no como DDL.
3. **Estado "requiere revision":** `idEstado='ERR'` con `Intento = MaximoIntentos` y `Bloqueo = 'S'` (el manager no reintenta solo). Aceptado.
4. **El router de la plataforma NO lleva cabecera de idempotencia hacia TomEE** (no se modifica el comportamiento del legacy): la idempotencia debe ser del lado del job (reclamo y marcas propias en BD), no del colaborador.

## Decisiones tras el inventario `JOBS_H1_H4_INVENTORY.md` (usuario, 2026-10-21)

Hallazgos que reencuadran: (a) ningun colaborador legacy confirma por si mismo (solo hacen commit en sus `main()` de prueba): reciben `logica(con, ...)` y confirma el anfitrion; (b) `BcsPlataformaJobs` no es un despachador (solo pauta y parametros financieros): el motor de cola moderno es `BcsJobManager` y esta incompleto (`executeJob` es un placeholder, `lockPrograma` no se usa); (c) el legacy no reintenta automaticamente (`updateTryProgramm` comentado, BcsJobManager.java:104-105) y su reclamo no es atomico; (d) `Programa.Nodo` lo escribe solo `BAT.AgregarPrograma` copiandolo de `TipoPrograma.Nodo`: un Nodo exclusivo para el motor moderno no se puede garantizar sin cambiar datos; (e) los 4 `JobStatusListener` solo escriben un log: no reproducen los UPDATE de `BAT.Programa`.

Decisiones:
1. **Despachador de los jobs nuevos: completar `BcsJobManager`** (reemplaza la decision anterior de ponerlo en `BcsPlataformaJobs`, que no tiene ese codigo); `BcsPlataformaJobs` queda como esta.
2. **Reintento: igual que el legacy, sin reintento automatico.** Un fallo queda en ERR y solo se relanza a mano (`ReprocesarPrograma`). Se desactiva el reintento automatico (RULE-059) del `BcsJobManager` moderno.
3. **Corte operativo por tipo de programa**, sin tocar la BD ni el legacy: al migrar un job, su `IdTipoPrograma` se excluye del arranque del Job Manager legacy (argumento de arranque o retiro de ese proceso); se documenta en el runbook de despliegue. Pendiente de un DBA (solo lectura): `SELECT DISTINCT Nodo FROM BAT.TipoPrograma` y `SELECT DISTINCT Nodo, IdTipoPrograma FROM BAT.Programa`, mas los argumentos de arranque reales de los procesos legacy.
4. Plan de implementacion: seccion D del inventario (pasos 0 a 9, sin DDL).

## Paso 5b (agregado por el usuario, 2026-10-21): router interno `/internal/java-call` en `BcsPlataformaWeb`

**Problema verificado:** los cuatro adaptadores de colaboradores (`ColaboradoresJobs` y `ColaboradoresJavaCall` de Tesoreria, APV, CuentaInversion y Contabilidad) mandan las llamadas a otros modulos a `POST {JAVACALL_URL}/internal/java-call` con `{"class","params"}` y `X-Internal-Token`, pero `BcsPlataformaWeb` solo expone `/call` (con sesion de usuario), `/CallBatch`, `/CrearSesion`... Los unicos `/internal/java-call` existentes estan en servicios destino (Contabilidad, piloto viejo de CanalAgente). Nadie atiende `JAVACALL_URL`: hoy esas llamadas no llegan a ninguna parte.

**Alcance (orden de ejecucion: despues de los pasos 1-4 en los cuatro Jobs):**
1. Nuevo `InternalJavaCallController` en `BcsPlataformaWeb`: `POST /internal/java-call` (cuerpo `{class, params}`), protegido por filtro de token interno (lista de rutas publicas, falla cerrado, comparacion en tiempo constante, ruta `//` y `;` normalizadas), SIN sesion de usuario y SIN la imposicion de `IdEmpresa/IdUsuario` de sesion de `/call` (ARQ-H5): los params los manda un job de confianza.
2. Enrutamiento por clase reutilizando `JavaCallRouterService`: clases habilitadas (`plataforma.javacall.clases-habilitadas`, hoy vacia = todo a TomEE) van al servicio migrado dueño; el resto a TomEE; mismo criterio de reintento (solo 503 o conexion rechazada se reintenta en TomEE; cualquier otro error se propaga: pudo ocurrir tras un commit, ARQ-H6) y sin cabecera de idempotencia hacia TomEE (legacy intacto).
3. Traduccion hacia los servicios migrados (los 14 colaboradores del grupo M del inventario, A.2): por dueño, una estrategia de destino: (a) Contabilidad: su `/internal/java-call` existente; (b) APV: la libreria `BcsApvJavaCallAdapter` (registro clase legacy -> ruta REST tipada) ya existe; (c) Tesoreria y CuentaInversion: inventariar si exponen un punto equivalente o si hace falta un adaptador analogo al de APV (sin cambiar su comportamiento ni la BD).
4. Pruebas con servidores simulados por rama: 401 sin token y con token mal formado, ruta habilitada -> destino, ruta no habilitada -> TomEE, 503 -> reintento en TomEE, 500/timeout -> se propaga, sin imposicion de identidad, cuerpo y respuesta pasan sin alterarse, cada clase del grupo M del inventario resuelve a un destino.
5. Limites conocidos: el mapeo completo de las ~1.817 clases requiere la exportacion del registro `PLA.Servicio` (requisito de la Fase 5, bloqueo externo); mientras tanto el enrutamiento es configuracion explicita por clase. En proceso (jobs que empaquetan a su colaborador migrado como libreria) NO usan este endpoint: usan el despachador local del paso 5 y la misma transaccion.

Criterio de entrada para trafico real: este paso completo (o aceptar por escrito que los colaboradores de otro modulo no estan disponibles).
