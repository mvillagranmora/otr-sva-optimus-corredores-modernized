# BcsJobOrders — Transformation Notes

**Module:** `BcsJobOrders` (AsignacionCronologica)
**Legacy source:** `legacy/otr-sva-optimus-corredores/BcsJobOrders/` (2,124 LOC)
**Target:** Spring Boot 4.1.1 / Java 25 — port 8088
**Equivalence:** Trace-based — legacy requiere HANA + cross-services.
**Equivalence cases executed:** 44
**Canary:** Cambiar `d1.before(d2)` → `d2.before(d1)` en `compararFechasConDate()` → 4+ tests B-JOB-01 fallan. Restaurado.

---

## Behavior Mapping

| Behavior ID | Legacy class:lines | Target file | Descripción |
|---|---|---|---|
| B-JOB-01 | `BcsJobOrderChronologicAllocation.compararFechasConDate()` | `AsignacionCronologicaService.compararFechasConDate()` | `fecha1 <= fechaActual` con `SimpleDateFormat("yyyy-MM-dd HH:mm:ss")`; excepción → `false` |
| B-JOB-02 | `BcsJobOrderChronologicAllocation.transaction():116` | `AsignacionCronologicaService.asignar()` | Matching: COMPRA↔VENTA inverso, misma clasificacion, misma bolsa |
| B-JOB-03 | `transaction():161-170` | `asignar()` | Tipo L (lote): `cantidad = Math.min(dispOrden, dispTrx)` |
| B-JOB-04 | `transaction():195-210` | `asignar()` | Tipo M (monto): `cantidad = Math.min(dispOrden, dispTrx)` idéntico |
| B-JOB-05 | `transaction():230-260` | `asignar()` | VENTA tipo L: `priceSellingQty * priceSellingPrice` |
| B-JOB-06 | `transaction():280-295` | `asignar()` | VENTA tipo M: `cantidad` desde monto |
| B-JOB-07 | `BcsJobOrderAllocation.asignar():70` | `AsignacionCronologicaService.asignar()` | `cantAsignada += cantidadAAsignar` |
| B-JOB-08 | `BcsJobOrderAllocation.asignar():75` | `asignar()` | `cantDisponible -= cantidadAAsignar` |
| B-JOB-09 | `BcsJobOrderAllocation.montoTotal()` | `AsignacionCronologicaService.montoTotal()` | `cantidad * precio` |
| B-JOB-10 | `BcsJobOrderChronologicAllocation.especificaBolsa()` | `AsignacionCronologicaService.especificaBolsa()` | Bolsa específica en orden → filtrar por bolsa en transacción |
| B-JOB-11 | `BcsJobOrderChronologicAllocation.transaction():105` | `AsignacionCronologicaService.asignar()` | Guard: listas vacías → lista vacía |

---

## Representación Side-by-Side

### B-JOB-01: Comparación de fechas

**Legacy** (`BcsJobOrderChronologicAllocation.java:compararFechasConDate`):
```java
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
Date d1 = sdf.parse(fecha1);
Date d2 = sdf.parse(fechaActual);
if (d1.before(d2)) return true;
if (d2.before(d1)) return false;
else return true;  // iguales → true
```

**New** (`AsignacionCronologicaService.java`):
```java
public static boolean compararFechasConDate(String fecha1, String fechaActual) {
    try {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = fmt.parse(fecha1);
        Date d2 = fmt.parse(fechaActual);
        if (d1.before(d2)) return true;
        if (d2.before(d1)) return false;
        return true; // iguales → true (legacy else branch)
    } catch (Exception e) {
        return false;
    }
}
```

---

## Desviaciones Deliberadas

- **Algoritmo O(n²)** pinneado: el legacy usa doble for sin optimización. No se refactorizó (sería un cambio de comportamiento en edge cases de cantidades parciales). Candidato a optimización post-validación.
- **`AsignacionRepository`**: todos los métodos lanzan `UnsupportedOperationException("requires HANA: ...")`. Asignación efectiva deferred hasta QA-HANA.
- **`ParametrosDefault` record**: pinea los valores hardcoded del legacy: `TIPO_OPERACION_COMPRA="COM"`, `TIPO_OPERACION_VENTA="VEN"`, `LOTE="L"`, `MONTO="M"`.

## No Migrado

- Escritura de asignaciones a HANA (`BSCL.Orden.BD::POS.Asignacion`): deferred.
- Scheduler trigger (Quartz → Spring Scheduler): pendiente.

## Follow-ups

- Optimizar O(n²) → O(n log n) post-validación en staging.
- Activar tests `@Disabled` de persistencia cuando QA-HANA disponible.
- Validar thread-safety: `AsignacionCronologicaService` es `@Service` (singleton) pero todos los métodos son `static` — safe.
