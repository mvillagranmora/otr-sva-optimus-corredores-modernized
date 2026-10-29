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
