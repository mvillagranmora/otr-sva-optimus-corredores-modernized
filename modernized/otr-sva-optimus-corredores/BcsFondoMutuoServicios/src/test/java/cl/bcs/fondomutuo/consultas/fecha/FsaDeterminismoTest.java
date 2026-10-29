package cl.bcs.fondomutuo.consultas.fecha;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.fecha.CasoFsa.Perfil;

/**
 * Plan F2 section 7 "Determinismo": the legacy runs the series map, the entry of dates and the output on {@code parallelStream()}; the oracle recorded every
 * case N = 20 times and only wrote goldens that came out identical (class of the exception for the one case whose message text is order dependent). The new
 * code is run N = 20 times (system property {@code fsa.corridas}, default 20) on every case with more than one request and EACH run must equal the golden,
 * with the output list in the same order.
 */
class FsaDeterminismoTest {

    static Stream<String> casosConVariasSolicitudes() {
        List<String> ids = CasoFsa.casos().stream().filter(id -> {
            JsonObject entrada = CasoFsa.caso(id).getAsJsonObject("entrada");
            return entrada.has("p_SolicitudAporteRescate") && entrada.get("p_SolicitudAporteRescate").isJsonArray()
                    && entrada.getAsJsonArray("p_SolicitudAporteRescate").size() >= 2;
        }).toList();
        assertTrue(ids.size() >= 30, "the determinism matrix has at least 30 multi-request cases, found " + ids.size());
        return ids.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConVariasSolicitudes")
    @DisplayName("FMM-FSA determinism: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id) throws Exception {
        int n = Integer.getInteger("fsa.corridas", 20);
        JsonObject golden = CasoFsa.golden(id);
        for (int i = 1; i <= n; i++) {
            FsaEquivalenciaTest.comparar(id + " (run " + i + "/" + n + ")", golden, CasoFsa.ejecutar(id, Perfil.ABIERTA));
        }
    }
}
