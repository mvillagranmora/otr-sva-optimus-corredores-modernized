package cl.bcs.normativa.jobs;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ParametrosJob pure logic.
 *
 * Oracle: legacy BcsJob.getParam(name) — returns the string value from
 *         the parameters map, which is populated by BAT.ParametrosPrograma.
 *
 * B-NJB-PAR-01: requerido() returns the value when present
 * B-NJB-PAR-02: requerido() throws IAE when absent or blank
 * B-NJB-PAR-03: requeridoLong() parses correctly
 * B-NJB-PAR-04: requeridoLong() throws NFE on non-numeric
 * B-NJB-PAR-05: opcional() returns default when absent
 */
@DisplayName("ParametrosJob — parámetros de job")
class ParametrosJobTest {

    private static ParametrosJob build(Map<String, String> params) {
        return new ParametrosJob(42L, params);
    }

    @Nested
    @DisplayName("requerido()")
    class Requerido {

        @Test
        @DisplayName("NJB-PAR-01: returns value when present")
        void requerido_returnsValue_whenPresent() {
            var params = build(Map.of("IdEmpresa", "1", "IdUsuario", "6"));
            assertThat(params.requerido("IdEmpresa")).isEqualTo("1");
            assertThat(params.requerido("IdUsuario")).isEqualTo("6");
        }

        @Test
        @DisplayName("NJB-PAR-02a: throws IAE when key is absent")
        void requerido_throwsIAE_whenAbsent() {
            var params = build(Map.of("IdEmpresa", "1"));
            assertThatThrownBy(() -> params.requerido("IdModulo"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("IdModulo");
        }

        @Test
        @DisplayName("NJB-PAR-02b: throws IAE when value is blank")
        void requerido_throwsIAE_whenBlank() {
            Map<String, String> map = new HashMap<>();
            map.put("IdModulo", "   ");
            var params = build(map);
            assertThatThrownBy(() -> params.requerido("IdModulo"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("IdModulo");
        }
    }

    @Nested
    @DisplayName("requeridoLong()")
    class RequeridoLong {

        @Test
        @DisplayName("NJB-PAR-03: parses valid long")
        void requeridoLong_parsesLong() {
            var params = build(Map.of("IdEmpresa", "1234567890"));
            assertThat(params.requeridoLong("IdEmpresa")).isEqualTo(1234567890L);
        }

        @Test
        @DisplayName("NJB-PAR-04: throws NFE on non-numeric")
        void requeridoLong_throwsNFE_whenNotNumeric() {
            var params = build(Map.of("IdEmpresa", "abc"));
            assertThatThrownBy(() -> params.requeridoLong("IdEmpresa"))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("NJB-PAR-03b: parses zero correctly")
        void requeridoLong_parsesZero() {
            var params = build(Map.of("IdSolicitud", "0"));
            assertThat(params.requeridoLong("IdSolicitud")).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("opcional()")
    class Opcional {

        @Test
        @DisplayName("NJB-PAR-05a: returns value when present")
        void opcional_returnsValue_whenPresent() {
            var params = build(Map.of("Modulo", "NOR"));
            assertThat(params.opcional("Modulo", "TRI")).isEqualTo("NOR");
        }

        @Test
        @DisplayName("NJB-PAR-05b: returns default when absent")
        void opcional_returnsDefault_whenAbsent() {
            var params = build(Map.of());
            assertThat(params.opcional("Modulo", "TRI")).isEqualTo("TRI");
        }

        @Test
        @DisplayName("NJB-PAR-05c: returns default when blank")
        void opcional_returnsDefault_whenBlank() {
            Map<String, String> map = new HashMap<>();
            map.put("Modulo", "");
            var params = build(map);
            assertThat(params.opcional("Modulo", "TRI")).isEqualTo("TRI");
        }
    }

    @Nested
    @DisplayName("jobId")
    class JobId {

        @Test
        @DisplayName("NJB-PAR-06: jobId is preserved")
        void jobId_isPreserved() {
            var params = new ParametrosJob(999L, Map.of());
            assertThat(params.jobId()).isEqualTo(999L);
        }
    }
}
