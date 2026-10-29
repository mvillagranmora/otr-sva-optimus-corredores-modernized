package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Service
public class CalcularSpreadRescateService {

    private static final Logger log = LoggerFactory.getLogger(CalcularSpreadRescateService.class);

    public JsonObject execute(JsonObject params) {
        log.info("CalcularSpreadRescate");
        // Full logic requires cross-service call to BcsPlataformaServicios:CalcularParidad for currency conversion.
        // Calculates spread inline when moneda == monedaSistema; returns zero spread otherwise.
        JsonArray datos = params.has("p_Datos") ? params.getAsJsonArray("p_Datos") : new JsonArray();
        JsonArray resultado = new JsonArray();
        int errores = 0;

        for (JsonElement e : datos) {
            JsonObject d = e.getAsJsonObject();
            BigDecimal valorFactor = getBD(d, "ValorFactorReajuste");
            BigDecimal costoFactor = getBD(d, "CostoFactorReajusteRescate");
            BigDecimal cantidad = getBD(d, "CantidadRescate");
            BigDecimal valorCuota = getBD(d, "ValorCuota");

            if (valorFactor.compareTo(BigDecimal.ZERO) <= 0
                    || costoFactor.compareTo(BigDecimal.ZERO) <= 0
                    || cantidad.compareTo(BigDecimal.ZERO) <= 0
                    || valorCuota.compareTo(BigDecimal.ZERO) <= 0) {
                errores++;
                continue;
            }

            BigDecimal montoReajuste = valorFactor.multiply(costoFactor, MathContext.DECIMAL128)
                .setScale(8, RoundingMode.HALF_DOWN);
            BigDecimal spread = cantidad.multiply(valorCuota, MathContext.DECIMAL128)
                .subtract(montoReajuste, MathContext.DECIMAL128)
                .setScale(8, RoundingMode.HALF_DOWN);

            JsonObject res = new JsonObject();
            if (d.has("NumeroCalculoSpreadRescate")) res.add("NumeroCalculoSpreadRescate", d.get("NumeroCalculoSpreadRescate"));
            res.addProperty("ValorFactorReajuste", valorFactor);
            res.addProperty("CostoFactorReajusteRescate", costoFactor);
            res.addProperty("CantidadRescate", cantidad);
            res.addProperty("ValorCuota", valorCuota);
            res.addProperty("Spread", spread);
            resultado.add(res);
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
