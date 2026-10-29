package cl.bcs.tesoreria.familia_b.support.colaboradores;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Port of the oracle fake of AnularCargoAbono. Script {@code colaboradores.anularCargoAbono}: {"mensajes":[..], "errores": n (default: number of ERROR messages),
 * "lanza": "text"}.
 */
public class AnularCargoAbonoGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "anularCargoAbono";
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
        out.addProperty("P_OUT_ERRORESCONTADOR", script.has("errores") ? script.get("errores").getAsLong() : Guiones.contarErrores(mensajes));
        return out;
    }
}
