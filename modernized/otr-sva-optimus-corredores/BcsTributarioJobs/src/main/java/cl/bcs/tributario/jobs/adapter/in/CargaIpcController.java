package cl.bcs.tributario.jobs.adapter.in;

import cl.bcs.tributario.jobs.domain.CargaIpcResultado;
import cl.bcs.tributario.jobs.service.CargaIpcJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * REST adapter (inbound) for triggering the IPC factor-load job.
 *
 * <p>The {@code /carga-ipc} endpoint maps to the legacy
 * {@code BcsJobCargaIPC#transaction} entry point. HTTP 503 is returned via
 * {@link cl.bcs.tributario.jobs.infrastructure.GlobalExceptionHandler}
 * when the underlying HANA stored procedure is unavailable.</p>
 */
@RestController
@RequestMapping("/api/tributario/jobs")
public class CargaIpcController {

    private final CargaIpcJobService jobService;

    public CargaIpcController(CargaIpcJobService jobService) {
        this.jobService = jobService;
    }

    /**
     * Triggers the IPC factor load job.
     *
     * <p>Legacy defaults (B-TRI-04):
     * {@code idEmpresa=1}, {@code idUsuario=3}.</p>
     *
     * @param idEmpresa        company identifier (default: 1)
     * @param idUsuario        user identifier (default: 3)
     * @param idModulo         module code, e.g. {@code "TRI"} (required)
     * @param simularOperacion simulate-only flag (default: empty string)
     * @param usaReglasNegocio apply business rules flag (default: empty string)
     * @return 200 with {@link CargaIpcResultado} on success; 503 when HANA
     *         SP is unavailable; 400 on missing/invalid parameters
     */
    @PostMapping("/carga-ipc")
    public ResponseEntity<CargaIpcResultado> ejecutarCargaIpc(
            @RequestParam(defaultValue = "1")  Long   idEmpresa,
            @RequestParam(defaultValue = "3")  Long   idUsuario,
            @RequestParam                      String idModulo,
            @RequestParam(defaultValue = "")   String simularOperacion,
            @RequestParam(defaultValue = "")   String usaReglasNegocio) {

        Objects.requireNonNull(idModulo, "idModulo es requerido");

        CargaIpcResultado resultado = jobService.ejecutar(
                idEmpresa, idUsuario, idModulo, simularOperacion, usaReglasNegocio);

        return ResponseEntity.ok(resultado);
    }
}
