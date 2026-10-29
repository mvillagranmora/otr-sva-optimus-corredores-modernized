package cl.bcs.cuentainversion.infrastructure.client;

import cl.bcs.cuentainversion.port.out.CalcularParidadPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Component
public class ParidadRestClient implements CalcularParidadPort {

    private final RestClient restClient;

    public ParidadRestClient(@Value("${services.paridad.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public BigDecimal calcularParidad(Long idEmpresa, String idMonedaOrigen,
            String idMonedaDestino, BigDecimal monto, LocalDate fecha) {
        return restClient.post()
                .uri("/api/v1/paridad/calcular")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "idEmpresa", idEmpresa,
                        "idMonedaOrigen", idMonedaOrigen,
                        "idMonedaDestino", idMonedaDestino,
                        "monto", monto,
                        "fecha", fecha))
                .retrieve()
                .body(BigDecimal.class);
    }
}
