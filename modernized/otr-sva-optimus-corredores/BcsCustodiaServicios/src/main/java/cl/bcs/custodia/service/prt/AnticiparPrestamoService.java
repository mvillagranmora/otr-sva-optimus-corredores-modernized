package cl.bcs.custodia.service.prt;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.prt.AnticiparPrestamo (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain, cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.AnticiparPrestamoBlockChain_Entrada, cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.AnticiparPrestamoBlockChain_Salida, cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.FolioPrestamoBlockChain, cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.Status, cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.TipoDocumento, cl.bcs.socionegocio.bck.ReintentoPrestamosBlockChain.Anticipos, cl.bcs.socionegocio.pojo.ConstantesBCK.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class AnticiparPrestamoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(AnticiparPrestamoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.prt.AnticiparPrestamo";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("AnticiparPrestamoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.prt.AnticiparPrestamo is deferred: cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain, cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.AnticiparPrestamoBlockChain_Entrada, cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.AnticiparPrestamoBlockChain_Salida, cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.FolioPrestamoBlockChain, cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.Status, cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.TipoDocumento, cl.bcs.socionegocio.bck.ReintentoPrestamosBlockChain.Anticipos, cl.bcs.socionegocio.pojo.ConstantesBCK");
    }
}
