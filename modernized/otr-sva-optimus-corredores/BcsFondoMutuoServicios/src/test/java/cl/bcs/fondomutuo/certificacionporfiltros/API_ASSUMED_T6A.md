# API assumed by the F3 W3 T6-A tests (`CertificarPorFiltrosSolicitudAporteRescate`, IN-PROCESS ONLY)

The production code of T6-A does NOT exist yet (red before code). The tests (`ApiT6a`, `CerPfEquivalenciaTest`, `CerPfDeterminismoTest`, `CerPfReglasTest`, `CerPfPuertoTest`, `CerPfEstructuraTest`,
`CerPfCableadoTest` in `cl.bcs.fondomutuo.certificacionporfiltros`, and the harness `CasoCerPf` / `CerCosturaPf` in `cl.bcs.fondomutuo.certificacion`) COMPILE without it: the new classes are reached by
reflection in `ApiT6a` (the only file that knows their names) and every test that needs them fails with `AssertionError: T6-A not implemented: the production class ... does not exist yet`.

Decision of the user (approved): the class is called by a job of another module, so it is IN-PROCESS ONLY: **no** JavaCall handler, **no** FQN in `JavaCallDispatcher.LISTA_BLANCA` (stays 13), **no** list in
`fondomutuo.solicitud.listas-servidas` (an invented name still stops the start-up), **no** HTTP exposure. It needs a port, a read adapter and Spring wiring.

Oracle: the goldens of `src/test/resources/fondomutuo-cerpf/cases/` (86 cases, 20 runs each, 0 unstable) were recorded from the REAL legacy `CertificarPorFiltrosSolicitudAporteRescate` calling the REAL legacy
`CertificarSolicitudAporteRescate` (`legacy-oracle/record_cerpf.sh`). Rule ids: the cards `FM-CXF-01..06` of `RULES_FONDOMUTUO_F3_B.md` 8.2.

## 1. Classes (package `cl.bcs.fondomutuo.certificacionporfiltros`)

| Item | Shape the tests use |
|---|---|
| `CertificarPorFiltrosContrato` | `public final` holder of three `public static class`es with PUBLIC fields, legacy names, legacy declaration order: `Entrada` (`Long p_IdEmpresa, Long p_IdUsuario, String p_IdModulo, String p_SimularOperacion, String p_UsaReglasNegocio, String p_FechaCertificacion, String p_IdTipoCanalIngreso, List<InFiltros> p_Filtros = new ArrayList<>()`), `InFiltros` (`String IdEmisor, IdMoneda, IdSerieFondo, Nacional, TipoSolicitud, IdTipoMovimientoFondoMutuo, MoneyMarket; Long IdCarga`), `Salida` (`Long P_OUT_ERRORESCONTADOR = 0L; List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>(); List<CertificarAporteRescateContrato.ResultadoCertificacion> P_OUT_RESULTADO = new ArrayList<>()`). Public no-argument constructors. The plain Gson (`JsonLegacy.plano()`) must read and write them as the legacy does |
| `CertificarPorFiltrosPort` | `public interface`, ONE method: `Salida logica(Entrada entrada) throws Exception` (legacy `logica(con, entrada)` without the connection) |
| `CertificarPorFiltrosDatos` | `public interface`, ONE method: `List<CertificarAporteRescateContrato.InSolicitudes> solicitudes(InFiltros filtro, String fechaCertificacion, Long idEmpresa)`, no checked exception. One call = ONE `SELECT DISTINCT` of the legacy |
| `CertificarPorFiltrosDatosJdbc` | `public class`, `implements CertificarPorFiltrosDatos`, ONE public constructor `(ConexionUnidadDeTrabajo)`, final fields only. Reads `cl/bcs/fondomutuo/sql/CertificarPorFiltrosSolicitudAporteRescate_SelectSolicitudes.sql` through `SqlRecursos.modulo`, VERBATIM (SHA-256 `1187dfe3e85966b8ebb9ccb84c1c68fe84263d024f5b6b680652bbd0eb0e5b0a`, no ORDER BY added) with `SqlNombrado.consultar(con, sql, params, InSolicitudes.class)`; params `IdEmisor, IdMoneda, IdSerieFondo, Nacional, TipoSolicitud, IdTipoMovimientoFondoMutuo, MoneyMarket, IdCarga, FechaCertificacion, IdEmpresa`, the filter fields passed AS THEY ARE (null stays null: the COALESCE of the SQL decides) |
| `CertificarPorFiltrosFachada` | `public class`, `implements CertificarPorFiltrosPort`, ONE public constructor `(CertificarAporteRescatePort, CertificarPorFiltrosDatos)`, final fields only, no static mutable state |
| `CertificarPorFiltrosConfiguracion` | `@Configuration`, exactly two `@Bean` methods: `CertificarPorFiltrosDatos` (the Jdbc adapter over the existing `ConexionUnidadDeTrabajo`) and `CertificarPorFiltrosPort` (the facade over the bean `CertificarAporteRescatePort` and that adapter). NO `LISTA` constant, NO `@Component` handler, NOT in the dispatcher, NOT in `ManejadorConfiguracion.LISTAS_*` |

No JSON class is added: the in-process caller passes the typed `Entrada`. The tests decode / encode with `JsonLegacy.plano()` (the legacy `call` uses plain `new Gson()`) only to feed the legacy JSON of the goldens.

## 2. Behaviour (`logica`, legacy C:93-131, card FM-CXF-nn)

1. Builds the entry of Certificar copying ONLY `p_IdEmpresa, p_IdUsuario, p_IdModulo, p_SimularOperacion, p_UsaReglasNegocio, p_FechaCertificacion, p_IdTipoCanalIngreso` (`p_NotificaOWP` stays `"S"` and
   `p_CertificacionAporteSwitchSimulado` stays `"N"`: FM-CXF-05). No validation of its own; a null header is copied as null.
2. For EACH filter, IN ORDER, one `Datos.solicitudes(filtro, entrada.p_FechaCertificacion, entrada.p_IdEmpresa)` and its rows are appended to `p_Solicitudes` with `addAll`: no de-duplication (the same id
   from two filters, or twice from one answer, stays repeated: Certificar rejects it as SOLICITUD_DUPLICADA, FM-CXF-03). ALL the queries run BEFORE Certificar is called.
3. Calls `CertificarAporteRescatePort.logica` EXACTLY ONCE, also with an EMPTY list (empty or absent `p_Filtros`, or filters with no rows): Certificar then answers NO_SE_INGRESARON_SOLICITUDES (FM-CXF-04).
4. Copies `P_OUT_ERRORESCONTADOR`, `P_OUT_MENSAJES`, `P_OUT_RESULTADO` of Certificar to its own `Salida` (a null counter stays null).
5. QUIRKS to keep bug for bug: `p_Filtros == null` (explicit JSON null) -> `NullPointerException` BEFORE any query or call; `entrada == null` -> `NullPointerException`; Certificar answering `null` ->
   `NullPointerException`; whatever the select or Certificar throws goes up UNCHANGED (same instance: checked, runtime, `Error`); a query that throws stops the loop (later filters are not queried and Certificar is
   not called). The request's own `p_Solicitudes` (the legacy `main()` sends one) is not part of the contract and is never forwarded.
6. RULE-076: only `logica` is ported, never the legacy `main`.

## 3. Test schema

`schema-cerpf.sql` (H2 stand-ins, in the fixtures) adds to the stand-in of the view `FMM.ConsultarSolicitudAporteRescate` the columns `EsFondoNacional` (NVARCHAR(1), default `S`) and `MoneyMarket` (NVARCHAR(1),
default `N`) and creates `FMM.DetalleCargaSolicitudesAporteRescate` with the real DDL of `FMM_Create_Tables.sql`. Production code needs nothing from it (the SQL is the legacy text).

## 4. Open questions (each with a recommendation)

* **Q-T6A-1 (is the in-process port enough for the job?).** The job of the other module (`BcsJobCertificacionMasivaSolicitudes`, `EsMasivo = "S"`) builds the filters by position of `;` lists (FM-CXF-06). It needs a
  typed `Entrada`, which this port gives; if it will instead call through the router (JSON) a handler would be needed, which the decision of the user excludes. Recommendation: keep the port typed.
* **Q-T6A-2 (typed result lists).** `Salida.P_OUT_RESULTADO` is assumed to be `List<CertificarAporteRescateContrato.ResultadoCertificacion>` (the type of Certificar) and not a copy of the pojo; the tests only
  compare JSON, so a different but JSON-identical type would also pass.
* **Q-T6A-3 (checked exception of the read port).** The tests assume `CertificarPorFiltrosDatos.solicitudes` declares no checked exception (the JDBC layer of the module is unchecked). If production prefers `throws
  SQLException`, change `losPuertosSonInterfaces` (one assertion) and the proxy of `ApiT6a.datosFalso` still works.
* **Q-T6A-4 (order of the rows inside one filter).** The select has no ORDER BY; the recorded order is the engine's (H2: ascending by id in every recorded case, stable in 20 runs). On HANA the order may
  differ: it is NOT pinned beyond H2, and Certificar partitions the list anyway. Recommendation: do not add an ORDER BY (verbatim SQL); decide with the owner if HANA order matters.
* **Q-T6A-5 (the 'Nacional' / 'MoneyMarket' empty string).** `Nacional = ""` or `MoneyMarket = ""` is NOT "no filter" (the COALESCE only maps null to `N`): it selects NOTHING (cases 24, 32). Pinned as the legacy
  does; the UI never sends it (to confirm with the owner).
