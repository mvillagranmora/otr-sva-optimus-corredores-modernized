package cl.bcs.cartolas.jobs.port.out;

/**
 * Puerto de salida para el proceso programado de cartolas.
 *
 * Oracle: cl.bcs.normativa.cartolas.procesos.ProcesoProgramacionCartolas
 *
 * B-CAR-07: Proceso programado — requiere HANA
 */
public interface ProgramacionPort {

    /**
     * Verifica y ejecuta los procesos de cartolas programados pendientes.
     *
     * Oracle: ProcesoProgramacionCartolas.procesar() — lee los procesos
     *         programados de HANA y los ejecuta si corresponde.
     *
     * @param idEmpresa identificador de la empresa
     */
    void procesarPendientes(long idEmpresa);
}
