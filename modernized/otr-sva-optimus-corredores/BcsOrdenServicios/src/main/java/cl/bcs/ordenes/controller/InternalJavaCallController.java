package cl.bcs.ordenes.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// MIGRATE service imports
import cl.bcs.ordenes.service.AceptaCancelacionInstruccionMasivoService;
import cl.bcs.ordenes.service.AgregarEventoBitacoraMasivoService;
import cl.bcs.ordenes.service.AjustarTipoCambioAsignacionService;
import cl.bcs.ordenes.service.AnticiparOrdenService;
import cl.bcs.ordenes.service.AnularAsignacionesService;
import cl.bcs.ordenes.service.AnularInstruccionMasivoService;
import cl.bcs.ordenes.service.AnularMovFacturarAsignacionService;
import cl.bcs.ordenes.service.AnularTransaccionMasivaService;
import cl.bcs.ordenes.service.AprobarRechazarSolicitudAnulacionOrdenesService;
import cl.bcs.ordenes.service.AsignacionMasivaPorArchivoService;
import cl.bcs.ordenes.service.AsignacionTransaccionLiquidacionFisicaOpMarketDataService;
import cl.bcs.ordenes.service.BorrarDocumentoInstruccionService;
import cl.bcs.ordenes.service.CalcularExigibilidadOrdenesService;
import cl.bcs.ordenes.service.CambiarEstadoAsignacionesService;
import cl.bcs.ordenes.service.CambiarEstadoLibroOrdenesService;
import cl.bcs.ordenes.service.CargaArchivoAsignacionRFService;
import cl.bcs.ordenes.service.CargaArchivoAsignacionRVService;
import cl.bcs.ordenes.service.CargaArchivoCuadraturaCavaliService;
import cl.bcs.ordenes.service.CargaArchivoIGKService;
import cl.bcs.ordenes.service.CargaArchivoTransaccionBVLService;
import cl.bcs.ordenes.service.CargaArchivoTransaccionROIXService;
import cl.bcs.ordenes.service.CargaArchivoTransaccionTOMSService;
import cl.bcs.ordenes.service.CargaAsignacionesPostrerasService;
import cl.bcs.ordenes.service.ConfirmarInstruccionMasivoService;
import cl.bcs.ordenes.service.ConfirmarRechazarAutorizacionAsignacionService;
import cl.bcs.ordenes.service.ConsultarClientesLibroOrdenesService;
import cl.bcs.ordenes.service.ConsultarCobrosOrdenesService;
import cl.bcs.ordenes.service.ConsultarCobrosOrdenesPeService;
import cl.bcs.ordenes.service.ConsultarCustodiaGarantizableService;
import cl.bcs.ordenes.service.ConsultarCustodiaIngresoOrdenService;
import cl.bcs.ordenes.service.ConsultarFormaDePagoUltimaOrdenService;
import cl.bcs.ordenes.service.ConsultarInformacionPortafolioOrdenesService;
import cl.bcs.ordenes.service.ConsultarInstruccionMasivoService;
import cl.bcs.ordenes.service.ConsultarInstruccionMotivoService;
import cl.bcs.ordenes.service.ConsultarOrdenesWSService;
import cl.bcs.ordenes.service.ConsultarProcesosCuadraturaDiariaOperacionesService;
import cl.bcs.ordenes.service.ConsultarTransaccionesAsignacionPrecioPromedioService;
import cl.bcs.ordenes.service.CrearConfiguracionShortNameService;
import cl.bcs.ordenes.service.CrearGarantiasOrdenesService;
import cl.bcs.ordenes.service.CrearInstruccionHistoricaService;
import cl.bcs.ordenes.service.CrearInstruccionMotivoService;
import cl.bcs.ordenes.service.CrearLibroOrdenesService;
import cl.bcs.ordenes.service.CrearLibrosPorDefectoService;
import cl.bcs.ordenes.service.CrearOrdenesMasivaService;
import cl.bcs.ordenes.service.CrearOrdenesPostrerasService;
import cl.bcs.ordenes.service.CrearOrdenesRapidasService;
import cl.bcs.ordenes.service.CrearSimulacionAsignacionService;
import cl.bcs.ordenes.service.CrearSimulacionAsignacionCronologicaService;
import cl.bcs.ordenes.service.CrearSimulacionAsignacionPostreraService;
import cl.bcs.ordenes.service.CrearSimulacionAsignacionPropCronoService;
import cl.bcs.ordenes.service.CrearTransaccionesHistoricasService;
import cl.bcs.ordenes.service.CrearTransaccionesMarketDataService;
import cl.bcs.ordenes.service.CuadraturaDiariaService;
import cl.bcs.ordenes.service.EditarConfiguracionShortNameService;
import cl.bcs.ordenes.service.EjecutarAsignacionPropCronoService;
import cl.bcs.ordenes.service.EjecutarDesasignacionService;
import cl.bcs.ordenes.service.EjecutarLiberacionMasivaService;
import cl.bcs.ordenes.service.EnvioFacturacionMasivoService;
import cl.bcs.ordenes.service.GenerarInformeEstadisticaService;
import cl.bcs.ordenes.service.GuardarConfiguracionAsignacionDirectaService;
import cl.bcs.ordenes.service.InformeOrdenesConfirmadasService;
import cl.bcs.ordenes.service.LiquidarCustodiaOrdenCompraService;
import cl.bcs.ordenes.service.LiquidarCustodiaOrdenVentaService;
import cl.bcs.ordenes.service.LiquidarInstruccionService;
import cl.bcs.ordenes.service.ManejadorOrdenesService;
import cl.bcs.ordenes.service.MarketData_CrearTransaccionesService;
import cl.bcs.ordenes.service.ModificarInstruccionMotivoService;
import cl.bcs.ordenes.service.ModificarLibroOrdenesService;
import cl.bcs.ordenes.service.NotificacionOrdenAsignadaService;
import cl.bcs.ordenes.service.NotificarAnticipoOrdenService;
import cl.bcs.ordenes.service.NotificarCambioTransaccionService;
import cl.bcs.ordenes.service.NotificarConfirmacionOrdenService;
import cl.bcs.ordenes.service.NotificarEstadoOrdenService;
import cl.bcs.ordenes.service.ObtenerDocumentoInstruccionService;
import cl.bcs.ordenes.service.ObtenerTipoOperacionInstrumentoService;
import cl.bcs.ordenes.service.PrepararOrdenesPostrerasJobService;
import cl.bcs.ordenes.service.ProcesarMensajeFixMMEService;
import cl.bcs.ordenes.service.RechazaCancelacionInstruccionMasivoService;
import cl.bcs.ordenes.service.RechazarInstruccionMasivoService;
import cl.bcs.ordenes.service.ReprocesarTransaccionService;
import cl.bcs.ordenes.service.ReservarLiberarTransaccionesService;
import cl.bcs.ordenes.service.ResumenOperacionesDiariasService;
import cl.bcs.ordenes.service.ReversarAnularMovFacturarAsignacionService;
import cl.bcs.ordenes.service.RutearOrdenesService;
import cl.bcs.ordenes.service.ServicioCumpleRequisitosModificarTransaccionService;
import cl.bcs.ordenes.service.ValidacionArchivoCalceService;
import cl.bcs.ordenes.service.VencimientoOrdenesService;

// DEFERRED service imports
import cl.bcs.ordenes.service.ActualizarOrderIdHTService;
import cl.bcs.ordenes.service.AdjuntarDocumentoInstruccionService;
import cl.bcs.ordenes.service.AnularAsociacionesService;
import cl.bcs.ordenes.service.AnularOrdenMasivaService;
import cl.bcs.ordenes.service.CalcularCantidadVisibleService;
import cl.bcs.ordenes.service.CalcularCoberturaOrdenesService;
import cl.bcs.ordenes.service.CalcularMontoYCobrosOrdenService;
import cl.bcs.ordenes.service.CambiarEstadoOrdenesService;
import cl.bcs.ordenes.service.CantarOrdenesService;
import cl.bcs.ordenes.service.CargaArchivoOrdenesRFService;
import cl.bcs.ordenes.service.CargaArchivoOrdenesRVService;
import cl.bcs.ordenes.service.ConfirmarAutorizacionOrdenesService;
import cl.bcs.ordenes.service.ConfirmarOrdenesService;
import cl.bcs.ordenes.service.ConfirmarOrdenesREService;
import cl.bcs.ordenes.service.ConsultarAsignacionesService;
import cl.bcs.ordenes.service.ConsultarComprobantesRefacturacionService;
import cl.bcs.ordenes.service.ConsultarCtaCustodioDefectoPortafolioService;
import cl.bcs.ordenes.service.ConsultarCustodiaSubyacenteSimultaneaService;
import cl.bcs.ordenes.service.ConsultarDemandaService;
import cl.bcs.ordenes.service.ConsultarDemandaAsignacionService;
import cl.bcs.ordenes.service.ConsultarInformacionAsignacionPropCronoService;
import cl.bcs.ordenes.service.ConsultarInformacionEnvioFacturacionMasivoService;
import cl.bcs.ordenes.service.ConsultarOrdenesService;
import cl.bcs.ordenes.service.ConsultarOrdenesConSobregiroCustodiaService;
import cl.bcs.ordenes.service.ConsultarPosicionService;
import cl.bcs.ordenes.service.ConsultarPosicionAsignacionService;
import cl.bcs.ordenes.service.ConsultarSaldoTransaccionesRefacturarService;
import cl.bcs.ordenes.service.ConsultarSolInstruccionRechazadaService;
import cl.bcs.ordenes.service.CrearAsignacionesService;
import cl.bcs.ordenes.service.CrearAsociacionesService;
import cl.bcs.ordenes.service.CrearInstruccionesService;
import cl.bcs.ordenes.service.CrearOrdenesService;
import cl.bcs.ordenes.service.CrearTransaccionInstruccionMasivoService;
import cl.bcs.ordenes.service.CrearTransaccionesService;
import cl.bcs.ordenes.service.CrearTransaccionesBolsaElectronicaService;
import cl.bcs.ordenes.service.CrearTransaccionesMarketDataBECService;
import cl.bcs.ordenes.service.EnviarAsignacionesFacturarService;
import cl.bcs.ordenes.service.ModificarInstruccionesService;
import cl.bcs.ordenes.service.ModificarOrdenesService;
import cl.bcs.ordenes.service.ModificarTasaPromedioOrdenesService;
import cl.bcs.ordenes.service.ModificarTransaccionService;
import cl.bcs.ordenes.service.ModificarTransaccionMarketDataService;
import cl.bcs.ordenes.service.ObtenerDetalleRefacturacionService;
import cl.bcs.ordenes.service.PrevisualizarDesasignacionService;
import cl.bcs.ordenes.service.PrevisualizarLiberacionMasivaService;
import cl.bcs.ordenes.service.ProcesarCargaMasivaService;
import cl.bcs.ordenes.service.ProcesarCargaMasivaInstruccionService;
import cl.bcs.ordenes.service.ProcesarIngresoOperacionSpotService;
import cl.bcs.ordenes.service.ProcesarMensajeFixService;
import cl.bcs.ordenes.service.RefacturarMovimientoService;

/**
 * Receives J-type JavaCall dispatches from BcsPlataformaWeb's CallController.
 *
 * Protocol:
 *   POST /internal/java-call
 *   Body: {"class": "fully.qualified.ClassName", "params": {...}, "skip": N, "take": M}
 *
 * Migration summary:
 *   - MIGRATED (88 classes): full Spring implementation, routed to service.execute()
 *   - DEFERRED (49 classes): external service dependencies; returns 503 for TomEE fallback
 *
 * Source: PluginLoader pattern from legacy CallServlet.java
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    private final AceptaCancelacionInstruccionMasivoService aceptaCancelacionInstruccionMasivoService;
    private final AgregarEventoBitacoraMasivoService agregarEventoBitacoraMasivoService;
    private final AjustarTipoCambioAsignacionService ajustarTipoCambioAsignacionService;
    private final AnticiparOrdenService anticiparOrdenService;
    private final AnularAsignacionesService anularAsignacionesService;
    private final AnularInstruccionMasivoService anularInstruccionMasivoService;
    private final AnularMovFacturarAsignacionService anularMovFacturarAsignacionService;
    private final AnularTransaccionMasivaService anularTransaccionMasivaService;
    private final AprobarRechazarSolicitudAnulacionOrdenesService aprobarRechazarSolicitudAnulacionOrdenesService;
    private final AsignacionMasivaPorArchivoService asignacionMasivaPorArchivoService;
    private final AsignacionTransaccionLiquidacionFisicaOpMarketDataService asignacionTransaccionLiquidacionFisicaOpMarketDataService;
    private final BorrarDocumentoInstruccionService borrarDocumentoInstruccionService;
    private final CalcularExigibilidadOrdenesService calcularExigibilidadOrdenesService;
    private final CambiarEstadoAsignacionesService cambiarEstadoAsignacionesService;
    private final CambiarEstadoLibroOrdenesService cambiarEstadoLibroOrdenesService;
    private final CargaArchivoAsignacionRFService cargaArchivoAsignacionRFService;
    private final CargaArchivoAsignacionRVService cargaArchivoAsignacionRVService;
    private final CargaArchivoCuadraturaCavaliService cargaArchivoCuadraturaCavaliService;
    private final CargaArchivoIGKService cargaArchivoIGKService;
    private final CargaArchivoTransaccionBVLService cargaArchivoTransaccionBVLService;
    private final CargaArchivoTransaccionROIXService cargaArchivoTransaccionROIXService;
    private final CargaArchivoTransaccionTOMSService cargaArchivoTransaccionTOMSService;
    private final CargaAsignacionesPostrerasService cargaAsignacionesPostrerasService;
    private final ConfirmarInstruccionMasivoService confirmarInstruccionMasivoService;
    private final ConfirmarRechazarAutorizacionAsignacionService confirmarRechazarAutorizacionAsignacionService;
    private final ConsultarClientesLibroOrdenesService consultarClientesLibroOrdenesService;
    private final ConsultarCobrosOrdenesService consultarCobrosOrdenesService;
    private final ConsultarCobrosOrdenesPeService consultarCobrosOrdenesPeService;
    private final ConsultarCustodiaGarantizableService consultarCustodiaGarantizableService;
    private final ConsultarCustodiaIngresoOrdenService consultarCustodiaIngresoOrdenService;
    private final ConsultarFormaDePagoUltimaOrdenService consultarFormaDePagoUltimaOrdenService;
    private final ConsultarInformacionPortafolioOrdenesService consultarInformacionPortafolioOrdenesService;
    private final ConsultarInstruccionMasivoService consultarInstruccionMasivoService;
    private final ConsultarInstruccionMotivoService consultarInstruccionMotivoService;
    private final ConsultarOrdenesWSService consultarOrdenesWSService;
    private final ConsultarProcesosCuadraturaDiariaOperacionesService consultarProcesosCuadraturaDiariaOperacionesService;
    private final ConsultarTransaccionesAsignacionPrecioPromedioService consultarTransaccionesAsignacionPrecioPromedioService;
    private final CrearConfiguracionShortNameService crearConfiguracionShortNameService;
    private final CrearGarantiasOrdenesService crearGarantiasOrdenesService;
    private final CrearInstruccionHistoricaService crearInstruccionHistoricaService;
    private final CrearInstruccionMotivoService crearInstruccionMotivoService;
    private final CrearLibroOrdenesService crearLibroOrdenesService;
    private final CrearLibrosPorDefectoService crearLibrosPorDefectoService;
    private final CrearOrdenesMasivaService crearOrdenesMasivaService;
    private final CrearOrdenesPostrerasService crearOrdenesPostrerasService;
    private final CrearOrdenesRapidasService crearOrdenesRapidasService;
    private final CrearSimulacionAsignacionService crearSimulacionAsignacionService;
    private final CrearSimulacionAsignacionCronologicaService crearSimulacionAsignacionCronologicaService;
    private final CrearSimulacionAsignacionPostreraService crearSimulacionAsignacionPostreraService;
    private final CrearSimulacionAsignacionPropCronoService crearSimulacionAsignacionPropCronoService;
    private final CrearTransaccionesHistoricasService crearTransaccionesHistoricasService;
    private final CrearTransaccionesMarketDataService crearTransaccionesMarketDataService;
    private final CuadraturaDiariaService cuadraturaDiariaService;
    private final EditarConfiguracionShortNameService editarConfiguracionShortNameService;
    private final EjecutarAsignacionPropCronoService ejecutarAsignacionPropCronoService;
    private final EjecutarDesasignacionService ejecutarDesasignacionService;
    private final EjecutarLiberacionMasivaService ejecutarLiberacionMasivaService;
    private final EnvioFacturacionMasivoService envioFacturacionMasivoService;
    private final GenerarInformeEstadisticaService generarInformeEstadisticaService;
    private final GuardarConfiguracionAsignacionDirectaService guardarConfiguracionAsignacionDirectaService;
    private final InformeOrdenesConfirmadasService informeOrdenesConfirmadasService;
    private final LiquidarCustodiaOrdenCompraService liquidarCustodiaOrdenCompraService;
    private final LiquidarCustodiaOrdenVentaService liquidarCustodiaOrdenVentaService;
    private final LiquidarInstruccionService liquidarInstruccionService;
    private final ManejadorOrdenesService manejadorOrdenesService;
    private final MarketData_CrearTransaccionesService marketData_CrearTransaccionesService;
    private final ModificarInstruccionMotivoService modificarInstruccionMotivoService;
    private final ModificarLibroOrdenesService modificarLibroOrdenesService;
    private final NotificacionOrdenAsignadaService notificacionOrdenAsignadaService;
    private final NotificarAnticipoOrdenService notificarAnticipoOrdenService;
    private final NotificarCambioTransaccionService notificarCambioTransaccionService;
    private final NotificarConfirmacionOrdenService notificarConfirmacionOrdenService;
    private final NotificarEstadoOrdenService notificarEstadoOrdenService;
    private final ObtenerDocumentoInstruccionService obtenerDocumentoInstruccionService;
    private final ObtenerTipoOperacionInstrumentoService obtenerTipoOperacionInstrumentoService;
    private final PrepararOrdenesPostrerasJobService prepararOrdenesPostrerasJobService;
    private final ProcesarMensajeFixMMEService procesarMensajeFixMMEService;
    private final RechazaCancelacionInstruccionMasivoService rechazaCancelacionInstruccionMasivoService;
    private final RechazarInstruccionMasivoService rechazarInstruccionMasivoService;
    private final ReprocesarTransaccionService reprocesarTransaccionService;
    private final ReservarLiberarTransaccionesService reservarLiberarTransaccionesService;
    private final ResumenOperacionesDiariasService resumenOperacionesDiariasService;
    private final ReversarAnularMovFacturarAsignacionService reversarAnularMovFacturarAsignacionService;
    private final RutearOrdenesService rutearOrdenesService;
    private final ServicioCumpleRequisitosModificarTransaccionService servicioCumpleRequisitosModificarTransaccionService;
    private final ValidacionArchivoCalceService validacionArchivoCalceService;
    private final VencimientoOrdenesService vencimientoOrdenesService;
    private final AdjuntarDocumentoInstruccionService adjuntarDocumentoInstruccionService;
    private final AnularAsociacionesService anularAsociacionesService;
    private final AnularOrdenMasivaService anularOrdenMasivaService;
    private final CalcularCantidadVisibleService calcularCantidadVisibleService;
    private final CalcularCoberturaOrdenesService calcularCoberturaOrdenesService;
    private final CalcularMontoYCobrosOrdenService calcularMontoYCobrosOrdenService;
    private final CambiarEstadoOrdenesService cambiarEstadoOrdenesService;
    private final CantarOrdenesService cantarOrdenesService;
    private final CargaArchivoOrdenesRFService cargaArchivoOrdenesRFService;
    private final CargaArchivoOrdenesRVService cargaArchivoOrdenesRVService;
    private final ConfirmarAutorizacionOrdenesService confirmarAutorizacionOrdenesService;
    private final ConfirmarOrdenesService confirmarOrdenesService;
    private final ConfirmarOrdenesREService confirmarOrdenesREService;
    private final ConsultarAsignacionesService consultarAsignacionesService;
    private final ConsultarComprobantesRefacturacionService consultarComprobantesRefacturacionService;
    private final ConsultarCtaCustodioDefectoPortafolioService consultarCtaCustodioDefectoPortafolioService;
    private final ConsultarCustodiaSubyacenteSimultaneaService consultarCustodiaSubyacenteSimultaneaService;
    private final ConsultarDemandaService consultarDemandaService;
    private final ConsultarDemandaAsignacionService consultarDemandaAsignacionService;
    private final ConsultarInformacionAsignacionPropCronoService consultarInformacionAsignacionPropCronoService;
    private final ConsultarInformacionEnvioFacturacionMasivoService consultarInformacionEnvioFacturacionMasivoService;
    private final ConsultarOrdenesService consultarOrdenesService;
    private final ConsultarOrdenesConSobregiroCustodiaService consultarOrdenesConSobregiroCustodiaService;
    private final ConsultarPosicionService consultarPosicionService;
    private final ConsultarPosicionAsignacionService consultarPosicionAsignacionService;
    private final ConsultarSaldoTransaccionesRefacturarService consultarSaldoTransaccionesRefacturarService;
    private final ConsultarSolInstruccionRechazadaService consultarSolInstruccionRechazadaService;
    private final CrearAsignacionesService crearAsignacionesService;
    private final CrearAsociacionesService crearAsociacionesService;
    private final CrearInstruccionesService crearInstruccionesService;
    private final CrearOrdenesService crearOrdenesService;
    private final CrearTransaccionInstruccionMasivoService crearTransaccionInstruccionMasivoService;
    private final CrearTransaccionesService crearTransaccionesService;
    private final CrearTransaccionesBolsaElectronicaService crearTransaccionesBolsaElectronicaService;
    private final CrearTransaccionesMarketDataBECService crearTransaccionesMarketDataBECService;
    private final EnviarAsignacionesFacturarService enviarAsignacionesFacturarService;
    private final ModificarInstruccionesService modificarInstruccionesService;
    private final ModificarOrdenesService modificarOrdenesService;
    private final ModificarTasaPromedioOrdenesService modificarTasaPromedioOrdenesService;
    private final ModificarTransaccionService modificarTransaccionService;
    private final ModificarTransaccionMarketDataService modificarTransaccionMarketDataService;
    private final ObtenerDetalleRefacturacionService obtenerDetalleRefacturacionService;
    private final PrevisualizarDesasignacionService previsualizarDesasignacionService;
    private final PrevisualizarLiberacionMasivaService previsualizarLiberacionMasivaService;
    private final ProcesarCargaMasivaService procesarCargaMasivaService;
    private final ProcesarCargaMasivaInstruccionService procesarCargaMasivaInstruccionService;
    private final ProcesarIngresoOperacionSpotService procesarIngresoOperacionSpotService;
    private final ProcesarMensajeFixService procesarMensajeFixService;
    private final RefacturarMovimientoService refacturarMovimientoService;
    private final ActualizarOrderIdHTService actualizarOrderIdHTService;

    public InternalJavaCallController(AceptaCancelacionInstruccionMasivoService aceptaCancelacionInstruccionMasivoService,
                                   AgregarEventoBitacoraMasivoService agregarEventoBitacoraMasivoService,
                                   AjustarTipoCambioAsignacionService ajustarTipoCambioAsignacionService,
                                   AnticiparOrdenService anticiparOrdenService,
                                   AnularAsignacionesService anularAsignacionesService,
                                   AnularInstruccionMasivoService anularInstruccionMasivoService,
                                   AnularMovFacturarAsignacionService anularMovFacturarAsignacionService,
                                   AnularTransaccionMasivaService anularTransaccionMasivaService,
                                   AprobarRechazarSolicitudAnulacionOrdenesService aprobarRechazarSolicitudAnulacionOrdenesService,
                                   AsignacionMasivaPorArchivoService asignacionMasivaPorArchivoService,
                                   AsignacionTransaccionLiquidacionFisicaOpMarketDataService asignacionTransaccionLiquidacionFisicaOpMarketDataService,
                                   BorrarDocumentoInstruccionService borrarDocumentoInstruccionService,
                                   CalcularExigibilidadOrdenesService calcularExigibilidadOrdenesService,
                                   CambiarEstadoAsignacionesService cambiarEstadoAsignacionesService,
                                   CambiarEstadoLibroOrdenesService cambiarEstadoLibroOrdenesService,
                                   CargaArchivoAsignacionRFService cargaArchivoAsignacionRFService,
                                   CargaArchivoAsignacionRVService cargaArchivoAsignacionRVService,
                                   CargaArchivoCuadraturaCavaliService cargaArchivoCuadraturaCavaliService,
                                   CargaArchivoIGKService cargaArchivoIGKService,
                                   CargaArchivoTransaccionBVLService cargaArchivoTransaccionBVLService,
                                   CargaArchivoTransaccionROIXService cargaArchivoTransaccionROIXService,
                                   CargaArchivoTransaccionTOMSService cargaArchivoTransaccionTOMSService,
                                   CargaAsignacionesPostrerasService cargaAsignacionesPostrerasService,
                                   ConfirmarInstruccionMasivoService confirmarInstruccionMasivoService,
                                   ConfirmarRechazarAutorizacionAsignacionService confirmarRechazarAutorizacionAsignacionService,
                                   ConsultarClientesLibroOrdenesService consultarClientesLibroOrdenesService,
                                   ConsultarCobrosOrdenesService consultarCobrosOrdenesService,
                                   ConsultarCobrosOrdenesPeService consultarCobrosOrdenesPeService,
                                   ConsultarCustodiaGarantizableService consultarCustodiaGarantizableService,
                                   ConsultarCustodiaIngresoOrdenService consultarCustodiaIngresoOrdenService,
                                   ConsultarFormaDePagoUltimaOrdenService consultarFormaDePagoUltimaOrdenService,
                                   ConsultarInformacionPortafolioOrdenesService consultarInformacionPortafolioOrdenesService,
                                   ConsultarInstruccionMasivoService consultarInstruccionMasivoService,
                                   ConsultarInstruccionMotivoService consultarInstruccionMotivoService,
                                   ConsultarOrdenesWSService consultarOrdenesWSService,
                                   ConsultarProcesosCuadraturaDiariaOperacionesService consultarProcesosCuadraturaDiariaOperacionesService,
                                   ConsultarTransaccionesAsignacionPrecioPromedioService consultarTransaccionesAsignacionPrecioPromedioService,
                                   CrearConfiguracionShortNameService crearConfiguracionShortNameService,
                                   CrearGarantiasOrdenesService crearGarantiasOrdenesService,
                                   CrearInstruccionHistoricaService crearInstruccionHistoricaService,
                                   CrearInstruccionMotivoService crearInstruccionMotivoService,
                                   CrearLibroOrdenesService crearLibroOrdenesService,
                                   CrearLibrosPorDefectoService crearLibrosPorDefectoService,
                                   CrearOrdenesMasivaService crearOrdenesMasivaService,
                                   CrearOrdenesPostrerasService crearOrdenesPostrerasService,
                                   CrearOrdenesRapidasService crearOrdenesRapidasService,
                                   CrearSimulacionAsignacionService crearSimulacionAsignacionService,
                                   CrearSimulacionAsignacionCronologicaService crearSimulacionAsignacionCronologicaService,
                                   CrearSimulacionAsignacionPostreraService crearSimulacionAsignacionPostreraService,
                                   CrearSimulacionAsignacionPropCronoService crearSimulacionAsignacionPropCronoService,
                                   CrearTransaccionesHistoricasService crearTransaccionesHistoricasService,
                                   CrearTransaccionesMarketDataService crearTransaccionesMarketDataService,
                                   CuadraturaDiariaService cuadraturaDiariaService,
                                   EditarConfiguracionShortNameService editarConfiguracionShortNameService,
                                   EjecutarAsignacionPropCronoService ejecutarAsignacionPropCronoService,
                                   EjecutarDesasignacionService ejecutarDesasignacionService,
                                   EjecutarLiberacionMasivaService ejecutarLiberacionMasivaService,
                                   EnvioFacturacionMasivoService envioFacturacionMasivoService,
                                   GenerarInformeEstadisticaService generarInformeEstadisticaService,
                                   GuardarConfiguracionAsignacionDirectaService guardarConfiguracionAsignacionDirectaService,
                                   InformeOrdenesConfirmadasService informeOrdenesConfirmadasService,
                                   LiquidarCustodiaOrdenCompraService liquidarCustodiaOrdenCompraService,
                                   LiquidarCustodiaOrdenVentaService liquidarCustodiaOrdenVentaService,
                                   LiquidarInstruccionService liquidarInstruccionService,
                                   ManejadorOrdenesService manejadorOrdenesService,
                                   MarketData_CrearTransaccionesService marketData_CrearTransaccionesService,
                                   ModificarInstruccionMotivoService modificarInstruccionMotivoService,
                                   ModificarLibroOrdenesService modificarLibroOrdenesService,
                                   NotificacionOrdenAsignadaService notificacionOrdenAsignadaService,
                                   NotificarAnticipoOrdenService notificarAnticipoOrdenService,
                                   NotificarCambioTransaccionService notificarCambioTransaccionService,
                                   NotificarConfirmacionOrdenService notificarConfirmacionOrdenService,
                                   NotificarEstadoOrdenService notificarEstadoOrdenService,
                                   ObtenerDocumentoInstruccionService obtenerDocumentoInstruccionService,
                                   ObtenerTipoOperacionInstrumentoService obtenerTipoOperacionInstrumentoService,
                                   PrepararOrdenesPostrerasJobService prepararOrdenesPostrerasJobService,
                                   ProcesarMensajeFixMMEService procesarMensajeFixMMEService,
                                   RechazaCancelacionInstruccionMasivoService rechazaCancelacionInstruccionMasivoService,
                                   RechazarInstruccionMasivoService rechazarInstruccionMasivoService,
                                   ReprocesarTransaccionService reprocesarTransaccionService,
                                   ReservarLiberarTransaccionesService reservarLiberarTransaccionesService,
                                   ResumenOperacionesDiariasService resumenOperacionesDiariasService,
                                   ReversarAnularMovFacturarAsignacionService reversarAnularMovFacturarAsignacionService,
                                   RutearOrdenesService rutearOrdenesService,
                                   ServicioCumpleRequisitosModificarTransaccionService servicioCumpleRequisitosModificarTransaccionService,
                                   ValidacionArchivoCalceService validacionArchivoCalceService,
                                   VencimientoOrdenesService vencimientoOrdenesService,
                                   AdjuntarDocumentoInstruccionService adjuntarDocumentoInstruccionService,
                                   AnularAsociacionesService anularAsociacionesService,
                                   AnularOrdenMasivaService anularOrdenMasivaService,
                                   CalcularCantidadVisibleService calcularCantidadVisibleService,
                                   CalcularCoberturaOrdenesService calcularCoberturaOrdenesService,
                                   CalcularMontoYCobrosOrdenService calcularMontoYCobrosOrdenService,
                                   CambiarEstadoOrdenesService cambiarEstadoOrdenesService,
                                   CantarOrdenesService cantarOrdenesService,
                                   CargaArchivoOrdenesRFService cargaArchivoOrdenesRFService,
                                   CargaArchivoOrdenesRVService cargaArchivoOrdenesRVService,
                                   ConfirmarAutorizacionOrdenesService confirmarAutorizacionOrdenesService,
                                   ConfirmarOrdenesService confirmarOrdenesService,
                                   ConfirmarOrdenesREService confirmarOrdenesREService,
                                   ConsultarAsignacionesService consultarAsignacionesService,
                                   ConsultarComprobantesRefacturacionService consultarComprobantesRefacturacionService,
                                   ConsultarCtaCustodioDefectoPortafolioService consultarCtaCustodioDefectoPortafolioService,
                                   ConsultarCustodiaSubyacenteSimultaneaService consultarCustodiaSubyacenteSimultaneaService,
                                   ConsultarDemandaService consultarDemandaService,
                                   ConsultarDemandaAsignacionService consultarDemandaAsignacionService,
                                   ConsultarInformacionAsignacionPropCronoService consultarInformacionAsignacionPropCronoService,
                                   ConsultarInformacionEnvioFacturacionMasivoService consultarInformacionEnvioFacturacionMasivoService,
                                   ConsultarOrdenesService consultarOrdenesService,
                                   ConsultarOrdenesConSobregiroCustodiaService consultarOrdenesConSobregiroCustodiaService,
                                   ConsultarPosicionService consultarPosicionService,
                                   ConsultarPosicionAsignacionService consultarPosicionAsignacionService,
                                   ConsultarSaldoTransaccionesRefacturarService consultarSaldoTransaccionesRefacturarService,
                                   ConsultarSolInstruccionRechazadaService consultarSolInstruccionRechazadaService,
                                   CrearAsignacionesService crearAsignacionesService,
                                   CrearAsociacionesService crearAsociacionesService,
                                   CrearInstruccionesService crearInstruccionesService,
                                   CrearOrdenesService crearOrdenesService,
                                   CrearTransaccionInstruccionMasivoService crearTransaccionInstruccionMasivoService,
                                   CrearTransaccionesService crearTransaccionesService,
                                   CrearTransaccionesBolsaElectronicaService crearTransaccionesBolsaElectronicaService,
                                   CrearTransaccionesMarketDataBECService crearTransaccionesMarketDataBECService,
                                   EnviarAsignacionesFacturarService enviarAsignacionesFacturarService,
                                   ModificarInstruccionesService modificarInstruccionesService,
                                   ModificarOrdenesService modificarOrdenesService,
                                   ModificarTasaPromedioOrdenesService modificarTasaPromedioOrdenesService,
                                   ModificarTransaccionService modificarTransaccionService,
                                   ModificarTransaccionMarketDataService modificarTransaccionMarketDataService,
                                   ObtenerDetalleRefacturacionService obtenerDetalleRefacturacionService,
                                   PrevisualizarDesasignacionService previsualizarDesasignacionService,
                                   PrevisualizarLiberacionMasivaService previsualizarLiberacionMasivaService,
                                   ProcesarCargaMasivaService procesarCargaMasivaService,
                                   ProcesarCargaMasivaInstruccionService procesarCargaMasivaInstruccionService,
                                   ProcesarIngresoOperacionSpotService procesarIngresoOperacionSpotService,
                                   ProcesarMensajeFixService procesarMensajeFixService,
                                   RefacturarMovimientoService refacturarMovimientoService,
                                   ActualizarOrderIdHTService actualizarOrderIdHTService) {
        this.aceptaCancelacionInstruccionMasivoService = aceptaCancelacionInstruccionMasivoService;
        this.agregarEventoBitacoraMasivoService = agregarEventoBitacoraMasivoService;
        this.ajustarTipoCambioAsignacionService = ajustarTipoCambioAsignacionService;
        this.anticiparOrdenService = anticiparOrdenService;
        this.anularAsignacionesService = anularAsignacionesService;
        this.anularInstruccionMasivoService = anularInstruccionMasivoService;
        this.anularMovFacturarAsignacionService = anularMovFacturarAsignacionService;
        this.anularTransaccionMasivaService = anularTransaccionMasivaService;
        this.aprobarRechazarSolicitudAnulacionOrdenesService = aprobarRechazarSolicitudAnulacionOrdenesService;
        this.asignacionMasivaPorArchivoService = asignacionMasivaPorArchivoService;
        this.asignacionTransaccionLiquidacionFisicaOpMarketDataService = asignacionTransaccionLiquidacionFisicaOpMarketDataService;
        this.borrarDocumentoInstruccionService = borrarDocumentoInstruccionService;
        this.calcularExigibilidadOrdenesService = calcularExigibilidadOrdenesService;
        this.cambiarEstadoAsignacionesService = cambiarEstadoAsignacionesService;
        this.cambiarEstadoLibroOrdenesService = cambiarEstadoLibroOrdenesService;
        this.cargaArchivoAsignacionRFService = cargaArchivoAsignacionRFService;
        this.cargaArchivoAsignacionRVService = cargaArchivoAsignacionRVService;
        this.cargaArchivoCuadraturaCavaliService = cargaArchivoCuadraturaCavaliService;
        this.cargaArchivoIGKService = cargaArchivoIGKService;
        this.cargaArchivoTransaccionBVLService = cargaArchivoTransaccionBVLService;
        this.cargaArchivoTransaccionROIXService = cargaArchivoTransaccionROIXService;
        this.cargaArchivoTransaccionTOMSService = cargaArchivoTransaccionTOMSService;
        this.cargaAsignacionesPostrerasService = cargaAsignacionesPostrerasService;
        this.confirmarInstruccionMasivoService = confirmarInstruccionMasivoService;
        this.confirmarRechazarAutorizacionAsignacionService = confirmarRechazarAutorizacionAsignacionService;
        this.consultarClientesLibroOrdenesService = consultarClientesLibroOrdenesService;
        this.consultarCobrosOrdenesService = consultarCobrosOrdenesService;
        this.consultarCobrosOrdenesPeService = consultarCobrosOrdenesPeService;
        this.consultarCustodiaGarantizableService = consultarCustodiaGarantizableService;
        this.consultarCustodiaIngresoOrdenService = consultarCustodiaIngresoOrdenService;
        this.consultarFormaDePagoUltimaOrdenService = consultarFormaDePagoUltimaOrdenService;
        this.consultarInformacionPortafolioOrdenesService = consultarInformacionPortafolioOrdenesService;
        this.consultarInstruccionMasivoService = consultarInstruccionMasivoService;
        this.consultarInstruccionMotivoService = consultarInstruccionMotivoService;
        this.consultarOrdenesWSService = consultarOrdenesWSService;
        this.consultarProcesosCuadraturaDiariaOperacionesService = consultarProcesosCuadraturaDiariaOperacionesService;
        this.consultarTransaccionesAsignacionPrecioPromedioService = consultarTransaccionesAsignacionPrecioPromedioService;
        this.crearConfiguracionShortNameService = crearConfiguracionShortNameService;
        this.crearGarantiasOrdenesService = crearGarantiasOrdenesService;
        this.crearInstruccionHistoricaService = crearInstruccionHistoricaService;
        this.crearInstruccionMotivoService = crearInstruccionMotivoService;
        this.crearLibroOrdenesService = crearLibroOrdenesService;
        this.crearLibrosPorDefectoService = crearLibrosPorDefectoService;
        this.crearOrdenesMasivaService = crearOrdenesMasivaService;
        this.crearOrdenesPostrerasService = crearOrdenesPostrerasService;
        this.crearOrdenesRapidasService = crearOrdenesRapidasService;
        this.crearSimulacionAsignacionService = crearSimulacionAsignacionService;
        this.crearSimulacionAsignacionCronologicaService = crearSimulacionAsignacionCronologicaService;
        this.crearSimulacionAsignacionPostreraService = crearSimulacionAsignacionPostreraService;
        this.crearSimulacionAsignacionPropCronoService = crearSimulacionAsignacionPropCronoService;
        this.crearTransaccionesHistoricasService = crearTransaccionesHistoricasService;
        this.crearTransaccionesMarketDataService = crearTransaccionesMarketDataService;
        this.cuadraturaDiariaService = cuadraturaDiariaService;
        this.editarConfiguracionShortNameService = editarConfiguracionShortNameService;
        this.ejecutarAsignacionPropCronoService = ejecutarAsignacionPropCronoService;
        this.ejecutarDesasignacionService = ejecutarDesasignacionService;
        this.ejecutarLiberacionMasivaService = ejecutarLiberacionMasivaService;
        this.envioFacturacionMasivoService = envioFacturacionMasivoService;
        this.generarInformeEstadisticaService = generarInformeEstadisticaService;
        this.guardarConfiguracionAsignacionDirectaService = guardarConfiguracionAsignacionDirectaService;
        this.informeOrdenesConfirmadasService = informeOrdenesConfirmadasService;
        this.liquidarCustodiaOrdenCompraService = liquidarCustodiaOrdenCompraService;
        this.liquidarCustodiaOrdenVentaService = liquidarCustodiaOrdenVentaService;
        this.liquidarInstruccionService = liquidarInstruccionService;
        this.manejadorOrdenesService = manejadorOrdenesService;
        this.marketData_CrearTransaccionesService = marketData_CrearTransaccionesService;
        this.modificarInstruccionMotivoService = modificarInstruccionMotivoService;
        this.modificarLibroOrdenesService = modificarLibroOrdenesService;
        this.notificacionOrdenAsignadaService = notificacionOrdenAsignadaService;
        this.notificarAnticipoOrdenService = notificarAnticipoOrdenService;
        this.notificarCambioTransaccionService = notificarCambioTransaccionService;
        this.notificarConfirmacionOrdenService = notificarConfirmacionOrdenService;
        this.notificarEstadoOrdenService = notificarEstadoOrdenService;
        this.obtenerDocumentoInstruccionService = obtenerDocumentoInstruccionService;
        this.obtenerTipoOperacionInstrumentoService = obtenerTipoOperacionInstrumentoService;
        this.prepararOrdenesPostrerasJobService = prepararOrdenesPostrerasJobService;
        this.procesarMensajeFixMMEService = procesarMensajeFixMMEService;
        this.rechazaCancelacionInstruccionMasivoService = rechazaCancelacionInstruccionMasivoService;
        this.rechazarInstruccionMasivoService = rechazarInstruccionMasivoService;
        this.reprocesarTransaccionService = reprocesarTransaccionService;
        this.reservarLiberarTransaccionesService = reservarLiberarTransaccionesService;
        this.resumenOperacionesDiariasService = resumenOperacionesDiariasService;
        this.reversarAnularMovFacturarAsignacionService = reversarAnularMovFacturarAsignacionService;
        this.rutearOrdenesService = rutearOrdenesService;
        this.servicioCumpleRequisitosModificarTransaccionService = servicioCumpleRequisitosModificarTransaccionService;
        this.validacionArchivoCalceService = validacionArchivoCalceService;
        this.vencimientoOrdenesService = vencimientoOrdenesService;
        this.adjuntarDocumentoInstruccionService = adjuntarDocumentoInstruccionService;
        this.anularAsociacionesService = anularAsociacionesService;
        this.anularOrdenMasivaService = anularOrdenMasivaService;
        this.calcularCantidadVisibleService = calcularCantidadVisibleService;
        this.calcularCoberturaOrdenesService = calcularCoberturaOrdenesService;
        this.calcularMontoYCobrosOrdenService = calcularMontoYCobrosOrdenService;
        this.cambiarEstadoOrdenesService = cambiarEstadoOrdenesService;
        this.cantarOrdenesService = cantarOrdenesService;
        this.cargaArchivoOrdenesRFService = cargaArchivoOrdenesRFService;
        this.cargaArchivoOrdenesRVService = cargaArchivoOrdenesRVService;
        this.confirmarAutorizacionOrdenesService = confirmarAutorizacionOrdenesService;
        this.confirmarOrdenesService = confirmarOrdenesService;
        this.confirmarOrdenesREService = confirmarOrdenesREService;
        this.consultarAsignacionesService = consultarAsignacionesService;
        this.consultarComprobantesRefacturacionService = consultarComprobantesRefacturacionService;
        this.consultarCtaCustodioDefectoPortafolioService = consultarCtaCustodioDefectoPortafolioService;
        this.consultarCustodiaSubyacenteSimultaneaService = consultarCustodiaSubyacenteSimultaneaService;
        this.consultarDemandaService = consultarDemandaService;
        this.consultarDemandaAsignacionService = consultarDemandaAsignacionService;
        this.consultarInformacionAsignacionPropCronoService = consultarInformacionAsignacionPropCronoService;
        this.consultarInformacionEnvioFacturacionMasivoService = consultarInformacionEnvioFacturacionMasivoService;
        this.consultarOrdenesService = consultarOrdenesService;
        this.consultarOrdenesConSobregiroCustodiaService = consultarOrdenesConSobregiroCustodiaService;
        this.consultarPosicionService = consultarPosicionService;
        this.consultarPosicionAsignacionService = consultarPosicionAsignacionService;
        this.consultarSaldoTransaccionesRefacturarService = consultarSaldoTransaccionesRefacturarService;
        this.consultarSolInstruccionRechazadaService = consultarSolInstruccionRechazadaService;
        this.crearAsignacionesService = crearAsignacionesService;
        this.crearAsociacionesService = crearAsociacionesService;
        this.crearInstruccionesService = crearInstruccionesService;
        this.crearOrdenesService = crearOrdenesService;
        this.crearTransaccionInstruccionMasivoService = crearTransaccionInstruccionMasivoService;
        this.crearTransaccionesService = crearTransaccionesService;
        this.crearTransaccionesBolsaElectronicaService = crearTransaccionesBolsaElectronicaService;
        this.crearTransaccionesMarketDataBECService = crearTransaccionesMarketDataBECService;
        this.enviarAsignacionesFacturarService = enviarAsignacionesFacturarService;
        this.modificarInstruccionesService = modificarInstruccionesService;
        this.modificarOrdenesService = modificarOrdenesService;
        this.modificarTasaPromedioOrdenesService = modificarTasaPromedioOrdenesService;
        this.modificarTransaccionService = modificarTransaccionService;
        this.modificarTransaccionMarketDataService = modificarTransaccionMarketDataService;
        this.obtenerDetalleRefacturacionService = obtenerDetalleRefacturacionService;
        this.previsualizarDesasignacionService = previsualizarDesasignacionService;
        this.previsualizarLiberacionMasivaService = previsualizarLiberacionMasivaService;
        this.procesarCargaMasivaService = procesarCargaMasivaService;
        this.procesarCargaMasivaInstruccionService = procesarCargaMasivaInstruccionService;
        this.procesarIngresoOperacionSpotService = procesarIngresoOperacionSpotService;
        this.procesarMensajeFixService = procesarMensajeFixService;
        this.refacturarMovimientoService = refacturarMovimientoService;
        this.actualizarOrderIdHTService = actualizarOrderIdHTService;
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

        String fqn    = body.has("class")  ? body.get("class").getAsString()           : "";
        JsonObject params = body.has("params") ? body.get("params").getAsJsonObject()   : new JsonObject();
        int skip      = body.has("skip")   ? body.get("skip").getAsInt()                : 0;
        int take      = body.has("take")   ? body.get("take").getAsInt()                : 0;

        if (fqn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'class' field required");
        }

        log.debug("InternalJavaCall: class={} skip={} take={}", fqn, skip, take);

        try {
            return switch (fqn) {
            case "cl.bcs.ordenes.AceptaCancelacionInstruccionMasivo" -> ResponseEntity.ok(aceptaCancelacionInstruccionMasivoService.execute(params).toString());
            case "cl.bcs.ordenes.AgregarEventoBitacoraMasivo" -> ResponseEntity.ok(agregarEventoBitacoraMasivoService.execute(params).toString());
            case "cl.bcs.ordenes.AjustarTipoCambioAsignacion" -> ResponseEntity.ok(ajustarTipoCambioAsignacionService.execute(params).toString());
            case "cl.bcs.ordenes.AnticiparOrden" -> ResponseEntity.ok(anticiparOrdenService.execute(params).toString());
            case "cl.bcs.ordenes.AnularAsignaciones" -> ResponseEntity.ok(anularAsignacionesService.execute(params).toString());
            case "cl.bcs.ordenes.AnularInstruccionMasivo" -> ResponseEntity.ok(anularInstruccionMasivoService.execute(params).toString());
            case "cl.bcs.ordenes.AnularMovFacturarAsignacion" -> ResponseEntity.ok(anularMovFacturarAsignacionService.execute(params).toString());
            case "cl.bcs.ordenes.AnularTransaccionMasiva" -> ResponseEntity.ok(anularTransaccionMasivaService.execute(params).toString());
            case "cl.bcs.ordenes.AprobarRechazarSolicitudAnulacionOrdenes" -> ResponseEntity.ok(aprobarRechazarSolicitudAnulacionOrdenesService.execute(params).toString());
            case "cl.bcs.ordenes.AsignacionMasivaPorArchivo" -> ResponseEntity.ok(asignacionMasivaPorArchivoService.execute(params).toString());
            case "cl.bcs.ordenes.AsignacionTransaccionLiquidacionFisicaOpMarketData" -> ResponseEntity.ok(asignacionTransaccionLiquidacionFisicaOpMarketDataService.execute(params).toString());
            case "cl.bcs.ordenes.BorrarDocumentoInstruccion" -> ResponseEntity.ok(borrarDocumentoInstruccionService.execute(params).toString());
            case "cl.bcs.ordenes.CalcularExigibilidadOrdenes" -> ResponseEntity.ok(calcularExigibilidadOrdenesService.execute(params).toString());
            case "cl.bcs.ordenes.CambiarEstadoAsignaciones" -> ResponseEntity.ok(cambiarEstadoAsignacionesService.execute(params).toString());
            case "cl.bcs.ordenes.CambiarEstadoLibroOrdenes" -> ResponseEntity.ok(cambiarEstadoLibroOrdenesService.execute(params).toString());
            case "cl.bcs.ordenes.CargaArchivoAsignacionRF" -> ResponseEntity.ok(cargaArchivoAsignacionRFService.execute(params).toString());
            case "cl.bcs.ordenes.CargaArchivoAsignacionRV" -> ResponseEntity.ok(cargaArchivoAsignacionRVService.execute(params).toString());
            case "cl.bcs.ordenes.CargaArchivoCuadraturaCavali" -> ResponseEntity.ok(cargaArchivoCuadraturaCavaliService.execute(params).toString());
            case "cl.bcs.ordenes.CargaArchivoIGK" -> ResponseEntity.ok(cargaArchivoIGKService.execute(params).toString());
            case "cl.bcs.ordenes.CargaArchivoTransaccionBVL" -> ResponseEntity.ok(cargaArchivoTransaccionBVLService.execute(params).toString());
            case "cl.bcs.ordenes.CargaArchivoTransaccionROIX" -> ResponseEntity.ok(cargaArchivoTransaccionROIXService.execute(params).toString());
            case "cl.bcs.ordenes.CargaArchivoTransaccionTOMS" -> ResponseEntity.ok(cargaArchivoTransaccionTOMSService.execute(params).toString());
            case "cl.bcs.ordenes.CargaAsignacionesPostreras" -> ResponseEntity.ok(cargaAsignacionesPostrerasService.execute(params).toString());
            case "cl.bcs.ordenes.ConfirmarInstruccionMasivo" -> ResponseEntity.ok(confirmarInstruccionMasivoService.execute(params).toString());
            case "cl.bcs.ordenes.ConfirmarRechazarAutorizacionAsignacion" -> ResponseEntity.ok(confirmarRechazarAutorizacionAsignacionService.execute(params).toString());
            case "cl.bcs.ordenes.ConsultarClientesLibroOrdenes" -> ResponseEntity.ok(consultarClientesLibroOrdenesService.execute(params).toString());
            case "cl.bcs.ordenes.ConsultarCobrosOrdenes" -> ResponseEntity.ok(consultarCobrosOrdenesService.execute(params).toString());
            case "cl.bcs.ordenes.ConsultarCobrosOrdenesPe" -> ResponseEntity.ok(consultarCobrosOrdenesPeService.execute(params).toString());
            case "cl.bcs.ordenes.ConsultarCustodiaGarantizable" -> ResponseEntity.ok(consultarCustodiaGarantizableService.execute(params).toString());
            case "cl.bcs.ordenes.ConsultarCustodiaIngresoOrden" -> ResponseEntity.ok(consultarCustodiaIngresoOrdenService.execute(params).toString());
            case "cl.bcs.ordenes.ConsultarFormaDePagoUltimaOrden" -> ResponseEntity.ok(consultarFormaDePagoUltimaOrdenService.execute(params).toString());
            case "cl.bcs.ordenes.ConsultarInformacionPortafolioOrdenes" -> ResponseEntity.ok(consultarInformacionPortafolioOrdenesService.execute(params).toString());
            case "cl.bcs.ordenes.ConsultarInstruccionMasivo" -> ResponseEntity.ok(consultarInstruccionMasivoService.execute(params).toString());
            case "cl.bcs.ordenes.ConsultarInstruccionMotivo" -> ResponseEntity.ok(consultarInstruccionMotivoService.execute(params).toString());
            case "cl.bcs.ordenes.ConsultarOrdenesWS" -> ResponseEntity.ok(consultarOrdenesWSService.execute(params).toString());
            case "cl.bcs.ordenes.ConsultarProcesosCuadraturaDiariaOperaciones" -> ResponseEntity.ok(consultarProcesosCuadraturaDiariaOperacionesService.execute(params).toString());
            case "cl.bcs.ordenes.ConsultarTransaccionesAsignacionPrecioPromedio" -> ResponseEntity.ok(consultarTransaccionesAsignacionPrecioPromedioService.execute(params).toString());
            case "cl.bcs.ordenes.CrearConfiguracionShortName" -> ResponseEntity.ok(crearConfiguracionShortNameService.execute(params).toString());
            case "cl.bcs.ordenes.CrearGarantiasOrdenes" -> ResponseEntity.ok(crearGarantiasOrdenesService.execute(params).toString());
            case "cl.bcs.ordenes.CrearInstruccionHistorica" -> ResponseEntity.ok(crearInstruccionHistoricaService.execute(params).toString());
            case "cl.bcs.ordenes.CrearInstruccionMotivo" -> ResponseEntity.ok(crearInstruccionMotivoService.execute(params).toString());
            case "cl.bcs.ordenes.CrearLibroOrdenes" -> ResponseEntity.ok(crearLibroOrdenesService.execute(params).toString());
            case "cl.bcs.ordenes.CrearLibrosPorDefecto" -> ResponseEntity.ok(crearLibrosPorDefectoService.execute(params).toString());
            case "cl.bcs.ordenes.CrearOrdenesMasiva" -> ResponseEntity.ok(crearOrdenesMasivaService.execute(params).toString());
            case "cl.bcs.ordenes.CrearOrdenesPostreras" -> ResponseEntity.ok(crearOrdenesPostrerasService.execute(params).toString());
            case "cl.bcs.ordenes.CrearOrdenesRapidas" -> ResponseEntity.ok(crearOrdenesRapidasService.execute(params).toString());
            case "cl.bcs.ordenes.CrearSimulacionAsignacion" -> ResponseEntity.ok(crearSimulacionAsignacionService.execute(params).toString());
            case "cl.bcs.ordenes.CrearSimulacionAsignacionCronologica" -> ResponseEntity.ok(crearSimulacionAsignacionCronologicaService.execute(params).toString());
            case "cl.bcs.ordenes.CrearSimulacionAsignacionPostrera" -> ResponseEntity.ok(crearSimulacionAsignacionPostreraService.execute(params).toString());
            case "cl.bcs.ordenes.CrearSimulacionAsignacionPropCrono" -> ResponseEntity.ok(crearSimulacionAsignacionPropCronoService.execute(params).toString());
            case "cl.bcs.ordenes.CrearTransaccionesHistoricas" -> ResponseEntity.ok(crearTransaccionesHistoricasService.execute(params).toString());
            case "cl.bcs.ordenes.CrearTransaccionesMarketData" -> ResponseEntity.ok(crearTransaccionesMarketDataService.execute(params).toString());
            case "cl.bcs.ordenes.CuadraturaDiaria" -> ResponseEntity.ok(cuadraturaDiariaService.execute(params).toString());
            case "cl.bcs.ordenes.EditarConfiguracionShortName" -> ResponseEntity.ok(editarConfiguracionShortNameService.execute(params).toString());
            case "cl.bcs.ordenes.EjecutarAsignacionPropCrono" -> ResponseEntity.ok(ejecutarAsignacionPropCronoService.execute(params).toString());
            case "cl.bcs.ordenes.EjecutarDesasignacion" -> ResponseEntity.ok(ejecutarDesasignacionService.execute(params).toString());
            case "cl.bcs.ordenes.EjecutarLiberacionMasiva" -> ResponseEntity.ok(ejecutarLiberacionMasivaService.execute(params).toString());
            case "cl.bcs.ordenes.EnvioFacturacionMasivo" -> ResponseEntity.ok(envioFacturacionMasivoService.execute(params).toString());
            case "cl.bcs.ordenes.GenerarInformeEstadistica" -> ResponseEntity.ok(generarInformeEstadisticaService.execute(params).toString());
            case "cl.bcs.ordenes.GuardarConfiguracionAsignacionDirecta" -> ResponseEntity.ok(guardarConfiguracionAsignacionDirectaService.execute(params).toString());
            case "cl.bcs.ordenes.InformeOrdenesConfirmadas" -> ResponseEntity.ok(informeOrdenesConfirmadasService.execute(params).toString());
            case "cl.bcs.ordenes.LiquidarCustodiaOrdenCompra" -> ResponseEntity.ok(liquidarCustodiaOrdenCompraService.execute(params).toString());
            case "cl.bcs.ordenes.LiquidarCustodiaOrdenVenta" -> ResponseEntity.ok(liquidarCustodiaOrdenVentaService.execute(params).toString());
            case "cl.bcs.ordenes.LiquidarInstruccion" -> ResponseEntity.ok(liquidarInstruccionService.execute(params).toString());
            case "cl.bcs.ordenes.ManejadorOrdenes" -> ResponseEntity.ok(manejadorOrdenesService.execute(params).toString());
            case "cl.bcs.ordenes.MarketData_CrearTransacciones" -> ResponseEntity.ok(marketData_CrearTransaccionesService.execute(params).toString());
            case "cl.bcs.ordenes.ModificarInstruccionMotivo" -> ResponseEntity.ok(modificarInstruccionMotivoService.execute(params).toString());
            case "cl.bcs.ordenes.ModificarLibroOrdenes" -> ResponseEntity.ok(modificarLibroOrdenesService.execute(params).toString());
            case "cl.bcs.ordenes.NotificacionOrdenAsignada" -> ResponseEntity.ok(notificacionOrdenAsignadaService.execute(params).toString());
            case "cl.bcs.ordenes.NotificarAnticipoOrden" -> ResponseEntity.ok(notificarAnticipoOrdenService.execute(params).toString());
            case "cl.bcs.ordenes.NotificarCambioTransaccion" -> ResponseEntity.ok(notificarCambioTransaccionService.execute(params).toString());
            case "cl.bcs.ordenes.NotificarConfirmacionOrden" -> ResponseEntity.ok(notificarConfirmacionOrdenService.execute(params).toString());
            case "cl.bcs.ordenes.NotificarEstadoOrden" -> ResponseEntity.ok(notificarEstadoOrdenService.execute(params).toString());
            case "cl.bcs.ordenes.ObtenerDocumentoInstruccion" -> ResponseEntity.ok(obtenerDocumentoInstruccionService.execute(params).toString());
            case "cl.bcs.ordenes.ObtenerTipoOperacionInstrumento" -> ResponseEntity.ok(obtenerTipoOperacionInstrumentoService.execute(params).toString());
            case "cl.bcs.ordenes.PrepararOrdenesPostrerasJob" -> ResponseEntity.ok(prepararOrdenesPostrerasJobService.execute(params).toString());
            case "cl.bcs.ordenes.ProcesarMensajeFixMME" -> ResponseEntity.ok(procesarMensajeFixMMEService.execute(params).toString());
            case "cl.bcs.ordenes.RechazaCancelacionInstruccionMasivo" -> ResponseEntity.ok(rechazaCancelacionInstruccionMasivoService.execute(params).toString());
            case "cl.bcs.ordenes.RechazarInstruccionMasivo" -> ResponseEntity.ok(rechazarInstruccionMasivoService.execute(params).toString());
            case "cl.bcs.ordenes.ReprocesarTransaccion" -> ResponseEntity.ok(reprocesarTransaccionService.execute(params).toString());
            case "cl.bcs.ordenes.ReservarLiberarTransacciones" -> ResponseEntity.ok(reservarLiberarTransaccionesService.execute(params).toString());
            case "cl.bcs.ordenes.ResumenOperacionesDiarias" -> ResponseEntity.ok(resumenOperacionesDiariasService.execute(params).toString());
            case "cl.bcs.ordenes.ReversarAnularMovFacturarAsignacion" -> ResponseEntity.ok(reversarAnularMovFacturarAsignacionService.execute(params).toString());
            case "cl.bcs.ordenes.RutearOrdenes" -> ResponseEntity.ok(rutearOrdenesService.execute(params).toString());
            case "cl.bcs.ordenes.ServicioCumpleRequisitosModificarTransaccion" -> ResponseEntity.ok(servicioCumpleRequisitosModificarTransaccionService.execute(params).toString());
            case "cl.bcs.ordenes.ValidacionArchivoCalce" -> ResponseEntity.ok(validacionArchivoCalceService.execute(params).toString());
            case "cl.bcs.ordenes.VencimientoOrdenes" -> ResponseEntity.ok(vencimientoOrdenesService.execute(params).toString());
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerDiaSiguiente; cl.bcs.plataforma.pla.utils.ConsultarEmpresa
            case "cl.bcs.ordenes.utils.ActualizarOrderIdHT" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.socionegocio.per.pojo.Entidad
            case "cl.bcs.ordenes.AdjuntarDocumentoInstruccion" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.AnularAsociaciones" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP
            case "cl.bcs.ordenes.AnularOrdenMasiva" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.mer.CalcularParidad
            case "cl.bcs.ordenes.CalcularCantidadVisible" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.mer.CalcularParidad
            case "cl.bcs.ordenes.CalcularCoberturaOrdenes" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.mer.CalcularParidad
            case "cl.bcs.ordenes.CalcularMontoYCobrosOrden" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP
            case "cl.bcs.ordenes.CambiarEstadoOrdenes" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.AgregarMensajeWebsocket
            case "cl.bcs.ordenes.CantarOrdenes" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerDiaSiguiente
            case "cl.bcs.ordenes.CargaArchivoOrdenesRF" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerDiaSiguiente
            case "cl.bcs.ordenes.CargaArchivoOrdenesRV" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.AgregarMensajeWebsocket
            case "cl.bcs.ordenes.ConfirmarAutorizacionOrdenes" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ConfirmarOrdenes" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ConfirmarOrdenesRE" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo
            case "cl.bcs.ordenes.ConsultarAsignaciones" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            case "cl.bcs.ordenes.ConsultarComprobantesRefacturacion" -> ResponseEntity.ok(consultarComprobantesRefacturacionService.execute(params).toString());
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ConsultarCtaCustodioDefectoPortafolio" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerDiaSiguiente
            case "cl.bcs.ordenes.ConsultarCustodiaSubyacenteSimultanea" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ConsultarDemanda" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ConsultarDemandaAsignacion" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ConsultarInformacionAsignacionPropCrono" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ConsultarInformacionEnvioFacturacionMasivo" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo
            case "cl.bcs.ordenes.ConsultarOrdenes" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerDiaSiguiente
            case "cl.bcs.ordenes.ConsultarOrdenesConSobregiroCustodia" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ConsultarPosicion" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ConsultarPosicionAsignacion" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            case "cl.bcs.ordenes.ConsultarSaldoTransaccionesRefacturar" -> ResponseEntity.ok(consultarSaldoTransaccionesRefacturarService.execute(params).toString());
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerDiaAnterior
            case "cl.bcs.ordenes.ConsultarSolInstruccionRechazada" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.mer.CalcularParidad
            case "cl.bcs.ordenes.CrearAsignaciones" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.mer.CalcularParidad
            case "cl.bcs.ordenes.CrearAsociaciones" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.CrearInstrucciones" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.AgregarMensajeWebsocket
            case "cl.bcs.ordenes.CrearOrdenes" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.socionegocio.soc.pojo.ResumenUsuario
            case "cl.bcs.ordenes.CrearTransaccionInstruccionMasivo" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.mer.CalcularParidad
            case "cl.bcs.ordenes.CrearTransacciones" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerDiaSiguiente
            case "cl.bcs.ordenes.CrearTransaccionesBolsaElectronica" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerPlazo
            case "cl.bcs.ordenes.CrearTransaccionesMarketDataBEC" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerPlazo
            case "cl.bcs.ordenes.EnviarAsignacionesFacturar" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ModificarInstrucciones" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ModificarOrdenes" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            case "cl.bcs.ordenes.ModificarTasaPromedioOrdenes" -> ResponseEntity.ok(modificarTasaPromedioOrdenesService.execute(params).toString());
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerPlazo
            case "cl.bcs.ordenes.ModificarTransaccion" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerDiaSiguiente
            case "cl.bcs.ordenes.ModificarTransaccionMarketData" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ObtenerDetalleRefacturacion" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.PrevisualizarDesasignacion" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            case "cl.bcs.ordenes.PrevisualizarLiberacionMasiva" -> ResponseEntity.ok(previsualizarLiberacionMasivaService.execute(params).toString());
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.ProcesarCargaMasiva" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerDiaSiguiente
            case "cl.bcs.ordenes.ProcesarCargaMasivaInstruccion" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            case "cl.bcs.ordenes.ProcesarIngresoOperacionSpot" -> ResponseEntity.ok(procesarIngresoOperacionSpotService.execute(params).toString());
            // DEFERRED: cl.bcs.plataforma.pla.ObtenerPlazo
            case "cl.bcs.ordenes.ProcesarMensajeFix" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
            // DEFERRED: cl.bcs.plataforma.pla.RevisarRequisitosBasicos
            case "cl.bcs.ordenes.RefacturarMovimiento" -> ResponseEntity.status(503).body("JavaCall '" + fqn + "' deferred — TomEE fallback active");
                default -> ResponseEntity.status(503).body(
                    "JavaCall class '" + fqn + "' not yet migrated; TomEE fallback active");
            };
        } catch (UnsupportedOperationException e) {
            // Deferred service — TomEE fallback
            log.warn("JavaCall deferred: {} — {}", fqn, e.getMessage());
            return ResponseEntity.status(503).body(e.getMessage());
        } catch (Exception e) {
            log.error("JavaCall error for {}: {}", fqn, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "JavaCall error for '" + fqn + "': " + e.getMessage());
        }
    }
}
