package cl.bcs.socionegocio.controller;

import cl.bcs.socionegocio.service.emp.EliminarSucursalAtencionService;
import cl.bcs.socionegocio.service.soc.ConsultarEnrolamientoService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Receives J-type JavaCall dispatches from BcsPlataformaWeb's CallController.
 *
 * Protocol:
 *   POST /internal/java-call
 *   Body: {"class": "fully.qualified.ClassName", "params": {...}, "skip": N, "take": M}
 *
 * Migration status (as of this version):
 *   MIGRATED  (2):
 *     - cl.bcs.socionegocio.emp.EliminarSucursalAtencion
 *     - cl.bcs.socionegocio.soc.ConsultarEnrolamiento
 *
 *   DEFERRED  (324) — all have unresolvable cross-service dependencies on
 *   cl.bcs.plataforma.* (ConfiguracionSistema, Diccionario, AgregarLog,
 *   ValidarCamposObligatorios, AsignarCamposFormulario, ModificarIndicador,
 *   PlataformaUtils.MensajeSalida, etc.) or external Blockchain HTTP APIs.
 *   These classes return 503 so BcsGateway falls back to the TomEE node:
 *
 *   bck: ActualizaEstadosBlockChain, ActualizaRequisitoClienteBlockChain,
 *        AnticiparPrestamistaBlockChain, AnticiparPrestamoBlockChain,
 *        AsignaPrestamistaBlockchain, ConsultaDataCorredoraBlockChain,
 *        ConsultaEstadoAnticipoBlockChain, ConsultaEstadoPrestamoBlockChain,
 *        ConsultaListaPorFolioBlockChain, ConsultaLogBlockChain,
 *        ConsultaOtrosPrestamosBlockChain, ConsultaPrestamoBlockChainPorFolio,
 *        ConsultarEstadoContratoBlockchain, ContratoEsBlockChainFirmado,
 *        ExtenderPlazoPrestamoBlockChain, FirmaAnexoUrl, FirmarContratoBlockchain,
 *        IngresoPrestamosBlockchain, ModificaPrestamoBlockChain, ProcesaLogBlockChain,
 *        ReintentoPrestamosBlockChain, RenovarPrestamosBlockChain,
 *        ValidacionRequisitosBlockChain
 *
 *   blo: BloqueoAutomaticoClientesPorConfiguracion,
 *        BloqueoAutomaticoClientesPorTemporalidad,
 *        BloqueoAutomaticoPortafolioPorTemporalidad,
 *        BloqueoDesbloqueoMasivoClientePortafolio,
 *        CambioEstadoMasivoClientesPortafolios,
 *        DesbloqueoAutomaticoClientesPorTemporalidad,
 *        DesbloqueoAutomaticoPortafolioPorTemporalidad
 *
 *   car: ProcesarCargaMasiva
 *
 *   cli: AutorizacionEnrolamientoMasivo, AutorizacionModificacionEstadoClienteMasivo,
 *        AutorizacionModificarClienteDatosGenerales, AutorizarCrearDeclaracionOrigenFondosMasivo,
 *        BloqueoTemporalClientePorDesactualizacionDatos, ConfirmarCrearDeclaracionOrigenFondos,
 *        ConfirmarModificarClienteDatosGeneralesMasivo, ConfirmarModificarDeclaracionOrigenFondosMasivo,
 *        ConsultaAvanzadaClientes, ConsultaDocumentos, ConsultarAnotacionesCliente,
 *        ConsultarArchivosAnexos, ConsultarConfiguracionNotificacionesCliente,
 *        ConsultarEntidadEnrolamiento, ConsultarInformacionCliente, CrearBorradorCliente,
 *        CrearCliente, CrearClienteNominaPeru, CrearDeclaracionOrigenFondos,
 *        GenerarReporteClienteFallecido, GenerarReporteConsultaAvanzadaCliente,
 *        GenerarReporteSocioNegocio, ModificarClienteDatosGenerales,
 *        ModificarConfiguracionBloqueoAutomaticoClientes, ModificarDeclaracionOrigenFondos,
 *        ModificarGrupo, ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento,
 *        NotificarCaducidadDatos, ObtenerClientesPortafolio, ObtenerPortafoliosEjecutivo,
 *        RechazaModificarClienteDatosGeneralesMasivo, ResumenPortafolioCliente,
 *        SubirArchivoAnotacionCliente
 *
 *   cnt: (4 classes — cross-service deps on plataforma)
 *
 *   ctt: (2 classes — cross-service deps on plataforma)
 *
 *   doc: (9 classes — cross-service deps on plataforma)
 *
 *   eje: (12 classes — cross-service deps on plataforma)
 *
 *   emp: AgregarSucursalAtencion (cross-service dep; EliminarSucursalAtencion migrated above)
 *        ActualizarSupervisorAEjecutivo, ModificarEmpleadoHorario, ObtenerConfiguracion,
 *        other emp classes — cross-service deps on plataforma
 *
 *   enr: (66 classes — cross-service deps on plataforma.frm.ValidarCamposObligatorios,
 *        plataforma.ind.ModificarIndicador, EntradaAdministrarBorrador, etc.)
 *
 *   per: (106 classes — cross-service deps on plataforma)
 *
 *   por: (9 classes — cross-service deps on plataforma)
 *
 *   pro: (some classes — cross-service deps on plataforma)
 *
 *   rel: (3 classes — cross-service deps on plataforma)
 *
 *   soc: ConsultarFichaSocioNegocio, CrearSocioNegocio, ModificarSocioNegocio,
 *        RegenerarDocumentosCliente, other soc classes — cross-service deps on plataforma
 *        (ConsultarEnrolamiento migrated above)
 *
 *   utils: (2 classes — cross-service deps on plataforma)
 *
 *   wbs: (39 classes — cross-service deps on plataforma / external web services)
 *
 * Source: PluginLoader pattern from legacy CallServlet.java
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    // -------------------------------------------------------------------------
    // Migrated services (2 of 326 J-type classes)
    // -------------------------------------------------------------------------
    private final EliminarSucursalAtencionService eliminarSucursalAtencionService;
    private final ConsultarEnrolamientoService    consultarEnrolamientoService;

    public InternalJavaCallController(
            EliminarSucursalAtencionService eliminarSucursalAtencionService,
            ConsultarEnrolamientoService    consultarEnrolamientoService) {
        this.eliminarSucursalAtencionService = eliminarSucursalAtencionService;
        this.consultarEnrolamientoService    = consultarEnrolamientoService;
    }

    @PostMapping(path = "/internal/java-call",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> dispatch(@RequestBody String rawBody,
                                           @RequestHeader(value = "X-SESSION-TOKEN",
                                                          required = false) String token) {
        JsonObject body;
        try {
            body = JsonParser.parseString(rawBody).getAsJsonObject();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON body");
        }

        String fqn       = body.has("class")  ? body.get("class").getAsString()              : "";
        JsonObject params = body.has("params") ? body.get("params").getAsJsonObject()         : new JsonObject();
        int skip         = body.has("skip")   ? body.get("skip").getAsInt()                  : 0;
        int take         = body.has("take")   ? body.get("take").getAsInt()                  : 0;

        if (fqn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'class' field required");
        }

        log.debug("InternalJavaCall: class={} skip={} take={}", fqn, skip, take);

        JsonObject result;
        try {
            result = switch (fqn) {

                // -----------------------------------------------------------------
                // MIGRATED: cl.bcs.socionegocio.emp.EliminarSucursalAtencion
                //   Pure JDBC soft-delete on Empleado.SucursalAtencion — no
                //   plataforma cross-service dependencies.
                // -----------------------------------------------------------------
                case "cl.bcs.socionegocio.emp.EliminarSucursalAtencion" ->
                        eliminarSucursalAtencionService.execute(params);

                // -----------------------------------------------------------------
                // MIGRATED: cl.bcs.socionegocio.soc.ConsultarEnrolamiento
                //   Pure JDBC SELECT on SOC.Enrolamiento — no plataforma
                //   cross-service dependencies.
                // -----------------------------------------------------------------
                case "cl.bcs.socionegocio.soc.ConsultarEnrolamiento" ->
                        consultarEnrolamientoService.execute(params);

                // -----------------------------------------------------------------
                // DEFERRED (324 classes):
                //   All remaining J-type classes have unresolvable cross-service
                //   dependencies on cl.bcs.plataforma.* (ConfiguracionSistema,
                //   Diccionario, AgregarLog, ValidarCamposObligatorios,
                //   AsignarCamposFormulario, ModificarIndicador,
                //   PlataformaUtils.MensajeSalida) or on external Blockchain HTTP
                //   APIs.  Return 503 so BcsGateway falls back to the TomEE node.
                // -----------------------------------------------------------------
                default -> throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "JavaCall class '" + fqn + "' not yet migrated; TomEE fallback active");
            };
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            log.error("Error executing JavaCall class={}", fqn, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return ResponseEntity.ok(result.toString());
    }
}
