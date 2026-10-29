package cl.bcs.cuentainversion.infrastructure.client;

import cl.bcs.cuentainversion.port.out.AgregarLogPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class LogRestClient implements AgregarLogPort {

    private final RestClient restClient;

    public LogRestClient(@Value("${services.log.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void agregarLog(Long idEmpresa, Long idUsuario, String idModulo, String descripcion) {
        restClient.post()
                .uri("/api/v1/log")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "idEmpresa", idEmpresa,
                        "idUsuario", idUsuario,
                        "idModulo", idModulo,
                        "descripcion", descripcion))
                .retrieve()
                .toBodilessEntity();
    }
}
