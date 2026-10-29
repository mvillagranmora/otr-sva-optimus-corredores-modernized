package cl.bcs.contabilidad.centraliza;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * CONT-E-SQL: every SQL resource the modern code names must exist on the classpath and be non-empty.
 *
 * <p>Scans every string literal ending in ".sql" in src/main/java (read from disk relative to the module dir)
 * and resolves it with the prefix the loading code uses:
 * <ul>
 *   <li>literal containing '/' (domain/service/* and ContabilidadJdbcRepository: {@code "sql/X.sql"} loaded by
 *       ContabilidadJdbcRepository.loadSql via ClassPathResource; service/* full paths via getResourceAsStream): as is;</li>
 *   <li>bare name in cl/bcs/contabilidad/service/*: {@code "cl/bcs/contabilidad/sql/" + name} (their private sql(name) helper);</li>
 *   <li>bare name anywhere else: {@code "sql/" + name}.</li>
 * </ul>
 * Legacy loads every SQL with {@code loadResourceToString(...)} and throws IllegalStateException("can't load resource")
 * when it is missing (BcsCentralizacion.java:86-98), so a missing resource is a hard failure, never a skip.
 */
class SqlResourcesTest {

    private static final Pattern SQL_LITERAL = Pattern.compile("\"([^\"\\s]+\\.sql)\"");

    record SqlRef(String source, int line, String literal, String classpath) {}

    static Path mainJava() {
        List<Path> candidates = new ArrayList<>();
        String basedir = System.getProperty("basedir");
        if (basedir != null) candidates.add(Paths.get(basedir, "src", "main", "java"));
        candidates.add(Paths.get("src", "main", "java"));
        candidates.add(Paths.get("BcsContabilidadServicios", "src", "main", "java"));
        for (Path p : candidates)
            if (Files.isDirectory(p.resolve("cl/bcs/contabilidad"))) return p;
        return fail("module sources not found (tried %s, cwd=%s); the scan must run, not skip", candidates,
                Paths.get("").toAbsolutePath());
    }

    static List<SqlRef> scan() {
        Path root = mainJava();
        List<SqlRef> refs = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path f : files.filter(p -> p.toString().endsWith(".java")).sorted().toList()) {
                String rel = root.relativize(f).toString().replace('\\', '/');
                boolean serviceLayer = rel.startsWith("cl/bcs/contabilidad/service/");
                List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    Matcher m = SQL_LITERAL.matcher(lines.get(i));
                    while (m.find()) {
                        String lit = m.group(1);
                        String cp = lit.contains("/") ? lit
                                : serviceLayer ? "cl/bcs/contabilidad/sql/" + lit : "sql/" + lit;
                        refs.add(new SqlRef(rel, i + 1, lit, cp));
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return refs;
    }

    static boolean presentAndNonEmpty(String classpath) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpath)) {
            return in != null && !new String(in.readAllBytes(), StandardCharsets.UTF_8).isBlank();
        } catch (IOException e) {
            return false;
        }
    }

    @Test
    @DisplayName("CONT-E-SQL-01: the scan finds SQL literals (a scan that finds nothing proves nothing)")
    void contESql01_scanIsNotVacuous() {
        List<SqlRef> refs = scan();
        System.out.println("SQL literals scanned: " + refs.size() + " (distinct classpath resources: "
                + refs.stream().map(SqlRef::classpath).distinct().count() + ")");
        assertThat(refs).as("SQL string literals in src/main/java").hasSizeGreaterThan(100);
    }

    @Test
    @DisplayName("CONT-E-SQL-02: every SQL literal in src/main/java resolves to a non-empty classpath resource")
    void contESql02_everySqlResourceExists() {
        List<SqlRef> refs = scan();
        Map<String, List<String>> missing = new TreeMap<>();
        for (SqlRef r : refs)
            if (!presentAndNonEmpty(r.classpath()))
                missing.computeIfAbsent(r.classpath(), k -> new ArrayList<>()).add(r.source() + ":" + r.line());
        StringBuilder msg = new StringBuilder();
        missing.forEach((cp, where) -> msg.append("\n  MISSING ").append(cp).append("  <- ").append(where));
        System.out.println("SQL resources: " + new TreeSet<>(refs.stream().map(SqlRef::classpath).toList()).size()
                + " distinct, " + missing.size() + " missing; literals: " + refs.size() + ", unresolved literals: "
                + missing.values().stream().mapToInt(List::size).sum());
        assertThat(missing)
                .as("%d of %d SQL literals do not resolve to a classpath resource:%s",
                        missing.values().stream().mapToInt(List::size).sum(), refs.size(), msg)
                .isEmpty();
    }
}
