package cl.bcs.cuentainversion.controller;

import cl.bcs.cuentainversion.domain.Movimiento;
import cl.bcs.cuentainversion.port.in.ConsultarMovimientoUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// B-CTA-11/11b: TipoFechaConsulta routing; Origen derivation delegated to service (MED-5 fix)
@RestController
@RequestMapping("/api/v1/movimientos")
public class ConsultarMovimientosController {

    private final ConsultarMovimientoUseCase useCase;

    public ConsultarMovimientosController(ConsultarMovimientoUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<List<Movimiento>> consultar(
            @RequestParam Long idEmpresa,
            @RequestParam Long idCuentaInversion,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "false") boolean incluyeAnulados) {

        return ResponseEntity.ok(
                useCase.consultar(idEmpresa, idCuentaInversion, fechaDesde, fechaHasta, incluyeAnulados));
    }
}
