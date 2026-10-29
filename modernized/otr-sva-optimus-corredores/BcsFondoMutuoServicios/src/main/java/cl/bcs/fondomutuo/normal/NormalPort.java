package cl.bcs.fondomutuo.normal;

/**
 * What the Manejador (step 4B) calls: legacy {@code new CrearSolicitudAporteRescateNormal().logica(con, entrada)}. The connection is NOT a parameter:
 * the unit of work of the call is the ambient Spring transaction (every adapter and the anti-duplicate lock use the same session), exactly as the legacy
 * shared the one {@code Connection}. The entry is the legacy typed input ({@link NormalContrato.Entrada}); the exit is the legacy output
 * ({@link NormalContrato.Salida}), never null. It does not catch: exceptions and {@code ServicioNoServidoException} (503, gates G1..G5) leave as in the
 * legacy / plan 6.3. The implementation is {@code CrearSolicitudAporteRescateNormalService} (singleton bean without request state).
 * The JSON entry (JavaCall FQN {@code cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal}) is {@code NormalJavaCallHandler}.
 */
public interface NormalPort {

    NormalContrato.Salida logica(NormalContrato.Entrada entrada) throws Exception;
}
