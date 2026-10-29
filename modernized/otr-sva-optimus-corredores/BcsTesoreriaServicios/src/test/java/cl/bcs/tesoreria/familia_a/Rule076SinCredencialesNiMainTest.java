package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * RULE-076: the migrated module must not carry the legacy's test mains nor hard-coded credentials / JDBC literals.
 * The legacy classes have a {@code public static void main} that opens {@code PlataformaUtils.getDESAConnection()}; the modern
 * code keeps a single main (the Spring Boot application class) and takes every connection from environment placeholders.
 * Fails (never skips) when src/main is not found.
 */
class Rule076SinCredencialesNiMainTest {

    private static final Path MAIN = Path.of("src", "main");
    private static final Pattern MAIN_METHOD = Pattern.compile("static\\s+void\\s+main\\s*\\(");
    private static final Pattern JDBC_LITERAL = Pattern.compile("jdbc:[a-z0-9]+:", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECRET_LITERAL = Pattern.compile(
            "(password|passwd|pwd|secret|api[_-]?key|token)[\"']?\\s*[:=,]\\s*\"[^\"$\\s]{3,}\"", Pattern.CASE_INSENSITIVE);
    /**
     * The property names listed in a JAXB {@code @XmlType(propOrder = {...})} (for example the SOAP type with the properties "password" and
     * "passwordAdic") are field names, not values: they are removed before looking for a secret literal. An assignment or a literal with a
     * value is still caught.
     */
    private static final Pattern XML_PROP_ORDER = Pattern.compile("propOrder\\s*=\\s*\\{[^}]*\\}");
    private static final Pattern SECRET_PROPERTY = Pattern.compile(
            "^\\s*[\\w.\\-]*(password|passwd|secret|api[_-]?key|token)[\\w.\\-]*\\s*=\\s*(?!\\$\\{)\\S+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static List<Path> archivos(String... extensiones) throws IOException {
        assertThat(MAIN).as("RULE-076 needs src/main (run the tests from the module directory)").isDirectory();
        try (Stream<Path> s = Files.walk(MAIN)) {
            return s.filter(Files::isRegularFile)
                    .filter(p -> {
                        String n = p.toString();
                        for (String e : extensiones) {
                            if (n.endsWith(e)) {
                                return true;
                            }
                        }
                        return false;
                    }).toList();
        }
    }

    @Test
    void rule076_soloLaClaseSpringApplicationTieneMain() throws IOException {
        List<Path> javas = archivos(".java");
        assertThat(javas).as("RULE-076 java sources scanned").isNotEmpty();
        List<String> conMain = new ArrayList<>();
        for (Path p : javas) {
            if (MAIN_METHOD.matcher(Files.readString(p, java.nio.charset.StandardCharsets.ISO_8859_1)).find()) {
                conMain.add(p.getFileName().toString());
            }
        }
        assertThat(conMain).as("RULE-076 classes with main()").containsExactly("TesoreriaServiceApplication.java");
    }

    @Test
    void rule076_sinLiteralesJdbcEnElCodigoNiEnLosRecursos() throws IOException {
        List<String> culpables = new ArrayList<>();
        for (Path p : archivos(".java", ".properties", ".yml", ".yaml", ".xml", ".json", ".sql")) {
            if (JDBC_LITERAL.matcher(Files.readString(p, java.nio.charset.StandardCharsets.ISO_8859_1)).find()) {
                culpables.add(p.toString());
            }
        }
        assertThat(culpables).as("RULE-076 files with a jdbc: literal").isEmpty();
    }

    @Test
    void rule076_sinCredencialesLiteralesEnElCodigoNiEnLaConfiguracion() throws IOException {
        List<String> culpables = new ArrayList<>();
        for (Path p : archivos(".java")) {
            String fuente = XML_PROP_ORDER.matcher(Files.readString(p, java.nio.charset.StandardCharsets.ISO_8859_1)).replaceAll("propOrder = {}");
            if (SECRET_LITERAL.matcher(fuente).find()) {
                culpables.add(p.toString());
            }
        }
        for (Path p : archivos(".properties", ".yml", ".yaml")) {
            if (SECRET_PROPERTY.matcher(Files.readString(p, java.nio.charset.StandardCharsets.ISO_8859_1)).find()) {
                culpables.add(p.toString());
            }
        }
        assertThat(culpables).as("RULE-076 files with a hard-coded credential").isEmpty();
    }

    @Test
    void rule076_lasPropiedadesDeConexionVienenDeVariablesDeEntorno() throws IOException {
        Path props = MAIN.resolve("resources").resolve("application.properties");
        assertThat(props).as("RULE-076 application.properties").exists();
        String t = Files.readString(props);
        assertThat(t).contains("spring.datasource.url=${HANA_URL}")
                .contains("spring.datasource.username=${HANA_USER}")
                .contains("spring.datasource.password=${HANA_PASSWORD}");
    }
}
