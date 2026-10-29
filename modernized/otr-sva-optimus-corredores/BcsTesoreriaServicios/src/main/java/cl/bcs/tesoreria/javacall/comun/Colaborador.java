package cl.bcs.tesoreria.javacall.comun;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * Typed call of a collaborator service through the {@link Colaboradores} port: the legacy Entrada object is encoded with the platform Gson, the
 * answer is decoded into the legacy Salida class. An empty answer (a service that returns nothing, or a double that is not scripted) decodes into
 * a fresh Salida, i.e. with the defaults its no-argument constructor sets, exactly as the legacy empty Salida.
 */
public final class Colaborador {

    private Colaborador() {
    }

    public static <T> T invocar(Colaboradores colaboradores, String servicio, Object entrada, Class<T> salida) {
        JsonObject respuesta = colaboradores.invocar(servicio, JsonLegacy.PLATAFORMA.toJsonTree(entrada).getAsJsonObject());
        return decodificar(respuesta, salida);
    }

    public static <T> T decodificar(JsonElement respuesta, Class<T> salida) {
        return JsonLegacy.PLATAFORMA.fromJson(respuesta == null || respuesta.isJsonNull() ? new JsonObject() : respuesta, salida);
    }
}
