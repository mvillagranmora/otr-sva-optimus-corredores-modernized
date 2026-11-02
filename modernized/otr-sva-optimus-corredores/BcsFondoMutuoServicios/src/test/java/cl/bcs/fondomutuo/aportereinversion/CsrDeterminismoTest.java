package cl.bcs.fondomutuo.aportereinversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.aportereinversion.CasoCsr.Perfil;

/**
 * Plan F3 section 4 "N = 20 corridas por caso donde hay List2Hash / toMap / parallelStream": the legacy builds its lists with {@code HashMap}s and {@code parallelStream} (the amounts of
 * ValidaMontoInversion, the patrimony candidates, the map of the Spot entries by IdInterno, the lists of regimes and portfolios), and the RECORDED goldens are only the ones that came
 * out identical in 20 runs of the oracle ({@code determinism-csr.txt}: cases x 20 runs, 0 unstable). The new code is run N = 20 times (system property {@code csr.corridas}, default
 * 20) on every case with more than one aporte (and fewer than 100: the two lots of 2.000 are compared once by the equivalence test), and EACH run must equal the golden.
 */
class CsrDeterminismoTest {

    static Stream<Arguments> casosConVariosAportes() {
        List<String> ids = CasoCsr.casos().stream().filter(id -> {
            JsonObject entrada = CasoCsr.caso(id).getAsJsonObject("entrada");
            JsonArray lista = entrada.has("p_SolicitudesAporte") && entrada.get("p_SolicitudesAporte").isJsonArray() ? entrada.getAsJsonArray("p_SolicitudesAporte") : new JsonArray();
            return lista.size() >= 2 && lista.size() < 100;
        }).toList();
        assertTrue(ids.size() >= 15, "the CSR determinism matrix has at least 15 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, CasoCsr.ref(id)));
    }

    @Test
    @DisplayName("FMM-CSR: the oracle report determinism-csr.txt: every case x 20 runs of the legacy, 0 unstable")
    void informeDelOraculo() throws Exception {
        var url = CsrDeterminismoTest.class.getClassLoader().getResource("fondomutuo-csr/determinism-csr.txt");
        assertTrue(url != null, "determinism-csr.txt is missing (legacy-oracle/record_csr.sh writes it; sync_csr.py copies it)");
        String linea = Files.readAllLines(java.nio.file.Path.of(url.toURI()), StandardCharsets.UTF_8).get(0);
        Matcher m = Pattern.compile("cases executed: (\\d+), repeats per case: (\\d+), unstable \\(hard \\+ message-only\\): (\\d+), hard: (\\d+)").matcher(linea);
        assertTrue(m.find(), "unexpected report line: " + linea);
        assertEquals(CasoCsr.casos().size(), Integer.parseInt(m.group(1)), "the report covers every case");
        assertEquals(20, Integer.parseInt(m.group(2)), "20 runs per case");
        assertEquals(0, Integer.parseInt(m.group(4)), "0 unstable cases (hard)");
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casosConVariosAportes")
    @DisplayName("FMM-CSR determinism FM-REI-43/47/54/56/63: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id, String reglas) throws Exception {
        int n = Integer.getInteger("csr.corridas", 20);
        JsonObject golden = CasoCsr.golden(id);
        for (int i = 1; i <= n; i++) {
            JsonObject r = CasoCsr.ejecutarReal(id, Perfil.ABIERTAS);
            try {
                CsrCostura.comparar(id, golden, r);
            } catch (AssertionError e) {
                throw new AssertionError("run " + i + " of " + n + " differs from the golden: " + e.getMessage(), e);
            }
        }
    }
}
