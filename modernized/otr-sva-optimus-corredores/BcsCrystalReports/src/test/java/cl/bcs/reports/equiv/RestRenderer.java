package cl.bcs.reports.equiv;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Renders a case THROUGH the new HTTP contract (POST /report, the legacy servlet's JSON body): every case, multi-row,
 * typed fields, subreports, several images/pdf417. The body is built from the case like the platform would send it.
 */
public final class RestRenderer {

    public static final String TOKEN = "equiv-token";

    private final MockMvc mvc;
    public String lastStderr = "";
    public String lastMode = "rest";

    public RestRenderer(MockMvc mvc) {
        this.mvc = mvc;
    }

    public static JsonObject body(EquivCase c, String format) {
        JsonObject main = new JsonObject();
        JsonArray params = new JsonArray();
        for (Map<String, String> p : c.params) {
            JsonObject o = new JsonObject();
            o.addProperty("type", p.get("type"));
            o.addProperty("name", p.get("name"));
            o.addProperty("data", p.get("data"));
            params.add(o);
        }
        main.add("parameters", params);
        main.add("data", c.rows);
        main.add("schema", schema(c.schema));
        JsonArray subs = new JsonArray();
        for (String n : c.subRows.keySet()) {
            JsonObject s = new JsonObject();
            s.addProperty("reportName", n);
            s.add("data", c.subRows.get(n));
            s.add("schema", schema(c.subSchemas.get(n)));
            subs.add(s);
        }
        JsonObject configuration = new JsonObject();
        configuration.add("mainReport", main);
        configuration.add("subReports", subs);
        JsonObject body = new JsonObject();
        body.addProperty("reportId", c.name);
        body.addProperty("exportFormat", format);
        body.add("configuration", configuration);
        return body;
    }

    private static JsonArray schema(Map<String, String> s) {
        JsonArray a = new JsonArray();
        s.forEach((field, type) -> {
            JsonObject o = new JsonObject();
            o.addProperty("field", field);
            o.addProperty("type", type);
            a.add(o);
        });
        return a;
    }

    public byte[] render(EquivCase c, String format) throws Exception {
        PrintStream old = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err, true, "UTF-8"));
        try {
            MvcResult res = mvc.perform(post("/report").header("X-Internal-Token", TOKEN)
                    .contentType(MediaType.APPLICATION_JSON).content(new Gson().toJson(body(c, format)))).andReturn();
            if (res.getResponse().getStatus() != 200) {
                throw new IllegalStateException("HTTP " + res.getResponse().getStatus() + " "
                        + res.getResponse().getContentAsString());
            }
            return res.getResponse().getContentAsByteArray();
        } finally {
            System.setErr(old);
            lastStderr = err.toString("UTF-8");
        }
    }
}
