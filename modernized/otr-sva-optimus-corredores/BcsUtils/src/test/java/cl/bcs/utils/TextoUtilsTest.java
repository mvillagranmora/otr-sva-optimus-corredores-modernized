package cl.bcs.utils;

import cl.bcs.utils.equivalencia.Golden;
import cl.bcs.utils.equivalencia.Golden.Caso;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure string helpers: StringUtils (UTIL-10, UTIL-11; legacy StringUtils.java:6-26) and the pure string functions of
 * Utils: obfuscateEmail (UTIL-12, Utils.java:725-742), emailValidate (UTIL-13, :923-934), codificarEvento /
 * decodificarEvento (UTIL-14, :1366-1377), appendKeys (UTIL-15, :1405-1412), generateToken / generateAlphanumericToken
 * (UTIL-16, :576-603) and StreamUtils.parallelFlatmap (UTIL-17, StreamUtils.java:10-15). Golden: golden/any/strings.json.
 */
class TextoUtilsTest {

    static final String AREA = "strings";

    static List<String> lista(JsonElement e) {
        if (e == null || e.isJsonNull()) return null;
        List<String> l = new ArrayList<>();
        for (JsonElement x : e.getAsJsonArray()) l.add(x.isJsonNull() ? null : x.getAsString());
        return l;
    }

    static List<Object> listaObj(JsonElement e) {
        List<Object> l = new ArrayList<>();
        for (JsonElement x : e.getAsJsonArray()) l.add(x.getAsJsonPrimitive().isNumber() ? (Object) x.getAsInt() : x.getAsString());
        return l;
    }

    static List<String> alfabeto(String s) {
        TreeSet<String> t = new TreeSet<>();
        for (char c : s.toCharArray()) t.add(String.valueOf(c));
        return new ArrayList<>(t);
    }

    static String sha256(byte[] b) throws Exception {
        StringBuilder s = new StringBuilder();
        for (byte x : MessageDigest.getInstance("SHA-256").digest(b)) s.append(String.format("%02x", x));
        return s.toString();
    }

    Callable<Object> moderno(Caso c) {
        String fn = c.s("fn");
        return switch (fn) {
            case "join" -> () -> StringUtils.join(c.s("sep"), lista(c.input().get("items")));
            case "isNullOrBlank" -> () -> StringUtils.isNullOrBlank(c.s("s"));
            case "obfuscateEmail" -> () -> Utils.obfuscateEmail(c.s("email"));
            case "emailValidate" -> () -> Utils.emailValidate(c.s("email"));
            case "codificarEvento" -> () -> Utils.codificarEvento(c.s("idEvento"));
            case "decodificarEvento" -> () -> Utils.decodificarEvento(c.s("codificado"));
            case "appendKeys" -> () -> {
                List<String> k = lista(c.input().get("llaves"));
                return Utils.appendKeys(k == null ? null : k.toArray(new String[0]));
            };
            case "generateToken.length" -> () -> Utils.generateToken((int) c.l("size")).length();
            case "generateAlphanumericToken.length" -> () -> Utils.generateAlphanumericToken((int) c.l("size")).length();
            case "generateToken.alphabet" -> () -> alfabeto(Utils.generateToken((int) c.l("size")));
            case "generateAlphanumericToken.alphabet" -> () -> alfabeto(Utils.generateAlphanumericToken((int) c.l("size")));
            case "parallelFlatmap" -> () -> {
                if (c.input().get("t").isJsonPrimitive()) { // "0..299" x [x,y,z]
                    List<Integer> big = new ArrayList<>();
                    for (int i = 0; i < 300; i++) big.add(i);
                    List<String> r = StreamUtils.parallelFlatmap(big, lista(c.input().get("s")), (t, s) -> t + s).collect(Collectors.toList());
                    return Golden.map("size", r.size(), "first", r.subList(0, 7), "last", r.get(r.size() - 1),
                            "sha256", sha256(String.join(",", r).getBytes(StandardCharsets.UTF_8)));
                }
                List<Object> t = listaObj(c.input().get("t"));
                List<Object> s = listaObj(c.input().get("s"));
                return StreamUtils.parallelFlatmap(t, s, (a, b) -> "" + a + b).collect(Collectors.toList());
            };
            default -> throw new AssertionError("unmapped golden fn " + fn + " in " + c.id());
        };
    }

    @TestFactory
    @DisplayName("UTIL-10..17 equivalence: string helpers == legacy for every recorded input")
    Stream<DynamicTest> util10a17_equivalencia() {
        return Golden.casos("any", AREA).stream().map(c -> DynamicTest.dynamicTest(c.nombre(), () -> Golden.igual(AREA, c, moderno(c))));
    }

    @AfterAll
    static void reporte() {
        System.out.println("equivalence cases executed [" + AREA + "]: " + Golden.ejecutados(AREA));
    }

    @Test
    @DisplayName("UTIL-10 legacy quirk pinned: join() skips the separator while the accumulator is still empty (leading \"\" items vanish)")
    void util10_joinAcumuladorVacio() {
        assertThat(StringUtils.join(",", Arrays.asList("", "", "c"))).isEqualTo("c");
        assertThat(StringUtils.join(",", Arrays.asList("a", "", "c"))).isEqualTo("a,,c");
        assertThat(StringUtils.join(",", Arrays.asList(null, "b"))).isEqualTo("null,b");
        assertThat(StringUtils.join(null, List.of("a", "b"))).isEqualTo("anullb");
        assertThatThrownBy(() -> StringUtils.join(",", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("UTIL-11: isNullOrBlank uses String.trim(): NUL counts as blank, NBSP and EM SPACE do not")
    void util11_trimDeJava() {
        assertThat(StringUtils.isNullOrBlank("\u0000")).isTrue();
        assertThat(StringUtils.isNullOrBlank(" ")).isFalse();
        assertThat(StringUtils.isNullOrBlank(" ")).isFalse();
    }

    @Test
    @DisplayName("UTIL-12: obfuscateEmail keeps first char, '@', char after '@', last '.', char after last '.'")
    void util12_ofuscar() {
        assertThat(Utils.obfuscateEmail("correo@dominio.cl")).isEqualTo("c*****@d******.c*");
        assertThat(Utils.obfuscateEmail("user.name@sub.domain.com")).isEqualTo("u********@s*********.c**");
        assertThat(Utils.obfuscateEmail(null)).isEqualTo("");
        assertThat(Utils.obfuscateEmail("a")).isEqualTo("a");
    }

    @Test
    @DisplayName("UTIL-12 legacy defect pinned: obfuscateEmail works on UTF-16 units and splits a surrogate pair")
    void util12_parSustituto() {
        assertThat(Utils.obfuscateEmail("😀x@y.cl")).isEqualTo("\ud83d**@y.c*");
    }

    @Test
    @DisplayName("UTIL-14: codificarEvento left-pads to 10 with spaces then Base64; decodificar trims; null encodes the text \"null\"")
    void util14_eventos() {
        assertThat(Utils.codificarEvento("123")).isEqualTo("ICAgICAgIDEyMw==");
        assertThat(Utils.codificarEvento(null)).isEqualTo("ICAgICAgbnVsbA==");
        assertThat(Utils.decodificarEvento("ICAgICAgIDEyMw==")).isEqualTo("123");
        assertThat(Utils.decodificarEvento("!!!")).isEqualTo("");
        assertThatThrownBy(() -> Utils.decodificarEvento(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("UTIL-15: appendKeys appends '-' after EVERY key, including the last")
    void util15_appendKeys() {
        assertThat(Utils.appendKeys("a", "b")).isEqualTo("a-b-");
        assertThat(Utils.appendKeys()).isEqualTo("");
    }

    @Test
    @DisplayName("UTIL-16: token alphabets are exactly the legacy dictionaries (no vowels in generateToken)")
    void util16_alfabetos() {
        assertThat(String.join("", alfabeto(Utils.generateToken(100_000))))
                .isEqualTo("!#$%&0123456789=?@BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz");
        assertThat(String.join("", alfabeto(Utils.generateAlphanumericToken(100_000))))
                .isEqualTo("0123456789abcdefghijklmnopqrstuvwxyz");
        assertThat(Utils.generateToken(-1)).isEmpty();
        assertThat(Utils.generateToken(32)).isNotEqualTo(Utils.generateToken(32));
    }

    @Test
    @DisplayName("UTIL-17: parallelFlatmap yields the cartesian product in t-major encounter order")
    void util17_orden() {
        assertThat(StreamUtils.parallelFlatmap(List.of("a", "b"), List.of(1, 2), (t, s) -> t + s).toList()).containsExactly("a1", "a2", "b1", "b2");
        assertThat(StreamUtils.parallelFlatmap(List.<String>of(), List.of(1), (t, s) -> t + s).toList()).isEmpty();
    }

    @Test
    @DisplayName("UTIL-13: emailValidate needs a 2+ letter TLD, rejects non-ASCII and accepts '-' leading a domain label")
    void util13_email() {
        assertThat(Utils.emailValidate("a@b.co")).isTrue();
        assertThat(Utils.emailValidate("a@b.c")).isFalse();
        assertThat(Utils.emailValidate("ñ@b.cl")).isFalse();
        assertThat(Utils.emailValidate("a@-b.cl")).isTrue();
        assertThatThrownBy(() -> Utils.emailValidate(null)).isInstanceOf(NullPointerException.class);
    }
}
