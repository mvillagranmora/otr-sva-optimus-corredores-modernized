package cl.bcs.reports.rest;

import cl.bcs.reports.equiv.EquivCase;
import cl.bcs.reports.equiv.H2Shim;
import cl.bcs.reports.equiv.RestRenderer;
import cl.bcs.reports.support.CrystalTestEnvironment;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** ?rpt, ?upload and ?update (multipart: parameters + part "data") over the H2 template table. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:restmodes;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "services.internal-token=" + RestRenderer.TOKEN
})
@AutoConfigureMockMvc
class ReportRestModesTest {

    @Autowired MockMvc mvc;
    @Autowired DataSource ds;
    private Path rpt;
    private byte[] rptBytes;

    @BeforeAll
    static void environment() {
        CrystalTestEnvironment.ensure();
    }

    @BeforeEach
    void data() throws Exception {
        H2Shim.init(ds);
        EquivCase c = new EquivCase(Paths.get("src/test/resources/crystal/equiv/Custodia_Anexo2"));
        rpt = Paths.get(c.template);
        rptBytes = Files.readAllBytes(rpt);
        H2Shim.putTemplate(ds, "tpl", rpt);
    }

    private MockMultipartHttpServletRequestBuilder form(String mode, byte[] file) {
        MockMultipartHttpServletRequestBuilder b = multipart("/report");
        if (file != null) {
            b.part(new MockPart("data", "t.rpt", file));
        }
        b.header("X-Internal-Token", RestRenderer.TOKEN).param(mode, "");
        return b;
    }

    private MockHttpServletResponse run(MockHttpServletRequestBuilder b) throws Exception {
        return mvc.perform(b).andReturn().getResponse();
    }

    private String[] row(String id) throws Exception {
        try (Connection c = ds.getConnection(); PreparedStatement p = c.prepareStatement(
                "SELECT \"ReportName\", \"ReportVersion\", \"CompanyId\", \"ReportFile\" FROM BSCL.\"BSCL.Report::Template\" "
                        + "WHERE \"Id\" = ?")) {
            p.setString(1, id);
            ResultSet rs = p.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return new String[] {rs.getString(1), rs.getString(2), rs.getString(3), String.valueOf(rs.getBytes(4).length)};
        }
    }

    @Test
    void rptReturnsTheTemplateBytesAs_rpt_attachment() throws Exception {
        MockHttpServletResponse r = run(post("/report").param("rpt", "").header("X-Internal-Token", RestRenderer.TOKEN)
                .contentType(MediaType.APPLICATION_JSON).content("{\"reportId\":\"tpl\"}"));
        assertEquals(200, r.getStatus());
        assertArrayEquals(rptBytes, r.getContentAsByteArray());
        // getDocument() never fills name/id, so the servlet's file name is "_.rpt"
        assertEquals("attachment; filename=\"_.rpt\"", r.getHeader("Content-Disposition"));
        assertNull(r.getContentType());
    }

    @Test
    void rptUnknownOrMissingReportIdIs404() throws Exception {
        for (String body : new String[] {"{\"reportId\":\"nope\"}", "{}", ""}) {
            MockHttpServletResponse r = run(post("/report").param("rpt", "").header("X-Internal-Token", RestRenderer.TOKEN)
                    .contentType(MediaType.APPLICATION_JSON).content(body));
            assertEquals(404, r.getStatus());
            assertEquals("404 - NOT FOUND", r.getContentAsString());
        }
    }

    @Test
    void uploadStoresTheTemplateAndReturnsItsId() throws Exception {
        MockHttpServletResponse r = run(form("upload", rptBytes).param("reportName", "Nuevo").param("reportVersion", "7")
                .param("companyId", "42"));
        assertEquals(200, r.getStatus());
        assertEquals("application/json", r.getContentType());
        JsonObject out = JsonParser.parseString(r.getContentAsString()).getAsJsonObject();
        assertEquals("OK", out.get("result").getAsString());
        String id = out.get("reportId").getAsString();
        assertFalse(id.isEmpty());
        assertEquals("[Nuevo, 7, 42, " + rptBytes.length + "]", java.util.Arrays.toString(row(id)));
        MockHttpServletResponse back = run(post("/report").param("rpt", "").header("X-Internal-Token", RestRenderer.TOKEN)
                .contentType(MediaType.APPLICATION_JSON).content("{\"reportId\":\"" + id + "\"}"));
        assertArrayEquals(rptBytes, back.getContentAsByteArray());
    }

    @Test
    void uploadWithoutVersionOrFileIs500() throws Exception {
        assertEquals(500, run(form("upload", rptBytes).param("reportName", "x")).getStatus());          // no reportVersion
        MockHttpServletResponse r = run(form("upload", null).param("reportName", "x").param("reportVersion", "1")
                .param("companyId", "1"));                                                              // no part "data"
        assertEquals(500, r.getStatus());
        assertEquals("500 - INTERNAL ERROR", r.getContentAsString());
    }

    @Test
    void updateReplacesNameVersionCompanyAndFile() throws Exception {
        byte[] other = java.util.Arrays.copyOf(rptBytes, rptBytes.length);   // same valid template, new name/version
        MockHttpServletResponse r = run(form("update", other).param("reportId", "tpl").param("reportName", "Renombrado")
                .param("reportVersion", "3").param("companyId", "9"));
        assertEquals(200, r.getStatus());
        assertEquals("application/json", r.getContentType());
        assertEquals("{\"result\":\"OK\"}", r.getContentAsString());
        assertEquals("[Renombrado, 3, 9, " + rptBytes.length + "]", java.util.Arrays.toString(row("tpl")));
    }

    @Test
    void updateWithEmptyFileKeepsTheStoredTemplate() throws Exception {
        MockHttpServletResponse r = run(form("update", new byte[0]).param("reportId", "tpl").param("reportName", "Solo nombre")
                .param("reportVersion", "2").param("companyId", "1"));
        assertEquals("{\"result\":\"OK\"}", r.getContentAsString());
        assertEquals("[Solo nombre, 2, 1, " + rptBytes.length + "]", java.util.Arrays.toString(row("tpl")));
    }

    @Test
    void updateUnknownReportIs404AndBadVersionIs500() throws Exception {
        MockHttpServletResponse r = run(form("update", rptBytes).param("reportId", "nope").param("reportName", "x")
                .param("reportVersion", "1").param("companyId", "1"));
        assertEquals(404, r.getStatus());
        assertEquals("404 - NOT FOUND", r.getContentAsString());
        r = run(form("update", rptBytes).param("reportId", "tpl").param("reportName", "x").param("reportVersion", "abc")
                .param("companyId", "1"));
        assertEquals(500, r.getStatus());
        assertEquals("500 - INTERNAL ERROR", r.getContentAsString());
    }
}
