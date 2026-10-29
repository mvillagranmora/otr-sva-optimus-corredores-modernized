package cl.bcs.valo2.domain.bono;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ValorizarBonosEntrada(
        String nemo,
        LocalDate fechaValorizacion,
        String moneda,
        String monedaPago,
        BigDecimal precio,
        BigDecimal tasa,
        BigDecimal valorNominal,
        long idRequerimiento
) {}
