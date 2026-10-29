package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
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

import com.google.gson.JsonObject;

/**
 * FMM-CER (F3 W3 T2), cards FM-CER-21, 22, 23 (aportes), 24..28 (front F-C) and FM-CER-31..41 (front F-D) and the defects D-F3B-01/03/05/06/08/09/10/11/37, D-NOR-9/11/16, D-COM-1/5
 * (RULES_FONDOMUTUO_F3_B.md 8.1 and 9): the units of F-C and F-D of the new {@code CertificarSolicitudAporteRescate} (REAL, behind the ports of the collaborators that are not ported:
 * the cash flow, the custody calculation and the custody entry / retirement, answered by the case script) against the goldens RECORDED from the unmodified legacy by
 * record_cer_t2.sh on the same H2 and the same scripted collaborators.
 *
 * <p>Every case runs F-A and F-B of T1 for real (REAL JDBC adapters), then the five steps of F-C and F-D ({@link ApiT2.Pasos}) at the seam, and {@link CerCosturaT2} compares what they did
 * with the golden: the calls to the collaborators with their complete arguments, the output (states, codes, descriptions, counter, messages), the columns of the UPDATE to CRT and the
 * pojo that Tesoreria / Spot receive. A case tagged {@code T2-costura:excepcion} is a case in which the legacy throws INSIDE F-C / F-D: the unit must throw the same class (the message
 * for a plain Exception) after making the same calls.
 *
 * <p>RED BEFORE CODE: the production units do not exist yet, so every case fails with "T2 not implemented: the production class ... does not exist yet". A golden that is missing FAILS
 * the case; a run in which no comparison executed FAILS ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CerT2EquivalenciaTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final AtomicInteger COMPLETOS = new AtomicInteger();
    private static final AtomicInteger EXCEPCIONES = new AtomicInteger();

    /** (id, the rules the case pins: its `ref`): the display name of every case NAMES its rules (FM-CER-nn, D-F3B-nn) */
    static Stream<Arguments> casos() {
        List<String> ids = CasoCerT2.casos();
        assertTrue(ids.size() >= 150, "the FMM-CER T2 matrix has at least 150 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoCerT2.refs(id))));
    }

    @Test
    @Order(0)
    @DisplayName("FMM-CER T2: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoCerT2.casos()) {
            if (!Files.exists(CasoCerT2.directorio().resolve(id).resolve("expected.json"))) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_cer_t2.sh): " + sin);
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FMM-CER T2 equivalence with the recorded legacy (F-C: FM-CER-21..28, F-D: FM-CER-31..41)")
    void equivalente(String id, String reglas) throws Exception {
        JsonObject golden = CasoCerT2.golden(id);
        CasoCerT2.Corrida corrida = CasoCerT2.ejecutar(id);
        CerCosturaT2.comparar(id, golden, corrida);
        if (CasoCerT2.modo(id) == CasoCerT2.Modo.COMPLETO) COMPLETOS.incrementAndGet();
        else EXCEPCIONES.incrementAndGet();
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FMM-CER T2: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoCerT2.casos().size() + " (complete runs of the legacy: " + COMPLETOS.get()
                + "; an exception of the legacy inside F-C / F-D or in a later front: " + EXCEPCIONES.get() + ")");
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoCerT2.casos().size(), EJECUTADOS.get(), "a case was not compared");
        assertTrue(COMPLETOS.get() > 0 && EXCEPCIONES.get() > 0, "both kinds of case must have run");
    }
}
