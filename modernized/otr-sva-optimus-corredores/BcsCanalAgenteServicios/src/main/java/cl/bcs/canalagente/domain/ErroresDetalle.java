package cl.bcs.canalagente.domain;

/**
 * Error detail for the service response.
 *
 * Derived from ConsultarHistorialMovimientosCuentaInversion.ErroresDetalle.
 */
public record ErroresDetalle(
        String codigo,
        String descripcion
) {}
