package cl.bcs.fondomutuo.jobs;

import cl.bcs.fondomutuo.jobs.service.GenerarArchivosFFMMJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization tests for GenerarArchivosFFMMJob.validarTipoArchivo().
 *
 * Oracle:
 *   legacy cl.bcs.fondomutuo.jobs.BcsJobGenerarArchivosFFMM (lines 102-142)
 *
 * Rule IDs pinned:
 *   B-FMJ-11  "INFORME_MOV_FFMM" → GenerarArchivoMovimientos.
 *   B-FMJ-12  "INFORME_PARTICIPE_FFMM" → GenerarArchivoParticipes.
 *   B-FMJ-13  Any other value → throws ("Tipo de archivo no válido").
 */
@DisplayName("GenerarArchivosFFMM — tipo archivo validation")
class GenerarArchivosFFMMJobTest {

    @Nested
    @DisplayName("TipoArchivoRouting — B-FMJ-11 / B-FMJ-12 / B-FMJ-13")
    class TipoArchivoRouting {

        @Test
        @DisplayName("FMJ-GAF-01 [B-FMJ-11]: INFORME_MOV_FFMM accepted and returned unchanged")
        void rule_FMJ_GAF_01_movimientosAccepted() {
            // B-FMJ-11: valid movimientos type
            String result = GenerarArchivosFFMMJob.validarTipoArchivo("INFORME_MOV_FFMM");

            assertThat(result).isEqualTo("INFORME_MOV_FFMM"); // FMJ-GAF-01
        }

        @Test
        @DisplayName("FMJ-GAF-02 [B-FMJ-12]: INFORME_PARTICIPE_FFMM accepted and returned unchanged")
        void rule_FMJ_GAF_02_participesAccepted() {
            // B-FMJ-12: valid participes type
            String result = GenerarArchivosFFMMJob.validarTipoArchivo("INFORME_PARTICIPE_FFMM");

            assertThat(result).isEqualTo("INFORME_PARTICIPE_FFMM"); // FMJ-GAF-02
        }

        @Test
        @DisplayName("FMJ-GAF-03 [B-FMJ-13]: unknown type → IllegalArgumentException with message")
        void rule_FMJ_GAF_03_unknownTypeThrowsIllegalArgumentException() {
            // B-FMJ-13: legacy throws new Exception("Tipo de archivo no válido: " + ...)
            assertThatThrownBy(() -> GenerarArchivosFFMMJob.validarTipoArchivo("TIPO_DESCONOCIDO"))
                    .isInstanceOf(IllegalArgumentException.class)    // FMJ-GAF-03
                    .hasMessageContaining("Tipo de archivo no válido"); // FMJ-GAF-03
        }

        @Test
        @DisplayName("FMJ-GAF-04 [B-FMJ-13]: null type → IllegalArgumentException")
        void rule_FMJ_GAF_04_nullTypeThrowsIllegalArgumentException() {
            // B-FMJ-13: null is not a recognised type
            assertThatThrownBy(() -> GenerarArchivosFFMMJob.validarTipoArchivo(null))
                    .isInstanceOf(IllegalArgumentException.class); // FMJ-GAF-04
        }

        @Test
        @DisplayName("FMJ-GAF-05 [B-FMJ-13]: blank type → IllegalArgumentException")
        void rule_FMJ_GAF_05_blankTypeThrowsIllegalArgumentException() {
            assertThatThrownBy(() -> GenerarArchivosFFMMJob.validarTipoArchivo(""))
                    .isInstanceOf(IllegalArgumentException.class); // FMJ-GAF-05
        }

        @Test
        @DisplayName("FMJ-GAF-06 [B-FMJ-11/12]: constants match legacy hardcoded values")
        void rule_FMJ_GAF_06_constantsMatchLegacyHardcodedValues() {
            // Legacy uses string literals "INFORME_MOV_FFMM" and "INFORME_PARTICIPE_FFMM"
            assertThat(GenerarArchivosFFMMJob.TIPO_MOVIMIENTOS).isEqualTo("INFORME_MOV_FFMM");     // FMJ-GAF-06
            assertThat(GenerarArchivosFFMMJob.TIPO_PARTICIPES).isEqualTo("INFORME_PARTICIPE_FFMM"); // FMJ-GAF-06
        }
    }
}
