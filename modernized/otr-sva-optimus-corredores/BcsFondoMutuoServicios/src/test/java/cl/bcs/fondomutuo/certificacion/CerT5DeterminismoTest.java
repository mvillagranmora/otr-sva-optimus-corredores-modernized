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
 * Plan F3 section 4 "N = 20 corridas por caso donde hay List2Hash / toMap / parallelStream": the legacy builds its collaborator entries, its output, the log and the notifications over lists that
 * come from {@code parallelStream} and {@code HashMap}s (partition into aportes and rescates, custody, the cuenta, Tesoreria, the OWP list, the numbering of AR), and the RECORDED goldens are only
 * the ones that came out identical in 20 runs of the oracle ({@code determinism-cer-t5.txt}). The new facade is run N = 20 times (system property {@code cer.corridas}, default 20) on every T5
 * case with more than one solicitud, and EACH run must equal the golden as {@link CerT5EquivalenciaTest} compares it: the order of the rows of the output, of the ids of the notifications, of the
 * entries of OWP, of the lines of the log and of the collaborator calls must not depend on the run. RULE-076: H2 only.
 */
class CerT5DeterminismoTest {

    static Stream<Arguments> casosConVariasSolicitudes() {
        List<String> ids = CasoCerT5.casos().stream().filter(id -> {
            JsonObject entrada = CasoCerT5.caso(id).getAsJsonObject("entrada");
            JsonArray lista = entrada.has("p_Solicitudes") && entrada.get("p_Solicitudes").isJsonArray() ? entrada.getAsJsonArray("p_Solicitudes") : new JsonArray();
            return lista.size() >= 2;
        }).toList();
        assertTrue(ids.size() >= 30, "the T5 determinism matrix has at least 30 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoCerT5.refs(id))));
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casosConVariasSolicitudes")
    @DisplayName("FMM-CER T5 determinism FM-CER-62/65/67/72: N runs of the new facade, each equal to the golden")
    void veinteCorridas(String id, String reglas) throws Exception {
        int n = Integer.getInteger("cer.corridas", 20);
        JsonObject golden = CasoCerT5.golden(id);
        for (int i = 1; i <= n; i++) {
            CasoCerT5.Corrida c = CasoCerT5.ejecutarReal(id, CasoCerT5.Opciones.abiertas());
            try {
                CerCosturaT5.comparar(id, golden, c);
            } catch (AssertionError e) {
                throw new AssertionError("run " + i + " of " + n + " differs from the golden: " + e.getMessage(), e);
            }
        }
    }
}
