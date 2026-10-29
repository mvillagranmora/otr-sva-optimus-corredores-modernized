package cl.bcs.fondomutuo.seguridad;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * C3 of PLAN_FONDOMUTUO_F2 4.1 (F2 W1): with tesoreria-service on the classpath as an in-process library, a resource path provided by two classpath roots
 * would be served from the first one, silently (Tesoreria loads {@code sql/CrearIngresoEgreso_*.sql}, this module {@code cl/bcs/fondomutuo/sql/...}). Every
 * resource and class of the PROJECT roots (this module's classes, tesoreria-service, bcs-utils: the {@code cl.bcs} artifacts) must be provided by exactly one
 * root of the whole classpath. Allowed, documented exceptions: {@code META-INF/**} and {@code module-info.class} (per-jar metadata) and the two shadow files
 * {@code application.properties} / {@code application.yml}, which must then resolve to THIS module (its own empty {@code application.properties} shadows
 * Tesoreria's, whose {@code services.javacall.read-timeout=PT60S} would otherwise override the 10 s of this module).
 */
class RecursosDuplicadosTest {

    private static final Set<String> SOMBRAS = Set.of("application.properties", "application.yml");

    /** Every classpath root (jar files and class directories), found through resources every root has. */
    static List<Path> raices() throws Exception {
        Set<Path> r = new LinkedHashSet<>();
        ClassLoader cl = RecursosDuplicadosTest.class.getClassLoader();
        for (URL u : Collections.list(cl.getResources("META-INF/MANIFEST.MF"))) {
            if ("jar".equals(u.getProtocol())) r.add(Path.of(((JarURLConnection) u.openConnection()).getJarFileURL().toURI()));
        }
        for (URL u : Collections.list(cl.getResources(""))) {
            if ("file".equals(u.getProtocol())) r.add(Path.of(u.toURI()));
        }
        return new ArrayList<>(r);
    }

    static List<String> entradas(Path raiz) throws IOException {
        List<String> e = new ArrayList<>();
        if (Files.isDirectory(raiz)) {
            try (Stream<Path> s = Files.walk(raiz)) {
                s.filter(Files::isRegularFile).forEach(p -> e.add(raiz.relativize(p).toString().replace('\\', '/')));
            }
        } else {
            try (JarFile j = new JarFile(raiz.toFile())) {
                j.stream().filter(x -> !x.isDirectory()).forEach(x -> e.add(x.getName()));
            }
        }
        return e;
    }

    static boolean esProyecto(Path raiz) {
        String s = raiz.toString().replace('\\', '/');
        return s.endsWith("BcsFondoMutuoServicios/target/classes") || s.contains("/cl/bcs/");
    }

    static boolean permitido(String entrada) {
        return entrada.startsWith("META-INF/") || entrada.endsWith("module-info.class") || SOMBRAS.contains(entrada);
    }

    /** resource path of a project root -> every root that provides it, when more than one (the allowed exceptions excluded). */
    static Map<String, List<Path>> duplicados(List<Path> raices) throws IOException {
        Map<String, List<Path>> donde = new TreeMap<>();
        for (Path r : raices) {
            for (String e : entradas(r)) donde.computeIfAbsent(e, k -> new ArrayList<>()).add(r);
        }
        Map<String, List<Path>> dup = new TreeMap<>();
        for (Path r : raices) {
            if (!esProyecto(r)) continue;
            for (String e : entradas(r)) {
                if (!permitido(e) && donde.get(e).size() > 1) dup.put(e, donde.get(e));
            }
        }
        return dup;
    }

    @Test
    void ningunRecursoNiClaseDelProyectoLoAportanDosRaicesDelClasspath() throws Exception {
        List<Path> raices = raices();
        assertThat(raices.stream().filter(RecursosDuplicadosTest::esProyecto).map(Path::toString))
                .as("project roots").anyMatch(s -> s.contains("tesoreria-service")).anyMatch(s -> s.replace('\\', '/').endsWith("target/classes"));
        assertThat(duplicados(raices)).as("resource paths provided twice (the first root would win silently)").isEmpty();
    }

    @Test
    void lasSombrasDeConfiguracionSeResuelvenEnEsteModulo_yTesoreriaTraeLasSuyas() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        for (String s : SOMBRAS) {
            assertThat(cl.getResource(s).toString().replace('\\', '/')).as(s).contains("BcsFondoMutuoServicios/target/classes");
        }
        assertThat(Collections.list(cl.getResources("application.properties"))).as("Tesoreria's is shadowed, not absent").hasSizeGreaterThanOrEqualTo(2);
        assertThat(Files.readString(Path.of(cl.getResource("application.properties").toURI())).lines().filter(l -> !l.isBlank() && !l.startsWith("#")))
                .as("the shadow file carries no setting").isEmpty();
        assertThat(Collections.list(cl.getResources("sql/CrearIngresoEgreso_InsertIngresoEgreso.sql"))).as("Tesoreria's SQL, once").hasSize(1);
    }

    @Test
    void elDetectorFallaConDosJarsQueAportanLaMismaRuta(@org.junit.jupiter.api.io.TempDir Path tmp) throws Exception {
        Path a = jar(tmp.resolve("cl/bcs/a.jar"), "sql/X.sql", "cl/bcs/uno/Clase.class");
        Path b = jar(tmp.resolve("otro/b.jar"), "sql/X.sql", "META-INF/LICENSE");
        Path c = jar(tmp.resolve("cl/bcs/c.jar"), "application.properties", "META-INF/LICENSE");
        Path d = jar(tmp.resolve("otro/d.jar"), "application.properties");
        assertThat(duplicados(List.of(a, b))).containsOnlyKeys("sql/X.sql");
        assertThat(duplicados(List.of(c, d))).as("documented shadow and per-jar metadata").isEmpty();
    }

    private static Path jar(Path p, String... entradas) throws IOException {
        Files.createDirectories(p.getParent());
        try (OutputStream o = Files.newOutputStream(p); JarOutputStream j = new JarOutputStream(o)) {
            for (String e : entradas) {
                j.putNextEntry(new JarEntry(e));
                j.write(1);
                j.closeEntry();
            }
        }
        return p;
    }
}
