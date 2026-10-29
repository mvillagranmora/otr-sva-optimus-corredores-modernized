# API assumed by the FM-CPFF tests (ConsultarCustodiaPortafolioFFMM, F3 W1 front 9) and by the chain tests

The production code does NOT exist yet. The tests of this package (`CasoFfmm`, `SujetoFfmm`, `FfmmEquivalenciaTest`, `FfmmDeterminismoTest`, `FfmmReglasTest`, `FfmmAdaptadorTest`, and for the chain
`CasoCadena`, `SujetoCadena`, `CadenaEquivalenciaTest`, `CadenaReglasTest`) are written against EXACTLY the classes, constructors and methods below, all in package
`cl.bcs.fondomutuo.consultas.custodiaffmm` (main source set) unless another package is named. Legacy FQN: `cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFFMM`.

**Decision (owner, 2026-10-28): FFMM is NOT a JavaCall entry of its own.** It has no registration in `PLA.Servicio` (Q-F3C-1) and the only Java caller is CPF, in process. So: NO list in
`fondomutuo.solicitud.listas-servidas`, NO `JavaCallHandler`, NO entry in `JavaCallDispatcher.LISTA_BLANCA`, NO gate. The production shape is **a service + a JDBC read port + an adapter that implements the
port CPF already consumes** (`consultas.custodia.ConsultarCustodiaPortafolioCustodiaPort`, `JsonObject consultar(JsonObject)`), answering the same JSON `{"p_out_tbl":[...]}` as the legacy `call`. Read only:
no lock, no Tesoreria, no sequences, no write port. If production needs another shape, change the tests (not the golden): nothing of the comparison depends on these names except the entry point marked (*).

**What FFMM really is** (cards FM-CPFF-01..22, defects D-F3C-01..15): five SQL of its own (`ConsultarCustodiaPortafolioFFMM_SelectConsultarCustodia / ...Historico / ...Garantia / ...GarantiaHistorico /
...PersonaDestino.sql`, already in `src/main/resources/cl/bcs/fondomutuo/sql/`, verbatim, SHA-256 equal to the legacy), `ConsultarEmpresa` (always, FIRST), `PLA.FechaSistema` and an in-memory grouping into
`HashMap`s. The oracle ran the REAL unmodified class; the only registered double was `ConsultarEmpresa`.

## 1. Classes (all main source set, package `cl.bcs.fondomutuo.consultas.custodiaffmm`)

| Class | Kind | Members the tests use |
|---|---|---|
| `ConsultarCustodiaPortafolioFfmmContrato` | holder of the JSON / row types | nested `public static class`: `Entrada` (the 14 fields of the legacy `EntradaConsultarCustodiaPortafolioFFMM` IN THIS ORDER with its defaults, see 2.1), `Salida` (`public List<SalidaCustodia> p_out_tbl = new ArrayList<>()`), `SalidaCustodia` (29 fields, see 2.2), and the row types the SQL are mapped to: `Custodia`, `Garantia`, `PersonaDestino` (public fields = the legacy classes of the same name, with their constructor defaults) and `Instrumento` (the grouping holder) |
| `ConsultarCustodiaPortafolioFfmmPort` | interface | `Salida logica(Entrada entrada)`: legacy `logica(con, entrada)` without the connection (the unit of work is the ambient Spring transaction, `readOnly`); NO checked exception (the legacy `throws Exception` is unchecked here: `ParametroNoEncontradoException`, `DateTimeParseException`, `NullPointerException`, ...); a functional interface |
| `ConsultarCustodiaPortafolioFfmmDatos` | read port | `garantiaVigente(Parametros)`, `garantiaHistorico(Parametros)`, `custodiaVigente(Parametros)`, `custodiaHistorico(Parametros)`, `personasDestino(Long idPersonaCuentaPropia)`; nested `final class Parametros` with PUBLIC fields `Long IdEmpresa, IdCliente, IdPortafolio, IdInstrumento, IdProductoAgrupador; String FechaConsulta, IdEmisor, IdFamiliaFondo, TipoFechaConsulta`: the rows of the five SQL, VERBATIM, with the SAME named parameters the legacy builds (see 3.1) |
| `ConsultarCustodiaPortafolioFfmmJdbc` | `@Component` adapter of the read port | constructor `(cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo conexion)`; `SqlRecursos.modulo(...)` + `SqlNombrado.consultar` (columns mapped by label to PUBLIC fields, no conversion) |
| `ConsultarCustodiaPortafolioFfmmService` | `implements ConsultarCustodiaPortafolioFfmmPort` | constructor `(cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort configuracion, cl.bcs.fondomutuo.port.out.kit.EmpresaPort empresas, ConsultarCustodiaPortafolioFfmmDatos datos)`; no state, no static state. Needs ONLY the company port (`empresas.informacion(p_IdEmpresa)` FIRST, FM-CPFF-05), the system configuration (`texto(p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema")`) and the read port: **no requirements port** (`RevisarRequisitosBasicos` is never called, FM-CPFF-04), no calendar, no series, no prices, no HTTP |
| `ConsultarCustodiaPortafolioFfmmFabrica` (*) | factory | `public static ConsultarCustodiaPortafolioFfmmPort crear(Dependencias d)`; nested `public static final class Dependencias` with PUBLIC non-final fields `configuracion` (`ConfiguracionSistemaPort`), `empresas` (`EmpresaPort`), `datos` (`ConsultarCustodiaPortafolioFfmmDatos`) |
| `ConsultarCustodiaPortafolioFfmmJson` | package-private | `static com.google.gson.Gson gson()`: the plain legacy `Utils.getGsonBuilder()` Gson (nulls WRITTEN, `serializeSpecialFloatingPointValues`, date format `yyyy-MM-dd HH:mm:ss.SSS`, length-based `Timestamp` / `Date` deserializers), a **NEW Gson PER CALL** (not a cached one: `FfmmAdaptadorTest.fmCpff03_unGsonNuevoPorLlamada`) |
| `ConsultarCustodiaPortafolioFfmmAdaptador` (*) | `implements consultas.custodia.ConsultarCustodiaPortafolioCustodiaPort` | constructor `(ConsultarCustodiaPortafolioFfmmPort servicio)`; `JsonObject consultar(JsonObject entrada)` = the legacy `call`: `gson.fromJson(entrada.toString(), Entrada.class)` -> `servicio.logica` -> `JsonParser.parseString(gson.toJson(salida)).getAsJsonObject()`. It catches NOTHING: every exception of the service goes up unchanged to CPF (FM-CPFF-22.6) |
| `ConsultarCustodiaPortafolioFfmmConfiguracion` | `@Configuration` | `public static final String PROPIEDAD_CERRADA = "fondomutuo.custodia.ffmm.cerrada"`; beans: the service (`ConsultarCustodiaPortafolioFfmmPort`, from the `ConfiguracionSistemaPort`, `EmpresaPort` and `ConsultarCustodiaPortafolioFfmmDatos` beans), the real adapter and the closed adapter (see 4) |

No `JavaCallHandler`, no `*Configuracion.LISTA`, no `FQN` constant: FFMM is not served on its own (the old FQN `cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFFMM` of `InternalJavaCallController`
belongs to the stub service `service.javacall.ConsultarCustodiaPortafolioFFMMService`, outside the Spring context: D-F3C-14 / Q-F3C-9; `FfmmReglasTest.dF3c14_servicioAntiguoNoImplementaLaClase` pins that it is NOT the class).

## 2. Contract types (field names = legacy, JSON = legacy)

### 2.1 `Entrada` (defaults = the legacy constructor; an absent key keeps the default, an explicit JSON `null` overrides it with null; `"7"` is coerced by Gson to 7; `"abc"` and `1.5` raise `JsonSyntaxException`)
`Long p_IdEmpresa; Long p_IdUsuario; String p_IdModulo; Long p_IdCliente = 0; Long p_IdPortafolio = 0; Long p_IdProductoAgrupador; String p_IdEmisor; Long p_IdInstrumento = 0; String p_IdBoveda = "";
String p_AgrupaPorBoveda = "N"; String p_AgrupaPorRelacion = "N"; String p_FechaConsulta = "0001-01-01"; String p_TipoFechaConsulta = "LIQ"; String p_IdFamiliaFondo = ""`. `p_IdUsuario`, `p_IdModulo` and
`p_IdBoveda` are received and NEVER read (D-F3C-03).

### 2.2 `Salida` and `SalidaCustodia`
`Salida` has ONLY `p_out_tbl` (lowercase). There is no error counter and no message list (D-F3A-27): the CallServlet decision would be ROLLBACK for EVERY response, so the harness records `decisionServlet =
ROLLBACK`. `SalidaCustodia` = the 29 fields of the legacy class, in declaration order, with NO defaults (every field is assigned in `logica`; `PPPCustodia` stays null when `CantidadPrecioPromedio = 0`):
`IdClasificacionTipoInstrumento, NombreClasificacionTipoInstrumento, IdTipoInstrumento, NombreTipoInstrumento, IdInstrumento (Long), Nemotecnico, IdEmisor, IdSocioDeNegocio (Long), IdCliente (Long),
IdPortafolio (Long), DocumentoIdentidad, RazonSocial, CodigoPortafolio, IdBoveda, Relacion, IdRegimenFondoMutuo, RegimenTributario, Producto, CantidadCustodia (BigDecimal), CantidadGarantia,
CantidadPrecioPromedio, MontoPrecioPromedio, INGPrecioPromedioCompra, INGMontoPrecioPromedioCompra, INGMontoPrecioPromedioMonedaInstrumento, TotalPosicion, IdMatrizAgrupador (Long), EsADC, PPPCustodia
(BigDecimal)`. Numbers are written with their `BigDecimal.toString` (`0E+8`, `980000.0000000000000000`, `9800`, `999999999999999999999999980000000000`): **the scale of every number is part of the golden**, which
means the same `MathContext.DECIMAL128` operations in the same order (including the plain `add` without `MathContext` of `MontoPrecioPromedio` / `CantidadPrecioPromedio`, D-F3C-11).

### 2.3 Row types of the SQL
`Custodia` (the columns of the custody SQL; constructor defaults `Cantidad, INGPrecioPromedioCompra, INGMontoPrecioPromedioCompra, INGMontoPrecioPromedioMonedaInstrumento, ValorCompra, PrecioCompra,
MontoCompra, MontoPrecioPromedio = ZERO` and `RegimenTributario = ""`: the historical SQL has no such column, so the default stays; an explicit NULL column OVERWRITES the default, which is how a null
`ValorCompra` becomes the `NullPointerException` of D-F3C-11), `Garantia` (`Cantidad = ZERO`; the extra `MontoCompraCarteraOrigen` is filled ONLY by the historical SQL and NEVER copied to the grouped guarantee:
D-F3C-01), `PersonaDestino {Long IdPersonaDestino}`, `Instrumento` (`Id` + the 20 descriptive fields, WITHOUT quantities; a group born from a guarantee leaves `RegimenTributario` null: D-F3C-08).

## 3. Behaviour the tests pin (bug for bug; the golden is the oracle, this is the index)

* **Order of the steps (and of the exceptions)** (FM-CPFF-05): `empresas.informacion(p_IdEmpresa)` FIRST, ALWAYS (a script error `Empresa no existe` is a plain `Exception`, nothing else is read); then
  `PLA.FechaSistema` (a missing parameter is `Exception("Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado")`, message compared); then the date of the entry (null => `NullPointerException`) and the
  system date, parsed like `Fecha.toDate`: **19 characters => `yyyy-MM-dd HH:mm:ss`, otherwise `yyyy-MM-dd`, both with `DateTimeFormatter.ofPattern` (resolver SMART: `2026-02-30` is accepted as 2026-02-28,
  D-F3C-16)**; then the two guarantee / custody queries; then the related persons (ALWAYS, D-F3C-12); then the grouping.
* **Source** (FM-CPFF-07, D-F3C-04): vigente when `"0001-01-01".equals(p_FechaConsulta)` OR the date is **not before** the system date. **Do not use `LocalDate.compareTo(..) != -1`**: it returns the day /
  month / year difference (-3, -2) and the legacy `java.sql.Date.compareTo` returns -1 / 0 / 1 (cases `historico-tres-dias-antes`, `historico-dos-meses-antes`, `historico-un-anio-antes` pin it). The raw text of
  the entry, NOT the system date, is what the SQL receives as `FechaConsulta` (the default `0001-01-01` gives an empty answer; a text with time makes the vigente SQL fail with `SQLException`).
* **Parameters** (3.1): the guarantee SQL are called WITHOUT `TipoFechaConsulta` (the kit raises `Parameter not found` for a parameter that is in the map but not in the SQL); the custody SQL WITH it
  (added after the guarantee call). `IdBoveda`, `IdUsuario`, `IdModulo` are never passed.
* **Grouping** (FM-CPFF-12..14): a `java.util.HashMap<String,..>` for custody, guarantee and group, **default capacity, filled in the order of the SQL rows (custody first, then guarantee)**; the key
  `IdInstrumento_Nemotecnico_IdEmisor_IdCliente_IdPortafolio_IdRegimenFondoMutuo[_relacion][_boveda]` (a null is the text `null`); the output order is the iteration order of THE GROUP map
  (`FfmmEquivalenciaTest` reports a different order as an ORDER difference; `FfmmReglasTest.fmCpff16_*` fix 6, 14, 16 and 20 keys). Relation: `S` exact (not `s`): client = `empresa.IdCliente` => `CPRO`,
  person in the set of `personasDestino(empresa.IdPersona)` => `REL`, else `TER`; a null `IdPersona` is `TER`; the set accepts a null element (D-F3C-18).
* **Arithmetic** (FM-CPFF-13/15, D-F3C-01/02/10/11): exactly the legacy operations and `MathContext`s (see 2.2). With a guarantee the price amount is reset to ZERO (`MontoCompraCarteraOrigen` is never copied).
* **Exceptions**: class compared (the message only for a plain `Exception`: the missing parameters, `Empresa no existe`). KNOWN KIT DEVIATION **DESVIO-KIT-1** (decided by the owner: the kit's behaviour is KEPT): for
  `empresa-ausente`, `empresa-nula` and `empresa-sin-configuracion` the legacy raises `NullPointerException` and the kit `ParametroNoEncontradoException`; the harness accepts either for those three slugs ONLY
  (`CasoFfmm.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION`).
* Read only: the tables of the dump come back unchanged; no session-local table, no autonomous commit, no sequence.

### 3.1 Parameters of each SQL (what the legacy `HashMap` holds)
Custody (current and historical): `IdEmpresa, IdCliente, IdPortafolio, IdInstrumento, FechaConsulta, IdProductoAgrupador, IdEmisor, IdFamiliaFondo, TipoFechaConsulta`. Guarantee (current and historical): the same
WITHOUT `TipoFechaConsulta`. Related persons: `IdPersonaCuentaPropia` (= `empresa.IdPersona`; the old stub service passes the company id instead: D-F3C-14). A null value is bound as SQL NULL.

## 4. How the adapter replaces `ConsultarCustodiaPortafolioCustodiaCerrada` (proposal; `FfmmAdaptadorTest.seleccion_*` and `guardiaM1_sigueVigente` pin it)

Today `CustodiaCerrada` is a `@Component` that fails closed (503, gate `G-CUS`), deliberately not `@ConditionalOnMissingBean`, so that the real adapter must replace it explicitly. The rule proposed:

1. **The real adapter is the bean.** `ConsultarCustodiaPortafolioFfmmConfiguracion` declares exactly one bean of `ConsultarCustodiaPortafolioCustodiaPort`:
   `@ConditionalOnProperty(name = PROPIEDAD_CERRADA, havingValue = "false", matchIfMissing = true)` => `ConsultarCustodiaPortafolioFfmmAdaptador`.
2. **`CustodiaCerrada` stays as a kill switch, not as a default**: it loses `@Component` (it would be a second bean) and the same configuration declares it with
   `@ConditionalOnProperty(name = PROPIEDAD_CERRADA, havingValue = "true")`. Operators close the in-process custody calculation by setting `fondomutuo.custodia.ffmm.cerrada=true`; there is ALWAYS one bean.
3. **The M1 guard of `consultas/ListasLecturaArranque` stays unchanged** (`custodia instanceof ConsultarCustodiaPortafolioCustodiaCerrada`): with the switch closed and CPF listed the start-up still fails; with the
   adapter and CPF listed it starts. CPF itself stays closed by default (its list is empty by default), so wiring the real adapter changes no served behaviour until an operator opens `CONSULTAR_CUSTODIA_PORTAFOLIO`.
4. Existing tests that name `ConsultarCustodiaPortafolioCustodiaCerrada` (`ListasLecturaArranqueTest`, `ListasLecturaArranqueRealTest`, `ListasLecturaValidadorTest`, `RreCompuertasTest`) were re-run on a throwaway
   implementation: all pass but ONE. **`ListasLecturaArranqueRealTest.cpfAbiertaConLaCustodiaCerrada_elArranqueFalla_M1` must be changed by the same production change**: it starts the REAL context with CPF listed and expects the
   M1 failure because, today, the scanned default is the closed adapter; with this change the default is the real adapter and the start-up succeeds, so the test has to pass
   `--fondomutuo.custodia.ffmm.cerrada=true` to keep pinning M1 (`FfmmArranqueRealTest` pins both: real adapter starts, switch closed + CPF open fails, switch closed without CPF starts with the closed bean).

Relation to the in-process custody library (`BcsCustodiaServicios`, `fondomutuo.custodia.habilitada`, only `ConsultarFlujoCustodiaFlujo` is wired today): FFMM does not use it (it reads the tables itself). If the
owner later prefers FFMM inside that library, only the package and the wiring change: the contract of `consultar(JsonObject)` and every golden stay.

## 5. Test resources (created by `legacy-oracle/sync_ffmm.py` and `sync_cpfchain.py` from the oracle output)

* FFMM alone: `src/test/resources/fondomutuo-ffmm/cases-ffmm/<id>/{case.json,seed.sql,expected.json}` (the cases live in `cases-ffmm/`, NOT `cases/`), `schema-ffmm.sql` (H2 STAND-INS: `CUS.Cartera` dropped and re-created
  with 22 columns, `HIS.Cartera`, `GTA.Garantia`, `HIS.Garantia`, 9 tables / views the F1 schema lacks, 4 added columns, the alias `MAP`; every column tagged REAL (catalog CSV or the owner's HANA QA check of
  2026-10-28) or ASSUMED), `seed/base-ffmm.sql`, `determinism-ffmm.txt`. Test source created by the same script: `src/test/java/cl/bcs/oracle/H2FnFfmm.java` (the H2 function `MAP`).
* The chain: `src/test/resources/fondomutuo-cpfchain/cases-cpfchain/<id>/...`, `schema-cpf.sql` and `seed/base-cpf.sql` (BYTE COPIES of the CPF front's, loaded first), `schema-cpfchain.sql` and
  `seed/base-cpfchain.sql` (what FFMM adds), `determinism-cpfchain.txt`. It reuses `src/test/java/cl/bcs/oracle/H2FnCpf.java` of the CPF front.
* The F1 schema and base seed come from `fondomutuo-3a/`.

## 6. The chain (part B): how CPF + FFMM are wired in the tests (`SujetoCadena`)

`ConsultarCustodiaPortafolioFabrica.Dependencias.custodia = new ConsultarCustodiaPortafolioFfmmAdaptador(ConsultarCustodiaPortafolioFfmmFabrica.crear(...))` with the SAME `ConexionUnidadDeTrabajo` (one connection, one
unit of work) and the SAME case script for `ConsultarEmpresa` (called by FFMM first and by CPF later). No double of the custody calculation. This is the wiring the Spring start-up produces once rule 4.1 holds
(`ConsultarCustodiaPortafolioConfiguracion` takes the `ConsultarCustodiaPortafolioCustodiaPort` bean).
