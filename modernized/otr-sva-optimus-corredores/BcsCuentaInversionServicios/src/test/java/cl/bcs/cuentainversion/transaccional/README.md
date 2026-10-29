# Characterization tests: BcsCuentaInversionServicios, transactional family (agent A)

Behaviour ids are `CTA-A-NN` (BUSINESS_RULES.md has no rule for this module except RULE-076, which is not touched here).
Every expected value comes from EXECUTING the unmodified legacy class (`legacy-oracle/`), never from hand calculation.
Legacy defects are KEPT and pinned (see "Legacy defects for SME review"). All 16 legacy classes are covered.

## Mapping legacy -> modern -> test

| Legacy class (cl.bcs.cuentaInversion) | Modern class | Test class | Cases |
|---|---|---|---|
| ActualizarSaldoCargoAbono | ActualizarSaldoCargoAbonoService | ActualizarSaldoCargoAbonoServiceTest | CTA-A-01..11 (11) |
| CertificarCargoAbono | CertificarCargoAbonoService | CertificarCargoAbonoServiceTest | CTA-A-12..31 (20) |
| AnularCargoAbono | AnularCargoAbonoService | AnularCargoAbonoServiceTest | CTA-A-32..47, 108 (17) |
| CrearCargoAbono (Java path) | CrearCargoAbonoService | CrearCargoAbonoServiceTest | CTA-A-48..76 (29) |
| CrearCargoAbono (HANA stored-procedure path) | CrearCargoAbonoService | CrearCargoAbonoProcedimientoAlmacenadoServiceTest | CTA-A-241..246 (6) |
| SaldoCuentaInversion | SaldoCuentaInversionService | SaldoCuentaInversionServiceTest | CTA-A-77..90 (14) |
| ConsultarSaldoCuentaInversion | ConsultarSaldoCuentaInversionService | ConsultarSaldoCuentaInversionServiceTest | CTA-A-91..107 (17) |
| ConsultarSaldoCuentaInversion (money-market valuation) | ConsultarSaldoCuentaInversionService | ConsultarSaldoMoneyMarketServiceTest | CTA-A-247..254 (8) |
| EliminaCargaSeleccionada, CrearCargaMasiva, ProcesaCargaSeleccionada | EliminaCarga.../CrearCargaMasiva.../ProcesaCarga...Service | CargaMasivaServicesTest | CTA-A-109..112, 113..119, 120..124 (16) |
| GenerarArchivoCargoAbono | GenerarArchivoCargoAbonoService | GenerarArchivoCargoAbonoServiceTest | CTA-A-125..137 (13) |
| ConsultaComparacionSaldo | ConsultaComparacionSaldoService | ConsultaComparacionSaldoServiceTest | CTA-A-138..145 (8) |
| CuadraturaCierreDiario | CuadraturaCierreDiarioService | CuadraturaCierreDiarioServiceTest | CTA-A-146..158 (13) |
| ConsultarMovimientoCuentaInversion | ConsultarMovimientoCuentaInversionService (+ HanaRepository) | ConsultarMovimientoCuentaInversionServiceTest | CTA-A-159..188 (30) |
| ConsultarCartolaDeMovimiento | ConsultarCartolaDeMovimientoService | ConsultarCartolaDeMovimientoServiceTest | CTA-A-189..207 (19) |
| webservices.CrearCargoAbonoWS | CrearCargoAbonoWSService | CrearCargoAbonoWSServiceTest | CTA-A-208..225 (18) |
| webservices.ConsultarMovimientos | ConsultarMovimientosWSService | ConsultarMovimientosWSServiceTest | CTA-A-226..240 (15) |

254 recorded cases, 254 equivalence tests (one per case), plus harness proofs: `HarnessSelfCheckTest` (5), `ArchivosSelfCheckTest` (5) and
`TransaccionalInventarioTest` (3: every case directory has exactly one test, a golden and provenance; nothing is silently skipped).

## How to run

```
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :bcs-cuenta-inversion-servicios test \
    -Dtest='cl.bcs.cuentainversion.transaccional.*Test' -Dsurefire.failIfNoSpecifiedTests=false
```
Each class prints `equivalence cases executed (X): N` and asserts N > 0. A missing `expected.json` fails (never skips). There is no `@Disabled`.
The tests pin the JVM time zone to UTC (`support/ZonaUtc`) because the legacy computes dates with the JVM zone.

## How to re-record the goldens (legacy oracle)

`legacy-oracle/record.sh` compiles the UNMODIFIED legacy sources (read from `legacy/...`) plus the shims in `legacy-oracle/shims` into a scratch
directory (`ORACLE_WORK`, never inside legacy/ or modernized/) and runs `legacy-oracle/runner/OracleRunner.java` with `-Duser.timezone=UTC`.
Requires JDK 25 and the dependency jars of the local maven repository (h2 2.4.240, gson, commons-collections). Knobs: `ORACLE_WORK`, `CTA_CLASSES`,
`WS_CLASSES` (`NONE` to skip the WS classes), `JAVA_OPTS`, `EXTRA_SOURCES`. Optional argument: case-name prefix (`./record.sh CTA-A-127`).
Shims replace the platform (SQL, Utils, JavaCall, Log, FechaDB, Diccionario, ConfiguracionSistema, ...) and the collaborating services (AgregarLog,
SaldoFlujoCaja, RevisarRequisitosBasicos, AgregarEvento, ConsultarImagen, ObtenerPortafoliosEjecutivo, CalcularParidad, EntregarPreciosTasas ...) with
recording fakes whose answers are scripted in `case.json` ("colaboradores"). `ObtenerDiaSiguiente`, `ObtenerDiaAnterior`, `Fecha`, `MonedaFormato` and the
POJOs run as the REAL legacy source. No real HANA, service or credential is used; none was copied from legacy code.

## How to add a case

1. Create `src/test/resources/transaccional/cases/CTA-A-NNN_<slug>/case.json` (`servicio` (prefix `webservices.` for the WS classes), `metodo` call|logica,
   `entrada`, optional `config`, `colaboradores`, `secuencias`, `tablas` to dump) and optional `seed.sql` (on top of `seed/base-transaccional.sql`).
2. Run `record.sh CTA-A-NNN` to create `expected.json`; review it as a spec, do not edit it by hand.
3. Add the name to the `CASOS` list and a `@Test ctaANNN_...` method (comment with legacy file:line) in the test class.

## File capture (GenerarArchivoCargoAbono, ConsultaComparacionSaldo)

These legacy classes write a csv under `PLA.RUTABASEARCHIVOS + RUTAARCHIVOSCTA` (both read from the configuration TABLES). The seed uses the placeholder
`${RUTA_BASE}`; `support/Archivos` replaces it with a fresh temporary directory (with a `CTA/` sub directory) in the oracle and in the tests, captures every
file written below it (golden `"archivos"`: relative name -> content) and deletes the directory.
* Masking regex (documented, the only masking): `\d{8}_\d{6}` (yyyyMMdd_HHmmss run timestamp) -> `<TS>`, applied to file names, to every compared string
  (`Canon`) and to the recorded golden.
* Contents are read as UTF-8 with line endings normalised to `\n` (the legacy uses the platform `newLine()`).
* The csv header text is whatever the oracle's `Diccionario` fake answers: `[CODE]{params}` (or `<CODE> sin traduccion` for a missing translation, as in
  the message descriptions of every golden). The modern code needs an injectable dictionary for these tests to pass.
* Number cells (`Cargo`, `Abono`, ...) are rendered by `CAST(.. AS NVARCHAR)` of the modelled view columns: `55,5000000000000000` is a consequence of the
  accepted DECIMAL(28,16) assumption, not a legacy rule.

## Dialect bridge (support/H2Hana)

Legacy SQL is HANA. Both the oracle and the modern tests run it on H2 (MODE=LEGACY) through the same bridge: `#Name` -> `TT_Name`, `BSCL."X"` -> `"X"`,
`CURRENT_UTCTIMESTAMP` -> `HANA_UTCNOW()` (fixed clock 2025-04-01 10:15:30), `MAP(` -> `DECODE(`, `UCASE(` -> `UPPER(`, `? AS "Alias"` casts, `RowNum`
alias rename, single-table `UPDATE .. FROM` rewrite, and structural overrides in `transaccional/h2-dialect/*.sql` (see its README.txt) for statements H2
cannot parse. HANA functions (TO_NVARCHAR, TO_DATE, ADD_SECONDS ...) are H2 aliases.

Schema assumptions (no DDL in the repo; accepted as documented assumptions): money columns DECIMAL(28,16); views are modelled as tables the seeds fill
(ConsultarCartolaDeMovimientos and ConsultarCargosAbonos are GENERATED from the column lists the legacy reads); PLA.Identidad is a 6x6 identity matrix;
CAR.CargaMasivaDetalle Plan/ProductoAgrupador/IdPortafolio are text; CTA.CuadraturaDiariaOperaciones has a primary key on its process id and
DetalleCuadraturaJSON is VARCHAR(1000000) instead of NCLOB; the columns the new classes need on existing tables (CLI.Cliente.IdEstado/FechaCreacion,
PER.*.Borrado, wide FMM.Fondo/SerieDeFondo) are H2 `INVISIBLE` columns so the positional INSERTs of the earlier cases keep working (all 124 earlier goldens
were re-recorded after this change and are byte-identical, except case 116 which now also dumps its tables). Transaction model confirmed: autocommit off,
commit on return, rollback on throw.

## Step 2 result (after the transactional family was implemented)

254/254 equivalence tests green plus harness (5), file harness (5), inventory (3). Collaborator traces ("colaboradores" of each golden) are verified in the
compiled tests of the services that use collaborators (`Verifica.comparar(caso, r, colab.traza(), ...)`); `ColaboradoresPendingTest` was removed because it is superseded.
The stored-procedure test (CTA-A-241..246) compares the scalar inputs except `p_UsaReglasNegocio` (the modern SQL fixes it to 'S' as the legacy does; the golden
records the request value). Constructor seams: `Colaboradores`, `DiccionarioMensajes`, `SecuenciasPort`, `CalendarioHabil`, `FormatoMoneda`.

## Historical: result against the stub family (before Step 2)

254 equivalence tests: 2 green, 252 RED. Harness (5), file harness (5) and inventory (3) green. The 84 pre-existing tests (CuentaInversionTest 60,
ServiceFormulasTest 24) still pass. The two green equivalence tests are honest coincidences (CTA-A-174 invalid date -> DateTimeParseException;
CTA-A-225 empty list -> empty output). Red tests are the Step 2 fix list, grouped by cause:

| Cause | Tests |
|---|---|
| Modern SQL uses named parameters without a value supplied ("No value supplied for the SQL parameter ...") | Actualizar 10, Anular 15, Crear 26, Saldo 13, ProcesaCarga 5, ConsultarMovimientosWS 13, Crear SP path 5, CrearCargoAbonoWS 10, Cuadratura 11, GenerarArchivo 9 = 117 |
| Modern SQL does not run on the HANA catalog / views (BadSqlGrammar: tables or columns that do not exist) | ConsultarSaldo 17, ConsultarSaldo money market 7, ConsultarCartola 19, ConsultarMovimientoCuentaInversion 25 = 68 |
| Output (messages, counters, rows) or table state differs from legacy | Certificar 18, carga masiva 11, ConsultaComparacionSaldo 8, CrearCargoAbonoWS 6, ConsultarMovimientoCuentaInversion 1, GenerarArchivo 2 = 46 |
| Legacy threw (pinned defect) but the modern code does not, or throws another type | Actualizar 1, Anular 2, Certificar 2, Crear 2, Saldo 1, ConsultarMovimiento 3, ConsultarMovimientosWS 2, MoneyMarket 1, CrearWS 1, Cuadratura 2, Generar 2 = 19 |
| Stored-procedure call to HANA from the modern code (`{call BSCL.CUENTAINVERSION...}`) | Crear 1, Crear SP path 1 = 2 |

Modern gaps visible in the red list: GenerarArchivoCargoAbonoService and ConsultaComparacionSaldoService write no file; ConsultarMovimientoCuentaInversionService
works per account through `HanaSaldoPort` (no per-portfolio "consulta" semantics, no SaldoSimulado, no historic/hybrid split, no JDBC path of its own).

Harness validity: `support/ActualizarSaldoReferencia` (test-side port of the legacy SQL sequence) is green against the 11 Actualizar goldens and 4 mutants of it
turn the suite red (`HarnessSelfCheckTest`). `support/GenerarArchivoReferencia` does the same for the FILE harness: green on the 13 GenerarArchivoCargoAbono goldens
(including the captured csv and the masked name) and 4 mutants (inclusive date bound, decimal separator, state filter, returned path) are killed
(`ArchivosSelfCheckTest`). Mutation of src/main is not applicable (it is already red).

## Legacy defects for SME review (kept and pinned)

ActualizarSaldoCargoAbono: `call()` is a no-op (returns null, never runs `logica`).
CertificarCargoAbono: `call()` forces P_OUT_ERRORESCONTADOR=0; PlazoEnDias never mapped (unaliased COALESCE) so FechaCalculada = FechaMovimiento; with reglas != S
FechaEfectivaMovimiento stays 0001-01-01; error Detalle ids are null; cash-flow check compares through `doubleValue` (a 1e-16 excess is accepted); notification
date pattern `dd-MM-YYYY` (week-year); IndexOutOfBounds when the user is not an employee; both legs of a transfer listed -> "key field is not unique".
AnularCargoAbono: simulation S still annuls; p_EjecutaAutorizacion N annuls without authorization; p_EsIngresoManual is taken from p_UsaReglasNegocio; NPE on a
missing id with reglas N; counterpart-branch messages have an empty tipo.
CrearCargoAbono: Monto `compareTo(ZERO) == -1` accepts zero; simulation S still schedules the job for own accounts; null simular acts as S; the *_NO_EXISTE checks
never trigger; missing Tesoreria currency -> NPE; message map keyed by IdOperacion (cross-contamination); an unmatched account is silently dropped; on the HANA path
`ObjInformacionDetalle` declares the field `Obj` instead of `NumeroCargoAbono`, so that column is never read.
SaldoCuentaInversion: the movement-date list is filtered by the EFFECTIVE-date balance; p_SoloSaldoDisponibleNegativo means `<> 0`.
ConsultarSaldoCuentaInversion: IdEntidad column not aliased so the employee portfolio filter is skipped; requests in transit are ignored with the default
p_IdProductoAgrupador 9999; FechaUltimoMovimiento renders 0001-01-03 without movement; a money-market series that SelectSerieFondo does not return makes the
valuation throw "List must be not null".
CrearCargaMasiva: every header uses `secuencias.get(0)` (PK violation with more than one header); any exception becomes a generic error.
ProcesaCargaSeleccionada: a record dropped silently by CrearCargoAbono leaves the header PRO; two messages with the same id fail in List2Hash.
GenerarArchivoCargoAbono: a NULL date in the view makes the whole concatenated line NULL and `BufferedWriter.write(null)` throws (the empty file stays, never
flushed); a NULL filter value (e.g. p_IdMoneda) matches no rows instead of meaning "all".
ConsultaComparacionSaldo: no differences is reported as an ERROR message (counter 1) although the header-only file is generated and its path returned.
CuadraturaCierreDiario: the delete ignores IdEmpresa while the select filters it (a row of another company is deleted and not re-inserted); the rows of unknown
processes that were selected are re-inserted without being deleted (primary key violation); null p_TipoLlamado -> NPE.
ConsultarMovimientoCuentaInversion: null IncluyeSaldo -> NPE; hybrid range without IncluyeSaldo uses `movimientos.get(1)` (drops the second historic movement, or
IndexOutOfBounds with one); the historic consultas list is never cleared (later consultas repeat earlier rows); historic query ignores the IdEstado filter.
ConsultarCartolaDeMovimiento: the movements query uses the request p_IdPortafolio / p_IdMoneda (null -> no rows although accounts matched); the counter stays null
when RevisarRequisitosBasicos reports errors.
webservices.CrearCargoAbonoWS: validation returns at the first error (the rest of the batch is not even looked at); with duplicate view rows the first row (no
ORDER BY) decides; null IdTipoMovimiento -> NPE.
webservices.ConsultarMovimientos: the condition for p_TipoFechaConsulta "LIQ" can never be true (always queries by movement date); the initial balance of each
consultation is the FINAL effective balance of the account; historic rows have no account (empty DocumentoIdentidad/CodigoPortafolio/Origen); null
FechaCertificacion -> NPE.
Locale: `NumberFormat es_CL` output is JDK/CLDR dependent.

## Paths NOT characterised (and why)

* The LOGIC of the HANA stored procedure `BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono` (SQLScript, not in the repository, not reproducible on H2). Only the
  Java side is pinned (CTA-A-241..246): the call and its scalar inputs are recorded as the collaborator "StoredProcedure", the case seed plays the procedure by
  pre-filling its four output tables. Whether the procedure computes the same rules as the Java path stays UNPROVEN.
* Prices and exchange rates of the money-market valuation come from scripted collaborators (EntregarPreciosTasas, CalcularParidad); their own logic is out of
  scope (CTA-A-247..254 pin quantities x unit value and the conversion call).
* Batch failure side effects (CTA-A-116) are now pinned, but they are the behaviour of the H2 JDBC driver (rows before the failing statement of the batch);
  HANA's driver may differ.

## Pending tests (not compiled) and intended API

`src/test-pending/transaccional/ColaboradoresPendingTest.java`: collaborator traces (`expected.json` "colaboradores", present in about 100 cases: Certificar, Anular,
Crear, ConsultarSaldo, ConsultarCartola (RevisarRequisitosBasicos), Cuadratura (AgregarLog), the stored-procedure call, EntregarPreciosTasas/CalcularParidad). The
modern ports are coarse and have no collaborator seams, so the traces cannot be verified yet. Intended API: a `Colaboradores` interface (`JsonObject invocar(String
servicio, JsonObject entrada)`) injected as last constructor argument of those services; the test hands the recorded `{servicio, entrada}` list to
`Verifica.colaboradores`. It is a sketch with six placeholder methods (their bodies return null on purpose: they would fail, they are not compiled).
Also needed for the compiled tests to go green: an injectable translation dictionary (see File capture), file writing in the two csv services, and a JDBC-backed
`ConsultarMovimientoCuentaInversionService`.

## Decisions needed from a human

1. Keep the call() no-ops and the listed defects as the modern target, or fix them (the tests currently pin the defects).
2. Confirm that the stored-procedure path of CrearCargoAbono (PLA.UsaCargoAbonoJava = N) stays outside the proof, or provide the SQLScript / a HANA test
   environment to characterise it.
3. Confirm the assumptions that surfaced with the new classes: primary key of CuadraturaDiariaOperaciones; the csv number rendering (16 decimals) is not a rule.
4. HANA vs H2 semantics of a failed batch (CTA-A-116); JDK/CLDR dependency of currency formatting; the UTC pin.
