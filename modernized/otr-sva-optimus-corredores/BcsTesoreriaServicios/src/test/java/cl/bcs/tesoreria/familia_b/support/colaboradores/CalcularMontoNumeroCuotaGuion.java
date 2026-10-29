package cl.bcs.tesoreria.familia_b.support.colaboradores;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Port of the oracle fake of CalcularMontoNumeroCuota. Script {@code colaboradores.calcularMontoNumeroCuota}: {"factor": n (1), "cuotas": n (10), "omitir": [IdInterno..]
 * (series left without result), "lanza": "text"}. Every requested series is answered {IdInterno, TipoSolicitud, Monto = Monto x factor, NumeroCuotas = cuotas}.
 */
public class CalcularMontoNumeroCuotaGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "calcularMontoNumeroCuota";
    }

    @Override
    public JsonObject responder(JsonObject entrada, JsonObject guiones) throws Exception {
        JsonObject script = Guiones.objeto(guiones, clave());
        if (script.has("lanza")) {
            throw new Exception(script.get("lanza").getAsString());
        }
        Set<Long> omitir = new HashSet<>();
        if (script.has("omitir")) {
            for (JsonElement e : script.getAsJsonArray("omitir")) {
                omitir.add(e.getAsLong());
            }
        }
        BigDecimal factor = script.has("factor") ? script.get("factor").getAsBigDecimal() : BigDecimal.ONE;
        BigDecimal cuotas = script.has("cuotas") ? script.get("cuotas").getAsBigDecimal() : new BigDecimal("10");
        JsonArray resultados = new JsonArray();
        if (entrada.has("p_Series") && entrada.get("p_Series").isJsonArray()) {
            for (JsonElement e : entrada.getAsJsonArray("p_Series")) {
                JsonObject s = e.getAsJsonObject();
                if (omitir.contains(s.get("IdInterno").getAsLong())) {
                    continue;
                }
                JsonObject r = new JsonObject();
                r.add("IdInterno", s.get("IdInterno"));
                r.add("TipoSolicitud", s.get("TipoSolicitud"));
                r.addProperty("Monto", s.get("Monto").getAsBigDecimal().multiply(factor));
                r.addProperty("NumeroCuotas", cuotas);
                resultados.add(r);
            }
        }
        JsonObject out = new JsonObject();
        out.add("P_OUT_RESULTADO", resultados);
        return out;
    }
}
