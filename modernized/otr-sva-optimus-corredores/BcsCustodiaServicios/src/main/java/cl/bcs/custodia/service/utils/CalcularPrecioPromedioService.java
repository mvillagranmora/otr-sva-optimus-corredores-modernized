package cl.bcs.custodia.service.utils;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.utils.CalcularPrecioPromedio (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.tas.EntregarPreciosTasas.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CalcularPrecioPromedioService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CalcularPrecioPromedioService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.utils.CalcularPrecioPromedio";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CalcularPrecioPromedioService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.utils.CalcularPrecioPromedio is deferred: cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.tas.EntregarPreciosTasas");
    }
}
