package cl.bcs.reports.equiv;

import cl.bcs.reports.report.JsonResultSet;
import cl.bcs.reports.report.ReportDocument;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Renders a case with the SIDECAR. Service mode = real controller -> CrystalReportService (POST /reports/generate) for
 * the cases the current REST contract can carry (one flat row, at most one image + one pdf417, no subreports, no typed
 * blob/date fields). Engine mode = the sidecar ReportDocument driven exactly like the legacy servlet does (multi-row,
 * subreports, several images), because the REST contract does not expose those yet (see README).
 */
public final class SidecarRenderer {

    private final MockMvc mvc;
    private final DataSource ds;
    public String lastStderr = "";
    public String lastMode = "";

    public SidecarRenderer(MockMvc mvc, DataSource ds) {
        this.mvc = mvc;
        this.ds = ds;
    }

    public byte[] render(EquivCase c, String format) throws Exception {
        PrintStream old = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err, true, "UTF-8"));
        try {
            return c.serviceCapable() ? service(c, format) : engine(c, format);
        } finally {
            System.setErr(old);
            lastStderr = err.toString("UTF-8");
        }
    }

    private byte[] service(EquivCase c, String format) throws Exception {
        lastMode = "service";
        JsonObject body = new JsonObject();
        body.addProperty("templateId", c.name);
        body.addProperty("format", format);
        body.add("beanData", c.rows.get(0));
        for (Map<String, String> p : c.params) {
            if ("image".equals(p.get("type"))) {
                body.addProperty("imageBase64", p.get("data"));
                body.addProperty("imageName", p.get("name"));
            } else {
                body.addProperty("barcodeData", p.get("data"));
                body.addProperty("barcodeName", p.get("name"));
            }
        }
        MvcResult res = mvc.perform(post("/reports/generate").contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(body))).andReturn();
        if (res.getResponse().getStatus() != 200) {
            throw new IllegalStateException("HTTP " + res.getResponse().getStatus() + " " + res.getResponse().getContentAsString());
        }
        return res.getResponse().getContentAsByteArray();
    }

    private byte[] engine(EquivCase c, String format) throws Exception {
        lastMode = "engine";
        ReportDocument r = new ReportDocument("BSCL", ds);
        try {
            if (!r.readDocument(c.name)) {
                throw new IllegalStateException("readDocument failed for " + c.name);
            }
            for (Map<String, String> p : c.params) {
                if ("image".equals(p.get("type"))) {
                    r.setDocumentImage(p.get("data"), p.get("name"));
                } else {
                    r.setDocumentBarcodeImage(p.get("type"), p.get("data"), p.get("name"));
                }
            }
            r.setDocumentResultSet(c.schema, c.rows);
            if (!c.subRows.isEmpty()) {
                Map<String, JsonResultSet> subs = new LinkedHashMap<>();
                for (String n : c.subRows.keySet()) {
                    subs.put(n, new JsonResultSet(c.subSchemas.get(n), c.subRows.get(n)));
                }
                r.setSubdocumentsResultSetFromJson(subs);
            }
            InputStream in = r.exportDocument(format);
            if (in == null) {
                throw new IllegalStateException("exportDocument returned null (" + format + ")");
            }
            return in.readAllBytes();
        } finally {
            r.close();
        }
    }
}
