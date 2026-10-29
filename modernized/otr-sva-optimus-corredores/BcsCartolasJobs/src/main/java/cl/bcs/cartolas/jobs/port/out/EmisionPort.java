package cl.bcs.cartolas.jobs.port.out;

/**
 * Puerto de salida para el proceso de emisión de cartolas.
 *
 * Oracle: cl.bcs.normativa.cartolas.master.ProcesoEmisionMaster
 *
 * B-CAR-06: Emisión de cartolas — requiere HANA (SPs y Crystal Reports)
 */
public interface EmisionPort {

    /**
     * Ejecuta el proceso de emisión (generación y distribución de cartolas).
     *
     * @param idEmpresa identificador de la empresa
     * @param jobId     identificador del job
     */
    void emitir(long idEmpresa, long jobId);
}
