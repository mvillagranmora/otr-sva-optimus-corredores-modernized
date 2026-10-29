package cl.bcs.fondomutuo.manejador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallUnitOfWorkFilter;
import cl.bcs.fondomutuo.adapter.in.javacall.TokenInternoFilter;
import cl.bcs.fondomutuo.kit.Compuertas;

/**
 * FM-MAN-19..: the REAL Spring context (H2, profile test, {@code fondomutuo.solicitud.listas-servidas=NORMAL}) serves the Manejador through the real
 * filters: empty request = 200 with the counter (FM-MAN-12), a list other than Normal = 503 before anything happens (FM-MAN-11, DEV-F4), and the
 * Manejador handler is the one of the allow-list.
 */
@SpringBootTest
@ActiveProfiles("test")
class ManejadorCableadoRealTest {

    static final String TOKEN = "test-secret";

    @Autowired WebApplicationContext wac;
    @Autowired ApplicationContext ctx;
    @Autowired JavaCallDispatcher dispatcher;
    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).addFilters(ctx.getBean(TokenInternoFilter.class), ctx.getBean(cl.bcs.fondomutuo.adapter.in.javacall.JavaCallLimitesFilter.class), ctx.getBean(JavaCallUnitOfWorkFilter.class)).build();
    }

    private String llamar(String params, int estado) throws Exception {
        return mvc.perform(post("/internal/java-call").header("X-Internal-Token", TOKEN).contentType("application/json")
                .content("{\"class\":\"" + JavaCallDispatcher.MANEJADOR + "\",\"params\":" + params + "}")).andExpect(status().is(estado)).andReturn()
                .getResponse().getContentAsString();
    }

    @Test
    void fmMan19_elManejadorEstaRegistradoEnLaListaBlanca_conUnSoloHandler() {
        assertThat(dispatcher.buscar(JavaCallDispatcher.MANEJADOR)).get().isInstanceOf(ManejadorJavaCallHandler.class);
        assertThat(ctx.getBeansOfType(ManejadorSolicitudAporteRescateService.class)).hasSize(1);
    }

    @Test
    void fmMan20_peticionVacia_200ConElContador_yElJsonDelLegacy() throws Exception {
        assertThat(llamar("{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"FMM\"}", 200))
                .isEqualTo("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_RESULTADO\":[]}");
    }

    @Test
    void fmMan21_g0_otraListaConFilas_503_aunConNormalPresente_yLaConfiguracionEsSoloNormal() throws Exception {
        assertThat(ctx.getBean(Compuertas.class).listasServidas()).containsExactly("NORMAL");
        for (String lista : new String[] {"p_SolicitudesAporteRescateProgramado", "p_SolicitudAporteOvernight", "p_SolicitudAporteReinversion",
                "p_SolicitudCambioSerieRescate", "p_SolicitudCambioSerieAporte", "p_SolicitudRescateReinversionRescate", "p_SolicitudRescateReinversionAporte",
                "p_SolicitudRescateTransferencia", "p_SolicitudRescateTraspaso"}) {
            llamar("{\"p_FormaPagoSpot\":[],\"" + lista + "\":[{\"IdInterno\":1}]}", 503);
            llamar("{\"p_FormaPagoSpot\":[],\"p_SolicitudesAporteRescateNormal\":[{\"IdInterno\":1}],\"" + lista + "\":[{\"IdInterno\":1}]}", 503);
        }
    }

    @Test
    void fmMan22_jsonMalFormado_unTipoIncorrecto_esUn500GenericoSinElTextoDeLaExcepcion() throws Exception {
        String cuerpo = llamar("{\"p_IdEmpresa\":\"x\"}", 500);
        assertThat(cuerpo).doesNotContain("NumberFormat").doesNotContain("p_IdEmpresa");
    }
}
