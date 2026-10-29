# API assumed by the FMM-CER T2 tests (F3 W3, tanda T2: fronts F-C aportes and F-D rescates: custody and costs of `CertificarSolicitudAporteRescate`)

The production code of T2 does NOT exist yet (red before code). The tests of this package for T2 (`ApiT2`, `CasoCerT2`, `ParadaT2`, `CerCosturaT2`, `CerT2EquivalenciaTest`, `CerT2DeterminismoTest`, `CerT2ReglasTest`,
`CerT2CompuertaTest`) COMPILE without it: the new classes are reached by reflection in `ApiT2` (the only file that knows the names) and every test that needs them fails with
`AssertionError: T2 not implemented: the production class ... does not exist yet`. T1 (`API_ASSUMED.md`) is untouched and stays green: the T1 facade, its seam and its tests are exactly the ones
of T1. Legacy: `cl.bcs.fondomutuo.CertificarSolicitudAporteRescate` (`logica`, C:575-679). Cards FM-CER-21, 22, 24..28 (F-C) and FM-CER-31..41 (F-D) of `RULES_FONDOMUTUO_F3_B.md` 8.1 (FM-CER-23, the
format of the amounts, is the pure rule of T1 `CertificarAporteRescateReglas.formatearMontos`: here it is only CALLED, after the saldo for the aportes and after the retention / commission for the
rescates). Defects D-F3B-01/03/05/06/08/09/10/11 and the new D-F3B-37 (see `TRANSFORMATION_NOTES.md`, "F3 W3 T2 (pruebas)").

## 0. THE SEAM of T2 and what it pins

T1's seam `CertificarAporteRescateFasesPosteriores.continuar(contexto)` stays where it is (right after F-B) and the T1 tests are not changed. In T2 the harness (`CasoCerT2`) runs the T1 facade
(F-A and F-B REAL, REAL JDBC adapters) and, at the seam, runs the FIVE STEPS of F-C and F-D over the `CertificarAporteRescateContexto` it receives, in the order of the legacy `logica`, and then
throws `ParadaT2`:

| # | step (production method) | legacy lines | cards | what it does |
|---|---|---|---|---|
| 1 | `CertificarAporteRescateAportes.validarSaldo(ctx)` | C:575-579, 1149-1303 | FM-CER-21, 22 (+23) | `SaldoFlujoCaja` for the aportes of the cuenta de inversion (not PAC, then PAC), rejection by saldo, then the format of the amounts of ALL the aportes |
| 2 | `CertificarAporteRescateRescates.prepararRescates(ctx)` | C:582-596, 1305-1465 | FM-CER-31..38 (+23) | `CalculaCustodiaRescate`, rejection / advice / total, `GeneraSpot`, traspaso, retention, commission (the REAL `CalcularComisiones`), format, valid retention |
| 3 | `CertificarAporteRescateAportes.recalcularCostoUF(ctx)` | C:600-601, 2939-3019 | FM-CER-24..26 | cost UF of the reinversion aportes (swallows every error: `try/catch` + `printStackTrace`, D-NOR-11) |
| 4 | `CertificarAporteRescateAportes.ingresarCustodia(ctx)` | C:602-620, 1467-1478, 1684-1739 | FM-CER-27, 28 | `ProcesarCustodiaFondoMutuo` (entry of the aportes), rejections, cartera and real profitability |
|   | (front E: cuenta de inversion of the aportes, C:622-640) | | | NOT in T2 (T3) |
| 5 | `CertificarAporteRescateRescates.retirarCustodiaYCostear(ctx)` | C:644-676, 1480-1578 | FM-CER-39, 40, 41 | the two retirement calls (with / without regimen), rejection by id, rescate without cartera, cost UF / spread / real profitability (`CalcularParidad` UTM -> CLP) |

Every step is a NO-OP when its list (`ctx.aportes` / `ctx.rescates`) is empty (the legacy guards with `if(list.size() > 0)`), reads and writes ONLY the context and its ports (no JDBC write: the
UPDATE to CRT, the log and the sequences are front J), adds its counter and messages to `ctx.salida`, and lets any exception go up UNTOUCHED (the legacy `logica` has no try/catch there; only
`recalcularCostoUF` catches, as the legacy does). In production the five steps will be called by the facade in this order (T5 joins the fronts behind one facade that preserves the order of
section 7 of the rules); the cuenta de inversion of the aportes (front E) goes between steps 4 and 5.

## 1. Classes (package `cl.bcs.fondomutuo.certificacion`, main source set)

| Class | Kind | Members the tests use |
|---|---|---|
| `ProcesarCustodiaFondoMutuoPort` | interface, ONE method | `JsonObject procesar(JsonObject entrada)`: the legacy JSON of `ProcesarCustodiaFondoMutuo.EntradaProcesarCustodiaFondoMutuo` -> `SalidaProcesarCustodiaFondoMutuo` (`P_OUT_ERRORESCONTADOR`, `P_OUT_MENSAJES`, `P_OUT_MOVIMIENTOS`). NO production implementation exists in T2 (gate G-CUS closed; the in-process adapter comes with the custody line, T5) |
| `CalculaCustodiaRescatePort` | interface, ONE method | `JsonObject calcular(JsonObject entrada)`: `EntradaCalculaCustodiaRescate` -> `SalidaCalculaCustodiaRescate` (`P_OUT_RESULTADO`). Same status |
| `CertificarAporteRescateAportes` | `final`, no state | constructor `(port.out.http.FlujoCajaPort flujoCaja, port.out.http.ParidadPort paridad, ProcesarCustodiaFondoMutuoPort custodia, CertificarAporteRescateDatos datos, cierre.FormatoMoneda formato)`; `void validarSaldo(CertificarAporteRescateContexto) throws Exception`; `void recalcularCostoUF(CertificarAporteRescateContexto)`; `void ingresarCustodia(CertificarAporteRescateContexto) throws Exception`. `datos.solicitudes(idEmpresa, ids)` is the read of the rescates of origin (`SelectSolicitudes`, C:2943-2946) |
| `CertificarAporteRescateRescates` | `final`, no state | constructor `(CalculaCustodiaRescatePort calculaCustodia, ProcesarCustodiaFondoMutuoPort custodia, comisiones.CalcularComisiones comisiones, port.out.http.ParidadPort paridad, cierre.FormatoMoneda formato, port.out.kit.DiccionarioMensajesPort diccionario)`; `void prepararRescates(CertificarAporteRescateContexto) throws Exception`; `void retirarCustodiaYCostear(CertificarAporteRescateContexto) throws Exception` |

If production needs another shape, change `ApiT2` (not the goldens): nothing of the comparison depends on the other names. The units get NO `Compuertas`, no write port, no connection and no
sequence port (`CerT2CompuertaTest` pins it): the host evaluates `g3SaldoFlujoCaja()` / `gCustodia()` AFTER the validations and BEFORE the first write (pending T5).

Existing classes the tests use as they are: everything of T1 (`CertificarAporteRescateService`, `...Contexto`, `...Contrato`, `...Datos`, `...DatosJdbc`, `SujetoCer`), `cierre.FormatoMoneda`,
`comisiones.CalcularComisiones` with `adapter.out.jdbc.ComisionesJdbc` and `SeriesFondoJdbc`, `adapter.out.jdbc.DiccionarioJdbc`, `kit.MensajeSalida`, `kit.Compuertas`,
`port.out.http.{FlujoCajaPort, ParidadPort, CustodiaPort, PreciosTasasPort}`, `port.out.kit.EmpresaPort`, `cierre3b.Guion`, `BdEfimeraH2` (every H2 of a case is shut down when the case ends: no
heap growth).

## 2. What the arguments to the collaborators look like (the legacy JSON, compared by value)

The tests compare, for every case, the calls to `SaldoFlujoCaja`, `CalculaCustodiaRescate`, `ProcesarCustodiaFondoMutuo`, `ConsultarFlujoCustodia`, `ConsultarEmpresa`, `CalcularParidad` and
`EntregarPreciosTasas` made AFTER F-B (name, COMPLETE arguments, answer, order). The arguments are the JSON the legacy built with its plain `Gson` + `serializeNulls` + date format
`yyyy-MM-dd HH:mm:ss.SSS` (the oracle's doubles record the same): a null field is a `null` key, a `java.sql.Date` is `"2026-03-02 00:00:00.000"`, a `BigDecimal` is a JSON number (compared
BY VALUE: `1.50` = `1.5`). Production serialises with `kit.JsonLegacy.conNulos()`. Key facts the goldens show (`expected.json` of any case):

* `SaldoFlujoCaja`: `{P_IDEMPRESA, P_FECHADESDE = P_FECHAHASTA = effective date "yyyy-MM-dd", P_USO "CERTIFICACION_APORTE_FMM" (first call) / "CERTIFICACION_APORTE_FMM_PAC" (second), P_DESPLEGARENUI "N", P_AGRUPARPORORIGEN "N", p_BarraProgreso null, P_PORTAFOLIOACONSULTAR [{IdPortafolio, IdMoneda}]}`; the second call exists only when a PAC aporte reaches the flow.
* `ProcesarCustodiaFondoMutuo` of the aportes: ONE call, movements in ascending id (`IdTipoOperacion "APORFM"`, `IdTipoComprobante "FFMM"`, `IdBolsa "BCS"`, `IdBoveda "COR"`, `ValorRescate 0`, `IdTipoCanalIngreso ""`, `p_simularOperacion "N"`, `p_UsaReglasNegocio "S"`); of the rescates: up to TWO calls (first with regimen, then without), `IdTipoOperacion "RESCFM"`.
* `CalcularParidad` of `recalcularCostoUF`: ONE REUSED entry object (D-F3B-10): the second call carries the entry twice with the same `IdInterno`; of `retirarCustodiaYCostear`: ONE entry per distinct `FechaCertificacion`, `IdMonedaOrigen "UTM"`.

## 3. Behaviour the tests pin (bug for bug; the golden is the oracle, this is the index)

F-C: the pairs sent to `SaldoFlujoCaja` are omitted when the portfolio AND the currency were both already seen (two independent lists, D-NOR-16: the omitted pair is read with saldo 0); the
total of a pair adds `CostoMonedaLocal` of the aportes of the same group (PAC and not PAC are NOT added together); `total > saldo` rejects ALL the aportes of the pair, not the last
(`UI_SALDO_INSUFICIENTE_FLUJO_CAJA`); the date of the saldo is the later of `FechaCertificacion` and `PLA.FechaSistema`; the LAST row with the same portfolio, currency and date is the saldo;
`OperaSaldoNegativo` is compared with `equalsIgnoreCase("N")`. Cost UF: only `APOREINV`; the rescate of origin is READ FROM THE DATABASE (not the in-memory one) and `findFirst().get()` raises
`NoSuchElementException` when it does not exist, which aborts the rest of the aportes (the whole body is in `try/catch`); inheritance for `APV-A`, `BF`, `DC-DL`, `DC-AL`, `DC-R`
(`equalsIgnoreCase`) or `IndicadorMLT "S"` with both regimes `18Q`; otherwise `UF` -> `CLP` of `CalcularParidad` (`findAny().get().MontoLocal`); a foreign-currency reinversion without Spot sums
the DISTINCT `MontoMonedaSistema` of the not rejected reinversion aportes. Custody: errors of type `ERROR` reject the FIRST aporte whose id is `Math.abs(mensaje.id)` and add the message
and the counter; `IdCartera` / `RentabilidadReal` come from the first movement of the aporte; an aporte without movement ends with `RentabilidadReal` NULL in the UPDATE (D-F3B-37).

F-D: a rescate without result of `CalculaCustodiaRescate` (or `RZD` without `CantidadDisponible`) raises `NullPointerException` and aborts the lot (D-F3B-05); `RZD`, or `ADV` with
`IdOmnibus > 0`, rejects with `UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION`; `ADV` advises (`UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION`) and takes the amounts of the custody; a total
rescate (`CalculoPorMontoCuota "T"`, `IdOmnibus` null or 0) takes them too; any other state of the result is approved; `GeneraSpot` is `S` only with the parameter (`equalsIgnoreCase`), tipo
`RESCNN` or `IdMoneda` USD and `IdMonedaPago` CLP, else `N`, and stays NULL in a rescate rejected there; `RelacionTraspaso` from the unified map; retention only for `RESNORM` APV-AN / APV-B,
medio de pago not `CIN`, no traspaso, no Spot, percentage <> 0, over the GROSS `Monto` / `CostoMonedaLocal` with a plain `divide(100)`; commission only with `CALCULAR_COMISION_CERTIFICACION = "S"`
(exactly) and applied only when `TieneComisionNNRelacionada = "N"`; retention reset when it is 0 after the format; the two retirement calls only with the rescates not rejected; the error
messages reject by `Math.abs(id)`; a rescate with no movement of its id is rejected `UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO`; cost UF / spread / real profitability are computed for EVERY
rescate of the list, also the rejected ones (D-F3B-11; read in the code of the legacy, the golden cannot show it: a rejected rescate is neither written nor sent), and marcarTraspasos also marks the rejected ones (D-F3B-38, same status).

## 4. Test resources (created by `legacy-oracle/sync_cer_t2.py` from the oracle output)

`src/test/resources/fondomutuo-cer/cases-t2/<id>/{case.json,seed.sql,expected.json}` (ids `FMM-CER-199_slug` on: T1 ends at `FMM-CER-198`), `fondomutuo-cer/seed/base-cer-t2.sql` (dictionary rows of the new
codes), `fondomutuo-cer/determinism-cer-t2.txt` (cases x 20 runs, 0 unstable). The schema (`schema-cer.sql`), `seed/base-cer.sql` and `seed/doubles-cer.json` are the ones of T1 (not modified).
