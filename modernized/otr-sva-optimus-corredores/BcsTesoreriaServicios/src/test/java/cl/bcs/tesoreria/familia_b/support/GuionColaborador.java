package cl.bcs.tesoreria.familia_b.support;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Scripted double of ONE legacy collaborator: the Java port of the oracle fake of the same name (legacy-oracle/shims/**). The class is looked up by
 * name ({@code support.colaboradores.<Servicio>Guion}) by {@link ColaboradoresDeCaso}.
 */
public interface GuionColaborador {

    /** Entry of the case.json {@code "colaboradores"} object this double reads (e.g. {@code requisitosBasicos}). */
    String clave();

    /** What the oracle fake wrote in the golden trace under "entrada" (default: the input as received). */
    default JsonElement paraTraza(JsonObject entrada, JsonObject guiones) {
        return entrada;
    }

    /** The legacy output object as JSON; may throw what the scripted {@code "lanza"} says (a plain {@code Exception}, like the fake). */
    JsonObject responder(JsonObject entrada, JsonObject guiones) throws Exception;
}
