package cl.bcs.fondomutuo.consultas.custodiaffmm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

/**
 * Plan F2 section 7 "Determinismo" (FM-CPFF-16, D-F3C-13, C22): the legacy builds the groups in a {@code HashMap<String,Instrumento>} (the output order is its iteration order) from SELECT without
 * ORDER BY. The oracle recorded every case N = 20 times and only wrote goldens that came out identical; the cases that were NOT stable have no golden and are listed in
 * {@code determinism-ffmm.txt}. The new code is run N = 20 times (system property {@code ffmm.corridas}, default 20) on every case with two or more output rows (the ones whose order matters), and
 * EACH run must equal the golden: same rows, same order, same text.
 */
class FfmmDeterminismoTest {

    static Stream<String> casosConVariasFilas() {
        List<String> ids = CasoFfmm.casos().stream().filter(id -> {
            JsonObject golden = CasoFfmm.golden(id);
            return golden.has("salida") && golden.getAsJsonObject("salida").has("p_out_tbl") && golden.getAsJsonObject("salida").getAsJsonArray("p_out_tbl").size() >= 2;
        }).toList();
        assertTrue(ids.size() >= 20, "the determinism matrix has at least 20 cases (two or more rows), found " + ids.size());
        return ids.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConVariasFilas")
    @DisplayName("FM-CPFF determinism FM-CPFF-16 / D-F3C-13: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id) throws Exception {
        int n = Integer.getInteger("ffmm.corridas", 20);
        JsonObject golden = CasoFfmm.golden(id);
        for (int i = 1; i <= n; i++) {
            FfmmEquivalenciaTest.comparar(id + " (run " + i + "/" + n + ")", golden, CasoFfmm.ejecutar(id));
        }
    }

    @Test
    @DisplayName("FM-CPFF determinism: the oracle's own report is present and every case without golden is listed in it (nothing unstable passes by omission)")
    void elReporteDelOraculoListaLosCasosSinGolden() throws Exception {
        java.nio.file.Path reporte = CasoFfmm.recursos().resolve("determinism-ffmm.txt");
        assertTrue(Files.exists(reporte), "determinism-ffmm.txt is missing (run legacy-oracle/sync_ffmm.py)");
        List<String> lineas = Files.readAllLines(reporte, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(lineas.get(0).startsWith("cases executed: "), lineas.get(0));
        assertTrue(lineas.get(0).contains("repeats per case: 20"), "the goldens must come from 20 runs per case: " + lineas.get(0));
        for (String id : CasoFfmm.casos()) {
            assertTrue(Files.exists(CasoFfmm.raizCasos().resolve(id).resolve("expected.json")), id + " has no golden");
        }
        long duros = lineas.stream().skip(1).filter(l -> l.contains(" UNSTABLE: ")).count();
        assertEquals(0L, duros, "an UNSTABLE case of the oracle has a golden here: " + lineas);
    }
}
