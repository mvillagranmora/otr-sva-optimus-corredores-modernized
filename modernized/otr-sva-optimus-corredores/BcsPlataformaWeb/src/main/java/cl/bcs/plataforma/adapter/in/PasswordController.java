package cl.bcs.plataforma.adapter.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cl.bcs.plataforma.login.UnidadDeTrabajo;
import cl.bcs.plataforma.login.clave.GestionClaveServicio;
import cl.bcs.plataforma.login.clave.GestionClaveServicio.Respuesta;

/**
 * Password endpoints of the login: {@code ActualizarClave}, {@code ValidaClaveProvisoria}, {@code SolicitudRecuperacionClave},
 * {@code RecuperacionClave} and {@code ConfirmarCambioClave} (T-12, T-24..T-27). None needs a session (they are exempt in the session filter): the
 * user proves who they are with the provisional password or the recovery token. One transaction per request (T-13): a normal answer is committed,
 * an unexpected failure rolls back and answers 500 (the legacy left the status to the container).
 */
@RestController
public class PasswordController {

    private static final Logger log = LoggerFactory.getLogger(PasswordController.class);

    private final UnidadDeTrabajo unidad;
    private final GestionClaveServicio servicio;

    public PasswordController(UnidadDeTrabajo unidad, GestionClaveServicio servicio) {
        this.unidad = unidad;
        this.servicio = servicio;
    }

    @PostMapping(value = "/ActualizarClave", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> actualizarClave(@RequestBody(required = false) String body,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-SESSION-TOKEN", required = false) String token) {
        return ejecutar("ActualizarClave", con -> servicio.actualizarClave(con, body, token));
    }

    @PostMapping(value = "/ValidaClaveProvisoria", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> validaClaveProvisoria(@RequestBody(required = false) String body) {
        return ejecutar("ValidaClaveProvisoria", con -> servicio.validaClaveProvisoria(con, body));
    }

    @PostMapping(value = "/SolicitudRecuperacionClave", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> solicitudRecuperacionClave(@RequestBody(required = false) String body) {
        return ejecutar("SolicitudRecuperacionClave", con -> servicio.solicitudRecuperacionClave(con, body == null ? "" : body));
    }

    @GetMapping(value = "/SolicitudRecuperacionClave", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> solicitudRecuperacionClaveGet(@RequestBody(required = false) String body) {
        return solicitudRecuperacionClave(body);
    }

    @PostMapping(value = "/RecuperacionClave", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> recuperacionClave(@RequestBody(required = false) String body) {
        return ejecutar("RecuperacionClave", con -> servicio.recuperacionClave(con, body));
    }

    @PostMapping(value = "/ConfirmarCambioClave", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> confirmarCambioClave(@RequestBody(required = false) String body) {
        return ejecutar("ConfirmarCambioClave", con -> servicio.confirmarCambioClave(con, body == null ? "" : body));
    }

    @GetMapping(value = "/ConfirmarCambioClave", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> confirmarCambioClaveGet() {
        Respuesta r = servicio.confirmarCambioClaveGet();
        return ResponseEntity.status(r.estado()).body(r.cuerpo());
    }

    private ResponseEntity<String> ejecutar(String nombreServicio, UnidadDeTrabajo.Trabajo<Respuesta> trabajo) {
        try {
            Respuesta r = unidad.ejecutar(trabajo);
            return ResponseEntity.status(r.estado()).body(r.cuerpo());
        } catch (Exception e) {
            log.error("Fallo de {}: {}", nombreServicio, e.getClass().getSimpleName());
            return ResponseEntity.status(500).build();
        }
    }
}
