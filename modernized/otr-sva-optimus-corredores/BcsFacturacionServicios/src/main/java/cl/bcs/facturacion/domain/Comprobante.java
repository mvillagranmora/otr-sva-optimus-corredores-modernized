package cl.bcs.facturacion.domain;

public record Comprobante(
        Long idComprobante,
        Long idEmpresa,
        String idTipoComprobante,
        String estado,
        String fechaComprobante
) {}
