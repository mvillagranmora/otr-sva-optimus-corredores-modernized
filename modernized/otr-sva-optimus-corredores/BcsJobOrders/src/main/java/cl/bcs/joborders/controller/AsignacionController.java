package cl.bcs.joborders.controller;

import cl.bcs.joborders.domain.Asignacion;
import cl.bcs.joborders.domain.Orden;
import cl.bcs.joborders.domain.Transaccion;
import cl.bcs.joborders.port.out.AsignacionDataPort;
import cl.bcs.joborders.service.AsignacionCronologicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/asignacion")
public class AsignacionController {

    private final AsignacionDataPort dataPort;

    public AsignacionController(AsignacionDataPort dataPort) {
        this.dataPort = dataPort;
    }

    /**
     * Ejecuta la asignación cronológica para un libro de órdenes.
     * El cuerpo contiene { "idLibroOrdenes": "123" }.
     * Requiere HANA (UnsupportedOperationException → 503 via GlobalExceptionHandler).
     */
    @PostMapping("/cronologica/ejecutar")
    public ResponseEntity<Map<String, Object>> ejecutar(@RequestBody Map<String, String> params) {
        String idLibroOrdenes = Objects.requireNonNull(params.get("idLibroOrdenes"),
            "idLibroOrdenes requerido");
        if (idLibroOrdenes.isBlank()) throw new IllegalArgumentException("idLibroOrdenes no puede ser vacío");

        // Requiere HANA — stub lanza UnsupportedOperationException → 503
        List<Orden> ordenes          = dataPort.obtenerOrdenes(0L);
        List<Transaccion> transacciones = dataPort.obtenerTransacciones(0L);
        List<Asignacion> resultado   = AsignacionCronologicaService.asignar(ordenes, transacciones);

        return ResponseEntity.ok(Map.of(
            "idLibroOrdenes", idLibroOrdenes,
            "asignaciones", resultado.size()
        ));
    }

    /**
     * Verifica el estado de asignación sin ejecutar (BcsJobOrderChronologiAllocationReader).
     * Requiere HANA.
     */
    @PostMapping("/cronologica/verificar")
    public ResponseEntity<Map<String, Object>> verificar(@RequestBody Map<String, String> params) {
        String idLibroOrdenes = Objects.requireNonNull(params.get("idLibroOrdenes"),
            "idLibroOrdenes requerido");

        // Requiere HANA — stub → 503
        boolean activo = dataPort.libroOrdenesActivo(idLibroOrdenes);
        int existe = dataPort.existeAsignacionAutomaticaActiva(
            Integer.parseInt(idLibroOrdenes), "ASIG_CRONO");

        return ResponseEntity.ok(Map.of(
            "libroActivo", activo,
            "asignacionActivaExiste", existe > 0
        ));
    }
}
