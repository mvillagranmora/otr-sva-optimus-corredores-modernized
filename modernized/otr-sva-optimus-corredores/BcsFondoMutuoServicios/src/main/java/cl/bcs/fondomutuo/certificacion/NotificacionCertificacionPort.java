package cl.bcs.fondomutuo.certificacion;

/**
 * Port of the asynchronous notification of the certification (FM-CER-62, legacy {@code NotificarEstadoOrdenFondoMutuo.logicaAsync}): the ids of the updated solicitudes go to the batch server over HTTP,
 * outside the transaction, and the answer is never waited for. The legacy implementation reads {@code PLA.CallBatchURL} and {@code PLA.CallBatchToken} BEFORE it sends anything and raises
 * {@code Exception("Parametro ... no encontrado")} when one is missing: that read belongs to the ADAPTER, not to the facade. Behind the gate G8 ({@code Compuertas.g8Notificaciones()}): there is no
 * production implementation yet, so a bean of it must not be wired by accident.
 */
public interface NotificacionCertificacionPort {

    void notificar(CertificarAporteRescateContrato.NotificarEstadoOrdenEntrada entrada) throws Exception;
}
