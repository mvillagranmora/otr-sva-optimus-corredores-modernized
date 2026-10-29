package cl.bcs.fondomutuo.seguridad;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallController;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallUnitOfWorkFilter;
import cl.bcs.fondomutuo.config.GlobalExceptionHandler;
import cl.bcs.fondomutuo.config.LogSeguro;

/** SEC-003/SEC-006: no handler returns the exception text or the JDBC cause; the detail is logged server-side with control characters removed. */
class ErroresSinDetalleInternoTest {

    static final String SECRETO = "jdbc:sap://hana-prod.interno:30015 user=SYSTEM SELECT * FROM TABLA_X";
    final GlobalExceptionHandler h = new GlobalExceptionHandler();

    private void sinTexto(ProblemDetail pd, int status) {
        assertThat(pd.getStatus()).isEqualTo(status);
        String todo = pd.getTitle() + "|" + pd.getDetail() + "|" + pd.getProperties();
        assertThat(todo).doesNotContain("hana-prod").doesNotContain("SELECT").doesNotContain("SYSTEM").doesNotContain("TABLA_X");
        assertThat(pd.getProperties()).containsKey("correlationId");
    }

    @Test
    void npeIaeNfeUoe_cuerpoFijoConCorrelationId() {
        sinTexto(h.handleNpe(new NullPointerException(SECRETO)), 400);
        sinTexto(h.handleIae(new IllegalArgumentException(SECRETO)), 400);
        sinTexto(h.handleNfe(new NumberFormatException(SECRETO)), 400);
        sinTexto(h.handleUoe(new UnsupportedOperationException(SECRETO)), 503);
        sinTexto(h.handleNpe(new NullPointerException()), 400);
    }

    @Test
    void jdbc_noDevuelveLaCausaRaiz() {
        ResponseEntity<Map<String, String>> r = h.handleJdbcConnection(
                new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", new SQLException(SECRETO)));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(503));
        assertThat(r.getBody()).containsKeys("error", "message", "correlationId");
        assertThat(r.getBody().toString()).doesNotContain("hana-prod").doesNotContain("SYSTEM").doesNotContain("jdbc:");
    }

    @Test
    void log_sinSaltosDeLineaNiControl_conElDetalleEnElServidor() {
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        ListAppender<ILoggingEvent> app = new ListAppender<>();
        app.start();
        logger.addAppender(app);
        try {
            h.handleIae(new IllegalArgumentException("a\r\n2026-01-01 ERROR forged line\tb\u0007"));
            h.handleJdbcConnection(new CannotGetJdbcConnectionException("x", new SQLException("host\nX")));
            assertThat(app.list).hasSize(2);
            for (ILoggingEvent e : app.list) {
                assertThat(e.getFormattedMessage()).doesNotContain("\r").doesNotContain("\n").doesNotContain("\t").doesNotContain("\u0007");
                assertThat(e.getFormattedMessage()).contains("correlationId=");
            }
            assertThat(app.list.get(0).getFormattedMessage()).contains("forged line");
        } finally {
            logger.detachAppender(app);
        }
        assertThat(LogSeguro.limpiar("x\ny")).isEqualTo("x_y");
        assertThat(LogSeguro.limpiar("z".repeat(1000))).hasSize(LogSeguro.MAXIMO + 3);
        assertThat(LogSeguro.limpiar(null)).isEqualTo("null");
    }

    @Test
    void controlador_sinConexion_da500GenericoSinTextoJdbc() {
        DataSource caido = (DataSource) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {DataSource.class},
                (p, m, a) -> {
                    if (m.getName().equals("getConnection")) throw new SQLException(SECRETO);
                    throw new UnsupportedOperationException();
                });
        JavaCallHandler manejador = new JavaCallHandler() {
            public String fqn() {
                return JavaCallDispatcher.CREAR_SOLICITUD_NORMAL;
            }

            public com.google.gson.JsonObject call(Connection con, com.google.gson.JsonObject j) {
                throw new AssertionError("must not run");
            }
        };
        JavaCallController c = new JavaCallController(new JavaCallDispatcher(List.of(manejador)), caido);
        ResponseEntity<String> r = c.dispatch("{\"class\":\"" + JavaCallDispatcher.CREAR_SOLICITUD_NORMAL + "\",\"params\":{}}");
        assertThat(r.getStatusCode().value()).isEqualTo(500);
        assertThat(r.getBody()).contains("\"error\":\"internal error\"").contains("correlationId")
                .doesNotContain("hana-prod").doesNotContain("SELECT").doesNotContain("jdbc");
    }

    @Test
    void filtroUnidadDeTrabajo_sinPoderAbrirLaTransaccion_da500GenericoConCorrelationId() throws Exception {
        PlatformTransactionManager roto = new PlatformTransactionManager() {
            public org.springframework.transaction.TransactionStatus getTransaction(org.springframework.transaction.TransactionDefinition d) {
                throw new CannotCreateTransactionException("Could not open JDBC Connection", new SQLException(SECRETO));
            }

            public void commit(org.springframework.transaction.TransactionStatus s) {
                throw new AssertionError();
            }

            public void rollback(org.springframework.transaction.TransactionStatus s) {
                throw new AssertionError();
            }
        };
        boolean[] llegoAlControlador = {false};
        MockHttpServletRequest rq = new MockHttpServletRequest("POST", "/internal/java-call");
        rq.setRequestURI("/internal/java-call");
        MockHttpServletResponse rs = new MockHttpServletResponse();
        new JavaCallUnitOfWorkFilter(roto).doFilter(rq, rs, (a, b) -> llegoAlControlador[0] = true);
        assertThat(llegoAlControlador[0]).isFalse();
        assertThat(rs.getStatus()).isEqualTo(500);
        assertThat(rs.getContentAsString()).matches("\\{\"error\":\"internal error\",\"correlationId\":\"[0-9a-f-]{36}\"\\}");
    }
}
