package cl.bcs.facturacion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Migrated from legacy cl.bcs.facturacion.ConsultarMovimientoComprobanteCobros (JavaCall).
 * The legacy logica() was an empty stub that returned an empty response; this preserves that.
 */
@Service
public class ConsultarMovimientoComprobanteCobrosService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarMovimientoComprobanteCobrosService.class);

    public ConsultarMovimientoComprobanteCobrosService(NamedParameterJdbcTemplate jdbc) {
        // jdbc kept for future implementation
    }

    /**
     * @param params JsonObject with p_IdEmpresa, p_IdsMovimientosComprobante, etc.
     * @return JsonObject with P_OUT_ERRORESCONTADOR=0, P_OUT_MENSAJES=[], P_OUT_COBROS=[]
     */
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ConsultarMovimientoComprobanteCobros");

        // Legacy logica() was empty — preserve that behaviour.
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        out.add("P_OUT_MENSAJES", new JsonArray());
        out.add("P_OUT_COBROS", new JsonArray());

        log.info("FIN::ConsultarMovimientoComprobanteCobros");
        return out;
    }
}
