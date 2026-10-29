# BcsOrdenJobs — Transformation Notes

**Module:** `BcsOrdenJobs`
**Legacy source:** `legacy/otr-sva-optimus-corredores/BcsOrdenJobs/` (11,867 LOC, 27 job classes)
**Target:** Spring Boot 4.1.1 / Java 25 — port 8090
**Equivalence:** Trace-based — legacy requiere HANA + cross-services (no ejecución local).
**Equivalence cases executed:** 33
**Canary:** `"drop table"` → `"DROP TABLE"` en `SqlTableBuilderService.eliminarTabla()` → 2 tests B-ORD-02 fallan. Restaurado.

---

## Behavior Mapping

| Behavior ID | Legacy class | Target file | Descripción |
|---|---|---|---|
| B-ORD-01 | `Campo.createTableString()` | `SqlTableBuilderService.crearTabla()` | `"Create table NAME(\ncol TYPE,\n...);"` |
| B-ORD-02 | `Campo.deleteTableString()` | `SqlTableBuilderService.eliminarTabla()` | `"drop table NAME"` (minúsculas — CANARY) |
| B-ORD-03 | `Campo.getInsertString(List)` | `SqlTableBuilderService.insertarPorLista()` | INSERT por `List<CampoSql>` |
| B-ORD-04 | `Campo.getInsertBatchString(Class)` | `SqlTableBuilderService.insertarPorClase()` | INSERT por reflexión; salta campos `_`-prefijados |
| B-ORD-05 | `ControlMercado.keyCompra/Venta()` | `ControlMercadoService.keyCompra/Venta()` | `tradeID + fechaSistema + "COM"/"VEN"` |
| B-ORD-06 | `ControlMercado.COMPRA/VENTA` | `ControlMercadoService.COMPRA/VENTA` | Constantes `"COM"` / `"VEN"` |
| B-ORD-07 | `ControlMercado.FLUJO_MODIFICACION_AUTO` | `ControlMercadoService.FLUJO_MODIFICACION_AUTO` | `"MODIFICACION_AUTO"` |
| B-ORD-08 | `ControlMercado.MOTIVO_ANULACION` | `ControlMercadoService.MOTIVO_ANULACION` | `"ANULACION POR SISTEMA BURSATIL"` (espacio, no guion) |
| B-ORD-09 | `ControlMercado.sonIguales()` | `ControlMercadoService.sonTransaccionesIguales()` | `BigDecimal.compareTo()==0` en 8 campos numéricos |
| B-ORD-10 | `Campo.getInsertBatchString()` | `SqlTableBuilderService.insertarPorClase()` | Bug pinneado: índice-based → trailing comma si `_`-campo al final |
| B-ORD-11 | `TransaccionRV` fields | `TransaccionRV` record | `BigDecimal.equals()` ≠ `compareTo()` — escala distinta |
| B-ORD-12 | All 27 job classes | `OrdenJobRepository` | `throw UnsupportedOperationException("requires HANA: ...")` |

---

## Representación Side-by-Side

### B-ORD-04: INSERT por reflexión (trailing comma bug pinneado)

**Legacy** (`Campo.getInsertBatchString(Class)`):
```java
Field[] fields = clazz.getDeclaredFields();
for (int i = 0; i < fields.length; i++) {
    if (fields[i].getName().startsWith("_")) continue;
    sql += fields[i].getName();
    if (i < fields.length - 1) sql += ",";  // trailing comma si _campo al final del array
}
```

**New** (`SqlTableBuilderService.insertarPorClase()`):
```java
// Idéntica lógica de índice — bug pinneado por oracle B-ORD-10:
// Si el último Field[] es _prefixed, el field anterior recibe coma trailing.
Field[] fields = clazz.getDeclaredFields();
for (int i = 0; i < fields.length; i++) {
    if (fields[i].getName().startsWith("_")) continue;
    sb.append(fields[i].getName());
    if (i < fields.length - 1) sb.append(",");
}
```

### B-ORD-09: BigDecimal compareTo (no equals)

**Legacy:**
```java
a.getCantidad().compareTo(b.getCantidad()) == 0
```
**New (`sonTransaccionesIguales`):** `BigDecimal.compareTo() == 0` en 8 campos — `new BigDecimal("1.0").equals(new BigDecimal("1.00"))` es `false`, `compareTo` es `0`. Bug de escala explícitamente pinneado.

---

## Desviaciones Deliberadas

- **27 endpoints POST** en `/api/jobs/ordenes/{job-name}/ejecutar` reemplazan los 27 `@EJB` jobs legacy. Firma idéntica: `idEmpresa` + `idUsuario` del body.
- **`OrdenJobRepository`**: todos los métodos lanzan `UnsupportedOperationException`. La lógica real queda en HANA SPs y se activa en QA-HANA.
- **`TransaccionRV` record**: 30 campos, `BigDecimal` para todos los campos numéricos — preserva la precisión de HANA `DECIMAL`.

## No Migrado

- Lógica interna de cada job (acceso HANA, archivos, FTP): deferred hasta QA-HANA.
- Scheduler Quartz → Spring Scheduler: pendiente de Fase 3 exit criterion.

## Follow-ups

- Activar tests `@Disabled` para cada job cuando HANA QA disponible.
- Agregar contrato de idempotencia: los jobs deben ser re-ejecutables sin duplicar datos.
