# Runbook: abrir y cerrar las listas del Manejador y las clases de FondoMutuo

Estado al 28-oct-2026. El servicio **no recibe tráfico real** hasta que el módulo esté PROVEN y firmado (`analysis/otr-sva-optimus-corredores/VERIFICATION.md`, `SIGNOFF.md`). Todo se abre por configuración y se revierte reiniciando, sin cambiar código.

## 1. Las perillas

| Qué | Dónde | Valor por defecto | Efecto |
|---|---|---|---|
| Listas del Manejador que sirve este módulo | `fondomutuo.solicitud.listas-servidas` (env `FONDOMUTUO_SOLICITUD_LISTAS_SERVIDAS`) | `NORMAL` | Una lista fuera de la propiedad responde 503 **antes** de escribir nada (G0); el router cae a TomEE. |
| Medio de pago distinto de CIN (Tesorería en proceso) | `fondomutuo.tesoreria.habilitada` | `true` | `false` devuelve esas peticiones a TomEE (503) sin cambiar código. |
| Compuerta de Spot (G2) | `fondomutuo.spot.habilitado` | cerrada | Sin `SpotPort` real queda cerrada. |
| Flujo de caja manual (G3) | `fondomutuo.flujo-caja.habilitado` | `false` | Cerrada: aportes CIN de origen manual (SaldoFlujoCaja) responden 503. |
| Canal OWP (G4) | `fondomutuo.owp.habilitado` | `false` | Cerrada: canal OWP con notificación de estado de órdenes responde 503. |
| Custodia en proceso (G-CUS) | `fondomutuo.custodia.habilitada` | `false` | Interruptor de la Custodia en proceso para **RescateReinversion** y del adaptador `@Primary` `FlujoCustodiaEnProceso`. Cerrada: la custodia por HTTP rompe la atomicidad. Se abre **solo** con la librería de Custodia en proceso (misma transacción, sección 2.1): con `true` el arranque **falla** si el puerto de custodia efectivo no es ese. Desde la revisión del 28-oct **ya no abre Normal ni cambia las comisiones** (dos interruptores propios, abajo). |
| Normal llama al flujo de custodia | `fondomutuo.custodia.normal-habilitada` | `false` | Con `false`, Normal responde 503 G-CUS al llegar al flujo de custodia aunque `habilitada=true`. Con `true` exige `habilitada=true` (si no, el arranque falla: Normal necesita el adaptador en proceso, el HTTP rompe la atomicidad). |
| `CalcularComisiones` usa la custodia en proceso | `fondomutuo.custodia.comisiones-en-proceso` | `false` | Con `false`, `CalcularComisiones` (la de Normal y la del cierre) sigue usando el `CustodiaPort` HTTP `ColaboradoresHttp` aunque `habilitada=true` haya puesto el adaptador en proceso como `@Primary`. Con `true` usa el adaptador en proceso y exige `habilitada=true` (si no, el arranque falla). |
| Guardia de transacción de la custodia | `fondomutuo.custodia.exigir-transaccion` | `true` | El flujo de custodia en proceso exige una transacción Spring activa y no de solo lectura (`IllegalStateException` si no). Solo el arnés de pruebas la apaga. |
| Cálculo FFMM de CPF (polaridad OPUESTA a `habilitada`) | `fondomutuo.custodia.ffmm.cerrada` | ausente = `false` = abierto | `true` **cierra** el cálculo (503 G-CUS); aquí `true` cierra, en `habilitada` `true` abre. Acepta solo `true`, `false` o ausente: cualquier otro valor (vacío, `yes`, `1`, un typo) **impide arrancar** (0 beans del puerto de custodia de CPF), a propósito (sección 7). |
| Qué clases entrega el router de BcsPlataformaWeb a este módulo | `plataforma.javacall.clases-habilitadas` (env `PLATAFORMA_JAVACALL_CLASES_HABILITADAS`) | vacío (todo a TomEE) | Solo las clases listadas y con destino van a este servicio. |
| Destino del paquete | `plataforma.javacall.destinos` (env `PLATAFORMA_JAVACALL_DESTINOS`) | sin entrada para `cl.bcs.fondomutuo.` | Hay que agregar `cl.bcs.fondomutuo.=JAVACALL@http://bcs-fondo-mutuo-servicios:8111` al abrir. |

Las clases de escritura que acepta este servicio (más las ocho lecturas de la sección 7): `cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate`, `cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal` y `cl.bcs.fondomutuo.CrearSolicitudRescateReinversion`; y, único de la UI que escribe, `cl.bcs.fondomutuo.CargarValorCuota` (sección 8). Cualquier otra se rechaza al arrancar.

## 2. Qué se puede abrir hoy

- **`NORMAL`**: es lo único abierto por defecto. Con la firma del experto, se enruta `cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate` y `...CrearSolicitudAporteRescateNormal`.
- **`RESCATE_REINVERSION`: ya puede abrirse, con `fondomutuo.custodia.habilitada=true` y el adaptador en proceso (sección 2.1).** La clase pasa siempre por la comprobación de custodia (G-CUS), incluso al simular. Con G-CUS cerrada (el valor por defecto) toda petición que llegue a ese punto responde 503 sin efectos, como antes: abrir la lista sin abrir la custodia solo agrega 503. Con G-CUS abierta el flujo de custodia se calcula **dentro de la misma transacción** de la petición (librería `BcsCustodiaServicios`), de modo que un rollback del servicio no deja movimiento de custodia. Abrirla sigue necesitando la firma del experto (esa decisión no está en el código).
- El arranque **se niega** si se configuran `RESCATE_REINVERSION` y otra lista a la vez con `fondomutuo.custodia.habilitada=false` (revisión de arquitectura del 26-oct: una petición mixta escribiría la otra lista y fallaría después en G-CUS con un 500; el reintento del cliente la duplicaría).
- Programado, Overnight, Cambio de serie, Transferencia, Traspaso y Aporte con reinversión **no tienen handler**: nombrarlos en la propiedad impide arrancar.

### 2.1 Custodia en proceso (G-CUS) — 28-oct-2026

- **Qué es.** `cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo` (jar plano de `BcsCustodiaServicios`, PROVEN contra 85 goldens del legacy `cl.bcs.custodia.ConsultarFlujoCustodia`) se llama dentro del proceso con la conexión de la transacción de la petición. La librería no abre otra conexión ni hace commit/rollback; escribe solo filas de trabajo en `CUS.FlujoCustodia_MatrizUso` y las borra al terminar; todo va con la transacción (un rollback no deja nada).
- **Cómo se abre.** `fondomutuo.custodia.habilitada=true`. Configure siempre `true` o `false`: las compuertas leen la propiedad con la conversión booleana de `@Value` de Spring, que también toma `true`, `yes`, `on` y `1` como abierta (el adaptador en proceso solo se registra con exactamente `true`: con `yes` el arranque falla por la guardia, a propósito). Eso registra el adaptador `FlujoCustodiaEnProceso` como `CustodiaPort` efectivo; el adaptador HTTP (`ColaboradoresHttp`) queda registrado como respaldo y, con la propiedad en `false` (por defecto), sigue siendo el único: toda petición que llegue a custodia responde 503 en G-CUS sin efectos.
- **Alcance de las banderas de custodia (revisión de arquitectura del 28-oct; el interruptor se SEPARÓ por decisión del usuario).** Con las tres en su valor por defecto el comportamiento es el de antes de la revisión.

| `habilitada` | `normal-habilitada` | `comisiones-en-proceso` | Resultado |
|---|---|---|---|
| `false` | `false` | `false` | Por defecto. Todo por HTTP; RRE y Normal responden 503 G-CUS al llegar a custodia. |
| `true` | `false` | `false` | **Solo RescateReinversion** se sirve en proceso. Normal sigue en 503 G-CUS; `CalcularComisiones` sigue con el HTTP (`ColaboradoresHttp`). |
| `true` | `true` | `false` | RRE y Normal en proceso (misma transacción). Las comisiones siguen por HTTP. |
| `true` | `false` | `true` | RRE en proceso y `CalcularComisiones` con el adaptador en proceso. Normal sigue en 503 G-CUS. |
| `true` | `true` | `true` | Todo en proceso. |
| `false` | `true` | cualquiera | **El arranque falla** (Normal necesita el adaptador en proceso; el HTTP rompe la atomicidad). |
| `false` | cualquiera | `true` | **El arranque falla** (las comisiones en proceso exigen el adaptador en proceso). |

  Clases que cambia cada una: `habilitada` -> `custodia.FlujoCustodiaEnProceso`, `rescatereinversion.*` (RRE) y la clase JavaCall del flujo de custodia de FondoMutuo; `normal-habilitada` -> `normal.NormalFlujos` (compuerta antes de llamar al flujo) y `custodia.ConsultarFlujoCustodiaFondoMutuoService` desde Normal; `comisiones-en-proceso` -> `comisiones.CalcularComisiones` (la del cierre, `cierre.CierrePuroConfiguracion`, y la que arma `normal.NormalFabrica`). `ffmm.cerrada` es de otra cosa (CPF) y tiene polaridad opuesta: `habilitada=true` abre, `ffmm.cerrada=true` cierra.
- **Plazo y transacción de la librería.** La librería recibe un proxy de la conexión: cada sentencia que crea lleva el plazo restante de la transacción (el `DELETE` de limpieza de sus filas de trabajo nunca falla por plazo vencido: con el plazo agotado lleva 1 s) y `commit`, `rollback`, `close`, `setAutoCommit`, etc. lanzan `IllegalStateException`. Una línea INFO por llamada: `custody flow in process: result=ok|sql|runtime|checked ms=... carteras=N instrumentos=M keyFlujoCustodia=...` (solo conteos y claves).
- **Guardia de arranque.** Si la propiedad está abierta y el `CustodiaPort` efectivo no es el adaptador en proceso, el servicio **no arranca** (`IllegalStateException`, mensaje `fondomutuo.custodia.habilitada is true but the effective CustodiaPort is ...`). Protege la atomicidad: no se puede abrir G-CUS con custodia por HTTP.
- **Límite: el flujo valorizado.** `ValorizarInstrumento` no es un colaborador de este módulo. Todas las llamadas de FondoMutuo piden `p_ValorizarFlujo=N`; si alguien pide `S` el adaptador responde 503 (G-CUS) antes de leer o escribir nada. Con `N` el monto a mercado que devuelve la custodia es 0 (no valoriza); FondoMutuo usa solo cantidades.
- **Que no se confunda con CPF.** Abrir G-CUS (`fondomutuo.custodia.habilitada`) no abre CPF: `CONSULTAR_CUSTODIA_PORTAFOLIO` tiene su propio cálculo de custodia en proceso (`ConsultarCustodiaPortafolioFFMM`, sección 7) y su propio interruptor `fondomutuo.custodia.ffmm.cerrada`.
- **Datos HANA que hay que verificar en QA antes de abrir** (aquí solo se probó en H2): las secuencias `BSCL.Custodia.BD.Secuencias::FlujoCustodia` y `::DatosFlujoCustodia` existen y el usuario del servicio puede leerlas y escribir/borrar en `CUS.FlujoCustodia_MatrizUso`; la configuración de uso (`CUS.UsoFlujoCustodia` / `CUS.ItemsFlujoCustodia`) del uso `RESCFM` (la similitud del legacy lanza NullPointerException si el instrumento, cliente o portafolio configurado es NULL frente a uno pedido: la fila genérica debe llevar 0); y el predicado `(1==1)` del SQL de cartera (heredado del legacy, no verificado en HANA; ver las notas de BcsCustodiaServicios).
- **Los RRE que envían cantidades**: con la custodia real la cantidad llega con 8 decimales (`1000.00000000`) y el texto del mensaje `UI_ERROR_FM_CLIENTE_SIN_CUSTODIA` la imprime así; el legacy con su custodia real hace lo mismo (el doble de los goldens usaba un entero).
- **Cómo revertir.** `fondomutuo.custodia.habilitada=false` y reiniciar: vuelve el 503 en G-CUS; no hay estado propio.

### 2.2 Límites operativos de la custodia y de FFMM (pendientes de medir en QA; revisión del 28-oct)

- **M6 (flujo de custodia):** la librería carga todo en memoria y pagina con `LIMIT`/`OFFSET` de 100.000 **sin `ORDER BY`**. Medir en QA con una cartera grande (tiempo, memoria y estabilidad de la paginación) antes de abrir `habilitada`.
- **M7 (FFMM):** sin tope de filas ni orden. Medir con los comodines `IdCliente`/`IdPortafolio` <= 0 antes de abrir `CONSULTAR_CUSTODIA_PORTAFOLIO`.
- **L1:** el `IdTrabajo` de `CUS.FlujoCustodia_MatrizUso` es **aleatorio** (n/1e10 de colisión por fila viva): frente a filas que deje el TomEE legado en la misma tabla la probabilidad es pequeña pero no nula.
- **L7:** el `IN` de instrumentos de la librería no tiene tope; el límite de parámetros de HANA para esa condición **no está verificado**.

## 3. Cómo abrir (cuando haya firma)

1. Verificar `VERIFICATION.md`: PROVEN para BcsFondoMutuoServicios y BcsPlataformaWeb, firmado.
2. Backups y ventana acordados con el experto de dominio; avisar a quienes llaman en proceso (los jobs y cargas masivas legacy siguen usando su copia en TomEE).
3. En el servicio: `fondomutuo.solicitud.listas-servidas=NORMAL` (más las que se hayan habilitado), reiniciar y comprobar el arranque.
4. En el router: agregar el destino del paquete y las clases en `clases-habilitadas`, reiniciar BcsPlataformaWeb.
5. Probar con una petición simulada (`p_SimularOperacion=S`) y luego una real de bajo monto; revisar la fila escrita y el log.

## 4. Cómo revertir

Quitar las clases de `plataforma.javacall.clases-habilitadas` (o la lista de `fondomutuo.solicitud.listas-servidas`) y reiniciar: el tráfico vuelve a TomEE sin cambio de código. Lo ya escrito está en las mismas tablas que usa el legacy; no hay estado propio que migrar.

## 5. Qué observar

- **503 con G0**: la lista no está servida; es el comportamiento seguro.
- **503 con G1/G2/G5/G-CUS**: la petición no se ejecutó; el router puede repetirla en TomEE.
- **500 después de una escritura confirmada** (`EscrituraConfirmada`): no se repite en TomEE; revisar a mano antes de que el cliente reintente, porque el bloqueo anti-duplicados puede haber liberado y un reintento duplicaría. Hoy no hay conciliación automática de este caso (hallazgo de la revisión).
- **Bloqueo de reinversión que no se libera**: tras una excepción posterior al bloqueo quedan la fila autónoma y la tabla de sesión (comportamiento heredado, D-F2-1); todas las reinversiones siguientes en esa conexión fallan con `UI_ERROR_BLOQUEO_REINVERSION` hasta reciclarla. Reciclar la conexión o reiniciar el servicio.

## 6. Pendiente conocido (revisión de arquitectura 26-oct, sin aplicar)

- Una tabla de sesión que ya existe hace fallar la creación del bloqueo; la corrección DEV-6-H1 solo la suelta cuando falla el INSERT o el CALL del propio bloqueo, no sana una fuga previa.
- Una liberación fallida del bloqueo seguida de 503 deja el bloqueo tomado; el repetido en TomEE ve "solicitud duplicada".
- Sin tope de tamaño en las listas de la petición (las condiciones `IN` crecen sin límite).
- Los mensajes de error de la base pueden llegar al log con valores.

## 7. Las ocho lecturas de la UI (F3 W1)

Estado al 27-oct-2026. Son lecturas de la interfaz, sin escritura, sin bloqueo, sin Tesorería. Cada una se abre con su **propio nombre** en `fondomutuo.solicitud.listas-servidas` (junto a `NORMAL`, separados por coma) y se enruta con su clase en `plataforma.javacall.clases-habilitadas`. Cerradas por defecto: la clase responde 503 y el router cae a TomEE.

| Nombre en la propiedad | Clase JavaCall |
|---|---|
| `CONSULTAR_FECHA_SOLICITUD` | `cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate` |
| `CONSULTAR_VALOR_DOLAR` | `cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo` |
| `CONSULTAR_FONDO_REINVERSION` | `cl.bcs.fondomutuo.ConsultarFondoMutuoReinversion` |
| `CONSULTAR_INSTRUMENTOS` | `cl.bcs.fondomutuo.ConsultarInstrumentosFondosMutuos` |
| `CONSULTAR_SOLICITUDES` | `cl.bcs.fondomutuo.ConsultarSolicitudesFFMM` |
| `CONSULTAR_SOLICITUDES_WS` | `cl.bcs.fondomutuo.ConsultarSolicitudesFFMMWS` |
| `CONSULTAR_DETALLE_SOLICITUD` | `cl.bcs.fondomutuo.ConsultarDetalleSolicitud` |
| `CONSULTAR_CUSTODIA_PORTAFOLIO` | `cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo` |

Ejemplo: `fondomutuo.solicitud.listas-servidas=NORMAL,CONSULTAR_FECHA_SOLICITUD,CONSULTAR_VALOR_DOLAR`. El arranque registra en INFO las lecturas abiertas (solo nombres).

- **Un nombre mal escrito impide arrancar** (`names an unknown list`): es a propósito.
- **`CONSULTAR_CUSTODIA_PORTAFOLIO` ya puede abrirse** (28-oct-2026). El cálculo de custodia que CPF llama en proceso (`ConsultarCustodiaPortafolioFFMM`, solo lectura, sin lock ni Tesorería) ya existe en este módulo y su adaptador real `ConsultarCustodiaPortafolioFfmmAdaptador` es el bean **por defecto** del puerto de custodia de CPF (siempre hay exactamente un bean). FFMM no es una entrada JavaCall propia: no tiene lista, ni handler, ni entrada en la lista blanca; solo se alcanza a través de CPF. Abrirla sigue necesitando la firma del experto (esa decisión no está en el código).
  - **Valores de `fondomutuo.custodia.ffmm.cerrada`:** acepta **solo** `true`, `false` o ausente (ausente = abierto). Cualquier otro valor (vacío, `yes`, `1`, un typo) impide arrancar (0 beans del puerto de custodia de CPF): es a propósito, un typo no deja el cálculo abierto o cerrado por accidente. Polaridad opuesta a `fondomutuo.custodia.habilitada` (allí `true` abre; aquí `true` cierra).
  - **Interruptor operativo:** `fondomutuo.custodia.ffmm.cerrada=true` vuelve a cerrar el cálculo (el bean pasa a ser el adaptador cerrado: 503, compuerta G-CUS, sin efectos). Con el interruptor cerrado y `CONSULTAR_CUSTODIA_PORTAFOLIO` en la propiedad el arranque **falla** con `IllegalStateException` (guardia de arranque, sin cambios); con el interruptor cerrado y la lista fuera, el servicio arranca con el adaptador cerrado.
  - **Hallazgos de datos reales (QA, solo lectura, 28-oct) que cambian lo que se ve al abrirla.** (1) **D-F3C-19:** 14 `IdPersona` tienen DOS socios de negocio en `SOC.SocioDeNegocio`; los SQL de custodia y garantía unen por persona, así que las cantidades de esas personas salen **duplicadas** (el legacy hace lo mismo: se replica, no se corrige). (2) `CUS.Cartera.IdRegimenFondoMutuo` admite nulos: esas posiciones agrupan con el régimen `null` (clave con el texto `null`). (3) `ConsultarCustodiaPortafolioFFMM` **no está registrada en `PLA.Servicio`**: solo existe la gemela `ConsultarCustodiaPortafolioFFMMWS` (id 700032, 0 usos); por eso FFMM no se expone por su cuenta.
  - El orden de las filas de CPF depende del `HashMap` de FFMM (bug replicado, D-F3C-13): no hay orden estable que un consumidor pueda asumir.
- Las lecturas junto a `RESCATE_REINVERSION` no cuentan como "otra lista" en la regla de custodia cerrada (sección 2).
- **Zona horaria: la JVM debe correr en UTC** (el contenedor lo hace). Si hay lecturas abiertas y la zona no es UTC, el arranque escribe un WARN (no falla): la aritmética de fechas replica el legacy y asume UTC.
- Las lecturas corren en una transacción de solo lectura con el plazo de la transacción (`services.javacall.transaction-timeout-seconds`, 120 s) aplicado también a cada consulta SQL (desde el 28-oct también a las del flujo de custodia en proceso, por el proxy de la conexión, sección 2.1; el `DELETE` de limpieza lleva como mínimo 1 s).
- **Supuesto del router (pendiente de dejar en el runbook de producción)**: `p_IdEmpresa` y `p_IdUsuario` los pone el router y este módulo **no los contrasta**.
- **Riesgo heredado, sin cambio (decisión del usuario, fiel al legacy)**: `CONSULTAR_SOLICITUDES` y `CONSULTAR_SOLICITUDES_WS` no exigen filtro ni filtran por empresa y no tienen tope de filas: una petición sin filtro lee toda la tabla (riesgo de DoS por volumen) y puede devolver datos de otra empresa. Abrirlas solo con el router como único llamador.

## 8. Carga de valor cuota (F3 W2, CVC): la única clase de la UI que ESCRIBE

Estado al 27-oct-2026. `cl.bcs.fondomutuo.CargarValorCuota` (registro 3035) se abre con su propio nombre **`CARGAR_VALOR_CUOTA`** en `fondomutuo.solicitud.listas-servidas` y se enruta con su clase en `plataforma.javacall.clases-habilitadas`. Cerrada por defecto: responde 503 G0 **antes** de leer o escribir nada y el router cae a TomEE. El arranque la acepta sola, junto a `NORMAL` y junto a `RESCATE_REINVERSION` aunque la custodia esté cerrada (no cuenta como "otra lista"); un nombre mal escrito (`CARGAR_VALOR_CUOTAS`) impide arrancar. Los nombres en minúsculas SÍ se aceptan (`kit/Compuertas.java:41` los pasa a mayúsculas), pero se documentan y se configuran en MAYÚSCULAS. **No es una lectura**: no está en las ocho lecturas ni en `LISTAS_LECTURA`, y su transacción es de lectura-escritura.

- **Cómo abrirla**: `fondomutuo.solicitud.listas-servidas=NORMAL,CARGAR_VALOR_CUOTA` (más las que ya estén abiertas), reiniciar, y agregar `cl.bcs.fondomutuo.CargarValorCuota` a `clases-habilitadas` del router. **Firma del experto de dominio antes de abrirla** (escribe datos de valorización). Para revertir: quitar la clase del router o el nombre de la propiedad y reiniciar.
- **Qué escribe** (solo con `p_SimularOperacion` exactamente `"N"`; cualquier otro valor simula y no escribe): `FMM.ValorCuotaHistorico` (se marca `EsUltimaCargaDiaria = N` la carga previa de la misma serie, fecha y empresa e inserta la nueva con `S`; el id sale de la secuencia `ValorCuotaHistorico`, que no es transaccional: un lote revertido consume ids) y `FMM.ValorCuota` (borra por serie y deja una fila por serie). Orden fijo: 1 actualizar, 2 insertar historico, 3 borrar vigentes, 4 insertar vigentes, 5 una línea de auditoría.
- **Atomicidad del log (decisión del usuario)**: la clase no hace commit ni rollback; el anfitrión confirma con contador 0 y revierte todo con contador mayor que 0. La línea de auditoría (`AgregarLog`) va por HTTP y el módulo dueño la confirma por su cuenta: **no se revierte con el lote**. Un lote revertido por una fila con error deja la auditoría de sus filas OK sin que esas filas existan (el legacy usaba la misma conexión y la revertía). Un fallo del log (incluido el 503 del router) se traga: la carga se confirma sin auditoría y no hay fallback a TomEE. Un `Error` de la JVM no se traga.
- **Un lote con una sola fila ERR se revierte entero por `/call`**: el contador es mayor que 0, así que las filas OK ya escritas se deshacen; la respuesta es 200 con los mensajes de error. Quien carga debe reenviar el lote completo corregido.
- **Una fecha atrasada pisa la vigente (D-F3A-19)**: `FMM.ValorCuota` queda con la fecha más reciente **del lote** sin compararla con la guardada; cargar una fecha anterior reemplaza el valor vigente por uno más viejo. La advertencia `VALOR_EXISTE_FECHA` es inalcanzable (D-F3A-16): recargar la misma fecha no avisa y deja una fila nueva en el histórico.
- **Modo ISIN (`p_AllFunds = S`)**: el `IDSERIEFONDO` trae un ISIN. **Un ISIN vacío (`''`) carga todas las series sin ISIN** (hay 374 de 1260 en QA, medido de solo lectura). Un ISIN repetido en la entrada lanza (500, rollback); un ISIN sin serie se descarta sin mensaje. En este modo no se valida la moneda.
- **Otros defectos heredados (casos, no corregidos)**: el valor escrito es el de entrada con toda su escala (el truncado por decimales de la serie se descarta, D-F3A-17), el "día anterior" sirve solo para la última fecha del lote (D-F3A-18), valor negativo pasa, valor nulo es NPE, y el binder redondea a 16 decimales HALF_DOWN (D-F3A-23). Empresa sin configuración: `ParametroNoEncontradoException` del kit (el legacy lanzaba NPE).
- **500/503 tras confirmar**: si la auditoría respondió bien, `EscrituraConfirmada` queda marcada y un 500/503 posterior no se repite en TomEE (igual que Normal y RescateReinversion); revisar a mano antes de reintentar.
- **Límite del motor**: HANA redondea al bajar a `DECIMAL(26,8)` distinto de H2 en el último decimal de valores con más de 8 decimales; no verificable sin escribir en HANA.

### 8.1 Operación de CVC tras la revisión de arquitectura (28-oct-2026)

- **Tope del cuerpo**: el router interno de Plataforma (`plataforma.javacall.internal.max-body-bytes`) pasó de 1 MB a **16 MB** (decisión del usuario; un lote de miles de series ya no es 400 en el router). La memoria por petición crece hasta ese tope. Con un lote grande, el **log** (`AgregarLog` por HTTP) lleva una línea y un detalle por fila OK en un solo cuerpo: puede acercarse al tope del router del módulo dueño del log; si el log falla (tope, 503, timeout) se traga y la carga se confirma sin auditoría (ver arriba). El fallo queda en el log del módulo como `WARN CVC audit failed: N rows, exception=<clase>` (sin mensaje ni valores).
- **Pool de 10 conexiones**: una carga mantiene **una conexión y una transacción abiertas** mientras corren los 4 pasos de escritura y la llamada HTTP del log (que va DENTRO de la transacción). Un log lento retiene la conexión y las filas bloqueadas de `FMM.ValorCuota`/`FMM.ValorCuotaHistorico`. No hay timeout propio del log ni de la transacción de CVC: se mide en QA antes de fijarlos.
- **Un solo vuelo por instancia (decisión del usuario)**: `fondomutuo.valorcuota.max-cargas-simultaneas` (por defecto **1**) y `fondomutuo.valorcuota.espera-ms` (por defecto **2000**). Una segunda carga que no obtiene el permiso en ese plazo responde **503 con compuerta `CVC_OCUPADA`** sin haber leído ni escrito nada; el router la repite en TomEE. El permiso se libera siempre (también con excepción o contador mayor que 0). **Solo serializa dentro de la JVM**: con varias instancias de FondoMutuo hace falta un lock de base de datos (no implementado). Con 2 o más instancias, dos cargas de la misma serie pueden cruzarse (el orden de bloqueo por `IdSerieFondo` reduce, no elimina, el riesgo de interbloqueo; probar con 2 sesiones en QA).
- **Zona horaria**: la aritmética de fechas heredada y las fechas de las filas asumen la zona del contenedor, **UTC**. El arranque deja un `WARN` si la JVM no está en UTC y hay una lista de lectura **o de escritura** abierta, y un `INFO UI WRITE open: CARGAR_VALOR_CUOTA` cuando CVC está abierta. No impide arrancar.
- **Transacción exigida**: las escrituras de CVC exigen una transacción Spring activa y no de solo lectura (`fondomutuo.valorcuota.exigir-transaccion`, por defecto `true`); sin ella `IllegalStateException` (500) antes de tocar la base. Solo el arnés de pruebas la desactiva.
- **Supuesto de identidad**: `p_IdUsuario` y `p_IdEmpresa` del cuerpo **no se contrastan con el llamador** (igual que el legacy); la única barrera es el token interno del router. `IdCabeceraLog = 159` es constante. Cualquier cliente con el token interno puede cargar valores a nombre de cualquier usuario y empresa.
- **Magnitud del valor**: un `VALORCUOTA` con más de 38 dígitos enteros o con `abs(escala)` mayor que 100 se rechaza (500 genérico, sin el valor en la respuesta) antes de cualquier lectura; los valores de uso normal (escala hasta 16 y más) no cambian.
- **Registro por escritura**: una línea `INFO CVC write: correlation=... empresa=... series=N rows=M ids=a..b auditLog=ok|failed|none errors=K` por petición de escritura (solo recuentos; sin series ni valores).
- **Reintento tras un timeout del cliente**: **revisar `FMM.ValorCuotaHistorico` antes de reintentar**. No hay clave de idempotencia y recargar la misma fecha agrega otra fila al histórico. El router **no reintenta** con 500 ni con timeout; solo con 503 o `ConnectException`. Un cliente que se queda sin respuesta no sabe si la carga se confirmó.
- **Lote en un solo `PreparedStatement`**: las cuatro escrituras corren como lote JDBC (tramos de 1000 filas) con el plazo restante de la transacción. Con un fallo en una fila, H2 sigue ejecutando las filas posteriores del lote (como el legacy); la transacción se revierte igual. No medido con 5000 filas en HANA: pendiente de QA.
