package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RevocacionContratoCSCManejadorService {

    private static final Logger log = LoggerFactory.getLogger(RevocacionContratoCSCManejadorService.class);

    private final RevocacionContratoCSCService revocacion;

    public RevocacionContratoCSCManejadorService(RevocacionContratoCSCService revocacion) {
        this.revocacion = revocacion;
    }

    public JsonObject execute(JsonObject params) {
        log.info("RevocacionContratoCSCManejador");
        JsonArray contratos = params.has("p_Contrato") ? params.getAsJsonArray("p_Contrato") : new JsonArray();
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray resultados = new JsonArray();
        for (JsonElement e : contratos) {
            JsonObject contratoParams = new JsonObject();
            contratoParams.add("p_Contrato", e);
            contratoParams.addProperty("p_IdEmpresa", idEmpresa);
            JsonObject resultado = revocacion.execute(contratoParams);
            resultados.add(resultado);
        }
        out.add("P_OUT_RESULTADO", resultados);
        return out;
    }
}
