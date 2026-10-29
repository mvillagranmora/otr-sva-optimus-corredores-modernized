package cl.bcs.jobmanager;

import cl.bcs.jobmanager.config.JobManagerProperties;
import cl.bcs.jobmanager.service.EnfriamientoNoEntregable;
import cl.bcs.jobmanager.service.EnfriamientoNoEntregable.Consulta;
import cl.bcs.jobmanager.service.ProgramaDespachador.Aceptacion;
import cl.bcs.jobmanager.support.BatH2;
import cl.bcs.jobmanager.support.MockModulo;
import cl.bcs.jobmanager.support.Stack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.net.ServerSocket;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cl.bcs.jobmanager.support.BatH2.AHORA;
import static org.assertj.core.api.Assertions.assertThat;

/** Cooldown of programs rejected with 404/422/503: unit (injectable clock) and end to end on H2 + mock module. */
class EnfriamientoTest {

    /** Manually advanced clock. */
    static final class Reloj extends Clock {
        volatile Instant ahora = Instant.parse("2026-01-01T00:00:00Z");
        void avanzar(Duration d) { ahora = ahora.plus(d); }
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId z) { return this; }
        @Override public Instant instant() { return ahora; }
    }

    private BatH2 bat;
    private final Reloj reloj = new Reloj();

    @BeforeEach
    void init() {
        bat = new BatH2();
        bat.tipo("T1", "cl.bcs.tesoreria.jobs.UnJob", 1);
    }

    private Stack stack(MockModulo m, Duration cooldown) {
        Stack s = new Stack(bat.adapter, List.of("T1"));
        s.props.getDestinos().put("T1", m.url());
        if (cooldown != null) {
            s.props.setCooldownNoEntregable(cooldown);
        }
        s.reloj = reloj;
        return s.arrancar();
    }

    @Test
    void propiedad_valorPorDefectoYOverride() {
        assertThat(new JobManagerProperties().getCooldownNoEntregable()).isEqualTo(Duration.ofSeconds(60));
        Binder b = new Binder(new MapConfigurationPropertySource(Map.of("jobmanager.cooldown-no-entregable", "PT5S")));
        JobManagerProperties p = b.bind("jobmanager", JobManagerProperties.class).get();
        assertThat(p.getCooldownNoEntregable()).isEqualTo(Duration.ofSeconds(5));
        Binder vacio = new Binder(new MapConfigurationPropertySource(Map.of("jobmanager.mode", "PROGRAMA")));
        assertThat(vacio.bind("jobmanager", JobManagerProperties.class).get().getCooldownNoEntregable())
                .isEqualTo(Duration.ofSeconds(60));
    }

    @ParameterizedTest
    @ValueSource(ints = {404, 422, 503})
    void conCooldown_noSeReenviaHastaQueExpira(int status) throws Exception {
        try (MockModulo m = new MockModulo().responde(status)) {
            long id = bat.programa("T1").insertar();
            Stack s = stack(m, Duration.ofSeconds(60));
            assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(1);
            s.esperarFin();
            assertThat(s.despachador.enfriamientoTotal()).isEqualTo(1);
            assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(0);
            assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(0);
            assertThat(m.peticiones()).hasSize(1);
            reloj.avanzar(Duration.ofSeconds(59));
            assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(0);
            reloj.avanzar(Duration.ofSeconds(2));
            assertThat(s.dispatcher.pollOnce(AHORA)).as("expired: posted again").isEqualTo(1);
            s.esperarFin();
            assertThat(m.peticiones()).hasSize(2);
            assertThat(id).isPositive();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {404, 422, 503})
    void sinCooldown_seReenviaEnCadaPoll(int status) throws Exception {
        try (MockModulo m = new MockModulo().responde(status)) {
            bat.programa("T1").insertar();
            Stack s = stack(m, Duration.ZERO);
            for (int i = 0; i < 3; i++) {
                assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(1);
                s.esperarFin();
            }
            assertThat(m.peticiones()).hasSize(3);
            assertThat(s.despachador.enfriamientoTotal()).isZero();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 409, 500, 401})
    void otrosResultados_nuncaEntranEnCooldown(int status) throws Exception {
        try (MockModulo m = new MockModulo().responde(status)) {
            bat.programa("T1").insertar();
            Stack s = stack(m, Duration.ofSeconds(60));
            for (int i = 0; i < 3; i++) {
                assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(1);
                s.esperarFin();
            }
            assertThat(m.peticiones()).hasSize(3);
            assertThat(s.despachador.enfriamientoTotal()).isZero();
        }
    }

    @Test
    void conexionRechazada_nuncaEnCooldown() throws Exception {
        int cerrado;
        try (ServerSocket ss = new ServerSocket(0)) {
            cerrado = ss.getLocalPort();
        }
        bat.programa("T1").insertar();
        Stack s = new Stack(bat.adapter, List.of("T1"));
        s.props.getDestinos().put("T1", "http://127.0.0.1:" + cerrado);
        s.reloj = reloj;
        s.arrancar();
        for (int i = 0; i < 3; i++) {
            assertThat(s.dispatcher.pollOnce(AHORA)).isEqualTo(1);
            s.esperarFin();
        }
        assertThat(s.despachador.enfriamientoTotal()).isZero();
    }

    @Test
    void enCooldown_despacharDevuelveEnEnfriamiento_yLaFilaQuedaIntacta() throws Exception {
        try (MockModulo m = new MockModulo().responde(404)) {
            long id = bat.programa("T1").insertar();
            List<Object> antes = bat.filaCompleta(id);
            Stack s = stack(m, null);
            s.dispatcher.pollOnce(AHORA);
            s.esperarFin();
            assertThat(s.despachador.despachar(bat.adapter.readEligiblePrograms("", List.of("T1"), AHORA).get(0)))
                    .isEqualTo(Aceptacion.EN_ENFRIAMIENTO);
            assertThat(bat.filaCompleta(id)).isEqualTo(antes);
        }
    }

    @Test
    void limpieza_elProgramaQueDejaDeSerElegibleSeOlvida() throws Exception {
        try (MockModulo m = new MockModulo().responde(422)) {
            long id = bat.programa("T1").insertar();
            Stack s = stack(m, null);
            s.dispatcher.pollOnce(AHORA);
            s.esperarFin();
            assertThat(s.despachador.enfriamientoTotal()).isEqualTo(1);
            s.despachador.depurarEnfriamiento(Set.of(id + 1000));
            assertThat(s.despachador.enfriamientoTotal()).isZero();
        }
    }

    @Test
    void unidad_expiraConRelojInyectado_primeraOmisionAvisaUnaVez() {
        EnfriamientoNoEntregable e = new EnfriamientoNoEntregable(Duration.ofSeconds(10), 10, reloj);
        assertThat(e.consultar(1)).isEqualTo(Consulta.LIBRE);
        e.registrar(1);
        assertThat(e.consultar(1)).isEqualTo(Consulta.EN_ENFRIAMIENTO_PRIMERA_OMISION);
        assertThat(e.consultar(1)).isEqualTo(Consulta.EN_ENFRIAMIENTO);
        reloj.avanzar(Duration.ofSeconds(10));
        assertThat(e.consultar(1)).isEqualTo(Consulta.LIBRE);
        assertThat(e.tamano()).isZero();
        e.registrar(1);
        assertThat(e.consultar(1)).as("a new cooldown warns again").isEqualTo(Consulta.EN_ENFRIAMIENTO_PRIMERA_OMISION);
    }

    @Test
    void unidad_tamanoAcotado_expulsaLaMasAntigua() {
        EnfriamientoNoEntregable e = new EnfriamientoNoEntregable(Duration.ofMinutes(5), 3, reloj);
        for (long id = 1; id <= 10; id++) {
            e.registrar(id);
            assertThat(e.tamano()).isLessThanOrEqualTo(3);
        }
        assertThat(e.tamano()).isEqualTo(3);
        assertThat(e.consultar(10)).isNotEqualTo(Consulta.LIBRE);
        assertThat(e.consultar(1)).isEqualTo(Consulta.LIBRE);
    }

    @Test
    void unidad_tamanoAcotado_primeroDescartaLosExpirados() {
        EnfriamientoNoEntregable e = new EnfriamientoNoEntregable(Duration.ofSeconds(10), 2, reloj);
        e.registrar(1);
        reloj.avanzar(Duration.ofSeconds(6));
        e.registrar(2);
        reloj.avanzar(Duration.ofSeconds(5)); // 1 expired, 2 alive
        e.registrar(3);
        assertThat(e.consultar(2)).isNotEqualTo(Consulta.LIBRE);
        assertThat(e.consultar(3)).isNotEqualTo(Consulta.LIBRE);
    }

    @Test
    void unidad_deshabilitadoNoRegistra() {
        EnfriamientoNoEntregable e = new EnfriamientoNoEntregable(Duration.ZERO, 3, reloj);
        e.registrar(1);
        assertThat(e.habilitado()).isFalse();
        assertThat(e.consultar(1)).isEqualTo(Consulta.LIBRE);
    }

    @Test
    void unidad_depurarConservaLosElegibles() {
        EnfriamientoNoEntregable e = new EnfriamientoNoEntregable(Duration.ofMinutes(1), 10, reloj);
        e.registrar(1);
        e.registrar(2);
        e.depurar(Set.of(2L));
        assertThat(e.tamano()).isEqualTo(1);
        assertThat(e.consultar(2)).isNotEqualTo(Consulta.LIBRE);
    }
}
