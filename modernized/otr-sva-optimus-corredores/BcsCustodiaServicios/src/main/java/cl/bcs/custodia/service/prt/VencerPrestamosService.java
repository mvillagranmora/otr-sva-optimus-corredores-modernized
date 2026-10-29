package cl.bcs.custodia.service.prt;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.prt.VencerPrestamos (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.facturacion.CrearMovimientoComprobante, cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.FolioPrestamoBlockChain.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class VencerPrestamosService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(VencerPrestamosService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.prt.VencerPrestamos";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("VencerPrestamosService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.prt.VencerPrestamos is deferred: cl.bcs.facturacion.CrearMovimientoComprobante, cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.FolioPrestamoBlockChain");
    }
}
