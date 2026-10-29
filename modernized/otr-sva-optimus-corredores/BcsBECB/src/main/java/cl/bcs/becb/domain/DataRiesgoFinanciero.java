package cl.bcs.becb.domain;

import java.math.BigDecimal;

public record DataRiesgoFinanciero(
        String instrumentoEmisor,
        BigDecimal rango,
        BigDecimal volatilidad,
        BigDecimal duracion,
        BigDecimal valorPresente,
        BigDecimal var
) {}
