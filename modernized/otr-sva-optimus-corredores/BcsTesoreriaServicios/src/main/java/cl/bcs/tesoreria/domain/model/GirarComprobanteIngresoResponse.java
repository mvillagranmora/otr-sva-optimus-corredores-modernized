package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record GirarComprobanteIngresoResponse(
        long erroresContador,
        List<String> mensajes,
        List<Long> comprobantesCreados
) {}
