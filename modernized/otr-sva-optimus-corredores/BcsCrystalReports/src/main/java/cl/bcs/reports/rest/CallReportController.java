package cl.bcs.reports.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * REST contract of the sidecar = the legacy CallReportServlet (@WebServlet("/report"), GET and POST identical), minus
 * the X-SESSION-TOKEN session check (replaced by the X-Internal-Token filter) and minus server-side data access.
 * Order of the modes as in the servlet: ?update, ?upload, then the JSON body is read and ?rpt, ?file (no-op, falls
 * through, as in the servlet) and finally the render. Responses are written straight to the servlet response so status,
 * headers and text bodies are the servlet's.
 */
@RestController
@RequestMapping(path = "/report", method = {RequestMethod.GET, RequestMethod.POST})
public class CallReportController {

    private final TemplateModes modes;
    private final RenderOrchestrator renderer;

    public CallReportController(DataSource dataSource, @Value("${app.hana.schema:BSCL}") String schema) {
        this.modes = new TemplateModes(dataSource, schema);
        this.renderer = new RenderOrchestrator(dataSource, schema);
    }

    @RequestMapping
    public void performTask(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean[] debug = {false};
        Reply reply;
        try {
            reply = dispatch(request, debug);
        } catch (Stop s) {
            reply = Reply.text(s.status, s.text);
            reply.reason = s.reason;
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            reply = Reply.text(500, debug[0] ? "500 - INTERNAL ERROR \n\n" + sw : "500 - INTERNAL ERROR");
        }
        reply.writeTo(response);
    }

    private Reply dispatch(HttpServletRequest request, boolean[] debug) throws Exception {
        if (request.getParameter("update") != null) {
            return modes.update(request);
        }
        if (request.getParameter("upload") != null) {
            return modes.upload(request);
        }
        JsonObject dataJsonIn = JsonParser.parseString(BodyReader.read(request)).getAsJsonObject();
        debug[0] = dataJsonIn.get("debug") != null ? dataJsonIn.get("debug").getAsBoolean() : false;
        if (debug[0]) {
            // servlet: only used by the object form; evaluated here only to keep the same failure behaviour
            String debugServiceUrl = dataJsonIn.get("_serviceURL") != null ? dataJsonIn.get("_serviceURL").getAsString() : null;
        }
        String reportId = dataJsonIn.get("reportId") == null ? "" : dataJsonIn.get("reportId").getAsString();
        if (request.getParameter("rpt") != null) {
            return modes.rpt(reportId);
        }
        // ?file: empty branch in the servlet, falls through to the render
        return renderer.render(dataJsonIn, reportId);
    }
}
