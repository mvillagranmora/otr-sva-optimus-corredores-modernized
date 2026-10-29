package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record LiquidarComprobanteResponse(
        long erroresContador,
        List<String> mensajes,
        List<ComprobanteResultado> resultadoProceso
) {}
