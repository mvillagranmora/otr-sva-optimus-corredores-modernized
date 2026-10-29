package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;

/**
 * Spring service interface for individual centralisation types.
 *
 * Replacing the legacy reflection-based dispatch (Class.forName + newInstance),
 * each implementation registers itself as a named Spring bean.  The
 * CentralizacionOrchestrator resolves them via an injected
 * Map&lt;String, BcsCentralizacionService&gt;.
 *
 * Legacy equivalent: BcsCentralizacion (a plain interface, not a Spring bean).
 */
public interface BcsCentralizacionService {

    /**
     * Performs the accounting centralisation for one TipoCentralizacion.
     *
     * When {@code entrada.simulaOperacion()} is {@code "S"}, implementations
     * must compute movements and events but must NOT call any repository write
     * method.
     */
    SalidaCentralizacion centralizar(EntradaCentralizacion entrada);
}
