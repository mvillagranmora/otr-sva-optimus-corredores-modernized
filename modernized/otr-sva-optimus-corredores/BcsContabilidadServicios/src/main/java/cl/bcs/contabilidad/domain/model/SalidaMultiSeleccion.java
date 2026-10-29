package cl.bcs.contabilidad.domain.model;

import java.util.List;

/**
 * Output of the CentralizacionOrchestrator.
 *
 * Derived from legacy CentralizaMultiSeleccion.SalidaCentralizaMultiSeleccion.
 * erroresContador initialises to 0 in the legacy constructor.
 */
public record SalidaMultiSeleccion(
        /** Accumulated error count across all dispatched types. Legacy starts at 0. */
        long                          erroresContador,
        List<String>                  mensajes,
        List<ResultadoCentralizacion> resultados
) {
    public static SalidaMultiSeleccion empty() {
        return new SalidaMultiSeleccion(0L, List.of(), List.of());
    }
}
