package cl.bcs.apv.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import cl.bcs.apv.api.casouso.CasosUso;

/**
 * Reflection invariants of the module: the use case interfaces are public and stateless, and the whole module depends on the JDK and gson only
 * (a contract library that every other APV module compiles against must not drag a framework, a driver or a legacy class).
 *
 * <p>Rule ids: B-APVAPI-19 (interfaces: public, no fields, no default/static/private methods, one abstract method, no type parameters, no nested types),
 * B-APVAPI-20 (imports of every main source: java.*, com.google.gson.* and cl.bcs.apv.api.* only; no Spring, no JDBC, no cl.bcs outside this module),
 * B-APVAPI-21 (records are immutable value types: public, final, no extra instance fields, no setters, no annotations needed by a framework).
 */
class ApvApiReflexionInterfacesTest {

    // B-APVAPI-19
    @Test
    void b_apvapi_19_las_interfaces_de_casouso_son_publicas_y_sin_estado() {
        assertEquals(64, CasosUso.TODOS.size());
        for (Class<?> c : CasosUso.TODOS) {
            String n = c.getName();
            assertTrue(c.isInterface() && Modifier.isPublic(c.getModifiers()), n + ": public interface");
            assertEquals(0, c.getDeclaredFields().length, n + ": no constants or state");
            assertEquals(0, c.getTypeParameters().length, n + ": not generic itself");
            assertEquals(0, c.getDeclaredClasses().length, n + ": no nested types");
            assertEquals(1, c.getInterfaces().length, n + ": extends only CasoUso");
            assertEquals(CasoUso.class, c.getInterfaces()[0], n);
            assertEquals(0, c.getDeclaredAnnotations().length, n + ": no annotations (framework independent)");
            List<Method> metodos = java.util.Arrays.stream(c.getDeclaredMethods()).filter(x -> !x.isBridge() && !x.isSynthetic()).toList();
            assertEquals(1, metodos.size(), n + ": one method");
            Method m = metodos.get(0);
            assertTrue(Modifier.isAbstract(m.getModifiers()) && Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers()) && !m.isDefault(),
                    n + ": ejecutar must be a plain abstract method");
            assertEquals(0, m.getExceptionTypes().length, n + ": no checked exceptions");
        }
        // the typed entry point of the JavaCall that is not an operation: also public and stateless
        Class<?> l = cl.bcs.apv.api.logica.ValidarMandatosPACLogica.class;
        assertTrue(l.isInterface() && Modifier.isPublic(l.getModifiers()) && l.getDeclaredFields().length == 0 && l.getDeclaredMethods().length == 1);
    }

    // B-APVAPI-20
    @Test
    void b_apvapi_20_el_modulo_solo_depende_del_JDK_y_de_gson() throws IOException {
        Path base = LegacyApv.modulo().resolve("src/main/java");
        Pattern imp = Pattern.compile("(?m)^\\s*import\\s+(?:static\\s+)?([\\w.]+)(?:\\.\\*)?\\s*;");
        List<String> prohibidos = new ArrayList<>();
        TreeSet<String> usados = new TreeSet<>();
        int archivos = 0;
        try (Stream<Path> s = Files.walk(base)) {
            for (Path p : s.filter(x -> x.toString().endsWith(".java")).toList()) {
                archivos++;
                Matcher m = imp.matcher(LegacyApv.leer(p));
                while (m.find()) {
                    String i = m.group(1);
                    usados.add(i.startsWith("cl.bcs.") ? "cl.bcs.apv.api.*" : i.replaceAll("\\.[A-Z]\\w*$", ".*"));
                    boolean ok = i.startsWith("java.util.") || i.startsWith("java.math.") || i.startsWith("java.time.")
                            || i.equals("com.google.gson.JsonObject") || i.startsWith("cl.bcs.apv.api.");
                    if (!ok) {
                        prohibidos.add(base.relativize(p) + ": " + i);
                    }
                }
            }
        }
        assertTrue(archivos > 300, "main sources scanned: " + archivos);
        assertTrue(prohibidos.isEmpty(), "imports outside java.util/math/time, gson JsonObject and this module:\n  " + String.join("\n  ", prohibidos.stream().limit(20).toList()));
        System.out.println("[B-APVAPI-20] files=" + archivos + " import families=" + usados);
        // the compiled classes agree: nothing of them references a third party package other than gson
        Path clases = LegacyApv.modulo().resolve("target/classes");
        assertTrue(Files.isDirectory(clases), "target/classes must exist when tests run");
        {
            try (Stream<Path> s = Files.walk(clases)) {
                List<Path> cls = s.filter(x -> x.toString().endsWith(".class")).toList();
                assertTrue(cls.size() >= 299, "compiled classes: " + cls.size());
                for (Path c : cls) {
                    String txt = new String(Files.readAllBytes(c), java.nio.charset.StandardCharsets.ISO_8859_1);
                    assertFalse(txt.contains("org/springframework") || txt.contains("java/sql/") || txt.contains("javax/") || txt.contains("jakarta/"),
                            c + ": references a framework / JDBC / servlet type");
                }
            }
        }
    }

    // B-APVAPI-21
    @Test
    void b_apvapi_21_los_DTO_son_records_publicos_inmutables_sin_anotaciones() throws Exception {
        GrafoApv g = GrafoApv.get();
        for (GrafoApv.Par p : g.pares) {
            Class<?> r = p.record();
            assertTrue(r.isRecord() && Modifier.isPublic(r.getModifiers()) && Modifier.isFinal(r.getModifiers()), r.getName());
            assertEquals(0, r.getDeclaredAnnotations().length, r.getName() + ": no annotations");
            long instancia = List.of(r.getDeclaredFields()).stream().filter(f -> !Modifier.isStatic(f.getModifiers())).count();
            assertEquals(r.getRecordComponents().length, instancia, r.getName() + ": no instance state beyond the components");
            for (Method m : r.getDeclaredMethods()) {
                assertFalse(m.getName().startsWith("set"), r.getName() + ": mutator " + m.getName());
            }
            assertEquals(r.getPackageName(), "cl.bcs.apv.api.dto." + p.caso().toLowerCase(java.util.Locale.ROOT), r.getName());
        }
        List<String> packages = g.pares.stream().map(x -> x.record().getPackageName()).distinct().sorted().collect(Collectors.toList());
        assertEquals(64, packages.size(), "one dto package per operation");
    }
}
