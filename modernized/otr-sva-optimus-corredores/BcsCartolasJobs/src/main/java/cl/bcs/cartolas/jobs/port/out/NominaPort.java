package cl.bcs.cartolas.jobs.port.out;

/**
 * Puerto de salida para operaciones sobre nóminas de cartolas.
 *
 * Oracle: cl.bcs.normativa.cartolas.dal.NominaService
 *         cl.bcs.normativa.cartolas.entidades.Nomina
 *
 * B-CAR-03: Operaciones de nómina — requieren HANA
 */
public interface NominaPort {

    /**
     * Obtiene el estado de certificación de la nómina activa para la empresa.
     *
     * @param idEmpresa identificador de la empresa
     * @param jobId     identificador del job
     * @return true si la nómina está certificada
     */
    boolean esCertificada(long idEmpresa, long jobId);

    /**
     * Abre la nómina para iniciar el proceso.
     *
     * @param idEmpresa   identificador de la empresa
     * @param jobId       identificador del job
     * @param tipoProceso tipo de proceso
     */
    void abrir(long idEmpresa, long jobId, String tipoProceso);

    /**
     * Cierra la nómina después de que el proceso finaliza correctamente.
     *
     * @param idEmpresa identificador de la empresa
     * @param jobId     identificador del job
     */
    void cerrar(long idEmpresa, long jobId);

    /**
     * Cierra la nómina con error cuando el proceso falla.
     *
     * @param idEmpresa identificador de la empresa
     * @param jobId     identificador del job
     * @param ex        la excepción que causó el error
     */
    void cerrarConError(long idEmpresa, long jobId, Exception ex);

    /**
     * Pobla la nómina con los clientes/portafolios del período.
     *
     * @param idEmpresa identificador de la empresa
     * @param jobId     identificador del job
     * @param idNomina  identificador de la nómina
     */
    void poblar(long idEmpresa, long jobId, long idNomina);

    /**
     * Obtiene el identificador de la nómina activa.
     *
     * @param idEmpresa identificador de la empresa
     * @param jobId     identificador del job
     * @return identificador de la nómina
     */
    long obtenerIdNomina(long idEmpresa, long jobId);
}
