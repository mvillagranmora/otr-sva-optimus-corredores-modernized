package cl.bcs.plataforma.adapter.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.login.UnidadDeTrabajo;
import cl.bcs.plataforma.login.sesion.CrearSesionServicio;

/**
 * {@code POST /PLA/CrearSesion}: user and password to a session token (legacy {@code CrearSesion} servlet, {@code CrearSesion.java:106-144}).
 *
 * <p>Request: {@code {"p_IdEmpresa": 1, "p_NombreUsuario": "USER", "p_ClaveUsuario": "<SHA-256 hex of the password>", "p_IdCanal": "COR"}}.
 * Success: 200 with the legacy shape ({@code P_OUT_ENTIDAD} empty, {@code P_OUT_TOKEN}, the two counters and the two detail arrays, T-09). Every
 * refusal, whatever the cause, and every failure: 401 {@code USUARIO_CLAVE_INCORRECTA}. The transaction commits after a refusal (the attempt counter
 * and the temporary block are confirmed) and rolls back after an exception (T-13).
 */
@RestController
public class CrearSesionController {

    private static final Logger log = LoggerFactory.getLogger(CrearSesionController.class);

    private final UnidadDeTrabajo unidad;
    private final CrearSesionServicio servicio;

    public CrearSesionController(UnidadDeTrabajo unidad, CrearSesionServicio servicio) {
        this.unidad = unidad;
        this.servicio = servicio;
    }

    @PostMapping(value = "/CrearSesion", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> crearSesion(@RequestBody(required = false) String body) {
        JsonObject in;
        try {
            in = JsonParser.parseString(body == null ? "{}" : body).getAsJsonObject();
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(errorPorDefecto());
        }
        String idEmpresa = texto(in, "p_IdEmpresa");
        String nombreUsuario = texto(in, "p_NombreUsuario");
        String claveUsuario = texto(in, "p_ClaveUsuario");
        String idCanal = texto(in, "p_IdCanal");
        try {
            CrearSesionServicio.Resultado r = unidad
                    .ejecutar(con -> servicio.crearSesionUsuario(con, Long.parseLong(idEmpresa), nombreUsuario, claveUsuario, idCanal));
            if (!r.entidad.equalsIgnoreCase("")) {
                return ResponseEntity.ok(exito(r));
            }
            return ResponseEntity.status(401).body(errorPorDefecto());
        } catch (Exception e) {
            log.warn("Error al iniciar sesion usuario {}: {}", nombreUsuario, e.getClass().getSimpleName());
            return ResponseEntity.status(401).body(errorPorDefecto());
        }
    }

    @GetMapping(value = "/CrearSesion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> crearSesionGet(@RequestBody(required = false) String body) {
        return crearSesion(body);
    }

    private static String texto(JsonObject o, String clave) {
        JsonElement e = o.get(clave);
        return e != null && !e.isJsonNull() ? e.getAsString() : "";
    }

    /** Legacy {@code CrearSesionSalida} serialised: the entity is blanked, the arrays are empty. */
    private static String exito(CrearSesionServicio.Resultado r) {
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ENTIDAD", "");
        out.addProperty("P_OUT_TOKEN", r.token);
        out.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0);
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        out.add("P_OUT_ADVERTENCIASDETALLE", new JsonArray());
        out.add("P_OUT_ERRORESDETALLE", new JsonArray());
        return out.toString();
    }

    private static String errorPorDefecto() {
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 1);
        JsonObject err = new JsonObject();
        err.addProperty("CODIGO", "USUARIO_CLAVE_INCORRECTA");
        err.addProperty("Descripcion", "Usuario o contrasena incorrecta.");
        JsonArray arr = new JsonArray();
        arr.add(err);
        out.add("P_OUT_ERRORESDETALLE", arr);
        return out.toString();
    }
}
