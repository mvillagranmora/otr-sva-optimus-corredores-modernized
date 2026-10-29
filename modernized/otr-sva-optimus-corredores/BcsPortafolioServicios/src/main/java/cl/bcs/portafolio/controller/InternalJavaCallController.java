package cl.bcs.portafolio.controller;

import cl.bcs.portafolio.javacall.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Receives J-type JavaCall dispatches from BcsPlataformaWeb's CallController.
 *
 * Protocol:
 *   POST /internal/java-call
 *   Body: {"class": "fully.qualified.ClassName", "params": {...}}
 *
 * All 13 BcsPortafolioServicios J-type classes are now migrated here.
 * Unknown classes return 503 → BcsGateway falls back to TomEE.
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    // RULE-076 / HIGH-2: token verified against env-var BCS_INTERNAL_SERVICE_TOKEN.
    // Empty/blank token disables verification (development mode; warn loudly).
    private final String expectedToken;

    private final DataSource ds;
    private final MatrizJavaCallService matrizSvc;
    private final AutorizadoJavaCallService autorizadoSvc;
    private final EliminarSinTransaccionesService eliminarSvc;
    private final PreferenciaPagoJavaCallService preferenciaPagoSvc;
    private final ModificarEstadoPortafolioService modificarEstadoSvc;
    private final AdministrarPortafolioDefectoService administrarDefectoSvc;
    private final CrearCuentaCustodioService crearCuentaCustodioSvc;
    private final ModificarPortafolioService modificarPortafolioSvc;
    private final CrearPortafolioService crearPortafolioSvc;
    private final CrearPortafolioConReplicaService crearPortafolioConReplicaSvc;

    public InternalJavaCallController(
            @Value("${bcs.internal.service-token:}") String expectedToken,
            DataSource ds,
            MatrizJavaCallService matrizSvc,
            AutorizadoJavaCallService autorizadoSvc,
            EliminarSinTransaccionesService eliminarSvc,
            PreferenciaPagoJavaCallService preferenciaPagoSvc,
            ModificarEstadoPortafolioService modificarEstadoSvc,
            AdministrarPortafolioDefectoService administrarDefectoSvc,
            CrearCuentaCustodioService crearCuentaCustodioSvc,
            ModificarPortafolioService modificarPortafolioSvc,
            CrearPortafolioService crearPortafolioSvc,
            CrearPortafolioConReplicaService crearPortafolioConReplicaSvc) {
        this.expectedToken = expectedToken;
        if (expectedToken.isBlank()) {
            log.warn("BCS_INTERNAL_SERVICE_TOKEN not configured — /internal/java-call is unauthenticated");
        }
        this.ds = ds;
        this.matrizSvc = matrizSvc;
        this.autorizadoSvc = autorizadoSvc;
        this.eliminarSvc = eliminarSvc;
        this.preferenciaPagoSvc = preferenciaPagoSvc;
        this.modificarEstadoSvc = modificarEstadoSvc;
        this.administrarDefectoSvc = administrarDefectoSvc;
        this.crearCuentaCustodioSvc = crearCuentaCustodioSvc;
        this.modificarPortafolioSvc = modificarPortafolioSvc;
        this.crearPortafolioSvc = crearPortafolioSvc;
        this.crearPortafolioConReplicaSvc = crearPortafolioConReplicaSvc;
    }

    @PostMapping(path = "/internal/java-call",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> dispatch(@RequestBody String rawBody,
                                           @RequestHeader(value = "X-SESSION-TOKEN",
                                                          required = false) String token) {
        if (!expectedToken.isBlank() && !expectedToken.equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing service token");
        }

        JsonObject body;
        try {
            body = JsonParser.parseString(rawBody).getAsJsonObject();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON body");
        }

        String fqn = body.has("class") ? body.get("class").getAsString() : "";
        JsonObject params = body.has("params") ? body.get("params").getAsJsonObject() : new JsonObject();

        if (fqn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'class' field required");
        }

        log.debug("InternalJavaCall: class={}", fqn);

        try (Connection conn = ds.getConnection()) {
            JsonObject result = switch (fqn) {
                // ── Wave 1: Matriz NOAPV ──────────────────────────────────────────
                case "cl.bcs.portafolio.CrearMatrizAgrupadorNOAPV" ->
                    matrizSvc.crearMatriz(conn, params);
                case "cl.bcs.portafolio.EliminarMatrizAgrupadorNOAPV" ->
                    matrizSvc.eliminarMatriz(conn, params);
                case "cl.bcs.portafolio.ModificarMatrizAgrupadorNOAPV" ->
                    matrizSvc.modificarMatriz(conn, params);

                // ── Wave 1: Autorizado / EliminarSinTransacciones ─────────────────
                case "cl.bcs.portafolio.ModificarAutorizadoOrden" ->
                    autorizadoSvc.modificarAutorizadoOrden(conn, params);
                case "cl.bcs.portafolio.EliminarSinTransacciones" ->
                    eliminarSvc.eliminarSinTransacciones(conn, params);

                // ── Wave 2: Preferencia Pago ──────────────────────────────────────
                case "cl.bcs.portafolio.IngresaPreferenciaPagoPortafolio" ->
                    preferenciaPagoSvc.ingresaPreferenciaPago(conn, params);
                case "cl.bcs.portafolio.ModificaPreferenciaPagoPortafolio" ->
                    preferenciaPagoSvc.modificaPreferenciaPago(conn, params);

                // ── Wave 2: Modificar Estado Portafolio WS ────────────────────────
                case "cl.bcs.portafolio.ws.ModificarEstadoPortafolioWS" ->
                    modificarEstadoSvc.modificarEstadoPortafolio(conn, params);

                // ── Wave 2: Administrar Portafolio Defecto ────────────────────────
                case "cl.bcs.portafolio.AdministrarPortafolioDefecto" ->
                    administrarDefectoSvc.administrarPortafolioDefecto(conn, params);

                // ── Wave 2: Crear Cuenta Custodio ─────────────────────────────────
                case "cl.bcs.portafolio.CrearCuentaCustodio" ->
                    crearCuentaCustodioSvc.crearCuentaCustodio(conn, params);

                // ── Wave 2: Modificar Portafolio ──────────────────────────────────
                case "cl.bcs.portafolio.ModificarPortafolio" ->
                    modificarPortafolioSvc.modificarPortafolio(conn, params);

                // ── Wave 2: Crear Portafolio ──────────────────────────────────────
                case "cl.bcs.portafolio.CrearPortafolio" ->
                    crearPortafolioSvc.crearPortafolio(conn, params);

                // ── Wave 2: Crear Portafolio Con Replica ──────────────────────────
                case "cl.bcs.portafolio.CrearPortafolioConReplica" ->
                    crearPortafolioConReplicaSvc.crearPortafolioConReplica(conn, params);

                default -> throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "JavaCall class not available in this service");
            };
            return ResponseEntity.ok(result.toString());
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            log.error("JavaCall execution failed for class={}", fqn, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "JavaCall execution error: " + e.getMessage());
        }
    }
}
