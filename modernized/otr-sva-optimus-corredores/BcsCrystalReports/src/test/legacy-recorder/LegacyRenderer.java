import cl.bcs.plataforma.report.api.JsonResultSet;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.reports.equiv.EquivCase;
import cl.bcs.reports.equiv.H2Shim;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Drives the UNMODIFIED legacy ReportDocument exactly like CallReportServlet does for a render request:
 * readDocument, images/pdf417 parameters, main data set, subreport data sets, exportDocument.
 */
public final class LegacyRenderer {

    public final DataSource ds;
    public String lastStderr = "";

    public LegacyRenderer(DataSource ds) {
        this.ds = ds;
    }

    public void load(EquivCase c, Path module) throws Exception {
        H2Shim.putTemplate(ds, c.name, module.resolve(c.template));
    }

    public byte[] render(EquivCase c, String format) throws Exception {
        PrintStream oldErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err, true, "UTF-8"));
        try {
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
        } finally {
            System.setErr(oldErr);
            lastStderr = err.toString("UTF-8");
        }
    }
}
