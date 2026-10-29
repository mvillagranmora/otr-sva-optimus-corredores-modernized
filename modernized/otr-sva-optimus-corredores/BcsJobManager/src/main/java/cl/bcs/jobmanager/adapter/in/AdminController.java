package cl.bcs.jobmanager.adapter.in;

import cl.bcs.jobmanager.port.out.NodoPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST admin controller.
 *
 * Replaces legacy cl.bcs.scheduler.web.WebAdmin servlet.
 * Migrated from @WebServlet("/admin") to Spring @RestController at /api/admin.
 *
 * There is no retry endpoint: the dispatcher never changes the state of a program (no automatic or manual retry
 * here; manual re-runs are ReprocesarPrograma, as in the legacy).
 *
 * Note: restart/log actions from legacy WebAdmin execute system commands and
 * are intentionally excluded here — in Kubernetes/containerized deployments
 * the scheduler is restarted via the orchestration layer (liveness probe /
 * rolling restart), not via a management endpoint.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final NodoPort     nodoPort;

    public AdminController(NodoPort nodoPort) {
        this.nodoPort     = Objects.requireNonNull(nodoPort);
    }

    /**
     * Health check — verifies DB connectivity.
     *
     * GET /api/admin/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean dbAlive = nodoPort.pingDb();
        if (dbAlive) {
            return ResponseEntity.ok(Map.of("status", "UP", "db", "connected"));
        }
        return ResponseEntity.status(503)
                .body(Map.of("status", "DOWN", "db", "unreachable"));
    }

    /**
     * Returns active zombie nodes (nodes whose heartbeat has expired).
     *
     * GET /api/admin/zombies?timeoutSeconds=30
     */
    @GetMapping("/zombies")
    public ResponseEntity<Map<String, Object>> zombies(
            @RequestParam(defaultValue = "30") int timeoutSeconds) {

        if (timeoutSeconds < 1) {
            throw new IllegalArgumentException("timeoutSeconds must be >= 1");
        }
        List<Integer> pids = nodoPort.getZombieNodos(timeoutSeconds, false);
        return ResponseEntity.ok(Map.of("zombies", pids, "count", pids.size()));
    }
}
