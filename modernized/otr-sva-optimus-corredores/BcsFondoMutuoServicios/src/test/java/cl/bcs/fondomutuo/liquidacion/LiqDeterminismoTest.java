package cl.bcs.fondomutuo.liquidacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

/**
 * Plan F3 section 4 "N = 20 corridas por caso donde hay List2Hash / toMap / parallelStream": the legacy groups the solicitudes in a {@code HashMap} keyed by {@code Grupo} (hashCode = SUM of the hashes, so the
 * order of the groups is the order of the buckets, FM-LIQ-07), reads the modalities with a {@code parallelStream().collect(toMap)} and walks the groups in that order to assign the folios of the carta. The
 * RECORDED goldens are only the ones that came out identical in 20 runs of the oracle ({@code determinism-liq.txt}). The new code is run N = 20 times (system property {@code liq.corridas}, default 20) on every
 * case whose outcome depends on the order of the groups or on repeated passes over them, and EACH run must equal the golden as {@link LiqEquivalenciaTest} compares it: the folio each group gets, the
 * numbering of the lines and everything downstream must not depend on the run. RULE-076: H2 only.
 */
class LiqDeterminismoTest {

    private static final List<String> ETIQUETAS = List.of("LIQ-quirk:orden-por-hash", "LIQ-quirk:producto-n-por-m", "LIQ-quirk:resultados-duplicados", "LIQ-quirk:carta-lista-completa");

    static Stream<Arguments> casosDondeImportaElOrden() {
        List<String> ids = CasoLiq.casos().stream().filter(id -> ETIQUETAS.stream().anyMatch(t -> CasoLiq.conTag(id, t))).toList();
        assertTrue(ids.size() >= 20, "the T6-B determinism matrix has at least 20 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoLiq.refs(id))));
    }

    @Test
    @DisplayName("FMM-LIQ determinism: the oracle report says 20 runs per case and no unstable case (the goldens exist only for stable cases)")
    void elInformeDelOraculoDiceVeinteCorridasSinInestables() throws Exception {
        List<String> lineas = Files.readAllLines(CasoLiq.recursosLiq().resolve("determinism-liq.txt"));
        String primera = lineas.get(0);
        assertTrue(primera.contains("repeats per case: 20"), primera);
        assertTrue(primera.contains("hard: 0"), primera);
        int ejecutados = Integer.parseInt(primera.replaceAll(".*cases executed: (\\d+),.*", "$1"));
        assertEquals(CasoLiq.casos().size(), ejecutados, "the report is of another matrix: record again");
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casosDondeImportaElOrden")
    @DisplayName("FMM-LIQ determinism FM-LIQ-07/08: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id, String reglas) throws Exception {
        int n = Integer.getInteger("liq.corridas", 20);
        JsonObject golden = CasoLiq.golden(id);
        for (int i = 1; i <= n; i++) {
            CasoLiq.Corrida c = CasoLiq.ejecutarReal(id);
            try {
                CosturaLiq.comparar(id, golden, c);
            } catch (AssertionError e) {
                throw new AssertionError("run " + i + " of " + n + " differs from the golden: " + e.getMessage(), e);
            }
        }
    }
}
