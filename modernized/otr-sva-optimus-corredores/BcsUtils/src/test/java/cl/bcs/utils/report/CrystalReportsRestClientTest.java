package cl.bcs.utils.report;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keeps the API consumed by 3 modules: CrystalReportRequest / CrystalReportsPort / CrystalReportsRestClient (first-pass
 * classes kept as-is, contract pinned here). Uses a local JDK HttpServer on an ephemeral port (no external service).
 */
class CrystalReportsRestClientTest {

    HttpServer server;
    final AtomicReference<String> metodo = new AtomicReference<>();
    final AtomicReference<String> ruta = new AtomicReference<>();
    final AtomicReference<String> tipo = new AtomicReference<>();
    final AtomicReference<String> cuerpo = new AtomicReference<>();

    @BeforeEach
    void arrancar() throws Exception {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", ex -> {
            metodo.set(ex.getRequestMethod());
            ruta.set(ex.getRequestURI().getPath());
            tipo.set(ex.getRequestHeaders().getFirst("Content-Type"));
            cuerpo.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] pdf = "%PDF-1.4 falso".getBytes(StandardCharsets.US_ASCII);
            ex.getResponseHeaders().add("Content-Type", "application/pdf");
            ex.sendResponseHeaders(200, pdf.length);
            ex.getResponseBody().write(pdf);
            ex.close();
        });
        server.start();
    }

    @AfterEach
    void parar() {
        server.stop(0);
    }

    @Test
    @DisplayName("CrystalReportRequest factories set templateId and format pdf / excel / word")
    void fabricas() {
        assertThat(CrystalReportRequest.pdf("T1")).satisfies(r -> {
            assertThat(r.templateId).isEqualTo("T1");
            assertThat(r.format).isEqualTo("pdf");
            assertThat(r.beanData).isNull();
        });
        assertThat(CrystalReportRequest.excel("T2").format).isEqualTo("excel");
        assertThat(CrystalReportRequest.word("T3").format).isEqualTo("word");
    }

    @Test
    @DisplayName("generateReport POSTs the request as JSON to {baseUrl}/reports/generate and returns the response bytes")
    void generaReporte() {
        CrystalReportsPort port = new CrystalReportsRestClient("http://127.0.0.1:" + server.getAddress().getPort());
        CrystalReportRequest r = CrystalReportRequest.pdf("CARTOLA");
        r.beanData = Map.of("rut", "1-9");
        byte[] out = port.generateReport(r);
        assertThat(new String(out, StandardCharsets.US_ASCII)).isEqualTo("%PDF-1.4 falso");
        assertThat(metodo.get()).isEqualTo("POST");
        assertThat(ruta.get()).isEqualTo("/reports/generate");
        assertThat(tipo.get()).startsWith("application/json");
        JsonObject j = JsonParser.parseString(cuerpo.get()).getAsJsonObject();
        assertThat(j.get("templateId").getAsString()).isEqualTo("CARTOLA");
        assertThat(j.get("format").getAsString()).isEqualTo("pdf");
        assertThat(j.getAsJsonObject("beanData").get("rut").getAsString()).isEqualTo("1-9");
    }

    @Test
    @DisplayName("the base URL comes from crystal.reports.url (default http://localhost:8100)")
    void urlConfigurable() throws Exception {
        Value v = (Value) CrystalReportsRestClient.class.getConstructor(String.class).getParameterAnnotations()[0][0];
        assertThat(v.value()).isEqualTo("${crystal.reports.url:http://localhost:8100}");
    }
}
