package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CalcularValoresService {

    private static final Logger log = LoggerFactory.getLogger(CalcularValoresService.class);

    public JsonObject execute(JsonObject params) {
        log.info("CalcularValores");
        // Passes through input custodia lists; UF/UTM factor adjustment requires
        // cross-service call to BcsPlataformaServicios:CalcularParidad (p_Paridades map).
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        if (params.has("p_IngresoCustodia")) out.add("p_IngresoCustodia", params.get("p_IngresoCustodia"));
        if (params.has("p_RetiroCustodia")) out.add("p_RetiroCustodia", params.get("p_RetiroCustodia"));
        return out;
    }
}
