package cl.bcs.normativa.port.out;

import cl.bcs.normativa.domain.CampoCuadratura;
import cl.bcs.normativa.domain.Cuadratura;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para acceso a datos de Cuadratura.
 */
public interface CuadraturaPort {

    /** Busca cuadratura por esquema/version/tipo. */
    Optional<Cuadratura> buscarPorClave(String nombreEsquema, String versionEsquema, String tipo);

    /** Busca campos de una cuadratura por esquema/version/tipoCuadratura. */
    List<CampoCuadratura> buscarCamposPorClave(
            String nombreEsquema, String versionEsquema, String tipoCuadratura);
}
