package cl.bcs.plataforma.login.dialecto;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Finds the key of a JSON object ignoring case and underscores, as the HANA procedures were fed ({@code p_IdEmpresa}, {@code P_IDEMPRESA} and {@code p_id_empresa} are one key). */
final class ClavesJson {

    private ClavesJson() {
    }

    /** The value under the key, or null when the key is absent. A JSON null is returned as such. */
    static JsonElement buscar(JsonObject objeto, String nombre) {
        String buscado = normalizar(nombre);
        for (Map.Entry<String, JsonElement> e : objeto.entrySet()) {
            if (normalizar(e.getKey()).equals(buscado)) {
                return e.getValue();
            }
        }
        return null;
    }

    static boolean esNulo(JsonElement elemento) {
        return elemento == null || elemento.isJsonNull();
    }

    private static String normalizar(String nombre) {
        return nombre.replace("_", "").toLowerCase();
    }
}
