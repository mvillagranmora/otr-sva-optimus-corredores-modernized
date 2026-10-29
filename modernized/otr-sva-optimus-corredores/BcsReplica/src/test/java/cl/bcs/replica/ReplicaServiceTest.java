package cl.bcs.replica;

import cl.bcs.replica.service.ReplicaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization tests for pure-logic in ReplicaService.
 *
 * Oracle:
 *   legacy cl.bcs.replica.IniciarReplicaFull  (IniciarReplicaFull.java)
 *   legacy cl.bcs.replica.IniciarReplicaDelta (IniciarReplicaDelta.java)
 *   legacy cl.bcs.replica.ReplicaUtils        (ReplicaUtils.java)
 *
 * Rule IDs pinned:
 *   B-REP-01  buildParametersString formats "BCS_query:<queryName>;Fecha:<fecha>"
 *   B-REP-02  Full uses "QueryReplica"; Delta uses "QueryReplicaDelta"
 *   B-REP-03  esRespaldoActivo gates backup via case-insensitive "S"
 *   B-REP-04  extractCodPer returns substring(0,7) — year-month from fechaSistema
 *
 * No @SpringBootTest needed — all methods are static.
 *
 * equivalence cases executed: 9 (all enabled tests)
 */
@DisplayName("BcsReplica: ReplicaService pure-logic tests")
class ReplicaServiceTest {

    @Nested
    @DisplayName("buildParametersString — B-REP-01 / B-REP-02")
    class BuildParametersString {

        @Test
        @DisplayName("REP-SVC-01 [B-REP-01]: Full query produces correct parameters string")
        void rule_REP_SVC_01_full_query_parameters_string() {
            String result = ReplicaService.buildParametersString("QueryReplica", "2024-01-15");
            assertThat(result).isEqualTo("BCS_query:QueryReplica;Fecha:2024-01-15"); // REP-SVC-01
        }

        @Test
        @DisplayName("REP-SVC-02 [B-REP-02]: Delta query produces correct parameters string")
        void rule_REP_SVC_02_delta_query_parameters_string() {
            String result = ReplicaService.buildParametersString("QueryReplicaDelta", "2024-01-15");
            assertThat(result).isEqualTo("BCS_query:QueryReplicaDelta;Fecha:2024-01-15"); // REP-SVC-02
        }

        @Test
        @DisplayName("REP-SVC-03 [B-REP-02]: Full vs Delta differ only in queryName — not in any other field")
        void rule_REP_SVC_03_full_vs_delta_differ_only_in_query_name() {
            String fullParams  = ReplicaService.buildParametersString("QueryReplica",      "2024-01-15");
            String deltaParams = ReplicaService.buildParametersString("QueryReplicaDelta", "2024-01-15");
            // Both share same fecha
            assertThat(fullParams).contains("Fecha:2024-01-15");  // REP-SVC-03
            assertThat(deltaParams).contains("Fecha:2024-01-15"); // REP-SVC-03
            // They differ only in query name
            assertThat(fullParams).contains("QueryReplica");      // REP-SVC-03
            assertThat(deltaParams).contains("QueryReplicaDelta"); // REP-SVC-03
            assertThat(fullParams).isNotEqualTo(deltaParams);     // REP-SVC-03
        }
    }

    @Nested
    @DisplayName("extractCodPer — B-REP-04")
    class ExtractCodPer {

        @Test
        @DisplayName("REP-SVC-04 [B-REP-04]: January date extracts year-month correctly")
        void rule_REP_SVC_04_january_extracts_year_month() {
            assertThat(ReplicaService.extractCodPer("2024-01-15")).isEqualTo("2024-01"); // REP-SVC-04
        }

        @Test
        @DisplayName("REP-SVC-05 [B-REP-04]: December date extracts year-month correctly")
        void rule_REP_SVC_05_december_extracts_year_month() {
            assertThat(ReplicaService.extractCodPer("2024-12-31")).isEqualTo("2024-12"); // REP-SVC-05
        }
    }

    @Nested
    @DisplayName("esRespaldoActivo — B-REP-03")
    class EsRespaldoActivo {

        @Test
        @DisplayName("REP-SVC-06 [B-REP-03]: 'S' uppercase returns true")
        void rule_REP_SVC_06_uppercase_S_returns_true() {
            assertThat(ReplicaService.esRespaldoActivo("S")).isTrue(); // REP-SVC-06
        }

        @Test
        @DisplayName("REP-SVC-07 [B-REP-03]: 's' lowercase returns true (case-insensitive)")
        void rule_REP_SVC_07_lowercase_s_returns_true() {
            assertThat(ReplicaService.esRespaldoActivo("s")).isTrue(); // REP-SVC-07
        }

        @Test
        @DisplayName("REP-SVC-08 [B-REP-03]: 'N' returns false")
        void rule_REP_SVC_08_N_returns_false() {
            assertThat(ReplicaService.esRespaldoActivo("N")).isFalse(); // REP-SVC-08
        }

        @Test
        @DisplayName("REP-SVC-09 [B-REP-03]: empty string returns false")
        void rule_REP_SVC_09_empty_string_returns_false() {
            assertThat(ReplicaService.esRespaldoActivo("")).isFalse(); // REP-SVC-09
        }
    }
}
