package cl.bcs.fondomutuo.port.out.http;

import com.google.gson.JsonObject;

/**
 * Port H (read, by HTTP through the platform router): legacy {@code cl.bcs.plataforma.mer.CalcularParidad}. Input and output are the legacy JSON
 * contract of the class (same field names as its Entrada/Salida), exactly what the oracle's registered double records. Several calls per request.
 * (Not to be confused with the old stub {@code cl.bcs.fondomutuo.port.out.ParidadPort}, which is out of the Spring context.)
 */
public interface ParidadPort {

    JsonObject calcular(JsonObject entrada);
}
