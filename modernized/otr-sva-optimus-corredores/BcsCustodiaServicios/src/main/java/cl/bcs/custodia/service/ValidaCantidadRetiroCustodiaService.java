package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.ValidaCantidadRetiroCustodia (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion, cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion, cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion, cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ValidaCantidadRetiroCustodiaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ValidaCantidadRetiroCustodiaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.ValidaCantidadRetiroCustodia";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ValidaCantidadRetiroCustodiaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.ValidaCantidadRetiroCustodia is deferred: cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion, cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion, cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion, cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar");
    }
}
