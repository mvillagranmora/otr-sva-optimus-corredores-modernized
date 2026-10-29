package cl.bcs.normativa.adapter.in;

import cl.bcs.normativa.domain.Validacion;
import cl.bcs.normativa.port.in.FindValidacionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * REST controller para validaciones normativas.
 */
@RestController
@RequestMapping("/api/normativa/esquemas/{nombre}/{version}/validaciones")
public class ValidacionController {

    private final FindValidacionUseCase findValidacionUseCase;

    public ValidacionController(FindValidacionUseCase findValidacionUseCase) {
        this.findValidacionUseCase = findValidacionUseCase;
    }

    /** GET /api/normativa/esquemas/{nombre}/{version}/validaciones */
    @GetMapping
    public ResponseEntity<List<Validacion>> findActivas(
            @PathVariable String nombre,
            @PathVariable String version) {
        Objects.requireNonNull(nombre, "nombre no puede ser null");
        Objects.requireNonNull(version, "version no puede ser null");
        return ResponseEntity.ok(findValidacionUseCase.findActivasPorEsquema(nombre, version));
    }
}
