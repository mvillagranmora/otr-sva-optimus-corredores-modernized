package cl.bcs.tesoreria.infrastructure.client;

import cl.bcs.tesoreria.domain.model.DepositanteResult;
import cl.bcs.tesoreria.domain.model.TransaccionBCPResult;
import cl.bcs.tesoreria.domain.port.out.PlataformaPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * HTTP adapter for the Plataforma cross-service during the strangler-fig transition.
 * The target service is still TomEE/EJB — this client will connect once that service is modernized.
 *
 * Source: BcsTesoreriaServicios — PlataformaPort (7 methods).
 * Base URL configured via ${PLATAFORMA_BASE_URL} (default: http://localhost:8080).
 */
@Component
public class PlataformaRestClient implements PlataformaPort {

    private final RestClient restClient;

    public PlataformaRestClient(
            @Value("${plataforma.base-url:http://localhost:8080}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public boolean validarRequisitosBasicos(Long idEmpresa, Long idUsuario, String idModulo) {
        Boolean ok = restClient.get()
                .uri("/api/v1/plataforma/requisitos-basicos?idEmpresa={e}&idUsuario={u}&idModulo={m}",
                        idEmpresa, idUsuario, idModulo)
                .retrieve()
                .body(Boolean.class);
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public String obtenerFechaSistema(Long idEmpresa) {
        return restClient.get()
                .uri("/api/v1/plataforma/fecha-sistema/{idEmpresa}", idEmpresa)
                .retrieve()
                .body(String.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> obtenerMensajesRequisitosBasicos(Long idEmpresa, Long idUsuario, String idModulo) {
        return restClient.get()
                .uri("/api/v1/plataforma/requisitos-basicos/mensajes?idEmpresa={e}&idUsuario={u}&idModulo={m}",
                        idEmpresa, idUsuario, idModulo)
                .retrieve()
                .body(List.class);
    }

    @Override
    public void agregarDetalleLog(Object idEmpresa, Object idUsuario,
                                   String campo, String valorAnterior, String valorActual) {
        record DetalleLog(Object idEmpresa, Object idUsuario,
                          String campo, String valorAnterior, String valorActual) {}
        restClient.post()
                .uri("/api/v1/plataforma/log/detalle")
                .body(new DetalleLog(idEmpresa, idUsuario, campo, valorAnterior, valorActual))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public DepositanteResult consultarDepositanteBCP(Object request) {
        return restClient.post()
                .uri("/api/v1/plataforma/bcp/depositante/consultar")
                .body(request)
                .retrieve()
                .body(DepositanteResult.class);
    }

    @Override
    public TransaccionBCPResult procesarTransaccionBCP(Object request) {
        return restClient.post()
                .uri("/api/v1/plataforma/bcp/transaccion")
                .body(request)
                .retrieve()
                .body(TransaccionBCPResult.class);
    }

    @Override
    public void revocarContratoCSC(Long idFolioCSC) {
        restClient.post()
                .uri("/api/v1/plataforma/contratos-csc/{idFolioCSC}/revocar", idFolioCSC)
                .retrieve()
                .toBodilessEntity();
    }
}
