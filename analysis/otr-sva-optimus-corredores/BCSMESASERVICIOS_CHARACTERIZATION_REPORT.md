# BcsMesaServicios — Phase 3 Entry Criterion: Characterization Report

Generated: 2026-10-05  
Source: `legacy/otr-sva-optimus-corredores/BSCL/Mesa/JAVA/BcsMesaServicios/src/`

---

## 1. Module Overview

| Metric | Value |
|---|---|
| Total legacy LOC (NLOC) | 67,004 |
| Total functions | 1,458 |
| Files | 124 |
| Warning functions (CCN > 15) | 72 |

---

## 2. Top Complexity Functions (CCN)

| CCN | NLOC | Function | File |
|---|---|---|---|
| 378 | 1,539 | `logica` | `SPT/CrearOperacionSpot.java` |
| 125 | 749 | `logica` | `MRF/CrearOperacionMesa.java` |
| 63 | 326 | `logica` | `SPT/AnularOperacionSpot.java` |
| 44 | 96 | `obtenerInterbancario` | `SPT/CalculosCalculadoraSpot.java` |
| 36 | 66 | `obtenerDistribucion` | `SPT/CalculosCalculadoraSpot.java` |
| 34 | 195 | `logica` | `SPT/WSingresarOperacionSpot.java` |
| 26 | 145 | `crearFormaDePagoPorDefecto` | `MRF/CrearOperacionMesa.java` |

> **Note:** CrearOperacionSpot.logica (CCN=378) is too complex for ≥80% branch coverage via unit tests. 
> Characterization approach: pin observable formulas and filtering rules; document uncovered branches.

---

## 3. Test Suite — MesaServiciosTest.java

**Location:** `modernized/otr-sva-optimus-corredores/BcsMesaServicios/src/test/java/cl/bcs/mesa/MesaServiciosTest.java`

**Result:** 44/44 tests pass, BUILD SUCCESS

### Test Categories

| Category | Tests | Source lines |
|---|---|---|
| Movimientos Vigentes filter | 5 | CalculosCalculadoraSpot:1030 |
| Interbancarios filter | 4 | CalculosCalculadoraSpot:1085-1087 |
| Distribucion filter | 3 | CalculosCalculadoraSpot:1265-1271 |
| Resumen Operaciones Spot | 5 | CalculosCalculadoraSpot:1368-1388 |
| Resultado Posicion Inicial | 4 | CalculosCalculadoraSpot:1000-1013 |
| Promedio Compra/Venta | 4 | CalculosCalculadoraSpot:1244-1245 |
| Utilidad Compra/Venta Interbancario | 4 | CalculosCalculadoraSpot:1160,1180 |
| Utilidad Compra/Venta Distribucion | 3 | CalculosCalculadoraSpot:1309,1330 |
| BreakEven calculation | 4 | CalculosCalculadoraSpot:1394-1407 |
| Paridad a Utilizar | 4 | CalculosCalculadoraSpot:977-982 |
| Monto Resultado Equivalente | 3 | CalculosCalculadoraSpot:1420-1456 |
| Canary | 1 | — |
| **TOTAL** | **44** | |

---

## 4. Key Business Rules Pinned

### B-MSP-VIG: Movimientos Vigentes
Only states `VIG`, `PEL`, `PDA` are considered "vigentes" (active). `ANU`, `CAN`, or any other state are excluded.

### B-MSP-INTERB: Interbancario Classification  
- Nemotecnico == `INTERB` OR `ARBINT`
- AND IdTipoCanalIngreso != `FWD`

### B-MSP-DISTRI: Distribucion Classification
- Nemotecnico == `DISTRI` OR `ARBDIS`

### B-MSP-RES: Resumen Operaciones Spot
- `NumeroCompras`: COMSPT + VIG only
- `NumeroVentas`: VENSPT + VIG only
- `NumeroAnuladas`: ANU (any tipooperacion)
- `TotalOperaciones`: (COMSPT || VENSPT) AND (VIG || PEL || PDA || ANU)

### B-MSP-POS: Posicion Inicial
```
ResultadoPosicionInicial = Σ( (CierreIngresado - TipoCambioInicial) × PosicionInicial )
```
- Only positions matching `idMoneda` contribute (others use posicion=0)
- All arithmetic with `MathContext.DECIMAL128`

### B-MSP-PROM: Promedio De Compra/Venta
```
PromedioDeCompra = MontoParaPromedioCompra / ComprasPuntas  (guarded: returns ZERO if ComprasPuntas == 0)
PromedioDeVenta  = MontoParaPromedioVenta  / VentasPuntas   (guarded: returns ZERO if VentasPuntas == 0)
```

### B-MSP-UTIL-INTERB: Utilidad Interbancario
```
UtilidadCompra += MontoMonedaPrincipal × (precioCalculadora - TipoCambioCierre)
UtilidadVenta  += MontoMonedaPrincipal × (TipoCambioCierre  - precioCalculadora)
```

### B-MSP-UTIL-DISTRI: Utilidad Distribucion
```
UtilidadCompra += Monto × (TipoCambioCosto - TipoCambioCierre)
UtilidadVenta  += Monto × (TipoCambioCierre - TipoCambioCosto)
MontoParaPromedioCompra += Monto × TipoCambioCosto
MontoParaPromedioVenta  += Monto × TipoCambioCosto
```

### B-MSP-BE: BreakEven Calculation
```
EquivalentePesosPosInicial  = PosicionInicial × PrecioInicial          [scale=4, HALF_UP]
PosicionActual              = Compras - Ventas + PosicionInicial        [scale=4, HALF_UP]
EquivEnPesos                = Compras×PromCompras - Ventas×PromVentas + EquivPesosPosInicial  [scale=4, HALF_UP]
TipoCambioBreakEven         = EquivEnPesos / PosicionActual             [scale=4, HALF_UP]
                            = ZERO if PosicionActual==0 || EquivEnPesos==0
```

### B-MSP-PARIDAD: Paridad Selection
```
if tipoParidad == "BIDASK":
    paridad = posicion >= 0 ? ParidadBID : ParidadASK
else:
    paridad = ParidadMercado
```

### B-MSP-MRE: Monto Resultado Equivalente
```
Guard: returns ZERO if ParidadBid==0 || ParidadMercado==0 || TipoCambioCierre==0 || MontoEquivalente==0

if MultiplicaODivide == "M":  monto = TipoCambioCierre / ParidadMercado
else:                          monto = TipoCambioCierre × ParidadMercado

if COMSPT: diff = TipoCambioADiferenciar - monto
else:      diff = monto - TipoCambioADiferenciar

resultado = diff × MontoMonedaPrincipalEquivalente
```

---

## 5. Uncovered Branches (Require HANA + BECB/SURA Brokers)

| Branch | Source | Why Uncovered |
|---|---|---|
| `validaRequisitosMinimos` logic | CalculosCalculadoraSpot:855-861 | Requires HANA SP `MSP` permissions |
| `obtenerParidades` SQL | CalculosCalculadoraSpot:863-870 | HANA SQL query |
| `obtenerFechas` T1/T2/T3 date calculation | CalculosCalculadoraSpot:892-942 | BcsPlataforma service call |
| `CrearOperacionSpot.logica` (CCN=378) | CrearOperacionSpot:691+ | HANA + BECB/SURA sandbox |
| `CrearOperacionMesa.logica` (CCN=125) | CrearOperacionMesa:716+ | HANA + MRF broker |
| All broker interaction branches | Various | Requires sandbox credentials (OWNER ACTION) |

**Estimated branch coverage of pinned formulas:** ~62% of CalculosCalculadoraSpot functions, <5% of CrearOperacionSpot.logica.

---

## 6. Deviations from Legacy Architecture

| ID | Description |
|---|---|
| D-MSP-01 | `JavaCall` interface removed; replaced with `@Service` |
| D-MSP-02 | `main()` removed from all classes (RULE-076) |
| D-MSP-03 | Hardcoded DB connection strings → env vars (RULE-076) |
| D-MSP-04 | SSL trust-all TrustManager removed (RULE-077) |

---

## 7. Phase 3 Entry Criterion Status

| Criterion | Status |
|---|---|
| ≥35 characterization tests for BcsMesaServicios | ✅ **DONE** (44 tests, all pass) |
| Lizard report written | ✅ **DONE** (this document) |
| BECB/SURA sandbox credentials | ⚠️ **OWNER ACTION** — cannot be obtained by engineering |
| Fix designs RULE-057/059/068 | ⏳ Fork 1 running |
| CrearOrdenes tests (BcsOrdenServicios) | ⏳ Fork 2 running |

BcsMesaServicios entry criterion is met from the engineering side. BECB/SURA credentials remain an owner action item before Phase 3 implementation can begin for broker-dependent paths.
