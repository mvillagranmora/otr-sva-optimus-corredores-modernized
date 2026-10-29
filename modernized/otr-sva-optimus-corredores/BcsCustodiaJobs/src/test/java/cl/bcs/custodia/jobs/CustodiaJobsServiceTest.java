package cl.bcs.custodia.jobs;

import cl.bcs.custodia.jobs.service.CustodiaJobsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-logic unit tests for CustodiaJobsService.
 * No Spring context required — all methods are public static.
 *
 * Canary: temporarily break obtenerDecimalesCantidad("UF") to return 3 instead of 4,
 *         verify test_obtenerDecimales_UF fails, then restore. (Documented in TRANSFORMATION_NOTES.md)
 */
class CustodiaJobsServiceTest {

    // -----------------------------------------------------------------------
    // esEnHorario
    // Legacy: ValidarHorarioEjercicioBloqueoOpciones.java lines 98-103
    // if (fechaLocalTS.after(fechaLocalIni) && fechaLocalTS.before(fechaLocalTer))
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("esEnHorario: time strictly within window -> true")
    void test_esEnHorario_withinRange() {
        LocalDateTime fechaLocal = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        LocalTime horaIni = LocalTime.of(9, 0, 0);
        LocalTime horaTer = LocalTime.of(17, 0, 0);

        assertThat(CustodiaJobsService.esEnHorario(fechaLocal, horaIni, horaTer)).isTrue();
    }

    @Test
    @DisplayName("esEnHorario: time before window -> false")
    void test_esEnHorario_beforeRange() {
        LocalDateTime fechaLocal = LocalDateTime.of(2024, 1, 15, 8, 0, 0);
        LocalTime horaIni = LocalTime.of(9, 0, 0);
        LocalTime horaTer = LocalTime.of(17, 0, 0);

        assertThat(CustodiaJobsService.esEnHorario(fechaLocal, horaIni, horaTer)).isFalse();
    }

    @Test
    @DisplayName("esEnHorario: time after window -> false")
    void test_esEnHorario_afterRange() {
        LocalDateTime fechaLocal = LocalDateTime.of(2024, 1, 15, 18, 0, 0);
        LocalTime horaIni = LocalTime.of(9, 0, 0);
        LocalTime horaTer = LocalTime.of(17, 0, 0);

        assertThat(CustodiaJobsService.esEnHorario(fechaLocal, horaIni, horaTer)).isFalse();
    }

    @Test
    @DisplayName("esEnHorario: exactly at horaIni boundary -> false (strictly after)")
    void test_esEnHorario_atStartBoundary() {
        LocalDateTime fechaLocal = LocalDateTime.of(2024, 1, 15, 9, 0, 0);
        LocalTime horaIni = LocalTime.of(9, 0, 0);
        LocalTime horaTer = LocalTime.of(17, 0, 0);

        assertThat(CustodiaJobsService.esEnHorario(fechaLocal, horaIni, horaTer)).isFalse();
    }

    @Test
    @DisplayName("esEnHorario: exactly at horaTer boundary -> false (strictly before)")
    void test_esEnHorario_atEndBoundary() {
        LocalDateTime fechaLocal = LocalDateTime.of(2024, 1, 15, 17, 0, 0);
        LocalTime horaIni = LocalTime.of(9, 0, 0);
        LocalTime horaTer = LocalTime.of(17, 0, 0);

        assertThat(CustodiaJobsService.esEnHorario(fechaLocal, horaIni, horaTer)).isFalse();
    }

    // -----------------------------------------------------------------------
    // obtenerDecimalesCantidad
    // Legacy: GenerarArchivoInformeMayorMenorValor.java lines 125-133
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("obtenerDecimalesCantidad: UF -> 4")
    void test_obtenerDecimales_UF() {
        assertThat(CustodiaJobsService.obtenerDecimalesCantidad("UF"))
                .isEqualTo(CustodiaJobsService.DECIMALES_CANTIDAD_UF)
                .isEqualTo(4);
    }

    @Test
    @DisplayName("obtenerDecimalesCantidad: USD -> 2")
    void test_obtenerDecimales_USD() {
        assertThat(CustodiaJobsService.obtenerDecimalesCantidad("USD"))
                .isEqualTo(CustodiaJobsService.DECIMALES_CANTIDAD_USD_EUR)
                .isEqualTo(2);
    }

    @Test
    @DisplayName("obtenerDecimalesCantidad: EUR -> 2")
    void test_obtenerDecimales_EUR() {
        assertThat(CustodiaJobsService.obtenerDecimalesCantidad("EUR"))
                .isEqualTo(CustodiaJobsService.DECIMALES_CANTIDAD_USD_EUR)
                .isEqualTo(2);
    }

    @Test
    @DisplayName("obtenerDecimalesCantidad: CLP -> 0")
    void test_obtenerDecimales_CLP() {
        assertThat(CustodiaJobsService.obtenerDecimalesCantidad("CLP"))
                .isEqualTo(CustodiaJobsService.DECIMALES_CANTIDAD_CLP)
                .isEqualTo(0);
    }

    @Test
    @DisplayName("obtenerDecimalesCantidad: GBP (unknown) -> 0 (default)")
    void test_obtenerDecimales_GBP_default() {
        assertThat(CustodiaJobsService.obtenerDecimalesCantidad("GBP"))
                .isEqualTo(CustodiaJobsService.DECIMALES_CANTIDAD_CLP)
                .isEqualTo(0);
    }

    // -----------------------------------------------------------------------
    // esDiferenciadorPacto
    // Legacy: GenerarArchivoInformeMayorMenorValor.java line 52
    // private static final List<String> DIFERENCIADORES_PACTOS = Arrays.asList("PP", "PC", "PD");
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("esDiferenciadorPacto: PP -> true")
    void test_esDiferenciadorPacto_PP() {
        assertThat(CustodiaJobsService.esDiferenciadorPacto("PP")).isTrue();
    }

    @Test
    @DisplayName("esDiferenciadorPacto: PC -> true")
    void test_esDiferenciadorPacto_PC() {
        assertThat(CustodiaJobsService.esDiferenciadorPacto("PC")).isTrue();
    }

    @Test
    @DisplayName("esDiferenciadorPacto: PD -> true")
    void test_esDiferenciadorPacto_PD() {
        assertThat(CustodiaJobsService.esDiferenciadorPacto("PD")).isTrue();
    }

    @Test
    @DisplayName("esDiferenciadorPacto: SF -> false")
    void test_esDiferenciadorPacto_SF_false() {
        assertThat(CustodiaJobsService.esDiferenciadorPacto("SF")).isFalse();
    }

    @Test
    @DisplayName("esDiferenciadorPacto: empty string -> false")
    void test_esDiferenciadorPacto_empty_false() {
        assertThat(CustodiaJobsService.esDiferenciadorPacto("")).isFalse();
    }

    @Test
    @DisplayName("esDiferenciadorPacto: null -> false")
    void test_esDiferenciadorPacto_null_false() {
        assertThat(CustodiaJobsService.esDiferenciadorPacto(null)).isFalse();
    }

    // -----------------------------------------------------------------------
    // determinarEventoHorario
    // Legacy: ValidarHorarioEjercicioBloqueoOpciones.java lines 138-149
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("determinarEventoHorario: SI + no consulta anterior -> INICIO")
    void test_determinarEvento_inicio() {
        assertThat(CustodiaJobsService.determinarEventoHorario("SI", false))
                .isEqualTo("INICIO");
    }

    @Test
    @DisplayName("determinarEventoHorario: NO + consulta anterior (INICIO) -> TERMINO")
    void test_determinarEvento_termino() {
        assertThat(CustodiaJobsService.determinarEventoHorario("NO", true))
                .isEqualTo("TERMINO");
    }

    @Test
    @DisplayName("determinarEventoHorario: SI + ya tiene consulta anterior -> null (no action)")
    void test_determinarEvento_already_started() {
        assertThat(CustodiaJobsService.determinarEventoHorario("SI", true)).isNull();
    }

    @Test
    @DisplayName("determinarEventoHorario: NO + no consulta anterior -> null (no action)")
    void test_determinarEvento_no_action() {
        assertThat(CustodiaJobsService.determinarEventoHorario("NO", false)).isNull();
    }

    // -----------------------------------------------------------------------
    // Constants sanity checks
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Constants: DIFERENCIADORES_PACTOS contains exactly PP, PC, PD")
    void test_diferenciadores_pactos_constants() {
        assertThat(CustodiaJobsService.DIFERENCIADORES_PACTOS)
                .hasSize(3)
                .containsExactlyInAnyOrder("PP", "PC", "PD");
    }

    @Test
    @DisplayName("Constants: DECIMALES_TASAS=6, DECIMALES_PRECIOS=2, DECIMALES_PORCENTAJES=2")
    void test_decimal_constants() {
        assertThat(CustodiaJobsService.DECIMALES_TASAS).isEqualTo(6);
        assertThat(CustodiaJobsService.DECIMALES_PRECIOS).isEqualTo(2);
        assertThat(CustodiaJobsService.DECIMALES_PORCENTAJES).isEqualTo(2);
    }
}
