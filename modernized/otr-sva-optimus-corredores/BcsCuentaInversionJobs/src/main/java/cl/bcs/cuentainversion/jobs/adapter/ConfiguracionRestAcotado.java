package cl.bcs.cuentainversion.jobs.adapter;

import cl.bcs.cuentainversion.infrastructure.client.ConfiguracionRestClient;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Review H-4: the {@link ConfiguracionRestClient} of the Servicios library builds a plain {@code RestClient} with no timeouts, and a hung
 * configuration service would hold a thread inside an open database transaction. This bean REPLACES it (the library class is not scanned,
 * see ServiciosCuentaInversionConfiguration; the Servicios sources are untouched): the same port, the same requests (the other methods of
 * the parent call {@link #getConfiguracion}), with the connect / read timeouts of {@link ColaboradoresJobs}. {@code services.configuracion.url}
 * (CONFIGURACION_URL) has no default: a deployment without it does not start.
 */
@Component
public class ConfiguracionRestAcotado extends ConfiguracionRestClient {

    private final RestClient cliente;

    @Autowired
    public ConfiguracionRestAcotado(@Value("${services.configuracion.url}") String url,
                                    @Value("${services.configuracion.connect-timeout:PT2S}") Duration tiempoConexion,
                                    @Value("${services.configuracion.read-timeout:PT60S}") Duration tiempoLectura) {
        super(url);
        this.cliente = cliente(url, tiempoConexion, tiempoLectura);
    }

    static RestClient cliente(String url, Duration tiempoConexion, Duration tiempoLectura) {
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(tiempoConexion).build());
        fabrica.setReadTimeout(tiempoLectura);
        return RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    @Override
    public String getConfiguracion(Long idEmpresa, String clave) {
        // same request as the library class (ConfiguracionRestClient.getConfiguracion), on the bounded client
        return cliente.get()
                .uri("/api/v1/configuracion?idEmpresa={e}&clave={c}", idEmpresa, clave)
                .retrieve()
                .body(String.class);
    }
}
