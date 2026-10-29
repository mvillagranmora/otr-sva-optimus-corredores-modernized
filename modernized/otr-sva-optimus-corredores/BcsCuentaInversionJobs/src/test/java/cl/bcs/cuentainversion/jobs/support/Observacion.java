package cl.bcs.cuentainversion.jobs.support;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.jobs.runtime.JobResultado;
import cl.bcs.cuentainversion.jobs.runtime.ProcesoPauta;
import cl.bcs.cuentainversion.jobs.runtime.SalidaPauta;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import cl.bcs.cuentainversion.jobs.runtime.JobRunner;
import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;

/**
 * Runs a job of a case through the production {@link JobRunner} (one transaction per run, BcsJob contract) on the case database and
 * observes it in the same shape {@code expected.json} has: desenlace, excepcion {tipo, mensaje}, estados (listener calls),
 * transaccion (commit / rollback order), tablas, archivos, directorios, colaboradores (trace) and secuencias.
 */
public final class Observacion {

    private Observacion() {
    }

    public static JsonObject ejecutar(Caso caso, CuentaInversionJob job, Traza traza) {
        List<JsonObject> estados = Collections.synchronizedList(new ArrayList<>());
        JobRunner runner = new JobRunner(caso.bd().transacciones(), caso.bd().jdbc(), List.of(job), (id, error, mensaje) -> {
            JsonObject o = new JsonObject();
            o.addProperty("error", error);
            o.addProperty("mensaje", Canon.mensaje(mensaje, caso.rutaBase()));
            estados.add(o);
        });
        JobResultado r = runner.ejecutar(job.clase(), caso.idPrograma(), caso.parametros());
        JsonObject act = new JsonObject();
        act.addProperty("desenlace", r.desenlace().name());
        if (r.error() != null) {
            JsonObject ex = new JsonObject();
            ex.addProperty("tipo", r.error().getClass().getSimpleName());
            ex.addProperty("mensaje", Canon.mensajeDe(r.error(), caso.rutaBase()));
            act.add("excepcion", ex);
        }
        JsonArray es = new JsonArray();
        for (JsonObject o : estados) {
            if (r.error() != null && o.get("error").getAsBoolean()) {
                // the status message of a NullPointerException is its helpful text (it names variables of the code that threw it): <npe> on both sides
                // Step 2 fix: a scripted NPE message ("nulo") stays as recorded; only the helpful text ("Cannot invoke ...") is masked
                o.addProperty("mensaje", Canon.mensaje(r.error().getMessage(), caso.rutaBase()));
            }
            es.add(o);
        }
        act.add("estados", es);
        JsonArray tx = new JsonArray();
        caso.bd().eventos().forEach(tx::add);
        act.add("transaccion", tx);
        return completar(caso, act, traza);
    }

    /**
     * Runs a PAUTA step (legacy BcsProcesoPauta): it is not a BcsJob, so there is no run contract (no commit, no status); the caller owns the
     * transaction. The step runs inside a transaction that is ALWAYS rolled back here (nobody commits it), through a manager that does not
     * record events: the golden has the facets {@code salida} or {@code excepcion}, {@code colaboradores}, {@code tablas} (nothing committed).
     */
    public static JsonObject ejecutarPauta(Caso caso, ProcesoPauta pauta, Traza traza) {
        DataSourceTransactionManager tm = new DataSourceTransactionManager(caso.bd().dataSource());
        TransactionStatus tx = tm.getTransaction(new DefaultTransactionDefinition());
        JsonObject act = new JsonObject();
        try {
            SalidaPauta s = pauta.transaction(caso.parametrosPauta());
            JsonObject so = new JsonObject();
            so.addProperty("P_OUT_MENSAJE", s.P_OUT_MENSAJE);
            so.addProperty("P_OUT_ERRORESCONTADOR", s.P_OUT_ERRORESCONTADOR);
            so.add("P_OUT_MENSAJES", new com.google.gson.Gson().toJsonTree(s.P_OUT_MENSAJES));
            act.add("salida", so);
        } catch (Throwable t) {
            JsonObject ex = new JsonObject();
            ex.addProperty("tipo", t.getClass().getSimpleName());
            ex.addProperty("mensaje", Canon.mensajeDe(t, caso.rutaBase()));
            act.add("excepcion", ex);
        } finally {
            tm.rollback(tx);
        }
        return completar(caso, act, traza);
    }

    /** The facets shared by every kind of run: tables, files, collaborator trace, sequences. */
    public static JsonObject completar(Caso caso, JsonObject act, Traza traza) {
        JsonObject tablas = new JsonObject();
        if (caso.caso().has("tablas")) {
            for (JsonElement el : caso.caso().getAsJsonArray("tablas")) {
                JsonObject t = el.getAsJsonObject();
                try {
                    tablas.add(t.get("nombre").getAsString(), Dump.tabla(caso.bd().setup(), t));
                } catch (SQLException e) {
                    throw new AssertionError("cannot dump " + t, e);
                }
            }
        }
        act.add("tablas", tablas);
        JsonObject f = Archivos.capturar(caso.rutaBase());
        act.add("archivos", f.get("archivos"));
        act.add("directorios", f.get("directorios"));
        act.add("colaboradores", traza.llamadas());
        act.add("secuencias", traza.secuencias());
        return act;
    }
}
