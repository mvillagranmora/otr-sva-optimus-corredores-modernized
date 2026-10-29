package cl.bcs.custodia.flujo;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.flujo.h2.FlujoCaso;

/**
 * The goldens of the slice, synced byte for byte by analysis/.../equivalence/BcsCustodiaServicios/legacy-oracle/sync.py into
 * src/test/resources/custodia-flujo/ (recorded ONLY by legacy-oracle/record.sh from the unmodified legacy). A missing directory, case or golden
 * is a test FAILURE (AssertionError), never a skip.
 */
public final class GoldenCasos {

    /** one golden case; toString = "[rule ids] case-id" so that the display name of every test names the rules it pins */
    public record Caso(String id, List<String> reglas, Path dir) {
        @Override
        public String toString() {
            return reglas + " " + id;
        }
    }

    private GoldenCasos() {
    }

    public static Path base() {
        URL u = GoldenCasos.class.getResource("/custodia-flujo/schema-flujo.sql");
        if (u == null) throw new AssertionError("test resources custodia-flujo/ missing: run py -3 -I legacy-oracle/sync.py");
        try {
            return Paths.get(u.toURI()).getParent();
        } catch (Exception e) {
            throw new AssertionError("cannot resolve custodia-flujo/", e);
        }
    }

    public static List<Caso> casos() {
        List<Caso> out = new ArrayList<>();
        try (Stream<Path> s = Files.list(base().resolve("cases"))) {
            for (Path d : s.filter(Files::isDirectory).sorted().toList()) {
                if (!Files.exists(d.resolve("expected.json"))) throw new AssertionError("case without golden: " + d.getFileName());
                JsonObject c = FlujoCaso.leer(d.resolve("case.json"));
                List<String> reglas = new ArrayList<>();
                for (JsonElement e : c.getAsJsonArray("reglas")) reglas.add(e.getAsString());
                out.add(new Caso(d.getFileName().toString(), reglas, d));
            }
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError("cannot list the golden cases", e);
        }
        if (out.isEmpty()) throw new AssertionError("no golden case under custodia-flujo/cases: nothing would be proven");
        return out;
    }

    public static JsonObject golden(Caso c) throws Exception {
        return FlujoCaso.leer(c.dir().resolve("expected.json"));
    }

    /** JSON with object keys sorted recursively (numbers and strings keep their text): key order of a REQUEST is not part of the contract */
    public static JsonElement canonico(JsonElement e) {
        if (e == null || e.isJsonNull() || e.isJsonPrimitive()) return e;
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) a.add(canonico(x));
            return a;
        }
        JsonObject o = new JsonObject();
        e.getAsJsonObject().keySet().stream().sorted().forEach(k -> o.add(k, canonico(e.getAsJsonObject().get(k))));
        return o;
    }

    public static JsonElement parse(String s) {
        return JsonParser.parseString(s);
    }

    /** JVM settings the legacy oracle runs with (record.sh): zone UTC, locale en. Returns the previous ones. */
    public static Object[] fijarZonaYLocale() {
        Object[] previo = { TimeZone.getDefault(), Locale.getDefault() };
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.US);
        org.h2.util.DateTimeUtils.resetCalendar();
        return previo;
    }

    public static void restaurar(Object[] previo) {
        TimeZone.setDefault((TimeZone) previo[0]);
        Locale.setDefault((Locale) previo[1]);
        org.h2.util.DateTimeUtils.resetCalendar();
    }
}
