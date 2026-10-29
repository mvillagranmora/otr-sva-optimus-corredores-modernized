package cl.bcs.plataforma.internal;

import cl.bcs.plataforma.service.InternalJavaCallRouter;
import cl.bcs.plataforma.service.JavaCallRouterService;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/** Builds the real router against local fake servers. */
final class Montaje {

    static final String TOKEN = "token-de-prueba";

    private Montaje() {
    }

    static JavaCallRouterService base(String habilitadas) {
        return new JavaCallRouterService("http://pla:1", "http://soc:1", "http://ord:1", "http://nor:1", "http://mes:1", "http://cus:1",
                "http://fmm:1", "http://apv:1", "http://tes:1", "http://fac:1", "http://cta:1", "http://con:1", "http://por:1", "http://cag:1",
                "http://tomee:1", "", RestClient.builder(), habilitadas);
    }

    static ClienteInterno cliente(int lecturaMs) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(Duration.ofMillis(1000));
        f.setReadTimeout(Duration.ofMillis(lecturaMs));
        return new ClienteInterno(RestClient.builder().requestFactory(f).build(), TOKEN);
    }

    static InternalJavaCallRouter router(String habilitadas, String destinos, String tomeeUrl, RegistroServicios registro, int lecturaMs) {
        return new InternalJavaCallRouter(base(habilitadas), cliente(lecturaMs), registro, destinos, tomeeUrl);
    }

    static SolicitudJavaCall solicitud(String json) {
        return SolicitudJavaCall.parsear(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
