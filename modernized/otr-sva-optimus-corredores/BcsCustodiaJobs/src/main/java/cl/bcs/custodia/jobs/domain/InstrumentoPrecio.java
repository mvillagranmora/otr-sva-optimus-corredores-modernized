package cl.bcs.custodia.jobs.domain;

import java.math.BigDecimal;

/**
 * Represents a financial instrument with its price information.
 * Legacy source: RegistraPrecioInstrumento.java (RegistraPrecioBolsaInstrumento_Calculado)
 */
public record InstrumentoPrecio(
        Long idInstrumento,
        String codigoNemo,
        BigDecimal precio,
        String fechaPrecio
) {}
