package cl.bcs.facturacion.controller;

import cl.bcs.facturacion.service.*;
import cl.bcs.facturacion.service.dte.SendDteService;
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
 * Migrated classes (52):
 *   4 original + 48 new = 52 total
 *
 * Unknown classes return 503 → BcsGateway falls back to TomEE.
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    // ── Original 4 ────────────────────────────────────────────────────────────
    private final AgregarQuitarConfiguracionAfpService agregarQuitarConfiguracionAfpService;
    private final ConsultarCuadraturaDiariaOperacionesService consultarCuadraturaDiariaOperacionesService;
    private final ConsultarMovimientoComprobanteCobrosService consultarMovimientoComprobanteCobrosService;
    private final ConsultarMovimientoComprobanteCobrosModificarService consultarMovimientoComprobanteCobrosModificarService;

    // ── New 48 ────────────────────────────────────────────────────────────────
    private final ConsultarCobrosClientesConDiferenciaService consultarCobrosClientesConDiferencia;
    private final AjusteSencilloComprobanteService ajusteSencilloComprobante;
    private final GenerarInformeComisionResumidoService generarInformeComisionResumido;
    private final CrearMovimientoComprobanteService crearMovimientoComprobante;
    private final ModificarRetencionIgkService modificarRetencionIgk;
    private final EmisionMasivaComprobanteService emisionMasivaComprobante;
    private final CrearFacturacionCobroMensualService crearFacturacionCobroMensual;
    private final GenerarInformePrefacturasService generarInformePrefacturas;
    private final GenerarInformesComprobanteComisionesDiariasService generarInformesComprobanteComisionesDiarias;
    private final CrearComprobanteNotaDeDebitoService crearComprobanteNotaDeDebito;
    private final ProcesarFacturacionCustodiaService procesarFacturacionCustodia;
    private final CrearComprobanteNotaDeCreditoService crearComprobanteNotaDeCredito;
    private final ModificarMovimientoComprobanteService modificarMovimientoComprobante;
    private final CrearComprobanteCobroMensualService crearComprobanteCobroMensual;
    private final ProcesarGananciaCapitalService procesarGananciaCapital;
    private final WsConsultaComprobantesService wsConsultaComprobantes;
    private final WsConsultaPDFService wsConsultaPDF;
    private final GenerarInformeAFPService generarInformeAFP;
    private final AnularMovimientosPorAsignacionMasivaService anularMovimientosPorAsignacionMasiva;
    private final GenerarArchivoAFPService generarArchivoAFP;
    private final ProcesarCargaMasivaService procesarCargaMasiva;
    private final ModificarMovimientoComprobanteSPService modificarMovimientoComprobanteSP;
    private final DuplicarFacturaService duplicarFactura;
    private final SimulacionPrecioPromedioRefacturacionService simulacionPrecioPromedioRefacturacion;
    private final ConsultarDetalleAnularMovimientosService consultarDetalleAnularMovimientos;
    private final SimulacionRefacturacionTransaccionesCienPorCientoService simulacionRefacturacionTransaccionesCienPorCiento;
    private final AnularMovimientoComprobanteWrapperService anularMovimientoComprobanteWrapper;
    private final GenerarInformeMatrizComisionesDiariasService generarInformeMatrizComisionesDiarias;
    private final GenerarInformeComisionDetalladoService generarInformeComisionDetallado;
    private final GenerarInformeMatrizComisionesService generarInformeMatrizComisiones;
    private final CuadraturaCierreDiarioService cuadraturaCierreDiario;
    private final AnularMovimientoComprobanteService anularMovimientoComprobante;
    private final AnularMovFacturarAsignacionMasivaWrapperService anularMovFacturarAsignacionMasivaWrapper;
    private final ReversarFacturacionCustodiaService reversarFacturacionCustodia;
    private final CrearMovimientoDesdeOtroComprobanteService crearMovimientoDesdeOtroComprobante;
    private final ModificarMovimientoComprobanteCobrosMasivoService modificarMovimientoComprobanteCobrosMasivo;
    private final AnularComprobanteFinalService anularComprobanteFinal;
    private final CrearCargaMasivaRefacturacionService crearCargaMasivaRefacturacion;
    private final GenerarInformesComprobanteComisionesMensualesService generarInformesComprobanteComisionesMensuales;
    private final AnularComprobanteNotaDeCreditoService anularComprobanteNotaDeCredito;
    private final GenerarInformeIGKService generarInformeIGK;
    private final EnviarFacturacionCobroMensualService enviarFacturacionCobroMensual;
    private final ConsultaDocumentoDTEService consultaDocumentoDTE;
    private final CrearVistaPreliminarFacturacionCobroMensualService crearVistaPreliminarFacturacionCobroMensual;
    private final NotificarCargoGananciaCapitalService notificarCargoGananciaCapital;
    private final CrearComprobanteService crearComprobante;
    private final SendDteService sendDte;

    public InternalJavaCallController(
            AgregarQuitarConfiguracionAfpService agregarQuitarConfiguracionAfpService,
            ConsultarCuadraturaDiariaOperacionesService consultarCuadraturaDiariaOperacionesService,
            ConsultarMovimientoComprobanteCobrosService consultarMovimientoComprobanteCobrosService,
            ConsultarMovimientoComprobanteCobrosModificarService consultarMovimientoComprobanteCobrosModificarService,
            ConsultarCobrosClientesConDiferenciaService consultarCobrosClientesConDiferencia,
            AjusteSencilloComprobanteService ajusteSencilloComprobante,
            GenerarInformeComisionResumidoService generarInformeComisionResumido,
            CrearMovimientoComprobanteService crearMovimientoComprobante,
            ModificarRetencionIgkService modificarRetencionIgk,
            EmisionMasivaComprobanteService emisionMasivaComprobante,
            CrearFacturacionCobroMensualService crearFacturacionCobroMensual,
            GenerarInformePrefacturasService generarInformePrefacturas,
            GenerarInformesComprobanteComisionesDiariasService generarInformesComprobanteComisionesDiarias,
            CrearComprobanteNotaDeDebitoService crearComprobanteNotaDeDebito,
            ProcesarFacturacionCustodiaService procesarFacturacionCustodia,
            CrearComprobanteNotaDeCreditoService crearComprobanteNotaDeCredito,
            ModificarMovimientoComprobanteService modificarMovimientoComprobante,
            CrearComprobanteCobroMensualService crearComprobanteCobroMensual,
            ProcesarGananciaCapitalService procesarGananciaCapital,
            WsConsultaComprobantesService wsConsultaComprobantes,
            WsConsultaPDFService wsConsultaPDF,
            GenerarInformeAFPService generarInformeAFP,
            AnularMovimientosPorAsignacionMasivaService anularMovimientosPorAsignacionMasiva,
            GenerarArchivoAFPService generarArchivoAFP,
            ProcesarCargaMasivaService procesarCargaMasiva,
            ModificarMovimientoComprobanteSPService modificarMovimientoComprobanteSP,
            DuplicarFacturaService duplicarFactura,
            SimulacionPrecioPromedioRefacturacionService simulacionPrecioPromedioRefacturacion,
            ConsultarDetalleAnularMovimientosService consultarDetalleAnularMovimientos,
            SimulacionRefacturacionTransaccionesCienPorCientoService simulacionRefacturacionTransaccionesCienPorCiento,
            AnularMovimientoComprobanteWrapperService anularMovimientoComprobanteWrapper,
            GenerarInformeMatrizComisionesDiariasService generarInformeMatrizComisionesDiarias,
            GenerarInformeComisionDetalladoService generarInformeComisionDetallado,
            GenerarInformeMatrizComisionesService generarInformeMatrizComisiones,
            CuadraturaCierreDiarioService cuadraturaCierreDiario,
            AnularMovimientoComprobanteService anularMovimientoComprobante,
            AnularMovFacturarAsignacionMasivaWrapperService anularMovFacturarAsignacionMasivaWrapper,
            ReversarFacturacionCustodiaService reversarFacturacionCustodia,
            CrearMovimientoDesdeOtroComprobanteService crearMovimientoDesdeOtroComprobante,
            ModificarMovimientoComprobanteCobrosMasivoService modificarMovimientoComprobanteCobrosMasivo,
            AnularComprobanteFinalService anularComprobanteFinal,
            CrearCargaMasivaRefacturacionService crearCargaMasivaRefacturacion,
            GenerarInformesComprobanteComisionesMensualesService generarInformesComprobanteComisionesMensuales,
            AnularComprobanteNotaDeCreditoService anularComprobanteNotaDeCredito,
            GenerarInformeIGKService generarInformeIGK,
            EnviarFacturacionCobroMensualService enviarFacturacionCobroMensual,
            ConsultaDocumentoDTEService consultaDocumentoDTE,
            CrearVistaPreliminarFacturacionCobroMensualService crearVistaPreliminarFacturacionCobroMensual,
            NotificarCargoGananciaCapitalService notificarCargoGananciaCapital,
            CrearComprobanteService crearComprobante,
            SendDteService sendDte) {

        this.agregarQuitarConfiguracionAfpService = agregarQuitarConfiguracionAfpService;
        this.consultarCuadraturaDiariaOperacionesService = consultarCuadraturaDiariaOperacionesService;
        this.consultarMovimientoComprobanteCobrosService = consultarMovimientoComprobanteCobrosService;
        this.consultarMovimientoComprobanteCobrosModificarService = consultarMovimientoComprobanteCobrosModificarService;
        this.consultarCobrosClientesConDiferencia = consultarCobrosClientesConDiferencia;
        this.ajusteSencilloComprobante = ajusteSencilloComprobante;
        this.generarInformeComisionResumido = generarInformeComisionResumido;
        this.crearMovimientoComprobante = crearMovimientoComprobante;
        this.modificarRetencionIgk = modificarRetencionIgk;
        this.emisionMasivaComprobante = emisionMasivaComprobante;
        this.crearFacturacionCobroMensual = crearFacturacionCobroMensual;
        this.generarInformePrefacturas = generarInformePrefacturas;
        this.generarInformesComprobanteComisionesDiarias = generarInformesComprobanteComisionesDiarias;
        this.crearComprobanteNotaDeDebito = crearComprobanteNotaDeDebito;
        this.procesarFacturacionCustodia = procesarFacturacionCustodia;
        this.crearComprobanteNotaDeCredito = crearComprobanteNotaDeCredito;
        this.modificarMovimientoComprobante = modificarMovimientoComprobante;
        this.crearComprobanteCobroMensual = crearComprobanteCobroMensual;
        this.procesarGananciaCapital = procesarGananciaCapital;
        this.wsConsultaComprobantes = wsConsultaComprobantes;
        this.wsConsultaPDF = wsConsultaPDF;
        this.generarInformeAFP = generarInformeAFP;
        this.anularMovimientosPorAsignacionMasiva = anularMovimientosPorAsignacionMasiva;
        this.generarArchivoAFP = generarArchivoAFP;
        this.procesarCargaMasiva = procesarCargaMasiva;
        this.modificarMovimientoComprobanteSP = modificarMovimientoComprobanteSP;
        this.duplicarFactura = duplicarFactura;
        this.simulacionPrecioPromedioRefacturacion = simulacionPrecioPromedioRefacturacion;
        this.consultarDetalleAnularMovimientos = consultarDetalleAnularMovimientos;
        this.simulacionRefacturacionTransaccionesCienPorCiento = simulacionRefacturacionTransaccionesCienPorCiento;
        this.anularMovimientoComprobanteWrapper = anularMovimientoComprobanteWrapper;
        this.generarInformeMatrizComisionesDiarias = generarInformeMatrizComisionesDiarias;
        this.generarInformeComisionDetallado = generarInformeComisionDetallado;
        this.generarInformeMatrizComisiones = generarInformeMatrizComisiones;
        this.cuadraturaCierreDiario = cuadraturaCierreDiario;
        this.anularMovimientoComprobante = anularMovimientoComprobante;
        this.anularMovFacturarAsignacionMasivaWrapper = anularMovFacturarAsignacionMasivaWrapper;
        this.reversarFacturacionCustodia = reversarFacturacionCustodia;
        this.crearMovimientoDesdeOtroComprobante = crearMovimientoDesdeOtroComprobante;
        this.modificarMovimientoComprobanteCobrosMasivo = modificarMovimientoComprobanteCobrosMasivo;
        this.anularComprobanteFinal = anularComprobanteFinal;
        this.crearCargaMasivaRefacturacion = crearCargaMasivaRefacturacion;
        this.generarInformesComprobanteComisionesMensuales = generarInformesComprobanteComisionesMensuales;
        this.anularComprobanteNotaDeCredito = anularComprobanteNotaDeCredito;
        this.generarInformeIGK = generarInformeIGK;
        this.enviarFacturacionCobroMensual = enviarFacturacionCobroMensual;
        this.consultaDocumentoDTE = consultaDocumentoDTE;
        this.crearVistaPreliminarFacturacionCobroMensual = crearVistaPreliminarFacturacionCobroMensual;
        this.notificarCargoGananciaCapital = notificarCargoGananciaCapital;
        this.crearComprobante = crearComprobante;
        this.sendDte = sendDte;
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

        String fqn        = body.has("class")  ? body.get("class").getAsString()             : "";
        JsonObject params = body.has("params") ? body.get("params").getAsJsonObject()         : new JsonObject();
        int skip          = body.has("skip")   ? body.get("skip").getAsInt()                  : 0;
        int take          = body.has("take")   ? body.get("take").getAsInt()                  : 0;

        if (fqn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'class' field required");
        }

        log.debug("InternalJavaCall: class={} skip={} take={}", fqn, skip, take);

        try {
            JsonObject result = switch (fqn) {

                // ── Original 4 ────────────────────────────────────────────────
                case "cl.bcs.facturacion.AgregarQuitarConfiguracionAFP" ->
                    agregarQuitarConfiguracionAfpService.execute(params);

                case "cl.bcs.facturacion.ConsultarCuadraturaDiariaOperaciones" ->
                    consultarCuadraturaDiariaOperacionesService.execute(params);

                case "cl.bcs.facturacion.ConsultarMovimientoComprobanteCobros" ->
                    consultarMovimientoComprobanteCobrosService.execute(params);

                case "cl.bcs.facturacion.ConsultarMovimientoComprobanteCobrosModificar" ->
                    consultarMovimientoComprobanteCobrosModificarService.execute(params);

                // ── New 48 ────────────────────────────────────────────────────

                case "cl.bcs.facturacion.ConsultarCobrosClientesConDiferencia" ->
                    consultarCobrosClientesConDiferencia.execute(params);

                case "cl.bcs.facturacion.AjusteSencilloComprobante" ->
                    ajusteSencilloComprobante.execute(params);

                case "cl.bcs.facturacion.GenerarInformeComisionResumido" ->
                    generarInformeComisionResumido.execute(params);

                case "cl.bcs.facturacion.CrearMovimientoComprobante" ->
                    crearMovimientoComprobante.execute(params);

                case "cl.bcs.facturacion.ModificarRetencionIgk" ->
                    modificarRetencionIgk.execute(params);

                case "cl.bcs.facturacion.EmisionMasivaComprobante" ->
                    emisionMasivaComprobante.execute(params);

                case "cl.bcs.facturacion.CrearFacturacionCobroMensual" ->
                    crearFacturacionCobroMensual.execute(params);

                case "cl.bcs.facturacion.GenerarInformePrefacturas" ->
                    generarInformePrefacturas.execute(params);

                case "cl.bcs.facturacion.GenerarInformesComprobanteComisionesDiarias" ->
                    generarInformesComprobanteComisionesDiarias.execute(params);

                case "cl.bcs.facturacion.CrearComprobanteNotaDeDebito" ->
                    crearComprobanteNotaDeDebito.execute(params);

                case "cl.bcs.facturacion.ProcesarFacturacionCustodia" ->
                    procesarFacturacionCustodia.execute(params);

                case "cl.bcs.facturacion.CrearComprobanteNotaDeCredito" ->
                    crearComprobanteNotaDeCredito.execute(params);

                case "cl.bcs.facturacion.ModificarMovimientoComprobante" ->
                    modificarMovimientoComprobante.execute(params);

                case "cl.bcs.facturacion.CrearComprobanteCobroMensual" ->
                    crearComprobanteCobroMensual.execute(params);

                case "cl.bcs.facturacion.ProcesarGananciaCapital" ->
                    procesarGananciaCapital.execute(params);

                case "cl.bcs.facturacion.WsConsultaComprobantes" ->
                    wsConsultaComprobantes.execute(params);

                case "cl.bcs.facturacion.WsConsultaPDF" ->
                    wsConsultaPDF.execute(params);

                case "cl.bcs.facturacion.GenerarInformeAFP" ->
                    generarInformeAFP.execute(params);

                case "cl.bcs.facturacion.AnularMovimientosPorAsignacionMasiva" ->
                    anularMovimientosPorAsignacionMasiva.execute(params);

                case "cl.bcs.facturacion.GenerarArchivoAFP" ->
                    generarArchivoAFP.execute(params);

                case "cl.bcs.facturacion.ProcesarCargaMasiva" ->
                    procesarCargaMasiva.execute(params);

                case "cl.bcs.facturacion.ModificarMovimientoComprobanteSP" ->
                    modificarMovimientoComprobanteSP.execute(params);

                case "cl.bcs.facturacion.DuplicarFactura" ->
                    duplicarFactura.execute(params);

                case "cl.bcs.facturacion.SimulacionPrecioPromedioRefacturacion" ->
                    simulacionPrecioPromedioRefacturacion.execute(params);

                case "cl.bcs.facturacion.ConsultarDetalleAnularMovimientos" ->
                    consultarDetalleAnularMovimientos.execute(params);

                case "cl.bcs.facturacion.SimulacionRefacturacionTransaccionesCienPorCiento" ->
                    simulacionRefacturacionTransaccionesCienPorCiento.execute(params);

                case "cl.bcs.facturacion.AnularMovimientoComprobanteWrapper" ->
                    anularMovimientoComprobanteWrapper.execute(params);

                case "cl.bcs.facturacion.GenerarInformeMatrizComisionesDiarias" ->
                    generarInformeMatrizComisionesDiarias.execute(params);

                case "cl.bcs.facturacion.GenerarInformeComisionDetallado" ->
                    generarInformeComisionDetallado.execute(params);

                case "cl.bcs.facturacion.GenerarInformeMatrizComisiones" ->
                    generarInformeMatrizComisiones.execute(params);

                case "cl.bcs.facturacion.CuadraturaCierreDiario" ->
                    cuadraturaCierreDiario.execute(params);

                case "cl.bcs.facturacion.AnularMovimientoComprobante" ->
                    anularMovimientoComprobante.execute(params);

                case "cl.bcs.facturacion.AnularMovFacturarAsignacionMasivaWrapper" ->
                    anularMovFacturarAsignacionMasivaWrapper.execute(params);

                case "cl.bcs.facturacion.ReversarFacturacionCustodia" ->
                    reversarFacturacionCustodia.execute(params);

                case "cl.bcs.facturacion.CrearMovimientoDesdeOtroComprobante" ->
                    crearMovimientoDesdeOtroComprobante.execute(params);

                case "cl.bcs.facturacion.ModificarMovimientoComprobanteCobrosMasivo" ->
                    modificarMovimientoComprobanteCobrosMasivo.execute(params);

                case "cl.bcs.facturacion.AnularComprobanteFinal" ->
                    anularComprobanteFinal.execute(params);

                case "cl.bcs.facturacion.CrearCargaMasivaRefacturacion" ->
                    crearCargaMasivaRefacturacion.execute(params);

                case "cl.bcs.facturacion.GenerarInformesComprobanteComisionesMensuales" ->
                    generarInformesComprobanteComisionesMensuales.execute(params);

                case "cl.bcs.facturacion.AnularComprobanteNotaDeCredito" ->
                    anularComprobanteNotaDeCredito.execute(params);

                case "cl.bcs.facturacion.GenerarInformeIGK" ->
                    generarInformeIGK.execute(params);

                case "cl.bcs.facturacion.EnviarFacturacionCobroMensual" ->
                    enviarFacturacionCobroMensual.execute(params);

                case "cl.bcs.facturacion.ConsultaDocumentoDTE" ->
                    consultaDocumentoDTE.execute(params);

                case "cl.bcs.facturacion.CrearVistaPreliminarFacturacionCobroMensual" ->
                    crearVistaPreliminarFacturacionCobroMensual.execute(params);

                case "cl.bcs.facturacion.NotificarCargoGananciaCapital" ->
                    notificarCargoGananciaCapital.execute(params);

                case "cl.bcs.facturacion.CrearComprobante" ->
                    crearComprobante.execute(params);

                case "cl.bcs.facturacion.dte.SendDte" ->
                    sendDte.execute(params);

                default -> null;
            };

            if (result != null) {
                return ResponseEntity.ok(result.toString());
            }

        } catch (Exception e) {
            log.error("Error executing JavaCall class={}: {}", fqn, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error executing JavaCall: " + e.getMessage());
        }

        // 503 → BcsGateway falls back to TomEE
        log.debug("JavaCall class '{}' not yet migrated; returning 503 for TomEE fallback", fqn);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "JavaCall class '" + fqn + "' not yet migrated; TomEE fallback active");
    }
}
