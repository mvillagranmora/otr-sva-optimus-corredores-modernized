package cl.bcs.replica;

import cl.bcs.replica.adapter.out.ReplicaRepositoryAdapter;
import cl.bcs.replica.domain.Workstack;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for ReplicaRepositoryAdapter against H2.
 *
 * Profile "test" loads application-test.yml with H2 in-memory datasource.
 * Schema: schema-replica.sql  Data: data-replica.sql
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("BcsReplica: ReplicaRepositoryAdapter integration tests (H2)")
class ReplicaIntegrationTest {

    @Autowired
    ReplicaRepositoryAdapter adapter;

    @Autowired
    NamedParameterJdbcTemplate jdbc;

    @Test
    @DisplayName("REP-INT-01: insertarWorkstack writes a row to WORKSTACK_T")
    void insertarWorkstack_writesRow() {
        int before = countWorkstack();
        Workstack ws = new Workstack(
                null,
                "TEST_KEY",
                "INICIAR_REPLICA_PAUTA",
                "F",
                0,
                Instant.now(),
                "BCS_query:QueryReplica;Fecha:2024-01-15"
        );
        adapter.insertarWorkstack(ws);
        assertThat(countWorkstack()).isEqualTo(before + 1); // REP-INT-01
    }

    @Test
    @DisplayName("REP-INT-02: obtenerFechaSistema returns '2024-01-15' for empresa=1")
    void obtenerFechaSistema_empresa1_returnsCorrectDate() {
        String fecha = adapter.obtenerFechaSistema(1L);
        assertThat(fecha).isEqualTo("2024-01-15"); // REP-INT-02
    }

    @Test
    @DisplayName("REP-INT-03: obtenerParametroRespaldoSebra returns 'S' for empresa=1")
    void obtenerParametroRespaldoSebra_empresa1_returnsS() {
        String valor = adapter.obtenerParametroRespaldoSebra(1L);
        assertThat(valor).isEqualTo("S"); // REP-INT-03
    }

    @Test
    @DisplayName("REP-INT-04: obtenerParametroRespaldoSebra returns 'N' for empresa=2")
    void obtenerParametroRespaldoSebra_empresa2_returnsN() {
        String valor = adapter.obtenerParametroRespaldoSebra(2L);
        assertThat(valor).isEqualTo("N"); // REP-INT-04
    }

    @Disabled("requires HANA: BSCL.Replica tables DELETE+INSERT across 13 BSCL tables")
    @Test
    @DisplayName("REP-INT-05: ejecutarRespaldoSebra — HANA-only, disabled in H2 tests")
    void ejecutarRespaldoSebra_hanaOnly() {
        // This path requires a live HANA connection.
        // The adapter intentionally throws UnsupportedOperationException.
        assertThatThrownBy(() -> adapter.ejecutarRespaldoSebra("2024-01"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("HANA");
    }

    // -------------------------------------------------------------------------

    private int countWorkstack() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM WORKSTACK_T",
                Map.of(),
                Integer.class);
        return count != null ? count : 0;
    }
}
