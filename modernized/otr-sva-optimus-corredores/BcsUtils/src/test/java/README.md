# BcsUtils: characterization tests

These tests **define done** for BcsUtils, scope A (a pure common library). They pin what the legacy
`legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA/BcsUtils` actually does. The pins come from golden records
of the unmodified legacy, run locally. They also pin the approved deviations: RULE-025, RULE-032, UTIL-56 and RULE-031 for passwords of 128+ characters.

## Running

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"; export PATH="$JAVA_HOME/bin:$PATH"
mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl BcsUtils clean test
```

- **Plain library.** There is no Spring context. JUnit 5 + AssertJ.
- **Counts.** Each golden-driven class prints `equivalence cases executed [<area>]: N`.
- **Inventory guard.** `AlcanceLibreriaTest.util91_inventarioGolden` fails if any fixture is missing or has a different number of cases. A missing oracle is a failure, never a skip.
- **No skips.** There is no `@Disabled` and no assumption-skip. The few golden cases that cannot be compared through the public API are excluded by an explicit id list (`NO_APLICA`). That list is asserted to exist in the fixture and is printed.
- **Time zone and locale.** Zone-dependent cases run each fixture directory under its JVM default zone (`America/Santiago`, `UTC`) and under the oracle's Locale (`es-CL`). See `Golden.enZona`. Surefire runs classes sequentially; keep it that way.
- **Dual-execution dump.** `-Dequivalence.out=<dir>` writes `<dir>/legacy|new/<zone>/<area>/<id>.json` for every compared case.

## Target API (what `src/main` must provide)

Package `cl.bcs.utils`. The legacy names and signatures are kept unless the entry says otherwise. There is no `main()` anywhere.

| Class | Members |
|---|---|
| `RutUtil` | `static boolean validar(String)` |
| `StringUtils` | `join(String, List<String>)`, `isNullOrBlank(String)` |
| `Utils` (pure subset only) | `obfuscateEmail`, `emailValidate`, `codificarEvento`, `decodificarEvento`, `appendKeys(String...)`, `generateToken(int)`, `generateAlphanumericToken(int)`, `printJson(Object, boolean)`, `getGsonBuilder()`, `diferenciaEnDias(Date, Date)`, `List2Hash`, `getColumn`, `List2ListHashGenericObject`, `copyPojo(src, target[, restrictDate])` (all `throws Exception` as legacy), `fileToZipFile(String, String, boolean)`, `touch(File, long)` |
| `BcsPojo` | `toString()` = `Utils.printJson(this, false)` |
| `StreamUtils` | `parallelFlatmap` |
| `DateUtil`, `DateFormatException` | All legacy statics. `DateFormatException extends RuntimeException`. |
| `Fecha` | All legacy statics and constants. |
| `Timer` | Legacy fluent API, plus `static Timer createTimer(LongSupplier clockMillis)`. `setLogger(org.slf4j.Logger)`: `endMetric` logs at trace, `printMetrics` at debug. The clock is read once per `addMetric` and once per `endMetric`. |
| `FileUtils` | `createFolder(File, String)`, `encodeBase64String(File)` |
| `ZipUtil`, `BadFilePathException` | `generateZip`, `generateMultipleZip`, `getFirstFileEntry`, `isRar` |
| `GsonDateDeserializer`, `GsonTimestampDeserializer` | Lenient parsing. Return `null` on a parse error. |
| `JavaCall`, `JavaCallExtra`, `JavaUtil`, `JavaCallMetadata` | Legacy contract. `call(java.sql.Connection, JsonObject) throws Exception`. |

`JsonWebToken` is **not ported** (decision 5, 2026-10-16): it is dead code in the legacy. `AlcanceLibreriaTest` requires it to be absent; its test and its `jwt.json` goldens were removed.

Package `cl.bcs.utils.clave` (RULE-031..034):

- `record PoliticaClave(long minLongitud, long minMayusculas, long minMinusculas, long minCaracteres, long minDigitos)`
  - Constants `ACC_NUM_LONGITUD`, `ACC_NUM_MAYUSCULAS`, `ACC_NUM_MINUSCULA`, `ACC_NUM_CARACTERES`, `ACC_NUM_DIGITOS = "ACC.numDigitos"`, `MIN_DIGITOS_POR_DEFECTO = 1`.
  - `static desdeConfiguracion(Map<String,String>)`: `Long.parseLong` of each raw value. A missing `ACC.numDigitos` means 1.
- `final class ValidadorClave`: `static ResultadoValidacion validar(String clave, PoliticaClave p, Predicate<String> esProhibida)`.
- `record ResultadoValidacion(boolean claveValida, long erroresContador, List<MensajeSalida> erroresDetalle, List<EstadoCriterio> estadoCriterios)`.
- `record MensajeSalida(long id, String tipo, String codigo, String descripcion)`.
- `record EstadoCriterio(String criterio, boolean esValido)`.

Package `cl.bcs.utils.sesion` (RULE-021, 024, 025, 071):

- `TokenSesion.raw(Long, String, Timestamp, UUID)` and `TokenSesion.generar(...)` (Base64, UTF-8).
- `PoliticaInactividad`:
  - `IDLE_MAX_POR_DEFECTO_MS = 1_800_000`
  - `canalEfectivo(String)`
  - `Optional<String> codigoParametro(String)`
  - `long idleMaxMs(String canal, String valorConfigurado)`
  - `boolean sesionExpirada(String canal, long ahoraUtcMs, long ultimoUsoUtcMs, long idleMaxMs)`
  - `boolean actualizaUltimoUso(String canal)`
- `RelojUtcCacheado(LongSupplier relojMs, Callable<Timestamp> consultaUtc)`: `Timestamp ahoraUtc() throws Exception`, `VIGENCIA_MS = 3_600_000`.

Kept unchanged, because other modules consume them: `cl.bcs.utils.security.HybridPasswordEncoder` and
`cl.bcs.utils.report.CrystalReportRequest` / `CrystalReportsPort` / `CrystalReportsRestClient`.

The library needs no dependency beyond the current `pom.xml`: gson, commons-codec, commons-io, spring-web, and SLF4J via spring-boot-starter-web.

## Rule ids

- `RULE-NNN` ids come from `analysis/otr-sva-optimus-corredores/BUSINESS_RULES.md`.
- `UTIL-NN` ids cover behavior without a business rule; each test cites the legacy `file:line` in its Javadoc.
- **Approved deviations:**
  - **RULE-025:** COR is no longer exempt from the idle timeout.
  - **RULE-032:** a separate digit minimum, default 1.
  - **UTIL-56** (decision 4, 2026-10-16): `getFirstFileEntry` returns the first file entry; the legacy hung when the first entry was a directory (`ZipUtil.java:118-119`). Golden ZIP-027 is excluded from equivalence and asserted as the deviation.
  - **RULE-031, passwords of 128+ characters** (decision 4, 2026-10-16): validated normally under the same rules; the legacy threw `StringIndexOutOfBoundsException` because its scan loop counter was a `byte` (`ValidarNuevaClave.java:164`). The 10 golden cases recording the crash (PASSWORD-065..068, 109, 110, 147, 148, 185, 186) are excluded from equivalence and asserted as the deviation in `PoliticaClaveTest.rule031_clavesLargasDesviacionAprobada`.

## Adding a case

1. Add the input to the matching section of the oracle: `<scratchpad>/bcsutils/oracle/runner/oracle/*Oracle.java`.
2. Recompile it with JDK 11 against `classes` and `classpath.txt`, and run `oracle.Main <out> ANY|DB|America/Santiago|UTC` with `-Dfile.encoding=UTF-8 -Duser.language=es -Duser.country=CL -Duser.timezone=<zone>`.
3. Run `py -3 publish.py`. It refuses any record holding a legacy secret literal or a local path.
4. Map the new input in the test's `moderno(Caso)` switch. An unmapped `fn` fails with `AssertionError`.
5. Update the expected counts in `AlcanceLibreriaTest.util91_inventarioGolden`.

Never put a credential, key or token from the legacy into a fixture or a test. Use fake values of the same shape, such as `FAKE_KEY_1`.
