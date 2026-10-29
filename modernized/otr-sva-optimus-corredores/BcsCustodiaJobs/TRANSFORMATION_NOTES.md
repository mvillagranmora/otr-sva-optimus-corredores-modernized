# BcsCustodiaJobs — Transformation Notes

Phase 4 of otr-sva-optimus-corredores. Legacy: Java EE 8 / TomEE. Target: Spring Boot 4.1.1 / Java 25. Port: 8096.

---

## Behavior Mapping Table

| Legacy File | Lines | Behavior | Target Location |
|---|---|---|---|
| ValidarHorarioEjercicioBloqueoOpciones.java | 98–103 | `esEnHorario`: strictly after horaIni AND before horaTer | `CustodiaJobsService.esEnHorario()` |
| ValidarHorarioEjercicioBloqueoOpciones.java | 138–149 | `determinarEventoHorario`: INICIO / TERMINO / null | `CustodiaJobsService.determinarEventoHorario()` |
| ValidarHorarioEjercicioBloqueoOpciones.java | 206–243 | `notificarHorario`: already public static, not ported (EJB notification infra) | — |
| GenerarArchivoInformeMayorMenorValor.java | 43–51 | Decimal constants (TASAS=6, PRECIOS=2, UF=4, USD_EUR=2, CLP=0, PORCENTAJES=2) | `CustodiaJobsService` constants |
| GenerarArchivoInformeMayorMenorValor.java | 52 | `DIFERENCIADORES_PACTOS = ["PP","PC","PD"]` | `CustodiaJobsService.DIFERENCIADORES_PACTOS` |
| GenerarArchivoInformeMayorMenorValor.java | 125–133 | `obtenerDecimalesCantidad`: UF=4, USD/EUR=2, else=0 | `CustodiaJobsService.obtenerDecimalesCantidad()` |
| RegistraPrecioInstrumento.java | 139–269 | Query instruments + get prices + insert/delete cycle | `CustodiaJobsDataPort.consultarPreciosInstrumentos()`, `actualizarPrecioInstrumento()` |
| PautaTipificarInstrumentos.java | 76–91 | `TipificarInstrumentos.logica()` — HANA SP | `CustodiaJobsDataPort.tipificarInstrumentos()` → UOE |
| PautaVencimientoIF.java | 75–98 | `EjecutarVencimiento.logica()` — HANA SP | `CustodiaJobsDataPort.ejecutarVencimiento()` → UOE |
| VencimientoProductos.java | — | SP_PROCESAR_VENCIMIENTO_PRODUCTOS | `CustodiaJobsDataPort.procesarVencimientoProductos()` → UOE |
| GenerarResumenPosiciones.java | 154–156 | SP_RESUMEN_TARJETA_EXISTENCIA (HANA call) — data portion only | `CustodiaJobsDataPort.consultarPosicionesCliente()` (plain SQL) |
| ConfiguracionSistema.getParametroConfiguracionString | all jobs | PLA.FechaSistema lookup | `CustodiaJobsDataPort.obtenerFechaSistema()` |

---

## Canary Result

Method broken: `CustodiaJobsService.obtenerDecimalesCantidad("UF")` — changed return from `DECIMALES_CANTIDAD_UF` (4) to `3`.

Test that failed: `CustodiaJobsServiceTest.test_obtenerDecimales_UF` — FAILURE confirmed.

Restored: build returns `DECIMALES_CANTIDAD_UF` (4). BUILD SUCCESS.

---

## Executed-Case Count

| Test Class | Tests | Failures | Skipped |
|---|---|---|---|
| CustodiaJobsServiceTest | 22 | 0 | 0 |
| CustodiaJobsIntegrationTest | 10 | 0 | 3 |
| **Total** | **32** | **0** | **3** |

---

## @Disabled Tests

| Test Method | Reason |
|---|---|
| `CustodiaJobsIntegrationTest.test_procesarVencimientoProductos_hana` | Requires HANA SP: SP_PROCESAR_VENCIMIENTO_PRODUCTOS — not callable from H2 |
| `CustodiaJobsIntegrationTest.test_tipificarInstrumentos_hana` | Requires HANA SP: SP_TIPIFICAR_INSTRUMENTOS — not callable from H2 |
| `CustodiaJobsIntegrationTest.test_ejecutarVencimiento_hana` | Requires HANA SP: SP_EJECUTAR_VENCIMIENTO — not callable from H2 |

Note: the three UOE-guard tests (without @Disabled) verify the correct exception and SP name are thrown — those pass in H2.

---

## Architecture Decisions

- `notificarHorario()` from ValidarHorarioEjercicioBloqueoOpciones.java is NOT ported: it constructs `AgregarEvento.ServiceEntrada` objects from a Java EE notification infrastructure (EJB services, JNDI). The pure logic it calls is captured in `determinarEventoHorario()`.
- `GenerarResumenPosiciones.java` calls a HANA stored procedure (`GenerarResumenPosicion_Call.sql`) for the full report. The adapter exposes a plain-SQL fallback for position data; the full SP invocation remains @Disabled.
- `CargarArchivoRetenciones.java` — the prorating logic (`BigDecimal prorrateo = ...`) involves complex joins between in-memory lists and DB queries; it is not a pure-logic candidate. The port/adapter layer covers its DB queries via named-parameter JDBC.
- `ValorizarCustodiaUIbatchJob.java` delegates to `ValorizarCustodiaUIbatch.logica()` (a HANA-dependent service). No pure-logic to extract; operation is mapped to the HANA-SP placeholder pattern.
