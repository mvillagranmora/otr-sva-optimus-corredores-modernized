package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.AnularGarantia (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.cuentaInversion.AnularCargoAbono, cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Entrada, cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Salida, cl.bcs.cuentaInversion.AnularCargoAbono.CargoAbono_Entrada, cl.bcs.plataforma.pla.AgregarLog.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class AnularGarantiaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(AnularGarantiaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.AnularGarantia";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("AnularGarantiaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.AnularGarantia is deferred: cl.bcs.cuentaInversion.AnularCargoAbono, cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Entrada, cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Salida, cl.bcs.cuentaInversion.AnularCargoAbono.CargoAbono_Entrada, cl.bcs.plataforma.pla.AgregarLog");
    }
}
