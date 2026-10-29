# BcsTesoreriaServicios - characterization tests, family A (ingreso/egreso, comprobantes, liquidacion, anulaciones, autorizaciones)

Goldens (`src/test/resources/tesoreria-a/cases/<TES-A-NN_slug>/expected.json`) are **recorded by executing the unmodified legacy classes**
(`legacy/.../BcsTesoreriaServicios/src/cl/bcs/tesoreria/*.java`), never written or computed by hand. The legacy is the oracle: where it looks
wrong the golden keeps the legacy behaviour and the defect is listed below.

## What runs

| Piece | Where |
|---|---|
| Tests (one class per legacy class, `tesA<NN>_<slug>` methods, comment cites the legacy file) | `src/test/java/cl/bcs/tesoreria/familia_a/*ServiceTest.java` |
| Harness (case loader, H2 bridge, canonical JSON comparison, dump) | `.../familia_a/support/` |
| Cases | `src/test/resources/tesoreria-a/cases/TES-A-NN_slug/{case.json,seed.sql,expected.json}` |
| Schema / base seed / config | `src/test/resources/tesoreria-a/{schema-tesoreria-a.sql,seed/base-tesoreria-a.sql,seed/config.json}` |
| Oracle (shims, runner, record.sh) | `src/test/resources/tesoreria-a/legacy-oracle/` |
| Pending tests (not compiled, intended API) | `src/test-pending/tesoreria-a/` |
| RULE-076 (no `main()` outside the Spring class, no credential / JDBC literal in `src/main`) | `Rule076SinCredencialesNiMainTest` |
| Harness self-check (the harness can fail) | `HarnessSelfCheckTest` |

Each test class prints `equivalence cases executed (<Servicio>): N` and asserts N > 0. A missing `case.json`/`expected.json`/seed fails (it never skips);
`TesoreriaAInventarioTest` checks every case directory is run by exactly one test (or listed in `pending-cases.txt`) and has a golden with provenance.
The tests run with the JVM zone forced to UTC (`ZonaUtc`), as recorded.

## Run

```
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :tesoreria-service test -Dtest='cl.bcs.tesoreria.familia_a.*Test'
```

Dual-execution hook: `-Dequivalence.out=<dir>` makes `cl.bcs.tesoreria.support.VolcadoEquivalencia` dump (expected, actual) pairs per case.

## Re-record (only if a legacy source or a case changes)

```
export JAVA_HOME=...; export ORACLE_WORK=<scratch dir>   # legacy classes are compiled there, nothing is written into legacy/
src/test/resources/tesoreria-a/legacy-oracle/record.sh [TES-A-prefix]
```

Mutation check of the goldens: `LEGACY_OVERLAY=<dir with a mutated copy of a legacy .java> ORACLE_OUT=<dir> record.sh` and compare
`<ORACLE_OUT>/<case>.expected.json` with the committed golden (a difference proves the case pins that behaviour).

## Add a case

1. Create `cases/TES-A-NN_slug/case.json`: `servicio` (class of `cl.bcs.tesoreria`), `metodo` (`call` | `logica` | `autorizarEvento` | `rechazarEvento`),
   `entrada`, optional `config` (`"MODULO/Parametro": value`, `null` removes), `secuencias`, `colaboradores` (scripted fakes), `tablas` (tables to dump);
   and `seed.sql` (rows on top of the base seed).
2. Run `record.sh TES-A-NN` (writes `expected.json`), review it, add the id to the test class `CASOS` list (and a `@Test` method that calls `verificar`).

## How the oracle executes the legacy

H2 2.4 (MODE=LEGACY) stands in for SAP HANA through the `H2Hana` connection proxy (it rewrites `#Temp` tables, `BSCL."X"` schema prefixes, `UPDATE .. FROM .. JOIN`
as MERGE, `"seq".NEXTVAL`, `MAP/UCASE`, `TO_*` functions, `CURRENT_UTCTIMESTAMP` = fixed clock 2025-04-01 10:15:30). Collaborators that are other services
(CrearCargoAbono, AnularCargoAbono, ActualizarSaldosCuentaBanco, ConsultarFlujoCaja, CrearEventoAutorizacionMasivo, ...) are fakes whose calls are recorded in
`expected.json` `"colaboradores"` (`{servicio, entrada}`). Stored procedures (EdicionMultiple, CrearComprobante with `USARCREARCOMPROBANTEJAVA=N`, the
LiquidacionMasiva call) are only recorded, never executed; their output tables are pre-seeded.

## Accepted assumptions and what is outside the proof

Decisions taken by the owner of the migration:

* The schema is **inferred** from the legacy SQL (about 110 tables; HANA views modelled as seeded tables). Accepted as a documented assumption: column types are approximations
  (for example `DECIMAL(28,16)` amounts, so a JSON built from a BigDecimal by the legacy, as in `CuadraturaCierreDiario.DetalleCuadraturaJSON`, carries scale 16).
* **Stored procedures are outside the proof**: `EdicionMultiple` (`BSCL.Tesoreria.BD.Servicios::EdicionMultiple`), `CrearComprobante` with `USARCREARCOMPROBANTEJAVA=N` or comprobantes as source
  (`...::CrearComprobante`), the `LiquidacionMasiva` call and `LiquidarMovimientoAutomatico` (`...::ModificarIngresoEgreso`) are NOT executed. The oracle records the call as the collaborator
  `StoredProcedure` (procedure name + scalar inputs) and the case seeds / scripted outputs (`colaboradores.SalidaProcedimiento`) stand in for what HANA would return. What these cases
  pin is the Java side around the procedure, never the procedure itself.
* Fixed clock `2025-04-01 10:15:30` (`CURRENT_TIMESTAMP`, `CURRENT_UTCTIMESTAMP`), empty `M_TEMPORARY_TABLES` and JVM zone UTC.
* The platform commits when a service returns and rolls back when it throws.
* Where H2 text or exception type would differ from what HANA gives, the case is DROPPED from the proof (not relaxed): TES-A-429 (`ModificarComprobanteTesoreria`, non numeric Monto: H2 conversion
  error), TES-A-443 (`EjecutarAnulacionIngresoEgreso`, invalid date: H2 parse error), and TES-A-345 / TES-A-355 (update-join with duplicated source rows, an artefact of the H2 MERGE emulation).
  They are listed as uncovered; the first two would need a real HANA (or a legacy-faithful text) to be pinned.
* Result order of queries without `ORDER BY` is not pinned: those arrays are compared as sets (`SIN_ORDEN`), e.g. `P_OUT_ERRORESDETALLE`, `P_OUT_COMPROBANTES`.

## Coverage (cases recorded from the legacy)

| Legacy class | Cases | Test |
|---|---|---|
| ValidarTransferenciaDVP | 17 | compiled |
| CrearIngresoEgreso | 60 | compiled |
| LiquidarComprobante | 65 | compiled |
| CrearComprobanteJava | 35 | compiled |
| CrearComprobante | 8 | compiled |
| CrearComprobantePartidaInEg | 7 | compiled |
| LiquidacionMasivaComprobante | 10 | compiled |
| PreLiquidarComprobante | 9 | compiled |
| AnularRegistros | 38 | compiled |
| ConfirmarRechazarAnularMovimientosTesoreria | 8 | compiled |
| ConfirmarAutorizarAnularMovimientosTesoreria | 7 | compiled |
| AutorizacionLiquidacion (autorizarEvento / rechazarEvento) | 15 | compiled |
| AutorizacionAnularMovimientosTesoreria | 8 | compiled |
| LiquidarComprobanteTodos | 13 | compiled |
| ModificarComprobanteTesoreria | 10 | compiled |
| EjecutarAnulacionIngresoEgreso | 4 | compiled |
| EjecutarAnulacionComprobantes | 4 | compiled |
| RequisitosGirarComprobanteIngreso | 26 | compiled |
| RequisitosGirarComprobanteEgreso | 22 | compiled |
| GirarComprobanteIngreso | 14 | compiled |
| GirarComprobantesEgreso | 13 | compiled |
| ConsultarComprobantes | 6 | compiled |
| ConsultarComprobantesWS | 19 | compiled |
| ConsultarIngresoEgreso | 14 | compiled |
| SaldoPendienteDeLiquidar | 21 | compiled |
| CrearTraspaso | 13 | compiled |
| cloper.ActualizarTransaccion | 20 | compiled |
| CuadraturaCierreDiario | 16 | compiled |
| EdicionMultiple (procedure outside the proof) | 10 | compiled |
| LiquidarMovimientoAutomatico (procedure outside the proof) | 13 | compiled |
| pojo.Traspaso (defaults of the data holders) | 1 | compiled |

The POJOs `IngresoEgreso`, `IngresoEgresoFormaDePago` and `ConstantesTesoreria` are exercised only through the services above (they hold no behaviour of their own).
Cases that need a collaborator the modern code cannot yet call (`Colaboradores`) are red against the current `src/main` by design (for example TES-A-632 / 633, which script
`ObtenerPortafoliosEjecutivo`).

## Legacy defects for SME review (KEPT and pinned by the goldens)

* CrearIngresoEgreso: a forma de pago tarjeta with null card ids throws NPE, reported as a generic error; `IdTipoComprobante == "CSC"` compares Strings with `==`; a usuario without empleado gives an NPE converted to a generic error.
* ValidarTransferenciaDVP: the last item wins on `P_OUT_VALIDO`; the counter of `logica` is always 1.
* CrearComprobanteJava: `COMPROBANTE_TESORERIA_SECUENCIA_UTILIZADA` and `FALTA_INCLUIR_MOVIMIENTOS_SECUENCIA` checks can never fire (cases TES-A-219/220).
* LiquidacionMasivaComprobante: `P_OUT_FORMADEPAGO` compares comprobante ids with forma-de-pago ids and always returns empty.
* PreLiquidarComprobante: inverted `NoValidaSaldoCaja` logic; early returns skip the rollback.
* AutorizacionLiquidacion: the success branch compares a String key with a `HashMap<Long,...>` and never reports success (TES-A-360).
* ConfirmarRechazarAnularMovimientosTesoreria: swallows exceptions; `call()` recomputes the error counter to 0.
* LiquidarComprobanteTodos: rolls back when the counter is null or greater than 0.
* AnularRegistros: a null message can be added; `INGRESO_EGRESO_ANULADO` is never added for non-TES modules.
* RequisitosGirarComprobanteIngreso / Egreso: `p_simularOperacion` is ignored (the cuenta check always runs as `N`, TES-A-518); a corredora account that does not exist raises no error because the
  query groups an empty set (TES-A-516); `P_OUT_ERRORESCONTADOR` is set to 1, never incremented; a null `p_ComprobantesAGirar` escapes as a SQLException from `SQL.batch` (TES-A-524 / 551).
* GirarComprobanteIngreso / GirarComprobantesEgreso: every exception is swallowed (`printStackTrace`) and an empty salida is returned while the rows already written stay committed (TES-A-567, 568, 585, 590, 591);
  Ingreso sets the comprobante to `GIR` even when `CrearComprobante` returned errors (TES-A-562); Egreso updates the formas de pago and creates the nomina even then (TES-A-588); `p_UsaReglasNegocio` is forced to `N`.
* ConsultarComprobantesWS: the one-sided date range returns the generic `FECHA_LIQUIDACION_NO_VALIDA`; the range limit counts both end days (30 days inclusive, TES-A-613 / 612).
* SaldoPendienteDeLiquidar: the query that resolves clients joins `MON.IdMoneda = MON.IdMoneda` (a cartesian product) and the list of "pending" socios is never filled, so a socio without IdPortafolio or IdMoneda
  is silently not processed (TES-A-709 / 710); only the first row per socio is kept from the movement queries (not pinned: with several liquidation dates HANA's row order decides).
* CrearTraspaso: after `NO_EXISTE_INGRESO_ASOCIADO` the process continues and fails later (TES-A-742); references match on absolute value but forma de pago on exact value, so two references with the same sign
  both receive every forma de pago (TES-A-748).
* cloper.ActualizarTransaccion: the fallback `"0"` for the corredora code is dead code (`obtenerCodigoCorredora` never returns null), the WS is called with an empty user (TES-A-777).
* CuadraturaCierreDiario: the same delete parameter object is added once per process id; the JSON detail embeds BigDecimals with the column scale and timestamps with time part.
* EdicionMultiple: warnings or authorizations silently skip the liquidation without any message (TES-A-803 / 804); the procedure always receives `p_LiquidaComprobante = 'N'`.
* LiquidarMovimientoAutomatico: the comprobante is created with `p_UsaReglasNegocio = 'N'`, so an annulled movement is accepted (TES-A-816); a forma de pago whose reference does not exist throws NPE (TES-A-819).
* ConsultarIngresoEgreso: the work id of the step table is `Math.random()` (not pinned: it never reaches the output).

## Not covered (uncovered with reason)

* TES-A-345 / 355 (ConfirmarRechazar / ConfirmarAutorizar): update-join with duplicated rows, only fails in the H2 MERGE emulation.
* TES-A-429 (ModificarComprobanteTesoreria) and TES-A-443 (EjecutarAnulacionIngresoEgreso): the failure is an H2 conversion/parse text, different from what HANA would give.
* The stored procedures themselves (see above), the HANA row order of queries without `ORDER BY` with several groups per key, and anything that depends on real HANA types.

## Mutation checks of the goldens (legacy-side mutants)

See the final report of the run; reproduce with `LEGACY_OVERLAY` / `ORACLE_OUT` (section "Re-record").

## Status after Step 2 (implementer A)

All 526 cases of family A run as compiled tests against `src/main` (`javacall/`): every service takes only the ports it uses
(`NamedParameterJdbcTemplate`, `ConfiguracionSistema`, `DiccionarioMensajes`, `Secuencias`, `Reloj`, `Colaboradores` last); the test doubles are in
`support/` (`Dobles.de(caso)`: `ConfiguracionDelCaso`, `DiccionarioDeCaso`, `ColaboradoresDeCaso` with its call trace, `SecuenciasDeCaso`, fixed clock).
`src/test-pending/tesoreria-a` only keeps its README; `pending-cases.txt` is empty.
