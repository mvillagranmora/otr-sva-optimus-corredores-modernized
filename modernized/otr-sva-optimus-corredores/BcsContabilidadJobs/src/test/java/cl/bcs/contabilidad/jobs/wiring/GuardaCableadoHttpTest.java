package cl.bcs.contabilidad.jobs.wiring;

import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.port.out.ProcesoBatchPort;
import cl.bcs.contabilidad.infrastructure.plataforma.TomEeProcesoBatchClient;
import cl.bcs.contabilidad.jobs.adapter.PlataformaEnProceso;
import cl.bcs.contabilidad.jobs.guarda.GuardaTomee;
import cl.bcs.contabilidad.jobs.guarda.GuardaTomeeException;
import cl.bcs.contabilidad.jobs.guarda.ProcesoBatchGuardado;
import cl.bcs.contabilidad.jobs.programa.EjecutorPrograma;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Step 6 ON ({@code contabilidad.jobs.guarda-tomee=true}) with the real application context on H2 and a fake TomEE: the batch registration port is
 * the guard around the HTTP adapter, the step 5 port is untouched and the executor has the guard. The H2 of the context has no
 * {@code BSCL.BAT.LogPrograma} (the marks tests use the BAT fixture), which makes the fail-closed path observable: no mark, no HTTP call.
 */
@SpringBootTest(properties = {"contabilidad.jobs.sesion-hana=false", "contabilidad.jobs.colaboradores-en-proceso=true",
        "contabilidad.jobs.guarda-tomee=true"})
@ActiveProfiles("test")
class GuardaCableadoHttpTest {

    private static final List<String> PETICIONES = new CopyOnWriteArrayList<>();
    private static final HttpServer TOMEE = arrancarTomee();

    @Autowired
    ApplicationContext contexto;
    @Autowired
    ProcesoBatchPort procesoBatch;
    @Autowired
    PlataformaPort plataforma;
    @Autowired
    EjecutorPrograma ejecutor;

    private static HttpServer arrancarTomee() {
        try {
            HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            servidor.createContext("/", ex -> {
                PETICIONES.add(ex.getRequestURI().getPath());
                ex.getRequestBody().readAllBytes();
                byte[] respuesta = "{}".getBytes(StandardCharsets.UTF_8);
                ex.getResponseHeaders().add("Content-Type", "application/json");
                ex.sendResponseHeaders(200, respuesta.length);
                ex.getResponseBody().write(respuesta);
                ex.close();
            });
            servidor.start();
            return servidor;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("plataforma.base-url", () -> "http://127.0.0.1:" + TOMEE.getAddress().getPort());
    }

    @BeforeEach
    void limpiar() {
        PETICIONES.clear();
    }

    @Test
    @DisplayName("G6-W01: guard on: the primary ProcesoBatchPort is the guard around TomEeProcesoBatchClient; PlataformaPort stays the step 5 one; the executor has the guard")
    void laGuardaEnvuelveElPuertoDeProcesoBatch() {
        assertThat(procesoBatch).isInstanceOf(ProcesoBatchGuardado.class);
        assertThat(contexto.getBeansOfType(ProcesoBatchPort.class)).hasSize(2);
        assertThat(ReflectionTestUtils.getField(procesoBatch, "delegado")).isInstanceOf(TomEeProcesoBatchClient.class);
        assertThat(plataforma).isInstanceOf(PlataformaEnProceso.class);
        assertThat(contexto.getBeansOfType(GuardaTomee.class)).hasSize(1);
        assertThat(ReflectionTestUtils.getField(ejecutor, "guarda")).isSameAs(contexto.getBean(GuardaTomee.class));
    }

    @Test
    @DisplayName("G6-W02: no open run (caller mode): the call goes through to TomEE; inside a run the INTENTO cannot be written here, so NO request leaves")
    void conYSinCorrida() {
        procesoBatch.inscribir(1L, "CTB", 2L, "ASIENTO_SAP", 0, List.of());
        assertThat(PETICIONES).containsExactly("/api/plataforma/InscribirProcesoBatch");

        PETICIONES.clear();
        GuardaTomee guarda = contexto.getBean(GuardaTomee.class);
        guarda.abrir(900L, 1, "ASIENTO_SAP");
        try {
            assertThatThrownBy(() -> procesoBatch.inscribir(1L, "CTB", 2L, "ASIENTO_SAP", 0, List.of())).isInstanceOf(GuardaTomeeException.class);
        } finally {
            guarda.cerrar();
        }
        assertThat(PETICIONES).isEmpty();
    }
}
