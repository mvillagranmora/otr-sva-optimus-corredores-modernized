package cl.bcs.fondomutuo.certificacionporfiltros;

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

import cl.bcs.fondomutuo.certificacion.CasoCerPf;
import cl.bcs.fondomutuo.certificacion.CerCosturaPf;

/**
 * FMM-CERPF (F3 W3 T6-A), cards FM-CXF-01 (contract), 02 (selection by filters), 03 (several filters, never de-duplicated), 04 (no filters or no rows), 05 (header copied, output copied) of
 * RULES_FONDOMUTUO_F3_B.md 8.2, and the matrix FM-CXF-C01..C05 (11.2): the PRODUCTION in-process port of {@code CertificarPorFiltrosSolicitudAporteRescate} (its REAL read-only JDBC adapter of the select
 * over the stand-in H2, the REAL production facade of Certificar behind it, the typed ports of Certificar answered by the scripted collaborators of the case) against the goldens RECORDED from the
 * unmodified legacy class calling the REAL legacy Certificar (record_cerpf.sh: H2, same registered doubles, 20 runs per case; an unstable case gets no golden).
 *
 * <p>Every case is a COMPLETE run of both sides and {@link CerCosturaPf} compares everything the harness can see: the output, the tables before and after the host decides, the complete entry the class
 * built for Certificar (header, ids in the order of the filters, duplicates), EVERY collaborator call of Certificar, the sequences, the host decision, the session; an exception of the legacy is an
 * exception of the new code (same class). RULE-076: no credential, no real id, H2 only.
 *
 * <p>RED BEFORE CODE: the production port does not exist yet, so every case fails with "T6-A not implemented: the production class ... does not exist yet". A golden that is missing FAILS the case; a
 * run in which no comparison executed FAILS ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CerPfEquivalenciaTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final AtomicInteger COMPLETOS = new AtomicInteger();
    private static final AtomicInteger EXCEPCIONES = new AtomicInteger();

    /** (id, the rules the case pins: its `ref`): the display name of every case NAMES its rules (FM-CXF-nn, matriz:Cnn) */
    static Stream<Arguments> casos() {
        List<String> ids = CasoCerPf.casos();
        assertTrue(ids.size() >= 80, "the FMM-CERPF matrix has at least 80 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoCerPf.refs(id))));
    }

    @Test
    @Order(0)
    @DisplayName("FMM-CERPF: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoCerPf.casos()) {
            if (!Files.exists(CasoCerPf.directorio().resolve(id).resolve("expected.json"))) sin.add(id);
            if (!Files.exists(CasoCerPf.directorio().resolve(id).resolve("seed.sql"))) sin.add(id + " (seed.sql)");
        }
        assertTrue(sin.isEmpty(), "cases without golden or seed (record them with legacy-oracle/record_cerpf.sh and sync_cerpf.py): " + sin);
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FMM-CERPF equivalence with the recorded legacy, WHOLE run (FM-CXF-01..05)")
    void equivalente(String id, String reglas) throws Exception {
        JsonObject golden = CasoCerPf.golden(id);
        CasoCerPf.Corrida corrida = CasoCerPf.ejecutarReal(id, CasoCerPf.Opciones.abiertas());
        CerCosturaPf.comparar(id, golden, corrida);
        if (CasoCerPf.esExcepcion(id)) EXCEPCIONES.incrementAndGet();
        else COMPLETOS.incrementAndGet();
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FMM-CERPF: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoCerPf.casos().size() + " (complete runs of the legacy compared as a whole: " + COMPLETOS.get()
                + "; runs in which the legacy throws: " + EXCEPCIONES.get() + ")");
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoCerPf.casos().size(), EJECUTADOS.get(), "a case was not compared");
        assertTrue(COMPLETOS.get() > 0 && EXCEPCIONES.get() > 0, "both kinds of case must have run");
    }
}
