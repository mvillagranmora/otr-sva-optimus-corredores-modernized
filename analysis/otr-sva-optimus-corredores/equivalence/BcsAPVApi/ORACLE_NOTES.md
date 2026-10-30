# Oraculo BcsAPVApi (modernize-verify, camino a PROVEN, 2026-10-30): notas

Modulo: contrato tipado `modernized/otr-sva-optimus-corredores/BcsAPVApi` (64 operaciones, 234 records) + mapeadores de `BcsApvJavaCallAdapter` (`mapeo/<Caso>Mapeo`). Lado legacy: las clases REALES `cl.bcs.APV.*` de
`legacy/otr-sva-optimus-corredores/BSCL/APV/JAVA/BcsAPVServicios` (las `Entrada*`/`Salida*` son clases estaticas anidadas de cada operacion). Sin HANA, sin red, sin credenciales. `legacy/` solo se lee.

## Que compara
- **Por defecto (`cases.json`, 128 casos = 64 operaciones x Entrada/Salida)**: la clase legacy instanciada con su constructor sin argumentos y serializada con el Gson que usa su `call()` (55 operaciones `new Gson()`,
  9 el real `Utils.getGsonBuilder()`; el sabor se lee del cuerpo de `call()`), contra `Entrada.porDefecto()` / `Resultado.porDefecto()` pasado por `mapeo.entradaAJson/resultadoAJson` y serializado con el Gson del propio mapeador.
  Comparacion de bytes (sin mascaras, sin tolerancias): orden de claves, nombres legacy, nulos incluidos o no, formato de numeros.
- **Frescas (`fresh-cases.json`, 384 casos)**: entradas que el desarrollo no uso. Para cada operacion y lado (Entrada, Salida) tres perfiles, construidos por reflexion sobre las clases legacy reales (`LegacyApvOracle.entradas`):
  P1 todo poblado (listas de 2, anidados rellenos, fechas, BigDecimal con escalas distintas); P2 limites (`Long.MAX/MIN`, `Double.MIN_VALUE/-0.0/1.0E21`, BigDecimal `1E+3`, `0E-10`, `1.10`, textos vacios, de 300 caracteres, unicode y comillas/`<>&=`, fechas `0001-01-01`/`9999-12-31`/epoch 0,
  listas vacias, anidados nulos); P3 peticion parcial (la mitad de las claves, cada tercera clave en `null`, dos claves que la clase no tiene). El MISMO texto entra a ambos lados: legacy `gson.fromJson(texto, Clase)` + `gson.toJson`, nuevo `mapeo.xxxDesdeJson` + `mapeo.xxxAJson`
  + Gson del mapeador (ida y vuelta JSON legacy -> record -> JSON legacy). Las salidas reales estan en `fresh/legacy/<id>/out.json` y `fresh/new/<id>/out.json`; las entradas en `fresh/input/<id>.json`. Ninguna entrada fue rechazada por el legacy (0 descartadas); ningun caso fresco repite la salida legacy de un caso por defecto.

## Como se corre (repetible)
Desde `analysis/otr-sva-optimus-corredores/equivalence/BcsAPVApi/oracle` con Git Bash (`ORACLE_WORK` = carpeta temporal, `ORACLE_JAVA_HOME` = JDK 21+; `JAVA_HOME` se ignora a proposito):
1. `bash compilar_legacy.sh`      escribe `ops.json` (`listar_operaciones.py`, lee con el lector del generador y relee el Gson de `call()`), compila las 73 fuentes APV (+ lo que referencian por source path) a `$ORACLE_WORK/legacy-build/classes` y el oraculo legacy.
2. `bash correr_legacy.sh defecto "$PWD/../legacy"`; `bash correr_legacy.sh entradas "$PWD/../fresh/input"`; `bash correr_legacy.sh ejecutar "$PWD/../fresh/input" "$PWD/../fresh/legacy"`.
3. `mvn -o compile` de BcsAPVApi y BcsApvJavaCallAdapter (el oraculo nuevo usa sus `target/classes`); `bash correr_nuevo.sh defecto "$PWD/../new"`; `bash correr_nuevo.sh ejecutar "$PWD/../fresh/input" "$PWD/../fresh/new"`.
4. `py -3 -I make_cases.py` escribe `cases.json` y `fresh-cases.json` (rutas relativas a `equivalence/BcsAPVApi`).
5. `py -3 -I <plugin>/scripts/compare.py cases.json --out EQUIVALENCE.json` y `... fresh-cases.json --out FRESH_EQUIVALENCE.json` (desde `equivalence/BcsAPVApi`).
Zona horaria fija `-Duser.timezone=UTC`, `-Dfile.encoding=UTF-8`. La corrida es determinista (sin reloj, sin azar).

## Compilacion de las clases legacy (limites y dobles, nada se oculta)
- Compilar solo la clase externa exige el source path completo de `BSCL/*/src` (BcsUtils, BcsPlataformaServicios, ...) y los jars de `legacy/libs-private` + el repositorio maven local. Las 73 fuentes APV compilan con `--release 11`, JDK 25: 544 clases `cl.bcs.APV.*`. No se omite ninguna clase: `leftOut` de clases = vacio.
- Dos clases NO APV que el source path arrastra no compilan en este JDK/juego de jars y se reemplazan por una COPIA parcheada ubicada primero en el source path (los originales no se tocan; `compilar_legacy.sh` verifica que la diferencia sea exactamente esa):
  `ValorizarWsBCS` (`javax.xml.soap` -> `jakarta.xml.soap`) y `GraficadorPastel` (3 lineas `setBaseSectionOutline*` de una API que jfreechart 1.5 quito). Ninguna `Entrada`/`Salida`, clase APV ni de BcsUtils se modifica.
- `Utils.getGsonBuilder()` es el REAL (BcsUtils compilada del legacy), no una copia.

## Alcance y `leftOut`
- **No cubierto**: la logica `logica(...)` de las 64 operaciones (necesita HANA; pertenece a `BcsAPVServicios`). Esta equivalencia prueba el contrato tipado y el adaptador JSON <-> record, no el resultado de los casos de uso.
- **Registros malformados**: `analysis/otr-sva-optimus-corredores/INTENT.md` NO existe en este espacio de trabajo, asi que nada dice que el comportamiento ante entradas malformadas deba coincidir exacto con sus rarezas; JSON malformado (tipos erroneos, texto truncado) NO se probo. Si; claves desconocidas y nulos (perfil P3).
  Linea para `leftOut` de `equivalence/test-runs.json` (no se edito por instruccion; el coordinador debe copiarla): `BcsAPVApi: registros malformados no probados (no hay INTENT.md que exija igualdad exacta de rarezas); logica(...) de las operaciones fuera del alcance (requiere HANA)`.
- Entrada de clases sin constructor sin argumentos: ninguna (`legacy/index.json`: `entradaOrigen`/`salidaOrigen` = `constructor-sin-argumentos` en las 128; `entradaIgualFromJsonVacio` verdadero en todas).

## Resultado (corrida del 2026-10-30)
- `EQUIVALENCE.json`: 128 casos, 128 iguales, 0 diferencias, 0 faltantes.
- `FRESH_EQUIVALENCE.json`: 384 casos, 380 iguales, 0 diferencias, **4 faltantes = hallazgo F-APV-01**.

### F-APV-01 (CORREGIDO 2026-10-30, decision del usuario)
Hallazgo original: el mapeador rechazaba 4 entradas frescas que el legacy acepta (`F-CrearTraspasoTesoreria-S-P2`, `F-DevolverBonificacionEntidadRecaudadora-S-P2`, `F-DevolverPagoRezagos-S-P2`, `F-TraspasarEntreCuentasBancarias-S-P2`): una lista `List<JsonObject>` de una clase externa de Tesoreria con un elemento `null` (`[{...}, null]`) hacia lanzar `JsonSyntaxException: Expected a JsonObject but was JsonNull` en `MapeoJson.listaEscalar` (adaptador, codigo escrito a mano, no generado).
Correccion: `MapeoJson.listaEscalar` lee la lista como `JsonArray` cuando el tipo es `JsonObject` y deja el elemento nulo como `null` (que se escribe de vuelta como `null`, como el legacy); el resto de las listas escalares no cambia. El lado nuevo se regenero (`correr_nuevo.sh defecto` y `ejecutar`, `compare.py` sobre ambos `cases`): `EQUIVALENCE.json` 128 de 128 iguales y `FRESH_EQUIVALENCE.json` 384 de 384 iguales, 0 diferencias, 0 faltantes. El pin de los 4 ids de `ApvEquivalenciaGoldensTest` se retiro: ahora exige 0 rechazadas.

### VB-V09 (desviacion APROBADA por el usuario el 2026-10-30)
El verify independiente (20261030b) encontro 1 diferencia en 15 entradas frescas propias (399 en total): los 10 campos de payload externo de Tesoreria (`CrearTraspasoTesoreria`, `DevolverBonificacionEntidadRecaudadora`, `DevolverPagoRezagos`, `TraspasarEntreCuentasBancarias`, ...) se llevan como `JsonObject` opaco (CONTRATO.md linea 141), mientras el legacy los deserializa a las clases de Tesoreria y los reescribe con sus valores por defecto (`"Monto":0`, `RequiereAutorizacion:"N"`, `IdTipoCanalIngreso:"OPT"`, `FolioCSC:""`) y con la escala de `BigDecimal` normalizada. Decision del usuario: se aprueba como desviacion de diseno (el servicio de Tesoreria aplica esos valores al tipar el JSON; solo cambia el JSON intermedio de APV). Registrada en `fresh-cases.json` (`approvedInputs`); `FRESH_EQUIVALENCE.json`: 399 casos, 398 iguales, 1 diferencia aprobada, 0 faltantes. Si algun dia APV debe reproducir los valores por defecto de Tesoreria, hay que modelar esas clases en el contrato (queda como pregunta abierta de CONTRATO.md linea 178).
