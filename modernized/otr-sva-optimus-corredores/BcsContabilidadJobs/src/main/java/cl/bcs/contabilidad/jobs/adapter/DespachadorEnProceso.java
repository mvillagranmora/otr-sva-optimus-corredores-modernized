package cl.bcs.contabilidad.jobs.adapter;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Optional;

/**
 * The local dispatcher of the jobs application (step 5): finds, among the in-process entry points of the owner libraries, the one that serves a
 * legacy class. It is where the Contabilidad -&gt; Tesoreria relationship is resolved: the Contabilidad library keeps its own
 * {@code PlataformaPort}, and the jobs application gives it this dispatcher, so the call is one between two entry points inside one JVM and one
 * transaction, with no library depending on the other.
 */
public final class DespachadorEnProceso {

    private final List<EntradaEnProceso> propietarios;

    public DespachadorEnProceso(List<? extends EntradaEnProceso> propietarios) {
        this.propietarios = List.copyOf(propietarios);
    }

    public Optional<EntradaEnProceso> propietarioDe(String claseLegacy) {
        return propietarios.stream().filter(p -> p.maneja(claseLegacy)).findFirst();
    }

    public boolean maneja(String claseLegacy) {
        return propietarioDe(claseLegacy).isPresent();
    }

    /** Runs the collaborator in process; the caller already checked {@link #maneja}. */
    public JsonObject despachar(String claseLegacy, JsonObject entrada) {
        EntradaEnProceso propietario = propietarioDe(claseLegacy)
                .orElseThrow(() -> new IllegalArgumentException("no in-process owner for " + claseLegacy));
        try {
            return propietario.despachar(claseLegacy, entrada);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("collaborator " + claseLegacy + " (" + propietario.propietario() + ") failed", e);
        }
    }
}
