package cl.bcs.socionegocio.controller;

import cl.bcs.socionegocio.domain.SocioNegocio;
import cl.bcs.socionegocio.port.out.SocioNegocioServiciosDataPort;
import cl.bcs.socionegocio.service.SocioNegocioServiciosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/socionegocio-servicios")
public class SocioNegocioServiciosController {

    private final SocioNegocioServiciosDataPort dataPort;

    public SocioNegocioServiciosController(SocioNegocioServiciosDataPort dataPort) {
        this.dataPort = dataPort;
    }

    @PostMapping("/iup/generar")
    public ResponseEntity<Map<String, String>> iupGenerar(@RequestBody Map<String, String> body) {
        String documentoIdentidad = Objects.requireNonNull(body.get("documentoIdentidad"), "documentoIdentidad is required");
        String tipoDocumentoIdentidad = Objects.requireNonNull(body.get("tipoDocumentoIdentidad"), "tipoDocumentoIdentidad is required");
        String tipoPersona = body.get("tipoPersona");
        String result = SocioNegocioServiciosService.iupGenerar(documentoIdentidad, tipoDocumentoIdentidad, tipoPersona);
        return ResponseEntity.ok(Map.of("iup", result));
    }

    @PostMapping("/iup/documento-asociado")
    public ResponseEntity<Map<String, String>> iupDocumentoAsociado(@RequestBody Map<String, String> body) {
        String documentoIdentidad = Objects.requireNonNull(body.get("documentoIdentidad"), "documentoIdentidad is required");
        String tipoDocumentoIdentidad = Objects.requireNonNull(body.get("tipoDocumentoIdentidad"), "tipoDocumentoIdentidad is required");
        String tipoPersona = body.get("tipoPersona");
        String result = SocioNegocioServiciosService.iupObtenerDocumentoAsociado(documentoIdentidad, tipoDocumentoIdentidad, tipoPersona);
        return ResponseEntity.ok(Map.of("documentoAsociado", result));
    }

    @PostMapping("/ofuscar")
    public ResponseEntity<Map<String, String>> ofuscar(@RequestBody Map<String, String> body) {
        String valor = Objects.requireNonNull(body.get("valor"), "valor is required");
        String result = SocioNegocioServiciosService.ofuscarDocumentoIdentidad(valor);
        return ResponseEntity.ok(Map.of("valorOfuscado", result));
    }

    @GetMapping("/socios/{idEmpresa}")
    public ResponseEntity<List<SocioNegocio>> consultarSociosNegocio(@PathVariable Long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        List<SocioNegocio> socios = dataPort.consultarSociosNegocio(idEmpresa);
        return ResponseEntity.ok(socios);
    }

    @GetMapping("/fecha-sistema/{idEmpresa}")
    public ResponseEntity<Map<String, String>> obtenerFechaSistema(@PathVariable Long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        String fecha = dataPort.obtenerFechaSistema(idEmpresa);
        return ResponseEntity.ok(Map.of("fechaSistema", fecha));
    }

    @PostMapping("/crear")
    public ResponseEntity<Void> crearSocioNegocio(@RequestBody Map<String, String> body) {
        Long idEmpresa = Long.parseLong(Objects.requireNonNull(body.get("idEmpresa"), "idEmpresa is required"));
        String documentoIdentidad = Objects.requireNonNull(body.get("documentoIdentidad"), "documentoIdentidad is required");
        dataPort.crearSocioNegocio(idEmpresa, documentoIdentidad);
        return ResponseEntity.ok().build();
    }
}
