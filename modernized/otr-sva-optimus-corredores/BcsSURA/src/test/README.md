# BcsSURA characterization tests

Legacy oracle: `legacy/otr-sva-optimus-corredores/BSCL/Corredor/SURA/JAVA/Servicios/src/cl/bcs/sura/socionegocio/`
(5 classes + pojo + 18 SQL files). Run from `BcsSURA/`: `mvn -q test` (add `-o` if the artifacts are cached;
the parent POM `spring-boot-starter-parent:4.1.1` must be resolvable).

Behavior ids continue TRANSFORMATION_NOTES.md (B-SURA-01..06 are pinned by PerfilesPortafolioTest):

| Id | Behavior | Test class |
|---|---|---|
| B-SURA-07 | which documents are queried (Crear: only CLIENTE; Modificar: all; CrearClienteFull: first NumeroDocumento per socio) | CrearSocio.., ModificarSocio.., CrearClienteFull.., UtilsPort, AdapterH2 |
| B-SURA-08 | profile UPDATE sent for every portfolio, unfiltered, only when error counter == 0 | UtilsPort, AdapterH2, flows |
| B-SURA-09 | any empty IdPerfilComercial/Productos/Cobros -> error entry + counter++ | flows |
| B-SURA-10 | error entry shape per class (EntidadCreada / EntidadModificada / EntidadManipulada / CrearClienteFullWS.EntidadCreada), catalogue params | flows |
| B-SURA-11 | temp-table seed (COBROCUST value on matching portfolios) and drop of rows with empty IdFormaCobro | UtilsPort, flows |
| B-SURA-12 | COBROCUST selection (first wins, even empty in Crear), one insert per socio with portfolios | flows |
| B-SURA-13 | operation order | flows |
| B-SURA-14 | p_ActualizarIndicadorFormaCobro: default "N" in ModificarSocioNegocio, "S" in ModificarClienteWS; only "N" neutralises APV | ModificarSocio.., ModificarClienteWS |
| B-SURA-15 | ModificarClienteWS DatosPortafolio loop quirks (empty list => nothing processed; 2 entries => duplicates) | ModificarClienteWS |
| B-SURA-16 | CrearClienteFull recounts ADVERTENCIA messages, output has exactly 3 fields | CrearClienteFull.. |
| B-SURA-17 | ModificarClienteWS output has exactly 3 fields, counters from jout | ModificarClienteWS |
| B-SURA-18 | obtenerEntidadesYPerfilesCompletas: one doc per id (duplicates kept), merge of IdFormaCobro, NoSuchElementException when no match | UtilsPort, flows |
| B-SURA-19 | pre-existing errors in jout block profile writes (Crear/Modificar socio: block ALL writes; WS flavours: indicators still written) | flows |
| B-SURA-20 | SQL is parameterized (RULE-068 pattern) | SqlOracle, UtilsJdbc, AdapterH2 |
| B-SURA-21 | migrated SQL == legacy SQL (oracle files) and HANA-only semantics pinned as text | SqlOracle |
| B-SURA-22 | adapter batch/filter semantics on H2 | AdapterH2 |
| B-SURA-23 | HTTP routing (JavaCall dispatch of the 4th class, endpoint stubs) | Dispatch |

## Test doubles
`support/SuraTestSupport.FakePort` is a recording dynamic proxy of `SuraDataPort`. Besides the 5 current operations it
answers two operations that main must add (looked up by name): `consultarPerfilesFiltrados(List<IdsEntidadYPerfiles>)`
(legacy temp-table INSERT + filtered SELECT + DELETE) and
`resolverMensajeError(Long idCliente, Long idEmpresa, String codigo, Map<String,String> params)` returning
`Map{codigo, descripcion, tipo}` (legacy `PlataformaUtils.MensajeSalida`). `SuraTestSupport.create` builds a main service
from constructor parameters of type `SuraDataPort`/`PerfilesPortafolioUtilsService`; if none exists it throws
`GAP-SEAM` (a failure, never a skip).

## Adding a case
1. Pick the legacy branch, give it a B-SURA id, put it in the test name.
2. Build rows with `idRow` / `perfilRow`, params with the `params(...)` helper of the class, call `execute(SVC, fake.port, params)`.
3. Assert the ordered port calls (`fake.names()`, `fake.writes()`), arguments (`Snap`) and output JSON.
4. Use `efectivosInsert` / `efectivosUpdate` to assert effective writes regardless of who applies the IdIndicador filter.

## Oracle
`SuraSqlOracleTest` reads the legacy SQL/Java from `../../../legacy/...` (override with `-Dsura.legacy.dir=`); a missing
directory fails the tests. It prints `equivalence cases executed: N`.

## H2 vs HANA
H2 cannot run any legacy statement (HANA views, `UPDATE ... FROM ... JOIN`, `NEXTVAL`, `CURRENT_UTCTIMESTAMP`, local
temporary tables). SQL is verified as text (SqlOracleTest) and through bound parameters (UtilsJdbcTest); the adapter
tests use flat tables, store ValorIndicador raw (HANA resolves it through DetalleIndicador with UPPER on both sides),
and update PERFILES_COMERCIALES_T instead of POR.Portafolio.
