# FM-CPFF: characterization tests of `ConsultarCustodiaPortafolioFFMM` and the CHAIN CPF + FFMM (F3 W1 front 9)

Legacy: `legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/ConsultarCustodiaPortafolioFFMM.java` (519 lines, CCN 39 in 14 functions, 25 decisions in `logica`, read only;
the custody calculation CPF calls in process; **no registry id**: it is not a JavaCall entry of this module, owner decision 2026-10-28) and, for the chain, `ConsultarCustodiaPortafolioFondoMutuo.java` (CPF, front 8).
Rules: `analysis/otr-sva-optimus-corredores/RULES_FONDOMUTUO_F3_C.md` (cards FM-CPFF-01..22, defects D-F3C-01..15, matrix section 11, questions Q-F3C-1..9) and `PLAN_FONDOMUTUO_F3.md`. **Step 1, red before code**:
the production classes do not exist yet, so this package does not compile until they do (expected). `API_ASSUMED.md` says exactly what the tests expect (names, constructors, ports, how the adapter replaces
`ConsultarCustodiaPortafolioCustodiaCerrada`).

## What is here

| File | What |
|---|---|
| `CasoFfmm`, `SujetoFfmm` | harness of part A: builds the H2 of a case (F1 schema from `fondomutuo-3a/`, then `fondomutuo-ffmm/schema-ffmm.sql` and `seed/base-ffmm.sql`, then the case seed), runs the NEW code through the PORT CPF consumes (`ConsultarCustodiaPortafolioCustodiaPort.consultar(JsonObject)`, implemented by `ConsultarCustodiaPortafolioFfmmAdaptador`) and returns the same dump the oracle writes. The only scripted collaborator is `ConsultarEmpresa` (`Guion`) |
| `FfmmEquivalenciaTest` | every case: rows of `p_out_tbl` by key (`IdInstrumento_Nemotecnico_IdEmisor_IdCliente_IdPortafolio_IdRegimenFondoMutuo_Relacion_IdBoveda`) and then by position (D-F3C-13), output text with the SCALE of every number, exception (class; message only for a plain `Exception`), servlet decision, tables, collaborator calls, sequences, session. A missing golden FAILS; prints `equivalence cases executed: N of M` and fails when N = 0 or N != M |
| `FfmmDeterminismoTest` | N = 20 runs (`-Dffmm.corridas=N`) of every case with two or more output rows (the order is a `HashMap` iteration), each equal to the golden; plus a test that reads `determinism-ffmm.txt` (20 runs per case, no UNSTABLE case) |
| `FfmmReglasTest` | one test per card / defect with LITERAL values (names carry `fmCpff05..`, `dF3c01..`, `dNor9`, `desvioKit1`, `rule076`); includes C24 (the old stub service `ConsultarCustodiaPortafolioFFMMService` is NOT the class) |
| `FfmmAdaptadorTest` | the adapter alone (stub service, no database): JSON contract of the entry (defaults, explicit null, coercions, the 14 fields CPF sends) and of the answer (29 fields, nulls written, exact `BigDecimal` scale), exceptions go up unchanged, a NEW Gson per call, no JavaCall entry; the selection rule that replaces `ConsultarCustodiaPortafolioCustodiaCerrada` (Spring `ApplicationContextRunner`) and the M1 start-up guard of `ListasLecturaArranque` |
| `CasoCadena`, `SujetoCadena` | harness of part B (THE CHAIN): ONE H2 with the CPF tables AND the custody tables; the NEW CPF (through its JavaCall handler) wired to the NEW FFMM adapter, no double of the custody calculation; `ConsultarEmpresa`, `CalcularParidad`, `EntregarPreciosTasas` scripted (one `Guion` for both classes) |
| `CadenaEquivalenciaTest` | every chain case against the golden recorded from the legacy CPF over the legacy FFMM (both real): same comparison points as the CPF front, the `IdInterno` of the solicitudes modulo renaming (cut at the first `ConsultarEmpresa`, which in the chain is FFMM's); N = 20 runs of the multi-saldo / tie cases |
| `CadenaReglasTest` | what the chain adds, with literals (FM-CPFF-22.1..22.6) and WHY the goldens of the CPF front stay valid (the request is identical; the 108 scripted answers the real class cannot produce) |

Resources (copied by the sync scripts, never edited by hand): `src/test/resources/fondomutuo-ffmm/{cases-ffmm/<FM-CPFF-Cnn_slug>/{case.json,seed.sql,expected.json},schema-ffmm.sql,seed/base-ffmm.sql,determinism-ffmm.txt}`
(192 cases; the directory is `cases-ffmm/`, NOT `cases/`) and `src/test/resources/fondomutuo-cpfchain/{cases-cpfchain/<FM-CHAIN-nnn_slug>/...,schema-cpf.sql,seed/base-cpf.sql,schema-cpfchain.sql,seed/base-cpfchain.sql,determinism-cpfchain.txt}`
(74 cases; `schema-cpf.sql` / `base-cpf.sql` are BYTE copies of the CPF front's). Test source created by the sync scripts: `src/test/java/cl/bcs/oracle/H2FnFfmm.java` (the H2 function `MAP`); the chain reuses `H2FnCpf.java` of the CPF front.

## How to run

Oracle (records the goldens from the UNMODIFIED legacy; JDK 25 + H2 MODE=LEGACY, UTC, fixed clock 2026-03-02 14:30:00; no HANA, no network; compiles into a temp dir; the cases live in their own directories and are passed
to the shared `OracleRunner` with `-Doracle.cases`; nothing of the CPF / CVC / other fronts is read or written):

    EQ=analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios
    py -3 -I $EQ/legacy-oracle/make_cases_ffmm.py          # inputs + schema-ffmm.sql + seed/base-ffmm.sql (deletes the old cases-ffmm/FM-CPFF-* and their goldens)
    bash $EQ/legacy-oracle/record_ffmm.sh FM-CPFF          # 20 runs per case; exit 4 = a case is unstable (no golden written)
    py -3 -I $EQ/legacy-oracle/sync_ffmm.py                # copies the cases that have a golden (and H2FnFfmm) into this module
    py -3 -I $EQ/legacy-oracle/make_cases_cpfchain.py      # the chain: inputs + schema-cpfchain.sql + seed/base-cpfchain.sql
    bash $EQ/legacy-oracle/record_cpfchain.sh FM-CHAIN     # CPF over the REAL FFMM, 20 runs per case
    py -3 -I $EQ/legacy-oracle/sync_cpfchain.py            # (needs sync_cpf.py to have put H2FnCpf in the module before)

Tests (only once the production classes of `API_ASSUMED.md` exist; until then this package does not compile and that is expected). Run them in a JVM with the zone UTC and language en (the module's surefire already sets
`-Duser.timezone=UTC -Duser.language=en`):

    mvn -pl BcsFondoMutuoServicios test -Dtest='Ffmm*Test,Cadena*Test'

Read-only check of why the CPF front's goldens stay valid (writes nothing): `py -3 -I $EQ/legacy-oracle/check_cpf_goldens_vs_real_ffmm.py -v`.

## How to add a case

1. Part A: add a `caso(num, slug, ref, entrada(...), seed=[cartera(id, ...), garantia(origen=id, ...), his(id, ...), hgar(origen, ...), solicitud(id, ...)], colaboradores=empresa(persona=.., cliente_id=..))` call to
   `$EQ/cases_ffmm_a.py` (`num` is the matrix number `C01..C22` or a limit: `"07b"`, `25`...). `cartera()` writes a `CUS.Cartera` row with EVERY modelled column (default: 100 quotas of FM001A, client 100, portfolio 500, regime
   GEN, vault COR, price 9800, purchase value 980000, rate amount 990000, in force since 2026-01-12, no egress); `his()` / `hgar()` the `HIS.*` ones (backup 2026-03-09). A new table or column goes into `NUEVAS` / `AGREGADAS` of
   `cases_ffmm_lib.py`. Part B: `caso(slug, ref, entrada(...), [sol(...)], seed=[...])` in `$EQ/cases_cpfchain_a.py` (the CPF `entrada`, default date = `PLA.FechaSistema` 2026-03-02).
2. Run the commands above. Read `determinism-*.txt`: an UNSTABLE case has no golden and the test fails until its cause is understood. **Never write `expected.json` by hand.**
3. Pin it with a literal in `FfmmReglasTest` / `CadenaReglasTest` when it documents a rule, and give it the card / defect id in the `ref` text.

## Assumptions and limits (read before trusting a green run)

* **The five SQL run for real on H2 over stand-in tables** (`schema-ffmm.sql`): `CUS.Cartera` (F1 had a reduced 11-column stand-in: dropped and re-created with the 22 columns the SQL read), `HIS.Cartera`, `GTA.Garantia`, `HIS.Garantia`
  with the types the owner verified in HANA QA (read only, 2026-10-28: `CUS` / `GTA` all NOT NULL, amounts `DECIMAL(26,8)`, `IdBoveda` and `IdTipoInstrumento` `NVARCHAR(3)`, ids `BIGINT`; `HIS.*` the same columns almost all
  NULLABLE plus `FechaRespaldo TIMESTAMP(27,7)`) and 9 tables / views the F1 schema lacks (tagged REAL when `hana_qa_results/*.csv` has the column, ASSUMED otherwise: `CLI.ConsultarCliente`, `INS.ConsultarInstrumento`,
  `REL.ConsultarRelacionPersona`, `SOC.SocioDeNegocio`, matrices, product...). `MAP(x, NULL, 0, x)` is "NULL matches NULL" (ASSUMED, HANA not verified).
* **Discrepancy to settle (Q-F3C-10)**: the F1 catalog CSV says `CUS.Cartera.IdRegimenFondoMutuo` is NULLABLE; the owner's 2026-10-28 note says every column of `CUS.Cartera` is NOT NULL. The stand-in follows the CSV (nullable): cases `regimen-nulo-*` /
  `regimen-nulo-y-regimen-informado-dos-grupos` depend on it; if the owner is right they become unreachable on `CUS` (still reachable on `HIS`).
* **H2 is not HANA**: the golden pins the H2 behaviour of the verbatim SQL. What H2 does that HANA may not: the type / scale of the `'0' AS "Cantidad"` literal of the request branch inside the `UNION ALL` (Q-F3C-13), `NULL = NULL`
  in the historical request branch (Q-F3C-5), the comparison of a `TIMESTAMP` backup with the text of a date (a backup with a time part never matches), and the text `'2026-03-10 00:00:00'` against a `DATE` (H2 raises `SQLException`).
  The numbers of the output (`DECIMAL128` operations) are Java and equal in both.
* The legacy is the oracle: D-F3C-01 (price amount 0 with a guarantee), -02, -03 (`p_IdBoveda` ignored), -04 / -05 (raw default date, ADQ never returns open positions), -06 (request branch ignores instrument / issuer / family;
  the historical one disappears with a null issuer), -07 (historical without `Producto` / `RegimenTributario`), -08 (guarantee-only group without `RegimenTributario`), -09 (key without product / matrix / code; JOINs that duplicate),
  -10 (`(X / q) * q`), -11 (`PrecioCompra` of the last row, null `ValorCompra` => NPE, mixed `MathContext`), -12, -13 (order and tie by `HashMap`) are KEPT and listed, not fixed.
* **Candidates found by the oracle** (new, not in the rules file; each pinned by a case): **D-F3C-16** `Fecha.toDate` with the SMART resolver accepts `2026-02-30` (as 2026-02-28) while the raw text goes to the SQL
  (`fecha-inexistente`); **D-F3C-17** a date with time (19 characters) parses but the CURRENT SQL then compares text with time against a `DATE` (H2 `SQLException`; HANA unknown) (`fecha-con-hora`);
  **D-F3C-04 refined** the default date `0001-01-01` is not always empty: it returns the positions / guarantees in force since `0001-01-01` (`fecha-cero-con-posicion-desde-el-origen`) and never the backup of that day (`fecha-cero-usa-la-fuente-vigente`); **D-F3C-18** `List2Hash` accepts a NULL `IdPersonaDestino` (the card assumed an NPE) (`personas-destino-persona-nula-npe`); **D-F3C-19** every `JOIN` without a unique key duplicates the position and its quantity: two series of the same
  instrument, two `NOAPV` matrices with the same code, two business partners of the same person, a repeated client row (cases `22b`, `22e`, `22f`, `22g`, `22h`); **D-F3C-20** the `NOT IN` of the request branch compares by client only and ignores
  the state: an ANNULLED cartera or a cartera of ANOTHER portfolio makes the request disappear (`10j`, `10k`) and with a date type other than `LIQ` / `ADQ` it excludes nothing: the request appears next to the position it
  duplicates (`15`); **D-CHAIN-1** a guarantee-only group that shares CPF's key with a custody group is LOST (CPF keeps the custody group's `CantidadGarantia` 0: `FM-CHAIN-059`); **D-CHAIN-2** the request branch of FFMM makes CPF show
  saldos it did not ask for when `p_IdInstrumento` / `p_IdEmisor` / `p_IdFamiliaFondo` filter (`FM-CHAIN-048..050`); **D-CHAIN-3** the vault of a CPF saldo with a request is `''` when FFMM answers the request and `'COR'` when
  it does not (accepted after the date, annulled cartera, historical date with null issuer: `FM-CHAIN-031`, `046`, `054`).
* **The ids of the solicitudes** (D-F3A-04): the legacy CPF assigns them in `parallelStream().forEach`; the chain oracle runs with `ForkJoinPool.common.parallelism=0` and `CadenaEquivalenciaTest.conIdsCanonicos` compares the calls of the first
  valuation modulo renaming (the set must be exactly 2..n+1), as `CpfEquivalenciaTest`. Part A has no parallel stream.
* **JVM dependent text**: the exception text of a `NullPointerException` and of a `JsonSyntaxException` are not compared (only the class); the text of a `DateTimeParseException` IS (it comes from `java.time`, same in both).
* Not observable on H2 / not pinned: real HANA behaviour, the HTTP router, whether any consumer depends on the row order (Q-F3C-8), `FFMMWS` / `FFMMWSS` (twin classes with their own SQL, not read). No "fresh inputs" run (F2 `make_fresh_*`)
  was produced for these classes.

## Why the goldens of the CPF front (with a DOUBLE of FFMM) stay valid, and what they never proved

`check_cpf_goldens_vs_real_ffmm.py` (read only) and `CadenaReglasTest.goldensDeCpf_*`:

1. **What they pinned** is (a) the REQUEST CPF sends to the custody port (14 fields, `N` / `N` / `LIQ`, vault `''`, a real date, never `0001-01-01` or null) and (b) what CPF does with ANY answer of the port (a JSON contract of 29 fields). Neither depends on
   who answers: the chain captures the same request (`CadenaReglasTest.goldensDeCpf_laPeticionEsLaMisma`: byte-equal to the one recorded in `FMM-CPF-001`), and `CadenaEquivalenciaTest` proves the composition.
2. **What they never proved** is that the real class produces the rows they script. Of the 172 goldens, 108 script a custody answer with rows and ALL 108 script a vault `COR` (the helper `custodia()` default), 2 a relation
   `CPRO` / `REL` and 1 two rows with the same group key: the real class answers `''` / `''` / one group for the request CPF sends. So those 108 goldens are **not replayable as they are over the real FFMM** (CPF would copy `''`, not `COR`),
   but **none is wrong or needs to change**: they fix CPF's copy of whatever the port returns. 64 goldens never reach the custody port with rows. **No golden of the CPF front is incompatible with the real chain in the sense of contradicting it.**
3. The chain goldens are the missing half: vault `''`, request rows inside the custody (quantity 0, `PPPCustodia` null -> 0), `'COR'` only when FFMM does not answer the request, "the last custody wins" by FFMM's `HashMap` order.

## HANA questions (read only SELECT, NOT run: each needs the owner's OK, plan D-F3-6)

1. **Q-F3C-10** nullability of the custody columns the stand-in follows the CSV for (the CSV says `IdRegimenFondoMutuo` is NULLABLE, the owner note says all NOT NULL):
   `SELECT COLUMN_NAME, DATA_TYPE_NAME, LENGTH, SCALE, IS_NULLABLE FROM SYS.TABLE_COLUMNS WHERE TABLE_NAME IN ('BSCL.Custodia.BD::CUS.Cartera','BSCL.Custodia.BD::HIS.Cartera','BSCL.Custodia.BD::GTA.Garantia','BSCL.Custodia.BD::HIS.Garantia') AND COLUMN_NAME IN ('IdRegimenFondoMutuo','IdBoveda','IdEmisor','Nemotecnico','IdCartera','FolioCartera','IdCarteraOrigen','INGFechaLiquidacion','EGRFechaLiquidacion','INGFechaOperacion','EGRFechaOperacion','EGRMonto','FechaRespaldo','IdEstado') ORDER BY TABLE_NAME, POSITION;`
2. **Q-F3C-11** types of the stand-ins tagged ASSUMED:
   `SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE_NAME, LENGTH, SCALE, IS_NULLABLE FROM SYS.TABLE_COLUMNS WHERE TABLE_NAME IN ('BSCL.SocioNegocio.BD::SOC.SocioDeNegocio','BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV','BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV','BSCL.APV.BD::APV.ProductoAgrupador','BSCL.Plataforma.BD::INS.Instrumento') ORDER BY TABLE_NAME, POSITION;`
   and `SELECT VIEW_NAME, COLUMN_NAME, DATA_TYPE_NAME, LENGTH, SCALE, IS_NULLABLE FROM SYS.VIEW_COLUMNS WHERE VIEW_NAME IN ('BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente','BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento','BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona') ORDER BY VIEW_NAME, POSITION;`
3. **Q-F3C-5** `NULL = NULL` in the historical request branch (D-F3C-06): `SELECT 1 AS "X" FROM DUMMY WHERE CAST(NULL AS NVARCHAR(40)) = CAST(NULL AS NVARCHAR(40));` (H2 and the SQL standard: no row).
4. **Q-F3C-12** `MAP(NULL, NULL, 0, NULL)`: `SELECT MAP(CAST(NULL AS BIGINT), NULL, 0, CAST(NULL AS BIGINT)) AS "ConNulo", MAP(CAST(5 AS BIGINT), NULL, 0, CAST(5 AS BIGINT)) AS "ConValor" FROM DUMMY;` (stand-in: 0 and 5).
5. **Q-F3C-13** type / scale of the `UNION ALL` of the request branch (`'0' AS "Cantidad"` with a `DECIMAL(26,8)`): `SELECT CAST(1.5 AS DECIMAL(26,8)) AS "C" FROM DUMMY UNION ALL SELECT '0' AS "C" FROM DUMMY;` and print `getScale()` / `getBigDecimal().toString()`
   of both rows with the HANA driver (H2: the request row comes back as a `BigDecimal` of scale 0, the position one with scale 8).
6. **Q-F3C-14** the fan-outs (D-F3C-19) really happen in production data (each query should return 0 rows):
   `SELECT "IdInstrumento", "IdEmpresa", COUNT(*) FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" GROUP BY "IdInstrumento", "IdEmpresa" HAVING COUNT(*) > 1;`
   `SELECT "CodigoPortafolio", COUNT(*) FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" GROUP BY "CodigoPortafolio" HAVING COUNT(*) > 1;`
   `SELECT "IdPersona", COUNT(*) FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" GROUP BY "IdPersona" HAVING COUNT(*) > 1;`
   `SELECT "IdCliente", COUNT(*) FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" GROUP BY "IdCliente" HAVING COUNT(*) > 1;`
7. **Q-F3C-7** (the tie of the CPF key, D-F3C-13): groups of the current custody that share `IdCliente / IdPortafolio / IdInstrumento / IdRegimenFondoMutuo` but differ in `Nemotecnico` or `IdEmisor`:
   `SELECT "IdCliente", "IdPortafolio", "IdInstrumento", "IdRegimenFondoMutuo", COUNT(DISTINCT "Nemotecnico" || '|' || "IdEmisor") AS "Grupos" FROM "BSCL.Custodia.BD::CUS.Cartera" WHERE "IdTipoInstrumento" = 'FFM' AND "IdEstado" = 'VIG' GROUP BY "IdCliente", "IdPortafolio", "IdInstrumento", "IdRegimenFondoMutuo" HAVING COUNT(DISTINCT "Nemotecnico" || '|' || "IdEmisor") > 1;`
   and the same with several versions of one `FolioCartera` and the same `IdCartera` in the backup (the `RANK` tie): `SELECT "FechaRespaldo", "FolioCartera", "IdCartera", COUNT(*) FROM "BSCL.Custodia.BD::HIS.Cartera" GROUP BY "FechaRespaldo", "FolioCartera", "IdCartera" HAVING COUNT(*) > 1;`
8. **Q-F3C-15** time part of the backup date (a backup with a time never matches `FechaRespaldo = '2026-03-09'`): `SELECT COUNT(*) AS "ConHora" FROM "BSCL.Custodia.BD::HIS.Cartera" WHERE TO_TIME("FechaRespaldo") <> TO_TIME('00:00:00');`
9. **Q-F3C-16** a NULL `IdPersonaDestino` among the active relations (D-F3C-18): `SELECT COUNT(*) FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" WHERE "IdEstado" = 'ACT' AND "IdPersonaDestino" IS NULL;`
10. **Q-F3C-1** (is it a registered service?): `SELECT "IdServicio", "RutaServicio", "TipoServicio", "UsoServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE UPPER("RutaServicio") LIKE '%CUSTODIAPORTAFOLIOFFMM%';`

## Do the goldens fail when the behaviour changes? (canary of the goldens and of a throwaway implementation)

The new code does not exist, so it cannot be mutated. What was done (as CPF / FSA / SFM / DET and F2 plan section 7, "mutation of goldens"):

* **FFMM alone**: **111 one-line mutations** of a COPY of the legacy class and its five SQL (`legacy-oracle/canary_ffmm.py`: `LEGACY_OVERLAY` / `LEGACY_OVERLAY_RES` + `ORACLE_OUT`, one repetition, nothing under `legacy/` touched, the
  goldens not rewritten; 66 in the Java class, 45 in the SQL) run through the oracle and compared with the goldens. The FIRST pass changed 96 of 111 and left 9 unpinned (the default date routed to the historical source, `'s'` grouping
  the guarantee by vault, the product of a guarantee-only group, the client and the portfolio filter of the request branch ALONE, the guarantee backup `<=` date, the historical guarantee state `= 'VIG'`): cases `02d`, `02e`, `07m`,
  `11f`, `10l2`, `10l3`, `19f`, `19g` were ADDED and the 8 mutations re-run (1 or 2 cases change in each). **Final: 104 of 111 change at least one golden; 7 are reported as EQUIVALENT with the reason** (the group price is never output;
  a no-op sanity mutation; a NULL quantity on the NOT NULL `CUS.Cartera` / `GTA.Garantia`; a guarantee without origin cartera is already dropped by the next INNER JOIN; `MontoCompraCarteraOrigen` is never copied to the grouped guarantee
  (D-F3C-01); the purchase value of a request row is never observable because its quantity is 0). No mutation is left unpinned.
* **The chain**: **31 mutations** (`legacy-oracle/canary_cpfchain.py`: 9 in CPF, 10 in FFMM, 12 in the FFMM SQL) against `cases-cpfchain/`: the first pass changed 24 of 31 and left 5 unpinned (the 16-digit PPP, the request-branch vault,
  the `NOT IN` over every client, the guarantee state and the guarantee egress date); cases `075..078` were APPENDED (no number moved) and the 4 observable mutations re-run (1 case changes in each). **Final: 28 of 31 change a golden; 3 are
  reported as EQUIVALENT through the chain** (the vault column of the request branch and `boveda = cus.IdBoveda`: CPF always asks `N`; `DISTINCT` of the related persons: no chain case repeats an active relation). Run
  `py -3 -I legacy-oracle/canary_ffmm.py [java|sql] [text of the mutation name]` / `canary_cpfchain.py` to repeat it (env `CANARY_RANGE=a:b` splits the list over parallel copies; `ORACLE_REUSE_CLASSES=<work dir>` reuses the compiled classes for
  the SQL mutations, one copy per parallel worker).
* **A throwaway implementation of the assumed API** (a temporary copy of the module OUTSIDE the repository, never delivered, nothing in `src/main` of the real module) was written to prove that these tests COMPILE and CAN PASS and FAIL:
  `FfmmEquivalenciaTest` 194 = **192 of 192 cases executed** + 2 guards, `FfmmDeterminismoTest` 39 (38 cases with two or more rows x 20 runs + the report test), `FfmmReglasTest` 39, `FfmmAdaptadorTest` 12, `FfmmArranqueRealTest` 3,
  `CadenaEquivalenciaTest` 97 (**78 of 78 chain cases executed**, the multi-saldo and tie cases x 20 runs), `CadenaReglasTest` 13: **all green (397)**. Ten one-line mutations of THAT implementation turned tests red (every one, with 1 to 238
  failing tests each): `LocalDate.compareTo(..) != -1` instead of "not before" (5), a `LinkedHashMap` for the groups (46), the price amount surviving a guarantee i.e. D-F3C-01 "fixed" (21), `TipoFechaConsulta` bound to the guarantee SQL (238),
  a cached Gson (1), relation compared ignoring case (3), PPP with `DECIMAL64` (3), `PLA.FechaSistema` read before `ConsultarEmpresa` (8), `ConsultarCustodiaPortafolioCustodiaCerrada` still a `@Component` (1), the related persons looked up with
  the company id (3). The whole module suite (3322 tests) was run on it: the ONLY failure caused by this work is `ListasLecturaArranqueRealTest.cpfAbiertaConLaCustodiaCerrada_elArranqueFalla_M1` (it must add `--fondomutuo.custodia.ffmm.cerrada=true`:
  `API_ASSUMED.md` 4.4); 6 other tests fail only because the copy lives outside the repo tree (their fixtures are resolved relative to `analysis/`).
  When the real production code exists, repeat the exercise on it (the "not before" date, the order of the groups, the guarantee reset, the parameters of the guarantee SQL, the new Gson per call) and confirm the tests go red.
