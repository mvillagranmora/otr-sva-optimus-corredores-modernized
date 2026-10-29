package cl.bcs.utils.equivalencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Golden fixtures recorded from the unmodified legacy BcsUtils (see {@code src/test/resources/golden/PROVENANCE.md}).
 *
 * <p>A missing or empty fixture is a FAILURE (never a skip): {@link #casos} throws {@link AssertionError}.
 * Outcomes are compared in the oracle's own shape: {@code {"value": ...}} or {@code {"throws": "<exception class>"}}
 * (exception MESSAGES are never compared), dates as {@link #d(Date)}.
 */
public final class Golden {

    public static final Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    private static final Map<String, AtomicInteger> EJECUTADOS = new ConcurrentHashMap<>();

    /** One recorded case. */
    public record Caso(String id, String regla, JsonObject input, JsonObject out) {
        public String s(String k) {
            JsonElement e = input.get(k);
            return e == null || e.isJsonNull() ? null : e.isJsonPrimitive() ? e.getAsString() : e.toString();
        }

        public boolean es(String k, String v) {
            return Objects.equals(s(k), v);
        }

        public long l(String k) {
            return input.get(k).getAsLong();
        }

        public boolean tiene(String k) {
            return input.has(k);
        }

        public JsonElement valor() {
            assertThat(out.has("value")).as(id + " records a value, not an exception: " + out).isTrue();
            return out.get("value");
        }

        public String nombre() {
            String in = input.toString();
            return regla + " " + id + " " + (in.length() > 140 ? in.substring(0, 140) + "…" : in);
        }
    }

    /** All cases of {@code golden/<dir>/<area>.json}; fails when absent or empty. */
    public static List<Caso> casos(String dir, String area) {
        String r = "golden/" + dir + "/" + area + ".json";
        try (InputStream is = Golden.class.getClassLoader().getResourceAsStream(r)) {
            if (is == null) throw new AssertionError("golden fixture missing: " + r + " (a missing oracle is a failure, not a skip)");
            JsonObject doc = JsonParser.parseString(new String(is.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray a = doc.getAsJsonArray("cases");
            if (a == null || a.isEmpty()) throw new AssertionError("golden fixture has no cases: " + r);
            List<Caso> l = new ArrayList<>();
            for (JsonElement e : a) {
                JsonObject c = e.getAsJsonObject();
                l.add(new Caso(c.get("id").getAsString(), c.get("rule").getAsString(), c.getAsJsonObject("input"), c.getAsJsonObject("out")));
            }
            return l;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Runs {@code body} and renders the outcome exactly as the oracle did. */
    public static JsonObject run(Callable<Object> body) {
        Map<String, Object> r = new LinkedHashMap<>();
        try {
            Object v = body.call();
            if (v instanceof String s && !bienFormado(s)) {
                List<String> u = new ArrayList<>();
                for (char ch : s.toCharArray()) u.add(String.format("%04x", (int) ch));
                r.put("utf16", u);
            } else {
                r.put("value", v);
            }
        } catch (Throwable t) {
            Throwable e = t instanceof InvocationTargetException && t.getCause() != null ? t.getCause() : t;
            r.put("throws", e.getClass().getName());
        }
        return GSON.toJsonTree(r).getAsJsonObject();
    }

    /** Asserts the modern outcome equals the legacy record and counts the case as executed. */
    public static void igual(String area, Caso c, Callable<Object> body) {
        comparar(area, c, c.out(), run(body));
    }

    /**
     * Single comparison point. With {@code -Dequivalence.out=<dir>} it also writes both sides per case
     * ({@code <dir>/legacy|new/<zone>/<area>/<id>.json}) for the dual-execution registry; off by default.
     */
    private static void comparar(String area, Caso c, JsonObject esperado, JsonObject actual) {
        String dir = System.getProperty("equivalence.out");
        if (dir != null && !dir.isBlank()) {
            String zona = TimeZone.getDefault().getID().replace('/', '_');
            try {
                for (String lado : new String[]{"legacy", "new"}) {
                    java.nio.file.Path f = java.nio.file.Path.of(dir, lado, zona, area, c.id() + ".json");
                    java.nio.file.Files.createDirectories(f.getParent());
                    java.nio.file.Files.writeString(f, GSON.toJson(lado.equals("legacy") ? esperado : actual) + System.lineSeparator(), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        assertThat(actual).as(c.id() + " (" + c.regla() + ") input " + c.input()).isEqualTo(esperado);
        EJECUTADOS.computeIfAbsent(area, k -> new AtomicInteger()).incrementAndGet();
    }

    /** Same, with a legacy-to-modern exception class mapping (documented per area). */
    public static void igual(String area, Caso c, Map<String, String> mapeoExcepciones, Callable<Object> body) {
        JsonObject esperado = c.out().deepCopy();
        mapear(esperado, mapeoExcepciones);
        JsonObject actual = run(body);
        mapear(actual, mapeoExcepciones);
        comparar(area, c, esperado, actual);
    }

    private static void mapear(JsonElement e, Map<String, String> m) {
        if (e.isJsonObject()) {
            JsonObject o = e.getAsJsonObject();
            if (o.has("throws") && m.containsKey(o.get("throws").getAsString())) o.addProperty("throws", m.get(o.get("throws").getAsString()));
            for (Map.Entry<String, JsonElement> x : o.entrySet()) mapear(x.getValue(), m);
        } else if (e.isJsonArray()) {
            for (JsonElement x : e.getAsJsonArray()) mapear(x, m);
        }
    }

    /** Same as {@link #igual(String, Caso, Callable)} after applying {@code normalizar} to BOTH records (documented per use). */
    public static void igualNormalizado(String area, Caso c, java.util.function.Consumer<JsonObject> normalizar, Callable<Object> body) {
        JsonObject esperado = c.out().deepCopy();
        normalizar.accept(esperado);
        JsonObject actual = run(body);
        normalizar.accept(actual);
        comparar(area, c, esperado, actual);
    }

    public static void contar(String area) {
        EJECUTADOS.computeIfAbsent(area, k -> new AtomicInteger()).incrementAndGet();
    }

    public static int ejecutados(String area) {
        AtomicInteger n = EJECUTADOS.get(area);
        return n == null ? 0 : n.get();
    }

    public static JsonElement json(Object o) {
        return GSON.toJsonTree(o);
    }

    /** java.util.Date family rendered as the oracle did (O.d). */
    public static Map<String, Object> d(Date x) {
        if (x == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", x.getClass().getName());
        m.put("ms", x.getTime());
        m.put("local", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(x));
        if (x instanceof java.sql.Timestamp t) m.put("nanos", t.getNanos());
        m.put("toString", x instanceof java.sql.Timestamp || x instanceof java.sql.Date || x instanceof java.sql.Time ? x.toString() : null);
        return m;
    }

    public static java.sql.Timestamp ts(long ms, int nanos) {
        java.sql.Timestamp t = new java.sql.Timestamp(ms);
        t.setNanos(nanos);
        return t;
    }

    public static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    /** Default locale of the oracle JVM (-Duser.language=es -Duser.country=CL). */
    public static final Locale LOCALE_ORACULO = Locale.of("es", "CL");

    /**
     * Runs {@code r} with the JVM default zone set to {@code zona} and the default locale set to the oracle's
     * (Calendar.getFirstDayOfWeek depends on it); restores both.
     */
    public static void enZona(String zona, ThrowingRunnable r) throws Exception {
        TimeZone antes = TimeZone.getDefault();
        Locale antesL = Locale.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone(zona));
        Locale.setDefault(LOCALE_ORACULO);
        try {
            r.run();
        } finally {
            TimeZone.setDefault(antes);
            Locale.setDefault(antesL);
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    static boolean bienFormado(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (i + 1 >= s.length() || !Character.isLowSurrogate(s.charAt(i + 1))) return false;
                i++;
            } else if (Character.isLowSurrogate(c)) return false;
        }
        return true;
    }

    /** Golden zone directories: the recorded zone id per directory. */
    public static final Map<String, String> ZONAS = Map.of("America_Santiago", "America/Santiago", "UTC", "UTC");

    private Golden() {
    }
}
