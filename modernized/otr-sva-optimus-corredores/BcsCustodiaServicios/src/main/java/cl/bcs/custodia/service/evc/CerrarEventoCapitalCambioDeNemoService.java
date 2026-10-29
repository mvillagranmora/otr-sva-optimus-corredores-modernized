package cl.bcs.custodia.service.evc;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.evc.CerrarEventoCapitalCambioDeNemo (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CerrarEventoCapitalCambioDeNemoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CerrarEventoCapitalCambioDeNemoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.evc.CerrarEventoCapitalCambioDeNemo";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CerrarEventoCapitalCambioDeNemoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.evc.CerrarEventoCapitalCambioDeNemo is deferred: cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida");
    }
}
