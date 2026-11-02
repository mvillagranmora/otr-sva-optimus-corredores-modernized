package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * F3 W3 T4-C, structure and security of what the step adds. (1) The new production classes hold no mutable static state (a static field is a final String / primitive / boxed constant) and the handler and the
 * adapter hold only final instance fields. (2) Every file the step writes (the tests, the bridge, the assumed API) and every new production source uses LF line endings. (3) RULE-076: nothing here carries a
 * real id, URL or credential, and nothing calls the legacy {@code main()}. Patterns are assembled from pieces so that this file does not match itself.
 *
 * <p>RED BEFORE CODE: the first part needs the production classes ("T4-C not implemented"); the parts about the test files do not and pass today.
 */
class CerT4cEstructuraTest {

    private static final Path TEST_CER = Path.of("src/test/java/cl/bcs/fondomutuo/certificacion");
    private static final Path TEST_AR = Path.of("src/test/java/cl/bcs/fondomutuo/aportereinversion");
    private static final Path MAIN = Path.of("src/main/java/cl/bcs/fondomutuo");

    private static final List<Path> ARCHIVOS_DEL_PASO = List.of(TEST_CER.resolve("ApiT4c.java"), TEST_CER.resolve("API_ASSUMED_T4C.md"), TEST_CER.resolve("CerT4cHandlerTest.java"),
            TEST_CER.resolve("CerT4cEquivalenciaTest.java"), TEST_CER.resolve("CerT4cCableadoTest.java"), TEST_CER.resolve("CerT4cEstructuraTest.java"), TEST_AR.resolve("CerT4cAdaptadorArTest.java"));

    private static List<Path> fuentesNuevas() {
        return List.of(MAIN.resolve("certificacion/CertificarAporteRescateJavaCallHandler.java"), MAIN.resolve("certificacion/CertificarAporteRescateConfiguracion.java"),
                MAIN.resolve("aportereinversion/AporteReinversionJsonAdapter.java"), MAIN.resolve("aportereinversion/AporteReinversionConfiguracion.java"));
    }

    private static boolean inmutable(Class<?> t) {
        return t.isPrimitive() || t == String.class || t == Boolean.class || t == Integer.class || t == Long.class;
    }

    @Test
    @DisplayName("T4-C: no mutable static state in the new classes (handler, adapter, both configurations); handler and adapter hold only final instance fields")
    void sinEstadoMutable() {
        for (String nombre : List.of(ApiT4c.HANDLER, ApiT4c.CONFIGURACION, ApiT4c.ADAPTADOR, ApiT4c.CONFIGURACION_AR)) {
            Class<?> c = ApiT4c.cargar(nombre);
            for (Field f : c.getDeclaredFields()) {
                if (f.isSynthetic()) continue;
                if (Modifier.isStatic(f.getModifiers())) {
                    assertTrue(Modifier.isFinal(f.getModifiers()) && inmutable(f.getType()), nombre + ": static field " + f + " must be a final String / primitive constant");
                } else if (nombre.equals(ApiT4c.HANDLER) || nombre.equals(ApiT4c.ADAPTADOR)) {
                    assertTrue(Modifier.isFinal(f.getModifiers()), nombre + ": instance field " + f + " must be final");
                }
            }
        }
    }

    @Test
    @DisplayName("T4-C: the files of the step use LF line endings (tests, bridge, assumed API; and the new production sources once they exist)")
    void finesDeLineaLf() throws IOException {
        List<String> conCr = new ArrayList<>();
        for (Path p : ARCHIVOS_DEL_PASO) {
            assertTrue(Files.exists(p), "missing file of the step: " + p);
            if (Files.readString(p, StandardCharsets.UTF_8).indexOf('\r') >= 0) conCr.add(p.toString());
        }
        for (Path p : fuentesNuevas()) {
            if (Files.exists(p) && Files.readString(p, StandardCharsets.UTF_8).indexOf('\r') >= 0) conCr.add(p.toString());
        }
        assertTrue(conCr.isEmpty(), "CR found (use LF): " + conCr);
        ApiT4c.cargar(ApiT4c.HANDLER);   // red before code: the production sources must exist for this check to mean anything
        for (Path p : fuentesNuevas()) assertTrue(Files.exists(p), "missing production source: " + p);
    }

    @Test
    @DisplayName("T4-C RULE-076: no URL with a scheme, no credential-like literal, no connection string and no call to the legacy main() in the files of the step or in the new production sources")
    void sinSecretosNiMain() throws IOException {
        String esquema = "ht" + "tps?:" + "//";
        List<Pattern> prohibidos = List.of(Pattern.compile(esquema), Pattern.compile("(?i)(pass" + "word|pass" + "wd|secr" + "et|api[_-]?k" + "ey)\\s*[=:]\\s*\"[^\"]{4,}"), Pattern.compile("Bear" + "er\\s+[A-Za-z0-9._-]{8,}"),
                Pattern.compile("jd" + "bc:(?!h2:mem)"), Pattern.compile("\\.ma" + "in\\s*\\(\\s*(new\\s+String|args|null)"));
        List<String> hallazgos = new ArrayList<>();
        List<Path> todos = new ArrayList<>(ARCHIVOS_DEL_PASO);
        for (Path p : fuentesNuevas()) if (Files.exists(p)) todos.add(p);
        for (Path p : todos) {
            String s = Files.readString(p, StandardCharsets.UTF_8);
            for (Pattern pat : prohibidos) {
                if (pat.matcher(s).find()) hallazgos.add(p.getFileName() + " ~ " + pat);
            }
        }
        assertEquals(List.of(), hallazgos, "RULE-076: no real id / URL / credential and no legacy main()");
        assertFalse(ARCHIVOS_DEL_PASO.isEmpty());
    }
}
