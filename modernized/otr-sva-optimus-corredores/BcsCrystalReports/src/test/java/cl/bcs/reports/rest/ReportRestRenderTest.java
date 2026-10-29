package cl.bcs.reports.rest;

import cl.bcs.reports.equiv.EquivCase;
import cl.bcs.reports.equiv.H2Shim;
import cl.bcs.reports.equiv.RestRenderer;
import cl.bcs.reports.support.CrystalTestEnvironment;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Status / body per branch of the servlet's default (render) mode. Same JSON body as the platform sends. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:restrender;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "services.internal-token=" + RestRenderer.TOKEN
})
@AutoConfigureMockMvc
class ReportRestRenderTest {

    private static final String NF = "404 - NOT FOUND";
    private static final String ISE = "500 - INTERNAL ERROR";

    @Autowired MockMvc mvc;
    @Autowired DataSource ds;
    private EquivCase simple;     // Custodia_Anexo2: 3 rows, no subreport
    private EquivCase withSub;    // APV_CONTRATO_SAAPV: subreport "Entidades", image

    @BeforeAll
    static void environment() {
        CrystalTestEnvironment.ensure();
    }

    @BeforeEach
    void data() throws Exception {
        H2Shim.init(ds);
        simple = load("Custodia_Anexo2");
        withSub = load("APV_CONTRATO_SAAPV");
    }

    private EquivCase load(String name) throws Exception {
        EquivCase c = new EquivCase(Paths.get("src/test/resources/crystal/equiv", name));
        H2Shim.putTemplate(ds, c.name, Paths.get(c.template));
        return c;
    }

    private MockHttpServletResponse send(String body) throws Exception {
        return mvc.perform(post("/report").header("X-Internal-Token", RestRenderer.TOKEN)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse();
    }

    private MockHttpServletResponse send(JsonObject body) throws Exception {
        return send(new Gson().toJson(body));
    }

    private static JsonObject mainOf(JsonObject body) {
        return body.getAsJsonObject("configuration").getAsJsonObject("mainReport");
    }

    private void assertText(MockHttpServletResponse r, int status, String text) throws Exception {
        assertEquals(status, r.getStatus());
        assertEquals(text, r.getContentAsString());
    }

    @Test
    void emptyBodyIsBraceBraceThenMissingExportFormatIs500() throws Exception {
        assertText(send(""), 500, ISE);
    }

    @Test
    void malformedAndNonObjectJsonAre500() throws Exception {
        assertText(send("{not json"), 500, ISE);
        assertText(send("[1,2]"), 500, ISE);
    }

    @Test
    void debugTrueAddsTheStackTraceTo500() throws Exception {
        MockHttpServletResponse r = send("{\"debug\":true}");
        assertEquals(500, r.getStatus());
        assertTrue(r.getContentAsString().startsWith("500 - INTERNAL ERROR \n\n"), r.getContentAsString());
        assertTrue(r.getContentAsString().contains("NullPointerException"));
    }

    @Test
    void missingKeysAre500LikeTheServletNullPointer() throws Exception {
        JsonObject b = RestRenderer.body(simple, "pdf");
        b.remove("exportFormat");
        assertText(send(b), 500, ISE);
        b = RestRenderer.body(simple, "pdf");
        b.remove("configuration");
        assertText(send(b), 500, ISE);
        b = RestRenderer.body(simple, "pdf");
        b.getAsJsonObject("configuration").remove("subReports");
        assertText(send(b), 500, ISE);
        b = RestRenderer.body(simple, "pdf");
        b.getAsJsonObject("configuration").remove("mainReport");
        assertText(send(b), 500, ISE);
    }

    @Test
    void missingOrUnknownReportIdIs404() throws Exception {
        JsonObject b = RestRenderer.body(simple, "pdf");
        b.remove("reportId");
        assertText(send(b), 404, NF);
        b.addProperty("reportId", "no-such-template");
        assertText(send(b), 404, NF);
    }

    @Test
    void parametersBranches() throws Exception {
        JsonObject b = RestRenderer.body(simple, "pdf");
        mainOf(b).remove("parameters");
        assertText(send(b), 404, NF);
        mainOf(b).addProperty("parameters", "x");
        assertText(send(b), 404, NF);
        JsonArray p = new JsonArray();
        JsonObject noName = new JsonObject();
        noName.addProperty("type", "image");
        noName.addProperty("data", "AAAA");
        p.add(noName);
        mainOf(b).add("parameters", p);
        assertText(send(b), 404, NF);
        JsonObject unknown = new JsonObject();      // unknown type is ignored, the render goes on
        unknown.addProperty("type", "text");
        unknown.addProperty("name", "n");
        unknown.addProperty("data", "d");
        p = new JsonArray();
        p.add(unknown);
        mainOf(b).add("parameters", p);
        assertEquals(200, send(b).getStatus());
        mainOf(b).add("parameters", new JsonObject());   // object form: TODO no-op in the servlet
        assertEquals(200, send(b).getStatus());
    }

    @Test
    void dataBranches() throws Exception {
        JsonObject b = RestRenderer.body(simple, "pdf");
        mainOf(b).remove("data");
        assertText(send(b), 404, NF);
        mainOf(b).addProperty("data", "select 1");
        assertText(send(b), 404, NF);
        JsonObject svc = new JsonObject();
        svc.addProperty("serviceId", "abc");
        svc.add("serviceData", new JsonObject());
        mainOf(b).add("data", svc);
        MockHttpServletResponse r = send(b);       // object form: platform's job, same 404 as an unsupported shape
        assertText(r, 404, NF);
        assertTrue(r.getHeader("X-Crystal-Reason").contains("object-form"));
        // (an empty array sets no result set, the template then tries its embedded HANA host: not exercised here)
    }

    @Test
    void subReportBranches() throws Exception {
        JsonObject b = RestRenderer.body(withSub, "pdf");
        JsonObject s = b.getAsJsonObject("configuration").getAsJsonArray("subReports").get(0).getAsJsonObject();
        s.add("data", new JsonObject());
        assertText(send(b), 404, NF);
        s.addProperty("data", "x");
        assertText(send(b), 404, NF);
        s.remove("data");                           // servlet: NullPointerException
        assertText(send(b), 500, ISE);
        b = RestRenderer.body(simple, "pdf");       // sub name that the template does not have: NPE in the engine call
        JsonObject extra = new JsonObject();
        extra.addProperty("reportName", "NoSuchSub");
        extra.add("data", new JsonArray());
        b.getAsJsonObject("configuration").getAsJsonArray("subReports").add(extra);
        assertText(send(b), 500, ISE);
        JsonObject nameless = new JsonObject();     // an entry without reportName is skipped
        b = RestRenderer.body(simple, "pdf");
        b.getAsJsonObject("configuration").getAsJsonArray("subReports").add(nameless);
        assertEquals(200, send(b).getStatus());
    }

    @Test
    void unsupportedFormatIs500AfterTheWholeSetup() throws Exception {
        assertText(send(RestRenderer.body(simple, "html")), 500, ISE);
    }

    @ParameterizedTest
    @CsvSource({"pdf,PDF", "' PDF ',PDF", "excel,XLS", "word,DOC", "rtf,RTF", "txt,TXT"})
    void formatsHaveTheServletsFileNameAndNoContentType(String format, String ext) throws Exception {
        MockHttpServletResponse r = send(RestRenderer.body(simple, format));
        assertEquals(200, r.getStatus());
        assertEquals("attachment; filename=\"_." + ext + "\"", r.getHeader("Content-Disposition"));
        assertNull(r.getContentType(), "the servlet sets no Content-Type");
        assertTrue(r.getContentAsByteArray().length > 100);
        if (ext.equals("PDF")) {
            assertEquals("%PDF", new String(r.getContentAsByteArray(), 0, 4, StandardCharsets.US_ASCII));
        }
    }

    @Test
    void templateWithSubreportsNamesTheFileAfterTheLastSubreport() throws Exception {
        // legacy quirk kept on purpose: readDocument sets documentName to the subreport name, documentId is never set
        MockHttpServletResponse r = send(RestRenderer.body(withSub, "pdf"));
        assertEquals(200, r.getStatus());
        assertEquals("attachment; filename=\"Entidades_.PDF\"", r.getHeader("Content-Disposition"));
    }

    @Test
    void fileModeFallsThroughToTheRender() throws Exception {
        MockHttpServletResponse r = mvc.perform(post("/report").param("file", "x")
                .header("X-Internal-Token", RestRenderer.TOKEN).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andReturn().getResponse();
        assertText(r, 500, ISE);         // like the servlet: ?file is an empty branch, then exportFormat is missing
        r = mvc.perform(post("/report").param("file", "x").header("X-Internal-Token", RestRenderer.TOKEN)
                .contentType(MediaType.APPLICATION_JSON).content(new Gson().toJson(RestRenderer.body(simple, "pdf"))))
                .andReturn().getResponse();
        assertEquals(200, r.getStatus());
    }
}
