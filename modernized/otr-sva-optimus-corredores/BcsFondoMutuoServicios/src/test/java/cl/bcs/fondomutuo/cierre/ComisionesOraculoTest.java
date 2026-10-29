package cl.bcs.fondomutuo.cierre;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import cl.bcs.fondomutuo.cierre.CasosOraculo.Caso;

/**
 * FM-COM-01..06 (rule cards of {@code RULES_FONDOMUTUO_SOLICITUD.md} section 14): {@code CalcularComisiones} against the goldens recorded from the
 * unmodified legacy class on H2 (custody lots, quota values and currency conversions answered by the same scripted doubles). The dump includes the input
 * after the run (contributions consume lots and shrink {@code Cantidad}, D-COM-6). The defects D-COM-1..6 are pinned by the cases that name them.
 */
class ComisionesOraculoTest {

    static List<Arguments> casos() {
        return CasosOraculo.cargar("FM-COM-").stream().map(c -> Arguments.of(c.id(), c)).toList();
    }

    @DisplayName("FM-COM: new code equals the legacy oracle")
    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void igualAlOraculo(String nombre, Caso caso) {
        CasosOraculo.comparar(caso, CasosOraculo.ejecutar(caso));
    }
}
