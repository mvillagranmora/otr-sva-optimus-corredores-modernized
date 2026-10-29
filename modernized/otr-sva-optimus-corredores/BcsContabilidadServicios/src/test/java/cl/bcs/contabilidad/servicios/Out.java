package cl.bcs.contabilidad.servicios;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Helpers to read the JavaCall output JSON (legacy shape: PlataformaUtils.MensajeSalida = {id,tipo,codigo,descripcion}). */
final class Out {
    private Out() {}

    static JsonObject json(String s) { return JsonParser.parseString(s).getAsJsonObject(); }

    static long errores(JsonObject out) {
        assertThat(out.has("P_OUT_ERRORESCONTADOR")).as("output has P_OUT_ERRORESCONTADOR: %s", out).isTrue();
        return out.get("P_OUT_ERRORESCONTADOR").getAsLong();
    }

    static String str(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }

    static Long lng(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return e == null || e.isJsonNull() ? null : e.getAsLong();
    }

    static BigDecimal dec(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return e == null || e.isJsonNull() ? null : e.getAsBigDecimal();
    }

    static List<JsonObject> mensajes(JsonObject out) {
        assertThat(out.has("P_OUT_MENSAJES") && out.get("P_OUT_MENSAJES").isJsonArray())
                .as("output has array P_OUT_MENSAJES: %s", out).isTrue();
        List<JsonObject> l = new ArrayList<>();
        for (JsonElement e : out.getAsJsonArray("P_OUT_MENSAJES")) l.add(e.getAsJsonObject());
        return l;
    }

    static List<JsonObject> array(JsonObject out, String key) {
        assertThat(out.has(key) && out.get(key).isJsonArray()).as("output has array %s: %s", key, out).isTrue();
        List<JsonObject> l = new ArrayList<>();
        for (JsonElement e : out.getAsJsonArray(key)) l.add(e.getAsJsonObject());
        return l;
    }

    /** Asserts a legacy MensajeSalida: {id, tipo, codigo, descripcion}. */
    static void assertMensaje(JsonObject m, Long id, String tipo, String codigo) {
        assertThat(lng(m, "id")).as("mensaje.id in %s", m).isEqualTo(id);
        assertThat(str(m, "tipo")).as("mensaje.tipo in %s", m).isEqualTo(tipo);
        assertThat(str(m, "codigo")).as("mensaje.codigo in %s", m).isEqualTo(codigo);
        assertThat(m.has("descripcion")).as("mensaje.descripcion present in %s", m).isTrue();
    }

    static List<String> codigos(JsonObject out) {
        return mensajes(out).stream().map(m -> str(m, "codigo")).toList();
    }

    static JsonArray parseArray(String s) { return JsonParser.parseString(s).getAsJsonArray(); }
}
