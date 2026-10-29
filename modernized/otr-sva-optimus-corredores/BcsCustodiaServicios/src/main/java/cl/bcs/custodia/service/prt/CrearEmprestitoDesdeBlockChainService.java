package cl.bcs.custodia.service.prt;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.prt.CrearEmprestitoDesdeBlockChain (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.PrestamosBlockChain, cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.lendingClients, cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoAsignado, cl.bcs.socionegocio.bck.ProcesaLogBlockChain.Instrumento.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CrearEmprestitoDesdeBlockChainService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CrearEmprestitoDesdeBlockChainService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.prt.CrearEmprestitoDesdeBlockChain";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CrearEmprestitoDesdeBlockChainService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.prt.CrearEmprestitoDesdeBlockChain is deferred: cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.PrestamosBlockChain, cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.lendingClients, cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoAsignado, cl.bcs.socionegocio.bck.ProcesaLogBlockChain.Instrumento");
    }
}
