package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CalcularCostoUFAportesService {

    private static final Logger log = LoggerFactory.getLogger(CalcularCostoUFAportesService.class);

    public JsonObject execute(JsonObject params) {
        log.info("CalcularCostoUFAportes");
        // Currency conversion (Moneda→UF) requires cross-service call to BcsPlataformaServicios:CalcularParidad.
        // Returns input calculos with zero CostoUF/ValorUF as placeholder until integration is wired.
        JsonArray calculos = params.has("p_Calculos") ? params.getAsJsonArray("p_Calculos") : new JsonArray();
        JsonArray resultado = new JsonArray();
        for (JsonElement e : calculos) {
            JsonObject c = e.getAsJsonObject();
            JsonObject res = new JsonObject();
            if (c.has("IdInterno")) res.add("IdInterno", c.get("IdInterno"));
            res.addProperty("CostoUF", 0);
            res.addProperty("ValorUF", 0);
            resultado.add(res);
        }
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        out.add("P_OUT_RESULTADO", resultado);
        return out;
    }
}
