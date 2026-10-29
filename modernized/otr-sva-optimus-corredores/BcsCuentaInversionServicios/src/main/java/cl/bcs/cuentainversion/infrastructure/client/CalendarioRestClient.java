package cl.bcs.cuentainversion.infrastructure.client;

import cl.bcs.cuentainversion.port.out.ObtenerDiaSiguientePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Component
public class CalendarioRestClient implements ObtenerDiaSiguientePort {

    private final RestClient restClient;

    public CalendarioRestClient(@Value("${services.calendario.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public LocalDate obtenerDiaSiguiente(LocalDate fecha, int plazoEnDias, Long idEmpresa) {
        return restClient.get()
                .uri("/api/v1/calendario/dia-siguiente?fecha={f}&plazoEnDias={p}&idEmpresa={e}",
                        fecha, plazoEnDias, idEmpresa)
                .retrieve()
                .body(LocalDate.class);
    }
}
