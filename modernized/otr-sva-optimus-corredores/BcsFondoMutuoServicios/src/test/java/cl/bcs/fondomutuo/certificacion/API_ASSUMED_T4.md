# API assumed by the FMM-CER T4 tests (F3 W3, tanda T4-B: fronts F-G Facturacion and F-I reinversion and switch of `CertificarSolicitudAporteRescate`)

The production code of T4-B does NOT exist yet (red before code). The tests of this package for T4 (`ApiT4`, `CasoCerT4`, `ParadaT4`, `CerCosturaT4`, `CerT4EquivalenciaTest`, `CerT4DeterminismoTest`,
`CerT4ReglasTest` (generated), `CerT4CompuertaTest`) COMPILE without it: the new classes are reached by reflection in `ApiT4` (the only file that knows their names) and every test that needs them
fails with `AssertionError: T4 not implemented: the production class ... does not exist yet`. T1 (`API_ASSUMED.md`), T2 (`API_ASSUMED_T2.md`) and T3 (`API_ASSUMED_T3.md`) are untouched and stay green.
Legacy: `cl.bcs.fondomutuo.CertificarSolicitudAporteRescate` (`logica`, C:724-787 and 838-845; `creaMovimientoFacturacion` C:2159-2309, `creaComprobanteFacturacion` C:2311-2341,
`crearAporteReinversion` C:2343-2557, `certificarAportesSwitch` C:2764-2775, `cambiarMontoMonedaSistemaRescatesReinversionInternacionalesSinSpot` C:2820-2835). Cards FM-CER-48..51 (F-G) and
FM-CER-52..58, 63, 64 (F-I) of `RULES_FONDOMUTUO_F3_B.md` 8.1; AR through its answer (FM-REI-41..68 are the rules of AR itself: another front). Defects D-F3B-01/03/04/13/30/32 and the new
D-F3B-45..50 (see `TRANSFORMATION_NOTES.md`, "F3 W3 T4-B (pruebas)").

## 0. THE SEAM of T4 and what it pins

T1's seam `CertificarAporteRescateFasesPosteriores.continuar(contexto)` stays where it is (right after F-B). The harness (`CasoCerT4`) runs the T1 facade (F-A, F-B REAL, REAL JDBC adapters) and, at the
seam, runs the steps of F-C..F-I over the `CertificarAporteRescateContexto` it receives, in the order of the legacy `logica`, and throws `ParadaT4`:

| # | step | legacy lines | cards | what it does |
|---|---|---|---|---|
| 1..8 | the T2 / T3 steps: `validarSaldo`, `prepararRescates`, `recalcularCostoUF`, `ingresarCustodia`, `moverAportes`, `retirarCustodiaYCostear`, `moverRescates`, `moverTesoreria` | C:575-721 | FM-CER-21..47 | T2 / T3, implemented |
| 9 | `CertificarAporteRescateFacturacion.facturar(ctx)` | C:724-759, 2159-2341 | FM-CER-48..51 | `CrearMovimientoComprobante` for the rescates with commission and, with the parameter, `CrearComprobante` |
| 10 | `CertificarAporteRescateReinversion.reinvertir(ctx)` | C:762-787, 2343-2557 | FM-CER-52..58 | the aportes of the reinversion, ONE call to AR, mapping of ids, `ctx.aportesSwitch` |
| 11 | `CertificarAporteRescateSpot.moverSpot(ctx)` | C:790-830 | FM-CER-59, 60 | T3, implemented |
| - | front J (UPDATE to CRT, notifications) | C:835-836 | FM-CER-61, 62 | NOT run; the harness emulates ONLY the column FM-CER-64 reads ("J-lite": `MontoMonedaSistema` of the pending solicitudes) |
| 12 | `CertificarAporteRescateSwitch.certificarAportesSwitch(ctx)` | C:838-843, 2764-2775 | FM-CER-63 | the child certification of `ctx.aportesSwitch` through `CertificacionHijaPort`; adds its counter, messages and results |
| 13 | `CertificarAporteRescateSwitch.cambiarMontoMonedaSistema(ctx)` | C:845, 2820-2835 | FM-CER-64 | reads the SUM by related rescate and UPDATEs `MontoMonedaSistema` (the ONLY write of F-C..F-I) |

Every step is a NO-OP when its list is empty, reads and writes ONLY the context, its ports and (step 13) its own write port, adds counter and messages to `ctx.salida`, and lets any exception go up
UNTOUCHED (the legacy has no try/catch there). The results of the child go FIRST in `ctx.salida.P_OUT_RESULTADO` (the legacy adds them before the rows of front J).

## 1. Classes (package `cl.bcs.fondomutuo.certificacion`, main source set)

| Class | Kind | Members the tests use |
|---|---|---|
| `FacturacionPort` | interface, TWO methods | `JsonObject crearMovimientoComprobante(JsonObject)`, `JsonObject crearComprobante(JsonObject)`: the legacy JSON of `CrearMovimientoComprobante_Entrada` / `_Salida` and `CrearComprobante_Entrada` / `_Salida` (G6). NO production implementation |
| `CrearAporteReinversionPort` | interface, ONE method | `JsonObject crear(JsonObject)`: the legacy JSON of `EntradaCrearSolicitudAporteReinversion` -> `SalidaCrearSolicitudAporteReinversion`. AR is not ported in T4-B (another front ports it into its own package); NO implementation here |
| `CertificacionHijaPort` | interface, ONE method | `CertificarAporteRescateContrato.Salida certificar(CertificarAporteRescateContrato.Entrada) throws Exception`: the recursion; its implementation is the facade of T5 |
| `CertificarAporteRescateFacturacion` | `final`, no state | constructor `(FacturacionPort, CertificarAporteRescateDatos)`; `void facturar(ctx) throws Exception` |
| `CertificarAporteRescateReinversion` | `final`, no state | constructor `(CrearAporteReinversionPort, CertificarAporteRescateDatos, cierre.FormatoMoneda)`; `void reinvertir(ctx) throws Exception` |
| `CertificarAporteRescateSwitch` | `final`, no state | constructor `(CertificacionHijaPort, CertificarAporteRescateDatos, CertificarAporteRescateEscrituraMonto)`; `void certificarAportesSwitch(ctx)`, `void cambiarMontoMonedaSistema(ctx)` |
| `CertificarAporteRescateContexto` (EXISTS) | public fields | gains `List<CertificarAporteRescateColaboradores.MovimientoSalida> carterasRetiradas`, filled by `retirarCustodiaYCostear` (the reinversion reads it: FM-CER-53); additive |
| `CertificarAporteRescateColaboradores.MovimientoSalida` (EXISTS) | JSON pojo | gains `BigDecimal Cantidad` and `String FechaOrigenCompra` (the answer of `ProcesarCustodiaFondoMutuo` that the reinversion reads); additive |
| `CertificarAporteRescateDatos` (EXISTS) | read port | gains: the destinations of the reinversion (`ReinversionSerieFondoDestino` joined to series and fund), the origin series flags of the rescates (`ReinversionSerieFondosSolicitudRescateReinversion`, with the empresa), the IVA (`COM.Cobro`, first row; no row / null value are the legacy's `IndexOutOfBounds` / `NullPointer`), the sum of `MontoMonedaSistema` by related rescate. The harness builds the real `CertificarAporteRescateDatosJdbc`: the method names are NOT part of the contract |
| `CertificarAporteRescateEscrituraMonto` (NEW) | write port | the UPDATE of `MontoMonedaSistema` of FM-CER-64 (legacy SQL `UpdateMontoMonedaSistemaSolicitudesReinversion`, one batch); joins the ambient transaction, never commits. `CertificarAporteRescateEscritura` (T1, the log) is NOT touched: `CerCompuertaTest` implements it with a lambda. The harness builds `CertificarAporteRescateEscrituraMontoJdbc(ConexionUnidadDeTrabajo)` by reflection |
| `kit.Compuertas` (EXISTS) | gate | gains `g6Facturacion()` (property `fondomutuo.facturacion.habilitada`, default false, fail closed): the 6- and 8-argument constructors NEVER open it (`CerT4CompuertaTest`); the unit does NOT call it: the host does, after the validations and before the first write (T5) |

The typed ports that exist are reused as in T3 (`ProcesosExternos.TesoreriaPort`, `SpotPort`, `ConfiguracionSistemaPort`). The units get NO `Compuertas`, no connection and no sequence port.

## 2. What the arguments to the collaborators look like (the legacy JSON, compared by value)

The tests compare, for every case, the calls made AFTER F-B to the collaborators of T2 / T3 AND, new in T4, `CrearMovimientoComprobante`, `CrearComprobante`, `CrearSolicitudAporteReinversion` and the child
certification (name `CertificarSolicitudAporteRescate.hijo`): name, COMPLETE arguments, answer, order. Key facts the goldens show:

* `CrearMovimientoComprobante`: `{p_Movimientos [...], p_MovimientosFormaDePago [...], p_MovimientosCobros [...], p_IdEmpresa, p_IdUsuario, p_IdModulo, p_simularOperacion "N", p_UsaReglasNegocio "S",
  p_EsIngresoAutomatico "N", p_EsCargaMasiva "N", p_EsPauta false}` (the last two are defaults of the legacy pojo and ARE in the golden); each movement carries 107 keys (nulls included); `RESNORM` / `RESTRAPOR`:
  `FOE`, `COSAPV` / `COSAFM`, net amount in `Monto`, `MontoMovimientoComprobante`, `MontoLiquidacion` and `Precio`; `RESCOM`: the type of voucher of the parameter, `COCAPV` / `COAFFM`, the amount of the rescate; a
  cobro `IVA` only when `Monto > 0`; a form of payment `CIN` only for `RESNORM` (the whole commission). `CrearComprobante`: `{P_MOVIMIENTOFACTURAR [{IdMovimientoComprobante, FechaComprobante = system date}], ...}`.
* `CrearSolicitudAporteReinversion`: `{p_IdEmpresa, p_IdUsuario, p_IdModulo, p_SimularOperacion "N", p_UsaReglasNegocio (of the request), p_IdTipoCanalIngreso "AGE", p_SolicitudesAporte [...]}`; each aporte is the legacy
  `InSolicitudesAporte` (every key, nulls included, `FechaValorCuota` null, `TipoReinversion` ""); dates are plain `yyyy-MM-dd` text.
* the child: `{p_IdEmpresa, p_IdUsuario, p_IdModulo, p_SimularOperacion, p_UsaReglasNegocio, p_FechaCertificacion, p_IdTipoCanalIngreso (of the request, null when absent), p_NotificaOWP "N",
  p_CertificacionAporteSwitchSimulado = p_SimularOperacion, p_Solicitudes [{IdSolicitudAporteRescate, IdTipoCanalIngreso}]}`.

AR and the child are REPLAYED from the golden by the harness (the answer the legacy got, call by call, in order); the request the new code makes is compared with the recorded one. The calls that AR and
the child made to THEIR collaborators are in the field `internas` of the record and are not compared (the new code does not run them).

## 3. Behaviour the tests pin (bug for bug; the golden is the oracle, this is the index)

F-G: a rescate is invoiced only when not rejected, `GeneraSpot "N"`, type `RESCOM` / `RESNORM` / `RESTRAPOR`, commission (after the format of F-D) `> 0` and no traspaso; the IVA is read for EVERY request with
rescates (no row: `IndexOutOfBoundsException`, null value: `NullPointerException`, D-F3B-47), `IVA = ValorCobro / 100` exact, net = commission / (1 + IVA) with 34 digits; the movement list is ordered by id;
Facturacion is called once, only with movements; its counter is added; each `ERROR` (`equals`) rejects the first rescate with `Math.abs(id)` (a missing id raises `NullPointerException`); with
`RESCATE_COMISION_COMPROBANTE_AUTOMATICO` `equalsIgnoreCase "S"` the voucher is asked for each generated movement whose rescate (lookup by id: missing raises) is not rejected, is `RESNORM` or `RESREINV`, has
commission and no traspaso; the counter added is the one of the MOVEMENT, twice, and never the one of the voucher (D-F3B-04); the errors of the voucher do reject.

F-I: only rescates not rejected of type `RESREINV` with destinations; destinations grouped by rescate in a `HashMap` (D-F3B-32); same family (not empty) and same currency of the destination fund: one aporte per
retired cartera (`ratio = Cantidad / NumeroCuotas`, amounts formatted, zero amounts skipped), otherwise one aporte per destination; the rounding difference goes to the FIRST aporte of the rescate, also the one of
another destination (D-F3B-13, D-F3B-45, D-F3B-46); an empty list raises `IndexOutOfBoundsException`; one call to AR for all the rescates, correlative `IdInterno` 1..n; messages with a correlative id are mapped
to the id of the rescate and each `ERROR` rejects the first rescate with that id; AR's counter is added; results with `Switch` exactly `"S"` (null raises, D-F3B-30) become `ctx.aportesSwitch`; the child is
called ONCE per request with the aportes of the switch (never when empty), after Spot and front J; FM-CER-64 writes, per related rescate of the aportes of the request that are APOREINV, not rejected, with
`IdMonedaSistema != IdMoneda`, the SUM of `MontoMonedaSistema` of ALL the rows related to it (D-F3B-49).

## 4. Test resources (created by `legacy-oracle/sync_cer_t4.py` from the oracle output)

`src/test/resources/fondomutuo-cer/cases-t4/<id>/{case.json,seed.sql,expected.json}` (ids `FMM-CER-575_slug` on: T3 ends at `FMM-CER-574`), `fondomutuo-cer/seed/base-cer-t4.sql` (families of the series, funds 11
and 21), `fondomutuo-cer/determinism-cer-t4.txt` (cases x 20 runs, 0 unstable). The schema (`schema-cer.sql`) and the bases of T1..T3 are not modified.
