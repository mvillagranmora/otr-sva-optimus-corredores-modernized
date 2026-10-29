package cl.bcs.fondomutuo.custodia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import cl.bcs.fondomutuo.BcsFondoMutuoApplication;
import cl.bcs.fondomutuo.adapter.out.http.ColaboradoresHttp;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;

/**
 * Review of 2026-10-28, H3 (user decision: the custody switch is SEPARATED): the REAL application context (H2, profile test) under every combination of
 * {@code fondomutuo.custodia.habilitada}, {@code normal-habilitada} and {@code comisiones-en-proceso}. With the three at their default nothing changes; with only
 * {@code habilitada=true} just RescateReinversion is served in process (Normal keeps answering 503 G-CUS and {@code CalcularComisiones} keeps the HTTP port);
 * the invalid combinations stop the start-up.
 */
class CustodiaBanderasContextoTest {

    private static ConfigurableApplicationContext arrancar(boolean habilitada, Boolean normal, Boolean comisiones) {
        java.util.List<String> args = new java.util.ArrayList<>();
        args.add("--fondomutuo.custodia.habilitada=" + habilitada);
        if (normal != null) args.add("--fondomutuo.custodia.normal-habilitada=" + normal);
        if (comisiones != null) args.add("--fondomutuo.custodia.comisiones-en-proceso=" + comisiones);
        return new SpringApplicationBuilder(BcsFondoMutuoApplication.class).web(WebApplicationType.NONE).profiles("test").properties("spring.main.banner-mode=off")
                .run(args.toArray(new String[0]));
    }

    private static CustodiaPort custodiaDeComisiones(ConfigurableApplicationContext c) {
        return (CustodiaPort) ReflectionTestUtils.getField(c.getBean(CalcularComisiones.class), "custodia");
    }

    private static void normalCerrada(Compuertas g) {
        assertThat(g.custodiaNormalHabilitada()).isFalse();
        assertThatThrownBy(g::gCustodiaNormal).isInstanceOf(ServicioNoServidoException.class).satisfies(x -> assertThat(((ServicioNoServidoException) x).compuerta()).isEqualTo("G-CUS"));
    }

    @Test
    @DisplayName("default (the three false / absent): everything over HTTP, Normal 503 G-CUS, the in-process adapter is not registered")
    void porDefecto() {
        for (Boolean explicito : new Boolean[] { null, Boolean.FALSE }) {
            try (ConfigurableApplicationContext c = arrancar(false, explicito, explicito)) {
                Compuertas g = c.getBean(Compuertas.class);
                assertThat(g.custodiaHabilitada()).isFalse();
                normalCerrada(g);
                assertThat(g.comisionesEnProceso()).isFalse();
                assertThat(c.getBean(CustodiaPort.class)).isInstanceOf(ColaboradoresHttp.class);
                assertThat(c.getBeansOfType(FlujoCustodiaEnProceso.class)).isEmpty();
                assertThat(custodiaDeComisiones(c)).isInstanceOf(ColaboradoresHttp.class);
            }
        }
    }

    @Test
    @DisplayName("only habilitada=true: RescateReinversion in process (the @Primary adapter); Normal still 503 G-CUS; CalcularComisiones still on the HTTP port")
    void soloHabilitada() {
        try (ConfigurableApplicationContext c = arrancar(true, null, null)) {
            Compuertas g = c.getBean(Compuertas.class);
            assertThat(g.custodiaHabilitada()).isTrue();
            assertThat(c.getBean(CustodiaPort.class)).isInstanceOf(FlujoCustodiaEnProceso.class);
            assertThatCode(g::gCustodia).doesNotThrowAnyException();
            normalCerrada(g);
            assertThat(g.comisionesEnProceso()).isFalse();
            assertThat(custodiaDeComisiones(c)).as("the commissions keep HTTP even with the in-process @Primary").isInstanceOf(ColaboradoresHttp.class);
        }
    }

    @Test
    @DisplayName("habilitada + normal-habilitada: Normal passes the gate; the commissions stay on HTTP")
    void habilitadaYNormal() {
        try (ConfigurableApplicationContext c = arrancar(true, true, null)) {
            Compuertas g = c.getBean(Compuertas.class);
            assertThat(g.custodiaNormalHabilitada()).isTrue();
            assertThatCode(g::gCustodiaNormal).doesNotThrowAnyException();
            assertThat(custodiaDeComisiones(c)).isInstanceOf(ColaboradoresHttp.class);
        }
    }

    @Test
    @DisplayName("habilitada + comisiones-en-proceso: CalcularComisiones uses the in-process adapter; Normal still 503 G-CUS")
    void habilitadaYComisiones() {
        try (ConfigurableApplicationContext c = arrancar(true, null, true)) {
            Compuertas g = c.getBean(Compuertas.class);
            assertThat(g.comisionesEnProceso()).isTrue();
            assertThat(custodiaDeComisiones(c)).isInstanceOf(FlujoCustodiaEnProceso.class);
            normalCerrada(g);
        }
    }

    @Test
    @DisplayName("the three open: everything in process")
    void lasTres() {
        try (ConfigurableApplicationContext c = arrancar(true, true, true)) {
            Compuertas g = c.getBean(Compuertas.class);
            assertThatCode(g::gCustodiaNormal).doesNotThrowAnyException();
            assertThat(custodiaDeComisiones(c)).isInstanceOf(FlujoCustodiaEnProceso.class);
        }
    }

    @Test
    @DisplayName("invalid: normal-habilitada=true or comisiones-en-proceso=true WITHOUT habilitada=true stop the start-up")
    void combinacionesInvalidas() {
        assertThatThrownBy(() -> arrancar(false, true, null).close()).hasRootCauseInstanceOf(IllegalStateException.class).rootCause()
                .hasMessageContaining("normal-habilitada").hasMessageContaining("habilitada is false");
        assertThatThrownBy(() -> arrancar(false, null, true).close()).hasRootCauseInstanceOf(IllegalStateException.class).rootCause()
                .hasMessageContaining("comisiones-en-proceso").hasMessageContaining("habilitada is false");
        assertThatThrownBy(() -> arrancar(false, true, true).close()).hasRootCauseInstanceOf(IllegalStateException.class);
    }
}
