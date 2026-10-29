package cl.bcs.normativa.adapter.in;

import cl.bcs.normativa.port.in.FindConfiguracionUseCase;
import cl.bcs.normativa.port.in.FindParametroUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * REST controller para configuracion y parametros normativos.
 */
@RestController
@RequestMapping("/api/normativa")
public class ConfiguracionController {

    private final FindConfiguracionUseCase findConfiguracionUseCase;
    private final FindParametroUseCase findParametroUseCase;

    public ConfiguracionController(
            FindConfiguracionUseCase findConfiguracionUseCase,
            FindParametroUseCase findParametroUseCase) {
        this.findConfiguracionUseCase = findConfiguracionUseCase;
        this.findParametroUseCase = findParametroUseCase;
    }

    /** GET /api/normativa/empresas/{idEmpresa}/configuracion/{codigo} */
    @GetMapping("/empresas/{idEmpresa}/configuracion/{codigo}")
    public ResponseEntity<Map<String, String>> findConfiguracion(
            @PathVariable long idEmpresa,
            @PathVariable String codigo) {
        Objects.requireNonNull(codigo, "codigo no puede ser null");
        String valor = findConfiguracionUseCase.findValor(idEmpresa, codigo);
        return ResponseEntity.ok(Map.of("valor", valor));
    }

    /** GET /api/normativa/esquemas/{nombre}/{version}/parametros/{codigo} */
    @GetMapping("/esquemas/{nombre}/{version}/parametros/{codigo}")
    public ResponseEntity<Map<String, String>> findParametro(
            @PathVariable String nombre,
            @PathVariable String version,
            @PathVariable String codigo) {
        Objects.requireNonNull(nombre, "nombre no puede ser null");
        Objects.requireNonNull(version, "version no puede ser null");
        Objects.requireNonNull(codigo, "codigo no puede ser null");
        String valor = findParametroUseCase.findValor(nombre, version, codigo);
        return ResponseEntity.ok(Map.of("valor", valor));
    }
}
