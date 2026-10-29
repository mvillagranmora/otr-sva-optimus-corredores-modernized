package cl.bcs.fondomutuo.jobs.domain;

import java.math.BigDecimal;

/**
 * Reinversion destino record for IngresarOrdenBonificacionFiscalReinversion.
 * Mirrors legacy cl.bcs.fondomutuo.pojo.ReinversionSerieFondoDestino.
 */
public record ReinversionSerieFondoDestino(
        Long       idSolicitudAporteRescate,
        String     idSerieFondoDestino,
        BigDecimal porcentaje
) {}
