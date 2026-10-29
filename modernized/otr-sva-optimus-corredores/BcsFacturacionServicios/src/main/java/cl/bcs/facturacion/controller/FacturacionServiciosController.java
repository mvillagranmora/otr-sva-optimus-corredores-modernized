package cl.bcs.facturacion.controller;

import cl.bcs.facturacion.domain.Comprobante;
import cl.bcs.facturacion.service.FacturacionServiciosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST controller for BcsFacturacionServicios.
 *
 * Endpoints:
 *   GET  /facturacion-servicios/tipos-factura
 *   GET  /facturacion-servicios/tipos-nota-credito
 *   GET  /facturacion-servicios/es-factura/{tipo}
 *   GET  /facturacion-servicios/comprobantes/{idEmpresa}?estado=
 *   GET  /facturacion-servicios/fecha-sistema/{idEmpresa}
 *   POST /facturacion-servicios/procesar-facturacion-custodia  → UOE → 503
 *   POST /facturacion-servicios/crear-comprobante              → UOE → 503
 */
@RestController
@RequestMapping("/facturacion-servicios")
public class FacturacionServiciosController {

    private final FacturacionServiciosService service;

    public FacturacionServiciosController(FacturacionServiciosService service) {
        this.service = service;
    }

    @GetMapping("/tipos-factura")
    public ResponseEntity<List<String>> getTiposFactura() {
        return ResponseEntity.ok(FacturacionServiciosService.TIPOS_FACTURA);
    }

    @GetMapping("/tipos-nota-credito")
    public ResponseEntity<List<String>> getTiposNotaCredito() {
        return ResponseEntity.ok(FacturacionServiciosService.TIPOS_NOTA_CREDITO);
    }

    @GetMapping("/es-factura/{tipo}")
    public ResponseEntity<Boolean> esFactura(@PathVariable String tipo) {
        Objects.requireNonNull(tipo, "tipo must not be null");
        return ResponseEntity.ok(FacturacionServiciosService.esTipoFactura(tipo));
    }

    @GetMapping("/fecha-sistema/{idEmpresa}")
    public ResponseEntity<String> getFechaSistema(@PathVariable Long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        return ResponseEntity.ok(service.obtenerFechaSistema(idEmpresa));
    }

    @GetMapping("/comprobantes/{idEmpresa}")
    public ResponseEntity<List<Comprobante>> getComprobantes(
            @PathVariable Long idEmpresa,
            @RequestParam String estado) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        Objects.requireNonNull(estado, "estado must not be null");
        return ResponseEntity.ok(service.consultarComprobantes(idEmpresa, estado));
    }

    @PostMapping("/procesar-facturacion-custodia")
    public ResponseEntity<Void> procesarFacturacionCustodia(
            @RequestBody Map<String, Long> body) {
        Long idEmpresa = Objects.requireNonNull(body.get("idEmpresa"), "idEmpresa must not be null");
        Long idPortafolio = Objects.requireNonNull(body.get("idPortafolio"), "idPortafolio must not be null");
        service.procesarFacturacionCustodia(idEmpresa, idPortafolio);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/crear-comprobante")
    public ResponseEntity<Void> crearComprobante(
            @RequestBody Map<String, Object> body) {
        Long idEmpresa = Objects.requireNonNull(
                body.get("idEmpresa") instanceof Number n ? n.longValue() : null,
                "idEmpresa must not be null");
        String tipoComprobante = Objects.requireNonNull(
                (String) body.get("tipoComprobante"), "tipoComprobante must not be null");
        service.crearComprobante(idEmpresa, tipoComprobante);
        return ResponseEntity.ok().build();
    }
}
