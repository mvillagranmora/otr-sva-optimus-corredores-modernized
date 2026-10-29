# BcsBECB — Transformation Notes

**Legacy:** `BSCL/Corredor/BECB/JAVA/` — 2,897 LOC (principal: `GenerarInformesRiesgoFinanciero.java` 207 LOC, `GenerarInformeVencimientosGenerales.java` 1,625 LOC, `riesgo/` package 1,065 LOC)
**Target:** Spring Boot 4.1.1 / Java 25
**Equivalencia:** trace-based — HANA y sistema de archivos requeridos para ejecución dual completa

---

## Behavior Mapping

| Behavior ID | Legacy file:lines | Target file | Description |
|---|---|---|---|
| B-BECB-01 | `GenerarInformesRiesgoFinanciero.java:116-120` | `service/RiesgoFinancieroService.determinarTabla` | tabla = "CUS" si fechaSistema == fechaInforme, else "HIS" |
| B-BECB-02 | `riesgo/FactoryRiesgoFinanciero.java:29-44` | `service/RiesgoFinancieroService.resolveCodigoInforme` | Factory por código informe: RIFI→RIFI, DIFI→DIFI, ALL→ALL; null/desconocido→null |
| B-BECB-03 | `riesgo/InformeRiesgoFinanciero.java:181-214`, `riesgo/RiesgoFinancieroCompleto.java:227-260` | `service/RiesgoFinancieroService.agruparFilasRifi` | Agrupación run-length: mismo Rango+InstrumentoEmisor (case-insensitive) → acumula ValorPresente, toma max Duracion; primer grupo siempre Volatilidad=ZERO y Var=ZERO |
| B-BECB-04 | `riesgo/InformeRiesgoFinanciero.java:96-98` | `service/RiesgoFinancieroService.tieneData` | Lista vacía → no genera reporte (retorna false) |
| B-BECB-05 | `GenerarInformesRiesgoFinanciero.java:146-151` | `service/RiesgoFinancieroService.generarNombreArchivo/Log` | Nombre: `Informe_<codigo>_<tabla>_<timestamp>.xls`; log: `.log` |
| B-BECB-06 | `GenerarInformesRiesgoFinanciero.java:140-143,157-160` | `service/RiesgoFinancieroService.salidaError` | Error path: ErroresContador=1, Comentario=mensaje |

---

## Canary

Romper `agruparFilasRifi` invirtiendo la condición de agrupación (usando `!=` en vez de `== 0` para comparar Rango) produce:
- `agrupar_mismoEmisorRango_acumulaValorPresente` → FAIL (obtiene 2 grupos en vez de 1)
- `agrupar_mismoEmisorRango_tomaMaxDuracion` → FAIL
- `agrupar_emisorCaseInsensitive_mismoGrupo` → FAIL
- `agrupar_tresGrupos_diferentesEmisores` → FAIL

**Canary: invertir condición de agrupación → 4 tests fallidos**

---

## Casos ejecutados

**29 tests, 0 fallos, 3 skipped** (HANA-deferred) — equivalencia de lógica pura verificada.

---

## Deviaciones deliberadas

| Código | Descripción |
|---|---|
| HANA-defer | `BecbRepositoryAdapter` lanza `UnsupportedOperationException("requires HANA: ...")` para todas las consultas a BSCL. |
| FileSystem-defer | Generación de archivo .xls (POI) y .log diferida a Fase 4 — requiere acceso a sistema de archivos del servidor. |
| Legacy-quirk-preserved | Primer grupo de `agruparFilasRifi` siempre tiene Volatilidad=ZERO y Var=ZERO (comportamiento del legacy preservado tal cual). |
| Vencimientos-deferred | `GenerarInformeVencimientosGenerales.java` (1,625 LOC) no migrado — toda su lógica depende de 7 consultas HANA y generación de PDF + ZIP + XLS. Diferido a Fase 4. |

---

## No migrado

- `GenerarInformeVencimientosGenerales.java` completo: generación de informe de vencimientos con Pactos Simultáneas, Spots, Cuenta Propia/Terceros y Operaciones a Término — 100% dependiente de HANA + PDFBox + ZIP. Diferido a Fase 4.
- `riesgo/InterfaceRiesgoFinanciero.java`: interfaz legacy `obtenerData(Connection, ...)` — reemplazada por port hexagonal `BecbDataPort`.
- `BcsJobGenerarInformesRiesgoFinanciero.java` y `BcsJobGenerarInformeVencimientosGenerales.java`: jobs legacy (timer-driven via TomEE) — deberán migrarse como `@Scheduled` en módulo BcsBECBJobs (Fase 4).

---

## Seguimiento

- Activar integración HANA en QA para habilitar `@Disabled` tests
- Implementar `GenerarInformeVencimientosGenerales` completo (PDFBox → PDFBOX 3.x, ZIP, múltiples hojas Excel)
- Migrar jobs a `@Scheduled` en módulo BcsBECBJobs
- Equivalencia dual-run con datos históricos en staging
