package cl.bcs.tesoreria.domain.model;

import java.math.BigDecimal;

public record FormaDePagoRequest(
        Long idFormaDePago,
        String tipo,
        BigDecimal monto,
        String idMoneda
) {}
