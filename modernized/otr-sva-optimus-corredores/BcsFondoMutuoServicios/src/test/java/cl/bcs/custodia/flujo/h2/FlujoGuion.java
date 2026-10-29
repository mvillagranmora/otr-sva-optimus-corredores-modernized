package cl.bcs.custodia.flujo.h2;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Script of the collaborators outside the slice, shared by the legacy oracle and the JUnit harness (SINGLE SOURCE, copied by legacy-oracle/sync.py).
 * <ul>
 * <li>ValorizarInstrumento (out of this slice, CUS-FLU-33..35): every request is RECORDED as JSON (the legacy ValorizarInstrumentoEntrada serialised
 *     by the oracle, or the JSON the new code hands to its valuation port) and answered from case.json "colaboradores.ValorizarInstrumento":
 *     {"respuestas":[{"cuando":{"0.p_Instrumentos.0.IdInstrumento":1001},"respuesta":{...ValorizarInstrumentoSalida...}} | {"error":"text"}],
 *     "defecto":{...}}. "cuando" paths start at the argument index (0 = the request). No matching answer = the call FAILS loudly (a case must script
 *     what it reaches); "error" = the collaborator throws java.lang.Exception(text).</li>
 * <li>Sequences (Utils.getSecuencia, CUS-FLU-36): every request (name, quantity) is RECORDED; the values come from the H2 sequence of the same name,
 *     the same sequence the SQL "...".NEXTVAL of the source reads draws from.</li>
 * </ul>
 */
public final class FlujoGuion {

    private final JsonObject config;
    private final JsonArray llamadas = new JsonArray();
    private final JsonArray secuencias = new JsonArray();
    private final List<JsonObject> usadas = new ArrayList<>();

    public FlujoGuion(JsonObject colaboradores) {
        this.config = colaboradores == null ? new JsonObject() : colaboradores;
    }

    public JsonArray llamadas() {
        return llamadas;
    }

    public JsonArray secuencias() {
        return secuencias;
    }

    /** true when some call reached a collaborator without a scripted answer (the golden must not be written) */
    public boolean huboLlamadaSinGuion() {
        for (JsonElement e : llamadas) {
            JsonElement r = e.getAsJsonObject().get("respuesta");
            if (r != null && r.isJsonPrimitive() && "SIN RESPUESTA CONFIGURADA".equals(r.getAsString())) return true;
        }
        return false;
    }

    /** ValorizarInstrumento.logica: request JSON in, ValorizarInstrumentoSalida JSON out (or java.lang.Exception) */
    public JsonObject valorizar(JsonElement solicitud) throws Exception {
        return responder("ValorizarInstrumento", "logica", solicitud);
    }

    public JsonObject responder(String colaborador, String metodo, JsonElement solicitud) throws Exception {
        JsonArray args = new JsonArray();
        args.add(solicitud == null ? com.google.gson.JsonNull.INSTANCE : solicitud.deepCopy());
        JsonObject rec = new JsonObject();
        rec.addProperty("colaborador", colaborador);
        rec.addProperty("metodo", metodo);
        rec.add("argumentos", args);
        llamadas.add(rec);
        JsonObject c = config.has(colaborador) && config.get(colaborador).isJsonObject() ? config.getAsJsonObject(colaborador) : null;
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
                throw new Exception(hit.get("error").getAsString());
            }
            rec.add("respuesta", hit.get("respuesta"));
            return hit.getAsJsonObject("respuesta").deepCopy();
        }
        if (c != null && c.has("defecto")) {
            rec.add("respuesta", c.get("defecto"));
            return c.getAsJsonObject("defecto").deepCopy();
        }
        rec.addProperty("respuesta", "SIN RESPUESTA CONFIGURADA");
        throw new IllegalStateException("scripted collaborator " + colaborador + "." + metodo + ": no answer configured for this call");
    }

    /** Utils.getSecuencia contract: List of `cantidad` values of sequence `nombre` (empty for null or <= 0; then nothing is recorded) */
    public List<Long> secuencia(Connection con, String nombre, Long cantidad) throws SQLException {
        List<Long> salida = new ArrayList<>();
        if (cantidad == null || cantidad <= 0) return salida;
        JsonObject o = new JsonObject();
        o.addProperty("secuencia", nombre);
        o.addProperty("cantidad", cantidad);
        secuencias.add(o);
        for (long k = 0; k < cantidad; k++) {
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT NEXT VALUE FOR \"" + nombre + "\"")) {
                rs.next();
                salida.add(rs.getLong(1));
            }
        }
        return salida;
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
                BigDecimal a = num(cur.getAsString());
                BigDecimal b = num(want.getAsString());
                if (a != null && b != null ? a.compareTo(b) != 0 : !cur.getAsString().equals(want.getAsString())) return false;
            }
        }
        return true;
    }

    private static BigDecimal num(String s) {
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
