package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;

@Service
public class CalcularCostoFactorAjusteService {

    private static final Logger log = LoggerFactory.getLogger(CalcularCostoFactorAjusteService.class);

    public JsonObject execute(JsonObject params) {
        log.info("CalcularCostoFactorAjuste");

        JsonArray datosCartera = params.has("p_DatosCartera") ? params.getAsJsonArray("p_DatosCartera") : new JsonArray();
        JsonArray resultado = new JsonArray();
        int errores = 0;

        for (JsonElement e : datosCartera) {
            JsonObject c = e.getAsJsonObject();
            BigDecimal cantidadRescate = getBD(c, "CantidadRescate");
            BigDecimal cantidadTotal = getBD(c, "CantidadTotalCartera");
            BigDecimal costoFactorTotal = getBD(c, "CostoFactorAjusteTotalCartera");

            if (cantidadRescate.compareTo(BigDecimal.ZERO) > 0
                    && cantidadTotal.compareTo(BigDecimal.ZERO) > 0
                    && costoFactorTotal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal costoRescate = cantidadRescate
                    .multiply(costoFactorTotal, MathContext.DECIMAL128)
                    .divide(cantidadTotal, MathContext.DECIMAL128);
                BigDecimal costoRestante = costoFactorTotal.subtract(costoRescate, MathContext.DECIMAL128);

                JsonObject res = new JsonObject();
                if (c.has("IdInstrumento")) res.add("IdInstrumento", c.get("IdInstrumento"));
                if (c.has("IdSerieFondo")) res.add("IdSerieFondo", c.get("IdSerieFondo"));
                res.addProperty("CantidadRescate", cantidadRescate);
                res.addProperty("CantidadTotalCartera", cantidadTotal);
                res.addProperty("CostoFactorAjusteTotalCartera", costoFactorTotal);
                res.addProperty("CostoFactorAjusteRescate", costoRescate);
                res.addProperty("CostoFactorAjusteRestante", costoRestante);
                resultado.add(res);
            } else {
                errores++;
            }
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", errores);
        out.add("P_OUT_RESULTADO", resultado);
        return out;
    }

    private BigDecimal getBD(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsBigDecimal() : BigDecimal.ZERO;
    }
}
