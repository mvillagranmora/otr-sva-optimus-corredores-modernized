package cl.bcs.apv.adapter.javacall;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Test support of RULE-076 (BUSINESS_RULES.md: hardcoded HANA credentials in the dead {@code main()} of {@code Sesion.java}: a manual-test {@code main}
 * with a JDBC URL, user and password literals that is never called at runtime but leaks the credentials).
 *
 * <p>Scans the sources of a module (Java and Python generators) for the three shapes the rule describes:
 * <ol>
 *   <li>a {@code main} entry point in Java code (the dead code that carried the credentials);</li>
 *   <li>a JDBC URL literal that carries user and password (query parameters or {@code user:pass@}), or the leaked HANA endpoint of the rule;</li>
 *   <li>a key {@code password}/{@code passwd}/{@code pwd}/{@code dbpass}... assigned (or put) a NON-empty string literal.</li>
 * </ol>
 * Comments are NOT stripped on purpose: a credential in a comment leaks exactly as much as one in code. This class contains no such literal itself
 * (the patterns are built from pieces), so scanning its own source finds nothing.
 */
final class Rule076Escaner {

    private static final String MAIN = "public\\s+static\\s+void\\s+ma" + "in\\s*\\(";
    private static final Pattern PATRON_MAIN = Pattern.compile(MAIN);
    /** jdbc:xxx: literal with user/password parameters, or user:password@host form */
    private static final Pattern PATRON_JDBC = Pattern.compile(
            "[\"'][^\"'\\n]*jdbc:[a-z0-9]+:[^\"'\\n]*(?:[?;&](?:user|username|password|pwd)=|//[^/\"'\\s:@]+:[^/\"'\\s@]+@)[^\"'\\n]*[\"']", Pattern.CASE_INSENSITIVE);
    /** the endpoint leaked by the rule (host:port of the HANA instance) */
    private static final Pattern PATRON_ENDPOINT = Pattern.compile("161\\.238\\.0\\.14");
    /** a password-like key (see CLAVES) assigned, mapped, put or set to a NON-empty string literal, in Java or Python syntax */
    private static final String CLAVES = "(?:pass" + "word|pass" + "wd|pwd|db[_-]?pass(?:word)?|contrase(?:n|\u00f1)a)";
    private static final Pattern PATRON_ASIGNADA = Pattern.compile(
            "(?i)(?:\\b\\w*" + CLAVES + "\\w*[\"']?\\s*(?:=|:|=>|,)\\s*[\"'][^\"'\\n]+[\"']"
                    + "|set" + CLAVES + "\\s*\\(\\s*[\"'][^\"'\\n]+[\"']\\s*\\))");

    private Rule076Escaner() {
    }

    /** One finding: file, line and a short description (never the literal itself in full). */
    record Hallazgo(String archivo, int linea, String tipo, String extracto) {
        @Override
        public String toString() {
            return archivo + ":" + linea + " [" + tipo + "] " + extracto;
        }
    }

    /** Files scanned for a module: {@code src/main}, {@code src/test} (any {@code .java}) and the generators {@code tools/*.py}. */
    static List<Path> archivosDe(Path modulo) {
        List<Path> out = new ArrayList<>();
        for (String sub : List.of("src/main", "src/test")) {
            Path d = modulo.resolve(sub);
            if (Files.isDirectory(d)) {
                try (Stream<Path> s = Files.walk(d)) {
                    s.filter(p -> Files.isRegularFile(p) && (p.toString().endsWith(".java") || p.toString().endsWith(".properties")
                            || p.toString().endsWith(".json") || p.toString().endsWith(".txt") || p.toString().endsWith(".xml")))
                            .forEach(out::add);
                } catch (IOException e) {
                    throw new UncheckedIOException(d.toString(), e);
                }
            }
        }
        Path tools = modulo.resolve("tools");
        if (Files.isDirectory(tools)) {
            try (Stream<Path> s = Files.list(tools)) {
                s.filter(p -> p.toString().endsWith(".py")).forEach(out::add);
            } catch (IOException e) {
                throw new UncheckedIOException(tools.toString(), e);
            }
        }
        out.sort(null);
        return out;
    }

    static List<Hallazgo> escanear(Path modulo) {
        List<Hallazgo> out = new ArrayList<>();
        for (Path f : archivosDe(modulo)) {
            String nombre = modulo.relativize(f).toString().replace('\\', '/');
            String[] lineas;
            try {
                lineas = Files.readString(f, StandardCharsets.UTF_8).split("\\R", -1);
            } catch (IOException e) {
                throw new UncheckedIOException(f.toString(), e);
            }
            boolean java = nombre.endsWith(".java");
            for (int i = 0; i < lineas.length; i++) {
                String l = lineas[i];
                if (java && PATRON_MAIN.matcher(l).find()) {
                    out.add(new Hallazgo(nombre, i + 1, "main", recorte(l)));
                }
                if (PATRON_JDBC.matcher(l).find()) {
                    out.add(new Hallazgo(nombre, i + 1, "url-jdbc-con-credenciales", "(literal jdbc con usuario/clave, no se imprime)"));
                }
                if (PATRON_ENDPOINT.matcher(l).find()) {
                    out.add(new Hallazgo(nombre, i + 1, "endpoint-hana-filtrado", "(host:puerto de la regla, no se imprime)"));
                }
                Matcher m = PATRON_ASIGNADA.matcher(l);
                if (m.find()) {
                    out.add(new Hallazgo(nombre, i + 1, "clave-asignada-a-literal", "(clave con literal, no se imprime): " + m.group().replaceAll("[\"'][^\"']*[\"']\\s*\\)?$", "\"***\"")));
                }
            }
        }
        return out;
    }

    private static String recorte(String l) {
        String t = l.strip();
        return t.length() > 80 ? t.substring(0, 80) : t;
    }
}
