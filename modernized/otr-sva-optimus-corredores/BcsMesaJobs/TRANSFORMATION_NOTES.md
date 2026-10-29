# BcsMesaJobs — Transformation Notes

**Module:** `BcsMesaJobs`
**Legacy source:** `legacy/otr-sva-optimus-corredores/BcsMesaJobs/` (4,314 LOC, 14 job classes)
**Target:** Spring Boot 4.1.1 / Java 25 — port 8089
**Equivalence:** Trace-based — legacy requiere HANA + cross-services.
**Equivalence cases executed:** 26
**Canary:** `separadorCsv()` retorna `','` en lugar de `';'` → 2 tests B-MES-04 fallan. Restaurado.

---

## Behavior Mapping

| Behavior ID | Legacy class:lines | Target file | Descripción |
|---|---|---|---|
| B-MES-01 | `Params.validate()` | `LiberaBloqueoService.validarParametros()` | Guard: `idEmpresa` y `idUsuario` no nulos |
| B-MES-02 | `Params.validate()` | `LiberaBloqueoService.validarParametros()` | Guard: `idEmpresa` parseable como `long` |
| B-MES-03 | `LiberarBloqueoOrdenes.execute()` | `LiberaBloqueoService.construirUserId()` | `userId = idEmpresa + "_" + idUsuario` |
| B-MES-04 | `GenerarArchivoAnexoIX:CSVWriter` | `RowFormatterService.separadorCsv()` | Separador `';'` (punto y coma, no coma) — CANARY |
| B-MES-05 | `GenerarArchivoAnexoIX:90-105` | `RowFormatterService.formatearFilaTXT()` | Concatenación sin separador; null→`""` |
| B-MES-06 | `MesaJob.SP_*` constants | `MesaJobController` / `MesaRepository` | Nombres de SPs: `BCS_EXECUTE_SP_GENERAR_ANEXO_IX`, etc. |
| B-MES-07 | `LiberarBloqueoOrdenes.execute()` | `LiberaBloqueoService` | `userId` como clave de bloqueo (`OperacionBloqueada.idUsuario`) |

---

## Representación Side-by-Side

### B-MES-04: Separador CSV

**Legacy** (`GenerarArchivoAnexoIX.java`):
```java
CSVWriter writer = new CSVWriter(fw, ';');  // punto y coma, no coma
```

**New** (`RowFormatterService.java`):
```java
public static char separadorCsv() {
    return ';';  // CANARY: cambiar a ',' → B-MES-04 falla
}
```

### B-MES-03: userId como clave de bloqueo

**Legacy** (`LiberarBloqueoOrdenes`):
```java
String userId = p_IdEmpresa + "_" + p_IdUsuario;
// SP: EXEC BCS_LIBERA_BLOQUEO_ORDENES(:userId)
```

**New** (`LiberaBloqueoService`):
```java
public static String construirUserId(long idEmpresa, String idUsuario) {
    return idEmpresa + "_" + idUsuario;
}
```

---

## Desviaciones Deliberadas

- **14 endpoints POST** en `/api/jobs/mesa/{job-name}/ejecutar` reemplazan los 14 EJBs del legacy. Misma firma de `idEmpresa` + `idUsuario`.
- **`MesaRepository`**: todos los métodos lanzan `UnsupportedOperationException("requires HANA: ...")`. Los 14 SPs de HANA quedan deferred hasta QA-HANA.
- **`RowFormatterService.esCsv/esTxt()`**: usa `compareTo() == 0` (case-sensitive) — idéntico al legacy. `"csv".compareTo("CSV") != 0`.

## No Migrado

- Generación física de archivos (Anexo IX, etc.): la lógica de escritura de archivos queda en HANA SPs; el servicio recibe y reenvía.
- Scheduler Quartz → Spring Scheduler: pendiente de Fase 3 exit criterion.

## Follow-ups

- Cuando QA-HANA disponible: activar tests `@Disabled` para los 14 SPs y validar que los archivos generados coincidan byte-a-byte con los legacy.
- Validar encoding de salida CSV (legacy usaba `ISO-8859-1` en algunos jobs chilenos con tildes).
