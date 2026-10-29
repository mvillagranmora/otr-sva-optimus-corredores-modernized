import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

import cl.bcs.fondomutuo.consultas.dolar.CasoVdo;
import cl.bcs.fondomutuo.consultas.fecha.CasoFsa;
import cl.bcs.fondomutuo.consultas.reinversion.CasoFmr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



/**
 * verify 2026-10-25 (read-only driver, analysis side): writes, for each case, the canonical text of the legacy golden and of the new code's run, so that the
 * skill's compare.py judges the bytes. The new side runs through the module's own test harness (CasoNormal / CasoRre, profile ABIERTAS, as the module tests);
 * nothing of the module is changed. Canonical text = the comparison points of NormalEquivalenciaTest / RreEquivalenciaTest, same canonicalisation
 * (keys sorted, collaborator calls reduced to name/arguments/answer); the exception is written as its class only (runtime text is not product output),
 * except a plain legacy {@code Exception}, whose message is the product's own text.
 *
 *   java Volcar nor <out>                      FMM-NOR goldens (fondomutuo-nor/cases)
 *   java Volcar rre <out>                      FMM-RRE goldens (fondomutuo-rre/cases)
 *   java Volcar rre-dir <out> <cases> <dumps>  fresh RR inputs: <cases>/<id>/case.json, legacy dump <dumps>/<id>.json
 */
public class Volcar27 {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final String[] CLAVES = { "salidaCruda", "tablas", "colaboradores", "secuenciasSolicitadas", "sesion", "decisionServlet" };

    public static void main(String[] a) throws Exception {
        // dev <fsa|vdo|fmr> <out> | dir <fsa|vdo|fmr> <out> <cases> <dumps>
        boolean dir = a[0].equals("dir");
        String f = a[1];
        Path out = Path.of(a[2]);
        Files.createDirectories(out.resolve("legacy"));
        Files.createDirectories(out.resolve("new"));
        int n = 0;
        if (!dir) {
            List<String> ids = ids(f);
            for (String id : ids) {
                escribir(out, id, golden(f, id), correr(() -> ejecutar(f, id, null)));
                n++;
            }
        } else {
            List<Path> dirs = new ArrayList<>();
            try (Stream<Path> s = Files.list(Path.of(a[3]))) {
                s.filter(d -> Files.exists(d.resolve("case.json"))).sorted().forEach(dirs::add);
            }
            for (Path d : dirs) {
                String id = d.getFileName().toString();
                Path dump = Path.of(a[4]).resolve(id + ".json");
                JsonObject legacy = Files.exists(dump) ? JsonParser.parseString(Files.readString(dump, StandardCharsets.UTF_8)).getAsJsonObject() : null;
                escribir(out, id, legacy, correr(() -> ejecutar(f, id, d)));
                n++;
            }
        }
        System.out.println(a[1] + ": " + n + " cases written to " + out);
    }

    static List<String> ids(String f) {
        return switch (f) {
            case "fsa" -> CasoFsa.casos();
            case "vdo" -> CasoVdo.casos();
            case "fmr" -> CasoFmr.casos();
            default -> throw new IllegalArgumentException(f);
        };
    }

    static JsonObject golden(String f, String id) {
        return switch (f) {
            case "fsa" -> CasoFsa.golden(id);
            case "vdo" -> CasoVdo.golden(id);
            default -> CasoFmr.golden(id);
        };
    }

    /** d == null: development case by id; otherwise the case directory. Profile ABIERTA (the listed class is served). */
    static JsonObject ejecutar(String f, String id, Path d) throws Exception {
        return switch (f) {
            case "fsa" -> d == null ? CasoFsa.ejecutar(id, CasoFsa.Perfil.ABIERTA) : CasoFsa.ejecutar(d, CasoFsa.Perfil.ABIERTA);
            case "vdo" -> d == null ? CasoVdo.ejecutar(id, CasoVdo.Perfil.ABIERTA) : CasoVdo.ejecutar(d, CasoVdo.Perfil.ABIERTA);
            default -> d == null ? CasoFmr.ejecutar(id, CasoFmr.Perfil.ABIERTA) : CasoFmr.ejecutar(d, CasoFmr.Perfil.ABIERTA);
        };
    }

    interface Corrida {
        JsonObject run() throws Exception;
    }

    /** A harness failure is recorded as such (it then differs from the golden): never hidden. */
    static JsonObject correr(Corrida c) {
        try {
            return c.run();
        } catch (Throwable t) {
            JsonObject o = new JsonObject();
            o.addProperty("errorDelArnes", t.getClass().getName());
            return o;
        }
    }

    static void escribir(Path out, String id, JsonObject legacy, JsonObject nuevo) throws Exception {
        // no legacy dump (the legacy refused the input): no legacy file, compare.py reports it missing and the case is left out by the caller
        if (legacy != null) Files.writeString(out.resolve("legacy").resolve(id + ".txt"), canonico(legacy), StandardCharsets.UTF_8);
        Files.writeString(out.resolve("new").resolve(id + ".txt"), canonico(nuevo), StandardCharsets.UTF_8);
        // for the reader only (not listed in cases.json): the full exception text of each side, to see whether only the class differs
        if (legacy != null && legacy.has("excepcion")) Files.writeString(out.resolve("legacy").resolve(id + ".exc"), legacy.get("excepcion").getAsString(), StandardCharsets.UTF_8);
        if (nuevo.has("excepcion")) Files.writeString(out.resolve("new").resolve(id + ".exc"), nuevo.get("excepcion").getAsString(), StandardCharsets.UTF_8);
    }

    static String canonico(JsonObject o) {
        StringBuilder b = new StringBuilder();
        if (o.has("errorDelArnes")) b.append("== errorDelArnes\n").append(o.get("errorDelArnes").getAsString()).append('\n');
        for (String k : CLAVES) b.append("== ").append(k).append('\n').append(texto(o.get(k))).append('\n');
        b.append("== excepcion\n");
        if (!o.has("excepcion")) {
            b.append("<none>\n");
        } else {
            String e = o.get("excepcion").getAsString();
            int i = e.indexOf(':');
            String clase = i < 0 ? e : e.substring(0, i);
            b.append(clase);
            if (clase.equals("Exception") && i >= 0) b.append(':').append(e.substring(i + 1));
            b.append('\n');
        }
        return b.toString();
    }

    static String texto(JsonElement e) {
        if (e == null) return "<absent>";
        if (e.isJsonArray() && !e.getAsJsonArray().isEmpty() && e.getAsJsonArray().get(0).isJsonObject() && e.getAsJsonArray().get(0).getAsJsonObject().has("colaborador")) {
            JsonArray n = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) {
                JsonObject o = x.getAsJsonObject();
                JsonObject m = new JsonObject();
                m.add("colaborador", o.get("colaborador"));
                m.add("argumentos", o.get("argumentos"));
                m.add("respuesta", o.get("respuesta"));
                n.add(m);
            }
            return PRETTY.toJson(canon(n));
        }
        if (e.isJsonPrimitive()) return e.getAsString();
        return PRETTY.toJson(canon(e));
    }

    static JsonElement canon(JsonElement e) {
        if (e == null || e.isJsonNull()) return JsonNull.INSTANCE;
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) a.add(canon(x));
            return a;
        }
        if (e.isJsonObject()) {
            TreeMap<String, JsonElement> m = new TreeMap<>();
            for (String k : e.getAsJsonObject().keySet()) m.put(k, canon(e.getAsJsonObject().get(k)));
            JsonObject o = new JsonObject();
            m.forEach(o::add);
            return o;
        }
        return e;
    }
}
