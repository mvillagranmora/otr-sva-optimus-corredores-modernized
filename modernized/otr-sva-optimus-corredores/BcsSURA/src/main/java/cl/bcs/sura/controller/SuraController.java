package cl.bcs.sura.controller;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.service.PerfilesPortafolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/sura/perfiles")
public class SuraController {

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

    @PostMapping("/crear-cliente")
    public ResponseEntity<?> crearCliente(@RequestBody Object request) {
        throw new UnsupportedOperationException(
                "requires SURA SOAP sandbox: CrearClienteFullWSModificarPerfilesPortafolio (deferred to Phase 4)");
    }

    @PostMapping("/modificar-cliente")
    public ResponseEntity<?> modificarCliente(@RequestBody Object request) {
        throw new UnsupportedOperationException(
                "requires SURA SOAP sandbox: ModificarClienteModificarPerfilesPortafolioWS (deferred to Phase 4)");
    }
}
