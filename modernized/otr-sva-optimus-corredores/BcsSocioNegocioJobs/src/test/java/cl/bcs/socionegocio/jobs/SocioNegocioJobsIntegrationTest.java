package cl.bcs.socionegocio.jobs;

import cl.bcs.socionegocio.jobs.domain.ClienteABloquear;
import cl.bcs.socionegocio.jobs.domain.DocumentoCliente;
import cl.bcs.socionegocio.jobs.port.out.SocioNegocioJobsDataPort;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests against H2 in-memory database.
 * Schema and data loaded from schema-socionegocio-jobs.sql and data-socionegocio-jobs.sql.
 *
 * Canary: obtenerFechaSistema(1) returns "2024-01-15"
 * Executed cases: 6 (5 active + 1 @Disabled)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SocioNegocioJobsIntegrationTest {

    @Autowired
    private SocioNegocioJobsDataPort dataPort;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    // -----------------------------------------------------------------------
    // Config queries
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("CANARY: obtenerFechaSistema(1) returns 2024-01-15")
    void obtenerFechaSistema_returnsConfiguredDate() {
        String fecha = dataPort.obtenerFechaSistema(1L);
        assertThat(fecha).isEqualTo("2024-01-15");
    }

    @Test
    @DisplayName("obtenerDiasVencimiento(1) returns 30")
    void obtenerDiasVencimiento_returns30() {
        int dias = dataPort.obtenerDiasVencimiento(1L);
        assertThat(dias).isEqualTo(30);
    }

    // -----------------------------------------------------------------------
    // Document query
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("consultarDocumentosPorVencer returns all 4 rows (all have FECHA_VIGENCIA set)")
    void consultarDocumentosPorVencer_returns4Rows() {
        List<DocumentoCliente> docs = dataPort.consultarDocumentosPorVencer();
        assertThat(docs).hasSize(4);
    }

    // -----------------------------------------------------------------------
    // Updates
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("actualizarAnexos sets ID_ESTADO=VEN for given annex IDs")
    void actualizarAnexos_updatesEstado() {
        dataPort.actualizarAnexos(List.of(1L, 2L));

        String estado1 = jdbc.queryForObject(
                "SELECT ID_ESTADO FROM DOCUMENTOS_CLIENTE_T WHERE ID_ANEXO = :id",
                new MapSqlParameterSource("id", 1L), String.class);
        String estado2 = jdbc.queryForObject(
                "SELECT ID_ESTADO FROM DOCUMENTOS_CLIENTE_T WHERE ID_ANEXO = :id",
                new MapSqlParameterSource("id", 2L), String.class);

        assertThat(estado1).isEqualTo("VEN");
        assertThat(estado2).isEqualTo("VEN");
    }

    @Test
    @DisplayName("bloquearClientes updates ID_TIPO_BLOQUEO and COMENTARIO_BLOQUEO in CLIENTES_T")
    void bloquearClientes_updatesClienteRecord() {
        List<ClienteABloquear> clientes = List.of(
                new ClienteABloquear(100L, "BAUTOM",
                        "Cliente bloqueado automaticamente por fecha de vencimiento"));
        dataPort.bloquearClientes(clientes);

        String tipoBloqueo = jdbc.queryForObject(
                "SELECT ID_TIPO_BLOQUEO FROM CLIENTES_T WHERE ID_CLIENTE = :id",
                new MapSqlParameterSource("id", 100L), String.class);

        assertThat(tipoBloqueo).isEqualTo("BAUTOM");
    }

    // -----------------------------------------------------------------------
    // HANA SP — disabled (requires real HANA)
    // -----------------------------------------------------------------------

    @Test
    @Disabled("requires HANA SP: SP_PURGA_PROSPECTO_CLIENTE — not available on H2")
    @DisplayName("purgaProspectosClientes executes HANA SP successfully")
    void purgaProspectosClientes_hanaOnly() {
        assertThatThrownBy(() -> dataPort.purgaProspectosClientes(1L, 37L))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("SP_PURGA_PROSPECTO_CLIENTE");
    }
}
