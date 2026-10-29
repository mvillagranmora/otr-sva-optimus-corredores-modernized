package cl.bcs.apv.application.comun;

import java.util.List;
import java.util.Map;

import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;

/**
 * Messages of an outcome (legacy {@code PlataformaUtils.MensajeSalida}): the tipo constants, the constructors the legacy used (literal text, or text
 * resolved by the dictionary) and the error counter.
 *
 * <p>{@link #numeroErrores} counts by {@code equals}. The legacy {@code getNumeroErrores} compared {@code tipo == MENSAJE_ERROR} (reference): every
 * message the APV code builds uses the constant, and the messages that come back from other services in process carried the same interned literal, so the
 * two agree; a message whose tipo came from JSON text is counted here (the oracle interns collaborator answers for the same reason).
 */
public final class Mensajes {

    public static final String MENSAJE_OK = "OK";
    public static final String MENSAJE_ERROR = "ERROR";
    public static final String MENSAJE_ADVERTENCIA = "ADVERTENCIA";
    public static final String MENSAJE_AUTORIZACION = "AUTORIZACION";
    public static final String MENSAJE_INFORMACION = "INFORMACION";
    public static final String MENSAJE_RECHAZO = "RECHAZO";

    private Mensajes() {
    }

    /** Legacy {@code new MensajeSalida(id, tipo, codigo, descripcion)}. */
    public static Mensaje de(Long id, String tipo, String codigo, String descripcion) {
        return new Mensaje(id, tipo, codigo, descripcion);
    }

    /** Legacy {@code new MensajeSalida(id, tipo, codigo, valores, con, idEmpresa)}: the text is resolved by the dictionary. */
    public static Mensaje conDiccionario(DiccionarioMensajes diccionario, Long idEmpresa, Long id, String tipo, String codigo, Map<String, String> valores) {
        return new Mensaje(id, tipo, codigo, diccionario.mensaje(idEmpresa, codigo, valores));
    }

    /** Legacy {@code MensajeSalida.getNumeroErrores}. */
    public static long numeroErrores(List<Mensaje> mensajes) {
        long c = 0;
        for (Mensaje m : mensajes) {
            if (MENSAJE_ERROR.equals(m.tipo())) {
                c++;
            }
        }
        return c;
    }
}
