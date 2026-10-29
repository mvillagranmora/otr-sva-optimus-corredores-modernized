package cl.bcs.custodia.service.prt;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.prt.LiquidarCustodiaPrestamo (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class LiquidarCustodiaPrestamoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(LiquidarCustodiaPrestamoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.prt.LiquidarCustodiaPrestamo";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("LiquidarCustodiaPrestamoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.prt.LiquidarCustodiaPrestamo is deferred: cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida");
    }
}
