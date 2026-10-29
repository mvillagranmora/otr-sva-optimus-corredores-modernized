import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.valorcuota.CasoCvc;

/**
 * verify 2026-10-27 (v27d, read-only driver, analysis side) for CargarValorCuota (CVC, W2, WRITES): for each case writes the canonical text of the legacy dump (golden of the module's
 * test resources, or the fresh oracle dump) and of the run of the NEW code through the module's own harness (CasoCvc, profile ABIERTA). Same comparison points as
 * CvcEquivalenciaTest: salidaCruda, tablasIniciales, tablasTrasLaLlamada, tablas (after the decision), colaboradores (AgregarLog: name / arguments / answer), secuenciasSolicitadas,
 * previas, sesion, decisionServlet, excepcion (CLASS only, message only for a plain Exception). Nothing is relaxed: DESVIO-KIT-1 shows up as a difference.
 *
 *   java Volcar27d dev <out>                          every development case (golden in the module's test resources)
 *   java Volcar27d fresh <out> <cases> <legacyDumps>  fresh inputs: <cases>/<id>/case.json, legacy dump <legacyDumps>/<id>.json
 */
public class Volcar27d {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final String[] CLAVES = { "salidaCruda", "tablasIniciales", "tablasTrasLaLlamada", "tablas", "colaboradores", "secuenciasSolicitadas", "previas", "sesion",
            "decisionServlet" };

    interface Corrida {
        JsonObject run() throws Exception;
    }

    public static void main(String[] a) throws Exception {
        String modo = a[0];
        Path out = Path.of(a[1]);
        Files.createDirectories(out.resolve("legacy"));
        Files.createDirectories(out.resolve("new"));
        int n = 0;
        if (modo.equals("dev")) {
            for (String id : CasoCvc.casos()) {
                JsonObject golden = CasoCvc.golden(id);
                JsonObject nuevo = correr(() -> CasoCvc.ejecutar(id, CasoCvc.Perfil.ABIERTA));
                escribir(out, id, golden, nuevo);
                n++;
            }
        } else {
            List<Path> dirs = new ArrayList<>();
            try (Stream<Path> s = Files.list(Path.of(a[2]))) {
                s.filter(d -> Files.exists(d.resolve("case.json"))).sorted().forEach(dirs::add);
            }
            for (Path d : dirs) {
                String id = d.getFileName().toString();
                Path dump = Path.of(a[3]).resolve(id + ".json");
                JsonObject legacy = Files.exists(dump) ? JsonParser.parseString(Files.readString(dump, StandardCharsets.UTF_8)).getAsJsonObject() : null;
                JsonObject nuevo = correr(() -> CasoCvc.ejecutar(d, CasoCvc.Perfil.ABIERTA));
                escribir(out, id, legacy, nuevo);
                n++;
            }
        }
        System.out.println(modo + " cvc: " + n + " cases written to " + out);
    }

    /** A harness failure is recorded as such (it then differs from the golden): never hidden. */
    static JsonObject correr(Corrida c) {
        try {
            return c.run();
        } catch (Throwable t) {
            JsonObject o = new JsonObject();
            o.addProperty("errorDelArnes", t.getClass().getName() + ": " + t.getMessage());
            return o;
        }
    }

    static void escribir(Path out, String id, JsonObject legacy, JsonObject nuevo) throws Exception {
        if (legacy != null) Files.writeString(out.resolve("legacy").resolve(id + ".txt"), canonico(legacy), StandardCharsets.UTF_8);
        Files.writeString(out.resolve("new").resolve(id + ".txt"), canonico(nuevo), StandardCharsets.UTF_8);
        // for the reader only (not listed in the fragments): the full exception text of each side
        if (legacy != null && legacy.has("excepcion")) Files.writeString(out.resolve("legacy").resolve(id + ".exc"), legacy.get("excepcion").getAsString(), StandardCharsets.UTF_8);
        if (nuevo.has("excepcion")) Files.writeString(out.resolve("new").resolve(id + ".exc"), nuevo.get("excepcion").getAsString(), StandardCharsets.UTF_8);
    }

    static String canonico(JsonObject o) {
        StringBuilder b = new StringBuilder();
        if (o.has("errorDelArnes")) b.append("== errorDelArnes\n").append(o.get("errorDelArnes").getAsString()).append('\n');
        if (o.has("canonicalizacionIdsFalla")) b.append("== canonicalizacionIdsFalla\n").append(o.get("canonicalizacionIdsFalla").getAsString()).append('\n');
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
