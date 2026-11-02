# API assumed by the FMM-CSR tests (F3 W3, tanda T4-A, step 1: tests and oracle of `CrearSolicitudAporteReinversion`)

Legacy class: `cl.bcs.fondomutuo.CrearSolicitudAporteReinversion` ("AR", 1.496 LOC, `logica` of one lot of up to 2.000 aportes; it creates the `APOREINV` requests that Certificar asks for
from `crearAporteReinversion`, C:2343-2560). Cards FM-REI-41..68 and defects D-F2-4, 8, 11, 12, 13, 18..21, D-NOR-5/7/9/16, D-SQL-1, D-REQ-1/6/7/9, D-TES-1/2 of
`RULES_FONDOMUTUO_F2_REINVERSION.md` 8.2 and 9, plus the new defects D-CSR-01..12 (see `TRANSFORMATION_NOTES.md`, "F3 W3 T4-A (pruebas)").

The production code does NOT exist yet (red before code). The tests of this package (`ApiCsr`, `CasoCsr`, `SujetoCsr`, `CsrCostura`, `CorridaCsr`, `CsrEquivalenciaTest`, `CsrDeterminismoTest`,
`CsrReglasTest`, `CsrReglasValidacionTest`, `CsrReglasEscrituraTest`, `CsrCompuertaTest`, `CsrContratoTest`) COMPILE without it: the new classes are reached by reflection in
`ApiCsr` (the only file that knows their names) and every test that needs them fails with `AssertionError: CSR not implemented: the production class ... does not exist yet`.
T1..T3 of Certificar (`certificacion/`, `Cer*Test`) and every other package are untouched and stay green.

## 1. Classes (package `cl.bcs.fondomutuo.aportereinversion`, main source set; the name follows `rescatereinversion`, the sibling family)

| Class | Kind | What the tests use |
|---|---|---|
| `AporteReinversionContrato` | `final`, nested classes | `Entrada`, `Salida`, `InSolicitudesAporte`, `ConsultaRegimenReinversion`: the legacy nested classes with the SAME public field names, Java types, ORDER and constructor defaults (pinned by `CsrContratoTest`: 7 + 38 fields, `FechaValorCuota` is `java.sql.Date`, `Salida` holds `kit.MensajeSalida` and `comisionnn.ResultadoSolicitud`) |
| `AporteReinversionPort` | interface, ONE method | `Salida logica(Entrada) throws Exception` (legacy `logica(con, entrada)`; the connection is the ambient transaction) |
| `AporteReinversionDatos` | interface | the reads and writes that are the class's own (SQL verbatim, all of them already in `src/main/resources/.../sql`): regimes, `GEN_OCCINV` indicator, `OperaLineaSobregiro` (view `FMM.ConsultarIndicadoresPortafolioFondoMutuo`), proposito, regimes of the related rescues, destination rows, UPDATE of `FechaFinalReinversion`, INSERT of the requests. Its method names are NOT part of the contract (the harness builds the real adapter over the case H2) |
| `AporteReinversionJdbc` | adapter | `public AporteReinversionJdbc(ConexionUnidadDeTrabajo)`: the only constructor the harness calls |
| `AporteReinversionFabrica` | `final` | `public static AporteReinversionPort crear(Dependencias)` and `public static final class Dependencias` with PUBLIC fields (below) |

`Dependencias` (public fields, set by name by `SujetoCsr`): `basicos` (RevisarRequisitosBasicosPort), `configuracion`, `empresas`, `diccionario`, `calendario`, `series` (SeriesFondoPort),
`requisitosDatos` (RequisitosDatos: portfolio + client), `comisionNnDatos` (ComisionNnDatos: movement types and currencies), `datos` (AporteReinversionDatos), `perfilPort`, `fechasPort`,
`cuotasPort`, `patrimonioPort`, `flujoCustodiaDatos`, `agfDatos`, `secuencias` (SecuenciaPort), `paridad`, `precios`, `custodia` (CustodiaPort), `flujoCaja` (FlujoCajaPort), `log` (LogPort),
`productos`, `tesoreria` (`normal.ProcesosExternos.TesoreriaPort`), `spot` (`normal.ProcesosExternos.SpotPort`), `compuertas`. Every closure service is the ALREADY PORTED one
(`CalcularFechaSolicitudAporteRescate`, `CalcularMontoNumeroCuota`, `ServicioCumpleRequisitosService`, `ValidaMontoInversionService`, `ValidaPatrimonioEmisorService`,
`ConsultarFlujoCustodiaFondoMutuoService`, `CrearSolicitudAgfService`, `ObtenerPerfilFondoMutuo`): nothing is ported again. No `Compuertas` bean method is added.
If production prefers other names, change `ApiCsr` / `SujetoCsr` (never the goldens).

## 2. Entry and exit

* The input is read and the output written with `JsonLegacy.plano()` (the legacy `call`: plain `new Gson()` both ways). In process (Certificar, Manejador, ProcesarCarga) the typed classes
  are used and the class MUTATES its input as the legacy: `TipoReinversion` of every aporte is set, and `p_SolicitudesAporte` is REPLACED by the sublist of each lot of 2.000 (after a
  call with more than 2.000 aportes the list holds only the last lot: D-F2-19). Pinned by `CsrContratoTest` and the cases `lote-mixto-de-tipos`, `lote-2001-aportes-dos-lotes`.
* Not served yet: no JavaCall handler, no registry entry, no `@Component` of the service, no Manejador destination (list `REINVERSION_APORTE`). See Q-T4A-6.
* Certificar's contract (cases `certificar-*`): it sends `p_IdTipoCanalIngreso` AGE, `p_SimularOperacion` N, `p_UsaReglasNegocio` of its own input, and per aporte the 31 fields it fills
  (C:2385-2442; `FechaValorCuota` and `GeneraSpot` are never sent; `Canal` = the channel of the rescue); it reads back `P_OUT_ERRORESCONTADOR`, every `ERROR` message (`id` = the
  `IdInterno` of the aporte, `Math.abs`, mapped by Certificar to the rescue) and, per result, `IdInterno`, `IdSolicitudAporteRescate`, `IdTipoCanalIngreso` and a NON-null `Switch`.

## 3. Collaborators, doubles and the two additive changes of existing classes

The oracle (legacy executed on H2, `record_csr.sh`) answers with REGISTERED DOUBLES: Plataforma (`ConsultarEmpresa`, `CalcularParidad`, `EntregarPreciosTasas`, `SaldoFlujoCaja`, `AgregarLog`,
`ConsultarProductosPortafolio`), Custodia (`ConsultarFlujoCustodia`) and, as in `record_cer.sh`, the two FondoMutuo helpers WHOLE (`ProcesarIngresoEgresoTesoreria`,
`ProcesarIngresoOperacionSpot`): the golden records the complete argument the AR builds. `SujetoCsr` records the same calls (`Guion`, same Gson settings): Tesoreria and Spot behind
`ProcesosExternos.TesoreriaPort` / `SpotPort`, the others behind the existing HTTP ports.

ONE additive change of an existing class is assumed (the tests cannot reference it, the harness converts by Gson): `ProcesosExternos.EntradaSpot` gains
`public List<Object> p_DatosOrigenInversion = new ArrayList<>()`: the legacy sends the INPUT aportes (all 38 fields, `TipoReinversion` as classified) in that key; Certificar always sends `[]`
(`CerT3*` compare it as absent when empty), the AR does not. `CsrCompuertaTest.puertoTipadoDeSpot` reads the field by reflection.

## 4. Gates (Q-T4A-3: ASSUMED, not in the legacy; the approved kind of difference: a 503 that the router repeats, nothing written)

| Gate | Evaluated | Condition |
|---|---|---|
| G3 `g3SaldoFlujoCaja()` | just before `SaldoFlujoCaja` | at least one PENDING CIN request: also in simulation (D-F2-13) |
| G-CUS `gCustodia()` | inside `ConsultarFlujoCustodiaFondoMutuoService.logica` (it already does it) | at least one PENDING request with `PatrimonioMinimo > 0` (also in simulation) |
| G1 `g1MedioDistintoDeCin()` | real pass, after the checks, BEFORE Tesoreria is called (it writes through CrearIngresoEgreso) | at least one request that goes to Tesoreria (medium other than CIN) |
| G2 `g2Spot()` | real pass, BEFORE Tesoreria and the INSERT | parameter `APORTE_GENERA_COMPRA_SPOT_DOLAR` = `S` (exact) and at least one PENDING request with `GeneraSpot S` (the legacy decides on the requests that survive Tesoreria; the gate is conservative) |
| G5 | not a property: an answer 503 of `AgregarLog` (router) | whole request = 503 `ROLLBACK`, nothing stays (`CsrCompuertaTest.g5_agregarLog503`) |

Order = order of the legacy calls: G3, G-CUS, then G1, then G2. The shipped defaults (every gate shut) serve only what needs no collaborator. `CsrCompuertaTest` pins all of it.

## 5. Test resources (created by `legacy-oracle/sync_csr.py` from the oracle output)

`src/test/resources/fondomutuo-csr/cases/<id>/{case.json,seed.sql,expected.json[.gz]}` (ids `FMM-CSR-nnn_slug`; the two goldens over 1 MB are gzipped), `schema-csr.sql` (the ONE table the AR
needs that the F1 schema lacks, `FMM.ReinversionSerieFondoDestino`, plus the stand-in of the view `ConsultarIndicadoresPortafolioFondoMutuo`: ASSUMED columns, Q-T4A-4),
`seed/base-csr.sql` (fake universe: 7 series, 7 regimes, 8 portfolios, rescues 9001 / 9002, destination rows 1..3, calendar, BATCH and UNITARIO date configuration) and
`determinism-csr.txt` (cases x 20 runs, 0 unstable). The F1 schema and `seed/base-fmm.sql` of `fondomutuo-3a/` are used as they are, the module's other fixtures are not modified.

## 6. Limits of H2 against HANA (documented, none changes a golden)

* `0001-01-01` (the sentinel date): the output text keeps `0001-01-01`, the table reads back `0000-12-30` (cases `switch-s-sin-fechas-de-origen`, `switch-minuscula`).
* `FMM.InterfazAgf` is one wide table in H2 (the columns NOT NULL of the beneficiary insert and of the request insert together): the AGF insert fails unless the case relaxes the columns
  (`agf-h2-standin-estricto-falla` pins the failure, `agf-con-fila-en-la-vista` the success).
* The views of HANA are tables in H2 (`ConsultarSolicitudAporteRescate`, `ConsultarIndicadoresPortafolioFondoMutuo`, `ConsultarClienteEstado`); `CASE`/`COALESCE` with decimals, `NEXTVAL`
  and `CURRENT_UTCTIMESTAMP` are the dialect bridge of `SesionHana` (the same one of the Normal and RescateReinversion oracles).
* No ORDER BY in `SelectReinversionSerieFondoDestino`: the order of the rows H2 returns is the insertion order; HANA does not promise one (the last UPDATE wins: D-F2-4).
