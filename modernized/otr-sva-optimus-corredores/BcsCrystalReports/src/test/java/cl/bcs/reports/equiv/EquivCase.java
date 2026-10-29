package cl.bcs.reports.equiv;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * One equivalence case = directory src/test/resources/crystal/equiv/&lt;Dominio&gt;_&lt;Plantilla&gt;/ with spec.json
 * (template, formats, params) and data.json (schema + rows + optional subreports). Shared by the legacy recorder and
 * the sidecar test: it only depends on Gson and the JDK.
 */
public final class EquivCase {

    public static final String[] ALL_FORMATS = {"pdf", "excel", "word", "rtf", "txt"};

    public final String name;
    public final Path dir;
    public final String template;          // path relative to BcsCrystalReports/ (templates/...)
    public final List<String> formats = new ArrayList<>();
    public final List<Map<String, String>> params = new ArrayList<>();   // type,name,data
    public final Map<String, String> schema;
    public final JsonArray rows;
    public final Map<String, Map<String, String>> subSchemas = new LinkedHashMap<>();
    public final Map<String, JsonArray> subRows = new LinkedHashMap<>();

    public EquivCase(Path dir) throws IOException {
        this.dir = dir;
        this.name = dir.getFileName().toString();
        JsonObject spec = read(dir.resolve("spec.json"));
        JsonObject data = read(dir.resolve("data.json"));
        this.template = spec.get("template").getAsString();
        spec.getAsJsonArray("formats").forEach(f -> formats.add(f.getAsString()));
        if (spec.has("params")) {
            for (JsonElement p : spec.getAsJsonArray("params")) {
                params.add(new Gson().fromJson(p, new TypeToken<LinkedHashMap<String, String>>() { }.getType()));
            }
        }
        this.schema = new Gson().fromJson(data.get("schema"), new TypeToken<LinkedHashMap<String, String>>() { }.getType());
        this.rows = data.getAsJsonArray("rows");
        if (data.has("subreports")) {
            for (Map.Entry<String, JsonElement> e : data.getAsJsonObject("subreports").entrySet()) {
                JsonObject s = e.getValue().getAsJsonObject();
                subSchemas.put(e.getKey(), new Gson().fromJson(s.get("schema"),
                        new TypeToken<LinkedHashMap<String, String>>() { }.getType()));
                subRows.put(e.getKey(), s.getAsJsonArray("rows"));
            }
        }
    }

    /** True when the current REST contract (one flat row, at most one image and one pdf417, no subreports) can carry it. */
    public boolean serviceCapable() {
        long img = params.stream().filter(p -> "image".equals(p.get("type"))).count();
        long bar = params.stream().filter(p -> "pdf417".equals(p.get("type"))).count();
        boolean typed = schema.values().stream().anyMatch(t -> "blob".equals(t) || "date".equals(t) || "datetime".equals(t));
        return rows.size() == 1 && subRows.isEmpty() && !typed && img <= 1 && bar <= 1 && params.size() == img + bar;
    }

    public static String ext(String format) {
        switch (format) {
            case "excel": return "xls";
            case "word": return "doc";
            default: return format;
        }
    }

    public static List<EquivCase> all(Path root) throws IOException {
        try (Stream<Path> s = Files.list(root)) {
            List<Path> dirs = s.filter(p -> Files.exists(p.resolve("spec.json"))).sorted().collect(Collectors.toList());
            List<EquivCase> out = new ArrayList<>();
            for (Path d : dirs) {
                out.add(new EquivCase(d));
            }
            return out;
        }
    }

    private static JsonObject read(Path p) throws IOException {
        return JsonParser.parseString(new String(Files.readAllBytes(p), StandardCharsets.UTF_8)).getAsJsonObject();
    }
}
