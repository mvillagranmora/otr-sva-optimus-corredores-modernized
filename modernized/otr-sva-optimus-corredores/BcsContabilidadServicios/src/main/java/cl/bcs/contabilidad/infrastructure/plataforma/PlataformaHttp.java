package cl.bcs.contabilidad.infrastructure.plataforma;

import cl.bcs.contabilidad.service.FuncionalidadNoMigradaException;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * HTTP access to platform services that still live on TomEE (strangler fig).
 *
 * <p>Connect and read timeouts are explicit. Any transport or HTTP failure (404 for an endpoint the platform does
 * not expose yet, 5xx, timeout) becomes {@link FuncionalidadNoMigradaException}, which the JavaCall endpoint
 * answers with 503. The unit of work is then rolled back and the call falls back to TomEE as a whole. A failure
 * is never turned into a default value.
 */
final class PlataformaHttp {

    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    static final Duration READ_TIMEOUT = Duration.ofSeconds(60);

    private final RestClient restClient;

    PlataformaHttp(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    String post(String uri, String jsonBody) {
        try {
            return restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new FuncionalidadNoMigradaException("Servicio de plataforma " + uri + " no disponible: "
                    + e.getClass().getSimpleName());
        }
    }
}
