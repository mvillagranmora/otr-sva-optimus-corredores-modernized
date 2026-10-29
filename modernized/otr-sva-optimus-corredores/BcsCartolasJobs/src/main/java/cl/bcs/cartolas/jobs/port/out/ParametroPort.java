package cl.bcs.cartolas.jobs.port.out;

import java.util.Map;

/**
 * Puerto de salida para guardar parámetros del proceso de cartolas.
 *
 * Oracle: cl.bcs.normativa.cartolas.dal.ParametroService
 *
 * B-CAR-04: Guardar parámetros — requiere HANA
 */
public interface ParametroPort {

    /**
     * Guarda los parámetros del proceso en HANA.
     *
     * @param idEmpresa   identificador de la empresa
     * @param jobId       identificador del job
     * @param parametros  mapa de parámetros clave-valor
     */
    void guardar(long idEmpresa, long jobId, Map<String, String> parametros);
}
