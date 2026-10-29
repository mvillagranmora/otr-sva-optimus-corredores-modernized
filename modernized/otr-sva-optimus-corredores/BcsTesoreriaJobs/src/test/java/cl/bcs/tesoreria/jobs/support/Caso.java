package cl.bcs.tesoreria.jobs.support;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * One recorded case under {@code src/test/resources/tesoreria-jobs/cases/<id>/}: {@code case.json} (input), optional
 * {@code seed.sql}, and {@code expected.json} recorded from the UNMODIFIED legacy by {@code legacy-oracle/record.sh}. A missing
 * directory, case.json or expected.json FAILS the test (AssertionError); nothing is skipped. {@code ${RUTA_BASE}} is replaced by a
 * fresh temporary directory (with the case "dirs" sub directories), exactly as the oracle did.
 */
public final class Caso {

    public static final String RAIZ = "tesoreria-jobs";

    private final String nombre;
    private final JsonObject caso;
    private final JsonObject esperado;
    private final Path rutaBase;
    private final BdCaso bd;

    private Caso(String nombre) {
        this.nombre = nombre;
        Path dir = recurso(RAIZ + "/cases/" + nombre);
        JsonObject caso0 = leer(dir.resolve("case.json"));
        this.esperado = leer(dir.resolve("expected.json"));
        List<String> subdirs = new ArrayList<>();
        if (caso0.has("dirs")) {
            caso0.getAsJsonArray("dirs").forEach(e -> subdirs.add(e.getAsString()));
        }
        this.rutaBase = Archivos.nuevaRaiz(subdirs);
        this.caso = JsonParser.parseString(Archivos.sustituir(caso0.toString(), rutaBase)).getAsJsonObject();
        Path semilla = dir.resolve("seed.sql");
        this.bd = new BdCaso(texto(recurso(RAIZ + "/schema-jobs.sql")), texto(recurso(RAIZ + "/seed/base.sql")),
                Files.exists(semilla) ? Archivos.sustituir(texto(semilla), rutaBase) : "");
        H2Hana.reloj = Timestamp.valueOf(reloj());
    }

    public static Caso cargar(String nombre) {
        return new Caso(nombre);
    }

    /** Every case directory whose name starts with the prefix, sorted. */
    public static List<String> todos(String prefijo) {
        try (var s = Files.list(recurso(RAIZ + "/cases"))) {
            return s.filter(Files::isDirectory).map(p -> p.getFileName().toString()).filter(n -> n.startsWith(prefijo)).sorted().toList();
        } catch (Exception e) {
            throw new AssertionError("cannot list the cases", e);
        }
    }

    public String nombre() {
        return nombre;
    }

    public JsonObject caso() {
        return caso;
    }

    public JsonObject esperado() {
        return esperado;
    }

    public Path rutaBase() {
        return rutaBase;
    }

    public BdCaso bd() {
        return bd;
    }

    public String job() {
        return caso.get("job").getAsString();
    }

    public long idPrograma() {
        return caso.has("idPrograma") ? caso.get("idPrograma").getAsLong() : 900L;
    }

    /** The database UTC clock (case "reloj", default 2025-04-01 10:15:30). */
    public String reloj() {
        return caso.has("reloj") ? caso.get("reloj").getAsString() : "2025-04-01 10:15:30";
    }

    /** Job parameters; JSON null removes the key. */
    public Map<String, String> parametros() {
        Map<String, String> p = new HashMap<>();
        if (caso.has("parametros")) {
            caso.getAsJsonObject("parametros").entrySet().forEach(e -> {
                if (!e.getValue().isJsonNull()) {
                    p.put(e.getKey(), e.getValue().getAsString());
                }
            });
        }
        return p;
    }

    /** seed/config.json merged with the case "config" (JSON null removes the key): "MODULO/Parametro" -> valor. */
    public Map<String, String> config() {
        JsonObject cfg = leer(recurso(RAIZ + "/seed/config.json"));
        if (caso.has("config")) {
            for (Map.Entry<String, JsonElement> e : caso.getAsJsonObject("config").entrySet()) {
                if (e.getValue().isJsonNull()) {
                    cfg.remove(e.getKey());
                } else {
                    cfg.add(e.getKey(), e.getValue());
                }
            }
        }
        Map<String, String> m = new LinkedHashMap<>();
        cfg.entrySet().forEach(e -> m.put(e.getKey(), Archivos.sustituir(e.getValue().getAsString(), rutaBase)));
        return m;
    }

    public JsonObject colaboradores() {
        return caso.has("colaboradores") ? caso.getAsJsonObject("colaboradores") : new JsonObject();
    }

    public Map<String, Long> secuencias() {
        Map<String, Long> m = new HashMap<>();
        if (caso.has("secuencias")) {
            caso.getAsJsonObject("secuencias").entrySet().forEach(e -> m.put(e.getKey(), e.getValue().getAsLong()));
        }
        return m;
    }

    public static Path recurso(String ruta) {
        URL url = Caso.class.getClassLoader().getResource(ruta);
        if (url == null) {
            throw new AssertionError("missing test fixture on classpath: " + ruta + " (a missing fixture is a FAILURE, not a skip)");
        }
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    private static String texto(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new AssertionError("cannot read " + p, e);
        }
    }

    private static JsonObject leer(Path p) {
        if (!Files.exists(p)) {
            throw new AssertionError("missing fixture " + p + " (record it with legacy-oracle/record.sh)");
        }
        return JsonParser.parseString(texto(p)).getAsJsonObject();
    }
}
