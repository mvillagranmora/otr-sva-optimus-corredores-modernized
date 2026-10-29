# BcsUtils — Transformation Notes (Step 2)

**Legacy source:** `legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA/BcsUtils/src/cl/bcs/{utils,sql}/`
**Target:** plain Java 25 library (no Spring context), consumed by other reactor modules.
**Definition of done:** the Step 1 characterization tests (commit `c8a9df7`), see `src/test/java/README.md`.
**Scope:** A — pure common library (binding decisions 2026-10-16).

## Mapping

| Legacy | New | Ids |
|---|---|---|
| `RutUtil.java:46-98` | `RutUtil` | UTIL-01 |
| `StringUtils.java` | `StringUtils` | UTIL-10, 11 |
| `Utils.java` (pure subset) | `Utils` | UTIL-12..16, 20, 22, 24..27, 33, 52, 55 |
| `BcsPojo.java` | `BcsPojo` | UTIL-21 |
| `GsonDateDeserializer.java`, `GsonTimestampDeserializer.java` | same names + `FechaJsonLenient` (shared parsing) | UTIL-23 |
| `StreamUtils.java` | `StreamUtils` | UTIL-17 |
| `DateUtil.java`, `DateFormatException.java` | same names | UTIL-30..32 |
| `Fecha.java` | `Fecha` | UTIL-34, 35 |
| `Timer.java` (log4j) | `Timer` (SLF4J, `createTimer(LongSupplier)`) | UTIL-36 |
| `JavaCall.java`, `JavaCallExtra.java`, `JavaUtil.java`, `JavaCallMetadata.java` | same names; the duplicated defaults live once in `JavaUtil` | UTIL-40, 41 |
| `FileUtils.java` | `FileUtils` | UTIL-50, 51 |
| `ZipUtil.java`, `BadFilePathException.java` | same names | UTIL-53..57 |
| `ValidarNuevaClave.logica` (`:103-228`) | `clave.PoliticaClave`, `clave.ValidadorClave` (+ records) | RULE-031..034 |
| `Sesion.insertarSesion` token (`:184-185`) | `sesion.TokenSesion` | RULE-021 |
| `SesionUtils.GetParametro` + `ProcesaLlamadaServletCall` idle check | `sesion.PoliticaInactividad` | RULE-024, 025 |
| `SesionUtils.ObtenerFechaHoraUTCTs` (`:83-108`) | `sesion.RelojUtcCacheado` | RULE-071 |

`security.HybridPasswordEncoder` and `report.CrystalReport*` are unchanged (consumed by other modules).

## Approved deviations

- **RULE-025:** COR is not exempt; it reads `PLA.IdleMaxTimeCOR`, is idle-checked and its last use is updated.
- **RULE-032:** separate minimum-digit rule `ACC.numDigitos`, default 1, appended as the last criterion.
- **RULE-031, 128+ characters:** validated normally; the legacy `byte` loop counter (`ValidarNuevaClave.java:164`) threw `StringIndexOutOfBoundsException`.
- **UTIL-56:** `getFirstFileEntry` returns the first file entry; the legacy hung when the first entry was a directory (`ZipUtil.java:118-119`).

## Other differences (not observable through the pinned API)

- `RelojUtcCacheado` is one synchronized instance per database instead of a JVM-wide unsynchronized static cache, and its first call always reads (the legacy skipped the read if the JVM clock was under one hour since epoch).
- Default-charset uses are explicit UTF-8 (`JavaUtil.loadResourceToString`, `Utils.codificarEvento/decodificarEvento`, `TokenSesion`, `ZipUtil.isRar`).
- `System.err` / `printStackTrace` replaced by SLF4J (`FechaJsonLenient` DEBUG, `ZipUtil.generateMultipleZip` WARN).
- `copyPojo` shares `Long`/`Integer` instances (immutable) instead of `new Long(..)`; `String`, `BigDecimal`, dates and lists are still fresh copies (identity pinned).
- `JavaCall`/`JavaCallExtra` extend `JavaUtil` for the shared defaults.
- `Fecha`, `Timer` are `final`; `Timer.Metric` is private.

## Not migrated

Data access (`SQL`, `JdbcUtil`, `Named*Statement`, `ListMapResultSet`, `NuamPreparedStatement`, `cl.bcs.sql.*`), servlet/TomEE/platform-DB classes (`ProcesaLlamadaServletCall`, `ServletUtils`, `JavaView*`, `PluginLoader`, `AgregarLog`, `AgregarNotificacion`, `Locker`, `CallRestService`, `ConnectionUtils`, `PropertiesUtil`, `JobFileManager`, `ConcurrentArchiver`), `StatsD`, `Log`, `Crypto`, `CryptoUtils`, `SesionUtils`, `ValidarNuevaClave`, `Sesion` (DB-bound), `JsonWebToken` (dead code), and the non-pure parts of `Utils`. The first-pass `PasswordScanner`, `SessionTimeout` and `Sesion` were removed.

## Transform pass of 2026-10-19 (Steps 1, 3, 4, 5 re-run on the existing code)

**Equivalence is dual-execution on the legacy's pure classes** (the legacy cannot build standalone; its pure classes and the DB-bound ones were compiled and run locally, DB on in-memory H2). No production or third-party service was called.

- **Step 1 (gap audit, test-engineer):** no gaps in scope A; no tests added. Every public member of the in-scope legacy classes is pinned by a recorded golden. Open for a person: (a) `HybridPasswordEncoderTest` still carries the comment `RULE-030 FIX`, but RULE-030 is `wrong` in `RULE_REVIEWS.json` and the test pins the new encoder's own contract, not legacy behavior; (b) `CrystalReportsRestClientTest` names no rule or UTIL id; (c) the pure legacy `Utils` members that are not migrated (number-to-words `convert`, `toCSVString`, `stackTraceToString`, `getCurrentTimeStamp`, `isWindowsOS`, `getJacksonBuilder`) stay out under the scope A decision.
- **Step 2:** `src/main` unchanged (no HIGH finding from Step 5).
- **Step 3.1–3.2:** `mvn -o -pl :bcs-utils clean test` → 2199 tests, 0 failures, 0 skipped. `equivalence cases executed: 1840` (dual execution, below).
- **Step 3.3 canary:** `RutUtil.digitoVerificador case 11 -> '0'` changed to `'1'` → 6 tests failed; `PoliticaInactividad.sesionExpirada '>'` → `'>='` → 1 test failed. Both in scratch copies; evidence in `analysis/otr-sva-optimus-corredores/equivalence/canary/BcsUtils-verify3/`.
- **Step 3.4 dual execution:** `analysis/otr-sva-optimus-corredores/equivalence/BcsUtils/cases.json` (+ `out/legacy`, `out/new`, `EQUIVALENCE.json`): 1840 cases, 1840 same, 0 differ, 0 approved. The other 55 golden cases (of 1895) have no per-case dump and are asserted field-wise in the unit tests (RULE-021/024/025/071 session cases, RULE-032 new-behavior checks, TIMER-064, PASSWORD-198, SESSION-017/019/038/040, ZIP-027). No masks. Limit accepted by the user 2026-10-19: the goldens were recorded with gson 2.8.2 / JDK 11 / commons-io 2.5, not the production pins (gson 2.6.2, Java 8); `json` and `collections` re-recorded with gson 2.11.0 are identical.
- **Step 5 (architecture-critic): no HIGH.** Not applied, for a person to schedule:
  - M1 `spring-boot-starter-web` is a compile dependency used by one class (`CrystalReportsRestClient`); BcsTesoreriaJobs has no starter-web of its own and would inherit Tomcat. Fix: `org.springframework:spring-web` only. M2 `starter-actuator` unused. M3 slf4j used but not declared (breaks if M1 is fixed).
  - M4 `HybridPasswordEncoder` / `CrystalReportsRestClient` are `@Component`s in a library with an optional `spring-security-crypto`. M5 `rehashEnabled` only settable by `@Value`. M6 SHA-256 legacy compare not constant-time and NPE on null (bcrypt truncates at 72 bytes).
  - M7 `Fecha` static offset can be null until set (as legacy). M8 `RelojUtcCacheado`: `int` cast, wall clock instead of monotonic, lock held across the DB call. M9 `PoliticaInactividad.sesionExpirada(canal…)`/`actualizaUltimoUso` ignore their argument. M10 `ZipUtil.isRar` uses `readLine` on binary data; `createFolder` keeps `..` (pinned).
  - LOW: Timer prints to `System.out` and is not thread-safe; legacy `Calendar`/`SimpleDateFormat` in `Fecha`; JOBOL names; `Utils` throws bare `Exception`; `TokenSesion` leaks user/company id (not a secret); no timeouts in the Crystal client; `commons-codec` for two calls.
