package cl.bcs.cuentainversion.port.out;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.utils.Utils;

/**
 * Generic collaborator seam of the transactional services (CrearCargoAbono, CertificarCargoAbono, AnularCargoAbono,
 * ConsultarSaldoCuentaInversion, ...): the legacy called other JavaCall services in process
 * ({@code Servicio.logica(...)}); the modern services call this port with the same JSON contract (the JSON form of the legacy
 * {@code Entrada_}/{@code Salida_} POJOs, field names and semantics unchanged). The recorded legacy call log
 * ({@code llamadasColaboradores} in each golden) is what an implementation must reproduce.
 *
 * <p>Injected as the last constructor argument of the services that need it, so tests can fake it.
 */
public interface Colaboradores {

    /**
     * Invokes the collaborator service.
     *
     * @param servicio fully qualified legacy class name of the collaborator (e.g. {@code cl.bcs.facturacion.CrearMovimientoComprobante})
     * @param entrada  JSON of its input POJO
     * @return JSON of its output POJO
     */
    JsonObject invocar(String servicio, JsonObject entrada);

    /**
     * Typed form of {@link #invocar(String, JsonObject)}: {@code entrada} is serialised with the platform Gson settings (nulls
     * kept, dates as {@code yyyy-MM-dd HH:mm:ss.SSS}) and the answer is decoded into {@code salida}. The contract classes live in
     * {@code cl.bcs.cuentainversion.domain.colaborador}.
     */
    default <S> S invocar(String servicio, Object entrada, Class<S> salida) {
        Gson gson = Utils.getGsonBuilder();
        JsonObject respuesta = invocar(servicio, gson.toJsonTree(entrada).getAsJsonObject());
        return gson.fromJson(respuesta, salida);
    }

    /**
     * Collaborators whose legacy entry point is not JSON (a scalar parameter or a list result) exchange the value as
     * {@code {"valor": <scalar or array>}}.
     */
    static JsonObject valor(Object v) {
        JsonObject o = new JsonObject();
        o.add("valor", Utils.getGsonBuilder().toJsonTree(v));
        return o;
    }
}
