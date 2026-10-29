package cl.bcs.normativa.port.out;

import java.util.Optional;

/**
 * Puerto de salida para acceso a datos de Configuracion.
 */
public interface ConfiguracionPort {

    /** Busca valor de parametro de configuracion por empresa y codigo. */
    Optional<String> buscarValor(long idEmpresa, String codigoParametro);
}
