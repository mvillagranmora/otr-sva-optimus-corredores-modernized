package cl.bcs.fondomutuo.custodia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * F3 custody in process: selection of the effective {@link CustodiaPort} and the start-up guard {@link CustodiaEnProcesoArranque}. G-CUS may be open only with
 * the in-process adapter; the HTTP adapter (fall-back) must keep failing closed behind the gate. Context tests use a runner with the real {@code Compuertas},
 * the real configuration and the real guard (only the infrastructure beans and an HTTP-like port are stand-ins); one test starts the whole application context.
 */
class CustodiaEnProcesoArranqueTest {

    static final CustodiaPort HTTP = entrada -> {
        throw new AssertionError("the HTTP adapter must not be called by these tests");
    };

    private ApplicationContextRunner base() {
        return new ApplicationContextRunner()
                .withBean(Compuertas.class)
                .withBean(CustodiaEnProcesoArranque.class)
                .withBean("colaboradoresHttp", CustodiaPort.class, () -> HTTP)
                .withBean(ConexionUnidadDeTrabajo.class, () -> new ConexionUnidadDeTrabajo(null))
                .withBean(SecuenciaPort.class, () -> (nombre, cantidad) -> java.util.List.of());
    }

    private static Set<String> beansDeCustodia(ApplicationContext ctx) {
        Set<String> r = new TreeSet<>();
        for (String n : ctx.getBeanDefinitionNames()) {
            Class<?> t = ctx.getType(n);
            if (t != null && t.getName().startsWith("cl.bcs.custodia.")) r.add(t.getName());
        }
        return r;
    }

    @Test
    @DisplayName("G-CUS open and the effective port is the HTTP adapter: the start-up FAILS with IllegalStateException (no atomicity)")
    void abiertaConHttpFalla() {
        base().withPropertyValues("fondomutuo.custodia.habilitada=true").run(ctx -> {
            assertThat(ctx).hasFailed();
            assertThat(rootCause(ctx.getStartupFailure())).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("fondomutuo.custodia.habilitada is true").hasMessageContaining("FlujoCustodiaEnProceso").hasMessageContaining("atomicity");
        });
    }

    @Test
    @DisplayName("G-CUS open and the effective port is a double / another adapter: fails as well, even with the in-process adapter registered but not effective")
    void abiertaConOtroPuertoPrimarioFalla() {
        base().withUserConfiguration(FlujoCustodiaEnProcesoConfiguracion.class)
                .withBean("otro", CustodiaPort.class, () -> e -> new JsonObject(), bd -> bd.setPrimary(true))
                .withPropertyValues("fondomutuo.custodia.habilitada=true").run(ctx -> {
                    // two primaries: Spring refuses the ambiguity itself; either way the application does not start
                    assertThat(ctx).hasFailed();
                });
    }

    @Test
    @DisplayName("the gate reads 'yes' as open (Spring boolean conversion) but the adapter is only registered for exactly 'true': the guard stops that mismatch")
    void abiertaConValorQueNoEsTrueExactoFalla() {
        base().withUserConfiguration(FlujoCustodiaEnProcesoConfiguracion.class).withPropertyValues("fondomutuo.custodia.habilitada=yes").run(ctx -> {
            assertThat(ctx).hasFailed();
            assertThat(rootCause(ctx.getStartupFailure())).isInstanceOf(IllegalStateException.class).hasMessageContaining("not the in-process adapter");
        });
    }

    @Test
    @DisplayName("G-CUS open and the in-process adapter registered: starts; the adapter is the effective port, the HTTP one stays as fall-back; only the library class is registered")
    void abiertaConEnProcesoArranca() {
        base().withUserConfiguration(FlujoCustodiaEnProcesoConfiguracion.class).withPropertyValues("fondomutuo.custodia.habilitada=true").run(ctx -> {
            assertThat(ctx).hasNotFailed();
            assertThat(ctx.getBean(CustodiaPort.class)).isInstanceOf(FlujoCustodiaEnProceso.class);
            assertThat(ctx.getBeansOfType(CustodiaPort.class)).as("the HTTP adapter stays registered as fall-back").hasSize(2).containsValue(HTTP);
            assertThat(beansDeCustodia(ctx)).as("no scan of cl.bcs.custodia: only the flow class").containsExactly(ConsultarFlujoCustodiaFlujo.class.getName());
            assertThat(ctx.getBean(Compuertas.class).custodiaHabilitada()).isTrue();
        });
    }

    @Test
    @DisplayName("default (property missing or false): starts; the HTTP adapter is the only port, nothing of Custodia is registered, the gate stays closed")
    void cerradaPorDefectoNoRegistraNada() {
        for (String[] props : new String[][] { {}, { "fondomutuo.custodia.habilitada=false" } }) {
            base().withUserConfiguration(FlujoCustodiaEnProcesoConfiguracion.class).withPropertyValues(props).run(ctx -> {
                assertThat(ctx).hasNotFailed();
                assertThat(ctx.getBean(CustodiaPort.class)).isSameAs(HTTP);
                assertThat(ctx.getBeansOfType(CustodiaPort.class)).hasSize(1);
                assertThat(beansDeCustodia(ctx)).isEmpty();
                assertThat(ctx.getBean(Compuertas.class).custodiaHabilitada()).isFalse();
            });
        }
    }

    @Test
    @DisplayName("the check itself: closed gate never fails; open gate fails for the HTTP port, a double and an absent port; passes for the in-process adapter")
    void laRegla() {
        FlujoCustodiaEnProceso enProceso = new FlujoCustodiaEnProceso(new ConexionUnidadDeTrabajo(null), null);
        assertThatCode(() -> CustodiaEnProcesoArranque.iniciar(false, HTTP)).doesNotThrowAnyException();
        assertThatCode(() -> CustodiaEnProcesoArranque.iniciar(false, null)).doesNotThrowAnyException();
        assertThatCode(() -> CustodiaEnProcesoArranque.iniciar(true, enProceso)).doesNotThrowAnyException();
        assertThatThrownBy(() -> CustodiaEnProcesoArranque.iniciar(true, HTTP)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> CustodiaEnProcesoArranque.iniciar(true, e -> new JsonObject())).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> CustodiaEnProcesoArranque.iniciar(true, null)).isInstanceOf(IllegalStateException.class).hasMessageContaining("absent");
    }

    private static Throwable rootCause(Throwable t) {
        while (t.getCause() != null && t.getCause() != t) t = t.getCause();
        return t;
    }
}
