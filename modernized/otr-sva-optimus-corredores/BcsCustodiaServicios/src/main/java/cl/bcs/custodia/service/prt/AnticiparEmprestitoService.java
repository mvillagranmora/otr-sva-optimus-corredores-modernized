package cl.bcs.custodia.service.prt;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.prt.AnticiparEmprestito (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.socionegocio.bck.AnticiparPrestamistaBlockChain, cl.bcs.socionegocio.bck.AnticiparPrestamistaBlockChain.AnticiparPrestamistaBlockChain_Entrada, cl.bcs.socionegocio.bck.AnticiparPrestamistaBlockChain.AnticiparPrestamistaBlockChain_Salida, cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.TipoDocumento, cl.bcs.socionegocio.pojo.ConstantesBCK.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class AnticiparEmprestitoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(AnticiparEmprestitoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.prt.AnticiparEmprestito";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("AnticiparEmprestitoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.prt.AnticiparEmprestito is deferred: cl.bcs.socionegocio.bck.AnticiparPrestamistaBlockChain, cl.bcs.socionegocio.bck.AnticiparPrestamistaBlockChain.AnticiparPrestamistaBlockChain_Entrada, cl.bcs.socionegocio.bck.AnticiparPrestamistaBlockChain.AnticiparPrestamistaBlockChain_Salida, cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.TipoDocumento, cl.bcs.socionegocio.pojo.ConstantesBCK");
    }
}
