package cl.bcs.plataforma.internal;

import cl.bcs.plataforma.adapter.in.InternalJavaCallController;
import cl.bcs.plataforma.service.InternalJavaCallRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.ResourceAccessException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP surface of POST /internal/java-call: filter + controller, router mocked. */
class InternalJavaCallControllerTest {

    private static final String T = "secreto";
    private InternalJavaCallRouter router;
    private MockMvc mvc;

    @BeforeEach
    void montar() {
        router = Mockito.mock(InternalJavaCallRouter.class);
        mvc = MockMvcBuilders.standaloneSetup(new InternalJavaCallController(router, 200)).addFilters(new InternalTokenFilter(T)).build();
    }

    private org.springframework.test.web.servlet.ResultActions llamar(String body, String token) throws Exception {
        var rq = post("/internal/java-call").contentType("application/json").content(body);
        if (token != null) rq = rq.header("X-Internal-Token", token);
        return mvc.perform(rq);
    }

    @Test
    void sinToken_o_tokenMalo_401_yNoLlegaAlRouter() throws Exception {
        llamar("{\"class\":\"a.B\"}", null).andExpect(status().isUnauthorized());
        llamar("{\"class\":\"a.B\"}", "malo").andExpect(status().isUnauthorized());
        verify(router, never()).enrutar(any(), any());
    }

    @Test
    void respuestaDelDestinoPasaSinAlterar_statusYCuerpo() throws Exception {
        when(router.enrutar(any(), any())).thenReturn(new Respuesta(500, "application/json", "{\"error\":\"x\"}"));
        llamar("{\"class\":\"cl.bcs.tesoreria.A\",\"params\":{\"k\":1}}", T).andExpect(status().isInternalServerError()).andExpect(content().string("{\"error\":\"x\"}"));
        when(router.enrutar(any(), any())).thenReturn(new Respuesta(200, "application/json", "{\"ok\":true}"));
        llamar("{\"class\":\"cl.bcs.tesoreria.A\"}", T).andExpect(status().isOk()).andExpect(content().string("{\"ok\":true}"));
    }

    @Test
    void cuerposInvalidos_400() throws Exception {
        for (String b : new String[] { "not json", "[]", "{}", "{\"class\":\"\"}", "{\"class\":5}", "{\"class\":\"a.B\",\"params\":[1]}",
                "{\"class\":\"a.B\",\"skip\":\"x\"}", "{\"class\":\"a.B\",\"take\":-1}", "{\"class\":\"" + "a.".repeat(150) + "B\"}" }) {
            llamar(b, T).andExpect(status().isBadRequest());
        }
        verify(router, never()).enrutar(any(), any());
    }

    @Test
    void cuerpoExcedeElLimite_400() throws Exception {
        llamar("{\"class\":\"a.B\",\"params\":{\"x\":\"" + "z".repeat(300) + "\"}}", T).andExpect(status().isBadRequest());
        verify(router, never()).enrutar(any(), any());
    }

    @Test
    void tiposNoJava_yNombresNoClase_422() throws Exception {
        for (String b : new String[] { "{\"class\":\"BSCL.Plataforma.BD.Servicios::Consultar\"}", "{\"class\":\"SELECT * FROM X\"}", "{\"class\":\"nodots\"}",
                "{\"class\":\"a.B\",\"tipoServicio\":\"P\"}", "{\"class\":\"a.B\",\"tipoServicio\":\"Q\"}" }) {
            llamar(b, T).andExpect(status().isUnprocessableContent()).andExpect(content().string(org.hamcrest.Matchers.containsString("type not routable")));
        }
        when(router.enrutar(any(), any())).thenReturn(new Respuesta(200, "application/json", "{}"));
        llamar("{\"class\":\"a.B\",\"tipoServicio\":\"J\"}", T).andExpect(status().isOk());
    }

    @Test
    void errorDeInfraestructura_noEcoaElMensajeDeLaExcepcion() throws Exception {
        when(router.enrutar(any(), any())).thenThrow(new ResourceAccessException("I/O error on POST http://10.0.0.7:8073/internal/java-call: secret-detail",
                new java.net.SocketTimeoutException("Read timed out")));
        llamar("{\"class\":\"a.B\"}", T).andExpect(status().isGatewayTimeout()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("10.0.0.7"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("correlationId")));
        org.mockito.Mockito.doThrow(new ResourceAccessException("secret-detail", new java.io.IOException("x"))).when(router).enrutar(any(), any());
        llamar("{\"class\":\"a.B\"}", T).andExpect(status().isBadGateway()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret-detail"))));
        org.mockito.Mockito.doThrow(new IllegalStateException("secret-detail")).when(router).enrutar(any(), any());
        llamar("{\"class\":\"a.B\"}", T).andExpect(status().isInternalServerError()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret-detail"))));
    }

    @Test
    void invalidaDelRouter_conSuStatus() throws Exception {
        org.mockito.Mockito.doThrow(new SolicitudJavaCall.Invalida(422, "operation is in-process only and not routable over HTTP")).when(router).enrutar(any(), any());
        llamar("{\"class\":\"cl.bcs.APV.ValidarMandatosPAC.logica\"}", T).andExpect(status().isUnprocessableContent())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("in-process only")));
    }
}
