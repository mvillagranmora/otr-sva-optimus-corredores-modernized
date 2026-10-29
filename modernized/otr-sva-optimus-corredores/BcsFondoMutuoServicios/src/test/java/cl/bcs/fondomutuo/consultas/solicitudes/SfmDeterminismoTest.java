package cl.bcs.fondomutuo.consultas.solicitudes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.solicitudes.CasoSfm.Perfil;

/**
 * Plan F2 section 7 "Determinismo": the legacy reads the view with {@code queryParallel} and computes the global state, the amounts and every filter on
 * {@code parallelStream()}; the oracle recorded every case N = 20 times and only wrote goldens that came out identical (class of the exception for the cases whose
 * message text is order dependent). The new code is run N = 20 times (system property {@code sfm.corridas}, default 20) on every case that seeds at least
 * {@code sfm.minFilas} (default 4) rows of the view (SFM and WS), and EACH run must equal the golden, with the output list in the same order (FM-SFM-04/05/06).
 */
class SfmDeterminismoTest {

    private static final String INSERT_VISTA = "INSERT INTO \"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesFFMM\"";

    static Stream<String> casosConVariasFilas() {
        int minimo = Integer.getInteger("sfm.minFilas", 4);
        List<String> ids = CasoSfm.casos().stream().filter(id -> CasoSfm.seed(id).split(java.util.regex.Pattern.quote(INSERT_VISTA), -1).length - 1 >= minimo).toList();
        assertTrue(ids.size() >= 20, "the determinism matrix has at least 20 multi-row cases, found " + ids.size());
        assertTrue(ids.stream().anyMatch(id -> CasoSfm.slug(id).equals("determinismo-grupo-de-doscientas")), "the 200-row group is in the matrix");
        assertTrue(ids.stream().anyMatch(id -> CasoSfm.slug(id).equals("ws-determinismo-sesenta-y-diez-filas")), "the 60+10 row WS set is in the matrix");
        return ids.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConVariasFilas")
    @DisplayName("FMM-SFM determinism FM-SFM-04/05/06: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id) throws Exception {
        int n = Integer.getInteger("sfm.corridas", 20);
        JsonObject golden = CasoSfm.golden(id);
        for (int i = 1; i <= n; i++) {
            SfmEquivalenciaTest.comparar(id + " (run " + i + "/" + n + ")", golden, CasoSfm.ejecutar(id, Perfil.ABIERTA));
        }
    }
}
