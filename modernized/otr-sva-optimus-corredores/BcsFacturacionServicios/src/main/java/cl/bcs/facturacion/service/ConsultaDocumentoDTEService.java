package cl.bcs.facturacion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsultaDocumentoDTEService {

    private static final Logger log = LoggerFactory.getLogger(ConsultaDocumentoDTEService.class);

    public ConsultaDocumentoDTEService() {
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ConsultaDocumentoDTE");

        // TODO: configurar endpoint WS DTE
        // Legacy: calls CallerWSDte to retrieve DTE document status from SII (Servicio de Impuestos Internos)
        // Requires: ${bcs.dte.ws.url} configuration property
        log.warn("ConsultaDocumentoDTE WS endpoint not configured — requires DTE service configuration");

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());
        out.add("P_OUT_DOCUMENTOS_DTE", new JsonArray());
        return out;
    }
}
