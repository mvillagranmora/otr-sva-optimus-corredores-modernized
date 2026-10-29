package cl.bcs.custodia.service.utils;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.utils.EstandarizacionRegistroCustodia (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.pla.ObtenerNemoSVS.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class EstandarizacionRegistroCustodiaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(EstandarizacionRegistroCustodiaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.utils.EstandarizacionRegistroCustodia";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("EstandarizacionRegistroCustodiaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.utils.EstandarizacionRegistroCustodia is deferred: cl.bcs.plataforma.pla.ObtenerNemoSVS");
    }
}
