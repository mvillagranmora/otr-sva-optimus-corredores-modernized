# Tests de caracterizacion de CertificarSolicitudAporteRescate, tanda T1 (F3 W3, Step 1: rojo antes de codigo)

Clase legacy `cl.bcs.fondomutuo.CertificarSolicitudAporteRescate` (`logica` de complejidad 95, 3.052 lineas; tarjetas FM-CER-01..20 y 23 de `analysis/otr-sva-optimus-corredores/RULES_FONDOMUTUO_F3_B.md` 8.1;
defectos D-F3B-01/08/12/16/31, D-NOR-9/16). Esta tanda cubre dos frentes de la seccion 12 de las reglas: **F-A** (cabecera, requisitos, parametros, lecturas, valor cuota, las 8 reglas por solicitud y la
salida temprana, FM-CER-01..14) y **F-B** (indicadores, recalculo de cuotas y montos con el `CalcularMontoNumeroCuota` real, fecha efectiva, particion y orden, `GEN_OCSNEG`, formato de montos,
FM-CER-15..20 y 23). Lo que viene despues (aportes, rescates, UPDATE, notificaciones, switch, logs: T2..T5) NO esta escrito.
**Estas pruebas no compilan hasta que exista el paquete de produccion `cl.bcs.fondomutuo.certificacion`** (ver `API_ASSUMED.md`: nombres, constructores y puertos exactos): es el estado "rojo".
No hay codigo de produccion en `src/main`; el legacy y el oraculo compartido no se tocaron.

## La costura de T1 (por que F-B se puede probar)

F-B no tiene salida propia: sus montos, su particion y su fecha solo se ven en lo que viene despues. La fachada `CertificarAporteRescateService` ejecuta F-A y F-B REALES y, al llegar a los aportes y
rescates, llama al puerto `CertificarAporteRescateFasesPosteriores.continuar(contexto)`; en las pruebas es un doble que REGISTRA el `CertificarAporteRescateContexto` y detiene la corrida con `ParadaT1`.

| Tipo de caso | Que grabo el legacy (sin modificar, sobre H2) | Que compara la prueba |
|---|---|---|
| terminal (sin etiqueta) | la clase termina o lanza DENTRO de F-A / F-B (requisitos, lista vacia, duplicados, nada leido, parametro faltante, excepcion de una lectura o del calculo, **ninguna solicitud valida = salida temprana FM-CER-14**: log y contador 1) | el volcado COMPLETO, seccion por seccion (salida, excepcion, decision del servlet, tablas antes / justo despues de la llamada / tras la decision, colaboradores, secuencias, previas, sesion) |
| `-parada` | el mismo caso con el primer colaborador que alcanzan F-C / F-D / F-J (`SaldoFlujoCaja`, `CalculaCustodiaRescate`, `ProcesarCustodiaFondoMutuo`, `NotificarEstadoOrdenFondoMutuo`) programado para fallar con `PARADA_T1`: prefijo de la lista de colaboradores con sus argumentos y tablas ANTES de cualquier escritura | el prefijo de llamadas de F-A / F-B, tablas intactas, sin secuencias, y que el contexto coincide con los ARGUMENTOS del colaborador de parada (pares y fecha del flujo de caja, rescates con sus montos) y con CUAL es ese colaborador |
| `completo` | el mismo caso con colaboradores benignos: la corrida entera del legacy (salida, UPDATE a CRT, cada entrada a colaboradores) | que el contexto coincide con los codigos y estados de la salida, el orden de las filas, los montos que escribio el UPDATE (con el formato de FM-CER-23), los aportes enviados a custodia, los rescates enviados a custodia y los ids notificados |

Cada caso que continua se graba DOS veces con la misma semilla y entrada (`<slug>` y `<slug>-parada`). El golden `completo` sirve despues a T2..T5 como golden de la corrida entera.

## Que compara (el legacy es el oraculo)

Los goldens (`src/test/resources/fondomutuo-cer/cases/<id>/expected.json`) los grabo el legacy SIN MODIFICAR (`CertificarSolicitudAporteRescate`, `AgregarLogCertificacionAporteRescate`, `CalcularMontoNumeroCuota`,
`CalcularComisiones`, `RevisarRequisitosBasicos`, el binder real de `SQL.query` / `SQL.batch`) sobre H2 en memoria con datos sinteticos y 20 dobles registrados generados de las fuentes reales
(`EntregarPreciosTasas`, `CalcularParidad`, `SaldoFlujoCaja`, `AgregarLog`, `NotificarEstadoOrdenesOWP`, `ProcesarCustodiaFondoMutuo`, `CalculaCustodiaRescate`, `CrearCargoAbono`, `CrearIngresoEgreso`,
`ProcesarIngresoEgresoTesoreria`, `ProcesarIngresoOperacionSpot`, `CrearOperacionSpot`, `CrearMovimientoComprobante`, `CrearComprobante`, `CrearSolicitudAporteReinversion`,
`NotificarEstadoOrdenFondoMutuo` y los tipos de `ResumenPatrimonial`). 20 corridas por caso (`determinism-cer.txt`); un caso inestable queda SIN golden.

## Archivos

| Archivo | Papel |
|---|---|
| `CasoCer` | arma el H2 del caso (`fondomutuo-3a/` + `fondomutuo-cer/`), ejecuta la fachada nueva por su entrada JSON y devuelve el volcado del oraculo MAS el contexto de la costura. Un golden ausente FALLA |
| `SujetoCer` | cablea la fachada sobre los adaptadores JDBC reales; `EntregarPreciosTasas` y `CalcularParidad` los responde el guion del caso (`Guion`); gancho `envolverEscritura` |
| `ParadaT1` | la parada conocida de la costura |
| `CerCostura` | la comparacion de un caso que continua (contexto contra el golden `completo` / `parada`); incluye las funciones "consumidoras" (pares de `SaldoFlujoCaja`, rechazo por saldo, primer colaborador) citadas con su linea del legacy |
| `CerEquivalenciaTest` | los casos contra su golden; imprime `equivalence cases executed: N of M` y falla si N = 0 o si algun caso no se comparo |
| `CerDeterminismoTest` | N = 20 corridas (`-Dcer.corridas=N`) de los casos con varias solicitudes o peticiones previas |
| `CerReglasTest` | cada tarjeta y defecto con valores LITERALES, mas las reglas puras de F-B (`CertificarAporteRescateReglas`) y la comprobacion del indice generado (`CerTablasLiterales`) |
| `CerCompuertaTest` | la clase NO se sirve en T1 (sin lista blanca, sin handler); G6 / G8 marcadas `@Disabled("pending ...")`; Gson nuevo por llamada; sin estado estatico; los 11 SQL byte a byte; solo se escribe el log de la salida temprana; RULE-076 (sin ids ni credenciales reales en los fixtures) |
| `API_ASSUMED.md` | la API de produccion que las pruebas suponen y la definicion exacta de la costura |

## Como correrlas

```
# una vez existe el paquete de produccion (el modulo se compila con mvn -o; NO se corre Maven sobre este modulo hasta entonces)
mvn -o test -Dtest='Cer*Test'                       # 4 clases de prueba
mvn -o test -Dtest=CerDeterminismoTest -Dcer.corridas=50
```
Zona horaria y locale: el arnes fija UTC y `en` durante cada caso, como se grabaron los goldens. Java 25, `mvn -o`, `-Duser.timezone=UTC -Duser.language=en` (ya en el pom).

## Como re-grabar el oraculo y agregar un caso (desde `analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/`)

```
py -3 -I make_cases_cer.py              # escribe cases-cer/FMM-CER-nnn_slug[-parada]/{case.json,seed.sql}, schema-cer.sql y seed/base-cer.sql (NUNCA los goldens); verifica el catalogo de HANA contra los stand-ins
legacy-oracle/record_cer.sh FMM-CER      # graba expected.json con el legacy sin modificar, 20 corridas por caso (determinism-cer.txt); un caso inestable queda SIN golden (salida 4)
py -3 -I legacy-oracle/sync_cer.py       # copia casos, esquema, semilla y dobles a src/test/resources/fondomutuo-cer/ y regenera CerTablasLiterales.java
```
Un caso nuevo = una linea `caso("slug", ["FM-CER-nn", "D-F3B-nn"], entrada([1001, ...]), seed=[rescate(1001, ...), aporte(1002, ...), config(...), param(...)], colaboradores=..., modo="continua")` en
`cases_cer_a.py` (F-A) o `cases_cer_b.py` (F-B). `modo="continua"` graba las dos variantes; el modo por defecto es terminal. Ayudantes: `rescate` / `aporte` / `sol` (la fila de la tabla y la de la vista
de la solicitud), `solo_tabla` (una solicitud relacionada que no va en la peticion), `config` / `param` (parametros PLATAFORMA / FONDOMUTUO), `precios` / `precio` (respuesta de `EntregarPreciosTasas`),
`saldo_flujo`, `indicador_portafolio`, `opera_saldo_negativo`, `comprobante_no_liquidado`, `Num("1234.50")` (un numero con su texto exacto). `ref` nombra las reglas que fija el caso.

## Prueba de que las pruebas pueden fallar

```
py -3 -I canary_cer.py legacy                       # 25 mutaciones de una copia del legacy: cuantos goldens cambia cada una
py -3 -I canary_cer.py nuevo <copia-del-modulo>     # mutaciones de la implementacion en una COPIA del modulo FUERA del repo; cada una debe poner rojo al menos una prueba
```

## Lo que no se puede probar aqui (limites declarados)

* Todo lo que dependa de HANA real: tipos, `NOT NULL` y claves de `FMM.SolicitudAporteRescate` y de su vista estan verificados contra el catalogo de HANA QA (`CATALOGO_HANA_W3.txt`, 2026-10-29) solo en las
  60 + 60 primeras columnas (el catalogo parece cortado en 60 filas: faltan `FolioContrato`, `IdTipoCanalIngreso`, `Switch`, `FechaValorCuota`, `EstadoSolicitud`, `IdMonedaFondo`, `IdOmnibus` ... que el SQL usa).
  SIGUEN ASUMIDOS: la definicion de la vista, `IdOmnibus` (nulo o 0 sin omnibus), la vista de comprobantes de Tesoreria, la vista de indicadores de portafolio, `COM.Cobro`, la tabla del log.
* LIMITE DEL MOTOR: H2 tipa como INTEGER el parametro de `COALESCE(:ParidadFechaCertificacion, 0)` y de `CASE ... THEN :Spread ELSE 0`, y los redondea a enteros al escribir el UPDATE; HANA conserva los
  decimales. Los goldens fijan el comportamiento de H2 (el codigo nuevo corre el mismo SQL sobre el mismo H2) y T1 no compara esas dos columnas.
* El orden de filas sin `ORDER BY` (las lecturas ordenan solo por `TipoSolicitud`; el primer indicador `GEN_OCSNEG`, el primer comprobante, la primera persona TGR / SII) es el del motor: H2 devuelve el orden de
  insercion; HANA no lo garantiza.
* El comportamiento real de los colaboradores no portados (custodia, cuenta de inversion, Tesoreria, Spot, Facturacion, notificaciones): solo dobles registrados con respuestas benignas.
* La parte de F-C / F-D / F-J que decide el primer colaborador y el rechazo por saldo esta reescrita en `CerCostura` solo para LEER la costura; T2..T5 la implementan y la fijan.

---

# Tanda T2 (F3 W3, Step 1: rojo antes de codigo): frentes F-C (aportes) y F-D (rescates: custodia y costos)

Mismas reglas de la seccion 12 de `RULES_FONDOMUTUO_F3_B.md`: **F-C** = FM-CER-21, 22, 24..28 (saldo de la cuenta de inversion PAC / no PAC, costo UF de la reinversion, custodia de ingreso, cartera y rentabilidad real; el formato
FM-CER-23 de los aportes se ve justo despues del saldo) y **F-D** = FM-CER-31..41 (custodia disponible, rescate total, GeneraSpot, traspaso, retencion APV, comision, retiro de custodia, rescate sin cartera, costo UF / spread /
rentabilidad). Defectos D-F3B-01/03/05/06/08/09/10/11 y el nuevo D-F3B-37 (notas, "F3 W3 T2 (pruebas)"). La API que suponen las pruebas esta en `API_ASSUMED_T2.md`; T1 (`API_ASSUMED.md`) no se toco.
**No hay codigo de produccion de T2 en `src/main`**: las pruebas COMPILAN (las clases nuevas se alcanzan por reflexion en `ApiT2`) y FALLAN con `T2 not implemented: the production class ... does not exist yet`.

## La costura de T2

El gancho de T1 (`CertificarAporteRescateFasesPosteriores`, justo despues de F-B) no cambia. El arnes (`CasoCerT2`) corre la fachada de T1 REAL y, en el gancho, ejecuta los CINCO pasos de F-C y F-D sobre el
`CertificarAporteRescateContexto` (`validarSaldo`, `prepararRescates`, `recalcularCostoUF`, `ingresarCustodia`, `retirarCustodiaYCostear`: el orden del legacy, que intercala los dos frentes y deja entre el tercero y el
cuarto la cuenta de inversion de los aportes, frente E) y despues lanza `ParadaT2`. Los colaboradores no portados (`SaldoFlujoCaja`, `CalculaCustodiaRescate`, `ProcesarCustodiaFondoMutuo`) quedan detras de puertos (los dos
ultimos nuevos, sin implementacion de produccion: compuerta `G-CUS` cerrada) y los responde el guion del caso, como los dobles registrados del oraculo.

| Tipo de caso | Que grabo el legacy (sin modificar, sobre H2) | Que compara la prueba |
|---|---|---|
| `T2-costura:completo` | la corrida entera con colaboradores benignos despues de F-D | las llamadas a los colaboradores de F-C / F-D (nombre, argumentos COMPLETOS, respuesta, orden; numeros por valor), la salida (estado, codigo, descripcion, contador, mensajes), las columnas del UPDATE a CRT y el pojo que copian Tesoreria / Spot (GeneraSpot, RelacionTraspaso, retencion, montos con decimales exactos) |
| `T2-costura:excepcion` | el legacy LANZA dentro de F-C / F-D (NPE del legacy, un colaborador que falla) | la misma clase de excepcion (el mensaje si es `Exception` simple) tras las mismas llamadas |
| `T2-costura:excepcion-posterior` | termina F-C / F-D y lanza en un frente posterior (UPDATE, Tesoreria) | F-C / F-D terminan sin error y hacen las mismas llamadas |

## Archivos nuevos

| Archivo | Papel |
|---|---|
| `ApiT2` | el UNICO sitio que conoce los nombres de la API de produccion (reflexion); falla con "T2 not implemented" |
| `CasoCerT2` | arma el H2 del caso (`fondomutuo-cer/cases-t2/`), corre T1 + los cinco pasos y devuelve contexto, llamadas y tablas |
| `ParadaT2` | la parada conocida de la costura T2 |
| `CerCosturaT2` | la comparacion de un caso contra su golden (el lente descrito arriba) |
| `CerT2EquivalenciaTest` | los casos contra su golden; imprime `equivalence cases executed: N of M` y falla si N = 0 o si algun caso no se comparo |
| `CerT2DeterminismoTest` | N = 20 corridas (`-Dcer.corridas=N`) de los casos con varias solicitudes |
| `CerT2ReglasTest` | cada tarjeta y defecto con valores LITERALES leidos del golden (una linea marca las pocas reglas que el golden no muestra) |
| `CerT2CompuertaTest` | puertos de un solo metodo, sin implementacion de produccion, G3 / G-CUS cerradas por defecto, unidades sin escritura ni estado, integridad de la evidencia (proveniencia, matriz, etiquetas, numeracion, RULE-076) |
| `API_ASSUMED_T2.md` | la API de produccion supuesta y la definicion de la costura |

## Como correrlas y como re-grabar el oraculo (desde `analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/`)

```
mvn -o test -Dtest='CerT2*Test'                     # 4 clases; hoy en rojo por ausencia de implementacion
py -3 -I make_cases_cer_t2.py                       # escribe cases-cer-t2/FMM-CER-199_slug/{case.json,seed.sql} y seed/base-cer-t2.sql (NUNCA los goldens)
legacy-oracle/record_cer_t2.sh FMM-CER              # graba expected.json con el legacy sin modificar, 20 corridas por caso (determinism-cer-t2.txt); un caso inestable queda SIN golden (salida 4)
py -3 -I legacy-oracle/check_cer_t2.py              # la etiqueta de cada caso concuerda con su golden; todos estables
py -3 -I legacy-oracle/sync_cer_t2.py               # copia casos, semilla y reporte a src/test/resources/fondomutuo-cer/
```
Un caso nuevo = una llamada `caso("slug", ["FM-CER-nn", "D-F3B-nn", "matriz:Cnn"], entrada([3001, ...]), seed=[...], custodia=..., saldo=..., calcula=..., paridad=[...], colaboradores=..., excepcion=False|True|"posterior",
front="F-C"|"F-D")` en `cases_cer_t2_c.py` / `cases_cer_t2_d.py` (los ayudantes `ap`, `reinv`, `origen`, `mov`, `err`, `resp_custodia`, `res_custodia`, `calcula_custodia`, `saldo_por_uso`, `paridad_uf`, `paridad_utm`,
`paridad_comision` estan en `cases_cer_t2.py`). Los casos nuevos se agregan AL FINAL para no renumerar.

## Lo que no se puede probar aqui (limites declarados, ademas de los de T1)

* `Spread` y `ParidadFechaCertificacion`: H2 tipa como INTEGER el parametro de `CASE ... THEN :Spread ELSE 0` / `COALESCE(:Paridad, 0)` y los redondea al escribir el UPDATE; HANA los conserva. La prueba compara el `Spread` del UPDATE con
  tolerancia de 0,5 y el EXACTO en el pojo que reciben Tesoreria / Spot (por eso los rescates de los casos de F-D se pagan por un medio distinto de `CIN`).
* El centinela de fecha `0001-01-01` (`FechaOriginalInversion`) se lee en H2 como `0001-01-03` (cambio de calendario juliano / gregoriano): la comparacion del legacy con `"0001-01-01"` no lo reconoce bajo H2; HANA depende del driver.
  Dos casos fijan ambas ramas (el segundo siembra `0000-12-30`, que H2 imprime como `0001-01-01`).
* El comportamiento real de `CalculaCustodiaRescate` y `ProcesarCustodiaFondoMutuo` (custodia), `SaldoFlujoCaja`, `CalcularParidad`: solo dobles con respuestas guionadas por caso.
* `parallelStream` del legacy sobre una misma conexion (D-F3B-06): el orden lo fija cada elemento y los 20 repeticiones salieron identicas; la carrera de la conexion compartida no es reproducible aqui.
* Lo que el golden no muestra en ninguna llamada ni columna del UPDATE (p. ej. `TieneMontoRetencion` de un rescate que se paga a la cuenta de inversion, `RelacionTraspaso` de un rescate rechazado por custodia): `CerT2ReglasTest` lo fija con el
  valor de la regla y lo marca "(rule; the golden does not show it)".

---

# Tanda T3 (F3 W3, Step 1: rojo antes de codigo): frentes F-E (cuenta de inversion), F-F (Tesoreria) y F-H (Spot)

Mismas reglas de la seccion 12 de `RULES_FONDOMUTUO_F3_B.md`: **F-E** = FM-CER-29, 30 (cargos y abonos de los aportes y sus errores) y FM-CER-42, 43 (los de los rescates); **F-F** = FM-CER-44..47 (seleccion de rescates y traspasos,
retencion y pago al SII, bonificacion fiscal / TGR, lectura del resultado); **F-H** = FM-CER-59, 60 (pojo y formas de pago de Spot, lectura del resultado). Defectos D-F3B-01/03/07/08/09/14/17 y los nuevos D-F3B-41..44 (notas, "F3 W3 T3
(pruebas)"). La API que suponen las pruebas esta en `API_ASSUMED_T3.md`; T1 y T2 no se tocaron. **No hay codigo de produccion de T3 en `src/main`**: las pruebas COMPILAN (las clases nuevas se alcanzan por reflexion en `ApiT3`) y FALLAN
con `T3 not implemented: the production class ... does not exist yet`.

## La costura de T3

El gancho de T1 no cambia. El arnes (`CasoCerT3`) corre la fachada de T1 REAL y, en el gancho, ejecuta los NUEVE pasos de F-C..F-H sobre el `CertificarAporteRescateContexto`, en el orden del legacy: los cinco de T2 (YA implementados, REALES) mas
`moverAportes` (entre el tercero y el cuarto de T2: despues de la custodia de ingreso), `moverRescates` (despues del retiro de custodia y los costos), `moverTesoreria` y `moverSpot` (al final del bloque de rescates, despues de Facturacion y
reinversion, que NO corren aqui: los dobles benignos del oraculo las responden sin efecto) y despues lanza `ParadaT3`. `CrearCargoAbono` queda detras de un puerto nuevo (`CrearCargoAbonoPort`, JSON, sin implementacion de produccion);
Tesoreria y Spot usan los puertos tipados que el modulo YA tiene (`ProcesosExternos.TesoreriaPort` / `SpotPort`), respondidos por el guion del caso como los dobles registrados.

| Tipo de caso | Que grabo el legacy (sin modificar, sobre H2) | Que compara la prueba |
|---|---|---|
| `T3-costura:completo` | la corrida entera con colaboradores benignos despues de F-H | las llamadas a TODOS los colaboradores de F-C..F-H (nombre, argumentos COMPLETOS, respuesta, orden), la salida (estado, codigo, contador, mensajes) y las columnas del UPDATE a CRT, incluidas `IdIngresoEgreso` e `IdOperacionSpot` |
| `T3-costura:excepcion` | el legacy LANZA dentro de F-C..F-H (NPE de un id que no existe, un colaborador que falla, un parametro ausente) | la misma clase de excepcion (el mensaje si es `Exception` simple) tras las mismas llamadas |

## Archivos nuevos

| Archivo | Papel |
|---|---|
| `ApiT3` | el UNICO sitio que conoce los nombres de las clases nuevas de produccion (reflexion); falla con "T3 not implemented" |
| `CasoCerT3` | arma el H2 del caso (`fondomutuo-cer/cases-t3/`), corre T1 + los nueve pasos y devuelve contexto, llamadas y tablas |
| `ParadaT3` | la parada conocida de la costura T3 |
| `CerCosturaT3` | la comparacion de un caso contra su golden (una sola normalizacion: la llave `p_DatosOrigenInversion` vacia de la entrada de Spot, que el pojo tipado del modulo no declara) |
| `CerT3EquivalenciaTest` | los casos contra su golden; imprime `equivalence cases executed: N of M` |
| `CerT3DeterminismoTest` | N = 20 corridas (`-Dcer.corridas=N`) de los casos con varias solicitudes |
| `CerT3ReglasTest` | cada tarjeta y defecto con valores LITERALES leidos del golden, proyectados igual para todos (lineas de la cuenta, entradas de Tesoreria, formas de Spot, estados, columnas del UPDATE) |
| `CerT3CompuertaTest` | puerto de un solo metodo y sin implementacion de produccion, G1 / G2 cerradas por defecto, `CrearCargoAbono` inalcanzable desde el modulo, unidades sin escritura ni estado, integridad de la evidencia |
| `API_ASSUMED_T3.md` | la API de produccion supuesta y la definicion de la costura |

## Como correrlas y como re-grabar el oraculo (desde `analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/`)

```
mvn -o test -Dtest='CerT3*Test'                     # 4 clases; hoy en rojo por ausencia de implementacion
py -3 -I make_cases_cer_t3.py                       # escribe cases-cer-t3/FMM-CER-378_slug/{case.json,seed.sql} y seed/base-cer-t3.sql (NUNCA los goldens)
legacy-oracle/record_cer_t3.sh FMM-CER              # graba expected.json con el legacy sin modificar, 20 corridas por caso (determinism-cer-t3.txt); un caso inestable queda SIN golden (salida 4)
py -3 -I legacy-oracle/check_cer_t3.py              # la etiqueta de cada caso concuerda con su golden; todos estables; ninguno sale en F-A
py -3 -I legacy-oracle/sync_cer_t3.py               # copia casos, semilla y reporte a src/test/resources/fondomutuo-cer/
py -3 -I canary_cer_t3.py <copia-del-modulo>        # mutaciones de una linea de la implementacion de referencia (COPIA fuera del repo)
```
Un caso nuevo = una llamada `caso("slug", ["FM-CER-nn", "D-F3B-nn", "matriz:Cnn"], entrada([3001, ...]), seed=[...], colaboradores=cuenta_script(...) | tesoreria(...) | spot(...), excepcion=False|True, front="F-E"|"F-F"|"F-H")`
en `cases_cer_t3_e.py` / `_f.py` / `_h.py` (los ayudantes `msg`, `resp_cuenta`, `cuenta_script`, `resp_tesoreria`, `rech`, `gen`, `resp_spot`, `op`, `TRAS_APV`, `TRAS_FM` estan en `cases_cer_t3.py`; los de T2 se reusan). Los casos nuevos
se agregan AL FINAL para no renumerar.

## Lo que no se puede probar aqui (limites declarados, ademas de los de T1 y T2)

* El comportamiento real de `CrearCargoAbono` (CuentaInversion), de `ProcesarIngresoEgresoTesoreria` (Tesoreria, G1) y de `ProcesarIngresoOperacionSpot` (Mesa, G2): solo dobles con respuestas guionadas por caso. Lo que SI se fija es todo lo que
  Certificar les ENVIA y la lectura que hace de lo que responden.
* `Switch` y `MontoComisionLocal` nulos (el matriz C18 / C26 los lista como NPE) no se construyeron: el catalogo de HANA QA declara `Switch` `NOT NULL` en la tabla (la vista lo deriva) y `MontoComisionLocal` `NOT NULL` en la tabla y en la vista; un dato real no puede traerlos nulos.
* La rama `VENSPT` de `idTipoOperacionSpot` (aporte de reinversion en USD) es inalcanzable: `mueveSpot` solo recibe los rescates.
* Un parametro `CONCEPTO_RETENCION_PAGO_SII` ausente se lee con `ConfiguracionSistemaPort.texto` y lanza como el legacy (`Parametro ... no encontrado`); en HANA real el parametro existe o la configuracion esta mal.
* Las fechas de las lineas de la cuenta viajan como texto `yyyy-MM-dd`; las de Tesoreria / Spot como `yyyy-MM-dd HH:mm:ss.SSS` (el centinela `0001-01-01` se escribe tal cual, no pasa por la lectura de H2).

---

# T4-B (F3 W3, Step 1: rojo antes de codigo): Certificar F-G (Facturacion) y F-I (reinversion y switch)

Frentes **F-G** (FM-CER-48..51: movimiento de facturacion de la comision, IVA, comprobante automatico; compuerta G6) y **F-I** (FM-CER-52..58, 63, 64: aportes de la reinversion, llamada a AR, aportes de switch,
recursion, monto en moneda de sistema). Casos `FMM-CER-575..754` (180: 75 F-G, 105 F-I; 165 `completo`, 15 `excepcion`) en `src/test/resources/fondomutuo-cer/cases-t4/`, grabados con el legacy ejecutado,
20 corridas por caso, 0 inestables. API supuesta: `API_ASSUMED_T4.md`. Las clases nuevas de produccion se alcanzan por reflexion SOLO desde `ApiT4`: las pruebas compilan sin ellas y fallan con
`T4 not implemented`. T1..T3 no se tocaron.

| Archivo | Que es |
|---|---|
| `ApiT4` | el unico archivo que conoce los nombres de las clases nuevas (`FacturacionPort`, `CrearAporteReinversionPort`, `CertificacionHijaPort`, `CertificarAporteRescateFacturacion`, `...Reinversion`, `...Switch`, `...EscrituraMonto`) y el orden de los pasos de F-C..F-I |
| `CasoCerT4` | arnes: la fachada de T1 real y, en la costura, los pasos de F-C..F-I; AR y la certificacion hija se REPITEN desde el golden (la respuesta que el legacy obtuvo, llamada por llamada); "J-lite" escribe solo `MontoMonedaSistema` entre Spot y la recursion |
| `CerCosturaT4` | la comparacion contra el golden: llamadas con argumentos completos (movimientos de ~107 llaves, aportes de AR, entrada del hijo), salida (los resultados del hijo van primero), columnas del UPDATE y la unica escritura de F-C..F-I (FM-CER-64) |
| `CerT4EquivalenciaTest`, `CerT4DeterminismoTest` | 180 casos; 20 corridas (`-Dcer.corridas=N`) de los 34 con varias solicitudes |
| `CerT4ReglasTest` | GENERADO por `gen_reglas_cer_t4.py` desde los goldens: una prueba por tarjeta y por defecto, valores LITERALES |
| `CerT4CompuertaTest` | forma de los puertos, G6 cerrada por defecto, unidades sin estado, integridad de la evidencia |

```
mvn -o test -Dtest='CerT4*Test'                      # hoy en rojo por ausencia de implementacion
py -3 -I make_cases_cer_t4.py                        # escribe cases-cer-t4/ y seed/base-cer-t4.sql (NUNCA los goldens; borra los directorios anteriores)
legacy-oracle/record_cer_t4.sh FMM-CER               # graba expected.json, 20 corridas por caso (determinism-cer-t4.txt)
py -3 -I legacy-oracle/check_cer_t4.py               # etiquetas vs goldens, estabilidad
py -3 -I legacy-oracle/sync_cer_t4.py                # copia a src/test/resources/fondomutuo-cer/
py -3 -I gen_reglas_cer_t4.py                        # regenera CerT4ReglasTest.java
py -3 -I canary_cer_t4.py <copia-del-modulo>         # mutaciones de la implementacion de referencia (COPIA fuera del repo)
```
IMPORTANTE: tras re-grabar con otra numeracion hay que correr `mvn clean` (Maven no borra de `target/test-classes` los directorios de casos que ya no existen y la guarda de conteo fallaria).
Limites declarados: AR corre como doble guionado (la clase real esta preparada en el oraculo, `"arReal": true`, pero sin semilla suficiente en esta tanda: ver `TRANSFORMATION_NOTES.md`, Q-T4B-1); la certificacion
hija se prueba por su ENTRADA y por la respuesta repetida, no corriendo el hijo; FM-CER-64 no se combina con la recursion en el mismo caso (el hijo escribe filas que el arnes nuevo no escribe).
