package cl.bcs.tesoreria.adapter.in;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.JavaCallDispatcher;

/** The internal endpoint: 400 for malformed requests, 503 for what is not migrated, a generic 500 that does not leak the cause. */
class InternalJavaCallControllerTest {

    private static final String CLASE = "cl.bcs.tesoreria.CuadraturaCierreDiario";

    private JavaCallDispatcher dispatcher;
    private MockMvc mvc;

    @BeforeEach
    void montaElControlador() {
        dispatcher = mock(JavaCallDispatcher.class);
        mvc = MockMvcBuilders.standaloneSetup(new InternalJavaCallController(dispatcher)).build();
    }

    private org.springframework.test.web.servlet.ResultActions llamar(String cuerpo) throws Exception {
        return mvc.perform(post("/internal/java-call").contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    void unaClaseMigradaSeDespachaYDevuelveElJsonDelServicio() throws Exception {
        when(dispatcher.maneja(CLASE)).thenReturn(true);
        JsonObject salida = JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\":0}").getAsJsonObject();
        when(dispatcher.despachar(any(String.class), any(JsonObject.class))).thenReturn(salida);

        llamar("{\"class\":\"" + CLASE + "\",\"params\":{\"p_IdEmpresa\":1}}").andExpect(status().isOk())
                .andExpect(content().json("{\"P_OUT_ERRORESCONTADOR\":0}"));
    }

    @Test
    void lasPeticionesMalFormadasSon400() throws Exception {
        llamar("no es json").andExpect(status().isBadRequest());
        llamar("[1,2]").andExpect(status().isBadRequest());
        llamar("{}").andExpect(status().isBadRequest());
        llamar("{\"class\":\"\"}").andExpect(status().isBadRequest());
        llamar("{\"class\":123}").andExpect(status().isBadRequest());
        llamar("{\"class\":\"" + CLASE + "\",\"params\":[1]}").andExpect(status().isBadRequest());
        llamar("{\"class\":\"" + CLASE + "\",\"params\":\"x\"}").andExpect(status().isBadRequest());
    }

    @Test
    void unaClaseNoMigradaOUnServicioNoDisponibleSon503ParaQueElGatewayVayaATomEE() throws Exception {
        llamar("{\"class\":\"cl.bcs.tesoreria.Inexistente\"}").andExpect(status().isServiceUnavailable());
        when(dispatcher.maneja(CLASE)).thenReturn(true);
        when(dispatcher.despachar(any(String.class), any(JsonObject.class))).thenThrow(new UnsupportedOperationException("sin cliente"));
        llamar("{\"class\":\"" + CLASE + "\"}").andExpect(status().isServiceUnavailable());
    }

    @Test
    void unErrorInesperadoEs500ConMensajeGenericoSinLaCausa() throws Exception {
        when(dispatcher.maneja(CLASE)).thenReturn(true);
        when(dispatcher.despachar(any(String.class), any(JsonObject.class))).thenThrow(new IllegalStateException("ORA-1234 tabla BSCL.Secreta"));

        var respuesta = llamar("{\"class\":\"" + CLASE + "\"}").andExpect(status().isInternalServerError()).andReturn().getResponse();
        org.assertj.core.api.Assertions.assertThat(respuesta.getErrorMessage()).contains("correlationId").doesNotContain("ORA-1234").doesNotContain("BSCL.Secreta");
        org.assertj.core.api.Assertions.assertThat(respuesta.getContentAsString()).doesNotContain("ORA-1234");
    }
}
