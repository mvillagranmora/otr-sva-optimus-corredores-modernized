package cl.bcs.cuentainversion.jobs.adapter;

import cl.bcs.cuentainversion.port.out.Colaboradores;
import cl.bcs.cuentainversion.service.JavaCallDispatcher;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * The {@link Colaboradores} port of the job module: the same routing as {@code ColaboradoresJavaCall} of bcs-cuenta-inversion-servicios,
 * with bounded waits. A class that the Servicios library migrated runs in process through {@link JavaCallDispatcher}; any other class (the
 * platform ones: {@code InscribirProcesoBatch}, {@code ConsultarEmpresa}, {@code CalcularParidad}, ...) goes to the platform JavaCall router
 * ({@code POST {services.javacall.url}/internal/java-call}, body {@code {"class", "params"}}, header {@code X-Internal-Token}).
 * No credentials in the code. This bean replaces the library adapter, which is not scanned.
 */
@Component
public class ColaboradoresJobs implements Colaboradores {

    static final String CABECERA = "X-Internal-Token";

    private final JavaCallDispatcher local;
    private final RestClient router;
    private final String tokenInterno;

    @Autowired
    public ColaboradoresJobs(JavaCallDispatcher local, @Value("${services.javacall.url:}") String urlRouter,
                             @Value("${services.javacall.connect-timeout:PT2S}") Duration tiempoConexion,
                             @Value("${services.javacall.read-timeout:PT60S}") Duration tiempoLectura,
                             @Value("${services.internal-token:}") String tokenInterno) {
        this(local, urlRouter.isBlank() ? null : cliente(urlRouter, tiempoConexion, tiempoLectura), tokenInterno);
    }

    /** For tests: the router client is given ({@code null}: no router configured, a platform call fails when it is made). */
    ColaboradoresJobs(JavaCallDispatcher local, RestClient router, String tokenInterno) {
        this.local = local;
        this.router = router;
        this.tokenInterno = tokenInterno == null ? "" : tokenInterno;
    }

    private static RestClient cliente(String url, Duration tiempoConexion, Duration tiempoLectura) {
        // bounded waits: a hung collaborator must not hold a thread inside an open database transaction
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(tiempoConexion).build());
        fabrica.setReadTimeout(tiempoLectura);
        return RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
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
        if (router == null) {
            throw new IllegalStateException("services.javacall.url (JAVACALL_URL) is not configured: cannot call " + servicio);
        }
        JsonObject peticion = new JsonObject();
        peticion.addProperty("class", servicio);
        peticion.add("params", entrada);
        RestClient.RequestBodySpec solicitud = router.post().uri("/internal/java-call").contentType(MediaType.APPLICATION_JSON);
        if (!tokenInterno.isBlank()) {
            solicitud = solicitud.header(CABECERA, tokenInterno);
        }
        String respuesta = solicitud.body(peticion.toString()).retrieve().body(String.class);
        JsonElement salida = respuesta == null ? null : JsonParser.parseString(respuesta);
        return salida == null || salida.isJsonNull() ? null : salida.getAsJsonObject();
    }
}
