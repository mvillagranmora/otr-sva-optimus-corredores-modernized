package cl.bcs.cuentainversion.infrastructure.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentainversion.port.out.Colaboradores;
import cl.bcs.cuentainversion.service.JavaCallDispatcher;

/**
 * Production {@link Colaboradores}: invokes a legacy JavaCall class by name with the JSON contract of its Entrada/Salida.
 * <ul>
 *   <li>a class that this module migrated runs in process through {@link JavaCallDispatcher} (no network hop);</li>
 *   <li>any other class goes to the platform JavaCall router ({@code POST {services.javacall.url}/internal/java-call},
 *       body {@code {"class": ..., "params": ...}}), which routes it to the service that owns it or falls back to TomEE.</li>
 * </ul>
 * The router URL comes from the environment ({@code JAVACALL_URL}); there are no credentials in the code.
 *
 * <p>Collaborators whose legacy entry point is not JSON (a scalar parameter or a list result) exchange their value as
 * {@code {"valor": <scalar or array>}}.
 */
@Component
public class ColaboradoresJavaCall implements Colaboradores {

    private final JavaCallDispatcher local;
    private final RestClient router;
    private final String tokenInterno;

    public ColaboradoresJavaCall(JavaCallDispatcher local, @Value("${services.javacall.url}") String urlRouter,
            @Value("${services.internal-token:}") String tokenInterno) {
        this.local = local;
        this.router = RestClient.builder().baseUrl(urlRouter).build();
        this.tokenInterno = tokenInterno == null ? "" : tokenInterno;
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        if (local.maneja(servicio)) {
            try {
                return local.despachar(servicio, entrada);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException("collaborator " + servicio + " failed", e);
            }
        }
        JsonObject peticion = new JsonObject();
        peticion.addProperty("class", servicio);
        peticion.add("params", entrada);
        RestClient.RequestBodySpec solicitud = router.post()
                .uri("/internal/java-call")
                .contentType(MediaType.APPLICATION_JSON);
        if (!tokenInterno.isBlank()) {
            solicitud = solicitud.header("X-Internal-Token", tokenInterno);
        }
        String respuesta = solicitud
                .body(peticion.toString())
                .retrieve()
                .body(String.class);
        JsonElement salida = respuesta == null ? null : JsonParser.parseString(respuesta);
        return salida == null || salida.isJsonNull() ? null : salida.getAsJsonObject();
    }
}
