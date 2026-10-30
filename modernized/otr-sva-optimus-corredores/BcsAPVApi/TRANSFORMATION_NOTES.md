# BcsAPVApi: transformation notes

BcsAPVApi is not a legacy module: it is the GENERATED typed contract of the APV service (64 invocable use cases, 234 records, 2096 fields, `CasosUso.TODOS`, `CasoUso`, `comun.Mensaje`), produced by `tools/generar_todo.py` from `legacy/.../BSCL/APV/JAVA/BcsAPVServicios`. See `CONTRATO.md` and `CONTRATO_CAMPOS.md` (generated, never edited by hand).

## Refuerzo de pruebas (2026-10-29)

Before: 1 test (`ApvApiSmokeTest`, kept). After: 25 tests in 7 classes, all green. The tests read the legacy `.java` sources with their own small reader (`LegacyApv`, no code shared with `tools/legacy_parser.py`), so a defect of the generator's parser or type rules shows up as a difference. They never skip: missing legacy sources or no Python interpreter is a failure.

| Class | Tests (rule id) | What it pins |
|---|---|---|
| `ApvApiCoberturaOperacionesTest` | B-APVAPI-01..05 | 66 JavaCall classes in the legacy, 64 invocable, the 2 with `call()` returning null (`ValidarBancoPrevired`, `ValidarMandatosPAC`) have no use case; each invocable has its interface with `Resultado ejecutar(Entrada)` and `CasoUso<E,R>`; `CasosUso.TODOS` has each exactly once (64, no extras, matches the files of `casouso/`); names of `catalogo.py` and `CONTRATO.md` agree with the code |
| `ApvApiCamposContraLegacyTest` | B-APVAPI-06..10 | for every Entrada/Salida graph read from the legacy: same fields, names, types and order (own fields first, inherited after) in the 234 records / 2096 fields; no record without legacy class and none missing; component names follow the camelCase rule (`p_`, `P_`, `P_OUT_` stripped; collision variants listed); figures of `CONTRATO.md` and the annex; every row of `CONTRATO_CAMPOS.md` equals legacy and record |
| `ApvApiNombresJsonTest` | B-APVAPI-11, 12 | Gson round trip of a populated record of each of the 234 classes and of the legacy-named JSON renamed position by position; every legacy JSON key (2096, 577 with `p_`/`P_` prefix) is a literal of the generated adapter mapper |
| `ApvApiGeneradorDeterminismoTest` | B-APVAPI-13, 14, 22, 23, 24 | runs the real generator (`ProcessBuilder`, py/python3/python, fails if none): API code and adapter mappers equal the generator output except the documented override; `generar_contrato.py --check` = 0; `generar_todo.py --check` prints the same twice; override default `[null]` of `ValidarContratoEntrada`; headers, UTF-8 without BOM, uniform line endings, the 5 hand-written main files |
| `ApvApiInvariantesCasoUsoMensajeTest` | B-APVAPI-15..18 | `CasoUso` has one abstract `ejecutar`, no state; `Mensaje` is the 4-field legacy `MensajeSalida`, fresh mutable list per call; all 234 records have `porDefecto()`, default lists not shared; `CasosUso` final, private constructor, immutable sorted list |
| `ApvApiReflexionInterfacesTest` | B-APVAPI-19..21 | interfaces public, stateless, one plain abstract method, no annotations; imports of the 304 main sources only `java.util/math/time`, gson `JsonObject` and the module; compiled classes reference no framework/JDBC/servlet; records public, final, immutable |
| `ApvApiSmokeTest` | (existing) | 64 interfaces with typed signature, `porDefecto` of the roots |

No `RULE-nnn` of `BUSINESS_RULES.md` describes this contract (the only P0 rule the pack lists for this module is about hardcoded HANA credentials in dead code of another module), so the ids are `B-APVAPI-nn`.

Mapping rule checked (CONTRATO.md): Long/Integer/Double/Boolean/String/BigDecimal and primitives keep their type; `java.util.Date` to `LocalDateTime`, `java.sql.Date` to `LocalDate`; `MensajeSalida` to `comun.Mensaje`; List/ArrayList/array to `List`; APV classes to records; any external class to `JsonObject` (4 occurrences, all in `TraspasarEntreCuentasBancariasResultado`: Tesoreria payload lists `CrearComprobante.*`, `CrearIngresoEgreso.*`); `Timestamp` and `Map` must not occur.

### Canarios (18, all red; clean copy 0 red)

Mutations on a COPY outside the repo (`$CLAUDE_JOB_DIR/tmp`, parent POM, adapter, legacy sources and analysis docs copied so that the generator runs), logs and surefire XML in `analysis/otr-sva-optimus-corredores/equivalence/canary/BcsAPVApi-20261029/<canario>/` (`mutation.txt`, `mvn.log`, `surefire/`), summary in `RESUMEN.txt`. Hash of 721 repository files (module, adapter src, legacy APV src, parent pom, analysis docs) before and after: identical.

| Canary | Red tests |
|---|---|
| todos-quitar-un-caso | 4 |
| todos-duplicar-un-caso | 3 |
| campo-cambiar-tipo (`Long` to `String`) | 3 |
| campo-renombrar | 4 |
| campo-orden-intercambiado | 4 |
| record-quitar-clase-anidada | 8 |
| record-sobrante | 4 |
| interfaz-no-publica | 3 |
| interfaz-con-default | 2 |
| casouso-metodo-extra | 1 |
| mensaje-default-no-nulo | 1 |
| mensaje-lista-compartida | 1 |
| dependencia-externa (`java.sql.Timestamp`) | 2 |
| default-alterado-sin-regenerar | 1 (only the generator comparison can see it) |
| anexo-editado-a-mano | 3 |
| legacy-agrega-campo-sin-regenerar | 7 |
| legacy-nueva-operacion-javacall | 5 |
| adaptador-pierde-prefijo | 1 |

### Findings of the reinforcement

1. **`generar_todo.py --check` fails on this tree (110 differences), and a plain `generar_todo.py` would destroy work.** 109 of the differences are in `BcsAPVServicios/.../application` (the generated skeleton was replaced by the real services; the generator `rmtree`s that directory when writing). 1 is in this module: `ValidarContratoEntrada.porDefecto` carries the hand override of `tools/overrides/ValidarContrato.md` (legacy default `[null]`), which the generator does not apply (it emits an empty list; `CONTRATO_CAMPOS.md` also says `empty list` for that field). Do NOT run `generar_todo.py` without `--check` until the generator knows both. The tests assert exactly this one known difference for `BcsAPVApi` and zero for the adapter mappers.
2. **Line endings.** The committed tree (and the generators) are CRLF; the generator emits LF, so `--check` compared bytes and reported every file. Fixed in the generator: `generar.comprobar()` and `generar_contrato.py --check` now ignore the CR of CRLF when comparing (content is still exact). Generated output is unchanged; nothing was regenerated. The tree of the module is uniformly CRLF (299 of 299 generated files); the tests accept uniform LF or CRLF, never a mix.
3. The legacy graph read independently agrees with the contract in all 234 records / 2096 fields: no discrepancy against the legacy.

### Limits

A generated contract proves SHAPE, not behavior: these tests show that the records, interfaces and lists have the legacy fields, names, types, order and count, and that the generator is reproducible. They do not show that any use case computes what the legacy computes (that is `BcsAPVServicios`), that the adapter mappers convert values correctly (`BcsApvJavaCallAdapter`), that defaults other than the `ValidarContrato` one equal the legacy constructors (only checked through generator reproducibility), nor that the opaque `JsonObject` Tesoreria payloads have the right inner shape. Name variants for ALLCAPS legacy fields are checked by letters, not by the human word split.

## Camino a PROVEN (2026-10-30)

Goal: close the three reasons of the proof pack for BcsAPVApi (P0 RULE-076 not named by a test; no `equivalence/cases.json`; no `equivalence/fresh-cases.json`). Suite after the work: BcsAPVApi 32 tests in 9 classes (before 25 in 7), BcsApvJavaCallAdapter 15 in 4 (before 12 in 3), all green, no skips (`analysis/otr-sva-optimus-corredores/equivalence/BcsAPVApi-camino-proven.test-output.txt`).

### A. The generator applies the ValidarContrato override, and refuses to destroy the services
- `tools/overrides/ValidarContrato.md` now carries a machine-readable line (`OVERRIDE-DEFAULT: ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada p_ContratosSAAPV = [null]`). `modelo.cargar_overrides()` reads every `OVERRIDE-DEFAULT:` line of `tools/overrides/*.md`; `generar.py` emits the `[null]` default (with the `// OVERRIDE (...)` comment) and `generar_contrato.py` writes `[null]` in `CONTRATO_CAMPOS.md`. An override that matches no legacy field stops the generator (`modelo.verificar_overrides()`).
- Regenerated ONLY in a temporary copy of BcsAPVApi, BcsApvJavaCallAdapter and BcsAPVServicios (same relative layout). The only files that changed: `CONTRATO_CAMPOS.md` (one row, `empty list` -> `[null]`) and its copy `analysis/.../APV_CONTRACT_FIELDS.md`; `ValidarContratoEntrada.java` was already byte-identical to what the generator now emits, so no Java file changed. Line endings preserved (CRLF in the module, LF in analysis).
- `generar_todo.py --check` on the real tree: 110 -> **109** differences, all of them the known drift of `BcsAPVServicios/.../application` (real services instead of skeletons); BcsAPVApi and the adapter: 0; contract docs: 0. The tests that asserted "exactly one difference in BcsAPVApi" (B-APVAPI-13) now assert 0 in BcsAPVApi/adapter, exactly 109 in BcsAPVServicios, and that the annex shows `[null]`; nothing else was weakened.
- GUARD: the write mode (`generar.py` / `generar_todo.py` without `--check`) calls `guardar_servicios()` BEFORE deleting anything: if `BcsAPVServicios/.../application` holds files that are neither skeletons (`UnsupportedOperationException("TODO Step 2`) nor equal to the generator's output, it prints `REFUSED ...`, exits 3 and writes nothing. `--forzar-servicios` overrides it; `--check` never needs it. Tried on the temporary copy: refused with the 109 real services (tree intact, rc 3); forced write replaced them by skeletons; a second write on the pristine skeleton ran without the flag; `--check` after that: 0 differences. New test B-APVAPI-25 (never calls the write mode).

### B. RULE-076 pinned
`Rule076CredencialesHardcodeadasTest` (+ helper `Rule076Escaner`): scans `src/main`, `src/test` of BcsAPVApi and BcsApvJavaCallAdapter and `tools/*.py` for a `main` entry point, a JDBC URL literal with user and password (or the leaked HANA endpoint of the rule), and a password-like key assigned a non-empty literal; it also checks the scanner itself finds each shape in a fake directory and does not flag clean code. Canary: a copy outside the repo with a fake JDBC literal in `Sabor.java` makes it red (`analysis/.../equivalence/canary/BcsAPVApi-20261030/rule076-credencial-en-literal/`).

### C. Same behavior (cases.json) and D. fresh inputs (fresh-cases.json)
Dual oracle in `analysis/otr-sva-optimus-corredores/equivalence/BcsAPVApi/` (see `ORACLE_NOTES.md`): the real legacy Entrada/Salida classes (compiled from `legacy/`, nothing modified there) against the typed records through the adapter mappers. `cases.json`: 128 cases (64 operations x Entrada/Salida, default behaviour), 128 same, 0 differ. `fresh-cases.json`: 384 cases (3 profiles x 2 sides x 64), 380 same, 0 differ, **4 missing = finding F-APV-01** (the hand written `MapeoJson.listaEscalar` rejects a `null` element inside a list of opaque Tesoreria objects that the legacy accepts; not fixed, not approved). JUnit: `ApvApiEquivalenciaDefectoGoldensTest` (BcsAPVApi) and `ApvEquivalenciaGoldensTest` (adapter, byte comparison of defaults and fresh round trips, pins the 4 known rejections); both fail if the goldens are missing.

### What is still NOT proven (limits)
- BcsAPVApi is a GENERATED typed contract: these tests and the oracle prove shape, defaults and JSON round trips, not what any use case computes. The behaviour of the 64 operations (`logica(...)`) is `BcsAPVServicios`, whose verdict is separate (and `generar_todo.py --check` still reports its 109-file drift).
- `BcsApvJavaCallAdapter` tests the mapper behaviour and has its own verdict (its pack row still lists 60 P0 rules not named by tests and has no cases of its own).
- Malformed records (wrong types, truncated JSON) were not exercised (no `INTENT.md`); see `leftOut` in `ORACLE_NOTES.md`.

### VB-V09 (desviacion APROBADA por el usuario el 2026-10-30)
El verify independiente (20261030b) encontro 1 diferencia en 15 entradas frescas propias (399 en total): los 10 campos de payload externo de Tesoreria (`CrearTraspasoTesoreria`, `DevolverBonificacionEntidadRecaudadora`, `DevolverPagoRezagos`, `TraspasarEntreCuentasBancarias`, ...) se llevan como `JsonObject` opaco (CONTRATO.md linea 141), mientras el legacy los deserializa a las clases de Tesoreria y los reescribe con sus valores por defecto (`"Monto":0`, `RequiereAutorizacion:"N"`, `IdTipoCanalIngreso:"OPT"`, `FolioCSC:""`) y con la escala de `BigDecimal` normalizada. Decision del usuario: se aprueba como desviacion de diseno (el servicio de Tesoreria aplica esos valores al tipar el JSON; solo cambia el JSON intermedio de APV). Registrada en `fresh-cases.json` (`approvedInputs`); `FRESH_EQUIVALENCE.json`: 399 casos, 398 iguales, 1 diferencia aprobada, 0 faltantes. Si algun dia APV debe reproducir los valores por defecto de Tesoreria, hay que modelar esas clases en el contrato (queda como pregunta abierta de CONTRATO.md linea 178).
