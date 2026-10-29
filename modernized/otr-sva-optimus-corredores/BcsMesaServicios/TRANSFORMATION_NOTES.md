# BcsMesaServicios — Transformation Notes

**Legacy:** `BSCL/Mesa/JAVA/BcsMesaServicios/` — 65 153 LOC (principal: `CalculosCalculadoraSpot.java`)
**Target:** Spring Boot 4.1.1 / Java 25
**Equivalencia:** trace-based — HANA y posiciones de mercado reales requeridas para ejecución dual

---

## Behavior Mapping

| Behavior ID | Legacy file:lines | Target file | Description |
|---|---|---|---|
| B-MSP-VIG-* | `CalculosCalculadoraSpot.java:1030` | `service/CalculadoraSpotService.filtrarVigentes` | Filtro estados vigentes: VIG, PEL, PDA — excluye ANU y otros |
| B-MSP-INTERB-* | `CalculosCalculadoraSpot.java:1085-1087` | `service/CalculadoraSpotService.filtrarInterbancarios` | INTERB/ARBINT con canal != FWD — excluye FWD (forward) |
| B-MSP-DISTRI-* | `CalculosCalculadoraSpot.java:1265-1271` | `service/CalculadoraSpotService.filtrarDistribucion` | DISTRI + ARBDIS — sin exclusión por canal |
| B-MSP-RES-* | `CalculosCalculadoraSpot.java:1368-1388` | `service/CalculadoraSpotService.resumenOperaciones` | Conteo: anuladas (ANU), compras (COMSPT+VIG), ventas (VENSPT+VIG), total (VIG/PEL/PDA/ANU) |
| B-MSP-POS-01 | `CalculosCalculadoraSpot.java:1000-1013` | `service/CalculadoraSpotService.resultadoPosicionInicial` | (cierre-tipoCambioInicial) × posicion (moneda matching) |
| B-MSP-POS-02 | `CalculosCalculadoraSpot.java:988-997` | `service/CalculadoraSpotService.posicionInicialMoneda` | Suma posicionInicial donde idMoneda coincide |
| B-MSP-PROM-* | `CalculosCalculadoraSpot.java:1244-1245` | `service/CalculadoraSpotService.promedioDeCompra/Venta` | monto/puntas; guarda: puntas==0 → retorna 0 |
| B-MSP-UTIL-INTERB-COM | `CalculosCalculadoraSpot.java:1160` | `service/CalculadoraSpotService.utilidadCompraIncrement` | monto × (precioCalculadora − tipoCambioCierre) |
| B-MSP-UTIL-INTERB-VEN | `CalculosCalculadoraSpot.java:1180` | `service/CalculadoraSpotService.utilidadVentaIncrement` | monto × (tipoCambioCierre − precioCalculadora) |
| B-MSP-UTIL-DISTRI-COM | `CalculosCalculadoraSpot.java:1309` | `service/CalculadoraSpotService.utilidadCompraDistribucion` | monto × (tipoCambioCosto − tipoCambioCierre) |
| B-MSP-UTIL-DISTRI-VEN | `CalculosCalculadoraSpot.java:1330` | `service/CalculadoraSpotService.utilidadVentaDistribucion` | monto × (tipoCambioCierre − tipoCambioCosto) |
| B-MSP-UTIL-DISTRI-MP | `CalculosCalculadoraSpot.java:1308` | `service/CalculadoraSpotService.montoParaPromedioCompraDistribucion` | monto × tipoCambioCosto |
| B-MSP-BE-* | `CalculosCalculadoraSpot.java:1394-1407` | `service/CalculadoraSpotService.calcularBreakEven` | BreakEven = equivEnPesos/posicionActual (scale 4, HALF_UP); MontoTotal = equivEnPesos |
| B-MSP-PARIDAD-* | `CalculosCalculadoraSpot.java:977-982` | `service/CalculadoraSpotService.paridadAUtilizar` | BIDASK: posicion≥0→BID; else→ASK; otro tipo→Mercado |
| B-MSP-MRE-* | `CalculosCalculadoraSpot.java:1420-1456` | `service/CalculadoraSpotService.montoResultadoEquivalente` | M: cierre/paridad; D: cierre×paridad; COMSPT/VENSPT diferencia; guarda ceros |

---

## Canary

Romper `utilidadCompraIncrement` invirtiendo los operandos (`cierre - precio` en vez de `precio - cierre`) produce:
- `utilidad_compra_precio_mayor_cierre_positiva` → FAIL (negativo donde se espera positivo)
- `canary_utilidad_compra_asimetrica_con_venta` → FAIL

Resultado registrado: **Canary: invertir UtilidadCompra → 2 tests fallidos**

---

## Casos ejecutados

**44 tests, 0 fallos, 0 skipped** — equivalencia de lógica pura (no requiere HANA ni mercado real).

---

## Deviaciones deliberadas

| Código | Descripción |
|---|---|
| HANA-defer | `MesaRepositoryAdapter` lanza `UnsupportedOperationException("requires HANA: ...")` para `SP_OBTENER_MOVIMIENTOS_SPT` y `POSICION_INICIAL_T`. |
| Mutable domain | `MovimientoSpot`, `PosicionInicial`, `Paridad`, `BreakEvenResult` usan campos públicos mutables para mantener equivalencia con el patrón de inner-class de la legacy (asignación directa de campos en el código legado). |
| Clase 8-arg no migrada | El overload 8-argumento de `calcularBreakEven` (los dos últimos args ignorados) se mantiene como método de test auxiliar, no en producción — es un artefacto de refactoring incremental del legacy. |

---

## No migrado

- `CalculosCalculadoraSpot.java` completo: sólo la lógica pura verificada por tests está migrada. Los cálculos que dependen de precios de mercado en tiempo real (blotter, posición P&L diario) requieren QA HANA y están diferidos a Fase 4.
- `ResumenOperacionesSpot`, `PosicionInicialServicio`, `FijacionPrecioSpot` — diferidos.

---

## Seguimiento

- Activar integración HANA en QA
- Implementar endpoints de blotter spot completo
- Equivalencia dual-run con datos de mercado históricos en staging
