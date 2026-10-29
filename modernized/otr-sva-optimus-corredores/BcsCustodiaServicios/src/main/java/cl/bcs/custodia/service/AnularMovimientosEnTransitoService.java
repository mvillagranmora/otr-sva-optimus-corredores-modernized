package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.AnularMovimientosEnTransito (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class AnularMovimientosEnTransitoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(AnularMovimientosEnTransitoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.AnularMovimientosEnTransito";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("AnularMovimientosEnTransitoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.AnularMovimientosEnTransito is deferred: cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida");
    }
}
