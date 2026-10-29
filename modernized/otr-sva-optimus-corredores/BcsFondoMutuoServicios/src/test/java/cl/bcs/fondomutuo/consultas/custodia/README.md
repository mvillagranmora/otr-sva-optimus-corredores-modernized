# FMM-CPF: characterization tests of `ConsultarCustodiaPortafolioFondoMutuo` (F3 W1 front 8)

Legacy: `legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/ConsultarCustodiaPortafolioFondoMutuo.java` (1.164 LOC, CCN 80 in 46 functions, read only,
registry id 3041, 8.886 uses; plan F3 section 10: the Java class, not the HANA procedure, is the live implementation) and, because CPF calls it four times and reads its calendar,
`CalcularMontoNumeroCuota.java` and `ObtenerDiaSiguiente.java`. Rules: `analysis/otr-sva-optimus-corredores/RULES_FONDOMUTUO_F3_A.md` section 8.2 (FM-CPF-01..14), defects D-F3A-03..08 and
D-F3A-27 / D-NOR-9 (section 9), matrix CPF-C01..C11 (section 11). The production classes do not exist yet: see `API_ASSUMED.md` for exactly what these tests expect.

## What is here

| File | What |
|---|---|
| `CasoCpf`, `SujetoCpf` | harness: builds the H2 of a case (F1 schema from `fondomutuo-3a/`, `fondomutuo-cpf/schema-cpf.sql` and `seed/base-cpf.sql`, the case seed), runs the NEW code through `ConsultarCustodiaPortafolioJavaCallHandler`, answers the four collaborators outside the module (custody calculation, `CalcularParidad`, `EntregarPreciosTasas`, `ConsultarEmpresa`) from the case script (`Guion`) and returns the same dump the oracle writes |
| `CpfEquivalenciaTest` | every case: rows of `P_OUT_TBL` by key and then by position (D-F3A-04), output text, exception (class; message only for a plain `Exception`), servlet decision, tables, collaborator calls (name, arguments, answer, order), sequences, session. A missing golden FAILS; prints `equivalence cases executed: N of M` and fails when N = 0 or N != M. The `IdInterno` of the solicitudes of the first valuation are compared modulo renaming (see "The ids of the solicitudes") |
| `CpfDeterminismoTest` | N = 20 runs (`-Dcpf.corridas=N`) of every case with several saldos, several solicitud rows or a valuation of solicitudes, each equal to the golden; plus a test that reads `determinism-cpf.txt` (20 runs per case, no UNSTABLE case) |
| `CpfReglasTest` | one test per card/defect with LITERAL values; names carry `fmCpf01..14`, `dF3A..`, `dNor9` and `rule076` ids |
| `CpfCompuertaTest` | closed-by-default gate (503 G0 before anything is read), list `CONSULTAR_CUSTODIA_PORTAFOLIO`, allow-list entry, JSON contract of the handler and of a default output row (54 fields) |

Resources (`src/test/resources/fondomutuo-cpf/`, copied by `sync_cpf.py`, never edited by hand): `cases-cpf/<FMM-CPF-nnn_slug>/{case.json,seed.sql,expected.json}` (172 cases; the directory is
`cases-cpf/`, NOT `cases/`), `schema-cpf.sql`, `seed/base-cpf.sql`, `determinism-cpf.txt`. Test source created by the same script: `src/test/java/cl/bcs/oracle/H2FnCpf.java` (the H2 functions `MAP` and
`TO_DECIMAL` of the SQL of CPF).

## How to run

Oracle (records the goldens from the UNMODIFIED legacy; JDK 25 + H2 MODE=LEGACY, UTC, fixed clock 2026-03-02 14:30:00; no HANA, no network; compiles into a temp dir; the cases live in
`cases-cpf/` and are passed to the shared `OracleRunner` with `-Doracle.cases`; `runner-cpf/` holds the H2 functions, the shared runner is NOT modified; JVM flag
`-Djava.util.concurrent.ForkJoinPool.common.parallelism=0` by default, knob `ORACLE_FJ_PARALLELISM`):

    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/make_cases_cpf.py        # inputs + schema-cpf.sql + seed/base-cpf.sql (deletes the old cases-cpf/FMM-CPF-* and their goldens)
    bash analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/record_cpf.sh FMM-CPF   # 20 runs per case; exit 4 = a case is unstable (no golden written)
    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/sync_cpf.py          # copies the cases that have a golden (and H2FnCpf) into this module

Tests (only once the production classes of `API_ASSUMED.md` exist; until then this package does not compile and that is expected). Run them in a JVM with the zone UTC and language en
(`-Duser.timezone=UTC -Duser.language=en`):

    mvn -pl BcsFondoMutuoServicios test -Dtest='Cpf*Test'

## How to add a case

1. Add a `caso(slug, ref, entrada(...), [sol(id, ...), solm(id, monto, ...)], seed=[...], colaboradores=...)` call to `equivalence/BcsFondoMutuoServicios/cases_cpf_a.py` (numbers follow call order).
   `sol(...)` writes a `FMM.SolicitudAporteRescate` row with EVERY column (defaults: APO, ING, by units 10, FM001A, client 100, portfolio 500, regime GEN); `solm(...)` a solicitud by amount with 0 quotas (NOT valued:
   no collaborator call, so it never exposes the racy ids). Collaborators: `custodias(cus("FM002B", cantidad=...))`, `precios(precio=..., fuente=..., por_id=...)`, `paridad(final=..., cuota=uf(...))`;
   a case that reaches a collaborator without a script FAILS loudly ("no canned answer configured"); the defaults are no custody, price 1500.1234, `CalcularParidad` 1 per quota. A new table or column goes into
   `NUEVAS` / `AGREGADAS` of `cases_cpf_lib.py`. Never write `expected.json` by hand.
2. Run the three commands above. Read `determinism-cpf.txt`: an UNSTABLE case has no golden and the test fails until its cause is understood.
3. Pin it with a literal in `CpfReglasTest` when it documents a rule, and give it the card/defect id in the `ref` text.

## The ids of the solicitudes (D-F3A-04) and the order of the output

* The legacy assigns the `IdInterno` of the solicitudes (2, 3, ...) inside `parallelStream().forEach`. In the oracle the assignment is **the JDK's splitting order, not the list order** (2 rows: 3,2; 3 rows: 4,3,2;
  4 rows: 5,4,2,3) and it was the same with 0, 1 and 2 worker threads; with the default pool one case of 163 flaked in 19 re-runs (a 4-row case). It is not a business rule: the ids only cross-reference the valuation
  results with their rows. So (1) the oracle runs with `parallelism=0` (no worker threads) and (2) `CpfEquivalenciaTest.conIdsCanonicos` compares the calls of the FIRST valuation modulo the renaming of those ids:
  the SET of ids must be exactly 2..n+1 (starts at 2, no gap: that is pinned) and they are renamed by order of first appearance. Cases with only unvalued rows (`solm`) never expose the ids.
* The order of `P_OUT_TBL` is the iteration order of a `HashMap<String,..>` keyed `IdCliente_IdPortafolio_IdInstrumento_IdRegimenFondoMutuo` filled solicitudes first, then custody. It is deterministic for the same
  keys (the order of `String.hashCode` buckets, which changes when the map grows past 12 entries: case `orden-trece-saldos-redimensiona`). The tests compare the rows by key AND the order; the production code must
  use a `java.util.HashMap` with the default capacity and the same insertion order.

## Assumptions and limits (read before trusting a green run)

* **The four SQL run for real on H2 over stand-in tables** (`schema-cpf.sql`): 9 tables / views of 5 modules the F1 schema lacks (see the tags in the file: which columns are REAL from the catalog CSVs and which are
  ASSUMED) and 2 columns added to existing tables. The SQL files are verbatim; `MAP(x, NULL, 0, x)` is implemented as "NULL matches NULL" (ASSUMED, HANA not verified) and `TO_DECIMAL(x,26,8)` as `setScale(8, HALF_UP)`.
* The custody calculation `ConsultarCustodiaPortafolioFFMM` (another piece), `CalcularParidad`, `EntregarPreciosTasas` and `ConsultarEmpresa` are scripted doubles on both sides (their real behaviour is theirs);
  `CalcularMontoNumeroCuota` and `ObtenerDiaSiguiente` run for real in the oracle. Q-F3A-6 (that `CalcularMontoNumeroCuota` answers with the `IdInterno` it received, also negative and `*-2`) is an assumption of the script:
  the guarantee valuation is asked with `IdInterno * -2` and looked up with `IdInterno`, so D-F3A-03 (`MontoGarantia` always 0) holds whatever it answers.
* The legacy is the oracle: D-F3A-03 (MontoGarantia 0), D-F3A-04 (order, ids), D-F3A-05 (valuation rules), D-F3A-06 (`Nemotecnico`/`NombreAdministradora` with ids), D-F3A-07 (the company is always queried), D-F3A-08 (the
  first regimen decides MLT), D-F3A-27 (no counter => ROLLBACK), the scale-sensitive and rounding rules of the valuation (D-CUO-*) are kept and listed, not fixed. **D-F3A-05, the TYPE used to value the rescate in
  transit (APO), is NOT observable at the boundary of the class** (`CalcularMontoNumeroCuota` only uses the type of a deferred solicitud and CPF never defers): no test can pin it; the canary lists the mutation as
  equivalent.
* **Open gap (D-F3A-29 candidate)**: `fechas.ObtenerDiaSiguiente` (PROVEN in F1) does not reproduce the legacy for dates of year 0001: the legacy uses `java.util.Calendar` (Julian / Gregorian hybrid: 0001-01-01 is a
  Saturday) and the port `LocalDate` (proleptic: a Monday). CPF exposes it when `PLA.CalculaValorCuotaDiaSiguiente` is exactly 'S' and a saldo has no valuation (`FechaValorCuota` = `0001-01-01`): the legacy asks the
  paridad for `0001-01-03`, the port for `0001-01-02`. Case `dia-siguiente-s-sin-valorizar` (and `CpfReglasTest.dF3A29_diaSiguienteDelAnio1`) are RED against the unmodified port until the owner decides (fix the port with
  the hybrid calendar, or approve a DEV). The disposable implementation passes it with a Calendar-based `ObtenerDiaSiguiente`.
* **JVM dependent text**: the exception text of a `NullPointerException` (helpful NPE messages) and the message of a `Collectors.toMap` duplicate key are not compared (only the class), as in the other fronts.
* Not observable on H2 / not pinned: real HANA behaviour (types and scales of the stand-ins, `MAP`, the ordering of a SELECT without ORDER BY: D-F3A-08 on HANA), whether any consumer depends on the row order
  (Q-F3A-5), the HTTP router, the custody library. No "fresh inputs" run (F2 `make_fresh_*`) was produced for this class yet.

## HANA questions (read only SELECT, NOT run: each needs the owner's OK, plan D-F3-6)

1. Types of the columns this front ASSUMES (every one is tagged ASSUMED in `schema-cpf.sql`):
   `SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE_NAME, LENGTH, SCALE, IS_NULLABLE FROM SYS.TABLE_COLUMNS WHERE TABLE_NAME IN ('BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV', 'BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV', 'BSCL.APV.BD::APV.ProductoAgrupador', 'BSCL.Mesa.BD::SPT.OperacionSpot', 'BSCL.Plataforma.BD::INS.Instrumento', 'BSCL.Plataforma.BD::INS.TipoInstrumento', 'BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento', 'BSCL.SocioNegocio.BD::SOC.SocioDeNegocio', 'BSCL.Portafolio.BD::POR.Portafolio') ORDER BY TABLE_NAME, POSITION;`
   and for the view `SELECT VIEW_NAME, COLUMN_NAME, DATA_TYPE_NAME, LENGTH, SCALE FROM SYS.VIEW_COLUMNS WHERE VIEW_NAME = 'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente' ORDER BY POSITION;`
2. `MAP(value, NULL, 0, value)` with a NULL value (the filter by `IdProductoAgrupador`): `SELECT MAP(CAST(NULL AS BIGINT), NULL, 0, CAST(NULL AS BIGINT)) AS "ConNulo", MAP(CAST(5 AS BIGINT), NULL, 0, CAST(5 AS BIGINT)) AS "ConValor" FROM DUMMY;` (H2 stand-in: 0 and 5).
3. Impact of D-F3A-08 (MLT takes the first regimen of the series, no ORDER BY): how many series have more than one ACT regimen with different mandate flags:
   `SELECT COUNT(*) AS "SeriesAmbiguas" FROM (SELECT R."IdSerieFondo" FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" R LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" G ON G."IdRegimenFondoMutuo" = R."IdRegimenFondoMutuo" WHERE R."IdEstado" = 'ACT' GROUP BY R."IdSerieFondo" HAVING COUNT(*) > 1 AND (COUNT(DISTINCT G."RescateConMandato") > 1 OR COUNT(DISTINCT G."AceptaMandatoEnReinversion") > 1));`
4. Can `IngresoAutomatico` be NULL in the solicitudes in transit (the automatic-income filter compares it with 'N'; H2 stand-in: NULL does not match)?
   `SELECT "IngresoAutomatico", COUNT(*) FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" WHERE "Estado" IN ('ING', 'PEN') GROUP BY "IngresoAutomatico";`
5. Does the HANA JDBC driver bind a Java `null` Long for `:IdCliente = 0 OR ...` as SQL NULL (so that a null client returns no rows, as in H2)? Probe: `SELECT 1 AS "X" FROM DUMMY WHERE (CAST(? AS BIGINT) = 0 OR 1 = 0);` with `setNull(1, Types.BIGINT)` (expected: no row).

## Do the goldens fail when the behaviour changes? (canary of the goldens)

The new code does not exist, so it cannot be mutated. What was done (as FSA / SFM / DET and F2 plan section 7, "mutation of goldens"): **62 one-line mutations** of a COPY of the legacy classes and SQL
(`legacy-oracle/canary_cpf.py`: `LEGACY_OVERLAY` / `LEGACY_OVERLAY_RES` + `ORACLE_OUT`, one repetition, nothing under `legacy/` touched, the goldens not rewritten; 45 in the Java class, 17 in its four SQL) were run through
the oracle and their dumps compared with the goldens. **59 change at least one golden** (from 1 to 162 cases each); **3 are reported as EQUIVALENT with the reason** (they cannot be observed at the boundary of the class):
the preliminary `NombreAdministradora` (a dead write: `obtenerSalidaCustodia` recomputes it), the rescate in transit valued as `RES` instead of `APO` (D-F3A-05: the type only selects the observed-dollar rate of a deferred
solicitud and CPF never defers), and `p_diaHabil` 'N' for ONE day (adds a calendar day and rolls forward to the same working day). **No mutation is left unpinned**: the first pass left two (the amount of a rescate taken
from `Monto` instead of `MontoMonedaSistema`, and the destination currency of the paridad in the 'S' branch, both invisible with CLP funds) plus a negative rescate in transit, and that is why cases 170-172
(`transito-rescate-sin-precio-en-el-saldo`, `dia-siguiente-s-fondo-en-dolares`, `rescate-total-custodia-negativa`) were APPENDED (no number moved) and the three mutations re-run (1 case changes in each). Run
`py -3 -I legacy-oracle/canary_cpf.py [java|sql] [text of the mutation name]` to repeat it (env `CANARY_RANGE=a:b` splits the list over parallel copies; `ORACLE_REUSE_CLASSES=<work dir>` reuses the compiled classes for the SQL mutations).

Besides, to prove that these tests COMPILE and CAN PASS and FAIL, a throwaway reference implementation of the assumed API was written OUTSIDE the repository (a temporary copy of the module, never delivered, nothing in `src/main`
of the real module): the whole package ran green there (`CpfCompuertaTest` 9, `CpfDeterminismoTest` 74 = 73 cases x 20 runs + the report test, `CpfEquivalenciaTest` 174 = **172 of 172 cases executed** + 2 guards, `CpfReglasTest` 40 = 297 tests) and
four one-line mutations of that implementation turned tests red (a `LinkedHashMap` instead of the `HashMap`: 16; the guarantee asked and looked up with the same id, i.e. D-F3A-03 "fixed": 8; the ids of the solicitudes starting
at 1: 47; the total rescate without the aportes in transit: 3). The only change the throwaway needed beyond the CPF classes was a Calendar-based `ObtenerDiaSiguiente` (open gap above): with the unmodified port exactly one case
(`dia-siguiente-s-sin-valorizar`) and `CpfReglasTest.dF3A29_diaSiguienteDelAnio1` are red (3 test results: the case, the "N of M" guard that follows it, and the rule). When the real production code exists, repeat the exercise on it
(the order of the saldos, the `IdInterno * -2` of the guarantee, the base 2 of the ids, the `!= 0` filters, the 'S' of the next day) and confirm `CpfEquivalenciaTest` / `CpfReglasTest` go red.
