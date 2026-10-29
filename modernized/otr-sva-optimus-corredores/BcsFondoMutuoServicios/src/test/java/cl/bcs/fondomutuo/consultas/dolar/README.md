# FMM-VDO: characterization tests of `ConsultarValorDolarFondoMutuo` (F3 W1 front 4)

Legacy: `legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/ConsultarValorDolarFondoMutuo.java` (213 LOC, read only,
387 uses; `ProcesarReinversionRescate` also calls it in process). Rules: `analysis/otr-sva-optimus-corredores/RULES_FONDOMUTUO_F3_A.md` section 8.7 (FM-VDO-01..04), defects
D-F3A-14 / 15 / 25 and D-NOR-9, matrix section 11 (VDO-C01..C04). The production classes do not exist yet: see `API_ASSUMED.md` for exactly what these tests expect.

## What is here

| File | What |
|---|---|
| `CasoVdo`, `SujetoVdo` | harness: builds the H2 of a case (F1 schema from `fondomutuo-3a/`, `fondomutuo-vdo/seed/base-vdo.sql`, the case seed), runs the NEW code through `ConsultarDolarJavaCallHandler` and returns the same dump the oracle writes; `CalcularParidad` is answered by `Guion` from the `colaboradores` of the case |
| `../DialectoLectura` | the one H2/HANA dialect rule of the read-only queries (needed by FMR only; applied here too, it does not match any VDO SQL) |
| `VdoEquivalenciaTest` | every case: output text, exception (class; message only for a plain `Exception`), servlet decision, tables, collaborator calls (name, arguments, answer), sequences, session. A missing golden FAILS; prints `equivalence cases executed: N of M (K relaxed)` and fails when N = 0 or N != M |
| `VdoDeterminismoTest` | N = 20 runs (`-Dvdo.corridas=N`) of every case with two or more series in the request, each equal to the golden |
| `VdoReglasTest` | one test per card/defect with LITERAL values and the exact JSON sent to `CalcularParidad`; names carry the FM-VDO / D-F3A / D-NOR ids and RULE-076 |
| `VdoCompuertaTest` | closed-by-default gate (503 G0 before anything is read), its own list `CONSULTAR_VALOR_DOLAR`, allow-list entry, JSON contract of the handler |

Resources (`src/test/resources/fondomutuo-vdo/`, copied by `sync_vdo.py`, never edited by hand): `cases/<FMM-VDO-nn_slug>/{case.json,seed.sql,expected.json}` (57 cases, 57 goldens),
`seed/base-vdo.sql` (the fake series V001..V005), `determinism-vdo.txt` (the 20-run report of the oracle: 0 unstable).

## How to run

Oracle (records the goldens from the UNMODIFIED legacy; JDK 25 + H2 MODE=LEGACY, UTC, fixed clock 2026-03-02 14:30:00; no HANA, no network; compiles into a temp dir; own runner `runner-lect/OracleRunnerLect.java`, own case root `cases-vdo/`; Plataforma `CalcularParidad` is the only registered double):

    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/make_cases_vdo.py        # inputs (deletes the old FMM-VDO-* dirs and their goldens)
    bash analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/record_vdo.sh FMM-VDO   # 20 runs per case; exit 4 = a case is unstable (no golden written)
    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/sync_vdo.py          # copies the cases that have a golden into this module

Tests (only once the production classes of `API_ASSUMED.md` exist; until then this package does not compile and that is expected):

    mvn -o -pl BcsFondoMutuoServicios test -Dtest='Vdo*Test'

## How to add a case

1. Add a `caso(slug, ref, entrada([sr(serie, tipo, fecha), ...]), seed=[...], colaboradores=paridad((idInterno, monto), ...))` call to `equivalence/BcsFondoMutuoServicios/cases_vdo_a.py` AT THE END (numbers follow call order).
   `parametro_dolar("S")` in the seed switches the branch; `paridad(...)` scripts the collaborator (it answers the same list to every call; `{"error": "text"}` in `respuestas` makes it throw). Never write `expected.json` by hand.
2. Run the three commands above. Read `determinism-vdo.txt`: an UNSTABLE case has no golden and the test fails until its cause is understood.
3. Pin it with a literal in `VdoReglasTest` when it documents a rule, and give it the card/defect id in the `ref` text.

## Assumptions and limits (read before trusting a green run)

* The legacy is the oracle: where it looks wrong (D-F3A-14 `IdInterno` always 0, so two distinct dates throw `key field is not unique`; D-F3A-15 every type other than `APO` takes the rescue rate and the date is ignored; zero rates printed as `0E-8`) the golden keeps it and the discrepancy is listed in the report, not fixed.
* `CalcularParidad` is a SCRIPTED double on both sides, not the real Plataforma class: the cases with two dates only show the exception because the script answers two outputs with `IdInterno 0` (what a faithful collaborator that echoes the received id would do). Q-F3A-6 asks how the real class behaves with a repeated `IdInterno`.
* **Kit divergence (a decision for the user, see `API_ASSUMED.md` 3)**: three goldens (company without configuration rows) pin a `NullPointerException` of the legacy configuration kit; `VdoEquivalenciaTest.KIT_NPE_DE_CONFIGURACION` accepts the class of the kit port for those three slugs only and prints how many were relaxed.
* SYNTHETIC cases (data HANA would not allow): `tabla-dos-filas-de-tipo-de-cambio-list2hash` (the primary key of `FMM.ValorTipoCambio` is dropped in its seed).
* Not observable on H2 / not pinned: real HANA types and scale (the H2 domains come from the catalog), the real `CalcularParidad`, concurrency, the HTTP router.
* No "fresh inputs" run (F2 `make_fresh_*`) was produced for this class yet.
* The comparison never ran against production code: the harness and the tests were validated against a THROWAWAY implementation kept outside the repository (57 of 57 cases equivalent with the 3 relaxed ones, all `Vdo*Test` green, determinism green) and 5 one-line mutations of it went red; the same exercise must be repeated on the real code.

## Do the goldens fail when the behaviour changes? (canary of the goldens, 2026-10-26)

`equivalence/BcsFondoMutuoServicios/canary_lect.py vdo` runs one-line mutations of a COPY of the legacy class (`LEGACY_OVERLAY` + `ORACLE_OUT`, nothing under `legacy/` touched) through the
oracle and counts the goldens whose dump changes. Result: 7 of 7 mutations change at least one golden: `APO` takes the rescue rate (21 goldens), dates not deduplicated (6), `UsaDolarObservado` `equals` -> `equalsIgnoreCase` (1: `observado-minuscula-no-es-s`), source currency USD -> EUR (17), `MontoOrigen` 1 -> 10 (17), no parity: 0 -> null (2), `IdInterno` sent as `i + 1` (17).
