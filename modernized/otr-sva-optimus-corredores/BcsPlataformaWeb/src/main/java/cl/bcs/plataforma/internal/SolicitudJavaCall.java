package cl.bcs.plataforma.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Parsed body of {@code POST /internal/java-call}: {@code {"class": "...", "params": {...}}} plus the optional
 * {@code skip}, {@code take} (forwarded to the owner) and {@code idServicio} (PLA.Servicio id, only for the TomEE leg).
 * The params are kept exactly as the job sent them (no identity override: the job is trusted).
 */
public record SolicitudJavaCall(String clase, JsonObject params, int skip, int take, Long idServicio) {

    /** Fully qualified Java class name (optionally with a method suffix such as {@code .logica}); rejects SQL/procedure names. */
    private static final Pattern FQN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_$]*(\\.[A-Za-z_][A-Za-z0-9_$]*)+$");
    static final int MAX_CLASE = 200;

    /** Raised for a malformed or unacceptable request: {@code status} is the HTTP status, {@code mensaje} a fixed text. */
    public static final class Invalida extends RuntimeException {
        private final int status;

        public Invalida(int status, String mensaje) {
            super(mensaje, null, false, false);
            this.status = status;
        }

        public int status() {
            return status;
        }
    }

    public static SolicitudJavaCall parsear(byte[] cuerpo) {
        JsonObject o;
        try {
            JsonElement e = JsonParser.parseString(new String(cuerpo, StandardCharsets.UTF_8));
            if (!e.isJsonObject()) throw new Invalida(400, "Invalid JSON body");
            o = e.getAsJsonObject();
        } catch (RuntimeException ex) {
            if (ex instanceof Invalida i) throw i;
            throw new Invalida(400, "Invalid JSON body");
        }
        JsonElement c = o.get("class");
        if (c == null || !c.isJsonPrimitive() || !c.getAsJsonPrimitive().isString() || c.getAsString().isBlank()) {
            throw new Invalida(400, "'class' must be a non-empty string");
        }
        // registry form 'PLUGIN_X.jar@cl.bcs.pkg.Clase': the plugin prefix is not part of the class name
        String clase = c.getAsString().trim().replaceFirst("^[A-Za-z0-9_.\\-]+@", "");
        if (clase.length() > MAX_CLASE) throw new Invalida(400, "'class' too long");
        JsonElement tipo = o.get("tipoServicio");
        boolean noJava = tipo != null && !(tipo.isJsonPrimitive() && "J".equals(tipo.getAsString()));
        if (noJava || !FQN.matcher(clase).matches()) {
            throw new Invalida(422, "type not routable by this endpoint: only JavaCall (type J) classes");
        }
        JsonElement p = o.get("params");
        JsonObject params = new JsonObject();
        if (p != null && !p.isJsonNull()) {
            if (!p.isJsonObject()) throw new Invalida(400, "'params' must be an object");
            params = p.getAsJsonObject();
        }
        return new SolicitudJavaCall(clase, params, entero(o, "skip"), entero(o, "take"), largo(o, "idServicio"));
    }

    private static int entero(JsonObject o, String k) {
        Long v = largo(o, k);
        if (v == null) return 0;
        if (v < 0 || v > Integer.MAX_VALUE) throw new Invalida(400, "'" + k + "' out of range");
        return v.intValue();
    }

    private static Long largo(JsonObject o, String k) {
        JsonElement e = o.get(k);
        if (e == null || e.isJsonNull()) return null;
        try {
            if (!e.isJsonPrimitive()) throw new NumberFormatException();
            return e.getAsLong();
        } catch (NumberFormatException ex) {
            throw new Invalida(400, "'" + k + "' must be an integer");
        }
    }

    /** Body sent to an owner's {@code /internal/java-call}: class, params untouched, skip/take only when present. */
    public String cuerpoParaDueno() {
        JsonObject b = new JsonObject();
        b.addProperty("class", clase);
        if (skip > 0) b.addProperty("skip", skip);
        if (take > 0) b.addProperty("take", take);
        b.add("params", params);
        return b.toString();
    }
}
