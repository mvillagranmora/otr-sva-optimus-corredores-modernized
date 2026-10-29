package cl.bcs.cuentainversion.infrastructure.client;

import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Component
public class ConfiguracionRestClient implements ConfiguracionSistemaPort {

    private final RestClient restClient;

    public ConfiguracionRestClient(@Value("${services.configuracion.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public String getConfiguracion(Long idEmpresa, String clave) {
        return restClient.get()
                .uri("/api/v1/configuracion?idEmpresa={e}&clave={c}", idEmpresa, clave)
                .retrieve()
                .body(String.class);
    }

    @Override
    public boolean usaCargoAbonoJava(Long idEmpresa) {
        return "S".equalsIgnoreCase(getConfiguracion(idEmpresa, "PLA.UsaCargoAbonoJava"));
    }

    @Override
    public boolean permiteAnularMovimiento(Long idEmpresa) {
        return "S".equalsIgnoreCase(getConfiguracion(idEmpresa, "CUENTAINVERSION.PERMITEANULARMOVIMIENTO"));
    }

    @Override
    public boolean validaFlujoCajaEnCertificacion(Long idEmpresa) {
        return "S".equalsIgnoreCase(getConfiguracion(idEmpresa, "CUENTAINVERSION.VALIDAFLUJOCAJAENCERTIFICACION"));
    }

    @Override
    public LocalDate getFechaGoLive(Long idEmpresa) {
        String raw = getConfiguracion(idEmpresa, "PLA.FechaGoLive");
        return raw != null ? LocalDate.parse(raw.substring(0, 10)) : null;
    }
}
