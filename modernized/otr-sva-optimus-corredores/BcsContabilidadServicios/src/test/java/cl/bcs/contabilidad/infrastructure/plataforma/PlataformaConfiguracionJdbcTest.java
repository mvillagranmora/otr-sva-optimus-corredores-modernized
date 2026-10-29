package cl.bcs.contabilidad.infrastructure.plataforma;

import cl.bcs.contabilidad.service.FuncionalidadNoMigradaException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Platform configuration read from the database, like legacy {@code cl.bcs.plataforma.pla.utils.ConfiguracionSistema}
 * (verbatim legacy SQL, filtered by company in memory), and the TomEE HTTP fallback contract. Architecture review
 * finding H4: only "parameter absent" yields a default, and a transport failure is a 503 fallback, never a default.
 */
class PlataformaConfiguracionJdbcTest {

    NamedParameterJdbcTemplate jdbc;
    PlataformaConfiguracionJdbc configuracion;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource ds = new DriverManagerDataSource(
                "jdbc:h2:mem:pla-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new NamedParameterJdbcTemplate(ds);
        jdbc.getJdbcTemplate().execute("CREATE TABLE \"BSCL.Plataforma.BD::PLA.Configuracion\" "
                + "(\"IdEmpresa\" BIGINT, \"CodigoParametro\" VARCHAR(100), \"Valor\" VARCHAR(100))");
        jdbc.getJdbcTemplate().execute("CREATE TABLE \"BSCL.Mesa.BD::MES.ConfiguracionMesa\" "
                + "(\"IdEmpresa\" BIGINT, \"CodigoParametro\" VARCHAR(100), \"Valor\" VARCHAR(100))");
        jdbc.getJdbcTemplate().update("INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES "
                + "(1, 'PLA.FechaSistema', '2025-03-10'), (2, 'PLA.FechaSistema', '2030-01-01'), "
                + "(1, 'PLA.MonedaSistema', 'USD'), (1, 'PLA.MonedaSistema', 'CLP'), (1, 'PLA.Nulo', NULL)");
        jdbc.getJdbcTemplate().update("INSERT INTO \"BSCL.Mesa.BD::MES.ConfiguracionMesa\" VALUES "
                + "(1, 'TASARECOMPRAVENTACONPACTO', 'S')");
        configuracion = new PlataformaConfiguracionJdbc(jdbc);
    }

    @Test
    @DisplayName("reads the parameter of the given company from the module's legacy SQL")
    void leeParametroDeLaEmpresa() {
        assertThat(configuracion.parametro(1L, "PLATAFORMA", "PLA.FechaSistema")).contains("2025-03-10");
        assertThat(configuracion.parametro(2L, "PLATAFORMA", "PLA.FechaSistema")).contains("2030-01-01");
        assertThat(configuracion.parametro(1L, "MESA", "TASARECOMPRAVENTACONPACTO")).contains("S");
    }

    @Test
    @DisplayName("duplicate rows: the last one wins, as legacy HashMap.put")
    void ultimaFilaGana() {
        assertThat(configuracion.parametro(1L, "PLATAFORMA", "PLA.MonedaSistema")).contains("CLP");
    }

    @Test
    @DisplayName("absent parameter -> empty (caller's default); required read fails like legacy getParametro")
    void ausente() {
        assertThat(configuracion.parametro(1L, "PLATAFORMA", "PLA.NoExiste")).isEmpty();
        assertThat(configuracion.parametro(3L, "PLATAFORMA", "PLA.FechaSistema")).isEmpty();
        assertThatThrownBy(() -> configuracion.parametroRequerido(1L, "PLATAFORMA", "PLA.NoExiste"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Parametro PLA.NoExiste en modulo PLATAFORMA no encontrado");
    }

    @Test
    @DisplayName("unknown module fails (legacy: 'Configuracion modulo X no encontrada')")
    void moduloDesconocido() {
        assertThatThrownBy(() -> configuracion.parametro(1L, "NOEXISTE", "X"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Configuracion modulo NOEXISTE no encontrada");
    }

    @Test
    @DisplayName("database error propagates instead of becoming the default")
    void errorDeBaseNoEsDefault() {
        jdbc.getJdbcTemplate().execute("DROP TABLE \"BSCL.Mesa.BD::MES.ConfiguracionMesa\"");
        TomEePlataformaClient cliente = new TomEePlataformaClient("http://127.0.0.1:9", jdbc);
        assertThatThrownBy(() -> cliente.obtenerParametroConfiguracion(1L, "MESA", "TASARECOMPRAVENTACONPACTO", "N"))
                .isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThat(cliente.obtenerParametroConfiguracion(1L, "PLATAFORMA", "PLA.NoExiste", "N")).isEqualTo("N");
        assertThat(cliente.obtenerFechaSistema(1L)).isEqualTo("2025-03-10");
    }

    @Test
    @DisplayName("TomEE endpoint 404 or unreachable -> FuncionalidadNoMigradaException (503 fallback), never a default")
    void httpFallaEs503() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", ex -> {
            byte[] b = "not found".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(404, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        server.start();
        try {
            TomEePlataformaClient cliente = new TomEePlataformaClient(
                    "http://127.0.0.1:" + server.getAddress().getPort(), jdbc);
            assertThatThrownBy(() -> cliente.obtenerDiaAnterior(56L, "2025-03-10"))
                    .isInstanceOf(FuncionalidadNoMigradaException.class);
        } finally {
            server.stop(0);
        }
        TomEePlataformaClient caido = new TomEePlataformaClient("http://127.0.0.1:9", jdbc);
        assertThatThrownBy(() -> caido.obtenerDiaAnterior(56L, "2025-03-10"))
                .isInstanceOf(FuncionalidadNoMigradaException.class);
    }
}
