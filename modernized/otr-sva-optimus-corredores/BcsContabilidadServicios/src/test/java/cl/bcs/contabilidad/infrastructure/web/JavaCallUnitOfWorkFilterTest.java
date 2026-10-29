package cl.bcs.contabilidad.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The JavaCall unit of work follows legacy BcsPlataformaWeb CallServlet.java:436-441,495: one transaction per
 * call, committed only when the response is 2xx and its error counter is 0, rolled back otherwise.
 * Architecture review finding H1/H2.
 */
class JavaCallUnitOfWorkFilterTest {

    DataSourceTransactionManager tm;
    JdbcTemplate jdbc;
    JavaCallUnitOfWorkFilter filter;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource ds = new DriverManagerDataSource(
                "jdbc:h2:mem:uow-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        tm = new DataSourceTransactionManager(ds);
        jdbc = new JdbcTemplate(ds);
        jdbc.execute("CREATE TABLE T (ID INT)");
        filter = new JavaCallUnitOfWorkFilter(tm);
    }

    MockHttpServletResponse call(FilterChain chain) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/internal/java-call");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req, resp, chain);
        return resp;
    }

    FilterChain writesAndAnswers(int status, String body) {
        return (req, resp) -> {
            jdbc.update("INSERT INTO T VALUES (1)");
            jdbc.update("INSERT INTO T VALUES (2)");
            ((jakarta.servlet.http.HttpServletResponse) resp).setStatus(status);
            resp.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        };
    }

    int rows() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM T", Integer.class);
    }

    @Test
    @DisplayName("counter 0 -> both writes committed, response unchanged (CallServlet :495)")
    void commitsWhenCounterIsZero() throws Exception {
        MockHttpServletResponse resp = call(writesAndAnswers(200, "{\"P_OUT_ERRORESCONTADOR\":0,\"x\":1}"));
        assertThat(rows()).isEqualTo(2);
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(resp.getContentAsString()).isEqualTo("{\"P_OUT_ERRORESCONTADOR\":0,\"x\":1}");
    }

    @Test
    @DisplayName("counter > 0 -> every write rolled back, the error JSON is still returned (CallServlet :439-441)")
    void rollsBackWhenCounterPositive() throws Exception {
        MockHttpServletResponse resp = call(writesAndAnswers(200, "{\"P_OUT_ERRORESCONTADOR\":1}"));
        assertThat(rows()).isZero();
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(resp.getContentAsString()).isEqualTo("{\"P_OUT_ERRORESCONTADOR\":1}");
    }

    @Test
    @DisplayName("missing counter -> rolled back, as legacy")
    void rollsBackWhenCounterMissing() throws Exception {
        call(writesAndAnswers(200, "{\"x\":1}"));
        assertThat(rows()).isZero();
    }

    @Test
    @DisplayName("P_OUT_ERRORCOUNTER takes precedence over P_OUT_ERRORESCONTADOR (CallServlet :290/:367)")
    void errorCounterAliasWins() throws Exception {
        call(writesAndAnswers(200, "{\"P_OUT_ERRORCOUNTER\":2,\"P_OUT_ERRORESCONTADOR\":0}"));
        assertThat(rows()).isZero();
        call(writesAndAnswers(200, "{\"P_OUT_ERRORCOUNTER\":0}"));
        assertThat(rows()).isEqualTo(2);
    }

    @Test
    @DisplayName("503 fallback (or any non-2xx) -> rolled back, so TomEE can re-run the call from scratch")
    void rollsBackOnNon2xx() throws Exception {
        call(writesAndAnswers(503, "{\"P_OUT_ERRORESCONTADOR\":0}"));
        assertThat(rows()).isZero();
    }

    @Test
    @DisplayName("exception -> rolled back and propagated")
    void rollsBackOnException() {
        FilterChain chain = (req, resp) -> {
            jdbc.update("INSERT INTO T VALUES (1)");
            throw new ServletException("boom");
        };
        assertThatThrownBy(() -> call(chain)).isInstanceOf(ServletException.class).hasMessage("boom");
        assertThat(rows()).isZero();
    }

    @Test
    @DisplayName("a nested savepoint that fails is undone alone; the rest of the call commits (B-03 + NESTED)")
    void nestedFailureDoesNotPoisonTheCall() throws Exception {
        TransactionTemplate nested = new TransactionTemplate(tm);
        nested.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
        FilterChain chain = (req, resp) -> {
            jdbc.update("INSERT INTO T VALUES (1)");
            try {
                nested.executeWithoutResult(s -> {
                    jdbc.update("INSERT INTO T VALUES (2)");
                    throw new IllegalStateException("tipo fallido");
                });
            } catch (IllegalStateException ignored) {
                // B-03: logged, not counted, the batch continues
            }
            resp.getOutputStream().write("{\"P_OUT_ERRORESCONTADOR\":0}".getBytes(StandardCharsets.UTF_8));
        };
        call(chain);
        assertThat(jdbc.queryForList("SELECT ID FROM T", Integer.class)).containsExactly(1);
    }

    @Test
    @DisplayName("participant marked rollback-only but response says success -> 500, nothing committed")
    void noFalseSuccess() throws Exception {
        TransactionTemplate required = new TransactionTemplate(tm);
        FilterChain chain = (req, resp) -> {
            jdbc.update("INSERT INTO T VALUES (1)");
            try {
                required.executeWithoutResult(s -> { throw new IllegalStateException("inner"); });
            } catch (IllegalStateException ignored) {
                // swallowed by a caller: the shared transaction is now rollback-only
            }
            resp.getOutputStream().write("{\"P_OUT_ERRORESCONTADOR\":0}".getBytes(StandardCharsets.UTF_8));
        };
        MockHttpServletResponse resp = call(chain);
        assertThat(rows()).isZero();
        assertThat(resp.getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("other paths are not wrapped")
    void otherPathsUntouched() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req, resp, writesAndAnswers(200, "{}"));
        assertThat(rows()).isEqualTo(2);
    }
}
