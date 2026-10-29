package cl.bcs.valo2.domain.pagare;

public record ValorizarPagareResultado(
        double captacion,
        double rescate,
        double fraccionAno,
        int codigoRetorno,
        String mensaje
) {}
