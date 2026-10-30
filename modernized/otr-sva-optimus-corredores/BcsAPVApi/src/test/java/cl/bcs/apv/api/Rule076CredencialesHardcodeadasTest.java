package cl.bcs.apv.api;

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
 * RULE-076 (BUSINESS_RULES.md, P0, security): hardcoded HANA credentials in the dead {@code main()} of {@code Sesion.java}. The modernized code must not
 * repeat it: no {@code main} entry point, no JDBC URL literal that carries user and password, no key {@code password}/{@code dbpass} assigned a literal,
 * in {@code BcsAPVApi}, in {@code BcsApvJavaCallAdapter} (main and test sources) nor in the generators {@code tools/*.py} that emit this code.
 *
 * <p>This is the only test that names the rule: the rule is about the code of the module, so what is pinned is exactly that. It never skips: a module
 * directory that is not where the layout says (the adapter is a sibling of this module) is a failure, and so is a scan that read suspiciously few files.
 */
class Rule076CredencialesHardcodeadasTest {

    private static Path modulo() {
        return LegacyApv.modulo();
    }

    private static Path adaptador() {
        return modulo().resolve("../BcsApvJavaCallAdapter").normalize();
    }

    @Test
    void RULE_076_ni_BcsAPVApi_ni_BcsApvJavaCallAdapter_ni_los_generadores_llevan_main_ni_credenciales_en_literales() {
        assertTrue(Files.isDirectory(adaptador().resolve("src/main")), "RULE-076: the adapter module must be found next to this one: " + adaptador());
        assertTrue(Files.isDirectory(modulo().resolve("tools")), "RULE-076: the generators must be found: " + modulo().resolve("tools"));
        List<Path> api = Rule076Escaner.archivosDe(modulo());
        List<Path> adp = Rule076Escaner.archivosDe(adaptador());
        assertTrue(api.size() >= 300, "RULE-076: the scan of BcsAPVApi must read the 299 generated sources, the hand written ones, the tests and the generators; read " + api.size());
        assertTrue(adp.size() >= 60, "RULE-076: the scan of BcsApvJavaCallAdapter must read the 64 mappers and the tests; read " + adp.size());
        assertTrue(api.stream().anyMatch(p -> p.getFileName().toString().equals("generar.py")), "RULE-076: tools/generar.py must be scanned");
        assertTrue(api.stream().anyMatch(p -> p.getFileName().toString().equals("Rule076CredencialesHardcodeadasTest.java")), "RULE-076: the tests are scanned too");
        List<Rule076Escaner.Hallazgo> h = Rule076Escaner.escanear(modulo());
        h.addAll(Rule076Escaner.escanear(adaptador()));
        assertEquals(List.of(), h.stream().map(Rule076Escaner.Hallazgo::toString).collect(Collectors.toList()),
                "RULE-076: main entry point, JDBC URL with credentials or a password-like key assigned a literal");
        System.out.println("[RULE-076] scanned " + api.size() + " files of BcsAPVApi and " + adp.size() + " of BcsApvJavaCallAdapter: no main, no credential literal");
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
