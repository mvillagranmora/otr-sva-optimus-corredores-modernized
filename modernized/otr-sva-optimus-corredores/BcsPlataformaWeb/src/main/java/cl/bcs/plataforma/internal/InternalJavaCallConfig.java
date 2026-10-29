package cl.bcs.plataforma.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

/** Wiring of the internal JavaCall router: HTTP client with timeouts and the optional registry export (class -> IdServicio). */
@Configuration
public class InternalJavaCallConfig {

    @Bean
    ClienteInterno internalJavaCallClient(
            @Value("${plataforma.javacall.internal.connect-timeout-ms:3000}") int conexionMs,
            @Value("${plataforma.javacall.internal.read-timeout-ms:60000}") int lecturaMs,
            @Value("${plataforma.javacall.internal-token:}") String tokenJavaCall,
            @Value("${services.internal-token:}") String tokenServicios) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(Duration.ofMillis(conexionMs));
        f.setReadTimeout(Duration.ofMillis(lecturaMs));
        String token = tokenJavaCall != null && !tokenJavaCall.isBlank() ? tokenJavaCall : tokenServicios;
        return new ClienteInterno(RestClient.builder().requestFactory(f).build(), token);
    }

    /** {@code plataforma.javacall.registro-servicios}: Spring resource location of the export; empty = no TomEE ids from file. */
    @Bean
    RegistroServicios registroServicios(@Value("${plataforma.javacall.registro-servicios:}") String ubicacion) throws IOException {
        if (ubicacion == null || ubicacion.isBlank()) return RegistroServicios.vacio();
        Resource r = new DefaultResourceLoader().getResource(ubicacion.trim());
        try (InputStream in = r.getInputStream()) {
            return RegistroServicios.desde(in);
        }
    }
}
