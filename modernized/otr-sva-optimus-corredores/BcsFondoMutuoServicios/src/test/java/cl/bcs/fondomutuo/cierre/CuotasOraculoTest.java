package cl.bcs.fondomutuo.cierre;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import cl.bcs.fondomutuo.cierre.CasosOraculo.Caso;

/**
 * FM-CUO-01..10 (rule cards of {@code RULES_FONDOMUTUO_SOLICITUD.md} section 13): {@code CalcularMontoNumeroCuota} against the goldens recorded from the
 * unmodified legacy class on H2 (the price and parity collaborators answered by the same scripted doubles). The defects D-CUO-1,2,3,6,7 (scale-sensitive
 * zero, null number of quotas, mixed rounding, null price map, duplicated ids) are pinned by the cases that name them.
 */
class CuotasOraculoTest {

    static List<Arguments> casos() {
        return CasosOraculo.cargar("FM-CUO-").stream().map(c -> Arguments.of(c.id(), c)).toList();
    }

    @DisplayName("FM-CUO: new code equals the legacy oracle")
    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void igualAlOraculo(String nombre, Caso caso) {
        CasosOraculo.comparar(caso, CasosOraculo.ejecutar(caso));
    }
}
