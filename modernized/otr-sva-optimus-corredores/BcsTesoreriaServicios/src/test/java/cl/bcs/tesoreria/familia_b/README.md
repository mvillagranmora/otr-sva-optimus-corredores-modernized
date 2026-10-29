# Tesoreria family B - characterization tests (TES-B-01 .. TES-B-330)

Expected values are RECORDED by executing the unmodified legacy classes (`legacy/otr-sva-optimus-corredores/BSCL/Tesoreria/...`) on H2
(MODE=LEGACY) with scripted collaborators; nothing is computed by the tests. The legacy source is never edited.

## Layout
- Cases: `src/test/resources/tesoreria-b/cases/TES-B-NN_<name>/{case.json, seed.sql, expected.json}` (expected = salida / excepcion / tablas / archivos /
  colaboradores / secuenciasSolicitadas).
- Oracle: `src/test/resources/tesoreria-b/legacy-oracle/{record.sh, mutation-check.sh, runner/OracleRunner.java, shims/}`; schema `schema-tesoreria-b.sql`, `seed/`, `h2-dialect/`.
- Tests: this package (one class per legacy class, method names start with the behaviour id `tesBNN_`, a comment cites legacy file:line). `FamiliaBSelfCheckTest`
  (inventory: every golden recorded by the oracle, ids unique, every case referenced by a test, no credential-looking text) is excluded from the dual-execution dump.
- Pending (not compiled): `src/test-pending/tesoreria-b/` (its README has the exact signatures of the intended Step 2 ports).
- Dual-execution hook: `-Dequivalence.out=<file>` through `cl.bcs.tesoreria.support.VolcadoEquivalencia` (family "familia_b").
- BUSINESS_RULES.md has no rule for this module, so ids are TES-B-NN, not RULE-NNN.

## Run
    JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot" \
    mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :tesoreria-service test -Dtest='cl.bcs.tesoreria.familia_b.*Test'
Each class prints `equivalence cases executed (X): N` and asserts N>0. A missing golden/oracle FAILS, never skips. JVM zone is pinned to UTC (ZonaUtc).

## Re-record / add a case / mutation check
1. Create `cases/TES-B-NNN_<short name>/` (directory name under ~70 chars: Windows MAX_PATH) with `case.json` (`servicio`, `metodo` call | logica | any public
   two-argument method such as `ValidaPreRendicion`, `entrada`, optional `config`, `colaboradores`, `secuencias`, `tablas`) and `seed.sql`.
   `tablas[]` entries accept `nombre`, `orden`, `columnas`, `excluir`, `expresiones` and `enmascarar` (columns whose date/timestamp text is written as `<TS>`, see below).
2. `legacy-oracle/record.sh TES-B-NNN` (JDK 25 + jars in ~/.m2; prefix `TES-B-` re-records all; beware that a prefix like `TES-B-2` matches 2, 20-29 and 200-299).
   Two full re-recordings produce byte-identical goldens (checked): identity hash codes in exception messages are written as `@<hash>` and the JDK message of a
   NullPointerException raised in a parallel-stream collector is normalised, because both vary between runs.
3. Add the id to the CASOS list and one `tesBNNN_` method in the test class. Review the golden by hand before committing.
4. Legacy-side mutation check: `legacy-oracle/mutation-check.sh <file under cl/bcs/tesoreria> <line> <text> <replacement> [casePrefix]` copies the legacy sources and this folder
   to a scratch directory, applies the one-line mutation there, re-records and lists the goldens that changed (nothing in legacy/ or in the module is touched).

## Mapping legacy -> modern -> cases
| Legacy | Modern (current src/main) | Cases | Test class |
|---|---|---|---|
| ban.ConsultarSaldoCuentaBanco | javacall.ban.ConsultarSaldoCuentaBancoService | 01-08 | compiled |
| ban.ConsultarLibretaBanco | javacall.ban.ConsultarLibretaBancoService | 09-22 | compiled |
| liq.ConsultarClearingFinanciero | javacall.liq.ConsultarClearingFinancieroService | 23-46 | compiled |
| liq.ProcesarClearingFinanciero | javacall.liq.ProcesarClearingFinancieroService | 47-63 | compiled |
| RecaudacionBCP / ConsultaBCP | javacall.RecaudacionBCPService / ConsultaBCPService | 64-88,96 / 89-95 | compiled |
| IngresarMandatoPAC / AnulacionMandatoPAC / RevocacionMandatoPAC | missing services | 97-119 / 135-147 / 148-159 | PENDING |
| IngresarMandatoPACManejador | javacall.IngresarMandatoPACManejadorService | 267-276 | compiled |
| ModificarPAC | javacall.ModificarPACService | 120-134, 330 | compiled |
| car.ProcesarCargaMasiva | javacall.car.ProcesarCargaMasivaService | 160-177 | compiled |
| CargaMasivaIngresoEgresoDAPFFMM | javacall.CargaMasivaIngresoEgresoDAPFFMMService (with CSV golden) | 178-193 | compiled |
| ConsultarFormatoArchivo / ActivarMandatosArchivoCargo | javacall... | 194-198 / 199-203 | compiled |
| ban.ActualizarSaldosCuentaBanco (+Corredora) | javacall... | 204-210 / 211-213 | compiled |
| CrearCargaMasivaPac | javacall.CrearCargaMasivaPacService | 214-220 | compiled |
| nor.AnularNominasTransferencias | javacall.AnularNominasTransferenciasService | 221-232 | compiled |
| nor.QuitarDetalleNominasTransferencias | javacall.QuitarDetalleNominasTransferenciasService | 233-242 | compiled |
| dep.GestionDeDepositos | javacall.dep.GestionDeDepositosService | 243-266 | compiled |
| ProcesarDistribucion.logica | javacall.ProcesarDistribucionService | 277-308, 319-321 | compiled |
| ProcesarDistribucion.ValidaPreRendicion | no counterpart | 309-318 | PENDING |
| CrearCargaMasivaPacRen | javacall.CrearCargaMasivaPacRenService | 322-329 | compiled |

## Status against current src/main
272 equivalence tests compiled: 3 green (ActualizarSaldosCuentaBanco 2, Corredora 1: coincidences of the empty-message path), 269 red; plus 58 pending tests (48 PAC + 10 pre-rendition).
Red causes (the Step 2 fix list): (a) no seam for platform configuration, message dictionary, clock, HANA sequences and collaborator services (RevisarRequisitosBasicos, CrearIngresoEgreso,
LiquidarComprobante, CrearCargoAbono, ...) so message text and generated ids differ; (b) behaviours absent or different in the modern services (several modern services read other input keys,
e.g. CrearCargaMasivaPacRen expects `p_RegistrosRendicion`); (c) file-generating classes have no output-directory seam.

## Intended ports
Accepted by the user: `Colaboradores.invocar(String servicio, JsonObject entrada)` as the last constructor argument, `DiccionarioMensajes`, `ConfiguracionSistema`, `Secuencias`, `Reloj`, and a
file-output seam (`AlmacenArchivos`). Exact signatures: `src/test-pending/tesoreria-b/README.md` and `src/test-pending/tesoreria-b/ports/`.

## Determinism decisions (ProcesarDistribucion)
- The request carries no data (`p_Recaudacion` is ignored): everything is read from CAR.CargaMasivaDetallePAC, PAC.MandatoPAC, PAC.LogMandatoPAC and the contracts. The oracle records the typed `logica`
  (the production `call` serialises with a plain Gson whose date format depends on the platform locale).
- `PAC.LogMandatoPAC.FechaProceso` is written by the database (`CURRENT_TIMESTAMP`) and read back with `= CURRENT_DATE`: it is stored as DATE (assumption, see below) and masked in the canonical dump with ONE regex,
  `Dump.TIMESTAMP` = `\d{4}-\d{2}-\d{2}([ T]\d{2}:\d{2}:\d{2}(\.\d+)?)?`, through `"enmascarar": ["FechaProceso"]` -> `<TS>` (null stays null).
- HashMap-order dependent output: the legacy groups the requests by IdentServicio in a HashMap. The four result lists `P_OUT_MENSAJES`, `P_OUT_SOLICITUDES_PROCESADAS`,
  `P_OUT_SOLICITUDES_NO_PROCESADAS` and `P_OUT_SOLICITUDES_A_PROCESAR` are compared as sets (`sinOrden`). What the set comparison cannot absorb: `IdTraspaso` (500, 501, ... assigned in iteration order) and the
  order of the collaborator calls. Only cases 306 and 308 have several groups: they are order-sensitive; a modern implementation has to iterate the groups in `String.hashCode` HashMap order to match them.
- Collaborators are recorded fakes with scripted answers (keys in case.json `colaboradores`): `factorParidad`, `requisitosBasicos`, `crearIngresoEgreso` (with `sinMascara`: the references -1, -2, ... are deterministic here),
  `liquidarComprobante`, `crearCargoAbono`, `calcularMontoNumeroCuota`, `crearSolicitudAporteRescate`, `precioBolsa`, `calcularMontoOrden`, `crearOrdenes`, `agregarPrograma`, `anularCargoAbono`.
- Branches that could NOT be pinned (unreachable through the legacy SQL, documented as defects below): bank-charge state != 3 in `logica`, CSC state RVC, `DEPOSITO_NO_EXISTE`/`DETALLE_DEPOSITO_NO_EXISTE` messages.

## Legacy defects for SME review (kept and pinned)
Earlier classes:
- ConsultarSaldoCuentaBanco: IncluyeSaldosContables=S with Pendientes=N gives NPE (03); projected balance counts annulled ingreso/egreso (IEFP leg lacks the ANU filter); lenient date parse.
- ConsultarLibretaBanco: null p_SaldoInicialSimulado NPE; USD forward stays 0; spot comprobante resolving to two operations ignored; several cargas: only the last used.
- ConsultarClearingFinanciero: dividends with CambiosCCLV=N named 'RV + PM' code 2; null AccionTipoOperacion NPE; resumen order is HashMap order (compared unordered).
- ProcesarClearingFinanciero: last group's comprobante stamped on ALL transactions (also already processed); zero-net group stamps NULL; PE localization NPE; references come from java.util.Random (masked by order of appearance).
- RecaudacionBCP: certification DNIs hard-coded (67412388, 44607843, 44225588, 11225588; kept as legacy behaviour by decision of the user); `NumeroOperacionBcp == ""` reference compare so an empty number throws; messages not copied.
- IngresarMandatoPAC: requisitos error does not copy messages; SELECT TOP 1 folio lookup; null DiaDescuento NPE; both folios informed leaves DiaDescuento null.
- ModificarPAC: `FolioCSC.equals(0L)` is always false, the bank is always updated.
- ProcesarCargaMasiva: IdCargaMasiva parameter overwritten in a loop (only the last load processed).
- CargaMasivaIngresoEgresoDAPFFMM: validation message of a short line lands at index 0; empty details still leave the header PRO.
- ConsultarFormatoArchivo.call always throws IllegalStateException. ActivarMandatosArchivoCargo.call is a stub returning null.
- CrearCargaMasivaPac swallows every exception into an 'Error' message (earlier deactivations stay committed).

New in this round:
- GestionDeDepositos: `" TES.DepositoDetalle"` (leading space) so LIQ by detail never collects detail ids (248); DEPOSITO_NO_EXISTE / DETALLE_DEPOSITO_NO_EXISTE are unreachable (the validator dereferences a null id first: NullPointerException, 259/260);
  EDP of a single detail sets the deposit status to NULL (the CASE has no EDP branch, 244).
- AnularNominasTransferencias: the requirements query is executed and ignored (a received nomina is annulled anyway, 232); every exception is swallowed (the temporary table is not dropped, changes already made stay, 228/231).
- QuitarDetalleNominasTransferencias: only the nomina of the FIRST register is recalculated (239); removing every detail leaves the nomina and partida amounts NULL (234); the comprobante/detail states go back to ING even for a comprobante outside the nomina (241).
- IngresarMandatoPACManejador: uses a plain Gson (default date format); only requests that reach IngresarMandatoPAC can produce its messages; its folio is `P_OUT_RESULTADO[0].IdMandatoPAC` or 0.
- ProcesarDistribucion: the investment-account step never fills `MovimientoGenerado` (`solicitud.equals(id)` compares a register with a Long); `Contador` is never incremented so every request adds the broker counter-entry;
  the corredor row is read before the basic requirements are checked (IndexOutOfBoundsException when missing, 288); the CSC "RVC" branch and the bank-charge-state branch of `logica` are unreachable because the SQL filters them; the file amount (CLP) is compared with the raw contract amount (UF) and the
  contract amount is converted afterwards; SAAPV with FFM + ACN lines: the loop `break`s at the first FFM line (Nemotecnico order), so ACN lines listed after it never reach the orders (286); orders: an order-creation message is repeated for EVERY request of the batch (message x request, 284); the output lists depend on HashMap order (IdTraspaso too).
- ProcesarDistribucion.ValidaPreRendicion: an orphan file row (no mandate) makes `groupingBy` receive a null key: NullPointerException (312/323); the "estado cargo != 3" detail errors count in `P_OUT_ERRORESCONTADOR`.
- CrearCargaMasivaPacRen: `P_OUT_ERRORESCONTADOR` is always 0 (contadorErrores is never updated); p_simular S validates and N schedules (inverted from what the names suggest); null p_simular fails after the details were inserted (327).
- DistribucionPac (pojo): dead code and does not compile (imports `cl.bcs.fondomutuo.RecaudacionTraspaso`, which does not exist); nothing refers to it: not covered.
- CalcularMontoYCobrosOrden.Orden_Entrada (used by ProcesarDistribucion) does not exist in the legacy source: the legacy tree does not compile as is; the oracle shim defines it from the usage.

## Not covered (NOT PROVEN)
- Cloper / CloperWsBSC: OUT of the proof by decision of the user (real HTTPS calls to a third party; needs org.json, absent from ~/.m2).
- Camara / CamaraCompensacion (POJOs: exercised through the clearing cases); DistribucionPac (dead, non-compiling pojo).
- ProcesarDistribucion: the stock-order path is pinned only with scripted fakes (price, quantity, order creation): the real CalcularMontoYCobrosOrden / CrearOrdenes are out of the module.

## Assumptions (documented, accepted)
- opencsv: legacy uses the 3-arg `CSVWriter(writer, ';', NO_QUOTE_CHARACTER)` removed in opencsv 5.9 (only version in .m2); a shim with 4.x semantics records the CSV golden.
- Fixed clock 2025-04-01 10:15:30 UTC, zone UTC. H2 stands in for HANA (translator `support/H2Hana`; DDL inferred from the legacy SQL). Multi-table UPDATE statements are rewritten in `h2-dialect/*.sql` (EXISTS / scalar subqueries; each file states why).
- DDL assumptions added for this round: PAC.LogMandatoPAC.FechaProceso is a DATE (the legacy compares it with CURRENT_DATE); CAR.CargaMasivaDetallePAC.MontoCargo is DECIMAL (the legacy reads it as BigDecimal; the earlier text column was changed and the
  PAC-universe goldens re-recorded); TES.Comprobante.IdTipoMovimiento exists; BAN.Banco.IdPersona, CSC.IdPortafolio/Canal/Fecha, SCS.FechaSuscripcion exist.

## Dates are masked, why
Some legacy SQL depends on the REAL database date (`ProcesarDistribucion_SELECT_RechazadosPreRen.sql` compares `FechaProceso = CURRENT_DATE`; the log inserts use `CURRENT_TIMESTAMP`), and H2 evaluates those with the
machine clock, not with the fixed harness clock. The behaviour must stay date-dependent, so the seeds of those cases use `CURRENT_DATE` / `CURRENT_TIMESTAMP`, and the DUMP must not carry the run day: the case lists the
columns in `"enmascarar"` and `Dump.TIMESTAMP` writes every date/timestamp text of them as `<TS>` on both sides (the legacy recording and the modern run).
- `PAC.LogMandatoPAC.FechaProceso` is masked in every ProcesarDistribucion / CrearCargaMasivaPacRen case (written by `CURRENT_TIMESTAMP`).
- TES-B-319 and TES-B-320 also mask `FechaSistema` (their seed stores `CURRENT_DATE` / `CURRENT_DATE - 3` there). Their messages are unchanged: 319 reports the log rejected TODAY as MANDATO_RECHAZADO_PRERENDICION, 320 does not report the one of another date.
- Audit: no other seed of families A or B stores a run-day value in a dumped column (the other CURRENT_* uses land in the masked column; the other dates are literal fixtures such as 2025-04-01 or 2026-03-01).
  A golden or dump containing a literal date derived from the run day is a defect: `grep -rn "<today>" cases/*/expected.json` must be empty.

## Mutation evidence
- Test-side: golden mutated (TES-B-209 SaldoDisponible 0 -> 1): ActualizarSaldosCuentaBancoServiceTest failures 5 -> 6; restored -> 5.
- Legacy-side (`mutation-check.sh`, whole suite re-recorded each time): see the table below.

| # | One-line mutation of the legacy copy (scratch) | Goldens changed (of 330 re-recorded) | Cases |
|---|---|---|---|
| 1 | `dep/GestionDeDepositos.java:366` `" TES.DepositoDetalle"` -> `"TES.DepositoDetalle"` (the leading-space defect fixed) | 2 | TES-B-248, TES-B-249 |
| 2 | `ProcesarDistribucion.java:1945` `compareTo(montoSumadoCSC) == 0` -> `!= 0` | 4 | TES-B-293, 296, 308, 318 |
| 3 | `nor/QuitarDetalleNominasTransferencias.java:144` `.get(0)` -> `.get(size - 1)` | 1 | TES-B-239 |
| 4 | `RecaudacionBCP.java:738` hard-coded DNI `"67412388"` -> `"67412389"` | 1 | TES-B-75 |
| 5 | `ModificarPAC.java:267` `FolioCSC.equals(0L)` -> `FolioCSC.equals("0")` | first run: 0 (a coverage gap: no case could observe it); after adding TES-B-330: 1 | TES-B-330 |

Every mutation is killed by at least one golden, and only by the cases of the mutated branch (two full re-recordings of the unmutated legacy are byte-identical, so a changed golden is a real behaviour change).
The mutation 5 gap was closed with a new case (TES-B-330: a SAAPV modification with a CSC contract whose folio is '0' shows the bank being updated).
