package cl.bcs.custodia.service.evc;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.evc.ProcesarEventoCapitalCorteCupon (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ProcesarEventoCapitalCorteCuponService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ProcesarEventoCapitalCorteCuponService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.evc.ProcesarEventoCapitalCorteCupon";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ProcesarEventoCapitalCorteCuponService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.evc.ProcesarEventoCapitalCorteCupon is deferred: cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut");
    }
}
