package cl.bcs.cuentainversion.infrastructure.client;

import cl.bcs.cuentainversion.port.out.SaldoFlujoCajaPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class FlujoCajaRestClient implements SaldoFlujoCajaPort {

    private final RestClient restClient;

    public FlujoCajaRestClient(@Value("${services.flujo-caja.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public BigDecimal consultarSaldo(Long idPortafolio, String idMoneda, Long idEmpresa,
            Long idUsuario, LocalDate fecha) {
        return restClient.get()
                .uri("/api/v1/flujo-caja/saldo?idPortafolio={p}&idMoneda={m}&idEmpresa={e}&idUsuario={u}&fecha={f}",
                        idPortafolio, idMoneda, idEmpresa, idUsuario, fecha)
                .retrieve()
                .body(BigDecimal.class);
    }
}
