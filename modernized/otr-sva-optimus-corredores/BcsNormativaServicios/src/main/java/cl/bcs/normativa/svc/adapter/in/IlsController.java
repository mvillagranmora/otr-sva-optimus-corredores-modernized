package cl.bcs.normativa.svc.adapter.in;

import cl.bcs.normativa.svc.domain.ils.CalcularObligacionesEntrada;
import cl.bcs.normativa.svc.domain.ils.CalcularObligacionesSalida;
import cl.bcs.normativa.svc.service.ils.IlsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones ILS (Indicadores de Liquidez y Solvencia).
 *
 * <p>Puerto de entrada HTTP para los casos de uso ILS.
 */
@RestController
@RequestMapping("/api/normativa/ils")
public class IlsController {

    private final IlsService ilsService;

    public IlsController(IlsService ilsService) {
        this.ilsService = ilsService;
    }

    /**
     * Calcula obligaciones y derechos en moneda extranjera (ILS).
     *
     * <p>Stub: retorna 503 hasta que se conecte el port HANA.
     *
     * @param entrada parámetros de empresa y fecha contable
     * @return resultado del cálculo (503 mientras no hay HANA)
     */
    @PostMapping("/calcular-obligaciones")
    public ResponseEntity<CalcularObligacionesSalida> calcularObligaciones(
            @RequestBody CalcularObligacionesEntrada entrada) {
        CalcularObligacionesSalida salida = ilsService.calcularObligaciones(entrada);
        return ResponseEntity.ok(salida);
    }
}
