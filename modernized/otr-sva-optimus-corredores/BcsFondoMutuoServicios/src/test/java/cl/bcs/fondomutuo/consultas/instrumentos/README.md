# FMM-IFM: characterization tests of `ConsultarInstrumentosFondosMutuos` (F3 W1 front 3)

Legacy: `legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/ConsultarInstrumentosFondosMutuos.java` (293 LOC, read only,
1,183 uses). Rules: `analysis/otr-sva-optimus-corredores/RULES_FONDOMUTUO_F3_A.md` section 8.5 (FM-IFM-01..06), defects D-F3A-12 / 13 / 25 / 27 and D-REQ-7, matrix section 11
(IFM-C01..C06). The production classes do not exist yet: see `API_ASSUMED.md` for exactly what these tests expect.

## What is here

| File | What |
|---|---|
| `CasoIfm`, `SujetoIfm` | harness: builds the H2 of a case (F1 schema from `fondomutuo-3a/`, `fondomutuo-ifm/seed/base-ifm.sql`, the case seed), runs the NEW code through `ConsultarInstrumentosJavaCallHandler` and returns the same dump the oracle writes |
| `../DialectoLectura` | the one H2/HANA dialect rule of the read-only queries (needed by FMR only; applied here too, it does not match any IFM SQL) |
| `IfmEquivalenciaTest` | every case: output text, exception (class; message only for a plain `Exception`), servlet decision, tables, collaborator calls, sequences, session. A missing golden FAILS; prints `equivalence cases executed: N of M` and fails when N = 0 or N != M |
| `IfmDeterminismoTest` | N = 20 runs (`-Difm.corridas=N`) of every case whose golden has two or more series (the legacy fills the regime lists on `parallelStream`), each equal to the golden |
| `IfmReglasTest` | one test per card/defect with LITERAL series, regimes and full JSON texts; names carry the FM-IFM / D-F3A / D-REQ ids and RULE-076 |
| `IfmCompuertaTest` | closed-by-default gate (503 G0 before anything is read), its own list `CONSULTAR_INSTRUMENTOS`, allow-list entry, JSON contract of the handler |

Resources (`src/test/resources/fondomutuo-ifm/`, copied by `sync_ifm.py`, never edited by hand): `cases/<FMM-IFM-nn_slug>/{case.json,seed.sql,expected.json}` (85 cases, 85 goldens),
`seed/base-ifm.sql` (ALTERs for catalog columns the F1 schema lacks, the three view stand-ins, the `TO_VARCHAR` alias, the fake universe, the dictionary row `UI_SIN_VALORES`),
`determinism-ifm.txt` (the 20-run report of the oracle: 0 unstable).

## How to run

Oracle (records the goldens from the UNMODIFIED legacy; JDK 25 + H2 MODE=LEGACY, UTC, fixed clock 2026-03-02 14:30:00; no HANA, no network; compiles into a temp dir; own runner `runner-lect/OracleRunnerLect.java`, own case root `cases-ifm/`):

    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/make_cases_ifm.py        # inputs (deletes the old FMM-IFM-* dirs and their goldens)
    bash analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/record_ifm.sh FMM-IFM   # 20 runs per case; exit 4 = a case is unstable (no golden written)
    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/sync_ifm.py          # copies the cases that have a golden into this module

Tests (only once the production classes of `API_ASSUMED.md` exist; until then this package does not compile and that is expected):

    mvn -o -pl BcsFondoMutuoServicios test -Dtest='Ifm*Test'

## How to add a case

1. Add a `caso(slug, ref, entrada(ef(...)), seed=[...])` call to `equivalence/BcsFondoMutuoServicios/cases_ifm_a.py` AT THE END (numbers follow call order; `ef(...)` carries ONLY the keys sent: the legacy constructor defaults the rest).
   Seed with the helpers of `cases_lect_lib.py` (universe: see the docstring of `cases_fmr_lib.py`; a new series goes into `SERIES` of `cases_lect_lib.py`, which FMR and IFM share). Never write `expected.json` by hand.
2. Run the three commands above. Read `determinism-ifm.txt`: an UNSTABLE case has no golden and the test fails until its cause is understood.
3. Pin it with a literal in `IfmReglasTest` when it documents a rule, and give it the card/defect id in the `ref` text.

## Assumptions and limits (read before trusting a green run)

* The legacy is the oracle: where it looks wrong (D-F3A-12 `ContadorErrores` instead of `P_OUT_ERRORESCONTADOR`, so the servlet rolls back every call; `IdSerieFondoOrigen` `" "` or null explicit take the reinversion branch; D-F3A-13 the reinversion SQL has no `DecimalesValorCuota` / `MontoPatrimonioEmisor` and no ACT filter on the regimes; amounts printed as `0E-8`) the golden keeps it and the discrepancy is listed in the report, not fixed.
* The three views (`MER.ConsultarEmisor`, `FMM.ConsultarCategoriaPerfilFondoMutuo`, `FMM.ConsultarConfiguracionRegimenFondoMutuo`) are H2 tables with ASSUMED columns (the catalog CSVs of `hana_qa_results/` do not list them: only the columns the SQL names). The real views may repeat rows or filter differently; the SYNTHETIC cases `emisor-repetido-en-la-vista-duplica-serie`, `categoria-repetida-en-la-vista-duplica-serie`, `reinversion-config-duplicada-no-duplica-filas`, `regimen-sin-maestro-no-se-lista` and `regimen-de-la-serie-repetida-en-la-tabla` document what the legacy SQL does IF that data existed.
* `FMM.RegimenSerieDeFondo` has no key in the H2 schema (HANA may have one); the case with a repeated regime row is SYNTHETIC.
* The SQL has no `ORDER BY` for the series: the order of the goldens is the order of H2 for this data; the HANA order is NOT proven. The regime lists are ordered by the SQL.
* `TO_VARCHAR(date, 'YYYY-MM-DD')` is emulated with the oracle's `H2Fn.toNvarchar` (alias created in `base-ifm.sql`); the real HANA function was not exercised.
* Not observable on H2 / not pinned: real HANA types and scale (the H2 domains come from the catalog), concurrency, the HTTP router, who calls the class (`ProcesaCargaSeleccionada` only imports `InFondos`; Q-F3A-1 says the Java class runs).
* No "fresh inputs" run (F2 `make_fresh_*`) was produced for this class yet.
* The comparison never ran against production code: the harness and the tests were validated against a THROWAWAY implementation kept outside the repository (85 of 85 cases equivalent, all `Ifm*Test` green, determinism 20 runs green) and 6 one-line mutations of it went red; the same exercise must be repeated on the real code.

## Do the goldens fail when the behaviour changes? (canary of the goldens, 2026-10-26)

`equivalence/BcsFondoMutuoServicios/canary_lect.py ifm` runs one-line mutations of a COPY of the legacy class (`LEGACY_OVERLAY` + `ORACLE_OUT`, nothing under `legacy/` touched) through the
oracle and counts the goldens whose dump changes. Result: 6 of 6 mutations change at least one golden: `OcultarBloqueados` `equalsIgnoreCase` -> `equals` (1 golden: `normal-ocultar-bloqueados-s-minuscula`), `FiltroRegimen` threshold `> 0` -> `> 1` (5), normal branch also for a null origin (1), `ContadorErrores` 1 -> 2 (4), per-serie regime filter inverted (61), `MostrarBloqueados` N/S swapped (19).
