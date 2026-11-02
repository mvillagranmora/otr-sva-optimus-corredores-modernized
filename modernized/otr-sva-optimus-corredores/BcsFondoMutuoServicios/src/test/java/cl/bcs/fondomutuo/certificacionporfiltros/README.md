# Tests of F3 W3 T6-A: `CertificarPorFiltrosSolicitudAporteRescate` (in-process only)

Characterization tests of the legacy class `cl.bcs.fondomutuo.CertificarPorFiltrosSolicitudAporteRescate` (154 lines: one `SELECT DISTINCT` per filter, the ids accumulated in the order of the filters
WITHOUT de-duplication, then the REAL `CertificarSolicitudAporteRescate.logica` once, whose counter / messages / results are copied). The legacy code is the oracle: the goldens were recorded from the real
class on H2 (`analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/legacy-oracle/record_cerpf.sh`), never derived from reading it. Rule ids: the cards `FM-CXF-01..06` of
`analysis/otr-sva-optimus-corredores/RULES_FONDOMUTUO_F3_B.md` 8.2 and the matrix `FM-CXF-C01..C06` (11.2); `BUSINESS_RULES.md` has no `RULE-nnn` for this class and `RULE_REVIEWS.json` has no verdict on it.

Decision of the user: T6-A is IN-PROCESS ONLY (a job of another module calls it): no JavaCall handler, no FQN in `JavaCallDispatcher.LISTA_BLANCA`, no list in `listas-servidas`.

**State: RED BEFORE CODE.** The production package `cl.bcs.fondomutuo.certificacionporfiltros` does not exist yet. The tests compile (the new classes are reached by reflection in `ApiT6a`, the only file that knows
their names) and fail with `AssertionError: T6-A not implemented: the production class ... does not exist yet`. What production must provide: `API_ASSUMED_T6A.md` (this directory).

## Files

| File | What it does |
|---|---|
| `ApiT6a.java` | the reflection bridge to the production classes; fakes of the read port; the JSON codec of the legacy `call` (plain Gson) |
| `API_ASSUMED_T6A.md` | the API production must meet, the decisions and the open questions |
| `CerPfEquivalenciaTest` | every recorded case (86) through the production port over the real JDBC adapter, compared as a whole with its golden; a missing golden FAILS; prints `equivalence cases executed: N` |
| `CerPfDeterminismoTest` | the oracle report (20 runs per case, 0 unstable) and N = 20 runs of the new port on every case with several filters or several rows |
| `CerPfReglasTest` | the rules with literals read in the goldens (the ids that reach Certificar, header, output, exceptions); works against the golden itself with `-Dcer.autocomprobacion=true` |
| `CerPfPuertoTest` | unit rules with a faked select and a faked Certificar (one filter one query one call, duplicates kept, empty filters, nulls forwarded, output copied, exceptions unchanged, plain Gson, contract) |
| `CerPfEstructuraTest` | in-process only (no handler, not in the allow-list, no list), port = interface, read-only adapter (verbatim SQL, SHA-256), LF, RULE-076 |
| `CerPfCableadoTest` | Spring context: the bean exists, ONE, over the bean of Certificar and the JDBC adapter; it is not served; no list |
| `../certificacion/CasoCerPf.java`, `CerCosturaPf.java` | the harness and the comparison (own copies of `CasoCerT5` / `CerCosturaT5`; in the package `certificacion` only because the pieces they reuse are package-private there) |

Fixtures: `src/test/resources/fondomutuo-cerpf/` (`cases/<id>/{case.json,seed.sql,expected.json}`, `schema-cerpf.sql`, `determinism-cerpf.txt`), copied by `legacy-oracle/sync_cerpf.py`.

## Run

```bash
export JAVA_HOME="C:\\Program Files\\Eclipse Adoptium\\jdk-25.0.4.101-hotspot"; export PATH="$JAVA_HOME/bin:$PATH"
cd modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios
mvn -o test -Dtest='CerPf*Test'                                   # the whole step (never two Maven at once)
mvn -o test -Dtest='CerPfReglasTest' -Dcer.autocomprobacion=true  # self-check: the rules against the goldens themselves (no production code runs)
mvn -o test -Dtest='CerPfDeterminismoTest' -Dcer.corridas=5       # fewer runs while developing (default 20)
```

## Add a case

1. Edit `analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios/cases_cerpf.py` (a filter case with `caso(...)`, a Certificar case with `desde_t5(...)`; fake data only, never a real id or credential).
2. `python -X utf8 make_cases_cerpf.py` (rewrites `cases-cerpf/` without goldens), then `legacy-oracle/record_cerpf.sh FMM-CERPF` (20 runs per case on the REAL legacy; an unstable case gets no golden and is listed in `determinism-cerpf.txt`).
3. `python -X utf8 -I legacy-oracle/check_cerpf.py` (tags and goldens consistent, cards covered), then `python -X utf8 -I legacy-oracle/sync_cerpf.py`.
4. The numbering is deterministic: `CerPfReglasTest` addresses cases by number (`CasoCerPf.id(n)`); append new cases at the END of `generar()` so no number moves.
5. Golden mutation: `python -X utf8 -I canary_cerpf.py` (mutates a COPY of the legacy through `LEGACY_OVERLAY`; lists killed / survived).
