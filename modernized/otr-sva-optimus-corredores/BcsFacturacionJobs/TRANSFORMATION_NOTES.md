# BcsFacturacionJobs — Transformation Notes

## Phase 4 · otr-sva-optimus-corredores

**From:** Java EE 8 / TomEE (custom scheduler framework, Sql2o, GSON)  
**To:** Spring Boot 4.1.1 / Java 25 (Spring JDBC, NamedParameterJdbcTemplate, records)  
**Port:** 8095  
**Canary:** `B-FAJ-18` — `formatearDec("OE-80", 6, 2, true, ".")` → `"0000.00"` (no-dot edge case)

---

## Behavior Mapping

### Legacy class: `ResumenFacturasClienteJob`

| Legacy Behavior | Modernized Behavior | Notes |
|---|---|---|
| Reads `PLA.FechaSistema` via HANA stored proc `ObtenerConfiguracion` | `FacturacionJobsRepositoryAdapter#obtenerFechaSistema` — direct SQL `SELECT VALOR FROM CONFIGURACION_T` | SP replaced with plain SQL; equivalent for the key value |
| Queries `ResumenFacturacionCliente_Select.sql` via Sql2o | `consultarResumenFacturas` — `SELECT ID_CLIENTE, ID_COMPROBANTE, MONTO_TOTAL, FECHA_COMPROBANTE FROM COMPROBANTES_T` | Simplified to testable columns; full HANA view query not portable |
| Inserts into temporal notification table | Not ported — notification pipeline is a separate concern | Legacy notification SP not available |
| Calls `FAC.NotificarResumenFacturasCliente` SP | Not ported — throws UOE via port interface | Requires live HANA environment |

### Legacy class: `BcsJobGenerarPrefacturasMasivas`

| Legacy Behavior | Modernized Behavior | Notes |
|---|---|---|
| Calls `GenerarInformePrefacturas.logica()` with entry params | `generarPrefacturasMasivas` → throws UOE | SP-dependent; `GenerarInformePrefacturas` class not available |
| Updates output path via batch SQL | Not ported | Downstream of the SP call |

### Pure Logic: `generarArchivo/FormatoArchivo.formatear`

| Rule ID | Legacy Rule | Test |
|---|---|---|
| B-FAJ-01 | CHAR short value → trailing space padding | `charPadRight` |
| B-FAJ-02 | CHAR long value → truncated to `largo` | `charTruncate` |
| B-FAJ-03 | CHAR rellenar=N → no padding | `charNoFill` |
| B-FAJ-04 | CHAR empty + valorDefecto → default applied then padded | `charDefaultValue` |
| B-FAJ-05 | CHAR surrounding spaces → trimmed before padding | `charTrimThenPad` |
| B-FAJ-06 | NUM rellenar=S → leading zeros | `numLeadingZeros` |
| B-FAJ-07 | NUM exact length → unchanged | `numExactLength` |
| B-FAJ-08 | NUM rellenar=N → no padding | `numNoFill` |
| B-FAJ-09 | DEC normal value → parteEntera leading zeros + parteDecimal trailing zeros | `decNormalValue` |
| B-FAJ-10 | DEC short decimal → trailing zeros on decimal part | `decTrailingZerosOnDecimalPart` |
| B-FAJ-11 | DEC no "." → entire value treated as "0" (OE-80 edge case) | `decNoDotTreatedAsZero` |
| B-FAJ-12 | DEC decimal part too long → truncated to `decimales` | `decTruncateDecimalPart` |
| B-FAJ-13 | DEC rellenar=N → no padding | `decNoFill` |
| B-FAJ-14 | DATE with formato → parsed yyyy-MM-dd, reformatted, padded | `dateDdMmYyyy` |
| B-FAJ-15 | DATE with formato → exact-length result (no padding needed) | `dateYyyyMmDd` |
| B-FAJ-16 | DATE no formato → raw value left-aligned (no truncation) | `dateEmptyFormat` |
| B-FAJ-17 | Unknown tipoRegistro/campo → returns `""` | `noMatchReturnsEmpty` |
| B-FAJ-18 | CANARY: DEC no-dot edge case "OE-80" → "0000.00" | `canaryDecLegacyEdgeCase` |

---

## Key Design Decisions

### DEC separator not counted in `largo`
The `separadorDecimales` character (e.g. "." or ",") is added between the integer and
decimal parts but is NOT counted toward `largo`. This matches the legacy behaviour where
`largoParteEntera = largo - decimales` and the separator is extra.

### DEC integer vs decimal part padding direction
- **Integer part**: right-aligned (`%<n>s`) → leading zeros (e.g. "1234" → "001234")
- **Decimal part**: left-aligned (`%-<n>s`) → trailing zeros (e.g. "5" → "50")

This is correct financial formatting: `9.5` becomes `9.50`, not `9.05`.

### public static service methods
All `FormatoArchivoService` methods are `public static` per Phase 4 pattern,
enabling zero-Spring-context unit testing while still supporting `@Service` injection.

### H2 test schema
The test schema uses H2-compatible DDL. The HANA-specific temporal table and
complex view queries from the legacy SQL files are replaced with a simplified
`COMPROBANTES_T` table covering the core billing summary fields.

### HANA SP operations marked UOE → 503
`emitirComprobantesMasivos` and `generarPrefacturasMasivas` throw
`UnsupportedOperationException` in the adapter and are `@Disabled` in tests.
The `GlobalExceptionHandler` maps UOE to HTTP 503 Service Unavailable.

---

## Equivalence Case Count

| Test class | Enabled | Disabled (HANA) | Total |
|---|---|---|---|
| `FormatoArchivoServiceTest` | 18 | 0 | 18 |
| `FacturacionJobsIntegrationTest` | 4 | 2 | 6 |
| **Total** | **22** | **2** | **24** |

---

## Canary Verification

```
FormatoArchivoService.formatearDec("OE-80", 6, 2, true, ".")
  → valor has no "." → valor = "0"
  → split("0") → parteEntera="0", parteDecimal="0"
  → largoParteEntera = 6-2 = 4
  → rellenar=true: "0" → "%4s" → "   0" → replace → "0000"
  → rellenar=true: "0" → "%-2s" → "0 " → replace → "00"
  → result = "0000" + "." + "00" = "0000.00"  ✓
```
