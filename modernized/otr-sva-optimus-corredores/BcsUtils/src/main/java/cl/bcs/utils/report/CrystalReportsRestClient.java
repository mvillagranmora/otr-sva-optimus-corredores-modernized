package cl.bcs.utils.report;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CrystalReportsRestClient implements CrystalReportsPort {

    private final RestClient restClient;

    public CrystalReportsRestClient(
            @Value("${crystal.reports.url:http://localhost:8100}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public byte[] generateReport(CrystalReportRequest request) {
        return restClient.post()
                .uri("/reports/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(byte[].class);
    }
}
