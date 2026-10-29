# TRANSFORMATION_NOTES — BcsCanalAgenteServicios

**Source:** Java EE 8 / TomEE  
**Target:** Spring Boot 4.1.1 / Java 25  
**Port:** 8102  
**Date:** 2026-10-05  

---

## Legacy module summary

The legacy `BcsCanalAgenteServicios` contained a single service class:

| Class | Purpose |
|---|---|
| `ConsultarHistorialMovimientosCuentaInversion` | Queries the investment-account movement history for a given portfolio, enriched with comprobante (voucher) data |

The class implemented the `JavaCall` interface (TomEE proprietary), loaded SQL from classpath resources, and used a custom `SQL` utility (`cl.bcs.utils.SQL`) backed by raw `java.sql.Connection`.

---

## Business rules extracted

| Rule ID | Description |
|---|---|
| B-CA-01 | Given `idPortafolio`, look up `IdCliente` from `BSCL.Portafolio.BD::POR.Portafolio`. If not found, return error "Cliente no encontrado." |
| B-CA-02 | Delegate to `ConsultarCartolaDeMovimiento` (BcsCuentaInversionServicios domain) with `p_UsaReglasNegocio = "S"` and `p_SaldoPorFechaEfectiva = "N"`. |
| B-CA-03 | Results are enriched with `FolioComprobante`, `CodigoPortafolio`, `IdRespuestaDTE`, `RespuestaPdfDTE`, and `DocumentoIdentidad` via a HANA temp-table INSERT/SELECT/DELETE pattern. |
| B-CA-04 | If a movement's `IdCargoAbonoMovimiento` has no matching comprobante record, all comprobante fields default to `null` (not an error). |
| B-CA-05 | `idMatrizAgrupador` and `esADC` are optional (default `null`); propagated to `ConsultarCartolaDeMovimiento`. |

---

## Hexagonal architecture

```
adapter/in/
  CanalAgenteController        POST /canal-agente/historial-movimientos
  GlobalExceptionHandler       NPE/IAE/NFE → 400, UOE → 503

port/in/
  ConsultarHistorialMovimientosUseCase

service/
  CanalAgenteService           implements use case; static helper buildClienteNoEncontradoResponse()

port/out/
  CanalAgenteDataPort          findIdClienteByPortafolio + consultarHistorialCompleto

adapter/out/
  CanalAgenteRepositoryAdapter NamedParameterJdbcTemplate (H2-compatible for lookup)

domain/
  HistorialMovimientosRequest  (record)
  HistorialMovimientosResponse (record with factory methods)
  MovimientoCuentaInversion    (record)
  ErroresDetalle               (record)
  AdvertenciasDetalle          (record)
```

---

## H2 vs HANA split

| Operation | H2-testable | Reason |
|---|---|---|
| `findIdClienteByPortafolio` | Yes | Simple `SELECT` from `PORTAFOLIO_T` |
| `consultarHistorialCompleto` | No — `@Disabled("requires HANA")` | Calls `ConsultarCartolaDeMovimiento` SP chain + HANA temp table INSERT/SELECT/DELETE pattern across 5 HANA tables |

---

## SQL adaptations (HANA → H2)

| Legacy (HANA) | Modernized (H2-compatible) |
|---|---|
| `SELECT TOP 1 "IdCliente" FROM "BSCL.Portafolio.BD::POR.Portafolio"` | `SELECT ID_CLIENTE FROM PORTAFOLIO_T LIMIT 1` |
| HANA quoted identifiers (`"IdPortafolio"`) | Standard SQL identifiers (`ID_PORTAFOLIO`) |
| HANA batch INSERT into temp table | `UnsupportedOperationException` until HANA wired |

---

## Test results

```
Tests run: 16, Failures: 0, Errors: 0, Skipped: 2
BUILD SUCCESS
```

- 8 pure-logic unit tests (`CanalAgenteServiceTest`) — no Spring context, instant
- 8 integration tests (`CanalAgenteIntegrationTest`) — H2-backed, 6 active / 2 @Disabled

---

## Patterns applied

- Hexagonal architecture: `domain/`, `service/`, `port/in/`, `port/out/`, `adapter/in/`, `adapter/out/`
- Pure-logic helpers as `public static` on `@Service` for framework-free testing
- H2 in-memory for integration tests with `@ActiveProfiles("test")`, `@SpringBootTest`, `@Transactional`
- `@Disabled("requires HANA")` for HANA-specific SP and temp-table operations
- `Objects.requireNonNull` before every port call
- `NamedParameterJdbcTemplate` + `MapSqlParameterSource` (H2-compatible)
- `GlobalExceptionHandler`: NPE→400, IAE→400, NFE→400, UOE→503
- No `@AutoConfigureMockMvc` — uses `MockMvcBuilders.webAppContextSetup(wac).build()` in `@BeforeEach`
- `main()` only in `BcsCanalAgenteServiciosApplication.java`
- No hardcoded credentials — `${HANA_URL}`, `${HANA_USER}`, `${HANA_PASSWORD}`
