package cl.bcs.tesoreria.domain.model;

import java.math.BigDecimal;

public record MovimientoRequest(
        Long numeroReferencia,
        String idTipoMovimiento,
        String idTipoPersona,
        Long idPersona,
        Long idPortafolio,
        String idMoneda,
        BigDecimal monto,
        String fechaLiquidacion,
        String idConcepto,
        String generaComprobante,
        String liquidarComprobante
) {}
