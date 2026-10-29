# WP-D characterization tests: informe cliente relacionado + interfaz Basilea

Legacy oracle: `legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/`
(`ConsultarOperacionesClienteRelacionado`, `GenerarInformeClienteRelacionado`, `ProcesarInterfazBasilea`
and their `sql/*.sql` files). `BUSINESS_RULES.md` has no Contabilidad rules, so behaviors are numbered
`CONT-D-NN`. Each test method name starts with the id, and a comment cites the legacy `file:line`.

| File | Ids | Pins |
|------|-----|------|
| `ConceptosClienteRelacionadoCalculatorTest` | CONT-D-10..25 | Concept matching, sign, folio dedup, TipoDato amounts, totals, output list and negation |
| `ConsultarOperacionesClienteRelacionadoServiceTest` | CONT-D-01..07 | Date-range validation, message precedence, query parameters, JSON field names |
| `InformeClienteRelacionadoWriterTest` | CONT-D-30..43 | `.xls` (HSSF) file name, sheets, headers, cells, number formats, rounding, maps, dates, logo |
| `GenerarInformeClienteRelacionadoServiceTest` | CONT-D-50..54 | Paths, file written to `@TempDir` and read back with POI, error codes |
| `InterfazBasileaMapperTest` | CONT-D-60..70 | Basilea account lookup (empty = equals / non-empty = contains), record mapping per product, RUT format |
| `ProcesarInterfazBasileaServiceTest` | CONT-D-75..81 | Query order, warnings and errors, dedup, "insert only if 0 errors", break on the first error |
| `SqlRecursosClienteRelacionadoBasileaTest` | CONT-D-90..92 | The 17 SQL resources exist under `cl/bcs/contabilidad/sql/` and keep the legacy filters |
| `ClienteRelacionadoFixtures` | - | Builders. All RUTs, names and portfolios are FAKE (same shape as real ones) |

## Running

Nothing here skips. Until the target classes exist, the module does not compile; that is intended.

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
mvn test -pl modernized/otr-sva-optimus-corredores/BcsContabilidadServicios -Dtest='cl.bcs.contabilidad.informe.*Test'
```

A missing SQL resource is a failure, not a skip: the existing `sql()` helpers return `""` for a missing file,
and `SqlRecursosClienteRelacionadoBasileaTest` exists so that gap cannot hide.

Expected count: **207 test executions** (parameterized cases included), 0 skipped. Report the real
`equivalence cases executed: N` from the Surefire summary. Zero means not proven.

## Adding a case

1. Find the legacy branch and note its `file:line`.
2. Pick the test class from the table above. Add a method `contDNN_whatItPins` with `@DisplayName("CONT-D-NN: ...")`,
   or add a row to an existing `@CsvSource`.
3. Build inputs with `ClienteRelacionadoFixtures` (`comprobante(..)`, `dolar(..)`, `pacto(..)`, `dato()`, `cuenta(..)`).
   Never paste real RUTs, names or credentials from legacy code or databases.
4. The expected value is what the legacy code does, even if it looks wrong. Flag the oddity in the comment
   (see "legacy quirk" comments) instead of "fixing" the expectation.
