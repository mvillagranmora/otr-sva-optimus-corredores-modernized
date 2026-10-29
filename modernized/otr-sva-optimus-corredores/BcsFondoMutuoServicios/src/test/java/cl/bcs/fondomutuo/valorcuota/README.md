# Tests de caracterizacion de CargarValorCuota (F3 W2, Step 1: rojo antes de codigo)

Clase legacy `cl.bcs.fondomutuo.CargarValorCuota` (registro 3035, tarjetas FM-CVC-01..14 de `analysis/otr-sva-optimus-corredores/RULES_FONDOMUTUO_F3_A.md` 8.8; defectos D-F3A-16..23).
Es la UNICA clase de la ola W2 y la unica de las lecturas de UI que ESCRIBE (`FMM.ValorCuotaHistorico`, `FMM.ValorCuota` y una linea de auditoria por `AgregarLog`).
**Estas pruebas no compilan hasta que exista el paquete de produccion `cl.bcs.fondomutuo.valorcuota`** (ver `API_ASSUMED.md`: nombres, constructores y puertos exactos): es el estado "rojo".
No hay codigo de produccion en `src/main`; el legacy y el oraculo compartido no se tocaron.

## Que compara (el legacy es el oraculo)

Los goldens (`src/test/resources/fondomutuo-cvc/cases/<id>/expected.json`) los grabo el legacy SIN MODIFICAR (`CargarValorCuota` real, `RevisarRequisitosBasicos`, `ObtenerDiaAnterior`, el
binder real de `SQL.batch`) sobre H2 en memoria con datos sinteticos, con un unico doble registrado (Plataforma `AgregarLog`, que traga el fallo de su escritura como el legacy,
`AgregarLog.java:239-241`). Como la clase escribe, cada caso compara MAS que la salida:

| Seccion del volcado | Que fija |
|---|---|
| `salidaCruda` / `excepcion` | texto exacto (orden de campos, escala de cada numero, fechas `yyyy-MM-dd HH:mm:ss.SSS`); la clase de la excepcion (el mensaje solo si es `Exception` plana) |
| `decisionServlet` | `COMMIT` solo con contador 0; `ROLLBACK` con contador > 0; `ROLLBACK_HTTP500` ante una excepcion (`CallServlet:441-456,495`: la clase no hace commit ni rollback) |
| `tablasIniciales`, `tablasTrasLaLlamada`, `tablas` | tablas ANTES, justo DESPUES de que la clase retorna (lo que escribio antes de que el anfitrion decida: un lote mixto ya escribio sus filas OK) y DESPUES de la decision |
| `colaboradores` | las llamadas a `AgregarLog`: argumentos completos (con los `null` que el legacy no llena) y respuesta |
| `secuenciasSolicitadas` | `{secuencia, cantidad}` de los ids del historico (el legacy los toma inline con `NEXTVAL`; el oraculo lo registra igual que el puerto de secuencias) |
| `previas` | peticiones anteriores de la MISMA sesion con su propia decision (repetir una carga) |
| `sesion` | tablas locales sin DROP y commits autonomos (ninguno: la clase no usa bloqueos) |

## Archivos

| Archivo | Papel |
|---|---|
| `CasoCvc` | arma el H2 del caso (`fondomutuo-3a/` + `fondomutuo-cvc/`), ejecuta el codigo NUEVO por su entrada JavaCall y devuelve el mismo volcado que el oraculo. Un golden ausente FALLA |
| `SujetoCvc` | cablea la implementacion nueva sobre los adaptadores JDBC reales; `AgregarLog` lo responde el guion del caso (`Guion`); ganchos de prueba `logPrueba` y `orden` |
| `CvcEquivalenciaTest` | los 199 casos contra su golden; imprime `equivalence cases executed: N of 199` y falla si N = 0 o si algun caso no se comparo |
| `CvcDeterminismoTest` | N = 20 corridas (`-Dcvc.corridas=N`) de los casos con varias filas, modo ISIN o peticiones previas: cada corrida igual al golden |
| `CvcReglasTest` | cada tarjeta y defecto con valores LITERALES (incluye una tabla por caso generada de los goldens, `CvcTablasLiterales`) |
| `CvcCompuertaTest` | lista propia `CARGAR_VALOR_CUOTA` cerrada por defecto (503 G0 sin leer ni escribir), lista blanca, no es clase de lectura, Gson nuevo por llamada, contrato JSON |
| `CvcAnfitrionTest` | el anfitrion REAL (`JavaCallController` + `JavaCallUnitOfWorkFilter`, una transaccion Spring sobre H2): contador 0 = commit; lote mixto = 200 con contador 1 y rollback de TODO (las filas OK incluidas); excepcion = 500 generico y rollback; compuerta = 503 sin escribir; repetir la carga por HTTP. Las tablas se leen desde OTRA conexion: lo visible es lo confirmado |
| `API_ASSUMED.md` | la API de produccion que las pruebas suponen |

## Como correrlas

```
# una vez existe el paquete de produccion (el modulo se compila con mvn -o; NO se corre Maven sobre este modulo hasta entonces)
mvn -o test -Dtest='Cvc*Test'            # 6 clases de prueba
mvn -o test -Dtest=CvcDeterminismoTest -Dcvc.corridas=50
```
Zona horaria y locale: el arnes fija UTC y `en` durante cada caso, como se grabaron los goldens.

## Como re-grabar el oraculo y agregar un caso (desde `analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/`)

```
py -3 -I make_cases_cvc.py                 # escribe cases-cvc/FMM-CVC-nnn_slug/{case.json,seed.sql}, schema-cvc.sql y seed/base-cvc.sql (NUNCA los goldens)
legacy-oracle/record_cvc.sh FMM-CVC         # graba expected.json con el legacy sin modificar, 20 corridas por caso (determinism-cvc.txt); un caso inestable queda SIN golden
py -3 -I legacy-oracle/sync_cvc.py          # copia casos, esquema y semilla a src/test/resources/fondomutuo-cvc/ y regenera CvcTablasLiterales.java
```
Un caso nuevo = una linea `caso("slug", ["FM-CVC-nn", "D-F3A-nn"], entrada([vc(...)], ...), seed=[...], colaboradores=..., previas=[...])` en `cases_cvc_a.py` / `cases_cvc_b.py`
(helpers: `vc`, `entrada`, `historico`, `vigente`, `config`, `falla_en`, `log_cae`, `Num("1500.50")` para un numero con su texto exacto). `ref` nombra las reglas que fija el caso.
Un fallo inyectado se hace con un CHECK en la semilla del caso (`falla_en`): falla el paso 1, 2 o 4 de la secuencia de escritura.

## Prueba de que las pruebas pueden fallar

```
py -3 -I canary_cvc.py legacy                       # 20 mutaciones de una copia del legacy: cuantos goldens cambian cada una
py -3 -I canary_cvc.py nuevo <copia-del-modulo>     # mutaciones de la implementacion en una COPIA del modulo FUERA del repo; cada una debe poner rojo al menos una prueba
```

## Lo que no se puede probar aqui (limites declarados)

* Todo lo que dependa de HANA real. YA VERIFICADO contra HANA QA (SELECT de solo lectura, 2026-10-27): tipos, NOT NULL y PK de `FMM.ValorCuota` (PK `IdSerieFondo`, una fila por serie) y de `FMM.ValorCuotaHistorico` (PK `IdValorCuotaHistorico`), columnas de la vista, secuencia (inicio 1, sin ciclo, cache 1), `SerieDeFondo.ISIN` (0 nulos, 374 vacios), 0 duplicados 'S'. SIGUE ASUMIDO: la definicion de la vista, el comportamiento del driver con `executeBatch` parcial.
* LIMITE DEL MOTOR (Q5): al bajar un valor a `DECIMAL(26,8)` HANA redondea `0.123456785` a `0.12345678` (mitad hacia abajo / al par) y H2 a `0.12345679` (HALF_UP). Es una diferencia del motor, no de la clase: los goldens de escala (`escala-mayor-a-16...`, `escala-del-motor-8...`) fijan el comportamiento de H2 y pueden diferir de HANA en el ultimo decimal; no se puede cerrar sin escribir en HANA.
* La atomicidad del log: por HTTP el modulo dueño confirma por su cuenta; los goldens fijan las llamadas, no su efecto remoto.
* Que ruta (`J` / `P`) usa produccion para el servicio (en el registro vivo es `J`, Q-F3A-1).
* `AgregarLog` real: el doble modela solo su parte de escritura (dentro del `try`); una excepcion previa al `try` (preparacion de cabeceras) no esta modelada.
