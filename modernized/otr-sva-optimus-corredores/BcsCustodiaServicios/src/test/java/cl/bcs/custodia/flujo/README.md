# ConsultarFlujoCustodia: characterization tests (slice CUS-FLU)

These tests define "done" for the migration of the legacy `cl.bcs.custodia.ConsultarFlujoCustodia` (JavaCall). The rules are in
`analysis/otr-sva-optimus-corredores/RULES_CUSTODIA_FLUJO.md` (CUS-FLU-01..36, D-CUS-01..10). Every test names the rule ids it pins
(display name, case id, or a one-line comment).

| Class | What it pins | State today |
|---|---|---|
| `ConsultarFlujoCustodiaGoldenTest` | 85 goldens recorded from the UNMODIFIED legacy: raw output text, exception, `CUS.FlujoCustodia_MatrizUso` writes and deletes, sequences, every ValorizarInstrumento request | RED: implementation missing |
| `FlujoCustodiaReglasTest` | pure rules: item to source map (41 items, also checked against the map read from the legacy), similarity score, discard reasons, ConsiderarRegistro, operator sign, historic switch | RED: implementation missing |
| `GoldenArnesTest` | the goldens exist and come from the legacy recorder, the cards are covered, the harness reproduces a replayed golden, the dialect bridge | GREEN |

Nothing is skipped. A missing implementation, golden or resource makes a test fail. The golden test prints `equivalence cases executed: N`
and fails when N = 0.

## Run

From the workspace root, JDK 25, offline:

```
cd modernized/otr-sva-optimus-corredores/BcsCustodiaServicios
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot" mvn -o test -Dtest='cl.bcs.custodia.flujo.**' -Dsurefire.failIfNoSpecifiedTests=false
```

The tests set the JVM zone to UTC and the locale to en_US themselves, because the legacy oracle runs that way. They restore both when they finish.

## The contract the implementation must provide (src/main)

`FlujoCustodiaContrato` finds the implementation by reflection. You can point it at other class names with `-Dcustodia.flujo.impl=` and
`-Dcustodia.flujo.reglas=`.

```java
public class cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo {
    public ConsultarFlujoCustodiaFlujo(Function<JsonObject, JsonObject> valorizador,           // ValorizarInstrumento port (out of slice)
                                       BiFunction<String, Long, List<Long>> secuencias)        // Utils.getSecuencia(con, name, qty)
    public JsonObject call(Connection con, JsonObject entrada) throws Exception               // same as the legacy call()
}
public final class cl.bcs.custodia.flujo.FlujoCustodiaReglas { /* static methods: see FlujoCustodiaReglasTest javadoc */ }
```

- The output JSON must equal the legacy text byte for byte. That covers field order, nulls, BigDecimal scale, the `yyyy-MM-dd HH:mm:ss.SSS` dates in
  UTC, the HashMap iteration order of the totalized lines (D-CUS-05) and the "sorted by IdRegistro" order of the detail.
- The valuation request is the JSON of the legacy `ValorizarInstrumentoEntrada`, with every field and its nulls. Key order is not compared.
  Propagate whatever the valuator throws.
- `con` is H2 behind the harness dialect bridge. Issue the same SQL as the legacy (`FlujoCustodia_*.sql`, plus the Plataforma SQL of
  ConsultarEmpresa, ConfiguracionSistema, Diccionario and ObtenerDiaSiguiente). Write `CUS.FlujoCustodia_MatrizUso` and delete it again in
  `finally`. Do not commit.
- The work id (`IdTrabajo`, Math.random, D-CUS-09) is masked in the dump. Its value is free, but all rows of one call must share it.

A throwaway adapter that put the unmodified legacy behind this contract passed 85/85 here (2026-10-25). A copy of it with the operator sign
ignored failed 18/85. So the goldens can be reached and they can fail.

## Where the expected values come from (never by hand)

The material lives in `analysis/otr-sva-optimus-corredores/equivalence/BcsCustodiaServicios/`:

- `legacy-oracle/record.sh` compiles the unmodified legacy (read-only from `legacy/`) into a temporary directory and runs it on in-memory H2
  with MODE=LEGACY, zone UTC and locale en. ValorizarInstrumento is a generated, scripted double. It writes `cases/<id>/expected.json` and
  `unit-goldens/item-fuente.json`. `ORACLE_REPEAT=5` records every case 5 times, and a case whose dumps differ gets no golden.
  `LEGACY_OVERLAY=<dir with a mutated copy>` with `ORACLE_OUT=<dir>` is the mutation check.
- `legacy-oracle/h2/` holds the harness: the dialect bridge, the HANA functions, the MatrizUso witness trigger, the case script and the case
  runner plus dump. It is the single source of `cl.bcs.custodia.flujo.h2.*`, so the oracle and these tests measure with the same code.
- `legacy-oracle/sync.py` copies the goldens, schema and seeds to `src/test/resources/custodia-flujo/` and the harness to
  `src/test/java/cl/bcs/custodia/flujo/h2/`, byte for byte. Do not edit those copies; regenerate them.

## Add a case

1. Add a `caso(...)` to `make_cases.py`. Give it an id that starts with its rule id, list its `reglas`, and supply the request plus the seed
   rows (fictitious data only, no credential). Script the ValorizarInstrumento answers if the case reaches valuation.
2. Run `py -3 -I make_cases.py`.
3. Run `bash legacy-oracle/record.sh <id-prefix>`, then `ORACLE_REPEAT=5 bash legacy-oracle/record.sh <id-prefix>` (it must be stable).
   The recorder refuses a case that reaches an unscripted collaborator.
4. Run `py -3 -I legacy-oracle/sync.py`.

## H2 dialect equivalences (HanaH2, schema-session.sql)

| HANA | H2 | Status |
|---|---|---|
| `"seq".NEXTVAL` | `NEXT VALUE FOR "seq"` | same meaning |
| `SELECT "seq".NEXTVAL FROM SERIES_GENERATE_BIGINT(1,0,n)` (Utils.getSecuencia) | n x `NEXT VALUE FOR` (FlujoGuion / oracle Utils shim) | same contract |
| `TO_DATE(x)` (default format) | `HANA_TO_DATE(x)`: first 10 characters as DATE | ASSUMED |
| `TO_DECIMAL(x)` / `TO_DECIMAL(x,p,s)` | `HANA_TO_DECIMAL`: value with its own scale / at scale s | ASSUMED (HANA's scale for the 1-argument form is not verified) |
| `TO_BIGINT(x)` | alias, integral part | ASSUMED |
| `(1==1)` (the legacy's always-true CARTERA join, :2198) | `(1=1)` | OPEN: whether HANA accepts `==` is not verified |

The schema (`schema-flujo.sql`, generated by `make_schema.py`) marks every table as REAL (catalog export) or ASSUMED (inferred from the SQL and
the pojo field types). NOT NULL and keys are not reproduced.
