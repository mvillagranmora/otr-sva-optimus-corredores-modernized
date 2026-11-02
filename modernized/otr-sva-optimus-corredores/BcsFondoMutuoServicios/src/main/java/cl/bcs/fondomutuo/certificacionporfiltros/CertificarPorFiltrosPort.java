package cl.bcs.fondomutuo.certificacionporfiltros;

/**
 * Legacy {@code CertificarPorFiltrosSolicitudAporteRescate.logica} without the connection (it is the one of the unit of work). In-process only. Inherited from
 * Certificar: its closed gates answer {@code ServicioNoServidoException} through this port, and the commit semantics are those of the caller (mass job).
 */
public interface CertificarPorFiltrosPort {

    CertificarPorFiltrosContrato.Salida logica(CertificarPorFiltrosContrato.Entrada entrada) throws Exception;
}
