package cl.bcs.custodia.service.prt;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.prt.ModificarPrestamoDesdeBlockChain (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoModificado, cl.bcs.socionegocio.pojo.ConstantesBCK.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ModificarPrestamoDesdeBlockChainService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ModificarPrestamoDesdeBlockChainService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.prt.ModificarPrestamoDesdeBlockChain";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ModificarPrestamoDesdeBlockChainService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.prt.ModificarPrestamoDesdeBlockChain is deferred: cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoModificado, cl.bcs.socionegocio.pojo.ConstantesBCK");
    }
}
