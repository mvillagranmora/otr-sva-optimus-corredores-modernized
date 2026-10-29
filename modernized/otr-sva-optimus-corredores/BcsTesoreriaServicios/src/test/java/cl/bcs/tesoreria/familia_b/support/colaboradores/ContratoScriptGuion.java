package cl.bcs.tesoreria.familia_b.support.colaboradores;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Shared behaviour of the oracle fakes AnulacionContratoCSC, RevocacionContratoCSC and CambiarEstadoContratosSAAPV: script
 * {"mensajes":[{id,tipo,codigo,descripcion}], "errores": n, "lanza": "text"}; the answer holds the messages and P_OUT_ERRORESCONTADOR = errores (default 0).
 * The SAAPV fake also answers P_OUT_ADVERTENCIASCONTADOR = 0.
 */
abstract class ContratoScriptGuion implements GuionColaborador {

    private final boolean conAdvertencias;

    ContratoScriptGuion(boolean conAdvertencias) {
        this.conAdvertencias = conAdvertencias;
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
        out.addProperty("P_OUT_ERRORESCONTADOR", script.has("errores") ? script.get("errores").getAsLong() : 0L);
        if (conAdvertencias) {
            out.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0L);
        }
        return out;
    }
}
