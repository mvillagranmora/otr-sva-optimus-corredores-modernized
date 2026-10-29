package cl.bcs.normativa.port.out;

import cl.bcs.normativa.domain.Esquema;

import java.util.Optional;

/**
 * Puerto de salida para acceso a datos de Esquema.
 */
public interface EsquemaPort {

    /** Busca esquema por nombre y version. */
    Optional<Esquema> buscarPorClave(String nombre, String version);
}
