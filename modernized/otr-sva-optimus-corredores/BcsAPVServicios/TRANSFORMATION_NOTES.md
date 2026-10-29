# BcsAPVServicios — Transformation Notes

Replaces the pilot notes (Object-typed stubs, 67 stubs): the pilot code was deleted on 2026-10-20 (`limpiar-piloto-apv.ps1`, 158 files).

## Summary

| Item | Detail |
|---|---|
| Legacy module | `legacy/otr-sva-optimus-corredores/BcsAPVServicios` (73 files, 31 950 lines; 66 JavaCall classes, 64 invocable) |
| Target | Spring Boot 4.1.1 / Java 25, three Maven modules: `bcs-apv-api` (typed records + `casouso.*` interfaces), `bcs-apv-javacall-adapter` (legacy JSON <-> typed, registry, pipeline), `apv-service` (use cases, ports' adapters, REST) |
| Decision | Option 1 (user): typed domain + REST in the service; JavaCall translation lives in the separate adapter library for the gateway |
| Tests | 1169 run, 0 failures, 0 skipped (BcsAPVApi 1, adapter 12, apv-service 1156); includes the typed `ValidarMandatosPAC.logica` entry (30 cases + 4 transaction tests + 4 mapper tests) |
| Equivalence | 939 characterization cases (A 334, B 240, C 365) recorded from the UNMODIFIED legacy on H2; dual execution `4214 of 4214 same, 0 differ` (includes the 14 authorization-hook cases APV-A-526..539, so family A is 348 cases and the total 953; plus the 30 typed-logica cases APV-C-480..509: family C 395, total 983, dual dump `4324 of 4324 same` after adding them) (`analysis/.../equivalence/BcsAPVServicios/cases.json`, `compare.py`) |
| Equivalence is | executed-legacy based (legacy classes ran on H2 with minimal runtime shims), not trace-only |

## Mapping (legacy class -> target)

Each legacy class becomes one use case `application/<familia>/<Caso>Servicio` (A contratos, B traspasos, C recaudacion) that implements `cl.bcs.apv.api.casouso.<Caso>`; the legacy JSON shape is reproduced by `bcs-apv-javacall-adapter` (`mapeo/<Caso>Mapeo`). Legacy `call(JsonObject)` bodies were ported statement by statement in behavior (not structure): SQL kept as resources in `src/main/resources/sql`, transactions as class-level `@Transactional` with the legacy mid-flow commits reproduced.

| Legacy class (`cl.bcs.APV.*`) | Family | Typed use case (`cl.bcs.apv.application.*`) | Route | Invocable |
|---|---|---|---|---|
| AnularTraspasoEgreso | B | `AnularTraspasoEgresoServicio` (typed `AnularTraspasoEgreso` in `bcs-apv-api`) | POST /apv/traspasos/anulacion | true |
| AsignarContratoARecaudacion | A | `AsignarContratoARecaudacionServicio` (typed `AsignarContratoARecaudacion` in `bcs-apv-api`) | POST /apv/recaudaciones/asignacion-contrato | true |
| AsignarDepositoEmpleador | C | `AsignarDepositoEmpleadorServicio` (typed `AsignarDepositoEmpleador` in `bcs-apv-api`) | POST /apv/recaudaciones/depositos/asignacion-empleador | true |
| AsociarPerfilesAPV | A | `AsociarPerfilesAPVServicio` (typed `AsociarPerfilesAPV` in `bcs-apv-api`) | POST /apv/perfiles/asociacion | true |
| BusquedaContratoSAAPVRecaudacion | A | `BusquedaContratoSAAPVRecaudacionServicio` (typed `BusquedaContratoSAAPVRecaudacion` in `bcs-apv-api`) | POST /apv/contratos/busqueda-recaudacion | true |
| CalculoMontosDesinversion | B | `CalculoMontosDesinversionServicio` (typed `CalculoMontosDesinversion` in `bcs-apv-api`) | POST /apv/traspasos/calculo-desinversion | true |
| CalculoRetencionTraspasos | B | `CalculoRetencionTraspasosServicio` (typed `CalculoRetencionTraspasos` in `bcs-apv-api`) | POST /apv/traspasos/calculo-retencion | true |
| CambiarAPlanActivoContratoSAAPV | A | `CambiarAPlanActivoContratoSAAPVServicio` (typed `CambiarAPlanActivoContratoSAAPV` in `bcs-apv-api`) | POST /apv/contratos/plan-activo | true |
| CambiarAPlanActivoContratoSAAPVWrapper | A | `CambiarAPlanActivoContratoSAAPVWrapperServicio` (typed `CambiarAPlanActivoContratoSAAPVWrapper` in `bcs-apv-api`) | POST /apv/contratos/plan-activo/solicitud | true |
| CambiarEstadoContratosSAAPV | A | `CambiarEstadoContratosSAAPVServicio` (typed `CambiarEstadoContratosSAAPV` in `bcs-apv-api`) | POST /apv/contratos/cambio-estado | true |
| CambiarEstadoEnvioNotificacionContratoSAAPV | A | `CambiarEstadoEnvioNotificacionContratoSAAPVServicio` (typed `CambiarEstadoEnvioNotificacionContratoSAAPV` in `bcs-apv-api`) | POST /apv/contratos/notificacion/cambio-estado | true |
| CambiarEstadoRecaudacion | C | `CambiarEstadoRecaudacionServicio` (typed `CambiarEstadoRecaudacion` in `bcs-apv-api`) | POST /apv/recaudaciones/cambio-estado | true |
| CambiarEstadoRevocacionSAAPV | A | `CambiarEstadoRevocacionSAAPVServicio` (typed `CambiarEstadoRevocacionSAAPV` in `bcs-apv-api`) | POST /apv/contratos/revocacion | true |
| CambiarEstadoTraspaso | B | `CambiarEstadoTraspasoServicio` (typed `CambiarEstadoTraspaso` in `bcs-apv-api`) | POST /apv/traspasos/cambio-estado | true |
| CargaBonificacionTGR | C | `CargaBonificacionTGRServicio` (typed `CargaBonificacionTGR` in `bcs-apv-api`) | POST /apv/bonificaciones/carga-tgr | true |
| CargaMasivaArchivo | A | `CargaMasivaArchivoServicio` (typed `CargaMasivaArchivo` in `bcs-apv-api`) | POST /apv/contratos/carga-masiva | true |
| CargaMasivaBanco | C | `CargaMasivaBancoServicio` (typed `CargaMasivaBanco` in `bcs-apv-api`) | POST /apv/recaudaciones/carga-masiva/banco | true |
| CargaMasivaPAC | C | `CargaMasivaPACServicio` (typed `CargaMasivaPAC` in `bcs-apv-api`) | POST /apv/pac/carga-masiva | true |
| CargaMasivaPrevired | C | `CargaMasivaPreviredServicio` (typed `CargaMasivaPrevired` in `bcs-apv-api`) | POST /apv/recaudaciones/carga-masiva/previred | true |
| CargaMasivaRecaudacion | C | `CargaMasivaRecaudacionServicio` (typed `CargaMasivaRecaudacion` in `bcs-apv-api`) | POST /apv/recaudaciones/carga-masiva | true |
| CargaMasivaSAAPVPAC | C | `CargaMasivaSAAPVPACServicio` (typed `CargaMasivaSAAPVPAC` in `bcs-apv-api`) | POST /apv/pac/contratos/carga-masiva | true |
| CargaRecaudacion | C | `CargaRecaudacionServicio` (typed `CargaRecaudacion` in `bcs-apv-api`) | POST /apv/recaudaciones/carga | true |
| CargaRegistroHistoricoTraspaso | B | `CargaRegistroHistoricoTraspasoServicio` (typed `CargaRegistroHistoricoTraspaso` in `bcs-apv-api`) | POST /apv/traspasos/historico | true |
| ConciliarDeposito | C | `ConciliarDepositoServicio` (typed `ConciliarDeposito` in `bcs-apv-api`) | POST /apv/recaudaciones/depositos/conciliacion | true |
| ConsultarClienteContratoSAAPV | A | `ConsultarClienteContratoSAAPVServicio` (typed `ConsultarClienteContratoSAAPV` in `bcs-apv-api`) | POST /apv/contratos/cliente | true |
| ConsultarCustodiaPortafolioAPV | A | `ConsultarCustodiaPortafolioAPVServicio` (typed `ConsultarCustodiaPortafolioAPV` in `bcs-apv-api`) | GET /apv/portafolios/custodia | true |
| ConsultarPreferenciaPagoPortafolio | A | `ConsultarPreferenciaPagoPortafolioServicio` (typed `ConsultarPreferenciaPagoPortafolio` in `bcs-apv-api`) | GET /apv/portafolios/preferencia-pago | true |
| ConsultarTotalesDistribucionRecaudacion | C | `ConsultarTotalesDistribucionRecaudacionServicio` (typed `ConsultarTotalesDistribucionRecaudacion` in `bcs-apv-api`) | GET /apv/recaudaciones/distribucion/totales | true |
| ConsultarTotalesOrigenPorDeposito | C | `ConsultarTotalesOrigenPorDepositoServicio` (typed `ConsultarTotalesOrigenPorDeposito` in `bcs-apv-api`) | GET /apv/recaudaciones/depositos/totales-origen | true |
| CrearAnotacionRecaudacion | C | `CrearAnotacionRecaudacionServicio` (typed `CrearAnotacionRecaudacion` in `bcs-apv-api`) | POST /apv/recaudaciones/anotaciones | true |
| CrearBancoPrevired | C | `CrearBancoPreviredServicio` (typed `CrearBancoPrevired` in `bcs-apv-api`) | POST /apv/bancos-previred | true |
| CrearContratoSAAPV | A | `CrearContratoSAAPVServicio` (typed `CrearContratoSAAPV` in `bcs-apv-api`) | POST /apv/contratos | true |
| CrearOrdenTraspaso | B | `CrearOrdenTraspasoServicio` (typed `CrearOrdenTraspaso` in `bcs-apv-api`) | POST /apv/traspasos/ordenes | true |
| CrearRecaudacionCliente | C | `CrearRecaudacionClienteServicio` (typed `CrearRecaudacionCliente` in `bcs-apv-api`) | POST /apv/recaudaciones/clientes | true |
| CrearRecaudacionDeposito | C | `CrearRecaudacionDepositoServicio` (typed `CrearRecaudacionDeposito` in `bcs-apv-api`) | POST /apv/recaudaciones/depositos | true |
| CrearRecaudacionEmpleador | C | `CrearRecaudacionEmpleadorServicio` (typed `CrearRecaudacionEmpleador` in `bcs-apv-api`) | POST /apv/recaudaciones/empleadores | true |
| CrearTraspaso | B | `CrearTraspasoServicio` (typed `CrearTraspaso` in `bcs-apv-api`) | POST /apv/traspasos/creacion | true |
| CrearTraspasoTesoreria | B | `CrearTraspasoTesoreriaServicio` (typed `CrearTraspasoTesoreria` in `bcs-apv-api`) | POST /apv/traspasos/tesoreria | true |
| DevolucionBF_EntidadRecaudadora | C | `DevolucionBF_EntidadRecaudadoraServicio` (typed `DevolucionBF_EntidadRecaudadora` in `bcs-apv-api`) | POST /apv/bonificaciones/devolucion-entidad-recaudadora | true |
| DevolucionPagoRezagos | C | `DevolucionPagoRezagosServicio` (typed `DevolucionPagoRezagos` in `bcs-apv-api`) | POST /apv/recaudaciones/devoluciones/rezagos | true |
| EjecutarBonificacion | C | `EjecutarBonificacionServicio` (typed `EjecutarBonificacion` in `bcs-apv-api`) | POST /apv/bonificaciones/ejecucion | true |
| EliminarBancoPrevired | C | `EliminarBancoPreviredServicio` (typed `EliminarBancoPrevired` in `bcs-apv-api`) | POST /apv/bancos-previred/eliminacion | true |
| EliminarRegistrosRecaudacion | C | `EliminarRegistrosRecaudacionServicio` (typed `EliminarRegistrosRecaudacion` in `bcs-apv-api`) | POST /apv/recaudaciones/registros/eliminacion | true |
| GrabarContratoSAAPV | A | `GrabarContratoSAAPVServicio` (typed `GrabarContratoSAAPV` in `bcs-apv-api`) | POST /apv/contratos/grabacion | true |
| GrabarContratoTraspasoSAAPV | B | `GrabarContratoTraspasoSAAPVServicio` (typed `GrabarContratoTraspasoSAAPV` in `bcs-apv-api`) | POST /apv/traspasos/contratos | true |
| GrabarRecaudacionTraspaso | B | `GrabarRecaudacionTraspasoServicio` (typed `GrabarRecaudacionTraspaso` in `bcs-apv-api`) | POST /apv/traspasos/recaudaciones | true |
| GrabarTraspasos | B | `GrabarTraspasosServicio` (typed `GrabarTraspasos` in `bcs-apv-api`) | POST /apv/traspasos | true |
| IngresoRecaudacionTraspaso | B | `IngresoRecaudacionTraspasoServicio` (typed `IngresoRecaudacionTraspaso` in `bcs-apv-api`) | POST /apv/traspasos/recaudaciones/ingreso | true |
| ModificarAnotacionRecaudacion | C | `ModificarAnotacionRecaudacionServicio` (typed `ModificarAnotacionRecaudacion` in `bcs-apv-api`) | PUT /apv/recaudaciones/anotaciones | true |
| ModificarBancoPrevired | C | `ModificarBancoPreviredServicio` (typed `ModificarBancoPrevired` in `bcs-apv-api`) | PUT /apv/bancos-previred | true |
| ModificarBonificacion | C | `ModificarBonificacionServicio` (typed `ModificarBonificacion` in `bcs-apv-api`) | PUT /apv/bonificaciones | true |
| ModificarContratoSAAPV | A | `ModificarContratoSAAPVServicio` (typed `ModificarContratoSAAPV` in `bcs-apv-api`) | PUT /apv/contratos | true |
| ModificarOrigenDestino | B | `ModificarOrigenDestinoServicio` (typed `ModificarOrigenDestino` in `bcs-apv-api`) | PUT /apv/traspasos/origen-destino | true |
| ModificarPortafolioTraspaso | B | `ModificarPortafolioTraspasoServicio` (typed `ModificarPortafolioTraspaso` in `bcs-apv-api`) | PUT /apv/traspasos/portafolios | true |
| ModificarRelacionLaboral | A | `ModificarRelacionLaboralServicio` (typed `ModificarRelacionLaboral` in `bcs-apv-api`) | PUT /apv/contratos/relacion-laboral | true |
| ObtenerFolioSuscripcion | A | `ObtenerFolioSuscripcionServicio` (typed `ObtenerFolioSuscripcion` in `bcs-apv-api`) | POST /apv/contratos/folios-suscripcion | true |
| ProcesarBonificacion | C | `ProcesarBonificacionServicio` (typed `ProcesarBonificacion` in `bcs-apv-api`) | POST /apv/bonificaciones/proceso | true |
| ProcesarCargaMasivaRecaudacion | C | `ProcesarCargaMasivaRecaudacionServicio` (typed `ProcesarCargaMasivaRecaudacion` in `bcs-apv-api`) | POST /apv/recaudaciones/carga-masiva/proceso | true |
| ProcesarDistribucion | C | `ProcesarDistribucionServicio` (typed `ProcesarDistribucion` in `bcs-apv-api`) | POST /apv/recaudaciones/distribucion | true |
| ReaperturarTraspaso | B | `ReaperturarTraspasoServicio` (typed `ReaperturarTraspaso` in `bcs-apv-api`) | POST /apv/traspasos/reapertura | true |
| ReplicaPortafolioAPV | C | `ReplicaPortafolioAPVServicio` (typed `ReplicaPortafolioAPV` in `bcs-apv-api`) | POST /apv/portafolios/replica | true |
| TraspasoCuentasBancarias | B | `TraspasoCuentasBancariasServicio` (typed `TraspasoCuentasBancarias` in `bcs-apv-api`) | POST /apv/traspasos/cuentas-bancarias | true |
| ValidaContratoSAAPV | A | `ValidaContratoSAAPVServicio` (typed `ValidaContratoSAAPV` in `bcs-apv-api`) | POST /apv/contratos/validacion | true |
| ValidarYAsignarContratosARecaudaciones | A | `ValidarYAsignarContratosARecaudacionesServicio` (typed `ValidarYAsignarContratosARecaudaciones` in `bcs-apv-api`) | POST /apv/recaudaciones/validacion-asignacion-contratos | true |

The two classes whose `call()` returns null (`ValidarBancoPrevired`, `ValidarMandatosPAC`) stay null adapters (`invocable=false`, no route); the TYPED `ValidarMandatosPAC.logica` is a separate in-process entry (see "Typed entry ValidarMandatosPAC.logica"). `AutorizacionCambiarAPlanActivoContratoSAAPV` (authorization hook) is not exposed (needs its own port).

## Typed entry ValidarMandatosPAC.logica (gap closed 2026-10-21)

**Gap.** The legacy `ValidarMandatosPAC` has `call(JsonObject)` (returns null, APV-C-411) and the typed `logica(Connection, SalidaRecaudacionTraspaso, Long IdEmpresa, Long IdUsuario)`, which the Tesoreria job `BcsJobProcesarDistribucion` calls with its own connection. `logica` stores the PAC outcome in `BSCL.Tesoreria.BD::PAC.LogMandatoPAC`; the migration had only the `call()` null model, so nobody wrote those rows.

**Legacy behaviour (recorded).** The statement file `ValidarMandatosPAC_INSERT_LogMandatoPAC.sql` is an UPDATE (by IdMandatoPAC and FechaSistema = PLA.FechaSistema): it only rewrites rows that already exist; nothing is inserted. A_PROCESAR requests whose id is also in NO_PROCESADAS are dropped; the rest are written first as PRO / PreRendicion S / Glosa "Procesado."; NO_PROCESADAS are written as PEN / S / Glosa = EstadoRendicion; Usuario = text of IdUsuario; FechaProceso = CURRENT_TIMESTAMP; P_OUT_SOLICITUDES_PROCESADAS is ignored; null lists/elements/ids/user raise NullPointerException; config errors raise Exception / DateTimeParseException; SQL failures SQLException.

**Modern.** `cl.bcs.apv.application.recaudacion.ValidarMandatosPACLogicaServicio` (port `cl.bcs.apv.api.logica.ValidarMandatosPACLogica`, typed `ValidarMandatosPACLogicaEntrada` / `RegistroMandatoPAC` in `bcs-apv-api`, SQL copied byte for byte to `sql/rec/ValidarMandatosPAC_INSERT_LogMandatoPAC.sql`). `@Transactional(REQUIRED)` on the `NamedParameterJdbcTemplate` it is given: it joins the transaction/connection of the caller, never commits. It is NOT in `CasosUso.TODOS`, NOT in `ApvJavaCallRegistro` (64 operations unchanged), no REST route; `ApvCasosDeUso.crear` also returns it (key `ValidarMandatosPACLogica.class`) and `CasosDeUsoConfig` registers the bean. `call()` stays the null adapter.

**In-process call for BcsTesoreriaJobs.** `ApvJavaCallPipelineEnProceso.invocar("cl.bcs.APV.ValidarMandatosPAC.logica", json)` (constant `ValidarMandatosPACLogicaMapeo.OPERACION`; `ApvJavaCallPipelineEnProceso.esOperacionTipada(name)` for dispatcher predicates, since the registry does not know it). Answers null. JSON: `{"entrada": {"P_OUT_SOLICITUDES_A_PROCESAR": [{"IdMandatoPAC": 1, "IdentServicio": "S1", "EstadoRendicion": "..."}], "P_OUT_SOLICITUDES_NO_PROCESADAS": [...], <any other SalidaRecaudacionTraspaso field: accepted, ignored>}, "idEmpresa": 1, "idUsuario": 21}`; the typed object may also be under `"salida"` (what the job already sends) or flat. Absent list = empty, absent IdMandatoPAC = 0, explicit null stays null (NPE as the legacy). Exceptions reach the caller unwrapped (checked ones too, through a proxy).

**Cases.** APV-C-480..509 (30, `metodo: "logica"`, test class `ValidarMandatosPACLogicaTest`, recorded by the unmodified legacy `logica` through the oracle; no existing golden changed), `ValidarMandatosPACLogicaTransaccionTest` (rolled back / committed with the caller, rollback-only through a real proxy), `ValidarMandatosPACLogicaMapeoTest`.

**Limitations.** Only the three record fields used (IdMandatoPAC, IdentServicio, EstadoRendicion) and the two lists are decoded: a malformed unused field (for example a bad date) that the legacy Gson would reject is accepted. The legacy `log.write` lines are not reproduced. A service exception through the proxy marks the shared transaction rollback-only: a caller that swallows it and commits gets `UnexpectedRollbackException` (the job must let it propagate, as the legacy did). HANA: the UPDATE and the date comparison are proven on H2 only. `BcsTesoreriaJobs` still has to route this operation (its APV entry only knows registry names and currently sends `ValidarMandatosPAC` to the null `call()`).

## Deliberate decisions (approved by the user at Step 1)

1. Mid-flow commits of the legacy are reproduced. 2. Legacy exception class names are kept. 3. Reference comparisons (`==` on boxed/strings) kept as they are. 4. `Reloj` port replaces system date. 5. Legacy defects kept bug-for-bug (list below). 6. HANA schema is inferred from the legacy SQL (assumption, shared by oracle and modern code).

## Legacy defects kept (for the domain expert)

- `CargaMasivaSAAPVPAC` uses the system date as mandate date.
- `ValidaContratoSAAPV.logica` never sets the error counter, so `ERROR_VALIDA_PORTAFOLIO` is unreachable in `GrabarTraspasos` / `ModificarPortafolioTraspaso`.
- Rule-by-rule list of other quirks: `src/test/java/cl/bcs/apv/familia_{a,b,c}/README.md`.

## Not migrated / not proven

- HANA is not exercised: SecuenciasJdbc, ProcedimientoAlmacenado, the three seeded recaudacion views, the unique-violation (301) path of `GrabarContratoSAAPV` are pinned only on H2 / Java side.
- Not covered by cases: CargaBonificacionTGR batches over 1000 rows, ReplicaPortafolioAPV with data, ProcesarCargaMasivaRecaudacion with more than 2 deposits.
- Cross-module collaborators proven against scripted doubles only; routing of the 27 names tested (`ColaboradoresJavaCall`), nothing exchanged with the real services.
- REST/gateway proven by wiring tests (`RestRegistroConsistenciaTest` 5, `ApvCableadoRealTest` 7), not by the dual-execution cases. No traffic goes to the new core; gateway routes stay disabled.
- No real-proxy test for services with mid-flow commit/rollback (CargaMasivaPAC, CargaMasivaBanco, CargaBonificacionTGR, DevolucionBF, EjecutarBonificacion, ProcesarDistribucion, EliminarRegistros).
- SME sign-off on APV Chilean-regulation correctness is an external blocker (brief Phase 2 exit criterion).

## Canaries (Step 3)

Scratch copy outside `modernized/`; XML and log kept in `analysis/otr-sva-optimus-corredores/equivalence/canary/BcsAPVServicios-transform/<name>/`.

| Change | Tests run | Failed |
|---|---|---|
| `ProcesarBonificacionServicio:116` `subtract` -> `add` (`ProcesarBonificacionTest`) | 10 | 5 |
| `TraspasarEntreCuentasBancariasServicio:59` concept `"ITRAPV"` -> `"INGAPV"` (`TraspasoCuentasBancariasTest`) | 20 | 1 |
| `ModificarContratosServicio:184` `!…equals("EGRESO")` -> `…equals("EGRESO")` (`ModificarContratoSAAPVCasosTest`) | 26 | 3 |

Families also carry their own mutation tables (README of each family). Candidate lines not exercised as canaries (rounding `HALF_DOWN` in `EjecutarBonificacionServicio:358/362`, `setScale` in `CalcularMontosDesinversionServicio:178`) are left for the verify step.

## Follow-ups

- Kits are duplicated across families (SqlNombrado lacks `int[]` batch and plain SQLException; C's SqlLegacy/Sneaky/Colab/Plataforma/MensajeLegacy, B's SqlTraspasos/MensajeLegacy): consolidate.
- Checked exceptions through CGLIB arrive as `UndeclaredThrowableException`: the REST handler unwraps; the gateway in-process pipeline should use `Sneaky.causa`.
- `ConsultarEmpresa` payload `{idEmpresa}` vs a possible `p_IdEmpresa` from callers: confirm with the registry export.
- Phase 5 prerequisite: `PLA.Servicio` registry export for the gateway route translation.

## Architecture review (Step 5)

Applied (HIGH): **H1** peer-use-case exceptions unwrapped with `Sneaky.causa` in 6 callers + `ExcepcionChequeadaTrasProxyTest` (real `ProxyFactory`); **H2** temp-table create..drop in try/finally (6 sites); **H5** Dockerfile installs `bcs-apv-api`/adapter, compose gets `JAVACALL_URL` (default `bcs-plataforma-web:8117`, to confirm) and `INTERNAL_TOKEN` (required via `:?`), Crystal env removed, UTC/en JVM flags; **M6** adapter pipeline unwraps `UndeclaredThrowableException`; README rewritten. After the fixes: 1024 apv-service tests, dual-execution dump regenerated and byte-identical (4149 files).

HIGH status (H3 and H7 resolved by user decision; H4 and H6 open, need other modules):
- **H3** FIXED (user decision: always replicate legacy): REST builds each `Entrada` with the same legacy defaults as the adapter (`EntradasConDefectosLegacy` + Config: Jackson deserializers per record, resolver for the 4 GET endpoints). `RestEntradaConDefectosLegacyTest` (69 tests) proves for all 64 operations that REST with `{}`/no params yields an object equal to the adapter's. Canary (disable the wiring): 50 of 69 fail (`canary/BcsAPVServicios-transform/rest-sin-defectos`). Not tested: a non-numeric GET parameter (returns the existing 400).
- **H4** collaborator calls go over HTTP from inside the open APV transaction (uncommitted rows invisible to Tesoreria/CuentaInversion, orphan risk, connection held up to 60 s). Needs an integration test against the real services before any route is enabled.
- **H6** BcsAPVJobs client and the gateway route send no `X-Internal-Token`; Jobs default URL points to 8140 instead of 8083.
- **H7** authorization hook `AutorizacionCambiarAPlanActivoContratoSAAPV`: MIGRATED as `AutorizacionCambioPlanActivoServicio` (`cl.bcs.apv.application.autorizaciones`, typed port `AutorizacionCambioPlanActivo`), bug for bug, characterized by 14 cases recorded from the unmodified legacy hook (APV-A-526..539). Entry points for the new platform (replace the legacy `Class.forName(JavaAutorizacion).newInstance().autorizarEvento(conn, entrada)`): `POST /apv/autorizaciones/cambio-plan-activo/autorizacion` and `POST /apv/autorizaciones/cambio-plan-activo/rechazo`, token protected, LEGACY JSON names so the platform router needs no refactor: body `EntradaAutorizacion` `{p_IdEmpresa, p_IdUsuario, p_IdModulo, p_UsaReglasNegocio, p_Lista:[Evento legacy fields: IdEvento, IdEmpresa, IdModulo, IdUsuario, IdCliente (others ignored)]}`, answer `SalidaAutorizacion` `{P_OUT_MENSAJES:[{id,tipo,codigo,descripcion}], P_OUT_ERRORESCONTADOR, P_OUT_RESULTADO:[{evento, resultado}]}` (same Gson flavour `Sabor.PLANO` and `MensajeMapeo` as the JavaCall operations, nulls omitted; conversion in `AutorizacionJsonLegacy`, the use case and its typed port are unchanged; the earlier typed camelCase body is gone, nothing consumed it; unparseable JSON is a 400). `AutorizacionesControllerTest` round-trips the golden `salida` of 9 of the 14 cases. The platform must route the event type MOPLAC (AUT.TipoEvento `JavaAutorizacion` = `cl.bcs.APV.AutorizacionCambiarAPlanActivoContratoSAAPV`) to these routes, one call per batch of events of the type (with 2 or more events the legacy hook fails by design), and the detail rows of AUT.EventoDetalle must exist in the same database/transaction.

MEDIUM/LOW (listed, not applied): M1 exception-handler status mapping (415/406 -> 500, RestClient 4xx -> 503, UnsupportedOperationException -> 503); M2 `ConfiguracionSistemaJdbc` scans the whole table per lookup; M3 no request-size/pool/graceful-shutdown settings; M4 identity taken from the body (single shared token); M5 default zone/locale in `GrabarTraspasos`, `MapeoJson`, `Sabor` (pinned only by JVM flags); M7 kit duplication (see follow-ups); M8 raw `Connection.commit/rollback` under Spring's transaction manager (guard against read-only transactions); M9 `ConsultarEmpresa` key; M10 HANA-only paths (stored-procedure OUT params by name, `substring(indexOf("["))` of the HANA message, `NEXTVAL FROM DUMMY`); M11 unbounded `DiccionarioJdbc` cache; L1 `AlmacenArchivosLocal` leaf symlink; L2 `AlmacenArchivos` port unused by any use case; L3 comment-stripping regex; L4 router URL not validated, no retry/correlation id; L5-L8 minor (NPE on missing `procedimiento`, context-path vs token filter, SimpleDateFormat, full exception logging with identifiers); L9 equivalence is "same on H2", not on HANA.
