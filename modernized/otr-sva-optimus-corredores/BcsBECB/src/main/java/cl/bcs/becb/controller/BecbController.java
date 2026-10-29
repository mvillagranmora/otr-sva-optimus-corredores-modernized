package cl.bcs.becb.controller;

import cl.bcs.becb.domain.DataRiesgoFinanciero;
import cl.bcs.becb.domain.DataRiesgoFinancieroAgrupado;
import cl.bcs.becb.domain.EntradaGenerarInforme;
import cl.bcs.becb.domain.SalidaGenerarInforme;
import cl.bcs.becb.domain.TipoInforme;
import cl.bcs.becb.port.out.BecbDataPort;
import cl.bcs.becb.service.RiesgoFinancieroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/becb/riesgo")
public class BecbController {

    private final BecbDataPort dataPort;

    public BecbController(BecbDataPort dataPort) {
        this.dataPort = dataPort;
    }

    @GetMapping("/tabla")
    public ResponseEntity<Map<String, String>> determinarTabla(
            @RequestParam String fechaSistema,
            @RequestParam String fechaInforme) {
        Objects.requireNonNull(fechaSistema, "fechaSistema requerida");
        Objects.requireNonNull(fechaInforme, "fechaInforme requerida");
        String tabla = RiesgoFinancieroService.determinarTabla(
                LocalDate.parse(fechaSistema), LocalDate.parse(fechaInforme));
        return ResponseEntity.ok(Map.of("tabla", tabla));
    }

    @GetMapping("/tipo-informe/{codigo}")
    public ResponseEntity<Map<String, String>> resolveCodigoInforme(@PathVariable String codigo) {
        TipoInforme tipo = RiesgoFinancieroService.resolveCodigoInforme(codigo);
        if (tipo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Código de informe no configurado: " + codigo));
        }
        return ResponseEntity.ok(Map.of("tipoInforme", tipo.name()));
    }

    @PostMapping("/agrupar-rifi")
    public ResponseEntity<List<DataRiesgoFinancieroAgrupado>> agruparRifi(
            @RequestBody List<DataRiesgoFinanciero> filas) {
        Objects.requireNonNull(filas, "filas requeridas");
        return ResponseEntity.ok(RiesgoFinancieroService.agruparFilasRifi(filas));
    }

    @PostMapping("/generar")
    public ResponseEntity<SalidaGenerarInforme> generarInforme(
            @RequestBody EntradaGenerarInforme entrada) {
        Objects.requireNonNull(entrada, "entrada requerida");
        throw new UnsupportedOperationException("requires HANA: GenerarInformesRiesgoFinanciero (full report generation deferred to Phase 4)");
    }
}
