# FMM-SFM: characterization tests of `ConsultarSolicitudesFFMM` and its twin `ConsultarSolicitudesFFMMWS` (F3 W1 front 3)

Legacy: `legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/ConsultarSolicitudesFFMM.java` (478 LOC) and `...WS.java` (485 LOC),
read only, 4,261 uses each. Rules: `analysis/otr-sva-optimus-corredores/RULES_FONDOMUTUO_F3_A.md` section 8.3 (FM-SFM-01..08, the WS differences are FM-SFM-07/08), defects D-F3A-08, 09,
10, 25, 28 and D-NOR-9 / D-NOR-10, matrix section 11 (SFM-C01..C08, WS-C01..C03). The production classes do not exist yet: see `API_ASSUMED.md` for exactly what these tests expect.

## What is here

| File | What |
|---|---|
| `CasoSfm`, `SujetoSfm` | harness: builds the H2 of a case (F1 schema from `fondomutuo-3a/`, `fondomutuo-sfm/schema-sfm.sql` and `seed/base-sfm.sql`, the case seed), runs the NEW code through the handler of the class the case names (`servicio` = `ConsultarSolicitudesFFMM` or `ConsultarSolicitudesFFMMWS`) and returns the same dump the oracle writes |
| `SfmEquivalenciaTest` | every case (325: 160 SFM + 165 WS): output text, exception (class; message only for a plain `Exception`), servlet decision, tables, collaborator calls, sequences, session. A missing golden FAILS; prints `equivalence cases executed: N of M` and fails when N = 0 or N != M |
| `SfmDeterminismoTest` | N = 20 runs (`-Dsfm.corridas=N`) of every case that seeds at least 4 rows of the view (88 cases, `-Dsfm.minFilas`), each equal to the golden (the legacy uses `queryParallel` and `parallelStream` everywhere) |
| `SfmReglasTest` | one test per card/defect with LITERAL values (46 tests); names carry `fmSfm01..08`, `D-F3A`, `D-NOR` ids and `rule076` |
| `SfmCompuertaTest` | closed-by-default gate (503 G0 before anything is read), separate lists `CONSULTAR_SOLICITUDES` / `CONSULTAR_SOLICITUDES_WS`, allow-list entries, JSON contract of the handlers |

Resources (`src/test/resources/fondomutuo-sfm/`, copied by `sync_sfm.py`, never edited by hand): `cases/<FMM-SFM-nnn_slug>/{case.json,seed.sql,expected.json}`, `schema-sfm.sql`,
`seed/base-sfm.sql`, `determinism-sfm.txt` (the 20-run report of the oracle). Every scenario exists TWICE with the same data: `slug` (SFM) and `ws-slug` (WS); `ws-` only
scenarios (`ws-fecha-ingreso-local-*`, `ws-offset-invalido-se-lee-igual`) have no SFM twin.

## How to run

Oracle (records the goldens from the UNMODIFIED legacy; JDK 25 + H2 MODE=LEGACY, UTC, fixed clock 2026-03-02 14:30:00; no HANA, no network; compiles into a temp dir; the cases live in
`cases-sfm/`, NOT in `cases/`, and are passed to the shared `OracleRunner` with `-Doracle.cases`, so no other record script can run them or touch their goldens):

    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/make_cases_sfm.py       # inputs + schema-sfm.sql + seed/base-sfm.sql (deletes the old cases-sfm/FMM-SFM-* and their goldens)
    bash analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/record_sfm.sh FMM-SFM   # 20 runs per case; exit 4 = a case is unstable (no golden written)
    py -3 -I analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/sync_sfm.py          # copies the cases that have a golden into this module

Tests (only once the production classes of `API_ASSUMED.md` exist; until then this package does not compile and that is expected). Run them in a JVM with the zone UTC and language en
(`-Duser.timezone=UTC -Duser.language=en`, as the goldens were recorded; the harness also switches the zone around each case):

    mvn -pl BcsFondoMutuoServicios test -Dtest='Sfm*Test'

## How to add a case

1. Add a `par(slug, ref, entrada(...), [fila(...), ...], seed=[...])` call to `equivalence/BcsFondoMutuoServicios/cases_sfm_a.py` or `cases_sfm_b.py` AT THE END of its function (numbers follow call order;
   `par` writes the SFM case and its WS twin). Rows: `fila(NumSolicitud, Columna=valor, ...)` of `cases_sfm_lib.py` (docstring: default row; `None` = SQL NULL; `offset=` only for the default
   `FechaIngresoLocal`). Never write `expected.json` by hand.
2. Run the three commands above. Read `determinism-sfm.txt`: an UNSTABLE case has no golden and the test fails until its cause is understood.
3. Pin it with a literal in `SfmReglasTest` (add a test, or an entry to its `FILTROS` / `ESTADOS` tables) when it documents a rule, and give it the card/defect id in the `ref` text.

## Assumptions and limits (read before trusting a green run)

* **The view is a stand-in.** `FMM.ConsultarSolicitudesFFMM` is not in the catalog CSVs (Q-F3A-2): on H2 it is a TABLE of the same name with one column per field the SQL selects or filters and ASSUMED types
  (text where the pojo field is `String`: `FechaSolicitud` and `HoraSolicitud` MUST be text in HANA or the legacy fails with `IllegalArgumentException` in `SQL.mapLenientHanaField`; `DECIMAL(26,8)` for amounts:
  the scale 8 appears in the goldens). The SQL is verbatim; what HANA computes inside the view (`TipoMovimiento`, `EstadoDetalle`, `FechaIngresoLocal`) is NOT exercised. The three Tesoreria tables carry only
  the columns the WS SQL reads.
* The legacy is the oracle: D-F3A-08 (mixed states vanish: the card example INGRESADO+TERMINADO+ANULADO has state `''` and is NOT visible in the output; the amounts are only observable in non-empty states),
  D-F3A-09 (the hour wraps and the date does not), D-F3A-10 (the WS multiplies rows and counts), D-NOR-10 (the offset is a static of the process, set on every call: not observable through one request) are kept and
  listed in the report, not fixed.
* **D-F3A-28** (`"IdSerieFondo" as "IdSerieFondoOrigen"`): on H2 the legacy fills `IdSerieFondo` and leaves `IdSerieFondoOrigen` null; the HANA driver may report the alias instead. The golden is H2's. See `API_ASSUMED.md`.
* **JVM dependent text**: the WS writes `FechaIngresoLocal` with the default Gson date format; with JDK 20+ the time part has U+202F before `AM/PM` (the goldens are JDK 25; the legacy in production runs JDK 11 and writes an
  ordinary space). Pinned by `fmSfm07e_fechaIngresoLocalConFormatoGson`; the owner decides whether the consumer of the WS tolerates it.
* **DESVIO-KIT-1**: three cases (empresa nula / sin configuracion / ausente) are NPE in the legacy and `ParametroNoEncontradoException` in the kit: accepted explicitly for those slugs only (see `API_ASSUMED.md`).
* The IllegalState / NPE MESSAGE text of a parallel stream is order dependent (one case is `MESSAGE-ONLY-UNSTABLE` in some runs): only the class is compared.
* Not observable on H2 / not pinned: real HANA behaviour (types, scale, the view itself, `queryParallel` BETA behaviour, concurrency, the true row order of the view: the SQL has no `ORDER BY`, H2 returns insertion order),
  the HTTP router. No "fresh inputs" run (F2 `make_fresh_*`) was produced for this class yet.

## Do the goldens fail when the behaviour changes? (canary of the goldens, 2026-10-26)

The new code does not exist, so it cannot be mutated. What was done (as FSA and F2 plan section 7, "mutation of goldens"): 11 one-line mutations of a COPY of the legacy classes
(`legacy-oracle/canary_sfm_det.py`: `LEGACY_OVERLAY` + `ORACLE_OUT`, one repetition, nothing under `legacy/` touched, the goldens not rewritten) were run through the oracle and their dumps compared with the goldens.
**All 11 change at least one golden** (cases that change): `enProceso > 0` -> `> 1` (16), the zone offset not applied to the hour (104), committed units of a non-REV from the destination (86),
the rows with empty state kept (14), `esFiltro(Long)` with 1 instead of 0 (114), the ADC parameter case insensitive (1), `FolioContrato` 0 also associating (29), `'rev'` in lower case as REV (1), the entry-date
separator (106), WS hour without seconds (6) and WS keeping the view date instead of the local one (15). Run `py -3 -I legacy-oracle/canary_sfm_det.py sfm` to repeat it.

Besides, to prove that these tests COMPILE and CAN PASS and FAIL, a throwaway reference implementation of the assumed API was written OUTSIDE the module (a temporary copy of it, never delivered, nothing in `src/main`):
the whole package ran green there (`SfmEquivalenciaTest` 325 of 325 executed, `SfmDeterminismoTest` 88 cases x 20 runs, `SfmReglasTest` 46, `SfmCompuertaTest` 6) and four one-line mutations of that implementation
(`enProceso > 1`, offset not applied, `esFiltro(Long)` with 1, WS hour without seconds) turned 29, 145, 179 and 9 tests red. When the real production code exists, repeat the exercise on it (flip the
`EN PROCESO` threshold, the `FolioContrato` 0 guard, the offset, the `INGGTA` exclusion, the WS hour format) and confirm `SfmEquivalenciaTest` / `SfmReglasTest` go red.
