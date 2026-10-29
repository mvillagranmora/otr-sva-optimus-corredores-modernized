package cl.bcs.reports.rest;

import cl.bcs.reports.report.RenderLock;
import cl.bcs.reports.report.ReportDocument;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.sql.DataSource;
import java.io.InputStream;

/**
 * Default mode of the servlet (lines 302-516): same order of reads (exportFormat, configuration, mainReport, subReports,
 * so a missing key is a NullPointerException = 500 exactly where the servlet had it), then readDocument (404),
 * parameters, main data, subreports, export. The only difference: the document is closed at the end (the servlet leaked
 * it), which does not touch the render.
 */
final class RenderOrchestrator {

    private final DataSource dataSource;
    private final String schema;

    RenderOrchestrator(DataSource dataSource, String schema) {
        this.dataSource = dataSource;
        this.schema = schema;
    }

    Reply render(JsonObject dataJsonIn, String reportId) throws Exception {
        String exportFormat = dataJsonIn.get("exportFormat").getAsString().toLowerCase().trim();
        JsonObject configuration = dataJsonIn.get("configuration").getAsJsonObject();
        JsonObject mainReport = configuration.get("mainReport").getAsJsonObject();
        JsonArray subReports = configuration.get("subReports").getAsJsonArray();

        RenderLock.lock();   // JRC global state + ListMapResultSet static metadata: one render at a time
        ReportDocument reportTest = null;
        try {
            reportTest = new ReportDocument(schema, dataSource);
            if (!reportTest.readDocument(reportId)) {
                throw Stop.notFound("unknown reportId");
            }
            ParametersApplier.apply(reportTest, mainReport);
            DataApplier.main(reportTest, mainReport);
            DataApplier.subReports(reportTest, subReports);

            InputStream inStreamRpt = null;
            String extension = "";
            switch (exportFormat) {
                case "pdf":
                    inStreamRpt = reportTest.exportDocument(exportFormat);
                    extension = "PDF";
                    break;
                case "excel":
                    inStreamRpt = reportTest.exportDocument(exportFormat);
                    extension = "XLS";
                    break;
                case "word":
                    inStreamRpt = reportTest.exportDocument(exportFormat);
                    extension = "DOC";
                    break;
                case "rtf":
                    inStreamRpt = reportTest.exportDocument(exportFormat);
                    extension = "RTF";
                    break;
                case "txt":
                    inStreamRpt = reportTest.exportDocument(exportFormat);
                    extension = "TXT";
                    break;
                default:
                    break;
            }
            if (inStreamRpt != null) {
                String fileName = reportTest.getDocumentName() + "_" + reportTest.getDocumentId() + "." + extension;
                return Reply.attachment(fileName, inStreamRpt.readAllBytes());
            }
            return Reply.text(500, "500 - INTERNAL ERROR");
        } finally {
            try {
                if (reportTest != null) {
                    reportTest.close();
                }
            } finally {
                RenderLock.unlock();
            }
        }
    }
}
