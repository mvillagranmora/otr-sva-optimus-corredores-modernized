# BcsTesoreriaServicios: Transformation Notes

**Equivalence is dual-execution on the legacy classes run locally** (the legacy cannot build standalone; its JavaCall classes were compiled from `legacy/` into a scratch folder and run on in-memory H2 with minimal runtime shims; the legacy tree does not even compile as is: `CalcularMontoYCobrosOrden.Orden_Entrada` does not exist in the source, the oracle defines it from its use). No HANA, QA or third-party service was called. These notes replace the earlier pilot notes (trace-based, 2026-10-03), which described the `domain/service` layer that was removed.

| Item | Detail |
|---|---|
| Legacy | `BcsTesoreriaServicios`: 62 classes, 23,224 non-blank lines (52 JavaCall); `BcsTesoreriaWebServices` (SOAP to the bank BCP): 72 files, ~9,950 lines, covered only by a static contract (below) |
| Target | Spring Boot 4.1.1 / Java 25; one `*Service` per legacy JavaCall class under `javacall/`; the JavaCall JSON contract (legacy field names, `P_OUT_*`) is kept inside the services |
| Decisions (user, 2026-10-19/20) | Legacy defects are **kept** and listed for the domain expert (SME); no traffic goes to this service yet; HANA schema assumptions are documented assumptions; `javacall/` is the single path (the pilot `domain/service` was removed); first prove equivalence on the current JSON contract, then refactor into a JavaCall adapter plus typed domain services; the SOAP contract is captured statically and the real wiring is a declared blocker; the session and provisional-password rules of the platform are not this module's (reassigned to BcsUtils / BcsPlataformaWeb) |
| Tests | 955, 0 failures, 0 skipped, one surefire fork with `-Duser.timezone=UTC` |
| Equivalence cases executed | **4332** in `analysis/otr-sva-optimus-corredores/equivalence/BcsTesoreriaServicios/cases.json`: 4332 same, 0 differ (`compare.py`, self-check passed) |
| Goldens | 856 cases recorded by running the unmodified legacy: family A `TES-A-*` (526 cases, 27 classes), family B `TES-B-*` (330 cases, 25 of 28 classes) |
| Rules | BUSINESS_RULES.md has no rule of this module. The only rule that applies is RULE-076 (no `main()` outside the Spring class, no credential or JDBC literal): `Rule076SinCredencialesNiMainTest`. Its scan ignores the `propOrder` lists of JAXB types (property names such as "password"), nothing else |

## Mapping (class level; line-level traceability is by behavior id TES-A-NN / TES-B-NN in the READMEs and in a comment on every class)

| Legacy class | Legacy LOC | Modern class |
|---|---|---|
| `ActivarMandatosArchivoCargo` | 96 | `javacall/ActivarMandatosArchivoCargaService.java` |
| `ActualizarSaldosCuentaBanco` | 231 | `javacall/ban/ActualizarSaldosCuentaBancoService.java` |
| `ActualizarSaldosCuentaBancoCorredora` | 107 | `javacall/ActualizarSaldosCuentaBancoCorredoraService.java` |
| `ActualizarTransaccion` | 326 | `javacall/cloper/ActualizarTransaccionService.java` |
| `AnulacionMandatoPAC` | 244 | `javacall/AnulacionMandatoPACService.java` |
| `AnularNominasTransferencias` | 192 | `javacall/AnularNominasTransferenciasService.java` |
| `AnularRegistros` | 567 | `javacall/AnularRegistrosService.java` |
| `AutorizacionAnularMovimientosTesoreria` | 133 | `javacall/AutorizacionAnularMovimientosTesoreriaService.java` |
| `AutorizacionLiquidacion` | 272 | `javacall/AutorizacionLiquidacionService.java` |
| `Camara` | 26 | (no service: POJO/constant/out of proof, see notes) |
| `CamaraCompensacion` | 15 | (no service: POJO/constant/out of proof, see notes) |
| `CargaMasivaIngresoEgresoDAPFFMM` | 703 | `javacall/CargaMasivaIngresoEgresoDAPFFMMService.java` |
| `Cloper` | 74 | (no service: POJO/constant/out of proof, see notes) |
| `CloperWsBSC` | 175 | (no service: POJO/constant/out of proof, see notes) |
| `ConfirmarAutorizarAnularMovimientosTesoreria` | 151 | `javacall/ConfirmarAutorizarAnularMovimientosTesoreriaService.java` |
| `ConfirmarRechazarAnularMovimientosTesoreria` | 155 | `javacall/ConfirmarRechazarAnularMovimientosTesoreriaService.java` |
| `ConstantesTesoreria` | 157 | (no service: POJO/constant/out of proof, see notes) |
| `ConsultaBCP` | 456 | `javacall/ConsultaBCPService.java` |
| `ConsultarClearingFinanciero` | 865 | `javacall/liq/ConsultarClearingFinancieroService.java` |
| `ConsultarComprobantes` | 103 | `javacall/ConsultarComprobantesService.java` |
| `ConsultarComprobantesWS` | 355 | `javacall/ConsultarComprobantesWSService.java` |
| `ConsultarFormatoArchivo` | 95 | `javacall/ConsultarFormatoArchivoService.java` |
| `ConsultarIngresoEgreso` | 352 | `javacall/ConsultarIngresoEgresoService.java` |
| `ConsultarLibretaBanco` | 502 | `javacall/ban/ConsultarLibretaBancoService.java` |
| `ConsultarSaldoCuentaBanco` | 496 | `javacall/ban/ConsultarSaldoCuentaBancoService.java` |
| `CrearCargaMasivaPac` | 183 | `javacall/CrearCargaMasivaPacService.java` |
| `CrearCargaMasivaPacRen` | 220 | `javacall/CrearCargaMasivaPacRenService.java` |
| `CrearComprobante` | 439 | `javacall/CrearComprobanteService.java` |
| `CrearComprobanteJava` | 895 | `javacall/CrearComprobanteJavaService.java` |
| `CrearComprobantePartidaInEg` | 574 | `javacall/CrearComprobantePartidaInEgService.java` |
| `CrearIngresoEgreso` | 1427 | `javacall/CrearIngresoEgresoService.java` |
| `CrearTraspaso` | 229 | `javacall/CrearTraspasoService.java` |
| `CuadraturaCierreDiario` | 348 | `javacall/CuadraturaCierreDiarioService.java` |
| `DistribucionPac` | 124 | (no service: POJO/constant/out of proof, see notes) |
| `EdicionMultiple` | 213 | `javacall/EdicionMultipleService.java` |
| `EjecutarAnulacionComprobantes` | 131 | `javacall/EjecutarAnulacionComprobantesService.java` |
| `EjecutarAnulacionIngresoEgreso` | 143 | `javacall/EjecutarAnulacionIngresoEgresoService.java` |
| `GestionDeDepositos` | 408 | `javacall/dep/GestionDeDepositosService.java` |
| `GirarComprobanteIngreso` | 224 | `javacall/GirarComprobanteIngresoService.java` |
| `GirarComprobantesEgreso` | 365 | `javacall/GirarComprobantesEgresoService.java` |
| `IngresarMandatoPAC` | 558 | `javacall/IngresarMandatoPACService.java` |
| `IngresarMandatoPACManejador` | 180 | `javacall/IngresarMandatoPACManejadorService.java` |
| `IngresoEgreso` | 74 | (no service: POJO/constant/out of proof, see notes) |
| `IngresoEgresoFormaDePago` | 45 | (no service: POJO/constant/out of proof, see notes) |
| `LiquidacionMasivaComprobante` | 623 | `javacall/LiquidacionMasivaComprobanteService.java` |
| `LiquidarComprobante` | 1279 | `javacall/LiquidarComprobanteService.java` |
| `LiquidarComprobanteTodos` | 175 | `javacall/LiquidarComprobanteTodosService.java` |
| `LiquidarMovimientoAutomatico` | 412 | `javacall/LiquidarMovimientoAutomaticoService.java` |
| `ModificarComprobanteTesoreria` | 194 | `javacall/ModificarComprobanteTesoreriaService.java` |
| `ModificarPAC` | 514 | `javacall/ModificarPACService.java` |
| `PreLiquidarComprobante` | 541 | `javacall/PreLiquidarComprobanteService.java` |
| `ProcesarCargaMasiva` | 584 | `javacall/car/ProcesarCargaMasivaService.java` |
| `ProcesarClearingFinanciero` | 558 | `javacall/liq/ProcesarClearingFinancieroService.java` |
| `ProcesarDistribucion` | 2066 | `javacall/ProcesarDistribucionService.java` |
| `QuitarDetalleNominasTransferencias` | 161 | `javacall/QuitarDetalleNominasTransferenciasService.java` |
| `RecaudacionBCP` | 872 | `javacall/RecaudacionBCPService.java` |
| `RequisitosGirarComprobanteEgreso` | 341 | `javacall/RequisitosGirarComprobanteEgresoService.java` |
| `RequisitosGirarComprobanteIngreso` | 302 | `javacall/RequisitosGirarComprobanteIngresoService.java` |
| `RevocacionMandatoPAC` | 252 | `javacall/RevocacionMandatoPACService.java` |
| `SaldoPendienteDeLiquidar` | 405 | `javacall/SaldoPendienteDeLiquidarService.java` |
| `Traspaso` | 158 | (no service: POJO/constant/out of proof, see notes) |
| `ValidarTransferenciaDVP` | 159 | `javacall/ValidarTransferenciaDVPService.java` |

Not services: `Camara`, `CamaraCompensacion`, `IngresoEgreso`, `IngresoEgresoFormaDePago`, `Traspaso`, `ConstantesTesoreria` are data holders used by the services (ported as POJOs). Out of the proof: `Cloper`, `CloperWsBSC` (real HTTPS calls, `org.json` absent), `DistribucionPac` (dead POJO that does not compile in the legacy).

Shared pieces (new): ports `Colaboradores`, `DiccionarioMensajes`, `ConfiguracionSistema`, `Secuencias`, `Reloj`, `AlmacenArchivos` (`port/out`); adapters `ColaboradoresJavaCall`, `ProcedimientoAlmacenado`, `DiccionarioJdbc`, `ConfiguracionSistemaJdbc`, `RelojSistema`, `SecuenciasJdbc`, `AlmacenArchivosLocal` (`infrastructure/adapter`); `TokenInternoFilter` (`infrastructure/web`, fail-closed `X-Internal-Token`); `JavaCallDispatcher` and a rewritten `InternalJavaCallController` (400 for malformed input, 503 for unknown classes, 500 with a generic message and a correlation id); shared kits `javacall/comun`, `javacall/contrato`, `javacall/kit` (named-parameter SQL helper, typed collaborator contracts). SQL lives in `src/main/resources/sql/`.

## Representative behavior, side by side

Legacy `RecaudacionBCP` hard-codes a certification client id (`"67412388"`) that forces `CLIENTE_NO_ACTIVO`; the legacy defect is reproduced (TES-B-75) and the BCP DNIs are listed for the SME:

```
legacy:  ... IdClienteBcp.equalsIgnoreCase("67412388") ...        // RecaudacionBCP.java:738
modern:  p.requisito(diccionario, ID_EMPRESA, pago && p.IdClienteBcp.equalsIgnoreCase("67412388") && existe, "CLIENTE_NO_ACTIVO", ...)   // RecaudacionBCPService:266, LEGACY DEFECT kept (TES-B-75)
```

## Deliberate deviations

- **None in behavior.** About 150 legacy defects found by the 856 recorded cases are reproduced and marked `LEGACY DEFECT kept (see README TES-A-NN | TES-B-NN)`; the full lists are in `src/test/java/cl/bcs/tesoreria/familia_a/README.md` and `familia_b/README.md` ("Legacy defects for SME review"). Examples: `RecaudacionBCP` hard-coded certification DNIs, `ProcesarClearingFinanciero` stamps the last group's comprobante on every transaction, `ModificarPAC` `FolioCSC.equals(0L)` is always false, `ProcesarDistribucion` never fills `MovimientoGenerado`, `GestionDeDepositos` compares against `" TES.DepositoDetalle"` (leading space), `AutorizacionLiquidacion` never reports success.
- **Order pinned by a golden:** `ProcesarDistribucion` iterates its groups in String-hash `HashMap` order (TES-B-306, 308), `CrearIngresoEgreso` `LimpiarSalida` and `AnularRegistros` follow hash order; reproduced and commented.
- **Structure:** the legacy called other services in process; the modern code goes through the ports (in process for this module's classes, HTTP to `{JAVACALL_URL}/internal/java-call` otherwise). The pilot `domain/service` classes (CrearIngresoEgreso, LiquidarComprobante, Girar*, ConfirmarRechazar, the three mandate services) and their five trace-based tests were deleted: their bean names collided with the `javacall` ones and `javacall/` is the single path. Their models, `TesoreriaRepository`, `TesoreriaJdbcRepository` and the orchestrator remain as dead code that the SOAP skeleton still uses.
- **Test infrastructure:** one UTC fork for the whole module (H2 caches the JVM zone). Goldens carry no run-day date: the `FechaSistema` / `FechaProceso` columns are masked as `<TS>` per table (TES-B-319 and 320 broke the day after they were recorded before that).

## Not migrated / not proven

- **Stored procedures:** `EdicionMultiple`, `CrearComprobante` with `USARCREARCOMPROBANTEJAVA=N` and the `LiquidacionMasiva` call are pinned only on their Java side (recorded as the collaborator `StoredProcedure`; their output tables are pre-seeded). The `ProcedimientoAlmacenado` adapter (marker numbering `:x`/`@x`/`$x`/`#t`, call text from `sql/Procedimiento_<name>.sql`) is tested on fakes only. `EdicionMultiple` is called from two services with slightly different inputs and one call text.
- **Cases left out of the proof:** TES-A-345 and 355 (an update-join with duplicated source rows fails only in the H2 MERGE emulation), TES-A-429 and 443 (H2 conversion or parse text instead of what HANA gives), the stock-order path of `ProcesarDistribucion` (proven only against scripted collaborators: `CalcularMontoYCobrosOrden` and `CrearOrdenes` contracts come from hand-written shims), branches of `ProcesarDistribucion` unreachable through the legacy SQL, and queries without `ORDER BY` that return several groups per key (HANA's order decides).
- **Cloper:** no production client exists; `ActualizarTransaccion` reports `ERROR_AL_CONSULTAR_CLOPER_WS` and the controller answers 503 for `Cloper` so the router falls back to TomEE. A real adapter must read `PLATAFORMA/PLA.CloperWsBSC.*` itself.
- **SOAP (`SvcRecaudaInqV1`, `SvcRecaudaTrnV1`, namespace `http://mfxV2_0`):** captured STATICALLY (`src/test/resources/soap/contract/*.contract.json`, copy under `analysis/.../equivalence/BcsTesoreriaServicios/soap-contract/`; 61 JAXB types ported to `jakarta.*` annotations in `infrastructure/soap/`; `SoapContratoEstaticoTest` 14 tests, canary: 3 mutations turned it red, `canary/BcsTesoreriaServicios-soap/`). It does NOT prove a WSDL: there is no real WSDL in the repo, no runtime (CXF/JAX-WS/JAXB) offline, no `wsgen`, no wire XML, no consumer validation. The legacy ObjectFactory registers `MFX_SvcRecaudaTrn` and `SvcRecaudaTrnRq/Rs` but not the Inq equivalents: check against the real WSDL. The modern endpoints are still the skeleton: they take simplified records, do not implement the PortType or the `Holder` signature, and do not reproduce the legacy port impls (full MFX response building, the CP0010/CP0141/CP0138 paths, the certification hack `IdClienteBcp == 13602934`, the `ConsultaBCP`/`RecaudacionBCP` call with commit or rollback). Real wiring needs a runtime choice, WSDL generation and a consumer check.
- **Assumed on H2 and not verified on HANA:** the schema (106 tables inferred from the legacy SQL, HANA views modeled as seeded tables, `PAC.LogMandatoPAC.FechaProceso` as DATE, `CAR.CargaMasivaDetallePAC.MontoCargo` as DECIMAL, `M_TEMPORARY_TABLES` empty), the fixed clock 2025-04-01 10:15:30 UTC, the opencsv 4.x semantics (a shim: only 5.9 is available offline), `SecuenciasJdbc` (`SELECT "<seq>".NEXTVAL FROM DUMMY`, name whitelisted), `GBL.*_Paso` staging tables, `REG.DiaFeriado`, session temp tables.
- **Cross-module collaborators** are proven only against scripted doubles: `RevisarRequisitosBasicos`, `CrearIngresoEgreso`, `LiquidarComprobante`, `CrearComprobante`, `CrearCargoAbono`, `AnularCargoAbono`, `AgregarPrograma`, `AgregarLog`, `CrearEventoAutorizacionMasivo`, `AnulacionContratoCSC`, `RevocacionContratoCSC`, `CambiarEstadoContratosSAAPV`, `CalcularParidad`, `CalcularMontoNumeroCuota`, `CrearSolicitudAporteRescateNormal`, `ConsultarInstrumentoPrecioBolsa`. `ObtenerPortafoliosEjecutivo` (+ `restringeCartera`) has no JSON entry on the router yet; the authorization hooks are exposed as `...AutorizacionLiquidacion.autorizarEvento` / `.rechazarEvento`, the router convention is open.
- **Configuration to set:** `tesoreria.archivos.base-dir` (must equal the platform's base folder for files), `services.javacall.url` / `JAVACALL_URL` (no default), `services.internal-token` / `INTERNAL_TOKEN` (empty rejects every `/internal/**` call; set the same secret in the router and in the services that receive collaborator calls).

## Proof the tests can fail (Canary)

Mutations of `src/main` in a scratch copy outside `modernized/` (evidence in `analysis/otr-sva-optimus-corredores/equivalence/canary/BcsTesoreriaServicios-transform/`):

- `RecaudacionBCPService:266` certification DNI `"67412388"` to `"67412389"` -> **1 test failed** (26 run).
- `CrearIngresoEgresoService:1012` `compareTo(ZERO) < 0` to `<= 0` -> **4 tests failed** (60 run).
- `CrearTraspasoService:186` `Math.abs` removed on one side of the reference match -> **3 tests failed** (13 run).

Two mutations SURVIVED and are kept as findings (not counted as evidence): `GestionDeDepositosService:171` (`"TES.DepositoDetalle"` renamed: that branch is unreachable because of the legacy leading-space defect, so no case can observe it) and `CrearTraspasoService:168` (`Math.abs` removed on the first side of the `salida` / `egreso` match: no case has a negative reference there). The Step 1 agents also mutated copies of the LEGACY classes and re-recorded: family A six mutations changed 3 to 14 goldens each, family B five mutations changed 1 to 4 each (the `ModificarPAC` one first changed none and exposed a gap, closed by TES-B-330).

## How to run and re-record

- Run: `mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :tesoreria-service clean test`
- Dual-execution dump: add `-Dequivalence.out=<dir>`; each comparison writes the canonical legacy and modern text to `<dir>/legacy|new/<familia>/`; the harness self-checks are excluded.
- Re-record goldens: see the READMEs of `familia_a` and `familia_b` (`legacy-oracle/record.sh`, `mutation-check.sh`).

## Follow-ups

- Refactor (user decision, after the proof): split each service into a JavaCall adapter (JSON legacy <-> typed command) and a typed domain service, with the tests as safety net; later the adapter can move to the gateway with the `PLA.Servicio` registry.
- SOAP: choose a runtime, rewrite the endpoints to the Holder-based PortType, port the Inq/Trn response building, generate and compare a real WSDL, validate with the BCP consumer.
- HANA: verify the SQL, the stored-procedure calls and the sequence syntax; confirm the schema assumptions against the real DDL; characterize the stored procedures with a real HANA.
- `Girar Ingreso/Egreso` swallow exceptions after calling a transactional `CrearComprobante`: under Spring proxies a throw through it marks the transaction rollback-only and the commit raises `UnexpectedRollbackException`, instead of the legacy partial commit (the tests do not use proxies). `LiquidarMovimientoAutomatico` rolls back with `Connection.rollback()` as the legacy did, bypassing the transaction manager. Decide: keep, call a non-transactional entry point, savepoint, or `setRollbackOnly`.
- Services that create session temp tables drop them only on normal ends.
- `SqlNombrado` is created with `new` inside services (stateless wrapper); several private comment-stripping SQL loaders could be consolidated in `javacall/comun`; `CargaMasiva` staging job id still uses `ThreadLocalRandom`.
- Production adapters for `Cloper` and for the portfolio collaborators; agree the router convention for the authorization hooks.
- Architecture review (Step 5): see below (4 HIGH fixed, MEDIUM and LOW open).

## Architecture review (Step 5, architecture-critic, static read: nothing run; not reviewed: ~170 services in depth, `SvcRecaudaInqV1Endpoint`, the JAXB classes, SQL resources in bulk, BcsUtils, the platform router, HANA behavior, CVEs)

**HIGH (4, all fixed and each proved through the real wiring: `TesoreriaCableadoRealTest`, `ColaboradoresJavaCallEnrutamientoTest`, `TokenInternoFilterTest`; the suite alone could not see them because it builds services with `new` and scripts the `Colaboradores` seam):**
- **H1 collaborator routing:** `ColaboradoresJavaCall` fell through to the router with a bare class name for every collaborator that was not in a 12-entry map (including this module's own `CrearComprobante`, `CrearIngresoEgreso` and `LiquidarComprobante`), so the mass load, deposits and distribution would have failed in production. Now an explicit map of 25 names (5 in process with their FQN, 20 remote with the legacy FQN under a prefix the router maps), an unknown or null name throws `IllegalArgumentException`, and a test scans every `invocar(...)` literal in `src/main`.
- **H2 `Girar Ingreso/Egreso` under Spring proxies:** a throw through the transactional `CrearComprobante` marked the shared transaction rollback-only and the commit raised `UnexpectedRollbackException` (HTTP 500, partial work lost) instead of the legacy "return the empty result and keep the partial work". The simpler fix (a non-transactional entry point) is not enough because `CrearComprobante` itself calls another transactional proxy. Fixed with `infrastructure/config/TransaccionesLegacyConfig` (`DataSourceTransactionManager.setGlobalRollbackOnParticipationFailure(false)`): the outermost boundary rolls back only when the exception reaches it, as the legacy model did. It applies to the whole module (about 37 swallow sites, ten with a proxied collaborator); only the Ingreso variant has its own test with a real transaction manager on H2.
- **H3 `TokenInternoFilter` bypass:** `/internal;x=1/java-call` or `//internal/java-call` did not start with `/internal/` and skipped the filter. Now an allow-list (only `/actuator/health` and `/actuator/info`, after removing path parameters and repeated slashes): everything else needs the token. The same filter in `BcsCuentaInversionServicios` had the same hole and was fixed in the same commit.
- **H4 Cloper:** the adapter throws `UnsupportedOperationException` for `Cloper` (503, so the router falls back to TomEE) but `ActualizarTransaccion` swallowed it and answered 200 with `ERROR_AL_CONSULTAR_CLOPER_WS`. Now `UnsupportedOperationException` escapes (WARN with stack, controller answers 503); every other runtime failure keeps the legacy swallow the 20 cases pin.

**MEDIUM (open, for a person to schedule):**
- M1 session temp tables dropped only on normal ends (`Girar*`, `Requisitos*Girar`, `ConfirmarAutorizar*`); in `EdicionMultiple` and `LiquidarMovimientoAutomatico` an unguarded `DROP` in a `finally` can mask the original exception and runs after the procedure wrote data. Needs real HANA to know whether `CREATE`/`DROP LOCAL TEMPORARY TABLE` commit implicitly (then the simulation `S` rollback would not undo what the procedure wrote).
- M2 `Connection.rollback()` bypasses the transaction manager (`LiquidarMovimientoAutomatico`, `LiquidarComprobanteTransaccion`, used by `LiquidarComprobante`, `PreLiquidar`, `LiquidarComprobanteTodos`): correct only when the service is the outermost transaction; a joined caller's earlier writes would be discarded without Spring knowing. Use `setRollbackOnly` when `isNewTransaction()`, with a joined-transaction test.
- M3 `ProcedimientoAlmacenado`: no injection or path issue (name sanitized, call text from the classpath, values bound); the marker numbering (`$` table outputs before `@` scalar outputs) is unproven on HANA (`registerOutParameter(6, ...)` addresses the first `$` placeholder if the driver ignores table-output `?`); `cs.getString(name)` is driver-dependent; comment stripping also removes `--` inside literals; no `setQueryTimeout`; a missing `procedimiento` key gives a 500.
- M4 dead pilot code that is still live as beans (`TesoreriaJdbcRepository` with 16 of 19 methods throwing, `BcsTesoreraOrchestrator`, ~30 `domain/model` records, `PlataformaRestClient` with no timeout or token and a default pointing at itself); inert because the SOAP endpoints are not published; `TesoreriaOrchestratorTest` pins dead code.
- M5 `ConfiguracionSistemaJdbc.parametro` reads the whole parameter table on every lookup and filters in Java (no cache, "last row wins" is accidental).
- M6 router errors are not mapped (`RestClientResponseException`, timeouts) and the 60 s read timeout holds a DB transaction open; local collaborators join the caller's transaction while remote ones commit separately (no compensation).
- M7 port mismatch: the Dockerfile, README and gateway say 8073 but no `server.port` is set (the app listens on 8080). M8 the Dockerfile runs as root (no `USER`, no `HEALTHCHECK`).
- M9 no lock or isolation on balance updates (`ActualizarSaldosCuentaBanco` reads, computes and updates; two concurrent calls on one account lose an update, as under TomEE; no concurrency test).
- M10 `CargaMasivaIngresoEgresoDAPFFMM` inserts staging rows with a random id and never deletes them (confirm in the legacy whether the table grows); use a sequence instead of `ThreadLocalRandom`.

**LOW:** `jsch` and `commons-io` unused in `src/main`; `spring.batch.jdbc.initialize-schema` with no batch starter; `management.endpoints.web.exposure.include=health` not set; `AlmacenArchivosLocal` follows a symlink in the last path component and overwrites silently (use `CREATE_NEW` or temp file plus move; `tesoreria.archivos.base-dir` absent from `application.properties`); `SecuenciasJdbc` draws one value per call where the legacy drew `cantidad`; duplicated helpers (`JsonLegacy`, `MensajeSalida`, `SqlNombrado` in both `comun/` and `kit/`, two `ProcedimientoAlmacenado` classes); `log.debug(e.getMessage())` in the Cloper path loses the stack (now WARN with stack); the controller echoes the client-supplied class name in its 500 and 503 messages; `CsvPlano` allows formula injection if the CSV is opened in Excel. Checked and NOT present here (unlike `BcsCuentaInversionServicios`): `@Transactional` without `rollbackFor`, a default `JAVACALL_URL`, a `RestClient` without timeouts or token toward the router, internal error text returned to the caller, services created with `new` inside services, a poi dependency. `SqlNombrado` created with `new` is harmless (a stateless wrapper over the injected transaction-aware template).

**What the later refactor (JavaCall adapter + typed domain services) must untangle:** two call styles for one target (typed bean call versus name and JSON through `Colaboradores`); the `execute`/`logica` pair already is a good seam but `execute` owns policy (error recounting, simulation rollback, swallowed-exception behavior) that belongs to the adapter; the legacy type shapes (`BcsPojo` public fields, the `P_OUT_*` convention, null-list defaults that are NPE-as-behavior) are the domain API today; swallowed exceptions are domain behavior (transaction semantics per use case); the `StoredProcedure` and temp-table protocol needs an explicit unit of work that holds the connection; in-process versus remote collaborators have different transaction semantics; `SqlNombrado`'s `SQLException` type and the `setScale(16)` batching are contract details pinned by the goldens; simulation by rollback becomes `setRollbackOnly` or a read-only path.

**Related, other module:** `BcsCuentaInversionServicios` has no routing hole (its call sites pass FQN constants), but its `RestClient` has no connect or read timeouts and it does not set `globalRollbackOnParticipationFailure=false` (it may have the H2 problem); not run.
