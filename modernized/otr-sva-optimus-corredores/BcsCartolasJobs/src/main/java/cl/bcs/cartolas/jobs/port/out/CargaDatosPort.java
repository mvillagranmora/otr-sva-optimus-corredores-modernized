package cl.bcs.cartolas.jobs.port.out;

/**
 * Puerto de salida para el proceso de carga de datos de cartolas.
 *
 * Oracle: cl.bcs.normativa.cartolas.procesos.ProcesoCargaDatos
 *
 * B-CAR-05: Carga de datos — requiere HANA (SPs BSCL.Normativa.BD.Servicios::CAR.*)
 */
public interface CargaDatosPort {

    /**
     * Ejecuta el proceso de carga de datos para la nómina activa.
     *
     * @param idEmpresa identificador de la empresa
     * @param jobId     identificador del job
     */
    void cargar(long idEmpresa, long jobId);
}
