package cl.bcs.fondomutuo.certificacion;

import java.util.List;

/**
 * Port of the OpenWorkPlace notification of the certification (FM-CER-65, legacy {@code NotificarEstadoOrdenesOWP.go}): one call per request with the orders of every solicitud of channel OWP,
 * outside the transaction. Typed, and NOT the {@code port.out.http.NotificacionOwpPort} of Normal (that one guards another caller with the gate G4). Behind the gate G8
 * ({@code Compuertas.g8Notificaciones()}): no production implementation yet.
 */
public interface NotificacionOrdenesOwpPort {

    void notificar(List<CertificarAporteRescateContrato.OrdenOwp> ordenes) throws Exception;
}
