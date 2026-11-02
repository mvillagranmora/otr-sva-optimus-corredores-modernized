package cl.bcs.fondomutuo.aportereinversion;

/**
 * Legacy {@code new CrearSolicitudAporteReinversion().logica(con, entrada)} (Certificar, the Manejador and ProcesarCarga call it in process). The connection is NOT a
 * parameter: the unit of work of the call is the ambient transaction. The input is the legacy typed class and the class MUTATES it as the legacy did (the callers read it
 * again). It does not catch: exceptions and {@code ServicioNoServidoException} (503, gates G1, G2, G3, G-CUS) leave as in the legacy / plan 6.3.
 */
public interface AporteReinversionPort {

    AporteReinversionContrato.Salida logica(AporteReinversionContrato.Entrada entrada) throws Exception;
}
