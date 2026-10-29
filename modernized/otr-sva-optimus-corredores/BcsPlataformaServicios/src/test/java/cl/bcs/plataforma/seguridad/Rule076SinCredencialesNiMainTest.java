package cl.bcs.plataforma.seguridad;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * RULE-076: no legacy test main() (the legacy ConsultarEstadoModulo.main carried a HANA host, user and password in clear text) and no
 * credential or JDBC literal in src/main. Fails (never skips) when src/main is not found.
 */
class Rule076SinCredencialesNiMainTest {

    private static final Path MAIN = Path.of("src", "main");
    private static final Pattern MAIN_METHOD = Pattern.compile("static\\s+void\\s+main\\s*\\(");
    private static final Pattern JDBC_LITERAL = Pattern.compile("jdbc:[a-z0-9]+:", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECRET_LITERAL = Pattern.compile(
            "(password|passwd|pwd|secret|api[_-]?key|token)[\"']?\\s*[:=,]\\s*\"[^\"$\\s]{3,}\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECRET_PROPERTY = Pattern.compile(
            "^\\s*[\\w.\\-]*(password|passwd|secret|api[_-]?key|token)[\\w.\\-]*\\s*[:=]\\s*(?!\\$\\{)\\S+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static List<Path> archivos(String... ext) throws IOException {
        assertThat(MAIN).as("RULE-076 needs src/main (run the tests from the module directory)").isDirectory();
        try (Stream<Path> s = Files.walk(MAIN)) {
            return s.filter(Files::isRegularFile).filter(p -> {
                for (String e : ext) if (p.toString().endsWith(e)) return true;
                return false;
            }).toList();
        }
    }

    private static String leer(Path p) throws IOException {
        return Files.readString(p, StandardCharsets.ISO_8859_1);
    }

    @Test
    void rule076_soloLaClaseSpringApplicationTieneMain() throws IOException {
        List<String> conMain = new ArrayList<>();
        for (Path p : archivos(".java")) if (MAIN_METHOD.matcher(leer(p)).find()) conMain.add(p.getFileName().toString());
        assertThat(conMain).containsExactly("PlataformaApplication.java");
    }

    @Test
    void rule076_sinLiteralesJdbcNiHostsHana() throws IOException {
        List<String> culpables = new ArrayList<>();
        for (Path p : archivos(".java", ".properties", ".yml", ".yaml", ".xml", ".json", ".sql")) {
            String t = leer(p);
            if (JDBC_LITERAL.matcher(t).find()) culpables.add(p.toString());
        }
        assertThat(culpables).as("files with a jdbc: literal").isEmpty();
    }

    @Test
    void rule076_sinCredencialesLiterales() throws IOException {
        List<String> culpables = new ArrayList<>();
        for (Path p : archivos(".java")) if (SECRET_LITERAL.matcher(leer(p)).find()) culpables.add(p.toString());
        for (Path p : archivos(".properties", ".yml", ".yaml")) if (SECRET_PROPERTY.matcher(leer(p)).find()) culpables.add(p.toString());
        assertThat(culpables).as("files with a hard-coded credential").isEmpty();
    }

    @Test
    void rule076_laConexionYElTokenVienenDelEntorno() throws IOException {
        String y = Files.readString(MAIN.resolve("resources/application.yml"));
        assertThat(y).contains("${HANA_URL}").contains("${HANA_USER}").contains("${HANA_PASSWORD}").contains("${INTERNAL_TOKEN:}");
    }
}
