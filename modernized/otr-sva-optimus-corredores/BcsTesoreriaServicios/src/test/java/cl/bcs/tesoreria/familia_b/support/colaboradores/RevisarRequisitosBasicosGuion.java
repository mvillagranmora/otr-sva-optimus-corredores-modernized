package cl.bcs.tesoreria.familia_b.support.colaboradores;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/** Port of the oracle fake of RevisarRequisitosBasicos: answers the messages listed in {@code colaboradores.requisitosBasicos}. */
public class RevisarRequisitosBasicosGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "requisitosBasicos";
    }

    @Override
    public JsonObject responder(JsonObject entrada, JsonObject guiones) {
        JsonArray mensajes = new JsonArray();
        for (JsonElement e : Guiones.lista(guiones, clave())) {
            mensajes.add(Guiones.mensaje(e.getAsJsonObject()));
        }
        JsonObject out = new JsonObject();
        out.add("p_out_Mensajes", mensajes);
        return out;
    }
}
