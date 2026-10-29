package cl.bcs.sura.controller;

import cl.bcs.sura.service.CrearSocioNegocioModificarPerfilesPortafolioService;
import cl.bcs.sura.service.ModificarClienteModificarPerfilesPortafolioWSService;
import cl.bcs.sura.service.ModificarSocioNegocioModificarPerfilesPortafolioService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Receives J-type JavaCall dispatches from BcsPlataformaWeb's CallController.
 *
 * Protocol:
 *   POST /internal/java-call
 *   Body: {"class": "fully.qualified.ClassName", "params": {...}, "skip": N, "take": M}
 *
 * Migrated classes (3/3):
 *   - cl.bcs.sura.socionegocio.CrearSocioNegocioModificarPerfilesPortafolio
 *   - cl.bcs.sura.socionegocio.ModificarSocioNegocioModificarPerfilesPortafolio
 *   - cl.bcs.sura.socionegocio.ModificarClienteModificarPerfilesPortafolioWS
 *
 * Unknown classes return 503 → BcsGateway falls back to TomEE.
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    private final CrearSocioNegocioModificarPerfilesPortafolioService crearSocioNegocio;
    private final ModificarSocioNegocioModificarPerfilesPortafolioService modificarSocioNegocio;
    private final ModificarClienteModificarPerfilesPortafolioWSService modificarClienteWS;

    public InternalJavaCallController(
            CrearSocioNegocioModificarPerfilesPortafolioService crearSocioNegocio,
            ModificarSocioNegocioModificarPerfilesPortafolioService modificarSocioNegocio,
            ModificarClienteModificarPerfilesPortafolioWSService modificarClienteWS) {
        this.crearSocioNegocio     = crearSocioNegocio;
        this.modificarSocioNegocio = modificarSocioNegocio;
        this.modificarClienteWS    = modificarClienteWS;
    }

    @PostMapping(path = "/internal/java-call",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> dispatch(@RequestBody String rawBody,
                                           @RequestHeader(value = "X-SESSION-TOKEN",
                                                          required = false) String token) {
        JsonObject body;
        try {
            body = JsonParser.parseString(rawBody).getAsJsonObject();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON body");
        }

        String fqn        = body.has("class")  ? body.get("class").getAsString()      : "";
        JsonObject params = body.has("params") ? body.get("params").getAsJsonObject() : new JsonObject();
        int skip          = body.has("skip")   ? body.get("skip").getAsInt()           : 0;
        int take          = body.has("take")   ? body.get("take").getAsInt()           : 0;

        if (fqn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'class' field required");
        }

        log.debug("InternalJavaCall: class={} skip={} take={}", fqn, skip, take);

        JsonObject result = switch (fqn) {
            case "cl.bcs.sura.socionegocio.CrearSocioNegocioModificarPerfilesPortafolio" ->
                crearSocioNegocio.execute(params);
            case "cl.bcs.sura.socionegocio.ModificarSocioNegocioModificarPerfilesPortafolio" ->
                modificarSocioNegocio.execute(params);
            case "cl.bcs.sura.socionegocio.ModificarClienteModificarPerfilesPortafolioWS" ->
                modificarClienteWS.execute(params);
            default -> throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "JavaCall class '" + fqn + "' not yet migrated; TomEE fallback active");
        };

        return ResponseEntity.ok(result.toString());
    }
}
