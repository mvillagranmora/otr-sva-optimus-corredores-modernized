# BcsCuentaInversionServicios - characterization tests, family B (reporting, commission, aging, indices)

Every expected value in these tests was **recorded by executing the unmodified legacy class**
(`legacy/otr-sva-optimus-corredores/BSCL/CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/*.java`)
on an H2 database (MODE=LEGACY) seeded by the case. No test computes its own expectation. Behaviour ids are `CTA-B-NN`
(local to this family: `BUSINESS_RULES.md` has only RULE-076 for the module and it is not relevant here; nothing marked `wrong`
in RULE_REVIEWS.json is pinned). Each test method name starts with its id (`ctaB93_...`) and carries a comment with the legacy
`file:line` and the legacy behaviour.

```
equivalence cases executed: 92 compiled golden cases (each prints "equivalence cases executed (<Class>): n/m")
                            + 121 harness replay cases (HarnessMecanicaTest)
recorded: 225 cases = 92 compiled + 133 pending (src/test-pending/reportes, not compiled, not counted as executed)
          all 28 legacy classes of family B have recorded cases (the last three were added in the second batch, ids 182-225)
```

## Layout

| Path | What |
|---|---|
| `src/test/java/cl/bcs/cuentainversion/reportes/*ServiceTest.java` | compiled golden tests, one class per legacy class |
| `.../reportes/ReportesGoldenInventoryTest.java` | every case dir has `case.json` + `expected.json` and is claimed by exactly one test class (compiled or pending) |
| `.../reportes/HarnessMecanicaTest.java` | proves the comparison can fail (replay of the recorded output passes, one altered field fails) |
| `.../reportes/support/` | `CasoReporte` (loads a case, builds H2, runs the modern service, compares), `H2Dialect`/`H2Functions` (HANA SQL -> H2, shared with the oracle), `Dump`, `Mask`, `JsonCanon` |
| `src/test/resources/reportes/cases/CTA-B-NN_slug/` | `case.json` (input, seeds, collaborator answers), optional `seed.sql`, `expected.json` (recorded) |
| `src/test/resources/reportes/schema-reportes.sql`, `seed/` | HANA tables modelled as H2 tables and the shared seeds |
| `src/test/resources/reportes/legacy-oracle/` | shims, `runner/OracleRunner.java`, `record.sh`, `reencode.py`, `classes.txt` (source only, NOT compiled by maven) |
| `src/test-pending/reportes/` | pending tests (not compiled) + `support/` (intended collaborator API, fake, file dump) |

## Legacy class -> modern class -> test class

Modern classes are in `cl.bcs.cuentainversion.service`. Cases = number of recorded cases (ids).

| Legacy class | Modern class | Test class | Cases | Ids |
|---|---|---|---|---|
| CrearCobroComisionOpcionesCobro | ...Service | CrearCobroComisionOpcionesCobroServiceTest | 7 | 01-07 |
| EditarCobroComisionOpcionesCobro | ...Service | EditarCobroComisionOpcionesCobroServiceTest | 5 | 08-12 |
| EliminarCobroComisionOpcionesCobro | ...Service | EliminarCobroComisionOpcionesCobroServiceTest | 3 | 13-15 |
| CrearCobroComisionExclusionConcepto | ...Service | CrearCobroComisionExclusionConceptoServiceTest | 7 | 16-21, 46 |
| EliminarCobroComisionExclusionConcepto | ...Service | EliminarCobroComisionExclusionConceptoServiceTest | 4 | 22-25 |
| ConsultarCobroComisionRegistros | ...Service | ConsultarCobroComisionRegistrosServiceTest | 6 | 26-31 |
| EditarCobroComisionRegistros | ...Service | EditarCobroComisionRegistrosServiceTest | 9 | 32-39, 45 |
| EliminarCobroComisionRegistros | ...Service | EliminarCobroComisionRegistrosServiceTest | 5 | 40-44 |
| ActualizarSaldosReporteAntiguedad | ...Service | ActualizarSaldosReporteAntiguedadServiceTest | 11 | 47-57 |
| CargaInicialSaldosReporteAntiguedad | ...Service | CargaInicialSaldosReporteAntiguedadServiceTest | 5 | 58-62 |
| GuardarSaldosReporteAntiguedad | ...Service | GuardarSaldosReporteAntiguedadServiceTest | 6 | 63-68 |
| GuardarPatrimonioCajaCalculoComision | ...Service | GuardarPatrimonioCajaCalculoComisionServiceTest | 7 | 117-123 |
| ProcesaIndicesAtributtionAnalisys | ...Service | ProcesaIndicesAtributtionAnalisysServiceTest | 10 | 165-174 |
| IndiceRentabilidadAtributtionAnalysis | IndiceRentabilidadAttributionAnalysisService | IndiceRentabilidadAtributtionAnalysisServiceTest | 7 | 175-181 |
| ConsultaReporteAntiguedadBolsa | ...Service | ConsultaReporteAntiguedadBolsaServicePendingTest (pending) | 14 | 69-82 |
| ConsultaReporteAcreedores | ...Service | ConsultaReporteAcreedoresServicePendingTest (pending) | 10 | 83-92 |
| ConsultaAntiguedadDeudasCuentaInversion | ...Service | ConsultaAntiguedadDeudasCuentaInversionServicePendingTest (pending) | 16 | 93-108 |
| ProcesarCobroComisionPatrimonio | ...Service | ProcesarCobroComisionPatrimonioServicePendingTest (pending) | 8 | 109-116 |
| GuardarPatrimonioCaja | ...Service | GuardarPatrimonioCajaServicePendingTest (pending) | 7 | 124-130 |
| GenerarArchivoSaldoCuentas | ...Service | GenerarArchivoSaldoCuentasServicePendingTest (pending) | 6 | 131-136 |
| GenerarReporteAcreedores | ...Service | GenerarReporteAcreedoresServicePendingTest (pending) | 8 | 137-144 |
| GenerarReporteAntiguedadDeudaCTA | ...Service | GenerarReporteAntiguedadDeudaCTAServicePendingTest (pending) | 6 | 145-150 |
| GenerarReporteAntiguedadDeudaBolsaCTA | ...Service | GenerarReporteAntiguedadDeudaBolsaCTAServicePendingTest (pending) | 6 | 151-156 |
| GenerarInformePatrimonioDiario | ...Service | GenerarInformePatrimonioDiarioServicePendingTest (pending) | 4 | 157-160 |
| GenerarInformeResumenCobro | ...Service | GenerarInformeResumenCobroServicePendingTest (pending) | 4 | 161-164 |
| GenerarArchivoClientesPorPagarYCobrar | ...Service | GenerarArchivoClientesPorPagarYCobrarServicePendingTest (pending) | 20 | 182-200, 224 |
| GenerarArchivoCuentasPorCobrarAnexo16 | ...Service | GenerarArchivoCuentasPorCobrarAnexo16ServicePendingTest (pending) | 13 | 201-212, 225 |
| InformeAtributtionAnalisys | InformeAttributionAnalysisService | InformeAtributtionAnalisysServicePendingTest (pending) | 11 | 213-223 |

Parts of the last three classes that stay uncharacterized, and why:

* PDF of Anexo16 (iText 5): iText is not available offline. The shim (`shims/com/itextpdf/text/**`) records, in order, the texts of every table cell with its alignment/border/colspan/rowspan,
  the table shape (columns, header rows), the line breaks and the page-event header/footer cells (as `pdfTextos`); the page event runs once (a single page is assumed). NOT reproduced: layout, fonts,
  colours, pagination (a report longer than one page would fire the page event several times), image decoding (only the magic number is checked) and the real PDF bytes (the shim writes a stub
  that starts with `%PDF-1.4`, the golden pins "is a pdf", not its content).
* `GenerarArchivoClientesPorPagarYCobrar` runs the real SQL resources on H2; ICU4J: the legacy builds against icu4j 71.1 (`build.gradle`), only icu4j 4.6 is available offline. The class uses only
  `Calendar.getInstance()/setTime/add(DATE, 0)/getTime`, whose behaviour is the same in both versions for the Gregorian calendar (the oracle forces Locale.US), so 4.6 is a faithful stand-in for
  this use; any other ICU use of the platform is not exercised here.
* The collaborators BcsCartola (MovimientosTitulos, MovimientosCaja, DatosCartolaSocioNegocio), EntregarPreciosTasas and ConsultarCarteras (data class only) are NOT executed: their answers are canned
  per case (`colaboradores.movimientosTitulos|movimientosCaja|datosSocioNegocio|preciosTasas`) and every request is recorded under `llamadasColaboradores`. The numerical behaviour of those services
  is therefore not pinned here (the Dates of EntradaCartola are logged the way Gson prints them).
* The H2 vs HANA caveats of the new SQL (see "Assumptions") and the reference string comparison of case 222 (driver dependent) need SME confirmation against HANA.

## How to run

```
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :bcs-cuenta-inversion-servicios test
mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :bcs-cuenta-inversion-servicios test -Dtest='cl.bcs.cuentainversion.reportes.*Test'
```

A test that cannot load its case or fixture FAILS (never skips). No `@Disabled`, no skips anywhere in this package.

## Status against the current src/main (Step 1 baseline)

92 compiled golden tests: **20 green, 72 red**. The red ones are the Step 2 fix list (the modern services are mostly stubs):

| Cause | Tests | Classes / ids |
|---|---|---|
| A. Modern service fails with `InvalidDataAccessApiUsageException` (verified for GuardarSaldos: the SQL file expects `:FechaInicio`, the service binds `p_FechaMovimiento`; the same exception class for the others, not individually diagnosed) | 34 | ActualizarSaldos (11), CargaInicial (5), CrearExclusion (6: 16-21,46 except 20), CalculoComision (6), GuardarSaldos (6) |
| B. Validation / messages not implemented: error flows return no `codigo`, `UI_ERROR_INTERNO` or `UI_PROCESO_COMPLETADO` instead of the legacy dictionary code; counters or table changes differ | 35 | CrearOpcionesCobro (6), CrearExclusion (1, id 20), EditarOpcionesCobro (4), EliminarOpcionesCobro (3), EliminarExclusion (4), EditarRegistros (9), EliminarRegistros (4), ProcesaIndices (4) |
| C. Insert fails with `DataIntegrityViolationException` where legacy accepts the ISO `T..Z` FechaVigencia (id 05) | 1 | CrearOpcionesCobro |
| D. Legacy NullPointerException defect not reproduced (modern returns normally) | 2 | EliminarRegistros (44), ProcesaIndices (174) |
| Green | 20 | ConsultarRegistros (6), IndiceRentabilidad (7), ProcesaIndices (5), EditarOpciones (1), CalculoComision (1) |

Infra tests (inventory 3 + harness 2) are green. The 84 pre-existing tests (`CuentaInversionTest` 60, `ServiceFormulasTest` 24) stay green.
Agent A's `transaccional` tests are not part of this table.

## Mutation check (the tests can fail)

* Modern side (scratch copy of the module, `src/main` untouched): `IndiceRentabilidadAttributionAnalysisService` line 65 `HALF_UP` -> `DOWN`:
  `IndiceRentabilidadAtributtionAnalysisServiceTest` goes from 7 green to exactly 1 red (`ctaB175`). Restored (copy discarded).
* Oracle side: six mutations were applied together to a scratch copy of the legacy classes (Bolsa tramo A end boundary excluded; rentabilidad `HALF_UP` -> `DOWN`;
  percentage `> 1.0` -> `>= 1.0`; Antiguedad AgrupaPortafolios "S" -> "N"; Bolsa CLP `DOWN` -> `HALF_UP`; ProcesarCobro `VIG` -> `ACT`), the cases were re-recorded into a copy of
  the resources and the goldens diffed: 32 of 181 expected.json changed (06, 69, 71, 72, 75, 76, 78, 80, 81, 93-98, 100, 102-104, 106-110, 113-116, 145, 151, 152, 175), including at least one
  case per mutation (06 percentage, 175 rounding, 100 grouping, 109-116 ProcesarCobro, Bolsa 69-81). The per-mutation attribution was read from the case content, not from separate runs.
* Second batch (ids 182-225), same method, four more mutations applied together to a scratch copy of the legacy: Anexo16 `DIAS_VIGENTE` 2 -> 3, ClientesPorPagarYCobrar 1-7 day range `<= 7` -> `<= 6`,
  ClientesPorPagarYCobrar day-0 filter `after` -> `!before`, InformeAtributtionAnalisys contribution rounding `HALF_UP` -> `DOWN`. Re-recorded: the second batch goldens that changed are 188, 201, 203, 204, 207-209, 211-213, 218, 221-225 (every mutation moved at least one golden: 188 day-0 filter, 224 the 1-7 day range, 225 and 201-212 the vigente tranche, 213/218/221-223 the contribution rounding); the per-mutation attribution was read from the case content. The boundary cases 224 and 225 were added because the first draft of the cases did not detect the range mutation.
* Harness: `HarnessMecanicaTest` replays the recorded output (passes on 121 cases) and rejects an altered one.

## Re-recording the goldens

```
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
bash src/test/resources/reportes/legacy-oracle/record.sh build
bash src/test/resources/reportes/legacy-oracle/record.sh run            # all cases (or: run CTA-B-93)
```

`record.sh` copies the legacy sources re-encoded to UTF-8 into `$ORACLE_WORK` (default `$TMPDIR/cta-b-oracle`), compiles them with the shims
and `OracleRunner`, and rewrites each `expected.json`. It never writes under `legacy/` or `src/main`. Needs the jars listed in the script
header (offline `~/.m2`). Re-recording must produce identical files (the oracle is deterministic; verified for 175 and the 17x cases); a diff means the
legacy source changed or the case is not deterministic.

`expected.json` sections: `salida` (JavaCall JSON), `excepcion` (class; message is not compared), `tablas` (only tables whose rows changed),
`llamadasColaboradores`, `auditoria`, `log`, `secuenciasSolicitadas`, `archivos` (generated files), `pdfTextos`.

## Adding a case

1. Create `cases/CTA-B-NNN_slug/case.json`: `id`, `clase` (legacy class), `nota`, `seeds` (fragments under `seed/`), `entrada` (JSON of the legacy Entrada;
   `${OUT}` = output directory), optionally `invocacion {metodo, entrada}` for classes whose `call()` returns null, `colaboradores`
   (`empresa`, `consultarSaldo` object or array in call order, `paridad` "ORIGEN>DESTINO" -> rate, `crearMovimientoComprobante`, `canonicalizarParidad`, and for InformeAtributtionAnalisys `movimientosTitulos`, `movimientosCaja`, `datosSocioNegocio`, `preciosTasas`),
   `directorios`, `sinOrden` (JSON keys whose arrays compare as multisets because the legacy order comes from an unordered SQL). Extra rows: `seed.sql` next to it.
2. `record.sh run CTA-B-NNN`, read `expected.json` and make sure it is what the legacy does, not what you expected.
3. Add the case to the test class (`CASOS` list and a test method named `ctaBNNN_...` with the legacy `file:line` comment and the literal assertions). The inventory test fails until the case is claimed.

## Conventions and masks

* Time zone America/Santiago and Locale.US in the oracle and in the modern run. System date in the seeds: 2025-03-14, country 44, currency CLP.
* Masked: `IdTrabajo` (random) and timestamps equal to today (`<NOW>`), today's date/time inside messages and logs (`<HOY>`, `<AHORA>`), the output path (`${OUT}`).
* JSON comparison ignores key order and null fields, compares numbers by value.
* The transaction model is an assumption: autocommit off, commit when `call()` returns, rollback when it throws. Tables changed before an exception are rolled back.

## Pending tests (src/test-pending/reportes, not compiled)

They need an API that does not exist in `src/main` yet. Intended API (declared in `support/ColaboradoresReportes.java`, mirrored by `support/FakeColaboradores.java`):

```java
interface ColaboradoresReportes {
    JsonObject consultarEmpresa(long idEmpresa);            // throws unchecked "Empresa no existe"
    JsonObject consultarSaldo(JsonObject entrada);          // JSON of Entrada/SalidaConsultarSaldoCuentaInversion
    JsonObject calcularParidad(JsonObject entrada);         // JSON of CalcularParidadEntrada/Salida
    JsonObject crearMovimientoComprobante(JsonObject entrada);
    // InformeAttributionAnalysis only:
    List<JsonObject> movimientosTitulos(JsonObject entradaCartola);   // rows of SalidaCartola.P_OUT_SALIDA["movimientosTitulos"]
    List<JsonObject> movimientosCaja(JsonObject entradaCartola);      // rows of ["movimientosCaja"]
    List<JsonObject> datosSocioNegocio(JsonObject entradaCartola);    // rows of ["datosSocioNegocio"]
    JsonObject entregarPreciosTasas(JsonObject entrada);              // EntregarPreciosTasas_Entrada / _Salida
}
```

* Services with collaborators: `new XService(caso.jdbc(), colab)` (`ProcesarCobroComisionPatrimonioService(caso.dataSource(), colab)`), `execute(JsonObject)`.
* File generators (3 xlsx reports, the 2 informes, GenerarArchivoSaldoCuentas zip/CSV/PDF): constructor as above (informes `new XService(caso.jdbc())`); they must write into the directory given by the
  request path configuration; the test compares file names and contents with `ArchivoDump` (xlsx/xls cells through POI `DataFormatter` with Locale.US, CSV lines, zip entries). `src/main` has no POI dependency today.
* The pending classes compile against stubs of the services (checked in scratch with javac); to activate: add the collaborators/POI, move the files to `src/test/java/cl/bcs/cuentainversion/reportes/` and `support/` to `.../reportes/support/`.
* Each pending test asserts: literals recorded from the legacy, the list of collaborator calls (`llamadasColaboradores`: service names, order and requests), the generated files, then the full golden comparison.

## Assumptions and limits of the oracle

* Second batch additions to the oracle (cases 182-225):
  * H2Dialect (shared by the oracle and by the modern tests) also rewrites `TO_DECIMAL(x,p,s)` -> `CAST(x AS DECIMAL(p,s))`, `MAP(a,b,c,d)` -> `CASE WHEN a = b THEN c ELSE d END` and
    `:p AS "Col"` / `? AS "Col"` in a select list -> `CAST(.. AS VARCHAR(100)) AS "Col"` (H2 cannot type a bare parameter there; HANA can); H2Functions adds `TO_DATE(x, fmt)` and `TO_VARCHAR(x, fmt)`
    (only the `YYYY-MM-DD` format is used). An untyped NULL parameter is bound as a VARCHAR null in the oracle SQL shim and in CasoReporte (same rule on both sides).
  * `schema-reportes.sql` models the views/tables read by the three classes as plain tables (indicator, employee, person name, partner, broker relation, account view, image, custody portfolio `CUS.Cartera`,
    guarantees, funds/series/unit values, operation types, instruments, asset classification). Assumptions to confirm: `GBL.ConsultarSaldoCuentaInversion_Cuenta_Delete` is modelled as a separate table
    (the legacy deletes from it, not from `_Cuenta_Paso`, so the step rows stay; their random IdTrabajo is masked); the extra columns of `CTA.CargoAbonoMovimiento` are added by
    `seed/cargoabono-ext.sql`, which the new cases list FIRST (earlier cases insert into that table positionally).
  * `CalcularParidad` shim: `MontoLocal` is set equal to `MontoDestino` (every caller converts to the system = local currency); Anexo16 reads MontoLocal.
  * Collaborator requests are logged with any non zero `IdTrabajo`/`p_IdTrabajo` masked as `"<random>"` (`Mask.llamada`); table dumps are ordered by their masked content (`Dump`).
  * `Colaboradores.listaMapas`: canned lists become `HashMap`s with BigDecimal numbers (the legacy casts them).
* The real legacy classes run; platform classes outside the module are shimmed (`legacy-oracle/shims`, with the deviations documented in each file): SQL/Utils/JavaCall/Log, `PlataformaUtils` (MensajeSalida verbatim),
  `AgregarLog` (records the audit instead of calling the service), `ConsultarEmpresa`, `CalcularParidad` (rate table), `ConsultarSaldoCuentaInversion` (canned answers), `CrearMovimientoComprobante`, PDFBox (records `showText` strings, widths 0).
  The collaborator shims were generated by extracting the legacy nested classes verbatim.
* HANA SQL runs on H2 through `H2Dialect.translate` (UPSERT -> MERGE KEY, `#Temp` quoting, `CURRENT_UTC*`, bare DECIMAL -> DECIMAL(34,16), TEMPORARY COLUMN TABLE) and `H2Functions` aliases (TO_DATE, TO_NVARCHAR, currentUtc*). The same translation is applied to the modern SQL, so a HANA-only construct that H2 cannot run would fail on both sides.
* `schema-reportes.sql` models the tables by inference from the SQL; assumed constraints: UNIQUE (OpcionCobro, PortafolioAPV, IdClasificacionTipoInstrumento) on CobroComisionOpcionesCobro (legacy catches the violation as a warning, case 10) and a PK on DefinicionIndiceFinanciero (cases 167); confirm against the real HANA DDL.
* Apache POI: only 3.17 is available offline but the legacy uses the 3.16 short constants. The compiled copy of the legacy classes has 4 STYLING constants rewritten (`CellStyle.BORDER_THIN`, `ALIGN_CENTER`, `VERTICAL_CENTER`, `SOLID_FOREGROUND`, see `reencode.py`); no cell value is touched. PDF layout (iText/PDFBox) is not reproduced, only the texts shown.
* Static caches of the legacy (Diccionario, ConfiguracionSistema, MonedaFormato) are reset per case. The dictionary is seeded as `[CODE] k=::k::` so a message text proves which code and parameters the legacy used.
* Secrets: no credential from the legacy appears in any fixture; every connection value is in-memory H2.

## Legacy defects for SME review (kept and pinned; do not fix silently)

New in the second batch (ids 182-225):

* ClientesPorPagarYCobrar: guarantees are never reported: the guarantee SQL filters on `:p_FechaHasta` but only `p_FechaDesde` is bound (NULL -> no row ever matches) (189).
* ClientesPorPagarYCobrar: the RELCORR indicator is an INNER JOIN, a client without it vanishes (187); `Garantias/Simultanea/SaldoRetenido` maps use `Collectors.toMap` on "portfolio-currency": two
  clients on the same portfolio and currency -> IllegalStateException (200); `EsBusqueda` absent -> NullPointerException after all queries (199); the day-0 filter `Calendar.add(DATE, 0)` adds nothing,
  so movements effective on FechaSistema are dropped (188); the age summary adds amounts of different currencies together (195); the clean-up deletes from `_Cuenta_Delete` instead of `_Cuenta_Paso`.
* ClientesPorPagarYCobrar: when the currency conversion of the money market has no rate the unconverted CLP amount stays on the foreign currency account (194).
* Anexo16: the configuration query uses `MAX()` and always answers one row, so `FALTA_CONFIGURACION_RUTA_ARCHIVO` can never fire; with no configuration the zip is written to the working directory
  (cannot be pinned without polluting it; the unwritable-directory case 204 throws NoSuchFileException).
* Anexo16: `imagenes != null || !imagenes.isEmpty()` (always true) -> IndexOutOfBoundsException without logo (205); a logo that is not an image -> IOException (206); rows with balance >= 0 or Relacion CPRO
  are skipped and an EMPTY report is still generated (203); movements of an account whose currency differs between the balance row and the account view -> NullPointerException (210); a foreign
  debt without a rate becomes zero silently (211); an unparseable date becomes 1900-01-01 and everything is "vigente" (212); the page event assumes one page (not a legacy defect of the shim only).
* InformeAtributtionAnalisys: RV row without a price answer -> NullPointerException (214); without a conversion rate -> NullPointerException (219); the catch block builds the error message in a local
  object that is thrown away and rethrows (214, 219); `subMercado == ""` is a reference comparison (222, driver dependent); `System.out.println` of the whole output; the result contains odd values
  (negative profit for a purchase held in the period: 213) that are pinned as they are.
* Original batch defects follow.

1. Crear/Eliminar OpcionesCobro never send the audit (an empty list is passed to `agregarLog`) (cases 01, 13).
2. Eliminar opcion of an unknown id is silent (14). Warnings are not counted as errors.
3. Exclusion create with an ISO `T..Z` date in a `java.sql.Date` field yields a null date -> NullPointerException (46); OpcionesCobro uses `java.util.Date` and accepts it (05).
4. EliminarCobroComisionRegistros with null `p_IdPortafolios` -> NullPointerException (44).
5. ConsultarCobroComisionRegistros adds the edited amount once per branch (custody + cash) (28).
6. ActualizarSaldosReporteAntiguedad with a single balance row plus movements -> IndexOutOfBounds (55). CargaInicial with an empty balance table -> NullPointerException (61).
7. ProcesarCobroComisionPatrimonio: missing IVA row -> IndexOutOfBounds (114); edited rows only go ACT -> PRO, never VIG (115); zero-amount VIG rows are also marked PRO.
8. Bolsa with `SeparaIntermediacion=N` returns an empty table (70).
9. A related client (`Relacionado = "SI"`) is compared against `"S"`, so it is never classified as related (Bolsa 77, Acreedores, Antiguedad 106).
10. Acreedores formats amounts with the system currency even for USD (87); `CodigoPortafolio` never filled; sheet named "cuentas_por_cobrar".
11. Antiguedad: days with only G or only I rows are ignored (97); empty intermediate tranches net older debt into tranche A (98, 99, 103); `Consolidar=S` with several currencies skips the conversion (102).
12. GuardarPatrimonioCaja: a client with only a foreign-currency account is lost (127). CalculoComision: only the previous-day rows of the requested portfolios are reported; with no previous patrimony the result is empty (117, 123).
13. GenerarArchivoSaldoCuentas: messages built with `valores == null` read "<code> sin traduccion"; a `;` inside a portfolio name shifts the CSV columns.
14. The three xlsx reports with a consult error still write a header-only workbook and return its path (144, 150, 156).
15. Informes: an unprotected log write in a missing directory -> FileNotFoundException (160, 164).
16. ProcesaIndices with null `p_Tarea` -> NullPointerException (174).
17. `getNumeroErrores` compares the message type by reference (works only with interned strings).
18. The real `AgregarLog` swallows audit failures. `IdTrabajo` is random and several rows carry `CURRENT_UTCTIMESTAMP` stamps (masked here).

## Status after Step 2 (implementer B)

All 225 recorded cases run as compiled tests (`*ServiceTest`, the 14 former `*ServicePendingTest` were moved; `src/test-pending/reportes` only keeps a placeholder
because the inventory test scans it). Reporting package: 233 tests, 0 red (225 golden cases + inventory 3 + harness 2 + adapter 3).
Test-side changes allowed by rule 2: `CasoReporte.ejecutar` takes a throwing functional interface (the legacy `call` throws checked exceptions: `Exception`, `IOException`),
the case `directorios` are created like the oracle did, the HANA UPSERT translation also accepts `?` (Spring rewrites `:name`), `GrabadorPdf`/`GrabadorTablaPdf` record the PDF cells.
Open points: PDF has a port (`GeneradorPdf`) and NO production adapter (`GeneradorPdfNoDisponible` placeholder); the cartola collaborators are not exposed by BcsNormativaServicios under their legacy names;
`GenerarArchivoSaldoCuentas` swallows PDF/zip failures and answers success (legacy, not in the list above); `IndiceRentabilidad...Service.calcularIndice` (pinned by ServiceFormulasTest) rounds the ratio to 4 decimals, the legacy to 2.
