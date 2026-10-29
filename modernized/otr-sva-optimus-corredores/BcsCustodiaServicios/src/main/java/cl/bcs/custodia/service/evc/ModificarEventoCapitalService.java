package cl.bcs.custodia.service.evc;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.evc.ModificarEventoCapital (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ModificarEventoCapitalService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ModificarEventoCapitalService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.evc.ModificarEventoCapital";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ModificarEventoCapitalService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.evc.ModificarEventoCapital is deferred: cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.AgregarLog.InAgregarLog");
    }
}
