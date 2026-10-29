# WP-G characterization tests: BcsContabilidadServicios JavaCall services

These tests pin what the **legacy** classes `legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/<Name>.java`
actually do. The modern `cl.bcs.contabilidad.service.<Name>Service` classes have to match them. Every test is named
`CONT-G-NNN` and has a comment citing the legacy `file:line`. No Contabilidad rules exist in BUSINESS_RULES.md, so the
CONT-G ids are the rule ids for this module.

Expected values come from reading the legacy code, not the modern code. A red test means the modern code differs from legacy.

## Run

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"; export PATH="$JAVA_HOME/bin:$PATH"
mvn -o -f modernized/otr-sva-optimus-corredores/BcsContabilidadServicios/pom.xml test -Dtest='cl.bcs.contabilidad.servicios.*Test'
```

## How the harness works

- `RecordingJdbc` is a real `NamedParameterJdbcTemplate` and `JdbcTemplate` running over a fake JDBC driver. Spring's own
  behavior is kept: named-parameter parsing, collection expansion, `No value supplied for the SQL parameter`,
  BeanPropertyRowMapper and BeanPropertySqlParameterSource semantics, and the empty-batch short-circuit. A blank statement
  fails like a real driver would.
- Each executed statement is identified by comparing its normalized text with the legacy oracle copy in
  `src/test/resources/servicios/sql/` (verbatim legacy files with no credentials). Legacy files with identical text are aliases.
  The harness records the value bound to every `:param` and returns the rows you script per legacy file.
- `LegacySql.injectBlank` puts the legacy SQL into a service field **only if the field is blank**. That lets the service logic be
  tested even though the module does not ship the resources today. The missing resources are pinned separately by
  `SqlResourcesTest` (CONT-G-001..018).
- `RecordingJdbcHarnessTest` (CONT-G-000x) shows that the harness itself can fail.
- Output messages follow the legacy `PlataformaUtils.MensajeSalida` shape `{id, tipo, codigo, descripcion}` with tipo values
  `ERROR`, `ADVERTENCIA`, `INFORMACION`, `AUTORIZACION` and `OK`. The `descripcion` text comes from the DB dictionary, so only its
  presence is asserted.

## Adding a case

1. Read the legacy branch and note its `file:line`.
2. Script the rows with `jdbc.rows("<LegacyFile>.sql", row("Col", value, ...))`, and use `jdbc.sequence(...)` for HANA `NEXTVAL`.
3. Call `execute(json(...))` with the legacy `p_*` input keys.
4. Assert on the output JSON and on `jdbc.executedParamSets("<LegacyFile>.sql")`. Name the method `contGNNN_...`, give it a
   `@DisplayName("CONT-G-NNN ...")`, and add a `// legacy X.java:L` comment.

Never copy credentials from legacy code into these tests. The SAP connection is a second fake datasource.
