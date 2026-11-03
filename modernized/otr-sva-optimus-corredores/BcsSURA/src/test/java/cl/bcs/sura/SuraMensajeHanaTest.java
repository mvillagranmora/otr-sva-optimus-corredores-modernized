package cl.bcs.sura;

import cl.bcs.sura.adapter.out.LegacyHanaSuraPort;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * B-SURA-10 against HANA QA, SELECT only (the dictionary query). Tagged "hana": excluded from the default run, and it FAILS
 * (never skips) when HANA_URL / HANA_USER / HANA_PASSWORD are missing. Run: mvn -o test -DexcludedGroups= -Dtest=SuraMensajeHanaTest, with the three variables set in the environment (direct connection)
 */
@Tag("hana")
class SuraMensajeHanaTest {

    @Test
    void bSura10_diccionarioRealDeQa() {
        String url = System.getenv("HANA_URL");
        assertThat(url).as("HANA_URL (set HANA_URL, HANA_USER and HANA_PASSWORD in the environment)").isNotBlank();
        DriverManagerDataSource ds = new DriverManagerDataSource(url, System.getenv("HANA_USER"), System.getenv("HANA_PASSWORD"));
        ds.setDriverClassName("com.sap.db.jdbc.Driver");
        LegacyHanaSuraPort port = new LegacyHanaSuraPort(new NamedParameterJdbcTemplate(ds));

        Map<String, String> m = port.resolverMensajeError(1L, 1L,
                "UI_ERROR_CONFIGURACION_PERFILES_PORTAFOLIO_NO_ENCONTRADOS", Map.of());
        assertThat(m.get("descripcion")).isEqualTo("No se encontró configuración para la Categoría de Inversionista y Forma Cobro.");
        assertThat(port.resolverMensajeError(1L, 1L, "NO_EXISTE_SURA_TEST", Map.of()).get("descripcion"))
                .isEqualTo("NO_EXISTE_SURA_TEST sin traduccion");
    }
}
