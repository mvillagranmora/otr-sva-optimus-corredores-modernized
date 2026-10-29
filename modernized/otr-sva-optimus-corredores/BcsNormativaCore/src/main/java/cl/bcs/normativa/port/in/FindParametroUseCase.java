package cl.bcs.normativa.port.in;

/**
 * Caso de uso: obtener parametros de un esquema normativo.
 */
public interface FindParametroUseCase {

    /**
     * Busca valor de parametro de un esquema.
     * @throws IllegalArgumentException si no existe.
     */
    String findValor(String nombreEsquema, String versionEsquema, String codigo);
}
