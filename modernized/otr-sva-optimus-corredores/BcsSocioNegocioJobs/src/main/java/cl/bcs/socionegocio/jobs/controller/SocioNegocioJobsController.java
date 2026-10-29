package cl.bcs.socionegocio.jobs.controller;

import cl.bcs.socionegocio.jobs.domain.ClienteABloquear;
import cl.bcs.socionegocio.jobs.domain.DocumentoCliente;
import cl.bcs.socionegocio.jobs.domain.ResultadoJob;
import cl.bcs.socionegocio.jobs.port.out.SocioNegocioJobsDataPort;
import cl.bcs.socionegocio.jobs.service.ClienteBloqueoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * REST controller exposing SocioNegocio job endpoints.
 *
 * POST /api/socionegocio/jobs/bloquear-por-vencimiento — runs the blocking job
 * POST /api/socionegocio/jobs/purga-prospectos         — delegates to HANA SP (503 when absent)
 * GET  /api/socionegocio/jobs/fecha-sistema            — returns configured system date
 */
@RestController
@RequestMapping("/api/socionegocio/jobs")
public class SocioNegocioJobsController {

    private final SocioNegocioJobsDataPort dataPort;

    SocioNegocioJobsController(SocioNegocioJobsDataPort dataPort) {
        this.dataPort = dataPort;
    }

    /**
     * Runs the client blocking job for documents past their expiry.
     *
     * Steps (mirroring legacy BloquearClientePorFechaVencimientoJob.logica()):
     * 1. Read system date and days-to-expiry from config
     * 2. Query all documents with a FECHA_VIGENCIA set
     * 3. Re-compute FECHA_VIGENCIA for each doc (skip PEN / null FechaModificacion)
     * 4. UPDATE annexes whose computed FECHA_VIGENCIA == today (set VEN)
     * 5. UPDATE clients where FECHA_VIGENCIA == today AND BloquearAlVencer == "S"
     */
    @PostMapping("/bloquear-por-vencimiento")
    public ResponseEntity<ResultadoJob> bloquearPorVencimiento(
            @RequestParam Long idEmpresa,
            @RequestParam Long idUsuario) {

        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");

        ResultadoJob resultado = new ResultadoJob();

        String fechaSistemaStr = dataPort.obtenerFechaSistema(idEmpresa);
        LocalDate fechaSistema = LocalDate.parse(fechaSistemaStr);
        int diasVencimiento = dataPort.obtenerDiasVencimiento(idEmpresa);

        List<DocumentoCliente> documentos = dataPort.consultarDocumentosPorVencer();

        // Re-compute expiry dates (B-BLQ-06)
        List<DocumentoCliente> documentosConVigencia =
                ClienteBloqueoService.calcularFechasVencimientoLote(documentos, diasVencimiento);

        // Collect annexes to mark as expired (B-BLQ-03: venceHoy)
        List<Long> idAnexosAVencer = documentosConVigencia.stream()
                .filter(doc -> ClienteBloqueoService.clienteVenceHoy(doc.fechaVigencia(), fechaSistema))
                .map(DocumentoCliente::idAnexo)
                .toList();
        dataPort.actualizarAnexos(idAnexosAVencer);

        // Block clients whose document expires today AND BloquearAlVencer == "S" (B-BLQ-07)
        List<ClienteABloquear> clientesABloquear =
                ClienteBloqueoService.filtrarClientesABloquear(documentosConVigencia, fechaSistema);
        dataPort.bloquearClientes(clientesABloquear);

        resultado.setMensaje("Bloqueo ejecutado. Anexos actualizados: " + idAnexosAVencer.size()
                + ", Clientes bloqueados: " + clientesABloquear.size());
        return ResponseEntity.ok(resultado);
    }

    /**
     * Triggers the prospect client purge HANA stored procedure.
     * Returns HTTP 503 (Service Unavailable) when running outside HANA
     * because the SP is not available (UnsupportedOperationException).
     */
    @PostMapping("/purga-prospectos")
    public ResponseEntity<ResultadoJob> purgaProspectos(
            @RequestParam Long idEmpresa,
            @RequestParam Long idUsuario) {

        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");

        // UnsupportedOperationException is caught by GlobalExceptionHandler → 503
        dataPort.purgaProspectosClientes(idEmpresa, idUsuario);

        ResultadoJob resultado = new ResultadoJob();
        resultado.setMensaje("Purga de prospectos ejecutada correctamente.");
        return ResponseEntity.ok(resultado);
    }

    /**
     * Returns the system date configured for the given company.
     */
    @GetMapping("/fecha-sistema")
    public ResponseEntity<String> obtenerFechaSistema(@RequestParam Long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        return ResponseEntity.ok(dataPort.obtenerFechaSistema(idEmpresa));
    }
}
