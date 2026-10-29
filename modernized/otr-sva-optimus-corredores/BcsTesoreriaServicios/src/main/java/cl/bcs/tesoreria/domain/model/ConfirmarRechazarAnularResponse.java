package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record ConfirmarRechazarAnularResponse(
        long erroresContador,
        List<String> mensajes
) {}
