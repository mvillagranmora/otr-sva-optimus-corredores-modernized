package cl.bcs.fondomutuo.rescatereinversion;

/**
 * Legacy {@code CrearSolicitudRescateReinversion.logica(con, entrada)}. The connection is not a parameter: the unit of work is the ambient Spring
 * transaction (every adapter, the session-local lock table and its two CALL share one session). Callers: the direct JavaCall entry
 * ({@link RescateReinversionJavaCallHandler}) and the Manejador ({@link RescateReinversionDestino}).
 */
public interface RescateReinversionPort {

    RescateReinversionContrato.Salida logica(RescateReinversionContrato.Entrada entrada) throws Exception;
}
