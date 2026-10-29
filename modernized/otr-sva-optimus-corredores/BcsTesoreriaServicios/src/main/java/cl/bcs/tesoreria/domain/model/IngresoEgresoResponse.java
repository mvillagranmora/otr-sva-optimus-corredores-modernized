package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record IngresoEgresoResponse(
        long erroresContador,
        List<String> mensajes,
        List<ResultadoServicioTesoreria> resultados
) {}
