package cl.bcs.custodia.service.prt;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.prt.ConsultarResumenEmprestitoVigente (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores, cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores, cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ConsultarResumenEmprestitoVigenteService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ConsultarResumenEmprestitoVigenteService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.prt.ConsultarResumenEmprestitoVigente";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ConsultarResumenEmprestitoVigenteService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.prt.ConsultarResumenEmprestitoVigente is deferred: cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores, cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores, cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores");
    }
}
