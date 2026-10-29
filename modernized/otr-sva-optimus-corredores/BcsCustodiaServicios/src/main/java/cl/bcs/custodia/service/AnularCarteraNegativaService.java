package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.AnularCarteraNegativa (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.facturacion.dte.CallerWSDte.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class AnularCarteraNegativaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(AnularCarteraNegativaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.AnularCarteraNegativa";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("AnularCarteraNegativaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.AnularCarteraNegativa is deferred: cl.bcs.facturacion.dte.CallerWSDte");
    }
}
