package cl.bcs.fondomutuo.adapter.in.web;

import cl.bcs.fondomutuo.service.javacall.*;
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
 * 132 J-type classes from cl.bcs.fondomutuo.* are registered here.
 * Classes with unsatisfiable TomEE dependencies throw UnsupportedOperationException,
 * which the handler converts to 503 → BcsGateway falls back to TomEE.
 * Only AperturaAutomaticoFondos is fully implemented (HANA SP, no TomEE deps).
 *
 * Source: PluginLoader pattern from legacy CallServlet.java
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    // ---- 132 JavaCall service beans ----
    private final ActualizaCarteraValorizadaFFMMService actualizaCarteraValorizadaFFMMService;
    private final ActualizarCarterasFondosMutuosService actualizarCarterasFondosMutuosService;
    private final ActualizarCostoUFService actualizarCostoUFService;
    private final ActualizarCostoUFCargaService actualizarCostoUFCargaService;
    private final ActualizarCostoUF_MLTService actualizarCostoUF_MLTService;
    private final ActualizarFechaGestionOperacionesService actualizarFechaGestionOperacionesService;
    private final ActualizarFondoIndustriaService actualizarFondoIndustriaService;
    private final AdministracionLiquidezService administracionLiquidezService;
    private final AdministrarComisionesService administrarComisionesService;
    private final AgregarLogCertificacionAporteRescateService agregarLogCertificacionAporteRescateService;
    private final AnulacionContratoCSCService anulacionContratoCSCService;
    private final AnulacionSolicitudAporteRescateService anulacionSolicitudAporteRescateService;
    private final AnulacionSolicitudAporteRescateFolioContratoService anulacionSolicitudAporteRescateFolioContratoService;
    private final AnulacionTraspasoService anulacionTraspasoService;
    private final AperturaAutomaticoFondosService aperturaAutomaticoFondosService;
    private final AutorizarOmnibusService autorizarOmnibusService;
    private final BuscarCantidadesPorRegimenService buscarCantidadesPorRegimenService;
    private final CalculaCustodiaRescateService calculaCustodiaRescateService;
    private final CalcularComisionesService calcularComisionesService;
    private final CalcularCostoFactorAjusteService calcularCostoFactorAjusteService;
    private final CalcularCostoUFAportesService calcularCostoUFAportesService;
    private final CalcularFechaSolicitudAporteRescateService calcularFechaSolicitudAporteRescateService;
    private final CalcularFechaSolicitudAporteRescateJavaService calcularFechaSolicitudAporteRescateJavaService;
    private final CalcularMontoNumeroCuotaService calcularMontoNumeroCuotaService;
    private final CalcularSpreadRescateService calcularSpreadRescateService;
    private final CalcularValoresService calcularValoresService;
    private final CambiarEstadoSolicitudAgfService cambiarEstadoSolicitudAgfService;
    private final CargarArchivoCuadraturaInternacionalUIService cargarArchivoCuadraturaInternacionalUIService;
    private final CargarArchivoCuadraturaNacionalUIService cargarArchivoCuadraturaNacionalUIService;
    private final CargarCustodiaFondoMutuoService cargarCustodiaFondoMutuoService;
    private final CargaReinversionesFondoMutuoService cargaReinversionesFondoMutuoService;
    private final CargarValorCuotaService cargarValorCuotaService;
    private final CertificarPorFiltrosSolicitudAporteRescateService certifiPorFiltrosSolicitudAporteRescateService;
    private final CertificarSolicitudAporteRescateService certificarSolicitudAporteRescateService;
    private final ConfigurarFondoAdministracionLiquidezService configurarFondoAdministracionLiquidezService;
    private final ConfirmarModificarFechaGestionOperacionesService confirmarModificarFechaGestionOperacionesService;
    private final ConfirmarTraspasoService confirmarTraspasoService;
    private final ConsultarComisionesNNFondoMutuoService consultarComisionesNNFondoMutuoService;
    private final ConsultarCustodiaDisponibleReinversion1NService consultarCustodiaDisponibleReinversion1NService;
    private final ConsultarCustodiaPortafolioFFMMService consultarCustodiaPortafolioFFMMService;
    private final ConsultarCustodiaPortafolioFFMMWSService consultarCustodiaPortafolioFFMMWSService;
    private final ConsultarCustodiaPortafolioFFMMWSSService consultarCustodiaPortafolioFFMMWSSService;
    private final ConsultarCustodiaPortafolioFondoMutuoService consultarCustodiaPortafolioFondoMutuoService;
    private final ConsultarCustodiaPortafolioFondoMutuoWSService consultarCustodiaPortafolioFondoMutuoWSService;
    private final ConsultarDetalleSolicitudService consultarDetalleSolicitudService;
    private final ConsultarFechaSolicitudAporteRescateService consultarFechaSolicitudAporteRescateService;
    private final ConsultarFlujoCustodiaFondoMutuoService consultarFlujoCustodiaFondoMutuoService;
    private final ConsultarFondoService consultarFondoService;
    private final ConsultarFondoMutuoReinversionService consultarFondoMutuoReinversionService;
    private final ConsultarInstrumentosFondosMutuosService consultarInstrumentosFondosMutuosService;
    private final ConsultarSolicitudAporteRescateUIService consultarSolicitudAporteRescateUIService;
    private final ConsultarSolicitudesCertificacionMasivaService consultarSolicitudesCertificacionMasivaService;
    private final ConsultarSolicitudesFFMMService consultarSolicitudesFFMMService;
    private final ConsultarSolicitudesFFMMAgenteService consultarSolicitudesFFMMAgenteService;
    private final ConsultarSolicitudesFFMMWSService consultarSolicitudesFFMMWSService;
    private final ConsultarSolicitudesParaAGFService consultarSolicitudesParaAGFService;
    private final ConsultarValidacionDJ1932PorAgnoComercialService consultarValidacionDJ1932PorAgnoComercialService;
    private final ConsultarValorCuotaHistoricoService consultarValorCuotaHistoricoService;
    private final ConsultarValorDolarFondoMutuoService consultarValorDolarFondoMutuoService;
    private final CrearAporteCSCService crearAporteCSCService;
    private final CrearAporteRescateFusionService crearAporteRescateFusionService;
    private final CrearAporteSAAPVService crearAporteSAAPVService;
    private final CrearComisionService crearComisionService;
    private final CrearIngresoCSCService crearIngresoCSCService;
    private final CrearIngresoCSCManejadorService crearIngresoCSCManejadorService;
    private final CrearIngresoRetiroGarantiasFondoMutuoService crearIngresoRetiroGarantiasFondoMutuoService;
    private final CrearIngresoRetiroTituloFondoMutuoService crearIngresoRetiroTituloFondoMutuoService;
    private final CrearSolicitudAGFService crearSolicitudAGFService;
    private final CrearSolicitudAporteOvernightService crearSolicitudAporteOvernightService;
    private final CrearSolicitudAportePorDividendosService crearSolicitudAportePorDividendosService;
    private final CrearSolicitudAportePorDividendosCFIService crearSolicitudAportePorDividendosCFIService;
    private final CrearSolicitudAporteReinversionService crearSolicitudAporteReinversionService;
    private final CrearSolicitudAporteRescateNormalService crearSolicitudAporteRescateNormalService;
    private final CrearSolicitudAporteRescateProgramadoService crearSolicitudAporteRescateProgramadoService;
    private final CrearSolicitudCambioSerieService crearSolicitudCambioSerieService;
    private final CrearSolicitudReinversionPorClienteService crearSolicitudReinversionPorClienteService;
    private final CrearSolicitudRescateComisionNNService crearSolicitudRescateComisionNNService;
    private final CrearSolicitudRescateNormalPorClienteService crearSolicitudRescateNormalPorClienteService;
    private final CrearSolicitudRescatePorComisionService crearSolicitudRescatePorComisionService;
    private final CrearSolicitudRescateReinversionService crearSolicitudRescateReinversionService;
    private final CrearSolicitudRescateReinversion1NService crearSolicitudRescateReinversion1NService;
    private final CrearSolicitudRescateTransferenciaService crearSolicitudRescateTransferenciaService;
    private final CrearSolicitudRescateTraspasoService crearSolicitudRescateTraspasoService;
    private final CrearSolicitudRescateTraspasoPortafolioService crearSolicitudRescateTraspasoPortafolioService;
    private final CrearTramoComisionService crearTramoComisionService;
    private final CrearTraspasoService crearTraspasoService;
    private final CuadrarOmnibusDetalleService cuadrarOmnibusDetalleService;
    private final CuadraturaCierreDiarioService cuadraturaCierreDiarioService;
    private final GeneracionArchivoAGFService generacionArchivoAGFService;
    private final GenerarArchivoMovimientosService generarArchivoMovimientosService;
    private final GenerarArchivoParticipesService generarArchivoParticipesService;
    private final GenerarArchivoSolicitudAporteRescateService generarArchivoSolicitudAporteRescateService;
    private final GenerarCertificado31MasivoService generarCertificado31MasivoService;
    private final GenerarDataReporteTRXIService generarDataReporteTRXIService;
    private final GenerarDataReporteTRXIFolioContratoService generarDataReporteTRXIFolioContratoService;
    private final GenerarInformeRentabilidadFFMMExtranjeroMasivoService generarInformeRentabilidadFFMMExtranjeroMasivoService;
    private final InventarioFileManagerService inventarioFileManagerService;
    private final LiquidacionSolicitudAporteRescateService liquidacionSolicitudAporteRescateService;
    private final ManejadorSolicitudAporteRescateService manejadorSolicitudAporteRescateService;
    private final ModificarComisionService modificarComisionService;
    private final ModificarComisionSolicitudPorPerfeccionamientoService modificarComisionSolicitudPorPerfeccionamientoService;
    private final ModificarCSCService modificarCSCService;
    private final ModificarCSCManejadorService modificarCSCManejadorService;
    private final ModificarMontoNumeroCuotasSolicitudService modificarMontoNumeroCuotasSolicitudService;
    private final ModificarTramoComisionService modificarTramoComisionService;
    private final ModificarTraspasoService modificarTraspasoService;
    private final NotificarEstadoOrdenFondoMutuoService notificarEstadoOrdenFondoMutuoService;
    private final NotificarTraspasoConfirmarService notificarTraspasoConfirmarService;
    private final ObtenerPerfilFondoMutuoService obtenerPerfilFondoMutuoService;
    private final ProcesarArchivoCuadraturaInternacionalService procesarArchivoCuadraturaInternacionalService;
    private final ProcesarArchivoCuadraturaNacionalService procesarArchivoCuadraturaNacionalService;
    private final ProcesarCargaSolicitudesAporteRescateService procesarCargaSolicitudesAporteRescateService;
    private final ProcesarComisionesNNService procesarComisionesNNService;
    private final ProcesarCustodiaFondoMutuoService procesarCustodiaFondoMutuoService;
    private final ProcesarIngresoEgresoTesoreriaService procesarIngresoEgresoTesoreriaService;
    private final ProcesarIngresoOperacionSpotService procesarIngresoOperacionSpotService;
    private final ProcesarReinversionRescateService procesarReinversionRescateService;
    private final QuitarTramoComisionService quitarTramoComisionService;
    private final RecaudacionTraspasoService recaudacionTraspasoService;
    private final RechazarModificarFechaGestionOperacionesService rechazarModificarFechaGestionOperacionesService;
    private final ReniciarFolioOperacionOmnibusEmisorService reniciarFolioOperacionOmnibusEmisorService;
    private final RevocacionContratoCSCService revocacionContratoCSCService;
    private final RevocacionContratoCSCManejadorService revocacionContratoCSCManejadorService;
    private final ServicioCumpleRequisitosCrearSolicitudAporteRescateService servicioCumpleRequisitosCrearSolicitudAporteRescateService;
    private final SimulaCalculoSpreadRescateService simulaCalculoSpreadRescateService;
    private final TrxCFileManagerService trxCFileManagerService;
    private final TrxPFileManagerService trxPFileManagerService;
    private final ValidaMontoInversionService validaMontoInversionService;
    private final ValidaPatrimonioEmisorService validaPatrimonioEmisorService;
    private final ValorCuotaFileManagerService valorCuotaFileManagerService;
    private final ValorCuotaInternacionalFileManagerService valorCuotaInternacionalFileManagerService;
    private final ValorizaCarteraFFMMService valorizaCarteraFFMMService;

    public InternalJavaCallController(
            ActualizaCarteraValorizadaFFMMService actualizaCarteraValorizadaFFMMService,
            ActualizarCarterasFondosMutuosService actualizarCarterasFondosMutuosService,
            ActualizarCostoUFService actualizarCostoUFService,
            ActualizarCostoUFCargaService actualizarCostoUFCargaService,
            ActualizarCostoUF_MLTService actualizarCostoUF_MLTService,
            ActualizarFechaGestionOperacionesService actualizarFechaGestionOperacionesService,
            ActualizarFondoIndustriaService actualizarFondoIndustriaService,
            AdministracionLiquidezService administracionLiquidezService,
            AdministrarComisionesService administrarComisionesService,
            AgregarLogCertificacionAporteRescateService agregarLogCertificacionAporteRescateService,
            AnulacionContratoCSCService anulacionContratoCSCService,
            AnulacionSolicitudAporteRescateService anulacionSolicitudAporteRescateService,
            AnulacionSolicitudAporteRescateFolioContratoService anulacionSolicitudAporteRescateFolioContratoService,
            AnulacionTraspasoService anulacionTraspasoService,
            AperturaAutomaticoFondosService aperturaAutomaticoFondosService,
            AutorizarOmnibusService autorizarOmnibusService,
            BuscarCantidadesPorRegimenService buscarCantidadesPorRegimenService,
            CalculaCustodiaRescateService calculaCustodiaRescateService,
            CalcularComisionesService calcularComisionesService,
            CalcularCostoFactorAjusteService calcularCostoFactorAjusteService,
            CalcularCostoUFAportesService calcularCostoUFAportesService,
            CalcularFechaSolicitudAporteRescateService calcularFechaSolicitudAporteRescateService,
            CalcularFechaSolicitudAporteRescateJavaService calcularFechaSolicitudAporteRescateJavaService,
            CalcularMontoNumeroCuotaService calcularMontoNumeroCuotaService,
            CalcularSpreadRescateService calcularSpreadRescateService,
            CalcularValoresService calcularValoresService,
            CambiarEstadoSolicitudAgfService cambiarEstadoSolicitudAgfService,
            CargarArchivoCuadraturaInternacionalUIService cargarArchivoCuadraturaInternacionalUIService,
            CargarArchivoCuadraturaNacionalUIService cargarArchivoCuadraturaNacionalUIService,
            CargarCustodiaFondoMutuoService cargarCustodiaFondoMutuoService,
            CargaReinversionesFondoMutuoService cargaReinversionesFondoMutuoService,
            CargarValorCuotaService cargarValorCuotaService,
            CertificarPorFiltrosSolicitudAporteRescateService certifiPorFiltrosSolicitudAporteRescateService,
            CertificarSolicitudAporteRescateService certificarSolicitudAporteRescateService,
            ConfigurarFondoAdministracionLiquidezService configurarFondoAdministracionLiquidezService,
            ConfirmarModificarFechaGestionOperacionesService confirmarModificarFechaGestionOperacionesService,
            ConfirmarTraspasoService confirmarTraspasoService,
            ConsultarComisionesNNFondoMutuoService consultarComisionesNNFondoMutuoService,
            ConsultarCustodiaDisponibleReinversion1NService consultarCustodiaDisponibleReinversion1NService,
            ConsultarCustodiaPortafolioFFMMService consultarCustodiaPortafolioFFMMService,
            ConsultarCustodiaPortafolioFFMMWSService consultarCustodiaPortafolioFFMMWSService,
            ConsultarCustodiaPortafolioFFMMWSSService consultarCustodiaPortafolioFFMMWSSService,
            ConsultarCustodiaPortafolioFondoMutuoService consultarCustodiaPortafolioFondoMutuoService,
            ConsultarCustodiaPortafolioFondoMutuoWSService consultarCustodiaPortafolioFondoMutuoWSService,
            ConsultarDetalleSolicitudService consultarDetalleSolicitudService,
            ConsultarFechaSolicitudAporteRescateService consultarFechaSolicitudAporteRescateService,
            ConsultarFlujoCustodiaFondoMutuoService consultarFlujoCustodiaFondoMutuoService,
            ConsultarFondoService consultarFondoService,
            ConsultarFondoMutuoReinversionService consultarFondoMutuoReinversionService,
            ConsultarInstrumentosFondosMutuosService consultarInstrumentosFondosMutuosService,
            ConsultarSolicitudAporteRescateUIService consultarSolicitudAporteRescateUIService,
            ConsultarSolicitudesCertificacionMasivaService consultarSolicitudesCertificacionMasivaService,
            ConsultarSolicitudesFFMMService consultarSolicitudesFFMMService,
            ConsultarSolicitudesFFMMAgenteService consultarSolicitudesFFMMAgenteService,
            ConsultarSolicitudesFFMMWSService consultarSolicitudesFFMMWSService,
            ConsultarSolicitudesParaAGFService consultarSolicitudesParaAGFService,
            ConsultarValidacionDJ1932PorAgnoComercialService consultarValidacionDJ1932PorAgnoComercialService,
            ConsultarValorCuotaHistoricoService consultarValorCuotaHistoricoService,
            ConsultarValorDolarFondoMutuoService consultarValorDolarFondoMutuoService,
            CrearAporteCSCService crearAporteCSCService,
            CrearAporteRescateFusionService crearAporteRescateFusionService,
            CrearAporteSAAPVService crearAporteSAAPVService,
            CrearComisionService crearComisionService,
            CrearIngresoCSCService crearIngresoCSCService,
            CrearIngresoCSCManejadorService crearIngresoCSCManejadorService,
            CrearIngresoRetiroGarantiasFondoMutuoService crearIngresoRetiroGarantiasFondoMutuoService,
            CrearIngresoRetiroTituloFondoMutuoService crearIngresoRetiroTituloFondoMutuoService,
            CrearSolicitudAGFService crearSolicitudAGFService,
            CrearSolicitudAporteOvernightService crearSolicitudAporteOvernightService,
            CrearSolicitudAportePorDividendosService crearSolicitudAportePorDividendosService,
            CrearSolicitudAportePorDividendosCFIService crearSolicitudAportePorDividendosCFIService,
            CrearSolicitudAporteReinversionService crearSolicitudAporteReinversionService,
            CrearSolicitudAporteRescateNormalService crearSolicitudAporteRescateNormalService,
            CrearSolicitudAporteRescateProgramadoService crearSolicitudAporteRescateProgramadoService,
            CrearSolicitudCambioSerieService crearSolicitudCambioSerieService,
            CrearSolicitudReinversionPorClienteService crearSolicitudReinversionPorClienteService,
            CrearSolicitudRescateComisionNNService crearSolicitudRescateComisionNNService,
            CrearSolicitudRescateNormalPorClienteService crearSolicitudRescateNormalPorClienteService,
            CrearSolicitudRescatePorComisionService crearSolicitudRescatePorComisionService,
            CrearSolicitudRescateReinversionService crearSolicitudRescateReinversionService,
            CrearSolicitudRescateReinversion1NService crearSolicitudRescateReinversion1NService,
            CrearSolicitudRescateTransferenciaService crearSolicitudRescateTransferenciaService,
            CrearSolicitudRescateTraspasoService crearSolicitudRescateTraspasoService,
            CrearSolicitudRescateTraspasoPortafolioService crearSolicitudRescateTraspasoPortafolioService,
            CrearTramoComisionService crearTramoComisionService,
            CrearTraspasoService crearTraspasoService,
            CuadrarOmnibusDetalleService cuadrarOmnibusDetalleService,
            CuadraturaCierreDiarioService cuadraturaCierreDiarioService,
            GeneracionArchivoAGFService generacionArchivoAGFService,
            GenerarArchivoMovimientosService generarArchivoMovimientosService,
            GenerarArchivoParticipesService generarArchivoParticipesService,
            GenerarArchivoSolicitudAporteRescateService generarArchivoSolicitudAporteRescateService,
            GenerarCertificado31MasivoService generarCertificado31MasivoService,
            GenerarDataReporteTRXIService generarDataReporteTRXIService,
            GenerarDataReporteTRXIFolioContratoService generarDataReporteTRXIFolioContratoService,
            GenerarInformeRentabilidadFFMMExtranjeroMasivoService generarInformeRentabilidadFFMMExtranjeroMasivoService,
            InventarioFileManagerService inventarioFileManagerService,
            LiquidacionSolicitudAporteRescateService liquidacionSolicitudAporteRescateService,
            ManejadorSolicitudAporteRescateService manejadorSolicitudAporteRescateService,
            ModificarComisionService modificarComisionService,
            ModificarComisionSolicitudPorPerfeccionamientoService modificarComisionSolicitudPorPerfeccionamientoService,
            ModificarCSCService modificarCSCService,
            ModificarCSCManejadorService modificarCSCManejadorService,
            ModificarMontoNumeroCuotasSolicitudService modificarMontoNumeroCuotasSolicitudService,
            ModificarTramoComisionService modificarTramoComisionService,
            ModificarTraspasoService modificarTraspasoService,
            NotificarEstadoOrdenFondoMutuoService notificarEstadoOrdenFondoMutuoService,
            NotificarTraspasoConfirmarService notificarTraspasoConfirmarService,
            ObtenerPerfilFondoMutuoService obtenerPerfilFondoMutuoService,
            ProcesarArchivoCuadraturaInternacionalService procesarArchivoCuadraturaInternacionalService,
            ProcesarArchivoCuadraturaNacionalService procesarArchivoCuadraturaNacionalService,
            ProcesarCargaSolicitudesAporteRescateService procesarCargaSolicitudesAporteRescateService,
            ProcesarComisionesNNService procesarComisionesNNService,
            ProcesarCustodiaFondoMutuoService procesarCustodiaFondoMutuoService,
            ProcesarIngresoEgresoTesoreriaService procesarIngresoEgresoTesoreriaService,
            ProcesarIngresoOperacionSpotService procesarIngresoOperacionSpotService,
            ProcesarReinversionRescateService procesarReinversionRescateService,
            QuitarTramoComisionService quitarTramoComisionService,
            RecaudacionTraspasoService recaudacionTraspasoService,
            RechazarModificarFechaGestionOperacionesService rechazarModificarFechaGestionOperacionesService,
            ReniciarFolioOperacionOmnibusEmisorService reniciarFolioOperacionOmnibusEmisorService,
            RevocacionContratoCSCService revocacionContratoCSCService,
            RevocacionContratoCSCManejadorService revocacionContratoCSCManejadorService,
            ServicioCumpleRequisitosCrearSolicitudAporteRescateService servicioCumpleRequisitosCrearSolicitudAporteRescateService,
            SimulaCalculoSpreadRescateService simulaCalculoSpreadRescateService,
            TrxCFileManagerService trxCFileManagerService,
            TrxPFileManagerService trxPFileManagerService,
            ValidaMontoInversionService validaMontoInversionService,
            ValidaPatrimonioEmisorService validaPatrimonioEmisorService,
            ValorCuotaFileManagerService valorCuotaFileManagerService,
            ValorCuotaInternacionalFileManagerService valorCuotaInternacionalFileManagerService,
            ValorizaCarteraFFMMService valorizaCarteraFFMMService) {

        this.actualizaCarteraValorizadaFFMMService = actualizaCarteraValorizadaFFMMService;
        this.actualizarCarterasFondosMutuosService = actualizarCarterasFondosMutuosService;
        this.actualizarCostoUFService = actualizarCostoUFService;
        this.actualizarCostoUFCargaService = actualizarCostoUFCargaService;
        this.actualizarCostoUF_MLTService = actualizarCostoUF_MLTService;
        this.actualizarFechaGestionOperacionesService = actualizarFechaGestionOperacionesService;
        this.actualizarFondoIndustriaService = actualizarFondoIndustriaService;
        this.administracionLiquidezService = administracionLiquidezService;
        this.administrarComisionesService = administrarComisionesService;
        this.agregarLogCertificacionAporteRescateService = agregarLogCertificacionAporteRescateService;
        this.anulacionContratoCSCService = anulacionContratoCSCService;
        this.anulacionSolicitudAporteRescateService = anulacionSolicitudAporteRescateService;
        this.anulacionSolicitudAporteRescateFolioContratoService = anulacionSolicitudAporteRescateFolioContratoService;
        this.anulacionTraspasoService = anulacionTraspasoService;
        this.aperturaAutomaticoFondosService = aperturaAutomaticoFondosService;
        this.autorizarOmnibusService = autorizarOmnibusService;
        this.buscarCantidadesPorRegimenService = buscarCantidadesPorRegimenService;
        this.calculaCustodiaRescateService = calculaCustodiaRescateService;
        this.calcularComisionesService = calcularComisionesService;
        this.calcularCostoFactorAjusteService = calcularCostoFactorAjusteService;
        this.calcularCostoUFAportesService = calcularCostoUFAportesService;
        this.calcularFechaSolicitudAporteRescateService = calcularFechaSolicitudAporteRescateService;
        this.calcularFechaSolicitudAporteRescateJavaService = calcularFechaSolicitudAporteRescateJavaService;
        this.calcularMontoNumeroCuotaService = calcularMontoNumeroCuotaService;
        this.calcularSpreadRescateService = calcularSpreadRescateService;
        this.calcularValoresService = calcularValoresService;
        this.cambiarEstadoSolicitudAgfService = cambiarEstadoSolicitudAgfService;
        this.cargarArchivoCuadraturaInternacionalUIService = cargarArchivoCuadraturaInternacionalUIService;
        this.cargarArchivoCuadraturaNacionalUIService = cargarArchivoCuadraturaNacionalUIService;
        this.cargarCustodiaFondoMutuoService = cargarCustodiaFondoMutuoService;
        this.cargaReinversionesFondoMutuoService = cargaReinversionesFondoMutuoService;
        this.cargarValorCuotaService = cargarValorCuotaService;
        this.certifiPorFiltrosSolicitudAporteRescateService = certifiPorFiltrosSolicitudAporteRescateService;
        this.certificarSolicitudAporteRescateService = certificarSolicitudAporteRescateService;
        this.configurarFondoAdministracionLiquidezService = configurarFondoAdministracionLiquidezService;
        this.confirmarModificarFechaGestionOperacionesService = confirmarModificarFechaGestionOperacionesService;
        this.confirmarTraspasoService = confirmarTraspasoService;
        this.consultarComisionesNNFondoMutuoService = consultarComisionesNNFondoMutuoService;
        this.consultarCustodiaDisponibleReinversion1NService = consultarCustodiaDisponibleReinversion1NService;
        this.consultarCustodiaPortafolioFFMMService = consultarCustodiaPortafolioFFMMService;
        this.consultarCustodiaPortafolioFFMMWSService = consultarCustodiaPortafolioFFMMWSService;
        this.consultarCustodiaPortafolioFFMMWSSService = consultarCustodiaPortafolioFFMMWSSService;
        this.consultarCustodiaPortafolioFondoMutuoService = consultarCustodiaPortafolioFondoMutuoService;
        this.consultarCustodiaPortafolioFondoMutuoWSService = consultarCustodiaPortafolioFondoMutuoWSService;
        this.consultarDetalleSolicitudService = consultarDetalleSolicitudService;
        this.consultarFechaSolicitudAporteRescateService = consultarFechaSolicitudAporteRescateService;
        this.consultarFlujoCustodiaFondoMutuoService = consultarFlujoCustodiaFondoMutuoService;
        this.consultarFondoService = consultarFondoService;
        this.consultarFondoMutuoReinversionService = consultarFondoMutuoReinversionService;
        this.consultarInstrumentosFondosMutuosService = consultarInstrumentosFondosMutuosService;
        this.consultarSolicitudAporteRescateUIService = consultarSolicitudAporteRescateUIService;
        this.consultarSolicitudesCertificacionMasivaService = consultarSolicitudesCertificacionMasivaService;
        this.consultarSolicitudesFFMMService = consultarSolicitudesFFMMService;
        this.consultarSolicitudesFFMMAgenteService = consultarSolicitudesFFMMAgenteService;
        this.consultarSolicitudesFFMMWSService = consultarSolicitudesFFMMWSService;
        this.consultarSolicitudesParaAGFService = consultarSolicitudesParaAGFService;
        this.consultarValidacionDJ1932PorAgnoComercialService = consultarValidacionDJ1932PorAgnoComercialService;
        this.consultarValorCuotaHistoricoService = consultarValorCuotaHistoricoService;
        this.consultarValorDolarFondoMutuoService = consultarValorDolarFondoMutuoService;
        this.crearAporteCSCService = crearAporteCSCService;
        this.crearAporteRescateFusionService = crearAporteRescateFusionService;
        this.crearAporteSAAPVService = crearAporteSAAPVService;
        this.crearComisionService = crearComisionService;
        this.crearIngresoCSCService = crearIngresoCSCService;
        this.crearIngresoCSCManejadorService = crearIngresoCSCManejadorService;
        this.crearIngresoRetiroGarantiasFondoMutuoService = crearIngresoRetiroGarantiasFondoMutuoService;
        this.crearIngresoRetiroTituloFondoMutuoService = crearIngresoRetiroTituloFondoMutuoService;
        this.crearSolicitudAGFService = crearSolicitudAGFService;
        this.crearSolicitudAporteOvernightService = crearSolicitudAporteOvernightService;
        this.crearSolicitudAportePorDividendosService = crearSolicitudAportePorDividendosService;
        this.crearSolicitudAportePorDividendosCFIService = crearSolicitudAportePorDividendosCFIService;
        this.crearSolicitudAporteReinversionService = crearSolicitudAporteReinversionService;
        this.crearSolicitudAporteRescateNormalService = crearSolicitudAporteRescateNormalService;
        this.crearSolicitudAporteRescateProgramadoService = crearSolicitudAporteRescateProgramadoService;
        this.crearSolicitudCambioSerieService = crearSolicitudCambioSerieService;
        this.crearSolicitudReinversionPorClienteService = crearSolicitudReinversionPorClienteService;
        this.crearSolicitudRescateComisionNNService = crearSolicitudRescateComisionNNService;
        this.crearSolicitudRescateNormalPorClienteService = crearSolicitudRescateNormalPorClienteService;
        this.crearSolicitudRescatePorComisionService = crearSolicitudRescatePorComisionService;
        this.crearSolicitudRescateReinversionService = crearSolicitudRescateReinversionService;
        this.crearSolicitudRescateReinversion1NService = crearSolicitudRescateReinversion1NService;
        this.crearSolicitudRescateTransferenciaService = crearSolicitudRescateTransferenciaService;
        this.crearSolicitudRescateTraspasoService = crearSolicitudRescateTraspasoService;
        this.crearSolicitudRescateTraspasoPortafolioService = crearSolicitudRescateTraspasoPortafolioService;
        this.crearTramoComisionService = crearTramoComisionService;
        this.crearTraspasoService = crearTraspasoService;
        this.cuadrarOmnibusDetalleService = cuadrarOmnibusDetalleService;
        this.cuadraturaCierreDiarioService = cuadraturaCierreDiarioService;
        this.generacionArchivoAGFService = generacionArchivoAGFService;
        this.generarArchivoMovimientosService = generarArchivoMovimientosService;
        this.generarArchivoParticipesService = generarArchivoParticipesService;
        this.generarArchivoSolicitudAporteRescateService = generarArchivoSolicitudAporteRescateService;
        this.generarCertificado31MasivoService = generarCertificado31MasivoService;
        this.generarDataReporteTRXIService = generarDataReporteTRXIService;
        this.generarDataReporteTRXIFolioContratoService = generarDataReporteTRXIFolioContratoService;
        this.generarInformeRentabilidadFFMMExtranjeroMasivoService = generarInformeRentabilidadFFMMExtranjeroMasivoService;
        this.inventarioFileManagerService = inventarioFileManagerService;
        this.liquidacionSolicitudAporteRescateService = liquidacionSolicitudAporteRescateService;
        this.manejadorSolicitudAporteRescateService = manejadorSolicitudAporteRescateService;
        this.modificarComisionService = modificarComisionService;
        this.modificarComisionSolicitudPorPerfeccionamientoService = modificarComisionSolicitudPorPerfeccionamientoService;
        this.modificarCSCService = modificarCSCService;
        this.modificarCSCManejadorService = modificarCSCManejadorService;
        this.modificarMontoNumeroCuotasSolicitudService = modificarMontoNumeroCuotasSolicitudService;
        this.modificarTramoComisionService = modificarTramoComisionService;
        this.modificarTraspasoService = modificarTraspasoService;
        this.notificarEstadoOrdenFondoMutuoService = notificarEstadoOrdenFondoMutuoService;
        this.notificarTraspasoConfirmarService = notificarTraspasoConfirmarService;
        this.obtenerPerfilFondoMutuoService = obtenerPerfilFondoMutuoService;
        this.procesarArchivoCuadraturaInternacionalService = procesarArchivoCuadraturaInternacionalService;
        this.procesarArchivoCuadraturaNacionalService = procesarArchivoCuadraturaNacionalService;
        this.procesarCargaSolicitudesAporteRescateService = procesarCargaSolicitudesAporteRescateService;
        this.procesarComisionesNNService = procesarComisionesNNService;
        this.procesarCustodiaFondoMutuoService = procesarCustodiaFondoMutuoService;
        this.procesarIngresoEgresoTesoreriaService = procesarIngresoEgresoTesoreriaService;
        this.procesarIngresoOperacionSpotService = procesarIngresoOperacionSpotService;
        this.procesarReinversionRescateService = procesarReinversionRescateService;
        this.quitarTramoComisionService = quitarTramoComisionService;
        this.recaudacionTraspasoService = recaudacionTraspasoService;
        this.rechazarModificarFechaGestionOperacionesService = rechazarModificarFechaGestionOperacionesService;
        this.reniciarFolioOperacionOmnibusEmisorService = reniciarFolioOperacionOmnibusEmisorService;
        this.revocacionContratoCSCService = revocacionContratoCSCService;
        this.revocacionContratoCSCManejadorService = revocacionContratoCSCManejadorService;
        this.servicioCumpleRequisitosCrearSolicitudAporteRescateService = servicioCumpleRequisitosCrearSolicitudAporteRescateService;
        this.simulaCalculoSpreadRescateService = simulaCalculoSpreadRescateService;
        this.trxCFileManagerService = trxCFileManagerService;
        this.trxPFileManagerService = trxPFileManagerService;
        this.validaMontoInversionService = validaMontoInversionService;
        this.validaPatrimonioEmisorService = validaPatrimonioEmisorService;
        this.valorCuotaFileManagerService = valorCuotaFileManagerService;
        this.valorCuotaInternacionalFileManagerService = valorCuotaInternacionalFileManagerService;
        this.valorizaCarteraFFMMService = valorizaCarteraFFMMService;
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

        String fqn        = body.has("class")  ? body.get("class").getAsString()        : "";
        JsonObject params = body.has("params") ? body.get("params").getAsJsonObject()   : new JsonObject();
        int skip          = body.has("skip")   ? body.get("skip").getAsInt()             : 0;
        int take          = body.has("take")   ? body.get("take").getAsInt()             : 0;

        if (fqn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'class' field required");
        }

        log.debug("InternalJavaCall: class={} skip={} take={}", fqn, skip, take);

        try {
            JsonObject result = switch (fqn) {
                // ---- 132 wired JavaCall classes (cl.bcs.fondomutuo.*) ----
                // AperturaAutomaticoFondos: fully implemented — calls HANA SP, no TomEE deps
                case "cl.bcs.fondomutuo.AperturaAutomaticoFondos" ->
                    aperturaAutomaticoFondosService.execute(params);

                // All remaining classes throw UnsupportedOperationException → 503 TomEE fallback
                case "cl.bcs.fondomutuo.ActualizaCarteraValorizadaFFMM" ->
                    actualizaCarteraValorizadaFFMMService.execute(params);
                case "cl.bcs.fondomutuo.ActualizarCarterasFondosMutuos" ->
                    actualizarCarterasFondosMutuosService.execute(params);
                case "cl.bcs.fondomutuo.ActualizarCostoUF" ->
                    actualizarCostoUFService.execute(params);
                case "cl.bcs.fondomutuo.ActualizarCostoUFCarga" ->
                    actualizarCostoUFCargaService.execute(params);
                case "cl.bcs.fondomutuo.ActualizarCostoUF_MLT" ->
                    actualizarCostoUF_MLTService.execute(params);
                case "cl.bcs.fondomutuo.ActualizarFechaGestionOperaciones" ->
                    actualizarFechaGestionOperacionesService.execute(params);
                case "cl.bcs.fondomutuo.ActualizarFondoIndustria" ->
                    actualizarFondoIndustriaService.execute(params);
                case "cl.bcs.fondomutuo.AdministracionLiquidez" ->
                    administracionLiquidezService.execute(params);
                case "cl.bcs.fondomutuo.AdministrarComisiones" ->
                    administrarComisionesService.execute(params);
                case "cl.bcs.fondomutuo.AgregarLogCertificacionAporteRescate" ->
                    agregarLogCertificacionAporteRescateService.execute(params);
                case "cl.bcs.fondomutuo.AnulacionContratoCSC" ->
                    anulacionContratoCSCService.execute(params);
                case "cl.bcs.fondomutuo.AnulacionSolicitudAporteRescate" ->
                    anulacionSolicitudAporteRescateService.execute(params);
                case "cl.bcs.fondomutuo.AnulacionSolicitudAporteRescateFolioContrato" ->
                    anulacionSolicitudAporteRescateFolioContratoService.execute(params);
                case "cl.bcs.fondomutuo.AnulacionTraspaso" ->
                    anulacionTraspasoService.execute(params);
                case "cl.bcs.fondomutuo.AutorizarOmnibus" ->
                    autorizarOmnibusService.execute(params);
                case "cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen" ->
                    buscarCantidadesPorRegimenService.execute(params);
                case "cl.bcs.fondomutuo.CalculaCustodiaRescate" ->
                    calculaCustodiaRescateService.execute(params);
                case "cl.bcs.fondomutuo.CalcularComisiones" ->
                    calcularComisionesService.execute(params);
                case "cl.bcs.fondomutuo.CalcularCostoFactorAjuste" ->
                    calcularCostoFactorAjusteService.execute(params);
                case "cl.bcs.fondomutuo.CalcularCostoUFAportes" ->
                    calcularCostoUFAportesService.execute(params);
                case "cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate" ->
                    calcularFechaSolicitudAporteRescateService.execute(params);
                case "cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescateJava" ->
                    calcularFechaSolicitudAporteRescateJavaService.execute(params);
                case "cl.bcs.fondomutuo.CalcularMontoNumeroCuota" ->
                    calcularMontoNumeroCuotaService.execute(params);
                case "cl.bcs.fondomutuo.CalcularSpreadRescate" ->
                    calcularSpreadRescateService.execute(params);
                case "cl.bcs.fondomutuo.CalcularValores" ->
                    calcularValoresService.execute(params);
                case "cl.bcs.fondomutuo.CambiarEstadoSolicitudAgf" ->
                    cambiarEstadoSolicitudAgfService.execute(params);
                case "cl.bcs.fondomutuo.CargarArchivoCuadraturaInternacionalUI" ->
                    cargarArchivoCuadraturaInternacionalUIService.execute(params);
                case "cl.bcs.fondomutuo.CargarArchivoCuadraturaNacionalUI" ->
                    cargarArchivoCuadraturaNacionalUIService.execute(params);
                case "cl.bcs.fondomutuo.CargarCustodiaFondoMutuo" ->
                    cargarCustodiaFondoMutuoService.execute(params);
                case "cl.bcs.fondomutuo.CargaReinversionesFondoMutuo" ->
                    cargaReinversionesFondoMutuoService.execute(params);
                case "cl.bcs.fondomutuo.CargarValorCuota" ->
                    cargarValorCuotaService.execute(params);
                case "cl.bcs.fondomutuo.CertificarPorFiltrosSolicitudAporteRescate" ->
                    certifiPorFiltrosSolicitudAporteRescateService.execute(params);
                case "cl.bcs.fondomutuo.CertificarSolicitudAporteRescate" ->
                    certificarSolicitudAporteRescateService.execute(params);
                case "cl.bcs.fondomutuo.ConfigurarFondoAdministracionLiquidez" ->
                    configurarFondoAdministracionLiquidezService.execute(params);
                case "cl.bcs.fondomutuo.ConfirmarModificarFechaGestionOperaciones" ->
                    confirmarModificarFechaGestionOperacionesService.execute(params);
                case "cl.bcs.fondomutuo.ConfirmarTraspaso" ->
                    confirmarTraspasoService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarComisionesNNFondoMutuo" ->
                    consultarComisionesNNFondoMutuoService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarCustodiaDisponibleReinversion1N" ->
                    consultarCustodiaDisponibleReinversion1NService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFFMM" ->
                    consultarCustodiaPortafolioFFMMService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFFMMWS" ->
                    consultarCustodiaPortafolioFFMMWSService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFFMMWSS" ->
                    consultarCustodiaPortafolioFFMMWSSService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo" ->
                    consultarCustodiaPortafolioFondoMutuoService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuoWS" ->
                    consultarCustodiaPortafolioFondoMutuoWSService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarDetalleSolicitud" ->
                    consultarDetalleSolicitudService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate" ->
                    consultarFechaSolicitudAporteRescateService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo" ->
                    consultarFlujoCustodiaFondoMutuoService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarFondo" ->
                    consultarFondoService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarFondoMutuoReinversion" ->
                    consultarFondoMutuoReinversionService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarInstrumentosFondosMutuos" ->
                    consultarInstrumentosFondosMutuosService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarSolicitudAporteRescateUI" ->
                    consultarSolicitudAporteRescateUIService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarSolicitudesCertificacionMasiva" ->
                    consultarSolicitudesCertificacionMasivaService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarSolicitudesFFMM" ->
                    consultarSolicitudesFFMMService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarSolicitudesFFMMAgente" ->
                    consultarSolicitudesFFMMAgenteService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarSolicitudesFFMMWS" ->
                    consultarSolicitudesFFMMWSService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarSolicitudesParaAGF" ->
                    consultarSolicitudesParaAGFService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarValidacionDJ1932PorAgnoComercial" ->
                    consultarValidacionDJ1932PorAgnoComercialService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarValorCuotaHistorico" ->
                    consultarValorCuotaHistoricoService.execute(params);
                case "cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo" ->
                    consultarValorDolarFondoMutuoService.execute(params);
                case "cl.bcs.fondomutuo.CrearAporteCSC" ->
                    crearAporteCSCService.execute(params);
                case "cl.bcs.fondomutuo.CrearAporteRescateFusion" ->
                    crearAporteRescateFusionService.execute(params);
                case "cl.bcs.fondomutuo.CrearAporteSAAPV" ->
                    crearAporteSAAPVService.execute(params);
                case "cl.bcs.fondomutuo.CrearComision" ->
                    crearComisionService.execute(params);
                case "cl.bcs.fondomutuo.CrearIngresoCSC" ->
                    crearIngresoCSCService.execute(params);
                case "cl.bcs.fondomutuo.CrearIngresoCSCManejador" ->
                    crearIngresoCSCManejadorService.execute(params);
                case "cl.bcs.fondomutuo.CrearIngresoRetiroGarantiasFondoMutuo" ->
                    crearIngresoRetiroGarantiasFondoMutuoService.execute(params);
                case "cl.bcs.fondomutuo.CrearIngresoRetiroTituloFondoMutuo" ->
                    crearIngresoRetiroTituloFondoMutuoService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudAGF" ->
                    crearSolicitudAGFService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudAporteOvernight" ->
                    crearSolicitudAporteOvernightService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos" ->
                    crearSolicitudAportePorDividendosService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudAportePorDividendosCFI" ->
                    crearSolicitudAportePorDividendosCFIService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudAporteReinversion" ->
                    crearSolicitudAporteReinversionService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal" ->
                    crearSolicitudAporteRescateNormalService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudAporteRescateProgramado" ->
                    crearSolicitudAporteRescateProgramadoService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudCambioSerie" ->
                    crearSolicitudCambioSerieService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudReinversionPorCliente" ->
                    crearSolicitudReinversionPorClienteService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudRescateComisionNN" ->
                    crearSolicitudRescateComisionNNService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudRescateNormalPorCliente" ->
                    crearSolicitudRescateNormalPorClienteService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudRescatePorComision" ->
                    crearSolicitudRescatePorComisionService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudRescateReinversion" ->
                    crearSolicitudRescateReinversionService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudRescateReinversion1N" ->
                    crearSolicitudRescateReinversion1NService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudRescateTransferencia" ->
                    crearSolicitudRescateTransferenciaService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudRescateTraspaso" ->
                    crearSolicitudRescateTraspasoService.execute(params);
                case "cl.bcs.fondomutuo.CrearSolicitudRescateTraspasoPortafolio" ->
                    crearSolicitudRescateTraspasoPortafolioService.execute(params);
                // CrearTramoComision: call() returns null stub — TomEE fallback via UnsupportedOperationException
                case "cl.bcs.fondomutuo.CrearTramoComision" ->
                    crearTramoComisionService.execute(params);
                case "cl.bcs.fondomutuo.CrearTraspaso" ->
                    crearTraspasoService.execute(params);
                case "cl.bcs.fondomutuo.CuadrarOmnibusDetalle" ->
                    cuadrarOmnibusDetalleService.execute(params);
                case "cl.bcs.fondomutuo.CuadraturaCierreDiario" ->
                    cuadraturaCierreDiarioService.execute(params);
                case "cl.bcs.fondomutuo.GeneracionArchivoAGF" ->
                    generacionArchivoAGFService.execute(params);
                case "cl.bcs.fondomutuo.GenerarArchivoMovimientos" ->
                    generarArchivoMovimientosService.execute(params);
                case "cl.bcs.fondomutuo.GenerarArchivoParticipes" ->
                    generarArchivoParticipesService.execute(params);
                case "cl.bcs.fondomutuo.GenerarArchivoSolicitudAporteRescate" ->
                    generarArchivoSolicitudAporteRescateService.execute(params);
                case "cl.bcs.fondomutuo.GenerarCertificado31Masivo" ->
                    generarCertificado31MasivoService.execute(params);
                case "cl.bcs.fondomutuo.GenerarDataReporteTRXI" ->
                    generarDataReporteTRXIService.execute(params);
                case "cl.bcs.fondomutuo.GenerarDataReporteTRXIFolioContrato" ->
                    generarDataReporteTRXIFolioContratoService.execute(params);
                case "cl.bcs.fondomutuo.GenerarInformeRentabilidadFFMMExtranjeroMasivo" ->
                    generarInformeRentabilidadFFMMExtranjeroMasivoService.execute(params);
                case "cl.bcs.fondomutuo.InventarioFileManager" ->
                    inventarioFileManagerService.execute(params);
                case "cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate" ->
                    liquidacionSolicitudAporteRescateService.execute(params);
                case "cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate" ->
                    manejadorSolicitudAporteRescateService.execute(params);
                case "cl.bcs.fondomutuo.ModificarComision" ->
                    modificarComisionService.execute(params);
                case "cl.bcs.fondomutuo.ModificarComisionSolicitudPorPerfeccionamiento" ->
                    modificarComisionSolicitudPorPerfeccionamientoService.execute(params);
                case "cl.bcs.fondomutuo.ModificarCSC" ->
                    modificarCSCService.execute(params);
                case "cl.bcs.fondomutuo.ModificarCSCManejador" ->
                    modificarCSCManejadorService.execute(params);
                case "cl.bcs.fondomutuo.ModificarMontoNumeroCuotasSolicitud" ->
                    modificarMontoNumeroCuotasSolicitudService.execute(params);
                // ModificarTramoComision: call() returns null stub — TomEE fallback
                case "cl.bcs.fondomutuo.ModificarTramoComision" ->
                    modificarTramoComisionService.execute(params);
                case "cl.bcs.fondomutuo.ModificarTraspaso" ->
                    modificarTraspasoService.execute(params);
                case "cl.bcs.fondomutuo.NotificarEstadoOrdenFondoMutuo" ->
                    notificarEstadoOrdenFondoMutuoService.execute(params);
                case "cl.bcs.fondomutuo.NotificarTraspasoConfirmar" ->
                    notificarTraspasoConfirmarService.execute(params);
                case "cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo" ->
                    obtenerPerfilFondoMutuoService.execute(params);
                case "cl.bcs.fondomutuo.ProcesarArchivoCuadraturaInternacional" ->
                    procesarArchivoCuadraturaInternacionalService.execute(params);
                case "cl.bcs.fondomutuo.ProcesarArchivoCuadraturaNacional" ->
                    procesarArchivoCuadraturaNacionalService.execute(params);
                case "cl.bcs.fondomutuo.ProcesarCargaSolicitudesAporteRescate" ->
                    procesarCargaSolicitudesAporteRescateService.execute(params);
                case "cl.bcs.fondomutuo.ProcesarComisionesNN" ->
                    procesarComisionesNNService.execute(params);
                case "cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo" ->
                    procesarCustodiaFondoMutuoService.execute(params);
                case "cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria" ->
                    procesarIngresoEgresoTesoreriaService.execute(params);
                case "cl.bcs.fondomutuo.ProcesarIngresoOperacionSpot" ->
                    procesarIngresoOperacionSpotService.execute(params);
                case "cl.bcs.fondomutuo.ProcesarReinversionRescate" ->
                    procesarReinversionRescateService.execute(params);
                // QuitarTramoComision: call() returns null stub — TomEE fallback
                case "cl.bcs.fondomutuo.QuitarTramoComision" ->
                    quitarTramoComisionService.execute(params);
                case "cl.bcs.fondomutuo.RecaudacionTraspaso" ->
                    recaudacionTraspasoService.execute(params);
                case "cl.bcs.fondomutuo.RechazarModificarFechaGestionOperaciones" ->
                    rechazarModificarFechaGestionOperacionesService.execute(params);
                case "cl.bcs.fondomutuo.ReniciarFolioOperacionOmnibusEmisor" ->
                    reniciarFolioOperacionOmnibusEmisorService.execute(params);
                case "cl.bcs.fondomutuo.RevocacionContratoCSC" ->
                    revocacionContratoCSCService.execute(params);
                case "cl.bcs.fondomutuo.RevocacionContratoCSCManejador" ->
                    revocacionContratoCSCManejadorService.execute(params);
                case "cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate" ->
                    servicioCumpleRequisitosCrearSolicitudAporteRescateService.execute(params);
                case "cl.bcs.fondomutuo.SimulaCalculoSpreadRescate" ->
                    simulaCalculoSpreadRescateService.execute(params);
                case "cl.bcs.fondomutuo.TrxCFileManager" ->
                    trxCFileManagerService.execute(params);
                case "cl.bcs.fondomutuo.TrxPFileManager" ->
                    trxPFileManagerService.execute(params);
                case "cl.bcs.fondomutuo.ValidaMontoInversion" ->
                    validaMontoInversionService.execute(params);
                case "cl.bcs.fondomutuo.ValidaPatrimonioEmisor" ->
                    validaPatrimonioEmisorService.execute(params);
                case "cl.bcs.fondomutuo.ValorCuotaFileManager" ->
                    valorCuotaFileManagerService.execute(params);
                case "cl.bcs.fondomutuo.ValorCuotaInternacionalFileManager" ->
                    valorCuotaInternacionalFileManagerService.execute(params);
                case "cl.bcs.fondomutuo.ValorizaCarteraFFMM" ->
                    valorizaCarteraFFMMService.execute(params);

                // Unknown class → 503 (BcsGateway falls back to TomEE)
                default -> throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "JavaCall class '" + fqn + "' not registered in BcsFondoMutuoServicios; TomEE fallback active");
            };
            return ResponseEntity.ok(result.toString());
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (UnsupportedOperationException uoe) {
            // Service stubs with TomEE dependencies → BcsGateway falls back to TomEE
            log.warn("InternalJavaCall: class={} has unsatisfiable TomEE dependency, falling back: {}", fqn, uoe.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "JavaCall class '" + fqn + "' requires TomEE; fallback active");
        } catch (Exception e) {
            log.error("InternalJavaCall: error dispatching class={}", fqn, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error executing JavaCall class '" + fqn + "': " + e.getMessage());
        }
    }
}
