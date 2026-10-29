package cl.bcs.facturacion.jobs.controller;

import cl.bcs.facturacion.jobs.domain.ConfiguracionFormato;
import cl.bcs.facturacion.jobs.domain.ResumenFacturacionCliente;
import cl.bcs.facturacion.jobs.port.out.FacturacionJobsDataPort;
import cl.bcs.facturacion.jobs.service.FormatoArchivoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * REST controller for facturacion job operations.
 *
 * <p>HANA stored-procedure endpoints ({@code /emitir-masivo} and
 * {@code /prefacturas-masivas}) return HTTP 503 via
 * {@link GlobalExceptionHandler} when the underlying adapter throws
 * {@link UnsupportedOperationException}.</p>
 */
@RestController
@RequestMapping("/api/facturacion/jobs")
public class FacturacionJobsController {

    private final FacturacionJobsDataPort dataPort;

    public FacturacionJobsController(FacturacionJobsDataPort dataPort) {
        this.dataPort = dataPort;
    }

    /**
     * Returns billing document summaries for the given company and date range.
     *
     * @param idEmpresa   company identifier
     * @param fechaDesde  start date (yyyy-MM-dd)
     * @param fechaHasta  end date (yyyy-MM-dd)
     */
    @GetMapping("/resumen")
    public ResponseEntity<List<ResumenFacturacionCliente>> obtenerResumen(
            @RequestParam Long idEmpresa,
            @RequestParam String fechaDesde,
            @RequestParam String fechaHasta) {

        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(fechaDesde, "fechaDesde is required");
        Objects.requireNonNull(fechaHasta, "fechaHasta is required");

        List<ResumenFacturacionCliente> result =
                dataPort.consultarResumenFacturas(idEmpresa, fechaDesde, fechaHasta);
        return ResponseEntity.ok(result);
    }

    /**
     * Returns the system date configured for the given company.
     *
     * @param idEmpresa company identifier
     */
    @GetMapping("/fecha-sistema")
    public ResponseEntity<String> obtenerFechaSistema(@RequestParam Long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        String fecha = dataPort.obtenerFechaSistema(idEmpresa);
        return ResponseEntity.ok(fecha);
    }

    /**
     * Triggers mass voucher issuance (requires live HANA environment).
     * Returns HTTP 503 when the HANA stored procedure is unavailable.
     */
    @PostMapping("/emitir-masivo")
    public ResponseEntity<Void> emitirComprobantesMasivos(
            @RequestParam Long idEmpresa,
            @RequestParam Long idUsuario) {

        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");

        dataPort.emitirComprobantesMasivos(idEmpresa, idUsuario);
        return ResponseEntity.ok().build();
    }

    /**
     * Triggers mass pre-invoice generation (requires live HANA environment).
     * Returns HTTP 503 when the HANA stored procedure is unavailable.
     */
    @PostMapping("/prefacturas-masivas")
    public ResponseEntity<Void> generarPrefacturasMasivas(
            @RequestParam Long idEmpresa,
            @RequestParam Long idUsuario) {

        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");

        dataPort.generarPrefacturasMasivas(idEmpresa, idUsuario);
        return ResponseEntity.ok().build();
    }

    /**
     * Formats a single field value using the provided format configuration.
     *
     * @param request  body containing config, tipoRegistro, campo, and valor
     * @return the formatted string
     */
    @PostMapping("/formato-campo")
    public ResponseEntity<String> formatearCampo(@RequestBody FormatoCampoRequest request) {
        Objects.requireNonNull(request, "request body is required");
        Objects.requireNonNull(request.config(), "config is required");

        String resultado = FormatoArchivoService.formatear(
                request.config(),
                request.tipoRegistro(),
                request.campo(),
                request.valor()
        );
        return ResponseEntity.ok(resultado);
    }

    /**
     * Request body for the {@code /formato-campo} endpoint.
     */
    public record FormatoCampoRequest(
            ConfiguracionFormato config,
            int tipoRegistro,
            String campo,
            String valor
    ) {}
}
