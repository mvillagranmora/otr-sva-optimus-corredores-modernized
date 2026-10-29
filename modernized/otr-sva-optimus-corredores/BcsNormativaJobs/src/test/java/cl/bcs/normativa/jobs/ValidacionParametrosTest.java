package cl.bcs.normativa.jobs;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.service.carga.CargaService;
import cl.bcs.normativa.jobs.service.cuadratura.CuadraturaService;
import cl.bcs.normativa.jobs.service.declaraciones.CertificadosTributariosService;
import cl.bcs.normativa.jobs.service.exportacion.ExportacionService;
import cl.bcs.normativa.jobs.service.generacion.GeneracionService;
import cl.bcs.normativa.jobs.service.serv.ProcesarServService;
import cl.bcs.normativa.jobs.service.validacion.ValidacionService;
import cl.bcs.normativa.jobs.service.valorizar.ValorizarService;
import cl.bcs.normativa.jobs.service.apv.ReporteApvService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for pure-logic parameter validation methods in all job services.
 *
 * These tests require no Spring context or HANA connection.
 * They pin the mandatory parameters for each job type.
 *
 * B-NJB-GEN-01 through B-NJB-CERT-03
 */
@DisplayName("Validación de parámetros — todos los jobs")
class ValidacionParametrosTest {

    // ─── GeneracionService ────────────────────────────────────────────────────

    @Nested
    @DisplayName("GeneracionService.validarParametros()")
    class GeneracionParams {

        @Test
        @DisplayName("NJB-GEN-PAR-01: valid params pass without exception")
        void validParams_noException() {
            var params = new ParametrosJob(1L, Map.of(
                    "idDocumento", "100",
                    "idUsuario", "6",
                    "schema", "BSCL"
            ));
            assertThatNoException().isThrownBy(() -> GeneracionService.validarParametros(params));
        }

        @Test
        @DisplayName("NJB-GEN-PAR-02: missing idDocumento throws IAE")
        void missingIdDocumento_throwsIAE() {
            var params = new ParametrosJob(1L, Map.of("idUsuario", "6", "schema", "BSCL"));
            assertThatThrownBy(() -> GeneracionService.validarParametros(params))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("idDocumento");
        }
    }

    // ─── ValidacionService ────────────────────────────────────────────────────

    @Nested
    @DisplayName("ValidacionService.validarParametros()")
    class ValidacionParams {

        @Test
        @DisplayName("NJB-VAL-PAR-01: valid params pass without exception")
        void validParams_noException() {
            var params = new ParametrosJob(2L, Map.of(
                    "idDocumento", "114",
                    "idUsuario", "51",
                    "schema", "BSCL"
            ));
            assertThatNoException().isThrownBy(() -> ValidacionService.validarParametros(params));
        }

        @Test
        @DisplayName("NJB-VAL-PAR-02: missing schema throws IAE")
        void missingSchema_throwsIAE() {
            var params = new ParametrosJob(2L, Map.of("idDocumento", "114", "idUsuario", "51"));
            assertThatThrownBy(() -> ValidacionService.validarParametros(params))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("schema");
        }
    }

    // ─── CargaService ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CargaService.validarParametros()")
    class CargaParams {

        @Test
        @DisplayName("NJB-CAR-PAR-01: valid params pass without exception")
        void validParams_noException() {
            var params = new ParametrosJob(3L, Map.of(
                    "idDocumento", "200",
                    "idUsuario", "10",
                    "schema", "BSCL"
            ));
            assertThatNoException().isThrownBy(() -> CargaService.validarParametros(params));
        }
    }

    // ─── CuadraturaService ────────────────────────────────────────────────────

    @Nested
    @DisplayName("CuadraturaService.validarParametros()")
    class CuadraturaParams {

        @Test
        @DisplayName("NJB-CUA-PAR-01: valid params pass without exception")
        void validParams_noException() {
            var params = new ParametrosJob(4L, Map.of(
                    "idDocumento", "300",
                    "idUsuario", "20",
                    "schema", "BSCL"
            ));
            assertThatNoException().isThrownBy(() -> CuadraturaService.validarParametros(params));
        }
    }

    // ─── ExportacionService ───────────────────────────────────────────────────

    @Nested
    @DisplayName("ExportacionService.validarParametros()")
    class ExportacionParams {

        @Test
        @DisplayName("NJB-EXP-PAR-01: valid params pass without exception")
        void validParams_noException() {
            var params = new ParametrosJob(5L, Map.of(
                    "idDocumento", "400",
                    "idUsuario", "30",
                    "schema", "BSCL"
            ));
            assertThatNoException().isThrownBy(() -> ExportacionService.validarParametros(params));
        }
    }

    // ─── ProcesarServService ──────────────────────────────────────────────────

    @Nested
    @DisplayName("ProcesarServService.validarParametros()")
    class ProcesarServParams {

        @Test
        @DisplayName("NJB-SRV-PAR-01: valid params pass without exception")
        void validParams_noException() {
            var params = new ParametrosJob(6L, Map.of(
                    "IdEmpresa", "1",
                    "IdTipoTarea", "ValidacionDJ",
                    "IdSolicitudCircular", "100"
            ));
            assertThatNoException().isThrownBy(() -> ProcesarServService.validarParametros(params));
        }

        @Test
        @DisplayName("NJB-SRV-PAR-02: missing IdTipoTarea throws IAE")
        void missingIdTipoTarea_throwsIAE() {
            var params = new ParametrosJob(6L, Map.of(
                    "IdEmpresa", "1",
                    "IdSolicitudCircular", "100"
            ));
            assertThatThrownBy(() -> ProcesarServService.validarParametros(params))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("IdTipoTarea");
        }

        @Test
        @DisplayName("NJB-SRV-PAR-03: non-numeric IdEmpresa throws NFE")
        void nonNumericIdEmpresa_throwsNFE() {
            var params = new ParametrosJob(6L, Map.of(
                    "IdEmpresa", "abc",
                    "IdTipoTarea", "ValidacionDJ",
                    "IdSolicitudCircular", "100"
            ));
            assertThatThrownBy(() -> ProcesarServService.validarParametros(params))
                    .isInstanceOf(NumberFormatException.class);
        }
    }

    // ─── ValorizarService ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("ValorizarService.validarParametrosNCG499()")
    class ValorizarNCG499Params {

        @Test
        @DisplayName("NJB-VAL-NCG-PAR-01: valid params pass without exception")
        void validParams_noException() {
            var params = new ParametrosJob(7L, Map.of(
                    "IdEmpresa", "1",
                    "IdUsuario", "6",
                    "IdPais", "44",
                    "IdModulo", "CUS",
                    "Fecha", "2024-03-31"
            ));
            assertThatNoException().isThrownBy(() -> ValorizarService.validarParametrosNCG499(params));
        }

        @Test
        @DisplayName("NJB-VAL-NCG-PAR-02: missing Fecha throws IAE")
        void missingFecha_throwsIAE() {
            var params = new ParametrosJob(7L, Map.of(
                    "IdEmpresa", "1",
                    "IdUsuario", "6",
                    "IdPais", "44",
                    "IdModulo", "CUS"
            ));
            assertThatThrownBy(() -> ValorizarService.validarParametrosNCG499(params))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Fecha");
        }
    }

    @Nested
    @DisplayName("ValorizarService.validarParametrosDeclaracion()")
    class ValorizarDeclaracionParams {

        @Test
        @DisplayName("NJB-VAL-DEC-PAR-01: valid params pass without exception")
        void validParams_noException() {
            var params = new ParametrosJob(8L, Map.of(
                    "IdEmpresa", "1",
                    "IdUsuario", "6",
                    "IdModulo", "NOR",
                    "Version", "2022",
                    "FuenteDatos", "INICIAL"
            ));
            assertThatNoException().isThrownBy(() -> ValorizarService.validarParametrosDeclaracion(params));
        }

        @Test
        @DisplayName("NJB-VAL-DEC-PAR-02: missing FuenteDatos throws IAE")
        void missingFuenteDatos_throwsIAE() {
            var params = new ParametrosJob(8L, Map.of(
                    "IdEmpresa", "1",
                    "IdUsuario", "6",
                    "IdModulo", "NOR",
                    "Version", "2022"
            ));
            assertThatThrownBy(() -> ValorizarService.validarParametrosDeclaracion(params))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FuenteDatos");
        }
    }

    // ─── CertificadosTributariosService ──────────────────────────────────────

    @Nested
    @DisplayName("CertificadosTributariosService")
    class CertificadosParams {

        @Test
        @DisplayName("NJB-CERT-PAR-01: valid params pass without exception")
        void validParams_noException() {
            var params = new ParametrosJob(9L, Map.of(
                    "IdEmpresa", "1",
                    "IdUsuario", "6",
                    "AnoTributario", "2024"
            ));
            assertThatNoException().isThrownBy(
                    () -> CertificadosTributariosService.validarParametros(params));
        }

        @Test
        @DisplayName("NJB-CERT-PAR-02: parsearAnoTributario accepts valid year")
        void parsearAnoTributario_validYear() {
            int ano = CertificadosTributariosService.parsearAnoTributario("2024");
            assertThat(Integer.toString(ano)).isEqualTo("2024");
        }

        @Test
        @DisplayName("NJB-CERT-PAR-03: parsearAnoTributario rejects non-numeric")
        void parsearAnoTributario_rejectsNonNumeric() {
            assertThatThrownBy(() -> CertificadosTributariosService.parsearAnoTributario("abc"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("NJB-CERT-PAR-04: parsearAnoTributario rejects out-of-range year")
        void parsearAnoTributario_rejectsOutOfRange() {
            assertThatThrownBy(() -> CertificadosTributariosService.parsearAnoTributario("1800"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("rango");
        }
    }

    // ─── ReporteApvService ────────────────────────────────────────────────────

    @Nested
    @DisplayName("ReporteApvService.validarParametros()")
    class ReporteApvParams {

        @Test
        @DisplayName("NJB-APV-PAR-01: valid params pass without exception")
        void validParams_noException() {
            var params = new ParametrosJob(10L, Map.of(
                    "IdEmpresa", "1",
                    "IdUsuario", "6",
                    "FechaCierre", "2024-03-31"
            ));
            assertThatNoException().isThrownBy(() -> ReporteApvService.validarParametros(params));
        }

        @Test
        @DisplayName("NJB-APV-PAR-02: missing FechaCierre throws IAE")
        void missingFechaCierre_throwsIAE() {
            var params = new ParametrosJob(10L, Map.of(
                    "IdEmpresa", "1",
                    "IdUsuario", "6"
            ));
            assertThatThrownBy(() -> ReporteApvService.validarParametros(params))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FechaCierre");
        }
    }
}
