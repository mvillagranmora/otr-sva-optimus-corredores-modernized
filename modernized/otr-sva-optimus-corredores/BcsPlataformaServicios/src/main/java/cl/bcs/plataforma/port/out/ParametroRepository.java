package cl.bcs.plataforma.port.out;

import java.util.Optional;

/**
 * Output port: retrieve platform configuration parameters.
 * Backed by PLA.Parametro table in SAP HANA.
 */
public interface ParametroRepository {

    /**
     * Retrieve a parameter value.
     *
     * @param idEmpresa       company ID
     * @param moduloParametro parameter module code (e.g. "PLATAFORMA")
     * @param nombreParametro parameter name (e.g. "PLA.FGL")
     * @return parameter value, or empty if not found
     */
    Optional<String> findValor(Long idEmpresa, String moduloParametro, String nombreParametro);
}
