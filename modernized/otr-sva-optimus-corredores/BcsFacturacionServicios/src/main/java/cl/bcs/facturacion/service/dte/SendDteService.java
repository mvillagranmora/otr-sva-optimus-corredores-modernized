package cl.bcs.facturacion.service.dte;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Migrated from legacy cl.bcs.facturacion.dte.SendDte.
 * Sends DTE (Documento Tributario Electrónico) documents to the SII web service.
 * Registered in InternalJavaCallController as cl.bcs.facturacion.dte.SendDte.
 */
@Component
public class SendDteService {

    private static final Logger log = LoggerFactory.getLogger(SendDteService.class);

    private final String dteWsUrl;

    public SendDteService(@Value("${bcs.dte.ws.url:}") String dteWsUrl) {
        this.dteWsUrl = dteWsUrl;
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::SendDte");

        // TODO: configurar endpoint WS DTE (SII)
        // Legacy: uses CallerWSDte with SSL and certificate-based auth to SII
        // Requires: ${bcs.dte.ws.url}, ${bcs.dte.ws.cert.path}, ${bcs.dte.ws.cert.password}
        // SSL trust-all → proper TLS (RULE-077)
        if (dteWsUrl == null || dteWsUrl.isBlank()) {
            log.warn("SendDte: bcs.dte.ws.url not configured — DTE submission skipped");
        } else {
            log.info("SendDte: DTE WS URL configured at {}", dteWsUrl);
            // TODO: implement RestClient-based DTE submission using proper TLS
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());
        out.addProperty("P_OUT_TRACKID", "");
        out.addProperty("P_OUT_ESTADO", "PENDIENTE");
        return out;
    }
}
