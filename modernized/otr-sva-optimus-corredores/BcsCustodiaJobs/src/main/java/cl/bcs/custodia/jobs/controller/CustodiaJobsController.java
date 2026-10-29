package cl.bcs.custodia.jobs.controller;

import cl.bcs.custodia.jobs.domain.InstrumentoPrecio;
import cl.bcs.custodia.jobs.domain.PosicionCustodia;
import cl.bcs.custodia.jobs.port.out.CustodiaJobsDataPort;
import cl.bcs.custodia.jobs.service.CustodiaJobsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST controller for Custodia Jobs operations.
 *
 * Endpoint summary:
 *   GET  /api/custodia/jobs/fecha-sistema   — obtenerFechaSistema
 *   GET  /api/custodia/jobs/precios         — consultarPreciosInstrumentos
 *   PUT  /api/custodia/jobs/precio          — actualizarPrecioInstrumento
 *   GET  /api/custodia/jobs/posiciones      — consultarPosicionesCliente
 *   POST /api/custodia/jobs/vencimiento     — procesarVencimientoProductos (UOE -> 503)
 *   POST /api/custodia/jobs/tipificar       — tipificarInstrumentos (UOE -> 503)
 *   POST /api/custodia/jobs/validar-horario — esEnHorario (pure-logic, no DB)
 */
@RestController
@RequestMapping("/api/custodia/jobs")
public class CustodiaJobsController {

    private final CustodiaJobsDataPort dataPort;

    public CustodiaJobsController(CustodiaJobsDataPort dataPort) {
        this.dataPort = dataPort;
    }

    // -----------------------------------------------------------------------
    // Plain-SQL endpoints
    // -----------------------------------------------------------------------

    @GetMapping("/fecha-sistema")
    public ResponseEntity<String> getFechaSistema(@RequestParam Long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        String fecha = dataPort.obtenerFechaSistema(idEmpresa);
        return ResponseEntity.ok(fecha);
    }

    @GetMapping("/precios")
    public ResponseEntity<List<InstrumentoPrecio>> getPrecios(
            @RequestParam Long idEmpresa,
            @RequestParam String fecha) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        Objects.requireNonNull(fecha, "fecha must not be null");
        List<InstrumentoPrecio> precios = dataPort.consultarPreciosInstrumentos(idEmpresa, fecha);
        return ResponseEntity.ok(precios);
    }

    @PutMapping("/precio")
    public ResponseEntity<Void> actualizarPrecio(
            @RequestParam Long idInstrumento,
            @RequestParam BigDecimal precio,
            @RequestParam String fecha) {
        Objects.requireNonNull(idInstrumento, "idInstrumento must not be null");
        Objects.requireNonNull(precio, "precio must not be null");
        Objects.requireNonNull(fecha, "fecha must not be null");
        dataPort.actualizarPrecioInstrumento(idInstrumento, precio, fecha);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posiciones")
    public ResponseEntity<List<PosicionCustodia>> getPosiciones(
            @RequestParam Long idEmpresa,
            @RequestParam String fecha) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        Objects.requireNonNull(fecha, "fecha must not be null");
        List<PosicionCustodia> posiciones = dataPort.consultarPosicionesCliente(idEmpresa, fecha);
        return ResponseEntity.ok(posiciones);
    }

    // -----------------------------------------------------------------------
    // HANA-SP endpoints (return 503 when UnsupportedOperationException thrown)
    // -----------------------------------------------------------------------

    @PostMapping("/vencimiento")
    public ResponseEntity<Void> procesarVencimiento(
            @RequestParam Long idEmpresa,
            @RequestParam String tipoProducto,
            @RequestParam String fecha) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        Objects.requireNonNull(tipoProducto, "tipoProducto must not be null");
        Objects.requireNonNull(fecha, "fecha must not be null");
        dataPort.procesarVencimientoProductos(idEmpresa, tipoProducto, fecha);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tipificar")
    public ResponseEntity<Void> tipificarInstrumentos(@RequestParam Long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        dataPort.tipificarInstrumentos(idEmpresa);
        return ResponseEntity.ok().build();
    }

    // -----------------------------------------------------------------------
    // Pure-logic endpoints
    // -----------------------------------------------------------------------

    /**
     * Validates whether a given datetime falls within an exercise/blockage schedule window.
     *
     * Request body: { "fechaLocal": "2024-01-15T10:30:00", "horaIni": "09:00:00", "horaTer": "17:00:00" }
     * Response: true | false
     *
     * Legacy: ValidarHorarioEjercicioBloqueoOpciones.java lines 98-103
     */
    @PostMapping("/validar-horario")
    public ResponseEntity<Boolean> validarHorario(@RequestBody Map<String, String> body) {
        Objects.requireNonNull(body.get("fechaLocal"), "fechaLocal must not be null");
        Objects.requireNonNull(body.get("horaIni"), "horaIni must not be null");
        Objects.requireNonNull(body.get("horaTer"), "horaTer must not be null");

        LocalDateTime fechaLocal = LocalDateTime.parse(body.get("fechaLocal"));
        LocalTime horaIni = LocalTime.parse(body.get("horaIni"));
        LocalTime horaTer = LocalTime.parse(body.get("horaTer"));

        boolean resultado = CustodiaJobsService.esEnHorario(fechaLocal, horaIni, horaTer);
        return ResponseEntity.ok(resultado);
    }
}
