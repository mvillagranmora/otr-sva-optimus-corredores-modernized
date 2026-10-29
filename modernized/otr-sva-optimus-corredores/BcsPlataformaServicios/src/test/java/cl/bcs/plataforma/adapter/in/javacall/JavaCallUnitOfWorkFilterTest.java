package cl.bcs.plataforma.adapter.in.javacall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.JsonObject;

/**
 * Unit of work of the legacy CallServlet (CallServlet.java:441-456,495): COMMIT only for 2xx with P_OUT_ERRORESCONTADOR = 0; ROLLBACK when
 * the counter is > 0 or absent, or when the call fails (500). A test-only handler writes a row and answers as told by the request.
 */
@SpringBootTest(properties = "services.internal-token=t")
@ActiveProfiles("test")
@Import(JavaCallUnitOfWorkFilterTest.Escritor.class)
class JavaCallUnitOfWorkFilterTest {

    @TestConfiguration
    static class Escritor {
        @Bean
        JavaCallHandler escritorDePrueba() {
            return new JavaCallHandler() {
                public String fqn() { return "test.Escribe"; }
                public JsonObject call(Connection con, JsonObject in) throws Exception {
                    con.createStatement().execute("INSERT INTO ESCRIBE_TEST VALUES (1)");
                    if (in.has("falla")) throw new IllegalStateException("boom");
                    JsonObject o = new JsonObject();
                    if (in.has("contador")) o.addProperty("P_OUT_ERRORESCONTADOR", in.get("contador").getAsLong());
                    return o;
                }
            };
        }
    }

    @Autowired WebApplicationContext wac;
    @Autowired ApplicationContext ctx;
    @Autowired DataSource ds;
    MockMvc mvc;

    @BeforeEach
    void preparar() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilters(ctx.getBean(TokenInternoFilter.class), ctx.getBean(JavaCallUnitOfWorkFilter.class)).build();
        try (Connection c = ds.getConnection()) {
            c.createStatement().execute("DROP TABLE IF EXISTS ESCRIBE_TEST");
            c.createStatement().execute("CREATE TABLE ESCRIBE_TEST (id INT)");
        }
    }

    private int filas(String params, String uri) throws Exception {
        mvc.perform(post(uri).header("X-Internal-Token", "t").contentType("application/json")
                .content("{\"class\":\"test.Escribe\",\"params\":" + params + "}"));
        try (Connection c = ds.getConnection(); var rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM ESCRIBE_TEST")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test void contadorCero_commit() throws Exception { assertThat(filas("{\"contador\":0}", "/internal/java-call")).isEqualTo(1); }
    @Test void contadorMayorACero_rollback() throws Exception { assertThat(filas("{\"contador\":1}", "/internal/java-call")).isZero(); }
    @Test void sinContador_rollback() throws Exception { assertThat(filas("{}", "/internal/java-call")).isZero(); }
    @Test void excepcion_500_rollback() throws Exception { assertThat(filas("{\"falla\":1}", "/internal/java-call")).isZero(); }

    @Test
    void rutaConVariantes_tambienVaEnLaUnidadDeTrabajo() throws Exception {
        assertThat(filas("{\"contador\":1}", "/internal;x=1/java-call")).isZero();
        assertThat(filas("{\"contador\":1}", "//internal/java-call")).isZero();
    }

    @Test
    void decisionPorEstadoYCuerpo() {
        byte[] ok = "{\"P_OUT_ERRORESCONTADOR\":0}".getBytes(StandardCharsets.UTF_8);
        assertThat(JavaCallUnitOfWorkFilter.confirmable(200, ok)).isTrue();
        assertThat(JavaCallUnitOfWorkFilter.confirmable(200, "{\"P_OUT_ERRORCOUNTER\":0}".getBytes(StandardCharsets.UTF_8))).isTrue();
        assertThat(JavaCallUnitOfWorkFilter.confirmable(500, ok)).isFalse();
        assertThat(JavaCallUnitOfWorkFilter.confirmable(503, ok)).isFalse();
        assertThat(JavaCallUnitOfWorkFilter.confirmable(200, "{\"P_OUT_ERRORESCONTADOR\":2}".getBytes(StandardCharsets.UTF_8))).isFalse();
        assertThat(JavaCallUnitOfWorkFilter.confirmable(200, "{}".getBytes(StandardCharsets.UTF_8))).isFalse();
        assertThat(JavaCallUnitOfWorkFilter.confirmable(200, "[]".getBytes(StandardCharsets.UTF_8))).isFalse();
        assertThat(JavaCallUnitOfWorkFilter.confirmable(200, new byte[0])).isFalse();
        assertThat(JavaCallUnitOfWorkFilter.confirmable(200, "no json".getBytes(StandardCharsets.UTF_8))).isFalse();
    }
}
