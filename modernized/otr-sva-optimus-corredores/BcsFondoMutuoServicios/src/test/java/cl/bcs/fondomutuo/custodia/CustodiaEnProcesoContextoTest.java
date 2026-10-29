package cl.bcs.fondomutuo.custodia;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo;
import cl.bcs.fondomutuo.adapter.out.http.ColaboradoresHttp;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionPort;

/**
 * The REAL application context with {@code fondomutuo.custodia.habilitada=true} (H2, profile test): the in-process adapter is the effective
 * {@link CustodiaPort}, the HTTP adapter stays registered as fall-back, RescateReinversion is wired on the effective port, the guard let the start-up
 * through, and of {@code cl.bcs.custodia} exactly one class is a bean (no component scan of the library). The closed default is covered by
 * {@link CustodiaEnProcesoArranqueTest} and by the existing contexts of the module (they run with the default and see only the HTTP adapter).
 */
@SpringBootTest(properties = "fondomutuo.custodia.habilitada=true")
@ActiveProfiles("test")
class CustodiaEnProcesoContextoTest {

    @Autowired ApplicationContext ctx;

    @Test
    void elAdaptadorEnProcesoEsElPuertoEfectivo_yLaCompuertaEstaAbierta() {
        assertThat(ctx.getBean(Compuertas.class).custodiaHabilitada()).isTrue();
        assertThat(ctx.getBean(CustodiaPort.class)).isInstanceOf(FlujoCustodiaEnProceso.class);
        assertThat(ctx.getBeansOfType(CustodiaPort.class).values()).anyMatch(p -> p instanceof ColaboradoresHttp);
        assertThat(ctx.getBean(RescateReinversionPort.class)).isNotNull();
        assertThat(ctx.getBean(CustodiaEnProcesoArranque.class)).isNotNull();
    }

    @Test
    void deCustodiaSoloEstaLaClaseDelFlujo_sinEscaneo() {
        Set<String> deCustodia = new TreeSet<>();
        for (String n : ctx.getBeanDefinitionNames()) {
            Class<?> t = ctx.getType(n);
            if (t == null) continue;
            String clase = org.springframework.util.ClassUtils.getUserClass(t).getName();
            if (clase.startsWith("cl.bcs.custodia.")) deCustodia.add(clase);
        }
        assertThat(deCustodia).containsExactly(ConsultarFlujoCustodiaFlujo.class.getName());
    }
}
