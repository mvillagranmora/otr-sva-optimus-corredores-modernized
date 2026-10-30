package cl.bcs.replica.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * RULE-076 (BUSINESS_RULES.md, P0, security, transversal): hardcoded HANA credentials in the dead {@code main()} of {@code Sesion.java}. The modernized
 * code must not repeat it: no {@code main} entry point, no JDBC URL literal that carries user and password, no key {@code password}/{@code dbpass}
 * assigned a literal, in the main and test sources of {@code BcsReplica}.
 *
 * <p>The only {@code main} allowed is the Spring Boot launcher {@code BcsReplicaApplication} when its body is a bare {@code SpringApplication.run(...)}
 * without literals (BcsReplica is a service); any other {@code main}, or a launcher with literals, is a finding.
 *
 * <p>It does not cover domain rules (B-REP/D-REP stay their own ids). It never skips: a module directory that is not where the layout says is a
 * failure, and so is a scan that read suspiciously few files.
 */
class Rule076CredencialesHardcodeadasTest {

    private static Path modulo() {
        String base = System.getProperty("basedir");
        return Path.of(base != null ? base : System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    @Test
    void RULE_076_BcsReplica_no_lleva_main_ni_credenciales_en_literales() {
        assertTrue(Files.isDirectory(modulo().resolve("src/main")), "RULE-076: module not found where expected: " + modulo());
        List<Path> archivos = Rule076Escaner.archivosDe(modulo());
        assertTrue(archivos.size() >= 500, "RULE-076: the scan must read main, tests, goldens and sql of BcsReplica; read " + archivos.size());
        assertTrue(archivos.stream().anyMatch(p -> p.getFileName().toString().equals("Rule076CredencialesHardcodeadasTest.java")), "RULE-076: the tests are scanned too");
        assertTrue(archivos.stream().anyMatch(p -> p.getFileName().toString().equals("pom.xml")), "RULE-076: the module root files are scanned too");
        assertTrue(Files.isRegularFile(modulo().resolve(Rule076Escaner.LANZADOR)), "RULE-076: the Spring Boot launcher is the one main allowed, and it must be where expected");
        List<Rule076Escaner.Hallazgo> h = Rule076Escaner.escanear(modulo());
        assertEquals(List.of(), h.stream().map(Rule076Escaner.Hallazgo::toString).collect(Collectors.toList()),
                "RULE-076: main entry point, JDBC URL with credentials or a password-like key assigned a literal");
        System.out.println("[RULE-076] scanned " + archivos.size() + " files of BcsReplica: no main, no credential literal");
    }

    @Test
    void RULE_076_el_escaner_detecta_main_url_jdbc_con_usuario_y_clave_endpoint_filtrado_y_claves_asignadas_a_literales(@org.junit.jupiter.api.io.TempDir Path tmp) throws IOException {
        // the offending shapes are built from pieces so that THIS file does not contain them (it is scanned too)
        String url = "\"jdb" + "c:sap://host:30015/?user=SYSTEM&pass" + "word=Secreta1\"";
        String endpoint = "161." + "238.0.14";
        String clave = "pass" + "word";
        String dbp = "db" + "pass";
        Path src = Files.createDirectories(tmp.resolve("src/main/java"));
        Files.writeString(src.resolve("Sucio.java"),
                "class Sucio {\n"
                        + "  public static void ma" + "in(String[] a) throws Exception {\n"
                        + "    String u = " + url + ";\n"
                        + "    String h = \"" + endpoint + ":30215\";\n"
                        + "    String " + clave + " = \"Bols1234\";\n"
                        + "    props.put(\"" + dbp + "\", \"x1\");\n"
                        + "  }\n}\n");
        Path tools = Files.createDirectories(tmp.resolve("tools"));
        Files.writeString(tools.resolve("gen.py"), clave + " = 'abc'\ncfg = {\"" + dbp + "\": \"abc\"}\n");
        List<Rule076Escaner.Hallazgo> h = Rule076Escaner.escanear(tmp);
        Set<String> tipos = h.stream().map(Rule076Escaner.Hallazgo::tipo).collect(Collectors.toSet());
        assertEquals(Set.of("main", "url-jdbc-con-credenciales", "endpoint-hana-filtrado", "clave-asignada-a-literal"), tipos, "RULE-076: " + h);
        assertTrue(h.stream().filter(x -> x.tipo().equals("clave-asignada-a-literal")).count() >= 4, "RULE-076: java assignment, put, py assignment, py dict: " + h);
        assertTrue(h.stream().noneMatch(x -> x.toString().contains("Secreta1") || x.toString().contains("Bols1234")), "RULE-076: the scanner must not print the literal it found");
    }

    @Test
    void RULE_076_el_escaner_no_marca_codigo_limpio_ni_claves_vacias_o_nulas(@org.junit.jupiter.api.io.TempDir Path tmp) throws IOException {
        String clave = "pass" + "word";
        Path src = Files.createDirectories(tmp.resolve("src/main/java"));
        Files.writeString(src.resolve("Limpio.java"),
                "class Limpio {\n"
                        + "  String " + clave + " = null;\n"
                        + "  String claveUsuario = \"\";\n"
                        + "  String url = \"jdbc-ish text without a driver prefix\";\n"
                        + "  void ejecutar(String[] args) { }\n"
                        + "}\n");
        assertEquals(List.of(), Rule076Escaner.escanear(tmp), "RULE-076: clean code is not a finding");
    }
}
