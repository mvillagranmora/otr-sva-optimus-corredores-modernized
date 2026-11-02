package cl.bcs.fondomutuo.certificacionporfiltros;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.certificacion.CasoCerPf;
import cl.bcs.fondomutuo.certificacion.CerCosturaPf;

/**
 * Plan F3 section 4 "N = 20 corridas por caso donde hay List2Hash / toMap / parallelStream": the legacy runs a SELECT DISTINCT per filter (no ORDER BY: the order of the rows is the engine's), concatenates the
 * rows in the order of the filters and hands them to Certificar, which partitions them with parallel streams and hash maps. The RECORDED goldens are only the ones that came out identical in 20 runs of
 * the oracle ({@code determinism-cerpf.txt}: 86 cases, 1720 runs, 0 unstable). The new port is run N = 20 times (system property {@code cer.corridas}, default 20) on every case with more than one
 * filter or more than one selected solicitud, and EACH run must equal the golden as {@link CerPfEquivalenciaTest} compares it: the order of the ids that reach Certificar (filter order, then the
 * engine's order inside a filter) and everything downstream must not depend on the run. RULE-076: H2 only.
 */
class CerPfDeterminismoTest {

    static Stream<Arguments> casosConVariosFiltrosOSolicitudes() {
        List<String> ids = CasoCerPf.casos().stream().filter(id -> {
            JsonObject entrada = CasoCerPf.caso(id).getAsJsonObject("entrada");
            JsonArray filtros = entrada.has("p_Filtros") && entrada.get("p_Filtros").isJsonArray() ? entrada.getAsJsonArray("p_Filtros") : new JsonArray();
            return filtros.size() >= 2 || CasoCerPf.conTag(id, "PF-seleccion:varias");
        }).toList();
        assertTrue(ids.size() >= 30, "the T6-A determinism matrix has at least 30 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoCerPf.refs(id))));
    }

    @Test
    @DisplayName("FMM-CERPF determinism: the oracle report says 20 runs per case and no unstable case (the goldens exist only for stable cases)")
    void elInformeDelOraculoDiceVeinteCorridasSinInestables() throws Exception {
        List<String> lineas = Files.readAllLines(CasoCerPf.recursosPf().resolve("determinism-cerpf.txt"));
        String primera = lineas.get(0);
        assertTrue(primera.contains("repeats per case: 20"), primera);
        assertTrue(primera.contains("hard: 0"), primera);
        int ejecutados = Integer.parseInt(primera.replaceAll(".*cases executed: (\\d+),.*", "$1"));
        assertEquals(CasoCerPf.casos().size(), ejecutados, "the report is of another matrix: record again");
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casosConVariosFiltrosOSolicitudes")
    @DisplayName("FMM-CERPF determinism FM-CXF-02/03: N runs of the new port, each equal to the golden")
    void veinteCorridas(String id, String reglas) throws Exception {
        int n = Integer.getInteger("cer.corridas", 20);
        JsonObject golden = CasoCerPf.golden(id);
        for (int i = 1; i <= n; i++) {
            CasoCerPf.Corrida c = CasoCerPf.ejecutarReal(id, CasoCerPf.Opciones.abiertas());
            try {
                CerCosturaPf.comparar(id, golden, c);
            } catch (AssertionError e) {
                throw new AssertionError("run " + i + " of " + n + " differs from the golden: " + e.getMessage(), e);
            }
        }
    }
}
