package cl.bcs.fondomutuo.consultas.reinversion;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.reinversion.CasoFmr.Perfil;

/**
 * Plan F2 section 7 "Determinismo" (FMR): the legacy builds the list from a SELECT without ORDER BY and copies it row by row (FMR:102-117). The oracle recorded every case N = 20 times and only wrote goldens that came out identical. The new code is
 * run N = 20 times (system property {@code fmr.corridas}, default 20) on every case whose golden list has two or more rows and EACH run must equal the golden, with the output list in the
 * same order (no ORDER BY in the SQL: the order is the one of the engine, the same on both sides).
 */
class FmrDeterminismoTest {

    static Stream<String> casosConVariasFilas() {
        List<String> ids = CasoFmr.casos().stream().filter(id -> {
            JsonObject s = CasoFmr.golden(id).has("salida") ? CasoFmr.golden(id).getAsJsonObject("salida") : null;
            return s != null && s.has("P_OUT_LISTA") && s.getAsJsonArray("P_OUT_LISTA").size() >= 2;
        }).toList();
        assertTrue(ids.size() >= 30, "the determinism matrix of FMR has at least 30 cases whose golden list has two or more rows, found " + ids.size());
        return ids.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConVariasFilas")
    @DisplayName("FMM-FMR determinism: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id) throws Exception {
        int n = Integer.getInteger("fmr.corridas", 20);
        JsonObject golden = CasoFmr.golden(id);
        for (int i = 1; i <= n; i++) {
            FmrEquivalenciaTest.comparar(id + " (run " + i + "/" + n + ")", golden, CasoFmr.ejecutar(id, Perfil.ABIERTA));
        }
    }
}
