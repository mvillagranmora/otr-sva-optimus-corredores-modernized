package cl.bcs.tesoreria.familia_b.support.colaboradores;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Port of the oracle fake of CrearSolicitudAporteRescateNormal. Script {@code colaboradores.crearSolicitudAporteRescate}: {"mensajes":[..], "idSolicitudBase": n (7000;
 * one result per requested solicitud, IdSolicitudAporteRescate = base + index), "tipoMovimiento": "APONORM", "sinResultados": true, "errores": n (default: number of
 * ERROR messages), "lanza": "text"}.
 */
public class CrearSolicitudAporteRescateNormalGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "crearSolicitudAporteRescate";
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
        long base = script.has("idSolicitudBase") ? script.get("idSolicitudBase").getAsLong() : 7000L;
        String tipo = script.has("tipoMovimiento") ? script.get("tipoMovimiento").getAsString() : "APONORM";
        JsonArray resultados = new JsonArray();
        if (!script.has("sinResultados")) {
            long i = 0;
            for (JsonElement e : entrada.getAsJsonArray("p_SolicitudesAporteRescate")) {
                JsonObject r = new JsonObject();
                r.add("IdInterno", e.getAsJsonObject().get("IdInterno"));
                r.addProperty("IdSolicitudAporteRescate", base + (i++));
                r.addProperty("IdTipoMovimientoFondoMutuo", tipo);
                resultados.add(r);
            }
        }
        out.add("P_OUT_RESULTADO", resultados);
        out.addProperty("P_OUT_ERRORESCONTADOR", script.has("errores") ? script.get("errores").getAsLong() : Guiones.contarErrores(mensajes));
        return out;
    }
}
