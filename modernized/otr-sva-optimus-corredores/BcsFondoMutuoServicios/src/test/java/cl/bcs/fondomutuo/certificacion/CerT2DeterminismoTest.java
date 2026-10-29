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
 * Plan F3 section 4 "N = 20 corridas por caso donde hay List2Hash / toMap / parallelStream": the legacy builds the lists of F-C and F-D with {@code parallelStream} (custody calculation,
 * partition into rescates with / without regimen, commission, retention, retirement), {@code HashMap}s and {@code Collectors.groupingBy}, and the RECORDED goldens are only the ones that came
 * out identical in 20 runs of the oracle ({@code determinism-cer-t2.txt}). The new code is run N = 20 times (system property {@code cer.corridas}, default 20) on every T2 case with more
 * than one solicitud, and EACH run must equal the golden as {@link CerT2EquivalenciaTest} compares it: the order of the lists, of the collaborator calls, of the codes and of the messages must
 * not depend on the run (D-F3B-06: the legacy shares one JDBC connection among threads to write the descriptions; the new code must not).
 */
class CerT2DeterminismoTest {

    static Stream<Arguments> casosConVariasSolicitudes() {
        List<String> ids = CasoCerT2.casos().stream().filter(id -> {
            JsonObject entrada = CasoCerT2.caso(id).getAsJsonObject("entrada");
            JsonArray lista = entrada.has("p_Solicitudes") && entrada.get("p_Solicitudes").isJsonArray() ? entrada.getAsJsonArray("p_Solicitudes") : new JsonArray();
            return lista.size() >= 2;
        }).toList();
        assertTrue(ids.size() >= 40, "the T2 determinism matrix has at least 40 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoCerT2.refs(id))));
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casosConVariasSolicitudes")
    @DisplayName("FMM-CER T2 determinism FM-CER-19/31/37/39/41: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id, String reglas) throws Exception {
        int n = Integer.getInteger("cer.corridas", 20);
        JsonObject golden = CasoCerT2.golden(id);
        for (int i = 1; i <= n; i++) {
            CasoCerT2.Corrida c = CasoCerT2.ejecutar(id);
            try {
                CerCosturaT2.comparar(id, golden, c);
            } catch (AssertionError e) {
                throw new AssertionError("run " + i + " of " + n + " differs from the golden: " + e.getMessage(), e);
            }
        }
    }
}
