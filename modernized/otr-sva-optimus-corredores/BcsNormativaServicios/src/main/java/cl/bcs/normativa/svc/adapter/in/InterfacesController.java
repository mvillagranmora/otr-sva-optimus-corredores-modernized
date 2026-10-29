package cl.bcs.normativa.svc.adapter.in;

import cl.bcs.normativa.svc.service.interfaces.InterfacesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para generación de interfaces regulatorias CMF.
 *
 * <p>Todos los endpoints delegan a stubs que retornan 503 (UnsupportedOperationException)
 * hasta que los stored procedures HANA estén disponibles.
 */
@RestController
@RequestMapping("/api/normativa/interfaces")
public class InterfacesController {

    private final InterfacesService interfacesService;

    public InterfacesController(InterfacesService interfacesService) {
        this.interfacesService = interfacesService;
    }

    @PostMapping("/c08-cartera-normal")
    public ResponseEntity<Map<String, Object>> c08CarteraNormal(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazC08CarteraNormal(p));
    }

    @PostMapping("/c08-cartera-intermediada")
    public ResponseEntity<Map<String, Object>> c08CarteraIntermediada(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazC08CarteraIntermediada(p));
    }

    @PostMapping("/c08-cartera-pacteada")
    public ResponseEntity<Map<String, Object>> c08CarteraPacteada(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazC08CarteraPacteada(p));
    }

    @PostMapping("/c08-informacion-contable")
    public ResponseEntity<Map<String, Object>> c08InformacionContable(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazC08InformacionContable(p));
    }

    @PostMapping("/c40-cartera-normal")
    public ResponseEntity<Map<String, Object>> c40CarteraNormal(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazC40CarteraNormal(p));
    }

    @PostMapping("/c40-cartera-intermediada")
    public ResponseEntity<Map<String, Object>> c40CarteraIntermediada(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazC40CarteraIntermediada(p));
    }

    @PostMapping("/c40-informacion-contable")
    public ResponseEntity<Map<String, Object>> c40InformacionContable(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazC40InformacionContable(p));
    }

    @PostMapping("/p40")
    public ResponseEntity<Map<String, Object>> p40(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazP40(p));
    }

    @PostMapping("/p40-sbif")
    public ResponseEntity<Map<String, Object>> p40Sbif(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazP40SBIF(p));
    }

    @PostMapping("/icu")
    public ResponseEntity<Map<String, Object>> icu(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazICU(p));
    }

    @PostMapping("/cliente")
    public ResponseEntity<Map<String, Object>> cliente(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazCliente(p));
    }

    @PostMapping("/ordenes")
    public ResponseEntity<Map<String, Object>> ordenes(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazOrdenes(p));
    }

    @PostMapping("/sig")
    public ResponseEntity<Map<String, Object>> sig(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazSIG(p));
    }

    @PostMapping("/lavado-dinero")
    public ResponseEntity<Map<String, Object>> lavadoDinero(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazLavadoDinero(p));
    }

    @PostMapping("/instrumentos-financieros")
    public ResponseEntity<Map<String, Object>> instrumentosFinancieros(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazInstrumentosFinancieros(p));
    }

    @PostMapping("/archivo-cm")
    public ResponseEntity<Map<String, Object>> archivoCM(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazArchivoCM(p));
    }

    @PostMapping("/asignaciones")
    public ResponseEntity<Map<String, Object>> asignaciones(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazAsignaciones(p));
    }

    @PostMapping("/polizas")
    public ResponseEntity<Map<String, Object>> polizas(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazPolizas(p));
    }

    @PostMapping("/montos-intermediados")
    public ResponseEntity<Map<String, Object>> montosIntermediados(@RequestBody Map<String, Object> p) {
        return ResponseEntity.ok(interfacesService.generarInterfazMontosIntermediados(p));
    }
}
