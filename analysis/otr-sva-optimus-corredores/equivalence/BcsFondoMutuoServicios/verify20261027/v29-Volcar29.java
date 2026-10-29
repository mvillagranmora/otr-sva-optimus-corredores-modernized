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

import cl.bcs.fondomutuo.normal.CasoNormal;

/**
 * verify 2026-10-29 (v29, read-only driver, analysis side) for Normal: canonical text of a legacy dump and of the run of the NEW code through the module's own harness
 * CasoNormal (profile ABIERTAS). The canonicalisation is the one of verify20261025/Volcar.java "nor" (same CLAVES, same canon/texto/canonico, exception as its class except a plain Exception).
 * CasoNormal.ejecutar(id, ...) only reads the cases packed in the module's test resources, so for a directory of fresh cases it invokes CasoNormal's own private
 * correr(Path, Perfil, Consumer) by reflection with the same zone / language / H2 calendar set-up as CasoNormal.ejecutar (nothing is copied, nothing of the module changes).
 *
 *   java Volcar29 dev <out>                       canonical text of every development golden (module test resources) -> <out>/dev/<id>.txt   (distinctness reference)
 *   java Volcar29 prev <out> <dumps-root>         canonical text of the earlier fresh Normal dumps <dumps-root>/*&#47;*.json (FRESH-NOR-*)  -> <out>/prev/<id>.txt
 *   java Volcar29 fresh <out> <cases> <dumps>     fresh inputs: <cases>/<id>/case.json, legacy dump <dumps>/<id>.json -> <out>/legacy/<id>.txt, <out>/new/<id>.txt
 *   java Volcar29 gates <out> <cases> <variant>   NEW side only through cl.bcs.fondomutuo.normal.V29Gates (copy of CasoNormal.correr taking the Compuertas): variant = harness | cn1 | cn0 | cn1ce1 | cn0ce1
 */
public class Volcar29 {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final String[] CLAVES = { "salidaCruda", "tablas", "colaboradores", "secuenciasSolicitadas", "sesion", "decisionServlet" };

    interface Corrida {
        JsonObject run() throws Exception;
    }

    public static void main(String[] a) throws Exception {
        Path out = Path.of(a[1]);
        int n = 0;
        switch (a[0]) {
            case "dev" -> {
                Files.createDirectories(out.resolve("dev"));
                for (String id : CasoNormal.casos()) {
                    Files.writeString(out.resolve("dev").resolve(id + ".txt"), canonico(CasoNormal.golden(id)), StandardCharsets.UTF_8);
                    n++;
                }
            }
            case "prev" -> {
                Files.createDirectories(out.resolve("prev"));
                try (Stream<Path> s = Files.walk(Path.of(a[2]))) {
                    List<Path> fs = new ArrayList<>();
                    s.filter(p -> p.getFileName().toString().startsWith("FRESH-NOR-") && p.getFileName().toString().endsWith(".json")).sorted().forEach(fs::add);
                    for (Path f : fs) {
                        String id = f.getFileName().toString().replaceFirst("\\.json$", "");
                        Files.writeString(out.resolve("prev").resolve(id + ".txt"), canonico(JsonParser.parseString(Files.readString(f, StandardCharsets.UTF_8)).getAsJsonObject()), StandardCharsets.UTF_8);
                        n++;
                    }
                }
            }
            case "fresh" -> {
                Files.createDirectories(out.resolve("legacy"));
                Files.createDirectories(out.resolve("new"));
                for (Path d : casos(Path.of(a[2]))) {
                    String id = d.getFileName().toString();
                    Path dump = Path.of(a[3]).resolve(id + ".json");
                    if (Files.exists(dump)) {
                        Files.writeString(out.resolve("legacy").resolve(id + ".txt"), canonico(JsonParser.parseString(Files.readString(dump, StandardCharsets.UTF_8)).getAsJsonObject()), StandardCharsets.UTF_8);
                    }
                    JsonObject nuevo = correr(() -> ejecutarDir(d));
                    Files.writeString(out.resolve("new").resolve(id + ".txt"), canonico(nuevo), StandardCharsets.UTF_8);
                    n++;
                }
            }
            case "gates" -> {
                Files.createDirectories(out);
                Class<?> g = Class.forName("cl.bcs.fondomutuo.normal.V29Gates");
                Method m = g.getDeclaredMethod("ejecutar", Path.class, String.class);
                for (Path d : casos(Path.of(a[2]))) {
                    JsonObject nuevo = correr(() -> {
                        try {
                            return (JsonObject) m.invoke(null, d, a[3]);
                        } catch (InvocationTargetException e) {
                            throw e.getCause() instanceof Exception ex ? ex : new RuntimeException(e.getCause());
                        }
                    });
                    Files.writeString(out.resolve(d.getFileName() + ".txt"), canonico(nuevo), StandardCharsets.UTF_8);
                    n++;
                }
            }
            default -> throw new IllegalArgumentException(a[0]);
        }
        System.out.println(a[0] + ": " + n + " cases written to " + out);
    }

    static List<Path> casos(Path root) throws Exception {
        List<Path> dirs = new ArrayList<>();
        try (Stream<Path> s = Files.list(root)) {
            s.filter(d -> Files.exists(d.resolve("case.json"))).sorted().forEach(dirs::add);
        }
        return dirs;
    }

    /** Same set-up as CasoNormal.ejecutar(id, perfil, null), but for a case directory outside the packed resources. */
    static JsonObject ejecutarDir(Path dir) throws Exception {
        Method correr = CasoNormal.class.getDeclaredMethod("correr", Path.class, CasoNormal.Perfil.class, java.util.function.Consumer.class);
        correr.setAccessible(true);
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        java.util.Locale idioma = java.util.Locale.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            org.h2.util.DateTimeUtils.resetCalendar();
            try {
                return (JsonObject) correr.invoke(null, dir, CasoNormal.Perfil.ABIERTAS, null);
            } catch (InvocationTargetException e) {
                throw e.getCause() instanceof Exception ex ? ex : new RuntimeException(e.getCause());
            }
        } finally {
            java.util.TimeZone.setDefault(zona);
            java.util.Locale.setDefault(idioma);
            org.h2.util.DateTimeUtils.resetCalendar();
        }
    }

    /** A harness failure is recorded as such (it then differs from the legacy): never hidden. */
    static JsonObject correr(Corrida c) {
        try {
            return c.run();
        } catch (Throwable t) {
            JsonObject o = new JsonObject();
            o.addProperty("errorDelArnes", t.getClass().getName());
            return o;
        }
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
