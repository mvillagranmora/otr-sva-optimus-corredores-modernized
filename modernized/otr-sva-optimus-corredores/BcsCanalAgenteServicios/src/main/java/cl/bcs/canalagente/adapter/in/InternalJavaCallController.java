package cl.bcs.canalagente.adapter.in;

import cl.bcs.canalagente.domain.HistorialMovimientosRequest;
import cl.bcs.canalagente.domain.HistorialMovimientosResponse;
import cl.bcs.canalagente.port.in.ConsultarHistorialMovimientosUseCase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal endpoint for J-type JavaCall dispatch.
 *
 * Accepts POST /internal/java-call with body:
 * {"class": "cl.bcs.canalagente.ConsultarHistorialMovimientosCuentaInversion", "params": {...}}
 *
 * Used by BcsPlataformaWeb dispatcher and cross-service callers.
 */
@RestController
@RequestMapping("/internal/java-call")
public class InternalJavaCallController {

    private static final Gson GSON = new Gson();

    private final ConsultarHistorialMovimientosUseCase useCase;

    public InternalJavaCallController(ConsultarHistorialMovimientosUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> dispatch(@RequestBody String body) {
        JsonObject req    = JsonParser.parseString(body).getAsJsonObject();
        String className  = req.has("class") ? req.get("class").getAsString() : "";
        JsonObject params = req.has("params") ? req.getAsJsonObject("params") : new JsonObject();

        JsonObject result = switch (className) {
            case "cl.bcs.canalagente.ConsultarHistorialMovimientosCuentaInversion" -> {
                HistorialMovimientosRequest request  = mapRequest(params);
                HistorialMovimientosResponse response = useCase.consultar(request);
                yield GSON.toJsonTree(response).getAsJsonObject();
            }
            default -> throw new IllegalArgumentException("Unknown J-type class: " + className);
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result.toString());
    }

    private static HistorialMovimientosRequest mapRequest(JsonObject p) {
        return new HistorialMovimientosRequest(
                getLong  (p, "p_IdEmpresa"),
                getLong  (p, "p_IdUsuario"),
                getString(p, "p_IdModulo"),
                getLong  (p, "p_IdPortafolio"),
                getString(p, "p_FechaDesde"),
                getString(p, "p_FechaHasta"),
                getString(p, "p_IdMoneda"),
                getLong  (p, "p_IdMatrizAgrupador"),
                getString(p, "p_EsADC")
        );
    }

    private static String getString(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return (e == null || e.isJsonNull()) ? null : e.getAsString();
    }

    private static Long getLong(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return (e == null || e.isJsonNull()) ? null : e.getAsLong();
    }
}
