package cl.bcs.valo2.controller;

import cl.bcs.valo2.domain.bono.ValorizarBonosEntrada;
import cl.bcs.valo2.domain.bono.ValorizarBonosResultado;
import cl.bcs.valo2.domain.calendario.ContarDiasEntrada;
import cl.bcs.valo2.domain.calendario.ContarDiasResultado;
import cl.bcs.valo2.domain.pagare.ValorizarPagareEntrada;
import cl.bcs.valo2.domain.pagare.ValorizarPagareResultado;
import cl.bcs.valo2.port.out.ValorizadorDataPort;
import cl.bcs.valo2.service.ContadorDiasService;
import cl.bcs.valo2.service.PagaresService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/valorizar")
public class ValorizadorController {

    private final ValorizadorDataPort dataPort;

    public ValorizadorController(ValorizadorDataPort dataPort) {
        this.dataPort = dataPort;
    }

    @PostMapping("/pagares")
    public ResponseEntity<ValorizarPagareResultado> valorizarPagare(@RequestBody ValorizarPagareEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada requerida");
        double fraccion = PagaresService.rescate(entrada.tipoInteres(), 0, 0, 0); // guard call order
        fraccion = (double) entrada.plazoEnDias() / entrada.diasAno();
        double captacion = PagaresService.captacion(entrada.tipoInteres(), fraccion, entrada.rescate(), entrada.tasa());
        double rescate   = PagaresService.rescate(entrada.tipoInteres(), fraccion, entrada.captacion(), entrada.tasa());
        return ResponseEntity.ok(new ValorizarPagareResultado(captacion, rescate, fraccion, 0, "OK"));
    }

    @PostMapping("/bonos")
    public ResponseEntity<ValorizarBonosResultado> valorizarBono(@RequestBody ValorizarBonosEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada requerida");
        Objects.requireNonNull(entrada.nemo(), "nemo requerido");
        // Full bono valuation requires HANA — delegates to port (throws UnsupportedOperationException)
        dataPort.buscarBono(entrada.nemo());
        return ResponseEntity.ok(null); // unreachable without HANA
    }

    @PostMapping("/calendario/dias")
    public ResponseEntity<ContarDiasResultado> contarDias(@RequestBody ContarDiasEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada requerida");
        Objects.requireNonNull(entrada.fechaDesde(), "fechaDesde requerida");
        Objects.requireNonNull(entrada.fechaHasta(), "fechaHasta requerida");
        Objects.requireNonNull(entrada.convencionDias(), "convencionDias requerida");
        // BUS/252 requiere feriados de HANA — retorna 503 en lugar de resultado silenciosamente incorrecto
        if (ContadorDiasService.ALIAS_BUS252.contains(entrada.convencionDias())) {
            throw new UnsupportedOperationException(
                "BUS/252 requiere feriados (HANA diferido a QA): use /api/valorizar/calendario/dias con HANA disponible");
        }
        int dias = ContadorDiasService.contarDias(
                entrada.fechaDesde(), entrada.fechaHasta(),
                entrada.convencionDias(), entrada.ajusteEOM(),
                Set.of()
        );
        double fraccion = (double) dias / (ContadorDiasService.ALIAS_ACT_360.contains(entrada.convencionDias()) ? 360
                : ContadorDiasService.ALIAS_BUS252.contains(entrada.convencionDias()) ? 252 : 365);
        return ResponseEntity.ok(new ContarDiasResultado(dias, fraccion));
    }
}
