package cl.bcs.mesa.controller;

import cl.bcs.mesa.domain.*;
import cl.bcs.mesa.port.out.MesaDataPort;
import cl.bcs.mesa.service.CalculadoraSpotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST API for BcsMesaServicios — Calculadora Spot.
 * Pure-logic endpoints run without HANA; HANA-dependent endpoints return 503.
 */
@RestController
@RequestMapping("/api/mesa/spot")
public class MesaController {

    private final MesaDataPort dataPort;

    public MesaController(MesaDataPort dataPort) {
        this.dataPort = dataPort;
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoSpot>> obtenerMovimientos(
            @RequestParam String fechaOperacion) {
        Objects.requireNonNull(fechaOperacion, "fechaOperacion requerida");
        List<MovimientoSpot> movimientos = dataPort.obtenerMovimientosSpot(fechaOperacion);
        List<MovimientoSpot> vigentes = CalculadoraSpotService.filtrarVigentes(movimientos);
        return ResponseEntity.ok(vigentes);
    }

    @PostMapping("/resumen")
    public ResponseEntity<SalidaResumenOperacionesSpot> resumenOperaciones(
            @RequestBody List<MovimientoSpot> movimientos) {
        Objects.requireNonNull(movimientos, "movimientos requeridos");
        return ResponseEntity.ok(CalculadoraSpotService.resumenOperaciones(movimientos));
    }

    @PostMapping("/breakeven")
    public ResponseEntity<Map<String, Object>> calcularBreakEven(
            @RequestParam String fechaOperacion,
            @RequestParam String idMoneda) {
        Objects.requireNonNull(fechaOperacion, "fechaOperacion requerida");
        Objects.requireNonNull(idMoneda,       "idMoneda requerida");
        List<PosicionInicial> posiciones = dataPort.obtenerPosicionesIniciales(fechaOperacion);
        return ResponseEntity.ok(Map.of("idMoneda", idMoneda,
            "posicion", CalculadoraSpotService.posicionInicialMoneda(posiciones, idMoneda)));
    }
}
