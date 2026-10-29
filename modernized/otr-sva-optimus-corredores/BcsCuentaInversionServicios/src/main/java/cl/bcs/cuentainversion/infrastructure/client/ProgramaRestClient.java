package cl.bcs.cuentainversion.infrastructure.client;

import cl.bcs.cuentainversion.port.out.AgregarProgramaPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class ProgramaRestClient implements AgregarProgramaPort {

    private final RestClient restClient;

    public ProgramaRestClient(@Value("${services.programa.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void agendarActualizarSaldoCargoAbono(Long idEmpresa, Long idUsuario,
            List<Long> idsCuentaInversion) {
        restClient.post()
                .uri("/api/v1/programa/actualizar-saldo-cargo-abono")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "idEmpresa", idEmpresa,
                        "idUsuario", idUsuario,
                        "idsCuentaInversion", idsCuentaInversion))
                .retrieve()
                .toBodilessEntity();
    }
}
