package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.BuscarEventosDeCapitalFaltantes (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.pla.ObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class BuscarEventosDeCapitalFaltantesService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(BuscarEventosDeCapitalFaltantesService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.BuscarEventosDeCapitalFaltantes";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("BuscarEventosDeCapitalFaltantesService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.BuscarEventosDeCapitalFaltantes is deferred: cl.bcs.plataforma.pla.ObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente");
    }
}
