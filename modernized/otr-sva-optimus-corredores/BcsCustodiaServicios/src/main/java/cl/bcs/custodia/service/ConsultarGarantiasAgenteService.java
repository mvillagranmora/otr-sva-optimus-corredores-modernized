package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.ConsultarGarantiasAgente (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo, cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ConsultarGarantiasAgenteService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ConsultarGarantiasAgenteService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.ConsultarGarantiasAgente";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ConsultarGarantiasAgenteService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.ConsultarGarantiasAgente is deferred: cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo, cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada");
    }
}
