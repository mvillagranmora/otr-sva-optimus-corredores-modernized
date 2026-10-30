# API assumed by the FMM-CER T3 tests (F3 W3, tanda T3: fronts F-E cuenta de inversion, F-F Tesoreria and F-H Spot of `CertificarSolicitudAporteRescate`)

The production code of T3 does NOT exist yet (red before code). The tests of this package for T3 (`ApiT3`, `CasoCerT3`, `ParadaT3`, `CerCosturaT3`, `CerT3EquivalenciaTest`, `CerT3DeterminismoTest`,
`CerT3ReglasTest`, `CerT3CompuertaTest`) COMPILE without it: the new classes are reached by reflection in `ApiT3` (the only file that knows their names) and every test that needs them fails with
`AssertionError: T3 not implemented: the production class ... does not exist yet`. T1 (`API_ASSUMED.md`) and T2 (`API_ASSUMED_T2.md`) are untouched and stay green: the T1 facade, its seam
and the five steps of F-C / F-D are exactly the ones already implemented. Legacy: `cl.bcs.fondomutuo.CertificarSolicitudAporteRescate` (`logica`, C:622-640 and 679-721 and 790-830;
`mueveCuentaInversion` C:1741-1935, `mueveTesoreria` C:1953-2054, `mueveSpot` C:2057-2157). Cards FM-CER-29, 30, 42, 43 (F-E), FM-CER-44..47 (F-F) and FM-CER-59, 60 (F-H) of
`RULES_FONDOMUTUO_F3_B.md` 8.1. Defects D-F3B-01/03/07/08/09/14/17 and the new D-F3B-41..44 (see `TRANSFORMATION_NOTES.md`, "F3 W3 T3 (pruebas)").

## 0. THE SEAM of T3 and what it pins

T1's seam `CertificarAporteRescateFasesPosteriores.continuar(contexto)` stays where it is (right after F-B) and the T1 and T2 tests are not changed. In T3 the harness (`CasoCerT3`) runs the T1
facade (F-A and F-B REAL, REAL JDBC adapters) and, at the seam, runs the NINE STEPS of F-C..F-H over the `CertificarAporteRescateContexto` it receives, in the order of the legacy `logica`, and then
throws `ParadaT3`. Five steps are the REAL ones of T2 (`ApiT2`); four are new:

| # | step (production method) | legacy lines | cards | what it does |
|---|---|---|---|---|
| 1..4 | `validarSaldo`, `prepararRescates`, `recalcularCostoUF`, `ingresarCustodia` | C:575-620 | FM-CER-21..28, 31..38 | T2, implemented |
| 5 | `CertificarAporteRescateCuentaInversion.moverAportes(ctx)` | C:622-640, 1741-1830, 1915-1951 | FM-CER-29, 30 | `CrearCargoAbono` for the aportes that move the cuenta; errors reject the aporte |
| 6 | `CertificarAporteRescateRescates.retirarCustodiaYCostear(ctx)` | C:644-676 | FM-CER-39..41 | T2, implemented |
| 7 | `CertificarAporteRescateCuentaInversion.moverRescates(ctx)` | C:679-694, 1827-1911 | FM-CER-42, 43 | `CrearCargoAbono` for the rescates; errors reject the rescate |
| 8 | `CertificarAporteRescateTesoreria.moverTesoreria(ctx)` | C:698-721, 1953-2054 | FM-CER-44..47 | the entries of Tesoreria (path a / path b / SII / TGR) and the reading of `RECH` and the generated ids |
|   | (front G, Facturacion, and front I, reinversion and switch, C:724-787) | | | NOT in T3 (the benign doubles of the oracle answer them) |
| 9 | `CertificarAporteRescateSpot.moverSpot(ctx)` | C:790-830, 2057-2157 | FM-CER-59, 60 | the Spot operation and its two payment forms; reading of `RECH` and the messages |

Every step is a NO-OP when its list (`ctx.aportes` / `ctx.rescates`) is empty (the legacy guards with `if(list.size() > 0)`), reads and writes ONLY the context and its ports (no JDBC write: the UPDATE
to CRT, the log and the sequences are front J), adds its counter and messages to `ctx.salida`, and lets any exception go up UNTOUCHED (the legacy has no try/catch there). In production the nine steps
(and the ones of F-G, F-I, F-J, T4 / T5) will be called by the facade in this order (section 7 of the rules); the harness only skips what is not F-C..F-H, and the benign doubles of the oracle
guarantee that what is skipped (Facturacion, reinversion) changes nothing the T3 steps read.

## 1. Classes (package `cl.bcs.fondomutuo.certificacion`, main source set)

| Class | Kind | Members the tests use |
|---|---|---|
| `CrearCargoAbonoPort` | interface, ONE method | `JsonObject crear(JsonObject entrada)`: the legacy JSON of `CrearCargoAbono.CrearCargoAbonoEntrada` -> `CrearCargoAbonoSalida` (`P_OUT_ERRORESCONTADOR`, `P_OUT_MENSAJES`). NO production implementation in T3 (see Q-T3-1: `CrearCargoAbono` lives in `BcsCuentaInversionServicios`, a service the module does not depend on) |
| `CertificarAporteRescateCuentaInversion` | `final`, no state | constructor `(CrearCargoAbonoPort cuenta, CertificarAporteRescateDatos datos)`; `void moverAportes(ctx) throws Exception`; `void moverRescates(ctx) throws Exception` |
| `CertificarAporteRescateTesoreria` | `final`, no state | constructor `(normal.ProcesosExternos.TesoreriaPort tesoreria, port.out.kit.ConfiguracionSistemaPort configuracion)`; `void moverTesoreria(ctx) throws Exception` |
| `CertificarAporteRescateSpot` | `final`, no state | constructor `(normal.ProcesosExternos.SpotPort spot)`; `void moverSpot(ctx) throws Exception` |
| `CertificarAporteRescateDatos` (EXISTS) | read port | gains ONE read, the legacy SELECT `CertificarSolicitudAporteRescate_SelectTipoMovimientoFondoMutuoGeneraCargoAbono` (a movement type without row reads as `"N"`; N+1 per solicitud in the legacy); the harness builds the real `CertificarAporteRescateDatosJdbc` over the case H2, so its method name is NOT part of the contract |

The typed ports of the module that EXIST are reused as they are (they are what `Normal` and the Tesoreria library already use): `ProcesosExternos.TesoreriaPort` (the in-process Tesoreria library,
G1) and `ProcesosExternos.SpotPort` (Mesa Spot, G2). Two ADDITIVE changes of `ProcesosExternos` that the tests assume (they cannot be referenced here, the harness converts by Gson):
`SalidaSpot` gains `Long P_OUT_ERRORESCONTADOR` (null when the answer has none: the legacy only adds it `if != null`, FM-CER-60) and the pojo of the Spot entry, as built by the units, must serialise
`p_PermiteOperarProspectos` = `"N"` (the legacy class default; the typed class of `Normal` leaves it null and sets it itself). The key `p_DatosOrigenInversion` (always `[]` in Certificar) is
removed from both sides by the comparison when empty, so the typed class does not have to declare it. If production prefers another shape, change `ApiT3` (not the goldens).

The units get NO `Compuertas`, no write port, no connection and no sequence port (`CerT3CompuertaTest` pins it): the host evaluates `g1MedioDistintoDeCin()` / `g2Spot()` AFTER the validations and
BEFORE the first write (pending T5), plus whatever gate the cuenta de inversion gets (Q-T3-1).

Existing classes the tests use as they are: everything of T1 and T2 (`CertificarAporteRescateAportes`, `...Rescates`, `...Contexto`, `...Contrato`, `...Datos`, `...DatosJdbc`, `SujetoCer`, `ApiT2`),
`kit.JsonLegacy` (the harness serialises the typed ports with `conNulos()`, as the oracle's doubles record), `cierre3b.Guion`, `BdEfimeraH2` (every H2 of a case is shut down when the case ends).

## 2. What the arguments to the collaborators look like (the legacy JSON, compared by value)

The tests compare, for every case, the calls made AFTER F-B to `SaldoFlujoCaja`, `CalculaCustodiaRescate`, `ProcesarCustodiaFondoMutuo`, `ConsultarFlujoCustodia`, `ConsultarEmpresa`,
`CalcularParidad`, `EntregarPreciosTasas` AND, new in T3, `CrearCargoAbono`, `ProcesarIngresoEgresoTesoreria`, `ProcesarIngresoOperacionSpot`: name, COMPLETE arguments, answer, order. The arguments
are the JSON the legacy built with its plain `Gson` + `serializeNulls` + date format `yyyy-MM-dd HH:mm:ss.SSS` (the oracle's doubles record the same): a null field is a `null` key, a `BigDecimal`
is a JSON number (compared BY VALUE: `1.50` = `1.5`). Key facts the goldens show (`expected.json` of any case):

* `CrearCargoAbono`: `{p_CargoAbonoAGenerar [...], p_IdEmpresa, p_IdUsuario, p_IdModulo, p_simularOperacion "N" (always), p_UsaReglasNegocio (of the request), p_FechaSistema "yyyy-MM-dd", p_EsIngresoManual "N"}`;
  ONE call for the aportes (`IdTipoOperacion "APORFM"`) and ONE for the rescates (`"RESCFM"`), each only when it has lines; the lines of a lot are numbered `NumeroCargoAbono` 0, 1, 2 ... in the order
  of the solicitudes (the numbering restarts in the second call); the dates of the line are plain `"yyyy-MM-dd"` text (the legacy pojo carries them as text); a DEFERRED aporte (`IdMoneda <> IdMonedaFondo`)
  is sent as [cargo in the investment currency, abono in the fund currency, **principal LAST**]; a DEFERRED rescate as [**principal FIRST**, cargo in the fund currency, abono in the investment currency]
  and, for `RESREINV` / `RESCOM` with `MontoComisionLocal > 0`, one more cargo of the commission; `IdTipoComprobante "FFM"`, `IdTipoProducto "INFM"`, `CertificadoAutomatico "S"`.
* `ProcesarIngresoEgresoTesoreria`: `{p_IdEmpresa, p_IdUsuario, p_IdModulo, p_SimularOperacion "N", p_UsaReglasNegocio, p_EsIngresoAutomatico "N", p_Solicitudes [...]}`; each entry is the WHOLE pojo
  `SolicitudAporteRescate` of the legacy (126 keys, nulls included, the keys `IdAutorizadoOrigen` = `IdPreferenciaPagoPortafolio` (D-F3B-41) and `FechaValorCuota` `0001-01-01` (not copied));
  entries in the order of the rescates, the SII entry (`IdInterno` = -id) right after its rescate.
* `ProcesarIngresoOperacionSpot`: `{p_IdEmpresa, p_IdUsuario, p_IdModulo, p_CalculaMargen "S", p_UsaReglasNegocio, p_PermiteOperarProspectos "N", p_DatosOperacion [...], p_DatosFormaPago [...]}`: the
  same pojo per rescate and TWO forms per rescate, "recibe" (`R`) then "paga" (`P`).

## 3. Behaviour the tests pin (bug for bug; the golden is the oracle, this is the index)

F-E: an aporte moves the cuenta only with `MueveCta S`, origin `M` and a movement type whose `GeneraCargoAbono` is `S` (a type without row = `N`); a rescate only needs the type (D-F3B-17); nothing
is sent for a solicitud already rejected (by saldo, custody, F-A...); `FechaCondicionDeLiquidacion` = the system date when it is later than `FechaLiquidacion` OR the solicitud is a switch (`Switch`
`equalsIgnoreCase "S"`), else `FechaLiquidacion`; `FechaLiquidacionOperacion` = the system date when it is later than `FechaLiquidacion` (the switch does not matter); `FechaOperacion` =
`FechaCertificacion` (aporte) / `FechaAceptacion` (rescate); `IdConcepto` is the one `SelectSolicitudes` computed; errors (`tipo` exactly `"ERROR"`) map the line number back to the solicitud,
reject the FIRST solicitud with that id (state `RZD`, the code and the description of the message), add the message with the solicitud id and sum the counter of the collaborator (also when it has no
message); a line number that is not in the map, or a message without id, raises `NullPointerException` (D-NOR-9); an exception of the collaborator comes out untouched.

F-F: only rescates, not rejected, not `GeneraSpot S`: path (a) = medium other than `CIN`, type other than `RESREINV`, `IndicadorMLT "N"` (exactly), no traspaso; path (b) = `RelacionTraspaso S` (the
traspaso of the solicitud or of its related one; medium forced to `TRA`, the person and account of the traspaso); `FechaEmision` = `FechaLiquidacion` always (D-F3B-07); `ParidadFechaInversion` =
`ParidadFechaCertificacion`; `TieneMontoRetencion S` subtracts `MontoRetencion` from `MontoPagoLocal` (D-F3B-09); `APV-B` / `APV-AN` add the SII entry (`IdInterno` -id, `MontoPagoLocal` =
`MontoRetencion`, SII person and account, medium `TRA`, `RelacionTraspaso S`, `IdConcepto` of the parameter `CONCEPTO_RETENCION_PAGO_SII`, read with `ConfiguracionSistemaPort.texto(empresa, "FONDOMUTUO",
...)`: a missing parameter raises before the call) even with retention 0; a null SII person raises `NullPointerException` (D-F3B-08); `BF` takes the TGR person and account (medium `TRA`, `RelacionTraspaso S`).
Reading: only `Resultado "RECH"` with `Campo "NumeroReferencia"` rejects (state `RZD`, NO code, the first rescate whose id is `Math.abs(Id)`); a null `Campo` of a matching id raises; the generated ids
go to the first solicitud whose id is `NumeroReferencia` (the last one wins when repeated); the counter and the messages of Tesoreria are NEVER read (D-F3B-03).

F-H: only rescates not rejected with `GeneraSpot S`: `p_CalculaMargen "S"`; form "recibe" = `MOVEGR`, `COMSPT_<CIN|TRA if TipoComision "PER">_<IdMoneda>_0`, amount `Monto`, `AfectarTesoreria S` only for
`RESCNN`; form "paga" = `MOVING`, `VENSPT_<medio, CIN when empty / null>_<IdMonedaSistema>_0`, amount `MontoMonedaSistema`, `AfectarTesoreria S` unless the medium is `CIN`; both with `FechaLiquidacion` of the
rescate. Reading: `Estado "RECH"` (exactly) rejects the first solicitud of the list with that `IdInterno` (parsed with `Long.parseLong`: a non numeric id raises `NumberFormatException`) with code
`ERROR_CREAR_SPOT`, else the `IdOperacionSpot` is assigned (also to an already rejected solicitud, D-F3B-42); messages `ERROR` (`equalsIgnoreCase`) reject the first solicitud whose id is `Math.abs(id)` OR,
when `id == 0`, the FIRST rescate of the list (D-F3B-14); the counter is added only when present.

## 4. Test resources (created by `legacy-oracle/sync_cer_t3.py` from the oracle output)

`src/test/resources/fondomutuo-cer/cases-t3/<id>/{case.json,seed.sql,expected.json}` (ids `FMM-CER-378_slug` on: T2 ends at `FMM-CER-377`), `fondomutuo-cer/seed/base-cer-t3.sql` (movement types with
`GeneraCargoAbono`, bank accounts of the persons Tesoreria pays, dictionary rows), `fondomutuo-cer/determinism-cer-t3.txt` (cases x 20 runs, 0 unstable). The schema (`schema-cer.sql`), `seed/base-cer.sql`,
`seed/base-cer-t2.sql` and `seed/doubles-cer.json` are the ones of T1 / T2 (not modified).
