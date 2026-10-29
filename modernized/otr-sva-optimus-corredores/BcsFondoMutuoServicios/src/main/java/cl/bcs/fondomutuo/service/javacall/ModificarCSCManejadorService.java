package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ModificarCSCManejadorService {

    private static final Logger log = LoggerFactory.getLogger(ModificarCSCManejadorService.class);

    private final ModificarCSCService modificarCSC;

    public ModificarCSCManejadorService(ModificarCSCService modificarCSC) {
        this.modificarCSC = modificarCSC;
    }

    public JsonObject execute(JsonObject params) {
        log.info("ModificarCSCManejador");
        JsonArray contratos = params.has("p_ModificarCSC") ? params.getAsJsonArray("p_ModificarCSC") : new JsonArray();
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray resultados = new JsonArray();
        for (JsonElement e : contratos) {
            JsonObject contratoParams = new JsonObject();
            contratoParams.add("p_Contrato", e);
            contratoParams.addProperty("p_IdEmpresa", params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L);
            JsonObject resultado = modificarCSC.execute(contratoParams);
            resultados.add(resultado);
        }
        out.add("P_OUT_RESULTADO", resultados);
        return out;
    }
}
