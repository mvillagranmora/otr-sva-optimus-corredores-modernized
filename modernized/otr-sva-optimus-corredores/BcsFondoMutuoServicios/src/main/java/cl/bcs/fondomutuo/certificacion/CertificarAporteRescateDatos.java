package cl.bcs.fondomutuo.certificacion;

import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ComprobanteNoLiquidado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Cobro;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Estado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.IndicadorPortafolio;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.OrigenReinversion;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ParametroOperaCSN;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Persona;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ReinversionDestino;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudReinversion;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Traspaso;

/** Read port of the SELECTs of F-A / F-B, verbatim SQL. */
public interface CertificarAporteRescateDatos {

    List<SolicitudProceso> solicitudes(Long idEmpresa, List<Long> idSolicitudes);

    List<Estado> estados();

    List<ComprobanteNoLiquidado> comprobantesNoLiquidados(List<Long> idSolicitudes);

    List<Traspaso> traspasosApv(List<Long> idSolicitudes);

    List<Traspaso> traspasosNoApv(List<Long> idSolicitudes);

    List<Persona> personasPorDocumento(String documentoIdentidad);

    List<IndicadorPortafolio> indicadoresPortafolio(List<Long> idPortafolios);

    List<ParametroOperaCSN> parametrosOperaConSaldoNegativo(List<Long> idPortafolios);

    /** {@code GeneraCargoAbono} of a movement type ({@code "N"} when the type has no row). */
    String generaCargoAbono(String idTipoMovimientoFondoMutuo);

    /** F-G (FM-CER-48): the rows of {@code COM.Cobro} for the IVA. The caller takes the FIRST one: no row is an IndexOutOfBoundsException, a null value a NullPointerException (D-F3B-47). */
    List<Cobro> cobrosIva();

    /** F-I (FM-CER-52): the destinations of the reinversion (with family and currency of the destination fund) of the rescates. */
    List<ReinversionDestino> destinosReinversion(List<Long> idSolicitudes);

    /** F-I (FM-CER-52, 56): the dollar flags of the origin and destination series of the rescates (against {@code PLA.MonedaAdicional} of the company). */
    List<OrigenReinversion> seriesOrigenReinversion(Long idEmpresa, List<Long> idSolicitudes);

    /** F-I (FM-CER-64): the SUM of {@code MontoMonedaSistema} of ALL the rows related to each rescate (D-F3B-49). */
    List<SolicitudReinversion> sumaMontoMonedaSistema(List<Long> idRescatesRelacionados);
}
