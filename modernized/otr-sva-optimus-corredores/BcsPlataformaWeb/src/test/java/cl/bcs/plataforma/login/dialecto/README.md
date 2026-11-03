# LOGIN-PG tests: HANA / Postgres dialect layer of the login MVP (T-30, T-31, T-32, T-35)

Characterization tests written BEFORE the production code (red phase). The legacy of this change is the CURRENT HANA code of `login/**`: its SQL is the oracle.
The Postgres catalog is not reachable yet, so nothing here touches a real database: H2 in memory only, no network, no `.env`.

## What is pinned

| Class | Pins | State without production code |
|---|---|---|
| `LoginPgSqlHanaTest` | The goldens in `src/test/resources/loginpg/golden-hana` equal the constants of the six classes and the SQL their flows send (green); the HANA dialect resources give byte-identical text per statement (red) | part A green, part B red |
| `LoginPgDialectoFuncionesTest` | Literal text of every `SqlDialecto` function per dialect, n=1, `:name` kept, unknown names | red |
| `LoginPgCatalogoTest` | The oracle derived from the xlsx (green); production CSVs and `CatalogoEquivalencias` against it; NO_DISPONIBLE, duplicates, format, unknown names on CSV text built in the test (red) | oracle green, rest red |
| `LoginPgTraduccionTest` | The 39 table-only statements in Postgres: no HANA token, exact tables, columns, aliases equal to the HANA names, `LIMIT`, derived-table alias, literal texts | scope green, rest red |
| `LoginPgResultadosH2Test` | Same fake rows in a HANA-shaped and a Postgres-shaped (flat snake_case, generated from the oracle) H2 schema; same labels and rows for each statement; dialect functions evaluated by H2 | fixture self-checks green, comparisons red |
| `LoginPgSeleccionTest` | Property absent -> HANA, `postgres` -> Postgres, invalid -> start fails with a clear message; one DataSource, no JPA | red (green: no JPA, default declared) |
| `LoginPgNoDisponibleTest` | Postgres: an object NO_DISPONIBLE answers 503 with ZERO SQL on the connection, also when it is the last object of the page; never HANA; HANA mode untouched; the existing `GlobalExceptionHandler` maps the exception to 503 | red |
| `LoginPgSeguridadTest` | No credential, connection string, network call or `.env` in fixtures and tests | green |

Rule ids: this change pins no business rule of BUSINESS_RULES.md; the tests carry the task ids (`T-30`..`T-35`) and `RULE-076` (no credentials) in their display names.

## How to run

```
export JAVA_HOME="C:\\Program Files\\Eclipse Adoptium\\jdk-25.0.4.101-hotspot"   # JDK 25, its bin first in PATH
cd modernized/otr-sva-optimus-corredores/BcsPlataformaWeb
mvn -o test -Dtest='LoginPg*Test'          # the whole LOGIN-PG suite
mvn -o test                                # everything (the 163 existing tests must stay green)
```

Never two Maven runs at once. Look for the lines `LOGINPG equivalence cases executed: N` and `LOGINPG statements exercised by the flows: ...` in the output:
zero cases executed means "not proven".

A missing fixture is a FAILURE (`FIXTURE MISSING: ...`), never a skip. A missing production class is a failure with
`LOGIN-PG not implemented: the production class ... does not exist yet`.

## Fixtures (`src/test/resources/loginpg/`)

- `oraculo-objetos.csv`, `oraculo-columnas.csv`, `login-alcance.txt`, `ORACULO_MANIFEST.json`: generated from `resumen_exportacion.xlsx` by
  `analysis/otr-sva-optimus-corredores/equivalence/BcsPlataformaWeb/xlsx_a_oraculo.py` (standard library only):
  `python -X utf8 xlsx_a_oraculo.py <xlsx> <this dir> <src/main/java>`. Do NOT reuse this script to produce the production CSV: the oracle must stay independent.
- `golden-hana/`: one `<Class>.<CONSTANT>.sql` per constant (byte exact), `INDEX.txt`, `enviado-por-flujo.json` (the SQL each flow sends). Recorded from the code BEFORE the
  dialect layer with `mvn -o test -Dtest='LoginPgSqlHanaTest#golden_cubre_exactamente_las_constantes' -Dloginpg.grabar=<dir>`. Never regenerate them from refactored code:
  they are the oracle. If a statement of the HANA code changes on purpose, record why in the commit.
- `seed-equivalencia-hana.sql`, `equivalencia-parametros.json`: fake rows and the value of every `:name` parameter per statement for the equivalence tests.

## How to add a case

1. A new SQL constant in one of the six classes: add it to the golden with the recording command above (on the pre-change code) or by hand (`<id>.sql` + a line in `INDEX.txt`),
   add its parameters to `equivalencia-parametros.json` and, if it writes, the tables to dump under `volcar_tras_escribir`, add its rows to the seed.
   `LoginPgTraduccionTest.CON_VISTAS` lists the statements over views that are not yet translatable.
2. A new dialect function: add it to `ApiLoginPg.f` callers in `LoginPgDialectoFuncionesTest` with its literal text per dialect and, if H2 can evaluate it, a case in
   `LoginPgResultadosH2Test.funciones_dan_el_mismo_valor`.
3. A new equivalence of names: change the xlsx, rerun the generator, commit the new oracle files and the manifest.

## Proof that the tests can fail and can pass

- Red log: `analysis/otr-sva-optimus-corredores/equivalence/BcsPlataformaWeb-LoginPg-rojo.test-output.txt`.
- Mutation of the oracle, the goldens and a throwaway implementation: `analysis/otr-sva-optimus-corredores/equivalence/BcsPlataformaWeb/mutar_loginpg.py`
  (run on a COPY of the module that holds the throwaway implementation, never on the repo).
- The throwaway implementation lives OUTSIDE the repo (scratch copy of the module); it is not committed. See TRANSFORMATION_NOTES.md, section "Login dialecto HANA/Postgres (pruebas)".
- API assumptions and open questions: `API_ASSUMED_LOGINPG.md`.
