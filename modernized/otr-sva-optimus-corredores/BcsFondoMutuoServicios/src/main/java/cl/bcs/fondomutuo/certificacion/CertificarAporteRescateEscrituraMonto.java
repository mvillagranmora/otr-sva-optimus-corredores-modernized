package cl.bcs.fondomutuo.certificacion;

import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudReinversion;

/** Write port of FM-CER-64: the UPDATE of {@code MontoMonedaSistema} of the rescates (one JDBC batch). Joins the ambient transaction; never commits or rolls back. */
public interface CertificarAporteRescateEscrituraMonto {

    void actualizarMontoMonedaSistema(List<SolicitudReinversion> filas);
}
