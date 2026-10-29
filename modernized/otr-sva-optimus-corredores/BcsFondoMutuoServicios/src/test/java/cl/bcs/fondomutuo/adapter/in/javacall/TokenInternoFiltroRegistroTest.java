package cl.bcs.fondomutuo.adapter.in.javacall;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/** L5: a rejected token is logged at WARN with the remote address and the path, never the token (neither the expected nor the received one). */
class TokenInternoFiltroRegistroTest {

    @Test
    void elRechazoSeRegistraConDireccionYRuta_sinElToken() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(TokenInternoFilter.class);
        ListAppender<ILoggingEvent> app = new ListAppender<>();
        app.start();
        logger.addAppender(app);
        try {
            TokenInternoFilter f = new TokenInternoFilter("secreto-esperado");
            MockHttpServletRequest rq = new MockHttpServletRequest("POST", "/internal/java-call");
            rq.setRequestURI("/internal/java-call");
            rq.setRemoteAddr("10.1.2.3");
            rq.addHeader(TokenInternoFilter.CABECERA, "secreto-recibido-malo");
            MockHttpServletResponse rs = new MockHttpServletResponse();
            f.doFilter(rq, rs, new MockFilterChain());
            assertThat(rs.getStatus()).isEqualTo(401);
            assertThat(app.list).hasSize(1);
            ILoggingEvent e = app.list.get(0);
            assertThat(e.getLevel()).isEqualTo(Level.WARN);
            assertThat(e.getFormattedMessage()).contains("10.1.2.3").contains("/internal/java-call")
                    .doesNotContain("secreto-esperado").doesNotContain("secreto-recibido-malo");
            MockHttpServletRequest ok = new MockHttpServletRequest("POST", "/internal/java-call");
            ok.setRequestURI("/internal/java-call");
            ok.addHeader(TokenInternoFilter.CABECERA, "secreto-esperado");
            f.doFilter(ok, new MockHttpServletResponse(), new MockFilterChain());
            assertThat(app.list).as("an accepted request is not logged").hasSize(1);
        } finally {
            logger.detachAppender(app);
        }
    }
}
