package cl.bcs.jobmanager.service;

import cl.bcs.jobmanager.config.JobManagerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * POST {destino}/internal/job-call with {@code {"idPrograma", "clase"}} and the X-Internal-Token header. The job module
 * runs the job synchronously inside the call, so the read timeout is generous and its expiry is NOT a job failure.
 * The status is mapped to a {@link JobCallDesenlace}; this class never touches the database.
 */
@Component
public class JobCallClient {

    public static final String RUTA = "/internal/job-call";
    public static final String CABECERA_TOKEN = "X-Internal-Token";

    private static final Logger log = LoggerFactory.getLogger(JobCallClient.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final HttpClient http;
    private final Duration readTimeout;
    private final String token;

    public JobCallClient(JobManagerProperties props) {
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(props.getConnectTimeoutMs()))
                .build();
        this.readTimeout = Duration.ofMillis(props.getReadTimeoutMs());
        this.token = props.getInternalToken() == null ? "" : props.getInternalToken();
    }

    public JobCallDesenlace llamar(String baseUrl, long idPrograma, String clase) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("idPrograma", idPrograma);
        if (clase != null && !clase.isBlank()) {
            cuerpo.put("clase", clase);
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + RUTA))
                .timeout(readTimeout)
                .header("Content-Type", "application/json")
                .header(CABECERA_TOKEN, token)
                .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(cuerpo)))
                .build();
        try {
            HttpResponse<Void> r = http.send(request, HttpResponse.BodyHandlers.discarding());
            return mapear(r.statusCode(), idPrograma);
        } catch (HttpConnectTimeoutException | ConnectException e) {
            log.warn("Job #{}: {} not reachable ({}); nothing was sent, it stays PEN", idPrograma, baseUrl, e.getClass().getSimpleName());
            return JobCallDesenlace.NO_ENVIADO;
        } catch (HttpTimeoutException e) {
            log.info("Job #{}: no response within {} ms; the job module owns the state, not treated as failure", idPrograma, readTimeout.toMillis());
            return JobCallDesenlace.SIN_RESPUESTA;
        } catch (IOException e) {
            log.warn("Job #{}: I/O error during the call ({}); state unknown, the job module owns it", idPrograma, e.getClass().getSimpleName());
            return JobCallDesenlace.SIN_RESPUESTA;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Job #{}: call interrupted (shutdown); the job module owns the state", idPrograma);
            return JobCallDesenlace.SIN_RESPUESTA;
        }
    }

    static JobCallDesenlace mapear(int status, long id) {
        switch (status) {
            case 200:
                log.info("Job #{} done (200)", id);
                return JobCallDesenlace.HECHO;
            case 409:
                log.info("Job #{} already claimed by the job module (409), skipped", id);
                return JobCallDesenlace.YA_RECLAMADO;
            case 404, 422:
                log.error("Job #{} rejected with {}: configuration/data inconsistency (program row or class), no state change", id, status);
                return JobCallDesenlace.INCONSISTENTE;
            case 503:
                log.warn("Job #{} got 503: the job module does not run that class (check jobmanager.destinos), no state change", id);
                return JobCallDesenlace.CLASE_NO_EJECUTADA;
            case 500:
                log.info("Job #{} failed inside the job module (500); it already wrote ERR", id);
                return JobCallDesenlace.ERROR_DEL_JOB;
            default:
                log.error("Job #{} got unexpected status {} (token or URL misconfigured?), no state change", id, status);
                return JobCallDesenlace.RESPUESTA_INESPERADA;
        }
    }
}
