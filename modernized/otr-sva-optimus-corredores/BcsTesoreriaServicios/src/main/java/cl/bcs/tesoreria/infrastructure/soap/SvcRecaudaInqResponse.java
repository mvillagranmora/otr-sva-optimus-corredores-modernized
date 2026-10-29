package cl.bcs.tesoreria.infrastructure.soap;

public record SvcRecaudaInqResponse(
        String codigoRespuesta,
        String severidad,
        String idDepositante,
        String estadoDeposito
) {}
