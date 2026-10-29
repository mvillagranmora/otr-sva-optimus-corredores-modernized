package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.NotificarVencimientoInstrumentos (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.evt.AgregarEvento.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class NotificarVencimientoInstrumentosService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(NotificarVencimientoInstrumentosService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.NotificarVencimientoInstrumentos";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("NotificarVencimientoInstrumentosService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.NotificarVencimientoInstrumentos is deferred: cl.bcs.plataforma.evt.AgregarEvento");
    }
}
