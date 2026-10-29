package cl.bcs.tesoreria.domain.model;

import java.math.BigDecimal;

public record ComprobanteInfo(
        Long idComprobante,
        Long idIngresoEgreso,
        String tipoMovimiento,
        String idEstado,
        BigDecimal monto,
        Long cantidadFormaDePago,
        BigDecimal montoPendiente,
        String fechaMovimiento,
        String idMoneda,
        String idEmpresa,
        String idModulo,
        String idConcepto,
        String usaReglasNegocio,
        String observacion
) {}
