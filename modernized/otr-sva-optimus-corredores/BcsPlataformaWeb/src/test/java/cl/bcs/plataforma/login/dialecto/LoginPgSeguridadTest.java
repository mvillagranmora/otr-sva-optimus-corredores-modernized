package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * T-35 / RULE-076: the fixtures and sources of the LOGIN-PG tests carry no credential, no connection string to a real database and make no network
 * connection (H2 in memory only; the only URL-like literal is the fake {@code sso.invalid} of the seed). Green today and must stay green.
 */
class LoginPgSeguridadTest {

    private static List<Path> archivos() throws IOException {
        List<Path> r = new ArrayList<>();
        for (String d : List.of("src/test/resources/loginpg", "src/test/java/cl/bcs/plataforma/login/dialecto")) {
            try (Stream<Path> s = Files.walk(Path.of(d))) {
                s.filter(Files::isRegularFile).filter(p -> !p.getFileName().toString().equals("LoginPgSeguridadTest.java") && !p.toString().endsWith(".md")).forEach(r::add);
            }
        }
        assertThat(r).as("files scanned").hasSizeGreaterThan(60);
        return r;
    }

    private static List<String> buscar(Pattern p) throws IOException {
        List<String> hits = new ArrayList<>();
        for (Path f : archivos()) {
            String t = Files.readString(f, StandardCharsets.UTF_8);
            var m = p.matcher(t);
            while (m.find()) hits.add(f.getFileName() + ": " + m.group());
        }
        return hits;
    }

    @Test
    @DisplayName("RULE-076 no real connection string in fixtures or tests: no jdbc:sap, no jdbc:postgresql, no user:password@host")
    void sin_cadenas_de_conexion() throws IOException {
        assertThat(buscar(Pattern.compile("jdbc:(sap|sqlserver|postgresql|hdb)[^\\s\"']*"))).isEmpty();
        assertThat(buscar(Pattern.compile("://[^/\\s:\"']+:[^@\\s\"']+@"))).isEmpty();
    }

    @Test
    @DisplayName("RULE-076 no credential-like literal: password/secret/apikey assigned a quoted value, nor values of the connection variables")
    void sin_credenciales() throws IOException {
        assertThat(buscar(Pattern.compile("(?i)\\b(password|passwd|pwd|secret|api[_-]?key|bearer)\\b\\s*[=:]\\s*[\"'][^\"']{4,}[\"']"))).isEmpty();
        assertThat(buscar(Pattern.compile("(HANA_PASSWORD|POSTGRES_PASSWORD|POSTGRES_USER|HANA_USER|qa01\\.[a-z.]+)\\s*=\\s*\\S+"))).isEmpty();
    }

    @Test
    @DisplayName("T-35 the tests never read .env and open no network connection: no socket, HttpClient, URL.openConnection, WebClient or RestTemplate in their sources")
    void sin_red_ni_env() throws IOException {
        assertThat(buscar(Pattern.compile("\\.env\\b|java\\.net\\.(Socket|URL|http)|HttpClient|openConnection|new Socket|RestTemplate|WebClient\\.create"))).isEmpty();
        assertThat(buscar(Pattern.compile("https?://(?!sso\\.invalid)[^\\s\"')]+"))).as("URLs other than the fake sso.invalid").isEmpty();
    }

    @Test
    @DisplayName("T-35 the only database the tests open is H2 in memory: every jdbc: literal is jdbc:h2:mem")
    void solo_h2_en_memoria() throws IOException {
        for (String hit : buscar(Pattern.compile("jdbc:[a-z0-9]+:[a-z]*"))) {
            assertThat(hit).as(hit).endsWith("jdbc:h2:mem");
        }
    }
}
