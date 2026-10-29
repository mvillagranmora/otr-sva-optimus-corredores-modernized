package cl.bcs.fondomutuo.consultas.dolar;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.dolar.CasoVdo.Perfil;

/**
 * Plan F2 section 7 "Determinismo" (VDO): the legacy deduplicates the dates and builds the output with {@code stream()} (VDO:121-181) and keys the rates with List2Hash. The oracle recorded every case N = 20 times and only wrote goldens that came out identical. The new code is
 * run N = 20 times (system property {@code vdo.corridas}, default 20) on every case with two or more series in the request and EACH run must equal the golden, with the output list in the
 * same order (no ORDER BY in the SQL: the order is the one of the engine, the same on both sides).
 */
class VdoDeterminismoTest {

    static Stream<String> casosConVariasFilas() {
        List<String> ids = CasoVdo.casos().stream().filter(id -> {
            JsonObject entrada = CasoVdo.caso(id).getAsJsonObject("entrada");
            return entrada.has("p_Series") && entrada.get("p_Series").isJsonArray() && entrada.getAsJsonArray("p_Series").size() >= 2;
        }).toList();
        assertTrue(ids.size() >= 15, "the determinism matrix of VDO has at least 15 cases with two or more series in the request, found " + ids.size());
        return ids.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConVariasFilas")
    @DisplayName("FMM-VDO determinism: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id) throws Exception {
        int n = Integer.getInteger("vdo.corridas", 20);
        JsonObject golden = CasoVdo.golden(id);
        for (int i = 1; i <= n; i++) {
            VdoEquivalenciaTest.comparar(id + " (run " + i + "/" + n + ")", golden, CasoVdo.ejecutar(id, Perfil.ABIERTA));
        }
    }
}
