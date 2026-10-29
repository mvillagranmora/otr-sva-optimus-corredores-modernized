package cl.bcs.canalagente.domain;

/**
 * Warning detail for the service response.
 *
 * Derived from ConsultarHistorialMovimientosCuentaInversion.AdvertenciasDetalle.
 */
public record AdvertenciasDetalle(
        String codigo,
        String requiereAutorizacion,
        String descripcion
) {}
