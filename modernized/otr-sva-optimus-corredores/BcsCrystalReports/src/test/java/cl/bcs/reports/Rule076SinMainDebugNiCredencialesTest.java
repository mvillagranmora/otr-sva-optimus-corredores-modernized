package cl.bcs.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RULE-076 (P0, reviewer verdict 2026-10-02: credentials must be rotated and the debug main() removed from source):
 * the sidecar carries no debug {@code main()} (the legacy BcsPlataformaReporteApi had demo/Main.java), no connection
 * file or hard-coded connection and no credential literal in its production sources and resources. Checks on shapes,
 * never repeating a secret. The .rpt templates embed a HANA host (legacy data, not code); they are binary and out of scope.
 */
class Rule076SinMainDebugNiCredencialesTest {

    private static final Path MAIN = Paths.get("src", "main");

    private static List<Path> archivos(String... extensiones) throws IOException {
        try (Stream<Path> s = Files.walk(MAIN)) {
            return s.filter(Files::isRegularFile)
                    .filter(p -> {
                        String n = p.getFileName().toString();
                        for (String e : extensiones) if (n.endsWith(e)) return true;
                        return false;
                    })
                    .collect(Collectors.toList());
        }
    }

    private static String texto(Path p) throws IOException {
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("RULE-076: the only main() in the production sources is the @SpringBootApplication entry point")
    void rule076_unicoMainEsSpringBootApplication() throws IOException {
        // the JVM entry point signature only (DataApplier.main(ReportDocument, JsonObject) is a helper that renders the servlet's "mainReport")
        Pattern main = Pattern.compile("static\\s+void\\s+main\\s*\\(\\s*(final\\s+)?String\\s*(\\[\\s*\\]|\\.\\.\\.)");
        List<String> conMain = new ArrayList<>();
        for (Path p : archivos(".java")) {
            String t = texto(p);
            if (main.matcher(t).find()) conMain.add(p.getFileName() + (t.contains("@SpringBootApplication") ? "" : " [NOT @SpringBootApplication]"));
        }
        assertThat(conMain).hasSize(1).noneMatch(s -> s.contains("NOT @SpringBootApplication"));
    }

    @Test
    @DisplayName("RULE-076: no debug connection or credential file in sources and resources")
    void rule076_sinConexionDebug() throws IOException {
        List<String> prohibidos = List.of("connections.properties", "getPropertiesConnection", "desa02", "\"desa2\"", "\"desa\"",
                "getDESA", "C:\\\\Users", "C:/Users", "jdbc:sap://");
        List<String> hallazgos = new ArrayList<>();
        for (Path p : archivos(".java", ".yml", ".properties", ".xml")) {
            String t = texto(p);
            for (String x : prohibidos) if (t.contains(x)) hallazgos.add(p.getFileName() + ": " + x);
        }
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("RULE-076: no credential literal (password/clave/secret/token = \"...\" must be empty, an ENV_VAR name or a placeholder)")
    void rule076_sinCredencialesEnJava() throws IOException {
        Pattern asignacion = Pattern.compile("(?i)\\b(\\w*(password|passwd|pwd|pass|clave|secret|token)\\w*)\\s*[=:,(]\\s*\"([^\"]*)\"");
        List<String> hallazgos = new ArrayList<>();
        for (Path p : archivos(".java")) {
            Matcher m = asignacion.matcher(texto(p));
            while (m.find()) {
                String v = m.group(3);
                boolean permitido = v.isEmpty() || v.matches("[A-Z][A-Z0-9_]*") || v.contains("${") || v.contains("<redacted>")
                        || v.matches("[A-Za-z]+[- ][A-Za-z -]*");
                if (!permitido) hallazgos.add(p.getFileName() + ": " + m.group(1) + " = \"" + "*".repeat(v.length()) + "\"");
            }
        }
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("RULE-076: application.yml reads every credential from the environment (no literal value)")
    void rule076_yamlSoloVariablesDeEntorno() throws IOException {
        Pattern credencial = Pattern.compile("(?im)^\\s*(password|username|internal-token)\\s*:\\s*(\\S.*)$");
        List<String> hallazgos = new ArrayList<>();
        for (Path p : archivos(".yml")) {
            Matcher m = credencial.matcher(texto(p));
            while (m.find()) {
                String v = m.group(2).trim();
                if (!v.startsWith("${")) hallazgos.add(p.getFileName() + ": " + m.group(1));
            }
        }
        assertThat(hallazgos).isEmpty();
    }
}
