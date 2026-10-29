# API assumed by the FMM-FSA tests (ConsultarFechaSolicitudAporteRescate, F3 W1 front 1)

The production code does NOT exist yet. The tests of this package (`CasoFsa`, `SujetoFsa`, `FsaEquivalenciaTest`, `FsaDeterminismoTest`, `FsaReglasTest`,
`FsaCompuertaTest`) are written against EXACTLY the classes, constructors and methods below, all in package `cl.bcs.fondomutuo.consultas.fecha` (main source set)
unless another package is named. Legacy FQN: `cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate` (the handler answers to it). Design = F1/F2 style: service +
read port + JavaCall handler, closed by default, read only, so **no lock, no Tesoreria, no custody, no sequences, no write port, no G1..G5 gate**.
If production needs another shape, change the tests (not the golden): nothing of the comparison depends on these names except the entry points marked (*).

## 1. Classes

| Class | Kind | Members the tests use |
|---|---|---|
| `ConsultarFechaConfiguracion` | `@Configuration` | `public static final String LISTA = "CONSULTAR_FECHA_SOLICITUD"` (name in `fondomutuo.solicitud.listas-servidas`; closed when absent); `public static final String FQN = "cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate"`; the `@Bean ConsultarFechaPort` (not used by the tests: they build with the factory) |
| `ConsultarFechaContrato` | holder of the JSON types (like `RescateReinversionContrato`) | nested `public static class Entrada`, `Solicitud`, `Salida`, `Resultado`, `ConfiguracionSerie` (see 2) |
| `ConsultarFechaPort` (*) | interface | `ConsultarFechaContrato.Salida logica(ConsultarFechaContrato.Entrada entrada) throws Exception` (legacy `logica(con, entrada)` without the connection: the unit of work is the ambient Spring transaction, `readOnly`) |
| `ConsultarFechaService` | `implements ConsultarFechaPort` | constructor `(RevisarRequisitosBasicosPort basicos, EmpresaPort empresas, cl.bcs.fondomutuo.perfil.ObtenerPerfilFondoMutuo perfil, ConsultarFechaDatos datos, cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate fechas, DiccionarioMensajesPort diccionario)`; no state, no static state |
| `ConsultarFechaDatos` | read port | `List<ConsultarFechaContrato.ConfiguracionSerie> configuracionSeries(List<String> idsSerieFondo, Long idEmpresa)`: ALL rows of `ConsultarFechaSolicitudAporteRescate_SelectSerieFondo.sql` (verbatim; `IN (:IdSerieFondo)` receives the list as given: duplicates and nulls included); the service builds the map |
| `ConsultarFechaJdbc` | `implements ConsultarFechaDatos` | constructor `(cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo conexion)` |
| `ConsultarFechaFabrica` (*) | factory | `public static ConsultarFechaPort crear(Dependencias d)`; nested `public static final class Dependencias` with PUBLIC non-final fields `configuracion` (ConfiguracionSistemaPort), `diccionario` (DiccionarioMensajesPort), `basicos` (RevisarRequisitosBasicosPort), `calendario` (CalendarioPort), `empresas` (EmpresaPort), `series` (cierre.SeriesFondoPort), `fechasPort` (fechas.FechasPort), `perfilPort` (perfil.PerfilFondoMutuoPort), `datos` (ConsultarFechaDatos). It builds `CalcularFechaSolicitudAporteRescate(configuracion, empresas, series, fechasPort, calendario)` and `ObtenerPerfilFondoMutuo(perfilPort)` exactly as `RescateReinversionFabrica` does |
| `ConsultarFechaJavaCallHandler` (*) | `@Component implements JavaCallHandler` | constructor `(ConsultarFechaPort servicio, kit.Compuertas compuertas)`; `String fqn()` returns `JavaCallDispatcher.CONSULTAR_FECHA_SOLICITUD_APORTE_RESCATE`; `JsonObject call(java.sql.Connection con, JsonObject in) throws Exception` |

Existing production classes the change touches (the tests reference them):
* `cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher`: new constant `CONSULTAR_FECHA_SOLICITUD_APORTE_RESCATE = "cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate"` and the constant added to `LISTA_BLANCA` (`FsaCompuertaTest` asserts it). Existing tests that assert the exact allow-list must be updated by the same change.
* `kit.Compuertas` is NOT changed: the class is served when `listasServidas()` contains `ConsultarFechaConfiguracion.LISTA` (same property as the Manejador lists and RESCATE_REINVERSION); the shipped default (empty) keeps it closed. `Compuertas(listas, false, false, false, false, false)` must be enough to run it (no tesoreria/spot/flujo/owp/custodia flags).

## 2. Contract types (field names = legacy, JSON = legacy)

* `Entrada`: `Long p_IdEmpresa; Long p_IdUsuario; String p_IdModulo; String p_SimularOperacion; String p_UsaReglasNegocio; List<Solicitud> p_SolicitudAporteRescate` (default: empty list when the key is absent; JSON `null` => null list). `p_SimularOperacion` and `p_UsaReglasNegocio` are received and never read (FM-FSA-01).
* `Solicitud`: `Long IdInterno; String FechaEmision; String IdSerieFondo; String TipoSolicitud; String IdMoneda = ""; String IdMonedaPago = ""` (defaults when the key is absent; an explicit JSON `null` stays null).
* `Salida`: `Long P_OUT_ERRORESCONTADOR = 0L; List<kit.MensajeSalida> P_OUT_MENSAJES = []; List<Resultado> P_OUT_RESULTADO = []`.
* `Resultado`: `Long IdInterno; String IdSerieFondo;` and six dates `FechaEmision, FechaDigitacion, FechaAceptacion, FechaValorCuota, FechaCertificacion, FechaLiquidacion`, serialized as `yyyy-MM-dd 00:00:00.000` (legacy `java.sql.Date` through `Utils.getGsonBuilder()`: date format `yyyy-MM-dd HH:mm:ss.SSS`, nulls written, special floats). Field order as listed (the golden compares the raw text).
* `ConfiguracionSerie` (22 columns of the SQL): `IdSerieFondo, EstadoAporteComercial, EstadoRescateComercial, EstadoAporteBackoffice, EstadoRescateBackoffice, EstadoLiquidezComercial, EstadoLiquidezBackoffice` (String), `DiasRetencionesAporte, DiasRetencionesRescate, DiasValutaValorCuotaAporte, DiasValutaValorCuotaRescate, DiasValutaCertificacionAporte, DiasValutaCertificacionRescate, DiasValutaCurseAporte, DiasValutaCurseRescate` (Long), `IdEmisor, FondoExtranjero` (String), `IdPaisFondo, IdPaisEmisor, IdPaisMoneda` (Long), `IdMercadoFondoMutuo, FondoNacionalDolar` (String).
* JSON in and out: the `Utils.getGsonBuilder()` Gson (as `JsonRre`): a package-private `JsonFsa.gson()` is expected.

## 3. Behaviour the tests pin (bug for bug; the golden is the oracle, this is the index)

* Validation order (FM-FSA-02): requisitos basicos with an ERROR => counter 1, its messages, return (profile never read); profile `null` => counter 1, message `PERFIL_INCORRECTO` (id 0, dictionary text, empty values), return; empty list => counter 1, `NO_SE_INGRESARON_SOLICITUDES`; null list => `NullPointerException`. In every early return `P_OUT_RESULTADO` is `[]`.
* One read of the series SQL per request, map by `IdSerieFondo` with `Collectors.toMap` on a parallel stream (a repeated key => `IllegalStateException`, FM-FSA-03 / D-NOR-9); `ConsultarEmpresa` is read twice (FSA and `CalcularFecha...`), the profile twice (D-F3A-02: not observable but kept).
* Reinversion-set detection by list size > 1 (FM-FSA-05 / D-F3A-01), retention 4/2, valutas 1/2, spot flag by size (FM-FSA-06/07): see the cards; a missing series in a list of 2+ is a `NullPointerException`; with one request and no series the entry goes to the collaborator with four fields.
* The counter and messages of the collaborator are not read (FM-FSA-08); the list keeps the collaborator order.
* Exceptions: class compared (message only for a plain `Exception`, e.g. the scripted `ConsultarEmpresa` failure and `Tipo Solicitud no valida.`).
* Read only: the tables of the dump come back unchanged; no session-local table, no autonomous commit, no sequence.

## 4. Test resources (created by `legacy-oracle/sync_fsa.py` from the oracle output)

`src/test/resources/fondomutuo-fsa/cases/<id>/{case.json,seed.sql,expected.json}`, `fondomutuo-fsa/seed/base-fsa.sql` (loaded after `fondomutuo-3a/seed/base-fmm.sql`, no extra schema file: every object the SQL reads is in `schema-fmm.sql`), `fondomutuo-fsa/determinism-fsa.txt` (result of the 20 runs). Schema types come from the F1 catalog files; the types ASSUMED for this class are listed in the README of this folder.
