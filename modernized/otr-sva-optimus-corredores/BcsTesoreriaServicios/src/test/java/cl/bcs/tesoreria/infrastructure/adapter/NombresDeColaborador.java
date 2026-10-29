package cl.bcs.tesoreria.infrastructure.adapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Every literal collaborator name that the production code passes to {@code Colaboradores.invocar(...)}: found by scanning {@code src/main/java}
 * for {@code invocar("X", ...)} / {@code invocar(colaboradores, "X", ...)} (the services, the typed kit contracts under {@code kit/colaborador} and the
 * {@code contrato} package), so a new call site with a name nobody registered is caught without anyone updating a list.
 */
public final class NombresDeColaborador {

    private static final Pattern LLAMADA = Pattern.compile("invocar\\(\\s*(?:\\w+\\s*,\\s*)?\"([A-Za-z_][A-Za-z0-9_.]*)\"");

    private NombresDeColaborador() {
    }

    public static Set<String> literalesDeInvocar() {
        Set<String> nombres = new TreeSet<>();
        try (Stream<Path> archivos = Files.walk(Path.of("src/main/java"))) {
            for (Path p : archivos.filter(f -> f.toString().endsWith(".java")).toList()) {
                Matcher m = LLAMADA.matcher(Files.readString(p, StandardCharsets.UTF_8));
                while (m.find()) {
                    nombres.add(m.group(1));
                }
            }
        } catch (IOException e) {
            throw new AssertionError("cannot scan src/main/java", e);
        }
        return nombres;
    }
}
