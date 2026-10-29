package cl.bcs.fondomutuo.consultas.detalle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.detalle.CasoDet.Perfil;

/**
 * Plan F2 section 7 "Determinismo": the delegate of the legacy values the solicitudes, maps the output and (for ING / PEN) builds a {@code toMap} on {@code parallelStream()};
 * the oracle recorded every case N = 20 times and only wrote goldens that came out identical (class of the exception for the one case whose message text is order dependent:
 * the {@code toMap} duplicate key). The new code is run N = 20 times (system property {@code det.corridas}, default 20) on every case that reaches the valuation (it scripts
 * collaborators) or returns more than one row, and EACH run must equal the golden.
 */
class DetDeterminismoTest {

    static Stream<String> casosConValorizacionOVariasFilas() {
        List<String> ids = CasoDet.casos().stream().filter(id -> {
            JsonObject caso = CasoDet.caso(id);
            JsonObject golden = CasoDet.golden(id);
            boolean valoriza = caso.has("colaboradores");
            boolean variasFilas = golden.has("salida") && golden.getAsJsonObject("salida").has("P_OUT_RESULTADO")
                    && golden.getAsJsonObject("salida").getAsJsonArray("P_OUT_RESULTADO").size() >= 2;
            boolean variasSolicitudes = CasoDet.seed(id).split("INSERT INTO \"BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate\"", -1).length - 1 >= 2;
            return valoriza || variasFilas || variasSolicitudes;
        }).toList();
        assertTrue(ids.size() >= 25, "the determinism matrix has at least 25 cases, found " + ids.size());
        return ids.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConValorizacionOVariasFilas")
    @DisplayName("FMM-DET determinism FM-DET-02/04: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id) throws Exception {
        int n = Integer.getInteger("det.corridas", 20);
        JsonObject golden = CasoDet.golden(id);
        for (int i = 1; i <= n; i++) {
            DetEquivalenciaTest.comparar(id + " (run " + i + "/" + n + ")", golden, CasoDet.ejecutar(id, Perfil.ABIERTA));
        }
    }
}
