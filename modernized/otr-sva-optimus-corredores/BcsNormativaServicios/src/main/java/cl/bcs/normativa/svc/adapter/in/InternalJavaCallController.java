package cl.bcs.normativa.svc.adapter.in;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import cl.bcs.normativa.svc.service.AgendaGenerarNCG365MasivoService;
import cl.bcs.normativa.svc.service.AgregarModificarEntidadAuditoraService;
import cl.bcs.normativa.svc.service.CargarDatosReporteAPVService;
import cl.bcs.normativa.svc.service.CargarMLTService;
import cl.bcs.normativa.svc.service.CircularizacionGrabarCertificadoService;
import cl.bcs.normativa.svc.service.ConsultarLibroOperacionesService;
import cl.bcs.normativa.svc.service.EliminarReporteAPVService;
import cl.bcs.normativa.svc.service.GenerarCartasCIR1985Service;
import cl.bcs.normativa.svc.service.GenerarNCG380Service;
import cl.bcs.normativa.svc.service.GenerarReporteAPVService;
import cl.bcs.normativa.svc.service.GenerarReporteParticipesGravamenesService;
import cl.bcs.normativa.svc.service.IngresarCalificacionService;
import cl.bcs.normativa.svc.service.IngresarMLTService;
import cl.bcs.normativa.svc.service.ModificarNCG499Service;
import cl.bcs.normativa.svc.service.ModificarReporteAPVService;
import cl.bcs.normativa.svc.service.NotificarRespuestaCircularizacionService;
import cl.bcs.normativa.svc.service.ResumenPatrimonialService;
import cl.bcs.normativa.svc.service.ValorizarNCG499Service;
import cl.bcs.normativa.svc.service.cartola.ActualizarReprocesoService;
import cl.bcs.normativa.svc.service.cartola.CalcularRentabilidadService;
import cl.bcs.normativa.svc.service.cartola.CartolaConsolidadaHistoricoService;
import cl.bcs.normativa.svc.service.cartola.ConsultarCarteraInversionCartolaConsolidadaService;
import cl.bcs.normativa.svc.service.cartola.ConsultarCartolaOnlineService;
import cl.bcs.normativa.svc.service.cartola.ConsultarGrupoPortaforliosCartolaService;
import cl.bcs.normativa.svc.service.cartola.ConsultarInformacionClienteService;
import cl.bcs.normativa.svc.service.cartola.FlujoPatrimonialYRentabilidadService;
import cl.bcs.normativa.svc.service.cartola.FlujoPatrimonialYRentabilidad_ProcesaMovimientosCajaService;
import cl.bcs.normativa.svc.service.cartola.FlujoPatrimonialYRentabilidad_ProcesaMovimientosTitulosService;
import cl.bcs.normativa.svc.service.cartola.FlujoPatrimonialYRentabilidad_SaldoFinalService;
import cl.bcs.normativa.svc.service.cartola.GeneradorCartolaService;
import cl.bcs.normativa.svc.service.cartola.GenerarCartolaPDFService;
import cl.bcs.normativa.svc.service.cartola.GenerarCartolaService;
import cl.bcs.normativa.svc.service.cartola.GestionarClientesService;
import cl.bcs.normativa.svc.service.cartola.GestionarNominaService;
import cl.bcs.normativa.svc.service.cartola.ResumenInversionBaseService;
import cl.bcs.normativa.svc.service.cartola.ResumenInversionWrapperService;
import cl.bcs.normativa.svc.service.cartola.utils.CargaCartolasSebraService;
import cl.bcs.normativa.svc.service.cartola.utils.ComprimirCartolaEncodedService;
import cl.bcs.normativa.svc.service.cartola.utils.ConfiguradorCartolaService;
import cl.bcs.normativa.svc.service.circulares.ConsolidarClientesService;
import cl.bcs.normativa.svc.service.circulares.ConsultarInversionistasRV13419Service;
import cl.bcs.normativa.svc.service.circulares.GenerarAnexo1Circular20DetalleService;
import cl.bcs.normativa.svc.service.circulares.GenerarAnexo1Circular20Service;
import cl.bcs.normativa.svc.service.circulares.GenerarArchivoCircular1720Service;
import cl.bcs.normativa.svc.service.circulares.GenerarInformeForwardsVigentesEBoxService;
import cl.bcs.normativa.svc.service.circulares.GenerarInformeGarantiasForwardEBoxService;
import cl.bcs.normativa.svc.service.circulares.GenerarInformeMensualInversionistasRV13419Service;
import cl.bcs.normativa.svc.service.circulares.GenerarNCG380CustodiaService;
import cl.bcs.normativa.svc.service.circulares.IngresarInstrumentoCircular1720Service;
import cl.bcs.normativa.svc.service.circulares.NotificarCIRCULAR1985Service;
import cl.bcs.normativa.svc.service.circulares.NotificarCircularizacionService;
import cl.bcs.normativa.svc.service.circulares.NotificarNCG365FFMMService;
import cl.bcs.normativa.svc.service.circulares.NotificarTodosCircular1985Service;
import cl.bcs.normativa.svc.service.circulares.ProcesarCuadraturaCustodiaService;
import cl.bcs.normativa.svc.service.circulares.ReeprocesarEventoCircular1985Service;
import cl.bcs.normativa.svc.service.circulares.reportesbcc.AgregarProgramaRepoSicamService;
import cl.bcs.normativa.svc.service.circulares.reportesbcc.ConsultarEditablesService;
import cl.bcs.normativa.svc.service.circulares.reportesbcc.EnviarTransaccionService;
import cl.bcs.normativa.svc.service.circulares.reportesbcc.GenerarRepoSicamMasivoService;
import cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p1.AutorizacionEliminarREPO2P1Service;
import cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p1.AutorizacionModificarREPO2P1Service;
import cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p2.AutorizacionEliminarREPO2P2Service;
import cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p2.AutorizacionModificarREPO2P2Service;
import cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes3p1.AutorizacionEliminarREPO3P1Service;
import cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes3p1.AutorizacionModificarREPO3P1Service;
import cl.bcs.normativa.svc.service.declaraciones.CargaCertificadosTributariosOfflineService;
import cl.bcs.normativa.svc.service.declaraciones.ConsultarCertificadoOfflineService;
import cl.bcs.normativa.svc.service.declaraciones.GenerarCertificado17MasivoService;
import cl.bcs.normativa.svc.service.declaraciones.GenerarCertificado21MasivoService;
import cl.bcs.normativa.svc.service.declaraciones.GenerarCertificado24MasivoService;
import cl.bcs.normativa.svc.service.declaraciones.GenerarCertificado36MasivoService;
import cl.bcs.normativa.svc.service.declaraciones.GenerarCertificado43MasivoService;
import cl.bcs.normativa.svc.service.declaraciones.GenerarCertificado71MasivoService;
import cl.bcs.normativa.svc.service.declaraciones.GenerarCertificado9MasivoService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.CrearCalendarioPagoService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.CrearGarantiaService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.CrearPagosPeriodoService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.CrearTipoActivoGarantiaService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.EliminarCalendarioPagoService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.EliminarGarantiaService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.EliminarPagosPeriodoService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.EliminarTipoActivoGarantiaService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.GrabarDetalleService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.ModificarCalendarioPagoService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.ModificarGarantiaService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.ModificarPagosPeriodoService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.ModificarTipoActivoGarantiaService;
import cl.bcs.normativa.svc.service.declaraciones.dj1.RectificarRegistroService;
import cl.bcs.normativa.svc.service.declaraciones.dj1871.AjustarRescatesDelPeriodoService;
import cl.bcs.normativa.svc.service.declaraciones.dj1874.ValorizarDeclaracionService;
import cl.bcs.normativa.svc.service.declaraciones.dj1889.ReporteCertificado9Service;
import cl.bcs.normativa.svc.service.declaraciones.dj1894.ReporteCertificado21Service;
import cl.bcs.normativa.svc.service.declaraciones.dj1899.ReporteCertificado24Service;
import cl.bcs.normativa.svc.service.declaraciones.dj1922.ReporteCertificado43Service;
import cl.bcs.normativa.svc.service.declaraciones.dj1944.ReporteCertificado17Service;
import cl.bcs.normativa.svc.service.declaraciones.dj1949.ReporteCertificado71Service;
import cl.bcs.normativa.svc.service.declaraciones.dj999.GenerarInformeSaldosCuentaCambioService;
import cl.bcs.normativa.svc.service.fatca.GenerarDatosFatcaService;
import cl.bcs.normativa.svc.service.ils.CalcularObligacionesDerechosMonedaExtranjeraService;
import cl.bcs.normativa.svc.service.ils.ConsultarSaldosCuentasContablesService;
import cl.bcs.normativa.svc.service.ils.DetalleCoberturaILSService;
import cl.bcs.normativa.svc.service.ils.GeneraReporteILSService;
import cl.bcs.normativa.svc.service.ils.HomologarCuentasContablesService;
import cl.bcs.normativa.svc.service.ils.ItemConfiguracionMixtoService;
import cl.bcs.normativa.svc.service.ils.ObtenerSaldoCuentaContableService;
import cl.bcs.normativa.svc.service.interfaces.CambiarEstadoDJCargarService;
import cl.bcs.normativa.svc.service.interfaces.CambiarEstadoDJCertificacionService;
import cl.bcs.normativa.svc.service.interfaces.CambiarEstadoDJEstadoCertificadoService;
import cl.bcs.normativa.svc.service.interfaces.CambiarEstadoDJPreDeclaracionService;
import cl.bcs.normativa.svc.service.interfaces.CambiarEstadoDJValidacionService;
import cl.bcs.normativa.svc.service.interfaces.CargaExtornosInterfazService;
import cl.bcs.normativa.svc.service.interfaces.CargaPagoOperacionesInterfazService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazArchivoCMService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazAsignacionesService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazC08CarteraIntermediadaService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazC08CarteraNormalService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazC08CarteraPacteadaService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazC08InformacionContableService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazC40CarteraIntermediadaService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazC40CarteraNormalService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazC40CarteraPacteadaService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazC40INTDETService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazC40INTService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazC40InformacionContableService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazClienteService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazClientesEnLineaService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazClientesFinalesService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazDetalleOperacionesPolizasService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazICUService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazInstrumentosFinancierosService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazLavadodeDineroService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazMontosIntermediadosService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazOrdenesEnLineaService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazOrdenesService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazP40AseguradoraService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazP40FilialService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazPolizasService;
import cl.bcs.normativa.svc.service.interfaces.GenerarInterfazSIGService;
import cl.bcs.normativa.svc.service.interfaces.P40AgregarModificarFilialesService;
import cl.bcs.normativa.svc.service.interfaces.P40EliminarFilialesService;
import cl.bcs.normativa.svc.service.interfaces.ValidaGenerarInterfazService;

/**
 * Receives J-type JavaCall dispatches from BcsPlataformaWeb's CallController.
 *
 * Protocol:
 *   POST /internal/java-call
 *   Body: {"class": "fully.qualified.ClassName", "params": {...}, "skip": N, "take": M}
 *
 * Source: PluginLoader pattern from legacy CallServlet.java
 * Generated: 159 J-type services wired.
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    private final AgendaGenerarNCG365MasivoService agendaGenerarNCG365MasivoService;
    private final AgregarModificarEntidadAuditoraService agregarModificarEntidadAuditoraService;
    private final CargarDatosReporteAPVService cargarDatosReporteAPVService;
    private final CargarMLTService cargarMLTService;
    private final CircularizacionGrabarCertificadoService circularizacionGrabarCertificadoService;
    private final ConsultarLibroOperacionesService consultarLibroOperacionesService;
    private final EliminarReporteAPVService eliminarReporteAPVService;
    private final GenerarCartasCIR1985Service generarCartasCIR1985Service;
    private final GenerarNCG380Service generarNCG380Service;
    private final GenerarReporteAPVService generarReporteAPVService;
    private final GenerarReporteParticipesGravamenesService generarReporteParticipesGravamenesService;
    private final IngresarCalificacionService ingresarCalificacionService;
    private final IngresarMLTService ingresarMLTService;
    private final ModificarNCG499Service modificarNCG499Service;
    private final ModificarReporteAPVService modificarReporteAPVService;
    private final NotificarRespuestaCircularizacionService notificarRespuestaCircularizacionService;
    private final ResumenPatrimonialService resumenPatrimonialService;
    private final ValorizarNCG499Service valorizarNCG499Service;
    private final ActualizarReprocesoService actualizarReprocesoService;
    private final CalcularRentabilidadService calcularRentabilidadService;
    private final CartolaConsolidadaHistoricoService cartolaConsolidadaHistoricoService;
    private final ConsultarCarteraInversionCartolaConsolidadaService consultarCarteraInversionCartolaConsolidadaService;
    private final ConsultarCartolaOnlineService consultarCartolaOnlineService;
    private final ConsultarGrupoPortaforliosCartolaService consultarGrupoPortaforliosCartolaService;
    private final ConsultarInformacionClienteService consultarInformacionClienteService;
    private final FlujoPatrimonialYRentabilidadService flujoPatrimonialYRentabilidadService;
    private final FlujoPatrimonialYRentabilidad_ProcesaMovimientosCajaService flujoPatrimonialYRentabilidad_ProcesaMovimientosCajaService;
    private final FlujoPatrimonialYRentabilidad_ProcesaMovimientosTitulosService flujoPatrimonialYRentabilidad_ProcesaMovimientosTitulosService;
    private final FlujoPatrimonialYRentabilidad_SaldoFinalService flujoPatrimonialYRentabilidad_SaldoFinalService;
    private final GeneradorCartolaService generadorCartolaService;
    private final GenerarCartolaService generarCartolaService;
    private final GenerarCartolaPDFService generarCartolaPDFService;
    private final GestionarClientesService gestionarClientesService;
    private final GestionarNominaService gestionarNominaService;
    private final ResumenInversionBaseService resumenInversionBaseService;
    private final ResumenInversionWrapperService resumenInversionWrapperService;
    private final CargaCartolasSebraService cargaCartolasSebraService;
    private final ComprimirCartolaEncodedService comprimirCartolaEncodedService;
    private final ConfiguradorCartolaService configuradorCartolaService;
    private final ConsolidarClientesService consolidarClientesService;
    private final ConsultarInversionistasRV13419Service consultarInversionistasRV13419Service;
    private final GenerarAnexo1Circular20Service generarAnexo1Circular20Service;
    private final GenerarAnexo1Circular20DetalleService generarAnexo1Circular20DetalleService;
    private final GenerarArchivoCircular1720Service generarArchivoCircular1720Service;
    private final GenerarInformeForwardsVigentesEBoxService generarInformeForwardsVigentesEBoxService;
    private final GenerarInformeGarantiasForwardEBoxService generarInformeGarantiasForwardEBoxService;
    private final GenerarInformeMensualInversionistasRV13419Service generarInformeMensualInversionistasRV13419Service;
    private final GenerarNCG380CustodiaService generarNCG380CustodiaService;
    private final IngresarInstrumentoCircular1720Service ingresarInstrumentoCircular1720Service;
    private final NotificarCIRCULAR1985Service notificarCIRCULAR1985Service;
    private final NotificarCircularizacionService notificarCircularizacionService;
    private final NotificarNCG365FFMMService notificarNCG365FFMMService;
    private final NotificarTodosCircular1985Service notificarTodosCircular1985Service;
    private final ProcesarCuadraturaCustodiaService procesarCuadraturaCustodiaService;
    private final ReeprocesarEventoCircular1985Service reeprocesarEventoCircular1985Service;
    private final AgregarProgramaRepoSicamService agregarProgramaRepoSicamService;
    private final ConsultarEditablesService consultarEditablesService;
    private final EnviarTransaccionService enviarTransaccionService;
    private final GenerarRepoSicamMasivoService generarRepoSicamMasivoService;
    private final AutorizacionEliminarREPO2P1Service autorizacionEliminarREPO2P1Service;
    private final AutorizacionModificarREPO2P1Service autorizacionModificarREPO2P1Service;
    private final cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p1.ConsultarReportesService reportes2p1ConsultarReportesService;
    private final cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p1.EliminarReportesService reportes2p1EliminarReportesService;
    private final cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p1.IngresarReportesService reportes2p1IngresarReportesService;
    private final cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p1.ModificarReportesService reportes2p1ModificarReportesService;
    private final AutorizacionEliminarREPO2P2Service autorizacionEliminarREPO2P2Service;
    private final AutorizacionModificarREPO2P2Service autorizacionModificarREPO2P2Service;
    private final cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p2.ConsultarReportesService reportes2p2ConsultarReportesService;
    private final cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p2.EliminarReportesService reportes2p2EliminarReportesService;
    private final cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p2.IngresarReportesService reportes2p2IngresarReportesService;
    private final cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p2.ModificarReportesService reportes2p2ModificarReportesService;
    private final AutorizacionEliminarREPO3P1Service autorizacionEliminarREPO3P1Service;
    private final AutorizacionModificarREPO3P1Service autorizacionModificarREPO3P1Service;
    private final cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes3p1.ConsultarReportesService reportes3p1ConsultarReportesService;
    private final cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes3p1.EliminarReportesService reportes3p1EliminarReportesService;
    private final cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes3p1.IngresarReportesService reportes3p1IngresarReportesService;
    private final cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes3p1.ModificarReportesService reportes3p1ModificarReportesService;
    private final CargaCertificadosTributariosOfflineService cargaCertificadosTributariosOfflineService;
    private final ConsultarCertificadoOfflineService consultarCertificadoOfflineService;
    private final CrearCalendarioPagoService crearCalendarioPagoService;
    private final CrearGarantiaService crearGarantiaService;
    private final CrearPagosPeriodoService crearPagosPeriodoService;
    private final CrearTipoActivoGarantiaService crearTipoActivoGarantiaService;
    private final EliminarCalendarioPagoService eliminarCalendarioPagoService;
    private final EliminarGarantiaService eliminarGarantiaService;
    private final EliminarPagosPeriodoService eliminarPagosPeriodoService;
    private final EliminarTipoActivoGarantiaService eliminarTipoActivoGarantiaService;
    private final GrabarDetalleService grabarDetalleService;
    private final ModificarCalendarioPagoService modificarCalendarioPagoService;
    private final ModificarGarantiaService modificarGarantiaService;
    private final ModificarPagosPeriodoService modificarPagosPeriodoService;
    private final ModificarTipoActivoGarantiaService modificarTipoActivoGarantiaService;
    private final RectificarRegistroService rectificarRegistroService;
    private final AjustarRescatesDelPeriodoService ajustarRescatesDelPeriodoService;
    private final cl.bcs.normativa.svc.service.declaraciones.dj1871.ReajustarValorIPCService dj1871ReajustarValorIPCService;
    private final ValorizarDeclaracionService valorizarDeclaracionService;
    private final cl.bcs.normativa.svc.service.declaraciones.dj1889.ReajustarValorIPCService dj1889ReajustarValorIPCService;
    private final ReporteCertificado9Service reporteCertificado9Service;
    private final ReporteCertificado21Service reporteCertificado21Service;
    private final cl.bcs.normativa.svc.service.declaraciones.dj1899.ActualizarDetallesService dj1899ActualizarDetallesService;
    private final cl.bcs.normativa.svc.service.declaraciones.dj1899.ReajustarValorIPCService dj1899ReajustarValorIPCService;
    private final ReporteCertificado24Service reporteCertificado24Service;
    private final cl.bcs.normativa.svc.service.declaraciones.dj1922.ActualizarDetallesService dj1922ActualizarDetallesService;
    private final ReporteCertificado43Service reporteCertificado43Service;
    private final ReporteCertificado17Service reporteCertificado17Service;
    private final cl.bcs.normativa.svc.service.declaraciones.dj1949.ActualizarDetallesService dj1949ActualizarDetallesService;
    private final ReporteCertificado71Service reporteCertificado71Service;
    private final GenerarInformeSaldosCuentaCambioService generarInformeSaldosCuentaCambioService;
    private final GenerarCertificado17MasivoService generarCertificado17MasivoService;
    private final GenerarCertificado21MasivoService generarCertificado21MasivoService;
    private final GenerarCertificado24MasivoService generarCertificado24MasivoService;
    private final GenerarCertificado36MasivoService generarCertificado36MasivoService;
    private final GenerarCertificado43MasivoService generarCertificado43MasivoService;
    private final GenerarCertificado71MasivoService generarCertificado71MasivoService;
    private final GenerarCertificado9MasivoService generarCertificado9MasivoService;
    private final GenerarDatosFatcaService generarDatosFatcaService;
    private final CalcularObligacionesDerechosMonedaExtranjeraService calcularObligacionesDerechosMonedaExtranjeraService;
    private final ConsultarSaldosCuentasContablesService consultarSaldosCuentasContablesService;
    private final DetalleCoberturaILSService detalleCoberturaILSService;
    private final GeneraReporteILSService generaReporteILSService;
    private final HomologarCuentasContablesService homologarCuentasContablesService;
    private final ItemConfiguracionMixtoService itemConfiguracionMixtoService;
    private final ObtenerSaldoCuentaContableService obtenerSaldoCuentaContableService;
    private final CambiarEstadoDJCargarService cambiarEstadoDJCargarService;
    private final CambiarEstadoDJCertificacionService cambiarEstadoDJCertificacionService;
    private final CambiarEstadoDJEstadoCertificadoService cambiarEstadoDJEstadoCertificadoService;
    private final CambiarEstadoDJPreDeclaracionService cambiarEstadoDJPreDeclaracionService;
    private final CambiarEstadoDJValidacionService cambiarEstadoDJValidacionService;
    private final CargaExtornosInterfazService cargaExtornosInterfazService;
    private final CargaPagoOperacionesInterfazService cargaPagoOperacionesInterfazService;
    private final GenerarInterfazArchivoCMService generarInterfazArchivoCMService;
    private final GenerarInterfazAsignacionesService generarInterfazAsignacionesService;
    private final GenerarInterfazC08CarteraIntermediadaService generarInterfazC08CarteraIntermediadaService;
    private final GenerarInterfazC08CarteraNormalService generarInterfazC08CarteraNormalService;
    private final GenerarInterfazC08CarteraPacteadaService generarInterfazC08CarteraPacteadaService;
    private final GenerarInterfazC08InformacionContableService generarInterfazC08InformacionContableService;
    private final GenerarInterfazC40CarteraIntermediadaService generarInterfazC40CarteraIntermediadaService;
    private final GenerarInterfazC40CarteraNormalService generarInterfazC40CarteraNormalService;
    private final GenerarInterfazC40CarteraPacteadaService generarInterfazC40CarteraPacteadaService;
    private final GenerarInterfazC40INTService generarInterfazC40INTService;
    private final GenerarInterfazC40INTDETService generarInterfazC40INTDETService;
    private final GenerarInterfazC40InformacionContableService generarInterfazC40InformacionContableService;
    private final GenerarInterfazClienteService generarInterfazClienteService;
    private final GenerarInterfazClientesEnLineaService generarInterfazClientesEnLineaService;
    private final GenerarInterfazClientesFinalesService generarInterfazClientesFinalesService;
    private final GenerarInterfazDetalleOperacionesPolizasService generarInterfazDetalleOperacionesPolizasService;
    private final GenerarInterfazICUService generarInterfazICUService;
    private final GenerarInterfazInstrumentosFinancierosService generarInterfazInstrumentosFinancierosService;
    private final GenerarInterfazLavadodeDineroService generarInterfazLavadodeDineroService;
    private final GenerarInterfazMontosIntermediadosService generarInterfazMontosIntermediadosService;
    private final GenerarInterfazOrdenesService generarInterfazOrdenesService;
    private final GenerarInterfazOrdenesEnLineaService generarInterfazOrdenesEnLineaService;
    private final GenerarInterfazP40AseguradoraService generarInterfazP40AseguradoraService;
    private final GenerarInterfazP40FilialService generarInterfazP40FilialService;
    private final GenerarInterfazPolizasService generarInterfazPolizasService;
    private final GenerarInterfazSIGService generarInterfazSIGService;
    private final P40AgregarModificarFilialesService p40AgregarModificarFilialesService;
    private final P40EliminarFilialesService p40EliminarFilialesService;
    private final ValidaGenerarInterfazService validaGenerarInterfazService;

    public InternalJavaCallController(
            AgendaGenerarNCG365MasivoService agendaGenerarNCG365MasivoService,
            AgregarModificarEntidadAuditoraService agregarModificarEntidadAuditoraService,
            CargarDatosReporteAPVService cargarDatosReporteAPVService,
            CargarMLTService cargarMLTService,
            CircularizacionGrabarCertificadoService circularizacionGrabarCertificadoService,
            ConsultarLibroOperacionesService consultarLibroOperacionesService,
            EliminarReporteAPVService eliminarReporteAPVService,
            GenerarCartasCIR1985Service generarCartasCIR1985Service,
            GenerarNCG380Service generarNCG380Service,
            GenerarReporteAPVService generarReporteAPVService,
            GenerarReporteParticipesGravamenesService generarReporteParticipesGravamenesService,
            IngresarCalificacionService ingresarCalificacionService,
            IngresarMLTService ingresarMLTService,
            ModificarNCG499Service modificarNCG499Service,
            ModificarReporteAPVService modificarReporteAPVService,
            NotificarRespuestaCircularizacionService notificarRespuestaCircularizacionService,
            ResumenPatrimonialService resumenPatrimonialService,
            ValorizarNCG499Service valorizarNCG499Service,
            ActualizarReprocesoService actualizarReprocesoService,
            CalcularRentabilidadService calcularRentabilidadService,
            CartolaConsolidadaHistoricoService cartolaConsolidadaHistoricoService,
            ConsultarCarteraInversionCartolaConsolidadaService consultarCarteraInversionCartolaConsolidadaService,
            ConsultarCartolaOnlineService consultarCartolaOnlineService,
            ConsultarGrupoPortaforliosCartolaService consultarGrupoPortaforliosCartolaService,
            ConsultarInformacionClienteService consultarInformacionClienteService,
            FlujoPatrimonialYRentabilidadService flujoPatrimonialYRentabilidadService,
            FlujoPatrimonialYRentabilidad_ProcesaMovimientosCajaService flujoPatrimonialYRentabilidad_ProcesaMovimientosCajaService,
            FlujoPatrimonialYRentabilidad_ProcesaMovimientosTitulosService flujoPatrimonialYRentabilidad_ProcesaMovimientosTitulosService,
            FlujoPatrimonialYRentabilidad_SaldoFinalService flujoPatrimonialYRentabilidad_SaldoFinalService,
            GeneradorCartolaService generadorCartolaService,
            GenerarCartolaService generarCartolaService,
            GenerarCartolaPDFService generarCartolaPDFService,
            GestionarClientesService gestionarClientesService,
            GestionarNominaService gestionarNominaService,
            ResumenInversionBaseService resumenInversionBaseService,
            ResumenInversionWrapperService resumenInversionWrapperService,
            CargaCartolasSebraService cargaCartolasSebraService,
            ComprimirCartolaEncodedService comprimirCartolaEncodedService,
            ConfiguradorCartolaService configuradorCartolaService,
            ConsolidarClientesService consolidarClientesService,
            ConsultarInversionistasRV13419Service consultarInversionistasRV13419Service,
            GenerarAnexo1Circular20Service generarAnexo1Circular20Service,
            GenerarAnexo1Circular20DetalleService generarAnexo1Circular20DetalleService,
            GenerarArchivoCircular1720Service generarArchivoCircular1720Service,
            GenerarInformeForwardsVigentesEBoxService generarInformeForwardsVigentesEBoxService,
            GenerarInformeGarantiasForwardEBoxService generarInformeGarantiasForwardEBoxService,
            GenerarInformeMensualInversionistasRV13419Service generarInformeMensualInversionistasRV13419Service,
            GenerarNCG380CustodiaService generarNCG380CustodiaService,
            IngresarInstrumentoCircular1720Service ingresarInstrumentoCircular1720Service,
            NotificarCIRCULAR1985Service notificarCIRCULAR1985Service,
            NotificarCircularizacionService notificarCircularizacionService,
            NotificarNCG365FFMMService notificarNCG365FFMMService,
            NotificarTodosCircular1985Service notificarTodosCircular1985Service,
            ProcesarCuadraturaCustodiaService procesarCuadraturaCustodiaService,
            ReeprocesarEventoCircular1985Service reeprocesarEventoCircular1985Service,
            AgregarProgramaRepoSicamService agregarProgramaRepoSicamService,
            ConsultarEditablesService consultarEditablesService,
            EnviarTransaccionService enviarTransaccionService,
            GenerarRepoSicamMasivoService generarRepoSicamMasivoService,
            AutorizacionEliminarREPO2P1Service autorizacionEliminarREPO2P1Service,
            AutorizacionModificarREPO2P1Service autorizacionModificarREPO2P1Service,
            cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p1.ConsultarReportesService reportes2p1ConsultarReportesService,
            cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p1.EliminarReportesService reportes2p1EliminarReportesService,
            cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p1.IngresarReportesService reportes2p1IngresarReportesService,
            cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p1.ModificarReportesService reportes2p1ModificarReportesService,
            AutorizacionEliminarREPO2P2Service autorizacionEliminarREPO2P2Service,
            AutorizacionModificarREPO2P2Service autorizacionModificarREPO2P2Service,
            cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p2.ConsultarReportesService reportes2p2ConsultarReportesService,
            cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p2.EliminarReportesService reportes2p2EliminarReportesService,
            cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p2.IngresarReportesService reportes2p2IngresarReportesService,
            cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p2.ModificarReportesService reportes2p2ModificarReportesService,
            AutorizacionEliminarREPO3P1Service autorizacionEliminarREPO3P1Service,
            AutorizacionModificarREPO3P1Service autorizacionModificarREPO3P1Service,
            cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes3p1.ConsultarReportesService reportes3p1ConsultarReportesService,
            cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes3p1.EliminarReportesService reportes3p1EliminarReportesService,
            cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes3p1.IngresarReportesService reportes3p1IngresarReportesService,
            cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes3p1.ModificarReportesService reportes3p1ModificarReportesService,
            CargaCertificadosTributariosOfflineService cargaCertificadosTributariosOfflineService,
            ConsultarCertificadoOfflineService consultarCertificadoOfflineService,
            CrearCalendarioPagoService crearCalendarioPagoService,
            CrearGarantiaService crearGarantiaService,
            CrearPagosPeriodoService crearPagosPeriodoService,
            CrearTipoActivoGarantiaService crearTipoActivoGarantiaService,
            EliminarCalendarioPagoService eliminarCalendarioPagoService,
            EliminarGarantiaService eliminarGarantiaService,
            EliminarPagosPeriodoService eliminarPagosPeriodoService,
            EliminarTipoActivoGarantiaService eliminarTipoActivoGarantiaService,
            GrabarDetalleService grabarDetalleService,
            ModificarCalendarioPagoService modificarCalendarioPagoService,
            ModificarGarantiaService modificarGarantiaService,
            ModificarPagosPeriodoService modificarPagosPeriodoService,
            ModificarTipoActivoGarantiaService modificarTipoActivoGarantiaService,
            RectificarRegistroService rectificarRegistroService,
            AjustarRescatesDelPeriodoService ajustarRescatesDelPeriodoService,
            cl.bcs.normativa.svc.service.declaraciones.dj1871.ReajustarValorIPCService dj1871ReajustarValorIPCService,
            ValorizarDeclaracionService valorizarDeclaracionService,
            cl.bcs.normativa.svc.service.declaraciones.dj1889.ReajustarValorIPCService dj1889ReajustarValorIPCService,
            ReporteCertificado9Service reporteCertificado9Service,
            ReporteCertificado21Service reporteCertificado21Service,
            cl.bcs.normativa.svc.service.declaraciones.dj1899.ActualizarDetallesService dj1899ActualizarDetallesService,
            cl.bcs.normativa.svc.service.declaraciones.dj1899.ReajustarValorIPCService dj1899ReajustarValorIPCService,
            ReporteCertificado24Service reporteCertificado24Service,
            cl.bcs.normativa.svc.service.declaraciones.dj1922.ActualizarDetallesService dj1922ActualizarDetallesService,
            ReporteCertificado43Service reporteCertificado43Service,
            ReporteCertificado17Service reporteCertificado17Service,
            cl.bcs.normativa.svc.service.declaraciones.dj1949.ActualizarDetallesService dj1949ActualizarDetallesService,
            ReporteCertificado71Service reporteCertificado71Service,
            GenerarInformeSaldosCuentaCambioService generarInformeSaldosCuentaCambioService,
            GenerarCertificado17MasivoService generarCertificado17MasivoService,
            GenerarCertificado21MasivoService generarCertificado21MasivoService,
            GenerarCertificado24MasivoService generarCertificado24MasivoService,
            GenerarCertificado36MasivoService generarCertificado36MasivoService,
            GenerarCertificado43MasivoService generarCertificado43MasivoService,
            GenerarCertificado71MasivoService generarCertificado71MasivoService,
            GenerarCertificado9MasivoService generarCertificado9MasivoService,
            GenerarDatosFatcaService generarDatosFatcaService,
            CalcularObligacionesDerechosMonedaExtranjeraService calcularObligacionesDerechosMonedaExtranjeraService,
            ConsultarSaldosCuentasContablesService consultarSaldosCuentasContablesService,
            DetalleCoberturaILSService detalleCoberturaILSService,
            GeneraReporteILSService generaReporteILSService,
            HomologarCuentasContablesService homologarCuentasContablesService,
            ItemConfiguracionMixtoService itemConfiguracionMixtoService,
            ObtenerSaldoCuentaContableService obtenerSaldoCuentaContableService,
            CambiarEstadoDJCargarService cambiarEstadoDJCargarService,
            CambiarEstadoDJCertificacionService cambiarEstadoDJCertificacionService,
            CambiarEstadoDJEstadoCertificadoService cambiarEstadoDJEstadoCertificadoService,
            CambiarEstadoDJPreDeclaracionService cambiarEstadoDJPreDeclaracionService,
            CambiarEstadoDJValidacionService cambiarEstadoDJValidacionService,
            CargaExtornosInterfazService cargaExtornosInterfazService,
            CargaPagoOperacionesInterfazService cargaPagoOperacionesInterfazService,
            GenerarInterfazArchivoCMService generarInterfazArchivoCMService,
            GenerarInterfazAsignacionesService generarInterfazAsignacionesService,
            GenerarInterfazC08CarteraIntermediadaService generarInterfazC08CarteraIntermediadaService,
            GenerarInterfazC08CarteraNormalService generarInterfazC08CarteraNormalService,
            GenerarInterfazC08CarteraPacteadaService generarInterfazC08CarteraPacteadaService,
            GenerarInterfazC08InformacionContableService generarInterfazC08InformacionContableService,
            GenerarInterfazC40CarteraIntermediadaService generarInterfazC40CarteraIntermediadaService,
            GenerarInterfazC40CarteraNormalService generarInterfazC40CarteraNormalService,
            GenerarInterfazC40CarteraPacteadaService generarInterfazC40CarteraPacteadaService,
            GenerarInterfazC40INTService generarInterfazC40INTService,
            GenerarInterfazC40INTDETService generarInterfazC40INTDETService,
            GenerarInterfazC40InformacionContableService generarInterfazC40InformacionContableService,
            GenerarInterfazClienteService generarInterfazClienteService,
            GenerarInterfazClientesEnLineaService generarInterfazClientesEnLineaService,
            GenerarInterfazClientesFinalesService generarInterfazClientesFinalesService,
            GenerarInterfazDetalleOperacionesPolizasService generarInterfazDetalleOperacionesPolizasService,
            GenerarInterfazICUService generarInterfazICUService,
            GenerarInterfazInstrumentosFinancierosService generarInterfazInstrumentosFinancierosService,
            GenerarInterfazLavadodeDineroService generarInterfazLavadodeDineroService,
            GenerarInterfazMontosIntermediadosService generarInterfazMontosIntermediadosService,
            GenerarInterfazOrdenesService generarInterfazOrdenesService,
            GenerarInterfazOrdenesEnLineaService generarInterfazOrdenesEnLineaService,
            GenerarInterfazP40AseguradoraService generarInterfazP40AseguradoraService,
            GenerarInterfazP40FilialService generarInterfazP40FilialService,
            GenerarInterfazPolizasService generarInterfazPolizasService,
            GenerarInterfazSIGService generarInterfazSIGService,
            P40AgregarModificarFilialesService p40AgregarModificarFilialesService,
            P40EliminarFilialesService p40EliminarFilialesService,
            ValidaGenerarInterfazService validaGenerarInterfazService) {
        this.agendaGenerarNCG365MasivoService = agendaGenerarNCG365MasivoService;
        this.agregarModificarEntidadAuditoraService = agregarModificarEntidadAuditoraService;
        this.cargarDatosReporteAPVService = cargarDatosReporteAPVService;
        this.cargarMLTService = cargarMLTService;
        this.circularizacionGrabarCertificadoService = circularizacionGrabarCertificadoService;
        this.consultarLibroOperacionesService = consultarLibroOperacionesService;
        this.eliminarReporteAPVService = eliminarReporteAPVService;
        this.generarCartasCIR1985Service = generarCartasCIR1985Service;
        this.generarNCG380Service = generarNCG380Service;
        this.generarReporteAPVService = generarReporteAPVService;
        this.generarReporteParticipesGravamenesService = generarReporteParticipesGravamenesService;
        this.ingresarCalificacionService = ingresarCalificacionService;
        this.ingresarMLTService = ingresarMLTService;
        this.modificarNCG499Service = modificarNCG499Service;
        this.modificarReporteAPVService = modificarReporteAPVService;
        this.notificarRespuestaCircularizacionService = notificarRespuestaCircularizacionService;
        this.resumenPatrimonialService = resumenPatrimonialService;
        this.valorizarNCG499Service = valorizarNCG499Service;
        this.actualizarReprocesoService = actualizarReprocesoService;
        this.calcularRentabilidadService = calcularRentabilidadService;
        this.cartolaConsolidadaHistoricoService = cartolaConsolidadaHistoricoService;
        this.consultarCarteraInversionCartolaConsolidadaService = consultarCarteraInversionCartolaConsolidadaService;
        this.consultarCartolaOnlineService = consultarCartolaOnlineService;
        this.consultarGrupoPortaforliosCartolaService = consultarGrupoPortaforliosCartolaService;
        this.consultarInformacionClienteService = consultarInformacionClienteService;
        this.flujoPatrimonialYRentabilidadService = flujoPatrimonialYRentabilidadService;
        this.flujoPatrimonialYRentabilidad_ProcesaMovimientosCajaService = flujoPatrimonialYRentabilidad_ProcesaMovimientosCajaService;
        this.flujoPatrimonialYRentabilidad_ProcesaMovimientosTitulosService = flujoPatrimonialYRentabilidad_ProcesaMovimientosTitulosService;
        this.flujoPatrimonialYRentabilidad_SaldoFinalService = flujoPatrimonialYRentabilidad_SaldoFinalService;
        this.generadorCartolaService = generadorCartolaService;
        this.generarCartolaService = generarCartolaService;
        this.generarCartolaPDFService = generarCartolaPDFService;
        this.gestionarClientesService = gestionarClientesService;
        this.gestionarNominaService = gestionarNominaService;
        this.resumenInversionBaseService = resumenInversionBaseService;
        this.resumenInversionWrapperService = resumenInversionWrapperService;
        this.cargaCartolasSebraService = cargaCartolasSebraService;
        this.comprimirCartolaEncodedService = comprimirCartolaEncodedService;
        this.configuradorCartolaService = configuradorCartolaService;
        this.consolidarClientesService = consolidarClientesService;
        this.consultarInversionistasRV13419Service = consultarInversionistasRV13419Service;
        this.generarAnexo1Circular20Service = generarAnexo1Circular20Service;
        this.generarAnexo1Circular20DetalleService = generarAnexo1Circular20DetalleService;
        this.generarArchivoCircular1720Service = generarArchivoCircular1720Service;
        this.generarInformeForwardsVigentesEBoxService = generarInformeForwardsVigentesEBoxService;
        this.generarInformeGarantiasForwardEBoxService = generarInformeGarantiasForwardEBoxService;
        this.generarInformeMensualInversionistasRV13419Service = generarInformeMensualInversionistasRV13419Service;
        this.generarNCG380CustodiaService = generarNCG380CustodiaService;
        this.ingresarInstrumentoCircular1720Service = ingresarInstrumentoCircular1720Service;
        this.notificarCIRCULAR1985Service = notificarCIRCULAR1985Service;
        this.notificarCircularizacionService = notificarCircularizacionService;
        this.notificarNCG365FFMMService = notificarNCG365FFMMService;
        this.notificarTodosCircular1985Service = notificarTodosCircular1985Service;
        this.procesarCuadraturaCustodiaService = procesarCuadraturaCustodiaService;
        this.reeprocesarEventoCircular1985Service = reeprocesarEventoCircular1985Service;
        this.agregarProgramaRepoSicamService = agregarProgramaRepoSicamService;
        this.consultarEditablesService = consultarEditablesService;
        this.enviarTransaccionService = enviarTransaccionService;
        this.generarRepoSicamMasivoService = generarRepoSicamMasivoService;
        this.autorizacionEliminarREPO2P1Service = autorizacionEliminarREPO2P1Service;
        this.autorizacionModificarREPO2P1Service = autorizacionModificarREPO2P1Service;
        this.reportes2p1ConsultarReportesService = reportes2p1ConsultarReportesService;
        this.reportes2p1EliminarReportesService = reportes2p1EliminarReportesService;
        this.reportes2p1IngresarReportesService = reportes2p1IngresarReportesService;
        this.reportes2p1ModificarReportesService = reportes2p1ModificarReportesService;
        this.autorizacionEliminarREPO2P2Service = autorizacionEliminarREPO2P2Service;
        this.autorizacionModificarREPO2P2Service = autorizacionModificarREPO2P2Service;
        this.reportes2p2ConsultarReportesService = reportes2p2ConsultarReportesService;
        this.reportes2p2EliminarReportesService = reportes2p2EliminarReportesService;
        this.reportes2p2IngresarReportesService = reportes2p2IngresarReportesService;
        this.reportes2p2ModificarReportesService = reportes2p2ModificarReportesService;
        this.autorizacionEliminarREPO3P1Service = autorizacionEliminarREPO3P1Service;
        this.autorizacionModificarREPO3P1Service = autorizacionModificarREPO3P1Service;
        this.reportes3p1ConsultarReportesService = reportes3p1ConsultarReportesService;
        this.reportes3p1EliminarReportesService = reportes3p1EliminarReportesService;
        this.reportes3p1IngresarReportesService = reportes3p1IngresarReportesService;
        this.reportes3p1ModificarReportesService = reportes3p1ModificarReportesService;
        this.cargaCertificadosTributariosOfflineService = cargaCertificadosTributariosOfflineService;
        this.consultarCertificadoOfflineService = consultarCertificadoOfflineService;
        this.crearCalendarioPagoService = crearCalendarioPagoService;
        this.crearGarantiaService = crearGarantiaService;
        this.crearPagosPeriodoService = crearPagosPeriodoService;
        this.crearTipoActivoGarantiaService = crearTipoActivoGarantiaService;
        this.eliminarCalendarioPagoService = eliminarCalendarioPagoService;
        this.eliminarGarantiaService = eliminarGarantiaService;
        this.eliminarPagosPeriodoService = eliminarPagosPeriodoService;
        this.eliminarTipoActivoGarantiaService = eliminarTipoActivoGarantiaService;
        this.grabarDetalleService = grabarDetalleService;
        this.modificarCalendarioPagoService = modificarCalendarioPagoService;
        this.modificarGarantiaService = modificarGarantiaService;
        this.modificarPagosPeriodoService = modificarPagosPeriodoService;
        this.modificarTipoActivoGarantiaService = modificarTipoActivoGarantiaService;
        this.rectificarRegistroService = rectificarRegistroService;
        this.ajustarRescatesDelPeriodoService = ajustarRescatesDelPeriodoService;
        this.dj1871ReajustarValorIPCService = dj1871ReajustarValorIPCService;
        this.valorizarDeclaracionService = valorizarDeclaracionService;
        this.dj1889ReajustarValorIPCService = dj1889ReajustarValorIPCService;
        this.reporteCertificado9Service = reporteCertificado9Service;
        this.reporteCertificado21Service = reporteCertificado21Service;
        this.dj1899ActualizarDetallesService = dj1899ActualizarDetallesService;
        this.dj1899ReajustarValorIPCService = dj1899ReajustarValorIPCService;
        this.reporteCertificado24Service = reporteCertificado24Service;
        this.dj1922ActualizarDetallesService = dj1922ActualizarDetallesService;
        this.reporteCertificado43Service = reporteCertificado43Service;
        this.reporteCertificado17Service = reporteCertificado17Service;
        this.dj1949ActualizarDetallesService = dj1949ActualizarDetallesService;
        this.reporteCertificado71Service = reporteCertificado71Service;
        this.generarInformeSaldosCuentaCambioService = generarInformeSaldosCuentaCambioService;
        this.generarCertificado17MasivoService = generarCertificado17MasivoService;
        this.generarCertificado21MasivoService = generarCertificado21MasivoService;
        this.generarCertificado24MasivoService = generarCertificado24MasivoService;
        this.generarCertificado36MasivoService = generarCertificado36MasivoService;
        this.generarCertificado43MasivoService = generarCertificado43MasivoService;
        this.generarCertificado71MasivoService = generarCertificado71MasivoService;
        this.generarCertificado9MasivoService = generarCertificado9MasivoService;
        this.generarDatosFatcaService = generarDatosFatcaService;
        this.calcularObligacionesDerechosMonedaExtranjeraService = calcularObligacionesDerechosMonedaExtranjeraService;
        this.consultarSaldosCuentasContablesService = consultarSaldosCuentasContablesService;
        this.detalleCoberturaILSService = detalleCoberturaILSService;
        this.generaReporteILSService = generaReporteILSService;
        this.homologarCuentasContablesService = homologarCuentasContablesService;
        this.itemConfiguracionMixtoService = itemConfiguracionMixtoService;
        this.obtenerSaldoCuentaContableService = obtenerSaldoCuentaContableService;
        this.cambiarEstadoDJCargarService = cambiarEstadoDJCargarService;
        this.cambiarEstadoDJCertificacionService = cambiarEstadoDJCertificacionService;
        this.cambiarEstadoDJEstadoCertificadoService = cambiarEstadoDJEstadoCertificadoService;
        this.cambiarEstadoDJPreDeclaracionService = cambiarEstadoDJPreDeclaracionService;
        this.cambiarEstadoDJValidacionService = cambiarEstadoDJValidacionService;
        this.cargaExtornosInterfazService = cargaExtornosInterfazService;
        this.cargaPagoOperacionesInterfazService = cargaPagoOperacionesInterfazService;
        this.generarInterfazArchivoCMService = generarInterfazArchivoCMService;
        this.generarInterfazAsignacionesService = generarInterfazAsignacionesService;
        this.generarInterfazC08CarteraIntermediadaService = generarInterfazC08CarteraIntermediadaService;
        this.generarInterfazC08CarteraNormalService = generarInterfazC08CarteraNormalService;
        this.generarInterfazC08CarteraPacteadaService = generarInterfazC08CarteraPacteadaService;
        this.generarInterfazC08InformacionContableService = generarInterfazC08InformacionContableService;
        this.generarInterfazC40CarteraIntermediadaService = generarInterfazC40CarteraIntermediadaService;
        this.generarInterfazC40CarteraNormalService = generarInterfazC40CarteraNormalService;
        this.generarInterfazC40CarteraPacteadaService = generarInterfazC40CarteraPacteadaService;
        this.generarInterfazC40INTService = generarInterfazC40INTService;
        this.generarInterfazC40INTDETService = generarInterfazC40INTDETService;
        this.generarInterfazC40InformacionContableService = generarInterfazC40InformacionContableService;
        this.generarInterfazClienteService = generarInterfazClienteService;
        this.generarInterfazClientesEnLineaService = generarInterfazClientesEnLineaService;
        this.generarInterfazClientesFinalesService = generarInterfazClientesFinalesService;
        this.generarInterfazDetalleOperacionesPolizasService = generarInterfazDetalleOperacionesPolizasService;
        this.generarInterfazICUService = generarInterfazICUService;
        this.generarInterfazInstrumentosFinancierosService = generarInterfazInstrumentosFinancierosService;
        this.generarInterfazLavadodeDineroService = generarInterfazLavadodeDineroService;
        this.generarInterfazMontosIntermediadosService = generarInterfazMontosIntermediadosService;
        this.generarInterfazOrdenesService = generarInterfazOrdenesService;
        this.generarInterfazOrdenesEnLineaService = generarInterfazOrdenesEnLineaService;
        this.generarInterfazP40AseguradoraService = generarInterfazP40AseguradoraService;
        this.generarInterfazP40FilialService = generarInterfazP40FilialService;
        this.generarInterfazPolizasService = generarInterfazPolizasService;
        this.generarInterfazSIGService = generarInterfazSIGService;
        this.p40AgregarModificarFilialesService = p40AgregarModificarFilialesService;
        this.p40EliminarFilialesService = p40EliminarFilialesService;
        this.validaGenerarInterfazService = validaGenerarInterfazService;
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
            case "cl.bcs.normativa.AgendaGenerarNCG365Masivo" -> ResponseEntity.ok(agendaGenerarNCG365MasivoService.execute(params).toString());
            case "cl.bcs.normativa.AgregarModificarEntidadAuditora" -> ResponseEntity.ok(agregarModificarEntidadAuditoraService.execute(params).toString());
            case "cl.bcs.normativa.CargarDatosReporteAPV" -> ResponseEntity.ok(cargarDatosReporteAPVService.execute(params).toString());
            case "cl.bcs.normativa.CargarMLT" -> ResponseEntity.ok(cargarMLTService.execute(params).toString());
            case "cl.bcs.normativa.CircularizacionGrabarCertificado" -> ResponseEntity.ok(circularizacionGrabarCertificadoService.execute(params).toString());
            case "cl.bcs.normativa.ConsultarLibroOperaciones" -> ResponseEntity.ok(consultarLibroOperacionesService.execute(params).toString());
            case "cl.bcs.normativa.EliminarReporteAPV" -> ResponseEntity.ok(eliminarReporteAPVService.execute(params).toString());
            case "cl.bcs.normativa.GenerarCartasCIR1985" -> ResponseEntity.ok(generarCartasCIR1985Service.execute(params).toString());
            case "cl.bcs.normativa.GenerarNCG380" -> ResponseEntity.ok(generarNCG380Service.execute(params).toString());
            case "cl.bcs.normativa.GenerarReporteAPV" -> ResponseEntity.ok(generarReporteAPVService.execute(params).toString());
            case "cl.bcs.normativa.GenerarReporteParticipesGravamenes" -> ResponseEntity.ok(generarReporteParticipesGravamenesService.execute(params).toString());
            case "cl.bcs.normativa.IngresarCalificacion" -> ResponseEntity.ok(ingresarCalificacionService.execute(params).toString());
            case "cl.bcs.normativa.IngresarMLT" -> ResponseEntity.ok(ingresarMLTService.execute(params).toString());
            case "cl.bcs.normativa.ModificarNCG499" -> ResponseEntity.ok(modificarNCG499Service.execute(params).toString());
            case "cl.bcs.normativa.ModificarReporteAPV" -> ResponseEntity.ok(modificarReporteAPVService.execute(params).toString());
            case "cl.bcs.normativa.NotificarRespuestaCircularizacion" -> ResponseEntity.ok(notificarRespuestaCircularizacionService.execute(params).toString());
            case "cl.bcs.normativa.ResumenPatrimonial" -> ResponseEntity.ok(resumenPatrimonialService.execute(params).toString());
            case "cl.bcs.normativa.ValorizarNCG499" -> ResponseEntity.ok(valorizarNCG499Service.execute(params).toString());
            case "cl.bcs.normativa.cartola.ActualizarReproceso" -> ResponseEntity.ok(actualizarReprocesoService.execute(params).toString());
            case "cl.bcs.normativa.cartola.CalcularRentabilidad" -> ResponseEntity.ok(calcularRentabilidadService.execute(params).toString());
            case "cl.bcs.normativa.cartola.CartolaConsolidadaHistorico" -> ResponseEntity.ok(cartolaConsolidadaHistoricoService.execute(params).toString());
            case "cl.bcs.normativa.cartola.ConsultarCarteraInversionCartolaConsolidada" -> ResponseEntity.ok(consultarCarteraInversionCartolaConsolidadaService.execute(params).toString());
            case "cl.bcs.normativa.cartola.ConsultarCartolaOnline" -> ResponseEntity.ok(consultarCartolaOnlineService.execute(params).toString());
            case "cl.bcs.normativa.cartola.ConsultarGrupoPortaforliosCartola" -> ResponseEntity.ok(consultarGrupoPortaforliosCartolaService.execute(params).toString());
            case "cl.bcs.normativa.cartola.ConsultarInformacionCliente" -> ResponseEntity.ok(consultarInformacionClienteService.execute(params).toString());
            case "cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad" -> ResponseEntity.ok(flujoPatrimonialYRentabilidadService.execute(params).toString());
            case "cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_ProcesaMovimientosCaja" -> ResponseEntity.ok(flujoPatrimonialYRentabilidad_ProcesaMovimientosCajaService.execute(params).toString());
            case "cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_ProcesaMovimientosTitulos" -> ResponseEntity.ok(flujoPatrimonialYRentabilidad_ProcesaMovimientosTitulosService.execute(params).toString());
            case "cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_SaldoFinal" -> ResponseEntity.ok(flujoPatrimonialYRentabilidad_SaldoFinalService.execute(params).toString());
            case "cl.bcs.normativa.cartola.GeneradorCartola" -> ResponseEntity.ok(generadorCartolaService.execute(params).toString());
            case "cl.bcs.normativa.cartola.GenerarCartola" -> ResponseEntity.ok(generarCartolaService.execute(params).toString());
            case "cl.bcs.normativa.cartola.GenerarCartolaPDF" -> ResponseEntity.ok(generarCartolaPDFService.execute(params).toString());
            case "cl.bcs.normativa.cartola.GestionarClientes" -> ResponseEntity.ok(gestionarClientesService.execute(params).toString());
            case "cl.bcs.normativa.cartola.GestionarNomina" -> ResponseEntity.ok(gestionarNominaService.execute(params).toString());
            case "cl.bcs.normativa.cartola.ResumenInversionBase" -> ResponseEntity.ok(resumenInversionBaseService.execute(params).toString());
            case "cl.bcs.normativa.cartola.ResumenInversionWrapper" -> ResponseEntity.ok(resumenInversionWrapperService.execute(params).toString());
            case "cl.bcs.normativa.cartola.utils.CargaCartolasSebra" -> ResponseEntity.ok(cargaCartolasSebraService.execute(params).toString());
            case "cl.bcs.normativa.cartola.utils.ComprimirCartolaEncoded" -> ResponseEntity.ok(comprimirCartolaEncodedService.execute(params).toString());
            case "cl.bcs.normativa.cartola.utils.ConfiguradorCartola" -> ResponseEntity.ok(configuradorCartolaService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ConsolidarClientes" -> ResponseEntity.ok(consolidarClientesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ConsultarInversionistasRV13419" -> ResponseEntity.ok(consultarInversionistasRV13419Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.GenerarAnexo1Circular20" -> ResponseEntity.ok(generarAnexo1Circular20Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.GenerarAnexo1Circular20Detalle" -> ResponseEntity.ok(generarAnexo1Circular20DetalleService.execute(params).toString());
            case "cl.bcs.normativa.circulares.GenerarArchivoCircular1720" -> ResponseEntity.ok(generarArchivoCircular1720Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.GenerarInformeForwardsVigentesEBox" -> ResponseEntity.ok(generarInformeForwardsVigentesEBoxService.execute(params).toString());
            case "cl.bcs.normativa.circulares.GenerarInformeGarantiasForwardEBox" -> ResponseEntity.ok(generarInformeGarantiasForwardEBoxService.execute(params).toString());
            case "cl.bcs.normativa.circulares.GenerarInformeMensualInversionistasRV13419" -> ResponseEntity.ok(generarInformeMensualInversionistasRV13419Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.GenerarNCG380Custodia" -> ResponseEntity.ok(generarNCG380CustodiaService.execute(params).toString());
            case "cl.bcs.normativa.circulares.IngresarInstrumentoCircular1720" -> ResponseEntity.ok(ingresarInstrumentoCircular1720Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.NotificarCIRCULAR1985" -> ResponseEntity.ok(notificarCIRCULAR1985Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.NotificarCircularizacion" -> ResponseEntity.ok(notificarCircularizacionService.execute(params).toString());
            case "cl.bcs.normativa.circulares.NotificarNCG365FFMM" -> ResponseEntity.ok(notificarNCG365FFMMService.execute(params).toString());
            case "cl.bcs.normativa.circulares.NotificarTodosCircular1985" -> ResponseEntity.ok(notificarTodosCircular1985Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.ProcesarCuadraturaCustodia" -> ResponseEntity.ok(procesarCuadraturaCustodiaService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReeprocesarEventoCircular1985" -> ResponseEntity.ok(reeprocesarEventoCircular1985Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.AgregarProgramaRepoSicam" -> ResponseEntity.ok(agregarProgramaRepoSicamService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.ConsultarEditables" -> ResponseEntity.ok(consultarEditablesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.EnviarTransaccion" -> ResponseEntity.ok(enviarTransaccionService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.GenerarRepoSicamMasivo" -> ResponseEntity.ok(generarRepoSicamMasivoService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes2p1.AutorizacionEliminarREPO2P1" -> ResponseEntity.ok(autorizacionEliminarREPO2P1Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes2p1.AutorizacionModificarREPO2P1" -> ResponseEntity.ok(autorizacionModificarREPO2P1Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes2p1.ConsultarReportes" -> ResponseEntity.ok(reportes2p1ConsultarReportesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes2p1.EliminarReportes" -> ResponseEntity.ok(reportes2p1EliminarReportesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes2p1.IngresarReportes" -> ResponseEntity.ok(reportes2p1IngresarReportesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes2p1.ModificarReportes" -> ResponseEntity.ok(reportes2p1ModificarReportesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.AutorizacionEliminarREPO2P2" -> ResponseEntity.ok(autorizacionEliminarREPO2P2Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.AutorizacionModificarREPO2P2" -> ResponseEntity.ok(autorizacionModificarREPO2P2Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.ConsultarReportes" -> ResponseEntity.ok(reportes2p2ConsultarReportesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.EliminarReportes" -> ResponseEntity.ok(reportes2p2EliminarReportesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.IngresarReportes" -> ResponseEntity.ok(reportes2p2IngresarReportesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.ModificarReportes" -> ResponseEntity.ok(reportes2p2ModificarReportesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes3p1.AutorizacionEliminarREPO3P1" -> ResponseEntity.ok(autorizacionEliminarREPO3P1Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes3p1.AutorizacionModificarREPO3P1" -> ResponseEntity.ok(autorizacionModificarREPO3P1Service.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes3p1.ConsultarReportes" -> ResponseEntity.ok(reportes3p1ConsultarReportesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes3p1.EliminarReportes" -> ResponseEntity.ok(reportes3p1EliminarReportesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes3p1.IngresarReportes" -> ResponseEntity.ok(reportes3p1IngresarReportesService.execute(params).toString());
            case "cl.bcs.normativa.circulares.ReportesBCC.Reportes3p1.ModificarReportes" -> ResponseEntity.ok(reportes3p1ModificarReportesService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.CargaCertificadosTributariosOffline" -> ResponseEntity.ok(cargaCertificadosTributariosOfflineService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.ConsultarCertificadoOffline" -> ResponseEntity.ok(consultarCertificadoOfflineService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.CrearCalendarioPago" -> ResponseEntity.ok(crearCalendarioPagoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.CrearGarantia" -> ResponseEntity.ok(crearGarantiaService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.CrearPagosPeriodo" -> ResponseEntity.ok(crearPagosPeriodoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.CrearTipoActivoGarantia" -> ResponseEntity.ok(crearTipoActivoGarantiaService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.EliminarCalendarioPago" -> ResponseEntity.ok(eliminarCalendarioPagoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.EliminarGarantia" -> ResponseEntity.ok(eliminarGarantiaService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.EliminarPagosPeriodo" -> ResponseEntity.ok(eliminarPagosPeriodoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.EliminarTipoActivoGarantia" -> ResponseEntity.ok(eliminarTipoActivoGarantiaService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.GrabarDetalle" -> ResponseEntity.ok(grabarDetalleService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.ModificarCalendarioPago" -> ResponseEntity.ok(modificarCalendarioPagoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.ModificarGarantia" -> ResponseEntity.ok(modificarGarantiaService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.ModificarPagosPeriodo" -> ResponseEntity.ok(modificarPagosPeriodoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.ModificarTipoActivoGarantia" -> ResponseEntity.ok(modificarTipoActivoGarantiaService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1.RectificarRegistro" -> ResponseEntity.ok(rectificarRegistroService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1871.AjustarRescatesDelPeriodo" -> ResponseEntity.ok(ajustarRescatesDelPeriodoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1871.ReajustarValorIPC" -> ResponseEntity.ok(dj1871ReajustarValorIPCService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1874.ValorizarDeclaracion" -> ResponseEntity.ok(valorizarDeclaracionService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1889.ReajustarValorIPC" -> ResponseEntity.ok(dj1889ReajustarValorIPCService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1889.ReporteCertificado9" -> ResponseEntity.ok(reporteCertificado9Service.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1894.ReporteCertificado21" -> ResponseEntity.ok(reporteCertificado21Service.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1899.ActualizarDetalles" -> ResponseEntity.ok(dj1899ActualizarDetallesService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1899.ReajustarValorIPC" -> ResponseEntity.ok(dj1899ReajustarValorIPCService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1899.ReporteCertificado24" -> ResponseEntity.ok(reporteCertificado24Service.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1922.ActualizarDetalles" -> ResponseEntity.ok(dj1922ActualizarDetallesService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1922.ReporteCertificado43" -> ResponseEntity.ok(reporteCertificado43Service.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1944.ReporteCertificado17" -> ResponseEntity.ok(reporteCertificado17Service.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1949.ActualizarDetalles" -> ResponseEntity.ok(dj1949ActualizarDetallesService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ1949.ReporteCertificado71" -> ResponseEntity.ok(reporteCertificado71Service.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.DJ999.GenerarInformeSaldosCuentaCambio" -> ResponseEntity.ok(generarInformeSaldosCuentaCambioService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.GenerarCertificado17Masivo" -> ResponseEntity.ok(generarCertificado17MasivoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.GenerarCertificado21Masivo" -> ResponseEntity.ok(generarCertificado21MasivoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.GenerarCertificado24Masivo" -> ResponseEntity.ok(generarCertificado24MasivoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.GenerarCertificado36Masivo" -> ResponseEntity.ok(generarCertificado36MasivoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.GenerarCertificado43Masivo" -> ResponseEntity.ok(generarCertificado43MasivoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.GenerarCertificado71Masivo" -> ResponseEntity.ok(generarCertificado71MasivoService.execute(params).toString());
            case "cl.bcs.normativa.declaraciones.GenerarCertificado9Masivo" -> ResponseEntity.ok(generarCertificado9MasivoService.execute(params).toString());
            case "cl.bcs.normativa.fatca.GenerarDatosFatca" -> ResponseEntity.ok(generarDatosFatcaService.execute(params).toString());
            case "cl.bcs.normativa.ils.CalcularObligacionesDerechosMonedaExtranjera" -> ResponseEntity.ok(calcularObligacionesDerechosMonedaExtranjeraService.execute(params).toString());
            case "cl.bcs.normativa.ils.ConsultarSaldosCuentasContables" -> ResponseEntity.ok(consultarSaldosCuentasContablesService.execute(params).toString());
            case "cl.bcs.normativa.ils.DetalleCoberturaILS" -> ResponseEntity.ok(detalleCoberturaILSService.execute(params).toString());
            case "cl.bcs.normativa.ils.GeneraReporteILS" -> ResponseEntity.ok(generaReporteILSService.execute(params).toString());
            case "cl.bcs.normativa.ils.HomologarCuentasContables" -> ResponseEntity.ok(homologarCuentasContablesService.execute(params).toString());
            case "cl.bcs.normativa.ils.ItemConfiguracionMixto" -> ResponseEntity.ok(itemConfiguracionMixtoService.execute(params).toString());
            case "cl.bcs.normativa.ils.ObtenerSaldoCuentaContable" -> ResponseEntity.ok(obtenerSaldoCuentaContableService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.CambiarEstadoDJCargar" -> ResponseEntity.ok(cambiarEstadoDJCargarService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.CambiarEstadoDJCertificacion" -> ResponseEntity.ok(cambiarEstadoDJCertificacionService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.CambiarEstadoDJEstadoCertificado" -> ResponseEntity.ok(cambiarEstadoDJEstadoCertificadoService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.CambiarEstadoDJPreDeclaracion" -> ResponseEntity.ok(cambiarEstadoDJPreDeclaracionService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.CambiarEstadoDJValidacion" -> ResponseEntity.ok(cambiarEstadoDJValidacionService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.CargaExtornosInterfaz" -> ResponseEntity.ok(cargaExtornosInterfazService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.CargaPagoOperacionesInterfaz" -> ResponseEntity.ok(cargaPagoOperacionesInterfazService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazArchivoCM" -> ResponseEntity.ok(generarInterfazArchivoCMService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazAsignaciones" -> ResponseEntity.ok(generarInterfazAsignacionesService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazC08CarteraIntermediada" -> ResponseEntity.ok(generarInterfazC08CarteraIntermediadaService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazC08CarteraNormal" -> ResponseEntity.ok(generarInterfazC08CarteraNormalService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazC08CarteraPacteada" -> ResponseEntity.ok(generarInterfazC08CarteraPacteadaService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazC08InformacionContable" -> ResponseEntity.ok(generarInterfazC08InformacionContableService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraIntermediada" -> ResponseEntity.ok(generarInterfazC40CarteraIntermediadaService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraNormal" -> ResponseEntity.ok(generarInterfazC40CarteraNormalService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraPacteada" -> ResponseEntity.ok(generarInterfazC40CarteraPacteadaService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazC40INT" -> ResponseEntity.ok(generarInterfazC40INTService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazC40INTDET" -> ResponseEntity.ok(generarInterfazC40INTDETService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazC40InformacionContable" -> ResponseEntity.ok(generarInterfazC40InformacionContableService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazCliente" -> ResponseEntity.ok(generarInterfazClienteService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazClientesEnLinea" -> ResponseEntity.ok(generarInterfazClientesEnLineaService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazClientesFinales" -> ResponseEntity.ok(generarInterfazClientesFinalesService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazDetalleOperacionesPolizas" -> ResponseEntity.ok(generarInterfazDetalleOperacionesPolizasService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazICU" -> ResponseEntity.ok(generarInterfazICUService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazInstrumentosFinancieros" -> ResponseEntity.ok(generarInterfazInstrumentosFinancierosService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazLavadodeDinero" -> ResponseEntity.ok(generarInterfazLavadodeDineroService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazMontosIntermediados" -> ResponseEntity.ok(generarInterfazMontosIntermediadosService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazOrdenes" -> ResponseEntity.ok(generarInterfazOrdenesService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazOrdenesEnLinea" -> ResponseEntity.ok(generarInterfazOrdenesEnLineaService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazP40Aseguradora" -> ResponseEntity.ok(generarInterfazP40AseguradoraService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazP40Filial" -> ResponseEntity.ok(generarInterfazP40FilialService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazPolizas" -> ResponseEntity.ok(generarInterfazPolizasService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.GenerarInterfazSIG" -> ResponseEntity.ok(generarInterfazSIGService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.P40AgregarModificarFiliales" -> ResponseEntity.ok(p40AgregarModificarFilialesService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.P40EliminarFiliales" -> ResponseEntity.ok(p40EliminarFilialesService.execute(params).toString());
            case "cl.bcs.normativa.interfaces.ValidaGenerarInterfaz" -> ResponseEntity.ok(validaGenerarInterfazService.execute(params).toString());
                default -> throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "JavaCall class '" + fqn + "' not yet migrated; TomEE fallback active");
            };
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (UnsupportedOperationException uoe) {
            log.warn("JavaCall stub not implemented: {}", fqn);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "JavaCall class '" + fqn + "' stub not yet fully implemented: " + uoe.getMessage());
        } catch (Exception e) {
            log.error("JavaCall error for class {}: {}", fqn, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "JavaCall execution error: " + e.getMessage());
        }
    }
}
