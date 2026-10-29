package cl.bcs.tesoreria.jobs.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Dual-execution dump hook (the same contract as BcsTesoreriaServicios / BcsAPVServicios). With {@code -Dequivalence.out=<dir>} every
 * comparison of the characterization tests also writes the canonical text of the legacy golden and of what the modern code produced, as
 * {@code <dir>/legacy/<familia>/<case>__<part>.txt} and {@code <dir>/new/...}, so that {@code scripts/compare.py} (and not the test's own
 * assertion) decides same or different. Without the property it does nothing. Self-check tests (class name ending in SelfCheckTest) feed
 * deliberately altered output and never write.
 */
public final class VolcadoEquivalencia {

    private static final String PROP = "equivalence.out";
    public static final String FAMILIA = "tesoreria-jobs";

    private VolcadoEquivalencia() {
    }

    public static void escribir(String caso, String parte, String legacy, String nuevo) {
        String out = System.getProperty(PROP);
        if (out == null || out.isBlank()) {
            return;
        }
        if (StackWalker.getInstance().walk(f -> f.anyMatch(x -> x.getClassName().endsWith("SelfCheckTest")))) {
            return;
        }
        String nombre = (caso + "__" + parte).replaceAll("[^A-Za-z0-9._-]+", "_") + ".txt";
        try {
            escribirArchivo(Paths.get(out, "legacy", FAMILIA, nombre), legacy);
            escribirArchivo(Paths.get(out, "new", FAMILIA, nombre), nuevo);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void escribirArchivo(Path p, String texto) throws IOException {
        Files.createDirectories(p.getParent());
        Files.write(p, texto.getBytes(StandardCharsets.UTF_8));
    }
}
