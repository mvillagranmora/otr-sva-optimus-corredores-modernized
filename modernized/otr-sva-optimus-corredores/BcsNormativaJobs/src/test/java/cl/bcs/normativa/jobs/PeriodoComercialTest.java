package cl.bcs.normativa.jobs;

import cl.bcs.normativa.jobs.domain.PeriodoComercial;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Characterization tests for PeriodoComercial pure-logic methods.
 *
 * Oracle: legacy ProcesoGeneracion.generarArchivoNormativa() (ProcesoGeneracion.java)
 *   - nombreArchivo.replace("RutEntidadLegal", rut.replace("-",""))
 *   - nombreArchivo.replace("Periodicidad", periodicidadEfectiva)
 *   - nombreArchivo.replace("PeriodoComercial", fechaPeriodo.replace("-",""))
 *   - nombreArchivo.replace("TipoArchivo", tipoArchivo)
 *
 * And periodicidadEfectiva:
 *   ("S".equals(rectifica) && "M".equals(periodicidad)) ? "C" : periodicidad
 *
 * B-NJB-10: DJ1 file naming rules
 * B-NJB-11: Periodicidad efectiva: S+M → "C"
 * B-NJB-12: Filename construction pattern
 */
@DisplayName("PeriodoComercial — construcción de nombre archivo DJ1")
class PeriodoComercialTest {

    @Nested
    @DisplayName("periodicidadEfectiva()")
    class PeriodicidadEfectiva {

        @Test
        @DisplayName("NJB-11a: rectifica=S and periodicidad=M → 'C'")
        void rectificaSAndPeriodicidadM_returnsC() {
            assertThat(PeriodoComercial.periodicidadEfectiva("M", "S")).isEqualTo("C");
        }

        @Test
        @DisplayName("NJB-11b: rectifica=N and periodicidad=M → 'M'")
        void rectificaNAndPeriodicidadM_returnsM() {
            assertThat(PeriodoComercial.periodicidadEfectiva("M", "N")).isEqualTo("M");
        }

        @Test
        @DisplayName("NJB-11c: rectifica=S and periodicidad=T → 'T'")
        void rectificaSAndPeriodicidadT_returnsT() {
            assertThat(PeriodoComercial.periodicidadEfectiva("T", "S")).isEqualTo("T");
        }

        @ParameterizedTest(name = "periodicidad={0}, rectifica={1} → {2}")
        @CsvSource({
            "M, S, C",
            "M, N, M",
            "T, S, T",
            "T, N, T",
            "A, S, A",
            "A, N, A"
        })
        @DisplayName("NJB-11d: parametrized periodicidad efectiva table")
        void periodicidadEfectiva_parametrized(String periodicidad, String rectifica, String expected) {
            assertThat(PeriodoComercial.periodicidadEfectiva(periodicidad, rectifica))
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("NJB-11e: null periodicidad throws NPE")
        void nullPeriodicidad_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> PeriodoComercial.periodicidadEfectiva(null, "N"));
        }

        @Test
        @DisplayName("NJB-11f: null rectifica throws NPE")
        void nullRectifica_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> PeriodoComercial.periodicidadEfectiva("M", null));
        }
    }

    @Nested
    @DisplayName("construirNombreArchivoDJ1()")
    class ConstruirNombreArchivo {

        @Test
        @DisplayName("NJB-12a: constructs FX filename correctly — normal case (rectifica=N)")
        void construye_fx_normal() {
            String plantilla = "/base/RutEntidadLegal_Periodicidad_PeriodoComercial_TipoArchivo.txt";
            String resultado = PeriodoComercial.construirNombreArchivoDJ1(
                    plantilla, "12345678-9", "M", "N", "2024-01", "FX");
            assertThat(resultado).isEqualTo("/base/123456789_M_202401_FX.txt");
        }

        @Test
        @DisplayName("NJB-12b: constructs FX filename with periodicidad C when rectifica=S")
        void construye_fx_rectifica_S() {
            String plantilla = "/base/RutEntidadLegal_Periodicidad_PeriodoComercial_TipoArchivo.txt";
            String resultado = PeriodoComercial.construirNombreArchivoDJ1(
                    plantilla, "12345678-9", "M", "S", "2024-01", "FX");
            // periodicidadEfectiva(M, S) = C
            assertThat(resultado).isEqualTo("/base/123456789_C_202401_FX.txt");
        }

        @Test
        @DisplayName("NJB-12c: rut with dashes is stripped")
        void rut_dashesAreStripped() {
            String plantilla = "RutEntidadLegal_Periodicidad_PeriodoComercial_TipoArchivo";
            String resultado = PeriodoComercial.construirNombreArchivoDJ1(
                    plantilla, "76543210-K", "T", "N", "2023-12", "IR");
            assertThat(resultado).startsWith("76543210K_");
        }

        @Test
        @DisplayName("NJB-12d: periodo comercial dashes are stripped")
        void periodoDashes_areStripped() {
            String plantilla = "RutEntidadLegal_Periodicidad_PeriodoComercial_TipoArchivo";
            String resultado = PeriodoComercial.construirNombreArchivoDJ1(
                    plantilla, "12345678-9", "M", "N", "2024-03-31", "FI");
            // 2024-03-31 → 20240331
            assertThat(resultado).contains("20240331");
        }

        @Test
        @DisplayName("NJB-12e: null plantilla throws NPE")
        void nullPlantilla_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> PeriodoComercial.construirNombreArchivoDJ1(
                            null, "12345678-9", "M", "N", "2024-01", "FX"));
        }

        @Test
        @DisplayName("NJB-12f: all three DJ1 type variants produce distinct filenames")
        void threeTypes_areDistinct() {
            String plantilla = "/ruta/RutEntidadLegal_Periodicidad_PeriodoComercial_TipoArchivo.txt";
            String fx = PeriodoComercial.construirNombreArchivoDJ1(
                    plantilla, "12345678-9", "M", "N", "2024-01", "FX");
            // IR replaces the previous file name with IR, not by re-running from plantilla
            // so we test the second replacement logic (FX→IR, IR→FI) differently:
            // from legacy: nombreArchivo.replace("FX","IR")
            String ir = fx.replace("FX", "IR");
            String fi = ir.replace("IR", "FI");

            assertThat(fx).endsWith("FX.txt");
            assertThat(ir).endsWith("IR.txt");
            assertThat(fi).endsWith("FI.txt");
            assertThat(fx).isNotEqualTo(ir);
            assertThat(ir).isNotEqualTo(fi);
        }
    }
}
