package cl.bcs.fondomutuo.adapter.in.javacall;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import cl.bcs.fondomutuo.TestBd;
import cl.bcs.fondomutuo.config.TransaccionConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Architecture review F3 W1 (H2, user decision): the transaction of the eight UI reads is READ ONLY; the writes, the Manejador and any other body are not. The chain is the real
 * one (limits filter, then unit of work); the "servlet" records what the transaction looks like from inside.
 */
class JavaCallLecturaSoloLecturaTest {

    private static final String OK = "{\"P_OUT_ERRORESCONTADOR\":0}";

    DataSource ds;
    PlatformTransactionManager tm;
    JavaCallLimitesFilter limites;
    JavaCallUnitOfWorkFilter uow;
    final List<Boolean> soloLectura = new ArrayList<>();
    final List<Boolean> activa = new ArrayList<>();

    @BeforeEach
    void preparar() {
        ds = TestBd.nueva();
        tm = new TransaccionConfig().transactionManager(ds);
        limites = new JavaCallLimitesFilter(JavaCallLimitesFilter.MAX_BODY_POR_DEFECTO, JavaCallLimitesFilter.MAX_PROFUNDIDAD_POR_DEFECTO,
                JavaCallLimitesFilter.MAX_LISTA_POR_DEFECTO);
        uow = new JavaCallUnitOfWorkFilter(tm);
    }

    private final HttpServlet servlet = new HttpServlet() {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            soloLectura.add(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
            activa.add(TransactionSynchronizationManager.isActualTransactionActive());
            resp.setContentType("application/json");
            resp.getOutputStream().write(OK.getBytes(StandardCharsets.UTF_8));
        }
    };

    private boolean esSoloLectura(String cuerpo) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/internal/java-call");
        req.setRequestURI("/internal/java-call");
        req.setContentType("application/json");
        req.setContent(cuerpo.getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse resp = new MockHttpServletResponse();
        soloLectura.clear();
        activa.clear();
        new MockFilterChain(servlet, limites, uow).doFilter(req, resp);
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(activa).containsExactly(true);
        assertThat(soloLectura).hasSize(1);
        return soloLectura.get(0);
    }

    private static String cuerpo(String clase) {
        return "{\"class\":\"" + clase + "\",\"params\":{\"p_IdEmpresa\":1,\"p_IdUsuario\":97}}";
    }

    @Test
    void lasOchoLecturas_abrenLaTransaccionSoloLectura() throws Exception {
        assertThat(JavaCallDispatcher.CLASES_LECTURA).hasSize(8).isSubsetOf(JavaCallDispatcher.LISTA_BLANCA);
        for (String clase : JavaCallDispatcher.CLASES_LECTURA) {
            assertThat(esSoloLectura(cuerpo(clase))).as(clase).isTrue();
        }
    }

    @Test
    void lasEscriturasYElManejador_siguenLeyendoYEscribiendo() throws Exception {
        for (String clase : new String[] {JavaCallDispatcher.MANEJADOR, JavaCallDispatcher.CREAR_SOLICITUD_NORMAL, JavaCallDispatcher.CREAR_SOLICITUD_RESCATE_REINVERSION,
                "x.Y", "cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate "}) {
            assertThat(esSoloLectura(cuerpo(clase))).as(clase).isFalse();
        }
    }

    @Test
    void sinClaseOConClaseQueNoEsTexto_noEsSoloLectura() throws Exception {
        assertThat(esSoloLectura("{\"params\":{}}")).isFalse();
        assertThat(esSoloLectura("{\"class\":7,\"params\":{}}")).isFalse();
        assertThat(esSoloLectura("[1]")).isFalse();
    }

    @Test
    void laClaseDeNivelSuperiorMandaYLaUltimaGana_comoElControlador() throws Exception {
        String lectura = JavaCallDispatcher.CONSULTAR_DETALLE_SOLICITUD;
        // a "class" nested in params is not the class of the request
        assertThat(esSoloLectura("{\"class\":\"" + JavaCallDispatcher.MANEJADOR + "\",\"params\":{\"class\":\"" + lectura + "\"}}")).isFalse();
        // repeated key: Gson keeps the last one for the controller, so it must be the one that decides
        assertThat(esSoloLectura("{\"class\":\"" + lectura + "\",\"class\":\"" + JavaCallDispatcher.MANEJADOR + "\"}")).isFalse();
        assertThat(esSoloLectura("{\"class\":\"" + JavaCallDispatcher.MANEJADOR + "\",\"class\":\"" + lectura + "\"}")).isTrue();
    }

    @Test
    void sinElFiltroDeLimites_laTransaccionNoEsSoloLectura_falloSeguro() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/internal/java-call");
        req.setRequestURI("/internal/java-call");
        req.setContent(cuerpo(JavaCallDispatcher.CONSULTAR_FECHA_SOLICITUD_APORTE_RESCATE).getBytes(StandardCharsets.UTF_8));
        soloLectura.clear();
        new MockFilterChain(servlet, uow).doFilter(req, new MockHttpServletResponse());
        assertThat(soloLectura).containsExactly(false);
    }

    @Test
    void unaTransaccionDeLecturaAunConfirmaConElContadorEnCero() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/internal/java-call");
        req.setRequestURI("/internal/java-call");
        req.setContent(cuerpo(JavaCallDispatcher.CONSULTAR_VALOR_DOLAR_FONDO_MUTUO).getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse resp = new MockHttpServletResponse();
        new MockFilterChain(servlet, limites, uow).doFilter(req, resp);
        assertThat(resp.getContentAsString()).isEqualTo(OK);
    }

    @Test
    void claseDe_extraeLaClaseDeNivelSuperior() throws SQLException {
        assertThat(JavaCallLimitesFilter.claseDe("{\"a\":{\"class\":\"n\"},\"class\":\"x.Y\"}".getBytes(StandardCharsets.UTF_8))).isEqualTo("x.Y");
        assertThat(JavaCallLimitesFilter.claseDe("no json".getBytes(StandardCharsets.UTF_8))).isNull();
        assertThat(JavaCallLimitesFilter.claseDe("{}".getBytes(StandardCharsets.UTF_8))).isNull();
    }
}
