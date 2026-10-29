package cl.bcs.fondomutuo.adapter.out.http;

import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.time.Duration;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.kit.ColaboradorErrorException;
import cl.bcs.fondomutuo.kit.EscrituraConfirmada;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * Client of the platform JavaCall router for the HTTP collaborators: {@code POST {services.javacall.url}/internal/java-call},
 * body {@code {"class": <legacy FQN>, "params": <legacy JSON>}}, header {@code X-Internal-Token} (from the environment; never logged). Bounded waits
 * (a hung collaborator must not hold a thread inside an open database transaction). Failure mapping, decided by what is KNOWN about the remote effect:
 * <ul>
 *   <li>HTTP 503 from the router (class not served: it already fell back or has nowhere to go) and connection refused / connect timeout (nothing was
 *       sent): {@link ServicioNoServidoException} -> the request answers 503 and rolls back, TomEE repeats it (DEV-F5, gate G5);</li>
 *   <li>read timeout (the request was sent), HTTP 500 or any other status, body that is not a JSON object:
 *       {@link ColaboradorErrorException} -> rollback and the generic 500 (never a 503: the collaborator may have committed).</li>
 * </ul>
 * M3: once a write of this request may be committed ({@link EscrituraConfirmada}), the two 503 cases above become the generic 500 as well (a TomEE repeat
 * would duplicate the write). M2: the read timeout default is 10 s ({@code services.javacall.read-timeout}); every HTTP call holds ONE pooled database
 * connection (the unit of work is open), so the worst case of a request is (number of calls) x (read timeout) and the pool must be sized for the
 * concurrent requests (see TRANSFORMATION_NOTES, Step 6).
 * Holds no per-request state; thread-safe.
 */
@Component
public class RouterClient {

    public static final String CABECERA = "X-Internal-Token";
    public static final String RUTA = "/internal/java-call";

    private final RestClient router;
    private final String token;

    public RouterClient(@Value("${services.javacall.url}") String url,
            @Value("${services.javacall.connect-timeout:PT2S}") Duration conexion,
            @Value("${services.javacall.read-timeout:PT10S}") Duration lectura,
            @Value("${services.internal-token:}") String token) {
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(conexion).build());
        fabrica.setReadTimeout(lectura);
        this.router = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
        this.token = token == null ? "" : token;
    }

    /** Collaborators that WRITE (and commit in their own module); the others only read. */
    static final Set<String> ESCRITURAS = Set.of(ColaboradoresHttp.AGREGAR_LOG, ColaboradoresHttp.INSCRIBIR_PROCESO_BATCH,
            ColaboradoresHttp.NOTIFICAR_ESTADO_ORDENES_OWP);

    /** A 503 / unreachable router is repeatable by TomEE ONLY while nothing of this request can be committed (M3); otherwise it is the generic 500. */
    private static RuntimeException noServido(String clase, String motivo) {
        if (EscrituraConfirmada.confirmada()) {
            return new ColaboradorErrorException("collaborator " + clase + " not served AFTER a write of this request was committed: no repeat (" + motivo + ")", null);
        }
        return new ServicioNoServidoException("ROUTER", motivo);
    }

    public JsonObject invocar(String clase, JsonObject params) {
        JsonObject peticion = new JsonObject();
        peticion.addProperty("class", clase);
        peticion.add("params", params == null ? new JsonObject() : params);
        RestClient.RequestBodySpec solicitud = router.post().uri(RUTA).contentType(MediaType.APPLICATION_JSON);
        if (!token.isBlank()) {
            solicitud = solicitud.header(CABECERA, token);
        }
        String cuerpo;
        try {
            cuerpo = solicitud.body(peticion.toString()).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 503) {
                throw noServido(clase, clase + " not served (HTTP 503)");
            }
            throw new ColaboradorErrorException("collaborator " + clase + " answered HTTP " + e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof ConnectException || e.getCause() instanceof HttpConnectTimeoutException) {
                throw noServido(clase, clase + " unreachable (connection refused or connect timeout)");
            }
            throw new ColaboradorErrorException("collaborator " + clase + " did not answer (" + e.getCause() + ")", e);
        }
        try {
            JsonElement salida = cuerpo == null ? null : JsonParser.parseString(cuerpo);
            if (salida == null || !salida.isJsonObject()) {
                throw new ColaboradorErrorException("collaborator " + clase + " answered a body that is not a JSON object", null);
            }
            if (ESCRITURAS.contains(clase)) {
                EscrituraConfirmada.escrituraRemotaConfirmada();   // M3: the remote module committed on its own; a later 503 must not make TomEE repeat
            }
            return salida.getAsJsonObject();
        } catch (RuntimeException e) {
            if (e instanceof ColaboradorErrorException c) throw c;
            throw new ColaboradorErrorException("collaborator " + clase + " answered an unreadable body", e);
        }
    }
}
