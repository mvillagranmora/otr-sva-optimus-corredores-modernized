# API assumed by the FMM-VDO tests (ConsultarValorDolarFondoMutuo, F3 W1 front 4)

The production code does NOT exist yet. The tests of this package (`CasoVdo`, `SujetoVdo`, `VdoEquivalenciaTest`, `VdoDeterminismoTest`, `VdoReglasTest`,
`VdoCompuertaTest`) are written against EXACTLY the classes, constructors and methods below, all in package `cl.bcs.fondomutuo.consultas.dolar` (main source set)
unless another package is named. Legacy FQN: `cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo` (the handler answers to it). Design = the FSA front: service + read port +
JavaCall handler, closed by default, read only, so **no lock, no Tesoreria, no custody, no sequences, no write port, no G1..G5 gate**. Collaborators: the kit
configuration port and Plataforma `CalcularParidad` (port H, a READ through the router; it already exists as `port.out.http.ParidadPort` + `cierre.contrato.ParidadContrato`).
If production needs another shape, change the tests (not the golden): nothing of the comparison depends on these names except the entry points marked (*).

## 1. Classes

| Class | Kind | Members the tests use |
|---|---|---|
| `ConsultarDolarConfiguracion` | `@Configuration` | `public static final String LISTA = "CONSULTAR_VALOR_DOLAR"` (its OWN name in `fondomutuo.solicitud.listas-servidas`; closed when absent); `public static final String FQN = "cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo"`; the `@Bean ConsultarDolarPort` (not used by the tests: they build with the factory) |
| `ConsultarDolarContrato` | holder of the JSON types | nested `public static class Entrada`, `SerieFondoParidad`, `Salida`, `ResultadoValorDolarFondoMutuo`, `ValorCuotaTipoParidad` (see 2) |
| `ConsultarDolarPort` (*) | interface, ONE abstract method (the tests use lambdas) | `ConsultarDolarContrato.Salida logica(ConsultarDolarContrato.Entrada entrada) throws Exception` (legacy `logica(con, entrada)` without the connection; the unit of work is the ambient Spring transaction, `readOnly`) |
| `ConsultarDolarService` | `implements ConsultarDolarPort` | constructor `(port.out.kit.ConfiguracionSistemaPort configuracion, port.out.http.ParidadPort paridad, ConsultarDolarDatos datos)`; no state |
| `ConsultarDolarDatos` | read port | `List<ConsultarDolarContrato.ValorCuotaTipoParidad> valoresTipoCambio(List<String> idsSerieFondo, Long idEmpresa)`: ALL rows of `ConsultarValorDolarFondoMutuo_SelectValorCuotaTipoCambio.sql` (verbatim, already in `src/main/resources/cl/bcs/fondomutuo/sql/`, same SHA-256 as the legacy); `IN (:IdSerieFondo)` receives the list as given (duplicates and nulls included), the service builds the map |
| `ConsultarDolarJdbc` | `implements ConsultarDolarDatos` | constructor `(cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo conexion)` |
| `ConsultarDolarFabrica` (*) | factory | `public static ConsultarDolarPort crear(Dependencias d)`; nested `public static final class Dependencias` with PUBLIC non-final fields `configuracion` (`ConfiguracionSistemaPort`), `paridad` (`port.out.http.ParidadPort`) and `datos` (`ConsultarDolarDatos`) |
| `ConsultarDolarJavaCallHandler` (*) | `@Component implements JavaCallHandler` | constructor `(ConsultarDolarPort servicio, kit.Compuertas compuertas)`; `String fqn()` returns `JavaCallDispatcher.CONSULTAR_VALOR_DOLAR_FONDO_MUTUO`; `JsonObject call(java.sql.Connection con, JsonObject in) throws Exception` |

Existing production classes the change touches (the tests reference them):
* `cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher`: new constant `CONSULTAR_VALOR_DOLAR_FONDO_MUTUO = "cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo"` and the constant added to
  `LISTA_BLANCA` (`VdoCompuertaTest` asserts it). Existing tests that assert the exact allow-list must be updated by the same change.
* `kit.Compuertas` is NOT changed: the class is served when `listasServidas()` contains `ConsultarDolarConfiguracion.LISTA`; the shipped default (empty) keeps it closed.
  No `fondomutuo.*.habilitad*` flag is needed (CalcularParidad is a read).

## 2. Contract types (field names = legacy, JSON = legacy plain `new Gson()`: `kit.JsonLegacy.plano()`)

* `Entrada`: `Long p_IdEmpresa; Long p_IdUsuario; String p_IdModulo; List<SerieFondoParidad> p_Series` (default `[]` when the key is absent; an explicit JSON `null` stays null). `p_IdEmpresa` is a boxed `Long` here (absent = null). A wrong JSON type is a `com.google.gson.JsonSyntaxException`.
* `SerieFondoParidad`: `String IdSerieFondo; String IdTipoSolicitud; String Fecha`.
* `Salida`: `Long P_OUT_ERRORESCONTADOR = 0L; List<ResultadoValorDolarFondoMutuo> P_OUT_RESULTADO = []`. No messages.
* `ResultadoValorDolarFondoMutuo` (field order = JSON order): `BigDecimal ValorParidad; String IdSerieFondo; String Fecha; String IdTipoSolicitud` (a null is omitted by the plain Gson).
* `ValorCuotaTipoParidad` (row of the SQL): `String IdSerieFondo; BigDecimal ValorDolarAporte; BigDecimal ValorDolarRescate`.
* The collaborator JSON is `ParidadContrato.CalcularParidadEntrada` / `CalcularParidadSalida` (already in production; the oracle's double records the same field names and order).

## 3. Behaviour the tests pin (bug for bug; the golden is the oracle, this is the index)

* Order of operations (FM-VDO-01): `configuracion.texto(p_IdEmpresa, "FONDOMUTUO", "UsaDolarObservado")` and then `texto(p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema")` are read FIRST, ALWAYS, even with an empty list; THEN `p_Series.size()` (a null list is a `NullPointerException`). Empty list => `{P_OUT_ERRORESCONTADOR 0, P_OUT_RESULTADO []}` without any other read or call.
* Branch `"S".equals(UsaDolarObservado)` (exact: `s`, ` S` and `` go to the table, FM-VDO-02): ONE call to `ParidadPort.calcular` with `p_IdEmpresa`, `p_IdTrabajo 1` and one `CalculoEntrada` per DISTINCT `Fecha` (first appearance; a null `Fecha` is one more key): `IdTrabajo 1`, **`IdInterno` = a `Long i = 0L` that is never incremented (D-F3A-14)**, `Fecha`, `IdMonedaOrigen "USD"`, `IdMonedaDestino` = `PLA.MonedaSistema`, `ValorCambioLocalModificado 0`, `MontoOrigen 1`, `IdUso ""`. The result is keyed with `ListaHash.aHash(salidas, IdInterno)`: two or more outputs => `Exception("key field is not unique")` (D-NOR-9, a plain `Exception`: the harness names the class `Exception` and compares the MESSAGE). Each series then gets `salida(IdInterno = fechasIdInterno.get(Fecha)).MontoDestino`, or `BigDecimal.ZERO` when there is none. The collaborator's error counter is NOT read; an exception of the collaborator propagates.
* Branch of the table (FM-VDO-03): ONE read with the ids of ALL the series in the request order; `ListaHash.aHash(rows, IdSerieFondo)` (a repeated key: the same `Exception("key field is not unique")`); per series `ValorDolarAporte` when `"APO".equals(IdTipoSolicitud)`, else `ValorDolarRescate`; unknown series = `BigDecimal.ZERO`; the `Fecha` is ignored.
* Output keeps the request order and repetitions; `P_OUT_ERRORESCONTADOR` is always 0 on a normal return (COMMIT).
* Exceptions: class compared (`JsonSyntaxException`, `NullPointerException`); the message only for the plain `Exception` (`key field is not unique`, the missing-parameter text, the collaborator's text). The parameter port may throw its own class for a missing parameter: the harness maps it by name and, because the legacy class is a plain `Exception`, only the MESSAGE is compared (the kit text `Parametro <p> en modulo <m> no encontrado`).
* Read only: tables unchanged; no session-local table, no autonomous commit, no sequence; the only collaborator calls in the dump are the `CalcularParidad` ones.

### Known kit divergence (a decision for the user)
Three goldens (`empresa-sin-configuracion`, `empresa-ausente`, `tabla-empresa-2-sin-configuracion`) have a company with NO row in the configuration: the legacy `ConfiguracionSistema` dies with a `NullPointerException` (its per-company map is null). `ConfiguracionSistemaJdbc` answers `ParametroNoEncontradoException`. `VdoEquivalenciaTest.KIT_NPE_DE_CONFIGURACION` accepts `NullPointerException | ParametroNoEncontradoException | IllegalArgumentException` for those three slugs only (class only; everything else is still compared) and prints how many cases were relaxed. Either align the kit (shared: coordinate) or register a DEV and keep the set; emptying the set makes the comparison strict (3 red cases).

## 4. Test resources (created by `legacy-oracle/sync_vdo.py` from the oracle output)

`src/test/resources/fondomutuo-vdo/cases/<id>/{case.json,seed.sql,expected.json}` (the `colaboradores` of each case.json script `CalcularParidad`; `fondomutuo-3a/seed/doubles-base.json` has a default that does not fit this class), `fondomutuo-vdo/seed/base-vdo.sql`
(loaded after `fondomutuo-3a/seed/base-fmm.sql`), `fondomutuo-vdo/determinism-vdo.txt` (result of the 20 runs).
