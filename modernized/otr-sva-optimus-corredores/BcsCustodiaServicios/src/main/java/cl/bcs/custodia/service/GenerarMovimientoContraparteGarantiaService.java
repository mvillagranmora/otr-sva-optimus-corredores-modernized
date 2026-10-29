package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.GenerarMovimientoContraparteGarantia (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores, cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores, cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores, cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores.FechasIn.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class GenerarMovimientoContraparteGarantiaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(GenerarMovimientoContraparteGarantiaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.GenerarMovimientoContraparteGarantia";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("GenerarMovimientoContraparteGarantiaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.GenerarMovimientoContraparteGarantia is deferred: cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores, cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores, cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores, cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores.FechasIn");
    }
}
