> **PARTLY SUPERSEDED by `API_ASSUMED_LOGINPG3.md` (2026-11-03).** The equivalence catalog is no longer a run-time resource: A-1, A-2, A-4 to A-8, A-10 to A-12 (second half), A-14 to A-18 and the NO_DISPONIBLE parts of A-21 and A-23 are VOID (list in section "Assumptions of the earlier rounds that are VOID" of that file). The tests no longer use `ApiLoginPg` (replaced by `ApiLoginPg3`).

# API assumed by the LOGIN-PG tests (dialect layer, T-30 / T-31 / T-32 / T-35)

The tests reach the production code ONLY through `ApiLoginPg.java` (reflection). Until the production classes exist every test that needs them fails with
`LOGIN-PG not implemented: the production class cl.bcs.plataforma.login.dialecto.X does not exist yet`. If the real API differs, change `ApiLoginPg.java`
and this file, not the tests. Everything below is an ASSUMPTION for the owner to approve before T-31/T-32 are implemented.

## A. Classes and signatures (package `cl.bcs.plataforma.login.dialecto`)

| # | Assumption |
|---|---|
| A-1 | Classes: `SqlDialecto` (interface), `HanaDialecto`, `PostgresDialecto`, `CatalogoEquivalencias`, `SentenciasSql`, `ObjetoNoDisponibleException`. |
| A-2 | Public constructors `HanaDialecto(CatalogoEquivalencias)`, `PostgresDialecto(CatalogoEquivalencias)`, `SentenciasSql(SqlDialecto)`. HANA needs the catalog to turn a token `ACC.Usuario` into the full HANA name. |
| A-3 | A statement id is `Class.CONSTANT` of the six classes (55 ids, the golden `INDEX.txt`, including the `SP_*` and `SECUENCIA_*` names). Its text lives in `/sql/comun/<id>.sql` or `/sql/hana/<id>.sql` (and `/sql/postgres/<id>.sql` when it differs in form). `SentenciasSql.ids()` is the same set in both dialects; `texto(id)` returns the final text of the dialect; `objetosDe(id)` the full HANA names the statement touches. |
| A-4 | `ObjetoNoDisponibleException(String hanaName)`; its message contains the HANA name. It extends `UnsupportedOperationException` (or is a `ResponseStatusException` 503): the module's `GlobalExceptionHandler`, unchanged, must answer 503 (Q-LOGINPG-2). |
| A-5 | Production CSV resources: `/equivalencias/equivalencias-objetos.csv` (header names `tipo_objeto,nombre_hana,nombre_postgres`, extra columns allowed) and `/equivalencias/equivalencias-columnas.csv` (`nombre_tabla_hana,nombre_tabla_postgresql,nombre_columna_hana,nombre_columna_postgresql`). UTF-8, comma, header row, columns found by NAME, CRLF and a BOM tolerated, quoted cells allowed. `CatalogoEquivalencias.desdeRecursos()` and `desdeCsv(Reader objetos, Reader columnas)` are public static. |
| A-6 | The marker is the literal `NO_DISPONIBLE` in `nombre_postgres`. An empty `nombre_postgres` and an object absent from the table are treated the same way: not available. Matching of names is exact (case, spaces, quotes). |
| A-7 | Duplicates: an identical repeated row is accepted once; the same key with other values (name, type, column) is refused with a message that names the key and contains "uplic"; two HANA objects with the same Postgres name are refused naming it. |
| A-8 | Catalog methods: `nombrePostgres(hana)` (throws `ObjetoNoDisponibleException` when unavailable/absent), `tipo(hana)`, `disponible(hana)`, `columnaPostgres(tablaHana, columnaHana)` (unavailable table: the 503 exception; unknown table or column: `IllegalArgumentException`-like, message with both names, NOT the 503 exception), `hanaDeFicha("ACC.Usuario")` (part after `::`, unique in the table; unknown: RuntimeException naming the token), `objetosHana()`. |

## B. Dialect functions (literal text, per dialect)

| # | Function | HANA | Postgres |
|---|---|---|---|
| A-9 | `nombre()` | `hana` | `postgres` |
| A-10 | `primeros(select, n)` | `SELECT TOP n ...` (inserted after the leading SELECT) | `select + " LIMIT n"` at the end; `n < 1` is refused with `IllegalArgumentException` in both |
| A-11 | `ahoraUtc()` | `CURRENT_UTCTIMESTAMP` | `(now() AT TIME ZONE 'UTC')` |
| A-12 | `siguienteValor(secuenciaHana)` | `"BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL` | `nextval('sq_pla_log')`; a sequence not in the catalog: the 503 exception |
| A-13 | `seleccionarSinTabla(expr)` | `SELECT expr FROM DUMMY` | `SELECT expr` |
| A-14 | `sumarSegundos(ts, n)` | `ADD_SECONDS (ts, n)` | `(ts + (n) * INTERVAL '1' SECOND)` (H2 and Postgres both parse it; `INTERVAL '1 second'` is not parsed by H2) |
| A-15 | `fechaATexto(x, fmt)` | `TO_VARCHAR (x,'fmt')` | `to_char(x,'fmt')` |
| A-16 | `aEntero(x)` | `to_int(x)` | `CAST(x AS integer)` |
| A-17 | `recortar(x)` | `rtrim(ltrim(x))` | `rtrim(ltrim(x))` |
| A-18 | `objeto("ACC.Usuario")` / `columna("ACC.Usuario.IdEmpresa")` / `columnaSeleccion(...)` | `"BSCL.Plataforma.BD::ACC.Usuario"` / `"IdEmpresa"` / `"IdEmpresa"` | `t_pla_acc_usuario` / `id_empresa` / `id_empresa AS "IdEmpresa"` |
| A-19 | Parameters | `:name` stays `:name` in every function (SqlNombrado does not change) | idem |

## C. Text of the Postgres statements (what the tests demand, beyond the literal ones above)

- No HANA token left (`::`, `BSCL`, `CURRENT_UTCTIMESTAMP`, `TOP n`, `DUMMY`, `NEXTVAL` not followed by `(`, `ADD_SECONDS`, `TO_VARCHAR`, `to_int`).
- It references exactly the Postgres names of the tables of the HANA statement, and every column the HANA statement uses appears under its Postgres name.
- Every double-quoted name is an alias defined with `AS` (or a table alias): columns come out as `id_empresa AS "IdEmpresa"`. An unquoted alias such as `AS OK` becomes `AS "OK"`, because Postgres folds unquoted names to lower case and the label must stay `OK`.
- `TOP n` becomes `LIMIT n` at the end. A derived table in `FROM (...)` gets an alias (Postgres before 16 refuses it, Q-LOGINPG-4).
- Literal Postgres text is pinned for `FiltroSesion.SQL_VALIDA_TOKEN`, `CrearSesionServicio.QUERY_BUSCA_TOKEN`, `UPDATE_INTENTOS`, `QUERY_RELOJ_UTC`, the add-seconds fragment of `UPDATE_BLOQUEO_TEMPORAL` and the `to_char` fragment of `SQL_FERIADOS` (whitespace collapsed).
- `SELECT *` (`SQL_ACCESOS`) is a statement over a view: pending (Q-LOGINPG-1).

## D. Wiring

| # | Assumption |
|---|---|
| A-20 | Property `plataforma.db.dialect`, values `hana` / `postgres` in lower case, default `hana` when absent. Any other value stops the start; the message contains the property name, the value and both valid values. `application*.properties` set it to `hana` only. |
| A-21 | One bean of type `SqlDialecto` and one of `SentenciasSql`; all consumers get the catalog by TYPE, so a test bean marked `@Primary` replaces it (the 503 tests register such a bean in which one object is NO_DISPONIBLE). One `DataSource`, no JPA (no data-jpa/hibernate in the pom). |
| A-22 | The constructors the 163 existing tests use keep working and keep sending HANA text: `new CrearSesionServicio(new AuditoriaSesion())`, `new SesionDatos()`, `new BootstrapServicio(new SesionDatos(), procedimientos)`, `new GestionClaveServicio(procedimientos)`, `new FiltroSesion(dataSource)`, `new UnidadDeTrabajo(dataSource)`. |
| A-23 | Before it reads anything, `GET /bootstrap/login` checks every object it needs (tables and views of its four statements); in Postgres mode an unavailable one answers 503 with zero SQL on the connection and the body names the HANA object; in HANA mode the catalog mark is ignored. The controller must let the exception reach `GlobalExceptionHandler` (today `BootstrapController` catches every `Exception` and answers 500: Q-LOGINPG-3). |
| A-24 | The Postgres-shaped H2 schema of the equivalence tests is generated from the HANA test schema scripts through the xlsx oracle; column TYPES are assumed equal (NVARCHAR, BIGINT, TIMESTAMP, DATE, VARBINARY). Real Postgres types are unknown until T-36. |

## Open questions (Q-LOGINPG-n), each with a recommendation

- **Q-LOGINPG-1 Views.** The 7 views (`PLA.ConsultarEmpresa`, `PLA.ConsultarImagen`, `PLA.ConsultarConfiguracionUsuario`, `PLA.ConsultarConfiguracionGrilla`, `ACC.ConsultarUsuarioAcceso`, `PER.ConsultarPersona`, `CTT.ConsultarContratoDatosGenerales`) have no columns in the table, so 6 statements (`BootstrapServicio.SQL_ACCESOS/SQL_EMPRESAS/SQL_IMAGEN/SQL_PREFERENCIAS/SQL_PREFERENCIAS_GRILLA`, `GestionClaveServicio.SQL_DATOS_RECUPERACION`) cannot be translated or compared. Tests only demand that their Postgres `texto()` either carries no HANA token or fails with a message. Recommendation: read the view columns from the Postgres catalog in T-36 and add them to `equivalencias-columnas.csv`; then add the 6 statements to `LoginPgTraduccionTest` / `LoginPgResultadosH2Test` (remove them from `CON_VISTAS`).
- **Q-LOGINPG-2 The 503 convention.** The module maps `UnsupportedOperationException` to 503 with code `NOT_IMPLEMENTED` and passes a `ResponseStatusException` through with code `STATUS_ERROR`; neither says "object not available". Recommendation: `ObjetoNoDisponibleException extends UnsupportedOperationException` now (no change in `GlobalExceptionHandler`); if the owner wants a specific code (`OBJETO_NO_DISPONIBLE`), add one handler method (this edits existing production code and needs approval).
- **Q-LOGINPG-3 Controllers swallow the exception.** `BootstrapController` catches `Exception` and answers 500 with an empty body; `CrearSesionController` and `PasswordController` probably do the same. The 503 needs `catch (UnsupportedOperationException e) { throw e; }` (or the check before the `try`) in each. Tested end to end only for `GET /bootstrap/login`; the other flows wait for their statements.
- **Q-LOGINPG-4 Derived-table alias.** Postgres before 16 requires an alias on `FROM (SELECT ...)`; `SQL_CONFIGURACIONES` has none. Recommendation: always alias (`) AS q`); it is valid on every version and on H2.
- **Q-LOGINPG-5 UTC clock on H2.** H2 returns `TIMESTAMP WITH TIME ZONE` for `now() AT TIME ZONE 'UTC'` (Postgres returns `timestamp`). The equivalence test rewrites that literal to `LOCALTIMESTAMP` on its H2-PostgreSQL connection (JVM in UTC) and pins the literal by text. Real behaviour waits for T-36.
- **Q-LOGINPG-6 Where NO_DISPONIBLE is written.** The xlsx has no such mark (nor duplicates, nor empty cells): see the oracle manifest. Recommendation: the production CSV carries the literal in `nombre_postgres`, edited by hand where needed, and the generator keeps it when regenerating.
- **Q-LOGINPG-7 Invalid values of the property.** An empty value or `POSTGRES` / ` hana ` are not pinned. Recommendation: strict lower case, empty is invalid.
- **Q-LOGINPG-8 ConsultarFormatos.** The sheet types it `procedimiento` but its Postgres name is a function (`fn_pla_consultar_formatos`). For T-33 decide the call form (`CALL` vs `SELECT * FROM fn(...)`) from the Postgres prefix, not from `tipo_objeto`.
- **Q-LOGINPG-9 57 vs 55 objects.** The plan says 57 (38 tables, 7 views, 10 procedures, 2 sequences). The 55 names cited literally by `src/main/java` are 38 / 7 / 8 / 2; the two missing procedures are the ones the code assembles by parts. The tests pin the 55.
- **Q-LOGINPG-10 Procedures (T-33), out of this round.** `ObtenerConfiguracion`, `ObtenerFechaHoraSistema`, `ObtenerFechas`, `ConsultarFormatos`, `ACC.ConsultarUsuarioAdmin`, `ACC.RecuperarClaveUsuario`, `ACC.ConfirmarCambioClaveUsuario`, `EVT.AgregarEvento` have no signature tests: signatures are unknown. Only their names (`SP_*` ids), the HANA call syntax (golden) and the catalog entries are pinned.
- **Q-LOGINPG-11 Container-level reds.** The three Spring-context groups (`LoginPgSeleccionTest`, `LoginPgNoDisponibleTest` nested classes) fail at class level while production does not exist (the context cannot be built), so the red count undercounts their methods (see TRANSFORMATION_NOTES).
- **Q-LOGINPG-12 SELECT lists with `TOP` plus `UNION`/`ORDER BY`.** Only three statements use `TOP 1`, none with a `UNION`; `primeros` appends `LIMIT n` to the whole text. If a future statement needs `TOP` inside a subquery the function is not enough.
