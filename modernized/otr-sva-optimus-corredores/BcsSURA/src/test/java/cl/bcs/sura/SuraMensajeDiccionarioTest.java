package cl.bcs.sura;

import cl.bcs.sura.adapter.out.LegacyHanaSuraPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * B-SURA-10: error text from the platform dictionary, as legacy Diccionario.getMensaje (PlataformaUtils.MensajeSalida).
 * Runs the real query text (legacy ConsultarDiccionario.sql) on H2 with flat tables named like the HANA ones.
 */
class SuraMensajeDiccionarioTest {

    static final String COD = "UI_ERROR_CONFIGURACION_PERFILES_PORTAFOLIO_NO_ENCONTRADOS";
    JdbcTemplate plain;
    LegacyHanaSuraPort port;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource ds = new DriverManagerDataSource("jdbc:h2:mem:dicc" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        plain = new JdbcTemplate(ds);
        plain.execute("CREATE TABLE \"BSCL.Plataforma.BD::PLA.Diccionario\" (\"CodLenguaje\" VARCHAR(10), \"CodDiccionario\" VARCHAR(200), \"Descripcion\" VARCHAR(500))");
        plain.execute("CREATE TABLE \"BSCL.Plataforma.BD::PLA.Configuracion\" (\"IdEmpresa\" BIGINT, \"CodigoParametro\" VARCHAR(100), \"Valor\" VARCHAR(50))");
        plain.update("INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.LocalizacionSistema', 'CL')");
        plain.update("INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (2, 'PLA.LocalizacionSistema', 'PE')");
        plain.update("INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('CL', ?, 'Sin configuracion para ::Categoria:: y ::Forma::.')", COD);
        plain.update("INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('PE', ?, 'Peru ::Categoria::')", COD);
        port = new LegacyHanaSuraPort(new NamedParameterJdbcTemplate(ds));
    }

    private Map<String, String> msg(long empresa, String codigo, Map<String, String> params) {
        return port.resolverMensajeError(7L, empresa, codigo, params);
    }

    @Test
    void bSura10_textoDelDiccionarioConLosMarcadoresReemplazados() {
        Map<String, String> m = msg(1, COD, Map.of("Categoria", "A", "Forma", "F1"));
        assertThat(m).containsEntry("codigo", COD).containsEntry("tipo", "ERROR")
                .containsEntry("descripcion", "Sin configuracion para A y F1.");
    }

    @Test
    void bSura10_valorNuloQuedaVacio() {
        Map<String, String> params = new HashMap<>();
        params.put("Categoria", null);
        params.put("Forma", "F1");
        assertThat(msg(1, COD, params).get("descripcion")).isEqualTo("Sin configuracion para  y F1.");
    }

    @Test
    void bSura10_codigoSinFilaDaSinTraduccion() {
        assertThat(msg(1, "NO_EXISTE", Map.of()).get("descripcion")).isEqualTo("NO_EXISTE sin traduccion");
    }

    @Test
    void bSura10_sinMapaDeParametrosDaSinTraduccionAunqueExista() {
        assertThat(msg(1, COD, null).get("descripcion")).isEqualTo(COD + " sin traduccion");
    }

    @Test
    void bSura10_cadaEmpresaUsaSuIdioma() {
        assertThat(msg(2, COD, Map.of("Categoria", "B")).get("descripcion")).isEqualTo("Peru B");
    }

    @Test
    void bSura10_eldiccionarioSeLeeUnaVezPorEmpresa() {
        msg(1, COD, Map.of());
        plain.update("UPDATE \"BSCL.Plataforma.BD::PLA.Diccionario\" SET \"Descripcion\" = 'cambiado' WHERE \"CodLenguaje\" = 'CL'");
        assertThat(msg(1, COD, Map.of("Categoria", "A", "Forma", "F1")).get("descripcion")).isEqualTo("Sin configuracion para A y F1.");
    }
}
