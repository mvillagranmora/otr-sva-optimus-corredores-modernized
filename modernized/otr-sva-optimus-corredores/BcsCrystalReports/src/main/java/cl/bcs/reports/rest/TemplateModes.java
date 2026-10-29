package cl.bcs.reports.rest;

import cl.bcs.reports.report.RenderLock;
import cl.bcs.reports.report.ReportDocument;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.sql.DataSource;
import java.io.InputStream;

/**
 * Template management modes of the servlet: ?update (lines 188-218), ?upload (220-244), ?rpt (273-291).
 * Parameters are read with request.getParameter (query string or multipart form field) and the file from the
 * multipart part "data", as the servlet does. companyId: the servlet took it from the X-SESSION-TOKEN session; the
 * sidecar has no session, so it is a required parameter (the servlet's own commented-out alternative).
 */
final class TemplateModes {

    private final DataSource dataSource;
    private final String schema;

    TemplateModes(DataSource dataSource, String schema) {
        this.dataSource = dataSource;
        this.schema = schema;
    }

    Reply update(HttpServletRequest request) throws Exception {
        JsonObject jsonOut = new JsonObject();
        String reportId = request.getParameter("reportId");
        String reportName = request.getParameter("reportName");
        int reportVersion = Integer.parseInt(request.getParameter("reportVersion"));
        long companyId = Long.parseLong(request.getParameter("companyId"));
        Part reportFile = request.getPart("data");

        RenderLock.lock();
        ReportDocument reportUpdate = null;
        try {
            reportUpdate = new ReportDocument(schema, dataSource);
            if (!reportUpdate.readDocument(reportId)) {
                throw Stop.notFound("unknown reportId");
            }
            reportUpdate.updateDocument(reportId, reportName, reportFile.getInputStream(), "", reportVersion, companyId);
            jsonOut.addProperty("result", "OK");
            return Reply.json(jsonOut.toString());
        } finally {
            try {
                if (reportUpdate != null) {
                    reportUpdate.close();
                }
            } finally {
                RenderLock.unlock();
            }
        }
    }

    Reply upload(HttpServletRequest request) throws Exception {
        JsonObject jsonOut = new JsonObject();
        String reportName = request.getParameter("reportName");
        int reportVersion = Integer.parseInt(request.getParameter("reportVersion"));
        long companyId = Long.parseLong(request.getParameter("companyId"));
        Part reportFile = request.getPart("data");

        RenderLock.lock();
        ReportDocument reportUpload = null;
        try {
            reportUpload = new ReportDocument(schema, dataSource);
            String reportId = reportUpload.uploadDocument(reportName, reportFile.getInputStream(), reportVersion, companyId);
            jsonOut.addProperty("result", "OK");
            jsonOut.addProperty("reportId", reportId);
            return Reply.json(jsonOut.toString());
        } finally {
            try {
                if (reportUpload != null) {
                    reportUpload.close();
                }
            } finally {
                RenderLock.unlock();
            }
        }
    }

    Reply rpt(String reportId) throws Exception {
        ReportDocument reportRpt = new ReportDocument(schema, dataSource);   // never opened: nothing to close
        InputStream streamRpt = reportRpt.getDocument(reportId);
        if (streamRpt == null) {
            throw Stop.notFound("unknown reportId");
        }
        // getDocument does not fill name/id: the servlet's file name is therefore "_.rpt"
        String fileName = reportRpt.getDocumentName() + "_" + reportRpt.getDocumentId() + ".rpt";
        return Reply.attachment(fileName, streamRpt.readAllBytes());
    }
}
