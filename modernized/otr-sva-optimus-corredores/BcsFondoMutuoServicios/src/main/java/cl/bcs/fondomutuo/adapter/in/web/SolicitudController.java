package cl.bcs.fondomutuo.adapter.in.web;

import cl.bcs.fondomutuo.service.solicitud.SolicitudAporteRescateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * REST adapter for solicitud (order) lifecycle operations.
 * Exposes HANA-backed operations via POST endpoints.
 */
@RestController
@RequestMapping("/api/fondo-mutuo/solicitud")
public class SolicitudController {

    private final SolicitudAporteRescateService service;

    public SolicitudController(SolicitudAporteRescateService service) {
        this.service = service;
    }

    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearSolicitud(@RequestBody Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        return ResponseEntity.ok(service.crearSolicitudNormal(params));
    }

    @PostMapping("/certificar")
    public ResponseEntity<Map<String, Object>> certificarSolicitud(@RequestBody Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        return ResponseEntity.ok(service.certificarSolicitud(params));
    }

    @PostMapping("/anular")
    public ResponseEntity<Map<String, Object>> anularSolicitud(@RequestBody Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        return ResponseEntity.ok(service.anularSolicitud(params));
    }

    @PostMapping("/liquidar")
    public ResponseEntity<Map<String, Object>> liquidarSolicitud(@RequestBody Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        return ResponseEntity.ok(service.liquidarSolicitud(params));
    }
}
