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
 * FMM-CER (F3 W3 T5), cards FM-CER-61 (UPDATE to CRT), 62 (asynchronous notification), 63 (recursion of the switch), 65 (OWP), 66 (logs), 67 (success mark and output), 68 (simulation),
 * 69 (rejections that do not raise the counter), 70 (order of effects) and 71 (exceptions) of RULES_FONDOMUTUO_F3_B.md 8.1, and the defects D-F3B-01/02/16/31/47/49/50, D-NOR-9: the FACADE
 * of the new {@code CertificarSolicitudAporteRescate} (fronts A..J behind one port, REAL JDBC adapters, TYPED ports answered by the scripted collaborators of the case) against the goldens
 * RECORDED from the unmodified legacy by record_cer_t5.sh on the same H2 and the same scripted collaborators.
 *
 * <p>Every case is a COMPLETE run of both sides and {@link CerCosturaT5} compares everything the harness can see: the output, the tables before and after the host decides, EVERY collaborator
 * call with its complete arguments and its answer in order (the child certification inside its record, recursively), the sequences, the host decision and the session; an exception of the legacy
 * is an exception of the new code (same class). The one intended difference, the depth guard, is compared as such ({@code T5-profundidad}). RULE-076: no credential, no real id, H2 only.
 *
 * <p>RED BEFORE CODE: the production facade does not exist yet, so every case fails with "T5 not implemented: the production class ... does not exist yet". A golden that is missing FAILS the
 * case; a run in which no comparison executed FAILS ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CerT5EquivalenciaTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final AtomicInteger COMPLETOS = new AtomicInteger();
    private static final AtomicInteger EXCEPCIONES = new AtomicInteger();
    private static final AtomicInteger PROFUNDIDAD = new AtomicInteger();

    /** (id, the rules the case pins: its `ref`): the display name of every case NAMES its rules (FM-CER-nn, D-F3B-nn) */
    static Stream<Arguments> casos() {
        List<String> ids = CasoCerT5.casos();
        assertTrue(ids.size() >= 150, "the FMM-CER T5 matrix has at least 150 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoCerT5.refs(id))));
    }

    @Test
    @Order(0)
    @DisplayName("FMM-CER T5: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoCerT5.casos()) {
            if (!Files.exists(CasoCerT5.directorio().resolve(id).resolve("expected.json"))) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_cer_t5.sh): " + sin);
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FMM-CER T5 equivalence with the recorded legacy, WHOLE run (FM-CER-61..71, RULE-076)")
    void equivalente(String id, String reglas) throws Exception {
        JsonObject golden = CasoCerT5.golden(id);
        CasoCerT5.Corrida corrida = CasoCerT5.ejecutarReal(id, CasoCerT5.Opciones.abiertas());
        CerCosturaT5.comparar(id, golden, corrida);
        if (CasoCerT5.conTag(id, "T5-profundidad")) PROFUNDIDAD.incrementAndGet();
        else if (CasoCerT5.esExcepcion(id)) EXCEPCIONES.incrementAndGet();
        else COMPLETOS.incrementAndGet();
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FMM-CER T5: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoCerT5.casos().size() + " (complete runs of the legacy compared as a whole: " + COMPLETOS.get()
                + "; runs in which the legacy throws: " + EXCEPCIONES.get() + "; depth-2 recursions compared with the fail-closed guard: " + PROFUNDIDAD.get() + ")");
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoCerT5.casos().size(), EJECUTADOS.get(), "a case was not compared");
        assertTrue(COMPLETOS.get() > 0 && EXCEPCIONES.get() > 0 && PROFUNDIDAD.get() > 0, "the three kinds of case must have run");
    }
}
