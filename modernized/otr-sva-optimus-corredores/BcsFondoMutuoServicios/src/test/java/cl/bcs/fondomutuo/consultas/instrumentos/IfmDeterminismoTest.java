package cl.bcs.fondomutuo.consultas.instrumentos;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.instrumentos.CasoIfm.Perfil;

/**
 * Plan F2 section 7 "Determinismo" (IFM): the legacy fills ListaRegimen of every series on {@code parallelStream()} (IFM:244-259) and reads the series ids on another. The oracle recorded every case N = 20 times and only wrote goldens that came out identical. The new code is
 * run N = 20 times (system property {@code ifm.corridas}, default 20) on every case whose golden has two or more series and EACH run must equal the golden, with the output list in the
 * same order (no ORDER BY in the SQL: the order is the one of the engine, the same on both sides).
 */
class IfmDeterminismoTest {

    static Stream<String> casosConVariasFilas() {
        List<String> ids = CasoIfm.casos().stream().filter(id -> {
            JsonObject s = CasoIfm.golden(id).has("salida") ? CasoIfm.golden(id).getAsJsonObject("salida") : null;
            return s != null && s.has("FondosMutuos") && s.getAsJsonArray("FondosMutuos").size() >= 2;
        }).toList();
        assertTrue(ids.size() >= 30, "the determinism matrix of IFM has at least 30 cases whose golden has two or more series, found " + ids.size());
        return ids.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConVariasFilas")
    @DisplayName("FMM-IFM determinism: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id) throws Exception {
        int n = Integer.getInteger("ifm.corridas", 20);
        JsonObject golden = CasoIfm.golden(id);
        for (int i = 1; i <= n; i++) {
            IfmEquivalenciaTest.comparar(id + " (run " + i + "/" + n + ")", golden, CasoIfm.ejecutar(id, Perfil.ABIERTA));
        }
    }
}
