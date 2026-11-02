package cl.bcs.sura.controller;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.service.CrearClienteFullWSModificarPerfilesPortafolioService;
import cl.bcs.sura.service.ModificarClienteModificarPerfilesPortafolioWSService;
import cl.bcs.sura.service.PerfilesPortafolioService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/sura/perfiles")
public class SuraController {

    private final CrearClienteFullWSModificarPerfilesPortafolioService crearClienteFull;
    private final ModificarClienteModificarPerfilesPortafolioWSService modificarClienteWS;

    public SuraController(CrearClienteFullWSModificarPerfilesPortafolioService crearClienteFull,
                          ModificarClienteModificarPerfilesPortafolioWSService modificarClienteWS) {
        this.crearClienteFull = crearClienteFull;
        this.modificarClienteWS = modificarClienteWS;
    }

    @PostMapping("/validar-apv")
    public ResponseEntity<List<IdsEntidadYPerfiles>> validarApv(
            @RequestBody List<IdsEntidadYPerfiles> perfiles) {
        Objects.requireNonNull(perfiles, "perfiles requeridos");
        return ResponseEntity.ok(PerfilesPortafolioService.validarIndicadorFormaCobroPortafolioApv(perfiles));
    }

    @PostMapping("/filtrar-insertar")
    public ResponseEntity<List<IdsEntidadYPerfiles>> filtrarInsertar(
            @RequestBody List<IdsEntidadYPerfiles> perfiles) {
        Objects.requireNonNull(perfiles, "perfiles requeridos");
        return ResponseEntity.ok(PerfilesPortafolioService.filtrarParaIngresarIndicadores(perfiles));
    }

    @PostMapping("/filtrar-modificar")
    public ResponseEntity<List<IdsEntidadYPerfiles>> filtrarModificar(
            @RequestBody List<IdsEntidadYPerfiles> perfiles) {
        Objects.requireNonNull(perfiles, "perfiles requeridos");
        return ResponseEntity.ok(PerfilesPortafolioService.filtrarParaModificarIndicadores(perfiles));
    }

    @PostMapping("/filtrar-con-forma-cobro")
    public ResponseEntity<List<IdsEntidadYPerfiles>> filtrarConFormaCobro(
            @RequestBody List<IdsEntidadYPerfiles> perfiles) {
        Objects.requireNonNull(perfiles, "perfiles requeridos");
        return ResponseEntity.ok(PerfilesPortafolioService.filtrarConIdFormaCobro(perfiles));
    }

    /** B-SURA-23: same params as the JavaCall dispatch (jin merged with jout), same three-field output. */
    @PostMapping(path = "/crear-cliente", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> crearCliente(@RequestBody String request) {
        return ResponseEntity.ok(crearClienteFull.execute(parse(request)).toString());
    }

    @PostMapping(path = "/modificar-cliente", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> modificarCliente(@RequestBody String request) {
        return ResponseEntity.ok(modificarClienteWS.execute(parse(request)).toString());
    }

    private static JsonObject parse(String raw) {
        try {
            return JsonParser.parseString(raw).getAsJsonObject();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON body");
        }
    }
}
