package cl.bcs.tesoreria.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import cl.bcs.tesoreria.javacall.JavaCallDispatcher;

/** {@code ColaboradoresJavaCall}: in-process dispatch, router call with the shared token, bounded waits, collaborators without a migrated client. */
class ColaboradoresJavaCallTest {

    private HttpServer servidor;
    private final AtomicReference<String> ruta = new AtomicReference<>();
    private final AtomicReference<String> token = new AtomicReference<>();
    private final AtomicReference<String> cuerpo = new AtomicReference<>();
    private final CountDownLatch colgar = new CountDownLatch(1);
    private volatile boolean colgado;

    @BeforeEach
    void arrancaElRouter() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", exchange -> {
            ruta.set(exchange.getRequestURI().getPath());
            token.set(exchange.getRequestHeaders().getFirst("X-Internal-Token"));
            cuerpo.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (colgado) {
                try {
                    colgar.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            byte[] r = "{\"P_OUT_ERRORESCONTADOR\":0}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, r.length);
            exchange.getResponseBody().write(r);
            exchange.close();
        });
        servidor.start();
    }

    @AfterEach
    void paraElRouter() {
        colgar.countDown();
        servidor.stop(0);
    }

    private ColaboradoresJavaCall colaboradores(JavaCallDispatcher local, Duration lectura, String secreto) {
        String url = "http://127.0.0.1:" + servidor.getAddress().getPort();
        return new ColaboradoresJavaCall(local, mock(ProcedimientoAlmacenado.class), url, Duration.ofSeconds(2), lectura, secreto);
    }

    @Test
    void unaClaseDeOtroServicioVaAlRouterConElTokenCompartidoYSuNombreCompleto() {
        JavaCallDispatcher local = mock(JavaCallDispatcher.class);
        JsonObject entrada = JsonParser.parseString("{\"p_IdEmpresa\": 1}").getAsJsonObject();

        JsonObject salida = colaboradores(local, Duration.ofSeconds(5), "secreto-de-prueba").invocar("CrearCargoAbono", entrada);

        assertThat(ruta.get()).isEqualTo("/internal/java-call");
        assertThat(token.get()).isEqualTo("secreto-de-prueba");
        JsonObject enviado = JsonParser.parseString(cuerpo.get()).getAsJsonObject();
        assertThat(enviado.get("class").getAsString()).isEqualTo("cl.bcs.cuentaInversion.CrearCargoAbono");
        assertThat(enviado.getAsJsonObject("params").get("p_IdEmpresa").getAsInt()).isEqualTo(1);
        assertThat(salida.get("P_OUT_ERRORESCONTADOR").getAsInt()).isZero();
    }

    @Test
    void sinSecretoConfiguradoNoSeEnviaCabecera() {
        colaboradores(mock(JavaCallDispatcher.class), Duration.ofSeconds(5), "").invocar("AgregarLog", new JsonObject());
        assertThat(token.get()).isNull();
    }

    @Test
    void unaClaseMigradaEnEsteModuloSeEjecutaEnProcesoSinSalirALaRed() throws Exception {
        JavaCallDispatcher local = mock(JavaCallDispatcher.class);
        when(local.maneja("cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco")).thenReturn(true);
        JsonObject respuesta = JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\": 7}").getAsJsonObject();
        when(local.despachar(any(String.class), any(JsonObject.class))).thenReturn(respuesta);

        JsonObject salida = colaboradores(local, Duration.ofSeconds(5), "x").invocar("ActualizarSaldosCuentaBanco", new JsonObject());

        assertThat(salida).isSameAs(respuesta);
        assertThat(ruta.get()).isNull();
    }

    @Test
    void unRouterQueNoContestaCortaPorTiempoDeLectura() {
        colgado = true;
        ColaboradoresJavaCall c = colaboradores(mock(JavaCallDispatcher.class), Duration.ofMillis(300), "x");
        assertThatThrownBy(() -> c.invocar("AgregarLog", new JsonObject())).isInstanceOf(RuntimeException.class);
    }

    @Test
    void elClienteDeClonerNoEstaMigradoYSeReportaComoNoDisponible() {
        assertThatThrownBy(() -> colaboradores(mock(JavaCallDispatcher.class), Duration.ofSeconds(5), "x").invocar("Cloper", new JsonObject()))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
