package cl.bcs.tesoreria.familia_b.support.colaboradores;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Port of the oracle fake of CrearEventoAutorizacionMasivo: one P_OUT_IDEVENTO per P_TABLAEVENTOS row (IdEvento = 7000 + index, NumeroOperacionConcepto copied);
 * script {@code colaboradores.crearEventoAutorizacion}: {"advertencias":[{Codigo,Descripcion}], "errores":[{Codigo,Descripcion}], "lanza": "text"}.
 */
public class CrearEventoAutorizacionMasivoGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "crearEventoAutorizacion";
    }

    @Override
    public JsonObject responder(JsonObject entrada, JsonObject guiones) throws Exception {
        JsonObject script = Guiones.objeto(guiones, clave());
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0L);
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        JsonArray ids = new JsonArray();
        long n = 7000;
        if (entrada.has("P_TABLAEVENTOS") && entrada.get("P_TABLAEVENTOS").isJsonArray()) {
            for (JsonElement e : entrada.getAsJsonArray("P_TABLAEVENTOS")) {
                JsonObject id = new JsonObject();
                id.addProperty("IdEvento", n++);
                JsonElement num = e.getAsJsonObject().get("NumeroOperacionConcepto");
                if (num != null && !num.isJsonNull()) {
                    id.add("NumeroOperacionConcepto", num);
                }
                ids.add(id);
            }
        }
        out.add("P_OUT_IDEVENTO", ids);
        if (script.has("lanza")) {
            throw new Exception(script.get("lanza").getAsString());
        }
        out.add("P_OUT_ADVERTENCIASDETALLE", detalle(script, "advertencias"));
        out.add("P_OUT_ERRORESDETALLE", detalle(script, "errores"));
        return out;
    }

    private static JsonArray detalle(JsonObject script, String clave) {
        JsonArray arr = new JsonArray();
        if (script.has(clave)) {
            for (JsonElement x : script.getAsJsonArray(clave)) {
                JsonObject a = new JsonObject();
                a.addProperty("Codigo", x.getAsJsonObject().get("Codigo").getAsString());
                a.addProperty("Descripcion", x.getAsJsonObject().get("Descripcion").getAsString());
                arr.add(a);
            }
        }
        return arr;
    }
}
