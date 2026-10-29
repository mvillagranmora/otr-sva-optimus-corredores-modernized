package cl.bcs.custodia.service.prt;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.prt.AnularEmprestitoDesdeBlockChain (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoPrestamistaDesasignado.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class AnularEmprestitoDesdeBlockChainService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(AnularEmprestitoDesdeBlockChainService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.prt.AnularEmprestitoDesdeBlockChain";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("AnularEmprestitoDesdeBlockChainService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.prt.AnularEmprestitoDesdeBlockChain is deferred: cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoPrestamistaDesasignado");
    }
}
