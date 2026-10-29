package cl.bcs.tesoreria.port.out;

import java.util.Map;

/** INTENDED API: legacy {@code Diccionario.getMensaje(con, idEmpresa, codigo, valores)}: message text of a code with its {Key} placeholders replaced; "CODE sin traduccion" when unknown. */
public interface DiccionarioMensajes {
    String mensaje(Long idEmpresa, String codigo, Map<String, String> valores);
}
