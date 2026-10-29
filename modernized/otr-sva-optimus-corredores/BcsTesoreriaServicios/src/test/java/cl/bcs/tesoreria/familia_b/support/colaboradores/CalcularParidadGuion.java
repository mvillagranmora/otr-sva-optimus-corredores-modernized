package cl.bcs.tesoreria.familia_b.support.colaboradores;

import java.math.BigDecimal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Port of the oracle fake of CalcularParidad: for every requested calculation answers {IdInterno, MontoDestino = MontoOrigen x factor}, factor being the number
 * {@code colaboradores.factorParidad} (default 1). Every other field keeps the legacy constructor default (FactorOrigenDestino = 0).
 */
public class CalcularParidadGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "factorParidad";
    }

    @Override
    public JsonObject responder(JsonObject entrada, JsonObject guiones) {
        BigDecimal factor = guiones.has(clave()) ? guiones.get(clave()).getAsBigDecimal() : BigDecimal.ONE;
        JsonArray calculos = new JsonArray();
        for (JsonElement e : entrada.getAsJsonArray("p_CalculosEntrada")) {
            JsonObject c = e.getAsJsonObject();
            JsonObject s = new JsonObject();
            s.add("IdInterno", c.get("IdInterno"));
            s.addProperty("MontoDestino", c.get("MontoOrigen").getAsBigDecimal().multiply(factor));
            calculos.add(s);
        }
        JsonObject out = new JsonObject();
        out.add("p_out_CalculosSalida", calculos);
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        return out;
    }
}
