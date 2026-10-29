package cl.bcs.normativa.procesos;

/**
 * Define forma de los procesos.
 *
 * @author fpino@email.com
 *
 */
public interface Proceso {

    /**
     * Punto de entrada del proceso.
     *
     * @throws ProcesoException
     *             En caso de error de proceso.
     */
    void procesar() throws ProcesoException;

}
