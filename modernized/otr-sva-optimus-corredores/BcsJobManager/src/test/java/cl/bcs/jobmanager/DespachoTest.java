package cl.bcs.jobmanager;

import cl.bcs.jobmanager.port.out.ProgramaPort;
import cl.bcs.jobmanager.service.ProgramaDespachador.Aceptacion;
import cl.bcs.jobmanager.support.BatH2;
import cl.bcs.jobmanager.support.MockModulo;
import cl.bcs.jobmanager.support.Stack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static cl.bcs.jobmanager.support.BatH2.AHORA;
import static org.assertj.core.api.Assertions.assertThat;

/** Dispatcher end to end: real adapter on H2 (real BAT names) + real HTTP client + local mock job module. */
class DespachoTest {

    private BatH2 bat;

    @BeforeEach
    void init() {
        bat = new BatH2();
        bat.tipo("T1", "cl.bcs.tesoreria.jobs.UnJob", 1);
    }

    private Stack stack(MockModulo m, String... tipos) {
        Stack s = new Stack(bat.adapter, List.of(tipos));
        s.props.getDestinos().put("T1", m.url());
        return s.arrancar();
    }

    @Test
    void inerteSinTiposHabilitados_nadaSeConsultaNiSeEnvia() throws Exception {
        ProgramaPort puerto = Mockito.mock(ProgramaPort.class);
        try (MockModulo m = new MockModulo()) {
            Stack s = new Stack(puerto, List.of());
            s.props.getDestinos().put("T1", m.url());
            s.arrancar();
            bat.programa("T1").insertar();
            assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(0);
            Mockito.verifyNoInteractions(puerto, s.nodos);
            assertThat(m.peticiones()).isEmpty();
        }
    }

    @Test
    void elegibleSeEnviaUnaVez_yLaFilaQuedaIntacta() throws Exception {
        try (MockModulo m = new MockModulo()) {
            long id = bat.programa("T1").insertar();
            List<Object> antes = bat.filaCompleta(id);
            Stack s = stack(m, "T1");
            assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(1);
            s.esperarFin();
            assertThat(m.peticiones()).hasSize(1);
            assertThat(m.peticiones().get(0).cuerpo()).contains("\"idPrograma\":" + id);
            assertThat(bat.filaCompleta(id)).isEqualTo(antes); // the job module owns the claim and the final state
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 409, 404, 422, 503, 500, 401})
    void cadaRespuestaDejaLaFilaSinTocar(int status) throws Exception {
        try (MockModulo m = new MockModulo().responde(status)) {
            long id = bat.programa("T1").insertar();
            List<Object> antes = bat.filaCompleta(id);
            Stack s = stack(m, "T1");
            s.dispatcher.pollOnce(AHORA);
            s.esperarFin();
            assertThat(m.peticiones()).hasSize(1);
            assertThat(bat.filaCompleta(id)).isEqualTo(antes);
        }
    }

    @Test
    void conexionRechazada_noCambiaNada_yElProgramaSeVuelveASeleccionar() throws Exception {
        int cerrado;
        try (ServerSocket ss = new ServerSocket(0)) {
            cerrado = ss.getLocalPort();
        }
        long id = bat.programa("T1").insertar();
        List<Object> antes = bat.filaCompleta(id);
        Stack s = new Stack(bat.adapter, List.of("T1"));
        s.props.getDestinos().put("T1", "http://127.0.0.1:" + cerrado);
        s.arrancar();
        assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(1);
        s.esperarFin();
        assertThat(bat.filaCompleta(id)).isEqualTo(antes);
        assertThat(s.dispatcher.pollOnce(AHORA)).as("selected again at the next poll").isEqualTo(1);
        s.esperarFin();
        assertThat(bat.filaCompleta(id)).isEqualTo(antes);
    }

    @Test
    void timeoutDeLecturaNoSeTrataComoFallo() throws Exception {
        try (MockModulo m = new MockModulo().demora(1_500)) {
            long id = bat.programa("T1").insertar();
            List<Object> antes = bat.filaCompleta(id);
            Stack s = stack(m, "T1");
            s.props.setReadTimeoutMs(300);
            s.arrancar();
            s.dispatcher.pollOnce(AHORA);
            s.esperarFin();
            assertThat(bat.filaCompleta(id)).isEqualTo(antes);
        }
    }

    @Test
    void guardaEnVuelo_elMismoProgramaNoSeEnviaDosVecesAlMismoTiempo() throws Exception {
        CountDownLatch compuerta = new CountDownLatch(1);
        try (MockModulo m = new MockModulo().retiene(compuerta)) {
            bat.programa("T1").insertar();
            Stack s = stack(m, "T1");
            assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(1);
            assertThat(s.dispatcher.pollOnce(AHORA)).as("still in flight").isEqualTo(0);
            assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(0);
            compuerta.countDown();
            s.esperarFin();
            assertThat(m.peticiones()).hasSize(1);
            assertThat(m.maxSimultaneas()).isEqualTo(1);
            assertThat(s.dispatcher.pollOnce(AHORA)).as("free again, still PEN here").isEqualTo(1);
            s.esperarFin();
        }
    }

    @Test
    void ejecutorAcotado_unJobLentoNoBloqueaElCiclo_yElExcesoQuedaPendiente() throws Exception {
        CountDownLatch compuerta = new CountDownLatch(1);
        try (MockModulo m = new MockModulo().retiene(compuerta)) {
            for (int i = 0; i < 5; i++) {
                bat.programa("T1").insertar();
            }
            Stack s = new Stack(bat.adapter, List.of("T1"));
            s.props.getDestinos().put("T1", m.url());
            s.props.setConcurrentJobs(1);
            s.props.setQueueCapacity(2);
            s.arrancar();
            long t0 = System.currentTimeMillis();
            int encolados = s.dispatcher.pollOnce(AHORA);
            assertThat(System.currentTimeMillis() - t0).as("poll returns while jobs are slow").isLessThan(2_000);
            assertThat(encolados).isEqualTo(3); // 1 running + 2 queued; 2 not accepted, stay PEN
            compuerta.countDown();
            s.esperarFin();
            assertThat(m.peticiones()).hasSize(3);
        }
    }

    @Test
    void demonioNoSeDespacha() throws Exception {
        try (MockModulo m = new MockModulo()) {
            bat.agenda(7, "T1", "S");
            long id = bat.programa("T1").agenda(7).insertar();
            List<Object> antes = bat.filaCompleta(id);
            Stack s = stack(m, "T1");
            assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(0);
            assertThat(m.peticiones()).isEmpty();
            assertThat(bat.filaCompleta(id)).isEqualTo(antes);
            assertThat(s.despachador.despachar(bat.adapter.readEligiblePrograms("", List.of("T1"), AHORA).get(0)))
                    .isEqualTo(Aceptacion.DEMONIO_OMITIDO);
        }
    }

    @Test
    void sinDestinoNoSeEnvia() throws Exception {
        bat.programa("T1").insertar();
        Stack s = new Stack(bat.adapter, List.of("T1")).arrancar();
        assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(0);
    }

    @Test
    void sinReintentoAutomatico_unErrConIntentosRestantesSigueEnErr() throws Exception {
        bat.tipo("T3", "cl.bcs.x.Job3", 3);
        try (MockModulo m = new MockModulo()) {
            long id = bat.programa("T3").estado("ERR").bloqueo("S").intento(1).insertar();
            List<Object> antes = bat.filaCompleta(id);
            Stack s = stack(m, "T1", "T3");
            for (int i = 0; i < 3; i++) {
                s.dispatcher.pollOnce(AHORA);
            }
            s.esperarFin();
            assertThat(m.peticiones()).isEmpty();
            assertThat(bat.filaCompleta(id)).isEqualTo(antes);
        }
    }
}
