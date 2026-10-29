package cl.bcs.ordenes.controller;

import cl.bcs.ordenes.domain.ConstantesOrdenes;
import cl.bcs.ordenes.port.out.OrdenDataPort;
import cl.bcs.ordenes.service.CalcularMontoService;
import cl.bcs.ordenes.service.CrearOrdenesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * REST API for BcsOrdenServicios.
 * Pure-logic endpoints run without HANA; HANA-dependent endpoints return 503.
 */
@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenDataPort dataPort;

    public OrdenController(OrdenDataPort dataPort) {
        this.dataPort = dataPort;
    }

    @GetMapping("/constantes/estado-inicial")
    public ResponseEntity<Map<String, String>> estadoOrdenInicial() {
        return ResponseEntity.ok(Map.of(
            "idEstadoOrden", ConstantesOrdenes.ESTADO_ORDEN_PENDIENTE_AUTORIZAR,
            "idEstadoInterno", ConstantesOrdenes.ESTADO_ORDEN_PENDIENTE_AUTORIZAR,
            "idEstadoLimite", ConstantesOrdenes.ESTADO_ORDEN_AUTORIZADA
        ));
    }

    @PostMapping("/bloqueo-id")
    public ResponseEntity<Map<String, String>> buildBloqueaOrdenId(
            @RequestParam Long idPortafolio,
            @RequestParam Long idInstrumento,
            @RequestParam String idTipoOperacion,
            @RequestParam BigDecimal cantidad,
            @RequestParam BigDecimal precioLimite,
            @RequestParam String fechaSistema) {
        Objects.requireNonNull(idPortafolio,   "idPortafolio requerido");
        Objects.requireNonNull(idInstrumento,  "idInstrumento requerido");
        Objects.requireNonNull(idTipoOperacion,"idTipoOperacion requerido");
        Objects.requireNonNull(cantidad,       "cantidad requerida");
        Objects.requireNonNull(precioLimite,   "precioLimite requerido");
        Objects.requireNonNull(fechaSistema,   "fechaSistema requerida");
        String key = CrearOrdenesService.buildIdBloqueaOrden(
            idPortafolio, idInstrumento, idTipoOperacion, cantidad, precioLimite, fechaSistema);
        return ResponseEntity.ok(Map.of("idBloqueaOrden", key));
    }

    @PostMapping("/monto-paridad")
    public ResponseEntity<Map<String, Object>> calcularMontoParidad(
            @RequestParam BigDecimal precio,
            @RequestParam BigDecimal cantidad,
            @RequestParam BigDecimal factor) {
        Objects.requireNonNull(precio,   "precio requerido");
        Objects.requireNonNull(cantidad, "cantidad requerida");
        Objects.requireNonNull(factor,   "factor requerido");
        BigDecimal monto = CalcularMontoService.calculaMontoParidad(precio, cantidad, factor);
        return ResponseEntity.ok(Map.of("monto", monto));
    }

    @PostMapping("/crear")
    public ResponseEntity<Map<String, String>> crearOrden(
            @RequestParam String idPortafolio,
            @RequestParam String idTipoOperacion) {
        Objects.requireNonNull(idPortafolio,   "idPortafolio requerido");
        Objects.requireNonNull(idTipoOperacion,"idTipoOperacion requerido");
        dataPort.registrarOrden(idPortafolio, idTipoOperacion);
        return ResponseEntity.ok(Map.of("estado", ConstantesOrdenes.ESTADO_ORDEN_PENDIENTE_AUTORIZAR));
    }
}
