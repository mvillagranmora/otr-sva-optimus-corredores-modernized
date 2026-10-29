package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidaMontoInversionService {

    private static final Logger log = LoggerFactory.getLogger(ValidaMontoInversionService.class);

    public JsonObject execute(JsonObject params) {
        log.info("ValidaMontoInversion");

        BigDecimal montoMinTotalInversion = params.has("p_MontoMinimoTotalInversion") ? params.get("p_MontoMinimoTotalInversion").getAsBigDecimal() : BigDecimal.ZERO;
        BigDecimal montoMaxTotalInversion = params.has("p_MontoMaximoTotalInversion") ? params.get("p_MontoMaximoTotalInversion").getAsBigDecimal() : BigDecimal.ZERO;
        BigDecimal montoMinAportesAltos = params.has("p_MontoMinimoAportesAltos") ? params.get("p_MontoMinimoAportesAltos").getAsBigDecimal() : BigDecimal.ZERO;
        BigDecimal montoMaxAportesAltos = params.has("p_MontoMaximoAportesAltos") ? params.get("p_MontoMaximoAportesAltos").getAsBigDecimal() : BigDecimal.ZERO;
        JsonArray solicitudes = params.has("p_Solicitudes") ? params.getAsJsonArray("p_Solicitudes") : new JsonArray();

        List<JsonObject> errores = new ArrayList<>();
        BigDecimal montoTotal = BigDecimal.ZERO;
        for (JsonElement e : solicitudes) {
            JsonObject s = e.getAsJsonObject();
            BigDecimal monto = s.has("Monto") ? s.get("Monto").getAsBigDecimal() : BigDecimal.ZERO;
            montoTotal = montoTotal.add(monto);
        }

        if (montoMinTotalInversion.compareTo(BigDecimal.ZERO) > 0 && montoTotal.compareTo(montoMinTotalInversion) < 0) {
            JsonObject err = new JsonObject();
            err.addProperty("CodigoMensaje", "MONTO_TOTAL_MENOR_MINIMO");
            errores.add(err);
        }
        if (montoMaxTotalInversion.compareTo(BigDecimal.ZERO) > 0 && montoTotal.compareTo(montoMaxTotalInversion) > 0) {
            JsonObject err = new JsonObject();
            err.addProperty("CodigoMensaje", "MONTO_TOTAL_MAYOR_MAXIMO");
            errores.add(err);
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", errores.size());
        JsonArray arrMensajes = new JsonArray();
        for (JsonObject err : errores) arrMensajes.add(err);
        out.add("P_OUT_MENSAJES", arrMensajes);
        return out;
    }
}
