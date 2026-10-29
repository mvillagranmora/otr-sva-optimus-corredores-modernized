package cl.bcs.utils.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RULE-076 (P0, reviewer verdict 2026-10-02: "credentials must be rotated ... and the main() method must be removed from
 * source"). The legacy BcsUtils had debug main() methods (Sesion.java:303, Crypto.java:26, RutUtil.java:21,
 * Fecha.java:182, ValidarNuevaClave.java:239, StatsD.java:254, Utils.java:1339), one of them opening a HANA connection
 * with a literal host and literal credentials (Sesion.java:304-319). None of those values is repeated here: the checks
 * are on shapes, not on the secrets.
 */
class Rule076SinMainNiCredencialesTest {

    @Test
    @DisplayName("RULE-076: no static main() in any compiled class of the library (it is a library: zero entry points)")
    void rule076_sinMain() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        List<String> conMain = new ArrayList<>();
        for (Path p : ModuloFuente.clasesCompiladas()) {
            String nombre = ModuloFuente.nombreClase(p);
            if (nombre.endsWith("package-info") || nombre.endsWith("module-info")) continue;
            Class<?> c = Class.forName(nombre, false, cl);
            for (Method m : c.getDeclaredMethods())
                if (m.getName().equals("main") && Modifier.isStatic(m.getModifiers())) conMain.add(nombre);
        }
        assertThat(conMain).as("static main() methods").isEmpty();
    }

    @Test
    @DisplayName("RULE-076: no JDBC URL with a literal host and no IPv4 literal in sources or resources")
    void rule076_sinJdbcConHost() {
        Pattern jdbcLiteral = Pattern.compile("jdbc:(sap|h2|oracle|postgresql|sqlserver|mysql)://[A-Za-z0-9\\[]");
        Pattern ipv4 = Pattern.compile("\\b(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\b");
        List<String> hallazgos = new ArrayList<>();
        List<Path> archivos = new ArrayList<>(ModuloFuente.fuentesJava());
        archivos.addAll(ModuloFuente.recursos());
        for (Path p : archivos) {
            String t = ModuloFuente.texto(p);
            if (jdbcLiteral.matcher(t).find()) hallazgos.add(p.getFileName() + ": literal JDBC host");
            Matcher ip = ipv4.matcher(t);
            while (ip.find()) if (!ip.group().equals("127.0.0.1") && !ip.group().equals("0.0.0.0")) hallazgos.add(p.getFileName() + ": IPv4 literal");
        }
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("RULE-076: no MASTER0<digit> literal (the legacy debug user shape) in sources, resources, compiled classes or golden fixtures")
    void rule076_sinUsuarioMaster() {
        Pattern master = Pattern.compile("MASTER0[0-9]");
        List<String> hallazgos = new ArrayList<>();
        List<Path> textos = new ArrayList<>(ModuloFuente.fuentesJava());
        textos.addAll(ModuloFuente.recursos());
        textos.addAll(ModuloFuente.goldens());
        for (Path p : textos) if (master.matcher(ModuloFuente.texto(p)).find()) hallazgos.add(p.getFileName().toString());
        for (Path p : ModuloFuente.clasesCompiladas())
            if (master.matcher(new String(ModuloFuente.bytes(p), StandardCharsets.ISO_8859_1)).find()) hallazgos.add(ModuloFuente.nombreClase(p));
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("RULE-076: the scanners are not blind (self-check on synthetic text)")
    void rule076_detectoresFuncionan() {
        assertThat(Pattern.compile("MASTER0[0-9]").matcher("user=MASTER07").find()).isTrue();
        assertThat(Pattern.compile("jdbc:(sap|h2|oracle|postgresql|sqlserver|mysql)://[A-Za-z0-9\\[]").matcher("jdbc:sap://hana.example:30015").find()).isTrue();
        assertThat(Pattern.compile("jdbc:(sap|h2|oracle|postgresql|sqlserver|mysql)://[A-Za-z0-9\\[]").matcher("${HANA_URL}").find()).isFalse();
    }

    @Test
    @DisplayName("RULE-076: no credential literal in Java sources (password/clave/secret/token/user = \"...\" must be empty, an ENV_VAR name or redacted)")
    void rule076_sinCredencialesEnJava() {
        Pattern asignacion = Pattern.compile("(?i)\\b(\\w*(password|passwd|pwd|pass|clave|secret|token|usuario|user)\\w*)\\s*[=:,(]\\s*\"([^\"]*)\"");
        List<String> hallazgos = new ArrayList<>();
        for (Path p : ModuloFuente.fuentesJava()) {
            Matcher m = asignacion.matcher(ModuloFuente.texto(p));
            while (m.find()) {
                String v = m.group(3);
                boolean permitido = v.isEmpty() || v.matches("[A-Z][A-Z0-9_]*") || v.contains("<redacted>") || v.contains("${")
                        || v.matches("[A-Za-z]+\\.[A-Za-z]+(\\.[A-Za-z-]+)*"); // config keys such as security.password.rehash-enabled
                if (!permitido) hallazgos.add(p.getFileName() + ": " + m.group(1) + " = \"" + "*".repeat(v.length()) + "\"");
            }
        }
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("RULE-076: credentials only from the environment: no DriverManager/DataSource built in the library; config secrets are ${ENV} placeholders")
    void rule076_credencialesSoloDesdeEntorno() {
        Pattern clave = Pattern.compile("(?im)^\\s*([\\w.\\-]*(password|passwd|pwd|pass|clave|secret|token|username|user)[\\w.\\-]*)\\s*[:=]\\s*(.*)$");
        List<String> hallazgos = new ArrayList<>();
        for (Path p : ModuloFuente.fuentesJava()) {
            String t = ModuloFuente.texto(p);
            for (String x : List.of("DriverManager.getConnection", "BasicDataSource", "setPassword(\"", "setUsername(\"", "connections.properties"))
                if (t.contains(x)) hallazgos.add(p.getFileName() + ": " + x);
        }
        for (Path p : ModuloFuente.recursos()) {
            String n = p.getFileName().toString();
            if (!(n.endsWith(".yml") || n.endsWith(".yaml") || n.endsWith(".properties"))) continue;
            Matcher m = clave.matcher(ModuloFuente.texto(p));
            while (m.find()) {
                String v = m.group(3).trim().replaceAll("^[\"']|[\"']$", "");
                if (!(v.isEmpty() || v.matches("\\$\\{[A-Za-z0-9_.\\-]+}"))) hallazgos.add(n + ": " + m.group(1) + " is not an env placeholder without default");
            }
        }
        assertThat(hallazgos).isEmpty();
    }
}
