# WP-E characterization tests: CentralizaA* .. CentralizaF*

Tests live in `src/test/java/cl/bcs/contabilidad/centraliza/`: one `Centraliza<Type>Test` per legacy type
(CentralizaAnticipoPactos is covered elsewhere), plus `SqlResourcesTest`.

The oracle is the legacy code in
`legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/`.
Each test is named `contENNN_*` with display name `CONT-E-NNN`, and cites the legacy `file:line` it pins.
A red test means the modern code differs from legacy. Do not change the expected value to match modern code.

## Run

```
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"; export PATH="$JAVA_HOME/bin:$PATH"
mvn -o -f BcsContabilidadServicios/pom.xml test -Dtest='cl.bcs.contabilidad.centraliza.*Test' --batch-mode
```

Nothing is skipped or `@Disabled`. `SqlResourcesTest` fails when it cannot find the module sources, and it
asserts that the scan found more than 100 literals, so it cannot pass by finding nothing.

## How the tests observe the modern code

* `ContabilidadRepository` and `PlataformaPort` are Mockito mocks with LENIENT strictness.
* SQL evidence (`paramsOf("X.sql")`) scans every repository invocation for a String argument equal to,
  or ending in `/X.sql`. It then reads the parameters from the `MapSqlParameterSource` or `Map` argument
  of that same call. This means the tests do not depend on which port method the implementation uses.
* Rows are served with `rows("X.sql", ...)`, which stubs `queryDatosMovimientoRows`. The maps are
  case-insensitive, like `JdbcTemplate.queryForList`.
* Configuration that is not stubbed returns the default value the caller passes.

## Add a case

The standard tests are generated. Edit `generator/specs.py` (field DSL: `Name`, `Name=kind:Column`;
kinds `s`, `svS/svL/svI` (String.valueOf), `dsql` (Fecha.stringToSqlDate), `dstr` (Fecha.toStringDate),
`ts` (Fecha.toStringDatetime), `tostrS/tostrD/tostrL`, `plain`, `dec`, `bdL`, `lit:VALUE`, `cfgDoc/cfgNom`),
then run:

```
python generator/gen.py <outdir>
```

Copy the output into the test package. IDs are renumbered in generation order, so append new specs at
the end to keep the existing IDs stable. Hand-written cases go into a spec's `extra` string, using `@ID@`
twice (once in the display name, once in the method name).
