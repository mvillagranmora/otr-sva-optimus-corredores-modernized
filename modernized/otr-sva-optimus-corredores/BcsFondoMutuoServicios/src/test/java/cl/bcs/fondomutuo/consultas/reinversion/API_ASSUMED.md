# API assumed by the FMM-FMR tests (ConsultarFondoMutuoReinversion, F3 W1 front 2)

The production code does NOT exist yet. The tests of this package (`CasoFmr`, `SujetoFmr`, `FmrEquivalenciaTest`, `FmrDeterminismoTest`, `FmrReglasTest`,
`FmrCompuertaTest`) are written against EXACTLY the classes, constructors and methods below, all in package `cl.bcs.fondomutuo.consultas.reinversion` (main source
set) unless another package is named. Legacy FQN: `cl.bcs.fondomutuo.ConsultarFondoMutuoReinversion` (the handler answers to it). Design = the FSA front (F3 W1
front 1): service + read port + JavaCall handler, closed by default, read only, so **no lock, no Tesoreria, no custody, no sequences, no write port, no G1..G5 gate**.
The class has NO collaborator at all: no kit port (no basic requirements, no profile, no dictionary, no configuration), no HTTP port.
If production needs another shape, change the tests (not the golden): nothing of the comparison depends on these names except the entry points marked (*).

## 1. Classes

| Class | Kind | Members the tests use |
|---|---|---|
| `ConsultarReinversionConfiguracion` | `@Configuration` | `public static final String LISTA = "CONSULTAR_FONDO_REINVERSION"` (its OWN name in `fondomutuo.solicitud.listas-servidas`; closed when absent); `public static final String FQN = "cl.bcs.fondomutuo.ConsultarFondoMutuoReinversion"`; the `@Bean ConsultarReinversionPort` (not used by the tests: they build with the factory) |
| `ConsultarReinversionContrato` | holder of the JSON types | nested `public static class Entrada`, `Salida`, `FiltroFondoMutuo` (see 2) |
| `ConsultarReinversionPort` (*) | interface, ONE abstract method (the tests use lambdas) | `ConsultarReinversionContrato.Salida logica(ConsultarReinversionContrato.Entrada entrada) throws Exception` (legacy `logica(con, entrada)` without the connection: the unit of work is the ambient Spring transaction, `readOnly`) |
| `ConsultarReinversionService` | `implements ConsultarReinversionPort` | constructor `(ConsultarReinversionDatos datos)`; no state |
| `ConsultarReinversionDatos` | read port | `List<ConsultarReinversionContrato.FiltroFondoMutuo> reinversion(Long idEmpresa, String idEstado, String idMoneda, String fondoExtranjero, String idFamiliaFondo, String idEmisor, String idRegimenFondoMutuo, String idSerieFondoOrigen)`: ALL rows of `ConsultarFondoMutuo_SelectReinversion.sql` (verbatim, already in `src/main/resources/cl/bcs/fondomutuo/sql/`, same SHA-256 as the legacy), parameters named `IdEmpresa, IdEstado, IdMoneda, FondoExtranjero, IdFamiliaFondo, IdEmisor, IdRegimenFondoMutuo, IdSerieFondoOrigen`, nulls passed as nulls |
| `ConsultarReinversionJdbc` | `implements ConsultarReinversionDatos` | constructor `(cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo conexion)` |
| `ConsultarReinversionFabrica` (*) | factory | `public static ConsultarReinversionPort crear(ConsultarReinversionDatos datos)` |
| `ConsultarReinversionJavaCallHandler` (*) | `@Component implements JavaCallHandler` | constructor `(ConsultarReinversionPort servicio, kit.Compuertas compuertas)`; `String fqn()` returns `JavaCallDispatcher.CONSULTAR_FONDO_MUTUO_REINVERSION`; `JsonObject call(java.sql.Connection con, JsonObject in) throws Exception` |

Existing production classes the change touches (the tests reference them):
* `cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher`: new constant `CONSULTAR_FONDO_MUTUO_REINVERSION = "cl.bcs.fondomutuo.ConsultarFondoMutuoReinversion"` and the
  constant added to `LISTA_BLANCA` (`FmrCompuertaTest` asserts it). Existing tests that assert the exact allow-list must be updated by the same change.
* `kit.Compuertas` is NOT changed: the class is served when `listasServidas()` contains `ConsultarReinversionConfiguracion.LISTA`; the shipped default (empty) keeps it
  closed. `Compuertas(listas, false, false, false, false, false)` must be enough to run it.

## 2. Contract types (field names = legacy, JSON = legacy plain `new Gson()`: `kit.JsonLegacy.plano()`)

* `Entrada`: `long p_IdEmpresa; long p_IdUsuario; String p_IdModulo; String p_IdEstado, p_IdMoneda, p_FondoExtranjero, p_IdFamiliaFondo, p_IdEmisor, p_IdRegimenFondoMutuo, p_IdSerieFondoOrigen`.
  `p_IdEmpresa` and `p_IdUsuario` are PRIMITIVE `long` in the legacy (absent or JSON `null` = 0); every String stays null when absent (a null is "no filter" for the SQL: FM-FMR-02).
  `p_IdUsuario` and `p_IdModulo` are read and never used (FM-FMR-01). A wrong JSON type (`"abc"` for the company, an object for a text) is a `com.google.gson.JsonSyntaxException`.
* `Salida`: `List<FiltroFondoMutuo> P_OUT_LISTA = []`. NOTHING else: no counter, no messages (D-F3A-27).
* `FiltroFondoMutuo` (field order = JSON order): `String IdSerieFondo, NombreSerieFondo, IdMoneda, IdEmisor, MLT, IdRegimenFondoMutuo; BigDecimal ValorCuotaInstrumento, MontoPatrimonioEmisor, MontoMinimo`.
  The service copies every row field by field into a new object (FM-FMR-04). The numbers are written by Gson as the plain `BigDecimal` text of the H2/HANA column (`1234.56780000`, `0E-8`, `0`): the tests pin that text.

## 3. Behaviour the tests pin (bug for bug; the golden is the oracle, this is the index)

* One read of the SQL per request; the list keeps the order of the engine (no `ORDER BY`: the same SQL on the same H2 gives the same order on both sides). One row per serie AND per ACT regime (D-F3A-11); a serie without regime appears once with the regime key omitted.
* The wildcard rules live in the SQL (`'-'`, `''`, null): the service must pass the values untouched, in particular `''` is NOT turned into null and `'-'` is NOT dropped.
* **Dialect (H2 only)**: the SQL compares the BIGINT company with `''` (`CASE WHEN :IdEmpresa <> '' THEN :IdEmpresa ELSE ... END`). H2 refuses that comparison, so the TEST connection goes through `cl.bcs.fondomutuo.consultas.DialectoLectura` (and the oracle through `legacy-oracle/runner-lect/DialectoLect.java`): for that one pattern `?` becomes `CAST(? AS VARCHAR)`. **Q-F3A-9 is CLOSED against HANA QA** (read-only SELECT, parameter bound with `setLong` as the legacy does): a bound BIGINT parameter `<> ''` is ALWAYS true (1 and 0 both give 'distinto'; a CAST literal would raise error 339 but a bound parameter is compared as text). So the CASE always takes `:IdEmpresa`, and a company 0 (absent, null or `{}`) filters `IdEmpresa = 0`: an empty list, NOT every company. Three goldens pin it (`empresa-ausente-es-cero`, `empresa-nula-explicita`, `entrada-vacia`). The SQL file of the module must stay verbatim the legacy one: the bridge sits at the JDBC boundary of the TEST connection only.
* Exceptions: class compared (`JsonSyntaxException`); no collaborator can fail.
* Read only: the tables of the dump come back unchanged; no session-local table, no autonomous commit, no sequence; `colaboradores` is `[]`; the servlet decision is `ROLLBACK` in every non-exception case because there is no `P_OUT_ERRORESCONTADOR` (D-F3A-27) and `ROLLBACK_HTTP500` for an exception.

## 4. Test resources (created by `legacy-oracle/sync_fmr.py` from the oracle output)

`src/test/resources/fondomutuo-fmr/cases/<id>/{case.json,seed.sql,expected.json}`, `fondomutuo-fmr/seed/base-fmr.sql` (loaded after `fondomutuo-3a/seed/base-fmm.sql`;
it ALTERs three tables with columns of the HANA catalog that the F1 H2 schema lacks, creates the three view stand-ins and the fake universe), `fondomutuo-fmr/determinism-fmr.txt` (result of the 20 runs).
