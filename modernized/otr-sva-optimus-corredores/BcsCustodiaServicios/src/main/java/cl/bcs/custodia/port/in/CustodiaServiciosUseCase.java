package cl.bcs.custodia.port.in;

import cl.bcs.custodia.domain.PosicionCustodia;

import java.util.List;

/**
 * Inbound port — driving-side contract for Custodia Servicios.
 *
 * H-3 fix: provides a typed interface so inbound adapters (REST controllers,
 * batch jobs, schedulers) depend on this port rather than the concrete service class.
 *
 * Static utility methods (cuentaDias, calculoInteres, determinarNuevoNemo, etc.)
 * are kept as public static on CustodiaServiciosService for testability; they are
 * not modelled here because they have no injected dependencies and are invoked
 * directly within the service layer, not from external adapters.
 */
public interface CustodiaServiciosUseCase {

    /**
     * Returns the system date for the given company from CONFIGURACION_T.
     *
     * @param idEmpresa company identifier
     * @return system date string
     */
    String obtenerFechaSistema(Long idEmpresa);

    /**
     * Returns the custody position list for the given portfolio from POSICION_CUSTODIA_T.
     *
     * @param idPortafolio portfolio identifier
     * @return list of custody positions
     */
    List<PosicionCustodia> consultarPosicion(Long idPortafolio);
}
