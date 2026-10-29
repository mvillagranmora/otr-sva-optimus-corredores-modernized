package cl.bcs.plataforma.reporte.service;

import cl.bcs.utils.report.CrystalReportRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Forwards report-generation requests to {@code BcsCrystalReports} (port 8100).
 *
 * <p>This service is the sole outbound dependency of this shim module.
 * Swap the {@code crystal.reports.url} property to redirect to a different backend.</p>
 */
@Service
public class ReporteApiShimService {

    private final RestClient restClient;

    public ReporteApiShimService(
            RestClient.Builder restClientBuilder,
            @Value("${crystal.reports.url:http://bcs-crystal-reports:8100}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Delegates to {@code POST /reports/generate} on {@code BcsCrystalReports}.
     *
     * @param request the report generation parameters
     * @return the generated report bytes (PDF, Excel, Word, RTF or TXT)
     */
    public byte[] generateReport(CrystalReportRequest request) {
        return restClient.post()
                .uri("/reports/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(byte[].class);
    }
}
