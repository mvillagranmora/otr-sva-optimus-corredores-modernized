package cl.bcs.cuentainversion.infrastructure.client;

import cl.bcs.cuentainversion.port.out.CrearMovimientoComprobantePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ComprobanteRestClient implements CrearMovimientoComprobantePort {

    private final RestClient restClient;

    public ComprobanteRestClient(@Value("${services.comprobante.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void crearMovimientoComprobante(Long idEmpresa, Long idUsuario, String concepto,
            String tipoVoucher, Long idPortafolio, BigDecimal monto,
            String idCondicion, String idMoneda) {
        restClient.post()
                .uri("/api/v1/comprobante/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "idEmpresa", idEmpresa,
                        "idUsuario", idUsuario,
                        "concepto", concepto,
                        "tipoVoucher", tipoVoucher,
                        "idPortafolio", idPortafolio,
                        "monto", monto,
                        "idCondicion", idCondicion,
                        "idMoneda", idMoneda))
                .retrieve()
                .toBodilessEntity();
    }
}
