package cl.bcs.fondomutuo.jobs.domain;

import java.math.BigDecimal;

/**
 * Serie de fondo with decimals for BF reinversion calculation.
 * Mirrors legacy cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes.
 */
public record SerieFondoSolicitudes(String idSerieFondo, BigDecimal decimalesCuota) {}
