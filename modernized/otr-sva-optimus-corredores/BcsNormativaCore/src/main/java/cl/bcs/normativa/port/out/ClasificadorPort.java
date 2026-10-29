package cl.bcs.normativa.port.out;

import cl.bcs.normativa.domain.CampoClasificador;
import cl.bcs.normativa.domain.Clasificador;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para acceso a datos de Clasificador.
 */
public interface ClasificadorPort {

    /** Busca clasificador por esquema/version/tipo. */
    Optional<Clasificador> buscarPorClave(String nombreEsquema, String versionEsquema, String tipo);

    /** Busca campos de un clasificador para un documento y tipo dado. */
    List<CampoClasificador> buscarCamposPorDocumentoMasTipo(
            String nombreEsquema, String versionEsquema, long idDocumento, String tipoClasificador);
}
