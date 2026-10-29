package cl.bcs.fondomutuo.consultas.custodia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.custodia.CasoCpf.Perfil;

/**
 * Plan F2 section 7 "Determinismo" (CPF-C11, FM-CPF-14, D-F3A-04): the legacy assigns the {@code IdInterno} of the solicitudes inside a {@code parallelStream().forEach}, builds the saldos in a
 * {@code HashMap<String,..>} (the output order is its iteration order), values on {@code parallelStream()} and merges with {@code Collectors.toMap}. The oracle recorded every case N = 20
 * times and only wrote goldens that came out identical (class of the exception for the cases whose message text is order dependent: the {@code toMap} duplicate key); the cases that
 * were NOT stable have no golden and are listed in {@code determinism-cpf.txt}. The new code is run N = 20 times (system property {@code cpf.corridas}, default 20) on every case with
 * several saldos, several solicitud rows, or a valuation of solicitudes (the ones the legacy ran in parallel), and EACH run must equal the golden: same rows, same order, same calls.
 */
class CpfDeterminismoTest {

    static Stream<String> casosConVariasFilasOValorizacion() {
        List<String> ids = CasoCpf.casos().stream().filter(id -> {
            JsonObject golden = CasoCpf.golden(id);
            boolean variasFilas = golden.has("salida") && golden.getAsJsonObject("salida").has("P_OUT_TBL") && golden.getAsJsonObject("salida").getAsJsonArray("P_OUT_TBL").size() >= 2;
            boolean variasSolicitudes = CasoCpf.seed(id).split("INSERT INTO \"BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate\"", -1).length - 1 >= 2;
            boolean valoraSolicitudes = golden.getAsJsonArray("colaboradores").size() >= 7;
            boolean mapaDuplicado = golden.has("excepcion") && golden.get("excepcion").getAsString().startsWith("IllegalStateException");
            return variasFilas || variasSolicitudes || valoraSolicitudes || mapaDuplicado;
        }).toList();
        assertTrue(ids.size() >= 40, "the determinism matrix has at least 40 cases, found " + ids.size());
        return ids.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConVariasFilasOValorizacion")
    @DisplayName("FMM-CPF determinism FM-CPF-14 / D-F3A-04: N runs of the new code, each equal to the golden")
    void veinteCorridas(String id) throws Exception {
        int n = Integer.getInteger("cpf.corridas", 20);
        JsonObject golden = CasoCpf.golden(id);
        for (int i = 1; i <= n; i++) {
            CpfEquivalenciaTest.comparar(id + " (run " + i + "/" + n + ")", golden, CasoCpf.ejecutar(id, Perfil.ABIERTA));
        }
    }

    @Test
    @DisplayName("FMM-CPF determinism: the oracle's own report is present and every case without golden is listed in it (nothing unstable passes by omission)")
    void elReporteDelOraculoListaLosCasosSinGolden() throws Exception {
        java.nio.file.Path reporte = CasoCpf.recursos().resolve("determinism-cpf.txt");
        assertTrue(Files.exists(reporte), "determinism-cpf.txt is missing (run legacy-oracle/sync_cpf.py)");
        List<String> lineas = Files.readAllLines(reporte, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(lineas.get(0).startsWith("cases executed: "), lineas.get(0));
        assertTrue(lineas.get(0).contains("repeats per case: 20"), "the goldens must come from 20 runs per case: " + lineas.get(0));
        // every case of the directory has its golden: a case that was unstable in the oracle is not copied by sync_cpf.py (and is listed in this report)
        for (String id : CasoCpf.casos()) {
            assertTrue(Files.exists(CasoCpf.raizCasos().resolve(id).resolve("expected.json")), id + " has no golden");
        }
        long duros = lineas.stream().skip(1).filter(l -> l.contains(" UNSTABLE: ")).count();
        assertEquals(0L, duros, "an UNSTABLE case of the oracle has a golden here: " + lineas);
    }
}
