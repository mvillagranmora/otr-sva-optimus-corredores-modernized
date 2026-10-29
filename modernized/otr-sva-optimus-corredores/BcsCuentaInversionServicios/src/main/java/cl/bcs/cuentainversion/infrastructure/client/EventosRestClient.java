package cl.bcs.cuentainversion.infrastructure.client;

import cl.bcs.cuentainversion.port.out.AgregarEventoPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class EventosRestClient implements AgregarEventoPort {

    private final RestClient restClient;

    public EventosRestClient(@Value("${services.eventos.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void agregarEvento(Long idEmpresa, String codigoEvento, Map<String, Object> datos) {
        restClient.post()
                .uri("/api/v1/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "idEmpresa", idEmpresa,
                        "codigoEvento", codigoEvento,
                        "datos", datos))
                .retrieve()
                .toBodilessEntity();
    }
}
