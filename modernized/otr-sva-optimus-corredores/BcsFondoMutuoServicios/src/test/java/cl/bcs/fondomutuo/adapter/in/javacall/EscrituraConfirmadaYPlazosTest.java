package cl.bcs.fondomutuo.adapter.in.javacall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.time.Duration;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariDataSource;

import cl.bcs.fondomutuo.ServidorRouterSimulado;
import cl.bcs.fondomutuo.ServidorRouterSimulado.Respuesta;
import cl.bcs.fondomutuo.adapter.out.http.ColaboradoresHttp;
import cl.bcs.fondomutuo.adapter.out.http.RouterClient;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.config.TransaccionConfig;
import cl.bcs.fondomutuo.kit.ColaboradorErrorException;
import cl.bcs.fondomutuo.kit.EscrituraConfirmada;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * Step 6: M3 (a 503 of the router after a write that may be committed becomes the generic 500, the flag dies with the transaction, nothing is kept between
 * requests) and M2 (transaction timeout, router read timeout: a hung router is a generic 500 and the connection of a pool of size 1 is released). H2 + a
 * simulated router; no HANA.
 */
class EscrituraConfirmadaYPlazosTest {

    static final String T = "\"BSCL.Plataforma.BD::REG.Ciudad\"";
    HikariDataSource ds;
    org.springframework.transaction.PlatformTransactionManager tm;

    @BeforeEach
    void preparar() throws Exception {
        ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:m23" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1;MODE=LEGACY");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setMaximumPoolSize(1);   // ONE connection: a leak or a hold is visible at once
        ds.setConnectionTimeout(1500);
        try (Connection c = ds.getConnection()) {
            ScriptUtils.executeSqlScript(c, new ClassPathResource("fondomutuo-kit/schema-kit.sql"));
        }
        tm = new TransaccionConfig().transactionManager(ds);
    }

    @AfterEach
    void cerrar() {
        ds.close();
    }

    private static RouterClient cliente(String url, Duration lectura) {
        return new RouterClient(url, Duration.ofMillis(500), lectura, "t");
    }

    private long filas() throws Exception {
        try (Connection c = ds.getConnection(); var rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM " + T)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    @Test
    void m3_sinEscrituraConfirmada_el503DelRouterSigueSiendoRepetible() {
        try (ServidorRouterSimulado s = new ServidorRouterSimulado().responde(ColaboradoresHttp.AGREGAR_LOG, Respuesta.estado(503))) {
            new TransactionTemplate(tm).executeWithoutResult(st -> {
                EscrituraConfirmada.escrituraLocalPendiente();   // an INSERT pending but NOT committed (no DDL ran): TomEE may repeat safely
                assertThatThrownBy(() -> new ColaboradoresHttp(cliente(s.url(), Duration.ofSeconds(2))).agregar(new JsonObject()))
                        .isInstanceOf(ServicioNoServidoException.class);
            });
        }
    }

    @Test
    void m3_ddlConUnaEscrituraLocalPendiente_el503DelRouterSeVuelve500Generico() {
        try (ServidorRouterSimulado s = new ServidorRouterSimulado().responde(ColaboradoresHttp.AGREGAR_LOG, Respuesta.estado(503))) {
            new TransactionTemplate(tm).executeWithoutResult(st -> {
                EscrituraConfirmada.escrituraLocalPendiente();
                EscrituraConfirmada.ddlConfirmaEscrituraPendiente();   // the DROP of the lock commits the INSERT on HANA
                assertThat(EscrituraConfirmada.confirmada()).isTrue();
                assertThatThrownBy(() -> new ColaboradoresHttp(cliente(s.url(), Duration.ofSeconds(2))).agregar(new JsonObject()))
                        .isInstanceOf(ColaboradorErrorException.class);
            });
        }
    }

    @Test
    void m3_ddlSinEscrituraLocalPendiente_noMarcaNada_loQueHacenLasCompuertasG1aG4() {
        new TransactionTemplate(tm).executeWithoutResult(st -> {
            EscrituraConfirmada.ddlConfirmaEscrituraPendiente();   // the lock DDL of a request that is stopped by a gate: nothing was written
            assertThat(EscrituraConfirmada.confirmada()).isFalse();
        });
    }

    @Test
    void m3_unaEscrituraRemotaExitosa_hace500ElSiguiente503_yLaMarcaNoSobreviveALaTransaccion() {
        try (ServidorRouterSimulado s = new ServidorRouterSimulado().responde(ColaboradoresHttp.AGREGAR_LOG, Respuesta.ok("{\"P_OUT_ERRORESCONTADOR\":0}"))
                .responde(ColaboradoresHttp.INSCRIBIR_PROCESO_BATCH, Respuesta.estado(503))) {
            ColaboradoresHttp http = new ColaboradoresHttp(cliente(s.url(), Duration.ofSeconds(2)));
            new TransactionTemplate(tm).executeWithoutResult(st -> {
                http.agregar(new JsonObject());
                assertThat(EscrituraConfirmada.confirmada()).isTrue();
                assertThatThrownBy(() -> http.inscribir(new JsonObject())).isInstanceOf(ColaboradorErrorException.class);
            });
            assertThat(EscrituraConfirmada.confirmada()).as("outside the unit of work: no state between requests").isFalse();
            new TransactionTemplate(tm).executeWithoutResult(st -> {
                assertThat(EscrituraConfirmada.confirmada()).as("the next request starts clean").isFalse();
                assertThatThrownBy(() -> http.inscribir(new JsonObject())).isInstanceOf(ServicioNoServidoException.class);
            });
        }
    }

    @Test
    void m3_unaLecturaExitosa_noMarcaEscritura() {
        try (ServidorRouterSimulado s = new ServidorRouterSimulado().responde(ColaboradoresHttp.CALCULAR_PARIDAD, Respuesta.ok("{\"p\":1}"))) {
            new TransactionTemplate(tm).executeWithoutResult(st -> {
                new ColaboradoresHttp(cliente(s.url(), Duration.ofSeconds(2))).calcular(new JsonObject());
                assertThat(EscrituraConfirmada.confirmada()).isFalse();
            });
        }
    }

    @Test
    void m2_routerColgado_lecturaVencida_es500Generico_y_laConexionDelPoolDeUnoSeLibera() throws Exception {
        JavaCallUnitOfWorkFilter filtro = new JavaCallUnitOfWorkFilter(tm, 30);
        try (ServidorRouterSimulado s = new ServidorRouterSimulado().responde(ColaboradoresHttp.CALCULAR_PARIDAD,
                new Respuesta(200, "{\"p\":1}", 3000))) {
            ColaboradoresHttp http = new ColaboradoresHttp(cliente(s.url(), Duration.ofMillis(300)));
            ConexionUnidadDeTrabajo conexion = new ConexionUnidadDeTrabajo(ds);
            MockHttpServletRequest rq = new MockHttpServletRequest("POST", "/internal/java-call");
            rq.setRequestURI("/internal/java-call");
            MockHttpServletResponse rs = new MockHttpServletResponse();
            long t0 = System.nanoTime();
            filtro.doFilter(rq, rs, (req, res) -> {
                conexion.usar(c -> c.createStatement().executeUpdate("INSERT INTO " + T + " VALUES (1, 'A')"));
                try {
                    http.calcular(new JsonObject());
                } catch (ColaboradorErrorException e) {
                    ((jakarta.servlet.http.HttpServletResponse) res).setStatus(500);   // what JavaCallController answers
                }
            });
            assertThat(Duration.ofNanos(System.nanoTime() - t0)).isLessThan(Duration.ofSeconds(2));
            assertThat(rs.getStatus()).isEqualTo(500);
            assertThat(filas()).as("rolled back").isZero();
            assertThat(ds.getHikariPoolMXBean().getActiveConnections()).as("connection released").isZero();
        }
    }

    @Test
    void m2_plazoDeLaTransaccion_vencido_elSiguienteAccesoFalla_pero_laLiberacionDelBloqueoAunCorre() throws Exception {
        JavaCallUnitOfWorkFilter filtro = new JavaCallUnitOfWorkFilter(tm, 1);
        ConexionUnidadDeTrabajo conexion = new ConexionUnidadDeTrabajo(ds);
        MockHttpServletRequest rq = new MockHttpServletRequest("POST", "/internal/java-call");
        rq.setRequestURI("/internal/java-call");
        MockHttpServletResponse rs = new MockHttpServletResponse();
        boolean[] liberacionCorrio = new boolean[1];
        filtro.doFilter(rq, rs, (req, res) -> {
            conexion.usar(c -> c.createStatement().executeUpdate("INSERT INTO " + T + " VALUES (1, 'A')"));
            try {
                Thread.sleep(1300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            assertThatThrownBy(() -> conexion.usar(c -> 1)).isInstanceOf(org.springframework.transaction.TransactionTimedOutException.class);
            conexion.usarAunVencido(c -> {
                liberacionCorrio[0] = true;
                return 1;
            });
            ((jakarta.servlet.http.HttpServletResponse) res).setStatus(500);
        });
        assertThat(liberacionCorrio[0]).isTrue();
        assertThat(filas()).isZero();
    }

    @Test
    void m2_valoresPorDefecto() {
        assertThat(JavaCallUnitOfWorkFilter.TIMEOUT_POR_DEFECTO).isEqualTo(120);
    }
}
