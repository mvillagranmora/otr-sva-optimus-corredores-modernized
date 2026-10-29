package cl.bcs.contabilidad.domain.port.in;

import cl.bcs.contabilidad.domain.model.EntradaMultiSeleccion;
import cl.bcs.contabilidad.domain.model.SalidaMultiSeleccion;

/**
 * Primary port: drives the multi-selection centralisation workflow.
 *
 * Replaces legacy CentralizaMultiSeleccion.logica().
 */
public interface CentralizacionUseCase {

    /**
     * Executes the centralisation batch for all requested TipoCentralizacion ids.
     *
     * When entrada.simulaOperacion() == "S", all DB writes are suppressed but
     * the computed movements/events are still returned.
     */
    SalidaMultiSeleccion ejecutar(EntradaMultiSeleccion entrada);
}
