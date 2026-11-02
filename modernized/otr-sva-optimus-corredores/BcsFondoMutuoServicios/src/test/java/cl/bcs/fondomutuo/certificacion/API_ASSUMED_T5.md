# API assumed by the FMM-CER T5 tests (F3 W3, tanda T5: front J the closing and the FACADE of the recursion of `CertificarSolicitudAporteRescate`)

The production code of T5 does NOT exist yet (red before code). The tests of this package for T5 (`ApiT5`, `CasoCerT5`, `CerCosturaT5`, `CerT5EquivalenciaTest`, `CerT5DeterminismoTest`, `CerT5ReglasTest`,
`CerT5CompuertaTest`) COMPILE without it: the new classes and fields are reached by reflection in `ApiT5` (the only file that knows their names) and every test that needs them fails with
`AssertionError: T5 not implemented: the production class ... does not exist yet`. T1 (`API_ASSUMED.md`), T2, T3 and T4 (`API_ASSUMED_T2.md`, `_T3.md`, `_T4.md`) are untouched and stay green.
Legacy: `cl.bcs.fondomutuo.CertificarSolicitudAporteRescate` (`logica`, C:835-864; `actualizarSolicitudes` C:2738-2750, `notificar` C:2752-2762, `certificarAportesSwitch` C:2764-2775, `notificarOWP`
C:2777-2818, `agregarLogSistema` C:2837-2871, `marcarSolicitudesExitosas` C:2873-2880, `procesarSalida` C:1580-1611, `insertarLog` C:1613-1643). Cards FM-CER-61..71 (front F-J) and FM-CER-63 (the
facade of the recursion) of `RULES_FONDOMUTUO_F3_B.md` 8.1; defects D-F3B-01/02/16/31/47/49/50, D-NOR-9 and the new D-F3B-53.. (see `TRANSFORMATION_NOTES.md`, "F3 W3 T5 (pruebas)").

## 0. What changes with T5: no seam

T1..T4 compare "up to the seam" (`CertificarAporteRescateFasesPosteriores.continuar`): what fronts A..I DECIDED. T5 has NO seam: the harness (`CasoCerT5`) builds the FACADE, runs the whole chain A..J over the REAL
JDBC adapters and typed ports answered by the scripted collaborators of the case, and `CerCosturaT5` compares the WHOLE run with the golden (a complete run of the unmodified legacy): output text,
tables before / after the host decides, EVERY collaborator call with its complete arguments in order (the child inside its record, recursively), sequences, host decision, session, exception class.

| # | step of the legacy (section 7 of the rules) | legacy lines | cards | production |
|---|---|---|---|---|
| 0 | F-A, F-B (T1) | C:370-531 | 01..20, 23 | `CertificarAporteRescateService` (exists) |
| 0b | the facade evaluates the gates G8, G1, G2, G3, G6, G-CUS AFTER the early exit and BEFORE the first write | - | - | `CertificarAporteRescateFachada` |
| 1..8 | F-C..F-H (T2, T3) | C:575-830 | 21..47, 59, 60 | the units of T2 / T3 (exist) |
| 9, 10 | F-G Facturacion, F-I reinversion (T4-B) | C:724-787 | 48..58 | the units of T4-B (exist) |
| 11 | UPDATE of the pending / warned solicitudes to `CRT` | C:835, 2738-2750 | 61 | `CertificarAporteRescateCierre.actualizarYNotificar` + `CertificarAporteRescateEscrituraCierre` |
| 12 | asynchronous notification (ids of the updated; even with an empty list, even in simulation) | C:836, 2752-2762 | 62 | the same step, port `NotificacionCertificacionPort` |
| 13 | recursion of the switch (the facade calls itself, same connection, depth + 1) | C:838-843, 2764-2775 | 63 | `CertificarAporteRescateSwitch` (T4-B) over the facade as `CertificacionHijaPort` |
| 14 | UPDATE of `MontoMonedaSistema` of the rescates of origin | C:845, 2820-2835 | 64 | `CertificarAporteRescateSwitch.cambiarMontoMonedaSistema` (T4-B) |
| 15 | OWP notification (param `PLA.NotificarEstadoOrdenOWP` AND `p_NotificaOWP`, `equalsIgnoreCase`) | C:848-850, 2777-2818 | 65 | `CertificarAporteRescateCierre.notificarOwp`, port `NotificacionOrdenesOwpPort` |
| 16 | `AgregarLog` (result never read), mark of the pending as `APD`/`UI_EXITO`, the output, the INSERT of the log of certification, the forced counter of the simulation | C:853-864, 1580-1643, 2837-2880 | 66, 67, 68 | `CertificarAporteRescateCierre.cerrar`, `LogPort`, `CertificarAporteRescateEscritura` (T1) |

`Cierre` steps: every one is a NO-OP only when the legacy is (an empty list still notifies; the OWP list may be empty and then nothing is sent), reads and writes ONLY the context, its ports and (the UPDATE) its
own write port, adds counter / messages / results to `ctx.salida` and lets any exception go up UNTOUCHED (the legacy has no try/catch there). The rows of the child come FIRST in `ctx.salida.P_OUT_RESULTADO`.

## 1. Classes (package `cl.bcs.fondomutuo.certificacion`, main source set)

| Class | Kind | What the tests use |
|---|---|---|
| `CertificarAporteRescateFachada` | `final`, no mutable state | implements `CertificarAporteRescatePort` AND `CertificacionHijaPort`. `logica(entrada)` = the request (depth 0). `certificar(entrada)` = the recursion: depth + 1, and it FAILS CLOSED (`ServicioNoServidoException`, see 4) when the depth would be greater than 1. Built by `CertificarAporteRescateFabrica.crear(d)` when `d.posteriores == null` (with a seam given, T1..T4 keep working unchanged) |
| `CertificarAporteRescateFabrica.Dependencias` (EXISTS) | public fields | gains: `flujoCaja`, `procesarCustodia`, `calculaCustodia`, `comisiones`, `formato`, `crearCargoAbono`, `tesoreria`, `spot`, `facturacion`, `crearAporteReinversion`, `escrituraMonto`, `escrituraCierre`, `notificacionCertificacion`, `notificacionOwp`, `agregarLog`, `compuertas`, `envolverHija`. The types are the ones of T2..T4 (`FlujoCajaPort`, `ProcesarCustodiaFondoMutuoPort`, `CalculaCustodiaRescatePort`, `CalcularComisiones`, `FormatoMoneda`, `CrearCargoAbonoPort`, `ProcesosExternos.TesoreriaPort`, `SpotPort`, `FacturacionPort`, `CrearAporteReinversionPort`, `CertificarAporteRescateEscrituraMonto`, `LogPort`, `Compuertas`) and the new ones below; `envolverHija` is a `UnaryOperator<CertificacionHijaPort>` (null in production) |
| `NotificacionCertificacionPort` | interface, ONE method, TYPED | `void notificar(CertificarAporteRescateContrato.NotificarEstadoOrdenEntrada e) throws Exception`. The entry has EXACTLY the fields `p_IdEmpresa Long`, `p_IdUsuario Long`, `p_IdModulo String`, `p_Solicitudes List<Long>` (the legacy `EntradaNotificarEstadoOrdenFondoMutuo`). NO production implementation (the HTTP adapter, gate G8) |
| `NotificacionOrdenesOwpPort` | interface, ONE method, TYPED | `void notificar(List<CertificarAporteRescateContrato.OrdenOwp> ordenes) throws Exception`. `OrdenOwp` has EXACTLY the 15 fields `IdEmpresa Long`, `IdUsuario Long`, `Solsec Long`, `Fecha String`, `Estado String`, `Mercado String`, `NTitulos BigDecimal`, `Precio BigDecimal`, `Comision BigDecimal`, `Gastos BigDecimal`, `Divisa String`, `TipoCambio BigDecimal`, `SolsecAsoc String`, `Nemo String`, `IdClasificacionTipoInstrumento String` (the keys the golden shows). The existing `port.out.http.NotificacionOwpPort` (JsonObject, gate G4) is NOT used: see Q-T5-1 |
| `CertificarAporteRescateEscrituraCierre` | write port | `void actualizarSolicitudes(List<SolicitudProceso>)`: the legacy `UpdateSolicitudes` (SQL verbatim, one batch, `CURRENT_UTCTIMESTAMP`); joins the ambient transaction, never commits. JDBC adapter `CertificarAporteRescateEscrituraCierreJdbc(ConexionUnidadDeTrabajo)`. `CertificarAporteRescateEscritura` (T1, the INSERT of the log) is reused for `insertarLog` and is NOT touched |
| `CertificarAporteRescateCierre` | `final`, no state | front J: `actualizarYNotificar(ctx)`, `notificarOwp(ctx)`, `cerrar(ctx)` (public void on the context). No connection, no `Compuertas`. Reads the re-read of OWP through `CertificarAporteRescateDatos` (additive: the legacy `SelectSolicitudes` with `IdEmpresa` and the list of ids) |
| `CertificarAporteRescateContexto` (EXISTS) | public fields | gains `List<SolicitudProceso> solicitudesActualizadas` (what `actualizarSolicitudes` returned: the OWP step reads it) |
| `kit.Compuertas` (EXISTS) | gate | gains a 10-argument constructor (`..., boolean facturacion, boolean notificaciones`; property `fondomutuo.notificaciones.habilitadas`, default false) and `g8Notificaciones()` (raises `ServicioNoServidoException("G8", ...)`). The 6-, 8- and 9-argument constructors NEVER open G8 (fail closed) |

The typed ports that exist are reused as in T2..T4. The `Responder` of the harness turns every typed argument into the legacy JSON with `JsonLegacy.conNulos()` (nulls written, `yyyy-MM-dd HH:mm:ss.SSS`), so
the field NAMES of the new classes are the legacy ones and are compared with the golden.

## 2. What the facade does that no unit does

1. After F-A / F-B and the early exit of FM-CER-14 (an early exit notifies nothing and needs no gate), and BEFORE the first write of the request: `compuertas.g8Notificaciones()` (always: every run that gets this far
   notifies), `g6Facturacion()` when a rescate would be invoiced, `g1MedioDistintoDeCin()` when Tesoreria would receive an entry, `g2Spot()` when Spot would, `g3SaldoFlujoCaja()` when an aporte reaches the cash
   flow, `gCustodia()` when custody is called. With a gate shut: `ServicioNoServidoException` naming it, nothing written, no writer called, no sequence consumed (`CerT5CompuertaTest`).
2. The recursion: `certificar(entrada)` runs the same chain on the SAME connection with `p_NotificaOWP "N"` and `p_CertificacionAporteSwitchSimulado = p_SimularOperacion` (built by the T4-B unit) and the
   guard of the depth (4). `envolverHija` is applied to the guarded port the facade hands to the switch unit: the harness uses it to RECORD the child (entry, answer, inner calls).
3. The host decision is NOT the facade's: counter 0 commits, anything else rolls back (`CallServlet`); the facade only returns the `Salida` or lets the exception go up.

## 3. What the tests pin (the golden is the oracle; this is the index)

FM-CER-61: the UPDATE writes, for the pending and warned solicitudes only, `FechaCertificacionRegistro` (the clock), `Monto`, `ValorCuota`, `NumeroCuotas`, `ParidadFechaCertificacion` (`COALESCE(...,0)`),
`CostoMonedaLocal`, `MontoPago(Local)`, `IdSolicitudAporteRescateRelacionado`, `Spread` (only `RES`, else 0), `CostoUF` (only `RES` / `APO`, else the table value), `RentabilidadReal`, `IdMonedaSistema`,
`MontoMonedaSistema`, `IdIngresoEgreso`, `IdOperacionSpot`, `IdCartera`, `MontoComision(Local)`, `ValorUFFechaInversion` (only `APOREINV`) and `Estado 'CRT'`, with NO guard of state in the WHERE (D-F3B-16); a type of
solicitud other than `APO` / `RES` is certified without effect; rejected ones are not touched. FM-CER-62: one call per request (per level) with the ids of the updated, in the order of the read, also with an empty
list, also in simulation, after the UPDATE and before the child; with `PLA.CallBatchURL` / `PLA.CallBatchToken` missing the ADAPTER fails with the legacy `Exception` (the harness reproduces that read) after the UPDATE.
FM-CER-63: the entry of the child, its counter / messages / results summed (rows first), its own front J. FM-CER-64: written AFTER the UPDATE and the child, BEFORE OWP and the log. FM-CER-65: both `equalsIgnoreCase`;
the list = the switch aportes of channel `OWP` first, then the updated of channel `OWP`, re-read from the view (the H2 stand-in of the view is a seeded table: the re-read sees the SEED state), `SolsecAsoc ""` for
`RESREINV`; an empty list sends nothing; fired also with counter > 0 and in simulation; the child never notifies OWP. FM-CER-66: `AgregarLog` entry (fixed record), result never read, one INSERT per solicitud
(rejected included) with ids from the sequence port, `Simulacion` copied. FM-CER-67: `APD` / `UI_EXITO` for the pending, `NombreEstado` from `PLA.Estado` (a missing state raises `NullPointerException` AFTER every effect),
description of the collaborator or the translation with parameters (`<code> sin traduccion` when missing). FM-CER-68: counter forced to 1 only with `p_SimularOperacion` exactly `"S"`, at the very end. FM-CER-69: a lot
with one rejection commits (counter 0). FM-CER-70: the order of effects of section 7; every exception rolls back everything of the connection, nothing compensates. FM-CER-71: the exceptions come out untouched.

## 4. The depth guard (decision 2; the ONLY intended behaviour difference of T5 besides the gates)

The legacy recurses with no limit. The facade FAILS CLOSED when the depth of the recursive certification is greater than 1: the child (depth 1) runs, a grandchild (depth 2) is refused with
`ServicioNoServidoException` BEFORE it does anything (503: the router repeats the request in TomEE, which has no limit). `CerT5CompuertaTest.laRecursionMasProfundaQueUnoFallaCerrada` and the two cases tagged
`T5-profundidad` (the golden ran three levels) pin it: the new calls are the legacy ones up to the ENTRY of the grandchild, whose record answers `error: ServicioNoServidoException` with no inner calls; the host
rolls back everything.

## 5. Test resources (created by `legacy-oracle/sync_cer_t5.py` from the oracle output)

`src/test/resources/fondomutuo-cer/cases-t5/<id>/{case.json,seed.sql,expected.json}` (ids `FMM-CER-759_slug` on: T4-B ends at `FMM-CER-758`) and `fondomutuo-cer/determinism-cer-t5.txt` (cases x 20 runs, 0 unstable).
NO new seed file: the cases load the schema and the bases of T4-B. The case tags are listed in `cases_cer_t5.py` (`T5-modo`, `T5-compuerta:Gn`, `T5-profundidad`, `T5-m3:x`, `T5-async-parametros`).

## 6. Open questions (a human decides before implementing; each has a recommendation)

* **Q-T5-1 (gate and ports of the notifications).** One gate G8 (`g8Notificaciones()`, property `fondomutuo.notificaciones.habilitadas`) for BOTH notifications, and two NEW typed ports. The existing
  `NotificacionOwpPort` (JsonObject) and `Compuertas.g4Owp()` (OWP of the channel of Normal) stay as they are. Recommendation: accept; do not reuse G4 (it guards a different caller).
* **Q-T5-2 (what the depth guard raises).** `ServicioNoServidoException` (503) vs a 500. Recommendation: 503: the router repeats the whole request in TomEE, whose legacy has no limit, so the behaviour for the
  (today impossible) depth 2 stays the legacy one; the limit (1) a named constant.
* **Q-T5-3 (the observation hook).** `Dependencias.envolverHija` is a test-oriented extension point that production leaves null. Alternative: the facade exposes the child through a decorator bean. Recommendation:
  keep the hook, documented, no logic behind it.
* **Q-T5-4 (granularity of G1, G2, G3, G6, G-CUS).** The host cannot know BEFORE the first write which rescate a collaborator will reject later (custody, Tesoreria), so a superset predicate ("a request that
  may reach the port") is sound and a rescate with commission that custody would reject still answers 503 with G6 shut. The tests pin only runs that DO reach the port (503) and runs that do not (the legacy run).
  Recommendation: the superset predicate evaluated right before `ingresarCustodia`.
* **Q-T5-5 (D-F3B-02: notifications inside the transaction).** The legacy sends the asynchronous notification and OWP BEFORE the commit, also in simulation or with counter > 0. The tests pin that (the golden). If the
  expert decides "after commit only" (QD-F3B-14) the ports make the change local, but the goldens must be re-read. Recommendation: replicate now.
* **Q-T5-6 (limits of H2).** `Spread` and `ParidadFechaCertificacion` are rounded to an integer when written (H2 types the parameter of the `CASE` / `COALESCE`); HANA keeps the decimals. The re-read of OWP
  sees the seeded state of the view. Both are properties of the stand-in; the same SQL text on HANA must be checked in a catalog run.
* **Q-T5-7 (the parameters of the batch server).** The facade does not read `PLA.CallBatchURL` / `PLA.CallBatchToken`: the ADAPTER of `NotificacionCertificacionPort` does and raises the legacy `Exception` when
  missing. The harness reproduces that read for the cases tagged `T5-async-parametros`; the adapter itself is not covered by the equivalence tests. Recommendation: a contract test of the adapter with the same
  message.
* **Q-T5-8 (the old T4 tests).** `CerT4CompuertaTest.sinImplementacionDeProduccionDeLosPuertos` asserts that NO class implements `CertificacionHijaPort`; the facade now does. It must be updated by the
  implementer (excluding `CertificarAporteRescateFachada`); the T5 tests supersede its `@Disabled` siblings (see the report).
