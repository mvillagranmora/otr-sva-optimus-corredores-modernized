package cl.bcs.fondomutuo.adapter.in.web;

import cl.bcs.fondomutuo.domain.CalcularSpreadRescateEntrada;
import cl.bcs.fondomutuo.domain.CalcularSpreadRescateSalida;
import cl.bcs.fondomutuo.port.in.CalcularSpreadRescateUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * REST adapter for spread calculation on rescate operations.
 * Exposes: POST /api/fondo-mutuo/calcular-spread
 */
@RestController
@RequestMapping("/api/fondo-mutuo")
public class CalcularSpreadController {

    private final CalcularSpreadRescateUseCase useCase;

    public CalcularSpreadController(CalcularSpreadRescateUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/calcular-spread")
    public ResponseEntity<CalcularSpreadRescateSalida> calcularSpread(
            @RequestBody CalcularSpreadRescateEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        return ResponseEntity.ok(useCase.calcular(entrada));
    }
}
