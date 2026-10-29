package cl.bcs.fondomutuo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.manejador.ManejadorConfiguracion;

/**
 * Architecture review F3 W1 (HIGH, H1): REAL start-up (H2, profile test) with the reads named in {@code fondomutuo.solicitud.listas-servidas}. Before the fix the first
 * name outside the eight lists of the Manejador stopped the context, so the reads could not be opened by configuration.
 */
@SpringBootTest(properties = "fondomutuo.solicitud.listas-servidas=NORMAL,CONSULTAR_FECHA_SOLICITUD,CONSULTAR_VALOR_DOLAR,CONSULTAR_FONDO_REINVERSION,CONSULTAR_INSTRUMENTOS,"
        + "CONSULTAR_SOLICITUDES,CONSULTAR_SOLICITUDES_WS,CONSULTAR_DETALLE_SOLICITUD")
@ActiveProfiles("test")
class ListasLecturaArranqueRealTest {

    @Autowired Compuertas compuertas;

    @Test
    void lasSieteLecturasAbribles_arrancanYQuedanEnLasCompuertas() {
        assertThat(compuertas.listasServidas()).contains("NORMAL", "CONSULTAR_FECHA_SOLICITUD", "CONSULTAR_VALOR_DOLAR", "CONSULTAR_FONDO_REINVERSION", "CONSULTAR_INSTRUMENTOS",
                "CONSULTAR_SOLICITUDES", "CONSULTAR_SOLICITUDES_WS", "CONSULTAR_DETALLE_SOLICITUD");
        assertThat(ManejadorConfiguracion.LISTAS_LECTURA).containsAll(compuertas.listasServidas().stream().filter(n -> !n.equals("NORMAL")).toList());
    }

    private static ConfigurableApplicationContext arrancar(String listas, String... extra) {
        return new SpringApplicationBuilder(BcsFondoMutuoApplication.class).web(WebApplicationType.NONE).profiles("test")
                .properties("spring.main.banner-mode=off").run(java.util.stream.Stream.concat(java.util.stream.Stream.of("--fondomutuo.solicitud.listas-servidas=" + listas), java.util.Arrays.stream(extra)).toArray(String[]::new));   // command line: the yaml of the profile must not win
    }

    @Test
    void cpfAbiertaConLaCustodiaCerrada_elArranqueFalla_M1() {
        assertThatThrownBy(() -> arrancar("NORMAL,CONSULTAR_CUSTODIA_PORTAFOLIO", "--fondomutuo.custodia.ffmm.cerrada=true").close()).hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause().hasMessageContaining("CONSULTAR_CUSTODIA_PORTAFOLIO").hasMessageContaining("custody");
    }

    @Test
    void unTypoDeUnaLectura_elArranqueFalla() {
        assertThatThrownBy(() -> arrancar("NORMAL,CONSULTAR_FECHA_SOLICITUDD").close()).hasStackTraceContaining("names an unknown list: CONSULTAR_FECHA_SOLICITUDD");
    }
}
