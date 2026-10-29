package cl.bcs.fondomutuo.adapter.in.web;

import cl.bcs.fondomutuo.domain.CalcularMontoNumeroCuotaEntrada;
import cl.bcs.fondomutuo.domain.CalcularMontoNumeroCuotaSalida;
import cl.bcs.fondomutuo.port.in.CalcularMontoNumeroCuotaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * REST adapter for monto/cuota calculation operations.
 * Exposes: POST /api/fondo-mutuo/calcular-cuota
 */
@RestController
@RequestMapping("/api/fondo-mutuo")
public class CalcularCuotaController {

    private final CalcularMontoNumeroCuotaUseCase useCase;

    public CalcularCuotaController(CalcularMontoNumeroCuotaUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/calcular-cuota")
    public ResponseEntity<CalcularMontoNumeroCuotaSalida> calcularCuota(
            @RequestBody CalcularMontoNumeroCuotaEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        return ResponseEntity.ok(useCase.calcular(entrada));
    }
}
