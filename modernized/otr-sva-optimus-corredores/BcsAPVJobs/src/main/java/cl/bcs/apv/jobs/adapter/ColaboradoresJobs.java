package cl.bcs.apv.jobs.adapter;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.infrastructure.adapter.ColaboradoresJavaCall;

/**
 * The {@link Colaboradores} port of the job module: the production {@link ColaboradoresJavaCall} of apv-service plus the two collaborators that only the
 * jobs call and that it does not route yet (the apv-service sources are not edited): {@code ConsultarFlujoCaja} (Plataforma) and
 * {@code ConsultarFlujoCustodiaFondoMutuo} (FondoMutuo). They live outside APV, so they go to the platform JavaCall router
 * ({@code POST {services.javacall.url}/internal/java-call}, body {@code {"class", "params"}}, header {@code X-Internal-Token}), the same way the service
 * reaches every collaborator it does not own. Any other name (CrearOrdenes, AgregarLog, StoredProcedure, ...) is the service adapter's.
 * It is the primary port of the application (so the services of the library use it too) but it is created by
 * {@code wiring.ColaboradoresJobsConfiguration}, not scanned: with the step 6 guard on, the guard is the primary port and this adapter its delegate.
 */
public class ColaboradoresJobs implements Colaboradores {

    static final String CABECERA = "X-Internal-Token";

    /** Collaborator simple name -> legacy fully qualified JavaCall class (the key of the router). */
    static final Map<String, String> CLASES = Map.of(
            "ConsultarFlujoCaja", "cl.bcs.plataforma.pla.ConsultarFlujoCaja",
            "ConsultarFlujoCustodiaFondoMutuo", "cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo");

    /** The names this adapter itself sends to the platform router (the others go to the service adapter), for the classification test. */
    public static java.util.Set<String> nombresPropios() {
        return java.util.Set.copyOf(CLASES.keySet());
    }

    private final Colaboradores servicio;
    private final RestClient router;
    private final String tokenInterno;

    public ColaboradoresJobs(ColaboradoresJavaCall servicio, @Value("${services.javacall.url}") String urlRouter,
            @Value("${services.javacall.connect-timeout:PT2S}") Duration tiempoConexion,
            @Value("${services.javacall.read-timeout:PT60S}") Duration tiempoLectura,
            @Value("${services.internal-token:}") String tokenInterno) {
        this(servicio, router(urlRouter, tiempoConexion, tiempoLectura), tokenInterno);
    }

    /** For tests: the router client is given. */
    ColaboradoresJobs(Colaboradores servicio, RestClient router, String tokenInterno) {
        this.servicio = servicio;
        this.router = router;
        this.tokenInterno = tokenInterno == null ? "" : tokenInterno;
    }

    /** Bounded waits: a hung collaborator must not hold a thread inside an open database transaction. */
    private static RestClient router(String url, Duration conexion, Duration lectura) {
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(conexion).build());
        fabrica.setReadTimeout(lectura);
        return RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        String clase = CLASES.get(servicio);
        if (clase == null) {
            return this.servicio.invocar(servicio, entrada);
        }
        JsonObject peticion = new JsonObject();
        peticion.addProperty("class", clase);
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
