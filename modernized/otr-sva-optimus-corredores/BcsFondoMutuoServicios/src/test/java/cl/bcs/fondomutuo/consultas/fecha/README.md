# FMM-FSA: characterization tests of `ConsultarFechaSolicitudAporteRescate` (F3 W1 front 1)

Legacy: `legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/ConsultarFechaSolicitudAporteRescate.java` (375 LOC, read only,
16,217 uses). Rules: `analysis/otr-sva-optimus-corredores/RULES_FONDOMUTUO_F3_A.md` section 8.1 (FM-FSA-01..08), defects D-F3A-01/02 and D-NOR-9, matrix
section 11 (FSA-C01..C09). The production classes do not exist yet: see `API_ASSUMED.md` for exactly what these tests expect.

## What is here

| File | What |
|---|---|
| `CasoFsa`, `SujetoFsa` | harness: builds the H2 of a case (F1 schema from `fondomutuo-3a/`, `fondomutuo-fsa/seed/base-fsa.sql`, the case seed), runs the NEW code through `ConsultarFechaJavaCallHandler` and returns the same dump the oracle writes |
| `FsaEquivalenciaTest` | every case: output text, exception (class; message only for a plain `Exception`), servlet decision, tables, collaborator calls, sequences, session. A missing golden FAILS; prints `equivalence cases executed: N of M` and fails when N = 0 or N != M |
| `FsaDeterminismoTest` | N = 20 runs (`-Dfsa.corridas=N`) of every multi-request case, each equal to the golden (the legacy uses `parallelStream`) |
| `FsaReglasTest` | one test per card/defect with LITERAL dates and messages; names carry the FM-FSA / D-F3A / D-NOR ids and RULE-076 |
| `FsaCompuertaTest` | closed-by-default gate (503 G0 before anything is read), allow-list entry, JSON contract of the handler |

Resources (`src/test/resources/fondomutuo-fsa/`, copied by `sync_fsa.py`, never edited by hand): `cases/<FMM-FSA-nn_slug>/{case.json,seed.sql,expected.json}`,
`seed/base-fsa.sql`, `schema-fsa.sql` (manifest: no DDL, every object is already in `fondomutuo-3a/schema-fmm.sql` with REAL catalog types, 0 ASSUMED),
`determinism-fsa.txt` (the 20-run report of the oracle).

## How to run

Oracle (records the goldens from the UNMODIFIED legacy; JDK 25 + H2 MODE=LEGACY, UTC, fixed clock 2026-03-02 14:30:00; no HANA, no network; compiles into a temp dir):

    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/make_cases_fsa.py     # inputs (deletes the old FMM-FSA-* dirs and their goldens)
    bash analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/record_fsa.sh FMM-FSA   # 20 runs per case; exit 4 = a case is unstable (no golden written)
    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/sync_fsa.py          # copies the cases that have a golden into this module

Tests (only once the production classes of `API_ASSUMED.md` exist; until then this package does not compile and that is expected):

    mvn -pl BcsFondoMutuoServicios test -Dtest='Fsa*Test'

## How to add a case

1. Add a `caso(slug, ref, entrada(...), seed=[...], colaboradores=...)` call to `equivalence/BcsFondoMutuoServicios/cases_fsa_a.py` AT THE END (numbers follow call order).
   Seed with the helpers of `cases_fsa_lib.py` (universe: see its docstring) and, if it needs a new series, add it to `SERIES` there. Never write `expected.json` by hand.
2. Run the three commands above. Read `determinism-fsa.txt`: an UNSTABLE case has no golden and the test fails until its cause is understood.
3. Pin it with a literal in `FsaReglasTest` when it documents a rule, and give it the card/defect id in the `ref` text.

## Assumptions and limits (read before trusting a green run)

* The legacy is the oracle: where it looks wrong (D-F3A-01 reinversion by list size, the NPE for foreign series whose fund country differs from the issuer country,
  `FondoNacionalDolar`, the collaborator's date rules) the golden keeps it and the discrepancy is listed in the report, not fixed.
* `ConsultarEmpresa` (Plataforma) is a scripted double in both sides; nothing else is doubled (`CalcularFecha...`, `ObtenerPerfilFondoMutuo`, the kit and the calendar run for real).
* SYNTHETIC cases (data HANA would not allow): `serie-dos-filas-toMap-IllegalState` (duplicate `PLA.Moneda` row), `db-nulos-*` (columns that are NOT NULL in HANA made nullable).
* `usuario-con-dos-perfiles-top-1` depends on the row order of H2; the order of HANA for `SELECT TOP 1` without `ORDER BY` is NOT proven (D-REQ-8).
* Not observable on H2 / not pinned: that the profile is read twice and that the dead SQL `SelectDiaEsFeriado` is loaded (D-F3A-02); real HANA behaviour (types, scale, concurrency);
  the real `ConsultarEmpresa` (it is scripted); the HTTP router. The IllegalStateException MESSAGE of `toMap` is order dependent (see `determinism-fsa.txt`): only its class is compared.
* No "fresh inputs" run (F2 `make_fresh_*`) was produced for this class yet.

## Do the goldens fail when the behaviour changes? (canary of the goldens, 2026-10-26)

The new code does not exist, so it cannot be mutated yet. What was done instead (as F2 plan section 7, "mutation of goldens"): 10 one-line mutations of a COPY of the legacy
class (`LEGACY_OVERLAY` + `ORACLE_OUT`, nothing under `legacy/` touched) were run through the oracle and their dumps compared with the goldens:
`size() > 1` -> `> 2` (17 cases changed), retention 4 -> 3 (3), `IdMonedaPago` empty check removed (1), rescue state commercial/back-office swapped (5),
FondoExtranjero `N` check removed (8), certification valuta 2 -> 3 (13), `PERFIL_INCORRECTO` code altered (2), empty-list check `size()+1` (3), emisor `equalsIgnoreCase` -> `equals` (1)
and `APO` `equals` -> `equalsIgnoreCase` (0: an EQUIVALENT mutant, the collaborator rejects 'apo' with "Tipo Solicitud no valida" before the difference shows).
When the production code exists, repeat the exercise on it (flip `> 1`, the 4/2 days, the spot condition, `PerfilComercial`) and confirm `FsaEquivalenciaTest` / `FsaReglasTest` go red.
