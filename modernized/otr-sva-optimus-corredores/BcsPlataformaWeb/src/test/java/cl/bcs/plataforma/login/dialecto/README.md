# LOGIN-PG tests: HANA / Postgres dialect layer of the login (T-30..T-36), WITHOUT the equivalence catalog at run time

Characterization tests. The legacy of this change is the CURRENT HANA code of `login/**`: its SQL is the oracle. Nothing here touches a real database: H2 in memory
only (and a recording fake of the JDBC seam), no network, no `.env`.

Decision of the owner (round 3): the HANA to Postgres equivalence table (`resumen_exportacion.xlsx`, the CSV made from it) exists ONLY for development. It is not a
run-time resource: production reads no equivalence CSV or catalog. The Postgres statements are plain resources (`sql/hana/<id>.sql`, `sql/postgres/<id>.sql`,
`sql/indice.txt`); the sequence names and the eight procedure definitions are Java code; the oracle CSVs below are TEST FIXTURES and the tests that use them are the
dev-time verification (A3-6). Contract and open questions: `API_ASSUMED_LOGINPG3.md` (it supersedes the catalog parts of `API_ASSUMED_LOGINPG.md` / `...2.md`).

The production code is reached ONLY through `ApiLoginPg3` (reflection). Without the redesign a test fails with `LOGIN-PG3 not implemented: ...` (the contract demands
something that is missing) or `LOGIN-PG3 catalog still in production: ...` (something that must be gone is still there), and with nothing else.

## What is pinned

| Class | Pins (rule ids: A3-n of API_ASSUMED_LOGINPG3.md; T-n of TODO_MVP.md 10; RULE-076 no credentials) |
|---|---|
| `LoginPg3SinCatalogoTest` | A3-1/A3-3/A3-5: the four catalog classes are gone (classpath and sources), no source names a `.csv` or `equivalencias`, no `equivalencias` folder and no `.csv` under `src/main/resources`, `sql/` holds only `hana/`, `postgres/`, `indice.txt`, 55 files per dialect, `ProcedimientosPostgres` with a no-arg constructor |
| `LoginPgDialectoTest` | A3-2: `SqlDialecto` has exactly `nombre`, `siguienteValor`, `seleccionarSinTabla`, default `exigirHana`; the 11 retired functions are gone; both dialects have one public no-arg constructor; literal text per dialect; Postgres knows two sequences and answers 503 for any other name |
| `LoginPg3SentenciasTest` | A3-3: `SentenciasSql` surface (no `objetosDe`, `texto` / `exigirDisponibles` public non-final); Postgres `texto` reads `sql/postgres/<id>.sql` (proven on the real class in an isolated class loader that replaces or hides one resource: `LoginPg3Aislado`); a missing file is `UnsupportedOperationException` naming id and postgres; `exigirDisponibles` calls `texto` per id; `de()`; the ten object-name ids hold the Postgres name |
| `LoginPg3OraculoSentenciasTest` | A3-6(a): every relation and column of the 45 Postgres texts exists in the oracle, relations equal those of the HANA golden, no column of another relation |
| `LoginPg3SecuenciasTest` | A3-6(c): the two Java sequence constants equal the oracle names and the name files; the six other sequences answer 503 |
| `LoginPg3DefinicionesTest` | A3-6(b): the Java definitions of the 8 procedures equal the QA evidence, proven from the evidence at the JDBC seam: call text, IN/OUT types and positions, legacy answer names, the 18 labels, the 16 composite attributes |
| `LoginPgSinTextoPostgresTest` | A3-3: the 503 end to end with a `SentenciasSql` double `@Primary` (one statement without Postgres text): 503, zero SQL, body names the id; HANA mode untouched |
| `LoginPgSqlHanaTest` | HANA regression net: 55 goldens equal the constants and the SQL of six flows (the code BEFORE the dialect layer); the HANA resources are byte-identical; static `hana(id)`, `predeterminadas()` |
| `LoginPgTraduccionTest` | The 39 table statements in Postgres: no HANA token, exact tables, columns under their Postgres names, aliases = HANA labels, `LIMIT`, derived-table alias, literal texts |
| `LoginPgResultadosH2Test` | Same fake rows in a HANA-shaped and a Postgres-shaped H2 schema (generated from the oracle): same labels and rows for 39 statements; no-FROM select and sequences evaluated by H2 |
| `LoginPgSeleccionTest` | A3-4: property absent -> HANA, `postgres` -> Postgres, invalid -> start fails; one DataSource, no JPA, no catalog bean, `DialectoConfiguracion` declares two beans |
| `LoginPgDespachadoresTest` | The two generic dispatchers answer 503 in Postgres mode before touching a connection; all 55 statements have text; pre-flight |
| `LoginPgOraculoTest`, `LoginPg2CatalogoVistasTest` | A3-6(d): the oracle fixtures equal their manifests (hashes, 365 / 142 / 1722, 148 view columns, name format, scope file of 55 names); a missing fixture FAILS |
| `LoginPg2TraduccionVistasTest`, `LoginPg2VistasH2Test` | The 6 statements over views: no `SELECT *`, `NULLS FIRST`, labels, canonical literal text; same rows in HANA- and Postgres-shaped H2 stand-ins (19 variants) |
| `LoginPg2ProcedimientosTest`, `LoginPg2EventoArreglosTest`, `LoginPg2ServiciosTest` | The JDBC contract of the 8 procedures at `JdbcGrabador` (B2/C2 of `API_ASSUMED_LOGINPG2.md`, unchanged): call text, parameters, cursors, composite arrays, legacy names, 503 for every other procedure of the catalog, error propagation; the real login services over it |
| `LoginPg2SeleccionProcedimientosTest` | `ProcedimientosPorDialecto` selector, the generic dispatcher closed in Postgres, Spring wiring in both modes |
| `LoginPgSeguridadTest`, `LoginPg2SeguridadTest` | No credential, connection string, network call or `.env` in fixtures and tests (RULE-076) |

Helpers: `ApiLoginPg3` (bridge), `LoginPg3Aislado` (class loader that hides or replaces one resource), `LoginPgContexto` (recording DataSource, the `SentenciasSql` double),
`LoginPgRecursos`, `LoginPg2Recursos` (oracles, routines parser, naming rules), `JdbcGrabador` (recording fake of Connection / CallableStatement / PreparedStatement / ResultSet /
Array; it refuses to read an OUT parameter by name, as pgjdbc does), `LoginPgFlujos` / `LoginPgGrabar` (golden recording).

## How to run

```
export JAVA_HOME="C:\\Program Files\\Eclipse Adoptium\\jdk-25.0.4.101-hotspot"   # JDK 25, its bin first in PATH
cd modernized/otr-sva-optimus-corredores/BcsPlataformaWeb
mvn -o test -Dtest='LoginPg*Test'          # the whole LOGIN-PG suite (LoginPg3*, LoginPg2*, LoginPg*)
mvn -o test                                # everything (the 943 pre-existing module tests must stay green)
```

Never two Maven runs at once. Look for these lines in the output: zero cases executed means "not proven".

```
LOGINPG equivalence cases executed: 78          (39 table statements, counted twice: case test and aggregate)
LOGINPG2 view equivalence cases executed: 38    (19 variants of the 6 view statements, counted twice)
LOGINPG3 oracle statement checks executed: 135  (45 statements x 3 checks, one of them also in the aggregate)
LOGINPG3 procedure definition cases executed: 32 (16 cases, counted by the case tests and by the aggregate)
```

A missing fixture is a FAILURE (`FIXTURE MISSING: ...`), never a skip.

## Fixtures

- `src/test/resources/loginpg/`: `oraculo-objetos.csv`, `oraculo-columnas.csv`, `login-alcance.txt`, `ORACULO_MANIFEST.json`, generated from `resumen_exportacion.xlsx` by
  `analysis/otr-sva-optimus-corredores/equivalence/BcsPlataformaWeb/xlsx_a_oraculo.py`. They are the ORACLE of the dev-time checks, not a copy of anything production reads.
  `golden-hana/` (one `<Class>.<CONSTANT>.sql` per constant, byte exact, `INDEX.txt`, `enviado-por-flujo.json`) was recorded from the code BEFORE the dialect layer; never
  regenerate it from refactored code. `seed-equivalencia-hana.sql`, `equivalencia-parametros.json`: fake rows and parameter values of the equivalence tests.
- `src/test/resources/loginpg2/`: `oraculo-vistas-columnas.csv`, `vistas-columnas-pg-qa-20261103.tsv`, `rutinas-y-tipos-postgres-qa-20261103.txt` (Postgres QA catalog, metadata
  only), `ORACULO_VISTAS_MANIFEST.json`, generated by `tsv_a_oraculo_vistas.py`; `vistas-h2.json`: synthetic rows and variants of the view equivalence test.
- `src/test/resources/loginpg3/nombres-postgres-esperados.tsv`: the ten ids that are an object name (8 `SP_*`, 2 `SECUENCIA_*`) with the Postgres name, written by hand (an independent
  second source next to the xlsx).

## How to add a case

1. A new statement: its HANA text in `sql/hana/<id>.sql` and the id in `sql/indice.txt`; the golden (`<id>.sql` + a line in `INDEX.txt`); its Postgres file; if it uses a view or table, nothing else:
   `LoginPg3OraculoSentenciasTest` and the translation tests find it through the golden; add its parameters to `equivalencia-parametros.json` and rows to the seed for the H2 equivalence.
2. A new Postgres statement text: edit `sql/postgres/<id>.sql`; the oracle tests (`LoginPg3OraculoSentenciasTest`, `LoginPgTraduccionTest`, `LoginPg2TraduccionVistasTest`) check it against the
   oracle, nobody regenerates it.
3. A new sequence: a constant in `PostgresDialecto`, its name file, a line in `nombres-postgres-esperados.tsv`; `LoginPg3SecuenciasTest` compares with the xlsx oracle.
4. A new procedure: its signature in the routines evidence, its HANA name in `LoginPg2Recursos.HANA`, a `Caso` in `LoginPg2ProcedimientosTest.casos()`; `LoginPg3DefinicionesTest` picks it up
   from the evidence and the xlsx oracle.
5. A new pgjdbc assumption: a `Q-LOGINPG2-n` / `Q-LOGINPG3-n` in the matching `API_ASSUMED_*` file.
6. New equivalences of names: change the xlsx, rerun the generators, commit the new oracle files and manifests. Nothing under `src/main` changes.

## Proof that the tests can fail and can pass

- Red log (production still has the catalog): `analysis/otr-sva-optimus-corredores/equivalence/BcsPlataformaWeb-LoginPg3-rojo.test-output.txt` and its classification
  `BcsPlataformaWeb-LoginPg3-rojo.clasificacion.txt` (tool `clasifica_rojo3.py`: every red must be `LOGIN-PG3 not implemented` or `LOGIN-PG3 catalog still in production`).
- Mutation: `analysis/.../mutar_loginpg3.py` (Postgres texts, name files, sequence constants, oracles and fixtures, HANA goldens, procedure definitions, `SentenciasSql` code, wiring, and
  forbidden files that are ADDED), run on a COPY of the module that holds the throwaway implementation, never on the repo.
- Missing fixtures: with `src/test/resources/loginpg/`, `loginpg2/` or `loginpg3/` removed from the copy the suite FAILS (never skips); see the notes.
- The throwaway implementation lives OUTSIDE the repo and is not committed (TRANSFORMATION_NOTES.md, "Login dialecto sin catalogo en produccion (ajuste de pruebas)").

## Existing tests changed or removed by round 3

See TRANSFORMATION_NOTES.md, section "Login dialecto sin catalogo en produccion (ajuste de pruebas)": every change with its reason, and the count of removed tests.
