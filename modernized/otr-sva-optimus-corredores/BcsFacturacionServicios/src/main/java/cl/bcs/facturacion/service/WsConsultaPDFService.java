package cl.bcs.facturacion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WsConsultaPDFService {

    private static final Logger log = LoggerFactory.getLogger(WsConsultaPDFService.class);

    public WsConsultaPDFService() {
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::WsConsultaPDF");

        // TODO: configurar endpoint WS para consulta de PDF
        // Legacy: calls Crystal Reports WS or DTE PDF service
        // Requires: ${bcs.pdf.ws.url} or ${bcs.crystalreports.url} configuration
        log.warn("WsConsultaPDF endpoint not configured — requires PDF/Crystal Reports service configuration");

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());
        out.addProperty("P_OUT_PDF_URL", "");
        return out;
    }
}
