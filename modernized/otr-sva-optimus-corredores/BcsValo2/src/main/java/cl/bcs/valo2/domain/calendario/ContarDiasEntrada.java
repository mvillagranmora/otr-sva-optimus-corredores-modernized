package cl.bcs.valo2.domain.calendario;

import java.time.LocalDate;

public record ContarDiasEntrada(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        String convencionDias,
        boolean ajusteEOM,
        String calendario
) {}
