package cl.bcs.plataforma.internal;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

/**
 * HTTP leg toward the owners and TomEE. Any status (2xx, 4xx, 5xx) comes back as a {@link Respuesta}, never as an exception;
 * only transport failures (connection refused, timeout) throw {@link org.springframework.web.client.ResourceAccessException}.
 * Sends {@code X-Internal-Token} when a secret is configured and never an idempotency header.
 */
public class ClienteInterno {

    public static final String CABECERA_TOKEN = "X-Internal-Token";

    private final RestClient rest;
    private final String token;

    public ClienteInterno(RestClient rest, String token) {
        this.rest = rest;
        this.token = token == null ? "" : token;
    }

    public Respuesta post(String url, String json, boolean conToken, String sessionToken) {
        RestClient.RequestBodySpec req = rest.post().uri(url).contentType(MediaType.APPLICATION_JSON);
        if (conToken && !token.isBlank()) req = req.header(CABECERA_TOKEN, token);
        if (sessionToken != null && !sessionToken.isBlank()) req = req.header("X-SESSION-TOKEN", sessionToken);
        return req.body(json).exchange(ClienteInterno::leer, false);
    }

    public Respuesta put(String url, String json) {
        RestClient.RequestBodySpec req = rest.put().uri(url).contentType(MediaType.APPLICATION_JSON);
        if (!token.isBlank()) req = req.header(CABECERA_TOKEN, token);
        return req.body(json).exchange(ClienteInterno::leer, false);
    }

    public Respuesta get(java.net.URI uri) {
        RestClient.RequestHeadersSpec<?> req = rest.get().uri(uri);
        if (!token.isBlank()) req = req.header(CABECERA_TOKEN, token);
        return req.exchange(ClienteInterno::leer, false);
    }

    private static Respuesta leer(org.springframework.http.HttpRequest rq, RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse rs)
            throws java.io.IOException {
        byte[] b = rs.getBody().readAllBytes();
        MediaType ct = rs.getHeaders().getContentType();
        return new Respuesta(rs.getStatusCode().value(), ct == null ? null : ct.toString(), new String(b, StandardCharsets.UTF_8));
    }
}
