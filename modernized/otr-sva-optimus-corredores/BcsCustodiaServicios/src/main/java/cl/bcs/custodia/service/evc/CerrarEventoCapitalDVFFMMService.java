package cl.bcs.custodia.service.evc;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.evc.CerrarEventoCapitalDVFFMM (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos, cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos.EntradaCrearSolicitudAportePorDividendos, cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos.InSolicitudesAporte, cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos.SalidaCrearSolicitudAportePorDividendos.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CerrarEventoCapitalDVFFMMService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CerrarEventoCapitalDVFFMMService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.evc.CerrarEventoCapitalDVFFMM";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CerrarEventoCapitalDVFFMMService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.evc.CerrarEventoCapitalDVFFMM is deferred: cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos, cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos.EntradaCrearSolicitudAportePorDividendos, cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos.InSolicitudesAporte, cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos.SalidaCrearSolicitudAportePorDividendos");
    }
}
