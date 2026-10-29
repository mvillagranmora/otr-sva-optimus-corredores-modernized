package cl.bcs.cuentainversion.infrastructure.client;

import cl.bcs.cuentainversion.port.out.RevisarRequisitosBasicosPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RequisitosRestClient implements RevisarRequisitosBasicosPort {

    private final RestClient restClient;

    public RequisitosRestClient(@Value("${services.requisitos.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public boolean cumpleRequisitos(Long idPortafolio, Long idEmpresa, Long idUsuario,
            String idModulo) {
        Boolean result = restClient.get()
                .uri("/api/v1/requisitos/cumple?idPortafolio={p}&idEmpresa={e}&idUsuario={u}&idModulo={m}",
                        idPortafolio, idEmpresa, idUsuario, idModulo)
                .retrieve()
                .body(Boolean.class);
        return Boolean.TRUE.equals(result);
    }
}
