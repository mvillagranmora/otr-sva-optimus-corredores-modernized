package cl.bcs.apv.familia_a.support;

import java.util.List;
import java.util.function.Supplier;

import org.assertj.core.api.SoftAssertions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import cl.bcs.apv.support.VolcadoEquivalencia;
import com.google.gson.JsonObject;

/**
 * Compares what the MODERN code produced for a case with the golden recorded from the legacy ({@code expected.json}):
 * exception type, output JSON, table dumps and (when the modern API allows injecting collaborators) the collaborator trace.
 * Every facet is a separate soft assertion so a red test lists everything that differs, not just the first difference.
 */
public final class Verifica {

    private Verifica() {
    }

    /** Result of running the modern code: either an output (possibly null) or the exception it threw. */
    public record Ejecucion(JsonElement salida, Throwable excepcion) {
        /** Runs the code under test inside the platform transaction model of the case (commit on return, rollback on throw). */
        public static Ejecucion de(Caso caso, java.util.concurrent.Callable<JsonElement> ejecucion) {
            try {
                return new Ejecucion(caso.transaccion(ejecucion), null);
            } catch (Throwable t) {
                return new Ejecucion(null, t);
            }
        }

        public static Ejecucion de(Supplier<JsonElement> ejecucion) {
            try {
                return new Ejecucion(ejecucion.get(), null);
            } catch (Throwable t) {
                return new Ejecucion(null, t);
            }
        }
    }

    /** As {@link #comparar(Caso, Ejecucion, String...)}, plus the collaborator trace (what the legacy handed to each collaborator). */
    public static void comparar(Caso caso, Ejecucion r, List<JsonObject> trazaColaboradores, String... sinOrden) {
        SoftAssertions s = new SoftAssertions();
        comparar(s, caso, r, sinOrden);
        colaboradores(s, caso, trazaColaboradores);
        s.assertAll();
    }

    public static void comparar(Caso caso, Ejecucion r, String... sinOrden) {
        SoftAssertions s = new SoftAssertions();
        comparar(s, caso, r, sinOrden);
        s.assertAll();
    }

    private static void comparar(SoftAssertions s, Caso caso, Ejecucion r, String... sinOrden) {
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
            JsonElement act = r.salida() == null ? JsonNull.INSTANCE : r.salida();
            VolcadoEquivalencia.escribir("apv-a", caso.nombre(), "salida", Canon.texto(exp, sinOrden), Canon.texto(act, sinOrden));
            s.assertThat(Canon.texto(act, sinOrden)).as(caso.nombre() + ": salida (modern) vs legacy golden").isEqualTo(Canon.texto(exp, sinOrden));
        }
        tablas(s, caso);
    }

    /** Table dumps only (for tests whose modern entry point returns nothing comparable). */
    public static void soloTablas(Caso caso) {
        SoftAssertions s = new SoftAssertions();
        tablas(s, caso);
        s.assertAll();
    }

    private static void tablas(SoftAssertions s, Caso caso) {
        JsonObject esperadas = caso.esperado().getAsJsonObject("tablas");
        JsonObject reales = caso.tablas();
        for (String t : esperadas.keySet()) {
            VolcadoEquivalencia.escribir("apv-a", caso.nombre(), "tabla_" + t, Canon.texto(esperadas.get(t)), Canon.texto(reales.get(t)));
            s.assertThat(Canon.texto(reales.get(t))).as(caso.nombre() + ": rows of " + t + " (modern) vs legacy golden")
                    .isEqualTo(Canon.texto(esperadas.get(t)));
        }
    }

    /** Collaborator trace: the modern code must hand each collaborator the same input, in the same order, as the legacy did. */
    public static void colaboradores(Caso caso, List<JsonObject> trazaModerna) {
        SoftAssertions s = new SoftAssertions();
        colaboradores(s, caso, trazaModerna);
        s.assertAll();
    }

    private static void colaboradores(SoftAssertions s, Caso caso, List<JsonObject> trazaModerna) {
        JsonArray esperada = caso.esperado().getAsJsonArray("colaboradores");
        JsonArray real = new JsonArray();
        java.util.Set<String> claves = caso.enmascararColaboradores();
        trazaModerna.forEach(o -> real.add(Dump.enmascararClaves(o, claves)));
        VolcadoEquivalencia.escribir("apv-a", caso.nombre(), "colaboradores", Canon.texto(esperada), Canon.texto(real));
        s.assertThat(Canon.texto(real)).as(caso.nombre() + ": collaborator calls (modern) vs legacy golden").isEqualTo(Canon.texto(esperada));
    }
}
