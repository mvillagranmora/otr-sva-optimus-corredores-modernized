package cl.bcs.fondomutuo.rescatereinversion;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cl.bcs.fondomutuo.rescatereinversion.CasoRre.Perfil;

/**
 * Plan 7 "entradas frescas": 40 random reproducible inputs (make_fresh_rre.py, seed 20261024, generated after the new code was finished) run through the
 * unmodified legacy (record_rre.sh, ORACLE_CASES) and here through the new code; same comparison as {@link RreEquivalenciaTest}.
 */
class RreFrescasTest {

    static Stream<String> casos() throws IOException {
        Path d = CasoRre.recursos().resolve("frescas");
        List<String> ids = new ArrayList<>();
        try (Stream<Path> s = Files.list(d)) {
            s.sorted().forEach(p -> ids.add(p.getFileName().toString()));
        }
        assertTrue(ids.size() >= 30, "fresh inputs synced: " + ids.size());
        return ids.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @DisplayName("FRESH-RRE: new code = legacy dump")
    void equivalente(String id) throws Exception {
        Path dir = CasoRre.recursos().resolve("frescas").resolve(id);
        RreEquivalenciaTest.comparar(id, CasoRre.leer(dir.resolve("expected.json")), CasoRre.ejecutar(dir, Perfil.ABIERTAS));
    }
}
