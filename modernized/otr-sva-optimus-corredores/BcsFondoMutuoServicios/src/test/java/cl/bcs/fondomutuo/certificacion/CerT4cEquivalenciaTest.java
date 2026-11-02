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
 * F3 W3 T4-C, the JavaCall handler of {@code CertificarSolicitudAporteRescate} against the T5 goldens (recorded from the UNMODIFIED legacy; no new oracle is needed: the handler adds only the JSON
 * codec). ALL the cases of {@code fondomutuo-cer/cases-t5} run THROUGH the handler with its list open: the input JSON of the case goes through {@code call(null, json)} (the plain Gson in, the facade of T5 over
 * the real JDBC adapters and the scripted ports of the case, the plain Gson out) and the output JSON is parsed back; {@link CerCosturaT5} compares the WHOLE run with the golden exactly as
 * {@code CerT5EquivalenciaTest} does (output, tables before and after the host decides, every collaborator call, sequences, session, exception class; the depth-2 recursions keep their fail-closed
 * comparison). The harness option is {@code CasoCerT5.Opciones.porHandler()}; its default behaviour is unchanged.
 *
 * <p>RED BEFORE CODE: the handler does not exist yet, so every case fails with "T4-C not implemented: the production class ... does not exist yet". A golden that is missing FAILS the case; a run in which
 * no comparison executed FAILS ("equivalence cases executed: 0" is not proven). With {@code -Dcer.autocomprobacion=true} the run IS the golden (no production code runs): the proof that the
 * comparison describes what the legacy did; it is a self-check of the harness and proves nothing about the handler.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CerT4cEquivalenciaTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final AtomicInteger COMPLETOS = new AtomicInteger();
    private static final AtomicInteger EXCEPCIONES = new AtomicInteger();
    private static final AtomicInteger PROFUNDIDAD = new AtomicInteger();

    /** (id, the rules the case pins: its `ref`): the display name of every case NAMES its rules (FM-CER-nn, D-F3B-nn) */
    static Stream<Arguments> casos() {
        List<String> ids = CasoCerT5.casos();
        assertTrue(ids.size() >= 200, "the FMM-CER T5 matrix has at least 200 case directories (206 at the time of writing), found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoCerT5.refs(id))));
    }

    @Test
    @Order(0)
    @DisplayName("T4-C: every T5 case has its recorded golden (a missing golden is a failure, never a skip)")
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
    @DisplayName("T4-C equivalence THROUGH THE HANDLER with the recorded legacy, WHOLE run (FM-CER-61..71, RULE-076)")
    void equivalentePorElHandler(String id, String reglas) throws Exception {
        JsonObject golden = CasoCerT5.golden(id);
        CasoCerT5.Corrida corrida = Boolean.getBoolean("cer.autocomprobacion") ? CasoCerT5.desdeGolden(id)
                : CasoCerT5.ejecutarReal(id, CasoCerT5.Opciones.abiertas().porHandler());
        CerCosturaT5.comparar(id, golden, corrida);
        if (CasoCerT5.conTag(id, "T5-profundidad")) PROFUNDIDAD.incrementAndGet();
        else if (CasoCerT5.esExcepcion(id)) EXCEPCIONES.incrementAndGet();
        else COMPLETOS.incrementAndGet();
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("T4-C: every case was compared through the handler (equivalence cases executed: N of M)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoCerT5.casos().size() + " (complete runs of the legacy compared as a whole: " + COMPLETOS.get()
                + "; runs in which the legacy throws: " + EXCEPCIONES.get() + "; depth-2 recursions compared with the fail-closed guard: " + PROFUNDIDAD.get() + ")"
                + (Boolean.getBoolean("cer.autocomprobacion") ? " [SELF-CHECK: the runs were the goldens, the handler was NOT exercised]" : ""));
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoCerT5.casos().size(), EJECUTADOS.get(), "a case was not compared");
        assertTrue(COMPLETOS.get() > 0 && EXCEPCIONES.get() > 0 && PROFUNDIDAD.get() > 0, "the three kinds of case must have run");
    }
}
