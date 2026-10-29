# WP-C characterization tests: generic centralisation and accruals (devengos)

Package `cl.bcs.contabilidad.generico`. These tests pin the behaviour of these legacy classes:

| Legacy class (legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad) | Test class |
|---|---|
| SQL of `DevengoATasaCompraCarteraPactada`, `DevengoATasaPacto`, `DevengoCarteraPropiaLibreYGarantia` (ROUND, sign, ABS, YEAR, MM-YYYY, CASE, filters) | `DevengoCalculatorTest` |
| `DevengoATasaCompraCarteraPactada.java` | `DevengoATasaCompraCarteraPactadaServiceTest` |
| `DevengoATasaPacto.java` | `DevengoATasaPactoServiceTest` |
| `DevengoCarteraPropiaLibreYGarantia.java` | `DevengoCarteraPropiaLibreYGarantiaServiceTest` |
| `CentralizaGenerico.java` | `CentralizaGenericoTest` |
| `ConsultarCondicionesConceptosNormativos.java` | `ConsultarCondicionesConceptosNormativosServiceTest` (H2, real SQL) |
| the `.sql` files these classes load | `LegacySqlParityTest` (compares with the legacy files) |

Every test method name starts with its behaviour id `contCNN_` (`CONT-C-NN` in the display
name), and carries a `// Legacy: file:line` comment. `BUSINESS_RULES.md` has no Contabilidad
rules, so the ids are local to WP-C.

## Running

From the module directory (`modernized/otr-sva-optimus-corredores/BcsContabilidadServicios`):

```
mvn test -Dtest='cl.bcs.contabilidad.generico.*Test'
```

The tests compile only once the target API exists (see the class Javadocs and the WP-C
report). Nothing is `@Disabled` and nothing is skipped.

`LegacySqlParityTest` reads the legacy SQL from
`../../../legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/sql`.
You can override that path with `-Dlegacy.sql.dir=<path>`. If the legacy file is missing,
the test fails; it does not skip. The test prints `equivalence cases executed: N`. If N is
0, the SQL parity is not proven.

`ConsultarCondicionesConceptosNormativosServiceTest` uses an embedded H2 database with
the schema in `src/test/resources/generico/condiciones_conceptos_schema.sql`. It needs no
external database and no credentials.

## Adding a case

1. Find the legacy branch (file:line) and work out the literal input and output from the
   legacy code. The legacy code is the oracle, even when it looks wrong.
2. Add a method `contCNN_description` with `@DisplayName("CONT-C-NN: ...")` and a
   `// Legacy: File.java:line` comment. For `CarteraValorizada` rows, use
   `DevengoFixtures.cartera()...build()`.
3. Use synthetic values only. Never copy credentials, connection strings or production
   data from the legacy sources.
