package cl.bcs.tesoreria.infrastructure.soap;

public record SvcRecaudaInqRequest(
        String idDepositante,
        String idModulo
) {}
