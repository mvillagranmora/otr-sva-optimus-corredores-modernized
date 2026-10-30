package cl.bcs.fondomutuo.certificacion;

import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ComprobanteNoLiquidado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Estado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.IndicadorPortafolio;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ParametroOperaCSN;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Persona;
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
}
