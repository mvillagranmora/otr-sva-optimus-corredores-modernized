package cl.bcs.cuentainversion.controller;

import cl.bcs.cuentainversion.domain.*;
import cl.bcs.cuentainversion.port.in.CrearCargoAbonoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// B-CTA-14: MOVTRN remapping, replaces SOAP endpoint (D-CTA-01)
@RestController
@RequestMapping("/api/v1/cargo-abono")
public class CrearCargoAbonoController {

    private final CrearCargoAbonoUseCase useCase;

    public CrearCargoAbonoController(CrearCargoAbonoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<CrearCargoAbonoSalida> crear(@RequestBody CrearCargoAbonoRequest request) {
        CrearCargoAbonoEntrada entrada = new CrearCargoAbonoEntrada(
                request.idEmpresa(),
                request.idUsuario(),
                request.idModulo(),
                request.simularOperacion(),
                request.usaReglasNegocio(),
                request.fechaSistema(),
                request.esIngresoManual(),
                request.cargosAbonos());

        return ResponseEntity.ok(useCase.crear(entrada));
    }

    // B-CTA-14: MOVTRN field remapping from legacy SOAP request to domain record
    public record CrearCargoAbonoRequest(
            Long idEmpresa,
            Long idUsuario,
            String idModulo,
            String simularOperacion,
            String usaReglasNegocio,
            LocalDate fechaSistema,
            String esIngresoManual,
            List<CargoAbono> cargosAbonos) {}
}
