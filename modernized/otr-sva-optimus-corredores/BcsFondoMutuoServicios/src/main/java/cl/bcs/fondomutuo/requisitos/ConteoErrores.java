package cl.bcs.fondomutuo.requisitos;

import java.util.List;

import cl.bcs.fondomutuo.kit.MensajeSalida;

/**
 * Legacy {@code MensajeSalida.getNumeroErrores}: counts the messages whose {@code tipo} IS (reference equality, {@code ==}) the constant
 * {@link MensajeSalida#MENSAJE_ERROR} (D-REQ-11). In-process messages built with the constant count; a message whose type came from a parsed JSON
 * string (not interned) never counts. Kept bug-for-bug: do not "fix" to {@code equals}.
 */
public final class ConteoErrores {

    private ConteoErrores() {
    }

    @SuppressWarnings("StringEquality")
    public static long errores(List<MensajeSalida> mensajes) {
        int c = 0;
        for (MensajeSalida s : mensajes) {
            if (s.tipo == MensajeSalida.MENSAJE_ERROR) {
                c++;
            }
        }
        return c;
    }
}
