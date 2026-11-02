# BcsSURA — Transformation Notes

**Legacy:** `BSCL/Corredor/SURA/JAVA/Servicios/src/cl/bcs/sura/socionegocio/` — 5 Java classes + pojo + 18 SQL files, 1,468 LOC Java
**Target:** Spring Boot 4.1.1 / Java 25 (built and tested here on JDK 26.0.1)
**Equivalence:** trace-based. The legacy was not executable here (HANA and the platform services are not available), so the tests assert against the legacy source logic, the legacy SQL files byte for byte, and recording doubles of the data port. The proof is weaker than dual execution.
**Brief phase:** Phase 3 (Trading & Portfolio). Its entry criteria (Phase 2 exit, BECB/SURA sandbox credentials) were not met when this was done; the transform was started at the user's request.

## Result

- `mvn -o test` in `BcsSURA`: **140 run, 0 failed, 0 skipped** (the 7 empty `@Disabled` placeholders were deleted: no body, no references; 140 − 7 + 6 for the message dictionary + 1 pinning the filtered-query SQL; the HANA-tagged test is excluded by default and passed against HANA QA when run on its own).
- `equivalence cases executed: 43` (`SuraSqlOracleTest`: legacy SQL and Java read from `legacy/`, a missing directory fails the test).
- Canary: `p_ActualizarIndicadorFormaCobro` default in `ModificarSocioNegocioModificarPerfilesPortafolioService` changed from "N" to "S" → **1 test failed** (`bSura14_flagAusente_porDefectoEsN_apvNoActualizaIndicador`); restored, 140 green. An earlier canary on `validarIndicadorFormaCobroPortafolioApv` (negated condition) failed 3 tests.
- Test suite validated before the implementation existed with a throwaway reference implementation (57/57 flow tests satisfiable; two mutations turned 9 and 1 tests red). The reference was deleted.

## Behavior mapping

| Behavior | Legacy | Target |
|---|---|---|
| B-SURA-01..06 pure helpers (APV flag, filters, IdFormaCobro merge, COBROCUST assign) | `ModificarPerfilesPortafolioUtils.java:94-287` | `service/PerfilesPortafolioService` |
| B-SURA-07..13, 18 document selection, profile queries, COBROCUST, operation order, merge | `ModificarPerfilesPortafolioUtils.java` | `service/PerfilesPortafolioUtilsService` |
| B-SURA-07..19 crear socio de negocio | `CrearSocioNegocioModificarPerfilesPortafolio.java` | `service/CrearSocioNegocioModificarPerfilesPortafolioService` |
| B-SURA-07..19 modificar socio de negocio | `ModificarSocioNegocioModificarPerfilesPortafolio.java` | `service/ModificarSocioNegocioModificarPerfilesPortafolioService` |
| B-SURA-14..17 modificar cliente WS | `ModificarClienteModificarPerfilesPortafolioWS.java` | `service/ModificarClienteModificarPerfilesPortafolioWSService` |
| B-SURA-16 crear cliente full WS | `CrearClienteFullWSModificarPerfilesPortafolio.java` | `service/CrearClienteFullWSModificarPerfilesPortafolioService` (new) |
| B-SURA-20, 21 SQL | `sql/*.sql` (11 loaded by Java) | `src/main/resources/.../sql/` (identical), run by `adapter/out/LegacyHanaSuraPort` |
| B-SURA-23 routing | JavaCall dispatch | `controller/InternalJavaCallController`, `SuraController` `/crear-cliente`, `/modificar-cliente` |

Behavior ids are pinned in test names; `src/test/README.md` has the table of what each id pins.

## Legacy quirks reproduced on purpose (pinned by tests)

- Flag `p_ActualizarIndicadorFormaCobro` defaults to "N" in ModificarSocioNegocio and "S" in ModificarClienteWS.
- First COBROCUST indicator wins even when its value is empty (Crear).
- `obtenerEntidadesYPerfilesCompletas` throws `NoSuchElementException` when a DB row has no in-memory portfolio.
- WS flavours return exactly 3 output fields; CrearClienteFullWS recomputes the ADVERTENCIA counter.
- In the WS flavours the indicator INSERT/UPDATE run before the missing-profile check; only the profile UPDATE is blocked.
- An empty `DatosPortafolio` in ModificarClienteWS drops that socio (`addAll` inside the loop).
- The ModificarClienteWS `UpdateIndicadorCobroCustodia.sql` does not upper-case `:ValorIndicador`.

## Deliberate deviations

- **Temp table → inline derived table.** Legacy `generarTablaTemporal` + filtered SELECT + DELETE become one `consultarPerfilesFiltrados` with bound `SELECT CAST(? ..) FROM DUMMY UNION ALL ..` rows (HANA rejects `(VALUES ..)` in FROM), safe under connection pooling. Empty seed or document list returns an empty list without querying (legacy would read whatever the temp table held).
- **SQL parameterized** everywhere (RULE-068 pattern); no values concatenated.
- `PlataformaUtils.MensajeSalida` is replaced by port operation `resolverMensajeError`, which reads the platform dictionary (`PLA.Diccionario` + `PLA.Configuracion`, legacy `ConsultarDiccionario.sql` copied unchanged) once per company, as legacy `Diccionario.getMensaje` ("{codigo} sin traduccion" when the code or the parameter map is missing; `::Key::` markers replaced). Checked against HANA QA (SELECT only) by `SuraMensajeHanaTest` (tag `hana`, excluded from the default run; `hana-jdbc/run.sh test -DexcludedGroups= -Dtest=SuraMensajeHanaTest`) and on H2 by `SuraMensajeDiccionarioTest`.
- `SuraDataPort.Variante { UTILS, WS }` keeps the two legacy SQL variants (including the non-upper-cased WS update).

## Equivalence against the legacy on HANA QA (read queries, SELECT only)

The unmodified legacy `cl.bcs.utils.SQL` + `IdsEntidadYPerfiles` and the legacy `.sql` files ran on HANA QA (`hana-jdbc/oracle/oracle.sh legacy`) next to `LegacyHanaSuraPort` (`SuraHanaEquivalenciaTest`, tag `hana`, `oracle.sh new`). 6 development inputs (`equivalence/BcsSURA/cases.json`) and 15 fresh inputs (`fresh-cases.json`), 3 queries each: 18/18 and 45/45 outputs identical (`compare.py`). Inputs were chosen from QA by purpose (APV/FFMM/INTERMEDIACION/PROPGENERALES, options 1-5, three risk categories, a client with 10 portfolios, odd document formats, unknown and empty).
**Two defects of the first implementation were found only here** (H2 and the unit doubles hid them), evidence in `equivalence/BcsSURA/new-side-before-fix.hana-defect.txt`: (1) `(VALUES ..)` in FROM is a syntax error on HANA; (2) the profile-by-document mapper read a `Proposito` column that query does not have. Both fixed; `bSura20_perfilesFiltrados_semilla…` pins (1).
Not compared with the legacy: INSERT/UPDATE statements (SELECT-only rule), the message dictionary, the flows above the port.

## Not migrated

- 7 legacy SQL files that no legacy Java class loads (dead; confirmed by `SuraSqlOracleTest`).
- `main()`/debug code, `BcsPojo`/`JavaCallExtra` plumbing replaced by Spring components.
- HANA behavior on H2: view joins, `DetalleIndicador` lookup with UPPER, `NEXTVAL`, `UPDATE ... JOIN`, `UPDATE POR.Portafolio`. SQL is verified as text and by bound parameters only. `SuraRepositoryAdapter` (flat-table H2 fake) now lives in `src/test`.

## Architecture review (architecture-critic)

Applied (HIGH): one production `SuraDataPort` implementation (`LegacyHanaSuraPort`, `@Repository`, real HANA SQL), flat-table adapter moved to `src/test` as `@Primary` fake; removed the `NamedParameterJdbcTemplate` constructor from `PerfilesPortafolioUtilsService` (test now builds `new LegacyHanaSuraPort(jdbc)`).

**Open, needs a person's decision:**
- HIGH: `InternalJavaCallController` (X-SESSION-TOKEN read but never checked) and `SuraController` write profile data with no authentication. Needs a service-to-service credential (mTLS or signed header) agreed with the gateway design.
- MEDIUM: `Variante` overloads with silent defaults; the four services repeat one pipeline (template method/strategy); unbounded `VALUES`/`IN` lists (chunk ~500); null handling (`rs.getLong` vs null, `idPortafolio.equals`); `GlobalExceptionHandler` maps every `NullPointerException` to 400 and echoes the JDBC root cause; no `DataAccessException` handler.
- LOW: mutable `IdsEntidadYPerfiles` (kept for the pinned aliasing quirks); `Collectors.toList()` → `.toList()`; dead imports/unused field in `SuraController`; four `/api/sura/perfiles/filtrar-*` and `validar-apv` endpoints with no caller.

## Follow-ups

- Real equivalence needs more read-only `hana`-tagged tests against HANA QA (the container is `hana-jdbc/`); the legacy classes call no external SURA SOAP service.
- Settle the authentication of the internal endpoints before any cutover.
- APV regulation sign-off by the domain expert is still pending in the brief.
