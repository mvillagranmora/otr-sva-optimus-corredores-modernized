package cl.bcs.normativa.port.in;

/**
 * Caso de uso: obtener parametros de configuracion por empresa.
 */
public interface FindConfiguracionUseCase {

    /**
     * Busca valor de parametro de configuracion.
     * @throws IllegalArgumentException si no existe.
     */
    String findValor(long idEmpresa, String codigoParametro);
}
