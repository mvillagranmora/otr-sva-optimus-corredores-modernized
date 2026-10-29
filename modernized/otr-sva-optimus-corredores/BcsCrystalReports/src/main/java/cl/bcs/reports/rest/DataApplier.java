package cl.bcs.reports.rest;

import cl.bcs.reports.report.ReportDocument;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mainReport.data and subReports (servlet lines 372-478). The object form {serviceId, serviceData, serviceResponse}
 * (server-side data access: getServiceData) is NOT served by the sidecar: it answers the servlet's 404 for an
 * unsupported shape (X-Crystal-Reason says why). The platform resolves the data and sends the array form.
 */
final class DataApplier {

    private DataApplier() {
    }

    static void main(ReportDocument doc, JsonObject mainReport) throws Exception {
        JsonElement data = mainReport.get("data");
        if (data == null) {
            throw Stop.notFound("mainReport.data missing");
        }
        if (data.isJsonObject()) {
            throw Stop.notFound("object-form data (serviceId/serviceData) is resolved by the platform, not by the sidecar");
        } else if (data.isJsonArray()) {
            List<Object> nList = RowsBuilder.rows(data.getAsJsonArray(), mainReport.get("schema"));
            if (nList.size() > 0) {
                doc.setDocumentResultSet(RowsBuilder.resultSet(nList));
            }
        } else {
            throw Stop.notFound("mainReport.data is neither array nor object");
        }
    }

    static void subReports(ReportDocument doc, JsonArray subReports) throws Exception {
        Map<String, ResultSet> subdocsResult = new HashMap<String, ResultSet>();
        for (int i = 0; i < subReports.size(); i++) {
            JsonObject subReport = subReports.get(i).getAsJsonObject();
            if (subReport.get("reportName") != null) {
                String subReportname = subReport.get("reportName").getAsString();
                if (subReport.get("data").isJsonObject()) {
                    throw Stop.notFound("object-form subreport data is resolved by the platform, not by the sidecar");
                } else if (subReport.get("data").isJsonArray()) {
                    List<Object> nList = RowsBuilder.rows(subReport.get("data").getAsJsonArray(), subReport.get("schema"));
                    subdocsResult.put(subReportname, RowsBuilder.resultSet(nList));
                } else {
                    throw Stop.notFound("subreport data is neither array nor object");
                }
            }
        }
        if (subdocsResult.size() > 0) {
            doc.setSubdocumentsResultSet(subdocsResult);
        }
    }
}
