package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.GenerarReporteCuadratura (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.report.api.ReportDocument.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class GenerarReporteCuadraturaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(GenerarReporteCuadraturaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.GenerarReporteCuadratura";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("GenerarReporteCuadraturaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.GenerarReporteCuadratura is deferred: cl.bcs.plataforma.report.api.ReportDocument");
    }
}
