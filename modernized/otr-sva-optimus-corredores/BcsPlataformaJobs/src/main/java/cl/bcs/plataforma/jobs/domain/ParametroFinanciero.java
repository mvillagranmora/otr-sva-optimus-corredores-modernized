package cl.bcs.plataforma.jobs.domain;

import java.math.BigDecimal;

/**
 * Financial parameter (exchange rate, UF, IPC, etc.).
 * Legacy: cl.bcs.plataforma.jobs.ActualizarParametrosFinancieros.ParametroFinanciero
 */
public record ParametroFinanciero(
        String idParametroFinanciero,
        String fecha,
        String agnoMes,
        String agno,
        BigDecimal valor
) {}
