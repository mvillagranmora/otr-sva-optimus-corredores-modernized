package cl.bcs.utils.clave;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * RULE-031..034: the new-password check of {@code ValidarNuevaClave.logica} (legacy
 * {@code ValidarNuevaClave.java:103-228}) as a pure function; the policy values and the prohibited-word lookup are
 * passed in instead of read from the database.
 *
 * <p>Criteria, in the legacy order and with the legacy texts: prohibited word (RULE-034: exact, case-sensitive match
 * of the whole password), minimum length, "valid characters", upper case {@code [A-Z]}, lower case {@code [a-z]},
 * "special" characters, then the RULE-032 digit criterion (approved deviation: ASCII {@code 0-9}, appended last).
 *
 * <p>Legacy defects kept as the tests pin them: "special" is the regex {@code \W}, i.e. anything that is not an ASCII
 * letter, ASCII digit or {@code _}. So spaces and non-ASCII letters count as specials, each UTF-16 unit counts once,
 * and {@code _} counts for nothing. The "valid characters" criterion can never fail, because the legacy space branch
 * came after {@code \W} and was unreachable (ValidarNuevaClave.java:172-177).
 *
 * <p>Approved deviation (2026-10-16): the legacy scan loop counter was a {@code byte}
 * ({@code ValidarNuevaClave.java:164}), so a password of 128 or more characters threw
 * {@link StringIndexOutOfBoundsException}; here every character is scanned, with no length limit.
 */
public final class ValidadorClave {

    static final String CRITERIO_PROHIBIDA = "La clave ingresada no puede contener palabras prohibidas.";
    static final String CRITERIO_CARACTERES_VALIDOS = "Carácteres válidos";

    private ValidadorClave() {
    }

    /**
     * @param clave       the new password; {@code null} throws {@link NullPointerException}
     * @param p           the policy values
     * @param esProhibida whether the whole password is a prohibited word
     */
    public static ResultadoValidacion validar(String clave, PoliticaClave p, Predicate<String> esProhibida) {
        Objects.requireNonNull(clave);
        Conteo c = Conteo.de(clave);
        Acumulador a = new Acumulador();
        a.criterio(CRITERIO_PROHIBIDA, !esProhibida.test(clave));
        a.criterio(String.format("Mínimo %d carácteres", p.minLongitud()), clave.length() >= p.minLongitud());
        a.criterio(CRITERIO_CARACTERES_VALIDOS, true);
        a.criterio(String.format("Al menos %d mayúscula", p.minMayusculas()), c.mayusculas() >= p.minMayusculas());
        a.criterio(String.format("Al menos %d minúscula", p.minMinusculas()), c.minusculas() >= p.minMinusculas());
        a.criterio(String.format("Al menos %d carácter especial", p.minCaracteres()), c.especiales() >= p.minCaracteres());
        a.criterio(String.format("Al menos %d dígito", p.minDigitos()), c.digitos() >= p.minDigitos());
        return a.resultado();
    }

    /** Character counts of a password, per UTF-16 unit. */
    private record Conteo(long mayusculas, long minusculas, long especiales, long digitos) {

        static Conteo de(String clave) {
            long may = 0;
            long min = 0;
            long esp = 0;
            long dig = 0;
            for (int i = 0; i < clave.length(); i++) {
                char ch = clave.charAt(i);
                if (ch >= 'A' && ch <= 'Z') {
                    may++;
                } else if (ch >= 'a' && ch <= 'z') {
                    min++;
                } else if (ch >= '0' && ch <= '9') {
                    dig++;
                } else if (ch != '_') {
                    esp++; // regex \W without UNICODE_CHARACTER_CLASS
                }
            }
            return new Conteo(may, min, esp, dig);
        }
    }

    private static final class Acumulador {
        private final List<MensajeSalida> errores = new ArrayList<>();
        private final List<EstadoCriterio> criterios = new ArrayList<>();

        void criterio(String descripcion, boolean cumple) {
            criterios.add(new EstadoCriterio(descripcion, cumple));
            if (!cumple) {
                errores.add(MensajeSalida.error(descripcion));
            }
        }

        ResultadoValidacion resultado() {
            return new ResultadoValidacion(errores.isEmpty(), errores.size(), errores, criterios);
        }
    }
}
