package cl.bcs.plataforma.service;

import cl.bcs.plataforma.service.plataforma.ConvertirMontoEscritoService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ConvertirMontoEscritoService pure logic.
 * No Spring context required — all methods are static.
 * Ported from legacy: cl.bcs.plataforma.pla.utils.ConvertirMontoEscrito
 */
class ConvertirMontoEscritoServiceTest {

    // -----------------------------------------------------------------------
    // cantidadConLetra tests
    // -----------------------------------------------------------------------

    @Test
    void cero_devuelve_cero() {
        assertThat(ConvertirMontoEscritoService.cantidadConLetra("0")).isEqualTo("Cero ");
    }

    @Test
    void numero_simple_unidades() {
        assertThat(ConvertirMontoEscritoService.cantidadConLetra("5")).isEqualTo("Cinco ");
    }

    @Test
    void numero_diez() {
        assertThat(ConvertirMontoEscritoService.cantidadConLetra("10")).isEqualTo("Diez ");
    }

    @Test
    void numero_once() {
        assertThat(ConvertirMontoEscritoService.cantidadConLetra("11")).isEqualTo("Once ");
    }

    @Test
    void numero_doce() {
        assertThat(ConvertirMontoEscritoService.cantidadConLetra("12")).isEqualTo("Doce ");
    }

    @Test
    void numero_quince() {
        assertThat(ConvertirMontoEscritoService.cantidadConLetra("15")).isEqualTo("Quince ");
    }

    @Test
    void numero_veinte() {
        assertThat(ConvertirMontoEscritoService.cantidadConLetra("20")).isEqualTo("Veinte ");
    }

    @Test
    void numero_veintiuno() {
        // Legacy: DECENAS[2]="Veinti" + UNIDADES[1]="Un " → "VeintiUn "
        assertThat(ConvertirMontoEscritoService.cantidadConLetra("21")).isEqualTo("VeintiUn ");
    }

    @Test
    void numero_cien() {
        assertThat(ConvertirMontoEscritoService.cantidadConLetra("100")).isEqualTo("Cien ");
    }

    @Test
    void numero_ciento_uno() {
        // Legacy: CENTENAS[1]="Ciento " + UNIDADES[1]="Un " → "Ciento Un "
        assertThat(ConvertirMontoEscritoService.cantidadConLetra("101")).isEqualTo("Ciento Un ");
    }

    @Test
    void numero_mil() {
        assertThat(ConvertirMontoEscritoService.cantidadConLetra("1000")).contains("Mil ");
    }

    @Test
    void numero_mil_doscientos_treinta_y_cuatro() {
        String result = ConvertirMontoEscritoService.cantidadConLetra("1234");
        assertThat(result).contains("Mil ");
        assertThat(result).contains("Doscientos ");
    }

    @Test
    void numero_con_decimales_usa_parte_entera() {
        // 1234.99 → same as 1234
        String resultConDecimales = ConvertirMontoEscritoService.cantidadConLetra("1234.99");
        String resultSinDecimales = ConvertirMontoEscritoService.cantidadConLetra("1234");
        assertThat(resultConDecimales).isEqualTo(resultSinDecimales);
    }

    @Test
    void numero_millon() {
        String result = ConvertirMontoEscritoService.cantidadConLetra("1000000");
        assertThat(result).contains("Millon ");
    }

    @Test
    void numero_dos_millones() {
        String result = ConvertirMontoEscritoService.cantidadConLetra("2000000");
        assertThat(result).contains("Millones ");
    }

    @Test
    void valor_nulo_lanza_excepcion() {
        assertThatThrownBy(() -> ConvertirMontoEscritoService.cantidadConLetra(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void valor_vacio_lanza_excepcion() {
        assertThatThrownBy(() -> ConvertirMontoEscritoService.cantidadConLetra(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void valor_no_numerico_lanza_number_format_exception() {
        assertThatThrownBy(() -> ConvertirMontoEscritoService.cantidadConLetra("abc"))
            .isInstanceOf(NumberFormatException.class);
    }

    // -----------------------------------------------------------------------
    // cantidadNumerosRomanos tests
    // -----------------------------------------------------------------------

    @Test
    void romano_uno_es_I() {
        assertThat(ConvertirMontoEscritoService.cantidadNumerosRomanos("1")).isEqualTo("I");
    }

    @Test
    void romano_cuatro_es_IV() {
        assertThat(ConvertirMontoEscritoService.cantidadNumerosRomanos("4")).isEqualTo("IV");
    }

    @Test
    void romano_nueve_es_IX() {
        assertThat(ConvertirMontoEscritoService.cantidadNumerosRomanos("9")).isEqualTo("IX");
    }

    @Test
    void romano_cuarenta_es_XL() {
        assertThat(ConvertirMontoEscritoService.cantidadNumerosRomanos("40")).isEqualTo("XL");
    }

    @Test
    void romano_cien_es_C() {
        assertThat(ConvertirMontoEscritoService.cantidadNumerosRomanos("100")).isEqualTo("C");
    }

    @Test
    void romano_cuatrocientos_es_CD() {
        assertThat(ConvertirMontoEscritoService.cantidadNumerosRomanos("400")).isEqualTo("CD");
    }

    @Test
    void romano_novecientos_es_CM() {
        assertThat(ConvertirMontoEscritoService.cantidadNumerosRomanos("900")).isEqualTo("CM");
    }

    @Test
    void romano_mil_es_M() {
        assertThat(ConvertirMontoEscritoService.cantidadNumerosRomanos("1000")).isEqualTo("M");
    }

    @Test
    void romano_dos_mil_cuatrocientos_cincuenta_y_cuatro_es_MMCDLIV() {
        assertThat(ConvertirMontoEscritoService.cantidadNumerosRomanos("2454")).isEqualTo("MMCDLIV");
    }

    @Test
    void romano_tres_mil_novecientos_noventa_y_nueve_es_MMMCMXCIX() {
        assertThat(ConvertirMontoEscritoService.cantidadNumerosRomanos("3999")).isEqualTo("MMMCMXCIX");
    }

    @Test
    void romano_cero_lanza_excepcion() {
        assertThatThrownBy(() -> ConvertirMontoEscritoService.cantidadNumerosRomanos("0"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("1 y 3999");
    }

    @Test
    void romano_cuatro_mil_lanza_excepcion() {
        assertThatThrownBy(() -> ConvertirMontoEscritoService.cantidadNumerosRomanos("4000"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("1 y 3999");
    }

    @Test
    void romano_nulo_lanza_excepcion() {
        assertThatThrownBy(() -> ConvertirMontoEscritoService.cantidadNumerosRomanos(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // -----------------------------------------------------------------------
    // numerosPrimos tests
    // -----------------------------------------------------------------------

    @Test
    void primos_hasta_10_son_2_3_5_7() {
        List<Integer> primos = ConvertirMontoEscritoService.numerosPrimos(10);
        assertThat(primos).containsExactly(2, 3, 5, 7);
    }

    @Test
    void primos_hasta_2_solo_contiene_2() {
        List<Integer> primos = ConvertirMontoEscritoService.numerosPrimos(3);
        assertThat(primos).containsExactly(2);
    }

    @Test
    void primos_hasta_20_son_correctos() {
        List<Integer> primos = ConvertirMontoEscritoService.numerosPrimos(20);
        assertThat(primos).containsExactly(2, 3, 5, 7, 11, 13, 17, 19);
    }
}
