package cl.bcs.fondomutuo.cierre;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import cl.bcs.fondomutuo.cierre.CasosOraculo.Caso;

/**
 * FM-FEC-01..14 (rule cards of {@code RULES_FONDOMUTUO_SOLICITUD.md} section 12): {@code CalcularFechaSolicitudAporteRescate} and the three date helpers
 * {@code ObtenerDiaSiguiente / ObtenerDiaAnterior / ObtenerDiaHabilMultiplesPaises} (ids {@code FM-FEC-Dnn}) against the goldens recorded from the
 * unmodified legacy classes on H2. Each case id says the rule and the situation; the card of the rule is in the rules file, the legacy lines in the
 * {@code ref} of the case. The defects D-FEC-1..6,10 are pinned by the cases that name them.
 */
class FechasOraculoTest {

    static List<Arguments> casos() {
        return CasosOraculo.cargar("FM-FEC-").stream().map(c -> Arguments.of(c.id(), c)).toList();
    }

    @DisplayName("FM-FEC: new code equals the legacy oracle")
    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void igualAlOraculo(String nombre, Caso caso) {
        CasosOraculo.comparar(caso, CasosOraculo.ejecutar(caso));
    }
}
