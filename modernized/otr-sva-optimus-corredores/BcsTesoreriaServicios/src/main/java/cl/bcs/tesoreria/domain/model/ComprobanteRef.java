package cl.bcs.tesoreria.domain.model;

public record ComprobanteRef(
        Long idComprobante,
        String tipoMovimiento,
        String observacion
) {}
