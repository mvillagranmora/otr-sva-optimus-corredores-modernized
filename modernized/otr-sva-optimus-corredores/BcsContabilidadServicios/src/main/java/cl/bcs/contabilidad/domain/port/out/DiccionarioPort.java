package cl.bcs.contabilidad.domain.port.out;

import java.util.Map;

/**
 * Message dictionary of the platform: text of a message code with its substitution values.
 * Legacy {@code cl.bcs.plataforma.pla.utils.Diccionario.getMensaje(con, idEmpresa, codigo, valores)}.
 */
public interface DiccionarioPort {

    String mensaje(Long idEmpresa, String codigo, Map<String, String> valores);
}
