package cl.bcs.fondomutuo.liquidacion;

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
 * FMM-LIQ (F3 W3 T6-B), cards FM-LIQ-01..20 of RULES_FONDOMUTUO_F3_B.md 8.3 and the quirks of the class: the PRODUCTION chain (JavaCall handler, facade, REAL JDBC adapter of the SQL over the stand-in H2, REAL
 * adapters of the parameters and the dictionary, the three collaborators outside the module as fakes at the port seam) against the goldens RECORDED from the UNMODIFIED legacy class
 * {@code LiquidacionSolicitudAporteRescate.call} (record_liq.sh: H2, registered doubles for Tesoreria, CuentaInversion and AgregarLog, 20 runs per case; an unstable case gets no golden).
 *
 * <p>Every case is a COMPLETE run of both sides and {@link CosturaLiq} compares everything the harness can see: the output with the legacy recount of the errors, the table of the solicitudes before and after the
 * host decides, EVERY call to the collaborators with its complete arguments, the sequences, the host decision; an exception of the legacy is an exception of the new code (same class). RULE-076: no credential, no real
 * id, H2 only.
 *
 * <p>RED BEFORE CODE: the production classes do not exist yet, so every case fails with "T6-B not implemented: the production class ... does not exist yet". A golden that is missing FAILS the case; a run in which
 * no comparison executed FAILS ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LiqEquivalenciaTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final AtomicInteger COMPLETOS = new AtomicInteger();
    private static final AtomicInteger EXCEPCIONES = new AtomicInteger();

    /** (id, the rules the case pins: its `ref`): the display name of every case NAMES its rules (FM-LIQ-nn and the quirk tags) */
    static Stream<Arguments> casos() {
        List<String> ids = CasoLiq.casos();
        assertTrue(ids.size() >= 80, "the FMM-LIQ matrix has at least 80 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoLiq.refs(id))));
    }

    @Test
    @Order(0)
    @DisplayName("FMM-LIQ: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoLiq.casos()) {
            if (!Files.exists(CasoLiq.directorio().resolve(id).resolve("expected.json"))) sin.add(id);
            if (!Files.exists(CasoLiq.directorio().resolve(id).resolve("seed.sql"))) sin.add(id + " (seed.sql)");
        }
        assertTrue(sin.isEmpty(), "cases without golden or seed (record them with legacy-oracle/record_liq.sh and sync_liq.py): " + sin);
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FMM-LIQ equivalence with the recorded legacy, WHOLE run (FM-LIQ-01..20)")
    void equivalente(String id, String reglas) throws Exception {
        JsonObject golden = CasoLiq.golden(id);
        CasoLiq.Corrida corrida = CasoLiq.ejecutarReal(id);
        CosturaLiq.comparar(id, golden, corrida);
        if (CasoLiq.esExcepcion(id)) EXCEPCIONES.incrementAndGet();
        else COMPLETOS.incrementAndGet();
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FMM-LIQ: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoLiq.casos().size() + " (complete runs of the legacy compared as a whole: " + COMPLETOS.get()
                + "; runs in which the legacy throws: " + EXCEPCIONES.get() + ")");
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven (while T6-B is not implemented every case fails before it compares anything)");
        assertEquals(CasoLiq.casos().size(), EJECUTADOS.get(), "a case was not compared");
        assertTrue(COMPLETOS.get() > 0 && EXCEPCIONES.get() > 0, "both kinds of case must have run");
    }
}
