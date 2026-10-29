package cl.bcs.normativa.svc.adapter.in;

import cl.bcs.normativa.svc.service.circulares.CircularesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para generación de circulares CMF.
 *
 * <p>Todos los endpoints delegan a stubs que retornan 503 (UnsupportedOperationException)
 * hasta que los stored procedures HANA estén disponibles.
 */
@RestController
@RequestMapping("/api/normativa/circulares")
public class CircularesController {

    private final CircularesService circularesService;

    public CircularesController(CircularesService circularesService) {
        this.circularesService = circularesService;
    }

    @PostMapping("/generar-archivo-circular-1720")
    public ResponseEntity<Map<String, Object>> generarArchivoCircular1720(
            @RequestBody Map<String, Object> parametros) {
        return ResponseEntity.ok(circularesService.generarArchivoCircular1720(parametros));
    }

    @PostMapping("/generar-ncg-380-custodia")
    public ResponseEntity<Map<String, Object>> generarNCG380Custodia(
            @RequestBody Map<String, Object> parametros) {
        return ResponseEntity.ok(circularesService.generarNCG380Custodia(parametros));
    }

    @PostMapping("/generar-ncg-380")
    public ResponseEntity<Map<String, Object>> generarNCG380(
            @RequestBody Map<String, Object> parametros) {
        return ResponseEntity.ok(circularesService.generarNCG380(parametros));
    }

    @PostMapping("/notificar-circular-1985")
    public ResponseEntity<Map<String, Object>> notificarCircular1985(
            @RequestBody Map<String, Object> parametros) {
        return ResponseEntity.ok(circularesService.notificarCircular1985(parametros));
    }

    @PostMapping("/generar-anexo1-circular-20")
    public ResponseEntity<Map<String, Object>> generarAnexo1Circular20(
            @RequestBody Map<String, Object> parametros) {
        return ResponseEntity.ok(circularesService.generarAnexo1Circular20(parametros));
    }

    @PostMapping("/notificar-ncg-365-ffmm")
    public ResponseEntity<Map<String, Object>> notificarNCG365FFMM(
            @RequestBody Map<String, Object> parametros) {
        return ResponseEntity.ok(circularesService.notificarNCG365FFMM(parametros));
    }

    @PostMapping("/consolidar-clientes")
    public ResponseEntity<Map<String, Object>> consolidarClientes(
            @RequestBody Map<String, Object> parametros) {
        return ResponseEntity.ok(circularesService.consolidarClientes(parametros));
    }

    @PostMapping("/generar-informe-mensual-inversionistas")
    public ResponseEntity<Map<String, Object>> generarInformeMensualInversionistas(
            @RequestBody Map<String, Object> parametros) {
        return ResponseEntity.ok(
                circularesService.generarInformeMensualInversionistasRV13419(parametros));
    }

    @PostMapping("/generar-datos-fatca")
    public ResponseEntity<Map<String, Object>> generarDatosFatca(
            @RequestBody Map<String, Object> parametros) {
        return ResponseEntity.ok(circularesService.generarDatosFatca(parametros));
    }
}
