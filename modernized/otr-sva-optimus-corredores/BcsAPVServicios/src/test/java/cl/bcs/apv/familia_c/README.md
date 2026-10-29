# APV family C (recaudacion, PAC, bonificacion, cargas masivas, distribucion): characterization tests, APV-C-001 .. APV-C-479 (365 cases, 31 legacy classes)

The expected values are RECORDED by executing the unmodified legacy classes (`legacy/otr-sva-optimus-corredores/BSCL/APV/JAVA/BcsAPVServicios/src/cl/bcs/APV/...`) on H2
(`MODE=LEGACY`) with scripted collaborators; nothing is computed by the tests and the legacy source is never edited (one SQL resource is replaced by a dialect-adjusted copy, see
"Assumptions"). BUSINESS_RULES.md has no rule of this module, so the behaviour ids are `APV-C-NNN` (not `RULE-NNN`); every test method name starts with its id (`apvC340_...`) and a
comment above it cites the legacy `file:line` it pins.

## What a test runs

`legacy JSON -> adapter -> REAL use case (cl.bcs.apv.application.recaudacion.*Servicio) on H2 -> adapter -> legacy JSON`, in process, through
`cl.bcs.apv.support.ApvPipelineDeCaso.crear(jdbc, cfg, dic, col, sec, reloj, archivos).invocar("cl.bcs.APV.<Clase>", json)` (CONTRATO.md section 3). Each case compares
(`support/Verifica`, every facet a soft assertion, so a red test lists everything that differs):

1. the output JSON (canonical: key order free, nulls dropped, numbers by value) or, when the legacy threw, the exception SIMPLE CLASS NAME (the message is recorded for humans only);
2. the table dumps the case declares (`"tablas"`), taken after the transaction ends (commit on return, rollback on throw: the production handler model);
3. the collaborator trace: every call to a service OUTSIDE the APV module (service name + the JSON the legacy handed it, stored procedures as `StoredProcedure {procedimiento, entrada}`), in order;
4. the sequences requested (legacy `Utils.getSecuencia(name, n)` is recorded as `name#n` and expanded to `n` requests of `name`).

Collaborators that are OTHER APV classes (`ValidaContratoSAAPV`, `TraspasoCuentasBancarias`, `CambiarEstadoRecaudacion`, `CrearRecaudacionDeposito/Cliente`, `CargaMasivaArchivo`, `GrabarContratoSAAPV`,
`ConsultarCustodiaPortafolioAPV`, ...) run FOR REAL in the oracle and are typed use cases in the modern code; they are never scripted. Collaborators outside APV (Tesoreria, Cuenta Inversion, Fondo Mutuo,
Custodia, Ordenes, Plataforma, Socio de Negocio, stored procedures) are scripted doubles answering what `case.json "colaboradores"` says, on BOTH sides (oracle fake and `support/ColaboradoresDeCaso`).

## Layout

- Cases: `src/test/resources/apv-c/cases/APV-C-NNN_<slug>/{case.json, seed.sql, expected.json}` (directory names stay under 70 characters: Windows MAX_PATH). `case.json` carries the id, the title and
  the legacy `file:line` pinned.
- Oracle: `src/test/resources/apv-c/legacy-oracle/{record.sh, mutation-check.sh, runner/OracleRunner.java, shims/, resources-override/, tools/}`; schema `schema-apv-c.sql` (GENERATED), `seed/{base.sql,config.json}`,
  `h2-dialect/` and `hana-sql/` (no override needed on the modern side so far).
- Case generators (the INPUTS of the cases, never the expected values): `legacy-oracle/tools/mkcases.py` loads every `tools/casegen/c_*.py`; `tools/mkschema_c.py` generates the schema from the legacy SQL and the seeds;
  `tools/mktests.py` generates one `<Clase>Test.java` per legacy class from the case directories.
- Tests: this package, one class per legacy class (`<Clase>Test`, GENERATED: do not edit, add a case and regenerate), plus `FamiliaCSelfCheckTest`. Support: `support/` (Caso, Verifica, Equivalencia, Canon, Dump, H2Hana, doubles).
- Dual-execution hook: `-Dequivalence.out=<dir>` through `cl.bcs.apv.support.VolcadoEquivalencia` (reused unchanged).

## Run

    JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot" \
    mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :bcs-apv-api,:bcs-apv-javacall-adapter,:apv-service test \
        -Dtest='cl.bcs.apv.familia_c.*Test' -Dsurefire.failIfNoSpecifiedTests=false

(`-pl :apv-service` alone cannot resolve its two sibling modules offline, so they are listed too.) Each class prints `equivalence cases executed (<Clase>): N` and asserts `N > 0`. A missing
case, golden or oracle FAILS (never skips). The JVM zone is pinned to UTC (`ZonaUtc`), as the recording is.

## Re-record, add a case, mutation check

1. Add the case in `legacy-oracle/tools/casegen/c_<area>.py` (`caso(num, 'Clase', 'slug', 'what it pins', L('Clase.java', 'regex of the legacy line'), entrada, seed(...), secuencias=, config=, colaboradores=, tablas=,
   enmascarar=)`; `L()` resolves the `file:line` citation from the legacy source at generation time and aborts when the regex matches nothing). `colaboradores.<Servicio>` is `{"salida": {...}}` (every call),
   `{"salidas": [{...}, ...]}` (k-th call, the last repeats) or `{"lanza": "text"}`; the script must list every collection field the legacy class serialises (the oracle refuses an incomplete script and
   prints `INCOMPLETE CASE`; scalar fields the legacy services always fill, such as `P_OUT_ERRORESCONTADOR`, must be listed too, or the legacy sees a null). Stored procedures:
   `"StoredProcedure": {"<procedure name>": {"salida": {...}}}` with the OUT parameters by name. `AgregarLog` and `CrearEventoAutorizacionMasivo` answer "nothing to report" when not scripted.
   `"enmascarar": ["Key"]` writes date text of that key as `<TS>`; `"*Key"` masks ANY primitive value of that key (used for the random `IdTrabajo` of `AgregarLog` callers).
2. Record: `legacy-oracle/record.sh APV-C-NNN` (JDK 25, jars in `~/.m2`; `LEGACY_BSCL` / `LEGACY_APV_SRC` point at another legacy copy, `ORACLE_WORK` at a SHORT scratch path, `ORACLE_REUSE_CLASSES=1` skips the
   compilation when only cases changed; a prefix like `APV-C-3` matches 3 and 300-399). Regenerate first: `python tools/mkcases.py <apv-c dir>` then `python tools/mkschema_c.py <workspace> <apv-c>/schema-apv-c.sql <apv-c>/cases`,
   then `python tools/mktests.py <apv-c dir> <this package dir>`. Three full re-recordings of the 365 cases are byte-identical (checked; the schema generator sorts what it scans so the column order of full-table dumps is stable).
3. Review the golden by hand (`expected.json`), run the suite.
4. `legacy-oracle/mutation-check.sh <file under cl/bcs/APV> <line> <text> <replacement> [casePrefix]` copies the legacy sources and the resources to a scratch directory, applies the one-line mutation
   there, re-records and lists the goldens that changed (nothing in legacy/ or in the module is touched).

## Mapping legacy -> use case -> test class -> cases

Use cases are in `cl.bcs.apv.application.recaudacion`. `ValidarBancoPrevired` and `ValidarMandatosPAC` have no TODO stub: their `call` returns null in the legacy and the adapter answers null.

| Legacy `cl.bcs.APV.` | Use case | Test class | Cases |
|---|---|---|---|
| ProcesarDistribucion | ProcesarDistribucionServicio | ProcesarDistribucionTest | 1-4, 10-14, 20-45 (35) |
| ProcesarCargaMasivaRecaudacion | ProcesarCargaMasivaRecaudacionServicio | ProcesarCargaMasivaRecaudacionTest | 50-69 (20) |
| CrearRecaudacionDeposito | CrearRecaudacionDepositoServicio | CrearRecaudacionDepositoTest | 70-89 (20) |
| ConciliarDeposito | ConciliarDepositoServicio | ConciliarDepositoTest | 90-107 (18) |
| CargaMasivaPrevired | CargarArchivoPreviredServicio | CargaMasivaPreviredTest | 110-131 (22) |
| CargaMasivaRecaudacion | CargarMasivaRecaudacionServicio | CargaMasivaRecaudacionTest | 140-148 (9) |
| CargaMasivaPAC | CargarPACServicio | CargaMasivaPACTest | 160-173 (14) |
| CargaMasivaBanco | CargarArchivoBancoServicio | CargaMasivaBancoTest | 180-197 (18) |
| CargaMasivaSAAPVPAC | CargarContratosPACServicio | CargaMasivaSAAPVPACTest | 200-209 (10) |
| CargaRecaudacion | CargarRecaudacionServicio | CargaRecaudacionTest | 210-216 (7) |
| CrearRecaudacionCliente | CrearRecaudacionClienteServicio | CrearRecaudacionClienteTest | 220-228 (9) |
| CrearRecaudacionEmpleador | CrearRecaudacionEmpleadorServicio | CrearRecaudacionEmpleadorTest | 230-236 (7) |
| DevolucionPagoRezagos | DevolverPagoRezagosServicio | DevolucionPagoRezagosTest | 240-254 (15) |
| DevolucionBF_EntidadRecaudadora | DevolverBonificacionEntidadRecaudadoraServicio | DevolucionBFEntidadRecaudadoraTest | 260-269 (10) |
| CargaBonificacionTGR | CargarBonificacionTGRServicio | CargaBonificacionTGRTest | 270-285, 287 (17) |
| EjecutarBonificacion | EjecutarBonificacionServicio | EjecutarBonificacionTest | 300-329 (30) |
| ProcesarBonificacion | ProcesarBonificacionServicio | ProcesarBonificacionTest | 340-349 (10) |
| ModificarBonificacion | ModificarBonificacionServicio | ModificarBonificacionTest | 350-355 (6) |
| ReplicaPortafolioAPV | ReplicarPortafolioServicio | ReplicaPortafolioAPVTest | 360-377 (18) |
| CrearBancoPrevired | CrearBancoPreviredServicio | CrearBancoPreviredTest | 380-389 (10) |
| ModificarBancoPrevired | ModificarBancoPreviredServicio | ModificarBancoPreviredTest | 390-396 (7) |
| EliminarBancoPrevired | EliminarBancoPreviredServicio | EliminarBancoPreviredTest | 400-405 (6) |
| ValidarBancoPrevired | (adapter answers null) | ValidarBancoPreviredTest | 410 (1) |
| ValidarMandatosPAC | (adapter answers null) | ValidarMandatosPACTest | 411 (1) |
| ValidarMandatosPAC.logica (TYPED entry point, in-process only; the Tesoreria job calls it) | ValidarMandatosPACLogicaServicio (`cl.bcs.apv.application.recaudacion`, typed port `cl.bcs.apv.api.logica.ValidarMandatosPACLogica`) | ValidarMandatosPACLogicaTest | 480-509 (30) |
| CambiarEstadoRecaudacion | CambiarEstadoRecaudacionServicio | CambiarEstadoRecaudacionTest | 420-429 (10) |
| CrearAnotacionRecaudacion | CrearAnotacionRecaudacionServicio | CrearAnotacionRecaudacionTest | 430-433 (4) |
| ModificarAnotacionRecaudacion | ModificarAnotacionRecaudacionServicio | ModificarAnotacionRecaudacionTest | 440-443 (4) |
| EliminarRegistrosRecaudacion | EliminarRegistrosRecaudacionServicio | EliminarRegistrosRecaudacionTest | 450-458 (9) |
| AsignarDepositoEmpleador | AsignarDepositoEmpleadorServicio | AsignarDepositoEmpleadorTest | 460-467 (8) |
| ConsultarTotalesDistribucionRecaudacion | ConsultarTotalesDistribucionServicio | ConsultarTotalesDistribucionRecaudacionTest | 470-473 (4) |
| ConsultarTotalesOrigenPorDeposito | ConsultarTotalesOrigenPorDepositoServicio | ConsultarTotalesOrigenPorDepositoTest | 474-479 (6) |

Ids are unique, not consecutive (blocks were reserved per class while the suite grew).

## Status against the current src/main

Every use case except the two `null` ones is still the generated stub (`UnsupportedOperationException("TODO Step 2: <Clase>")`), so the suite is RED by design: that red list is the Step 2 fix list.
Counts: 365 equivalence tests (363 red, 2 green) + 13 self-check tests (green) = 378 tests. Red by cause: 296 "legacy answered, the stub threw UnsupportedOperationException (TODO Step 2)"; 67 "legacy threw an
exception, the stub threw UnsupportedOperationException (type differs)" (these become green only when the modern code throws the same exception CLASS as the legacy). The 2 green ones (410, 411) pin that the
legacy `call` of `ValidarBancoPrevired` / `ValidarMandatosPAC` returns null and writes nothing; `FamiliaCSelfCheckTest` (inventory + "the comparator can fail" + "every case that does not write accepts its own golden")
is green.

### ValidarMandatosPAC.logica (typed entry point, cases APV-C-480..509)

The legacy `ValidarMandatosPAC` has two entry points. `call(JsonObject)` returns null (APV-C-411). The typed `public void logica(Connection, SalidaRecaudacionTraspaso, Long IdEmpresa, Long IdUsuario)` is what the Tesoreria job
`BcsJobProcesarDistribucion` calls with its own connection: it UPDATEs `BSCL.Tesoreria.BD::PAC.LogMandatoPAC` (the SQL file is named `..._INSERT_...` but it is an UPDATE by IdMandatoPAC + FechaSistema: rows that do not exist are not created).
Cases carry `"metodo": "logica"` and `entrada = {"entrada": {typed fields}, "idEmpresa", "idUsuario"}`; the oracle decodes `entrada.entrada` with the legacy Gson and calls `logica`; the modern side runs the in-process operation
`cl.bcs.APV.ValidarMandatosPAC.logica` (`Caso.operacion()`, `Equivalencia`). The golden has `salida: null` (void) or the exception class, plus the LogMandatoPAC dump (`FechaProceso` is CURRENT_TIMESTAMP: dumped as NULA / SIN_CAMBIO / ACTUALIZADA).
Pinned: PRO/PEN branches, rejected-wins, PRO batch before PEN, ignored PROCESADAS list, UPDATE without a previous row, other FechaSistema, duplicate rows, null/absent EstadoRendicion, user/company variations, config errors (absent / invalid date / with time), NPE paths (null lists, null element, null ids, null user), JsonSyntaxException, SQLException.
Generated class `ValidarMandatosPACLogicaTest`; the transaction behaviour (joins the caller, rolled back with it) is in `cl.bcs.apv.application.ValidarMandatosPACLogicaTransaccionTest`.

## Legacy defects for SME review (kept and pinned as legacy behaviour)

In decreasing weight. Each is reachable from a recorded case.

- **EjecutarBonificacion, renta variable**: the order built for an RV custody never gets `IdInterno`, so when the quantity calculation answers an order the match loop throws NullPointerException; the inner catch answers
  `ERROR_CREAR_CLIENTE_APV` and the bonificacion is never set to EJE (301). The RV path can only complete when the calculation answers no order (307, 316, 317, 329). The deposit, the client detail and the cargo/abono
  already written stay (partial result).
- **EjecutarBonificacion, errors swallowed**: CrearPortafolio errors are read but never reported (303); the cargo/abono errors are added to the counter but the translated message is built and never added (318; the
  counter is anyway recomputed from the messages by `call`); exceptions of the order service and of the FFMM service are swallowed (315, 317); every failure of the second stage is named `ERROR_CREAR_CLIENTE_APV`
  ("Error al crear cliente"), of the first `ERROR_CREAR_RECAUDACION_APV`. The LEFT JOIN to `PLA.ConfiguracionAPV (APV.ToleranciaReajuste)` multiplies the bonificacion when the parameter has two rows (324). The
  percentage of each custody is an integer (`BigDecimal.divide` with the scale of the dividend: HALF_DOWN for funds, HALF_UP for shares: 12.5% -> 12 for a fund, 87.5% -> 88 for a share, 307). The USD custodies are dropped
  before distributing (305). Also, the regimen check (`FMM.RegimenSerieDeFondo`, `BF`) is applied to shares too, so a share whose series has no BF row is skipped silently (306).
- **ReplicaPortafolioAPV**: the PREFERENCIASDEPAGO insert targets `"BSCLPortafolioBD::PORPreferenciaPagoPortafolio"` (dots missing; its sequence name too), so every replication of a preference ends in SQLException and
  rollback even when there is nothing to copy (372-373); the INDICADORES delete filters `IND.Indicador` by a column the table does not have (`IdPortafolio`; the entity column is `IdEntidad`) and its select requires
  `SolicitudCambio.IdEntidadOrigen` to be both the text `INDICADOR` and an integer id, so indicator replication cannot work (374); the `PorDefecto == "S"` String comparison is by reference and the per-portfolio lists
  accumulate (code, not reachable because of the above); `UPDATE ... PP.IdTipoOperacion = :IdPortafolio` binds the portfolio id to the operation type. AUTORIZACION/CUENTACUSTODIO work (365-370). Note 372-374 pin
  SQLException on H2; the assumption is that HANA fails the same way (missing object / missing column).
- **CargaBonificacionTGR**: `fila.TipoDeRegistro == "3"` compares Strings by reference, so `TipoRegistro` is ALWAYS `ACTUAL` (271-272); the "already ingested" and DJ checks use the year pair of the FIRST row of each batch
  of 1000 (285); the same RUT twice in a file is inserted twice (276); the date parser is lenient (`15-03-2025` becomes year 0014, 287); `con.commit()` runs at the end and rows are batched, a missing DJ is only an INFORMACION
  (275); DV lookup is case sensitive in SQL although the Java sets are lower-cased (278).
- **ProcesarBonificacion**: net savings `AhorrosDJ - RetirosDJ` = 0 divides by zero (ArithmeticException, 347); `MontoCalculado` can be negative when the withdrawals exceed the reajustado amount (348); amounts use
  DECIMAL128 then DECIMAL(26,8) storage rounding (342, 348).
- **DevolucionBF_EntidadRecaudadora**: Tesoreria errors of the traspaso, the egreso and the comprobante are never checked, the output is always empty (265); a `con.commit()` runs between the traspaso and the rest (269);
  with no bonificacion found the zero amount is still sent to Tesoreria (264, 267, 268); a series other than fund A leaves a NULL sum and ends in NullPointerException (266).
- **DevolucionPagoRezagos**: `p_DevolucionIdBanco` is used as the `IdCuentaBancaria` of the persona (248); the zero-amount check increments the counter and `call` recomputes it from the messages (243-245).
- **CargaMasivaSAAPVPAC**: an existing contract answers ADVERTENCIA `FOLIO_CONTRATO_EXISTE` but also increments the error counter (recomputed by `call`, 202); `IdMandatoPAC` of the LAST mandate result is written to ALL
  results; the bank-code check cuts the loop with `break` (207); a contract without `IdInterno` throws NullPointerException after adding a message that is then lost (203).
- **CargaRecaudacion / CrearRecaudacionCliente / CrearRecaudacionEmpleador / AsignarDepositoEmpleador / ModificarBonificacion / ConsultarTotalesOrigenPorDeposito**: one `MensajeSalida` instance is shared by every list
  entry, so a batch shows the LAST row's result in every entry; the message type is `APV`, so the error counters stay 0; `CargaRecaudacion` never copies the deposit messages (the `P_OUT_ERRORESCONTADOR == null` test is never true)
  and swallowed exceptions become message id 5.
- **ProcesarCargaMasivaRecaudacion**: the grouping key is `FolioCartola` + `FolioLineaCartola` as text (collides `1`+`23` vs `12`+`3`); the employer step walks all rows (NPE for non CRT rows with another key); the order of
  `HashMap` iteration decides the order of multi-deposit cases (the modern code must use a HashMap with the same keys to reproduce it); duplicate keys in `toMap` throw; the PRE counter is always 0.
- **ConciliarDeposito**: amounts are compared as `long` (truncation); in masivo mode the logic forces the counter to 0 (so the log is written) but `call` recomputes it from the messages; the log `IdTrabajo` is random (masked).
- **CargaMasivaPAC / CargaMasivaBanco / CargaMasivaPrevired / ProcesarDistribucion**: `con.commit()` / `con.rollback()` inside the legacy (partial commits: CargaMasivaBanco swallows the exception after the rollback and still writes
  the CargaMasiva row; CargaMasivaPAC commits the client rows before failing on `p_Archivo.get(0)`, 173); the `IdRecaudacion` sequence block is requested per iteration, giving duplicate ids; `!= BigDecimal.valueOf(0)` in
  CargaMasivaPrevired works only because of the BigDecimal cache; CargaMasivaBanco: an invalid file name message does not increment the counter and every row (even with `MENSAJEVALIDACION != OK`) becomes a deposit.
- **ProcesarDistribucion**: a non-fund instrument is distributed only when `p_DistribucionOrigen` is `S` (the restricted-fund filter is NULL for shares, 45); the "RV" observation carries an empty nemotecnico
  (`RV: : <text>`, 41-44).
- **ValidarBancoPrevired**: the repeated-code message is built with an empty value map; null `IdInterno`/`Codigo` throw (groupingBy); `call` returns null (410), as `ValidarMandatosPAC` (411).
- **EliminarRegistrosRecaudacion**: client/employer messages only when the batch count is 1; the employer is not reset when no client was deleted.
- **CrearAnotacionRecaudacion / ModificarAnotacionRecaudacion**: `IdMotivo` takes `p_IdAmbito`; the REFID of the modification is a new sequence value; the annotation dates are wall-clock (masked).
- Null/short inputs end in raw NullPointerException / IndexOutOfBounds / StringIndexOutOfBounds in most classes (pinned by exception class name only; the JDK "helpful NPE" text is masked).
- Not a defect but a rule for the modern code: the legacy calls `con.commit()` / `con.rollback()` itself in several classes (see above); the modern implementation must commit/rollback at the same points on the connection
  it is given (decision 1).

## Not covered (NOT PROVEN) and why

All 31 legacy classes have cases. Not covered inside them:

- `CargaBonificacionTGR` batches of 1000 rows (the second batch and the "year of the first row" rule across batches): a 1001-row case needs a golden of about 1 MB of messages; only one batch is pinned.
- `ReplicaPortafolioAPV` replication of INDICADORES and PREFERENCIASDEPAGO with data: both end in SQLException in the legacy (see defects), so only the failure is pinned.
- `EjecutarBonificacion` RV success with an order answered by the calculation: unreachable in the legacy (NullPointerException); cases pin the failure.
- `CargaMasivaSAAPVPAC`: creation of persons/telephones/addresses inside `GrabarContratoSAAPV` (family A's classes; here only the new-contract path with an existing person); `IngresarMandatoPAC` and
  `CrearPersonaCuentaBancaria` are scripted.
- `ProcesarCargaMasivaRecaudacion` ordering with more than two deposits depending on `HashMap` iteration.
- `ValidarBancoPrevired` / `ValidarMandatosPAC` only have the `call -> null` case: their `logica` is not reachable through `call`, so it is not an observable contract of the JavaCall.
- Stored procedures and HANA views run only as scripted collaborators / tables seeded by the cases (the Java side is characterised, not the procedure).
- Concurrency, performance and real HANA behaviour (decimal rounding of the engine, `CURRENT_UTCTIMESTAMP`) are not characterised.

## Assumptions (documented)

- The HANA DDL is not in the repository: `schema-apv-c.sql` is INFERRED from the legacy SQL, the POJOs and the seeds by `tools/mkschema_c.py` (columns read or written; types by name heuristic plus an explicit OVERRIDES table:
  money DECIMAL(26,8), ids BIGINT, codes NVARCHAR; `AnoAhorro/AnoProceso/FolioEgreso` BIGINT; `INV.Bonificacion.FechaGeneracion/FechaCalculado` TIMESTAMP). Views are tables the seeds fill; tables have no keys. A column the legacy SQL
  names but the table cannot have (for example `IND.Indicador.IdPortafolio`) is excluded on purpose so that the failure reproduces.
- ONE legacy SQL resource is replaced in the oracle: `legacy-oracle/resources-override/cl/bcs/APV/sql/ConsultarBonificacionAEjecutar.sql` (used by `EjecutarBonificacion` only): the inline view
  `SELECT * FROM ConsultarPortafolio JOIN MatrizAgrupadorAPV JOIN RegimenTributario` is replaced by the two columns the outer query uses, because H2 rejects the duplicate column names ("Duplicate column name") that HANA tolerates. Same rows,
  same semantics; the file is first on the oracle classpath and carries a header saying so. The modern code does not read the legacy SQL (it has its own repository).
- Clock: the legacy takes dates from `new Date()` / `CURRENT_UTCTIMESTAMP` (annotations, `EstadoTransicion.FechaTransicion`, `AgregarLog`): those values are masked as `<TS>` (`"enmascarar"` per table column, per
  output/trace key) on BOTH sides; the `H2Hana` rewrite of `CURRENT_(UTC)TIMESTAMP` uses the fixed clock 2025-04-01T10:15:30Z. `PLA.FechaSistema` is the fixed `2025-04-01` of `seed/config.json`. Goldens carry no run-day
  literal (checked by `FamiliaCSelfCheckTest`; the recording day was 2026-10-2x and none appears).
- `PLA.MonedaFormato` is seeded by `seed/base.sql` (CLP/USD/UF) because the legacy `MonedaFormato` caches the table in a static field for the JVM lifetime.
- Identity hash codes (`@6d3af739`) and the JDK helpful NullPointerException text are normalised (`Dump.sinHash`) on both sides. The message of an H2 / JDK exception is NOT pinned (only the simple class name is compared).
- Sequences: the oracle `Utils.getSecuencia` returns `firstValue, firstValue+1, ...` per name from `case.json "secuencias"` (default 1); a `"name".NEXTVAL` INSIDE a statement uses the real H2 sequence of the schema
  (started at a fixed value per sequence, e.g. 8100 for the bonificacion id): the two counters are independent, as in production.
- `MensajeSalida.tipo` of a scripted collaborator answer is interned (`Hooks.internarTipos`): the legacy counts errors with `tipo == "ERROR"`, which only works for the literal strings the real in-process services create.
  Translated messages are rendered by the Diccionario double as `[CODE]{key=value}` (or `CODE sin traduccion` when the legacy passes no values), on both sides.
- No collaborator of family C writes a file; `AlmacenArchivosDeCaso` fails the case if the modern code uses the port.
- The assumption that H2 and HANA fail alike for a missing table/column (ReplicaPortafolioAPV 372-374) cannot be verified here.

## Decisions needed from a human

1. Transaction model: several legacy classes call `con.commit()` / `con.rollback()` in the middle of the work (CargaMasivaPAC, CargaMasivaBanco, CargaBonificacionTGR, DevolucionBF_EntidadRecaudadora, EjecutarBonificacion,
   ProcesarDistribucion). The goldens pin the PARTIAL commits. Keep them (the modern use case commits on the same connection at the same points) or move to one transaction per call?
2. The exception contract: the golden pins the exception CLASS NAME (`SQLException`, `NullPointerException`, `IndexOutOfBoundsException`, `ArithmeticException`...). A typed Spring implementation throws other types: translate
   them to the legacy names or relax the comparison for those 67 cases?
3. Fix or keep the defects above (especially EjecutarBonificacion RV path, ReplicaPortafolioAPV indicators/preferences, CargaBonificacionTGR `TipoRegistro`, DevolucionBF ignoring Tesoreria errors). The tests pin the legacy behaviour; any fix changes
   goldens deliberately.
4. `HashMap` iteration order (ProcesarCargaMasivaRecaudacion) and reference comparisons (`==` on Strings/BigDecimal): port the value semantics the code was meant to have, or the cache/ordering-dependent behaviour recorded here?
5. Rows that depend on the real clock (`FechaTransicion`, annotation dates): confirm the modern code may take them from the `Reloj` port (the masks make the comparison independent of the value).

## Requests / seams

- None blocking: the pipeline (`ApvPipelineDeCaso`) already offers the ports the cases need. Requests for Step 2: (a) the modern code must issue `"seq".NEXTVAL` inside its own SQL only through sequences that exist in
  `schema-apv-c.sql` (the schema file is the shared contract of oracle and modern fixture); (b) stored procedures must go through `Colaboradores` with the name `StoredProcedure` and `{procedimiento, entrada}`;
  (c) the two `null` adapters (`ValidarBancoPrevired`, `ValidarMandatosPAC`) must stay `null`.

## Mutation evidence

Each mutation changes ONE legacy line in a scratch copy, re-records ALL 365 cases and counts goldens that change (a golden that changes = the suite pins that line).

| # | Legacy line | Mutation | Goldens changed |
|---|---|---|---|
| M1 | ProcesarBonificacion.java:206 | `subtract(montoARestar` to `add(montoARestar` | 4 (342, 343, 344, 348) |
| M2 | EjecutarBonificacion.java:424 | fund percentage `HALF_DOWN` to `HALF_UP` | 1 (307) |
| M3 | CargaBonificacionTGR.java:318 | `fila.TipoDeRegistro == "3"` to `"3".equals(fila.TipoDeRegistro)` | 7 (271, 272, 275, 276, 277, 285, 287) |
| M4 | ProcesarDistribucion.java:866 | `compareTo(suma) == 1` to `== -1` (remainder added to the last fund) | 1 (22) |
| M5 | DevolucionPagoRezagos.java:279 | concept `"DEVRZG"` to `"DEVRZX"` | 6 (240, 241, 242, 250, 251, 252) |
| M6 | ReplicaPortafolioAPV.java:392 | AUTORIZACION operation `"I"` to `"X"` | 3 (365, 367, 368) |
| M7 | ConciliarDeposito.java:312 | client APV total `+` to `-` | 1 (91) |
| M8 | AsignarDepositoEmpleador.java:111 | `<=` to `<` (limit of the deposit) | 1 (466) |
| M9 | CargaBonificacionTGR.java:227 | `batchSize = 1000` to `1` | 2 (276, 285): the 1000-row boundary itself is NOT pinned, only the per-batch year rule |
| M10 | CargaMasivaBanco.java:289 | `PR.IdEstado = "ING"` to `"XXX"` | 6 (180, 186, 188, 194, 195, 196) |

Every mutation changed at least one golden, so no case had to be added for lack of sensitivity. The mutations also show which cases are the sole witness of a line (M2, M4, M7, M8: one case each).
