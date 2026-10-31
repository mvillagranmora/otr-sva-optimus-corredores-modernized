package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Plan F3 section 4 "N = 20 corridas por caso donde hay List2Hash / toMap / parallelStream": the legacy builds the cuenta de inversion, Tesoreria and Spot entries over lists that come from
 * {@code parallelStream} and {@code HashMap}s (custody calculation, partition into rescates with / without regimen, the traspaso map, the numbering of the lines of the cuenta), and the
 * RECORDED goldens are only the ones that came out identical in 20 runs of the oracle ({@code determinism-cer-t3.txt}). The new code is run N = 20 times (system property
 * {@code cer.corridas}, default 20) on every T4 case with more than one solicitud, and EACH run must equal the golden as {@link CerT4EquivalenciaTest} compares it: the order of the lines
 * of the cuenta, of the entries of Tesoreria and Spot, of the collaborator calls, of the codes and of the messages must not depend on the run.
 */
class CerT4DeterminismoTest {

    static Stream<Arguments> casosConVariasSolicitudes() {
        List<String> ids = CasoCerT4.casos().stream().filter(id -> {
            JsonObject entrada = CasoCerT4.caso(id).getAsJsonObject("entrada");
            JsonArray lista = entrada.has("p_Solicitudes") && entrada.get("p_Solicitudes").isJsonArray() ? entrada.getAsJsonArray("p_Solicitudes") : new JsonArray();
            return lista.size() >= 2;
        }).toList();
        assertTrue(ids.size() >= 30, "the T4 determinism matrix has at least 30 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoCerT4.refs(id))));
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casosConVariasSolicitudes")
    @DisplayName("FMM-CER T4 determinism FM-CER-19/48/52/57/63: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id, String reglas) throws Exception {
        int n = Integer.getInteger("cer.corridas", 20);
        JsonObject golden = CasoCerT4.golden(id);
        for (int i = 1; i <= n; i++) {
            CasoCerT4.Corrida c = CasoCerT4.ejecutar(id);
            try {
                CerCosturaT4.comparar(id, golden, c);
            } catch (AssertionError e) {
                throw new AssertionError("run " + i + " of " + n + " differs from the golden: " + e.getMessage(), e);
            }
        }
    }
}
