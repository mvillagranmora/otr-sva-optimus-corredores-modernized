package cl.bcs.normativa.port.out;

import cl.bcs.normativa.domain.Campo;

import java.util.List;

/**
 * Puerto de salida para acceso a datos de Campo.
 */
public interface CampoPort {

    /** Busca campos de un esquema ordenados por Orden ASC. */
    List<Campo> buscarPorEsquema(String nombreEsquema, String versionEsquema);
}
