package cl.bcs.fondomutuo.cierre;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import cl.bcs.fondomutuo.cierre.CasosOraculo.Caso;

/**
 * FM-REQ-20 (rule card of {@code RULES_FONDOMUTUO_SOLICITUD.md} section 15, defect D-REQ-8): {@code ObtenerPerfilFondoMutuo} against the goldens recorded from
 * the unmodified legacy class on H2: the commercial / back-office profile of the user, none when the user has no such profile, the first row when the
 * {@code TOP 1} query (no {@code ORDER BY}) finds two.
 */
class PerfilOraculoTest {

    static List<Arguments> casos() {
        return CasosOraculo.cargar("FM-REQ-20").stream().map(c -> Arguments.of(c.id(), c)).toList();
    }

    @DisplayName("FM-REQ-20: new code equals the legacy oracle")
    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void igualAlOraculo(String nombre, Caso caso) {
        CasosOraculo.comparar(caso, CasosOraculo.ejecutar(caso));
    }
}
