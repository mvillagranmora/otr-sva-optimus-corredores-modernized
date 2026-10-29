package cl.bcs.custodia.service.evc;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.evc.CerrarEventoCapitalMasivo (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida, cl.bcs.tesoreria.LiquidarMovimientoAutomatico, cl.bcs.tesoreria.LiquidarMovimientoAutomatico.LiquidarMovimientoAutomatico_Entrada, cl.bcs.tesoreria.LiquidarMovimientoAutomatico.LiquidarMovimientoAutomatico_Salida.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CerrarEventoCapitalMasivoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CerrarEventoCapitalMasivoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.evc.CerrarEventoCapitalMasivo";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CerrarEventoCapitalMasivoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.evc.CerrarEventoCapitalMasivo is deferred: cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida, cl.bcs.tesoreria.LiquidarMovimientoAutomatico, cl.bcs.tesoreria.LiquidarMovimientoAutomatico.LiquidarMovimientoAutomatico_Entrada, cl.bcs.tesoreria.LiquidarMovimientoAutomatico.LiquidarMovimientoAutomatico_Salida");
    }
}
