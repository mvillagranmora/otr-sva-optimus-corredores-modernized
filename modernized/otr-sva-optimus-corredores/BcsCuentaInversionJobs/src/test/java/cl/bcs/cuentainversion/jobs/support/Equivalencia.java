package cl.bcs.cuentainversion.jobs.support;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.SoftAssertions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Compares what the MODERN code produced for a case with the golden recorded from the unmodified legacy. Every facet is a separate soft
 * assertion, so a red test lists everything that differs. A facet the golden does not have must not appear in the observation either.
 * Each comparison also goes to the dump hook ({@link VolcadoEquivalencia}).
 */
public final class Equivalencia {

    private static final List<String> FACETAS = List.of("desenlace", "excepcion", "estados", "transaccion", "salida", "archivos", "directorios",
            "colaboradores", "secuencias");

    private Equivalencia() {
    }

    public static void comparar(Caso caso, JsonObject actual) {
        JsonObject esperado = caso.esperado();
        SoftAssertions s = new SoftAssertions();
        for (String k : FACETAS) {
            comparar(s, caso, k, esperado.get(k), actual.get(k));
        }
        JsonObject et = esperado.getAsJsonObject("tablas");
        JsonObject at = actual.getAsJsonObject("tablas");
        for (String t : et.keySet()) {
            comparar(s, caso, "tabla_" + t, et.get(t), at.get(t));
        }
        s.assertAll();
    }

    private static String rutaBase(String texto) {
        return texto.replaceAll("(?<=\")[^\"]*?cijobs-\\d+", java.util.regex.Matcher.quoteReplacement("${RUTA_BASE}"));
    }

    private static void comparar(SoftAssertions s, Caso caso, String faceta, JsonElement esperado, JsonElement actual) {
        // Step 2 fix: the goldens of IP-nn / RC-nn were recorded with the temp path of the recording machine inside the collaborator trace
        // (p_Ruta); both sides are compared with that base path masked as ${RUTA_BASE} (re-record the goldens to remove the literal path)
        String exp = rutaBase(Canon.texto(esperado));
        String act = rutaBase(Canon.texto(actual));
        VolcadoEquivalencia.escribir(caso.nombre(), faceta, exp, act);
        s.assertThat(act).as(caso.nombre() + ": " + faceta + " (modern) vs legacy golden").isEqualTo(exp);
    }

    /** Counts the executed cases of a test class, so that a green run that executed nothing is reported as not proven. */
    public static final class Contador {
        private final AtomicInteger n = new AtomicInteger();

        public void ejecutado() {
            n.incrementAndGet();
        }

        public int total() {
            return n.get();
        }
    }
}
