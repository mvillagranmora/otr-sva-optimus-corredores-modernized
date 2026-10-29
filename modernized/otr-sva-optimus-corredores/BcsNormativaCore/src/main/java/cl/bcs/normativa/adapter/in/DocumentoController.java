package cl.bcs.normativa.adapter.in;

import cl.bcs.normativa.domain.Documento;
import cl.bcs.normativa.port.in.FindDocumentoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * REST controller para operaciones sobre Documento normativo.
 */
@RestController
@RequestMapping("/api/normativa/documentos")
public class DocumentoController {

    private final FindDocumentoUseCase findDocumentoUseCase;

    public DocumentoController(FindDocumentoUseCase findDocumentoUseCase) {
        this.findDocumentoUseCase = findDocumentoUseCase;
    }

    /** GET /api/normativa/documentos/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<Documento> findById(@PathVariable long id) {
        Objects.requireNonNull(id);
        return ResponseEntity.ok(findDocumentoUseCase.findById(id));
    }

    /** PATCH /api/normativa/documentos/{id}/en-proceso */
    @PatchMapping("/{id}/en-proceso")
    public ResponseEntity<Documento> setEnProceso(
            @PathVariable long id,
            @RequestBody Map<String, Boolean> body) {
        Objects.requireNonNull(body, "body no puede ser null");
        Boolean enProceso = body.get("enProceso");
        Objects.requireNonNull(enProceso, "enProceso no puede ser null");
        return ResponseEntity.ok(findDocumentoUseCase.setEnProceso(id, enProceso));
    }
}
