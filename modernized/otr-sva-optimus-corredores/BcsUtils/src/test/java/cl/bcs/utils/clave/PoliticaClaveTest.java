package cl.bcs.utils.clave;

import cl.bcs.utils.equivalencia.Golden;
import cl.bcs.utils.equivalencia.Golden.Caso;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static cl.bcs.utils.equivalencia.Golden.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Password policy of ValidarNuevaClave.logica (legacy ValidarNuevaClave.java:103-228) as pure functions with the
 * policy values passed in. Golden: golden/db/password.json, recorded by running the UNMODIFIED legacy logica() on H2.
 *
 * <ul>
 *   <li>RULE-031 (P0): length, upper [A-Z], lower [a-z], "special" = {@code \W}; criteria order and texts.</li>
 *   <li>RULE-032 (P0, reviewed WRONG, 2026-10-16 decision): NEW = separate minimum-digit rule, configurable, default 1;
 *       specials stay symbols only. The legacy (digits count for nothing) is pinned as the APPROVED DEVIATION baseline:
 *       new(minDigitos=0) minus the digit criterion == legacy.</li>
 *   <li>RULE-033 (P1): legacy reads the policy of IdEmpresa=1 for every company (ValidarNuevaClave.java:107). The pure
 *       policy takes the company's values as input; WHICH company's values a caller passes is left to an SME.</li>
 *   <li>RULE-034 (P1): prohibited word = exact match of the whole password (predicate passed in).</li>
 * </ul>
 */
class PoliticaClaveTest {

    static final String AREA = "password";
    static final Set<String> NO_APLICA = Set.of("PASSWORD-198"); // getParametroSistema: a DB-row lookup helper, not ported
    /**
     * APPROVED DEVIATION (decision 4, 2026-10-16): the legacy scan loop counter is a {@code byte}
     * (ValidarNuevaClave.java:164, {@code for (byte i = 0; i < longitudClaveentrada; i++)}), so a password of 128 or more
     * characters overflows to {@code charAt(-128)} and throws StringIndexOutOfBoundsException (golden records below).
     * The new code validates it normally under the same rules: asserted in {@link #rule031_clavesLargasDesviacionAprobada()}.
     */
    static final Set<String> DESVIACION_APROBADA = Set.of("PASSWORD-065", "PASSWORD-066", "PASSWORD-067", "PASSWORD-068",
            "PASSWORD-109", "PASSWORD-110", "PASSWORD-147", "PASSWORD-148", "PASSWORD-185", "PASSWORD-186");

    static Map<String, String> config(JsonElement e) {
        Map<String, String> m = new HashMap<>();
        for (Map.Entry<String, JsonElement> x : e.getAsJsonObject().entrySet())
            m.put(x.getKey(), x.getValue().isJsonNull() || x.getValue().getAsString().equals("NULL") ? null : x.getValue().getAsString());
        return m;
    }

    static Predicate<String> prohibidas(Caso c) {
        Set<String> s = new java.util.HashSet<>();
        if (c.tiene("prohibidas")) for (JsonElement x : c.input().getAsJsonArray("prohibidas")) s.add(x.getAsString());
        else s.addAll(List.of("Prohibida1!", "clave"));
        return s::contains;
    }

    /** Legacy output shape (SalidaValidarNuevaClave). */
    static Map<String, Object> salida(ResultadoValidacion r, boolean sinCriterioDigitos) {
        List<Object> det = new ArrayList<>();
        for (MensajeSalida m : r.erroresDetalle())
            if (!(sinCriterioDigitos && m.descripcion().startsWith("Al menos 0 d"))) det.add(map("id", m.id(), "tipo", m.tipo(), "codigo", m.codigo(), "descripcion", m.descripcion()));
        List<Object> est = new ArrayList<>();
        List<EstadoCriterio> ec = r.estadoCriterios();
        for (int i = 0; i < ec.size(); i++) {
            if (sinCriterioDigitos && i == ec.size() - 1) {
                assertThat(ec.get(i).criterio()).isEqualTo("Al menos 0 dígito");
                assertThat(ec.get(i).esValido()).isTrue();
                continue;
            }
            est.add(map("criterio", ec.get(i).criterio(), "esValido", ec.get(i).esValido()));
        }
        return map("ClaveValida", r.claveValida(), "P_OUT_ERRORESCONTADOR", r.erroresContador(), "P_OUT_ERRORESDETALLE", det, "P_OUT_ESTADOCRITERIOSNUEVACLAVE", est);
    }

    static PoliticaClave politicaLegado(Caso c) {
        Map<String, String> m = config(c.input().get("empresa1")); // RULE-033: legacy always reads company 1
        m.put(PoliticaClave.ACC_NUM_DIGITOS, "0");
        return PoliticaClave.desdeConfiguracion(m);
    }

    @TestFactory
    @DisplayName("RULE-031/033/034 equivalence + RULE-032 legacy baseline: new(minDigitos=0) minus digit criterion == legacy logica()")
    Stream<DynamicTest> rule031a034_equivalencia() {
        List<Caso> casos = Golden.casos("db", AREA);
        assertThat(casos).extracting(Caso::id).containsAll(NO_APLICA).containsAll(DESVIACION_APROBADA);
        return casos.stream().filter(c -> !NO_APLICA.contains(c.id()) && !DESVIACION_APROBADA.contains(c.id())).map(c -> DynamicTest.dynamicTest(
                (c.tiene("p_IdEmpresa") && c.l("p_IdEmpresa") == 2 ? "RULE-033 " : "RULE-032 baseline ") + c.nombre(),
                () -> Golden.igual(AREA, c, () -> salida(ValidadorClave.validar(c.s("clave"), politicaLegado(c), prohibidas(c)), true))));
    }

    @TestFactory
    @DisplayName("RULE-032 NEW behavior over every legacy case: same as legacy plus 'Al menos 1 dígito' (default minimum), failing iff no ASCII digit")
    Stream<DynamicTest> rule032_nuevoSobreCasosLegado() {
        return Golden.casos("db", AREA).stream()
                .filter(c -> !NO_APLICA.contains(c.id()) && c.out().has("value") && c.es("note", null) && c.s("clave") != null)
                .map(c -> DynamicTest.dynamicTest("RULE-032 new " + c.nombre(), () -> {
                    Map<String, String> m = config(c.input().get("empresa1"));
                    ResultadoValidacion r = ValidadorClave.validar(c.s("clave"), PoliticaClave.desdeConfiguracion(m), prohibidas(c));
                    JsonObject legado = c.valor().getAsJsonObject();
                    boolean tieneDigito = c.s("clave").chars().anyMatch(ch -> ch >= '0' && ch <= '9');
                    assertThat(r.estadoCriterios()).hasSize(7);
                    assertThat(r.estadoCriterios().get(6)).isEqualTo(new EstadoCriterio("Al menos 1 dígito", tieneDigito));
                    assertThat(r.claveValida()).isEqualTo(legado.get("ClaveValida").getAsBoolean() && tieneDigito);
                    assertThat(r.erroresContador()).isEqualTo(legado.get("P_OUT_ERRORESCONTADOR").getAsLong() + (tieneDigito ? 0 : 1));
                    Golden.contar(AREA + "-rule032-new");
                }));
    }

    @AfterAll
    static void reporte() {
        System.out.println("equivalence cases executed [" + AREA + "]: " + Golden.ejecutados(AREA) + " (+ RULE-032 new-behavior checks: "
                + Golden.ejecutados(AREA + "-rule032-new") + "; approved deviation (128+ chars): " + Golden.ejecutados(AREA + "-deviation")
                + "; not applicable: " + NO_APLICA + ")");
    }

    /** Independent oracle of the legacy rules (ValidarNuevaClave.java:150-226, regex per char) with an int counter. */
    static List<Object> criteriosEsperados(String clave, PoliticaClave p, boolean prohibida) {
        long may = 0, min = 0, car = 0;
        for (int i = 0; i < clave.length(); i++) {
            String ch = String.valueOf(clave.charAt(i));
            if (ch.matches("[A-Z]")) may++;
            else if (ch.matches("[a-z]")) min++;
            else if (ch.matches("\\W")) car++;
        }
        return List.of(
                map("criterio", "La clave ingresada no puede contener palabras prohibidas.", "esValido", !prohibida),
                map("criterio", "Mínimo " + p.minLongitud() + " carácteres", "esValido", clave.length() >= p.minLongitud()),
                map("criterio", "Carácteres válidos", "esValido", true),
                map("criterio", "Al menos " + p.minMayusculas() + " mayúscula", "esValido", may >= p.minMayusculas()),
                map("criterio", "Al menos " + p.minMinusculas() + " minúscula", "esValido", min >= p.minMinusculas()),
                map("criterio", "Al menos " + p.minCaracteres() + " carácter especial", "esValido", car >= p.minCaracteres()));
    }

    @TestFactory
    @DisplayName("RULE-031 APPROVED DEVIATION (decision 4; ValidarNuevaClave.java:164 byte loop counter): passwords of 128+ chars are validated normally, not StringIndexOutOfBoundsException")
    Stream<DynamicTest> rule031_clavesLargasDesviacionAprobada() {
        return Golden.casos("db", AREA).stream().filter(c -> DESVIACION_APROBADA.contains(c.id())).map(c -> DynamicTest.dynamicTest(
                "RULE-031 approved deviation " + c.nombre(), () -> {
                    assertThat(c.out().get("throws").getAsString()).as("legacy record").isEqualTo("java.lang.StringIndexOutOfBoundsException");
                    String clave = c.s("clave");
                    assertThat(clave.length()).isGreaterThanOrEqualTo(128);
                    PoliticaClave p = politicaLegado(c);
                    boolean prohibida = prohibidas(c).test(clave);
                    Map<String, Object> r = salida(ValidadorClave.validar(clave, p, prohibidas(c)), true);
                    List<Object> esperados = criteriosEsperados(clave, p, prohibida);
                    assertThat(Golden.json(r.get("P_OUT_ESTADOCRITERIOSNUEVACLAVE"))).isEqualTo(Golden.json(esperados));
                    long fallos = esperados.stream().filter(e -> !(Boolean) ((Map<?, ?>) e).get("esValido")).count();
                    assertThat(r.get("P_OUT_ERRORESCONTADOR")).isEqualTo(fallos);
                    assertThat(r.get("ClaveValida")).isEqualTo(fallos == 0);
                    Golden.contar(AREA + "-deviation");
                }));
    }

    static PoliticaClave p(long lon, long may, long min, long car, long dig) {
        return new PoliticaClave(lon, may, min, car, dig);
    }

    static final Predicate<String> NINGUNA = s -> false;

    // ---------------------------------------------------------------- RULE-032: new behavior (defines done)

    @Test
    @DisplayName("RULE-032 new: a password without digits is rejected by the separate digit rule (legacy accepted 'Abcdefg!')")
    void rule032_sinDigitosRechazada() {
        ResultadoValidacion r = ValidadorClave.validar("Abcdefg!", p(8, 1, 1, 1, 1), NINGUNA);
        assertThat(r.claveValida()).isFalse();
        assertThat(r.erroresContador()).isEqualTo(1);
        assertThat(r.erroresDetalle()).containsExactly(new MensajeSalida(0L, "ERROR", "", "Al menos 1 dígito"));
        assertThat(r.estadoCriterios()).containsExactly(
                new EstadoCriterio("La clave ingresada no puede contener palabras prohibidas.", true),
                new EstadoCriterio("Mínimo 8 carácteres", true),
                new EstadoCriterio("Carácteres válidos", true),
                new EstadoCriterio("Al menos 1 mayúscula", true),
                new EstadoCriterio("Al menos 1 minúscula", true),
                new EstadoCriterio("Al menos 1 carácter especial", true),
                new EstadoCriterio("Al menos 1 dígito", false));
    }

    @Test
    @DisplayName("RULE-032 new: the minimum is configurable (ACC.numDigitos) and defaults to 1 when the parameter is absent")
    void rule032_minimoConfigurable() {
        Map<String, String> m = new LinkedHashMap<>(Map.of("ACC.numLongitud", "8", "ACC.numMayusculas", "1", "ACC.numMinuscula", "1", "ACC.numCaracteres", "1"));
        assertThat(PoliticaClave.desdeConfiguracion(m)).isEqualTo(p(8, 1, 1, 1, 1));
        assertThat(PoliticaClave.MIN_DIGITOS_POR_DEFECTO).isEqualTo(1L);
        m.put(PoliticaClave.ACC_NUM_DIGITOS, "3");
        PoliticaClave tres = PoliticaClave.desdeConfiguracion(m);
        assertThat(tres.minDigitos()).isEqualTo(3);
        assertThat(ValidadorClave.validar("Hola12!x", tres, NINGUNA).erroresDetalle()).extracting(MensajeSalida::descripcion).containsExactly("Al menos 3 dígito");
        assertThat(ValidadorClave.validar("Hola123!", tres, NINGUNA).claveValida()).isTrue();
        m.put(PoliticaClave.ACC_NUM_DIGITOS, "x");
        assertThatThrownBy(() -> PoliticaClave.desdeConfiguracion(m)).isInstanceOf(NumberFormatException.class);
        assertThat(PoliticaClave.ACC_NUM_DIGITOS).isEqualTo("ACC.numDigitos");
    }

    @Test
    @DisplayName("RULE-032 new: digits never count as special characters; only ASCII 0-9 count as digits")
    void rule032_digitosNoSonEspeciales() {
        ResultadoValidacion r = ValidadorClave.validar("Abcdefg1", p(8, 1, 1, 1, 1), NINGUNA);
        assertThat(r.erroresDetalle()).extracting(MensajeSalida::descripcion).containsExactly("Al menos 1 carácter especial");
        // Arabic-Indic digit three: not a digit, and (like in legacy) a \W "special"
        ResultadoValidacion u = ValidadorClave.validar("Abcdefg٣", p(8, 1, 1, 1, 1), NINGUNA);
        assertThat(u.erroresDetalle()).extracting(MensajeSalida::descripcion).containsExactly("Al menos 1 dígito");
    }

    @Test
    @DisplayName("RULE-032 legacy defect pinned (approved deviation baseline): digits count for nothing in the legacy, so 'Abcdefg!' was valid")
    void rule032_defectoLegado() {
        Caso legado = Golden.casos("db", AREA).stream().filter(c -> "Abcdefg!".equals(c.s("clave")) && c.l("p_IdEmpresa") == 1
                && c.input().getAsJsonObject("empresa1").get("ACC.numLongitud").getAsString().equals("8")).findFirst().orElseThrow();
        assertThat(legado.valor().getAsJsonObject().get("ClaveValida").getAsBoolean()).isTrue();
        assertThat(ValidadorClave.validar("Abcdefg!", p(8, 1, 1, 1, 1), NINGUNA).claveValida()).isFalse();
    }

    // ---------------------------------------------------------------- RULE-031: legacy behavior kept

    @Test
    @DisplayName("RULE-031 legacy behavior pinned: a space matches \\W, so it COUNTS AS SPECIAL and the 'Carácteres válidos' check never fails (BUSINESS_RULES says spaces are rejected: discrepancy)")
    void rule031_espacioCuentaComoEspecial() {
        ResultadoValidacion r = ValidadorClave.validar("Hola 123", p(8, 1, 1, 1, 1), NINGUNA);
        assertThat(r.claveValida()).isTrue();
        assertThat(r.estadoCriterios().get(2)).isEqualTo(new EstadoCriterio("Carácteres válidos", true));
    }

    @Test
    @DisplayName("RULE-031 legacy behavior pinned: non-ASCII letters (Ñ, á, Ⅻ) are \\W specials, not upper/lower; '_' counts for nothing")
    void rule031_unicode() {
        assertThat(ValidadorClave.validar("Ñandú123", p(8, 1, 1, 1, 1), NINGUNA).erroresDetalle()).extracting(MensajeSalida::descripcion)
                .containsExactly("Al menos 1 mayúscula");
        assertThat(ValidadorClave.validar("Aa1_Aa1_", p(8, 1, 1, 1, 1), NINGUNA).erroresDetalle()).extracting(MensajeSalida::descripcion)
                .containsExactly("Al menos 1 carácter especial");
        // an emoji is two UTF-16 units: two specials, length 2
        assertThat(ValidadorClave.validar("Aa😀1234", p(8, 1, 1, 2, 1), NINGUNA).claveValida()).isTrue();
    }

    @Test
    @DisplayName("RULE-031 APPROVED DEVIATION (decision 4; legacy ValidarNuevaClave.java:164 byte loop counter crashed at 128+ chars): long passwords are validated under the same rules; null -> NPE")
    void rule031_largoMaximo() {
        String c127 = "Aa!" + "1".repeat(124);
        assertThat(ValidadorClave.validar(c127, p(8, 1, 1, 1, 1), NINGUNA).claveValida()).isTrue();
        assertThat(ValidadorClave.validar(c127 + "1", p(8, 1, 1, 1, 1), NINGUNA).claveValida()).isTrue();
        // every character is scanned: the capitals and specials after position 127 of a 1250-char password count
        String c1250 = "a".repeat(990) + "1" + "B".repeat(129) + "!".repeat(130);
        ResultadoValidacion largo = ValidadorClave.validar(c1250, p(8, 129, 1, 130, 1), NINGUNA);
        assertThat(largo.claveValida()).isTrue();
        assertThat(ValidadorClave.validar("a".repeat(300), p(8, 1, 1, 1, 1), NINGUNA).erroresDetalle()).extracting(MensajeSalida::descripcion)
                .containsExactly("Al menos 1 mayúscula", "Al menos 1 carácter especial", "Al menos 1 dígito");
        assertThatThrownBy(() -> ValidadorClave.validar(null, p(8, 1, 1, 1, 1), NINGUNA)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("RULE-031: a missing or non-numeric policy value fails with NumberFormatException (Long.parseLong of the raw value)")
    void rule031_configuracionInvalida() {
        assertThatThrownBy(() -> PoliticaClave.desdeConfiguracion(Map.of("ACC.numMayusculas", "1", "ACC.numMinuscula", "1", "ACC.numCaracteres", "1")))
                .isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> PoliticaClave.desdeConfiguracion(Map.of("ACC.numLongitud", "8", "ACC.numMayusculas", " 1", "ACC.numMinuscula", "1", "ACC.numCaracteres", "1")))
                .isInstanceOf(NumberFormatException.class);
    }

    // ---------------------------------------------------------------- RULE-033 / RULE-034

    @Test
    @DisplayName("RULE-033 legacy defect (for an SME, not decided here): company 2 got company 1's policy; the pure policy validates against whatever values it is given")
    void rule033_politicaDeLaEmpresaRecibida() {
        Caso legado = Golden.casos("db", AREA).stream().filter(c -> "Hola123!".equals(c.s("clave")) && c.tiene("p_IdEmpresa") && c.l("p_IdEmpresa") == 2).findFirst().orElseThrow();
        assertThat(legado.valor().getAsJsonObject().get("ClaveValida").getAsBoolean()).as("legacy: company 2 validated with company 1 (8/1/1/1)").isTrue();
        PoliticaClave empresa2 = PoliticaClave.desdeConfiguracion(config(legado.input().get("empresa2")));
        assertThat(ValidadorClave.validar("Hola123!", empresa2, NINGUNA).claveValida()).as("with company 2's own values (12/2/2/2)").isFalse();
    }

    @Test
    @DisplayName("RULE-034: prohibited check is an exact, case-sensitive match of the whole password, evaluated first")
    void rule034_palabraProhibida() {
        Predicate<String> lista = Set.of("Prohibida1!")::contains;
        ResultadoValidacion r = ValidadorClave.validar("Prohibida1!", p(8, 1, 1, 1, 1), lista);
        assertThat(r.erroresDetalle()).extracting(MensajeSalida::descripcion).containsExactly("La clave ingresada no puede contener palabras prohibidas.");
        assertThat(r.estadoCriterios().get(0).esValido()).isFalse();
        assertThat(ValidadorClave.validar("prohibida1!", p(8, 0, 1, 1, 1), lista).estadoCriterios().get(0).esValido()).isTrue();
        assertThat(ValidadorClave.validar("Prohibida1! ", p(8, 1, 1, 1, 1), lista).claveValida()).isTrue();
    }
}
