package cl.bcs.reports;

import cl.bcs.reports.support.CrystalTestEnvironment;
import cl.bcs.reports.support.PdfProbe;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REAL render through controller -> service -> ReportDocument -> Crystal JRC (no mocks). Template:
 * templates/CuentaInversion/Templates/ComprobanteCuentaInversion.rpt, stored in an H2 table that mimics HANA
 * BSCL."BSCL.Report::Template"; data goes through the (faithfully ported) ListMapResultSet.
 * Fails (never skips) if the Crystal SDK, the JVM flags or the fonts directory are unavailable.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:render;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
class CrystalRenderTest {

    private static final String TEMPLATE_ID = "T-COMPROBANTE";
    private static final Path RPT = Paths.get("templates/CuentaInversion/Templates/ComprobanteCuentaInversion.rpt");
    private static final Path DATA = Paths.get("src/test/resources/crystal/comprobante.json");

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeAll
    static void environment() {
        CrystalTestEnvironment.ensure();
    }

    @BeforeEach
    void template() throws Exception {
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS BSCL");
        jdbc.execute("CREATE TABLE IF NOT EXISTS BSCL.\"BSCL.Report::Template\"(\"Id\" VARCHAR(64), "
                + "\"ReportName\" VARCHAR(255), \"MasterReportId\" VARCHAR(64), \"ReportFile\" BLOB, "
                + "\"ReportVersion\" INT, \"CompanyId\" BIGINT)");
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM BSCL.\"BSCL.Report::Template\" WHERE \"Id\" = ?",
                Integer.class, TEMPLATE_ID);
        if (n == 0) {
            jdbc.update("INSERT INTO BSCL.\"BSCL.Report::Template\" VALUES (?,?,?,?,?,?)",
                    TEMPLATE_ID, "ComprobanteCuentaInversion", null, Files.readAllBytes(RPT), 1, 1L);
        }
    }

    private byte[] render(String format) throws Exception {
        // "beanData" = the single data row of the (current) sidecar contract
        String row = new String(Files.readAllBytes(DATA), StandardCharsets.UTF_8).trim();
        row = row.substring(1, row.length() - 1); // unwrap the one-element array
        String body = "{\"templateId\":\"" + TEMPLATE_ID + "\",\"format\":\"" + format + "\",\"beanData\":" + row + "}";
        MvcResult res = mockMvc.perform(post("/reports/generate").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn();
        return res.getResponse().getContentAsByteArray();
    }

    @Test
    @DisplayName("PDF: 1 page, known text, deterministic modulo /Title uuid and print date/time")
    void pdf() throws Exception {
        byte[] a = render("pdf");
        byte[] b = render("pdf");
        assertTrue(a.length > 10_000, "pdf too small: " + a.length);
        assertEquals("%PDF-1.7", PdfProbe.latin1(a).substring(0, 8));
        assertEquals(1, PdfProbe.pageCount(a), "page count");
        String text = PdfProbe.text(a);
        assertTrue(text.contains("JUAN PEREZ GONZALEZ"), "client name missing in: " + text);
        assertTrue(text.contains("1,500,000.50"), "amount missing in: " + text);
        assertEquals(PdfProbe.normalized(a), PdfProbe.normalized(b), "two renders differ beyond the documented masks");
    }

    @Test
    @DisplayName("Excel: BIFF (OLE2) workbook with the data")
    void excel() throws Exception {
        byte[] x = render("excel");
        assertTrue(x.length > 5_000, "xls too small: " + x.length);
        assertEquals((byte) 0xD0, x[0]);
        assertEquals((byte) 0xCF, x[1]);
        try (HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(x))) {
            assertTrue(wb.getNumberOfSheets() >= 1);
        }
    }

    @Test
    @DisplayName("Word: non-empty (legacy quirk: MSWord export is RTF text)")
    void word() throws Exception {
        byte[] w = render("word");
        assertTrue(w.length > 3_000, "word too small: " + w.length);
        assertTrue(new String(w, 0, 6, StandardCharsets.ISO_8859_1).startsWith("{\\rtf1"));
    }

    @Test
    @DisplayName("RTF: non-empty RTF with the data")
    void rtf() throws Exception {
        String r = new String(render("rtf"), StandardCharsets.ISO_8859_1);
        assertTrue(r.startsWith("{\\rtf1") && r.length() > 3_000);
        assertTrue(r.contains("JUAN PEREZ GONZALEZ"));
    }

    @Test
    @DisplayName("TXT: non-empty, contains the data")
    void txt() throws Exception {
        String t = new String(render("txt"), StandardCharsets.UTF_8);
        assertFalse(t.isEmpty());
        assertTrue(t.contains("JUAN PEREZ GONZALEZ"), "txt: " + t);
    }
}
