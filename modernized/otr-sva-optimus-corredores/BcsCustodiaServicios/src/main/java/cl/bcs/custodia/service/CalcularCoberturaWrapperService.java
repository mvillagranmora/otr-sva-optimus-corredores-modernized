package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.CalcularCoberturaWrapper (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA, cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJADETALLADO, cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CalcularCoberturaWrapperService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CalcularCoberturaWrapperService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.CalcularCoberturaWrapper";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CalcularCoberturaWrapperService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.CalcularCoberturaWrapper is deferred: cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA, cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJADETALLADO, cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR");
    }
}
