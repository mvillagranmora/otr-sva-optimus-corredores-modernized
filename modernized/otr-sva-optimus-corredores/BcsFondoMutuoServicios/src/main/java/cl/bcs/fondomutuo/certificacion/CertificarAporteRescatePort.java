package cl.bcs.fondomutuo.certificacion;

/** Legacy {@code CertificarSolicitudAporteRescate.logica(con, entrada)} without the connection. */
public interface CertificarAporteRescatePort {

    CertificarAporteRescateContrato.Salida logica(CertificarAporteRescateContrato.Entrada entrada) throws Exception;
}
