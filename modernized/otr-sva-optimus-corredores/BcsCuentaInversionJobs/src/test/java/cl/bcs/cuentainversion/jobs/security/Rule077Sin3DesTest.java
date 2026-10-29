package cl.bcs.cuentainversion.jobs.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RULE-077 (P0, reviewer verdict 2026-10-02: "production credentials must not be stored with Static3DES"): the legacy
 * Job Manager decrypted job datasource credentials with OpenEJB {@code Static3DES} and its public key
 * (BcsJobManager CypherEncryption.java:11). Jobs now run in-process on the Spring datasource, so this module must not
 * carry that cipher in any form. Real checks, not mentions:
 * <ol>
 *   <li>the COMPILED classes' constant pools contain no 3DES / OpenEJB cipher identifier;</li>
 *   <li>the TEST RUNTIME classpath (= the module's dependencies, Servicios included) cannot load the OpenEJB ciphers and
 *       holds no openejb/tomee jar;</li>
 *   <li>configuration holds no cipher selector and no encrypted-value wrapper.</li>
 * </ol>
 */
class Rule077Sin3DesTest {

    static final List<String> MARCAS = List.of("DESede", "TripleDES", "Static3DES", "PasswordCipher", "OpenEJB5PasswordCipherKey",
            "org/apache/openejb", "org.apache.openejb", "DES/ECB", "DES/CBC", "CypherEncryption");

    private static boolean contiene(byte[] datos, byte[] patron) {
        outer:
        for (int i = 0; i <= datos.length - patron.length; i++) {
            for (int j = 0; j < patron.length; j++) if (datos[i + j] != patron[j]) continue outer;
            return true;
        }
        return false;
    }

    @Test
    @DisplayName("RULE-077: no 3DES / OpenEJB cipher identifier in the constant pool of any compiled class of the module")
    void rule077_bytecodeSinTripleDes() {
        List<String> hallazgos = new ArrayList<>();
        List<Path> clases = ModuloFuente.clasesCompiladas();
        for (Path p : clases) {
            byte[] b = ModuloFuente.bytes(p);
            for (String m : MARCAS) if (contiene(b, m.getBytes(StandardCharsets.UTF_8))) hallazgos.add(ModuloFuente.nombreClase(p) + ": " + m);
        }
        assertThat(clases).hasSizeGreaterThanOrEqualTo(ModuloFuente.CLASES_OBLIGATORIAS.size());
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("RULE-077: the scanner itself detects a 3DES identifier (self-check: the bytecode scan is not blind)")
    void rule077_detectorFunciona() {
        byte[] conDes = "xx\u0001\u0000\u0006DESede/CBC/PKCS5Padding".getBytes(StandardCharsets.UTF_8);
        assertThat(contiene(conDes, "DESede".getBytes(StandardCharsets.UTF_8))).isTrue();
        assertThat(contiene("AES/GCM/NoPadding".getBytes(StandardCharsets.UTF_8), "DESede".getBytes(StandardCharsets.UTF_8))).isFalse();
    }

    @ParameterizedTest(name = "RULE-077: {0} is not loadable from the module runtime classpath")
    @ValueSource(strings = {
            "org.apache.openejb.cipher.StaticDESPasswordCipher",
            "org.apache.openejb.cipher.PasswordCipherFactory",
            "org.apache.openejb.cipher.PasswordCipher",
            "org.apache.openejb.resource.jdbc.cipher.StaticDESPasswordCipher",
            "org.apache.openejb.resource.jdbc.PasswordCipher",
            "cl.bcs.scheduler.tools.CypherEncryption"})
    void rule077_classpathSinCifradoOpenEjb(String clase) {
        assertThatThrownBy(() -> Class.forName(clase, false, getClass().getClassLoader()))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("RULE-077: no openejb / tomee jar on the runtime classpath")
    void rule077_sinJarsTomee() throws Exception {
        // Every jar/directory the class loader sees (surefire may hide them behind a manifest-only booter jar).
        List<String> origenes = new ArrayList<>();
        var urls = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (urls.hasMoreElements()) origenes.add(urls.nextElement().toString().toLowerCase(Locale.ROOT));
        origenes.addAll(Arrays.asList(System.getProperty("java.class.path").toLowerCase(Locale.ROOT).split(File.pathSeparator)));
        assertThat(origenes).as("class path origins").anyMatch(s -> s.contains("spring-core"));
        assertThat(origenes).noneMatch(s -> s.contains("openejb") || s.contains("tomee"));
    }

    @Test
    @DisplayName("RULE-077: configuration has no cipher selector and no encrypted-value wrapper (Static3DES, PasswordCipher, ENC(...), {cipher})")
    void rule077_configuracionSinValoresCifrados() {
        List<String> hallazgos = new ArrayList<>();
        List<Path> archivos = new ArrayList<>(ModuloFuente.recursos());
        archivos.addAll(ModuloFuente.fuentesJava());
        for (Path p : archivos) {
            String t = ModuloFuente.texto(p);
            for (String m : List.of("Static3DES", "PasswordCipher", "ENC(", "{cipher}", "DESede", "TripleDES", "OpenEJB5PasswordCipherKey")) {
                if (t.contains(m)) hallazgos.add(p.getFileName() + ": " + m);
            }
        }
        assertThat(hallazgos).isEmpty();
    }
}
