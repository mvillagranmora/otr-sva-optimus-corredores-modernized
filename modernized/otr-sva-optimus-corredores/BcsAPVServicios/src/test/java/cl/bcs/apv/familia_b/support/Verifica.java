package cl.bcs.apv.familia_b.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.SoftAssertions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import cl.bcs.apv.support.VolcadoEquivalencia;

/**
 * Compares what the MODERN code produced for a case with the golden recorded from the legacy ({@code expected.json}): exception type, output JSON,
 * table dumps, the collaborator trace (what the legacy handed to each collaborator outside APV, in order) and the sequences requested.
 * Every facet is a separate soft assertion so a red test lists everything that differs, not just the first difference.
 */
public final class Verifica {

    public static final String FAMILIA = "familia_b";

    private Verifica() {
    }

    /** Result of running the modern code: either an output (possibly null) or the exception it threw. */
    public record Ejecucion(JsonElement salida, Throwable excepcion) {
    }

    /** Compares the modern run with the golden of {@code caso}. */
    public static void comparar(Caso caso, Ejecucion r, List<JsonObject> trazaColaboradores, List<String> secuenciasModernas) {
        SoftAssertions s = new SoftAssertions();
        Set<String> mascara = Dump.claves(caso.caso(), "enmascarar");
        Set<String> sinOrden = Dump.claves(caso.caso(), "sinOrden");
        String[] so = sinOrden.toArray(new String[0]);
        JsonObject esperado = caso.esperado();
        if (esperado.has("excepcion")) {
            String tipo = esperado.get("excepcion").getAsString().split(":")[0];
            s.assertThat(r.excepcion()).as(caso.nombre() + ": the legacy threw " + esperado.get("excepcion").getAsString()).isNotNull();
            if (r.excepcion() != null) {
                s.assertThat(r.excepcion().getClass().getSimpleName()).as(caso.nombre() + ": exception type").isEqualTo(tipo);
            }
        } else {
            s.assertThat(r.excepcion()).as(caso.nombre() + ": the modern code must not throw (legacy did not): "
                    + (r.excepcion() == null ? "" : r.excepcion())).isNull();
            JsonElement exp = esperado.get("salida");
            JsonElement act = r.salida() == null ? JsonNull.INSTANCE : Dump.enmascarar(r.salida(), mascara);
            VolcadoEquivalencia.escribir(FAMILIA, caso.nombre(), "salida", Canon.texto(exp, so), Canon.texto(act, so));
            s.assertThat(Canon.texto(act, so)).as(caso.nombre() + ": salida (modern) vs legacy golden").isEqualTo(Canon.texto(exp, so));
        }
        tablas(s, caso);
        colaboradores(s, caso, trazaColaboradores, mascara);
        secuencias(s, caso, secuenciasModernas);
        s.assertAll();
    }

    private static void tablas(SoftAssertions s, Caso caso) {
        JsonObject esperadas = caso.esperado().getAsJsonObject("tablas");
        JsonObject reales = caso.tablas();
        for (String t : esperadas.keySet()) {
            VolcadoEquivalencia.escribir(FAMILIA, caso.nombre(), "tabla_" + t, Canon.texto(esperadas.get(t)), Canon.texto(reales.get(t)));
            s.assertThat(Canon.texto(reales.get(t))).as(caso.nombre() + ": rows of " + t + " (modern) vs legacy golden")
                    .isEqualTo(Canon.texto(esperadas.get(t)));
        }
    }

    /** Collaborator trace: the modern code must hand each collaborator the same input, in the same order, as the legacy did. */
    private static void colaboradores(SoftAssertions s, Caso caso, List<JsonObject> trazaModerna, Set<String> mascara) {
        JsonArray esperada = caso.esperado().getAsJsonArray("colaboradores");
        JsonArray real = new JsonArray();
        trazaModerna.forEach(o -> real.add(Dump.enmascarar(o, mascara)));
        VolcadoEquivalencia.escribir(FAMILIA, caso.nombre(), "colaboradores", Canon.texto(esperada), Canon.texto(real));
        s.assertThat(Canon.texto(real)).as(caso.nombre() + ": collaborator calls (modern) vs legacy golden").isEqualTo(Canon.texto(esperada));
    }

    /**
     * Sequences requested. The legacy asks {@code Utils.getSecuencia(con, name, n)} (golden "name#n"); the modern port gives ONE value per call:
     * a golden "name#n" is expanded to n occurrences of name before comparing.
     */
    private static void secuencias(SoftAssertions s, Caso caso, List<String> modernas) {
        List<String> esperadas = new ArrayList<>();
        for (JsonElement e : caso.esperado().getAsJsonArray("secuenciasSolicitadas")) {
            String v = e.getAsString();
            int i = v.lastIndexOf('#');
            int n = Integer.parseInt(v.substring(i + 1));
            for (int k = 0; k < n; k++) {
                esperadas.add(v.substring(0, i));
            }
        }
        VolcadoEquivalencia.escribir(FAMILIA, caso.nombre(), "secuencias", String.join("\n", esperadas), String.join("\n", modernas));
        s.assertThat(modernas).as(caso.nombre() + ": sequences requested (modern) vs legacy golden").isEqualTo(esperadas);
    }
}
