package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record AnulacionMandatoPACResponse(
        long erroresContador,
        List<String> mensajes
) {}
