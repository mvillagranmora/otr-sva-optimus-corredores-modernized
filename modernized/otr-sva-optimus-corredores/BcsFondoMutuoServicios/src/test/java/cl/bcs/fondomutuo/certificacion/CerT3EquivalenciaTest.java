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
 * FMM-CER (F3 W3 T3), cards FM-CER-29, 30, 42, 43 (front F-E, cuenta de inversion de aportes y rescates), FM-CER-44..47 (front F-F, Tesoreria), FM-CER-59, 60 (front F-H, Spot) and the
 * defects D-F3B-01/03/07/08/09/14/17/41/42, D-NOR-9 (RULES_FONDOMUTUO_F3_B.md 8.1 and 9): the units of F-E, F-F and F-H of the new {@code CertificarSolicitudAporteRescate} (REAL, behind the
 * ports of the collaborators that are not ported: {@code CrearCargoAbono}, and the typed {@code ProcesosExternos.TesoreriaPort} / {@code SpotPort} of the module, answered by the case
 * script) against the goldens RECORDED from the unmodified legacy by record_cer_t3.sh on the same H2 and the same scripted collaborators.
 *
 * <p>Every case runs F-A and F-B of T1 for real (REAL JDBC adapters), then the NINE steps of F-C..F-H ({@link ApiT3.Pasos}: the five of T2, REAL, plus the cuenta de inversion of the
 * aportes and of the rescates, Tesoreria and Spot) at the seam, and {@link CerCosturaT3} compares what they did with the golden: the calls to the collaborators with their COMPLETE arguments
 * (every line of the cuenta, every field of the pojo of Tesoreria and Spot, the payment forms), the output (states, codes, counter, messages) and the columns of the UPDATE to CRT. A case
 * tagged {@code T3-costura:excepcion} is a case in which the legacy throws INSIDE F-C..F-H: the unit must throw the same class (the message for a plain Exception) after making the same
 * calls.
 *
 * <p>RED BEFORE CODE: the production units of T3 do not exist yet, so every case fails with "T3 not implemented: the production class ... does not exist yet". A golden that is missing
 * FAILS the case; a run in which no comparison executed FAILS ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CerT3EquivalenciaTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final AtomicInteger COMPLETOS = new AtomicInteger();
    private static final AtomicInteger EXCEPCIONES = new AtomicInteger();

    /** (id, the rules the case pins: its `ref`): the display name of every case NAMES its rules (FM-CER-nn, D-F3B-nn) */
    static Stream<Arguments> casos() {
        List<String> ids = CasoCerT3.casos();
        assertTrue(ids.size() >= 150, "the FMM-CER T3 matrix has at least 150 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoCerT3.refs(id))));
    }

    @Test
    @Order(0)
    @DisplayName("FMM-CER T3: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoCerT3.casos()) {
            if (!Files.exists(CasoCerT3.directorio().resolve(id).resolve("expected.json"))) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_cer_t3.sh): " + sin);
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FMM-CER T3 equivalence with the recorded legacy (F-E: FM-CER-29, 30, 42, 43; F-F: FM-CER-44..47; F-H: FM-CER-59, 60)")
    void equivalente(String id, String reglas) throws Exception {
        JsonObject golden = CasoCerT3.golden(id);
        CasoCerT3.Corrida corrida = CasoCerT3.ejecutar(id);
        CerCosturaT3.comparar(id, golden, corrida);
        if (CasoCerT3.modo(id) == CasoCerT3.Modo.COMPLETO) COMPLETOS.incrementAndGet();
        else EXCEPCIONES.incrementAndGet();
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FMM-CER T3: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoCerT3.casos().size() + " (complete runs of the legacy: " + COMPLETOS.get()
                + "; an exception of the legacy inside F-C..F-H or in a later front: " + EXCEPCIONES.get() + ")");
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoCerT3.casos().size(), EJECUTADOS.get(), "a case was not compared");
        assertTrue(COMPLETOS.get() > 0 && EXCEPCIONES.get() > 0, "both kinds of case must have run");
    }
}
