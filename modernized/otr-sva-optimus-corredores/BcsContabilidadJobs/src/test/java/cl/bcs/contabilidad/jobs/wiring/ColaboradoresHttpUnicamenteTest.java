package cl.bcs.contabilidad.jobs.wiring;

import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.plataforma.TomEePlataformaClient;
import cl.bcs.contabilidad.jobs.adapter.DespachadorEnProceso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step 5 switch off ({@code contabilidad.jobs.colaboradores-en-proceso=false}, or absent): exactly the wiring before step 5, so the clearing keeps
 * going over HTTP: {@code TomEePlataformaClient} is the only {@code PlataformaPort} and nothing of Tesoreria is a bean.
 */
@SpringBootTest(properties = {"contabilidad.jobs.sesion-hana=false", "contabilidad.jobs.colaboradores-en-proceso=false"})
@ActiveProfiles("test")
class ColaboradoresHttpUnicamenteTest {

    @Autowired
    ApplicationContext contexto;

    @Autowired
    PlataformaPort plataforma;

    @Test
    @DisplayName("S5-H01: switch off -> TomEePlataformaClient is the one PlataformaPort, no dispatcher, no Tesoreria bean")
    void sinElInterruptorElCableadoEsElDeAntes() {
        assertThat(plataforma).isInstanceOf(TomEePlataformaClient.class);
        assertThat(contexto.getBeansOfType(PlataformaPort.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(DespachadorEnProceso.class)).isEmpty();
        for (String nombre : contexto.getBeanDefinitionNames()) {
            assertThat(nombre).doesNotStartWith("cl.bcs.tesoreria.");
        }
    }
}
