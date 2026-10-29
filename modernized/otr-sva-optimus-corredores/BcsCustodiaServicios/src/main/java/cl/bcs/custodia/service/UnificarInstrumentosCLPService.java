package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.UnificarInstrumentosCLP (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.cre.ConsultarEstadoModulo, cl.bcs.plataforma.cre.ConsultarEstadoModulo.InConsultarEstadoModulo, cl.bcs.plataforma.cre.ConsultarEstadoModulo.Modulo, cl.bcs.plataforma.cre.ConsultarEstadoModulo.OutConsultarEstadoModulo.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class UnificarInstrumentosCLPService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(UnificarInstrumentosCLPService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.UnificarInstrumentosCLP";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("UnificarInstrumentosCLPService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.UnificarInstrumentosCLP is deferred: cl.bcs.plataforma.cre.ConsultarEstadoModulo, cl.bcs.plataforma.cre.ConsultarEstadoModulo.InConsultarEstadoModulo, cl.bcs.plataforma.cre.ConsultarEstadoModulo.Modulo, cl.bcs.plataforma.cre.ConsultarEstadoModulo.OutConsultarEstadoModulo");
    }
}
