package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.ConsultarDetallePrecioPromedio (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.tas.EntregarPreciosTasas, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ConsultarDetallePrecioPromedioService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ConsultarDetallePrecioPromedioService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.ConsultarDetallePrecioPromedio";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ConsultarDetallePrecioPromedioService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.ConsultarDetallePrecioPromedio is deferred: cl.bcs.plataforma.tas.EntregarPreciosTasas, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas");
    }
}
