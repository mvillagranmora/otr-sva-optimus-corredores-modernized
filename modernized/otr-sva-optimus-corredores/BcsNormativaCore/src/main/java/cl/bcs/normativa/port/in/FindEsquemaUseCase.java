package cl.bcs.normativa.port.in;

import cl.bcs.normativa.domain.Esquema;

/**
 * Caso de uso: obtener esquemas normativos con sus campos.
 */
public interface FindEsquemaUseCase {

    /**
     * Busca esquema por nombre y version (incluye lista de campos).
     * @throws IllegalArgumentException si no existe.
     */
    Esquema findByNombreVersion(String nombre, String version);
}
