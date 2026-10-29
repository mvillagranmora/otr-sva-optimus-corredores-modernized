package cl.bcs.fondomutuo.port.out.kit;

import java.util.Map;

/**
 * Kit port K: legacy {@code Diccionario.getMensaje(con, idEmpresa, codigo, valores)} (text of a message code with its {@code ::Key::} placeholders
 * replaced). Rules kept: unknown code OR {@code null} value map gives {@code "<codigo> sin traduccion"}; a {@code null} value is an empty text.
 * The legacy dictionary was a JVM-wide static (it survived the request and the company); here it is read once per UNIT OF WORK (DEV-F2).
 */
public interface DiccionarioMensajesPort {

    String mensaje(Long idEmpresa, String codigo, Map<String, String> valores);

    /** Legacy fluent form {@code getMensaje(con, idEmpresa, codigo).add(..).msg()} with no values added. */
    default String mensaje(Long idEmpresa, String codigo) {
        return mensaje(idEmpresa, codigo, Map.of());
    }
}
