package cl.bcs.reports.rest;

import cl.bcs.reports.report.ReportDocument;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** mainReport.parameters (servlet lines 329-370): image / pdf417 pictures replaced in the template. */
final class ParametersApplier {

    private ParametersApplier() {
    }

    static void apply(ReportDocument doc, JsonObject mainReport) {
        if (mainReport.get("parameters") == null) {
            throw Stop.notFound("mainReport.parameters missing");
        }
        if (mainReport.get("parameters").isJsonArray()) {
            JsonArray mainReportParams = mainReport.get("parameters").getAsJsonArray();
            for (int i = 0; i < mainReportParams.size(); i++) {
                JsonObject obj = mainReportParams.get(i).getAsJsonObject();
                JsonElement e = null;
                String type = (e = obj.get("type")) == null ? "" : e.getAsString();
                String name = (e = obj.get("name")) == null ? "" : e.getAsString();
                String data = (e = obj.get("data")) == null ? "" : e.getAsString();
                if (type.equals("") || name.equals("") || data.equals("")) {
                    throw Stop.notFound("parameter without type, name or data");
                }
                switch (type) {
                    case "image":
                        doc.setDocumentImage(data, name);
                        break;
                    case "pdf417":
                        doc.setDocumentBarcodeImage(type, data, name);
                        break;
                    default:
                        break;   // unknown types are ignored, as in the servlet
                }
            }
        } else if (mainReport.get("parameters").isJsonObject()) {
            // servlet: "TODO", no-op
        } else {
            throw Stop.notFound("mainReport.parameters is neither array nor object");
        }
    }
}
