package cl.bcs.plataforma.reporte.controller;

import cl.bcs.plataforma.reporte.service.ReporteApiShimService;
import cl.bcs.utils.report.CrystalReportRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compatibility shim endpoints for BcsPlataformaReporteApi (legacy Crystal Reports API).
 *
 * <p>This controller is temporary and must be removed when TomEE is shut down.</p>
 */
@RestController
@RequestMapping("/reporte-api")
public class ReporteApiController {

    private final ReporteApiShimService shimService;

    public ReporteApiController(ReporteApiShimService shimService) {
        this.shimService = shimService;
    }

    /**
     * Healthcheck endpoint — used by load-balancer probes during canary transition.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("BcsPlataformaReporteApi UP");
    }

    /**
     * Proxies a report-generation request to BcsCrystalReports on port 8100.
     *
     * <p>Accepts the same JSON body as {@code POST /reports/generate} on BcsCrystalReports,
     * using the shared {@code CrystalReportRequest} DTO from {@code bcs-utils}.</p>
     */
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@RequestBody CrystalReportRequest request) {
        byte[] result = shimService.generateReport(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, resolveContentType(request.format))
                .body(result);
    }

    private String resolveContentType(String format) {
        if (format == null) return "application/pdf";
        return switch (format.toLowerCase()) {
            case "excel" -> "application/vnd.ms-excel";
            case "word"  -> "application/msword";
            case "rtf"   -> "application/rtf";
            case "txt"   -> "text/plain";
            default      -> "application/pdf";
        };
    }
}
