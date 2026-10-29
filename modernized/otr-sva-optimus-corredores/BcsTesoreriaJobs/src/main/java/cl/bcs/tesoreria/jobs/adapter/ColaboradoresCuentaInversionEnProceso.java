package cl.bcs.tesoreria.jobs.adapter;

import cl.bcs.cuentainversion.port.out.Colaboradores;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

/**
 * The {@link Colaboradores} port of the CuentaInversion services that this job application carries (step 5): the services
 * {@code CrearCargoAbono} / {@code AnularCargoAbono} call, by fully qualified legacy class, platform collaborators (SaldoFlujoCaja, AgregarLog,
 * AgregarPrograma, RevisarRequisitosBasicos, ...) and, through the job application, other owners.
 *
 * <p>A class that an owner library serves runs in process through {@link DespachadorEnProceso} (so a CuentaInversion service that needs a
 * Tesoreria or APV service reaches it inside the same transaction and without any library depending on the other); any other class goes to the platform
 * JavaCall router ({@code POST {services.javacall.url}/internal/java-call}, body {@code {"class", "params"}}, header {@code X-Internal-Token}),
 * with bounded waits, exactly as {@code ColaboradoresJobs} of BcsCuentaInversionJobs does. No credentials in the code.
 */
public class ColaboradoresCuentaInversionEnProceso implements Colaboradores {

    static final String CABECERA = "X-Internal-Token";

    private final DespachadorEnProceso despachador;
    private final RestClient router;
    private final String tokenInterno;

    public ColaboradoresCuentaInversionEnProceso(DespachadorEnProceso despachador, String urlRouter, Duration tiempoConexion,
                                                 Duration tiempoLectura, String tokenInterno) {
        this(despachador, urlRouter == null || urlRouter.isBlank() ? null : cliente(urlRouter, tiempoConexion, tiempoLectura), tokenInterno);
    }

    /** For tests: the router client is given ({@code null}: no router configured, a platform call fails when it is made). */
    ColaboradoresCuentaInversionEnProceso(DespachadorEnProceso despachador, RestClient router, String tokenInterno) {
        this.despachador = Objects.requireNonNull(despachador, "despachador");
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
        if (despachador.maneja(servicio)) {
            return despachador.despachar(servicio, entrada);
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
