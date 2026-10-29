package cl.bcs.fondomutuo.cierre;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

/**
 * The collaborators outside the closure, answered from the SAME configuration the legacy oracle used ({@code seed/doubles-base.json} + the
 * {@code colaboradores} of the case) with the SAME matching rules as the oracle's {@code Doubles} ("cuando" dotted paths from the argument index, first
 * match wins, "una_vez", "error", "defecto"; no answer at all fails loudly) - so a call the new code makes is answered exactly as the legacy call was.
 * Every call is recorded in the oracle's dump format ({@code colaborador, metodo, argumentos, respuesta}).
 */
final class DoblesGuionados implements ParidadPort, PreciosTasasPort, CustodiaPort, EmpresaPort {

    private final JsonObject config;
    private final JsonArray llamadas = new JsonArray();
    private final Set<JsonObject> usadas = new HashSet<>();

    DoblesGuionados(JsonObject config) {
        this.config = config;
    }

    JsonArray llamadas() {
        return llamadas;
    }

    @Override
    public JsonObject calcular(JsonObject entrada) {
        return responder("CalcularParidad", "logica", entrada);
    }

    @Override
    public JsonObject entregar(JsonObject entrada) {
        return responder("EntregarPreciosTasas", "logica", entrada);
    }

    @Override
    public JsonObject consultarFlujo(JsonObject entrada) {
        return responder("ConsultarFlujoCustodia", "logica", entrada);
    }

    @Override
    public InformacionEmpresa informacion(Long idEmpresa) {
        JsonObject r = responder("ConsultarEmpresa", "logica", new JsonPrimitive(idEmpresa));
        return new Gson().fromJson(r, InformacionEmpresa.class);
    }

    private JsonObject responder(String colab, String metodo, JsonElement argumento) {
        JsonArray args = new JsonArray();
        args.add(argumento.deepCopy());
        JsonObject rec = new JsonObject();
        rec.addProperty("colaborador", colab);
        rec.addProperty("metodo", metodo);
        rec.add("argumentos", args);
        llamadas.add(rec);
        JsonObject c = config.has(colab) && config.get(colab).isJsonObject() ? config.getAsJsonObject(colab) : null;
        JsonObject hit = null;
        if (c != null && c.has("respuestas")) {
            for (JsonElement e : c.getAsJsonArray("respuestas")) {
                JsonObject r = e.getAsJsonObject();
                if (r.has("una_vez") && r.get("una_vez").getAsBoolean() && usadas.contains(r)) continue;
                if (coincide(r.has("cuando") ? r.getAsJsonObject("cuando") : null, args)) {
                    hit = r;
                    break;
                }
            }
        }
        if (hit != null) {
            usadas.add(hit);
            if (hit.has("error")) {
                rec.addProperty("respuesta", "error: " + hit.get("error").getAsString());
                throw new IllegalStateException(hit.get("error").getAsString());
            }
            rec.add("respuesta", hit.get("respuesta"));
            return hit.get("respuesta") == null || hit.get("respuesta").isJsonNull() ? null : hit.getAsJsonObject("respuesta").deepCopy();
        }
        if (c != null && c.has("defecto")) {
            rec.add("respuesta", c.get("defecto"));
            return c.getAsJsonObject("defecto").deepCopy();
        }
        rec.addProperty("respuesta", "SIN RESPUESTA CONFIGURADA");
        throw new IllegalStateException("double " + colab + "." + metodo + ": no canned answer configured for this call");
    }

    private static boolean coincide(JsonObject cuando, JsonArray args) {
        if (cuando == null) return true;
        for (String path : cuando.keySet()) {
            JsonElement cur = args;
            for (String p : path.split("\\.")) {
                if (cur == null || cur.isJsonNull()) {
                    cur = null;
                    break;
                }
                if (cur.isJsonArray()) {
                    int i = Integer.parseInt(p);
                    cur = i < cur.getAsJsonArray().size() ? cur.getAsJsonArray().get(i) : null;
                } else if (cur.isJsonObject()) {
                    cur = cur.getAsJsonObject().get(p);
                } else {
                    cur = null;
                }
            }
            JsonElement want = cuando.get(path);
            boolean wantNull = want == null || want.isJsonNull();
            boolean gotNull = cur == null || cur.isJsonNull();
            if (wantNull != gotNull) return false;
            if (!wantNull) {
                if (!(cur instanceof JsonPrimitive) || !(want instanceof JsonPrimitive)) return false;
                BigDecimal a = numero(cur.getAsString());
                BigDecimal b = numero(want.getAsString());
                if (a != null && b != null ? a.compareTo(b) != 0 : !cur.getAsString().equals(want.getAsString())) return false;
            }
        }
        return true;
    }

    private static BigDecimal numero(String s) {
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
