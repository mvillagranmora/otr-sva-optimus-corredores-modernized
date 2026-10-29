package cl.bcs.custodia.service.wbs;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.wbs.ConsultarPatrimonioCustodia (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.normativa.cartola.BcsCartola.Dato, cl.bcs.normativa.cartola.ResumenInversionBase, cl.bcs.normativa.cartola.ResumenInversionBase.EntradaResumenInversionBase, cl.bcs.normativa.cartola.ResumenInversionBase.OutResumenInversion, cl.bcs.normativa.cartola.ResumenInversionBase.SalidaResumenInversionBase.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ConsultarPatrimonioCustodiaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ConsultarPatrimonioCustodiaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.wbs.ConsultarPatrimonioCustodia";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ConsultarPatrimonioCustodiaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.wbs.ConsultarPatrimonioCustodia is deferred: cl.bcs.normativa.cartola.BcsCartola.Dato, cl.bcs.normativa.cartola.ResumenInversionBase, cl.bcs.normativa.cartola.ResumenInversionBase.EntradaResumenInversionBase, cl.bcs.normativa.cartola.ResumenInversionBase.OutResumenInversion, cl.bcs.normativa.cartola.ResumenInversionBase.SalidaResumenInversionBase");
    }
}
