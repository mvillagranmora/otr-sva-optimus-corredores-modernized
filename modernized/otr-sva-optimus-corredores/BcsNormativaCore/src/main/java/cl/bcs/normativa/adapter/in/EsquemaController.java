package cl.bcs.normativa.adapter.in;

import cl.bcs.normativa.domain.Esquema;
import cl.bcs.normativa.port.in.FindEsquemaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * REST controller para operaciones sobre Esquema normativo.
 */
@RestController
@RequestMapping("/api/normativa/esquemas")
public class EsquemaController {

    private final FindEsquemaUseCase findEsquemaUseCase;

    public EsquemaController(FindEsquemaUseCase findEsquemaUseCase) {
        this.findEsquemaUseCase = findEsquemaUseCase;
    }

    /** GET /api/normativa/esquemas/{nombre}/{version} */
    @GetMapping("/{nombre}/{version}")
    public ResponseEntity<Esquema> findByNombreVersion(
            @PathVariable String nombre,
            @PathVariable String version) {
        Objects.requireNonNull(nombre, "nombre no puede ser null");
        Objects.requireNonNull(version, "version no puede ser null");
        return ResponseEntity.ok(findEsquemaUseCase.findByNombreVersion(nombre, version));
    }
}
