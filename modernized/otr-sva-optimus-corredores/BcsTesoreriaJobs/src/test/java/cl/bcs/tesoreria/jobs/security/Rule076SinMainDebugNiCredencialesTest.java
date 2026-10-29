package cl.bcs.tesoreria.jobs.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RULE-076 (P0, reviewer verdict 2026-10-02: "credentials must be rotated ... and the main() method must be removed from
 * source"): no debug {@code main()}, no debug connection, no hard-coded credential in this module's production code.
 *
 * <p>Legacy shapes this guards against, in BcsTesoreriaJobs: {@code main()} blocks opening
 * {@code PlataformaUtils.getPropertiesConnection("desa02" | "desa" | "desa2", "connections.properties" | a user path)}
 * (CentralizaMultiSeleccionJob.java:122-140, InterfazContable.java:262-273, GenerarInformeClienteRelacionadoBatch.java:76-85),
 * {@code getDESADatasource()} / {@code getDESAPEB1Datasource()} (CreaInterfazDiaria.java:111, :246), hard-coded debug
 * inputs ({@code logica(con, debug)}, CentralizaMultiSeleccionJob.java:47-56; {@code DEBUG} -> "./", InterfazContable.java:42,85),
 * credential files read from the working directory ({@code CreaInterfazSIGA.properties}, {@code interfazSAP.properties}),
 * and commented-out credential blocks (CreaInterfazDiaria.java:42-47, InterfazContable.java:97-99). None of those values
 * is repeated here: the checks are on shapes, not on the secrets.
 */
class Rule076SinMainDebugNiCredencialesTest {

    @Test
    @DisplayName("RULE-076: the only main() in the compiled module is the @SpringBootApplication entry point")
    void rule076_unicoMainEsSpringBootApplication() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        List<String> conMain = new ArrayList<>();
        for (Path p : ModuloFuente.clasesCompiladas()) {
            String nombre = ModuloFuente.nombreClase(p);
            if (nombre.endsWith("package-info") || nombre.endsWith("module-info")) continue;
            Class<?> c = Class.forName(nombre, false, cl);
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals("main") && Modifier.isStatic(m.getModifiers())) {
                    conMain.add(nombre + (c.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class) ? "" : " [NOT @SpringBootApplication]"));
                }
            }
        }
        assertThat(conMain).as("static main() methods").hasSize(1).noneMatch(s -> s.contains("NOT @SpringBootApplication"));
    }

    @Test
    @DisplayName("RULE-076: no debug connection or credential file: connections.properties, getPropertiesConnection, desa*, getDESA*, *.properties from the working directory")
    void rule076_sinConexionDebug() {
        List<String> prohibidos = List.of("connections.properties", "getPropertiesConnection", "desa02", "\"desa2\"", "\"desa\"",
                "getDESA", "CreaInterfazSIGA.properties", "interfazSAP.properties", "Interfaz.properties", "application.properties\"",
                "C:\\\\Users", "C:/Users", "PlataformaUtils", "new FileInputStream(");
        List<Path> archivos = new ArrayList<>(ModuloFuente.fuentesJava());
        archivos.addAll(ModuloFuente.recursos());
        List<String> hallazgos = new ArrayList<>();
        for (Path p : archivos) {
            String t = ModuloFuente.texto(p);
            for (String x : prohibidos) if (t.contains(x)) hallazgos.add(p.getFileName() + ": " + x);
        }
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("RULE-076: no debug switch ported (no `boolean debug` parameter, no DEBUG field) in the job classes")
    void rule076_sinModoDebug() {
        Pattern debug = Pattern.compile("boolean\\s+debug\\b|\\bDEBUG\\s*=|\\bboolean\\s+DEBUG\\b");
        List<String> hallazgos = new ArrayList<>();
        for (Path p : ModuloFuente.fuentesJava()) {
            if (debug.matcher(ModuloFuente.texto(p)).find()) hallazgos.add(p.getFileName().toString());
        }
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("RULE-076: no credential literal in Java sources (password/clave/secret/token = \"...\" must be empty, an ENV_VAR name or redacted)")
    void rule076_sinCredencialesEnJava() {
        Pattern asignacion = Pattern.compile("(?i)\\b(\\w*(password|passwd|pwd|pass|clave|secret|token)\\w*)\\s*[=:,(]\\s*\"([^\"]*)\"");
        List<String> hallazgos = new ArrayList<>();
        for (Path p : ModuloFuente.fuentesJava()) {
            Matcher m = asignacion.matcher(ModuloFuente.texto(p));
            while (m.find()) {
                String v = m.group(3);
                boolean permitido = v.isEmpty() || v.matches("[A-Z][A-Z0-9_]*") || v.contains("<redacted>") || v.contains("${");
                if (!permitido) hallazgos.add(p.getFileName() + ": " + m.group(1) + " = \"" + "*".repeat(v.length()) + "\"");
            }
        }
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("RULE-076: configuration secrets are ${ENV} placeholders without inline defaults; no literal JDBC host or IPv4 address")
    void rule076_configuracionSoloDesdeEntorno() {
        Pattern clave = Pattern.compile("(?im)^\\s*([\\w.\\-]*(password|passwd|pwd|pass|clave|secret|token)[\\w.\\-]*)\\s*[:=]\\s*(.*)$");
        Pattern ipv4 = Pattern.compile("\\b(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\b");
        Pattern jdbcLiteral = Pattern.compile("jdbc:(sap|h2|oracle|postgresql)://[A-Za-z0-9]");
        List<String> hallazgos = new ArrayList<>();
        List<Path> archivos = new ArrayList<>(ModuloFuente.recursos());
        archivos.addAll(ModuloFuente.fuentesJava());
        for (Path p : archivos) {
            String t = ModuloFuente.texto(p);
            String n = p.getFileName().toString();
            if (n.endsWith(".yml") || n.endsWith(".yaml") || n.endsWith(".properties")) {
                Matcher m = clave.matcher(t);
                while (m.find()) {
                    String v = m.group(3).trim().replaceAll("^[\"']|[\"']$", "");
                    if (!(v.isEmpty() || v.matches("\\$\\{[A-Za-z0-9_.\\-]+:?}"))) hallazgos.add(n + ": " + m.group(1) + " is not an env placeholder");
                }
            }
            Matcher ip = ipv4.matcher(t);
            while (ip.find()) {
                String s = ip.group();
                if (!s.equals("127.0.0.1") && !s.equals("0.0.0.0")) hallazgos.add(n + ": IPv4 literal");
            }
            if (jdbcLiteral.matcher(t).find()) hallazgos.add(n + ": literal JDBC host");
        }
        assertThat(hallazgos).isEmpty();
    }
}
