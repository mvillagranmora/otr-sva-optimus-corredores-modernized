package cl.bcs.normativa.port.in;

import cl.bcs.normativa.domain.Validacion;

import java.util.List;

/**
 * Caso de uso: obtener reglas de validacion normativas.
 */
public interface FindValidacionUseCase {

    /**
     * Busca validaciones activas de un esquema.
     */
    List<Validacion> findActivasPorEsquema(String nombreEsquema, String versionEsquema);
}
