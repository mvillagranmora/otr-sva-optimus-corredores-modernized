package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record MandatoPACResponse(
        long erroresContador,
        List<String> mensajes,
        List<Long> resultado
) {}
