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

import cl.bcs.fondomutuo.consultas.instrumentos.CasoIfm;
import cl.bcs.fondomutuo.consultas.solicitudes.CasoSfm;

/**
 * verify 2026-10-27 (v27b, read-only): canonical text of the legacy golden / legacy dump and of the new code's run (module harness, profile ABIERTA), same comparison points and
 * canonicalisation as IfmEquivalenciaTest / SfmEquivalenciaTest. NOTHING is relaxed (DESVIO-KIT-1 included): an exception is written as its class, except a plain Exception whose
 * message is the product's own.
 *   java Volcar27b dev <outRoot>                         ifm, sfm, ws goldens -> <outRoot>/{ifm,sfm,ws}/{legacy,new}/<id>.txt
 *   java Volcar27b fresh <ifm|sfm|ws> <cases> <dumps> <out>   fresh inputs: legacy dump <dumps>/<id>.json -> <out>/{legacy,new}/<id>.txt
 */
public class Volcar27b {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final String[] CLAVES = { "salidaCruda", "tablas", "colaboradores", "secuenciasSolicitadas", "sesion", "decisionServlet" };

    interface Corrida { JsonObject run() throws Exception; }

    public static void main(String[] a) throws Exception {
        if (a[0].equals("dev")) {
            Path root = Path.of(a[1]);
            int ni = 0, ns = 0, nw = 0;
            for (String id : CasoIfm.casos()) {
                escribir(root.resolve("ifm"), id, CasoIfm.golden(id), correr(() -> CasoIfm.ejecutar(id, CasoIfm.Perfil.ABIERTA)));
                ni++;
            }
            for (String id : CasoSfm.casos()) {
                boolean ws = CasoSfm.servicio(id).equals(CasoSfm.WS);
                escribir(root.resolve(ws ? "ws" : "sfm"), id, CasoSfm.golden(id), correr(() -> CasoSfm.ejecutar(id, CasoSfm.Perfil.ABIERTA)));
                if (ws) nw++; else ns++;
            }
            System.out.println("dev: ifm=" + ni + " sfm=" + ns + " ws=" + nw);
        } else {
            String front = a[1];
            Path cases = Path.of(a[2]), dumps = Path.of(a[3]), out = Path.of(a[4]);
            List<Path> dirs = new ArrayList<>();
            try (Stream<Path> s = Files.list(cases)) { s.filter(d -> Files.exists(d.resolve("case.json"))).sorted().forEach(dirs::add); }
            for (Path d : dirs) {
                String id = d.getFileName().toString();
                Path dump = dumps.resolve(id + ".json");
                JsonObject legacy = Files.exists(dump) ? JsonParser.parseString(Files.readString(dump, StandardCharsets.UTF_8)).getAsJsonObject() : null;
                Corrida c = front.equals("ifm") ? () -> CasoIfm.ejecutar(d, CasoIfm.Perfil.ABIERTA) : () -> CasoSfm.ejecutar(d, CasoSfm.Perfil.ABIERTA);
                escribir(out, id, legacy, correr(c));
            }
            System.out.println("fresh " + front + ": " + dirs.size());
        }
    }

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
        Files.createDirectories(out.resolve("legacy"));
        Files.createDirectories(out.resolve("new"));
        if (legacy != null) Files.writeString(out.resolve("legacy").resolve(id + ".txt"), canonico(legacy), StandardCharsets.UTF_8);
        Files.writeString(out.resolve("new").resolve(id + ".txt"), canonico(nuevo), StandardCharsets.UTF_8);
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
