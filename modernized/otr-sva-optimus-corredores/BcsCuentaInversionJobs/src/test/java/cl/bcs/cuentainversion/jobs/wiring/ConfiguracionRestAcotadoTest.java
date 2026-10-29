package cl.bcs.cuentainversion.jobs.wiring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.bcs.cuentainversion.infrastructure.client.ConfiguracionRestClient;
import cl.bcs.cuentainversion.jobs.adapter.ConfiguracionRestAcotado;
import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/** Review H-4: the configuration client of the Servicios library is replaced by one with bounded waits. */
@SpringBootTest(properties = {"cuentainversion.jobs.sesion-hana=false"})
@ActiveProfiles("test")
class ConfiguracionRestAcotadoTest {

    @Autowired
    ApplicationContext contexto;

    @Test
    @DisplayName("the only ConfiguracionSistemaPort of the context is the bounded one, not the library class")
    void soloElClienteAcotado() {
        assertThat(contexto.getBeansOfType(ConfiguracionSistemaPort.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(ConfiguracionRestClient.class).values()).allMatch(b -> b instanceof ConfiguracionRestAcotado);
    }

    @Test
    @DisplayName("a configuration service that does not answer fails by the read timeout instead of hanging")
    void lecturaConTimeout() throws Exception {
        CountDownLatch liberar = new CountDownLatch(1);
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", ex -> {
            try {
                liberar.await();
            } catch (InterruptedException ignorada) {
                Thread.currentThread().interrupt();
            }
            ex.close();
        });
        servidor.start();
        try {
            ConfiguracionRestAcotado cliente = new ConfiguracionRestAcotado("http://127.0.0.1:" + servidor.getAddress().getPort(),
                    Duration.ofSeconds(1), Duration.ofMillis(300));
            long t0 = System.nanoTime();
            assertThatThrownBy(() -> cliente.usaCargoAbonoJava(1L)).isInstanceOf(RuntimeException.class);
            assertThat(Duration.ofNanos(System.nanoTime() - t0)).isLessThan(Duration.ofSeconds(10));
        } finally {
            liberar.countDown();
            servidor.stop(0);
        }
    }

    @Test
    @DisplayName("same request and answer as the library client (flag 'S' read through getConfiguracion)")
    void mismaPeticionQueLaLibreria() throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/api/v1/configuracion", ex -> {
            byte[] cuerpo = (ex.getRequestURI().getQuery().contains("clave=PLA.UsaCargoAbonoJava") ? "S" : "N").getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, cuerpo.length);
            ex.getResponseBody().write(cuerpo);
            ex.close();
        });
        servidor.start();
        try {
            ConfiguracionRestAcotado cliente = new ConfiguracionRestAcotado("http://127.0.0.1:" + servidor.getAddress().getPort(),
                    Duration.ofSeconds(1), Duration.ofSeconds(5));
            assertThat(cliente.usaCargoAbonoJava(1L)).isTrue();
            assertThat(cliente.permiteAnularMovimiento(1L)).isFalse();
        } finally {
            servidor.stop(0);
        }
    }
}
