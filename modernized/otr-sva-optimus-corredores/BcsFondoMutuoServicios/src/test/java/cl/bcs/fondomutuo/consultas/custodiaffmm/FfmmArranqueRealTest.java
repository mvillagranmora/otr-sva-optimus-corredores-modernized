package cl.bcs.fondomutuo.consultas.custodiaffmm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import cl.bcs.fondomutuo.BcsFondoMutuoApplication;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaCerrada;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaPort;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioPort;

/**
 * REAL start-up (H2, profile test) of the module with CPF named in {@code fondomutuo.solicitud.listas-servidas}: once the adapter replaces {@code ConsultarCustodiaPortafolioCustodiaCerrada}
 * (rule 4 of {@code API_ASSUMED.md}) the context starts and the bean of the custody port is the REAL adapter; the M1 guard of {@code ListasLecturaArranque} still stops the start-up when the operator closes
 * the in-process custody calculation with {@code fondomutuo.custodia.ffmm.cerrada=true} while CPF is open. This replaces, for the new wiring, what
 * {@code ListasLecturaArranqueRealTest.cpfAbiertaConLaCustodiaCerrada_elArranqueFalla_M1} pins for the old one (that test must add the switch: see API_ASSUMED.md 4.4).
 */
@SpringBootTest(properties = "fondomutuo.solicitud.listas-servidas=NORMAL,CONSULTAR_CUSTODIA_PORTAFOLIO")
@ActiveProfiles("test")
class FfmmArranqueRealTest {

    @Autowired ApplicationContext contexto;
    @Autowired ConsultarCustodiaPortafolioCustodiaPort custodia;

    @Test
    void cpfAbiertaConElAdaptadorReal_arranca() {
        assertThat(custodia).isInstanceOf(ConsultarCustodiaPortafolioFfmmAdaptador.class);
        assertThat(contexto.getBeansOfType(ConsultarCustodiaPortafolioCustodiaPort.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(ConsultarCustodiaPortafolioCustodiaCerrada.class)).isEmpty();
        assertThat(contexto.getBeansOfType(ConsultarCustodiaPortafolioFfmmPort.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(ConsultarCustodiaPortafolioPort.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(ConsultarCustodiaPortafolioFfmmJdbc.class)).hasSize(1);
    }

    private static ConfigurableApplicationContext arrancar(String... args) {
        return new SpringApplicationBuilder(BcsFondoMutuoApplication.class).web(WebApplicationType.NONE).profiles("test").properties("spring.main.banner-mode=off").run(args);   // command line: the yaml of the profile must not win
    }

    @Test
    void cpfAbiertaConElInterruptorCerrado_elArranqueFalla_M1() {
        assertThatThrownBy(() -> arrancar("--fondomutuo.solicitud.listas-servidas=NORMAL,CONSULTAR_CUSTODIA_PORTAFOLIO", "--" + ConsultarCustodiaPortafolioFfmmConfiguracion.PROPIEDAD_CERRADA + "=true").close())
                .hasRootCauseInstanceOf(IllegalStateException.class).rootCause().hasMessageContaining("CONSULTAR_CUSTODIA_PORTAFOLIO").hasMessageContaining("custody");
    }

    @Test
    void elInterruptorCerradoConCpfCerrada_arrancaConLaCustodiaCerrada() {
        try (ConfigurableApplicationContext c = arrancar("--fondomutuo.solicitud.listas-servidas=NORMAL", "--" + ConsultarCustodiaPortafolioFfmmConfiguracion.PROPIEDAD_CERRADA + "=true")) {
            assertThat(c.getBeansOfType(ConsultarCustodiaPortafolioCustodiaPort.class)).hasSize(1);
            assertThat(c.getBean(ConsultarCustodiaPortafolioCustodiaPort.class)).isInstanceOf(ConsultarCustodiaPortafolioCustodiaCerrada.class);
        }
    }
}
