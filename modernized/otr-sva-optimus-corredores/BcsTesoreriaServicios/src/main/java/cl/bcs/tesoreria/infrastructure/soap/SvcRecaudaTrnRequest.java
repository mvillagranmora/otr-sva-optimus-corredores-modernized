package cl.bcs.tesoreria.infrastructure.soap;

import java.math.BigDecimal;

public record SvcRecaudaTrnRequest(
        String idDepositante,
        String idServicio,
        BigDecimal monto,
        String idModulo
) {}
