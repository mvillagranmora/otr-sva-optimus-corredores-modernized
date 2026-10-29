package cl.bcs.sura;

import cl.bcs.sura.adapter.out.SuraRepositoryAdapter;
import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SuraIntegrationTest {

    @Autowired SuraRepositoryAdapter adapter;
    @Autowired NamedParameterJdbcTemplate jdbc;

    // ─── consultarIdsPortafolioIdCliente (H2) ────────────────────────────────

    @Test
    void consultarIds_documentoExistente_devuelvePerfil() {
        List<IdsEntidadYPerfiles> result = adapter.consultarIdsPortafolioIdCliente(List.of("11111111-1"));
        assertThat(result).hasSize(2);
        assertThat(result).extracting(p -> p.documentoIdentidad).containsOnly("11111111-1");
    }

    @Test
    void consultarIds_variosDocumentos_devuelveTodos() {
        List<IdsEntidadYPerfiles> result = adapter.consultarIdsPortafolioIdCliente(List.of("11111111-1", "22222222-2"));
        assertThat(result).hasSize(3);
    }

    @Test
    void consultarIds_documentoInexistente_devuelveVacia() {
        List<IdsEntidadYPerfiles> result = adapter.consultarIdsPortafolioIdCliente(List.of("99999999-9"));
        assertThat(result).isEmpty();
    }

    // ─── consultarPerfilesComerciales (H2) ───────────────────────────────────

    @Test
    void consultarPerfiles_documentoExistente_devuelvePerfil() {
        List<IdsEntidadYPerfiles> result = adapter.consultarPerfilesComerciales(List.of("11111111-1"));
        assertThat(result).hasSize(2);
        assertThat(result).extracting(p -> p.idPerfilComercial).containsExactlyInAnyOrder("PC-01", "PC-02");
    }

    @Test
    void consultarPerfiles_documentoInexistente_devuelveVacia() {
        List<IdsEntidadYPerfiles> result = adapter.consultarPerfilesComerciales(List.of("99999999-9"));
        assertThat(result).isEmpty();
    }

    // ─── modificarPerfilesPortafolio (H2) ────────────────────────────────────

    @Test
    void modificarPerfiles_actualiza_perfilComercial() {
        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idPortafolio    = 201L;
        p.idPerfilComercial = "PC-NUEVO";
        p.idPerfilCobros    = "PCO-NUEVO";
        p.idPerfilProductos = "PP-NUEVO";

        adapter.modificarPerfilesPortafolio(List.of(p));

        String updated = jdbc.queryForObject(
            "SELECT ID_PERFIL_COMERCIAL FROM PERFILES_COMERCIALES_T WHERE ID_PORTAFOLIO = :id",
            Map.of("id", 201L), String.class);
        assertThat(updated).isEqualTo("PC-NUEVO");
    }

    @Test
    void modificarPerfiles_listaVacia_noFalla() {
        assertThatCode(() -> adapter.modificarPerfilesPortafolio(List.of()))
            .doesNotThrowAnyException();
    }

    // ─── ingresarIndicadoresCobroCustodia (H2) ───────────────────────────────

    @Test
    void ingresarIndicadores_idIndicadorCero_inserta() {
        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idPortafolio = 101L;
        p.idIndicador  = 0L;

        long before = contarIndicadores(101L);
        adapter.ingresarIndicadoresCobroCustodia(List.of(p), "MONTO");
        long after  = contarIndicadores(101L);

        assertThat(after).isEqualTo(before + 1);
    }

    @Test
    void ingresarIndicadores_idIndicadorNoCero_noInserta() {
        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idPortafolio = 101L;
        p.idIndicador  = 99L;

        long before = contarIndicadores(101L);
        adapter.ingresarIndicadoresCobroCustodia(List.of(p), "MONTO");
        long after  = contarIndicadores(101L);

        assertThat(after).isEqualTo(before);
    }

    // ─── modificarIndicadoresCobroCustodia (H2) ──────────────────────────────

    @Test
    void modificarIndicadores_actualizarS_actualizaValor() {
        // The seed row inserted by data-sura.sql has ID_INDICADOR=1 (GENERATED ALWAYS AS IDENTITY)
        long seedId = jdbc.queryForObject(
            "SELECT MIN(ID_INDICADOR) FROM INDICADORES_PORTAFOLIO_T", Map.of(), Long.class);

        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idIndicador      = seedId;
        p.actualizarFormaCobro = "S";
        p.idFormaCobro         = "FC-NUEVO";

        adapter.modificarIndicadoresCobroCustodia(List.of(p));

        String valor = jdbc.queryForObject(
            "SELECT VALOR_INDICADOR FROM INDICADORES_PORTAFOLIO_T WHERE ID_INDICADOR = :id",
            Map.of("id", seedId), String.class);
        assertThat(valor).isEqualTo("FC-NUEVO");
    }

    @Test
    void modificarIndicadores_actualizarN_noModifica() {
        long seedId = jdbc.queryForObject(
            "SELECT MIN(ID_INDICADOR) FROM INDICADORES_PORTAFOLIO_T", Map.of(), Long.class);

        String originalValor = jdbc.queryForObject(
            "SELECT VALOR_INDICADOR FROM INDICADORES_PORTAFOLIO_T WHERE ID_INDICADOR = :id",
            Map.of("id", seedId), String.class);

        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idIndicador          = seedId;
        p.actualizarFormaCobro = "N";
        p.idFormaCobro         = "DEBERIA-IGNORARSE";

        adapter.modificarIndicadoresCobroCustodia(List.of(p));

        String valor = jdbc.queryForObject(
            "SELECT VALOR_INDICADOR FROM INDICADORES_PORTAFOLIO_T WHERE ID_INDICADOR = :id",
            Map.of("id", seedId), String.class);
        assertThat(valor).isEqualTo(originalValor);
    }

    // ─── SOAP sandbox (@Disabled) ────────────────────────────────────────────

    @Disabled("requires SOAP sandbox: CrearClienteFullWS — servicio externo SURA no disponible en CI")
    @Test
    void soap_crearClienteFullWS() { }

    @Disabled("requires SOAP sandbox: ModificarClienteWS — servicio externo SURA no disponible en CI")
    @Test
    void soap_modificarClienteWS() { }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private long contarIndicadores(long idEntidad) {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM INDICADORES_PORTAFOLIO_T WHERE ID_ENTIDAD = :id",
            Map.of("id", idEntidad), Long.class);
    }
}
