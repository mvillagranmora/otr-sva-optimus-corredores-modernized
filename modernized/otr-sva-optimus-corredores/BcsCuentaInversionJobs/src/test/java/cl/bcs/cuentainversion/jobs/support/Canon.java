package cl.bcs.cuentainversion.jobs.support;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Canonical JSON text used to compare the legacy goldens with the modern output (and written by the dump hook).
 * Object keys are sorted; keys whose value is JSON null are dropped (the legacy Gson uses serializeNulls, consumers treat an
 * absent key and a null alike); numbers are compared by value; the wall-clock file-name part is masked; arrays keep their order.
 * Pure JDK + gson: it is also compiled by the oracle runner.
 */
public final class Canon {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private Canon() {
    }

    public static JsonElement canon(JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return JsonNull.INSTANCE;
        }
        if (e.isJsonPrimitive()) {
            JsonPrimitive p = e.getAsJsonPrimitive();
            if (p.isNumber()) {
                BigDecimal bd = new BigDecimal(p.getAsString()).stripTrailingZeros();
                return new JsonPrimitive(new BigDecimal(bd.toPlainString()));
            }
            if (p.isString()) {
                return new JsonPrimitive(Archivos.mascara(p.getAsString()));
            }
            return p;
        }
        if (e.isJsonArray()) {
            List<JsonElement> items = new ArrayList<>();
            for (JsonElement x : e.getAsJsonArray()) {
                items.add(canon(x));
            }
            JsonArray a = new JsonArray();
            items.forEach(a::add);
            return a;
        }
        TreeMap<String, JsonElement> sorted = new TreeMap<>();
        for (Map.Entry<String, JsonElement> en : e.getAsJsonObject().entrySet()) {
            JsonElement v = canon(en.getValue());
            if (!v.isJsonNull()) {
                sorted.put(en.getKey(), v);
            }
        }
        JsonObject o = new JsonObject();
        sorted.forEach(o::add);
        return o;
    }

    public static String texto(JsonElement e) {
        return PRETTY.toJson(canon(e));
    }

    /**
     * An exception message made reproducible: the temporary base path is {@code ${RUTA_BASE}}, JVM identity hashes are {@code @<hash>},
     * and the helpful text of a NullPointerException (it names local variables of the code that threw it) is {@code <npe>}.
     * The same function runs on the golden side and on the modern side.
     */
    public static String mensaje(String m, java.nio.file.Path base) {
        if (m == null) {
            return null;
        }
        String s = Archivos.enmascararRuta(m, base);
        // an IOException text names the OS error in the platform language and uses the platform separator: normalised
        s = s.replaceAll("\\((The system cannot find the (path|file) specified|No such file or directory|Access is denied|Permission denied)\\)", "(<os-error>)");
        s = s.replaceAll("(\\$\\{RUTA_BASE\\}[^ ]*)", "$1").replace("\\", "/");
        s = s.replaceAll("@[0-9a-f]{4,8}\\b", "@<hash>");
        s = s.replaceAll("NullPointerException(: .*)?", "NullPointerException: <npe>");
        // the status message of a failed run is the bare helpful text of a NullPointerException ("Cannot invoke ... because ...")
        return s.replaceAll("^Cannot (invoke|read|load from|store to|assign|throw|enter|exit|read the array length).*$", "<npe>");
    }

    /** Message of an exception as compared: a top-level NullPointerException is {@code <npe>} whatever its helpful text. */
    public static String mensajeDe(Throwable t, java.nio.file.Path base) {
        return t instanceof NullPointerException ? "<npe>" : mensaje(t.getMessage(), base);
    }

    /** Placeholder of the random internal file name the PAUTA jobs generate (30 distinct letters / digits, legacy generarNombreRandom). */
    public static final String NOMBRE_ALEATORIO = "<RANDOM30>";

    /**
     * Mask of the only non-deterministic payload of the PAUTA jobs: the {@code Valor} of the parameter {@code NombreInternoArchivo} is a
     * random string of 30 DISTINCT letters / digits. It becomes {@link #NOMBRE_ALEATORIO} only when it has exactly that shape, so a wrong
     * length or a repeated character stays visible. Applied by the oracle and by the modern collaborators when they trace a call.
     */
    public static JsonElement enmascararAleatorios(JsonElement e) {
        if (e == null || e.isJsonNull() || e.isJsonPrimitive()) {
            return e;
        }
        if (e.isJsonArray()) {
            e.getAsJsonArray().forEach(Canon::enmascararAleatorios);
            return e;
        }
        JsonObject o = e.getAsJsonObject();
        JsonElement nombre = o.get("NombreParametro");
        JsonElement valor = o.get("Valor");
        if (nombre != null && nombre.isJsonPrimitive() && "NombreInternoArchivo".equals(nombre.getAsString())
                && valor != null && valor.isJsonPrimitive() && valor.getAsString().matches("[A-Za-z0-9]{30}")
                && valor.getAsString().chars().distinct().count() == 30) {
            o.addProperty("Valor", NOMBRE_ALEATORIO);
        }
        for (Map.Entry<String, JsonElement> en : o.entrySet()) {
            enmascararAleatorios(en.getValue());
        }
        return e;
    }

    public static JsonElement parse(String json) {
        return JsonParser.parseString(json);
    }
}
