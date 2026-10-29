package cl.bcs.apv.jobs.support;

import java.util.List;

import org.assertj.core.api.SoftAssertions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.apv.jobs.support.EjecucionJob.Corrida;

/**
 * Compares what the MODERN code did for a case with the golden recorded from the legacy ({@code expected.json}), one soft assertion per comparison point
 * so a red test lists everything that differs: {@code resultado} (outcome, commit/rollback events, status calls, exception class), {@code tablas},
 * {@code colaboradores} (calls and payloads, in order) and {@code archivos} (job files, bytes). With {@code -Dequivalence.out=<dir>} every point also
 * writes its canonical text, for the dual-execution comparison (VolcadoEquivalencia).
 */
public final class VerificaJob {

    public static final String FAMILIA = "apv-jobs";

    private VerificaJob() {
    }

    private static final JsonObject TABLAS_ENMASCARADAS = new JsonObject();

    static {
        TABLAS_ENMASCARADAS.addProperty("enmascarado", "job ended with a java.lang.Error: tables not comparable (see VerificaJob.comparar)");
    }

    /** True when the run ended with a Throwable that is not an Exception (a java.lang.Error). */
    private static boolean terminaConError(JsonObject resultado) {
        return resultado.has("esException") && !resultado.get("esException").getAsBoolean();
    }

    public static void comparar(CasoJob caso, Corrida r) {
        SoftAssertions s = new SoftAssertions();
        JsonObject exp = caso.esperado();
        punto(s, caso, "resultado", exp.get("resultado"), r.resultado());
        if (terminaConError(r.resultado())) {
            // OBF-006 decision (user, 2026-10-21): the `tablas` point is MASKED, on both sides, when the job ends with a java.lang.Error. The legacy
            // BcsJob.run only closes the connection for an Error (no rollback) and the oracle dumped the tables on that still open connection, so the
            // golden shows the uncommitted rows; JobRunner rolls back on every Throwable. In production the end state is the same (HANA rolls back an
            // open transaction when the connection is closed); only the in-process dump differs. The commit/rollback events are masked likewise.
            punto(s, caso, "tablas", TABLAS_ENMASCARADAS, TABLAS_ENMASCARADAS);
        } else {
            punto(s, caso, "tablas", exp.get("tablas"), r.tablas());
        }
        JsonArray traza = new JsonArray();
        r.colaboradores().forEach(traza::add);
        punto(s, caso, "colaboradores", exp.get("colaboradores"), traza);
        punto(s, caso, "archivos", exp.get("archivos"), r.archivos());
        if (exp.has("marcaDeTiempoEsLaHoraDeLaCorrida")) {
            // the legacy stamped the file with the wall clock of the run; the new code must stamp it with the injected clock
            s.assertThat(r.marcaDeTiempoDelReloj()).as(caso.nombre() + ": the file-name stamp is the clock of the run").isTrue();
        }
        s.assertAll();
    }

    private static void punto(SoftAssertions s, CasoJob caso, String parte, JsonElement esperado, JsonElement real) {
        String e = Canon.texto(esperado);
        String a = Canon.texto(real);
        VolcadoEquivalencia.escribir(FAMILIA, caso.nombre(), parte, e, a);
        s.assertThat(a).as(caso.nombre() + ": " + parte + " (modern) vs legacy golden").isEqualTo(e);
    }

    /** The cases of a job, from the case directories (by the job name in case.json), for the inventory tests. */
    public static List<String> casosDe(String job) {
        return CasoJob.todos().stream().filter(n -> n.startsWith(prefijo(job))).toList();
    }

    public static String prefijo(String job) {
        return switch (job) {
            case "BcsJobCargaMasivaPrevired" -> "APVJ-CMP-";
            case "IngresarOrdenBonificacionFiscal" -> "APVJ-OBF-";
            case "IngresarOrdenFMBonificacionFiscal" -> "APVJ-FMB-";
            case "IngresarOrdenRVBonificacionFiscal" -> "APVJ-RVB-";
            default -> throw new IllegalArgumentException(job);
        };
    }
}
