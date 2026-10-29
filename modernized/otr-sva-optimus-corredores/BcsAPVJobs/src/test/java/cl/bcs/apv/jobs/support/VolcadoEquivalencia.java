package cl.bcs.apv.jobs.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Dual-execution dump. With {@code -Dequivalence.out=<dir>} every comparison of the characterization tests also writes the
 * canonical text of the legacy golden and of what the modern code produced, as
 * {@code <dir>/legacy/<familia>/<case>__<part>.txt} and {@code <dir>/new/...}, so that
 * {@code scripts/compare.py} (and not the test's own assertion) can decide same or different.
 * Without the property it does nothing.
 */
public final class VolcadoEquivalencia {

    private static final String PROP = "equivalence.out";

    private VolcadoEquivalencia() {
    }

    public static void escribir(String familia, String caso, String parte, String legacy, String nuevo) {
        String out = System.getProperty(PROP);
        if (out == null || out.isBlank()) {
            return;
        }
        if (esAutochequeo()) {
            return;
        }
        String nombre = (caso + "__" + parte).replaceAll("[^A-Za-z0-9._-]+", "_") + ".txt";
        try {
            escribirArchivo(Paths.get(out, "legacy", familia, nombre), legacy);
            escribirArchivo(Paths.get(out, "new", familia, nombre), nuevo);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * The harness self-checks (HarnessMecanicaTest, HarnessSelfCheckTest, SelfCheckTest) feed the comparator a
     * deliberately altered output to prove it can fail: those pairs are mutants, not equivalence evidence.
     */
    private static boolean esAutochequeo() {
        return StackWalker.getInstance().walk(frames -> frames.anyMatch(f -> {
            String n = f.getClassName();
            return n.endsWith("HarnessMecanicaTest") || n.endsWith("HarnessSelfCheckTest") || n.endsWith("SelfCheckTest");
        }));
    }

    private static void escribirArchivo(Path p, String texto) throws IOException {
        Files.createDirectories(p.getParent());
        Files.write(p, texto.getBytes(StandardCharsets.UTF_8));
    }
}
