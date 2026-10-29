package cl.bcs.tesoreria.familia_b.support.colaboradores;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Port of the oracle fake of CrearOrdenes. Script {@code colaboradores.crearOrdenes}: {"mensajes":[..], "idOrdenBase": n (8000; one result per order, IdOrden = base + index),
 * "lanza": "text"}.
 */
public class CrearOrdenesGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "crearOrdenes";
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
        long base = script.has("idOrdenBase") ? script.get("idOrdenBase").getAsLong() : 8000L;
        JsonArray resultados = new JsonArray();
        long i = 0;
        for (JsonElement e : entrada.getAsJsonArray("p_Ordenes")) {
            JsonObject r = new JsonObject();
            r.add("IdInterno", e.getAsJsonObject().get("IdInterno"));
            r.addProperty("IdOrden", base + (i++));
            resultados.add(r);
        }
        out.add("P_OUT_RESULTADO", resultados);
        out.addProperty("P_OUT_ERRORESCONTADOR", Guiones.contarErrores(mensajes));
        return out;
    }
}
