package cl.bcs.normativa.svc.adapter.in;

import cl.bcs.normativa.svc.domain.cartola.CalcularRentabilidadEntrada;
import cl.bcs.normativa.svc.domain.cartola.CalcularRentabilidadSalida;
import cl.bcs.normativa.svc.service.cartola.RentabilidadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de cartola normativa.
 *
 * <p>Puerto de entrada HTTP para los casos de uso de cartola.
 */
@RestController
@RequestMapping("/api/normativa/cartola")
public class CartolaNormativaController {

    private final RentabilidadService rentabilidadService;

    public CartolaNormativaController(RentabilidadService rentabilidadService) {
        this.rentabilidadService = rentabilidadService;
    }

    /**
     * Calcula la rentabilidad MTD/YTD/ITD de los clientes en la cartola.
     *
     * <p>Equivale a {@code CalcularRentabilidad.call()} del sistema legacy.
     *
     * @param entrada datos de movimientos de caja, títulos y saldos finales
     * @return rentabilidad por cliente
     */
    @PostMapping("/calcular-rentabilidad")
    public ResponseEntity<CalcularRentabilidadSalida> calcularRentabilidad(
            @RequestBody CalcularRentabilidadEntrada entrada) {
        CalcularRentabilidadSalida salida = rentabilidadService.calcular(entrada);
        return ResponseEntity.ok(salida);
    }
}
