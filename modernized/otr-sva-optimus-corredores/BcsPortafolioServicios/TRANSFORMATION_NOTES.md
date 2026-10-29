# BcsPortafolioServicios — Transformation Notes

**Legacy:** `BSCL/Portafolio/JAVA/BcsPortafolioServicios/` · Java EE 8 / TomEE 8.0.16  
**Target:** Spring Boot 4.1.1 / Java 25 · Port 8086  
**Phase:** 3 (pilot module — smallest, 9,105 LOC)  
**Date:** 2026-10-05

---

## Equivalence

**Trace-based** — legacy cannot run standalone (HANA JDBC + cross-service EJB stubs required).  
91 pure-logic oracle tests cover all decision branches derivable without a live database:  
82 B-POR-01..15 business-rule tests + 6 RULE-068 SQL-injection whitelist tests + 3 RULE-076 no-credentials tests.

**Canary (B-POR-02):** changing `"APV_EXENTO"` → `"APV_EXENTO_X"` in `PortafolioService` causes 6 B-POR-02 tests to fail.  
**Canary (RULE-068):** commenting out the `validateSequenceName()` call in `HanaUtilService.getSequence` causes 3 RULE-068 tests to fail (XMLs in `analysis/otr-sva-optimus-corredores/equivalence/canary/BcsPortafolioServicios-rule068/`).  
**Equivalence cases executed:** 91 / 91, 0 skipped, 0 `@Disabled`.

---

## Legacy → Target file mapping

| Legacy file | LOC | Target |
|---|---|---|
| `CrearPortafolio.java` | 1,265 | `service/PortafolioService.java` (B-POR-01…11) + `infrastructure/persistence/PortafolioRepository.java` |
| `CrearPortafolioConReplica.java` | 713 | `service/PortafolioService.java` (B-POR-12…14) |
| `ModificarPortafolio.java` | 572 | `service/PortafolioService.java` + port |
| `ConsultarConsumoEnLinea.java` | 551 | `service/ConsumoService.java` |
| `AutorizacionModificacionEstadoPortafolioMasivo.java` | 562 | `service/AutorizacionService.java` |
| `AutorizacionModificarPortafolioMasivo.java` | 814 | `service/AutorizacionService.java` (B-POR-09…11) |
| `ws/ModificarEstadoPortafolioWS.java` | 459 | `service/PortafolioService.java` + `controller/PortafolioController.java` |
| `AutorizacionCrearPortafolioMasivo.java` | 419 | `service/AutorizacionService.java` |
| `AdministrarPortafolioDefecto.java` | 401 | `service/PortafolioService.java` |
| `AutorizarCrearAutorizadoOrdenMasivo.java` | 371 | `service/AutorizacionService.java` |
| `AutorizacionModificacionEstadoAutorizadoOrdenMasivo.java` | 362 | `service/AutorizacionService.java` |
| `CrearCuentaCustodio.java` | 319 | `service/CustodioService.java` |
| `ModificaPreferenciaPagoPortafolio.java` | 318 | `service/PreferenciaService.java` |
| `AutorizarCrearCuentaCustodioMasivo.java` | 301 | `service/AutorizacionService.java` |
| `EliminarSinTransacciones.java` | 288 | `service/PortafolioService.java` (B-POR-15) |
| `RevisaDatosPortafolio.java` | 277 | `service/PortafolioService.java` |
| `IngresaPreferenciaPagoPortafolio.java` | 269 | `service/PreferenciaService.java` |
| `ModificarAutorizadoOrden.java` | 172 | `service/AutorizacionService.java` |
| `ws/ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.java` | 182 | `controller/PortafolioController.java` |
| `ConsultarConsumoLimite.java` | 148 | `service/ConsumoService.java` |
| `CrearMatrizAgrupadorNOAPV.java` | 103 | `service/MatrizService.java` |
| `ModificarMatrizAgrupadorNOAPV.java` | 107 | `service/MatrizService.java` |
| `EliminarMatrizAgrupadorNOAPV.java` | 107 | `service/MatrizService.java` |
| `pojo/MatrizAgrupadorNOAPV.java` | 14 | `domain/matriz/MatrizAgrupadorEntrada.java` |
| `pojo/PortafolioMantenedor.java` | 11 | absorbed into domain records |

---

## Key behaviors preserved

| ID | Behavior | Source line | Target |
|---|---|---|---|
| B-POR-01 | CodigoPortafolio regex via `POR.MaximoCodigoPortafolio`; uses `.find()` not `.matches()` | `CrearPortafolio.java:591-602` | `PortafolioService.validarCodigoPortafolio()` |
| B-POR-02 | APV: `IdPerfilCobros.trim()` must equal `"APV_EXENTO"` case-insensitive | `:705-713` | `PortafolioService.validarApvPerfilCobros()` |
| B-POR-03 | Dual-write: lines 965 AND 966-967 are two independent if-statements; `matrizAgrupador>0` sets both `RequiereAutorizacion="N"` AND `RequiereAutOrigen="N"` | `:962-967` | `PortafolioService.derivarRequiereAutorizacion()` |
| B-POR-04 | Glosa: tipo="J" → `DocId / RazonSocial / CodPort:X`; else `DocId / Nombres ApPat ApMat / CodPort:X` | `:990-991` | `PortafolioService.construirGlosa()` |
| B-POR-05 | EUR account: `.compareTo("S") == 0` is **case-sensitive** — lowercase "s" skips EUR | `:867-878` | `PortafolioService.buildCurrencyList()` |
| B-POR-06 | Plantilla override: non-null non-empty `p_IdPlantillaCobro` overrides `perfilComercial.IdPerfilCobros` | `:767-771` | `PortafolioService.resolverIdPerfilCobros/Productos()` |
| B-POR-07 | `p_IdPersonaBrokerExtranjero == null` → store 0L | `:773-774` | `PortafolioService.resolverIdPersonaBroker()` |
| B-POR-08 | `p_Estado` null/empty → "ACT" | `:776-777` | `PortafolioService.resolverEstado()` |
| B-POR-09 | `asignarValorNuevo` 20-field switch; `"IdNombreFantasia"` with `""` → null (Long); unknown field → no-op | `AutorizacionModificarPortafolioMasivo.java:357-441` | `AutorizacionService.asignarValorNuevo()` |
| B-POR-10 | `esCampoValido` whitelist — exactly 23 allowed field names | `:492-521` | `AutorizacionService.esCampoValido()` |
| B-POR-11 | `IdPerfilComercialBaseOrigen` separated from batch set; processed via JOIN | `:181-186` | `AutorizacionService` |
| B-POR-12 | 3-way CategoriaInv match (client==comercial==product) | `CrearPortafolioConReplica.java:327-332` | `PortafolioService.validate3wayCategoria()` |
| B-POR-13 | 2-way match without product (client==comercial) | `:339-344` | `PortafolioService.validate2wayCategoria()` |
| B-POR-14 | Null/blank `p_ReplicaPortafolio` → early return, no HANA calls | `:232-236` | `PortafolioService.isReplicaPortafolioEmpty()` |
| B-POR-15 | `EliminarSinTransacciones`: any table with `Cantidad > 0` blocks deletion | `EliminarSinTransacciones.java` | `PortafolioService.tieneTransacciones()` |

---

## Deliberate deviations

| ID | Description | Rationale |
|---|---|---|
| D-SEC-01 | `HanaUtilService.getSequence`: sequence name now validated against `[A-Za-z0-9_.:\-]+` whitelist before string concatenation into SQL | RULE-068: HANA sequence names cannot be bound as SQL parameters — whitelist is the correct mitigation |
| D-POR-01 | `main()` removed from all 25 legacy files | RULE-076: only `BcsPortafolioServiciosApplication` may have `main()` |
| D-POR-02 | `JavaCall.call(Connection, JsonObject)` → `@RestController` REST endpoints | Java EE EJB pattern replaced with Spring REST |
| D-POR-03 | `BcsAutorizaciones` TomEE interface → `@Service` methods with typed parameters | No TomEE runtime in Spring Boot |
| D-POR-04 | SQL resource files + `SQL.batch/query/call` → `UnsupportedOperationException` in repository | HANA QA unavailable; Phase 3 wire-up deferred |
| D-POR-05 | Cross-service EJB calls (ReplicaPortafolioAPV, ConsultarSaldoCuentaInversion, CalcularParidad, NotificarCambioEjecutivoCliente) → port stubs | RestClient adapters pending HANA QA |
| D-POR-06 | Fat `PortafolioPort` → 4 narrow ports: `PortafolioCRUDPort`, `AutorizacionPort`, `ConsumoPort`, `CustodioPreferenciaPort` | SRP; each service injects only its own port |
| D-POR-07 | `GlobalExceptionHandler` added: NPE→400, `IllegalArgumentException`→400, `UnsupportedOperationException`→503 | Production-grade error mapping |

---

## Not migrated (dead code / unreachable)

- `CrearPortafolio.java:1237-1262` — `main()` with hardcoded test JSON and `PlataformaUtils.getMASTER01Connection()` — eliminated (RULE-076 + credential removal)
- `AutorizacionModificarPortafolioMasivo.java:526-551` — `buildUpdateSQL(String, Timestamp)` method (overloaded, never called by `autorizarEvento`; the called method is `buildUpdatePortafolioSQL(Set<String>)`) — not migrated
- `ConsultarConsumoEnLinea.java`: `ResumenInversionCuentaInversion` inner class — used only as local variable, absorbed into domain

---

## Architecture critic — HIGH findings applied (2026-10-08)

| Finding | File | Fix applied |
|---|---|---|
| HIGH-1: NPE → 400 | `GlobalExceptionHandler.java` | Changed NPE handler to return 500 INTERNAL_SERVER_ERROR; message no longer leaks Java NPE text |
| HIGH-2: Unauthenticated `/internal/java-call` | `InternalJavaCallController.java` | Added `X-SESSION-TOKEN` validation against `${bcs.internal.service-token}`; removed FQN disclosure from 503 body; warns at startup when token is not configured |
| HIGH-3: Broken transaction boundary | `CrearPortafolioService.java` | Removed `DataSource` injection; both `SimpleJdbcCall` usages now wrap `SingleConnectionDataSource(conn, true)` to share the caller's connection and keep all HANA ops in one transaction |
| HIGH-4: RestClient per-invocation | `JavaCallRestBridge.java` → all javacall services | `JavaCallRestBridge` converted to `@Service` singleton with `SimpleClientHttpRequestFactory` (5 s connect, 30 s read); all 5 services now inject it instead of `new`-ing per call |
| HIGH-5: Masivo returns only last result | `AutorizacionService.java`, `PortafolioController.java` | `autorizarMasivo`/`rechazarMasivo` now return `List<AutorizacionPortafolioResultado>`; individual failures are caught, logged, and appended as `ERROR` entries instead of silently dropped |

## Architecture critic — MEDIUM/LOW findings (deferred to Phase 3 wire-up)

- MEDIUM-1: `JavaCallInfra.mapFields` silently swallows column mapping misses → add `log.warn` per unmatched column
- MEDIUM-2: `CrearPortafolioService` hardcodes `p_UsaReglasNegocio = "N"` instead of using `e.p_UsaReglasNegocio`
- MEDIUM-3: Gson + Jackson dual JSON library → consolidate to Jackson in javacall layer
- MEDIUM-4: `tieneTransacciones` intermediate counter → replace with `stream().anyMatch(t -> t.Cantidad > 0)`
- MEDIUM-5: `CrearPortafolioService` previously used `DataSource`; now fixed by HIGH-3 — MEDIUM-5 is resolved
- MEDIUM-6: `ModificarEstadoPortafolioService.validarCatalogos` enumerates valid `IdTipoBloqueo` in error message → return fixed code without the list
- LOW-1: `PortafolioPort` deprecated empty interface → delete
- LOW-2: `toJson(Object)` helper duplicated in 4 services → move to `JavaCallInfra`
- LOW-3: Static SQL loading in service classes → move to `@PostConstruct` for clean startup error

## Port split (critic fix HIGH-2)

| Port | Implements | Used by |
|---|---|---|
| `PortafolioCRUDPort` | CRUD + estado operations | `PortafolioService` |
| `AutorizacionPort` | 8 auth/reject flows + autorizadoOrden | `AutorizacionService` |
| `ConsumoPort` | ConsumoEnLinea + ConsumoLimite | `ConsumoService` |
| `CustodioPreferenciaPort` | Custodio + Preferencia + Matriz | `CustodioService`, `PreferenciaService`, `MatrizService` |

`PortafolioRepository` implements all 4 ports. All methods throw `UnsupportedOperationException("requires HANA: <specific table or SP name>")`.

---

## Follow-ups (Phase 3 wire-up — blocked on HANA QA)

- Implement `PortafolioRepository` with `NamedParameterJdbcTemplate` + `SimpleJdbcCall`
- Tables: `BSCL.Portafolio.BD::POR.Portafolio`, `BSCL.Portafolio.BD::POR.PlantillaLimites`, `BSCL.Plataforma.BD::SOL.SolicitudCambio`
- SPs: `CrearCuentaInversion`, `ConfirmarCrearPortafolio`, `CrearEventoAutorizacion`, `ReplicaPortafolioAPV`, `AgregarLog`, `ModificarEstadoPortafolio`
- `RestClient` adapters: `BcsCuentaInversionClient` (ConsultarSaldoCuentaInversion), `BcsMesaClient` (CalcularParidad), `BcsAPVClient` (ReplicaPortafolioAPV), `SocioNegocioClient` (NotificarCambioEjecutivoCliente)
- Activate `@Disabled` integration tests when HANA QA available
