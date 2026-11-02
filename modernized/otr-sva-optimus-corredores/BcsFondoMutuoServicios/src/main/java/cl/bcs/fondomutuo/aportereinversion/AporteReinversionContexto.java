package cl.bcs.fondomutuo.aportereinversion;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.fondomutuo.aportereinversion.AporteReinversionContrato.ConsultaRegimenReinversion;
import cl.bcs.fondomutuo.aportereinversion.AporteReinversionDatos.IndicadoresPortafolioFondoMutuo;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos;
import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.normal.NormalDatos.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.normal.NormalDatos.IndicadoresPortafolio;
import cl.bcs.fondomutuo.normal.NormalDatos.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService;

/**
 * Everything one request of {@code CrearSolicitudAporteReinversion} reads and builds between its phases: the local variables of the legacy methods {@code logica},
 * {@code procesarEnLotes} and {@code procesarSolicitudes}, in one object that lives for the call only (no state in the service). The lot-level fields are reset by
 * {@link #nuevoLote()} so a lot starts as the legacy locals did.
 */
final class AporteReinversionContexto {

    AporteReinversionContrato.Entrada entrada;
    final AporteReinversionContrato.Salida out = new AporteReinversionContrato.Salida();

    // the call
    String aporteGeneraCompraSpotDolar;
    String idMonedaLocal;
    String fechaSistema;
    String respetaValutasCargaMasiva = AporteReinversionConstantes.NO;
    Long idPaisEmpresa;
    Timestamp fechaHoraUTC;

    // the lot
    String perfilFondoMutuo;
    HashMap<String, SerieFondoSolicitudes> hseries;
    HashMap<String, SerieFondoSolicitudes> hseriesOrigenDestino;
    HashMap<Long, ClientePortafolioSolicitudes> hclientes;
    HashMap<String, ComisionNnDatos.TipoMovimientoFondoMutuo> htipoMovimiento;
    HashMap<String, RegimenSerieDeFondo> hseriesRegimen;
    HashMap<Long, IndicadoresPortafolio> hindicadoresPortafolio;
    HashMap<Long, IndicadoresPortafolioFondoMutuo> hindicadoresPortafolioFFMM;
    HashMap<String, ComisionNnDatos.Moneda> hmonedasFondo;
    HashMap<String, ConfiguracionPropositoPortafolio> hconfRegimenPortafolio;
    HashMap<Long, ParidadContrato.CalculoSalida> hashMontoTotalInversionMonedaSistema;
    HashMap<Long, ResultadoFecha> hfechas;
    HashMap<Long, CalcularMontoNumeroCuota.Resultado> valoresCuota;
    HashMap<Long, AporteReinversionContrato.InSolicitudesAporte> listaEntrada;
    List<String> regimen = new ArrayList<>();
    List<ConsultaRegimenReinversion> regimenSolicitudReinversion;
    List<Long> secuencias;
    List<Long> folioContratoSolicitudes;
    List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<>();
    List<ResultadoSolicitud> resultadoTemp = new ArrayList<>();
    HashMap<Long, ResultadoSolicitud> hSolicitudSalida;
    HashMap<String, BigDecimal> hashMontosPortafolioEmisor = new HashMap<>();
    Date fechaMaximaCertificacion;
    ProcesosExternos.EntradaTesoreria entradaTesoreria = new ProcesosExternos.EntradaTesoreria();
    List<SolicitudAporteRescate> solicitudesInsertar = new ArrayList<>();
    ServicioCumpleRequisitosService.Entrada entradaCumpleRequisitos = new ServicioCumpleRequisitosService.Entrada();
    ValidaPatrimonioEmisorService.Entrada entradaValidaPatrimonioEmisor = new ValidaPatrimonioEmisorService.Entrada();

    /** The locals of {@code procesarSolicitudes} start empty on every lot. */
    void nuevoLote() {
        regimen = new ArrayList<>();
        solicitudesProcesar = new ArrayList<>();
        resultadoTemp = new ArrayList<>();
        hashMontosPortafolioEmisor = new HashMap<>();
        entradaTesoreria = new ProcesosExternos.EntradaTesoreria();
        solicitudesInsertar = new ArrayList<>();
        entradaCumpleRequisitos = new ServicioCumpleRequisitosService.Entrada();
        entradaValidaPatrimonioEmisor = new ValidaPatrimonioEmisorService.Entrada();
    }
}
