package cl.bcs.tesoreria.familia_b.support.colaboradores;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/** Port of the oracle fake of AgregarLog: records the call and answers P_OUT_ERRORESCONTADOR = 0. */
public class AgregarLogGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "agregarLog";
    }

    @Override
    public JsonObject responder(JsonObject entrada, JsonObject guiones) {
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        return out;
    }
}
