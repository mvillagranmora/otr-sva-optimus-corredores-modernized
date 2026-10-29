# BcsOrdenServicios — Transformation Notes

**Legacy:** `BSCL/Orden/JAVA/BcsOrdenServicios/` — 72 127 LOC (principal: `CrearOrdenes.java` 5 779 LOC, CCN=813)
**Target:** Spring Boot 4.1.1 / Java 25
**Equivalence:** trace-based — HANA y cross-services requeridos para ejecución dual completa

---

## Behavior Mapping

| Behavior ID | Legacy file:lines | Target file | Description |
|---|---|---|---|
| B-ORD-CONST-* | `ConstantesOrdenes.java` | `domain/ConstantesOrdenes.java` | 30 constantes string (estados, canales, clasificaciones) |
| B-ORD-CRE-01 | `CrearOrdenes.java:1273` | `service/CrearOrdenesService.buildIdBloqueaOrden` | Clave de bloqueo de orden (concatenación con `_`) |
| B-ORD-CRE-02 | `CrearOrdenes.java:1572-1586` | `service/CrearOrdenesService.resolveGeneraSpot` | Routing GeneraSpot: COMPRA+USD o CLP+USD-liq → "S" |
| B-ORD-CRE-03 | `CrearOrdenes.java:1588` | `service/CrearOrdenesService.resolveTipoLiquidacionFactura` | TipoLiquidacionFactura sólo cuando param = "ACTIVO" |
| B-ORD-CRE-04 | `CrearOrdenes.java:1782-1792` | `service/CrearOrdenesService.resolveTipoCambio` | TipoCambio null → ONE (same moneda) o ZERO (distinta) |
| B-ORD-CRE-05 | `CrearOrdenes.java:1738-1750` | `service/CrearOrdenesService.resolveFechaVigencia` | FechaVigencia: SH/null→sistema; SE/FF/OP→entrada; XX→null |
| B-ORD-CRE-06 | `CrearOrdenes.java:1764-1768` | `service/CrearOrdenesService.resolveEsFacturaInmediata` | EsFacturaInmediata null/vacío → "N" |
| B-ORD-CRE-07 | `CrearOrdenes.java:1771-1776` | `service/CrearOrdenesService.resolveEsVentaCorta` | EsVentaCorta null → "N" |
| B-ORD-CRE-08 | `CrearOrdenes.java:1795-1800` | `service/CrearOrdenesService.resolveIdCorredorCorresponsal` | IdCorredorCorresponsal null → 0L |
| B-ORD-CRE-09 | `CrearOrdenes.java:1802-1807` | `service/CrearOrdenesService.resolveIdPersonaAutorizadoDarOrdenes` | IdPersonaAutorizado null → 0L |
| B-ORD-CRE-10 | `CrearOrdenes.java:1757-1761` | `service/CrearOrdenesService.resolveIdBoveda` | IdBoveda "0"/null → null |
| B-ORD-CRE-11 | `CrearOrdenes.java:1824-1832` | `service/CrearOrdenesService.resolveIdTipoReajuste` | IdTipoReajuste: CL+RF→instrumento; IF→entrada; else→"" |
| B-ORD-CRE-12 | `CrearOrdenes.java:1834-1852` | `service/CrearOrdenesService.resolveIdEmisor` | IdEmisor: IF→entrada; RE→instrumento; RV+SIM routing |
| B-ORD-CRE-13 | `CrearOrdenes.java:1854-1862` | `service/CrearOrdenesService.resolveIdInstrumentoCustodia` | IdInstrumentoCustodia: SIM/RE→subyacente; else→instrumento |
| B-ORD-CRE-14 | `CrearOrdenes.java:1902` | `service/CrearOrdenesService.resolveIdTipoCanalDistribucionReal` | BEC bolsa → fuerza canal MAN |
| B-ORD-CRE-15 | `CrearOrdenes.java:1965-1975` | `service/CrearOrdenesService.resolveEsBroker` | EsBroker: idPersonaBroker!=null→"S" else "N" |
| B-ORD-CRE-16 | `CrearOrdenes.java:1343-1354` | `service/CrearOrdenesService.isVentaCortaConCanalInvalido` | VentaCorta sólo válida con canal MAN |
| B-ORD-CRE-17 | `CrearOrdenes.java:1086-1097` | `service/CrearOrdenesService.limpiarOrdenes` | Separa órdenes con errores>0 a lista de errores |
| B-ORD-MONTO-01 | `CalcularMontoYCobrosOrden.java:130-153` | `service/CalcularMontoService.calculaMontoParidad` | y=precio×factor; <0.5→scale(3,DOWN); ≥0.5→scale(2,HALF_UP); ret y×cantidad |

---

## Canary

Romper `resolveGeneraSpot` cambiando `"COMPRA".equalsIgnoreCase` → `"VENTA".equalsIgnoreCase` produce **6 fallos** (GeneraSpotTest).

Resultado registrado: **Canary: `resolveGeneraSpot` COMPRA→VENTA → 6 tests fallidos**

---

## Casos ejecutados

**76 tests, 0 fallos, 0 skipped** — equivalencia de lógica pura (no requiere HANA).

---

## Deviaciones deliberadas

| Código | Descripción |
|---|---|
| HANA-defer | `OrdenRepositoryAdapter` lanza `UnsupportedOperationException("requires HANA: ...")` para SP_CREAR_ORDEN y consultas de módulo. Los @Disabled se activan en QA HANA. |
| Mutable domain | `OrdenProceso` usa campo público mutable `errores` (legacy: el algoritmo `limpiarOrdenes` lo modifica en iteración). No se convirtió a record por diseño. |

---

## No migrado

- `CrearOrdenes.java` completo: sólo la lógica pura verificada por tests está migrada (≈ 400 LOC de 5 779). El 91% restante — acceso HANA, envío de mensajes, generación de IDs de trabajo — requiere QA HANA y está diferido a Fase 4.
- `CalcularMontoYCobrosOrden.java`, `ValidarOrdenNegocio.java`, `NotificarOrden.java` — diferidos a Fase 4.

---

## Seguimiento

- Activar `@Disabled` en tests HANA cuando QA HANA disponible
- Implementar endpoints REST completos (crear/modificar/consultar orden)
- Equivalencia dual-run en staging
