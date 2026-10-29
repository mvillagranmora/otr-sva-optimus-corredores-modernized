package cl.bcs.fondomutuo.jobs.domain;

import java.math.BigDecimal;

/**
 * Custody portfolio row from ConsultarCustodiaPortafolio service.
 * Mirrors legacy Tabla_Salida from ConsultarCustodiaPortafolio.
 */
public record CustodiaPortafolio(
        Long       idPortafolio,
        String     nemotecnico,
        BigDecimal cantidadCustodia,
        BigDecimal montoMercadoCustodia
) {}
