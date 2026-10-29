package cl.bcs.canalagente.adapter.in;

import cl.bcs.canalagente.domain.HistorialMovimientosRequest;
import cl.bcs.canalagente.domain.HistorialMovimientosResponse;
import cl.bcs.canalagente.port.in.ConsultarHistorialMovimientosUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * REST controller for BcsCanalAgenteServicios.
 *
 * Endpoints:
 *   POST /canal-agente/historial-movimientos
 *       Body: HistorialMovimientosRequest JSON
 *       Returns: HistorialMovimientosResponse JSON
 *
 *   GET  /canal-agente/health   → 200 OK
 */
@RestController
@RequestMapping("/canal-agente")
public class CanalAgenteController {

    private final ConsultarHistorialMovimientosUseCase useCase;

    public CanalAgenteController(ConsultarHistorialMovimientosUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/historial-movimientos")
    public ResponseEntity<HistorialMovimientosResponse> consultarHistorial(
            @RequestBody HistorialMovimientosRequest request) {
        Objects.requireNonNull(request, "request body must not be null");
        Objects.requireNonNull(request.idPortafolio(), "idPortafolio must not be null");
        Objects.requireNonNull(request.idEmpresa(), "idEmpresa must not be null");
        Objects.requireNonNull(request.idMoneda(), "idMoneda must not be null");
        return ResponseEntity.ok(useCase.consultar(request));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }
}
