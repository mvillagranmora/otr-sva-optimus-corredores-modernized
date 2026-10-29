# API assumed by the FMM-IFM tests (ConsultarInstrumentosFondosMutuos, F3 W1 front 3)

The production code does NOT exist yet. The tests of this package (`CasoIfm`, `SujetoIfm`, `IfmEquivalenciaTest`, `IfmDeterminismoTest`, `IfmReglasTest`,
`IfmCompuertaTest`) are written against EXACTLY the classes, constructors and methods below, all in package `cl.bcs.fondomutuo.consultas.instrumentos` (main source
set) unless another package is named. Legacy FQN: `cl.bcs.fondomutuo.ConsultarInstrumentosFondosMutuos` (the handler answers to it). Design = the FSA front: service +
read port + JavaCall handler, closed by default, read only, so **no lock, no Tesoreria, no custody, no sequences, no write port, no G1..G5 gate**.
The only kit collaborator is the dictionary (the `UI_SIN_VALORES` text); no basic requirements, no profile, no HTTP port.
If production needs another shape, change the tests (not the golden): nothing of the comparison depends on these names except the entry points marked (*).

## 1. Classes

| Class | Kind | Members the tests use |
|---|---|---|
| `ConsultarInstrumentosConfiguracion` | `@Configuration` | `public static final String LISTA = "CONSULTAR_INSTRUMENTOS"` (its OWN name in `fondomutuo.solicitud.listas-servidas`; closed when absent); `public static final String FQN = "cl.bcs.fondomutuo.ConsultarInstrumentosFondosMutuos"`; the `@Bean ConsultarInstrumentosPort` (not used by the tests: they build with the factory) |
| `ConsultarInstrumentosContrato` | holder of the JSON types | nested `public static class Entrada`, `InFondos`, `Salida`, `OutFondoMutuo`, `OutListaRegimen`, `Regimen`, `MensajesErrores` (see 2) |
| `ConsultarInstrumentosPort` (*) | interface, ONE abstract method (the tests use lambdas) | `ConsultarInstrumentosContrato.Salida logica(ConsultarInstrumentosContrato.Entrada entrada) throws Exception` (legacy `logica(con, entrada)` without the connection; the unit of work is the ambient Spring transaction, `readOnly`) |
| `ConsultarInstrumentosService` | `implements ConsultarInstrumentosPort` | constructor `(port.out.kit.DiccionarioMensajesPort diccionario, ConsultarInstrumentosDatos datos)`; no state |
| `ConsultarInstrumentosDatos` | read port | three reads, SQL verbatim (already in `src/main/resources/cl/bcs/fondomutuo/sql/`, same SHA-256 as the legacy): `List<ConsultarInstrumentosContrato.OutFondoMutuo> fondos(Long idEmpresa, ConsultarInstrumentosContrato.InFondos filtro, long filtroRegimen, String mostrarBloqueados)` (`..._SelectFondoMutuo.sql`), `List<OutFondoMutuo> fondosReinversion(Long idEmpresa, InFondos filtro, long filtroRegimen)` (`..._SelectFondoMutuoReinversion.sql`), `List<ConsultarInstrumentosContrato.Regimen> regimenes(List<String> idsSerieFondo)` (`..._SelectRegimen.sql`). Parameter names as the legacy: `IdEmpresa, IdSerieFondo, IdEmisor, IdFamiliaFondo, FiltroRegimen, IdRegimenFondoMutuo (the list), IdCategoriaPerfilFondoMutuo (from IdCategoriaPerfilInversionista), IdEstado, IdMoneda, FondoExtranjero, InstrumentoOWP, EsAPV, MostrarBloqueados` and, only in the reinversion SQL, `IdSerieFondoOrigen` |
| `ConsultarInstrumentosJdbc` | `implements ConsultarInstrumentosDatos` | constructor `(cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo conexion)` |
| `ConsultarInstrumentosFabrica` (*) | factory | `public static ConsultarInstrumentosPort crear(Dependencias d)`; nested `public static final class Dependencias` with PUBLIC non-final fields `diccionario` (`DiccionarioMensajesPort`) and `datos` (`ConsultarInstrumentosDatos`) |
| `ConsultarInstrumentosJavaCallHandler` (*) | `@Component implements JavaCallHandler` | constructor `(ConsultarInstrumentosPort servicio, kit.Compuertas compuertas)`; `String fqn()` returns `JavaCallDispatcher.CONSULTAR_INSTRUMENTOS_FONDOS_MUTUOS`; `JsonObject call(java.sql.Connection con, JsonObject in) throws Exception` |

Existing production classes the change touches (the tests reference them):
* `cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher`: new constant `CONSULTAR_INSTRUMENTOS_FONDOS_MUTUOS = "cl.bcs.fondomutuo.ConsultarInstrumentosFondosMutuos"` and the constant added to
  `LISTA_BLANCA` (`IfmCompuertaTest` asserts it). Existing tests that assert the exact allow-list must be updated by the same change.
* `kit.Compuertas` is NOT changed: the class is served when `listasServidas()` contains `ConsultarInstrumentosConfiguracion.LISTA`; the shipped default (empty) keeps it closed.

## 2. Contract types (field names = legacy, JSON = legacy plain `new Gson()`: `kit.JsonLegacy.plano()`)

* `Entrada`: `long p_IdEmpresa; long p_IdUsuario; String p_IdModulo; InFondos p_EntradaFondosMutuos` (absent or JSON null = null). `p_IdEmpresa` is a primitive `long` (absent = 0). A wrong JSON type is a `com.google.gson.JsonSyntaxException`.
* `InFondos` (legacy constructor defaults when a key is absent; an explicit JSON `null` stays null): `String IdSerieFondo = "", IdEmisor = "", IdFamiliaFondo = ""; List<String> IdRegimenFondoMutuo = []; String IdSerieFondoOrigen = "", IdCategoriaPerfilInversionista = "", IdEstado = "", IdMoneda = "", FondoExtranjero = "", InstrumentoOWP = "", EsAPV = ""; String OcultarBloqueados = "N"`.
* `Salida`: `List<OutFondoMutuo> FondosMutuos = []; List<MensajesErrores> Mensajes = []; Long ContadorErrores = 0L` (NOT `P_OUT_ERRORESCONTADOR`: D-F3A-12/27).
* `MensajesErrores`: `Long IdInterno; String TipoMensaje; String Codigo; String Mensaje` (the legacy constructor `(id, tipo, codigo, valores, con, idEmpresa)` takes the text from the dictionary; here `diccionario.mensaje(idEmpresa, codigo, valores)`).
* `OutFondoMutuo` (field order = JSON order): `String IdSerieFondo, CodigoFondo, NombreFondo, Nemotecnico, CodigoSerie, NombreSerie, IdEmisor, NombreEmisor, IdMoneda, IdFamiliaFondo; BigDecimal ValorCuota; Long DecimalesValorCuota; String FechaValorCuota; BigDecimal MontoMinimoInversionPesos, MontoMinimoInversionTotal, MontoMaximoInversionPesos, MontoMaximoInversionTotal; String IdCategoriaPerfilInversionista, NombreCategoriaPerfilInversionista, CobraComision, InstrumentoOWP, EsAPV, RestringeAporte; BigDecimal MontoPatrimonioEmisor; List<OutListaRegimen> ListaRegimen = []`.
* `OutListaRegimen`: `String IdRegimen, DescripcionRegimen, PermiteMLT`. `Regimen` (row of `SelectRegimen`): `String IdSerieFondo, IdRegimenFondoMutuo, DescripcionRegimen, PermiteMLT`.
* The row mapping is by exact column label to the PUBLIC fields (`SqlNombrado.consultar`): `DecimalesValorCuota` must be a `Long` (BIGINT) and the money columns `BigDecimal`.

## 3. Behaviour the tests pin (bug for bug; the golden is the oracle, this is the index)

* Order of operations (FM-IFM-01/02): `p_EntradaFondosMutuos == null` => message `UI_SIN_VALORES` (id 0, tipo `ERROR` = `MensajeSalida.MENSAJE_ERROR`), `ContadorErrores = 1`, return BEFORE any read; then `IdRegimenFondoMutuo.size()` (a null list is a `NullPointerException`: FM-IFM-01), `FiltroRegimen = size > 0 ? 1L : 0L`; `"".equals(IdSerieFondoOrigen)` selects the normal SQL with `MostrarBloqueados = "S".equalsIgnoreCase(OcultarBloqueados) ? "N" : "S"`, any other value (including `null` and `" "`) the reinversion SQL (D-F3A-13); then ONE read of the regimes with the ids of the series found (empty list => the SQL runs with `IN (NULL)`).
* The regime lists are filled per series with `equals` on `IdSerieFondo` (a repeated series, e.g. from a repeated view row, repeats its list); the legacy used `parallelStream`: the order of the series is the SQL order, the order of each `ListaRegimen` is the SQL `ORDER BY IdRegimenFondoMutuo`.
* `PermiteMLT` comes from the SQL (`CASE` without `ELSE`: null for a FondoExtranjero other than S/N; the plain Gson omits the key). The reinversion SQL has no `DecimalesValorCuota` / `MontoPatrimonioEmisor` columns (keys omitted).
* Dictionary: unknown code (or an empty company configuration) gives `"<codigo> sin traduccion"` (kit rule, tested in the golden).
* Exceptions: class compared (`JsonSyntaxException`, `NullPointerException`).
* Read only: tables unchanged; no session-local table, no autonomous commit, no sequence; `colaboradores` is `[]`; the servlet decision is `ROLLBACK` in every non-exception case (no `P_OUT_ERRORESCONTADOR`: D-F3A-27) and `ROLLBACK_HTTP500` for an exception.
* H2 only: the SQL calls `TO_VARCHAR(date, 'YYYY-MM-DD')`; `base-ifm.sql` creates the alias `TO_VARCHAR` for `cl.bcs.oracle.H2Fn.toNvarchar` (the test helper that already exists in `src/test/java/cl/bcs/oracle/`). The three views (`MER.ConsultarEmisor`, `FMM.ConsultarCategoriaPerfilFondoMutuo`, `FMM.ConsultarConfiguracionRegimenFondoMutuo`) are H2 tables whose columns are ASSUMED (only those the SQL names).

## 4. Test resources (created by `legacy-oracle/sync_ifm.py` from the oracle output)

`src/test/resources/fondomutuo-ifm/cases/<id>/{case.json,seed.sql,expected.json}`, `fondomutuo-ifm/seed/base-ifm.sql` (loaded after `fondomutuo-3a/seed/base-fmm.sql`: ALTERs, view stand-ins,
alias, fake universe and the dictionary row `UI_SIN_VALORES`), `fondomutuo-ifm/determinism-ifm.txt` (result of the 20 runs).
