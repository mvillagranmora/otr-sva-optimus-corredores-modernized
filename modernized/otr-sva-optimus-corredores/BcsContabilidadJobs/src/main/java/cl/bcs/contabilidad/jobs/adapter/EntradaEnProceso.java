package cl.bcs.contabilidad.jobs.adapter;

import com.google.gson.JsonObject;

/**
 * In-process entry point of the library that OWNS a collaborator (step 5, JOBS_PATTERN.md H-1): a legacy JavaCall class name plus its legacy
 * JSON go in, the legacy JSON comes out, with no HTTP and inside the transaction of the caller. Today one owner: Tesoreria
 * ({@code cl.bcs.tesoreria.javacall.JavaCallDispatcher}). The jobs application hands it to {@link DespachadorEnProceso}, so no library depends
 * on another one (no Maven or compile-time cycle).
 */
public interface EntradaEnProceso {

    /** Name of the owner library, for messages and tests. */
    String propietario();

    /** {@code true} when this owner serves the legacy fully qualified class (for example {@code cl.bcs.tesoreria.liq.ConsultarClearingFinanciero}). */
    boolean maneja(String claseLegacy);

    /**
     * Runs the collaborator. A runtime exception of the service propagates unchanged (so the job rolls back); a checked one is rethrown as it is.
     */
    JsonObject despachar(String claseLegacy, JsonObject entrada) throws Exception;

    /** The dispatch half of an entry point. */
    @FunctionalInterface
    interface Despacho {
        JsonObject despachar(String claseLegacy, JsonObject entrada) throws Exception;
    }

    /** Builds an entry point from its three parts. */
    static EntradaEnProceso de(String propietario, java.util.function.Predicate<String> maneja, Despacho despacho) {
        return new EntradaEnProceso() {
            @Override
            public String propietario() {
                return propietario;
            }

            @Override
            public boolean maneja(String claseLegacy) {
                return maneja.test(claseLegacy);
            }

            @Override
            public JsonObject despachar(String claseLegacy, JsonObject entrada) throws Exception {
                return despacho.despachar(claseLegacy, entrada);
            }
        };
    }
}
