package cl.bcs.reports.controller;

import cl.bcs.reports.domain.GenerateReportRequest;
import cl.bcs.reports.service.CrystalReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final CrystalReportService service;

    public ReportController(CrystalReportService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@RequestBody GenerateReportRequest req) throws Exception {
        Objects.requireNonNull(req.templateId, "templateId is required");
        byte[] bytes = service.generateReport(req);
        String contentType = resolveContentType(req.format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=report." + resolveExtension(req.format))
                .body(bytes);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("BcsCrystalReports UP");
    }

    private String resolveContentType(String format) {
        if (format == null) return "application/pdf";
        switch (format.toLowerCase()) {
            case "excel": return "application/vnd.ms-excel";
            case "word":  return "application/msword";
            case "rtf":   return "application/rtf";
            case "txt":   return "text/plain";
            default:      return "application/pdf";
        }
    }

    private String resolveExtension(String format) {
        if (format == null) return "pdf";
        switch (format.toLowerCase()) {
            case "excel": return "xls";
            case "word":  return "doc";
            case "rtf":   return "rtf";
            case "txt":   return "txt";
            default:      return "pdf";
        }
    }
}
