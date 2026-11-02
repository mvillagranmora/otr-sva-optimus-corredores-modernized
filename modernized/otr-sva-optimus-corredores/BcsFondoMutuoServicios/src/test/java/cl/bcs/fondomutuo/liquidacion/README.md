# Tests of F3 W3 T6-B: `LiquidacionSolicitudAporteRescate`

Characterization tests of the legacy class `cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate` (765 lines: the select of the solicitudes to liquidate, the omnibus filter, the grouping by `Grupo`, the lines of Tesoreria of
the AGF and of the client (APV), the withholding APV-B, the call to Tesoreria, the UPDATE to liquidated, the cargos / abonos of the client's account and the log). Only `logica` is ported (RULE-076: never `main`). The legacy
code is the oracle: the goldens were recorded from the REAL class on H2 (`analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/record_liq.sh`), never derived from reading it. Rule ids: the cards
`FM-LIQ-01..20` of `analysis/otr-sva-optimus-corredores/RULES_FONDOMUTUO_F3_B.md` 8.3 and the defects `D-F3B-nn` (9); `BUSINESS_RULES.md` has no `RULE-nnn` for this class.

Decision of the user: a direct JavaCall handler (registry id 1689), CLOSED by default (own list `LIQUIDACION_SOLICITUD_APORTE_RESCATE`, G0 first, whitelist 13 -> 14, not a read class), following T4-C.

**State: RED BEFORE CODE.** The production package `cl.bcs.fondomutuo.liquidacion` does not exist yet. The tests compile (the new classes are reached by reflection in `ApiT6b`, the only file that knows their names) and fail with
`AssertionError: T6-B not implemented: the production class ... does not exist yet`. What production must provide: `API_ASSUMED_T6B.md` (this directory).

## Files

| File | What it does |
|---|---|
| `ApiT6b.java` | the reflection bridge to the production classes; fakes of the port and of the data port |
| `API_ASSUMED_T6B.md` | the API production must meet, the decisions, the existing tests that will conflict, the open questions |
| `CasoLiq`, `CosturaLiq`, `ConexionLiq` | the harness (H2, the production facade over the real JDBC adapter, fakes of the three collaborators answered by the scripted doubles of the case), the comparison and the H2 dialect bridge |
| `LiqEquivalenciaTest` | every recorded case (156) through the production handler, compared as a whole with its golden; a missing golden FAILS; prints `equivalence cases executed: N` |
| `LiqDeterminismoTest` | the oracle report (20 runs per case, 0 unstable) and N = 20 runs of the new code on every case where the order of the groups or repeated passes matter |
| `LiqReglasTest` | the rules with literals read in the goldens; works against the golden itself with `-Dliq.autocomprobacion=true` |
| `LiqPuertoTest` | the facade in isolation (order of reads and calls, arguments of the data port, typed entry of Tesoreria, retention, K of the CIN branch, no recount, references of the message types, exceptions unchanged) |
| `LiqHandlerTest` | the handler: shape, FQN, whitelist 14, lists, G0 first, the codec with nulls, the recount of the errors by reference, exceptions unchanged |
| `LiqEstructuraTest` | port = interface, facade without state and only ports, verbatim SQL (SHA-256 of five files), the adapter writes only the batch UPDATE, G0 is the first statement, the wiring, LF, RULE-076 |
| `LiqCableadoTest` | Spring context: the bean exists, ONE, over the beans of the context; the handler is served closed (503); no second bean of the three collaborators; the validator and the start-up |

Fixtures: `src/test/resources/fondomutuo-liq/` (`cases/<id>/{case.json,seed.sql,expected.json}`, `schema-liq.sql`, `seed/base-liq.sql`, `determinism-liq.txt`), copied by `legacy-oracle/sync_liq.py`.

## Run

```bash
export JAVA_HOME="C:\\Program Files\\Eclipse Adoptium\\jdk-25.0.4.101-hotspot"; export PATH="$JAVA_HOME/bin:$PATH"
cd modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios
mvn -o test -Dtest='Liq*Test'                                     # the whole step (never two Maven at once)
mvn -o test -Dtest='LiqReglasTest' -Dliq.autocomprobacion=true    # self-check: the rules against the goldens themselves (no production code runs)
mvn -o test -Dtest='LiqDeterminismoTest' -Dliq.corridas=5         # fewer runs while developing (default 20)
```

## Add a case

1. Edit `analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/cases_liq.py` (`caso(slug, refs, entrada, seed=[...], colaboradores=...)`; fake data only, never a real id or credential).
2. `python -X utf8 make_cases_liq.py` (rewrites `cases-liq/` without goldens), then `PYTHON_CMD="python -X utf8" legacy-oracle/record_liq.sh FMM-LIQ` (20 runs per case on the REAL legacy; an unstable case gets no golden and is listed in `determinism-liq.txt`).
3. `python -X utf8 -I legacy-oracle/check_liq.py` (tags and goldens consistent, cards and quirks shown by the goldens), then `python -X utf8 -I legacy-oracle/sync_liq.py`.
4. `LiqReglasTest` addresses cases by SLUG (`CasoLiq.idPorSlug`), so a renumbering never misleads it; append new cases at the end of `generar()` anyway.
5. Golden mutation: `python -X utf8 -I canary_liq.py` (mutates a COPY of the legacy through `LEGACY_OVERLAY` / `LEGACY_OVERLAY_RES`; lists killed / survived).

A comparison that cannot run is a failure, never a skip: a missing golden, schema or oracle report FAILS, and `equivalence cases executed: 0` is "not proven".
