package cl.bcs.canalagente.caracterizacion;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Readers of the dump of a run ({@link CasoCanal#ejecutar}) for the rule tests: the SQL sent, the rows of the work table, the exception, the collaborator calls, the movements. */
final class VolcadoAyuda {
    static final String PROC = "BSCL.CanalAgente.BD::ConsultarHistorialMovimientosCuentaInversion_Proceso";

    private VolcadoAyuda() {
    }

    /** the dump of the run of the case whose id contains every fragment */
    static JsonObject run(String... fragmentos) throws Exception {
        return CasoCanal.corrida(CasoCanal.id(fragmentos));
    }

    static JsonArray filas(JsonObject d, String tabla) {
        return d.getAsJsonObject("tablas").getAsJsonArray(tabla);
    }

    static JsonArray filasIniciales(JsonObject d, String tabla) {
        return d.getAsJsonObject("tablasIniciales").getAsJsonArray(tabla);
    }

    /** the kind of every sentence in order: query / batch / exec */
    static List<String> tipos(JsonObject d) {
        List<String> l = new ArrayList<>();
        for (JsonElement s : d.getAsJsonArray("sentencias")) l.add(s.getAsJsonObject().get("tipo").getAsString());
        return l;
    }

    static JsonObject sentencia(JsonObject d, int i) {
        return d.getAsJsonArray("sentencias").get(i).getAsJsonObject();
    }

    static String sql(JsonObject d, int i) {
        return sentencia(d, i).get("sql").getAsString();
    }

    static int sentencias(JsonObject d) {
        return d.getAsJsonArray("sentencias").size();
    }

    static String excepcion(JsonObject d) {
        return d.has("excepcion") ? d.get("excepcion").getAsString().split(":")[0] : null;
    }

    static JsonObject salida(JsonObject d) {
        return d.getAsJsonObject("salida");
    }

    static JsonArray movimientos(JsonObject d) {
        return salida(d).getAsJsonArray("P_OUT_RESULTADO");
    }

    static JsonObject movimiento(JsonObject d, int i) {
        return movimientos(d).get(i).getAsJsonObject();
    }

    /** the argument (entrada JSON) of the n-th call to the cartola */
    static JsonObject entradaCartola(JsonObject d, int n) {
        return d.getAsJsonArray("colaboradores").get(n).getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsJsonObject();
    }

    /** a field of a movement as text (null when JSON null) */
    static String t(JsonObject m, String campo) {
        JsonElement v = m.get(campo);
        return v == null || v.isJsonNull() ? null : v.getAsString();
    }
}
