package cl.bcs.normativa.adapter.in;

import cl.bcs.normativa.domain.Empresa;
import cl.bcs.normativa.port.in.FindEmpresaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller para datos de empresa.
 */
@RestController
@RequestMapping("/api/normativa/empresas")
public class EmpresaController {

    private final FindEmpresaUseCase findEmpresaUseCase;

    public EmpresaController(FindEmpresaUseCase findEmpresaUseCase) {
        this.findEmpresaUseCase = findEmpresaUseCase;
    }

    /** GET /api/normativa/empresas/{idEmpresa} */
    @GetMapping("/{idEmpresa}")
    public ResponseEntity<Empresa> findById(@PathVariable long idEmpresa) {
        return ResponseEntity.ok(findEmpresaUseCase.findById(idEmpresa));
    }

    /** GET /api/normativa/empresas/{idEmpresa}/corredora?idBolsa= */
    @GetMapping("/{idEmpresa}/corredora")
    public ResponseEntity<Map<String, String>> findCodigoCorredora(
            @PathVariable long idEmpresa,
            @RequestParam String idBolsa) {
        Objects.requireNonNull(idBolsa, "idBolsa no puede ser null");
        Optional<String> codigo = findEmpresaUseCase.findCodigoCorredora(idEmpresa, idBolsa);
        return codigo
                .map(c -> ResponseEntity.ok(Map.of("codigoCorredora", c)))
                .orElse(ResponseEntity.noContent().build());
    }
}
