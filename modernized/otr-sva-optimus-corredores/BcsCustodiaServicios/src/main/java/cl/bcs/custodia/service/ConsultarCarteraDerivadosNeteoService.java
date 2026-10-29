package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.ConsultarCarteraDerivadosNeteo (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ConsultarCarteraDerivadosNeteoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ConsultarCarteraDerivadosNeteoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.ConsultarCarteraDerivadosNeteo";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ConsultarCarteraDerivadosNeteoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.ConsultarCarteraDerivadosNeteo is deferred: cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente");
    }
}
