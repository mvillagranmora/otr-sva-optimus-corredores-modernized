package cl.bcs.tesoreria.familia_b.support.colaboradores;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/** Port of the oracle fake of AgregarPrograma: answers {"idPrograma": n (default 9001)} from {@code colaboradores.agregarPrograma}; "lanza": "text". */
public class AgregarProgramaGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "agregarPrograma";
    }

    @Override
    public JsonObject responder(JsonObject entrada, JsonObject guiones) throws Exception {
        JsonObject script = Guiones.objeto(guiones, clave());
        if (script.has("lanza")) {
            throw new Exception(script.get("lanza").getAsString());
        }
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_IDPROGRAMA", script.has("idPrograma") ? script.get("idPrograma").getAsLong() : 9001L);
        return out;
    }
}
