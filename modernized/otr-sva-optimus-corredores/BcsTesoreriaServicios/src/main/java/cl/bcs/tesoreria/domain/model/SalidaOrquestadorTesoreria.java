package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record SalidaOrquestadorTesoreria(
        long erroresContador,
        List<String> mensajes,
        List<ResultadoServicioTesoreria> resultados
) {}
