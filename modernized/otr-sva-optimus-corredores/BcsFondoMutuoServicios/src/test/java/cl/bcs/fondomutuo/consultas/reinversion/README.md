# FMM-FMR: characterization tests of `ConsultarFondoMutuoReinversion` (F3 W1 front 2)

Legacy: `legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/ConsultarFondoMutuoReinversion.java` (142 LOC, read only,
1,773 uses). Rules: `analysis/otr-sva-optimus-corredores/RULES_FONDOMUTUO_F3_A.md` section 8.4 (FM-FMR-01..04), defects D-F3A-11 / D-F3A-25 / D-F3A-27 and D-REQ-7,
matrix section 11 (FMR-C01..C03). The production classes do not exist yet: see `API_ASSUMED.md` for exactly what these tests expect.

## What is here

| File | What |
|---|---|
| `CasoFmr`, `SujetoFmr` | harness: builds the H2 of a case (F1 schema from `fondomutuo-3a/`, `fondomutuo-fmr/seed/base-fmr.sql`, the case seed), runs the NEW code through `ConsultarReinversionJavaCallHandler` and returns the same dump the oracle writes |
| `../DialectoLectura` | the one H2/HANA dialect rule of the read-only queries (shared by the three fronts, see `API_ASSUMED.md` 3) |
| `FmrEquivalenciaTest` | every case: output text, exception (class; message only for a plain `Exception`), servlet decision, tables, collaborator calls, sequences, session. A missing golden FAILS; prints `equivalence cases executed: N of M` and fails when N = 0 or N != M |
| `FmrDeterminismoTest` | N = 20 runs (`-Dfmr.corridas=N`) of every case whose golden list has two or more rows, each equal to the golden |
| `FmrReglasTest` | one test per card/defect with LITERAL rows and values; names carry the FM-FMR / D-F3A / D-REQ ids and RULE-076 |
| `FmrCompuertaTest` | closed-by-default gate (503 G0 before anything is read), its own list `CONSULTAR_FONDO_REINVERSION`, allow-list entry, JSON contract of the handler |

Resources (`src/test/resources/fondomutuo-fmr/`, copied by `sync_fmr.py`, never edited by hand): `cases/<FMM-FMR-nn_slug>/{case.json,seed.sql,expected.json}` (59 cases, 59 goldens),
`seed/base-fmr.sql` (ALTERs for columns of the HANA catalog the F1 schema lacks, the three view stand-ins, the fake universe), `determinism-fmr.txt` (the 20-run report of the oracle: 0 unstable).

## How to run

Oracle (records the goldens from the UNMODIFIED legacy; JDK 25 + H2 MODE=LEGACY, UTC, fixed clock 2026-03-02 14:30:00; no HANA, no network; compiles into a temp dir; own runner `runner-lect/OracleRunnerLect.java`, own case root `cases-fmr/`, so no other oracle script reads or rewrites these cases):

    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/make_cases_fmr.py        # inputs (deletes the old FMM-FMR-* dirs and their goldens)
    bash analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/record_fmr.sh FMM-FMR   # 20 runs per case; exit 4 = a case is unstable (no golden written)
    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/sync_fmr.py          # copies the cases that have a golden into this module

Tests (only once the production classes of `API_ASSUMED.md` exist; until then this package does not compile and that is expected):

    mvn -o -pl BcsFondoMutuoServicios test -Dtest='Fmr*Test'

## How to add a case

1. Add a `caso(slug, ref, entrada(...), seed=[...])` call to `equivalence/BcsFondoMutuoServicios/cases_fmr_a.py` AT THE END (numbers follow call order).
   Seed with the helpers of `cases_lect_lib.py` (universe: see the docstring of `cases_fmr_lib.py`; a new series goes into `SERIES` of `cases_lect_lib.py`, which FMR and IFM share). Never write `expected.json` by hand.
2. Run the three commands above. Read `determinism-fmr.txt`: an UNSTABLE case has no golden and the test fails until its cause is understood.
3. Pin it with a literal in `FmrReglasTest` when it documents a rule, and give it the card/defect id in the `ref` text.

## Assumptions and limits (read before trusting a green run)

* The legacy is the oracle: where it looks wrong (D-F3A-11 a row per serie and regime, `''` is not a wildcard, the `MontoPatrimonioEmisor` zero printed as `0E-8`) the golden keeps it and the discrepancy is listed in the report, not fixed.
* **H2 dialect bridge (Q-F3A-9 CLOSED against HANA QA)**: the SQL compares the BIGINT company with `''`; H2 refuses it for every call, so the oracle and the tests rewrite that one comparison (`DialectoLect` / `DialectoLectura`: `?` -> `CAST(? AS VARCHAR)`). Evidence (read-only SELECT on HANA QA, parameter bound with `setLong` like the legacy): the bound BIGINT `<> ''` is ALWAYS true (1 and 0 both 'distinto'; a CAST literal would raise error 339). So the CASE always takes `:IdEmpresa`; a company 0 (absent, null, `{}`) filters `IdEmpresa = 0` and returns an empty list. Three goldens pin it (`empresa-ausente-es-cero`, `empresa-nula-explicita`, `entrada-vacia`). The earlier 'all companies' hypothesis was wrong and is withdrawn: there is no multi-company leak candidate.
* The SQL has no `ORDER BY`: the row order of the goldens is the order of H2 for this data; the HANA order is NOT proven (the tests compare lists in order because both sides run the same SQL on the same H2).
* The H2 tables of `FMM.ValorCuota`, `FMM.SerieDeFondo`, `FMM.RegimenFondoMutuo` get the catalog columns the F1 schema lacks (`FechaCierre`, `InstrumentoOWP`, `Descripcion`); the three views (`MER.ConsultarEmisor` etc.) are created by the same `base-fmr.sql` only because it shares the universe with IFM: FMR itself reads none of them. `FMM.ValorCuota` has the real key `IdSerieFondo`, so D-REQ-7 ("no date filter") is not observable on a real HANA with one row per serie; the case `serie-con-cuota-historica-no-aplica-fecha` pins the SQL, not a multiplication of rows.
* Not observable on H2 / not pinned: real HANA types and scale (the H2 domains come from the catalog), the HANA `NVARCHAR` comparison of trailing spaces, concurrency, the HTTP router, who calls the class (nobody in the repo: Q-F3A-1 says the Java class runs).
* No "fresh inputs" run (F2 `make_fresh_*`) was produced for this class yet.
* The comparison never ran against production code: the harness and the tests were validated against a THROWAWAY implementation kept outside the repository (59 of 59 cases equivalent, all `Fmr*Test` green) and 4 one-line mutations of it went red (copy of a field, dropped rows, list name, MLT); the same exercise must be repeated on the real code.

## Do the goldens fail when the behaviour changes? (canary of the goldens, 2026-10-26)

`equivalence/BcsFondoMutuoServicios/canary_lect.py fmr` runs one-line mutations of a COPY of the legacy class (`LEGACY_OVERLAY` + `ORACLE_OUT`, nothing under `legacy/` touched) through the
oracle and counts the goldens whose dump changes. Result: 6 of 6 mutations change at least one golden: `MontoMinimo` copied from `MontoPatrimonioEmisor` (47 goldens), `MLT` copied from the currency (47), the regime key lost (46), `p_IdEstado` fixed to ACT (11), `p_IdEmpresa` fixed to 1 (7), the origin serie not sent (4).
