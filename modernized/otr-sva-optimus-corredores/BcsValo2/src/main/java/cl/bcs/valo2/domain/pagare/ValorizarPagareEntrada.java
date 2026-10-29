package cl.bcs.valo2.domain.pagare;

import java.time.LocalDate;

public record ValorizarPagareEntrada(
        int tipoInteres,
        int diasAno,
        int plazoEnDias,
        LocalDate fechaCaptacion,
        LocalDate fechaRescate,
        double captacion,
        double rescate,
        double tasa
) {}
