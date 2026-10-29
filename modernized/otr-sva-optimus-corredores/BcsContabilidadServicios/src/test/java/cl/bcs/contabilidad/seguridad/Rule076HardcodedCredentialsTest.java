package cl.bcs.contabilidad.seguridad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RULE-076 (P0, BUSINESS_RULES.md): the legacy {@code BcsUtils/Sesion.java:304-319} {@code main()} carried hardcoded
 * HANA credentials (host, user and password of a QA database). The rewrite must not carry them over in any form: no
 * {@code Sesion} {@code main()}, no JDBC URL with a literal host, no HANA master-user literal, and every datasource
 * credential is an environment placeholder.
 *
 * <p>The checks scan the module's own production sources and resources. They never contain the legacy values
 * themselves, only their shapes, so this test is not a copy of the secret.
 */
class Rule076HardcodedCredentialsTest {

    static final Path MAIN = Path.of("src", "main");

    /** {@code jdbc:sap://} followed by a literal IPv4 address (legacy Sesion.java shape). */
    static final Pattern JDBC_SAP_LITERAL_HOST = Pattern.compile("jdbc:sap://\\d{1,3}(\\.\\d{1,3}){3}");
    /** HANA master users named in the RULE-076/077 rotation list. */
    static final Pattern HANA_MASTER_USER = Pattern.compile("\"MASTER0\\d\"");
    static final Pattern MAIN_METHOD = Pattern.compile("static\\s+void\\s+main\\s*\\(");

    static List<Path> archivos(String... extensiones) throws IOException {
        List<Path> out = new ArrayList<>();
        try (Stream<Path> s = Files.walk(MAIN)) {
            s.filter(Files::isRegularFile).forEach(p -> {
                for (String e : extensiones) if (p.toString().endsWith(e)) out.add(p);
            });
        }
        assertThat(out).as("production sources under %s", MAIN.toAbsolutePath()).isNotEmpty();
        return out;
    }

    static String leer(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(p.toString(), e);
        }
    }

    @Test
    @DisplayName("RULE-076: no Sesion class and no main() other than the Spring Boot entry point")
    void rule076_noSesionMainPorted() throws IOException {
        List<Path> fuentes = archivos(".java");
        assertThat(fuentes).noneMatch(p -> p.getFileName().toString().equals("Sesion.java"));
        assertThat(fuentes)
                .filteredOn(p -> MAIN_METHOD.matcher(leer(p)).find())
                .extracting(p -> p.getFileName().toString())
                .containsOnly("BcsContabilidadApplication.java");
    }

    @Test
    @DisplayName("RULE-076: no JDBC SAP URL with a literal host and no HANA master-user literal in code or resources")
    void rule076_noHardcodedHanaConnection() throws IOException {
        for (Path p : archivos(".java", ".properties", ".yml", ".yaml", ".xml", ".json")) {
            String texto = leer(p);
            assertThat(JDBC_SAP_LITERAL_HOST.matcher(texto).find()).as("literal HANA host in %s", p).isFalse();
            assertThat(HANA_MASTER_USER.matcher(texto).find()).as("HANA master user literal in %s", p).isFalse();
        }
    }

    @Test
    @DisplayName("RULE-076: datasource URL, user and password come only from environment placeholders")
    void rule076_datasourceCredentialsFromEnvironment() throws IOException {
        Properties props = new Properties();
        try (var in = Files.newBufferedReader(MAIN.resolve("resources").resolve("application.properties"),
                StandardCharsets.UTF_8)) {
            props.load(in);
        }
        for (String clave : List.of("spring.datasource.url", "spring.datasource.username", "spring.datasource.password")) {
            assertThat(props.getProperty(clave)).as(clave).isNotNull().matches("\\$\\{[A-Z0-9_]+}");
        }
        props.stringPropertyNames().stream()
                .filter(k -> k.toLowerCase().contains("password") || k.toLowerCase().contains("clave"))
                .forEach(k -> assertThat(props.getProperty(k)).as(k).matches("|\\$\\{[^}]+}"));
    }
}
