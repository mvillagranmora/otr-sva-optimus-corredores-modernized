package cl.bcs.tesoreria.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** The JDBC production adapters of the shared ports, on an in-memory H2 that models only the tables they read. */
class AdaptadoresJdbcTest {

    private NamedParameterJdbcTemplate jdbc;

    @BeforeEach
    void baseDeDatos() {
        jdbc = new NamedParameterJdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:adaptadores_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", ""));
        JdbcTemplate j = jdbc.getJdbcTemplate();
        j.execute("CREATE TABLE \"BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria\" (\"IdEmpresa\" BIGINT, \"CodigoParametro\" VARCHAR(100), \"Valor\" VARCHAR(100))");
        j.execute("CREATE TABLE \"BSCL.Plataforma.BD::PLA.Configuracion\" (\"IdEmpresa\" BIGINT, \"CodigoParametro\" VARCHAR(100), \"Valor\" VARCHAR(100))");
        j.execute("CREATE TABLE \"BSCL.Plataforma.BD::PLA.Diccionario\" (\"CodLenguaje\" VARCHAR(10), \"CodDiccionario\" VARCHAR(100), \"Descripcion\" VARCHAR(200))");
        j.update("INSERT INTO \"BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria\" VALUES (1, 'LIQUIDACIONPARCIALENVIAACTA', 'N'), (2, 'LIQUIDACIONPARCIALENVIAACTA', 'S')");
        j.update("INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.LocalizacionSistema', 'es-CL'), (1, 'PLA.FechaSistema', '2025-04-01')");
        j.update("INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('es-CL', 'ERROR_UNO', 'Falla en ::Campo:: de ::Id::'), ('en-US', 'ERROR_UNO', 'Failure')");
    }

    @Test
    void laConfiguracionSeLeePorEmpresaModuloYParametro() {
        ConfiguracionSistemaJdbc c = new ConfiguracionSistemaJdbc(jdbc);
        assertThat(c.parametro(1L, "TESORERIA", "LIQUIDACIONPARCIALENVIAACTA")).isEqualTo("N");
        assertThat(c.parametro(2L, "TESORERIA", "LIQUIDACIONPARCIALENVIAACTA")).isEqualTo("S");
        assertThat(c.parametro(1L, "PLATAFORMA", "PLA.FechaSistema")).isEqualTo("2025-04-01");
    }

    @Test
    void unParametroInexistenteDaNullYElServicioDecide() {
        ConfiguracionSistemaJdbc c = new ConfiguracionSistemaJdbc(jdbc);
        assertThat(c.parametro(1L, "TESORERIA", "NO_EXISTE")).isNull();
        assertThat(c.parametro(9L, "TESORERIA", "LIQUIDACIONPARCIALENVIAACTA")).isNull();
    }

    @Test
    void unModuloDesconocidoEsUnErrorDeProgramacion() {
        assertThatThrownBy(() -> new ConfiguracionSistemaJdbc(jdbc).parametro(1L, "INVENTADO", "X"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Configuracion modulo INVENTADO no encontrada");
    }

    @Test
    void elDiccionarioTraduceEnElIdiomaDeLaEmpresaYReemplazaLosMarcadores() {
        DiccionarioJdbc d = new DiccionarioJdbc(jdbc);
        Map<String, String> valores = new HashMap<>();
        valores.put("Campo", "Monto");
        valores.put("Id", null);
        assertThat(d.mensaje(1L, "ERROR_UNO", valores)).isEqualTo("Falla en Monto de ");
    }

    @Test
    void unCodigoSinTraduccionOUnMapaNuloDaElTextoDelLegado() {
        DiccionarioJdbc d = new DiccionarioJdbc(jdbc);
        assertThat(d.mensaje(1L, "NO_EXISTE", Map.of())).isEqualTo("NO_EXISTE sin traduccion");
        assertThat(d.mensaje(1L, "ERROR_UNO", null)).isEqualTo("ERROR_UNO sin traduccion");
    }

    @Test
    void elRelojDelSistemaEntregaElInstanteUtcActual() {
        Instant antes = Instant.now();
        Instant ahora = new RelojSistema().ahora();
        assertThat(ahora).isBetween(antes, Instant.now().plusSeconds(1));
    }
}
