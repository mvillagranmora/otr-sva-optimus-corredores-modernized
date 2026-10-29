package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record GirarComprobantesEgresoResponse(
        long erroresContador,
        List<String> mensajes,
        List<Long> comprobantesCreados
) {}
