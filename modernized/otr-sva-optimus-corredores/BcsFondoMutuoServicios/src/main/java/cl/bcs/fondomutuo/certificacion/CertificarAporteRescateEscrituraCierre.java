package cl.bcs.fondomutuo.certificacion;

import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;

/** Write port of FM-CER-61: the mass UPDATE of the pending and warned solicitudes to certified (one JDBC batch). Joins the ambient transaction; never commits or rolls back. */
public interface CertificarAporteRescateEscrituraCierre {

    void actualizarSolicitudes(List<SolicitudProceso> solicitudes);
}
