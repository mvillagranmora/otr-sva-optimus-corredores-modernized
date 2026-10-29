package cl.bcs.tesoreria.familia_b.support.colaboradores;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Port of the oracle fake of CrearCargoAbono. Script {@code colaboradores.crearCargoAbono}: {"mensajes":[..], "idCargoAbonoMovimientoBase": n (6000; every requested
 * NumeroCargoAbono is answered with base + index), "sinGenerados": true, "errores": n (default: number of ERROR messages), "lanza": "text"}.
 */
public class CrearCargoAbonoGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "crearCargoAbono";
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
        long base = script.has("idCargoAbonoMovimientoBase") ? script.get("idCargoAbonoMovimientoBase").getAsLong() : 6000L;
        JsonArray generados = new JsonArray();
        if (!script.has("sinGenerados")) {
            long i = 0;
            for (JsonElement e : entrada.getAsJsonArray("p_CargoAbonoAGenerar")) {
                JsonObject g = e.getAsJsonObject();
                JsonObject o = new JsonObject();
                o.add("NumeroCargoAbono", g.get("NumeroCargoAbono"));
                o.addProperty("IdCargoAbonoMovimiento", base + (i++));
                o.add("IdTipoMovimiento", g.get("IdTipoMovimiento"));
                generados.add(o);
            }
        }
        out.add("P_OUT_CARGOABONOGENERADO", generados);
        out.addProperty("P_OUT_ERRORESCONTADOR", script.has("errores") ? script.get("errores").getAsLong() : Guiones.contarErrores(mensajes));
        return out;
    }
}
