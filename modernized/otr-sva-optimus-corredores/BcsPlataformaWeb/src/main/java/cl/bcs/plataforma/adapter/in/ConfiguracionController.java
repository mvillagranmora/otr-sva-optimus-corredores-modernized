package cl.bcs.plataforma.adapter.in;

import cl.bcs.plataforma.domain.RegistroConfiguracion;
import cl.bcs.plataforma.service.ConfiguracionCacheService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Configuration cache endpoint.
 * Source: ConfiguracionServlet — @WebServlet("/Configuraciones")
 *
 * Integration tests use @Disabled("requires HANA") because the queries
 * span 11 HANA schemas in a UNION ALL.
 */
@RestController
public class ConfiguracionController {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionController.class);

    private final ConfiguracionCacheService cacheService;
    private final Gson                      gson;

    public ConfiguracionController(ConfiguracionCacheService cacheService) {
        this.cacheService = Objects.requireNonNull(cacheService, "cacheService");
        this.gson         = new Gson();
    }

    /**
     * GET /Configuraciones — returns configuration values from cache.
     * Body can be a single JSON object or a JSON array.
     * Source: ConfiguracionServlet.doGet
     */
    @GetMapping(value = "/Configuraciones", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getConfiguraciones(
            @RequestBody(required = false) String body) throws Exception {
        List<RegistroConfiguracion> keys   = parseKeys(body);
        List<RegistroConfiguracion> result = cacheService.getConfiguraciones(keys);
        return ResponseEntity.ok(gson.toJson(result));
    }

    @PostMapping(value = "/Configuraciones", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getConfiguracionesPost(
            @RequestBody(required = false) String body) throws Exception {
        return getConfiguraciones(body);
    }

    // ─── Pure-logic helper (public static for characterization tests) ────────

    /**
     * Parses request body into a list of RegistroConfiguracion lookup keys.
     * Source: ConfiguracionServlet.doGet — array vs single object logic
     */
    public static List<RegistroConfiguracion> parseKeys(String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonElement root = JsonParser.parseString(body.trim());
        if (root.isJsonArray()) {
            JsonArray arr = root.getAsJsonArray();
            List<RegistroConfiguracion> keys = new ArrayList<>();
            for (JsonElement el : arr) {
                keys.add(fromJsonObject(el.getAsJsonObject()));
            }
            return Collections.unmodifiableList(keys);
        }
        return List.of(fromJsonObject(root.getAsJsonObject()));
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private static RegistroConfiguracion fromJsonObject(JsonObject obj) {
        long   idEmpresa       = obj.has("idEmpresa")       ? obj.get("idEmpresa").getAsLong()   : 0L;
        String modulo          = obj.has("modulo")          ? obj.get("modulo").getAsString()     : "";
        String codigoParametro = obj.has("codigoParametro") ? obj.get("codigoParametro").getAsString() : "";
        return new RegistroConfiguracion(idEmpresa, modulo, codigoParametro);
    }
}
