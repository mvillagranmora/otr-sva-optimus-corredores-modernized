package cl.bcs.tesoreria.familia_b.support.colaboradores;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Port of the oracle fake of LiquidarComprobante. Script {@code colaboradores.liquidarComprobante}: {"mensajes":[..], "resultados":[{Campo,Id,Servicio,Resultado}],
 * "lanza": "text"}.
 */
public class LiquidarComprobanteGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "liquidarComprobante";
    }

    @Override
    public JsonObject responder(JsonObject entrada, JsonObject guiones) throws Exception {
        JsonObject script = Guiones.objeto(guiones, clave());
        if (script.has("lanza")) {
            throw new Exception(script.get("lanza").getAsString());
        }
        JsonObject out = new JsonObject();
        JsonArray mensajes = Guiones.mensajes(script);
        out.add("P_OUT_MENSAJES", mensajes);
        JsonArray resultados = new JsonArray();
        if (script.has("resultados")) {
            for (JsonElement e : script.getAsJsonArray("resultados")) {
                JsonObject o = e.getAsJsonObject();
                JsonObject r = new JsonObject();
                r.addProperty("Campo", o.get("Campo").getAsString());
                r.addProperty("Id", o.get("Id").getAsLong());
                r.add("Servicio", o.has("Servicio") ? o.get("Servicio") : com.google.gson.JsonNull.INSTANCE);
                r.addProperty("Resultado", o.get("Resultado").getAsString());
                resultados.add(r);
            }
        }
        out.add("P_OUT_RESULTADOPROCESO", resultados);
        out.addProperty("P_OUT_ERRORESCONTADOR", Guiones.contarErrores(mensajes));
        return out;
    }
}
