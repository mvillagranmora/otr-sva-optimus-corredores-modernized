package cl.bcs.fondomutuo.adapter.in.javacall;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import cl.bcs.fondomutuo.TestBd;
import cl.bcs.fondomutuo.config.TransaccionConfig;

/**
 * FM-KIT-70..82: the unit of work is a SPRING transaction (C1) with the {@code CallServlet} rule (no counter or counter &gt; 0 = rollback; exception or non-2xx
 * = rollback), and its manager does not mark rollback-only a transaction whose participant threw and was swallowed (C1b). H2 only.
 */
class JavaCallUnitOfWorkFilterTest {

    static final String T = "\"BSCL.Plataforma.BD::REG.Ciudad\"";
    DataSource ds;
    PlatformTransactionManager tm;
    JavaCallUnitOfWorkFilter filtro;

    @BeforeEach
    void preparar() {
        ds = TestBd.nueva();
        tm = new TransaccionConfig().transactionManager(ds);
        filtro = new JavaCallUnitOfWorkFilter(tm);
    }

    /** Inserts one row on the connection of the current unit of work (what a port adapter does through {@code DataSourceUtils}). */
    private void insertar(String nombre) {
        Connection c = DataSourceUtils.getConnection(ds);
        try {
            c.createStatement().execute("INSERT INTO " + T + " VALUES ((SELECT COALESCE(MAX(\"IdCiudad\"),0)+1 FROM " + T + "), '" + nombre + "')");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            DataSourceUtils.releaseConnection(c, ds);
        }
    }

    private long filas() {
        return TestBd.contar(ds, T);
    }

    private MockHttpServletResponse correr(String uri, int status, String json, String filaInsertada) throws Exception {
        MockHttpServletRequest rq = new MockHttpServletRequest("POST", uri);
        rq.setRequestURI(uri);
        MockHttpServletResponse rs = new MockHttpServletResponse();
        filtro.doFilter(rq, rs, (req, res) -> {
            insertar(filaInsertada);
            jakarta.servlet.http.HttpServletResponse h = (jakarta.servlet.http.HttpServletResponse) res;
            h.setStatus(status);
            if (json != null) h.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        });
        return rs;
    }

    @Test
    void fmKit70_contadorCero_commit_yElClienteRecibeElCuerpoIntacto() throws Exception {
        MockHttpServletResponse rs = correr("/internal/java-call", 200, "{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[]}", "A");
        assertThat(rs.getStatus()).isEqualTo(200);
        assertThat(rs.getContentAsString()).isEqualTo("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[]}");
        assertThat(filas()).isEqualTo(1);
    }

    @Test
    void fmKit71_contadorMayorACero_rollback_peroElCuerpoSeEntrega() throws Exception {
        MockHttpServletResponse rs = correr("/internal/java-call", 200, "{\"P_OUT_ERRORESCONTADOR\":2}", "A");
        assertThat(rs.getStatus()).isEqualTo(200);
        assertThat(rs.getContentAsString()).contains("\"P_OUT_ERRORESCONTADOR\":2");
        assertThat(filas()).isZero();
    }

    @Test
    void fmKit72_faltaElContador_rollback_reglaDeCallServlet() throws Exception {
        correr("/internal/java-call", 200, "{\"P_OUT_MENSAJES\":[]}", "A");
        assertThat(filas()).isZero();
    }

    @Test
    void fmKit73_cuerpoVacioOQueNoEsObjeto_rollback() throws Exception {
        correr("/internal/java-call", 200, null, "A");
        correr("/internal/java-call", 200, "[0]", "B");
        correr("/internal/java-call", 200, "no es json", "C");
        correr("/internal/java-call", 200, "{\"P_OUT_ERRORESCONTADOR\":\"x\"}", "D");
        assertThat(filas()).isZero();
    }

    @Test
    void fmKit74_noEs2xx_rollback_503DeCompuertaY500GenericoIncluidos() throws Exception {
        correr("/internal/java-call", 503, "{\"P_OUT_ERRORESCONTADOR\":0}", "A");
        correr("/internal/java-call", 500, "{\"P_OUT_ERRORESCONTADOR\":0}", "B");
        correr("/internal/java-call", 400, "{\"P_OUT_ERRORESCONTADOR\":0}", "C");
        assertThat(filas()).isZero();
    }

    @Test
    void fmKit75_nombreAlternativoDelContador_P_OUT_ERRORCOUNTER() {
        assertThat(JavaCallUnitOfWorkFilter.confirmable(200, "{\"P_OUT_ERRORCOUNTER\":0}".getBytes(StandardCharsets.UTF_8))).isTrue();
        assertThat(JavaCallUnitOfWorkFilter.confirmable(200, "{\"P_OUT_ERRORCOUNTER\":1}".getBytes(StandardCharsets.UTF_8))).isFalse();
        assertThat(JavaCallUnitOfWorkFilter.confirmable(299, "{\"P_OUT_ERRORESCONTADOR\":0}".getBytes(StandardCharsets.UTF_8))).isTrue();
        assertThat(JavaCallUnitOfWorkFilter.confirmable(300, "{\"P_OUT_ERRORESCONTADOR\":0}".getBytes(StandardCharsets.UTF_8))).isFalse();
    }

    @Test
    void fmKit76_laRutaSeCompara_normalizada_lasVariantesTambienTienenTransaccion() throws Exception {
        for (String ruta : new String[] {"//internal/java-call", "/internal;x=1/java-call", "/internal/./java-call", "/internal/%6Aava-call"}) {
            correr(ruta, 200, "{\"P_OUT_ERRORESCONTADOR\":1}", "R" + Math.abs(ruta.hashCode()));
        }
        assertThat(filas()).isZero();   // rolled back: they ran inside the unit of work
    }

    @Test
    void fmKit77_fueraDeInternal_noAbreTransaccion() throws Exception {
        correr("/actuator/health", 200, "{\"P_OUT_ERRORESCONTADOR\":1}", "A");
        assertThat(filas()).isEqualTo(1);   // no unit of work: the insert went out on its own connection and committed
    }

    @Test
    void fmKit78_excepcionDelHandler_rollback() throws Exception {
        MockHttpServletRequest rq = new MockHttpServletRequest("POST", "/internal/java-call");
        rq.setRequestURI("/internal/java-call");
        try {
            filtro.doFilter(rq, new MockHttpServletResponse(), (req, res) -> {
                insertar("A");
                throw new jakarta.servlet.ServletException("boom");
            });
        } catch (jakarta.servlet.ServletException esperado) {
            assertThat(esperado).hasMessage("boom");
        }
        assertThat(filas()).isZero();
    }

    @Test
    void fmKit79_c1_elTrabajoDelRequestUsaLaMismaConexionQueLaTransaccion() throws Exception {
        AtomicReference<Connection> enFiltro = new AtomicReference<>();
        MockHttpServletRequest rq = new MockHttpServletRequest("POST", "/internal/java-call");
        rq.setRequestURI("/internal/java-call");
        filtro.doFilter(rq, new MockHttpServletResponse(), (req, res) -> {
            Connection a = DataSourceUtils.getConnection(ds);
            Connection b = DataSourceUtils.getConnection(ds);
            enFiltro.set(a);
            assertThat(b).isSameAs(a);
            assertThat(org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            assertThat(DataSourceUtils.isConnectionTransactional(a, ds)).isTrue();
            DataSourceUtils.releaseConnection(a, ds);
            DataSourceUtils.releaseConnection(b, ds);
        });
        assertThat(enFiltro.get()).isNotNull();
    }

    @Test
    void fmKit80_c1_unParticipanteTransaccionalSeUneALaMismaTransaccion_ySuRollbackTotalDeshaceTodo() throws Exception {
        TransactionTemplate participante = new TransactionTemplate(tm);   // what an @Transactional library (Tesoreria) does: REQUIRED
        MockHttpServletRequest rq = new MockHttpServletRequest("POST", "/internal/java-call");
        rq.setRequestURI("/internal/java-call");
        MockHttpServletResponse rs = new MockHttpServletResponse();
        filtro.doFilter(rq, rs, (req, res) -> {
            insertar("PROPIO");
            participante.executeWithoutResult(s -> insertar("DELPARTICIPANTE"));
            assertThat(filas()).as("both uncommitted, same session").isZero();
            jakarta.servlet.http.HttpServletResponse h = (jakarta.servlet.http.HttpServletResponse) res;
            h.setStatus(200);
            h.getOutputStream().write("{\"P_OUT_ERRORESCONTADOR\":1}".getBytes(StandardCharsets.UTF_8));
        });
        assertThat(filas()).isZero();   // the counter rolled back the work of the participant as well
    }

    @Test
    void fmKit81_c1b_unParticipanteQueLanzaYSeTraga_noDejaLaTransaccionEnRollbackOnly() throws Exception {
        assertThat(((DataSourceTransactionManager) tm).isGlobalRollbackOnParticipationFailure()).isFalse();
        TransactionTemplate participante = new TransactionTemplate(tm);
        MockHttpServletRequest rq = new MockHttpServletRequest("POST", "/internal/java-call");
        rq.setRequestURI("/internal/java-call");
        MockHttpServletResponse rs = new MockHttpServletResponse();
        filtro.doFilter(rq, rs, (req, res) -> {
            insertar("ANTES");
            try {
                participante.executeWithoutResult(s -> {
                    throw new IllegalStateException("collaborator failed");
                });
            } catch (IllegalStateException tragada) {
                // the legacy caller handled it and went on (GirarComprobante... around CrearComprobante)
            }
            jakarta.servlet.http.HttpServletResponse h = (jakarta.servlet.http.HttpServletResponse) res;
            h.setStatus(200);
            h.getOutputStream().write("{\"P_OUT_ERRORESCONTADOR\":0}".getBytes(StandardCharsets.UTF_8));
        });
        assertThat(rs.getStatus()).isEqualTo(200);          // no UnexpectedRollbackException -> 500
        assertThat(filas()).isEqualTo(1);                  // the work before the swallowed failure is committed (legacy behaviour)
    }

    @Test
    void fmKit82_c1b_conElValorPorDefectoDeSpring_elMismoCasoSeriaUn500() throws Exception {
        DataSourceTransactionManager porDefecto = new DataSourceTransactionManager(ds);   // control: Spring default (true)
        assertThat(porDefecto.isGlobalRollbackOnParticipationFailure()).isTrue();
        filtro = new JavaCallUnitOfWorkFilter(porDefecto);
        TransactionTemplate participante = new TransactionTemplate(porDefecto);
        MockHttpServletRequest rq = new MockHttpServletRequest("POST", "/internal/java-call");
        rq.setRequestURI("/internal/java-call");
        MockHttpServletResponse rs = new MockHttpServletResponse();
        filtro.doFilter(rq, rs, (req, res) -> {
            insertar("ANTES");
            try {
                participante.executeWithoutResult(s -> {
                    throw new IllegalStateException("collaborator failed");
                });
            } catch (IllegalStateException tragada) {
                // swallowed
            }
            jakarta.servlet.http.HttpServletResponse h = (jakarta.servlet.http.HttpServletResponse) res;
            h.setStatus(200);
            h.getOutputStream().write("{\"P_OUT_ERRORESCONTADOR\":0}".getBytes(StandardCharsets.UTF_8));
        });
        assertThat(rs.getStatus()).isEqualTo(500);
        assertThat(rs.getContentAsString()).matches("\\{\"error\":\"internal error\",\"correlationId\":\"[0-9a-f-]{36}\"\\}");
        assertThat(filas()).isZero();
    }
}
