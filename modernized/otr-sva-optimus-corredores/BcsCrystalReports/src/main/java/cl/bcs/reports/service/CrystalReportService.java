package cl.bcs.reports.service;

import cl.bcs.reports.domain.GenerateReportRequest;
import cl.bcs.reports.report.RenderLock;
import cl.bcs.reports.report.ReportDocument;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class CrystalReportService {

    private final DataSource dataSource;
    private final String schema;

    public CrystalReportService(DataSource dataSource,
                                @Value("${app.hana.schema:BSCL}") String schema) {
        this.dataSource = dataSource;
        this.schema = schema;
    }

    public byte[] generateReport(GenerateReportRequest req) throws Exception {
        RenderLock.lock();   // shared with the /report path: JRC global state is not thread safe
        ReportDocument report = null;
        try {
            report = new ReportDocument(schema, dataSource);
            boolean opened = report.readDocument(req.templateId);
            if (!opened) {
                throw new IllegalArgumentException("Template not found: " + req.templateId);
            }

            // Optional: set image
            if (req.imageBase64 != null && req.imageName != null) {
                report.setDocumentImage(req.imageBase64, req.imageName);
            }

            // Optional: set barcode
            if (req.barcodeData != null && req.barcodeName != null) {
                report.setDocumentBarcodeImage("pdf417", req.barcodeData, req.barcodeName);
            }

            // Optional: set bean data as ResultSet from JSON
            if (req.beanData != null && !req.beanData.isEmpty()) {
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(new Gson().toJsonTree(req.beanData).getAsJsonObject());
                // infer all fields as "string"
                Map<String, String> fieldSchema = new HashMap<>();
                req.beanData.keySet().forEach(k -> fieldSchema.put(k, "string"));
                report.setDocumentResultSet(fieldSchema, jsonArray);
            }

            InputStream stream = report.exportDocument(req.format != null ? req.format : "pdf");
            if (stream == null) {
                throw new IllegalStateException("Export returned null for format: " + req.format);
            }
            return stream.readAllBytes();
        } finally {
            try {
                if (report != null) {
                    report.close();
                }
            } catch (Exception ignored) {
            } finally {
                RenderLock.unlock();
            }
        }
    }
}
