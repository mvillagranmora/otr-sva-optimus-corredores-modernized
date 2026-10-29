package cl.bcs.contabilidad.domain.model.sap;

import com.google.gson.JsonObject;

/**
 * HTTP status and body of a Service Layer call (legacy jobs/Respuesta.java). {@link #toString()} is the legacy
 * {@code Utils.printJson} text {@code {"Codigo":n,"Mensaje":"..."}}, which legacy embeds in partner error messages.
 */
public record SapRespuesta(int Codigo, String Mensaje) {

    @Override
    public String toString() {
        JsonObject o = new JsonObject();
        o.addProperty("Codigo", Codigo);
        o.addProperty("Mensaje", Mensaje);
        return o.toString();
    }
}
