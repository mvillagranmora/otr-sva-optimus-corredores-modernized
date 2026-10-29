package cl.bcs.apv.infrastructure.adapter;

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

    private static final Pattern LLAMADA = Pattern.compile("(?:invocar|llamar)\\(\\s*(?:\\w+\\s*,\\s*)?\"([A-Za-z_][A-Za-z0-9_.]*)\"");

    /** A call of {@code .invocar(} whose first argument is not a string literal (a name nobody can check statically). */
    private static final Pattern LLAMADA_CUALQUIERA = Pattern.compile("\\.invocar\\(\\s*([^\\s\"][^,)]*)");

    /**
     * Private helpers that forward a name to the port; their callers pass a literal ({@code llamar("CrearPersona", ...)}), which the first scan also
     * reads. Adding an entry here is a conscious review decision.
     */
    private static final Set<String> HELPERS_CON_LLAMADORES_LITERALES = Set.of("GrabarContratosServicio.java: invocar(servicio ...)", "GrabarContratosTraspasoServicio.java: invocar(servicio ...)");

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

    /** The call sites of {@code <receiver>.invocar(x, ...)} in {@code src/main} whose collaborator name is not a literal: there must be none. */
    public static Set<String> llamadasConNombreNoLiteral() {
        Set<String> sitios = new TreeSet<>();
        try (Stream<Path> archivos = Files.walk(Path.of("src/main/java"))) {
            for (Path p : archivos.filter(f -> f.toString().endsWith(".java")).toList()) {
                String texto = Files.readString(p, StandardCharsets.UTF_8);
                Matcher m = LLAMADA_CUALQUIERA.matcher(texto);
                while (m.find()) {
                    String sitio = p.getFileName() + ": invocar(" + m.group(1).strip() + " ...)";
                    if (!HELPERS_CON_LLAMADORES_LITERALES.contains(sitio)) {
                        sitios.add(sitio);
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError("cannot scan src/main/java", e);
        }
        return sitios;
    }
}
