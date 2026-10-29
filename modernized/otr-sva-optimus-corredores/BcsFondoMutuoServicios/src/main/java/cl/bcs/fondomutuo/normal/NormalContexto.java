package cl.bcs.fondomutuo.normal;

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
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones.ResultadoCalcularComision;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.normal.BloqueoSolicitudPort.Bloqueo;
import cl.bcs.fondomutuo.normal.NormalDatos.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.normal.NormalDatos.IndicadoresPortafolio;
import cl.bcs.fondomutuo.normal.NormalDatos.PortafolioAPV;
import cl.bcs.fondomutuo.normal.NormalDatos.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.normal.NormalDatos.SalidaTraspasoTotal;
import cl.bcs.fondomutuo.normal.NormalDatos.TraspasoAPV;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService;

/**
 * Everything one request of {@code CrearSolicitudAporteRescateNormal} reads and builds between its phases: the local variables of the legacy method
 * {@code logica} (its maps, lists and counters), in one object that lives for the call only (no state in the service, DEV-F2).
 */
final class NormalContexto {

    NormalContrato.Entrada entrada;
    NormalContrato.Salida out = new NormalContrato.Salida();

    String fechaSistema;
    Long idPaisEmpresa;
    String calcularComisionCertificacion;
    String respetaValutasCargaMasiva = NormalConstantes.NO;
    String rescateGeneraSpotDolar;
    String perfilFondoMutuo;
    List<Bloqueo> solicitudesBloqueadas = new ArrayList<>();

    HashMap<String, SerieFondoSolicitudes> hseries;
    HashMap<Long, ClientePortafolioSolicitudes> hclientes;
    HashMap<String, ComisionNnDatos.TipoMovimientoFondoMutuo> htipoMovimiento;
    HashMap<String, RegimenSerieDeFondo> hseriesRegimen;
    HashMap<Long, IndicadoresPortafolio> hindicadoresPortafolio;
    HashMap<String, ComisionNnDatos.Moneda> hmonedasFondo;
    HashMap<Long, PortafolioAPV> hportafolioAPV;
    HashMap<String, ConfiguracionPropositoPortafolio> hconfRegimenPortafolio;
    HashMap<String, SalidaTraspasoTotal> hcustodiasDC;
    Map<Long, TraspasoAPV> hTraspasoAPV;

    HashMap<Long, ResultadoFecha> hfechas;
    HashMap<Long, CalcularMontoNumeroCuota.Resultado> valoresCuota;
    HashMap<Long, ParidadContrato.CalculoSalida> hashMontoMonedaPago;
    HashMap<Long, ResultadoCalcularComision> hcomisiones;

    List<Long> secuencias;
    List<Long> folioContratoSolicitudes;
    Timestamp fechaHoraUTC;

    List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<>();
    List<ResultadoSolicitud> resultadoTemp = new ArrayList<>();
    HashMap<Long, ResultadoSolicitud> hSolicitudSalida;
    HashMap<String, BigDecimal> hashMontosPortafolioEmisor = new HashMap<>();

    java.sql.Date fechaMaximaCertificacion;
    ProcesosExternos.EntradaTesoreria entradaTesoreria = new ProcesosExternos.EntradaTesoreria();
    ProcesosExternos.EntradaSpot entradaSpot = new ProcesosExternos.EntradaSpot();
    List<SolicitudAporteRescate> solicitudesInsertar = new ArrayList<>();

    ServicioCumpleRequisitosService.Entrada entradaCumpleRequisitos = new ServicioCumpleRequisitosService.Entrada();
    cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService.Entrada entradaValidaPatrimonioEmisor =
            new cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService.Entrada();
}
