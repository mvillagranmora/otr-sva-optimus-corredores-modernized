package cl.bcs.cuentainversion.infrastructure.client;

import cl.bcs.cuentainversion.port.out.EntregarPreciosTasasPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class PreciosTasasRestClient implements EntregarPreciosTasasPort {

    private static final ParameterizedTypeReference<Map<Long, BigDecimal>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public PreciosTasasRestClient(@Value("${services.precios-tasas.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public Map<Long, BigDecimal> consultarValoresCuota(Long idEmpresa,
            List<Long> idsInstrumento, LocalDate fecha) {
        return restClient.post()
                .uri("/api/v1/precios-tasas/valores-cuota")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "idEmpresa", idEmpresa,
                        "idsInstrumento", idsInstrumento,
                        "fecha", fecha))
                .retrieve()
                .body(MAP_TYPE);
    }
}
