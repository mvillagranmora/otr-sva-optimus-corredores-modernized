package cl.bcs.normativa.port.out;

import cl.bcs.normativa.domain.Validacion;

import java.util.List;

/**
 * Puerto de salida para acceso a datos de Validacion.
 */
public interface ValidacionPort {

    /** Busca validaciones activas de un esquema ordenadas por Codigo ASC. */
    List<Validacion> buscarActivasPorEsquema(String nombreEsquema, String versionEsquema);
}
