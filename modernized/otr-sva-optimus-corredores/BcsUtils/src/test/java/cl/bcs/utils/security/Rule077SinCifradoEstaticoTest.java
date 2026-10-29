package cl.bcs.utils.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RULE-077 (P0, reviewer verdict 2026-10-02: "production credentials must not be stored with Static3DES ... replace with
 * proper encryption with a managed key"). In BcsUtils the shapes are: {@code Crypto} (fixed key from
 * {@code CryptoUtils.CRYPTO_KEY}, fixed IV, Crypto.java:15-98) and {@code JsonWebToken}'s hard-coded HMAC key
 * (JsonWebToken.java:26). Neither Crypto nor JsonWebToken (dead code, decision 5) is ported. Real checks on bytecode and sources; no
 * legacy key value appears in this file.
 */
class Rule077SinCifradoEstaticoTest {

    static final List<String> MARCAS = List.of("DESede", "TripleDES", "Static3DES", "PasswordCipher", "OpenEJB5PasswordCipherKey",
            "org/apache/openejb", "DES/ECB", "DES/CBC", "CypherEncryption", "CRYPTO_KEY", "javax/crypto/Cipher", "IvParameterSpec",
            "cl/bcs/utils/Crypto", "cl/bcs/utils/CryptoUtils");

    @Test
    @DisplayName("RULE-077: no 3DES / OpenEJB / static-key cipher identifier in the constant pool of any compiled class")
    void rule077_bytecodeSinCifradoEstatico() {
        List<String> hallazgos = new ArrayList<>();
        List<Path> clases = ModuloFuente.clasesCompiladas();
        for (Path p : clases) {
            byte[] b = ModuloFuente.bytes(p);
            for (String m : MARCAS) if (ModuloFuente.contiene(b, m.getBytes(StandardCharsets.UTF_8))) hallazgos.add(ModuloFuente.nombreClase(p) + ": " + m);
        }
        assertThat(clases).hasSizeGreaterThanOrEqualTo(ModuloFuente.CLASES_OBLIGATORIAS.size());
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("RULE-077: the bytecode scanner detects an identifier (self-check: the scan is not blind)")
    void rule077_detectorFunciona() {
        assertThat(ModuloFuente.contiene("xx\u0001\u0000\u0006DESede/CBC/PKCS5Padding".getBytes(StandardCharsets.UTF_8), "DESede".getBytes(StandardCharsets.UTF_8))).isTrue();
        assertThat(ModuloFuente.contiene("AES/GCM/NoPadding".getBytes(StandardCharsets.UTF_8), "DESede".getBytes(StandardCharsets.UTF_8))).isFalse();
    }

    @Test
    @DisplayName("RULE-077: no key material in sources: SecretKeySpec/IvParameterSpec never built from a literal or a constant")
    void rule077_sinClaveLiteralEnFuentes() {
        Pattern spec = Pattern.compile("new\\s+(SecretKeySpec|IvParameterSpec)\\s*\\(\\s*(\"|[A-Z][A-Z0-9_]{2,}\\b)");
        Pattern campo = Pattern.compile("(?i)static\\s+final\\s+(String|char\\[\\]|byte\\[\\])\\s+\\w*(key|secret|password|passwd|pwd|clave|iv)\\w*\\s*=\\s*\"[^\"]+\"");
        List<String> hallazgos = new ArrayList<>();
        for (Path p : ModuloFuente.fuentesJava()) {
            String t = ModuloFuente.texto(p);
            Matcher a = spec.matcher(t);
            if (a.find()) hallazgos.add(p.getFileName() + ": " + a.group(1) + " from literal/constant");
            Matcher b = campo.matcher(t);
            if (b.find()) hallazgos.add(p.getFileName() + ": static key-like field with literal value");
        }
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("RULE-077: no compiled class has a static String/char[]/byte[] field named like a key, secret, password or IV")
    void rule077_sinCampoClaveEstatico() throws Exception {
        Pattern nombre = Pattern.compile("(?i).*(key|secret|password|passwd|pwd|clave|^iv$|_iv$).*");
        List<String> hallazgos = new ArrayList<>();
        for (Path p : ModuloFuente.clasesCompiladas()) {
            Class<?> c = Class.forName(ModuloFuente.nombreClase(p), false, getClass().getClassLoader());
            for (Field f : c.getDeclaredFields()) {
                boolean tipo = f.getType() == String.class || f.getType() == char[].class || f.getType() == byte[].class;
                if (Modifier.isStatic(f.getModifiers()) && tipo && nombre.matcher(f.getName()).matches()
                        && !f.getName().matches("ACC_NUM_\\w+|.*PARAM.*")) hallazgos.add(c.getName() + "." + f.getName());
            }
        }
        assertThat(hallazgos).isEmpty();
    }

    @ParameterizedTest(name = "RULE-077: {0} is not part of the library")
    @ValueSource(strings = {"cl.bcs.utils.Crypto", "cl.bcs.utils.CryptoUtils", "org.apache.openejb.cipher.StaticDESPasswordCipher",
            "org.apache.openejb.cipher.PasswordCipherFactory", "cl.bcs.scheduler.tools.CypherEncryption"})
    void rule077_clasesAusentes(String clase) {
        assertThatThrownBy(() -> Class.forName(clase, false, getClass().getClassLoader())).isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("RULE-077: no openejb / tomee jar on the runtime classpath")
    void rule077_sinJarsTomee() throws Exception {
        List<String> origenes = new ArrayList<>();
        var urls = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (urls.hasMoreElements()) origenes.add(urls.nextElement().toString().toLowerCase(Locale.ROOT));
        origenes.addAll(Arrays.asList(System.getProperty("java.class.path").toLowerCase(Locale.ROOT).split(File.pathSeparator)));
        assertThat(origenes).as("class path origins").anyMatch(s -> s.contains("gson"));
        assertThat(origenes).noneMatch(s -> s.contains("openejb") || s.contains("tomee"));
    }
}
