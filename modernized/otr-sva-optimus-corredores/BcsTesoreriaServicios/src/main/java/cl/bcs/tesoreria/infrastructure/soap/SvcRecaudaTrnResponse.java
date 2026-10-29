package cl.bcs.tesoreria.infrastructure.soap;

public record SvcRecaudaTrnResponse(
        String codigoRespuesta,
        String severidad,
        String idTransaccion
) {}
