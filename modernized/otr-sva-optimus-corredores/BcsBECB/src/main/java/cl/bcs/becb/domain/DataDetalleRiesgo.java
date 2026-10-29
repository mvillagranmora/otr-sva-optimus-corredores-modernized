package cl.bcs.becb.domain;

import java.math.BigDecimal;

public record DataDetalleRiesgo(
        String estado,
        String emisor,
        String familia,
        String nemo,
        BigDecimal cantidad,
        BigDecimal nominal,
        BigDecimal tirTasa,
        String tipoReajuste,
        String fechaVencimiento,
        BigDecimal valorPresente,
        BigDecimal plazo,
        BigDecimal volatilidad,
        BigDecimal duracion,
        BigDecimal var,
        Long secCar
) {}
