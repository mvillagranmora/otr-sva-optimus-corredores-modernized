package cl.bcs.normativa.port.out;

import java.util.Optional;

/**
 * Puerto de salida para acceso a datos de Parametro de esquema.
 */
public interface ParametroPort {

    /** Busca valor de parametro de un esquema por codigo. */
    Optional<String> buscarValor(String nombreEsquema, String versionEsquema, String codigo);
}
