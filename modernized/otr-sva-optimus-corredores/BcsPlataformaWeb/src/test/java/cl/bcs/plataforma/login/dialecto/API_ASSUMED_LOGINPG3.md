# API assumed by the LOGIN-PG3 tests (dialect layer WITHOUT the equivalence catalog at run time)

Decision of the owner (not open for discussion): the HANA to Postgres equivalence table (`resumen_exportacion.xlsx` and the CSV made from it) exists ONLY for
development. It is NOT a run-time resource of the project: production code reads no equivalence CSV and no catalog at run time. The Postgres statements stay as
SOURCE (`src/main/resources/sql/hana/<id>.sql`, `sql/postgres/<id>.sql`, `sql/indice.txt`: generated once with the table, reviewed by hand, then they are the
source); everything else that used the catalog is plain Java. The equivalence data stays in development: the oracle CSVs are test fixtures
(`src/test/resources/loginpg/oraculo-*.csv`, `loginpg2/oraculo-vistas-columnas.csv`, the Postgres QA evidence files) and the tools in
`analysis/otr-sva-optimus-corredores/equivalence/BcsPlataformaWeb/`.

This file SUPERSEDES the catalog parts of `API_ASSUMED_LOGINPG.md` and `API_ASSUMED_LOGINPG2.md` (the void assumptions are listed at the end). The tests reach the
production code that does not exist yet ONLY through `ApiLoginPg3.java` (reflection; it replaces `ApiLoginPg` and `ApiLoginPg2`), so they COMPILE before the
redesign and fail with `LOGIN-PG3 not implemented: ...` (something the contract demands is missing) or `LOGIN-PG3 catalog still in production: ...` (something that
must be GONE is still there). If the real API differs, change `ApiLoginPg3.java` and this file, not the tests. Everything below is the contract the owner approved;
the lines marked "(my reading)" are details the owner did not spell out and are open questions at the end.

## A3. Production contract

| # | Contract | Pinned by |
|---|---|---|
| A3-1 | GONE from production: the classes `CatalogoEquivalencias`, `LectorCsv`, `FichaDeColumna`, `ObjetoNoDisponibleException` (and any procedure-definition class that reads a CSV); no `equivalencias` folder and no `.csv` (nor `.xlsx`) anywhere under `src/main/resources`; no string literal naming a `.csv` or `equivalencias` in `src/main/java`; under `sql/` only `hana/`, `postgres/` and `indice.txt`. | `LoginPg3SinCatalogoTest`, `LoginPgSeleccionTest` (no bean of those types) |
| A3-2 | `SqlDialecto` is minimal: `String nombre()`, `String siguienteValor(String secuenciaHana)`, `String seleccionarSinTabla(String expresion)` and `default void exigirHana(String que)` (unchanged guard). Gone: `objeto`, `columna`, `columnaSeleccion`, `nombreDe`, `ahoraUtc`, `primeros`, `sumarSegundos`, `fechaATexto`, `aEntero`, `recortar`, `exigirDisponible`. `HanaDialecto` and `PostgresDialecto` have ONE public constructor, without parameters, and no public method beyond the interface. `siguienteValor`: HANA unchanged (`"<name>".NEXTVAL`, any name); Postgres knows only `BSCL.Plataforma.BD.Secuencias::Log` -> `nextval('sq_pla_log')` and `BSCL.Plataforma.BD.Secuencias::DetalleLog` -> `nextval('sq_pla_detalle_log')` as Java constants; any other name (the other six sequences of the catalog included, and the Postgres names, other case, spaces, empty) is `UnsupportedOperationException` whose message contains the name (503 by the existing `GlobalExceptionHandler`). `seleccionarSinTabla` unchanged. | `LoginPgDialectoTest`, `LoginPg3SecuenciasTest` |
| A3-3 | `SentenciasSql(SqlDialecto)` keeps `ids()`, `texto(id)`, `de(hanaText)`, `exigirDisponibles(Collection<String>)` (calls `texto` on each id, in order), `dialecto()`, static `hana(id)`, static `predeterminadas()` (the HANA statements). `texto` and `exigirDisponibles` are public NON-final (a test double overrides them); `objetosDe` is GONE. Postgres `texto(id)` reads `/sql/postgres/<id>.sql` and returns its text; when the file does not exist it throws `UnsupportedOperationException` whose message contains the id and `postgres` (replaces the NO_DISPONIBLE mark: no text = 503, nothing is sent). HANA `texto(id)` reads `/sql/hana/<id>.sql` and is byte-identical to the 55 goldens. For the 10 ids that are an object NAME (the 8 `SP_*`, the 2 `SECUENCIA_*`) `sql/postgres/<id>.sql` holds just the Postgres name on one line. `sql/hana` and `sql/postgres` have exactly one `<id>.sql` per id of the index (55 each). | `LoginPg3SentenciasTest`, `LoginPgSqlHanaTest`, `LoginPg3SinCatalogoTest` |
| A3-4 | `DialectoConfiguracion` declares the beans `SqlDialecto` and `SentenciasSql` only (no `CatalogoEquivalencias` bean). The property rules are unchanged: `plataforma.db.dialect` absent or `hana` is HANA, `postgres` is Postgres, anything else stops the start with a message that names the property, the value and both valid values. | `LoginPgSeleccionTest` |
| A3-5 | `ProcedimientosPostgres` has ONE public constructor, without parameters. The definitions of the 8 procedures (Postgres name, kind, parameters with position / mode / HANA-legacy name / Postgres name / type, the 18 labels of `fn_pla_consultar_formatos`, the attributes of the four composite types of `sp_pla_evt_agregar_evento`) are Java code, not a file. The JDBC contract B2/C2 of `API_ASSUMED_LOGINPG2.md` is UNCHANGED (escape call, IN/OUT/INOUT, cursor read, composite arrays, answer under the legacy OUT names, function as `SELECT * FROM`, no state, the connection belongs to the caller) EXCEPT that there is no NO_DISPONIBLE mark: a name that is not one of the 8 throws `UnsupportedOperationException` naming it, with zero calls on the connection. `ProcedimientosPorDialecto(SqlDialecto, hana, postgres)` and the `@Primary` wiring are as in A2-2/A2-3. `ProcedureDispatcherService` and `QueryDispatcherService` do not change (503 in Postgres mode for every name). | `LoginPg2ProcedimientosTest`, `LoginPg2EventoArreglosTest`, `LoginPg2SeleccionProcedimientosTest`, `LoginPg2ServiciosTest`, `LoginPg3DefinicionesTest`, `LoginPgDespachadoresTest` |
| A3-6 | Dev-time verification replaces the run-time catalog. These are TESTS that read the oracle fixtures (the reason the table is kept in development): (a) every table, view and column each Postgres statement text uses exists in the oracle, and every column the HANA text cites maps to the Postgres column the text uses; (b) the Java definitions of the 8 procedures equal the signatures and composite attributes of the Postgres QA evidence; (c) the two sequence names equal the oracle names; (d) the oracle fixtures still equal the xlsx manifest (hashes, 365 objects / 142 tables / 1722 columns, name format, scope file of 55 names). | `LoginPg3OraculoSentenciasTest` + `LoginPgTraduccionTest` + `LoginPg2TraduccionVistasTest` (a); `LoginPg3DefinicionesTest` (b); `LoginPg3SecuenciasTest` (c); `LoginPgOraculoTest`, `LoginPg2CatalogoVistasTest` (d) |

### The 503 end to end (rewritten)

In Postgres mode `GET /bootstrap/login` with ONE statement without Postgres text answers 503, with ZERO SQL on the connection, and the body names the statement id and
carries no HANA text. The statement without text comes from a test double of `SentenciasSql` registered `@Primary` (`LoginPgContexto.SinTextoPostgres`: overrides
`texto` and `exigirDisponibles`; it hides the text only when the dialect is Postgres). Two cases (the first and the last statement of the page), the no-fallback
case and the HANA-mode case (the double is inert: 200). `GlobalExceptionHandler` is unchanged (UnsupportedOperationException is 503).

### How the procedure definitions are proven without an accessor

Production exposes no accessor for the definitions (the contract names none), so `LoginPg3DefinicionesTest` derives input and scripted answers from the evidence
(`loginpg2/rutinas-y-tipos-postgres-qa-20261103.txt`, Postgres name from the xlsx oracle) and checks the JDBC call that comes out, per routine: call text, one `?`
per parameter, IN type and position, OUT/INOUT registered type, every OUT value under its legacy name from its own position, the 18 function labels and the
attributes of the four composite types (order, JSON key = PascalCase of the attribute, `bytea` as hex). If production adds an accessor, a direct comparison can be
added; nothing here needs it.

## Assumptions of the earlier rounds that are VOID

From `API_ASSUMED_LOGINPG.md`: A-1 (the classes `CatalogoEquivalencias`, `ObjetoNoDisponibleException`), A-2 (constructors with a catalog), A-3 only in its last words
(`objetosDe`; the ids and the resources stay), A-4 (`ObjetoNoDisponibleException`), A-5 (production CSV resources, `desdeRecursos`, `desdeCsv`), A-6 (the
`NO_DISPONIBLE` marker and its equivalents), A-7 (duplicate policy of the catalog), A-8 (catalog methods), A-10 (`primeros`), A-11 (`ahoraUtc`), A-12 in its second half (a
sequence "not in the catalog": now a sequence that is not one of the two constants), A-14 (`sumarSegundos`), A-15 (`fechaATexto`), A-16 (`aEntero`), A-17 (`recortar`),
A-18 (`objeto`, `columna`, `columnaSeleccion`), A-21 in the part "all consumers get the catalog by TYPE... one object is NO_DISPONIBLE" (the 503 test registers a
`SentenciasSql` double), A-23 in the words "every object it needs... the body names the HANA object" (now the pre-flight checks that each statement has a text; the body
names the statement id), Q-LOGINPG-6 (where NO_DISPONIBLE is written), Q-LOGINPG-12 (`primeros`). Still valid: A-9, A-13, A-19, A-20, A-22, A-24, section C (the text of the
Postgres statements) and the other questions.

From `API_ASSUMED_LOGINPG2.md`: A2-1 in the words "constructor `ProcedimientosPostgres(CatalogoEquivalencias)`... resolves the Postgres name through the catalog...
`NO_DISPONIBLE`... loads the mapping resource" (now a no-arg constructor and Java definitions), A2-4 (the resource `/equivalencias/procedimientos-postgres.csv` and its
rows), A2-6 in the sentence "`equivalencias-columnas.csv` gains 148 rows" (the view columns are only the development oracle now; the six view statements and their
`sql/postgres/<id>.sql` files stay), B2-1 in the NO_DISPONIBLE sentence. Still valid: A2-2, A2-3, A2-5, B2-2 to B2-10, C2, and Q-LOGINPG2-1..10.

## Open questions (Q-LOGINPG3-n), each with a recommendation

- **Q-LOGINPG3-1 Unknown statement id.** The contract says what a MISSING Postgres file does, not what an id that is not in `indice.txt` does. Pinned as before
  (round 1): `IllegalArgumentException` naming the id from `texto` and from `exigirDisponibles`, in both dialects (a programming error, not a 503). Recommendation:
  keep it; the check against the index must come BEFORE the file read, otherwise `Clase.NoExiste` would be a 503.
- **Q-LOGINPG3-2 Name files and `texto`.** For the ten object-name ids the tests accept the file with or without a trailing line break (`texto(id).strip()` equals the
  name, no inner whitespace), and they pin that SQL texts are returned exactly as the file holds them. Recommendation: write the name files with a final line break and
  have no code depend on it (the dialect constants, not the files, build `nextval(...)`).
- **Q-LOGINPG3-3 Two places for the two sequence names.** The Java constants of `PostgresDialecto.siguienteValor` and `sql/postgres/AuditoriaSesion.SECUENCIA_*.sql` say the
  same thing; `LoginPg3SecuenciasTest.constante_y_archivo_coinciden` makes them agree. Recommendation: keep both (the file is the documented source of each id; the
  constant is what `AuditoriaSesion` calls) or drop the Java constants and read the file in `siguienteValor`; the owner chose constants.
- **Q-LOGINPG3-4 Procedure definitions without an accessor.** See above. Recommendation: accept the behavioural proof; if the owner prefers a direct comparison, expose a
  package-private `DefinicionesProcedimientos.de(String)` and I add a test in the same package (it would compile only after the redesign, through the bridge).
- **Q-LOGINPG3-5 The Postgres statement texts are generated, then source.** The tests check each text against the oracle (A3-6(a)) but, by the owner's decision, nothing
  regenerates them. A new column in Postgres or a changed text is verified by the same tests when someone edits the file. Recommendation: none; this is the decision.
- **Q-LOGINPG3-6 `LoginPg2TraduccionVistasTest` was checking nothing.** Its "everything the text uses exists in the oracle" test built a regular expression with a Java
  escape error (`"\b"` is a backspace, not a word boundary), so its list of words was always empty and it passed whatever the text said. Fixed in this round and
  generalised to the 45 statements in `LoginPg3OraculoSentenciasTest`. Recommendation: none, FYI (the round 2 mutation run did not catch it because its mutants of the
  view texts were killed by the other tests).
