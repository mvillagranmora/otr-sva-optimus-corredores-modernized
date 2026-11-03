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
 * LOGIN-PG round 2 / RULE-076: the fixtures (loginpg2/) and the sources of the round 2 tests carry no credential and no connection string, make no network
 * connection, never read the environment file, and open no database but H2 in memory. The Postgres catalog files are metadata only (names and types, no rows).
 * Green today and must stay green.
 */
class LoginPg2SeguridadTest {

    private static List<Path> archivos() throws IOException {
        List<Path> r = new ArrayList<>();
        try (Stream<Path> s = Files.walk(Path.of("src/test/resources/loginpg2"))) {
            s.filter(Files::isRegularFile).forEach(r::add);
        }
        try (Stream<Path> s = Files.list(Path.of("src/test/java/cl/bcs/plataforma/login/dialecto"))) {
            s.filter(p -> {
                String n = p.getFileName().toString();
                return (n.startsWith("LoginPg2") || n.equals("JdbcGrabador.java") || n.equals("ApiLoginPg3.java")) && !n.equals("LoginPg2SeguridadTest.java");
            }).forEach(r::add);
        }
        assertThat(r).as("files scanned").hasSizeGreaterThan(10);
        return r;
    }

    private static List<String> buscar(Pattern p) throws IOException {
        List<String> hits = new ArrayList<>();
        for (Path f : archivos()) {
            var m = p.matcher(Files.readString(f, StandardCharsets.UTF_8));
            while (m.find()) hits.add(f.getFileName() + ": " + m.group());
        }
        return hits;
    }

    @Test
    @DisplayName("RULE-076 round 2: no real connection string, no user:secret@host, no credential-like literal in the fixtures or the tests")
    void sin_credenciales() throws IOException {
        assertThat(buscar(Pattern.compile("jdbc:(sap|sqlserver|postgresql|hdb)[^\\s\"']*"))).isEmpty();
        assertThat(buscar(Pattern.compile("://[^/\\s:\"']+:[^@\\s\"']+@"))).isEmpty();
        assertThat(buscar(Pattern.compile("(?i)\\b(password|passwd|pwd|secret|api[_-]?key|bearer)\\b\\s*[=:]\\s*[\"'][^\"']{4,}[\"']"))).isEmpty();
        assertThat(buscar(Pattern.compile("(HANA_PASSWORD|POSTGRES_PASSWORD|POSTGRES_USER|HANA_USER|qa01\\.[a-z.]+)\\s*=\\s*\\S+"))).isEmpty();
    }

    @Test
    @DisplayName("T-35 round 2: no network and no environment file: no socket, HttpClient, URL.openConnection, RestTemplate or WebClient, no http(s) URL, and the only jdbc: literal is H2 in memory")
    void sin_red_ni_env_y_solo_h2() throws IOException {
        assertThat(buscar(Pattern.compile("\\.env\\b|java\\.net\\.(Socket|URL|http)|HttpClient|openConnection|new Socket|RestTemplate|WebClient\\.create"))).isEmpty();
        assertThat(buscar(Pattern.compile("https?://[^\\s\"')]+"))).isEmpty();
        for (String hit : buscar(Pattern.compile("jdbc:[a-z0-9]+:[a-z]*"))) assertThat(hit).as(hit).endsWith("jdbc:h2:mem");
    }

    @Test
    @DisplayName("T-35 round 2: the Postgres catalog fixtures hold names and types only: every line of the view-column TSV has four fields and the routines file has only RUTINA and TIPO lines")
    void solo_metadatos() {
        for (String l : LoginPg2Recursos.texto("vistas-columnas-pg-qa-20261103.tsv").split("\n")) {
            if (!l.isBlank()) assertThat(l.split("\t", -1)).hasSize(4);
        }
        for (String l : LoginPg2Recursos.texto("rutinas-y-tipos-postgres-qa-20261103.txt").split("\n")) {
            if (!l.isBlank()) assertThat(l).matches("^(RUTINA|TIPO) .*");
        }
    }
}
