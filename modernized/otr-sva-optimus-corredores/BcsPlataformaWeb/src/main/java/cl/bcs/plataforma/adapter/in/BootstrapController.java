package cl.bcs.plataforma.adapter.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.login.UnidadDeTrabajo;
import cl.bcs.plataforma.login.bootstrap.BootstrapServicio;

/**
 * The bootstrap of the web application as JSON (T-20, T-21, T-21b; option B: the UI is outside the core). Replaces the placeholder that answered a
 * fixed HTML page and checked no token.
 *
 * <ul>
 *   <li>{@code GET /PLA/bootstrap/login}: what the login page needs (companies, logo, dictionary, environment). No token.</li>
 *   <li>{@code POST /PLA/bootstrap/home} (header {@code X-SESSION-TOKEN}): what the home page needs for the session. The session filter has already
 *       answered 400 when the header is missing and 401 when the token is unknown; here a session that exists but is not usable (blocked or expired
 *       user, company badly configured) is also 401. A usable one is committed (use of the token, user connected).</li>
 * </ul>
 * The keys of the answers are the names of the constants the legacy page defined ({@code BCS_*}): see {@link BootstrapServicio}.
 */
@RestController
public class BootstrapController {

    private static final Logger log = LoggerFactory.getLogger(BootstrapController.class);

    private final UnidadDeTrabajo unidad;
    private final BootstrapServicio servicio;

    public BootstrapController(UnidadDeTrabajo unidad, BootstrapServicio servicio) {
        this.unidad = unidad;
        this.servicio = servicio;
    }

    @GetMapping(value = "/bootstrap/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login() {
        try {
            JsonObject datos = unidad.ejecutar(servicio::login);
            return ResponseEntity.ok(datos.toString());
        } catch (Exception e) {
            log.error("Fallo del bootstrap de login: {}", e.getClass().getSimpleName());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping(value = "/bootstrap/home", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> home(@RequestHeader(value = "X-SESSION-TOKEN", required = false) String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        try {
            BootstrapServicio.Home home = unidad.ejecutar(con -> servicio.home(con, token));
            if (home.valida()) {
                return ResponseEntity.ok(home.datos().toString());
            }
            return ResponseEntity.status(401).body(home.datos().toString());
        } catch (Exception e) {
            log.error("Fallo del bootstrap de home", e);
            return ResponseEntity.status(500).build();
        }
    }
}
