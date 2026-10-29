package cl.bcs.ordenjobs.controller;

import cl.bcs.ordenjobs.port.out.OrdenJobDataPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/jobs/ordenes")
public class OrdenJobController {

    private final OrdenJobDataPort dataPort;

    public OrdenJobController(OrdenJobDataPort dataPort) {
        this.dataPort = dataPort;
    }

    private long idEmpresa(Map<String, String> body) {
        return Long.parseLong(Objects.requireNonNull(body.get("idEmpresa"), "idEmpresa requerido"));
    }

    private long idUsuario(Map<String, String> body) {
        return Long.parseLong(Objects.requireNonNull(body.get("idUsuario"), "idUsuario requerido"));
    }

    @PostMapping("/asignacion-automatica/ejecutar")
    public ResponseEntity<Map<String, String>> asignacionAutomatica(@RequestBody Map<String, String> body) {
        dataPort.ejecutarAsignacionAutomatica(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/cargar-archivo-ordenes-transacciones/ejecutar")
    public ResponseEntity<Map<String, String>> cargarArchivoOrdenesTransacciones(@RequestBody Map<String, String> body) {
        dataPort.cargarArchivoOrdenesTransacciones(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/cargar-transacciones-bolsa-electronica/ejecutar")
    public ResponseEntity<Map<String, String>> cargarTransaccionesBolsaElectronica(@RequestBody Map<String, String> body) {
        dataPort.cargarTransaccionesBolsaElectronica(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/control-mercado-carga-modificacion-ruebas/ejecutar")
    public ResponseEntity<Map<String, String>> controlMercadoCargaModificacionRuebas(@RequestBody Map<String, String> body) {
        dataPort.controlMercadoCargaModificacionRuebas(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/control-mercado-fix/ejecutar")
    public ResponseEntity<Map<String, String>> controlMercadoFix(@RequestBody Map<String, String> body) {
        dataPort.controlMercadoFix(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/control-mercado-fix-mme/ejecutar")
    public ResponseEntity<Map<String, String>> controlMercadoFixMME(@RequestBody Map<String, String> body) {
        dataPort.controlMercadoFixMME(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/control-mercado-market-data-palos-bec/ejecutar")
    public ResponseEntity<Map<String, String>> controlMercadoMarketDataPalosBEC(@RequestBody Map<String, String> body) {
        dataPort.controlMercadoMarketDataPalosBEC(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/control-mercado-market-data-palos-if/ejecutar")
    public ResponseEntity<Map<String, String>> controlMercadoMarketDataPalosIF(@RequestBody Map<String, String> body) {
        dataPort.controlMercadoMarketDataPalosIF(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/control-mercado-market-data-palos-op/ejecutar")
    public ResponseEntity<Map<String, String>> controlMercadoMarketDataPalosOP(@RequestBody Map<String, String> body) {
        dataPort.controlMercadoMarketDataPalosOP(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/control-mercado-market-data-palos-rf/ejecutar")
    public ResponseEntity<Map<String, String>> controlMercadoMarketDataPalosRF(@RequestBody Map<String, String> body) {
        dataPort.controlMercadoMarketDataPalosRF(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/control-mercado-market-data-palos-rv/ejecutar")
    public ResponseEntity<Map<String, String>> controlMercadoMarketDataPalosRV(@RequestBody Map<String, String> body) {
        dataPort.controlMercadoMarketDataPalosRV(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/control-mercado-market-data-palos-sim/ejecutar")
    public ResponseEntity<Map<String, String>> controlMercadoMarketDataPalosSIM(@RequestBody Map<String, String> body) {
        dataPort.controlMercadoMarketDataPalosSIM(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/control-mercado-modificacion-palos-ruebas/ejecutar")
    public ResponseEntity<Map<String, String>> controlMercadoModificacionPalosRuebas(@RequestBody Map<String, String> body) {
        dataPort.controlMercadoModificacionPalosRuebas(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/control-mercado-palos-rv-mme/ejecutar")
    public ResponseEntity<Map<String, String>> controlMercadoPalosRVMME(@RequestBody Map<String, String> body) {
        dataPort.controlMercadoPalosRVMME(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/control-mercado-transacciones-bolsa-electronica/ejecutar")
    public ResponseEntity<Map<String, String>> controlMercadoTransaccionesBolsaElectronica(@RequestBody Map<String, String> body) {
        dataPort.controlMercadoTransaccionesBolsaElectronica(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/crear-ordenes-postreras/ejecutar")
    public ResponseEntity<Map<String, String>> crearOrdenesPostreras(@RequestBody Map<String, String> body) {
        dataPort.crearOrdenesPostreras(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/drop-copy-rv-marca-negociacion/ejecutar")
    public ResponseEntity<Map<String, String>> dropCopyRvMarcaNegociacion(@RequestBody Map<String, String> body) {
        dataPort.dropCopyRvMarcaNegociacion(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/generar-informe-estadisticas/ejecutar")
    public ResponseEntity<Map<String, String>> generarInformeEstadisticas(@RequestBody Map<String, String> body) {
        dataPort.generarInformeEstadisticas(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/market-data-op-instrumentos/ejecutar")
    public ResponseEntity<Map<String, String>> marketDataOpInstrumentos(@RequestBody Map<String, String> body) {
        dataPort.marketDataOpInstrumentos(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/market-data-op-precio-instrumentos/ejecutar")
    public ResponseEntity<Map<String, String>> marketDataOpPrecioInstrumentos(@RequestBody Map<String, String> body) {
        dataPort.marketDataOpPrecioInstrumentos(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/market-data-op-puntas/ejecutar")
    public ResponseEntity<Map<String, String>> marketDataOpPuntas(@RequestBody Map<String, String> body) {
        dataPort.marketDataOpPuntas(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/market-data-rv-instrumentos/ejecutar")
    public ResponseEntity<Map<String, String>> marketDataRvInstrumentos(@RequestBody Map<String, String> body) {
        dataPort.marketDataRvInstrumentos(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/market-data-rv-instrumentos-mme/ejecutar")
    public ResponseEntity<Map<String, String>> marketDataRvInstrumentosMME(@RequestBody Map<String, String> body) {
        dataPort.marketDataRvInstrumentosMME(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/market-data-rv-puntas/ejecutar")
    public ResponseEntity<Map<String, String>> marketDataRvPuntas(@RequestBody Map<String, String> body) {
        dataPort.marketDataRvPuntas(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/market-data-rv-puntas-mme/ejecutar")
    public ResponseEntity<Map<String, String>> marketDataRvPuntasMME(@RequestBody Map<String, String> body) {
        dataPort.marketDataRvPuntasMME(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/procesamiento-transacciones-operadores/ejecutar")
    public ResponseEntity<Map<String, String>> procesamientoTransaccionesOperadores(@RequestBody Map<String, String> body) {
        dataPort.procesamientoTransaccionesOperadores(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/refacturar-movimiento/ejecutar")
    public ResponseEntity<Map<String, String>> refacturarMovimiento(@RequestBody Map<String, String> body) {
        dataPort.refacturarMovimiento(idEmpresa(body), idUsuario(body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
