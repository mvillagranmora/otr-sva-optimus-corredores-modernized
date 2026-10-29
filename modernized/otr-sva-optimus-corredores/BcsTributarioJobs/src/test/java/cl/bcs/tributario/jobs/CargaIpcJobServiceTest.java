package cl.bcs.tributario.jobs;

import cl.bcs.tributario.jobs.adapter.out.CargaIpcHanaAdapter;
import cl.bcs.tributario.jobs.domain.CargaIpcResultado;
import cl.bcs.tributario.jobs.port.out.CargaIpcHanaPort;
import cl.bcs.tributario.jobs.service.CargaIpcJobService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pure-logic characterization tests for {@link CargaIpcJobService}.
 *
 * <p>No Spring context is loaded — the port is mocked and pure-logic static
 * methods are called directly.</p>
 *
 * <p>Oracle: legacy {@code cl.bcs.tributario.cargaipc.BcsJobCargaIPC#transaction}</p>
 *
 * <p>Rule IDs pinned: B-TRI-01 through B-TRI-08</p>
 */
@DisplayName("CargaIpcJobService — characterization tests")
class CargaIpcJobServiceTest {

    // =========================================================================
    // B-TRI-01: null parameter guards
    // =========================================================================

    @Nested
    @DisplayName("B-TRI-01: required parameter null-checks")
    class NullParameterGuards {

        @Test
        @DisplayName("B-TRI-01a: null idModulo → NullPointerException")
        void nullIdModulo_throwsNPE() {
            CargaIpcHanaPort port = mock(CargaIpcHanaPort.class);
            CargaIpcJobService svc = new CargaIpcJobService(port);

            assertThatThrownBy(() -> svc.ejecutar(1L, 3L, null, "", ""))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("idModulo");
        }

        @Test
        @DisplayName("B-TRI-01b: null simularOperacion → NullPointerException")
        void nullSimularOperacion_throwsNPE() {
            CargaIpcHanaPort port = mock(CargaIpcHanaPort.class);
            CargaIpcJobService svc = new CargaIpcJobService(port);

            assertThatThrownBy(() -> svc.ejecutar(1L, 3L, "TRI", null, ""))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("simularOperacion");
        }

        @Test
        @DisplayName("B-TRI-01c: null usaReglasNegocio → NullPointerException")
        void nullUsaReglasNegocio_throwsNPE() {
            CargaIpcHanaPort port = mock(CargaIpcHanaPort.class);
            CargaIpcJobService svc = new CargaIpcJobService(port);

            assertThatThrownBy(() -> svc.ejecutar(1L, 3L, "TRI", "", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("usaReglasNegocio");
        }
    }

    // =========================================================================
    // B-TRI-02: erroresContador > 0 → RuntimeException
    // =========================================================================

    @Test
    @DisplayName("B-TRI-02: erroresContador > 0 → RuntimeException thrown")
    void erroresContadorGtZero_throwsRuntimeException() {
        CargaIpcHanaPort port = mock(CargaIpcHanaPort.class);
        when(port.ejecutarCargarValorFactorIpc(anyLong(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(new CargaIpcResultado(0, 3)); // 3 errors

        CargaIpcJobService svc = new CargaIpcJobService(port);

        assertThatThrownBy(() -> svc.ejecutar(1L, 3L, "TRI", "", ""))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("erroresContador=3");
    }

    // =========================================================================
    // B-TRI-03: erroresContador == 0 → success
    // =========================================================================

    @Test
    @DisplayName("B-TRI-03: erroresContador == 0 → returns resultado without throwing")
    void erroresContadorZero_returnsResultado() throws Exception {
        CargaIpcHanaPort port = mock(CargaIpcHanaPort.class);
        CargaIpcResultado expected = new CargaIpcResultado(2, 0); // 2 advertencias, 0 errors
        when(port.ejecutarCargarValorFactorIpc(1L, 3L, "TRI", "", ""))
                .thenReturn(expected);

        CargaIpcJobService svc = new CargaIpcJobService(port);
        CargaIpcResultado actual = svc.ejecutar(1L, 3L, "TRI", "", "");

        assertThat(actual.erroresContador()).isEqualTo(0);
        assertThat(actual.advertenciasContador()).isEqualTo(2);
        assertThat(actual.exitoso()).isTrue();
    }

    // =========================================================================
    // B-TRI-04: default parameter values
    // =========================================================================

    @Nested
    @DisplayName("B-TRI-04: parseLongOrDefault — legacy default values")
    class ParseLongOrDefault {

        @Test
        @DisplayName("B-TRI-04a: null string → default value returned")
        void nullValue_returnsDefault() {
            long result = CargaIpcJobService.parseLongOrDefault(null, 1L);
            assertThat(result).isEqualTo(1L);
        }

        @Test
        @DisplayName("B-TRI-04b: blank string → default value returned")
        void blankValue_returnsDefault() {
            long result = CargaIpcJobService.parseLongOrDefault("   ", 3L);
            assertThat(result).isEqualTo(3L);
        }

        @Test
        @DisplayName("B-TRI-04c: valid numeric string → parsed correctly")
        void validNumericString_returnsParsedLong() {
            long result = CargaIpcJobService.parseLongOrDefault("42", 1L);
            assertThat(result).isEqualTo(42L);
        }

        @Test
        @DisplayName("B-TRI-04d: non-numeric string → NumberFormatException")
        void nonNumericString_throwsNumberFormatException() {
            assertThatThrownBy(() -> CargaIpcJobService.parseLongOrDefault("abc", 1L))
                    .isInstanceOf(NumberFormatException.class);
        }
    }

    // =========================================================================
    // B-TRI-05: domain record
    // =========================================================================

    @Test
    @DisplayName("B-TRI-05: CargaIpcResultado.exitoso() true when erroresContador == 0")
    void cargaIpcResultado_exitosoTrueWhenNoErrors() {
        assertThat(new CargaIpcResultado(5, 0).exitoso()).isTrue();
    }

    @Test
    @DisplayName("B-TRI-05b: CargaIpcResultado.exitoso() false when erroresContador > 0")
    void cargaIpcResultado_exitosoFalseWhenErrors() {
        assertThat(new CargaIpcResultado(0, 1).exitoso()).isFalse();
    }

    // =========================================================================
    // B-TRI-06: default constants
    // =========================================================================

    @Test
    @DisplayName("B-TRI-06: DEFAULT_ID_EMPRESA is 1 (legacy hardcoded default)")
    void defaultIdEmpresa_is1() {
        assertThat(CargaIpcJobService.DEFAULT_ID_EMPRESA).isEqualTo(1L);
    }

    @Test
    @DisplayName("B-TRI-06b: DEFAULT_ID_USUARIO is 3 (legacy hardcoded default)")
    void defaultIdUsuario_is3() {
        assertThat(CargaIpcJobService.DEFAULT_ID_USUARIO).isEqualTo(3L);
    }

    // =========================================================================
    // CANARY — B-TRI-07: SP name constant must match HANA exactly
    // =========================================================================

    @Test
    @DisplayName("B-TRI-07 [CANARY]: SP_NOMBRE matches exact HANA procedure name")
    void canary_spNombreMatchesHanaExactName() {
        // Flip SP_NOMBRE → this test fails (canary behaviour verified)
        assertThat(CargaIpcHanaAdapter.SP_NOMBRE)
                .isEqualTo("BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc");
    }
}
