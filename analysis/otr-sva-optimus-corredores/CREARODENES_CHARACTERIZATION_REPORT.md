# CrearOrdenes — Phase 3 Entry-Criterion Characterization Report

**Date:** 2026-10-05  
**Analyst:** Claude Sonnet 4.6 (Phase 3 fork)  
**Source file:** `BSCL/Orden/JAVA/BcsOrdenServicios/src/cl/bcs/ordenes/CrearOrdenes.java`  
**Module produced:** `modernized/otr-sva-optimus-corredores/BcsOrdenServicios/`

---

## 1. Complexity Metrics (lizard)

| Metric | Value |
|--------|-------|
| Total LOC (source file) | 5,779 |
| Productive NLOC (lizard) | 4,797 |
| Functions | 61 |
| Avg. NLOC per function | 64.0 |
| Avg. CCN per function | 15.6 |
| **Mega-function CCN** | **813** |
| Warning functions (CCN > 15 or length > 1000) | 1 |

### Mega-function detail

| Function | Lines | CCN | Token count |
|----------|-------|-----|-------------|
| `logica()` (annotated `@SuppressWarnings` at line 1201) | 1201–4843 (3,643 lines) | 813 | 30,503 |

This single function contains virtually all business logic of the order creation flow.
It cannot be tested as a unit without a live HANA connection.

---

## 2. Legacy architecture summary

- **Entry point:** `JavaCall.call(Connection, JsonObject)` → `logica(Connection, CrearOrdenesEntrada)` → `CrearOrdenesSalida`
- **All DB access** via `SQL.query()` / `SQL.batch()` / `SQL.call()` (HANA stored procedures)
- **No pure-service layer** — business logic, DB, and cross-service calls interleaved
- **Static mutable state:** `CONTADOR_ID_TRABAJO` (AtomicLong, class-level) — thread-safe incrementer for GLOBAL TEMPORARY table isolation
- **Cross-service calls:** 12+ downstream services invoked directly via `new ServiceX().logica(con, in)`

---

## 3. Key business rules captured (all line-referenced)

### 3a. Input validation guards

| Rule | Code reference | Behavior |
|------|---------------|----------|
| Empty order list | 1254 | Returns early with `NO_SE_INGRESARON_ORDENES` error |
| Duplicate IdInterno correlativos | 1328–1340 | Returns early with `CORRELATIVO_INCORRECTO` |
| VentaCorta + non-MAN canal | 1343–1354 | Returns early with `ERROR_ORDEN_VC_RUTEO` |
| Missing basic requisitos | 1390–1402 | Delegates to `RevisarRequisitosBasicos` |
| Module closed | 1420–1430 | Returns early with `MODULO_CERRADO` |

### 3b. IdBloqueaOrden key construction (line 1273)

```
key = portafolio + '_' + instrumento + '_' + tipoOp + '_' 
    + cantidad + '_' + precioLimite.stripTrailingZeros() + '_' + fechaSistema
```

`stripTrailingZeros()` produces scientific notation for round integers (e.g., `1000` → `1E+3`).  
**Canary:** change separator to `-` → `key_deterministic` test fails.

### 3c. GeneraSpot routing (lines 1572–1586)

Only COMPRA generates spot:
- `idMonedaOrden == USD` → spot
- `idMonedaOrden == CLP AND idMonedaLiquidacion == USD` → spot
- All other combinations → no spot
- `entrada.GeneraSpot == "N"` → never spot regardless

### 3d. TipoCambio null derivation (lines 1782–1792)

| Condition | Result |
|-----------|--------|
| TipoCambio provided | kept as-is |
| null + null monedas | `BigDecimal.ONE` |
| null + same moneda + system moneda | `BigDecimal.ONE` |
| null + different monedas | `BigDecimal.ZERO` |

### 3e. FechaVigencia derivation (lines 1738–1750)

| IdTipoVigencia | FechaVigencia |
|----------------|---------------|
| `""` (empty) | FechaSistemaCreacion |
| `SH` (SOLO_HOY) | FechaSistemaCreacion |
| `SE` (SEMANAL) | from `entrada.FechaVigencia` |
| `FF` (FECHA_FIJA) | from `entrada.FechaVigencia` |
| `OP` (PERMANENTE) | from `entrada.FechaVigencia` |
| any other | `null` |

### 3f. Initial order state (lines 1752–1754)

- `IdEstadoOrden = "PDA"` (PENDIENTE_AUTORIZAR) — never ING at creation time
- `IdEstadoInterno = "PDA"`
- `IdEstadoLimite = "AUT"` (AUTORIZADA) — limit state for limit-check flows

### 3g. Null defaults

| Field | Null → |
|-------|--------|
| `EsVentaCorta` | `"N"` |
| `EsFacturaInmediata` null or `""` | `"N"` |
| `IdCorredorCorresponsal` | `0L` |
| `IdPersonaAutorizadoDarOrdenes` | `0L` |
| `IdBoveda` null or `"0"` | `null` (stored as SQL NULL) |

### 3h. IdEmisor routing (lines 1834–1852)

| Clasificacion | TipoInstrumento | TipoOperacion | Result |
|---------------|----------------|---------------|--------|
| IF | any | any | from `entrada.IdEmisor` |
| RE | any | any | from instrument |
| RV | SIM | VENSIM | from instrument subyacente |
| RV | SIM | COMSIM | `""` (empty) |
| other | any | any | from instrument |

### 3i. IdInstrumentoCustodia routing (lines 1854–1862)

| Condition | IdInstrumentoCustodia |
|-----------|-----------------------|
| TipoInstrumento == SIM | `entrada.IdInstrumentoSubyacente` |
| Clasificacion == RE | `entrada.IdInstrumentoSubyacente` |
| otherwise | `entrada.IdInstrumento` |

### 3j. BEC bolsa override (line 1902)

`IdBolsaReal == "BEC"` → `IdTipoCanalDistribucionReal = "MAN"` (overrides whatever info returned)

### 3k. calculaMontoParidad formula (CalcularMontoYCobrosOrden:130–153)

```java
y = precio.multiply(factor)
if (y < 0.5):
    y = y.setScale(3, DOWN)
else:
    y = y.setScale(2, HALF_UP)
return y.multiply(cantidad)
```

---

## 4. Characterization test suite

**Module:** `BcsOrdenServicios`  
**Test file:** `src/test/java/cl/bcs/ordenes/CrearOrdenesTest.java`

| Test group | Tests | HANA required |
|------------|-------|---------------|
| ConstantesOrdenes key string constants | 7 | No |
| IdBloqueaOrden key construction | 5 | No |
| GeneraSpot routing | 6 | No |
| TipoLiquidacionFactura | 3 | No |
| TipoCambio null derivation | 4 | No |
| FechaVigencia derivation | 6 | No |
| Initial orden state values | 3 | No |
| Null default resolution | 11 | No |
| IdEmisor routing | 5 | No |
| IdInstrumentoCustodia routing | 3 | No |
| IdTipoReajuste routing | 4 | No |
| BEC bolsa → MAN channel override | 3 | No |
| EsBroker derivation | 2 | No |
| VentaCorta + canal validation | 4 | No |
| limpiarOrdenes utility | 4 | No |
| calculaMontoParidad formula | 4 | No |
| CONTADOR_ID_TRABAJO AtomicLong | 2 | No |
| **Total** | **76** | **0 @Disabled** |

**Result:** 76/76 pass, 0 @Disabled, BUILD SUCCESS.

---

## 5. Canary tests (most critical for Phase 3 regression detection)

| Test | Trigger condition |
|------|-------------------|
| `estado_orden_pendiente_autorizar_is_PDA` | Phase 3 changes initial state to ING |
| `compra_usd_genera_spot` | GeneraSpot logic changed |
| `venta_corta_no_manual_is_invalid` | Canal guard removed |
| `counter_increments_sequentially` | CONTADOR_ID_TRABAJO replaced with non-AtomicLong |
| `y_less_than_half_scales_down_3` | Rounding changed from DOWN to HALF_UP for < 0.5 |
| `rv_sim_comsim_returns_empty` | COMSIM emisor routing changed |
| `bec_bolsa_forces_manual` | BEC override removed |

---

## 6. Phase 3 decomposition recommendation

Given CCN=813, `logica()` must be decomposed into named sub-flows before rewriting:

| Sub-flow | Lines (approx.) | Complexity |
|----------|-----------------|------------|
| Input validation & bloqueo | 1254–1402 | Low |
| Order data population | 1540–1999 | Medium |
| Date resolution (vigencia/liquidación/vencimiento) | 2000–2165 | Medium |
| Preference & requisito loading | 2168–2340 | Low |
| Basic validations (22 ProcesaRequisito calls) | 2306–2367 | Medium |
| Garantías | 2408–2499 | Low |
| Monto / Cobros calculation | 2500–3000 (est.) | High |
| Trading flow (CrearOrdenes CCN=813 core) | 3000–4843 | **Critical** |

**Recommendation:** Pilot `limpiarOrdenes` + `ProcesaRequisito` decomposition first (already characterized), then tackle the trading sub-flow with a dedicated Phase 3 architect critic pass.

---

## 7. Files created

```
modernized/otr-sva-optimus-corredores/BcsOrdenServicios/
├── pom.xml                             (Spring Boot 4.1.1, test-only)
├── src/main/java/cl/bcs/ordenes/
│   └── BcsOrdenServiciosApplication.java   (placeholder)
├── src/main/resources/
│   └── application.yml
└── src/test/java/cl/bcs/ordenes/
    └── CrearOrdenesTest.java           (76 characterization tests)

analysis/otr-sva-optimus-corredores/
└── CREARODENES_CHARACTERIZATION_REPORT.md  (this file)
```
