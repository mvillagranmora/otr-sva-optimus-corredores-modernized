import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import cl.bcs.fondomutuo.consultas.custodia.CasoCpf;
import cl.bcs.fondomutuo.consultas.detalle.CasoDet;

/**
 * verify 2026-10-27 (read-only driver, analysis side) for DET and CPF: for each case writes the canonical text of the legacy dump (golden, or the fresh oracle dump) and of the
 * run of the NEW code through the module's own harness (CasoDet / CasoCpf, profile ABIERTA). Same points as DetEquivalenciaTest / CpfEquivalenciaTest: salidaCruda, tablas,
 * colaboradores (name / arguments / answer), secuenciasSolicitadas, sesion, decisionServlet, exception (CLASS only, message only for a plain Exception). Nothing is relaxed:
 * DESVIO-KIT-1 shows up as a difference. CPF: the ids of the solicitudes (D-F3A-04) are canonicalised on BOTH sides with the test's own CpfEquivalenciaTest.conIdsCanonicos
 * (invoked by reflection); the order of P_OUT_TBL stays inside salidaCruda, so it is compared by bytes.
 *
 *   java Volcar27c dev <det|cpf> <out>                              every development case (golden in the module's test resources)
 *   java Volcar27c fresh <det|cpf> <out> <cases> <legacyDumps>      fresh inputs: <cases>/<id>/case.json, legacy dump <legacyDumps>/<id>.json
 */
public class Volcar27c {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final String[] CLAVES = { "salidaCruda", "tablas", "colaboradores", "secuenciasSolicitadas", "sesion", "decisionServlet" };
    private static Method canonIds;

    interface Corrida {
        JsonObject run() throws Exception;
    }

    public static void main(String[] a) throws Exception {
        String modo = a[0];
        String frente = a[1];
        Path out = Path.of(a[2]);
        Files.createDirectories(out.resolve("legacy"));
        Files.createDirectories(out.resolve("new"));
        boolean cpf = frente.equals("cpf");
        if (cpf) {
            canonIds = Class.forName("cl.bcs.fondomutuo.consultas.custodia.CpfEquivalenciaTest").getDeclaredMethod("conIdsCanonicos", String.class, JsonObject.class);
            canonIds.setAccessible(true);
        }
        int n = 0;
        if (modo.equals("dev")) {
            for (String id : cpf ? CasoCpf.casos() : CasoDet.casos()) {
                JsonObject golden = cpf ? CasoCpf.golden(id) : CasoDet.golden(id);
                JsonObject nuevo = correr(() -> cpf ? CasoCpf.ejecutar(id, CasoCpf.Perfil.ABIERTA) : CasoDet.ejecutar(id, CasoDet.Perfil.ABIERTA));
                escribir(out, id, golden, nuevo, cpf);
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
                JsonObject nuevo = correr(() -> cpf ? CasoCpf.ejecutar(d, CasoCpf.Perfil.ABIERTA) : CasoDet.ejecutar(d, CasoDet.Perfil.ABIERTA));
                escribir(out, id, legacy, nuevo, cpf);
                n++;
            }
        }
        System.out.println(modo + " " + frente + ": " + n + " cases written to " + out);
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

    static JsonObject ids(String etiqueta, JsonObject dump) {
        try {
            return (JsonObject) canonIds.invoke(null, etiqueta, dump);
        } catch (InvocationTargetException e) {
            JsonObject o = dump.deepCopy();
            o.addProperty("canonicalizacionIdsFalla", e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
            return o;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static void escribir(Path out, String id, JsonObject legacy, JsonObject nuevo, boolean cpf) throws Exception {
        if (cpf) {
            if (legacy != null) legacy = ids(id + " (golden)", legacy);
            nuevo = ids(id + " (new code)", nuevo);
        }
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
