package cl.bcs.cartolas.jobs.adapter.in;

import cl.bcs.cartolas.jobs.domain.CartolasJobRequest;
import cl.bcs.cartolas.jobs.domain.TipoProceso;
import cl.bcs.cartolas.jobs.port.in.CartolasJobUseCase;
import cl.bcs.cartolas.jobs.service.CartolasJobService;
import cl.bcs.cartolas.jobs.service.ProgramacionCartolasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * Controlador REST para los jobs de cartolas.
 *
 * Modernización de: BcsJobCartolas + BcsScheduledJobCartolas (BcsJob / TomEE Scheduler)
 *
 * B-CAR-01: POST /api/jobs/cartolas/ejecutar      — proceso principal
 * B-CAR-07: POST /api/jobs/cartolas/programado/ejecutar — proceso programado
 * B-CAR-08: GET  /api/jobs/cartolas/flujo/{tipoProceso} — consulta flujo (diagnóstico, lógica pura)
 */
@RestController
@RequestMapping("/api/jobs/cartolas")
public class CartolasJobController {

    private final CartolasJobUseCase cartolasJobUseCase;
    private final ProgramacionCartolasService programacionService;

    public CartolasJobController(CartolasJobUseCase cartolasJobUseCase,
                                 ProgramacionCartolasService programacionService) {
        this.cartolasJobUseCase  = cartolasJobUseCase;
        this.programacionService = programacionService;
    }

    /**
     * B-CAR-01: Ejecuta el proceso principal de cartolas.
     *
     * Body esperado:
     * <pre>{
     *   "jobId":      123,
     *   "idEmpresa":  1,
     *   "tipoProceso": "NORMAL"
     * }</pre>
     *
     * Tipos de proceso aceptados: PROGRAMADO, NORMAL, RECURRENTE, REPROCESO, REEMPAQUETA
     */
    @PostMapping("/ejecutar")
    public ResponseEntity<Map<String, Object>> ejecutar(@RequestBody Map<String, Object> body) {
        final long jobId     = parseLong(Objects.requireNonNull(body.get("jobId"),     "jobId requerido"));
        final long idEmpresa = parseLong(Objects.requireNonNull(body.get("idEmpresa"), "idEmpresa requerido"));
        final String tipo    = (String) Objects.requireNonNull(body.get("tipoProceso"), "tipoProceso requerido");

        if (tipo.isBlank()) {
            throw new IllegalArgumentException("tipoProceso no puede ser vacío");
        }

        final CartolasJobRequest request = new CartolasJobRequest(jobId, idEmpresa, tipo);
        cartolasJobUseCase.ejecutar(request);

        return ResponseEntity.ok(Map.of(
                "status",     "ok",
                "jobId",      jobId,
                "idEmpresa",  idEmpresa,
                "tipoProceso", tipo
        ));
    }

    /**
     * B-CAR-07: Ejecuta el proceso programado de cartolas.
     *
     * Oracle: BcsScheduledJobCartolas.transaction() → ProcesoProgramacionCartolas.procesar()
     *
     * Body esperado:
     * <pre>{ "idEmpresa": 1 }</pre>
     */
    @PostMapping("/programado/ejecutar")
    public ResponseEntity<Map<String, Object>> ejecutarProgramado(
            @RequestBody Map<String, Object> body) {
        final long idEmpresa = parseLong(
                Objects.requireNonNull(body.get("idEmpresa"), "idEmpresa requerido"));

        programacionService.procesarPendientes(idEmpresa);

        return ResponseEntity.ok(Map.of(
                "status",    "ok",
                "idEmpresa", idEmpresa
        ));
    }

    /**
     * B-CAR-08: Consulta el flujo de pasos para un tipo de proceso (lógica pura).
     *
     * No requiere HANA. Útil para diagnóstico y documentación.
     *
     * Ejemplo: GET /api/jobs/cartolas/flujo/NORMAL
     * Respuesta: { "tipoProceso": "NORMAL", "flujo": "PARAMETROS POBLAR_NOMINA CARGA_DATOS EMISION", "pasosActivos": 4 }
     */
    @GetMapping("/flujo/{tipoProceso}")
    public ResponseEntity<Map<String, Object>> consultarFlujo(@PathVariable String tipoProceso) {
        Objects.requireNonNull(tipoProceso, "tipoProceso no puede ser null");
        final String flujo = CartolasJobService.describirFlujo(tipoProceso);
        final int pasos    = CartolasJobService.contarPasosActivos(tipoProceso);

        return ResponseEntity.ok(Map.of(
                "tipoProceso",  tipoProceso,
                "flujo",        flujo,
                "pasosActivos", pasos
        ));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static long parseLong(Object value) {
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(value));
    }
}
