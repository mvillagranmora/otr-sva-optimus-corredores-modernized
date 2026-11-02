# API assumed by the F3 W3 T6-B tests (`LiquidacionSolicitudAporteRescate`, served by the router, closed by default)

The production code of T6-B does NOT exist yet (red before code). The tests (`ApiT6b`, `LiqEquivalenciaTest`, `LiqDeterminismoTest`, `LiqReglasTest`, `LiqPuertoTest`, `LiqHandlerTest`, `LiqEstructuraTest`,
`LiqCableadoTest` and the harness `CasoLiq` / `CosturaLiq` / `ConexionLiq`, all in `cl.bcs.fondomutuo.liquidacion`) COMPILE without it: the new classes and constants are reached by reflection in `ApiT6b` (the only
file that knows their names) and every test that needs them fails with `AssertionError: T6-B not implemented: the production class ... does not exist yet`.

Decisions of the user (approved): port ONLY `logica` (RULE-076: never the legacy `main`); target package `cl.bcs.fondomutuo.liquidacion`; a closed-by-default direct JavaCall handler for the legacy class (registry id 1689,
FQN `cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate`), following the T4-C pattern: own list in `fondomutuo.solicitud.listas-servidas`, G0 as the FIRST statement, in `ManejadorConfiguracion.LISTAS_ESCRITURA_UI`,
NOT in `JavaCallDispatcher.CLASES_LECTURA`, whitelist 13 -> 14. No `RULE-nnn` exists for this class: the tests name the cards `FM-LIQ-01..20` of `RULES_FONDOMUTUO_F3_B.md` 8.3 and the defects `D-F3B-nn` of its section 9.

Oracle: the goldens of `src/test/resources/fondomutuo-liq/cases/` (156 cases, 20 runs each, 0 unstable) were recorded from the UNMODIFIED legacy class running in H2 (`legacy-oracle/record_liq.sh`), with registered doubles
for Tesoreria `CrearIngresoEgreso`, CuentaInversion `CrearCargoAbono` and Plataforma `AgregarLog` (every call recorded in order with its arguments) and the shim `Utils.getSecuencia` over H2 sequences.

## 1. Classes and constants (package `cl.bcs.fondomutuo.liquidacion` unless said)

| Item | Shape the tests use |
|---|---|
| `LiquidacionContrato` | `public final` holder of PUBLIC static classes with PUBLIC fields, legacy names and order: `Entrada` (`Long p_IdEmpresa, p_IdUsuario; String p_IdModulo, p_SimularOperacion, p_UsaReglasNegocio; List<InMovimientos> p_Movimientos = new ArrayList<>(); List<InFiltros> p_Filtros = new ArrayList<>()`), `InMovimientos` (`Long IdMovimiento`), `InFiltros` (`Long IdInterno; String IdEmisor, TipoSolicitud, IdEstado, FechaCertificacion, FechaLiquidacion, IdSerieFondo`), `Salida` (`List<ResultadoLiquidacion> P_OUT_RESULTADO = new ArrayList<>(); List<cl.bcs.fondomutuo.kit.MensajeSalida> P_OUT_MENSAJES = new ArrayList<>(); Long P_OUT_ERRORESCONTADOR = 0L`, in THIS field order), `ResultadoLiquidacion` (`Long IdInterno, IdSolicitudAporteRescate; String IdSerieFondo, Codigo, Estado, NombreEstado`), `SolicitudProceso` (the legacy `SolicitudesProcesoLiquidacion`: `Long IdInterno = 0L, IdSolicitudAporteRescate; String Codigo = "UI_EXITO", Descripcion = "", TipoSolicitud, IdSerieFondo; Long IdInstrumento; String IdTipoInstrumento; Long IdPortafolio; String IdMoneda, IdMonedaFondo; BigDecimal CostoMonedaLocal, Monto; java.sql.Date FechaLiquidacion, FechaCertificacion; Long IdPortafolioAGF; String IdMedioDePago; Long IdPersonaAGF; String IdMedioDePagoAbonoAGF, IdMedioDePagoCargoAGF, EstadoLiquidacion; BigDecimal MontoPago, MontoComision, MontoPagoLocal, MontoComisionLocal; Long IdOmnibus, Rechazo = 0L, FolioCartaLiquidacion = 0L, IdPersona; String CodigoPortafolio, IdTipoMovimientoFondoMutuo; Long IdMatrizAgrupadorAPV`), `ModalidadPago` (`String IdMedioDePago, IdModalidadDePago`), `Retencion` (`String CodigoRetiro; Long IdPersonaRetencion, IdRegimenTributario; BigDecimal PorcentajeRetencion, MontoTopeRetencion`). Public no-argument constructors; the codec of the legacy `call` (`JsonLegacy.conNulos()`) must read and write them as the legacy does |
| `LiquidacionPort` | `public interface`, ONE method: `Salida logica(Entrada entrada) throws Exception` (legacy `logica(con, entrada)` without the connection). It does NOT recount the errors (the counter stays what the collaborators put, `0L` by default) |
| `LiquidacionDatos` | `public interface`, exactly five methods, none declaring a checked exception: `List<SolicitudProceso> porMovimientos(Long idEmpresa, List<Long> idMovimientos)`; `List<SolicitudProceso> porFiltro(Long idEmpresa, String idEmisor, String tipoSolicitud, String idEstado, String fechaCertificacion, String fechaLiquidacion, String idSerieFondo)` (the facade already applied the rule of the empty date); `List<ModalidadPago> modalidadesPago()`; `List<Retencion> configuracionRetencion()`; `void actualizar(List<SolicitudProceso> solicitudes)` |
| `LiquidacionDatosJdbc` | `public class`, `implements LiquidacionDatos`, ONE public constructor `(ConexionUnidadDeTrabajo)`, final fields only. Loads the five SQL files VERBATIM (SHA-256 in `ApiT6b.SQL_SHA256`): `cl/bcs/fondomutuo/sql/LiquidacionSolicitudAporteRescate_SelectSolicitudes.sql`, `..._SelectSolicitudesFiltros.sql`, `..._UpdateSolicitudes.sql`, `ComunesSolicitudes_SelectModalidadPago.sql` (these four already exist, byte-identical to the legacy) and `cl/bcs/APV/sql/ConsultarRetencion.sql` (to be COPIED verbatim into `src/main/resources`); reads with `SqlNombrado.consultar`, writes ONLY with ONE `SqlLote.ejecutar` (the UPDATE as the legacy `SQL.batch`: public fields of the bean as parameters, BigDecimal at scale 16, which runs `SqlNombrado.actualizarLote`); no SQL literal of its own, no commit, no rollback. The IN list of the movements is the named parameter `p_Movimientos`; the select by filter uses `p_IdEmpresa, IdEmisor, TipoSolicitud, IdEstado, FechaCertificacion, FechaLiquidacion, IdSerieFondo` |
| `LiquidacionFachada` | `public class`, `implements LiquidacionPort`, ONE public constructor `(LiquidacionDatos, ConfiguracionSistemaPort, DiccionarioMensajesPort, SecuenciaPort, ProcesarIngresoEgresoTesoreriaService.CrearIngresoEgresoPuerto, CrearCargoAbonoPort, LogPort)`, final fields only, no static mutable state, no `java.sql`/Spring/`SqlNombrado` in its source; the sequences are requested through `SecuenciaPort.siguientes` (the name of each sequence appears ONCE in the source: `BSCL.FondoMutuo.BD.Secuencias::FolioCartaLiquidacionAporteRescate` and `...::SecuenciaComprobanteLiquidacion`) |
| `LiquidacionJavaCallHandler` | `@Component`, `public`, `implements JavaCallHandler`, ONE public constructor `(LiquidacionPort, Compuertas)`, final fields. `fqn()` = `JavaCallDispatcher.LIQUIDACION_SOLICITUD_APORTE_RESCATE`. `call(Connection con, JsonObject in) throws Exception` starts with `if (!compuertas.listasServidas().contains(LiquidacionConfiguracion.LISTA)) { throw new ServicioNoServidoException("G0", ...); }` as its FIRST statement (the structure test reads the source) |
| `LiquidacionConfiguracion` | `@Configuration`; `public static final String LISTA = "LIQUIDACION_SOLICITUD_APORTE_RESCATE"`; `@Bean LiquidacionDatos` (the JDBC adapter over `ConexionUnidadDeTrabajo`) and `@Bean LiquidacionPort` (the facade over the beans of the context: `ConfiguracionSistemaPort`, `DiccionarioMensajesPort`, `SecuenciaPort`, Tesoreria in process as `crearIngresoEgreso::logica` of the existing bean `CrearIngresoEgresoService`, `CrearCargoAbonoPort`, `LogPort`). Any OTHER `@Bean` must be `@ConditionalOnMissingBean` and must not be a `CrearCargoAbonoPort`, `LogPort`, `CrearIngresoEgresoPuerto` or `SecuenciaPort` |
| `JavaCallDispatcher.LIQUIDACION_SOLICITUD_APORTE_RESCATE` | `public static final String` = `"cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate"`; added to `LISTA_BLANCA` (13 -> 14); NOT in `CLASES_LECTURA` (stays 8) |
| `ManejadorConfiguracion.LISTAS_ESCRITURA_UI` | gains `LiquidacionConfiguracion.LISTA` (size 3: `CARGAR_VALOR_CUOTA`, `CERTIFICAR_SOLICITUD_APORTE_RESCATE`, the new one). `LISTAS_LECTURA` unchanged (8) |

NO new port is introduced for the three collaborators: Tesoreria (`CrearIngresoEgresoService`, in process), CuentaInversion (`CrearCargoAbonoPort`, whose fail-closed replacement is in `CertificarAporteRescateConfiguracion`) and the log
(`LogPort`) already have a bean each; `LiqCableadoTest` asserts exactly ONE bean of each, so a second one (or a second fail-closed replacement) makes it fail. If production does add a port without an adapter it must be a
fail-closed `@ConditionalOnMissingBean` replacement (`ServicioNoServidoException` before the argument is read).

## 2. Behaviour (`logica`, legacy L:215-736; cards FM-LIQ-01..20)

Order of events (pinned by `LiqPuertoTest`): parameters `PLATAFORMA/PLA.FechaSistema`, `FONDOMUTUO/LiquidacionCuentaInversionCliente`, `FONDOMUTUO/VALIDAR_LIQUIDACION_OMNIBUS` (company of the request), the modalities (`toMap` by medium: a
repeated medium raises `IllegalStateException`), the validation of the filters, `PLATAFORMA/APV.Habilitado` with company **1 fixed**, the selection, the omnibus removal, the grouping, the two sequences (G or G + S with APV, asked
even for 0), the withholding configuration (only with APV), the lines, Tesoreria, one UPDATE per generated reference, the CIN loop, the log.

1. FM-LIQ-03: no filters adds `NO_SE_INGRESARON_FILTROS` (id 0, `tipo` = the constant `MensajeSalida.MENSAJE_ERROR` BY REFERENCE, text from the dictionary asked with an empty NON-null map) and CONTINUES. No movements and no filters:
   `NoSuchElementException("No value present")` (`findFirst().get()`).
2. FM-LIQ-04/05: movements select by `IdMovimiento` (ids as given, repeats and null included); with none, ONLY the first filter selects: texts as given, an EMPTY `FechaCertificacion` / `FechaLiquidacion` becomes `0001-01-01`
   (it filters by it), a null date raises `NullPointerException` before the select.
3. FM-LIQ-06: `VALIDAR_LIQUIDACION_OMNIBUS = "S"` removes the APORTES (`TipoSolicitud = "APO"`) with `IdOmnibus` exactly 0, in SILENCE (no message).
4. FM-LIQ-07: groups in a `java.util.HashMap` keyed by `Grupo{IdPersonaAGF, TipoSolicitud, IdMoneda = IdMonedaFondo, IdPortafolioAGF, IdMedioDePago (Cargo for an aporte, Abono otherwise), FechaLiquidacion, FechaCertificacion}` with the legacy
   `hashCode` (31 x hash(IdMoneda) + SUM of the other hashes, `java.sql.Date` in the JVM zone) and `equals`; the groups are visited IN THE ORDER OF THAT MAP (numerous goldens depend on it: Q-T6B-4). A null field of the key raises `NullPointerException`.
5. FM-LIQ-09: the line of a group (every field is pinned by `LiqReglasTest.laLineaDeUnAporte`), the payment form (modality from the table or `MODCAJ`).
6. FM-LIQ-10/11/12: with APV, one client line per solicitud (reference G + index), retention for `RESNORM` / `RESTRASP` of `APV-B` with a medium other than `CIN` (first `RES` row with percentage <> 0 and regimen null or equal), routing by
   medium and type, and `carta_solicitud[reference] = the COMPLETE list`.
7. FM-LIQ-13/14: Tesoreria is always called once (empty lists too) with `p_EsIngresoAutomatico = "N"`; a counter > 0 returns ITS counter and ITS messages (the `tipo` String is passed on as the same reference). Per generated reference the
   solicitudes of `carta_solicitud[reference]` get `FolioCartaLiquidacion = carta[reference]`, are updated in a batch and reported (`Codigo` `UI_EXITO`, `Estado` = the state READ).
8. FM-LIQ-15/16/17: with the client's account `S`, per CIN solicitud ALL the lines go to `CrearCargoAbono` (always `MOVCAR` / `CAPEFM`), errors replace the messages, and per generated movement `carta_solicitud[0]` is re-updated with
   `secuencia_carta.get(K)`, K = index of the solicitud (so two solicitudes in group 0 raise `IndexOutOfBoundsException`). A message parsed from the JSON of the collaborator must keep a `tipo` that is NOT the interned constant.
9. FM-LIQ-18: `AgregarLog` is called ONCE with the default entry (`p_IdTrabajo` 1, empty `p_Log` and `p_LogDetalle`).
10. The handler (`call`): `JsonLegacy.conNulos()` in and out (null keys written, field order of the classes) and the RECOUNT `P_OUT_ERRORESCONTADOR = ConteoErrores.errores(P_OUT_MENSAJES)` (reference comparison, D-REQ-11).
11. Exceptions: nothing is caught; whatever a port raises goes up UNCHANGED (same instance).

## 3. JSON the collaborators see (the goldens are the contract)

* Tesoreria: the typed `CrearIngresoEgreso_Entrada` of `cl.bcs.tesoreria.javacall.CrearIngresoEgresoService`, written with `JsonLegacy.conNulos()`, EVERY field equal to the golden (header, `p_IngresoEgresoGenerar`, `p_FormaDePago`).
* CuentaInversion: `CrearCargoAbonoPort.crear(JsonObject)` with the legacy `CrearCargoAbonoEntrada` JSON, nulls written (`FechaVencimientoOperacion`, `IdPersonaPagoCobro`, `EsIndicadorBilateral`, `IdPortafolioTraspaso`, `IdComprobante`, `Cantidad` are `null`).
* Log: `LogPort.agregar(JsonObject)` with `{"p_IdEmpresa":null,"p_IdUsuario":null,"p_IdModulo":null,"p_UsaReglasNegocio":null,"p_IdEstadoAutorizacion":null,"p_IdTrabajo":1,"p_Log":[],"p_LogDetalle":[]}`.

## 4. Test schema (fixtures)

`schema-liq.sql` adds to the H2 stand-in of the view `FMM.ConsultarSolicitudAporteRescate` the columns `IdPersonaAGF`, `IdPortafolioAGF`, `IdMedioDePagoAbonoAGF`, `IdMedioDePagoCargoAGF`, `IdMatrizAgrupadorAPV`, `IdOmnibus`
(ASSUMED: in no catalog), widens `Monto` to `DECFLOAT(34)` (HANA `DECIMAL(34)` without scale), creates the two sequences, `PAG.MedioDePagoYModalidadDePago` and `TRA.ConfiguracionRetencionTraspaso` (ASSUMED shape). `seed/base-liq.sql`: parameters
(`LiquidacionCuentaInversionCliente` N, `VALIDAR_LIQUIDACION_OMNIBUS` N, `APV.Habilitado` N), two dictionary rows and three modalities. The harness bridges HANA's `UPDATE alias SET .. FROM "t" AS alias WHERE ..` for H2 (`ConexionLiq`, the same
regex as the oracle's); production keeps the verbatim HANA text.

## 5. Existing tests that conflict with the step (NOT edited here; change them with the implementation)

`CerT4cCableadoTest` (`hasSize(13)`), `CerT4cHandlerTest` (`assertEquals(13, ...)`), `CerPfCableadoTest` and `CerPfEstructuraTest` (`hasSize(13)`, `LISTAS_ESCRITURA_UI` `hasSize(2)`), `FondoMutuoCableadoRealTest` (`hasSize(13)`): all
assume the allow-list of 13 and two UI write lists; T6-B makes them 14 and 3. They pass today and will go red the day `LISTA_BLANCA` gains the 14th FQN.

## 6. Open questions (each with a recommendation)

* **Q-T6B-1 (type of the messages).** `Salida.P_OUT_MENSAJES` is assumed to be `List<cl.bcs.fondomutuo.kit.MensajeSalida>` (public fields `id, tipo, codigo, descripcion`), the type the other ports of the module use; `LiqHandlerTest` and
  `LiqPuertoTest` build it. A different but JSON-identical class would need two lines of `LiqHandlerTest` changed. Recommendation: the kit class.
* **Q-T6B-2 (Tesoreria port).** The facade takes the EXISTING typed port `ProcesarIngresoEgresoTesoreriaService.CrearIngresoEgresoPuerto` (`CrearIngresoEgreso_Salida logica(CrearIngresoEgreso_Entrada) throws Exception`), wired as
  `crearIngresoEgreso::logica` of the in-process bean. Recommendation: keep; no new bean of that interface.
* **Q-T6B-3 (checked exceptions of `CrearCargoAbonoPort.crear`).** The port declares none while the legacy `CrearCargoAbono.logica` throws `Exception`; the goldens compare the exception CLASS. Same question as Q-T4C-1: recommendation, add
  `throws Exception` to the port (or sneaky-throw in the adapter); the tests do not force one (the fakes throw with a sneaky lambda and the equivalence compares the class).
* **Q-T6B-4 (order of the groups).** The goldens pin the order of a `HashMap<Grupo,...>` whose key hashes `java.sql.Date` (milliseconds in the zone of the JVM, UTC in the oracle and in the tests). A port that groups with a `LinkedHashMap`,
  a `TreeMap`, a `LocalDate` key or a different `hashCode` gets other folios (about 20 cases fail). Recommendation: copy `Grupo` (fields, `hashCode`, `equals`) and `java.sql.Date` as they are; on HANA the zone of the host decides the same way.
* **Q-T6B-5 (the recount by reference and the doubles).** The messages of the registered doubles come from parsed JSON, so they never count in the recount of `call` (cases `tesoreria-con-error-...`, `cuenta-de-inversion-con-error-...`:
  counter 0, COMMIT). With the REAL in-process Tesoreria their `tipo` is the interned constant and WOULD count. The tests pin the first (what the oracle can record); the second needs a dual run with the real collaborators. The facade must pass the
  `tipo` String on by reference (never `new String`, never `intern()`): `LiqPuertoTest.unErrorDeTesoreria`.
* **Q-T6B-6 (view columns).** `IdPersonaAGF`, `IdPortafolioAGF`, `IdMedioDePago{Abono,Cargo}AGF`, `IdMatrizAgrupadorAPV` and `IdOmnibus` of the view are in no catalog (ASSUMED types); `MontoComision` is NOT NULL in the catalog, so the
  legacy `NullPointerException` of a null commission (D-F3B-29) cannot be seeded and has no case. Recommendation: verify the five columns in HANA QA (read only) before the dual run.
* **Q-T6B-7 (the withholding SQL).** `cl/bcs/APV/sql/ConsultarRetencion.sql` must live in `src/main/resources` of THIS module (the test reads the file); if an APV library ever shares the classpath, the same resource path would clash:
  `SqlRecursos.cargar` takes the first on the classpath. Recommendation: keep the module's copy first.
* **Q-T6B-8 (size of the IN list).** `p_Movimientos` expands into an IN list of as many parameters as ids; the cases use at most 12 ids. HANA's limit and the behaviour with hundreds of ids are NOT pinned.
* **Q-T6B-9 (QD-F3B-11/12, for the expert).** The tests pin that Liquidacion does not require a certified state by the movements path (FM-LIQ-04), that `0001-01-01` filters (FM-LIQ-05) and that the APV flag uses company 1 (FM-LIQ-02): all kept
  bug for bug (Q-01 unanswered); a person decides whether any of them is fixed after equivalence.
