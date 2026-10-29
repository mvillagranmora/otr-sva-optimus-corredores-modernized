package cl.bcs.custodia.domain;

import java.math.BigDecimal;

/**
 * Domain record for custodian position data.
 *
 * Real HANA source: BSCL."BSCL.Custodia.BD::CUS.CustodiaPortafolio"
 * Columns: IdPortafolio, IdInstrumento, Nemotecnico, CantidadCustodia
 */
public record PosicionCustodia(
        Long idPortafolio,
        Long idInstrumento,
        String nemotecnico,
        BigDecimal cantidad
) {}
