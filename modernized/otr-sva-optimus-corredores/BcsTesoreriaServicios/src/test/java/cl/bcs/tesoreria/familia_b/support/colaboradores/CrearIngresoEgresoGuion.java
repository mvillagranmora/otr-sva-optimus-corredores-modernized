package cl.bcs.tesoreria.familia_b.support.colaboradores;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Port of the oracle fake of CrearIngresoEgreso. Script {@code colaboradores.crearIngresoEgreso}: {"mensajes":[..], "idIngresoEgresoBase": n (9000),
 * "omitirReferencias": [NumeroReferencia..], "sinMascara": true (keep the real NumeroReferencia in the trace), "resultados": [{Campo,Id,Servicio,Resultado,
 * NumeroReferencia}], "lanza": "text"}. Every requested movement is answered {NumeroReferencia (echoed), IdIngresoEgreso = base + index, IdTipoMovimiento}.
 */
public class CrearIngresoEgresoGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "crearIngresoEgreso";
    }

    @Override
    public JsonElement paraTraza(JsonObject entrada, JsonObject guiones) {
        return Guiones.objeto(guiones, clave()).has("sinMascara") ? entrada : Guiones.enmascararReferencias(entrada);
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
        long base = script.has("idIngresoEgresoBase") ? script.get("idIngresoEgresoBase").getAsLong() : 9000L;
        Set<Long> omitir = new HashSet<>();
        if (script.has("omitirReferencias")) {
            for (JsonElement e : script.getAsJsonArray("omitirReferencias")) {
                omitir.add(e.getAsLong());
            }
        }
        JsonArray generados = new JsonArray();
        long i = 0;
        JsonArray pedidos = entrada.has("p_IngresoEgresoGenerar") && entrada.get("p_IngresoEgresoGenerar").isJsonArray()
                ? entrada.getAsJsonArray("p_IngresoEgresoGenerar") : new JsonArray();
        for (JsonElement el : pedidos) {
            JsonObject ie = el.getAsJsonObject();
            JsonElement ref = ie.get("NumeroReferencia");
            if (ref != null && !ref.isJsonNull() && omitir.contains(ref.getAsLong())) {
                i++;
                continue;
            }
            JsonObject g = new JsonObject();
            g.add("NumeroReferencia", ref == null ? com.google.gson.JsonNull.INSTANCE : ref);
            g.addProperty("IdIngresoEgreso", base + (i++));
            g.add("IdTipoMovimiento", ie.has("IdTipoMovimiento") ? ie.get("IdTipoMovimiento") : com.google.gson.JsonNull.INSTANCE);
            generados.add(g);
        }
        out.add("P_OUT_INGRESOEGRESOGENERADO", generados);
        JsonArray resultados = new JsonArray();
        if (script.has("resultados")) {
            for (JsonElement e : script.getAsJsonArray("resultados")) {
                JsonObject o = e.getAsJsonObject();
                JsonObject r = new JsonObject();
                r.addProperty("Campo", o.get("Campo").getAsString());
                r.addProperty("Id", o.get("Id").getAsLong());
                r.add("Servicio", o.has("Servicio") ? o.get("Servicio") : com.google.gson.JsonNull.INSTANCE);
                r.addProperty("Resultado", o.get("Resultado").getAsString());
                r.addProperty("NumeroReferencia", o.has("NumeroReferencia") ? o.get("NumeroReferencia").getAsLong() : 0L);
                resultados.add(r);
            }
        }
        out.add("P_OUT_RESULTADOPROCESO", resultados);
        out.addProperty("P_OUT_ERRORESCONTADOR", Guiones.contarErrores(mensajes));
        return out;
    }
}
