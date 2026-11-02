package cl.bcs.fondomutuo.aportereinversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import cl.bcs.fondomutuo.aportereinversion.CasoCsr.Perfil;

/**
 * FMM-CSR (F3 W3 T4-A), cards FM-REI-41..68 of RULES_FONDOMUTUO_F2_REINVERSION.md 8.2 and defects D-F2-4, 6..21, D-NOR-5/9/11/12, D-SQL-1, D-TES-1/2: the new
 * {@code CrearSolicitudAporteReinversion} (REAL, over the real JDBC adapters of the module, behind the typed ports of the collaborators that are not ported or sit behind a gate) against
 * the goldens RECORDED from the unmodified legacy class by record_csr.sh on the same H2 and the same scripted collaborators (20 runs per case, 0 unstable). Compared per case: the
 * output text, the tables, every collaborator call with its COMPLETE arguments, the sequences, the session, the decision of CallServlet and the exception (see {@link CsrCostura}).
 *
 * <p>RED BEFORE CODE: the production classes do not exist yet, so every case fails with "CSR not implemented: the production class ... does not exist yet". A golden that is missing
 * FAILS the case; a run in which no comparison executed FAILS ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CsrEquivalenciaTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    /** (id, the rules the case pins: its `ref`): the display name of every case NAMES its rules (FM-REI-nn, D-F2-nn) */
    static Stream<Arguments> casos() {
        List<String> ids = CasoCsr.casos();
        assertTrue(ids.size() >= 150, "the FMM-CSR matrix has at least 150 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, CasoCsr.ref(id)));
    }

    @Test
    @Order(0)
    @DisplayName("FMM-CSR: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoCsr.casos()) {
            if (!CasoCsr.tieneGolden(id)) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_csr.sh): " + sin);
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FMM-CSR equivalence with the recorded legacy (FM-REI-41..68)")
    void equivalente(String id, String reglas) throws Exception {
        var expected = CasoCsr.golden(id);
        var actual = CasoCsr.ejecutarReal(id, Perfil.ABIERTAS);
        CsrCostura.comparar(id, expected, actual);
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FMM-CSR: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoCsr.casos().size());
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoCsr.casos().size(), EJECUTADOS.get(), "a case was not compared");
    }
}
