package cl.bcs.cuentainversion.infrastructure.client;

import cl.bcs.cuentainversion.port.out.CrearEventoAutorizacionPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class AutorizacionRestClient implements CrearEventoAutorizacionPort {

    private final RestClient restClient;

    public AutorizacionRestClient(@Value("${services.autorizacion.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void crearEventoAutorizacion(Long idEmpresa, Long idUsuario, String concepto,
            List<Long> idsMovimiento) {
        restClient.post()
                .uri("/api/v1/autorizacion/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "idEmpresa", idEmpresa,
                        "idUsuario", idUsuario,
                        "concepto", concepto,
                        "idsMovimiento", idsMovimiento))
                .retrieve()
                .toBodilessEntity();
    }
}
