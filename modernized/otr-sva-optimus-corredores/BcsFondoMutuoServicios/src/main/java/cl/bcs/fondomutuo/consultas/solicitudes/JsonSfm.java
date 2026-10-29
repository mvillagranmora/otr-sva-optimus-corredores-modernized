package cl.bcs.fondomutuo.consultas.solicitudes;

import com.google.gson.Gson;

/**
 * The Gson of legacy {@code ConsultarSolicitudesFFMM.call}: the plain {@code new Gson()} it uses to read its input AND to write its output (null keys omitted, {@code BigDecimal} with its
 * {@code toString}, default date format in the zone of the JVM at the moment the Gson is CREATED). A NEW Gson PER CALL: a cached instance would freeze the zone. No request state.
 */
final class JsonSfm {

    private JsonSfm() {
    }

    static Gson gson() {
        return new Gson();
    }
}
