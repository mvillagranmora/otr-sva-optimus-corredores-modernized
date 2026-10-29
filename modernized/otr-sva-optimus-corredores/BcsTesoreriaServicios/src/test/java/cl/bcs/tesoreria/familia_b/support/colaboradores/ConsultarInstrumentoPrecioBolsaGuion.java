package cl.bcs.tesoreria.familia_b.support.colaboradores;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Port of the oracle fake of ConsultarInstrumentoPrecioBolsa. Script {@code colaboradores.precioBolsa}: {"instrumentos":[{IdInstrumento, PrecioCierre, IdMoneda (CLP)}],
 * "errores": n (0)}; the instruments are answered with IdInterno 1, 2, ... in script order whatever was requested.
 */
public class ConsultarInstrumentoPrecioBolsaGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "precioBolsa";
    }

    @Override
    public JsonObject responder(JsonObject entrada, JsonObject guiones) {
        JsonObject script = Guiones.objeto(guiones, clave());
        JsonArray instrumentos = new JsonArray();
        if (script.has("instrumentos")) {
            long i = 1;
            for (JsonElement e : script.getAsJsonArray("instrumentos")) {
                JsonObject o = e.getAsJsonObject();
                JsonObject d = new JsonObject();
                d.addProperty("IdInterno", i++);
                d.addProperty("IdInstrumento", o.get("IdInstrumento").getAsLong());
                d.addProperty("PrecioCierre", o.get("PrecioCierre").getAsBigDecimal());
                d.addProperty("IdMoneda", o.has("IdMoneda") ? o.get("IdMoneda").getAsString() : "CLP");
                instrumentos.add(d);
            }
        }
        JsonObject out = new JsonObject();
        out.add("P_OUT_MENSAJES", new JsonArray());
        out.add("P_OUT_INSTRUMENTO", instrumentos);
        out.addProperty("P_OUT_ERRORESCONTADOR", script.has("errores") ? script.get("errores").getAsLong() : 0L);
        return out;
    }
}
