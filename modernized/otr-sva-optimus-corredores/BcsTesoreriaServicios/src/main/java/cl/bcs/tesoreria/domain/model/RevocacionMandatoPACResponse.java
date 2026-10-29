package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record RevocacionMandatoPACResponse(
        long erroresContador,
        List<String> mensajes
) {}
