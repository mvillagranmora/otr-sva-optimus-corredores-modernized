package cl.bcs.tributario.jobs.port.out;

import cl.bcs.tributario.jobs.domain.CargaIpcResultado;

/**
 * Output port for calling the HANA stored procedure that loads IPC (Consumer
 * Price Index) factor values into the system.
 *
 * <p>The underlying SP is:
 * {@code BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc}.</p>
 */
public interface CargaIpcHanaPort {

    /**
     * Executes the IPC load stored procedure.
     *
     * @param idEmpresa         company identifier (IN param 1)
     * @param idUsuario         user identifier (IN param 2)
     * @param idModulo          module identifier, typically {@code "TRI"} (IN param 3)
     * @param simularOperacion  simulate-only flag, e.g. {@code ""} or {@code "S"} (IN param 4)
     * @param usaReglasNegocio  apply business rules flag, e.g. {@code ""} or {@code "S"} (IN param 5)
     * @return {@link CargaIpcResultado} containing advertencias and errores counters
     * @throws UnsupportedOperationException when a live HANA environment is required
     */
    CargaIpcResultado ejecutarCargarValorFactorIpc(
            long idEmpresa,
            long idUsuario,
            String idModulo,
            String simularOperacion,
            String usaReglasNegocio);
}
