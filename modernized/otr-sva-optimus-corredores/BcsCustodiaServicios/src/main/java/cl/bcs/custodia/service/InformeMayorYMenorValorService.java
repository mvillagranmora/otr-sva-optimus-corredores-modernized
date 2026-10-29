package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.InformeMayorYMenorValor (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.mesa.MRF.ConsultarCarteraPropiaValorizada.Cliente, cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.AgregarLog.InAgregarLog, cl.bcs.plataforma.pla.AgregarLog.OutAgregarLog, cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class InformeMayorYMenorValorService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(InformeMayorYMenorValorService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.InformeMayorYMenorValor";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("InformeMayorYMenorValorService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.InformeMayorYMenorValor is deferred: cl.bcs.mesa.MRF.ConsultarCarteraPropiaValorizada.Cliente, cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.AgregarLog.InAgregarLog, cl.bcs.plataforma.pla.AgregarLog.OutAgregarLog, cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log");
    }
}
