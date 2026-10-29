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
 * Plan F3 section 4 "N = 20 corridas por caso donde hay List2Hash / toMap / parallelStream": the legacy {@code logica} builds its lists with {@code parallelStream} (ordered), {@code HashMap}s
 * and {@code Collectors.toMap}, and the RECORDED goldens are only the ones that came out identical in 20 runs of the oracle ({@code determinism-cer.txt}). The new code is run N = 20
 * times (system property {@code cer.corridas}, default 20) on every case that has more than one solicitud, a request carrying more than one id, or an earlier request in the session,
 * and EACH run must equal the golden in the same way the equivalence test compares it (a TERMINAL case as a whole, a case that continues up to the seam): the order of the lists, of the
 * collaborator calls, of the codes and of the sequence ids must not depend on the run.
 */
class CerDeterminismoTest {

    static Stream<Arguments> casosConVariasSolicitudes() {
        List<String> ids = CasoCer.casos().stream().filter(id -> {
            JsonObject caso = CasoCer.caso(id);
            JsonObject entrada = caso.getAsJsonObject("entrada");
            JsonArray lista = entrada.has("p_Solicitudes") && entrada.get("p_Solicitudes").isJsonArray() ? entrada.getAsJsonArray("p_Solicitudes") : new JsonArray();
            return lista.size() >= 2 || caso.has("entradasPrevias");
        }).toList();
        assertTrue(ids.size() >= 80, "the determinism matrix has at least 80 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoCer.refs(id))));
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casosConVariasSolicitudes")
    @DisplayName("FMM-CER determinism FM-CER-04/19/67/72: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id, String reglas) throws Exception {
        int n = Integer.getInteger("cer.corridas", 20);
        JsonObject golden = CasoCer.golden(id);
        for (int i = 1; i <= n; i++) {
            CasoCer.Corrida c = CasoCer.ejecutar(id);
            try {
                if (CasoCer.modo(id) == CasoCer.Modo.TERMINAL) {
                    CerEquivalenciaTest.compararCompleto(id, golden, c.volcado);
                } else {
                    CerCostura.comparar(id, golden, c);
                }
            } catch (AssertionError e) {
                throw new AssertionError("run " + i + " of " + n + " differs from the golden: " + e.getMessage(), e);
            }
        }
    }
}
