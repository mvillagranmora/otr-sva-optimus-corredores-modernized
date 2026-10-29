package cl.bcs.utils;

import cl.bcs.utils.equivalencia.Golden;
import cl.bcs.utils.equivalencia.Golden.Caso;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UTIL-01: RutUtil.validar (legacy RutUtil.java:46-98), modulo-11 check digit. Equivalence against golden/any/rut.json
 * (80 legacy cases, including the full check-digit table of 20 mantissas x {0-9,K,k}).
 */
class RutUtilTest {

    static final String AREA = "rut";

    @TestFactory
    @DisplayName("UTIL-01 equivalence: RutUtil.validar == legacy for every recorded input")
    Stream<DynamicTest> util01_equivalencia() {
        return Golden.casos("any", AREA).stream().map(c -> DynamicTest.dynamicTest(c.nombre(), () -> {
            if (c.tiene("mantisa")) {
                Golden.igual(AREA, c, () -> {
                    List<String> validos = new ArrayList<>();
                    for (String dv : new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "K", "k"})
                        if (RutUtil.validar(c.l("mantisa") + "-" + dv)) validos.add(dv);
                    return validos;
                });
            } else {
                Golden.igual(AREA, c, () -> RutUtil.validar(c.s("rut")));
            }
        }));
    }

    @AfterAll
    static void reporte() {
        System.out.println("equivalence cases executed [" + AREA + "]: " + Golden.ejecutados(AREA));
    }

    @ParameterizedTest(name = "UTIL-01: \"{0}\" -> {1}")
    @CsvSource(delimiter = '|', value = {
            "1-9|true", "23452345-7|true", "23452345-5|false", "5.123.225-9|true", "97036000-K|true", "97080000-k|true",
            "90249000-0|true", "'12,345,678-5'|false", "'1-9 '|true", "'1 -9'|false", "1-|false", "123456789|false"})
    void util01_ejemplosLiterales(String rut, boolean esperado) {
        assertThat(RutUtil.validar(rut)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("UTIL-01 legacy defect pinned: RUT 0 ('0-0', '00-0') is accepted (n == 11 -> '0')")
    void util01_rutCeroAceptado() {
        assertThat(RutUtil.validar("0-0")).isTrue();
        assertThat(RutUtil.validar("000000000-0")).isTrue();
    }

    @Test
    @DisplayName("UTIL-01: dots are removed ANYWHERE in the mantissa ('1.-9', '.1-9'); commas are rejected (javadoc says the opposite)")
    void util01_puntosEnCualquierLugar() {
        assertThat(RutUtil.validar("1.-9")).isTrue();
        assertThat(RutUtil.validar(".1-9")).isTrue();
        assertThat(RutUtil.validar("12,345,678-5")).isFalse();
    }

    @Test
    @DisplayName("UTIL-01: only String.trim() whitespace is ignored (NBSP and zero-width space are not); non-ASCII digits rejected")
    void util01_espaciosYUnicode() {
        assertThat(RutUtil.validar("\t1-9\n")).isTrue();
        assertThat(RutUtil.validar("1-9 ")).isFalse();
        assertThat(RutUtil.validar("76645030-K​")).isFalse();
        assertThat(RutUtil.validar("１-9")).isFalse();
        assertThat(RutUtil.validar(null)).isFalse();
    }
}
