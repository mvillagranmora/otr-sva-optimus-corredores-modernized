package cl.bcs.fondomutuo.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain model for the unit price (valor cuota) of a mutual fund series.
 * Migrated from: cl.bcs.fondomutuo.pojo.ValorCuota
 */
public record ValorCuota(
        String idSerieFondo,
        LocalDate fechaCierre,
        BigDecimal valorCuota,
        String idMoneda,
        String observacion,
        LocalDate fechaCarga,
        Long idEmpresa
) {}
