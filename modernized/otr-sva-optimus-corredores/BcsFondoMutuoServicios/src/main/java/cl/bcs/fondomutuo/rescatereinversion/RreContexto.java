package cl.bcs.fondomutuo.rescatereinversion;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos;
import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.Entrada;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesRescate;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.Salida;

/**
 * The local variables of legacy {@code CrearSolicitudRescateReinversion.logica} (its lists, maps and counters) in one object that lives for one call
 * (no state in the service, DEV-F2). Names are the legacy ones.
 */
final class RreContexto {

    Entrada entrada;
    Salida out = new Salida();

    String fechaSistema;
    String rescateGeneraVentaSpotDolar;
    String respetaValutasCargaMasiva = RreConstantes.NO;
    Long idPaisEmpresa;
    String perfilFondoMutuo;

    int countTransaccionesBatch = 0;
    List<String> series = new ArrayList<>();
    List<Long> portafolios = new ArrayList<>();
    List<String> regimen = new ArrayList<>();

    HashMap<String, SerieFondoSolicitudes> hseries;
    HashMap<Long, ClientePortafolioSolicitudes> hclientes;
    HashMap<String, ComisionNnDatos.TipoMovimientoFondoMutuo> htipoMovimiento;
    HashMap<String, RreDatos.RegimenSerieDeFondo> hseriesRegimen;
    HashMap<Long, InSolicitudesRescate> hrescates;
    HashMap<Long, InSolicitudesAporte> haportes;
    HashMap<String, ComisionNnDatos.Moneda> hmonedasFondo;
    HashMap<String, RreDatos.ConfiguracionRegimenFondoMutuo> hregimenConfiguraciones;
    HashMap<String, RreDatos.ConfiguracionPropositoPortafolio> hconfRegimenPortafolio;
    List<RreDatos.RegimenMandato> regimenMandato;
    HashMap<Long, String> hseriesMonedasDiferentes = new HashMap<>();

    List<BloqueoReinversionPort.Bloqueo> bloquearReinversion = new ArrayList<>();
    /** Modern bookkeeping only: the CREATE + CALL of the lock succeeded (used to release it before a 503, never by a legacy branch). */
    boolean bloqueoCreado;

    HashMap<Long, ResultadoFecha> hfechas;
    HashMap<Long, ResultadoFecha> hfechasAporte;
    HashMap<Long, Object> rescatesUnicos = new HashMap<>();
    String pIdSerieFondo = "";
    HashMap<Long, CalcularMontoNumeroCuota.Resultado> valoresCuota;

    List<Long> secuencias;
    List<Long> secuenciasIdAgrupador;
    List<Long> folioContratoSolicitudes;
    Timestamp fechaHoraUTC;
    HashMap<RreReglas.KeyAporteMonto, BigDecimal> aportesAgrupadosPorSerieOperacion = new HashMap<>();
    HashMap<String, BigDecimal> hashMontosPortafolioEmisor = new HashMap<>();

    ServicioCumpleRequisitosService.Entrada entradaCumpleRequisitos = new ServicioCumpleRequisitosService.Entrada();
    ValidaPatrimonioEmisorService.Entrada entradaValidaPatrimonioEmisor = new ValidaPatrimonioEmisorService.Entrada();
    List<ResultadoSolicitud> resultadoTemp = new ArrayList<>();
    List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<>();
    HashMap<Long, Object> rescatesUnicosV2 = new HashMap<>();
    HashMap<Long, ResultadoSolicitud> hSolicitudSalida;
    List<SolicitudAporteRescate> solicitudesProcesarFinal = new ArrayList<>();
    Map<Long, String> solicitudesBatches;
}
