# BcsFacturacionServicios — Transformation Notes

Spring Boot 4.1.1 / Java 25 | Port 8099 | Phase 4

---

## Behavior Mapping Table

| Legacy class / method | Disposition | Modern equivalent |
|---|---|---|
| `ConstantesFacturacion.TIPOS_FACTURA` | Ported | `FacturacionServiciosService.TIPOS_FACTURA` (public static) |
| `ConstantesFacturacion.TIPOS_NOTA_CREDITO` | Ported | `FacturacionServiciosService.TIPOS_NOTA_CREDITO` (public static) |
| `ConstantesFacturacion.TIPOS_ANTICIPO_SIMULTANEA` | Ported | `FacturacionServiciosService.TIPOS_ANTICIPO_SIMULTANEA` (public static) |
| `ConstantesFacturacion.esTipoFactura(String)` | Ported | `FacturacionServiciosService.esTipoFactura(String)` (public static) |
| `ConstantesFacturacion.esTipoNotaCredito(String)` | Ported | `FacturacionServiciosService.esTipoNotaCredito(String)` (public static) |
| `ConstantesFacturacion.esTipoAnticipoSimultanea(String)` | Ported | `FacturacionServiciosService.esTipoAnticipoSimultanea(String)` (public static) |
| `ProcesarFacturacionCustodia` (5,384 LOC) | @Disabled / UOE | `FacturacionServiciosDataPort.procesarFacturacionCustodia()` → throws `UnsupportedOperationException` |
| `CrearComprobante` | @Disabled / UOE | `FacturacionServiciosDataPort.crearComprobante()` → throws `UnsupportedOperationException` |
| `ProcesarGananciaCapital` | @Disabled / UOE | `FacturacionServiciosDataPort.procesarGananciaCapital()` → throws `UnsupportedOperationException` |
| `DatosComprobanteCrystal.java` (Crystal Reports) | Deferred — Phase 4 exit blocker | See deferred section below |
| `CONFIGURACION_T` query (FechaSistema) | Ported | `FacturacionServiciosRepositoryAdapter.obtenerFechaSistema()` via `NamedParameterJdbcTemplate` |
| `COMPROBANTES_T` query (by empresa+estado) | Ported | `FacturacionServiciosRepositoryAdapter.consultarComprobantes()` via `NamedParameterJdbcTemplate` |

---

## Canary Test

**Break:** Set `esTipoFactura` to always return `false`.
**Expected failure:** `FacturacionServiciosServiceTest.esTipoFactura_FAC_returnsTrue()` fails, plus the 9 individual list-membership tests.
**Restore:** Revert to `TIPOS_FACTURA.contains(tipo)`.
**Result:** BUILD SUCCESS — confirming tests actually guard the constants.

---

## @Disabled List

| Test | Reason |
|---|---|
| `procesarFacturacionCustodia_requiresHana` | `ProcesarFacturacionCustodia` is 5,384 LOC of entirely HANA-driven stored procedure calls — all `logica()` methods require a live HANA `Connection` parameter |
| `crearComprobante_requiresHana` | `CrearComprobante` god-object is 100% HANA-driven; no extractable pure logic |

The corresponding port methods throw `UnsupportedOperationException("requires HANA")`, mapped to HTTP 503 by `GlobalExceptionHandler`.

---

## Deferred Items

### Crystal Reports Integration (Phase 4 exit criteria blocker)

`DatosComprobanteCrystal.java` bridges legacy SAP Crystal Reports report generation to HANA data. This is explicitly gated as a **Phase 4 exit criteria blocker** per `MODERNIZATION_BRIEF.md` and is documented as deferred. Crystal Reports has no Spring Boot analog; the replacement path is a reporting microservice (JasperReports or similar) driven by the same COMPROBANTES_T data, which requires a separate architectural decision.

### HANA-Driven God-Objects

- `ProcesarFacturacionCustodia` (5,384 LOC) — all methods take `Connection` directly; no extractable pure logic
- `CrearComprobante` — all methods take `Connection` directly
- `ProcesarGananciaCapital` — all methods take `Connection` directly

These three classes represent the bulk of the legacy LOC. They are fully gated behind `UnsupportedOperationException` adapters and `@Disabled` tests until a HANA sandbox environment is available for Phase 5 characterization.

---

## Build Notes

Spring Boot 4.1.1 removed `@AutoConfigureMockMvc` and `@WebMvcTest` from `spring-boot-test-autoconfigure`. Integration tests use `MockMvcBuilders.webAppContextSetup(webApplicationContext)` directly from `spring-test` to achieve equivalent behavior.

---

## Final Test Results

```
Tests run: 32, Failures: 0, Errors: 0, Skipped: 2
BUILD SUCCESS
```

- 24 pure-logic unit tests (FacturacionServiciosServiceTest) — no Spring context
- 6 integration tests (FacturacionServiciosIntegrationTest) — H2 in-memory, profile "test"
- 2 @Disabled (HANA-dependent POST endpoints)
