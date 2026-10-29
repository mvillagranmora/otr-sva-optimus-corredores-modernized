# FMM-DET: characterization tests of `ConsultarDetalleSolicitud` (F3 W1 front 4)

Legacy: `legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/ConsultarDetalleSolicitud.java` (367 LOC, read only, 693 uses) and, because DET
only delegates, `ConsultarSolicitudAporteRescateUI.java` (valuation + APV retention + the 270-line SQL) and `CalcularMontoNumeroCuota.java`. Rules:
`analysis/otr-sva-optimus-corredores/RULES_FONDOMUTUO_F3_A.md` section 8.6 (FM-DET-01..04), defects D-F3A-24 / D-F3A-25 and D-NOR-9, matrix section 11 (DET-C01..C04). The production classes do
not exist yet: see `API_ASSUMED.md` for exactly what these tests expect.

## What is here

| File | What |
|---|---|
| `CasoDet`, `SujetoDet` | harness: builds the H2 of a case (F1 schema from `fondomutuo-3a/`, `fondomutuo-det/schema-det.sql` and `seed/base-det.sql`, the case seed), runs the NEW code through `ConsultarDetalleJavaCallHandler`, answers `CalcularParidad` / `EntregarPreciosTasas` from the case script (`Guion`) and returns the same dump the oracle writes |
| `DetEquivalenciaTest` | every case: output text, exception (class; message only for a plain `Exception`), servlet decision, tables, collaborator calls (name, arguments, answer), sequences, session. A missing golden FAILS; prints `equivalence cases executed: N of M` and fails when N = 0 or N != M |
| `DetDeterminismoTest` | N = 20 runs (`-Ddet.corridas=N`) of every case that reaches the valuation or returns several rows, each equal to the golden (the legacy maps and values on `parallelStream`) |
| `DetReglasTest` | one test per card/defect with LITERAL values; names carry `fmDet01..04`, `D-F3A`, `D-NOR` ids and `rule076` |
| `DetCompuertaTest` | closed-by-default gate (503 G0 before anything is read), list `CONSULTAR_DETALLE_SOLICITUD`, allow-list entry, JSON contract of the handler |

Resources (`src/test/resources/fondomutuo-det/`, copied by `sync_det.py`, never edited by hand): `cases/<FMM-DET-nnn_slug>/{case.json,seed.sql,expected.json}`, `schema-det.sql`, `seed/base-det.sql`,
`determinism-det.txt`. Test source created by the same script: `src/test/java/cl/bcs/oracle/H2FnDet.java` (the H2 functions `MAP` and `TO_DECIMAL` of the delegate SQL).

## How to run

Oracle (records the goldens from the UNMODIFIED legacy; JDK 25 + H2 MODE=LEGACY, UTC, fixed clock 2026-03-02 14:30:00; no HANA, no network; compiles into a temp dir; the cases live in `cases-det/`,
NOT in `cases/`, and are passed to the shared `OracleRunner` with `-Doracle.cases`; `runner-det/` holds the H2 functions, the shared runner is NOT modified):

    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/make_cases_det.py       # inputs + schema-det.sql + seed/base-det.sql (deletes the old cases-det/FMM-DET-* and their goldens)
    bash analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/record_det.sh FMM-DET   # 20 runs per case; exit 4 = a case is unstable (no golden written)
    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/sync_det.py          # copies the cases that have a golden (and H2FnDet) into this module

Tests (only once the production classes of `API_ASSUMED.md` exist; until then this package does not compile and that is expected). Run them in a JVM with the zone UTC and language en
(`-Duser.timezone=UTC -Duser.language=en`):

    mvn -pl BcsFondoMutuoServicios test -Dtest='Det*Test'

## How to add a case

1. Add a `caso(slug, ref, entrada(id), [sol(id, ...)], seed=[...], colaboradores=valorizada(id))` call to `equivalence/BcsFondoMutuoServicios/cases_det_a.py` AT THE END of its function (numbers follow
   call order). `sol(...)` writes a `FMM.SolicitudAporteRescate` row with EVERY column (docstring in the file for the defaults); the collaborators of the valuation are scripted with `precios(...)`,
   `paridad(t1=..., t2=...)`, `uf(...)` and `valorizada(id)` of `cases_det_lib.py` (a case that reaches them without a script FAILS loudly: "no canned answer configured"). A new table or column goes into `NUEVAS` /
   `AGREGADAS` of `cases_det_lib.py`. Never write `expected.json` by hand.
2. Run the three commands above. Read `determinism-det.txt`: an UNSTABLE case has no golden and the test fails until its cause is understood.
3. Pin it with a literal in `DetReglasTest` when it documents a rule, and give it the card/defect id in the `ref` text.

## Assumptions and limits (read before trusting a green run)

* **The delegate SQL runs for real on H2 over stand-in tables** (`schema-det.sql`): 22 tables / views of 8 modules the F1 schema lacks (25 columns have a REAL type from the catalog CSVs, 50 are ASSUMED from the pojo
  field) and 8 columns added to existing tables (all REAL). The SQL file is verbatim; its joins are exercised only as far as the case data goes: the Tesoreria, Spot, Facturacion, APV transfer, recaudacion and
  omnibus branches are covered by a few rows each, not by their real semantics. `MAP(x, NULL, 0, x)` is implemented as "NULL matches NULL" (ASSUMED, HANA not verified) and `TO_DECIMAL(0,26,8)` as `setScale(8)`.
* `CalcularParidad` and `EntregarPreciosTasas` are scripted doubles on both sides (their real behaviour is Plataforma's); `CalcularMontoNumeroCuota`, the delegate and DET run for real in the oracle. Q-F3A-6 (that the
  collaborators answer with the `IdInterno` they received) is an assumption of the script.
* The legacy is the oracle: D-F3A-24 (a "query" that runs valuations; an ING with an unknown series is overwritten with NULLs and, with retention, raises `NullPointerException`; an ING repeated by a JOIN raises
  `IllegalStateException`), the scale-sensitive and rounding rules of the valuation (D-CUO-*), `APV.PorcentajeRetencion` read as a `Long` ('12.5' => 0) are kept and listed in the report, not fixed.
* **JVM dependent text**: `java.sql.Timestamp` is written with the default Gson format (U+202F before `AM/PM` on JDK 20+; the goldens are JDK 25, the legacy in production is JDK 11) and `java.sql.Date` as `MMM d, yyyy`
  (the year-1 default date prints `Jan 3, 0001`); both in the JVM zone when the Gson is created (`API_ASSUMED.md`: a new `Gson` per call).
* **DESVIO-KIT-1**: two cases (empresa nula con id, empresa sin configuracion) are NPE in the legacy and `ParametroNoEncontradoException` in the kit: accepted explicitly for those slugs only.
* The `IllegalStateException` message of `Collectors.toMap` is order dependent (`MESSAGE-ONLY-UNSTABLE` in `determinism-det.txt`): only its class is compared.
* Not observable on H2 / not pinned: real HANA behaviour (types, scale, the real joins), whether the vigent implementation is this class or the HANA procedure (settled: the class, plan section 10 Q-F3A-1), the HTTP router.
  No "fresh inputs" run (F2 `make_fresh_*`) was produced for this class yet.

## Do the goldens fail when the behaviour changes? (canary of the goldens, 2026-10-26)

The new code does not exist, so it cannot be mutated. What was done (as FSA and F2 plan section 7, "mutation of goldens"): 9 one-line mutations of a COPY of the legacy classes
(`legacy-oracle/canary_sfm_det.py`: `LEGACY_OVERLAY` + `ORACLE_OUT`, one repetition, nothing under `legacy/` touched, the goldens not rewritten) were run through the oracle and their dumps compared with the goldens.
**All 9 change at least one golden** (cases that change): PEN not valued (1), APV-B not retaining (18), the percentage divisor 100 -> 10 (13), the investment-account payment also retaining (1),
`RESNORM` case insensitive (1), the valued amount taken from `MontoDestino` (8), id 0 not rejected (3), the output amount from `MontoMonedaSistema` (62) and the Spot state copied from the wrong field (3).
(The matrix of this front had 97 cases when the canary ran and 100 after adding the three `MontoMonedaSistema` cases.) Run `py -3 -I legacy-oracle/canary_sfm_det.py det` to repeat it.

Besides, to prove that these tests COMPILE and CAN PASS and FAIL, a throwaway reference implementation of the assumed API was written OUTSIDE the module (a temporary copy of it, never delivered, nothing in `src/main`):
the whole package ran green there (`DetEquivalenciaTest` 100 of 100 executed, `DetDeterminismoTest` 39 cases x 20 runs, `DetReglasTest` 32, `DetCompuertaTest` 7) and two one-line mutations of that implementation
(retention divisor 100 -> 10, PEN not valued) turned 27 and 14 tests red. When the real production code exists, repeat the exercise on it (the retention conditions and divisor, the `ING`/`PEN` filter, the
valuation date, the `p_SolicitudesAporteRescate == 0` guard) and confirm `DetEquivalenciaTest` / `DetReglasTest` go red.
