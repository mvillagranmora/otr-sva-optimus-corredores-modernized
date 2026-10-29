package cl.bcs.tesoreria.jobs.adapter;

import cl.bcs.tesoreria.infrastructure.adapter.ColaboradoresJavaCall;
import cl.bcs.tesoreria.jobs.job.port.ServiciosExternos;
import cl.bcs.tesoreria.port.out.Colaboradores;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

/**
 * The {@link Colaboradores} port of the job module: the production {@link ColaboradoresJavaCall} of tesoreria-service plus the four
 * services that only the jobs call and that it does not route yet (decision 3, 2026-10-16; the Servicios sources are not edited):
 * {@code ValidarMandatosPAC} (APV), {@code ValidaEjecucionProcesoRecPAC}, {@code ConsultarEstadoModulo} and
 * {@code ObtenerDiaAnterior} (Plataforma). They live outside the Tesoreria family, so they go to the platform JavaCall router
 * ({@code POST {services.javacall.url}/internal/java-call}, body {@code {"class", "params"}}, header {@code X-Internal-Token}), the
 * same way the service reaches every collaborator it does not own. Any other name (AgregarLog, ...) is the service adapter's.
 * It is the HTTP port of the module: {@code wiring.ColaboradoresJobsConfiguration} creates it and makes it the {@code @Primary}
 * {@link Colaboradores} (the services of the library use it too) unless {@code tesoreria.jobs.colaboradores-en-proceso=true}, in which case
 * {@link ColaboradoresEnProceso} is the primary one and delegates to this one every collaborator that is not already migrated (step 5).
 * It is deliberately NOT a {@code @Component}: its role (primary or delegate) depends on that property.
 */
public class ColaboradoresJobs implements Colaboradores {

    static final String CABECERA = "X-Internal-Token";

    /** Collaborator simple name -> legacy fully qualified JavaCall class (the key of the router). */
    static final Map<String, String> CLASES = Map.of(
            ServiciosExternos.VALIDAR_MANDATOS_PAC, "cl.bcs.APV.ValidarMandatosPAC",
            ServiciosExternos.VALIDA_EJECUCION_PROCESO_REC_PAC, "cl.bcs.plataforma.pla.ValidaEjecucionProcesoRecPAC",
            ServiciosExternos.CONSULTAR_ESTADO_MODULO, "cl.bcs.plataforma.cre.ConsultarEstadoModulo",
            ServiciosExternos.OBTENER_DIA_ANTERIOR, "cl.bcs.plataforma.pla.ObtenerDiaAnterior");

    private final Colaboradores servicio;
    private final RestClient router;
    private final String tokenInterno;

    @Autowired
    public ColaboradoresJobs(ColaboradoresJavaCall servicio, @Value("${services.javacall.url}") String urlRouter,
                             @Value("${services.javacall.connect-timeout:PT2S}") Duration tiempoConexion,
                             @Value("${services.javacall.read-timeout:PT60S}") Duration tiempoLectura,
                             @Value("${services.internal-token:}") String tokenInterno) {
        // bounded waits: a hung collaborator must not hold a thread inside an open database transaction
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(tiempoConexion).build());
        fabrica.setReadTimeout(tiempoLectura);
        this(servicio, RestClient.builder().baseUrl(urlRouter).requestFactory(fabrica).build(), tokenInterno);
    }

    /** For tests: the router client is given. */
    ColaboradoresJobs(Colaboradores servicio, RestClient router, String tokenInterno) {
        this.servicio = servicio;
        this.router = router;
        this.tokenInterno = tokenInterno == null ? "" : tokenInterno;
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
