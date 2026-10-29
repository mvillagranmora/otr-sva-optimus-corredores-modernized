package cl.bcs.fondomutuo.valorcuota;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.valorcuota.CasoCvc.Perfil;

/**
 * Plan F3 section 4 "N = 20 corridas por caso donde hay List2Hash / toMap / groupingBy": CargarValorCuota groups the rows by serie in a {@code HashMap} to pick the vigente one
 * and builds several {@code Collectors.toMap} (series, ISIN, vigentes, previous-day). The oracle recorded EVERY case 20 times and only wrote goldens that came out identical
 * (see {@code determinism-cvc.txt}). The new code is run N = 20 times (system property {@code cvc.corridas}, default 20) on every case that has more than one row, an ISIN
 * list, an earlier request or a seed with more than one stored value, and EACH run must equal the golden: the order of the writes, the ids of the sequence and the order of the
 * messages must not depend on hash order or on the run.
 */
class CvcDeterminismoTest {

    static Stream<String> casosConVariasFilas() {
        List<String> ids = CasoCvc.casos().stream().filter(id -> {
            JsonObject caso = CasoCvc.caso(id);
            int filas = caso.getAsJsonObject("entrada").has("p_ValorCuota") && caso.getAsJsonObject("entrada").get("p_ValorCuota").isJsonArray()
                    ? caso.getAsJsonObject("entrada").getAsJsonArray("p_ValorCuota").size() : 0;
            boolean isin = caso.getAsJsonObject("entrada").has("p_AllFunds") && !caso.getAsJsonObject("entrada").get("p_AllFunds").isJsonNull()
                    && "S".equalsIgnoreCase(caso.getAsJsonObject("entrada").get("p_AllFunds").getAsString());
            return filas >= 2 || isin || caso.has("entradasPrevias");
        }).toList();
        assertTrue(ids.size() >= 40, "the determinism matrix has at least 40 cases, found " + ids.size());
        return ids.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConVariasFilas")
    @DisplayName("FMM-CVC determinism FM-CVC-03/09/10/12: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id) throws Exception {
        int n = Integer.getInteger("cvc.corridas", 20);
        JsonObject golden = CasoCvc.golden(id);
        for (int i = 1; i <= n; i++) {
            CvcEquivalenciaTest.comparar(id + " (run " + i + "/" + n + ")", golden, CasoCvc.ejecutar(id, Perfil.ABIERTA));
        }
    }
}
